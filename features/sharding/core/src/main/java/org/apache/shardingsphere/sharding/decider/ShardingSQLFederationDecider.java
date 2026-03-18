/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sharding.decider;

import com.cedarsoftware.util.CaseInsensitiveSet;
import com.google.common.base.Joiner;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.value.AlwaysFalseShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.BindingTableCheckedConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sharding.rule.checker.ShardingRuleChecker;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationDecider;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Sharding SQL federation decider.
 */
@HighFrequencyInvocation
public final class ShardingSQLFederationDecider implements SQLFederationDecider<ShardingRule> {
    
    @Override
    public boolean decide(final SQLStatementContext sqlStatementContext, final List<Object> parameters,
                          final RuleMetaData globalRuleMetaData, final ShardingSphereDatabase database, final ShardingRule rule, final Collection<DataNode> includedDataNodes) {
        if (sqlStatementContext instanceof SelectStatementContext) {
            return decide0((SelectStatementContext) sqlStatementContext, parameters, globalRuleMetaData, database, rule, includedDataNodes);
        } else if (sqlStatementContext instanceof ExplainStatementContext) {
            ExplainStatementContext explainStatementContext = (ExplainStatementContext) sqlStatementContext;
            return decide(explainStatementContext.getExplainableSQLStatementContext(), parameters, globalRuleMetaData, database, rule, includedDataNodes);
        }
        throw new UnsupportedSQLOperationException(String.format("unsupported SQL statement %s in sql federation", sqlStatementContext.getSqlStatement().getClass().getSimpleName()));
    }
    
    private boolean decide0(final SelectStatementContext selectStatementContext, final List<Object> parameters, final RuleMetaData globalRuleMetaData, final ShardingSphereDatabase database,
                            final ShardingRule rule, final Collection<DataNode> includedDataNodes) {
        Collection<String> tableNames = rule.getShardingLogicTableNames(selectStatementContext.getTablesContext().getTableNames());
        if (tableNames.isEmpty()) {
            return false;
        }
        appendTableDataNodes(rule, database, tableNames, includedDataNodes);
        boolean allShardingTables = isAllShardingTables(selectStatementContext, tableNames);
        if (allShardingTables && isSubqueryAllSameShardingConditions(selectStatementContext, parameters, globalRuleMetaData, database, rule)) {
            return false;
        }
        if (allShardingTables && isSingleOrJoinWithSameEqualityShardingCondition(selectStatementContext, parameters, globalRuleMetaData, database, rule, tableNames)) {
            return false;
        }
        if (selectStatementContext.isContainsSubquery() || selectStatementContext.isContainsHaving()
                || selectStatementContext.isContainsCombine() || selectStatementContext.isContainsPartialDistinctAggregation()) {
            return true;
        }
        if (!selectStatementContext.isContainsJoinQuery() || rule.isAllTablesInSameDataSource(tableNames)) {
            return false;
        }
        if (isSelfJoinWithoutShardingColumn(selectStatementContext, rule, tableNames)) {
            return true;
        }
        return tableNames.size() > 1 && !rule.isBindingTablesUseShardingColumnsJoin(selectStatementContext, tableNames);
    }
    
    private boolean isSingleOrJoinWithSameEqualityShardingCondition(final SelectStatementContext selectStatementContext, final List<Object> parameters, final RuleMetaData globalRuleMetaData,
                                                                    final ShardingSphereDatabase database, final ShardingRule rule, final Collection<String> tableNames) {
        if (selectStatementContext.isContainsSubquery() || selectStatementContext.isContainsCombine()) {
            return false;
        }
        // TODO consider supporting JOIN optimization when config database and table sharding strategy @duanzhengqiang
        if (isConfigDatabaseAndTableShardingStrategy(tableNames, rule)) {
            return false;
        }
        ShardingConditions shardingConditions = createShardingConditions(selectStatementContext, parameters, globalRuleMetaData, database, rule);
        shardingConditions.merge();
        if (!shardingConditions.isSameShardingCondition()) {
            return false;
        }
        if (!isAllEqualitySameShardingValues(shardingConditions, tableNames)) {
            return false;
        }
        if (1 == tableNames.size() && !selectStatementContext.isContainsJoinQuery()) {
            return true;
        }
        Collection<ShardingTableReferenceRuleConfiguration> bindingTableGroups = Collections.singleton(new ShardingTableReferenceRuleConfiguration("", Joiner.on(",").join(tableNames)));
        BindingTableCheckedConfiguration configuration = new BindingTableCheckedConfiguration(rule.getDataSourceNames(), rule.getShardingAlgorithms(), rule.getConfiguration().getShardingAlgorithms(),
                bindingTableGroups, rule.getDefaultDatabaseShardingStrategyConfig(), rule.getDefaultTableShardingStrategyConfig(), rule.getDefaultShardingColumn());
        return new ShardingRuleChecker(rule).isValidBindingTableConfiguration(rule.getShardingTables(), configuration);
    }
    
