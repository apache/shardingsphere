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

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.tool.handler.execute.audit.AuditRecorder;
import org.apache.shardingsphere.mcp.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.context.MCPRequestContext;
import org.apache.shardingsphere.mcp.protocol.error.MCPError;
import org.apache.shardingsphere.mcp.protocol.error.MCPErrorConverter;
import org.apache.shardingsphere.mcp.protocol.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.protocol.exception.StatementClassNotSupportedException;
import org.apache.shardingsphere.mcp.tool.response.SQLExecutionResponse;
import org.apache.shardingsphere.mcp.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.session.MCPSessionNotExistedException;
import org.apache.shardingsphere.mcp.tool.request.SQLExecutionRequest;

import java.util.Optional;

/**
 * MCP SQL execution facade.
 */
public final class MCPSQLExecutionFacade {
    
    private final MCPDatabaseCapabilityProvider databaseCapabilityProvider;
    
    private final MCPSessionExecutionCoordinator sessionExecutionCoordinator;
    
    private final MCPJdbcTransactionResourceManager transactionResourceManager;
    
    private final MCPJdbcTransactionStatementExecutor transactionStatementExecutor;
    
    private final MCPJdbcStatementExecutor statementExecutor;
    
    private final AuditRecorder auditRecorder = new AuditRecorder();
    
    public MCPSQLExecutionFacade(final MCPRequestContext requestContext) {
        databaseCapabilityProvider = requestContext.getDatabaseCapabilityProvider();
        sessionExecutionCoordinator = new MCPSessionExecutionCoordinator(requestContext.getSessionManager());
        transactionResourceManager = requestContext.getSessionManager().getTransactionResourceManager();
        transactionStatementExecutor = new MCPJdbcTransactionStatementExecutor(requestContext.getSessionManager());
        statementExecutor = new MCPJdbcStatementExecutor(
                transactionResourceManager.getRuntimeDatabases(), transactionResourceManager);
    }
    
    /**
     * Execute one MCP SQL request.
     *
     * @param executionRequest SQL execution request
     * @return execution response
     */
    public SQLExecutionResponse execute(final SQLExecutionRequest executionRequest) {
        try {
            return sessionExecutionCoordinator.executeWithSessionLock(executionRequest.getSessionId(), () -> executeInternal(executionRequest));
        } catch (final MCPSessionNotExistedException ex) {
            throw recordFailure(executionRequest, SupportedMCPStatement.QUERY.name(), ex);
        }
    }
    
    private SQLExecutionResponse executeInternal(final SQLExecutionRequest executionRequest) {
        Optional<MCPDatabaseCapability> databaseCapability = databaseCapabilityProvider.provide(executionRequest.getDatabase());
        ShardingSpherePreconditions.checkState(databaseCapability.isPresent(), () -> recordFailure(executionRequest, "QUERY", new DatabaseCapabilityNotFoundException()));
        MCPDatabaseCapability actualDatabaseCapability = databaseCapability.orElseThrow();
        ClassificationResult classificationResult;
        try {
            classificationResult = new StatementClassifier().classify(executionRequest.getSql());
        } catch (final UnsupportedOperationException | IllegalArgumentException ex) {
            throw recordFailure(executionRequest, SupportedMCPStatement.QUERY.name(), ex);
        }
        ShardingSpherePreconditions.checkContains(actualDatabaseCapability.getSupportedStatementClasses(), classificationResult.getStatementClass(),
                () -> recordFailure(executionRequest, classificationResult.getAuditMarker(), new StatementClassNotSupportedException()));
        try {
            switch (classificationResult.getStatementClass()) {
                case TRANSACTION_CONTROL:
                case SAVEPOINT:
                    return recordSuccess(executionRequest, transactionStatementExecutor.execute(
                            executionRequest.getSessionId(), executionRequest.getDatabase(), actualDatabaseCapability, classificationResult), classificationResult.getAuditMarker());
                case QUERY:
                case DML:
                case DDL:
                case DCL:
                    return recordSuccess(executionRequest, statementExecutor.execute(executionRequest, classificationResult, actualDatabaseCapability), classificationResult.getAuditMarker());
                case EXPLAIN_ANALYZE:
                    ShardingSpherePreconditions.checkState(actualDatabaseCapability.isSupportsExplainAnalyze(), () -> new MCPUnsupportedException("EXPLAIN ANALYZE is not supported."));
                    return recordSuccess(executionRequest, statementExecutor.execute(executionRequest, classificationResult, actualDatabaseCapability),
                            classificationResult.getAuditMarker());
                default:
                    throw new StatementClassNotSupportedException();
            }
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            throw recordFailure(executionRequest, classificationResult.getAuditMarker(), ex);
        }
    }
    
    private SQLExecutionResponse recordSuccess(final SQLExecutionRequest executionRequest, final SQLExecutionResponse response, final String statementMarker) {
        auditRecorder.recordQueryExecution(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), true, statementMarker);
        return response;
    }
    
    private <T extends RuntimeException> T recordFailure(final SQLExecutionRequest executionRequest, final String statementMarker, final T ex) {
        MCPError error = MCPErrorConverter.convert(ex);
        auditRecorder.recordQueryExecution(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), false, error.getCode(), statementMarker);
        return ex;
    }
}
