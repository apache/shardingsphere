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

package org.apache.shardingsphere.driver.jdbc.core.datasource.metadata;

import lombok.Getter;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractConnectionAdapter;
import org.apache.shardingsphere.driver.jdbc.adapter.AdaptedDatabaseMetaData;
import org.apache.shardingsphere.driver.jdbc.core.resultset.DatabaseMetaDataResultSet;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.rule.DataNodeBasedRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;

/**
 * ShardingSphere database meta data.
 */
@Getter
public final class ShardingSphereDatabaseMetaData extends AdaptedDatabaseMetaData {
    
    private final AbstractConnectionAdapter connection;
    
    private final Collection<ShardingSphereRule> rules;
    
    private final Collection<String> datasourceNames;
    
    private final DataSourcesMetaData dataSourcesMetaData;
    
    private final Random random = new SecureRandom();
    
    private String currentDataSourceName;
    
    private DatabaseMetaData currentDatabaseMetaData;
    
    public ShardingSphereDatabaseMetaData(final AbstractConnectionAdapter connection) {
        super(connection.getSchemaContexts().getDefaultMetaData().getResource().getCachedDatabaseMetaData());
        this.connection = connection;
        rules = connection.getSchemaContexts().getDefaultMetaData().getRuleMetaData().getRules();
        datasourceNames = connection.getDataSourceMap().keySet();
        dataSourcesMetaData = connection.getSchemaContexts().getDefaultMetaData().getResource().getDataSourcesMetaData();
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return connection.getConnection(getDataSourceName());
    }
    
