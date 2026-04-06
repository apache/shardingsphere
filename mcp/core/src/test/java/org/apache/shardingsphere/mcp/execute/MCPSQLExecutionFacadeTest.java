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

package org.apache.shardingsphere.mcp.execute;

import org.apache.shardingsphere.mcp.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResultKind;
import org.apache.shardingsphere.mcp.protocol.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.protocol.response.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.session.MCPSessionNotExistedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MCPSQLExecutionFacadeTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertClassifyCases")
    void assertClassify(final String name, final String sql, final SupportedMCPStatement expectedStatementClass, final String expectedStatementType,
                        final String expectedTargetObjectName, final String expectedSavepointName) {
        StatementClassifier classifier = new StatementClassifier();
        ClassificationResult actual = classifier.classify(sql);
        assertThat(actual.getStatementClass(), is(expectedStatementClass));
        assertThat(actual.getStatementType(), is(expectedStatementType));
        assertThat(actual.getTargetObjectName().orElse(""), is(expectedTargetObjectName));
        assertThat(actual.getSavepointName().orElse(""), is(expectedSavepointName));
    }
    
    static Stream<Arguments> assertClassifyCases() {
        return Stream.of(
                Arguments.of("query", "SELECT * FROM orders", SupportedMCPStatement.QUERY, "QUERY", "orders", ""),
                Arguments.of("dml", "UPDATE orders SET status = 'DONE'", SupportedMCPStatement.DML, "UPDATE", "orders", ""),
                Arguments.of("ddl", "CREATE TABLE orders", SupportedMCPStatement.DDL, "CREATE", "orders", ""),
                Arguments.of("dcl", "GRANT SELECT ON orders TO app_user", SupportedMCPStatement.DCL, "GRANT", "", ""),
                Arguments.of("transaction", "BEGIN", SupportedMCPStatement.TRANSACTION_CONTROL, "BEGIN", "", ""),
                Arguments.of("savepoint", "SAVEPOINT sp_1", SupportedMCPStatement.SAVEPOINT, "SAVEPOINT", "", "sp_1"),
                Arguments.of("explain analyze", "EXPLAIN ANALYZE SELECT * FROM orders", SupportedMCPStatement.EXPLAIN_ANALYZE, "EXPLAIN ANALYZE", "orders", ""));
    }
    
    @Test
    void assertClassifyWithBannedCommand() {
        StatementClassifier classifier = new StatementClassifier();
        UnsupportedOperationException actual = assertThrows(UnsupportedOperationException.class, () -> classifier.classify("SET search_path public"));
        assertThat(actual.getMessage(), is("Statement is banned by the MCP contract."));
    }
    
    @Test
    void assertClassifyWithMultipleStatements() {
        StatementClassifier classifier = new StatementClassifier();
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> classifier.classify("SELECT 1; SELECT 2"));
        assertThat(actual.getMessage(), is("Only one SQL statement is allowed."));
    }
    
    @Test
    void assertExecuteWithMissingSession() {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        MCPSessionManager sessionManager = new MCPSessionManager(createTransactionResourceManager(runtimeDatabaseConfig));
        MCPSQLExecutionFacade facade = createFacade(sessionManager, createMetadataCatalog("H2"));
        MCPSessionNotExistedException actual = assertThrows(MCPSessionNotExistedException.class, () -> facade.execute(createExecutionRequest("SELECT * FROM orders", 10)));
        assertThat(actual.getMessage(), is("Session does not exist."));
        verifyNoInteractions(runtimeDatabaseConfig);
    }
    
    @Test
    void assertExecuteWithUnknownCapability() {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        MCPSessionManager sessionManager = new MCPSessionManager(createTransactionResourceManager(runtimeDatabaseConfig));
        sessionManager.createSession("session-1");
        MCPSQLExecutionFacade facade = createFacade(sessionManager, createMetadataCatalog("Unknown"));
        DatabaseCapabilityNotFoundException actual = assertThrows(DatabaseCapabilityNotFoundException.class,
                () -> facade.execute(createExecutionRequest("SELECT * FROM orders", 10)));
        assertThat(actual.getMessage(), is("Database capability does not exist."));
        verifyNoInteractions(runtimeDatabaseConfig);
    }
    
    @Test
    void assertExecuteQueryWithTruncation() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mockRuntimeDatabaseConfiguration(createQueryConnection(createResultSet(
                List.of(new ExecuteQueryColumnDefinition("order_id", "INTEGER", "INTEGER", false),
                        new ExecuteQueryColumnDefinition("status", "VARCHAR", "VARCHAR", true)),
                List.of(List.of(1, "NEW"), List.of(2, "DONE")))));
        MCPSessionManager sessionManager = new MCPSessionManager(createTransactionResourceManager(runtimeDatabaseConfig));
        sessionManager.createSession("session-1");
        MCPSQLExecutionFacade facade = createFacade(sessionManager, createMetadataCatalog("H2"));
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("SELECT * FROM orders", 1));
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.RESULT_SET));
        assertThat(actual.getRows().size(), is(1));
        assertTrue(actual.isTruncated());
        verify(runtimeDatabaseConfig).openConnection("logic_db");
    }
    
    @Test
    void assertExecuteUpdate() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mockRuntimeDatabaseConfiguration(createUpdateConnection(3));
        MCPSessionManager sessionManager = new MCPSessionManager(createTransactionResourceManager(runtimeDatabaseConfig));
        sessionManager.createSession("session-1");
        MCPSQLExecutionFacade facade = createFacade(sessionManager, createMetadataCatalog("H2"));
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("UPDATE orders SET status = 'DONE'", 10));
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.UPDATE_COUNT));
        assertThat(actual.getAffectedRows(), is(3));
        verify(runtimeDatabaseConfig).openConnection("logic_db");
    }
    
    @Test
    void assertExecuteTransactionCommand() {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        MCPJdbcTransactionResourceManager transactionResourceManager = createTransactionResourceManager(runtimeDatabaseConfig);
        MCPSessionManager sessionManager = new MCPSessionManager(transactionResourceManager);
        sessionManager.createSession("session-1");
        MCPSQLExecutionFacade facade = createFacade(sessionManager, createMetadataCatalog("H2"));
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("BEGIN", 10));
        assertThat(actual.getMessage(), is("Transaction started."));
        verify(transactionResourceManager).beginTransaction("session-1", "logic_db");
        verifyNoInteractions(runtimeDatabaseConfig);
    }
    
    @Test
    void assertExecuteDdl() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mockRuntimeDatabaseConfiguration(
                createStatementAckConnection(), createMetadataConnection("public", "orders_archive", List.of("order_id", "status")));
        MCPSessionManager sessionManager = new MCPSessionManager(createTransactionResourceManager(runtimeDatabaseConfig));
        sessionManager.createSession("session-1");
        MCPDatabaseMetadataCatalog metadataCatalog = createMetadataCatalog("H2");
        MCPSQLExecutionFacade facade = createFacade(sessionManager, metadataCatalog);
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("CREATE TABLE orders_archive", 10));
        MCPDatabaseMetadata actualMetadata = metadataCatalog.findMetadata("logic_db").orElseThrow();
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.STATEMENT_ACK));
        assertThat(actual.getMessage(), is("Statement executed."));
        assertThat(actualMetadata.getSchemas().size(), is(1));
        assertThat(actualMetadata.getSchemas().get(0).getSchema(), is("public"));
        assertThat(actualMetadata.getSchemas().get(0).getTables().get(0).getTable(), is("orders_archive"));
        verify(runtimeDatabaseConfig, times(2)).openConnection("logic_db");
    }
    
    @Test
    void assertExecuteDcl() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mockRuntimeDatabaseConfiguration(
                createStatementAckConnection(), createMetadataConnection("public", "orders", List.of("order_id", "status")));
        MCPSessionManager sessionManager = new MCPSessionManager(createTransactionResourceManager(runtimeDatabaseConfig));
        sessionManager.createSession("session-1");
        MCPDatabaseMetadataCatalog metadataCatalog = createMetadataCatalog("H2");
        MCPSQLExecutionFacade facade = createFacade(sessionManager, metadataCatalog);
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("GRANT SELECT ON orders TO PUBLIC", 10));
        MCPDatabaseMetadata actualMetadata = metadataCatalog.findMetadata("logic_db").orElseThrow();
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.STATEMENT_ACK));
        assertThat(actual.getMessage(), is("Statement executed."));
        assertThat(actualMetadata.getSchemas().size(), is(1));
        assertThat(actualMetadata.getSchemas().get(0).getSchema(), is("public"));
        assertThat(actualMetadata.getSchemas().get(0).getTables().get(0).getTable(), is("orders"));
        verify(runtimeDatabaseConfig, times(2)).openConnection("logic_db");
    }
    
    @Test
    void assertExecuteExplainAnalyzeWithUnsupportedCapability() {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        MCPSessionManager sessionManager = new MCPSessionManager(createTransactionResourceManager(runtimeDatabaseConfig));
        sessionManager.createSession("session-1");
        MCPSQLExecutionFacade facade = createFacade(sessionManager, createMetadataCatalog("MySQL"));
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class,
                () -> facade.execute(createExecutionRequest("EXPLAIN ANALYZE SELECT * FROM orders", 10)));
        assertThat(actual.getMessage(), is("Statement class is not supported."));
        verifyNoInteractions(runtimeDatabaseConfig);
    }
    
    @Test
    void assertExecuteExplainAnalyzeWithSupportedCapability() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mockRuntimeDatabaseConfiguration(createQueryConnection(createResultSet(
                List.of(new ExecuteQueryColumnDefinition("plan", "VARCHAR", "VARCHAR", true)), List.of(List.of("plan")))));
        MCPSessionManager sessionManager = new MCPSessionManager(createTransactionResourceManager(runtimeDatabaseConfig));
        sessionManager.createSession("session-1");
        MCPSQLExecutionFacade facade = createFacade(sessionManager, createMetadataCatalog("H2"));
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("EXPLAIN ANALYZE SELECT * FROM orders", 10));
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.RESULT_SET));
        assertThat(actual.getRows().size(), is(1));
        verify(runtimeDatabaseConfig).openConnection("logic_db");
    }
    
    private MCPSQLExecutionFacade createFacade(final MCPSessionManager sessionManager, final MCPDatabaseMetadataCatalog metadataCatalog) {
        return new MCPSQLExecutionFacade(new MCPRuntimeContext(sessionManager, metadataCatalog));
    }
    
    private MCPDatabaseMetadataCatalog createMetadataCatalog(final String databaseType) {
        return new MCPDatabaseMetadataCatalog(Map.of("logic_db", new MCPDatabaseMetadata("logic_db", databaseType, "", Collections.emptyList())));
    }
    
    private MCPJdbcTransactionResourceManager createTransactionResourceManager(final RuntimeDatabaseConfiguration runtimeDatabaseConfig) {
        MCPJdbcTransactionResourceManager result = mock(MCPJdbcTransactionResourceManager.class);
        when(result.getRuntimeDatabases()).thenReturn(Map.of("logic_db", runtimeDatabaseConfig));
        when(result.findTransactionConnection("session-1", "logic_db")).thenReturn(Optional.empty());
        return result;
    }
    
    private ExecutionRequest createExecutionRequest(final String sql, final int maxRows) {
        return new ExecutionRequest("session-1", "logic_db", "public", sql, maxRows, 1000);
    }
    
    private RuntimeDatabaseConfiguration mockRuntimeDatabaseConfiguration(final Connection... connections) throws SQLException {
        RuntimeDatabaseConfiguration result = mock(RuntimeDatabaseConfiguration.class);
        if (1 == connections.length) {
            when(result.openConnection("logic_db")).thenReturn(connections[0]);
        } else {
            Connection[] remainingConnections = new Connection[connections.length - 1];
            System.arraycopy(connections, 1, remainingConnections, 0, remainingConnections.length);
            when(result.openConnection("logic_db")).thenReturn(connections[0], remainingConnections);
        }
        return result;
    }
    
    private Connection createQueryConnection(final ResultSet resultSet) throws SQLException {
        return createStatementConnection(true, 0, resultSet);
    }
    
    private Connection createUpdateConnection(final int updateCount) throws SQLException {
        return createStatementConnection(false, updateCount, null);
    }
    
    private Connection createStatementAckConnection() throws SQLException {
        return createStatementConnection(false, 0, null);
    }
    
    private Connection createStatementConnection(final boolean hasResultSet, final int updateCount, final ResultSet resultSet) throws SQLException {
        Connection result = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(result.createStatement()).thenReturn(statement);
        when(statement.execute(anyString())).thenReturn(hasResultSet);
        if (hasResultSet) {
            when(statement.getResultSet()).thenReturn(resultSet);
        } else {
            when(statement.getUpdateCount()).thenReturn(updateCount);
        }
        return result;
    }
    
    private Connection createMetadataConnection(final String schemaName, final String tableName, final List<String> columnNames) throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        ResultSet columnResultSet = createStringResultSet(columnNames.stream().map(each -> Map.of("COLUMN_NAME", each)).toList());
        ResultSet indexResultSet = createStringResultSet(Collections.emptyList());
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("2.2.224");
        when(databaseMetaData.getTables(nullable(String.class), nullable(String.class), anyString(), any(String[].class))).thenAnswer(invocation -> {
            String[] types = invocation.getArgument(3, String[].class);
            if (1 == types.length && "TABLE".equals(types[0])) {
                return createStringResultSet(List.of(Map.of("TABLE_SCHEM", schemaName, "TABLE_NAME", tableName)));
            }
            return createStringResultSet(Collections.emptyList());
        });
        when(databaseMetaData.getColumns(nullable(String.class), nullable(String.class), eq(tableName), anyString())).thenReturn(columnResultSet);
        when(databaseMetaData.getIndexInfo(nullable(String.class), nullable(String.class), eq(tableName), eq(false), eq(false))).thenReturn(indexResultSet);
        return result;
    }
    
    private ResultSet createResultSet(final List<ExecuteQueryColumnDefinition> columns, final List<List<Object>> rows) throws SQLException {
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
    
    private ResultSet createStringResultSet(final List<Map<String, String>> rows) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        AtomicInteger rowIndex = new AtomicInteger(-1);
        when(result.next()).thenAnswer(invocation -> rowIndex.incrementAndGet() < rows.size());
        when(result.getString(anyString())).thenAnswer(invocation -> rows.get(rowIndex.get()).get(invocation.getArgument(0, String.class)));
        return result;
    }
}
