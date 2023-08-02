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

import org.apache.shardingsphere.distsql.handler.ral.query.DatabaseRequiredQueryableRALExecutor;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetaDataStatement;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Show table meta data executor.
 */
public final class ShowTableMetaDataExecutor implements DatabaseRequiredQueryableRALExecutor<ShowTableMetaDataStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("database_name", "table_name", "type", "name");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowTableMetaDataStatement sqlStatement) {
        String defaultSchema = new DatabaseTypeRegistry(database.getProtocolType()).getDefaultSchemaName(database.getName());
        ShardingSphereSchema schema = database.getSchema(defaultSchema);
        return schema.getAllTableNames().stream().filter(each -> sqlStatement.getTableNames().contains(each))
                .map(each -> buildTableRows(database.getName(), schema, each)).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private Collection<LocalDataQueryResultRow> buildTableRows(final String databaseName, final ShardingSphereSchema schema, final String tableName) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        Collection<LocalDataQueryResultRow> columnRows = schema.getAllColumnNames(tableName).stream().map(each -> buildRow(databaseName, tableName, "COLUMN", each)).collect(Collectors.toList());
        Collection<LocalDataQueryResultRow> indexRows = schema.getTable(tableName).getIndexValues().stream().map(ShardingSphereIndex::getName)
                .map(each -> buildRow(databaseName, tableName, "INDEX", each)).collect(Collectors.toList());
        result.addAll(columnRows);
        result.addAll(indexRows);
        return result;
    }
    
    private LocalDataQueryResultRow buildRow(final String databaseName, final String tableName, final String type, final String name) {
        return new LocalDataQueryResultRow(databaseName, tableName, type, name);
    }
    
    @Override
    public String getType() {
        return ShowTableMetaDataStatement.class.getName();
    }
}
