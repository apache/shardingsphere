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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.support.database.exception.MCPDatabaseQueryFailedException;
import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCErrorCategory;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowProxyQueryServiceTest {
    
    @Test
    void assertQueryReturnsLowerCaseRows() throws SQLException {
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
        List<Map<String, Object>> actual = service.query("logic_db", "SHOW MASK RULES");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).get("table"), is("orders"));
        assertThat(actual.get(0).get("column"), is("status"));
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
        Exception actual = assertThrows(MCPUnavailableException.class, () -> service.query("logic_db", "SHOW MASK RULES"));
        assertThat(actual.getMessage(), is("Database `logic_db` is not configured."));
    }
    
    @Test
    void assertQueryClassifiesDatabaseFailure() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SHOW MASK RULES")).thenThrow(new SQLException("access denied", "42000", 1044));
        WorkflowProxyQueryService service = createService(Map.of("logic_db", runtimeDatabaseConfig));
        MCPDatabaseQueryFailedException actual = assertThrows(MCPDatabaseQueryFailedException.class, () -> service.query("logic_db", "SHOW MASK RULES"));
        assertThat(actual.getCategory(), is(MCPJDBCErrorCategory.AUTHORIZATION));
    }
    
    @Test
    void assertCheckDatabaseCapabilityWithoutCapability() {
        WorkflowProxyQueryService service = new WorkflowProxyQueryService(new MCPSessionManager(Map.of()), mock(MCPDatabaseCapabilityProvider.class));
        assertThrows(DatabaseCapabilityNotFoundException.class, () -> service.checkDatabaseCapability("logic_db"));
    }
    
    @Test
    void assertIsSameColumnIdentifier() {
        assertTrue(createService(Map.of()).isSameIdentifier("logic_db", IdentifierScope.COLUMN, "Phone", "phone"));
    }
    
    @Test
    void assertIsSameTableIdentifier() {
        assertFalse(createService(Map.of()).isSameIdentifier("logic_db", IdentifierScope.TABLE, "Phone", "phone"));
    }
    
    private WorkflowProxyQueryService createService(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        return createService(runtimeDatabases, "MySQL");
    }
    
    private WorkflowProxyQueryService createService(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases, final String databaseType) {
        MCPDatabaseCapability databaseCapability = mock(MCPDatabaseCapability.class);
        when(databaseCapability.getDatabaseType()).thenReturn(databaseType);
        IdentifierCasePolicySet insensitivePolicySet = IdentifierCasePolicyFactory.newInsensitivePolicySet();
        when(databaseCapability.getIdentifierCasePolicySet()).thenReturn(new IdentifierCasePolicySet(
                IdentifierCasePolicyFactory.newSensitivePolicySet().getPolicy(IdentifierScope.TABLE),
                Map.of(IdentifierScope.COLUMN, insensitivePolicySet.getPolicy(IdentifierScope.COLUMN))));
        MCPDatabaseCapabilityProvider databaseCapabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        when(databaseCapabilityProvider.provide("logic_db")).thenReturn(Optional.of(databaseCapability));
        return new WorkflowProxyQueryService(new MCPSessionManager(runtimeDatabases), databaseCapabilityProvider);
    }
    
}
