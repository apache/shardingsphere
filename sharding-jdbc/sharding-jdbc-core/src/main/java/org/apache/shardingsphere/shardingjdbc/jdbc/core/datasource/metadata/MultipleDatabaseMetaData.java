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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Multiple database meta data.
 */
@Getter
public abstract class MultipleDatabaseMetaData<C extends AbstractConnectionAdapter> extends AdaptedDatabaseMetaData {
    
    private final C connection;
    
    private final Collection<String> datasourceNames;
    
    public MultipleDatabaseMetaData(final C connection, final Collection<String> datasourceNames, final CachedDatabaseMetaData cachedDatabaseMetaData) {
        super(cachedDatabaseMetaData);
        this.connection = connection;
        this.datasourceNames = datasourceNames;
    }
    
    @Override
    public final Connection getConnection() throws SQLException {
        return connection.getConnection(getRandomDatasourceName());
    }
    
    private String getRandomDatasourceName() {
        Collection<String> datasourceNames = connection.getCachedConnections().isEmpty() ? this.datasourceNames : connection.getCachedConnections().keySet();
        return new ArrayList<>(datasourceNames).get(new Random().nextInt(datasourceNames.size()));
    }
    
    @Override
    public final ResultSet getSuperTypes(final String catalog, final String schemaPattern, final String typeNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getSuperTypes(catalog, schemaPattern, typeNamePattern));
    }
    
    @Override
    public final ResultSet getSuperTables(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getSuperTables(catalog, schemaPattern, getActualTableNamePattern(tableNamePattern)));
    }
    
    @Override
    public final ResultSet getAttributes(final String catalog, final String schemaPattern, final String typeNamePattern, final String attributeNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getAttributes(catalog, schemaPattern, typeNamePattern, attributeNamePattern));
    }
    
    @Override
    public final ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getProcedures(catalog, schemaPattern, procedureNamePattern));
    }
    
    @Override
    public final ResultSet getProcedureColumns(final String catalog, final String schemaPattern, final String procedureNamePattern, final String columnNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern));
    }
    
    @Override
    public final ResultSet getTables(final String catalog, final String schemaPattern, final String tableNamePattern, final String[] types) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getTables(catalog, schemaPattern, getActualTableNamePattern(tableNamePattern), types));
    }
    
    @Override
    public final ResultSet getSchemas() throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getSchemas());
    }
    
    @Override
    public final ResultSet getSchemas(final String catalog, final String schemaPattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getSchemas(catalog, schemaPattern));
    }
    
    @Override
    public final ResultSet getCatalogs() throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getCatalogs());
    }
    
    @Override
    public final ResultSet getTableTypes() throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getTableTypes());
    }
    
    @Override
    public final ResultSet getColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getColumns(catalog, schemaPattern, getActualTableNamePattern(tableNamePattern), columnNamePattern));
    }
    
    @Override
    public final ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getColumnPrivileges(catalog, schema, getActualTable(table), columnNamePattern));
    }
    
    @Override
    public final ResultSet getTablePrivileges(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getTablePrivileges(catalog, schemaPattern, getActualTableNamePattern(tableNamePattern)));
    }
    
    @Override
    public final ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getBestRowIdentifier(catalog, schema, getActualTable(table), scope, nullable));
    }
    
    @Override
    public final ResultSet getVersionColumns(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getVersionColumns(catalog, schema, getActualTable(table)));
    }
    
    @Override
    public final ResultSet getPrimaryKeys(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getPrimaryKeys(catalog, schema, getActualTable(table)));
    }
    
    @Override
    public final ResultSet getImportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getImportedKeys(catalog, schema, getActualTable(table)));
    }
    
    @Override
    public final ResultSet getExportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getExportedKeys(catalog, schema, getActualTable(table)));
    }
    
    @Override
    public final ResultSet getCrossReference(final String parentCatalog,
                                       final String parentSchema, final String parentTable, final String foreignCatalog, final String foreignSchema, final String foreignTable) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getCrossReference(parentCatalog, parentSchema, parentTable, foreignCatalog, foreignSchema, foreignTable));
    }
    
    @Override
    public final ResultSet getTypeInfo() throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getTypeInfo());
    }
    
    @Override
    public final ResultSet getIndexInfo(final String catalog, final String schema, final String table, final boolean unique, final boolean approximate) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getIndexInfo(catalog, schema, getActualTable(table), unique, approximate));
    }
    
    @Override
    public final ResultSet getUDTs(final String catalog, final String schemaPattern, final String typeNamePattern, final int[] types) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getUDTs(catalog, schemaPattern, typeNamePattern, types));
    }
    
    @Override
    public final ResultSet getClientInfoProperties() throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getClientInfoProperties());
    }
    
    @Override
    public final ResultSet getFunctions(final String catalog, final String schemaPattern, final String functionNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getFunctions(catalog, schemaPattern, functionNamePattern));
    }
    
    @Override
    public final ResultSet getFunctionColumns(final String catalog, final String schemaPattern, final String functionNamePattern, final String columnNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getFunctionColumns(catalog, schemaPattern, functionNamePattern, columnNamePattern));
    }
    
    @Override
    public final ResultSet getPseudoColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        return createDatabaseMetaDataResultSet(getConnection().getMetaData().getPseudoColumns(catalog, schemaPattern, getActualTableNamePattern(tableNamePattern), columnNamePattern));
    }
    
    protected abstract String getActualTableNamePattern(String tableNamePattern);
    
    protected abstract String getActualTable(String table);
    
    protected abstract ResultSet createDatabaseMetaDataResultSet(ResultSet resultSet) throws SQLException;
}
