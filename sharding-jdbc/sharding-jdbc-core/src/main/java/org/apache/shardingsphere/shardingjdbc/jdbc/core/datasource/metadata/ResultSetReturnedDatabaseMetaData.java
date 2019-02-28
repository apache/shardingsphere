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

import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.DatabaseMetaDataResultSet;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * {@code ResultSet} returned database meta data.
 *
 * @author yangyi
 */
public abstract class ResultSetReturnedDatabaseMetaData extends ConnectionRequiredDatabaseMetaData {
    
    private final ShardingRule shardingRule;
    
    public ResultSetReturnedDatabaseMetaData(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) {
        super(dataSourceMap, shardingRule);
        this.shardingRule = shardingRule;
    }
    
    @Override
    public final ResultSet getSuperTypes(final String catalog, final String schemaPattern, final String typeNamePattern) throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getSuperTypes(catalog, schemaPattern, typeNamePattern), shardingRule);
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
        String shardingTableNamePattern = getShardingTableNamePattern(tableNamePattern);
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getTables(catalog, schemaPattern, shardingTableNamePattern, types), shardingRule);
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
        String shardingTableNamePattern = getShardingTableNamePattern(tableNamePattern);
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getColumns(catalog, schemaPattern, shardingTableNamePattern, columnNamePattern), shardingRule);
        }
    }
    
    @Override
    public final ResultSet getTypeInfo() throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getTypeInfo(), shardingRule);
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
    
    private String getShardingTableNamePattern(final String tableNamePattern) {
        return null == tableNamePattern ? tableNamePattern : (shardingRule.findTableRule(tableNamePattern).isPresent() ? "%" + tableNamePattern + "%" : tableNamePattern);
    }
}
