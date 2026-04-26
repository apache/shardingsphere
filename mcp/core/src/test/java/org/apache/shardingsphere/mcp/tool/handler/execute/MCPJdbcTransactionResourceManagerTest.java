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

package org.apache.shardingsphere.mcp.tool.handler.execute;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPJdbcTransactionResourceManagerTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertFindTransactionConnectionCases")
    void assertFindTransactionConnection(final String name,
                                         final String activeDatabaseName, final String databaseName, final boolean expectedPresent, final String expectedMessage) throws SQLException {
        Connection connection = mock(Connection.class);
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        MCPJdbcTransactionResourceManager manager = new MCPJdbcTransactionResourceManager(Map.of("logic_db", runtimeDatabaseConfig));
        if (null != activeDatabaseName) {
            manager.beginTransaction("session-1", activeDatabaseName);
        }
        if (null != expectedMessage) {
            assertThat(assertThrows(IllegalStateException.class, () -> manager.findTransactionConnection("session-1", databaseName)).getMessage(), is(expectedMessage));
            return;
        }
        Optional<Connection> actual = manager.findTransactionConnection("session-1", databaseName);
        assertThat(actual.isPresent(), is(expectedPresent));
        if (expectedPresent) {
            assertThat(actual.orElseThrow(IllegalStateException::new), is(connection));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertBeginTransactionCases")
    void assertBeginTransaction(final String name, final String configuredDatabaseName, final String activeDatabaseName, final String databaseName,
                                final String openFailureMessage, final String expectedMessage) throws SQLException {
        Connection connection = mock(Connection.class);
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        if (null != configuredDatabaseName && null == openFailureMessage) {
            when(runtimeDatabaseConfig.openConnection(configuredDatabaseName)).thenReturn(connection);
        }
        if (null != configuredDatabaseName && null != openFailureMessage) {
            when(runtimeDatabaseConfig.openConnection(configuredDatabaseName)).thenThrow(new SQLException(openFailureMessage));
        }
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = null == configuredDatabaseName ? Collections.emptyMap() : Map.of(configuredDatabaseName, runtimeDatabaseConfig);
        MCPJdbcTransactionResourceManager manager = new MCPJdbcTransactionResourceManager(runtimeDatabases);
        if (null != activeDatabaseName) {
            manager.beginTransaction("session-1", activeDatabaseName);
        }
        if (null != expectedMessage) {
            assertThat(assertThrows(IllegalStateException.class, () -> manager.beginTransaction("session-1", databaseName)).getMessage(), is(expectedMessage));
            return;
        }
        manager.beginTransaction("session-1", databaseName);
        verify(runtimeDatabaseConfig).openConnection(databaseName);
        verify(connection).setAutoCommit(false);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertCommitTransactionCases")
    void assertCommitTransaction(final String name, final boolean active, final String commitFailureMessage, final String expectedMessage) throws SQLException {
        Connection connection = mock(Connection.class);
        MCPJdbcTransactionResourceManager manager = createResourceManager(connection);
        if (active) {
            manager.beginTransaction("session-1", "logic_db");
        }
        if (null != commitFailureMessage) {
            doThrow(new SQLException(commitFailureMessage)).when(connection).commit();
        }
        if (null != expectedMessage) {
            assertThat(assertThrows(IllegalStateException.class, () -> manager.commitTransaction("session-1")).getMessage(), is(expectedMessage));
            return;
        }
        manager.commitTransaction("session-1");
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }
    
    @Test
    void assertCommitTransactionFailureKeepsTransactionResource() throws SQLException {
        Connection connection = mock(Connection.class);
        MCPJdbcTransactionResourceManager manager = createResourceManager(connection);
        manager.beginTransaction("session-1", "logic_db");
        doThrow(new SQLException("commit failed")).when(connection).commit();
        assertThat(assertThrows(IllegalStateException.class, () -> manager.commitTransaction("session-1")).getMessage(), is("commit failed"));
        assertTrue(manager.findTransactionConnection("session-1", "logic_db").isPresent());
        verify(connection).commit();
        verify(connection, never()).setAutoCommit(true);
        verify(connection, never()).close();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertRollbackTransactionCases")
    void assertRollbackTransaction(final String name, final boolean active, final String rollbackFailureMessage, final String expectedMessage) throws SQLException {
        Connection connection = mock(Connection.class);
        MCPJdbcTransactionResourceManager manager = createResourceManager(connection);
        if (active) {
            manager.beginTransaction("session-1", "logic_db");
        }
        if (null != rollbackFailureMessage) {
            doThrow(new SQLException(rollbackFailureMessage)).when(connection).rollback();
        }
        if (null != expectedMessage) {
            assertThat(assertThrows(IllegalStateException.class, () -> manager.rollbackTransaction("session-1")).getMessage(), is(expectedMessage));
            return;
        }
        manager.rollbackTransaction("session-1");
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertCreateSavepointCases")
    void assertCreateSavepoint(final String name, final String savepointName,
                               final String savepointFailureMessage, final Class<? extends RuntimeException> expectedExceptionType, final String expectedMessage) throws SQLException {
        Connection connection = mock(Connection.class);
        Savepoint savepoint = mock(Savepoint.class);
        MCPJdbcTransactionResourceManager manager = createResourceManager(connection);
        manager.beginTransaction("session-1", "logic_db");
        if (null == expectedExceptionType) {
            when(connection.setSavepoint("SP_1")).thenReturn(savepoint);
            manager.createSavepoint("session-1", savepointName);
            verify(connection).setSavepoint("SP_1");
            return;
        }
        if (null != savepointFailureMessage) {
            when(connection.setSavepoint("SP_1")).thenThrow(new SQLException(savepointFailureMessage));
        }
        assertThat(assertThrows(expectedExceptionType, () -> manager.createSavepoint("session-1", savepointName)).getMessage(), is(expectedMessage));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertRollbackToSavepointCases")
    void assertRollbackToSavepoint(final String name, final boolean savepointExists, final String rollbackFailureMessage, final String expectedMessage) throws SQLException {
        Connection connection = mock(Connection.class);
        Savepoint savepoint = mock(Savepoint.class);
        when(connection.setSavepoint("SP_1")).thenReturn(savepoint);
        MCPJdbcTransactionResourceManager manager = createResourceManager(connection);
        manager.beginTransaction("session-1", "logic_db");
        if (savepointExists) {
            manager.createSavepoint("session-1", "sp_1");
        }
        if (null != rollbackFailureMessage) {
            doThrow(new SQLException(rollbackFailureMessage)).when(connection).rollback(savepoint);
        }
        if (null != expectedMessage) {
            assertThat(assertThrows(IllegalStateException.class, () -> manager.rollbackToSavepoint("session-1", "sp_1")).getMessage(), is(expectedMessage));
            return;
        }
        manager.rollbackToSavepoint("session-1", "sp_1");
        verify(connection).rollback(savepoint);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertReleaseSavepointCases")
    void assertReleaseSavepoint(final String name, final boolean savepointExists, final String releaseFailureMessage, final String expectedMessage) throws SQLException {
        Connection connection = mock(Connection.class);
        Savepoint savepoint = mock(Savepoint.class);
        when(connection.setSavepoint("SP_1")).thenReturn(savepoint);
        MCPJdbcTransactionResourceManager manager = createResourceManager(connection);
        manager.beginTransaction("session-1", "logic_db");
        if (savepointExists) {
            manager.createSavepoint("session-1", "sp_1");
        }
        if (null != releaseFailureMessage) {
            doThrow(new SQLException(releaseFailureMessage)).when(connection).releaseSavepoint(savepoint);
        }
        if (null != expectedMessage) {
            assertThat(assertThrows(IllegalStateException.class, () -> manager.releaseSavepoint("session-1", "sp_1")).getMessage(), is(expectedMessage));
            return;
        }
        manager.releaseSavepoint("session-1", "sp_1");
        verify(connection).releaseSavepoint(savepoint);
    }
    
    @Test
    void assertCloseSession() throws SQLException {
        TransactionState state = new TransactionState("NEW");
        Connection connection = createTransactionalConnection(state);
        MCPJdbcTransactionResourceManager manager = createResourceManager(connection);
        manager.beginTransaction("session-1", "logic_db");
        try (Statement statement = manager.findTransactionConnection("session-1", "logic_db").orElseThrow(IllegalStateException::new).createStatement()) {
            statement.execute("UPDATE public.orders SET status = 'PROCESSING' WHERE order_id = 1");
        }
        assertThat(state.getCurrentStatus(), is("PROCESSING"));
        manager.closeSession("session-1");
        assertThat(state.getCurrentStatus(), is("NEW"));
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }
    
    @Test
    void assertCloseSessionWithoutTransaction() {
        assertDoesNotThrow(() -> new MCPJdbcTransactionResourceManager(Collections.emptyMap()).closeSession("session-1"));
    }
    
    private MCPJdbcTransactionResourceManager createResourceManager(final Connection connection) throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        return new MCPJdbcTransactionResourceManager(Map.of("logic_db", runtimeDatabaseConfig));
    }
    
    private Connection createTransactionalConnection(final TransactionState state) throws SQLException {
        Connection result = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(result.createStatement()).thenReturn(statement);
        when(statement.execute("UPDATE public.orders SET status = 'PROCESSING' WHERE order_id = 1")).thenAnswer(invocation -> {
            state.update("PROCESSING");
            return true;
        });
        doAnswer(invocation -> {
            state.rollback();
            return null;
        }).when(result).rollback();
        return result;
    }
    
    private static Stream<Arguments> assertFindTransactionConnectionCases() {
        return Stream.of(
                Arguments.of("active transaction", "logic_db", "logic_db", true, null),
                Arguments.of("no active transaction", null, "logic_db", false, null),
                Arguments.of("cross database transaction", "logic_db", "analytics_db", false, "Cross-database transaction switching is not supported."));
    }
    
    private static Stream<Arguments> assertBeginTransactionCases() {
        return Stream.of(
                Arguments.of("new transaction", "logic_db", null, "logic_db", null, null),
                Arguments.of("already active transaction", "logic_db", "logic_db", "logic_db", null, "Transaction already active."),
                Arguments.of("cross database transaction", "logic_db", "logic_db", "analytics_db", null, "Cross-database transaction switching is not supported."),
                Arguments.of("missing database", null, null, "logic_db", null, "Database `logic_db` is not configured."),
                Arguments.of("open connection failure", "logic_db", null, "logic_db", "open failed", "open failed"));
    }
    
    private static Stream<Arguments> assertCommitTransactionCases() {
        return Stream.of(
                Arguments.of("commit active transaction", true, null, null),
                Arguments.of("commit without transaction", false, null, "No active transaction."),
                Arguments.of("commit failure", true, "commit failed", "commit failed"));
    }
    
    private static Stream<Arguments> assertRollbackTransactionCases() {
        return Stream.of(
                Arguments.of("rollback active transaction", true, null, null),
                Arguments.of("rollback without transaction", false, null, "No active transaction."),
                Arguments.of("rollback failure", true, "rollback failed", "rollback failed"));
    }
    
    private static Stream<Arguments> assertRollbackToSavepointCases() {
        return Stream.of(
                Arguments.of("rollback to savepoint", true, null, null),
                Arguments.of("missing savepoint", false, null, "Savepoint does not exist."),
                Arguments.of("rollback to savepoint failure", true, "rollback to savepoint failed", "rollback to savepoint failed"));
    }
    
    private static Stream<Arguments> assertCreateSavepointCases() {
        return Stream.of(
                Arguments.of("create savepoint", "sp_1", null, null, null),
                Arguments.of("empty savepoint name", " ", null, IllegalArgumentException.class, "Savepoint name is required."),
                Arguments.of("savepoint creation failure", "sp_1", "savepoint failed", IllegalStateException.class, "savepoint failed"));
    }
    
    private static Stream<Arguments> assertReleaseSavepointCases() {
        return Stream.of(
                Arguments.of("release savepoint", true, null, null),
                Arguments.of("missing savepoint", false, null, "Savepoint does not exist."),
                Arguments.of("release savepoint failure", true, "release savepoint failed", "release savepoint failed"));
    }
    
    private static final class TransactionState {
        
        private final String initialStatus;
        
        private String workingStatus;
        
        private TransactionState(final String initialStatus) {
            this.initialStatus = initialStatus;
            workingStatus = initialStatus;
        }
        
        private void update(final String newStatus) {
            workingStatus = newStatus;
        }
        
        private void rollback() {
            workingStatus = initialStatus;
        }
        
        private String getCurrentStatus() {
            return workingStatus;
        }
    }
}
