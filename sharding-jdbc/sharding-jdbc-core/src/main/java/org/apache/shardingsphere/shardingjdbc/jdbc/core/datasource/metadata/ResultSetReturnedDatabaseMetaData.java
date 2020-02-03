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

import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.WrapperAdapter;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.DatabaseMetaDataResultSet;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * {@code ResultSet} returned database meta data.
 *
 * @author yangyi
 */
public abstract class ResultSetReturnedDatabaseMetaData extends WrapperAdapter implements DatabaseMetaData {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRule shardingRule;
    
    private Connection currentConnection;
    
    private String currentDataSourceName;
    
    public ResultSetReturnedDatabaseMetaData(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) {
        this.dataSourceMap = dataSourceMap;
        this.shardingRule = shardingRule;
    }
    
    @Override
    public final Connection getConnection() throws SQLException {
        return getCurrentConnection();
    }
    
    @Override
    public final ResultSet getSuperTypes(final String catalog, final String schemaPattern, final String typeNamePattern) throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getSuperTypes(catalog, schemaPattern, typeNamePattern), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getSuperTables(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        String actualTableNamePattern = getActualTableNamePattern(tableNamePattern);
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getSuperTables(catalog, schemaPattern, actualTableNamePattern), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getAttributes(final String catalog, final String schemaPattern, final String typeNamePattern, final String attributeNamePattern) throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getAttributes(catalog, schemaPattern, typeNamePattern, attributeNamePattern), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getProcedures(catalog, schemaPattern, procedureNamePattern), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getProcedureColumns(final String catalog, final String schemaPattern, final String procedureNamePattern, final String columnNamePattern) throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getTables(final String catalog, final String schemaPattern, final String tableNamePattern, final String[] types) throws SQLException {
        String actualTableNamePattern = getActualTableNamePattern(tableNamePattern);
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getTables(catalog, schemaPattern, actualTableNamePattern, types), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getSchemas() throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getSchemas(), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getSchemas(final String catalog, final String schemaPattern) throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getSchemas(catalog, schemaPattern), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getCatalogs() throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getCatalogs(), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getTableTypes() throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getTableTypes(), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        String actualTableNamePattern = getActualTableNamePattern(tableNamePattern);
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getColumns(catalog, schemaPattern, actualTableNamePattern, columnNamePattern), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) throws SQLException {
        String actualTable = getActualTable(table);
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getColumnPrivileges(catalog, schema, actualTable, columnNamePattern), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getTablePrivileges(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        String actualTableNamePattern = getActualTableNamePattern(tableNamePattern);
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getTablePrivileges(catalog, schemaPattern, actualTableNamePattern), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) throws SQLException {
        String actualTable = getActualTable(table);
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getBestRowIdentifier(catalog, schema, actualTable, scope, nullable), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getVersionColumns(final String catalog, final String schema, final String table) throws SQLException {
        String actualTable = getActualTable(table);
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getVersionColumns(catalog, schema, actualTable), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getPrimaryKeys(final String catalog, final String schema, final String table) throws SQLException {
        String actualTable = getActualTable(table);
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getPrimaryKeys(catalog, schema, actualTable), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getImportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        String actualTable = getActualTable(table);
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getImportedKeys(catalog, schema, actualTable), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getExportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        String actualTable = getActualTable(table);
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getExportedKeys(catalog, schema, actualTable), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getCrossReference(final String parentCatalog,
        final String parentSchema, final String parentTable, final String foreignCatalog, final String foreignSchema, final String foreignTable) throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getCrossReference(parentCatalog, parentSchema, parentTable, foreignCatalog, foreignSchema, foreignTable), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getTypeInfo() throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getTypeInfo(), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getIndexInfo(final String catalog, final String schema, final String table, final boolean unique, final boolean approximate) throws SQLException {
        String actualTable = getActualTable(table);
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getIndexInfo(catalog, schema, actualTable, unique, approximate), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getUDTs(final String catalog, final String schemaPattern, final String typeNamePattern, final int[] types) throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getUDTs(catalog, schemaPattern, typeNamePattern, types), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getClientInfoProperties() throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getClientInfoProperties(), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getFunctions(final String catalog, final String schemaPattern, final String functionNamePattern) throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getFunctions(catalog, schemaPattern, functionNamePattern), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getFunctionColumns(final String catalog, final String schemaPattern, final String functionNamePattern, final String columnNamePattern) throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getFunctionColumns(catalog, schemaPattern, functionNamePattern, columnNamePattern), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getPseudoColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        String actualTableNamePattern = getActualTableNamePattern(tableNamePattern);
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getPseudoColumns(catalog, schemaPattern, actualTableNamePattern, columnNamePattern), shardingRule);
        }
    }
    
    private Connection getCurrentConnection() throws SQLException {
        if (null == currentConnection || currentConnection.isClosed()) {
            DataSource dataSource = null == shardingRule ? dataSourceMap.values().iterator().next() : dataSourceMap.get(getCurrentDataSourceName());
            currentConnection = dataSource.getConnection();
        }
        return currentConnection;
    }
    
    private String getCurrentDataSourceName() {
        currentDataSourceName = null == currentDataSourceName ? shardingRule.getShardingDataSourceNames().getRandomDataSourceName() : currentDataSourceName;
        return shardingRule.getShardingDataSourceNames().getRawMasterDataSourceName(currentDataSourceName);
    }
    
    private String getActualTableNamePattern(final String tableNamePattern) {
        if (null == tableNamePattern || null == shardingRule) {
            return tableNamePattern;
        }
        return shardingRule.findTableRule(tableNamePattern).isPresent() ? "%" + tableNamePattern + "%" : tableNamePattern;
    }
    
    private String getActualTable(final String table) {
        if (null == table || null == shardingRule) {
            return table;
        }
        String result = table;
        if (shardingRule.findTableRule(table).isPresent()) {
            DataNode dataNode = shardingRule.getDataNode(table);
            currentDataSourceName = dataNode.getDataSourceName();
            result = dataNode.getTableName();
        }
        return result;
    }
}
