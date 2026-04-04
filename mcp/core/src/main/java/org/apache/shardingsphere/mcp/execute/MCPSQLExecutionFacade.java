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
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.protocol.MCPErrorPayload.MCPErrorCode;
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
            return recordFailure(executionRequest, "QUERY", MCPErrorCode.NOT_FOUND, ex.getMessage());
        }
    }
    
    private ExecuteQueryResponse executeInternal(final ExecutionRequest executionRequest) {
        Optional<DatabaseCapability> databaseCapability = capabilityBuilder.buildDatabaseCapability(executionRequest.getDatabase());
        if (databaseCapability.isEmpty()) {
            return recordFailure(executionRequest, "QUERY", MCPErrorCode.NOT_FOUND, "Database capability does not exist.");
        }
        ClassificationResult classificationResult;
        try {
            classificationResult = new StatementClassifier().classify(executionRequest.getSql());
        } catch (final UnsupportedOperationException ex) {
            return recordFailure(executionRequest, "QUERY", MCPErrorCode.UNSUPPORTED, ex.getMessage());
        } catch (final IllegalArgumentException ex) {
            return recordFailure(executionRequest, "QUERY", MCPErrorCode.INVALID_REQUEST, ex.getMessage());
        }
        if (!databaseCapability.get().getSupportedStatementClasses().contains(classificationResult.getStatementClass())) {
            return recordFailure(executionRequest, classificationResult.getStatementType(), MCPErrorCode.UNSUPPORTED, "Statement class is not supported.");
        }
        switch (classificationResult.getStatementClass()) {
            case TRANSACTION_CONTROL:
            case SAVEPOINT:
                return recordResult(executionRequest, transactionStatementExecutor.execute(
                        executionRequest.getSessionId(), executionRequest.getDatabase(), databaseCapability.get(), classificationResult), classificationResult.getStatementType());
            case QUERY:
                return recordResult(executionRequest, statementExecutor.execute(executionRequest, classificationResult), classificationResult.getStatementType());
            case DML:
                return recordResult(executionRequest, statementExecutor.execute(executionRequest, classificationResult), classificationResult.getStatementType());
            case DDL:
                return recordResult(executionRequest, executeAndRefreshMetadata(executionRequest, classificationResult), classificationResult.getStatementType());
            case DCL:
                return recordResult(executionRequest, executeAndRefreshMetadata(executionRequest, classificationResult), classificationResult.getStatementType());
            case EXPLAIN_ANALYZE:
                return databaseCapability.get().isSupportsExplainAnalyze()
                        ? recordResult(executionRequest, statementExecutor.execute(executionRequest, classificationResult), classificationResult.getStatementType())
                        : recordFailure(executionRequest, classificationResult.getStatementType(), MCPErrorCode.UNSUPPORTED, "EXPLAIN ANALYZE is not supported.");
            default:
                return recordFailure(executionRequest, classificationResult.getStatementType(), MCPErrorCode.UNSUPPORTED, "Statement class is not supported.");
        }
    }
    
    private ExecuteQueryResponse executeAndRefreshMetadata(final ExecutionRequest executionRequest, final ClassificationResult classificationResult) {
        ExecuteQueryResponse result = statementExecutor.execute(executionRequest, classificationResult);
        if (result.isSuccessful()) {
            jdbcMetadataRefresher.refresh(executionRequest.getDatabase());
        }
        return result;
    }
    
    private ExecuteQueryResponse recordResult(final ExecutionRequest executionRequest, final ExecuteQueryResponse response, final String transactionMarker) {
        if (response.isSuccessful()) {
            auditRecorder.recordQueryExecution(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), true, transactionMarker);
            return response;
        }
        MCPErrorCode errorCode = response.getError().get().getCode();
        auditRecorder.recordQueryExecution(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), false, errorCode, transactionMarker);
        return response;
    }
    
    private ExecuteQueryResponse recordFailure(final ExecutionRequest executionRequest, final String transactionMarker, final MCPErrorCode errorCode, final String message) {
        ExecuteQueryResponse result = ExecuteQueryResponse.error(errorCode, message);
        auditRecorder.recordQueryExecution(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), false, errorCode, transactionMarker);
        return result;
    }
}
