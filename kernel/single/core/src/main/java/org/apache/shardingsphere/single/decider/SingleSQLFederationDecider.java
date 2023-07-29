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

import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.single.constant.SingleOrder;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationDecider;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Single SQL federation decider.
 */
public final class SingleSQLFederationDecider implements SQLFederationDecider<SingleRule> {
    
    @Override
    public boolean decide(final SelectStatementContext selectStatementContext, final List<Object> parameters,
                          final ShardingSphereRuleMetaData globalRuleMetaData, final ShardingSphereDatabase database, final SingleRule rule, final Collection<DataNode> includedDataNodes) {
        Collection<QualifiedTable> singleTables = getSingleTables(selectStatementContext, database, rule);
        if (singleTables.isEmpty()) {
            return false;
        }
        if (containsView(database, singleTables)) {
            return true;
        }
        boolean isAllTablesInSameComputeNode = rule.isAllTablesInSameComputeNode(includedDataNodes, singleTables);
        includedDataNodes.addAll(getTableDataNodes(rule, singleTables));
        return !isAllTablesInSameComputeNode;
    }
    
    private Collection<QualifiedTable> getSingleTables(final SelectStatementContext selectStatementContext, final ShardingSphereDatabase database, final SingleRule rule) {
        Collection<QualifiedTable> qualifiedTables = rule.getQualifiedTables(selectStatementContext, database);
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
        Collection<DataNode> result = new HashSet<>();
        for (QualifiedTable each : singleTables) {
            rule.findTableDataNode(each.getSchemaName(), each.getTableName()).ifPresent(result::add);
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
