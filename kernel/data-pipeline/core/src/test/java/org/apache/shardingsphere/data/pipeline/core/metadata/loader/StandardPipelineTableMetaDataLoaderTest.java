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

package org.apache.shardingsphere.data.pipeline.core.metadata.loader;

import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StandardPipelineTableMetaDataLoaderTest {
    
    @Test
    void assertGetTableMetaDataWithActualIdentifiers() throws SQLException {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        ResultSet tables = mockTablesResultSet();
        ResultSet primaryKeys = mockEmptyResultSet();
        ResultSet indexes = mockEmptyResultSet();
        ResultSet columns = mockColumnsResultSet("USER_ID");
        when(databaseMetaData.getTables("foo_db", "TEST", "T_USER", null)).thenReturn(tables);
        when(databaseMetaData.getPrimaryKeys("foo_db", "TEST", "T_USER")).thenReturn(primaryKeys);
        when(databaseMetaData.getIndexInfo("foo_db", "TEST", "T_USER", true, true)).thenReturn(indexes);
        when(databaseMetaData.getColumns("foo_db", "TEST", "T_USER", "%")).thenReturn(columns);
        PipelineTableMetaData actual = new StandardPipelineTableMetaDataLoader(createPipelineDataSource(databaseMetaData)).getTableMetaData("TEST", "T_USER");
        assertThat(actual.getColumnNames().get(0), is("USER_ID"));
        verify(databaseMetaData).getTables("foo_db", "TEST", "T_USER", null);
    }
    
    @Test
    void assertGetTableMetaDataWithQualifiedCacheIdentity() throws SQLException {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        setUpTableMetaData(databaseMetaData, "TEST", "T_USER", "UPPER_ID");
        setUpTableMetaData(databaseMetaData, "test", "T_USER", "LOWER_ID");
        StandardPipelineTableMetaDataLoader loader = new StandardPipelineTableMetaDataLoader(createPipelineDataSource(databaseMetaData));
        assertThat(loader.getTableMetaData("TEST", "T_USER").getColumnNames().get(0), is("UPPER_ID"));
        assertThat(loader.getTableMetaData("test", "T_USER").getColumnNames().get(0), is("LOWER_ID"));
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(ints = {1, 2})
    void assertGetTableMetaDataWithCaseInsensitiveMySQLTableCacheIdentity(final int lowerCaseTableNames) throws SQLException {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        setUpTableMetaData(databaseMetaData, null, "T_USER", "USER_ID");
        StandardPipelineTableMetaDataLoader loader = new StandardPipelineTableMetaDataLoader(createMySQLPipelineDataSource(databaseMetaData, lowerCaseTableNames));
        assertThat(loader.getTableMetaData(null, "T_USER").getColumnNames().get(0), is("USER_ID"));
        assertThat(loader.getTableMetaData(null, "t_user").getColumnNames().get(0), is("USER_ID"));
        verify(databaseMetaData).getTables("foo_db", null, "T_USER", null);
    }
    
    @Test
    void assertGetTableMetaDataWithCaseSensitiveMySQLTableCacheIdentity() throws SQLException {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        setUpTableMetaData(databaseMetaData, null, "T_USER", "USER_ID");
        StandardPipelineTableMetaDataLoader loader = new StandardPipelineTableMetaDataLoader(createMySQLPipelineDataSource(databaseMetaData, 0));
        assertThat(loader.getTableMetaData(null, "T_USER").getColumnNames().get(0), is("USER_ID"));
        ResultSet tables = mockEmptyResultSet();
        when(databaseMetaData.getTables("foo_db", null, "t_user", null)).thenReturn(tables);
        assertNull(loader.getTableMetaData(null, "t_user"));
        verify(databaseMetaData).getTables("foo_db", null, "t_user", null);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getMySQLCasePolicyFallbackArguments")
    void assertGetTableMetaDataWithMySQLCasePolicyFallback(final String name, final Integer lowerCaseTableNames, final boolean queryFailed) throws SQLException {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        setUpTableMetaData(databaseMetaData, null, "T_USER", "USER_ID");
        StandardPipelineTableMetaDataLoader loader = new StandardPipelineTableMetaDataLoader(createMySQLPipelineDataSource(databaseMetaData, lowerCaseTableNames, queryFailed));
        assertThat(name, loader.getTableMetaData(null, "T_USER").getColumnNames().get(0), is("USER_ID"));
        assertThat(name, loader.getTableMetaData(null, "t_user").getColumnNames().get(0), is("USER_ID"));
        verify(databaseMetaData).getTables("foo_db", null, "T_USER", null);
    }
    
    private PipelineDataSource createPipelineDataSource(final DatabaseMetaData databaseMetaData) throws SQLException {
        return createPipelineDataSource(databaseMetaData, TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
    }
    
    private PipelineDataSource createPipelineDataSource(final DatabaseMetaData databaseMetaData, final DatabaseType databaseType) throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("foo_db");
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        return createPipelineDataSource(dataSource, databaseType);
    }
    
    private PipelineDataSource createPipelineDataSource(final DataSource dataSource, final DatabaseType databaseType) {
        return new PipelineDataSource(dataSource, databaseType);
    }
    
    private PipelineDataSource createMySQLPipelineDataSource(final DatabaseMetaData databaseMetaData, final int lowerCaseTableNames) throws SQLException {
        return createMySQLPipelineDataSource(databaseMetaData, lowerCaseTableNames, false);
    }
    
    private PipelineDataSource createMySQLPipelineDataSource(final DatabaseMetaData databaseMetaData, final Integer lowerCaseTableNames, final boolean queryFailed) throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        if (queryFailed) {
            when(connection.prepareStatement("SELECT @@lower_case_table_names")).thenThrow(new SQLException("Query failed"));
        } else {
            setUpMySQLCasePolicyResult(connection, lowerCaseTableNames);
        }
        when(connection.getCatalog()).thenReturn("foo_db");
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        return createPipelineDataSource(dataSource, TypedSPILoader.getService(DatabaseType.class, "MySQL"));
    }
    
    private void setUpMySQLCasePolicyResult(final Connection connection, final Integer lowerCaseTableNames) throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareStatement("SELECT @@lower_case_table_names")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(null != lowerCaseTableNames);
        if (null != lowerCaseTableNames) {
            when(resultSet.getInt(1)).thenReturn(lowerCaseTableNames);
        }
    }
    
    private static Stream<Arguments> getMySQLCasePolicyFallbackArguments() {
        return Stream.of(Arguments.of("no_result_row", null, false), Arguments.of("sql_exception", null, true), Arguments.of("unexpected_config", 3, false));
    }
    
    private void setUpTableMetaData(final DatabaseMetaData databaseMetaData, final String schemaName, final String tableName, final String columnName) throws SQLException {
        ResultSet tables = mockTablesResultSet(tableName);
        ResultSet primaryKeys = mockEmptyResultSet();
        ResultSet indexes = mockEmptyResultSet();
        ResultSet columns = mockColumnsResultSet(columnName);
        when(databaseMetaData.getTables("foo_db", schemaName, tableName, null)).thenReturn(tables);
        when(databaseMetaData.getPrimaryKeys("foo_db", schemaName, tableName)).thenReturn(primaryKeys);
        when(databaseMetaData.getIndexInfo("foo_db", schemaName, tableName, true, true)).thenReturn(indexes);
        when(databaseMetaData.getColumns("foo_db", schemaName, tableName, "%")).thenReturn(columns);
    }
    
    private ResultSet mockTablesResultSet() throws SQLException {
        return mockTablesResultSet("T_USER");
    }
    
    private ResultSet mockTablesResultSet(final String tableName) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("TABLE_NAME")).thenReturn(tableName);
        return result;
    }
    
    private ResultSet mockColumnsResultSet(final String columnName) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getInt("ORDINAL_POSITION")).thenReturn(1);
        when(result.getString("COLUMN_NAME")).thenReturn(columnName);
        when(result.getInt("DATA_TYPE")).thenReturn(Types.BIGINT);
        when(result.getString("TYPE_NAME")).thenReturn("int8");
        when(result.getString("IS_NULLABLE")).thenReturn("NO");
        return result;
    }
    
    private ResultSet mockEmptyResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(false);
        return result;
    }
}
