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
import org.apache.shardingsphere.infra.binder.context.statement.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationDecider;

import java.util.Collection;
import java.util.HashSet;
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
            return decide0((SelectStatementContext) sqlStatementContext, database, rule, includedDataNodes);
        } else if (sqlStatementContext instanceof ExplainStatementContext) {
            ExplainStatementContext explainStatementContext = (ExplainStatementContext) sqlStatementContext;
            ShardingSpherePreconditions.checkState(explainStatementContext.getSqlStatementContext() instanceof SelectStatementContext,
                    () -> new UnsupportedSQLOperationException(String.format("unsupported Explain %s statement in sql federation", explainStatementContext.getSqlStatement().getSqlStatement())));
            return decide0((SelectStatementContext) explainStatementContext.getSqlStatementContext(), database, rule, includedDataNodes);
        }
        throw new UnsupportedSQLOperationException(String.format("unsupported SQL statement %s in sql federation", sqlStatementContext.getSqlStatement()));
    }
    
    private boolean decide0(final SelectStatementContext selectStatementContext, final ShardingSphereDatabase database, final ShardingRule rule, final Collection<DataNode> includedDataNodes) {
        Collection<String> tableNames = rule.getShardingLogicTableNames(selectStatementContext.getTablesContext().getTableNames());
        if (tableNames.isEmpty()) {
            return false;
        }
        includedDataNodes.addAll(getTableDataNodes(rule, database, tableNames));
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
    
    private boolean isSelfJoinWithoutShardingColumn(final SelectStatementContext selectStatementContext, final ShardingRule rule, final Collection<String> tableNames) {
        return 1 == tableNames.size() && selectStatementContext.isContainsJoinQuery() && !rule.isBindingTablesUseShardingColumnsJoin(selectStatementContext, tableNames);
    }
    
    private Collection<DataNode> getTableDataNodes(final ShardingRule rule, final ShardingSphereDatabase database, final Collection<String> tableNames) {
        Collection<DataNode> result = new HashSet<>();
        for (String each : tableNames) {
            rule.findShardingTable(each).ifPresent(optional -> result.addAll(new DataNodes(database.getRuleMetaData().getRules()).getDataNodes(each)));
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
