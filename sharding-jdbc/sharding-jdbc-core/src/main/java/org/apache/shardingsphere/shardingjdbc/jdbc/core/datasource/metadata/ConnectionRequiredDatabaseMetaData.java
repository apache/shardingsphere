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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.metadata;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.WrapperAdapter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Connection required database meta data.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class ConnectionRequiredDatabaseMetaData extends WrapperAdapter implements DatabaseMetaData {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRule shardingRule;
    
    private Connection currentConnection;
    
    @Override
    public final Connection getConnection() throws SQLException {
        return getCurrentConnection();
    }
    
    @Override
    public final ResultSet getSuperTables(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        return getCurrentConnection().getMetaData().getSuperTables(catalog, schemaPattern, tableNamePattern);
    }
    
    @Override
    public final ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) throws SQLException {
        return getCurrentConnection().getMetaData().getColumnPrivileges(catalog, schema, table, columnNamePattern);
    }
    
    @Override
    public final ResultSet getTablePrivileges(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        return getCurrentConnection().getMetaData().getTablePrivileges(catalog, schemaPattern, tableNamePattern);
    }
    
    @Override
    public final ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) throws SQLException {
        return getCurrentConnection().getMetaData().getBestRowIdentifier(catalog, schema, table, scope, nullable);
    }
    
    @Override
    public final ResultSet getVersionColumns(final String catalog, final String schema, final String table) throws SQLException {
        return getCurrentConnection().getMetaData().getVersionColumns(catalog, schema, table);
    }
    
    @Override
    public final ResultSet getPrimaryKeys(final String catalog, final String schema, final String table) throws SQLException {
        return getCurrentConnection().getMetaData().getPrimaryKeys(catalog, schema, table);
    }
    
    @Override
    public final ResultSet getImportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        return getCurrentConnection().getMetaData().getImportedKeys(catalog, schema, table);
    }
    
    @Override
    public final ResultSet getExportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        return getCurrentConnection().getMetaData().getExportedKeys(catalog, schema, table);
    }
    
    @Override
    public final ResultSet getCrossReference(final String parentCatalog,
                                       final String parentSchema, final String parentTable, final String foreignCatalog, final String foreignSchema, final String foreignTable) throws SQLException {
        return getCurrentConnection().getMetaData().getCrossReference(parentCatalog, parentSchema, parentTable, foreignCatalog, foreignSchema, foreignTable);
    }
    
    @Override
    public final ResultSet getTypeInfo() throws SQLException {
        return getCurrentConnection().getMetaData().getTypeInfo();
    }
    
    @Override
    public final ResultSet getIndexInfo(final String catalog, final String schema, final String table, final boolean unique, final boolean approximate) throws SQLException {
        return getCurrentConnection().getMetaData().getIndexInfo(catalog, schema, table, unique, approximate);
    }
    
    @Override
    public final ResultSet getUDTs(final String catalog, final String schemaPattern, final String typeNamePattern, final int[] types) throws SQLException {
        return getCurrentConnection().getMetaData().getUDTs(catalog, schemaPattern, typeNamePattern, types);
    }
    
    @Override
    public final ResultSet getClientInfoProperties() throws SQLException {
        return getCurrentConnection().getMetaData().getClientInfoProperties();
    }
    
    @Override
    public final ResultSet getFunctions(final String catalog, final String schemaPattern, final String functionNamePattern) throws SQLException {
        return getCurrentConnection().getMetaData().getFunctions(catalog, schemaPattern, functionNamePattern);
    }
    
    @Override
    public final ResultSet getFunctionColumns(final String catalog, final String schemaPattern, final String functionNamePattern, final String columnNamePattern) throws SQLException {
        return getCurrentConnection().getMetaData().getFunctionColumns(catalog, schemaPattern, functionNamePattern, columnNamePattern);
    }
    
    @Override
    public final ResultSet getPseudoColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        return getCurrentConnection().getMetaData().getPseudoColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
    }
    
    private Connection getCurrentConnection() throws SQLException {
        if (null == currentConnection || currentConnection.isClosed()) {
            DataSource dataSource = null == shardingRule ? dataSourceMap.values().iterator().next() : dataSourceMap.get(shardingRule.getShardingDataSourceNames().getRandomDataSourceName());
            currentConnection = dataSource.getConnection();
        }
        return currentConnection;
    }
}
