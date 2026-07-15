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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPBannedSQLStatementException;
import org.apache.shardingsphere.mcp.core.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.core.session.MCPSessionNotExistedException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.trace.SQLExecutionTraceFactory;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.capability.SchemaExecutionSemantics;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.support.database.exception.StatementClassNotSupportedException;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        when(coordinator.executeWithSessionLock(eq("session-1"), any())).thenThrow(new MCPSessionNotExistedException());
        MCPSessionNotExistedException actual = assertThrows(MCPSessionNotExistedException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Session does not exist."));
        verify(traceFactory).create("session-1", "logic_db", "SELECT 1", false, "QUERY");
        verifyNoInteractions(capabilityProvider, transactionExecutor, statementExecutor);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertExecuteDefersWorkUntilSessionLock() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        StatementClassifier statementClassifier = mock(StatementClassifier.class);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT 1", "", List.of());
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        ArgumentCaptor<Supplier<SQLExecutionResult>> lockedExecution = ArgumentCaptor.forClass(Supplier.class);
        when(coordinator.executeWithSessionLock(eq("session-1"), lockedExecution.capture())).thenReturn(expectedResult);
        when(statementClassifier.classify("SELECT 1")).thenReturn(classification);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        when(statementExecutor.execute(request, classification, capability)).thenReturn(expectedResult);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory, statementClassifier);
        assertThat(facade.execute(request), is(expectedResult));
        verifyNoInteractions(capabilityProvider, transactionExecutor, statementExecutor, statementClassifier, traceFactory);
        assertThat(lockedExecution.getValue().get(), is(expectedResult));
        verify(statementClassifier).classify("SELECT 1");
        verify(capabilityProvider).provide("logic_db");
        verify(statementExecutor).execute(request, classification, capability);
        verify(traceFactory).create("session-1", "logic_db", "SELECT 1", true, "QUERY");
        verifyNoInteractions(transactionExecutor);
    }
    
    @Test
    void assertExecuteWithUnknownCapability() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.empty());
        DatabaseCapabilityNotFoundException actual = assertThrows(DatabaseCapabilityNotFoundException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Database capability does not exist."));
        verify(traceFactory).create("session-1", "logic_db", "SELECT 1", false, "QUERY");
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteWithUnsupportedStatement() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        StatementClassifier statementClassifier = mock(StatementClassifier.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        when(statementClassifier.classify(anyString())).thenThrow(new MCPBannedSQLStatementException());
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory, statementClassifier);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        MCPBannedSQLStatementException actual = assertThrows(MCPBannedSQLStatementException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Statement is banned by the MCP contract."));
        verify(traceFactory).create("session-1", "logic_db", "SELECT 1", false, "QUERY");
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteWithUnexpectedClassifierFailure() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        StatementClassifier statementClassifier = mock(StatementClassifier.class);
        mockSessionLock(coordinator);
        when(statementClassifier.classify(anyString())).thenThrow(new IllegalArgumentException("Malformed SQL."));
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory, statementClassifier);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Malformed SQL."));
        verify(traceFactory).create("session-1", "logic_db", "SELECT 1", false, "QUERY");
        verifyNoInteractions(capabilityProvider, transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteWithUnsupportedStatementClass() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT 1", "",
                List.of(SQLStatementObjectName.fromNormalizedName("orders")));
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory, createStatementClassifier(classification));
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.DML));
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        StatementClassNotSupportedException actual = assertThrows(StatementClassNotSupportedException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Statement class is not supported."));
        verify(traceFactory).create("session-1", "logic_db", "SELECT 1", false, "QUERY");
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteTransactionStatement() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.TRANSACTION_CONTROL));
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.TRANSACTION_CONTROL, "BEGIN", "BEGIN", "", List.of());
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        when(transactionExecutor.execute("session-1", "logic_db", capability, classification)).thenReturn(expectedResult);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory, createStatementClassifier(classification));
        SQLExecutionRequest request = createExecutionRequest("BEGIN");
        SQLExecutionResult actual = facade.execute(request);
        assertThat(actual, is(expectedResult));
        verify(transactionExecutor).execute("session-1", "logic_db", capability, classification);
        verify(traceFactory).create("session-1", "logic_db", "BEGIN", true, "BEGIN");
        verifyNoInteractions(statementExecutor);
    }
    
    @Test
    void assertExecuteQueryStatement() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT 1", "",
                List.of(SQLStatementObjectName.fromNormalizedName("orders")));
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        when(statementExecutor.execute(request, classification, capability)).thenReturn(expectedResult);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory, createStatementClassifier(classification));
        SQLExecutionResult actual = facade.execute(request);
        assertThat(actual, is(expectedResult));
        verify(statementExecutor).execute(request, classification, capability);
        verify(traceFactory).create("session-1", "logic_db", "SELECT 1", true, "QUERY");
        verifyNoInteractions(transactionExecutor);
    }
    
    @Test
    void assertExecuteWithCrossSchemaQueryDisabled() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        SQLExecutionRequest request = createExecutionRequest("SELECT * FROM other_db.orders");
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT * FROM other_db.orders", "",
                List.of(SQLStatementObjectName.fromNormalizedName("other_db.orders")));
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory, createStatementClassifier(classification));
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Cross-schema SQL is not supported for database `logic_db`: `other_db.orders`."));
        verify(traceFactory).create("session-1", "logic_db", "SELECT * FROM other_db.orders", false, "QUERY");
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteWithCrossSchemaSubqueryDisabled() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        SQLExecutionRequest request = createExecutionRequest("SELECT * FROM logic_db.orders WHERE EXISTS (SELECT 1 FROM other_db.items)");
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Cross-schema SQL is not supported for database `logic_db`: `other_db.items`."));
        verify(traceFactory).create("session-1", "logic_db", "SELECT * FROM logic_db.orders WHERE EXISTS (SELECT 1 FROM other_db.items)", false, "QUERY");
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteWithCrossSchemaReferencesDisabledCases")
    void assertExecuteWithCrossSchemaReferencesDisabled(final String name, final String sql, final String expectedObjectName, final String expectedTraceStatementMarker) {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY, SupportedMCPStatement.DML, SupportedMCPStatement.DDL, SupportedMCPStatement.DCL));
        SQLExecutionRequest request = createExecutionRequest(sql);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is(String.format("Cross-schema SQL is not supported for database `logic_db`: `%s`.", expectedObjectName)));
        verify(traceFactory).create("session-1", "logic_db", sql, false, expectedTraceStatementMarker);
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteWithCrossSchemaDMLDisabled() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.DML));
        SQLExecutionRequest request = createExecutionRequest("UPDATE other_db.orders SET status = 'DONE'");
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE other_db.orders SET status = 'DONE'", "",
                List.of(SQLStatementObjectName.fromNormalizedName("other_db.orders")));
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory, createStatementClassifier(classification));
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Cross-schema SQL is not supported for database `logic_db`: `other_db.orders`."));
        verify(traceFactory).create("session-1", "logic_db", "UPDATE other_db.orders SET status = 'DONE'", false, "DML");
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteWithQualifiedCurrentDatabase() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT * FROM logic_db.orders", "",
                List.of(SQLStatementObjectName.fromNormalizedName("logic_db.orders")));
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        SQLExecutionRequest request = createExecutionRequest("SELECT * FROM logic_db.orders");
        when(statementExecutor.execute(request, classification, capability)).thenReturn(expectedResult);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory, createStatementClassifier(classification));
        SQLExecutionResult actual = facade.execute(request);
        assertThat(actual, is(expectedResult));
        verify(statementExecutor).execute(request, classification, capability);
        verify(traceFactory).create("session-1", "logic_db", "SELECT * FROM logic_db.orders", true, "QUERY");
        verifyNoInteractions(transactionExecutor);
    }
    
    @Test
    void assertExecuteWithCaseInsensitiveQualifiedCurrentDatabase() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        SQLExecutionRequest request = createExecutionRequest("SELECT * FROM Logic_DB.orders");
        when(statementExecutor.execute(eq(request), any(), eq(capability))).thenReturn(expectedResult);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), statementExecutor,
                mock(SQLExecutionTraceFactory.class));
        assertThat(facade.execute(request), is(expectedResult));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteWithNonMatchingCurrentDatabaseIdentifierCases")
    void assertExecuteWithNonMatchingCurrentDatabaseIdentifier(final String name, final String sql, final IdentifierCasePolicySet identifierCasePolicySet) {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        when(capability.getIdentifierCasePolicySet()).thenReturn(identifierCasePolicySet);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), mock(MCPJdbcStatementExecutor.class),
                mock(SQLExecutionTraceFactory.class));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> facade.execute(createExecutionRequest(sql)));
        assertThat(actual.getMessage(), is("Cross-schema SQL is not supported for database `logic_db`: `Logic_DB.orders`."));
    }
    
    @Test
    void assertExecuteWithCrossSchemaSqlEnabled() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY), SchemaExecutionSemantics.BEST_EFFORT);
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT * FROM other_db.orders", "",
                List.of(SQLStatementObjectName.fromNormalizedName("other_db.orders")));
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        SQLExecutionRequest request = createExecutionRequest("SELECT * FROM other_db.orders");
        when(statementExecutor.execute(request, classification, capability)).thenReturn(expectedResult);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory, createStatementClassifier(classification));
        SQLExecutionResult actual = facade.execute(request);
        assertThat(actual, is(expectedResult));
        verify(statementExecutor).execute(request, classification, capability);
        verify(traceFactory).create("session-1", "logic_db", "SELECT * FROM other_db.orders", true, "QUERY");
        verifyNoInteractions(transactionExecutor);
    }
    
    @Test
    void assertExecuteWithExecutorFailure() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT 1", "",
                List.of(SQLStatementObjectName.fromNormalizedName("orders")));
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        when(statementExecutor.execute(request, classification, capability)).thenThrow(new MCPInvalidRequestException("bad query"));
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory, createStatementClassifier(classification));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("bad query"));
        verify(traceFactory).create("session-1", "logic_db", "SELECT 1", false, "QUERY");
        verifyNoInteractions(transactionExecutor);
    }
    
    @Test
    void assertExecuteExplain() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.EXPLAIN));
        SQLExecutionRequest request = createExecutionRequest("EXPLAIN SELECT * FROM orders");
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        when(statementExecutor.execute(eq(request), any(), eq(capability))).thenReturn(expectedResult);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory);
        SQLExecutionResult actual = facade.executeExplain(request, "SELECT * FROM orders");
        assertThat(actual, is(expectedResult));
        ArgumentCaptor<ClassificationResult> classificationCaptor = ArgumentCaptor.forClass(ClassificationResult.class);
        verify(statementExecutor).execute(eq(request), classificationCaptor.capture(), eq(capability));
        assertThat(classificationCaptor.getValue().getStatementClass(), is(SupportedMCPStatement.EXPLAIN));
        assertThat(classificationCaptor.getValue().getReferencedObjectNames(), contains("orders"));
        verify(traceFactory).create("session-1", "logic_db", "EXPLAIN SELECT * FROM orders", true, "EXPLAIN");
        verifyNoInteractions(transactionExecutor);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteExplainWithSyntaxFailureCases")
    void assertExecuteExplainWithSyntaxFailure(final String name, final RuntimeException executionFailure) {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.EXPLAIN));
        SQLExecutionRequest request = createExecutionRequest("EXPLAIN BROKEN SELECT * FROM orders");
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        when(statementExecutor.execute(eq(request), any(), eq(capability))).thenThrow(executionFailure);
        SQLExecutionTraceFactory traceFactory = mock(SQLExecutionTraceFactory.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), statementExecutor, traceFactory);
        ExplainSQLSyntaxException actual = assertThrows(ExplainSQLSyntaxException.class, () -> facade.executeExplain(request, "SELECT * FROM orders"));
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getSchema(), is("public"));
        assertThat(actual.getSql(), is("SELECT * FROM orders"));
        assertThat(actual.getExplainSql(), is("EXPLAIN BROKEN SELECT * FROM orders"));
        verify(traceFactory).create("session-1", "logic_db", "EXPLAIN BROKEN SELECT * FROM orders", false, "EXPLAIN");
    }
    
    @Test
    void assertExecuteExplainWithNonSyntaxFailure() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.EXPLAIN));
        SQLExecutionRequest request = createExecutionRequest("EXPLAIN SELECT * FROM missing_orders");
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPQueryFailedException executionFailure = new MCPQueryFailedException("missing table", new SQLException("missing table", "42P01"));
        when(statementExecutor.execute(eq(request), any(), eq(capability))).thenThrow(executionFailure);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), statementExecutor,
                mock(SQLExecutionTraceFactory.class));
        assertThat(assertThrows(MCPQueryFailedException.class, () -> facade.executeExplain(request, "SELECT * FROM missing_orders")), is(executionFailure));
    }
    
    private MCPSQLExecutionFacade createFacade(final MCPDatabaseCapabilityProvider capabilityProvider, final MCPSessionExecutionCoordinator coordinator,
                                               final MCPJdbcTransactionStatementExecutor transactionExecutor, final MCPJdbcStatementExecutor statementExecutor,
                                               final SQLExecutionTraceFactory traceFactory) {
        return createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, traceFactory, new StatementClassifier());
    }
    
    private MCPSQLExecutionFacade createFacade(final MCPDatabaseCapabilityProvider capabilityProvider, final MCPSessionExecutionCoordinator coordinator,
                                               final MCPJdbcTransactionStatementExecutor transactionExecutor, final MCPJdbcStatementExecutor statementExecutor,
                                               final SQLExecutionTraceFactory traceFactory, final StatementClassifier statementClassifier) {
        return new MCPSQLExecutionFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, statementClassifier, new SQLStatementScanner(), traceFactory);
    }
    
    @SuppressWarnings("unchecked")
    private void mockSessionLock(final MCPSessionExecutionCoordinator coordinator) {
        when(coordinator.executeWithSessionLock(eq("session-1"), any())).thenAnswer(invocation -> ((Supplier<SQLExecutionResult>) invocation.getArgument(1, Supplier.class)).get());
    }
    
    private MCPDatabaseCapability createCapability(final Set<SupportedMCPStatement> supportedStatementClasses) {
        return createCapability(supportedStatementClasses, SchemaExecutionSemantics.FIXED_TO_DATABASE);
    }
    
    private MCPDatabaseCapability createCapability(final Set<SupportedMCPStatement> supportedStatementClasses, final SchemaExecutionSemantics schemaExecutionSemantics) {
        MCPDatabaseCapability result = mock(MCPDatabaseCapability.class);
        when(result.getSupportedStatementClasses()).thenReturn(supportedStatementClasses);
        when(result.getSchemaExecutionSemantics()).thenReturn(schemaExecutionSemantics);
        when(result.getIdentifierCasePolicySet()).thenReturn(IdentifierCasePolicyFactory.newInsensitivePolicySet());
        return result;
    }
    
    private StatementClassifier createStatementClassifier(final ClassificationResult classificationResult) {
        StatementClassifier result = mock(StatementClassifier.class);
        when(result.classify(anyString())).thenReturn(classificationResult);
        return result;
    }
    
    private static Stream<Arguments> assertExecuteWithCrossSchemaReferencesDisabledCases() {
        return Stream.of(
                Arguments.of("query object list", "SELECT * FROM logic_db.orders, other_db.items", "other_db.items", "QUERY"),
                Arguments.of("query aliased object list", "SELECT * FROM logic_db.orders o, other_db.items i", "other_db.items", "QUERY"),
                Arguments.of("query partitioned object list", "SELECT * FROM logic_db.orders PARTITION (p0) o, other_db.items i", "other_db.items", "QUERY"),
                Arguments.of("cte object list", "WITH query_result AS (SELECT * FROM logic_db.orders, other_db.items) SELECT * FROM query_result", "other_db.items", "QUERY"),
                Arguments.of("qualified object distinct from quoted cte alias", "WITH \"other_db.items\" AS (SELECT 1) SELECT * FROM other_db.items", "other_db.items", "QUERY"),
                Arguments.of("unused cte reference", "WITH unused_result AS (SELECT * FROM other_db.items) SELECT * FROM logic_db.orders", "other_db.items", "QUERY"),
                Arguments.of("insert select object list", "INSERT INTO logic_db.orders_archive SELECT * FROM logic_db.orders, other_db.items", "other_db.items", "DML"),
                Arguments.of("update target object list", "UPDATE logic_db.orders o, other_db.items i SET o.status = 'DONE'", "other_db.items", "DML"),
                Arguments.of("delete target before from", "DELETE other_db.orders FROM logic_db.orders JOIN other_db.items ON 1 = 1", "other_db.orders", "DML"),
                Arguments.of("delete using object list", "DELETE FROM logic_db.orders USING other_db.items", "other_db.items", "DML"),
                Arguments.of("create view object list", "CREATE VIEW logic_db.active_orders AS SELECT * FROM logic_db.orders, other_db.items", "other_db.items", "DDL"),
                Arguments.of("create table like", "CREATE TABLE logic_db.orders_archive LIKE other_db.orders", "other_db.orders", "DDL"),
                Arguments.of("create table foreign key reference", "CREATE TABLE logic_db.order_items (order_id INT REFERENCES other_db.orders(id))", "other_db.orders", "DDL"),
                Arguments.of("create table inherits reference", "CREATE TABLE logic_db.child_orders (LIKE logic_db.orders) INHERITS (other_db.parent_orders)", "other_db.parent_orders", "DDL"),
                Arguments.of("create index with modifiers", "CREATE INDEX CONCURRENTLY IF NOT EXISTS other_db.orders_idx ON logic_db.orders (status)", "other_db.orders_idx", "DDL"),
                Arguments.of("create type target", "CREATE TYPE other_db.order_status AS ENUM ('PENDING', 'DONE')", "other_db.order_status", "DDL"),
                Arguments.of("create function target", "CREATE FUNCTION other_db.refresh_orders() RETURNS INT AS 'SELECT 1'", "other_db.refresh_orders", "DDL"),
                Arguments.of("create trigger source table", "CREATE TRIGGER refresh_orders AFTER INSERT ON other_db.orders EXECUTE FUNCTION logic_db.refresh_orders()", "other_db.orders", "DDL"),
                Arguments.of("alter table with modifiers", "ALTER TABLE IF EXISTS other_db.orders ADD COLUMN status VARCHAR(10)", "other_db.orders", "DDL"),
                Arguments.of("alter table foreign key reference",
                        "ALTER TABLE logic_db.order_items ADD CONSTRAINT order_fk FOREIGN KEY (order_id) REFERENCES other_db.orders(id)", "other_db.orders", "DDL"),
                Arguments.of("alter table inherit reference", "ALTER TABLE logic_db.child_orders INHERIT other_db.parent_orders", "other_db.parent_orders", "DDL"),
                Arguments.of("alter table rename destination", "ALTER TABLE logic_db.orders RENAME TO other_db.orders_archive", "other_db.orders_archive", "DDL"),
                Arguments.of("alter table set schema destination", "ALTER TABLE logic_db.orders SET SCHEMA other_db", "other_db", "DDL"),
                Arguments.of("create database target", "CREATE DATABASE other_db", "other_db", "DDL"),
                Arguments.of("commented create database target", "/* guard */ CREATE DATABASE other_db", "other_db", "DDL"),
                Arguments.of("drop schema target", "DROP SCHEMA IF EXISTS other_db", "other_db", "DDL"),
                Arguments.of("drop table object list", "DROP TABLE IF EXISTS logic_db.orders, other_db.items", "other_db.items", "DDL"),
                Arguments.of("drop index with modifiers", "DROP INDEX CONCURRENTLY IF EXISTS other_db.orders_idx", "other_db.orders_idx", "DDL"),
                Arguments.of("truncate table object list", "TRUNCATE TABLE logic_db.orders, other_db.items", "other_db.items", "DDL"),
                Arguments.of("grant global wildcard", "GRANT SELECT ON *.* TO PUBLIC", "*.*", "DCL"),
                Arguments.of("grant database target", "GRANT CONNECT ON DATABASE other_db TO PUBLIC", "other_db", "DCL"),
                Arguments.of("qualified function", "SELECT other_db.refresh_orders()", "other_db.refresh_orders", "QUERY"));
    }
    
    private static Stream<Arguments> assertExecuteWithNonMatchingCurrentDatabaseIdentifierCases() {
        return Stream.of(
                Arguments.of("case-sensitive unquoted identifier", "SELECT * FROM Logic_DB.orders", IdentifierCasePolicyFactory.newSensitivePolicySet()),
                Arguments.of("quoted identifier exact match", "SELECT * FROM \"Logic_DB\".orders", IdentifierCasePolicyFactory.newInsensitivePolicySet()));
    }
    
    private static Stream<Arguments> assertExecuteExplainWithSyntaxFailureCases() {
        return Stream.of(
                Arguments.of("JDBC syntax exception", new MCPInvalidRequestException("bad explain", new SQLSyntaxErrorException("bad explain"))),
                Arguments.of("SQLState syntax exception", new MCPQueryFailedException("bad explain", new SQLException("bad explain", "42601"))));
    }
    
    private SQLExecutionRequest createExecutionRequest(final String sql) {
        return new SQLExecutionRequest("session-1", "logic_db", "public", sql, 1, 1000);
    }
}
