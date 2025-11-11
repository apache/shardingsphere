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

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationDecider;

import java.util.Collection;
import java.util.List;

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
        if (isAllShardingTables(selectStatementContext, tableNames) && isSubqueryAllSameShardingConditions(selectStatementContext, parameters, globalRuleMetaData, database, rule)) {
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
