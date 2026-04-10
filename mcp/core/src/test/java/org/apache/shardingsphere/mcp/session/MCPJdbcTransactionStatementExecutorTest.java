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

package org.apache.shardingsphere.mcp.session;

import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.tool.handler.execute.MCPJdbcTransactionStatementExecutor;
import org.apache.shardingsphere.mcp.tool.handler.execute.StatementClassifier;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPTransactionStateException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.tool.response.SQLExecutionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;
import java.sql.Connection;
import java.sql.Savepoint;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPJdbcTransactionStatementExecutorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteCases")
    void assertExecute(final String name, final String sql, final String expectedStatementType, final String expectedMessage) throws SQLException {
        Connection connection = mock(Connection.class);
        Savepoint savepoint = mock(Savepoint.class);
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of("logic_db", runtimeDatabaseConfig));
        sessionManager.createSession("session-1");
        prepareTransactionState(sql, sessionManager, connection, savepoint);
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager);
        SQLExecutionResponse actual = executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify(sql));
        assertThat(actual.getStatementType(), is(expectedStatementType));
        assertThat(actual.getMessage(), is(expectedMessage));
        assertDatabaseExecution(sql, sessionManager, runtimeDatabaseConfig, connection, savepoint);
    }
    
    static Stream<Arguments> assertExecuteCases() {
        return Stream.of(
                Arguments.of("begin", "BEGIN", "BEGIN", "Transaction started."),
                Arguments.of("start transaction", "START TRANSACTION", "START TRANSACTION", "Transaction started."),
                Arguments.of("commit", "COMMIT", "COMMIT", "Transaction committed."),
                Arguments.of("rollback", "ROLLBACK", "ROLLBACK", "Transaction rolled back."),
                Arguments.of("savepoint", "SAVEPOINT sp_1", "SAVEPOINT", "Savepoint created."),
                Arguments.of("rollback to savepoint", "ROLLBACK TO SAVEPOINT sp_1", "ROLLBACK TO SAVEPOINT", "Savepoint rolled back."),
                Arguments.of("release savepoint", "RELEASE SAVEPOINT sp_1", "RELEASE SAVEPOINT", "Savepoint released."));
    }
    
    @Test
    void assertExecuteWithUnsupportedSavepoint() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession("session-1");
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager);
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class,
                () -> executor.execute("session-1", "warehouse", createCapability("warehouse"), new StatementClassifier().classify("SAVEPOINT sp_1")));
        assertThat(actual.getMessage(), is("Savepoint is not supported."));
    }
    
    @Test
    void assertExecuteWithInvalidCommand() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession("session-1");
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager);
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify("SELECT 1")));
        assertThat(actual.getMessage(), is("Statement is not a transaction command."));
    }
    
    @Test
    void assertExecuteWithMissingSession() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager);
        MCPSessionNotExistedException actual = assertThrows(MCPSessionNotExistedException.class,
                () -> executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify("BEGIN")));
        assertThat(actual.getMessage(), is("Session does not exist."));
    }
    
    @Test
    void assertExecuteWithNoActiveTransaction() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession("session-1");
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager);
        MCPTransactionStateException actual = assertThrows(MCPTransactionStateException.class,
                () -> executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify("COMMIT")));
        assertThat(actual.getMessage(), is("No active transaction."));
    }
    
    private MCPDatabaseCapabilityProvider createDatabaseCapabilityBuilder() {
        return new MCPDatabaseCapabilityProvider(new MCPDatabaseMetadataCatalog(Map.of(
                "logic_db", new MCPDatabaseMetadata("logic_db", "MySQL", "", Collections.emptyList()),
                "warehouse", new MCPDatabaseMetadata("warehouse", "Hive", "", Collections.emptyList()))));
    }
    
    private MCPDatabaseCapability createCapability(final String databaseName) {
        return createDatabaseCapabilityBuilder().provide(databaseName).orElseThrow(IllegalStateException::new);
    }
    
    private void prepareTransactionState(final String sql, final MCPSessionManager sessionManager, final Connection connection, final Savepoint savepoint) throws SQLException {
        if ("BEGIN".equals(sql) || "START TRANSACTION".equals(sql)) {
            return;
        }
        sessionManager.getTransactionResourceManager().beginTransaction("session-1", "logic_db");
        if (sql.startsWith("SAVEPOINT") || sql.startsWith("ROLLBACK TO SAVEPOINT") || sql.startsWith("RELEASE SAVEPOINT")) {
            when(connection.setSavepoint("SP_1")).thenReturn(savepoint);
        }
        if (sql.startsWith("ROLLBACK TO SAVEPOINT") || sql.startsWith("RELEASE SAVEPOINT")) {
            sessionManager.getTransactionResourceManager().createSavepoint("session-1", "sp_1");
        }
    }
    
    private void assertDatabaseExecution(final String sql, final MCPSessionManager sessionManager, final RuntimeDatabaseConfiguration runtimeDatabaseConfig,
                                         final Connection connection, final Savepoint savepoint) throws SQLException {
        if ("BEGIN".equals(sql) || "START TRANSACTION".equals(sql)) {
            verify(runtimeDatabaseConfig).openConnection("logic_db");
            verify(connection).setAutoCommit(false);
            assertTrue(sessionManager.getTransactionResourceManager().findTransactionConnection("session-1", "logic_db").isPresent());
            return;
        }
        if ("COMMIT".equals(sql)) {
            verify(connection).commit();
            verify(connection).setAutoCommit(true);
            verify(connection).close();
            assertFalse(sessionManager.getTransactionResourceManager().findTransactionConnection("session-1", "logic_db").isPresent());
            return;
        }
        if ("ROLLBACK".equals(sql)) {
            verify(connection).rollback();
            verify(connection).setAutoCommit(true);
            verify(connection).close();
            assertFalse(sessionManager.getTransactionResourceManager().findTransactionConnection("session-1", "logic_db").isPresent());
            return;
        }
        if (sql.startsWith("SAVEPOINT")) {
            verify(connection).setSavepoint("SP_1");
            return;
        }
        if (sql.startsWith("ROLLBACK TO SAVEPOINT")) {
            verify(connection).rollback(savepoint);
            return;
        }
        verify(connection).releaseSavepoint(savepoint);
    }
}
