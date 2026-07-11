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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DefaultSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaSemantics;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

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
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockDialectDatabaseMetaData("MySQL", QuoteCharacter.BACK_QUOTE, DialectSchemaSemantics.DATABASE_AS_SCHEMA, typedSPILoader, databaseTypedSPILoader);
            WorkflowProxyQueryService service = createService(Map.of("logic_db", runtimeDatabaseConfig));
            String actual = service.queryColumnDefinition("`logic_db`", "`public`", "order detail", "amount due");
            assertThat(actual, is("VARCHAR(4000)"));
            verify(connection).setSchema("public");
        }
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
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockDialectDatabaseMetaData("PostgreSQL", QuoteCharacter.QUOTE, DialectSchemaSemantics.NATIVE_SCHEMA, typedSPILoader, databaseTypedSPILoader);
            WorkflowProxyQueryService service = createService(Map.of("logic_db", runtimeDatabaseConfig), "PostgreSQL");
            String actual = service.queryColumnDefinition("\"logic_db\"", "\"public\"", "order detail", "amount due");
            assertThat(actual, is("VARCHAR(4000)"));
            verify(connection).setSchema("public");
        }
    }
    
    private WorkflowProxyQueryService createService(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        return createService(runtimeDatabases, "MySQL");
    }
    
    private WorkflowProxyQueryService createService(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases, final String databaseType) {
        Map<String, RuntimeDatabaseConfiguration> capabilityRuntimeDatabases = new LinkedHashMap<>(runtimeDatabases.isEmpty() ? 0 : 1, 1F);
        if (!runtimeDatabases.isEmpty()) {
            capabilityRuntimeDatabases.put("logic_db", createCapabilityRuntimeDatabaseConfiguration());
        }
        try (MockedStatic<DatabaseTypeFactory> ignored = mockDatabaseTypeFactory(databaseType)) {
            return new WorkflowProxyQueryService(new MCPSessionManager(runtimeDatabases), new MCPDatabaseCapabilityProvider(capabilityRuntimeDatabases));
        }
    }
    
    private MockedStatic<DatabaseTypeFactory> mockDatabaseTypeFactory(final String databaseType) {
        MockedStatic<DatabaseTypeFactory> result = mockStatic(DatabaseTypeFactory.class, CALLS_REAL_METHODS);
        DatabaseType databaseTypeFromMetadata = mock(DatabaseType.class);
        when(databaseTypeFromMetadata.getType()).thenReturn(databaseType);
        result.when(() -> DatabaseTypeFactory.get(any(DatabaseMetaData.class))).thenReturn(databaseTypeFromMetadata);
        return result;
    }
    
    private void mockDialectDatabaseMetaData(final String databaseType, final QuoteCharacter quoteCharacter, final DialectSchemaSemantics schemaSemantics,
                                             final MockedStatic<TypedSPILoader> typedSPILoader, final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        DatabaseType databaseTypeFromSPI = mock(DatabaseType.class);
        when(databaseTypeFromSPI.getTrunkDatabaseType()).thenReturn(Optional.empty());
        typedSPILoader.when(() -> TypedSPILoader.findService(DatabaseType.class, databaseType)).thenReturn(Optional.of(databaseTypeFromSPI));
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getQuoteCharacter()).thenReturn(quoteCharacter);
        when(dialectDatabaseMetaData.getSchemaOption()).thenReturn(new DefaultSchemaOption(false, null, schemaSemantics));
        when(dialectDatabaseMetaData.getExplainOption()).thenReturn(() -> false);
        when(dialectDatabaseMetaData.getSequenceOption()).thenReturn(Optional.empty());
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectDatabaseMetaData.class, databaseTypeFromSPI)).thenReturn(Optional.of(dialectDatabaseMetaData));
    }
    
    private RuntimeDatabaseConfiguration createCapabilityRuntimeDatabaseConfiguration() {
        RuntimeDatabaseConfiguration result = mock(RuntimeDatabaseConfiguration.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        try {
            when(result.openConnection("logic_db")).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(databaseMetaData);
            when(databaseMetaData.getDatabaseProductVersion()).thenReturn("");
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
