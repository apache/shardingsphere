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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.exception.MCPTransactionStateException;
import org.apache.shardingsphere.mcp.api.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.core.session.MCPSessionNotExistedException;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.metadata.TransactionCapability;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResult;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResultKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    void assertExecute(final String name, final String sql, final SupportedMCPStatement statementClass, final String expectedStatementType,
                       final String savepointName) throws SQLException {
        Connection connection = mock(Connection.class);
        Savepoint savepoint = mock(Savepoint.class);
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of("logic_db", runtimeDatabaseConfig));
        sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        prepareTransactionState(sql, sessionManager, connection, savepoint);
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager);
        ClassificationResult classificationResult = new ClassificationResult(statementClass, expectedStatementType, sql, savepointName, List.of(), false);
        SQLExecutionResult actual = executor.execute("session-1", "logic_db", createCapability(), classificationResult);
        assertThat(actual.getResultKind(), is(SQLExecutionResultKind.STATEMENT_ACK));
        assertThat(actual.getStatementClass(), is(classificationResult.getStatementClass()));
        assertThat(actual.getStatementType(), is(expectedStatementType));
        assertThat(actual.getAppliedMaxRows(), is(0));
        assertThat(actual.getAppliedTimeoutMs(), is(0));
        assertThat(actual.getNormalizedSql(), is(sql));
        assertDatabaseExecution(sql, sessionManager, runtimeDatabaseConfig, connection, savepoint);
    }
    
    static Stream<Arguments> assertExecuteCases() {
        return Stream.of(
                Arguments.of("begin", "BEGIN", SupportedMCPStatement.TRANSACTION_CONTROL, "BEGIN", ""),
                Arguments.of("start transaction", "START TRANSACTION", SupportedMCPStatement.TRANSACTION_CONTROL, "START TRANSACTION", ""),
                Arguments.of("commit", "COMMIT", SupportedMCPStatement.TRANSACTION_CONTROL, "COMMIT", ""),
                Arguments.of("rollback", "ROLLBACK", SupportedMCPStatement.TRANSACTION_CONTROL, "ROLLBACK", ""),
                Arguments.of("savepoint", "SAVEPOINT sp_1", SupportedMCPStatement.SAVEPOINT, "SAVEPOINT", "sp_1"),
                Arguments.of("rollback to savepoint", "ROLLBACK TO SAVEPOINT sp_1", SupportedMCPStatement.SAVEPOINT, "ROLLBACK TO SAVEPOINT", "sp_1"),
                Arguments.of("release savepoint", "RELEASE SAVEPOINT sp_1", SupportedMCPStatement.SAVEPOINT, "RELEASE SAVEPOINT", "sp_1"));
    }
    
    @Test
    void assertExecuteWithUnsupportedSavepoint() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager);
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class,
                () -> executor.execute("session-1", "warehouse", createCapabilityWithoutSavepoint(),
                        new ClassificationResult(SupportedMCPStatement.SAVEPOINT, "SAVEPOINT", "SAVEPOINT sp_1", "sp_1", List.of(), false)));
        assertThat(actual.getMessage(), is("Savepoint is not supported."));
    }
    
    @Test
    void assertExecuteWithInvalidCommand() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager);
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> executor.execute("session-1", "logic_db", createCapability(),
                        new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT 1", "", List.of(), false)));
        assertThat(actual.getMessage(), is("Statement is not a transaction command."));
    }
    
    @Test
    void assertExecuteWithMissingSession() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager);
        MCPSessionNotExistedException actual = assertThrows(MCPSessionNotExistedException.class,
                () -> executor.execute("session-1", "logic_db", createCapability(),
                        new ClassificationResult(SupportedMCPStatement.TRANSACTION_CONTROL, "BEGIN", "BEGIN", "", List.of(), false)));
        assertThat(actual.getMessage(), is("Session does not exist."));
    }
    
    @Test
    void assertExecuteWithNoActiveTransaction() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager);
        MCPTransactionStateException actual = assertThrows(MCPTransactionStateException.class,
                () -> executor.execute("session-1", "logic_db", createCapability(),
                        new ClassificationResult(SupportedMCPStatement.TRANSACTION_CONTROL, "COMMIT", "COMMIT", "", List.of(), false)));
        assertThat(actual.getMessage(), is("No active transaction."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteWithMissingSavepointNameCases")
    void assertExecuteWithMissingSavepointName(final String name, final String statementType, final String sql) {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager);
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> executor.execute("session-1", "logic_db", createCapability(),
                        new ClassificationResult(SupportedMCPStatement.SAVEPOINT, statementType, sql, "", List.of(), false)));
        assertThat(actual.getMessage(), is("Savepoint name is required."));
    }
    
    private MCPDatabaseCapability createCapability() {
        return createCapability(TransactionCapability.LOCAL_WITH_SAVEPOINT);
    }
    
    private MCPDatabaseCapability createCapability(final TransactionCapability transactionCapability) {
        MCPDatabaseCapability result = mock(MCPDatabaseCapability.class);
        when(result.supportsTransactionControl()).thenReturn(TransactionCapability.NONE != transactionCapability);
        when(result.supportsSavepoint()).thenReturn(TransactionCapability.LOCAL_WITH_SAVEPOINT == transactionCapability);
        return result;
    }
    
    private MCPDatabaseCapability createCapabilityWithoutSavepoint() {
        return createCapability(TransactionCapability.LOCAL);
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
    
    static Stream<Arguments> assertExecuteWithMissingSavepointNameCases() {
        return Stream.of(
                Arguments.of("savepoint", "SAVEPOINT", "SAVEPOINT"),
                Arguments.of("rollback to savepoint", "ROLLBACK TO SAVEPOINT", "ROLLBACK TO SAVEPOINT"),
                Arguments.of("release savepoint", "RELEASE SAVEPOINT", "RELEASE SAVEPOINT"));
    }
}
