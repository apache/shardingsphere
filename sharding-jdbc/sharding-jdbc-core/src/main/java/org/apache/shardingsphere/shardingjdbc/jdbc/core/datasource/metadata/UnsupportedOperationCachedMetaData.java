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

import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.WrapperAdapter;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Unsupported operation cached database meta data.
 *
 * @author zhangliang
 */
public abstract class UnsupportedOperationCachedMetaData extends WrapperAdapter implements DatabaseMetaData {
    
    @Override
    public final Connection getConnection() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getSuperTypes(final String catalog, final String schemaPattern, final String typeNamePattern) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getSuperTables(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getAttributes(final String catalog, final String schemaPattern, final String typeNamePattern, final String attributeNamePattern) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getProcedureColumns(final String catalog, final String schemaPattern, final String procedureNamePattern, final String columnNamePattern) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getTables(final String catalog, final String schemaPattern, final String tableNamePattern, final String[] types) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getSchemas() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getSchemas(final String catalog, final String schemaPattern) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getCatalogs() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getTableTypes() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getTablePrivileges(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getVersionColumns(final String catalog, final String schema, final String table) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getPrimaryKeys(final String catalog, final String schema, final String table) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getImportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getExportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getCrossReference(final String parentCatalog,
                                       final String parentSchema, final String parentTable, final String foreignCatalog, final String foreignSchema, final String foreignTable) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getTypeInfo() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getIndexInfo(final String catalog, final String schema, final String table, final boolean unique, final boolean approximate) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getUDTs(final String catalog, final String schemaPattern, final String typeNamePattern, final int[] types) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getClientInfoProperties() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getFunctions(final String catalog, final String schemaPattern, final String functionNamePattern) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getFunctionColumns(final String catalog, final String schemaPattern, final String functionNamePattern, final String columnNamePattern) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final ResultSet getPseudoColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
