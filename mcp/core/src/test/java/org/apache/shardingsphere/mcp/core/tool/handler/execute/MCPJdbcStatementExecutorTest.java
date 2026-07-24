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

import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.SchemaExecutionSemantics;
import org.apache.shardingsphere.mcp.support.database.exception.QueryDidNotReturnResultSetException;
import org.apache.shardingsphere.mcp.support.database.exception.StatementClassNotSupportedException;
import org.apache.shardingsphere.mcp.support.database.exception.MCPDatabaseQueryFailedException;
import org.apache.shardingsphere.mcp.support.database.exception.MCPDatabaseSQLSyntaxException;
import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCErrorCategory;
import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCExceptionClassifier;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionColumnDefinition;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResult;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResultKind;
import org.apache.shardingsphere.mcp.api.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.api.exception.ShardingSphereMCPException;
import org.apache.shardingsphere.mcp.api.exception.MCPTimeoutException;
import org.apache.shardingsphere.mcp.api.exception.MCPTransactionStateException;
import org.apache.shardingsphere.mcp.api.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.api.exception.MCPUnsupportedException;
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
import java.sql.SQLTransientConnectionException;
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
                       final SQLExecutionResultKind expectedResultKind, final String expectedStatementType, final int expectedRowCount, final int expectedAffectedRows,
                       final boolean expectedTruncated) throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", runtimeDatabaseConfig), transactionResourceManager);
        SQLExecutionResult actual = statementExecutor.execute(executionRequest, classificationResult, createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE));
        assertThat(actual.getResultKind(), is(expectedResultKind));
        assertThat(actual.getStatementClass(), is(classificationResult.getStatementClass()));
        assertThat(actual.getStatementType(), is(expectedStatementType));
        assertThat(actual.getRows().size(), is(expectedRowCount));
        assertThat(actual.getAffectedRows(), is(expectedAffectedRows));
        assertThat(actual.isTruncated(), is(expectedTruncated));
        assertThat(actual.getAppliedMaxRows(), is(executionRequest.getMaxRows()));
        assertThat(actual.getAppliedTimeoutMs(), is(executionRequest.getTimeoutMs()));
        assertThat(actual.getNormalizedSql(), is(classificationResult.getNormalizedSql()));
        verify(runtimeDatabaseConfig).openConnection("logic_db");
    }
    
    @Test
    void assertExecuteWithTransactionConnection() throws SQLException {
        Connection connection = createStatementConnection(false, 2, null);
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.of(connection));
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Collections.emptyMap(), transactionResourceManager);
        SQLExecutionResult actual = statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "UPDATE orders SET status = 'DONE'", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'DONE'", "", List.of(), false),
                createDatabaseCapability(SchemaExecutionSemantics.BEST_EFFORT));
        assertThat(actual.getAffectedRows(), is(2));
        verify(connection).setSchema("public");
        verify(connection, never()).close();
    }
    
    @Test
    void assertExecuteReadOnlyQueryWithRollback() throws SQLException {
        Statement statement = mock(Statement.class);
        ResultSet resultSet = createResultSet(createColumns(), List.of(List.of(1, "NEW")));
        when(statement.execute(anyString())).thenReturn(true);
        when(statement.getResultSet()).thenReturn(resultSet);
        Connection connection = createStatementConnection(statement);
        when(connection.isReadOnly()).thenReturn(false);
        when(connection.getAutoCommit()).thenReturn(true);
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        SQLExecutionResult actual = statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "SELECT status FROM orders", 10, 1000, true),
                new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT status FROM orders", "", List.of(), false),
                createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE));
        assertThat(actual.getRows().size(), is(1));
        verify(connection).setReadOnly(true);
        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).setReadOnly(false);
        verify(connection).close();
    }
    
    @Test
    void assertExecuteReadOnlyQueryWithTransactionConnection() throws SQLException {
        Connection connection = createStatementConnection(true, 0, createResultSet(createColumns(), List.of(List.of(1, "NEW"))));
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.of(connection));
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Collections.emptyMap(), transactionResourceManager);
        SQLExecutionResult actual = statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "SELECT status FROM orders", 10, 1000, true),
                new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT status FROM orders", "", List.of(), false),
                createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE));
        assertThat(actual.getRows().size(), is(1));
        verify(connection, never()).setReadOnly(true);
        verify(connection, never()).rollback();
        verify(connection, never()).commit();
        verify(connection, never()).setAutoCommit(false);
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
        SQLExecutionResult actual = statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", " ", "SELECT status FROM orders", 0, 0),
                new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT status FROM orders", "", List.of(), false),
                createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE));
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
        SQLExecutionResult actual = statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "SELECT status FROM orders", Integer.MAX_VALUE, 1000),
                new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT status FROM orders", "", List.of(), false),
                createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE));
        assertThat(actual.getRows().size(), is(0));
        assertFalse(actual.isTruncated());
        verify(statement).setMaxRows(Integer.MAX_VALUE);
    }
    
    @Test
    void assertExecuteWithDmlResultSet() throws SQLException {
        Statement statement = mock(Statement.class);
        ResultSet resultSet = createResultSet(List.of(new SQLExecutionColumnDefinition("order_id", "INTEGER", "INTEGER", false)), List.of(List.of(1)));
        when(statement.execute(anyString())).thenReturn(true);
        when(statement.getResultSet()).thenReturn(resultSet);
        Connection connection = createStatementConnection(statement);
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        SQLExecutionResult actual = statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "WITH updated_orders AS (UPDATE orders SET status = 'DONE' RETURNING *) SELECT * FROM updated_orders", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.DML, "SELECT",
                        "WITH updated_orders AS (UPDATE orders SET status = 'DONE' RETURNING *) SELECT * FROM updated_orders", "", List.of(), false),
                createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE));
        assertThat(actual.getResultKind(), is(SQLExecutionResultKind.RESULT_SET));
        assertThat(actual.getStatementClass(), is(SupportedMCPStatement.DML));
        assertThat(actual.getStatementType(), is("SELECT"));
        assertThat(actual.getRows().size(), is(1));
    }
    
    @Test
    void assertExecuteWithFixedSchemaSemantics() throws SQLException {
        Connection connection = createStatementConnection(false, 1, null);
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        SQLExecutionResult actual = statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "logic_db", "UPDATE orders SET status = 'DONE'", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'DONE'", "", List.of(), false),
                createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE));
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
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "UPDATE orders SET status = 'DONE'", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'DONE'", "", List.of(), false),
                createDatabaseCapability(SchemaExecutionSemantics.BEST_EFFORT)));
        assertThat(actual.getMessage(), is("schema"));
        verify(connection).setSchema("public");
        verify(connection, never()).createStatement();
        verify(connection).close();
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
                new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT status FROM orders", "", List.of(), false),
                createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE)));
        assertThat(actual.getMessage(), is("Query did not return a result set."));
    }
    
    @Test
    void assertExecuteWithUnsupportedStatementClass() throws SQLException {
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        Connection connection = createStatementConnection(false, 0, null);
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        StatementClassNotSupportedException actual = assertThrows(StatementClassNotSupportedException.class, () -> statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "BEGIN", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.TRANSACTION_CONTROL, "BEGIN", "BEGIN", "", List.of(), false), createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE)));
        assertThat(actual.getMessage(), is("Statement class is not supported."));
    }
    
    @Test
    void assertExecuteRuleDistSQLSyntaxError() throws SQLException {
        SQLException cause = new SQLException("syntax error", "42601");
        Statement statement = mock(Statement.class);
        when(statement.execute(anyString())).thenThrow(cause);
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        Connection connection = createStatementConnection(statement);
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("sharding_db", databaseConfig), transactionResourceManager);
        ClassificationResult classificationResult = new ClassificationResult(SupportedMCPStatement.DDL, "CREATE",
                "CREATE SHARDING TABLE RULE t_order(DATANODES('ds_${0..1}.t_order_${0..1}'))", "", List.of(), true);
        RuleDistSQLExecutionException actual = assertThrows(RuleDistSQLExecutionException.class, () -> statementExecutor.execute(new SQLExecutionRequest("session-1",
                "sharding_db", "public", "CREATE SHARDING TABLE RULE t_order(DATANODES('ds_${0..1}.t_order_${0..1}'))", 10, 1000),
                classificationResult, createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE)));
        assertThat(actual.getDatabase(), is("sharding_db"));
        assertThat(actual.getClassificationResult(), is(classificationResult));
        assertThat(actual.getCause(), is(cause));
        assertThat(actual.getMessage(),
                is("Rule DistSQL execution failed for database `sharding_db`; check MCP runtime capability and workflow guidance before asking for corrected SQL."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteWithSQLExceptionCases")
    void assertExecuteWithSQLException(final String name, final String databaseType, final SQLException sqlException,
                                       final Class<? extends ShardingSphereMCPException> expectedExceptionClass, final MCPJDBCErrorCategory expectedCategory) throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.execute(anyString())).thenThrow(sqlException);
        doThrow(new SQLException("statement close failed")).when(statement).close();
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenReturn(Optional.empty());
        Connection connection = createStatementConnection(statement);
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        MCPDatabaseCapability databaseCapability = createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE);
        when(databaseCapability.getDatabaseType()).thenReturn(databaseType);
        ShardingSphereMCPException actual = assertThrows(expectedExceptionClass, () -> statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "SELECT status FROM orders", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT status FROM orders", "", List.of(), false),
                databaseCapability));
        assertThat(actual.getClass(), is(expectedExceptionClass));
        assertThat(actual.getMessage(), is(sqlException.getMessage()));
        assertThat(actual.getCause(), is(sqlException));
        assertThat(MCPJDBCExceptionClassifier.classify(databaseType, actual), is(expectedCategory));
    }
    
    @Test
    void assertExecuteWithTransactionStateException() {
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        IllegalStateException cause = new IllegalStateException("Transaction already active.");
        when(transactionResourceManager.findTransactionConnection(anyString(), anyString())).thenThrow(cause);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Collections.emptyMap(), transactionResourceManager);
        MCPTransactionStateException actual = assertThrows(MCPTransactionStateException.class, () -> statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "SELECT status FROM orders", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT status FROM orders", "", List.of(), false),
                createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE)));
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
                new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT status FROM orders", "", List.of(), false),
                createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE)));
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
        Connection connection = createStatementConnection(statement);
        RuntimeDatabaseConfiguration databaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(databaseConfig.openConnection(anyString())).thenReturn(connection);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(Map.of("logic_db", databaseConfig), transactionResourceManager);
        MCPQueryFailedException actual = assertThrows(MCPQueryFailedException.class, () -> statementExecutor.execute(new SQLExecutionRequest("session-1", "logic_db",
                "public", "UPDATE orders SET status = 'DONE'", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'DONE'", "", List.of(), false),
                createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE)));
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
        SQLExecutionResult actual = statementExecutor.execute(new SQLExecutionRequest("session-1",
                "logic_db", "public", "UPDATE orders SET status = 'DONE'", 10, 1000),
                new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'DONE'", "", List.of(), false),
                createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE));
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
                new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT status FROM orders", "", List.of(), false),
                createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE)));
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
                new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT status FROM orders", "", List.of(), false),
                createDatabaseCapability(SchemaExecutionSemantics.FIXED_TO_DATABASE)));
    }
    
    private static MCPDatabaseCapability createDatabaseCapability(final SchemaExecutionSemantics schemaExecutionSemantics) {
        MCPDatabaseCapability result = mock(MCPDatabaseCapability.class);
        when(result.getSchemaExecutionSemantics()).thenReturn(schemaExecutionSemantics);
        when(result.getDatabaseType()).thenReturn("PostgreSQL");
        return result;
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
    
    private static ResultSet createResultSet(final List<SQLExecutionColumnDefinition> columns, final List<List<Object>> rows) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        AtomicInteger rowIndex = new AtomicInteger(-1);
        when(result.getMetaData()).thenReturn(resultSetMetaData);
        when(result.next()).thenAnswer(invocation -> rowIndex.incrementAndGet() < rows.size());
        when(result.getObject(anyInt())).thenAnswer(invocation -> rows.get(rowIndex.get()).get(invocation.getArgument(0, Integer.class) - 1));
        when(resultSetMetaData.getColumnCount()).thenReturn(columns.size());
        for (int index = 0; index < columns.size(); index++) {
            SQLExecutionColumnDefinition column = columns.get(index);
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
                        new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT status FROM orders ORDER BY order_id", "", List.of(), false),
                        createStatementConnection(true, 0, createResultSet(createColumns(), List.of(List.of(1, "NEW"), List.of(2, "DONE")))),
                        SQLExecutionResultKind.RESULT_SET, "SELECT", 1, 0, true),
                Arguments.of("explain result set", new SQLExecutionRequest("session-1", "logic_db", "public", "EXPLAIN SELECT * FROM orders", 10, 1000),
                        new ClassificationResult(SupportedMCPStatement.EXPLAIN, "EXPLAIN", "EXPLAIN SELECT * FROM orders", "", List.of(), false),
                        createStatementConnection(true, 0, createResultSet(List.of(new SQLExecutionColumnDefinition("plan", "VARCHAR", "VARCHAR", true)), List.of(List.of("plan")))),
                        SQLExecutionResultKind.RESULT_SET, "EXPLAIN", 1, 0, false),
                Arguments.of("dml update count", new SQLExecutionRequest("session-1", "logic_db", "public", "UPDATE orders SET status = 'DONE'", 10, 1000),
                        new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'DONE'", "", List.of(), false), createStatementConnection(false, 3, null),
                        SQLExecutionResultKind.UPDATE_COUNT, "UPDATE", 0, 3, false),
                Arguments.of("ddl ack", new SQLExecutionRequest("session-1", "logic_db", "public", "CREATE TABLE orders_archive", 10, 1000),
                        new ClassificationResult(SupportedMCPStatement.DDL, "CREATE", "CREATE TABLE orders_archive", "", List.of(), false), createStatementConnection(false, 0, null),
                        SQLExecutionResultKind.STATEMENT_ACK, "CREATE", 0, 0, false),
                Arguments.of("dcl ack", new SQLExecutionRequest("session-1", "logic_db", "public", "GRANT SELECT ON orders TO app_user", 10, 1000),
                        new ClassificationResult(SupportedMCPStatement.DCL, "GRANT", "GRANT SELECT ON orders TO app_user", "", List.of(), false), createStatementConnection(false, 0, null),
                        SQLExecutionResultKind.STATEMENT_ACK, "GRANT", 0, 0, false));
    }
    
    private static Stream<Arguments> assertExecuteWithSQLExceptionCases() {
        return Stream.of(
                Arguments.of("timeout", "PostgreSQL", new SQLTimeoutException("timeout"), MCPTimeoutException.class, MCPJDBCErrorCategory.TIMEOUT),
                Arguments.of("unsupported feature", "PostgreSQL", new SQLFeatureNotSupportedException("unsupported feature"), MCPUnsupportedException.class,
                        MCPJDBCErrorCategory.FEATURE_NOT_SUPPORTED),
                Arguments.of("typed syntax error", "PostgreSQL", new SQLSyntaxErrorException("syntax error"), MCPDatabaseSQLSyntaxException.class, MCPJDBCErrorCategory.SYNTAX),
                Arguments.of("SQLState syntax error", "PostgreSQL", new SQLException("syntax error", "42601"), MCPDatabaseSQLSyntaxException.class, MCPJDBCErrorCategory.SYNTAX),
                Arguments.of("MySQL syntax error", "MySQL", new SQLException("syntax error", "42000", 1064), MCPDatabaseSQLSyntaxException.class, MCPJDBCErrorCategory.SYNTAX),
                Arguments.of("object not visible", "PostgreSQL", new SQLException("missing table", "42P01"), MCPDatabaseQueryFailedException.class,
                        MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE),
                Arguments.of("insufficient privileges", "PostgreSQL", new SQLException("permission denied", "42501"), MCPDatabaseQueryFailedException.class,
                        MCPJDBCErrorCategory.AUTHORIZATION),
                Arguments.of("MySQL insufficient privileges", "MySQL", new SQLException("permission denied", "42000", 1044), MCPDatabaseQueryFailedException.class,
                        MCPJDBCErrorCategory.AUTHORIZATION),
                Arguments.of("connection interrupted", "PostgreSQL", new SQLTransientConnectionException("connection lost", "08006"), MCPDatabaseQueryFailedException.class,
                        MCPJDBCErrorCategory.CONNECTION),
                Arguments.of("ambiguous MySQL error", "MySQL", new SQLSyntaxErrorException("query failed", "42000", 1055), MCPDatabaseQueryFailedException.class,
                        MCPJDBCErrorCategory.QUERY_FAILED),
                Arguments.of("query failed", "PostgreSQL", new SQLException("query failed"), MCPDatabaseQueryFailedException.class, MCPJDBCErrorCategory.QUERY_FAILED));
    }
    
    private static List<SQLExecutionColumnDefinition> createColumns() {
        return List.of(
                new SQLExecutionColumnDefinition("order_id", "INTEGER", "INTEGER", false),
                new SQLExecutionColumnDefinition("status", "VARCHAR", "VARCHAR", true));
    }
}
