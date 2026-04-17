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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowProxyQueryServiceTest {
    
    @Test
    void assertQueryReturnsLowerCaseRowsAndAppliesSchema() throws Exception {
        WorkflowProxyQueryService service = new WorkflowProxyQueryService();
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
        List<Map<String, Object>> actual = service.query(createRuntimeContext(Map.of("logic_db", runtimeDatabaseConfig)), "logic_db", "public", "SHOW MASK RULES");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).get("table"), is("orders"));
        assertThat(actual.get(0).get("column"), is("status"));
        verify(connection).setSchema("public");
    }
    
    @Test
    void assertQueryWithAnyDatabaseDelegatesToAvailableDatabase() throws Exception {
        WorkflowProxyQueryService service = new WorkflowProxyQueryService();
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
        List<Map<String, Object>> actual = service.queryWithAnyDatabase(createRuntimeContext(Map.of("logic_db", runtimeDatabaseConfig)), "SHOW ENCRYPT ALGORITHM PLUGINS");
        assertThat(actual.get(0).get("type"), is("AES"));
    }
    
    @Test
    void assertQueryWithAnyDatabaseThrowsWhenNoRuntimeDatabaseExists() {
        WorkflowProxyQueryService service = new WorkflowProxyQueryService();
        Exception actual = assertThrows(RuntimeException.class,
                () -> service.queryWithAnyDatabase(createRuntimeContext(Map.of()), "SHOW ENCRYPT ALGORITHM PLUGINS"));
        assertThat(actual.getMessage(), is("No runtime database is configured."));
    }
    
    @Test
    void assertQueryThrowsWhenDatabaseIsNotConfigured() {
        WorkflowProxyQueryService service = new WorkflowProxyQueryService();
        Exception actual = assertThrows(MCPUnavailableException.class,
                () -> service.query(createRuntimeContext(Map.of()), "logic_db", "", "SHOW MASK RULES"));
        assertThat(actual.getMessage(), is("Database `logic_db` is not configured."));
    }
    
    @Test
    void assertQueryColumnDefinitionFormatsDecimalDefinition() throws Exception {
        WorkflowProxyQueryService service = new WorkflowProxyQueryService();
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
        String actual = service.queryColumnDefinition(createRuntimeContext(Map.of("logic_db", runtimeDatabaseConfig)), "logic_db", "public", "orders", "amount");
        assertThat(actual, is("DECIMAL(10, 2)"));
    }
    
    @Test
    void assertQueryColumnDefinitionReturnsDefaultWhenMetadataIsEmpty() throws Exception {
        WorkflowProxyQueryService service = new WorkflowProxyQueryService();
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
        String actual = service.queryColumnDefinition(createRuntimeContext(Map.of("logic_db", runtimeDatabaseConfig)), "logic_db", "public", "orders", "amount");
        assertThat(actual, is("VARCHAR(4000)"));
    }
    
    @Test
    void assertQueryInformationSchemaColumnNamesReturnsDistinctColumnNames() throws Exception {
        WorkflowProxyQueryService service = new WorkflowProxyQueryService();
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
        Set<String> columnNames = new LinkedHashSet<>(List.of("status_cipher", "status_assisted_query"));
        Set<String> actual = service.queryInformationSchemaColumnNames(createRuntimeContext(Map.of("logic_db", runtimeDatabaseConfig)),
                "logic_db", "public", "orders", columnNames);
        assertThat(actual, is(Set.of("status_cipher", "status_assisted_query")));
    }
    
    @Test
    void assertQueryInformationSchemaColumnNamesUsesSchemaFilterForPostgreSql() throws Exception {
        WorkflowProxyQueryService service = new WorkflowProxyQueryService();
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
        Set<String> actual = service.queryInformationSchemaColumnNames(createRuntimeContext(Map.of("logic_db", runtimeDatabaseConfig), "PostgreSQL"),
                "logic_db", "public", "orders", new LinkedHashSet<>(List.of("status_cipher")));
        assertThat(actual, is(Set.of("status_cipher")));
    }
    
    private MCPRuntimeContext createRuntimeContext(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        return createRuntimeContext(runtimeDatabases, "MySQL");
    }
    
    private MCPRuntimeContext createRuntimeContext(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases, final String databaseType) {
        MCPDatabaseMetadata databaseMetadata = ResourceTestDataFactory.createDatabaseMetadataCatalog().findMetadata("logic_db").orElseThrow();
        MCPDatabaseMetadataCatalog metadataCatalog = new MCPDatabaseMetadataCatalog(Map.of("logic_db",
                new MCPDatabaseMetadata(databaseMetadata.getDatabase(), databaseType, databaseMetadata.getDatabaseVersion(), databaseMetadata.getSchemas())));
        return new MCPRuntimeContext(new MCPSessionManager(runtimeDatabases), metadataCatalog);
    }
}
