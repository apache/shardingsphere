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

package org.apache.shardingsphere.mcp.core.workflow;

import org.apache.shardingsphere.mcp.core.fixture.CoreDatabaseTypeFactoryMocker;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowProxyQueryServiceTest {
    
    @Test
    void assertQueryReturnsLowerCaseRowsAndAppliesSchema() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SHOW MASK RULES")).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("TABLE");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("COLUMN");
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject(1)).thenReturn("orders");
        when(resultSet.getObject(2)).thenReturn("status");
        WorkflowProxyQueryService service = createService(Map.of("logic_db", runtimeDatabaseConfig));
        List<Map<String, Object>> actual = service.query("logic_db", "public", "SHOW MASK RULES");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).get("table"), is("orders"));
        assertThat(actual.get(0).get("column"), is("status"));
        verify(connection).setSchema("public");
    }
    
    @Test
    void assertQueryWithAnyDatabaseDelegatesToAvailableDatabase() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SHOW ENCRYPT ALGORITHM PLUGINS")).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("TYPE");
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject(1)).thenReturn("AES");
        WorkflowProxyQueryService service = createService(Map.of("logic_db", runtimeDatabaseConfig));
        List<Map<String, Object>> actual = service.queryWithAnyDatabase("SHOW ENCRYPT ALGORITHM PLUGINS");
        assertThat(actual.get(0).get("type"), is("AES"));
    }
    
    @Test
    void assertQueryWithAnyDatabaseThrowsWhenNoRuntimeDatabaseExists() {
        WorkflowProxyQueryService service = createService(Map.of());
        Exception actual = assertThrows(RuntimeException.class, () -> service.queryWithAnyDatabase("SHOW ENCRYPT ALGORITHM PLUGINS"));
        assertThat(actual.getMessage(), is("No runtime database is configured."));
    }
    
    @Test
    void assertQueryThrowsWhenDatabaseIsNotConfigured() {
        WorkflowProxyQueryService service = createService(Map.of());
        Exception actual = assertThrows(MCPUnavailableException.class, () -> service.query("logic_db", "", "SHOW MASK RULES"));
        assertThat(actual.getMessage(), is("Database `logic_db` is not configured."));
    }
    
    @Test
    void assertQueryColumnDefinitionFormatsDecimalDefinition() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT amount FROM orders WHERE 1 = 0")).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.DECIMAL);
        when(resultSetMetaData.getColumnTypeName(1)).thenReturn("DECIMAL");
        when(resultSetMetaData.getPrecision(1)).thenReturn(10);
        when(resultSetMetaData.getScale(1)).thenReturn(2);
        WorkflowProxyQueryService service = createService(Map.of("logic_db", runtimeDatabaseConfig));
        String actual = service.queryColumnDefinition("logic_db", "public", "orders", "amount");
        assertThat(actual, is("DECIMAL(10, 2)"));
    }
    
    @Test
    void assertQueryColumnDefinitionReturnsDefaultWhenMetadataIsEmpty() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT amount FROM orders WHERE 1 = 0")).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(0);
        WorkflowProxyQueryService service = createService(Map.of("logic_db", runtimeDatabaseConfig));
        String actual = service.queryColumnDefinition("logic_db", "public", "orders", "amount");
        assertThat(actual, is("VARCHAR(4000)"));
    }
    
    @Test
    void assertQueryColumnDefinitionFormatsSpecialCharacterIdentifiers() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT `amount due` FROM `order detail` WHERE 1 = 0")).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(0);
        WorkflowProxyQueryService service = createService(Map.of("logic_db", runtimeDatabaseConfig));
        String actual = service.queryColumnDefinition("`logic_db`", "`public`", "order detail", "amount due");
        assertThat(actual, is("VARCHAR(4000)"));
        verify(connection).setSchema("public");
    }
    
    @Test
    void assertQueryColumnDefinitionFormatsPostgreSQLIdentifiers() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT \"amount due\" FROM \"order detail\" WHERE 1 = 0")).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(0);
        WorkflowProxyQueryService service = createService(Map.of("logic_db", runtimeDatabaseConfig), "PostgreSQL");
        String actual = service.queryColumnDefinition("\"logic_db\"", "\"public\"", "order detail", "amount due");
        assertThat(actual, is("VARCHAR(4000)"));
        verify(connection).setSchema("public");
    }
    
    @Test
    void assertQueryInformationSchemaColumnNamesSkipsSchemaFilterWhenSchemaIsEmpty() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT DISTINCT column_name FROM information_schema.columns WHERE table_name = 'orders' "
                + "AND column_name IN ('status_cipher', 'status_assisted_query')"))
                .thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("COLUMN_NAME");
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getObject(1)).thenReturn("status_cipher", "status_assisted_query");
        WorkflowProxyQueryService service = createService(Map.of("logic_db", runtimeDatabaseConfig));
        Set<String> actual = service.queryInformationSchemaColumnNames("logic_db", "", "orders", List.of("status_cipher", "status_assisted_query"));
        assertThat(actual, is(Set.of("status_cipher", "status_assisted_query")));
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"MySQL", "MariaDB", "PostgreSQL", "openGauss"})
    void assertQueryInformationSchemaColumnNamesUsesSchemaFilter(final String databaseType) throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT DISTINCT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'orders' "
                + "AND column_name IN ('status_cipher')")).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("COLUMN_NAME");
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject(1)).thenReturn("status_cipher");
        WorkflowProxyQueryService service = createService(Map.of("logic_db", runtimeDatabaseConfig), databaseType);
        Set<String> actual = service.queryInformationSchemaColumnNames("logic_db", "public", "orders", List.of("status_cipher"));
        assertThat(actual, is(Set.of("status_cipher")));
    }
    
    private WorkflowProxyQueryService createService(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        return createService(runtimeDatabases, "MySQL");
    }
    
    private WorkflowProxyQueryService createService(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases, final String databaseType) {
        Map<String, RuntimeDatabaseConfiguration> capabilityRuntimeDatabases = new LinkedHashMap<>(runtimeDatabases.isEmpty() ? 0 : 1, 1F);
        if (!runtimeDatabases.isEmpty()) {
            capabilityRuntimeDatabases.put("logic_db", createCapabilityRuntimeDatabaseConfiguration(databaseType));
        }
        return new WorkflowProxyQueryService(new MCPSessionManager(runtimeDatabases), CoreDatabaseTypeFactoryMocker.createDatabaseCapabilityProvider(capabilityRuntimeDatabases));
    }
    
    private RuntimeDatabaseConfiguration createCapabilityRuntimeDatabaseConfiguration(final String databaseType) {
        RuntimeDatabaseConfiguration result = mock(RuntimeDatabaseConfiguration.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        try {
            when(result.openConnection("logic_db")).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(databaseMetaData);
            when(databaseMetaData.getDatabaseProductVersion()).thenReturn("");
            when(databaseMetaData.getURL()).thenReturn(CoreDatabaseTypeFactoryMocker.createJdbcUrl(databaseType));
            mockEmptyScalarQueries(connection);
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
        return result;
    }
    
    private void mockEmptyScalarQueries(final Connection connection) throws SQLException {
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
    }
}
