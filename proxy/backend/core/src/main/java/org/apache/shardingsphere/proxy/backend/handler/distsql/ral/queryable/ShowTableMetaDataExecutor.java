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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.type.ral.query.DatabaseAwareQueryableRALExecutor;
import org.apache.shardingsphere.distsql.statement.ral.queryable.ShowTableMetaDataStatement;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.json.JsonUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Show table meta data executor.
 */
@Setter
public final class ShowTableMetaDataExecutor implements DatabaseAwareQueryableRALExecutor<ShowTableMetaDataStatement> {
    
    private ShardingSphereDatabase currentDatabase;
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("database_name", "table_name", "type", "name", "value");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowTableMetaDataStatement sqlStatement, final ShardingSphereMetaData metaData) {
        String defaultSchema = new DatabaseTypeRegistry(currentDatabase.getProtocolType()).getDefaultSchemaName(currentDatabase.getName());
        ShardingSphereSchema schema = currentDatabase.getSchema(defaultSchema);
        return sqlStatement.getTableNames().stream().filter(each -> schema.getAllTableNames().contains(each.toLowerCase()))
                .map(each -> buildTableRows(currentDatabase.getName(), schema, each.toLowerCase())).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private Collection<LocalDataQueryResultRow> buildTableRows(final String databaseName, final ShardingSphereSchema schema, final String tableName) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        ShardingSphereTable table = schema.getTable(tableName);
        result.addAll(table.getColumnValues().stream().map(each -> buildColumnRow(databaseName, tableName, each)).collect(Collectors.toList()));
        result.addAll(table.getIndexValues().stream().map(each -> buildIndexRow(databaseName, tableName, each)).collect(Collectors.toList()));
        return result;
    }
    
    private LocalDataQueryResultRow buildColumnRow(final String databaseName, final String tableName, final ShardingSphereColumn column) {
        return new LocalDataQueryResultRow(databaseName, tableName, "COLUMN", column.getName(), JsonUtils.toJsonString(column));
    }
    
    private LocalDataQueryResultRow buildIndexRow(final String databaseName, final String tableName, final ShardingSphereIndex index) {
        return new LocalDataQueryResultRow(databaseName, tableName, "INDEX", index.getName(), JsonUtils.toJsonString(index));
    }
    
    @Override
    public Class<ShowTableMetaDataStatement> getType() {
        return ShowTableMetaDataStatement.class;
    }
}
