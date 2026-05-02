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

import org.apache.shardingsphere.mcp.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.database.exception.StatementClassNotSupportedException;
import org.apache.shardingsphere.mcp.api.protocol.error.MCPError.MCPErrorCode;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.session.MCPSessionNotExistedException;
import org.apache.shardingsphere.mcp.tool.handler.execute.audit.AuditRecorder;
import org.apache.shardingsphere.mcp.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.database.tool.response.SQLExecutionResponse;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MCPSQLExecutionFacadeTest {
    
    @Test
    void assertExecuteWithMissingSession() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        AuditRecorder auditRecorder = mock(AuditRecorder.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, auditRecorder);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        when(coordinator.executeWithSessionLock(eq("session-1"), any())).thenThrow(new MCPSessionNotExistedException());
        MCPSessionNotExistedException actual = assertThrows(MCPSessionNotExistedException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Session does not exist."));
        verify(auditRecorder).recordQueryExecution("session-1", "logic_db", "SELECT 1", false, MCPErrorCode.NOT_FOUND, "QUERY");
        verifyNoInteractions(capabilityProvider, transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteWithUnknownCapability() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        AuditRecorder auditRecorder = mock(AuditRecorder.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, auditRecorder);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.empty());
        DatabaseCapabilityNotFoundException actual = assertThrows(DatabaseCapabilityNotFoundException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Database capability does not exist."));
        verify(auditRecorder).recordQueryExecution("session-1", "logic_db", "SELECT 1", false, MCPErrorCode.NOT_FOUND, "QUERY");
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteWithUnsupportedStatement() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        AuditRecorder auditRecorder = mock(AuditRecorder.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, auditRecorder);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY), false);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        try (
                MockedConstruction<StatementClassifier> mocked = mockConstruction(StatementClassifier.class,
                        (mock, context) -> when(mock.classify(anyString())).thenThrow(new UnsupportedOperationException("Statement is banned by the MCP contract.")))) {
            UnsupportedOperationException actual = assertThrows(UnsupportedOperationException.class, () -> facade.execute(request));
            assertThat(actual.getMessage(), is("Statement is banned by the MCP contract."));
            assertThat(mocked.constructed().size(), is(1));
        }
        verify(auditRecorder).recordQueryExecution("session-1", "logic_db", "SELECT 1", false, MCPErrorCode.UNSUPPORTED, "QUERY");
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteWithUnsupportedStatementClass() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        AuditRecorder auditRecorder = mock(AuditRecorder.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, auditRecorder);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT 1", "orders", "");
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.DML), false);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        try (MockedConstruction<StatementClassifier> mocked = mockClassification(classification)) {
            StatementClassNotSupportedException actual = assertThrows(StatementClassNotSupportedException.class, () -> facade.execute(request));
            assertThat(actual.getMessage(), is("Statement class is not supported."));
            assertThat(mocked.constructed().size(), is(1));
        }
        verify(auditRecorder).recordQueryExecution("session-1", "logic_db", "SELECT 1", false, MCPErrorCode.UNSUPPORTED, "QUERY");
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteTransactionStatement() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        AuditRecorder auditRecorder = mock(AuditRecorder.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.TRANSACTION_CONTROL), false);
        SQLExecutionRequest request = createExecutionRequest("BEGIN");
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.TRANSACTION_CONTROL, "BEGIN", "BEGIN", "", "");
        SQLExecutionResponse response = SQLExecutionResponse.statementAck(SupportedMCPStatement.TRANSACTION_CONTROL, "BEGIN", "Transaction started.");
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, auditRecorder);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        when(transactionExecutor.execute("session-1", "logic_db", capability, classification)).thenReturn(response);
        try (MockedConstruction<StatementClassifier> mocked = mockClassification(classification)) {
            SQLExecutionResponse actual = facade.execute(request);
            assertThat(actual, is(response));
            assertThat(mocked.constructed().size(), is(1));
        }
        verify(transactionExecutor).execute("session-1", "logic_db", capability, classification);
        verify(auditRecorder).recordQueryExecution("session-1", "logic_db", "BEGIN", true, "BEGIN");
        verifyNoInteractions(statementExecutor);
    }
    
    @Test
    void assertExecuteQueryStatement() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        AuditRecorder auditRecorder = mock(AuditRecorder.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY), false);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT 1", "orders", "");
        SQLExecutionResponse response = SQLExecutionResponse.resultSet(SupportedMCPStatement.QUERY, "SELECT", Collections.emptyList(), Collections.emptyList(), false);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, auditRecorder);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        when(statementExecutor.execute(request, classification, capability)).thenReturn(response);
        try (MockedConstruction<StatementClassifier> mocked = mockClassification(classification)) {
            SQLExecutionResponse actual = facade.execute(request);
            assertThat(actual, is(response));
            assertThat(mocked.constructed().size(), is(1));
        }
        verify(statementExecutor).execute(request, classification, capability);
        verify(auditRecorder).recordQueryExecution("session-1", "logic_db", "SELECT 1", true, "QUERY");
        verifyNoInteractions(transactionExecutor);
    }
    
    @Test
    void assertExecuteWithExplainAnalyzeUnsupported() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        AuditRecorder auditRecorder = mock(AuditRecorder.class);
        SQLExecutionRequest request = createExecutionRequest("EXPLAIN ANALYZE SELECT 1");
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.EXPLAIN_ANALYZE, "EXPLAIN ANALYZE", "EXPLAIN ANALYZE SELECT 1", "orders", "");
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.EXPLAIN_ANALYZE), false);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, auditRecorder);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        try (MockedConstruction<StatementClassifier> mocked = mockClassification(classification)) {
            MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> facade.execute(request));
            assertThat(actual.getMessage(), is("EXPLAIN ANALYZE is not supported."));
            assertThat(mocked.constructed().size(), is(1));
        }
        verify(auditRecorder).recordQueryExecution("session-1", "logic_db", "EXPLAIN ANALYZE SELECT 1", false, MCPErrorCode.UNSUPPORTED, "EXPLAIN_ANALYZE");
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteWithExecutorFailure() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        AuditRecorder auditRecorder = mock(AuditRecorder.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY), false);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT 1", "orders", "");
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, auditRecorder);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        when(statementExecutor.execute(request, classification, capability)).thenThrow(new MCPInvalidRequestException("bad query"));
        try (MockedConstruction<StatementClassifier> mocked = mockClassification(classification)) {
            MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> facade.execute(request));
            assertThat(actual.getMessage(), is("bad query"));
            assertThat(mocked.constructed().size(), is(1));
        }
        verify(auditRecorder).recordQueryExecution("session-1", "logic_db", "SELECT 1", false, MCPErrorCode.INVALID_REQUEST, "QUERY");
        verifyNoInteractions(transactionExecutor);
    }
    
    @Test
    void assertExecuteExplainAnalyze() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        AuditRecorder auditRecorder = mock(AuditRecorder.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.EXPLAIN_ANALYZE), true);
        SQLExecutionRequest request = createExecutionRequest("EXPLAIN ANALYZE SELECT 1");
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.EXPLAIN_ANALYZE, "EXPLAIN ANALYZE", "EXPLAIN ANALYZE SELECT 1", "orders", "");
        SQLExecutionResponse response = SQLExecutionResponse.resultSet(SupportedMCPStatement.EXPLAIN_ANALYZE, "EXPLAIN ANALYZE", Collections.emptyList(), Collections.emptyList(), false);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, auditRecorder);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        when(statementExecutor.execute(request, classification, capability)).thenReturn(response);
        try (MockedConstruction<StatementClassifier> mocked = mockClassification(classification)) {
            SQLExecutionResponse actual = facade.execute(request);
            assertThat(actual, is(response));
            assertThat(mocked.constructed().size(), is(1));
        }
        verify(statementExecutor).execute(request, classification, capability);
        verify(auditRecorder).recordQueryExecution("session-1", "logic_db", "EXPLAIN ANALYZE SELECT 1", true, "EXPLAIN_ANALYZE");
        verifyNoInteractions(transactionExecutor);
    }
    
    private MCPSQLExecutionFacade createFacade(final MCPDatabaseCapabilityProvider capabilityProvider, final MCPSessionExecutionCoordinator coordinator,
                                               final MCPJdbcTransactionStatementExecutor transactionExecutor, final MCPJdbcStatementExecutor statementExecutor,
                                               final AuditRecorder auditRecorder) {
        MCPSQLExecutionFacade result = new MCPSQLExecutionFacade(capabilityProvider, new MCPSessionManager(Collections.emptyMap()));
        setField(result, "sessionExecutionCoordinator", coordinator);
        setField(result, "transactionStatementExecutor", transactionExecutor);
        setField(result, "statementExecutor", statementExecutor);
        setField(result, "auditRecorder", auditRecorder);
        return result;
    }
    
    private void setField(final Object target, final String fieldName, final Object value) {
        try {
            Plugins.getMemberAccessor().set(MCPSQLExecutionFacade.class.getDeclaredField(fieldName), target, value);
        } catch (final ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void mockSessionLock(final MCPSessionExecutionCoordinator coordinator) {
        when(coordinator.executeWithSessionLock(eq("session-1"), any())).thenAnswer(invocation -> ((Supplier<SQLExecutionResponse>) invocation.getArgument(1, Supplier.class)).get());
    }
    
    private MCPDatabaseCapability createCapability(final Set<SupportedMCPStatement> supportedStatementClasses, final boolean supportsExplainAnalyze) {
        MCPDatabaseCapability result = mock(MCPDatabaseCapability.class);
        when(result.getSupportedStatementClasses()).thenReturn(supportedStatementClasses);
        when(result.isSupportsExplainAnalyze()).thenReturn(supportsExplainAnalyze);
        return result;
    }
    
    private MockedConstruction<StatementClassifier> mockClassification(final ClassificationResult classificationResult) {
        return mockConstruction(StatementClassifier.class, (mock, context) -> when(mock.classify(anyString())).thenReturn(classificationResult));
    }
    
    private SQLExecutionRequest createExecutionRequest(final String sql) {
        return new SQLExecutionRequest("session-1", "logic_db", "public", sql, 1, 1000);
    }
}
