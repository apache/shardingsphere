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

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.decider.SQLFederationDecider;
import org.apache.shardingsphere.infra.binder.decider.context.SQLFederationDeciderContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngineFactory;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.List;

/**
 * Sharding SQL federation decider.
 */
public final class ShardingSQLFederationDecider implements SQLFederationDecider<ShardingRule> {
    
    @Override
    public void decide(final SQLFederationDeciderContext deciderContext, final QueryContext queryContext,
                       final ShardingSphereDatabase database, final ShardingRule rule, final ConfigurationProperties props) {
        SelectStatementContext select = (SelectStatementContext) queryContext.getSqlStatementContext();
        Collection<String> tableNames = rule.getShardingLogicTableNames(select.getTablesContext().getTableNames());
        if (tableNames.isEmpty()) {
            return;
        }
        addTableDataNodes(deciderContext, rule, tableNames);
        ShardingConditions shardingConditions = createShardingConditions(queryContext, database, rule);
        // TODO remove this judge logic when we support issue#21392 
        if (select.getPaginationContext().isHasPagination() && !(select.getDatabaseType() instanceof PostgreSQLDatabaseType) && !(select.getDatabaseType() instanceof OpenGaussDatabaseType)) {
            return;
        }
        if (shardingConditions.isNeedMerge() && shardingConditions.isSameShardingCondition()) {
            return;
        }
        if (select.isContainsSubquery() || select.isContainsHaving() || select.isContainsCombine() || select.isContainsPartialDistinctAggregation()) {
            deciderContext.setUseSQLFederation(true);
            return;
        }
        if (!select.isContainsJoinQuery() || rule.isAllTablesInSameDataSource(tableNames)) {
            return;
        }
        boolean allBindingTables = tableNames.size() > 1 && rule.isAllBindingTables(database, select, tableNames);
        deciderContext.setUseSQLFederation(tableNames.size() > 1 && !allBindingTables);
    }
    
    private static void addTableDataNodes(final SQLFederationDeciderContext deciderContext, final ShardingRule rule, final Collection<String> tableNames) {
        for (String each : tableNames) {
            rule.findTableRule(each).ifPresent(optional -> deciderContext.getDataNodes().addAll(optional.getActualDataNodes()));
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ShardingConditions createShardingConditions(final QueryContext queryContext, final ShardingSphereDatabase database, final ShardingRule rule) {
        ShardingConditionEngine shardingConditionEngine = ShardingConditionEngineFactory.createShardingConditionEngine(queryContext, database, rule);
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(queryContext.getSqlStatementContext(), queryContext.getParameters());
        return new ShardingConditions(shardingConditions, queryContext.getSqlStatementContext(), rule);
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
