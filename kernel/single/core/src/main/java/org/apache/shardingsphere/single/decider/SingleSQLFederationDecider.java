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

package org.apache.shardingsphere.single.decider;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.single.constant.SingleOrder;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.statement.core.enums.JoinType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationDecider;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Single SQL federation decider.
 */
public final class SingleSQLFederationDecider implements SQLFederationDecider<SingleRule> {
    
    @Override
    public boolean decide(final SQLStatementContext sqlStatementContext, final List<Object> parameters, final RuleMetaData globalRuleMetaData,
                          final ShardingSphereDatabase database, final SingleRule rule, final Collection<DataNode> includedDataNodes) {
        if (sqlStatementContext instanceof SelectStatementContext) {
            return decide0(sqlStatementContext, database, rule, includedDataNodes);
        } else if (sqlStatementContext instanceof ExplainStatementContext) {
            ExplainStatementContext explainStatementContext = (ExplainStatementContext) sqlStatementContext;
            return decide(explainStatementContext.getExplainableSQLStatementContext(), parameters, globalRuleMetaData, database, rule, includedDataNodes);
        }
        throw new UnsupportedSQLOperationException(String.format("unsupported SQL statement %s in sql federation", sqlStatementContext.getSqlStatement().getClass().getSimpleName()));
    }
    
    private boolean decide0(final SQLStatementContext sqlStatementContext, final ShardingSphereDatabase database, final SingleRule rule, final Collection<DataNode> includedDataNodes) {
        Collection<QualifiedTable> singleTables = getSingleTables(sqlStatementContext, database, rule);
        if (singleTables.isEmpty()) {
            return false;
        }
        if (containsView(database, singleTables)) {
            return true;
        }
        if (!includedDataNodes.isEmpty() && !isInnerCommaJoin(sqlStatementContext.getSqlStatement())) {
            return true;
        }
        boolean result = rule.isAllTablesInSameComputeNode(includedDataNodes, singleTables);
        includedDataNodes.addAll(getTableDataNodes(rule, singleTables));
        return !result;
    }
    
    private boolean isInnerCommaJoin(final SQLStatement sqlStatement) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return true;
        }
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        if (!selectStatement.getFrom().isPresent() || !(selectStatement.getFrom().get() instanceof JoinTableSegment)) {
            return true;
        }
        JoinTableSegment joinTableSegment = (JoinTableSegment) selectStatement.getFrom().get();
        return JoinType.INNER.name().equalsIgnoreCase(joinTableSegment.getJoinType()) || JoinType.COMMA.name().equalsIgnoreCase(joinTableSegment.getJoinType());
    }
    
    private Collection<QualifiedTable> getSingleTables(final SQLStatementContext sqlStatementContext, final ShardingSphereDatabase database, final SingleRule rule) {
        Collection<QualifiedTable> qualifiedTables = rule.getQualifiedTables(sqlStatementContext, database);
        return rule.getSingleTables(qualifiedTables);
    }
    
    private boolean containsView(final ShardingSphereDatabase database, final Collection<QualifiedTable> singleTables) {
        for (QualifiedTable each : singleTables) {
            if (database.getSchema(each.getSchemaName()).containsView(each.getTableName())) {
                return true;
            }
        }
        return false;
    }
    
    private Collection<DataNode> getTableDataNodes(final SingleRule rule, final Collection<QualifiedTable> singleTables) {
        Collection<DataNode> result = new HashSet<>(singleTables.size(), 1F);
        for (QualifiedTable each : singleTables) {
            rule.getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).findTableDataNode(each.getSchemaName(), each.getTableName()).ifPresent(result::add);
        }
        return result;
    }
    
    @Override
    public int getOrder() {
        return SingleOrder.ORDER;
    }
    
    @Override
    public Class<SingleRule> getTypeClass() {
        return SingleRule.class;
    }
}
