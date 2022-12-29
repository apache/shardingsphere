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

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.decider.SQLFederationDecider;
import org.apache.shardingsphere.infra.binder.decider.context.SQLFederationDeciderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.type.IndexAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtil;
import org.apache.shardingsphere.single.constant.SingleOrder;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Single SQL federation decider.
 */
public final class SingleSQLFederationDecider implements SQLFederationDecider<SingleRule> {
    
    @Override
    public void decide(final SQLFederationDeciderContext deciderContext, final QueryContext queryContext,
                       final ShardingSphereDatabase database, final SingleRule rule, final ConfigurationProperties props) {
        SelectStatementContext select = (SelectStatementContext) queryContext.getSqlStatementContext();
        Collection<QualifiedTable> singleTableNames = getSingleTableNames(select, database, rule);
        if (singleTableNames.isEmpty()) {
            return;
        }
        if (containsView(database, singleTableNames)) {
            deciderContext.setUseSQLFederation(true);
            return;
        }
        deciderContext.setUseSQLFederation(!isAllTablesInSameDataSource(deciderContext, rule, singleTableNames));
        addTableDataNodes(deciderContext, rule, singleTableNames);
    }
    
    private static void addTableDataNodes(final SQLFederationDeciderContext deciderContext, final SingleRule rule, final Collection<QualifiedTable> singleTableNames) {
        for (QualifiedTable each : singleTableNames) {
            rule.findSingleTableDataNode(each.getSchemaName(), each.getTableName()).ifPresent(optional -> deciderContext.getDataNodes().add(optional));
        }
    }
    
    private static boolean isAllTablesInSameDataSource(final SQLFederationDeciderContext deciderContext, final SingleRule rule, final Collection<QualifiedTable> singleTableNames) {
        if (!rule.isSingleTablesInSameDataSource(singleTableNames)) {
            return false;
        }
        QualifiedTable sampleTable = singleTableNames.iterator().next();
        Optional<DataNode> dataNode = rule.findSingleTableDataNode(sampleTable.getSchemaName(), sampleTable.getTableName());
        if (!dataNode.isPresent()) {
            return true;
        }
        for (DataNode each : deciderContext.getDataNodes()) {
            if (!each.getDataSourceName().equalsIgnoreCase(dataNode.get().getDataSourceName())) {
                return false;
            }
        }
        return true;
    }
    
    private static Collection<QualifiedTable> getSingleTableNames(final SQLStatementContext<?> sqlStatementContext,
                                                                  final ShardingSphereDatabase database, final SingleRule rule) {
        DatabaseType databaseType = sqlStatementContext.getDatabaseType();
        Collection<QualifiedTable> result = getQualifiedTables(database, databaseType, sqlStatementContext.getTablesContext().getTables());
        if (result.isEmpty() && sqlStatementContext instanceof IndexAvailable) {
            result = IndexMetaDataUtil.getTableNames(database, databaseType, ((IndexAvailable) sqlStatementContext).getIndexes());
        }
        return rule.getSingleTableNames(result);
    }
    
    private static Collection<QualifiedTable> getQualifiedTables(final ShardingSphereDatabase database, final DatabaseType databaseType, final Collection<SimpleTableSegment> tableSegments) {
        Collection<QualifiedTable> result = new LinkedList<>();
        String schemaName = DatabaseTypeEngine.getDefaultSchemaName(databaseType, database.getName());
        for (SimpleTableSegment each : tableSegments) {
            String actualSchemaName = each.getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(schemaName);
            result.add(new QualifiedTable(actualSchemaName, each.getTableName().getIdentifier().getValue()));
        }
        return result;
    }
    
    private boolean containsView(final ShardingSphereDatabase database, final Collection<QualifiedTable> singleTableNames) {
        for (QualifiedTable each : singleTableNames) {
            if (database.getSchema(each.getSchemaName()).containsView(each.getTableName())) {
                return true;
            }
        }
        return false;
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
