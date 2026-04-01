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

import org.apache.shardingsphere.mcp.capability.MCPCapabilityBuilder;
import org.apache.shardingsphere.mcp.capability.StatementClass;
import org.apache.shardingsphere.mcp.metadata.MCPJdbcMetadataRefresher;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResultKind;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MCPSQLExecutionFacadeTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertClassifyCases")
    void assertClassify(final String name, final String sql, final StatementClass expectedStatementClass, final String expectedStatementType,
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
                Arguments.of("query", "SELECT * FROM orders", StatementClass.QUERY, "QUERY", "orders", ""),
                Arguments.of("dml", "UPDATE orders SET status = 'DONE'", StatementClass.DML, "UPDATE", "orders", ""),
                Arguments.of("ddl", "CREATE TABLE orders", StatementClass.DDL, "CREATE", "orders", ""),
                Arguments.of("dcl", "GRANT SELECT ON orders TO app_user", StatementClass.DCL, "GRANT", "", ""),
                Arguments.of("transaction", "BEGIN", StatementClass.TRANSACTION_CONTROL, "BEGIN", "", ""),
                Arguments.of("savepoint", "SAVEPOINT sp_1", StatementClass.SAVEPOINT, "SAVEPOINT", "", "sp_1"),
                Arguments.of("explain analyze", "EXPLAIN ANALYZE SELECT * FROM orders", StatementClass.EXPLAIN_ANALYZE, "EXPLAIN ANALYZE", "orders", ""));
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
        MCPSessionManager sessionManager = new MCPSessionManager(mock());
        MCPCapabilityBuilder capabilityBuilder = new MCPCapabilityBuilder(
                new DatabaseMetadataSnapshots(Map.of("logic_db", new DatabaseMetadataSnapshot("MySQL", "", Collections.emptyList()))));
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPSQLExecutionFacade facade = new MCPSQLExecutionFacade(
                capabilityBuilder, new MCPSessionExecutionCoordinator(sessionManager), new MCPJdbcTransactionStatementExecutor(sessionManager, mock()), statementExecutor, mock());
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("SELECT * FROM orders", 10));
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getErrorCode(), is(MCPErrorCode.NOT_FOUND));
        assertThat(actual.getError().get().getMessage(), is("Session does not exist."));
        verifyNoInteractions(statementExecutor);
    }
    
    @Test
    void assertExecuteWithUnknownCapability() {
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        final MCPSessionManager sessionManager = new MCPSessionManager(mock());
        final MCPJdbcTransactionResourceManager jdbcTransactionResourceManager = mock();
        final MCPJdbcMetadataRefresher metadataRefreshCoordinator = mock();
        if (!sessionManager.hasSession("session-1")) {
            sessionManager.createSession("session-1");
        }
        MCPCapabilityBuilder capabilityBuilder = new MCPCapabilityBuilder(
                new DatabaseMetadataSnapshots(Map.of("logic_db", new DatabaseMetadataSnapshot("Unknown", "", Collections.emptyList()))));
        MCPSQLExecutionFacade facade = new MCPSQLExecutionFacade(capabilityBuilder, new MCPSessionExecutionCoordinator(sessionManager), new MCPJdbcTransactionStatementExecutor(sessionManager, jdbcTransactionResourceManager), statementExecutor, metadataRefreshCoordinator);
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("SELECT * FROM orders", 10));
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getErrorCode(), is(MCPErrorCode.NOT_FOUND));
        verifyNoInteractions(statementExecutor);
    }
    
    @Test
    void assertExecuteQueryWithTruncation() {
        MCPJdbcStatementExecutor statementExecutor = createStatementExecutor(createResultSetResponse(1, true));
        final MCPSessionManager sessionManager = new MCPSessionManager(mock());
        final MCPJdbcTransactionResourceManager jdbcTransactionResourceManager = mock();
        final MCPJdbcMetadataRefresher metadataRefreshCoordinator = mock();
        if (!sessionManager.hasSession("session-1")) {
            sessionManager.createSession("session-1");
        }
        MCPCapabilityBuilder capabilityBuilder = new MCPCapabilityBuilder(
                new DatabaseMetadataSnapshots(Map.of("logic_db", new DatabaseMetadataSnapshot("MySQL", "", Collections.emptyList()))));
        MCPSQLExecutionFacade facade = new MCPSQLExecutionFacade(capabilityBuilder, new MCPSessionExecutionCoordinator(sessionManager), new MCPJdbcTransactionStatementExecutor(sessionManager, jdbcTransactionResourceManager), statementExecutor, metadataRefreshCoordinator);
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("SELECT * FROM orders", 1));
        assertTrue(actual.isSuccessful());
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.RESULT_SET));
        assertThat(actual.getRows().size(), is(1));
        assertTrue(actual.isTruncated());
        verify(statementExecutor).execute(any(ExecutionRequest.class), any(ClassificationResult.class));
    }
    
    @Test
    void assertExecuteUpdate() {
        MCPJdbcStatementExecutor statementExecutor = createStatementExecutor(ExecuteQueryResponse.updateCount("UPDATE", 3));
        final MCPSessionManager sessionManager = new MCPSessionManager(mock());
        final MCPJdbcTransactionResourceManager jdbcTransactionResourceManager = mock();
        final MCPJdbcMetadataRefresher metadataRefreshCoordinator = mock();
        if (!sessionManager.hasSession("session-1")) {
            sessionManager.createSession("session-1");
        }
        MCPCapabilityBuilder capabilityBuilder = new MCPCapabilityBuilder(
                new DatabaseMetadataSnapshots(Map.of("logic_db", new DatabaseMetadataSnapshot("MySQL", "", Collections.emptyList()))));
        MCPSQLExecutionFacade facade = new MCPSQLExecutionFacade(capabilityBuilder, new MCPSessionExecutionCoordinator(sessionManager), new MCPJdbcTransactionStatementExecutor(sessionManager, jdbcTransactionResourceManager), statementExecutor, metadataRefreshCoordinator);
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("UPDATE orders SET status = 'DONE'", 10));
        assertTrue(actual.isSuccessful());
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.UPDATE_COUNT));
        assertThat(actual.getAffectedRows(), is(3));
        verify(statementExecutor).execute(any(ExecutionRequest.class), any(ClassificationResult.class));
    }
    
    @Test
    void assertExecuteTransactionCommand() {
        MCPSessionManager sessionManager = new MCPSessionManager(mock());
        sessionManager.createSession("session-1");
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        final MCPJdbcMetadataRefresher metadataRefreshCoordinator = mock();
        if (!sessionManager.hasSession("session-1")) {
            sessionManager.createSession("session-1");
        }
        MCPCapabilityBuilder capabilityBuilder = new MCPCapabilityBuilder(
                new DatabaseMetadataSnapshots(Map.of("logic_db", new DatabaseMetadataSnapshot("MySQL", "", Collections.emptyList()))));
        MCPSQLExecutionFacade facade = new MCPSQLExecutionFacade(capabilityBuilder, new MCPSessionExecutionCoordinator(sessionManager), new MCPJdbcTransactionStatementExecutor(sessionManager, transactionResourceManager), statementExecutor, metadataRefreshCoordinator);
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("BEGIN", 10));
        assertTrue(actual.isSuccessful());
        assertThat(actual.getMessage(), is("Transaction started."));
        verify(transactionResourceManager).beginTransaction("session-1", "logic_db");
    }
    
    @Test
    void assertExecuteDdl() {
        MCPJdbcMetadataRefresher metadataRefresher = mock(MCPJdbcMetadataRefresher.class);
        MCPJdbcStatementExecutor statementExecutor = createStatementExecutor(ExecuteQueryResponse.statementAck("CREATE", "Statement executed."));
        MCPSQLExecutionFacade facade = createFacade("MySQL", new MCPSessionManager(mock()), statementExecutor, metadataRefresher);
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("CREATE TABLE orders_archive", 10));
        assertTrue(actual.isSuccessful());
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.STATEMENT_ACK));
        assertThat(actual.getMessage(), is("Statement executed."));
        verify(statementExecutor).execute(any(ExecutionRequest.class), any(ClassificationResult.class));
        verify(metadataRefresher).refresh("logic_db");
    }
    
    @Test
    void assertExecuteDcl() {
        MCPJdbcMetadataRefresher metadataRefresher = mock(MCPJdbcMetadataRefresher.class);
        MCPJdbcStatementExecutor statementExecutor = createStatementExecutor(ExecuteQueryResponse.statementAck("GRANT", "Statement executed."));
        MCPSQLExecutionFacade facade = createFacade("MySQL", new MCPSessionManager(mock()), statementExecutor, metadataRefresher);
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("GRANT SELECT ON orders TO app_user", 10));
        assertTrue(actual.isSuccessful());
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.STATEMENT_ACK));
        assertThat(actual.getMessage(), is("Statement executed."));
        verify(statementExecutor).execute(any(ExecutionRequest.class), any(ClassificationResult.class));
        verify(metadataRefresher).refresh("logic_db");
    }
    
    @Test
    void assertExecuteExplainAnalyzeWithUnsupportedCapability() {
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        final MCPSessionManager sessionManager = new MCPSessionManager(mock());
        final MCPJdbcTransactionResourceManager jdbcTransactionResourceManager = mock();
        final MCPJdbcMetadataRefresher metadataRefreshCoordinator = mock();
        if (!sessionManager.hasSession("session-1")) {
            sessionManager.createSession("session-1");
        }
        MCPCapabilityBuilder capabilityBuilder = new MCPCapabilityBuilder(
                new DatabaseMetadataSnapshots(Map.of("logic_db", new DatabaseMetadataSnapshot("MySQL", "", Collections.emptyList()))));
        MCPSQLExecutionFacade facade = new MCPSQLExecutionFacade(capabilityBuilder, new MCPSessionExecutionCoordinator(sessionManager), new MCPJdbcTransactionStatementExecutor(sessionManager, jdbcTransactionResourceManager), statementExecutor, metadataRefreshCoordinator);
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("EXPLAIN ANALYZE SELECT * FROM orders", 10));
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getErrorCode(), is(MCPErrorCode.UNSUPPORTED));
        verifyNoInteractions(statementExecutor);
    }
    
    @Test
    void assertExecuteExplainAnalyzeWithSupportedCapability() {
        MCPJdbcStatementExecutor statementExecutor = createStatementExecutor(createResultSetResponse(2, false));
        final MCPSessionManager sessionManager = new MCPSessionManager(mock());
        final MCPJdbcTransactionResourceManager jdbcTransactionResourceManager = mock();
        final MCPJdbcMetadataRefresher metadataRefreshCoordinator = mock();
        if (!sessionManager.hasSession("session-1")) {
            sessionManager.createSession("session-1");
        }
        MCPCapabilityBuilder capabilityBuilder = new MCPCapabilityBuilder(
                new DatabaseMetadataSnapshots(Map.of("logic_db", new DatabaseMetadataSnapshot("H2", "", Collections.emptyList()))));
        MCPSQLExecutionFacade facade = new MCPSQLExecutionFacade(capabilityBuilder, new MCPSessionExecutionCoordinator(sessionManager), new MCPJdbcTransactionStatementExecutor(sessionManager, jdbcTransactionResourceManager), statementExecutor, metadataRefreshCoordinator);
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("EXPLAIN ANALYZE SELECT * FROM orders", 10));
        assertTrue(actual.isSuccessful());
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.RESULT_SET));
        assertThat(actual.getRows().size(), is(2));
        verify(statementExecutor).execute(any(ExecutionRequest.class), any(ClassificationResult.class));
    }
    
    private MCPSQLExecutionFacade createFacade(final String databaseType, final MCPSessionManager sessionManager,
                                               final MCPJdbcStatementExecutor statementExecutor, final MCPJdbcMetadataRefresher metadataRefresher) {
        if (!sessionManager.hasSession("session-1")) {
            sessionManager.createSession("session-1");
        }
        MCPCapabilityBuilder capabilityBuilder = new MCPCapabilityBuilder(
                new DatabaseMetadataSnapshots(Map.of("logic_db", new DatabaseMetadataSnapshot(databaseType, "", Collections.emptyList()))));
        return new MCPSQLExecutionFacade(
                capabilityBuilder, new MCPSessionExecutionCoordinator(sessionManager), new MCPJdbcTransactionStatementExecutor(sessionManager, mock()), statementExecutor, metadataRefresher);
    }
    
    private ExecutionRequest createExecutionRequest(final String sql, final int maxRows) {
        return new ExecutionRequest("session-1", "logic_db", "public", sql, maxRows, 1000);
    }
    
    private MCPJdbcStatementExecutor createStatementExecutor(final ExecuteQueryResponse response) {
        MCPJdbcStatementExecutor result = mock(MCPJdbcStatementExecutor.class);
        when(result.execute(any(ExecutionRequest.class), any(ClassificationResult.class))).thenReturn(response);
        return result;
    }
    
    private ExecuteQueryResponse createResultSetResponse(final int rowCount, final boolean truncated) {
        List<ExecuteQueryColumnDefinition> columns = List.of(
                new ExecuteQueryColumnDefinition("order_id", "INTEGER", "INT", false),
                new ExecuteQueryColumnDefinition("status", "VARCHAR", "VARCHAR", true));
        List<List<Object>> rows = 1 == rowCount ? List.of(List.of(1, "NEW")) : List.of(List.of(1, "NEW"), List.of(2, "DONE"));
        return ExecuteQueryResponse.resultSet(columns, rows, truncated);
    }
}
