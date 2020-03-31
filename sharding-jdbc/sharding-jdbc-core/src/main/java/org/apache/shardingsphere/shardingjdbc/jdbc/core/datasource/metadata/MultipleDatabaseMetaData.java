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

import lombok.Getter;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractConnectionAdapter;
import org.apache.shardingsphere.underlying.common.database.DefaultSchema;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;

/**
 * Multiple database meta data.
 */
@Getter
public abstract class MultipleDatabaseMetaData<C extends AbstractConnectionAdapter> extends AdaptedDatabaseMetaData {
    
    private final C connection;
    
    private final Collection<String> datasourceNames;
    
    private final ShardingSphereMetaData shardingSphereMetaData;
    
    private String currentDataSourceName;
    
    private DatabaseMetaData currentDatabaseMetaData;
    
    public MultipleDatabaseMetaData(final C connection, final Collection<String> datasourceNames,
                                    final CachedDatabaseMetaData cachedDatabaseMetaData, final ShardingSphereMetaData shardingSphereMetaData) {
        super(cachedDatabaseMetaData);
        this.connection = connection;
        this.datasourceNames = datasourceNames;
        this.shardingSphereMetaData = shardingSphereMetaData;
    }
    
    @Override
    public final Connection getConnection() throws SQLException {
        return connection.getConnection(getDataSourceName());
    }
    
