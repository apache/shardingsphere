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

import com.google.common.base.Strings;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.driver.jdbc.adapter.AdaptedDatabaseMetaData;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.resultset.DatabaseMetaDataResultSet;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * ShardingSphere database meta data.
 */
public final class ShardingSphereDatabaseMetaData extends AdaptedDatabaseMetaData {
    
    private final ShardingSphereConnection connection;
    
    private final Collection<ShardingSphereRule> rules;
    
    private String currentPhysicalDataSourceName;
    
    private Connection currentPhysicalConnection;
    
    private DatabaseMetaData currentDatabaseMetaData;
    
    public ShardingSphereDatabaseMetaData(final ShardingSphereConnection connection) throws SQLException {
        super(connection.getDatabaseConnectionManager().getRandomConnection().getMetaData());
        this.connection = connection;
        rules = connection.getContextManager().getMetaDataContexts().getMetaData().getDatabase(connection.getCurrentDatabaseName()).getRuleMetaData().getRules();
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        if (null == currentPhysicalConnection) {
            currentPhysicalConnection = connection.getDatabaseConnectionManager().getRandomConnection();
        }
        return currentPhysicalConnection;
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
                getDatabaseMetaData().getColumnPrivileges(getActualCatalog(catalog), getActualSchema(schema), getActualTable(getActualCatalog(catalog), table), columnNamePattern));
    }
    
    @Override
    public ResultSet getTablePrivileges(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getTablePrivileges(getActualCatalog(catalog), getActualSchema(schemaPattern), getActualTableNamePattern(tableNamePattern)));
    }
    
    @Override
    public ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getBestRowIdentifier(getActualCatalog(catalog), getActualSchema(schema),
                getActualTable(getActualCatalog(catalog), table), scope, nullable));
    }
    
    @Override
    public ResultSet getVersionColumns(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getVersionColumns(getActualCatalog(catalog), getActualSchema(schema), getActualTable(getActualCatalog(catalog), table)));
    }
    
    @Override
    public ResultSet getPrimaryKeys(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getPrimaryKeys(getActualCatalog(catalog), getActualSchema(schema), getActualTable(getActualCatalog(catalog), table)));
    }
    
    @Override
    public ResultSet getImportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getImportedKeys(getActualCatalog(catalog), getActualSchema(schema), getActualTable(getActualCatalog(catalog), table)));
    }
    
    @Override
    public ResultSet getExportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getExportedKeys(getActualCatalog(catalog), getActualSchema(schema), getActualTable(getActualCatalog(catalog), table)));
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
        return createDatabaseMetaDataResultSet(getDatabaseMetaData().getIndexInfo(getActualCatalog(catalog), getActualSchema(schema),
                getActualTable(getActualCatalog(catalog), table), unique, approximate));
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
        return null == tableNamePattern
                ? null
                : findDataNodeRuleAttribute().filter(optional -> optional.findFirstActualTable(tableNamePattern).isPresent()).map(optional -> "%" + tableNamePattern + "%").orElse(tableNamePattern);
    }
    
    private String getActualTable(final String catalog, final String table) {
        return null == table ? null : findDataNodeRuleAttribute().map(each -> findActualTable(each, catalog, table).orElse(table)).orElse(table);
    }
    
    private Optional<String> findActualTable(final DataNodeRuleAttribute ruleAttribute, final String catalog, final String table) {
        return Strings.isNullOrEmpty(catalog) ? ruleAttribute.findFirstActualTable(table) : ruleAttribute.findActualTableByCatalog(catalog, table);
    }
    
    private Optional<DataNodeRuleAttribute> findDataNodeRuleAttribute() {
        for (ShardingSphereRule each : rules) {
            Optional<DataNodeRuleAttribute> ruleAttribute = each.getAttributes().findAttribute(DataNodeRuleAttribute.class);
            if (ruleAttribute.isPresent()) {
                return ruleAttribute;
            }
        }
        return Optional.empty();
    }
    
    private ResultSet createDatabaseMetaDataResultSet(final ResultSet resultSet) throws SQLException {
        return new DatabaseMetaDataResultSet(resultSet, rules);
    }
    
    private String getActualCatalog(final String catalog) {
        if (null == catalog) {
            return null;
        }
        // TODO consider get actual catalog by logic catalog rather than random physical datasource's catalog.
        ConnectionProperties connectionProps = connection.getContextManager()
                .getMetaDataContexts().getMetaData().getDatabase(connection.getCurrentDatabaseName()).getResourceMetaData().getStorageUnits().get(getDataSourceName()).getConnectionProperties();
        return connectionProps.getCatalog();
    }
    
    private String getActualSchema(final String schema) {
        if (null == schema) {
            return null;
        }
        // TODO consider get actual schema by logic catalog rather than random physical datasource's schema.
        ConnectionProperties connectionProps = connection.getContextManager()
                .getMetaDataContexts().getMetaData().getDatabase(connection.getCurrentDatabaseName()).getResourceMetaData().getStorageUnits().get(getDataSourceName()).getConnectionProperties();
        return Optional.ofNullable(connectionProps.getSchema()).map(String::toUpperCase).orElse(null);
    }
    
    private String getDataSourceName() {
        if (null == currentPhysicalDataSourceName) {
            currentPhysicalDataSourceName = connection.getDatabaseConnectionManager().getRandomPhysicalDataSourceName();
        }
        return currentPhysicalDataSourceName;
    }
    
    private DatabaseMetaData getDatabaseMetaData() throws SQLException {
        if (null == currentDatabaseMetaData) {
            currentDatabaseMetaData = getConnection().getMetaData();
        }
        return currentDatabaseMetaData;
    }
}
