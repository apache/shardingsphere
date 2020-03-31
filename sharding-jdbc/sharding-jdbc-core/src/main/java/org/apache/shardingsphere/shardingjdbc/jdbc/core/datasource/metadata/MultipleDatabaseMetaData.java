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
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.DatabaseMetaDataResultSet;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;

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
public abstract class MultipleDatabaseMetaData<T extends BaseRule, C extends AbstractConnectionAdapter> extends AdaptedDatabaseMetaData {
    
    private final T rule;
    
    private final C connection;
    
    private final Collection<String> datasourceNames;
    
    public MultipleDatabaseMetaData(final C connection, final T rule, final Collection<String> datasourceNames, final CachedDatabaseMetaData cachedDatabaseMetaData) {
        super(cachedDatabaseMetaData);
        this.connection = connection;
        this.rule = rule;
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
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getSuperTypes(catalog, schemaPattern, typeNamePattern), rule);
    }
    
    @Override
    public final ResultSet getSuperTables(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getSuperTables(catalog, schemaPattern, getActualTableNamePattern(tableNamePattern)), rule);
    }
    
    @Override
    public final ResultSet getAttributes(final String catalog, final String schemaPattern, final String typeNamePattern, final String attributeNamePattern) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getAttributes(catalog, schemaPattern, typeNamePattern, attributeNamePattern), rule);
    }
    
    @Override
    public final ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getProcedures(catalog, schemaPattern, procedureNamePattern), rule);
    }
    
    @Override
    public final ResultSet getProcedureColumns(final String catalog, final String schemaPattern, final String procedureNamePattern, final String columnNamePattern) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern), rule);
    }
    
    @Override
    public final ResultSet getTables(final String catalog, final String schemaPattern, final String tableNamePattern, final String[] types) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getTables(catalog, schemaPattern, getActualTableNamePattern(tableNamePattern), types), rule);
    }
    
    @Override
    public final ResultSet getSchemas() throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getSchemas(), rule);
    }
    
    @Override
    public final ResultSet getSchemas(final String catalog, final String schemaPattern) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getSchemas(catalog, schemaPattern), rule);
    }
    
    @Override
    public final ResultSet getCatalogs() throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getCatalogs(), rule);
    }
    
    @Override
    public final ResultSet getTableTypes() throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getTableTypes(), rule);
    }
    
    @Override
    public final ResultSet getColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getColumns(catalog, schemaPattern, getActualTableNamePattern(tableNamePattern), columnNamePattern), rule);
    }
    
    @Override
    public final ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getColumnPrivileges(catalog, schema, getActualTable(table), columnNamePattern), rule);
    }
    
    @Override
    public final ResultSet getTablePrivileges(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getTablePrivileges(catalog, schemaPattern, getActualTableNamePattern(tableNamePattern)), rule);
    }
    
    @Override
    public final ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getBestRowIdentifier(catalog, schema, getActualTable(table), scope, nullable), rule);
    }
    
    @Override
    public final ResultSet getVersionColumns(final String catalog, final String schema, final String table) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getVersionColumns(catalog, schema, getActualTable(table)), rule);
    }
    
    @Override
    public final ResultSet getPrimaryKeys(final String catalog, final String schema, final String table) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getPrimaryKeys(catalog, schema, getActualTable(table)), rule);
    }
    
    @Override
    public final ResultSet getImportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getImportedKeys(catalog, schema, getActualTable(table)), rule);
    }
    
    @Override
    public final ResultSet getExportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getExportedKeys(catalog, schema, getActualTable(table)), rule);
    }
    
    @Override
    public final ResultSet getCrossReference(final String parentCatalog,
                                       final String parentSchema, final String parentTable, final String foreignCatalog, final String foreignSchema, final String foreignTable) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getCrossReference(parentCatalog, parentSchema, parentTable, foreignCatalog, foreignSchema, foreignTable), rule);
    }
    
    @Override
    public final ResultSet getTypeInfo() throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getTypeInfo(), rule);
    }
    
    @Override
    public final ResultSet getIndexInfo(final String catalog, final String schema, final String table, final boolean unique, final boolean approximate) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getIndexInfo(catalog, schema, getActualTable(table), unique, approximate), rule);
    }
    
    @Override
    public final ResultSet getUDTs(final String catalog, final String schemaPattern, final String typeNamePattern, final int[] types) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getUDTs(catalog, schemaPattern, typeNamePattern, types), rule);
    }
    
    @Override
    public final ResultSet getClientInfoProperties() throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getClientInfoProperties(), rule);
    }
    
    @Override
    public final ResultSet getFunctions(final String catalog, final String schemaPattern, final String functionNamePattern) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getFunctions(catalog, schemaPattern, functionNamePattern), rule);
    }
    
    @Override
    public final ResultSet getFunctionColumns(final String catalog, final String schemaPattern, final String functionNamePattern, final String columnNamePattern) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getFunctionColumns(catalog, schemaPattern, functionNamePattern, columnNamePattern), rule);
    }
    
    @Override
    public final ResultSet getPseudoColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        return new DatabaseMetaDataResultSet<>(getConnection().getMetaData().getPseudoColumns(catalog, schemaPattern, getActualTableNamePattern(tableNamePattern), columnNamePattern), rule);
    }
    
    protected abstract String getActualTableNamePattern(String tableNamePattern);
    
    protected abstract String getActualTable(String table);
}
