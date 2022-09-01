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

import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetadataStatement;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.QueryableRALBackendHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Show table metadata handler.
 */
public final class ShowTableMetadataHandler extends QueryableRALBackendHandler<ShowTableMetadataStatement> {
    
    private static final String SCHEMA_NAME = "schema_name";
    
    private static final String TABLE_NAME = "table_name";
    
    private static final String TYPE = "type";
    
    private static final String NAME = "name";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(SCHEMA_NAME, TABLE_NAME, TYPE, NAME);
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) {
        String databaseName = getDatabaseName();
        String defaultSchema = DatabaseTypeEngine.getDefaultSchemaName(getConnectionSession().getDatabaseType(), getConnectionSession().getDatabaseName());
        ShardingSphereSchema schema = ProxyContext.getInstance().getDatabase(databaseName).getSchema(defaultSchema);
        return schema.getAllTableNames().stream().filter(each -> getSqlStatement().getTableNames().contains(each))
                .map(each -> buildTableRows(databaseName, schema, each)).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private String getDatabaseName() {
        String result = getSqlStatement().getDatabase().isPresent() ? getSqlStatement().getDatabase().get().getIdentifier().getValue() : getConnectionSession().getDatabaseName();
        if (Strings.isNullOrEmpty(result)) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().databaseExists(result)) {
            throw new UnknownDatabaseException(result);
        }
        return result;
    }
    
    private Collection<LocalDataQueryResultRow> buildTableRows(final String databaseName, final ShardingSphereSchema schema, final String tableName) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        Collection<LocalDataQueryResultRow> columnRows = schema.getAllColumnNames(tableName).stream().map(each -> buildRow(databaseName, tableName, "COLUMN", each)).collect(Collectors.toList());
        Collection<LocalDataQueryResultRow> indexRows = schema.getTable(tableName).getIndexes().values().stream().map(ShardingSphereIndex::getName)
                .map(each -> buildRow(databaseName, tableName, "INDEX", each)).collect(Collectors.toList());
        result.addAll(columnRows);
        result.addAll(indexRows);
        return result;
    }
    
    private LocalDataQueryResultRow buildRow(final String databaseName, final String tableName, final String type, final String name) {
        return new LocalDataQueryResultRow(databaseName, tableName, type, name);
    }
}
