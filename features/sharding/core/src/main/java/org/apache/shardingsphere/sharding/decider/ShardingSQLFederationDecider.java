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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationDecider;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Sharding SQL federation decider.
 */
public final class ShardingSQLFederationDecider implements SQLFederationDecider<ShardingRule> {
    
    @Override
    public boolean decide(final SelectStatementContext selectStatementContext, final List<Object> parameters,
                          final RuleMetaData globalRuleMetaData, final ShardingSphereDatabase database, final ShardingRule rule, final Collection<DataNode> includedDataNodes) {
        Collection<String> tableNames = rule.getShardingLogicTableNames(selectStatementContext.getTablesContext().getTableNames());
        if (tableNames.isEmpty()) {
            return false;
        }
        includedDataNodes.addAll(getTableDataNodes(rule, tableNames));
        ShardingConditions shardingConditions = getMergedShardingConditions(selectStatementContext, parameters, globalRuleMetaData, database, rule);
        if (shardingConditions.isNeedMerge() && shardingConditions.isSameShardingCondition()) {
            return false;
        }
        if (selectStatementContext.isContainsSubquery() || selectStatementContext.isContainsHaving()
                || selectStatementContext.isContainsCombine() || selectStatementContext.isContainsPartialDistinctAggregation()) {
            return true;
        }
        if (!selectStatementContext.isContainsJoinQuery() || rule.isAllTablesInSameDataSource(tableNames)) {
            return false;
        }
        return tableNames.size() > 1 && !rule.isAllBindingTables(database, selectStatementContext, tableNames);
    }
    
    private Collection<DataNode> getTableDataNodes(final ShardingRule rule, final Collection<String> tableNames) {
        Collection<DataNode> result = new HashSet<>();
        for (String each : tableNames) {
            rule.findTableRule(each).ifPresent(optional -> result.addAll(optional.getActualDataNodes()));
        }
        return result;
    }
    
    private ShardingConditions getMergedShardingConditions(final SQLStatementContext sqlStatementContext, final List<Object> parameters,
                                                           final RuleMetaData globalRuleMetaData, final ShardingSphereDatabase database, final ShardingRule rule) {
        List<ShardingCondition> shardingConditions = new ShardingConditionEngine(
                globalRuleMetaData, database, rule).createShardingConditions(sqlStatementContext, parameters);
        ShardingConditions result = new ShardingConditions(shardingConditions, sqlStatementContext, rule);
        if (result.isNeedMerge()) {
            result.merge();
        }
        return result;
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
