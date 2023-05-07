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

import org.apache.shardingsphere.infra.binder.decider.SQLFederationDecider;
import org.apache.shardingsphere.infra.binder.decider.SQLFederationDeciderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.List;

/**
 * Sharding SQL federation decider.
 */
public final class ShardingSQLFederationDecider implements SQLFederationDecider<ShardingRule> {
    
    @Override
    public void decide(final SQLFederationDeciderContext deciderContext, final SelectStatementContext selectStatementContext, final List<Object> parameters,
                       final ShardingSphereRuleMetaData globalRuleMetaData, final ShardingSphereDatabase database, final ShardingRule rule, final ConfigurationProperties props) {
        Collection<String> tableNames = rule.getShardingLogicTableNames(selectStatementContext.getTablesContext().getTableNames());
        if (tableNames.isEmpty()) {
            return;
        }
        addTableDataNodes(deciderContext, rule, tableNames);
        ShardingConditions shardingConditions = getMergedShardingConditions(selectStatementContext, parameters, globalRuleMetaData, database, rule);
        if (shardingConditions.isNeedMerge() && shardingConditions.isSameShardingCondition()) {
            return;
        }
        if (selectStatementContext.isContainsSubquery() || selectStatementContext.isContainsHaving()
                || selectStatementContext.isContainsCombine() || selectStatementContext.isContainsPartialDistinctAggregation()) {
            deciderContext.setUseSQLFederation(true);
            return;
        }
        if (!selectStatementContext.isContainsJoinQuery() || rule.isAllTablesInSameDataSource(tableNames)) {
            return;
        }
        boolean allBindingTables = tableNames.size() > 1 && rule.isAllBindingTables(database, selectStatementContext, tableNames);
        deciderContext.setUseSQLFederation(tableNames.size() > 1 && !allBindingTables);
    }
    
    private static void addTableDataNodes(final SQLFederationDeciderContext deciderContext, final ShardingRule rule, final Collection<String> tableNames) {
        for (String each : tableNames) {
            rule.findTableRule(each).ifPresent(optional -> deciderContext.getDataNodes().addAll(optional.getActualDataNodes()));
        }
    }
    
    private static ShardingConditions getMergedShardingConditions(final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters,
                                                                  final ShardingSphereRuleMetaData globalRuleMetaData, final ShardingSphereDatabase database, final ShardingRule rule) {
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