    private boolean isConfigDatabaseAndTableShardingStrategy(final Collection<String> tableNames, final ShardingRule rule) {
        for (String each : tableNames) {
            Optional<ShardingTable> shardingTable = rule.findShardingTable(each);
            if (!shardingTable.isPresent()) {
                continue;
            }
            boolean isConfigDatabaseShardingStrategy = !(rule.getDatabaseShardingStrategyConfiguration(shardingTable.get()) instanceof NoneShardingStrategyConfiguration);
            boolean isConfigTableShardingStrategy = !(rule.getTableShardingStrategyConfiguration(shardingTable.get()) instanceof NoneShardingStrategyConfiguration);
            if (isConfigDatabaseShardingStrategy && isConfigTableShardingStrategy) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isAllEqualitySameShardingValues(final ShardingConditions shardingConditions, final Collection<String> tableNames) {
        Object sampleValue = null;
        Collection<String> shardingTableNames = new CaseInsensitiveSet<>(tableNames);
        for (ShardingCondition each : shardingConditions.getConditions()) {
            for (ShardingConditionValue value : each.getValues()) {
                if (value instanceof RangeShardingConditionValue || value instanceof AlwaysFalseShardingConditionValue) {
                    return false;
                }
                if (value instanceof ListShardingConditionValue) {
                    ListShardingConditionValue<?> values = (ListShardingConditionValue<?>) value;
                    if (1 != values.getValues().size()) {
                        return false;
                    }
                    Object currentValue = values.getValues().iterator().next();
                    if (null == sampleValue) {
                        sampleValue = currentValue;
                    } else if (!sampleValue.equals(currentValue)) {
                        return false;
                    }
                    shardingTableNames.remove(value.getTableName());
                }
            }
        }
        return shardingTableNames.isEmpty();
    }
    
    private boolean isSubqueryAllSameShardingConditions(final SelectStatementContext selectStatementContext, final List<Object> parameters, final RuleMetaData globalRuleMetaData,
                                                        final ShardingSphereDatabase database, final ShardingRule rule) {
        if (!selectStatementContext.isContainsSubquery()) {
            return false;
        }
        if (selectStatementContext.isContainsCombine()) {
            return false;
        }
        ShardingConditions shardingConditions = createShardingConditions(selectStatementContext, parameters, globalRuleMetaData, database, rule);
        return shardingConditions.isSameShardingCondition();
    }
    
    private boolean isAllShardingTables(final SelectStatementContext selectStatementContext, final Collection<String> tableNames) {
        return tableNames.size() == selectStatementContext.getTablesContext().getTableNames().size();
    }
    
    private ShardingConditions createShardingConditions(final SelectStatementContext selectStatementContext, final List<Object> parameters, final RuleMetaData globalRuleMetaData,
                                                        final ShardingSphereDatabase database, final ShardingRule rule) {
        List<ShardingCondition> shardingConditions = new ShardingConditionEngine(globalRuleMetaData, database, rule).createShardingConditions(selectStatementContext, parameters);
        ShardingConditions result = new ShardingConditions(shardingConditions, selectStatementContext, rule);
        if (result.isNeedMerge()) {
            result.merge();
        }
        return result;
    }
    
    private boolean isSelfJoinWithoutShardingColumn(final SelectStatementContext selectStatementContext, final ShardingRule rule, final Collection<String> tableNames) {
        return 1 == tableNames.size() && selectStatementContext.isContainsJoinQuery() && !rule.isBindingTablesUseShardingColumnsJoin(selectStatementContext, tableNames);
    }
    
    private void appendTableDataNodes(final ShardingRule rule, final ShardingSphereDatabase database, final Collection<String> tableNames, final Collection<DataNode> includedDataNodes) {
        DataNodes dataNodes = new DataNodes(database.getRuleMetaData().getRules());
        for (String each : tableNames) {
            rule.findShardingTable(each).ifPresent(optional -> includedDataNodes.addAll(dataNodes.getDataNodes(each)));
        }
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRule> getTypeClass() {
        return ShardingRule.class;
    }
}
