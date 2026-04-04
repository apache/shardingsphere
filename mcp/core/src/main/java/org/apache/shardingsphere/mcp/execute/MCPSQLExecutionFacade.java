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
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.MCPCapabilityBuilder;
import org.apache.shardingsphere.mcp.metadata.jdbc.MCPJdbcMetadataRefresher;
import org.apache.shardingsphere.mcp.protocol.error.MCPError;
import org.apache.shardingsphere.mcp.protocol.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPProtocolException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.protocol.exception.StatementClassNotSupportedException;
import org.apache.shardingsphere.mcp.protocol.response.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.protocol.error.MCPProtocolErrorConverter;
import org.apache.shardingsphere.mcp.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.session.MCPSessionNotExistedException;

import java.util.Optional;

/**
 * MCP SQL execution facade.
 */
@RequiredArgsConstructor
public final class MCPSQLExecutionFacade {
    
    private final MCPCapabilityBuilder capabilityBuilder;
    
    private final MCPSessionExecutionCoordinator sessionExecutionCoordinator;
    
    private final MCPJdbcTransactionStatementExecutor transactionStatementExecutor;
    
    private final MCPJdbcStatementExecutor statementExecutor;
    
    private final MCPJdbcMetadataRefresher jdbcMetadataRefresher;
    
    private final AuditRecorder auditRecorder = new AuditRecorder();
    
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
        Optional<DatabaseCapability> databaseCapability = capabilityBuilder.buildDatabaseCapability(executionRequest.getDatabase());
        if (databaseCapability.isEmpty()) {
            throw recordFailure(executionRequest, "QUERY", new DatabaseCapabilityNotFoundException());
        }
        ClassificationResult classificationResult;
        try {
            classificationResult = new StatementClassifier().classify(executionRequest.getSql());
        } catch (final UnsupportedOperationException ex) {
            throw recordFailure(executionRequest, "QUERY", ex);
        } catch (final IllegalArgumentException ex) {
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
        } catch (final MCPProtocolException | IllegalArgumentException | IllegalStateException | UnsupportedOperationException ex) {
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
        MCPError error = MCPProtocolErrorConverter.toError(ex);
        auditRecorder.recordQueryExecution(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), false, error.getCode(), transactionMarker);
        return ex;
    }
}
