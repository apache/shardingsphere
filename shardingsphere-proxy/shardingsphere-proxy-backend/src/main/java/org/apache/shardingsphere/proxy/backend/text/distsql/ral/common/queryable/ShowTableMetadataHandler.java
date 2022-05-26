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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.ShowTableMetadataStatement;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.exception.DatabaseNotExistedException;
import org.apache.shardingsphere.infra.metadata.database.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.IndexMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Show table metadata handler.
 */
public final class ShowTableMetadataHandler extends QueryableRALBackendHandler<ShowTableMetadataStatement, ShowTableMetadataHandler> {
    
    private static final String SCHEMA_NAME = "schema_name";
    
    private static final String TABLE_NAME = "table_name";
    
    private static final String TYPE = "type";
    
    private static final String NAME = "name";
    
    private ConnectionSession connectionSession;
    
    @Override
    public ShowTableMetadataHandler init(final HandlerParameter<ShowTableMetadataStatement> parameter) {
        connectionSession = parameter.getConnectionSession();
        return super.init(parameter);
    }
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(SCHEMA_NAME, TABLE_NAME, TYPE, NAME);
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) {
        String databaseName = getDatabaseName();
        String defaultSchema = DatabaseTypeEngine.getDefaultSchemaName(connectionSession.getDatabaseType(), connectionSession.getDatabaseName());
        ShardingSphereSchema schema = ProxyContext.getInstance().getDatabase(databaseName).getSchemas().get(defaultSchema);
        return schema.getAllTableNames().stream().filter(each -> sqlStatement.getTableNames().contains(each))
                .map(each -> buildTableRows(databaseName, schema, each)).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private String getDatabaseName() {
        String result = sqlStatement.getDatabase().isPresent() ? sqlStatement.getDatabase().get().getIdentifier().getValue() : connectionSession.getDatabaseName();
        if (Strings.isNullOrEmpty(result)) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().getAllDatabaseNames().contains(result)) {
            throw new DatabaseNotExistedException(result);
        }
        return result;
    }
    
    private Collection<List<Object>> buildTableRows(final String databaseName, final ShardingSphereSchema schema, final String tableName) {
        Collection<List<Object>> result = new LinkedList<>();
        Collection<List<Object>> columnRows = schema.getAllColumnNames(tableName).stream().map(each -> buildRow(databaseName, tableName, "COLUMN", each))
                .collect(Collectors.toList());
        Collection<List<Object>> indexRows = schema.getTables().get(tableName).getIndexes().values().stream().map(IndexMetaData::getName)
                .map(each -> buildRow(databaseName, tableName, "INDEX", each)).collect(Collectors.toList());
        result.addAll(columnRows);
        result.addAll(indexRows);
        return result;
    }
    
    private List<Object> buildRow(final String databaseName, final String tableName, final String type, final String name) {
        return new ArrayList<>(Arrays.asList(databaseName, tableName, type, name));
    }
}
