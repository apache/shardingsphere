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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.export;

import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowTableMetaDataStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Show table meta data executor.
 */
@Setter
public final class ShowTableMetaDataExecutor implements DistSQLQueryExecutor<ShowTableMetaDataStatement>, DistSQLExecutorDatabaseAware {
    
    private ShardingSphereDatabase database;
    
    @Override
    public Collection<String> getColumnNames(final ShowTableMetaDataStatement sqlStatement) {
        return Arrays.asList("database_name", "table_name", "type", "name", "value");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowTableMetaDataStatement sqlStatement, final ContextManager contextManager) {
        String defaultSchema = new DatabaseTypeRegistry(database.getProtocolType()).getDefaultSchemaName(database.getName());
        ShardingSphereSchema schema = database.getSchema(defaultSchema);
        return sqlStatement.getTableNames().stream()
                .filter(schema::containsTable).map(each -> buildTableRows(database.getName(), schema, each)).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private Collection<LocalDataQueryResultRow> buildTableRows(final String databaseName, final ShardingSphereSchema schema, final String tableName) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        ShardingSphereTable table = schema.getTable(tableName);
        result.addAll(table.getAllColumns().stream().map(each -> buildColumnRow(databaseName, tableName, each)).collect(Collectors.toList()));
        result.addAll(table.getAllIndexes().stream().map(each -> buildIndexRow(databaseName, tableName, each)).collect(Collectors.toList()));
        return result;
    }
    
    private LocalDataQueryResultRow buildColumnRow(final String databaseName, final String tableName, final ShardingSphereColumn column) {
        return new LocalDataQueryResultRow(databaseName, tableName, "COLUMN", column.getName(), column);
    }
    
    private LocalDataQueryResultRow buildIndexRow(final String databaseName, final String tableName, final ShardingSphereIndex index) {
        return new LocalDataQueryResultRow(databaseName, tableName, "INDEX", index.getName(), index);
    }
    
    @Override
    public Class<ShowTableMetaDataStatement> getType() {
        return ShowTableMetaDataStatement.class;
    }
}