    @Override
    public ResultSet getSuperTypes(final String catalog, final String schemaPattern, final String typeNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getSuperTypes(getActualCatalog(catalog), getActualSchema(schemaPattern), typeNamePattern));
    }
    
    @Override
    public ResultSet getSuperTables(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getSuperTables(getActualCatalog(catalog), getActualSchema(schemaPattern), getActualTableNamePattern(tableNamePattern)));
    }
    
    @Override
    public ResultSet getAttributes(final String catalog, final String schemaPattern, final String typeNamePattern, final String attributeNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getAttributes(getActualCatalog(catalog), getActualSchema(schemaPattern), typeNamePattern, attributeNamePattern));
    }
    
    @Override
    public ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getProcedures(getActualCatalog(catalog), getActualSchema(schemaPattern), procedureNamePattern));
    }
    
    @Override
    public ResultSet getProcedureColumns(final String catalog, final String schemaPattern, final String procedureNamePattern, final String columnNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getProcedureColumns(getActualCatalog(catalog), getActualSchema(schemaPattern), procedureNamePattern, columnNamePattern));
    }
    
    @Override
    public ResultSet getTables(final String catalog, final String schemaPattern, final String tableNamePattern, final String[] types) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getTables(getActualCatalog(catalog), getActualSchema(schemaPattern), getActualTableNamePattern(tableNamePattern), types));
    }
    
    @Override
    public ResultSet getSchemas() throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getSchemas());
    }
    
    @Override
    public ResultSet getSchemas(final String catalog, final String schemaPattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getSchemas(getActualCatalog(catalog), getActualSchema(schemaPattern)));
    }
    
    @Override
    public ResultSet getCatalogs() throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getCatalogs());
    }
    
    @Override
    public ResultSet getTableTypes() throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getTableTypes());
    }
    
    @Override
    public ResultSet getColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(
            getDatabaseMetaData().getColumns(getActualCatalog(catalog), getActualSchema(schemaPattern), getActualTableNamePattern(tableNamePattern), columnNamePattern));
    }
    
    @Override
    public ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(
            getDatabaseMetaData().getColumnPrivileges(getActualCatalog(catalog), getActualSchema(schema), getActualTable(table), columnNamePattern));
    }
    
    @Override
    public ResultSet getTablePrivileges(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getTablePrivileges(getActualCatalog(catalog), getActualSchema(schemaPattern), getActualTableNamePattern(tableNamePattern)));
    }
    
    @Override
    public ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getBestRowIdentifier(getActualCatalog(catalog), getActualSchema(schema), getActualTable(table), scope, nullable));
    }
    
    @Override
    public ResultSet getVersionColumns(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getVersionColumns(getActualCatalog(catalog), getActualSchema(schema), getActualTable(table)));
    }
    
    @Override
    public ResultSet getPrimaryKeys(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getPrimaryKeys(getActualCatalog(catalog), getActualSchema(schema), getActualTable(table)));
    }
    
    @Override
    public ResultSet getImportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getImportedKeys(getActualCatalog(catalog), getActualSchema(schema), getActualTable(table)));
    }
    
    @Override
    public ResultSet getExportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getExportedKeys(getActualCatalog(catalog), getActualSchema(schema), getActualTable(table)));
    }
    
    @Override
    public ResultSet getCrossReference(final String parentCatalog,
                                       final String parentSchema, final String parentTable, final String foreignCatalog, final String foreignSchema, final String foreignTable) throws SQLException {
        return createDatabaseMetaDataResultSet(
            getDatabaseMetaData().getCrossReference(getActualCatalog(parentCatalog), getActualSchema(parentSchema), parentTable, foreignCatalog, foreignSchema, foreignTable));
    }
    
    @Override
    public ResultSet getTypeInfo() throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getTypeInfo());
    }
    
    @Override
    public ResultSet getIndexInfo(final String catalog, final String schema, final String table, final boolean unique, final boolean approximate) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getIndexInfo(getActualCatalog(catalog), getActualSchema(schema), getActualTable(table), unique, approximate));
    }
    
    @Override
    public ResultSet getUDTs(final String catalog, final String schemaPattern, final String typeNamePattern, final int[] types) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getUDTs(getActualCatalog(catalog), getActualSchema(schemaPattern), typeNamePattern, types));
    }
    
    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getClientInfoProperties());
    }
    
    @Override
    public ResultSet getFunctions(final String catalog, final String schemaPattern, final String functionNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getFunctions(getActualCatalog(catalog), getActualSchema(schemaPattern), functionNamePattern));
    }
    
    @Override
    public ResultSet getFunctionColumns(final String catalog, final String schemaPattern, final String functionNamePattern, final String columnNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getFunctionColumns(getActualCatalog(catalog), getActualSchema(schemaPattern), functionNamePattern, columnNamePattern));
    }
    
    @Override
    public ResultSet getPseudoColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(
            getDatabaseMetaData().getPseudoColumns(getActualCatalog(catalog), getActualSchema(schemaPattern), getActualTableNamePattern(tableNamePattern), columnNamePattern));
    }
    
    private String getActualTableNamePattern(final String tableNamePattern) {
        if (null == tableNamePattern) {
            return null;
        }
        Optional<DataNodeBasedRule> dataNodeRoutedRule = findDataNodeRoutedRule();
        if (dataNodeRoutedRule.isPresent()) {
            return dataNodeRoutedRule.get().findFirstActualTable(tableNamePattern).isPresent() ? "%" + tableNamePattern + "%" : tableNamePattern;
        }
        return tableNamePattern;
    }
    
    private String getActualTable(final String table) {
        if (null == table) {
            return null;
        }
        Optional<DataNodeBasedRule> dataNodeRoutedRule = findDataNodeRoutedRule();
        return dataNodeRoutedRule.map(nodeRoutedRule -> nodeRoutedRule.findFirstActualTable(table).orElse(table)).orElse(table);
    }
    
    private Optional<DataNodeBasedRule> findDataNodeRoutedRule() {
        return rules.stream().filter(each -> each instanceof DataNodeBasedRule).findFirst().map(rule -> (DataNodeBasedRule) rule);
    }
    
    private ResultSet createDatabaseMetaDataResultSet(final ResultSet resultSet) throws SQLException {
        return new DatabaseMetaDataResultSet(resultSet, rules);
    }
    
    private String getActualCatalog(final String catalog) {
        return null != catalog && catalog.contains(DefaultSchema.LOGIC_NAME) ? dataSourcesMetaData.getDataSourceMetaData(getDataSourceName()).getCatalog() : catalog;
    }
    
    private String getActualSchema(final String schema) {
        return null != schema && schema.contains(DefaultSchema.LOGIC_NAME) ? dataSourcesMetaData.getDataSourceMetaData(getDataSourceName()).getSchema() : schema;
    }
    
    private String getDataSourceName() {
        currentDataSourceName = Optional.ofNullable(currentDataSourceName).orElse(getRandomDataSourceName());
        return currentDataSourceName;
    }
    
    private String getRandomDataSourceName() {
        Collection<String> datasourceNames = connection.getCachedConnections().isEmpty() ? this.datasourceNames : connection.getCachedConnections().keySet();
        return new ArrayList<>(datasourceNames).get(random.nextInt(datasourceNames.size()));
    }
    
    private DatabaseMetaData getDatabaseMetaData() throws SQLException {
        currentDatabaseMetaData = Optional.ofNullable(currentDatabaseMetaData).orElse(getConnection().getMetaData());
        return currentDatabaseMetaData;
    }
}