    @Override
    public final ResultSet getSuperTypes(final String catalog, final String schemaPattern, final String typeNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getSuperTypes(getActualCatalog(catalog), getActualSchema(schemaPattern), typeNamePattern));
    }
    
    @Override
    public final ResultSet getSuperTables(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getSuperTables(getActualCatalog(catalog), getActualSchema(schemaPattern), getActualTableNamePattern(tableNamePattern)));
    }
    
    @Override
    public final ResultSet getAttributes(final String catalog, final String schemaPattern, final String typeNamePattern, final String attributeNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getAttributes(getActualCatalog(catalog), getActualSchema(schemaPattern), typeNamePattern, attributeNamePattern));
    }
    
    @Override
    public final ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getProcedures(getActualCatalog(catalog), getActualSchema(schemaPattern), procedureNamePattern));
    }
    
    @Override
    public final ResultSet getProcedureColumns(final String catalog, final String schemaPattern, final String procedureNamePattern, final String columnNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getProcedureColumns(getActualCatalog(catalog), getActualSchema(schemaPattern), procedureNamePattern, columnNamePattern));
    }
    
    @Override
    public final ResultSet getTables(final String catalog, final String schemaPattern, final String tableNamePattern, final String[] types) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getTables(getActualCatalog(catalog), getActualSchema(schemaPattern), getActualTableNamePattern(tableNamePattern), types));
    }
    
    @Override
    public final ResultSet getSchemas() throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getSchemas());
    }
    
    @Override
    public final ResultSet getSchemas(final String catalog, final String schemaPattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getSchemas(getActualCatalog(catalog), getActualSchema(schemaPattern)));
    }
    
    @Override
    public final ResultSet getCatalogs() throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getCatalogs());
    }
    
    @Override
    public final ResultSet getTableTypes() throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getTableTypes());
    }
    
    @Override
    public final ResultSet getColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(
            getDatabaseMetaData().getColumns(getActualCatalog(catalog), getActualSchema(schemaPattern), getActualTableNamePattern(tableNamePattern), columnNamePattern));
    }
    
    @Override
    public final ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(
            getDatabaseMetaData().getColumnPrivileges(getActualCatalog(catalog), getActualSchema(schema), getActualTable(table), columnNamePattern));
    }
    
    @Override
    public final ResultSet getTablePrivileges(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getTablePrivileges(getActualCatalog(catalog), getActualSchema(schemaPattern), getActualTableNamePattern(tableNamePattern)));
    }
    
    @Override
    public final ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getBestRowIdentifier(getActualCatalog(catalog), getActualSchema(schema), getActualTable(table), scope, nullable));
    }
    
    @Override
    public final ResultSet getVersionColumns(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getVersionColumns(getActualCatalog(catalog), getActualSchema(schema), getActualTable(table)));
    }
    
    @Override
    public final ResultSet getPrimaryKeys(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getPrimaryKeys(getActualCatalog(catalog), getActualSchema(schema), getActualTable(table)));
    }
    
    @Override
    public final ResultSet getImportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getImportedKeys(getActualCatalog(catalog), getActualSchema(schema), getActualTable(table)));
    }
    
    @Override
    public final ResultSet getExportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getExportedKeys(getActualCatalog(catalog), getActualSchema(schema), getActualTable(table)));
    }
    
    @Override
    public final ResultSet getCrossReference(final String parentCatalog,
                                       final String parentSchema, final String parentTable, final String foreignCatalog, final String foreignSchema, final String foreignTable) throws SQLException {
        return createDatabaseMetaDataResultSet(
            getDatabaseMetaData().getCrossReference(getActualCatalog(parentCatalog), getActualSchema(parentSchema), parentTable, foreignCatalog, foreignSchema, foreignTable));
    }
    
    @Override
    public final ResultSet getTypeInfo() throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getTypeInfo());
    }
    
    @Override
    public final ResultSet getIndexInfo(final String catalog, final String schema, final String table, final boolean unique, final boolean approximate) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getIndexInfo(getActualCatalog(catalog), getActualSchema(schema), getActualTable(table), unique, approximate));
    }
    
    @Override
    public final ResultSet getUDTs(final String catalog, final String schemaPattern, final String typeNamePattern, final int[] types) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getUDTs(getActualCatalog(catalog), getActualSchema(schemaPattern), typeNamePattern, types));
    }
    
    @Override
    public final ResultSet getClientInfoProperties() throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getClientInfoProperties());
    }
    
    @Override
    public final ResultSet getFunctions(final String catalog, final String schemaPattern, final String functionNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getFunctions(getActualCatalog(catalog), getActualSchema(schemaPattern), functionNamePattern));
    }
    
    @Override
    public final ResultSet getFunctionColumns(final String catalog, final String schemaPattern, final String functionNamePattern, final String columnNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getFunctionColumns(getActualCatalog(catalog), getActualSchema(schemaPattern), functionNamePattern, columnNamePattern));
    }
    
    @Override
    public final ResultSet getPseudoColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(
            getDatabaseMetaData().getPseudoColumns(getActualCatalog(catalog), getActualSchema(schemaPattern), getActualTableNamePattern(tableNamePattern), columnNamePattern));
    }
    
    protected abstract String getActualTableNamePattern(String tableNamePattern);
    
    protected abstract String getActualTable(String table);
    
    protected abstract ResultSet createDatabaseMetaDataResultSet(ResultSet resultSet) throws SQLException;
    
    private String getActualCatalog(final String catalog) {
        return null != catalog && catalog.contains(DefaultSchema.LOGIC_NAME) ? shardingSphereMetaData.getDataSources().getDataSourceMetaData(getDataSourceName()).getCatalog() : catalog;
    }
    
    private String getActualSchema(final String schema) {
        return null != schema && schema.contains(DefaultSchema.LOGIC_NAME) ? shardingSphereMetaData.getDataSources().getDataSourceMetaData(getDataSourceName()).getSchema() : schema;
    }
    
    private String getDataSourceName() {
        currentDataSourceName = Optional.ofNullable(currentDataSourceName).orElse(getRandomDataSourceName());
        return currentDataSourceName;
    }
    
    private String getRandomDataSourceName() {
        Collection<String> datasourceNames = connection.getCachedConnections().isEmpty() ? this.datasourceNames : connection.getCachedConnections().keySet();
        return new ArrayList<>(datasourceNames).get(new Random().nextInt(datasourceNames.size()));
    }
    
    private DatabaseMetaData getDatabaseMetaData() throws SQLException {
        currentDatabaseMetaData = Optional.ofNullable(currentDatabaseMetaData).orElse(getConnection().getMetaData());
        return currentDatabaseMetaData;
    }
}
