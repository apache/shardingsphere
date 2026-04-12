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

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityOption;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResultKind;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPProtocolException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPTimeoutException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPTransactionStateException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.protocol.exception.QueryDidNotReturnResultSetException;
import org.apache.shardingsphere.mcp.protocol.exception.StatementClassNotSupportedException;
import org.apache.shardingsphere.mcp.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.tool.response.SQLExecutionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPJdbcStatementExecutorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteCases")
    void assertExecute(final String name, final SQLExecutionRequest executionRequest, final ClassificationResult classificationResult, final Connection connection,
                       final ExecuteQueryResultKind expectedResultKind, final String expectedStatementType, final int expectedRowCount, final int expectedAffectedRows,
                       final String expectedMessage, final boolean expectedTruncated) throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", runtimeDatabaseConfig), transactionResourceManager);
        SQLExecutionResponse actual = statementExecutor.execute(executionRequest, classificationResult, createDatabaseCapability("H2"));
        assertThat(actual.getResultKind(), is(expectedResultKind));
        assertThat(actual.getStatementType(), is(expectedStatementType));
        assertThat(actual.getRows().size(), is(expectedRowCount));
        assertThat(actual.getAffectedRows(), is(expectedAffectedRows));
        assertThat(actual.getMessage(), is(expectedMessage));
        assertThat(actual.isTruncated(), is(expectedTruncated));
        verify(runtimeDatabaseConfig).openConnection("logic_db");
    }
    
    @Test
    void assertExecuteWithTransactionConnection() throws SQLException {
        Connection connection = createStatementConnection(false, 2, null);
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.of(connection));
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Collections.emptyMap(), transactionResourceManager);
        SQLExecutionResponse actual = statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "UPDATE orders SET status = 'DONE'", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'DONE'", "", ""), createDatabaseCapability("H2"));
        assertThat(actual.getAffectedRows(), is(2));
        verify(connection, never()).close();
    }
    
    @Test
    void assertExecuteWithBlankSchemaAndNoLimits() throws SQLException {
        Statement statement = mock(Statement.class);
        ResultSet resultSet = createResultSet(createColumns(), List.of(List.of(1, "NEW"), List.of(2, "DONE")));
        when(statement.execute(anyString())).thenReturn(true);
        when(statement.getResultSet()).thenReturn(resultSet);
        Connection connection = createStatementConnection(statement);
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        SQLExecutionResponse actual = statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", " ", "SELECT status FROM orders", 0, 0),
                new ClassificationResult(SupportedMCPStatement.QUERY, "QUERY", "SELECT status FROM orders", "", ""), createDatabaseCapability("H2"));
        assertThat(actual.getRows().size(), is(2));
        assertFalse(actual.isTruncated());
        verify(connection, never()).setSchema(anyString());
        verify(statement, never()).setMaxRows(anyInt());
        verify(statement, never()).setQueryTimeout(anyInt());
    }
    
    @Test
    void assertExecuteWithUnboundedStatementMaxRows() throws SQLException {
        Statement statement = mock(Statement.class);
        ResultSet resultSet = createResultSet(createColumns(), Collections.emptyList());
        when(statement.execute(anyString())).thenReturn(true);
        when(statement.getResultSet()).thenReturn(resultSet);
        Connection connection = createStatementConnection(statement);
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        SQLExecutionResponse actual = statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "SELECT status FROM orders", Integer.MAX_VALUE, 1000),
                new ClassificationResult(SupportedMCPStatement.QUERY, "QUERY", "SELECT status FROM orders", "", ""), createDatabaseCapability("H2"));
        assertThat(actual.getRows().size(), is(0));
        assertFalse(actual.isTruncated());
        verify(statement).setMaxRows(Integer.MAX_VALUE);
    }
    
    @Test
    void assertExecuteWithFixedSchemaSemantics() throws SQLException {
        Connection connection = createStatementConnection(false, 1, null);
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        SQLExecutionResponse actual = statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "logic_db", "UPDATE orders SET status = 'DONE'", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'DONE'", "", ""), createDatabaseCapability("MySQL"));
        assertThat(actual.getAffectedRows(), is(1));
        verify(connection, never()).setSchema(anyString());
    }
    
    @Test
    void assertExecuteWithUnsupportedSchema() throws SQLException {
        Connection connection = createStatementConnection(false, 1, null);
        doThrow(new SQLFeatureNotSupportedException("schema")).when(connection).setSchema("public");
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        SQLExecutionResponse actual = statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "UPDATE orders SET status = 'DONE'", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'DONE'", "", ""), createDatabaseCapability("H2"));
        assertThat(actual.getAffectedRows(), is(1));
        verify(connection).setSchema("public");
    }
    
    @Test
    void assertExecuteWithQueryWithoutResultSet() throws SQLException {
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        Connection connection = createStatementConnection(false, 0, null);
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        QueryDidNotReturnResultSetException actual = assertThrows(QueryDidNotReturnResultSetException.class, () -> statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "SELECT status FROM orders", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.QUERY, "QUERY", "SELECT status FROM orders", "", ""), createDatabaseCapability("H2")));
        assertThat(actual.getMessage(), is("Query did not return a result set."));
    }
    
    @Test
    void assertExecuteWithUnsupportedStatementClass() throws SQLException {
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        final Connection connection = createStatementConnection(false, 0, null);
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        StatementClassNotSupportedException actual = assertThrows(StatementClassNotSupportedException.class, () -> statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "BEGIN", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.TRANSACTION_CONTROL, "BEGIN", "BEGIN", "", ""), createDatabaseCapability("H2")));
        assertThat(actual.getMessage(), is("Statement class is not supported."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteWithSQLExceptionCases")
    void assertExecuteWithSQLException(final String name, final SQLException sqlException, final Class<? extends MCPProtocolException> expectedExceptionClass,
                                       final String expectedMessage) throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.execute(anyString())).thenThrow(sqlException);
        doThrow(new SQLException("statement close failed")).when(statement).close();
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        final Connection connection = createStatementConnection(statement);
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        MCPProtocolException actual = assertThrows(expectedExceptionClass, () -> statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "SELECT status FROM orders", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.QUERY, "QUERY", "SELECT status FROM orders", "", ""), createDatabaseCapability("H2")));
        assertThat(actual.getClass(), is(expectedExceptionClass));
        assertThat(actual.getMessage(), is(expectedMessage));
        assertThat(actual.getCause(), is(sqlException));
    }
    
    @Test
    void assertExecuteWithTransactionStateException() {
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        IllegalStateException cause = new IllegalStateException("Transaction already active.");
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenThrow(cause);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Collections.emptyMap(), transactionResourceManager);
        MCPTransactionStateException actual = assertThrows(MCPTransactionStateException.class, () -> statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "SELECT status FROM orders", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.QUERY, "QUERY", "SELECT status FROM orders", "", ""), createDatabaseCapability("H2")));
        assertThat(actual.getMessage(), is("Transaction already active."));
        assertThat(actual.getCause(), is(cause));
    }
    
    @Test
    void assertExecuteWithMissingDatabase() {
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Collections.emptyMap(), transactionResourceManager);
        MCPUnavailableException actual = assertThrows(MCPUnavailableException.class, () -> statementExecutor.execute(new SQLExecutionRequest("session-1",
                "missing_db", "public", "SELECT status FROM orders", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.QUERY, "QUERY", "SELECT status FROM orders", "", ""), createDatabaseCapability("H2")));
        assertThat(actual.getMessage(), is("Database `missing_db` is not configured."));
    }
    
    @Test
    void assertExecuteWithStatementCloseFailure() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.execute(anyString())).thenReturn(false);
        when(statement.getUpdateCount()).thenReturn(1);
        doThrow(new SQLException("statement close failed")).when(statement).close();
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        final Connection connection = createStatementConnection(statement);
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        MCPQueryFailedException actual = assertThrows(MCPQueryFailedException.class, () -> statementExecutor.execute(new SQLExecutionRequest("session-1", "logic_db",
                "public", "UPDATE orders SET status = 'DONE'", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'DONE'", "", ""), createDatabaseCapability("H2")));
        assertThat(actual.getMessage(), is("statement close failed"));
    }
    
    @Test
    void assertExecuteWithConnectionCloseFailure() throws SQLException {
        Connection connection = createStatementConnection(false, 1, null);
        doThrow(new SQLException("connection close failed")).when(connection).close();
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        SQLExecutionResponse actual = statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "UPDATE orders SET status = 'DONE'", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'DONE'", "", ""), createDatabaseCapability("H2"));
        assertThat(actual.getAffectedRows(), is(1));
        verify(connection).close();
    }
    
    @Test
    void assertExecuteWithNullConnection() throws SQLException {
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(null);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        assertThrows(NullPointerException.class, () -> statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "", "SELECT status FROM orders", 0, 0),
                new ClassificationResult(SupportedMCPStatement.QUERY, "QUERY", "SELECT status FROM orders", "", ""), createDatabaseCapability("H2")));
    }
    
    @Test
    void assertExecuteWithNullStatement() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.createStatement()).thenReturn(null);
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        assertThrows(NullPointerException.class, () -> statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "", "SELECT status FROM orders", 0, 0),
                new ClassificationResult(SupportedMCPStatement.QUERY, "QUERY", "SELECT status FROM orders", "", ""), createDatabaseCapability("H2")));
    }
    
    private static MCPDatabaseCapability createDatabaseCapability(final String databaseType) {
        return new MCPDatabaseCapability("logic_db", "", TypedSPILoader.getService(MCPDatabaseCapabilityOption.class, databaseType));
    }
    
    private static Connection createStatementConnection(final boolean hasResultSet, final int updateCount, final ResultSet resultSet) throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.execute(anyString())).thenReturn(hasResultSet);
        if (hasResultSet) {
            when(statement.getResultSet()).thenReturn(resultSet);
        } else {
            when(statement.getUpdateCount()).thenReturn(updateCount);
        }
        return createStatementConnection(statement);
    }
    
    private static Connection createStatementConnection(final Statement statement) throws SQLException {
        Connection result = mock(Connection.class);
        when(result.createStatement()).thenReturn(statement);
        return result;
    }
    
    private static ResultSet createResultSet(final List<ExecuteQueryColumnDefinition> columns, final List<List<Object>> rows) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        AtomicInteger rowIndex = new AtomicInteger(-1);
        when(result.getMetaData()).thenReturn(resultSetMetaData);
        when(result.next()).thenAnswer(invocation -> rowIndex.incrementAndGet() < rows.size());
        when(result.getObject(anyInt())).thenAnswer(invocation -> rows.get(rowIndex.get()).get(invocation.getArgument(0, Integer.class) - 1));
        when(resultSetMetaData.getColumnCount()).thenReturn(columns.size());
        for (int index = 0; index < columns.size(); index++) {
            ExecuteQueryColumnDefinition column = columns.get(index);
            int columnIndex = index + 1;
            when(resultSetMetaData.getColumnLabel(columnIndex)).thenReturn(column.getColumnName());
            when(resultSetMetaData.getColumnTypeName(columnIndex)).thenReturn(column.getNativeType());
            when(resultSetMetaData.isNullable(columnIndex)).thenReturn(column.isNullable() ? ResultSetMetaData.columnNullable : ResultSetMetaData.columnNoNulls);
        }
        return result;
    }
    
    private static Stream<Arguments> assertExecuteCases() throws SQLException {
        return Stream.of(
                Arguments.of("query result set", new SQLExecutionRequest("session-1", "logic_db", "public", "SELECT status FROM orders ORDER BY order_id", 1, 1000),
                        new ClassificationResult(SupportedMCPStatement.QUERY, "QUERY", "SELECT status FROM orders ORDER BY order_id", "", ""),
                        createStatementConnection(true, 0, createResultSet(createColumns(), List.of(List.of(1, "NEW"), List.of(2, "DONE")))),
                        ExecuteQueryResultKind.RESULT_SET, "QUERY", 1, 0, "", true),
                Arguments.of("explain analyze result set", new SQLExecutionRequest("session-1", "logic_db", "public", "EXPLAIN ANALYZE SELECT * FROM orders", 10, 1000),
                        new ClassificationResult(SupportedMCPStatement.EXPLAIN_ANALYZE, "EXPLAIN ANALYZE", "EXPLAIN ANALYZE SELECT * FROM orders", "", ""),
                        createStatementConnection(true, 0, createResultSet(List.of(new ExecuteQueryColumnDefinition("plan", "VARCHAR", "VARCHAR", true)), List.of(List.of("plan")))),
                        ExecuteQueryResultKind.RESULT_SET, "QUERY", 1, 0, "", false),
                Arguments.of("dml update count", new SQLExecutionRequest("session-1", "logic_db", "public", "UPDATE orders SET status = 'DONE'", 10, 1000),
                        new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'DONE'", "", ""), createStatementConnection(false, 3, null),
                        ExecuteQueryResultKind.UPDATE_COUNT, "UPDATE", 0, 3, "", false),
                Arguments.of("ddl ack", new SQLExecutionRequest("session-1", "logic_db", "public", "CREATE TABLE orders_archive", 10, 1000),
                        new ClassificationResult(SupportedMCPStatement.DDL, "CREATE", "CREATE TABLE orders_archive", "", ""), createStatementConnection(false, 0, null),
                        ExecuteQueryResultKind.STATEMENT_ACK, "CREATE", 0, 0, "Statement executed.", false),
                Arguments.of("dcl ack", new SQLExecutionRequest("session-1", "logic_db", "public", "GRANT SELECT ON orders TO app_user", 10, 1000),
                        new ClassificationResult(SupportedMCPStatement.DCL, "GRANT", "GRANT SELECT ON orders TO app_user", "", ""), createStatementConnection(false, 0, null),
                        ExecuteQueryResultKind.STATEMENT_ACK, "GRANT", 0, 0, "Statement executed.", false));
    }
    
    private static Stream<Arguments> assertExecuteWithSQLExceptionCases() {
        return Stream.of(
                Arguments.of("timeout", new SQLTimeoutException("timeout"), MCPTimeoutException.class, "timeout"),
                Arguments.of("unsupported feature", new SQLFeatureNotSupportedException("unsupported feature"), MCPUnsupportedException.class, "unsupported feature"),
                Arguments.of("syntax error", new SQLSyntaxErrorException("syntax error"), MCPInvalidRequestException.class, "syntax error"),
                Arguments.of("query failed", new SQLException("query failed"), MCPQueryFailedException.class, "query failed"));
    }
    
    private static List<ExecuteQueryColumnDefinition> createColumns() {
        return List.of(
                new ExecuteQueryColumnDefinition("order_id", "INTEGER", "INTEGER", false),
                new ExecuteQueryColumnDefinition("status", "VARCHAR", "VARCHAR", true));
    }
}
