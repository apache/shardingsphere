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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.audit.AuditRecorder;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.jdbc.MCPJdbcMetadataRefresher;
import org.apache.shardingsphere.mcp.protocol.error.MCPError;
import org.apache.shardingsphere.mcp.protocol.error.MCPErrorConverter;
import org.apache.shardingsphere.mcp.protocol.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.protocol.exception.StatementClassNotSupportedException;
import org.apache.shardingsphere.mcp.protocol.response.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.session.MCPSessionNotExistedException;

import java.util.Optional;

/**
 * MCP SQL execution facade.
 */
@RequiredArgsConstructor
public final class MCPSQLExecutionFacade {
    
    private final MCPDatabaseCapabilityProvider databaseCapabilityProvider;
    
    private final MCPSessionExecutionCoordinator sessionExecutionCoordinator;
    
    private final MCPJdbcTransactionStatementExecutor transactionStatementExecutor;
    
    private final MCPJdbcStatementExecutor statementExecutor;
    
    private final MCPJdbcMetadataRefresher jdbcMetadataRefresher;
    
    private final AuditRecorder auditRecorder = new AuditRecorder();
    
    public MCPSQLExecutionFacade(final MCPRuntimeContext runtimeContext) {
        databaseCapabilityProvider = runtimeContext.getDatabaseCapabilityProvider();
        sessionExecutionCoordinator = new MCPSessionExecutionCoordinator(runtimeContext.getSessionManager());
        transactionStatementExecutor = new MCPJdbcTransactionStatementExecutor(runtimeContext.getSessionManager(), runtimeContext.getSessionManager().getTransactionResourceManager());
        statementExecutor = new MCPJdbcStatementExecutor(
                runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases(), runtimeContext.getSessionManager().getTransactionResourceManager());
        jdbcMetadataRefresher = new MCPJdbcMetadataRefresher(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases(), runtimeContext.getMetadataCatalog());
    }
    
    /**
     * Execute one MCP SQL request.
     *
     * @param executionRequest execution request
     * @return execution response
     */
    public ExecuteQueryResponse execute(final ExecutionRequest executionRequest) {
        try {
            return sessionExecutionCoordinator.executeWithSessionLock(executionRequest.getSessionId(), () -> executeInternal(executionRequest));
        } catch (final MCPSessionNotExistedException ex) {
            throw recordFailure(executionRequest, "QUERY", ex);
        }
    }
    
    private ExecuteQueryResponse executeInternal(final ExecutionRequest executionRequest) {
        Optional<MCPDatabaseCapability> databaseCapability = databaseCapabilityProvider.provide(executionRequest.getDatabase());
        if (databaseCapability.isEmpty()) {
            throw recordFailure(executionRequest, "QUERY", new DatabaseCapabilityNotFoundException());
        }
        ClassificationResult classificationResult;
        try {
            classificationResult = new StatementClassifier().classify(executionRequest.getSql());
        } catch (final UnsupportedOperationException | IllegalArgumentException ex) {
            throw recordFailure(executionRequest, "QUERY", ex);
        }
        if (!databaseCapability.get().getSupportedStatementClasses().contains(classificationResult.getStatementClass())) {
            throw recordFailure(executionRequest, classificationResult.getStatementType(), new StatementClassNotSupportedException());
        }
        try {
            switch (classificationResult.getStatementClass()) {
                case TRANSACTION_CONTROL:
                case SAVEPOINT:
                    return recordSuccess(executionRequest, transactionStatementExecutor.execute(
                            executionRequest.getSessionId(), executionRequest.getDatabase(), databaseCapability.get(), classificationResult), classificationResult.getStatementType());
                case QUERY:
                case DML:
                    return recordSuccess(executionRequest, statementExecutor.execute(executionRequest, classificationResult), classificationResult.getStatementType());
                case DDL:
                case DCL:
                    return recordSuccess(executionRequest, executeAndRefreshMetadata(executionRequest, classificationResult), classificationResult.getStatementType());
                case EXPLAIN_ANALYZE:
                    if (!databaseCapability.get().isSupportsExplainAnalyze()) {
                        throw new MCPUnsupportedException("EXPLAIN ANALYZE is not supported.");
                    }
                    return recordSuccess(executionRequest, statementExecutor.execute(executionRequest, classificationResult), classificationResult.getStatementType());
                default:
                    throw new StatementClassNotSupportedException();
            }
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            throw recordFailure(executionRequest, classificationResult.getStatementType(), ex);
        }
    }
    
    private ExecuteQueryResponse executeAndRefreshMetadata(final ExecutionRequest executionRequest, final ClassificationResult classificationResult) {
        ExecuteQueryResponse result = statementExecutor.execute(executionRequest, classificationResult);
        jdbcMetadataRefresher.refresh(executionRequest.getDatabase());
        return result;
    }
    
    private ExecuteQueryResponse recordSuccess(final ExecutionRequest executionRequest, final ExecuteQueryResponse response, final String transactionMarker) {
        auditRecorder.recordQueryExecution(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), true, transactionMarker);
        return response;
    }
    
    private <T extends RuntimeException> T recordFailure(final ExecutionRequest executionRequest, final String transactionMarker, final T ex) {
        MCPError error = MCPErrorConverter.convert(ex);
        auditRecorder.recordQueryExecution(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), false, error.getCode(), transactionMarker);
        return ex;
    }
}
