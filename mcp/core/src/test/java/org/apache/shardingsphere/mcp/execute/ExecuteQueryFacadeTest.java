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

import org.apache.shardingsphere.mcp.audit.AuditRecorder;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler;
import org.apache.shardingsphere.mcp.capability.StatementClass;
import org.apache.shardingsphere.mcp.metadata.MetadataRefreshCoordinator;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResultKind;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.session.TransactionCommandExecutor;
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

class ExecuteQueryFacadeTest {
    
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
    void assertExecuteWithUnknownCapability() {
        ExecuteQueryFacade facade = createFacade("Unknown", new MCPSessionManager(), new AuditRecorder());
        DatabaseExecutionBackend databaseExecutionBackend = mock(DatabaseExecutionBackend.class);
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("SELECT * FROM orders", 10, databaseExecutionBackend));
        
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getErrorCode(), is(MCPErrorCode.NOT_FOUND));
        verifyNoInteractions(databaseExecutionBackend);
    }
    
    @Test
    void assertExecuteQueryWithTruncation() {
        ExecuteQueryFacade facade = createFacade("MySQL", new MCPSessionManager(), new AuditRecorder());
        DatabaseExecutionBackend databaseExecutionBackend = createDatabaseExecutionBackend(createResultSetResponse(1, true));
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("SELECT * FROM orders", 1, databaseExecutionBackend));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.RESULT_SET));
        assertThat(actual.getRows().size(), is(1));
        assertTrue(actual.isTruncated());
        verify(databaseExecutionBackend).execute(any(ExecutionRequest.class), any(ClassificationResult.class));
    }
    
    @Test
    void assertExecuteUpdate() {
        ExecuteQueryFacade facade = createFacade("MySQL", new MCPSessionManager(), new AuditRecorder());
        DatabaseExecutionBackend databaseExecutionBackend = createDatabaseExecutionBackend(ExecuteQueryResponse.updateCount("UPDATE", 3));
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("UPDATE orders SET status = 'DONE'", 10, databaseExecutionBackend));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.UPDATE_COUNT));
        assertThat(actual.getAffectedRows(), is(3));
        verify(databaseExecutionBackend).execute(any(ExecutionRequest.class), any(ClassificationResult.class));
    }
    
    @Test
    void assertExecuteTransactionCommand() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        DatabaseExecutionBackend transactionBackend = mock(DatabaseExecutionBackend.class);
        ExecuteQueryFacade facade = createFacade("MySQL", sessionManager, new AuditRecorder(), transactionBackend);
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("BEGIN", 10, mock(DatabaseExecutionBackend.class)));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getMessage(), is("Transaction started."));
        verify(transactionBackend).beginTransaction("session-1", "logic_db");
    }
    
    @Test
    void assertExecuteDdl() {
        MetadataRefreshCoordinator metadataRefreshCoordinator = mock(MetadataRefreshCoordinator.class);
        ExecuteQueryFacade facade = createFacade("MySQL", new MCPSessionManager(), new AuditRecorder(), mock(DatabaseExecutionBackend.class), metadataRefreshCoordinator);
        DatabaseExecutionBackend databaseExecutionBackend = createDatabaseExecutionBackend(ExecuteQueryResponse.statementAck("CREATE", "Statement executed."));
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("CREATE TABLE orders_archive", 10, databaseExecutionBackend));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.STATEMENT_ACK));
        assertThat(actual.getMessage(), is("Statement executed."));
        verify(metadataRefreshCoordinator).refresh("logic_db");
    }
    
    @Test
    void assertExecuteDcl() {
        MetadataRefreshCoordinator metadataRefreshCoordinator = mock(MetadataRefreshCoordinator.class);
        ExecuteQueryFacade facade = createFacade("MySQL", new MCPSessionManager(), new AuditRecorder(), mock(DatabaseExecutionBackend.class), metadataRefreshCoordinator);
        DatabaseExecutionBackend databaseExecutionBackend = createDatabaseExecutionBackend(ExecuteQueryResponse.statementAck("GRANT", "Statement executed."));
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("GRANT SELECT ON orders TO app_user", 10, databaseExecutionBackend));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.STATEMENT_ACK));
        assertThat(actual.getMessage(), is("Statement executed."));
        verify(metadataRefreshCoordinator).refresh("logic_db");
    }
    
    @Test
    void assertExecuteExplainAnalyzeWithUnsupportedCapability() {
        ExecuteQueryFacade facade = createFacade("MySQL", new MCPSessionManager(), new AuditRecorder());
        DatabaseExecutionBackend databaseExecutionBackend = mock(DatabaseExecutionBackend.class);
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("EXPLAIN ANALYZE SELECT * FROM orders", 10, databaseExecutionBackend));
        
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getErrorCode(), is(MCPErrorCode.UNSUPPORTED));
        verifyNoInteractions(databaseExecutionBackend);
    }
    
    @Test
    void assertExecuteExplainAnalyzeWithSupportedCapability() {
        ExecuteQueryFacade facade = createFacade("H2", new MCPSessionManager(), new AuditRecorder());
        DatabaseExecutionBackend databaseExecutionBackend = createDatabaseExecutionBackend(createResultSetResponse(2, false));
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("EXPLAIN ANALYZE SELECT * FROM orders", 10, databaseExecutionBackend));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.RESULT_SET));
        assertThat(actual.getRows().size(), is(2));
        verify(databaseExecutionBackend).execute(any(ExecutionRequest.class), any(ClassificationResult.class));
    }
    
    private ExecuteQueryFacade createFacade(final String databaseType, final MCPSessionManager sessionManager, final AuditRecorder auditRecorder) {
        return createFacade(databaseType, sessionManager, auditRecorder, mock(DatabaseExecutionBackend.class));
    }
    
    private ExecuteQueryFacade createFacade(final String databaseType, final MCPSessionManager sessionManager, final AuditRecorder auditRecorder,
                                            final DatabaseExecutionBackend databaseExecutionBackend) {
        return createFacade(databaseType, sessionManager, auditRecorder, databaseExecutionBackend, mock(MetadataRefreshCoordinator.class));
    }
    
    private ExecuteQueryFacade createFacade(final String databaseType, final MCPSessionManager sessionManager, final AuditRecorder auditRecorder,
                                            final DatabaseExecutionBackend databaseExecutionBackend, final MetadataRefreshCoordinator metadataRefreshCoordinator) {
        DatabaseCapabilityAssembler capabilityAssembler = new DatabaseCapabilityAssembler(
                new DatabaseMetadataSnapshots(Map.of("logic_db", new DatabaseMetadataSnapshot(databaseType, "", Collections.emptyList()))));
        return new ExecuteQueryFacade(new StatementClassifier(), capabilityAssembler,
                new TransactionCommandExecutor(capabilityAssembler, sessionManager, databaseExecutionBackend),
                auditRecorder, metadataRefreshCoordinator);
    }
    
    private ExecutionRequest createExecutionRequest(final String sql, final int maxRows, final DatabaseExecutionBackend databaseExecutionBackend) {
        return new ExecutionRequest("session-1", "logic_db", "public", sql, maxRows, 1000, databaseExecutionBackend);
    }
    
    private DatabaseExecutionBackend createDatabaseExecutionBackend(final ExecuteQueryResponse response) {
        DatabaseExecutionBackend result = mock(DatabaseExecutionBackend.class);
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
