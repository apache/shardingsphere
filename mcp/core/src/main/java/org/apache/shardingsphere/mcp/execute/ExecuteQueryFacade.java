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
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityBuilder;
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.metadata.MetadataRefreshCoordinator;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.session.TransactionCommandExecutor;

import java.util.Optional;

/**
 * Execute the unified MCP {@code execute_query} contract against a database execution backend.
 */
@RequiredArgsConstructor
public final class ExecuteQueryFacade {
    
    private final StatementClassifier statementClassifier;
    
    private final DatabaseCapabilityBuilder capabilityAssembler;
    
    private final TransactionCommandExecutor transactionCommandExecutor;
    
    private final MCPJdbcExecutionAdapter jdbcExecutionAdapter;
    
    private final AuditRecorder auditRecorder;
    
    private final MetadataRefreshCoordinator metadataRefreshCoordinator;
    
    /**
     * Execute one MCP SQL request.
     *
     * @param executionRequest execution request
     * @return execution response
     */
    public ExecuteQueryResponse execute(final ExecutionRequest executionRequest) {
        Optional<DatabaseCapability> databaseCapability = capabilityAssembler.assembleDatabaseCapability(executionRequest.getDatabase());
        if (databaseCapability.isEmpty()) {
            return recordFailure(executionRequest, "QUERY", MCPErrorCode.NOT_FOUND, "Database capability does not exist.");
        }
        DatabaseCapability actualDatabaseCapability = databaseCapability.get();
        ClassificationResult classificationResult;
        try {
            classificationResult = statementClassifier.classify(executionRequest.getSql());
        } catch (final UnsupportedOperationException ex) {
            return recordFailure(executionRequest, "QUERY", MCPErrorCode.UNSUPPORTED, ex.getMessage());
        } catch (final IllegalArgumentException ex) {
            return recordFailure(executionRequest, "QUERY", MCPErrorCode.INVALID_REQUEST, ex.getMessage());
        }
        if (!actualDatabaseCapability.getSupportedStatementClasses().contains(classificationResult.getStatementClass())) {
            return recordFailure(executionRequest, classificationResult.getStatementType(), MCPErrorCode.UNSUPPORTED, "Statement class is not supported.");
        }
        switch (classificationResult.getStatementClass()) {
            case TRANSACTION_CONTROL:
            case SAVEPOINT:
                return recordResult(executionRequest, transactionCommandExecutor.execute(executionRequest.getSessionId(),
                        executionRequest.getDatabase(), actualDatabaseCapability, classificationResult),
                        classificationResult.getStatementType());
            case QUERY:
                return recordResult(executionRequest, jdbcExecutionAdapter.execute(executionRequest, classificationResult),
                        classificationResult.getStatementType());
            case DML:
                return recordResult(executionRequest, jdbcExecutionAdapter.execute(executionRequest, classificationResult),
                        classificationResult.getStatementType());
            case DDL:
                return recordResult(executionRequest, executeAndRefreshMetadata(executionRequest, classificationResult),
                        classificationResult.getStatementType());
            case DCL:
                return recordResult(executionRequest, executeAndRefreshMetadata(executionRequest, classificationResult),
                        classificationResult.getStatementType());
            case EXPLAIN_ANALYZE:
                if (!actualDatabaseCapability.isSupportsExplainAnalyze()) {
                    return recordFailure(executionRequest, classificationResult.getStatementType(), MCPErrorCode.UNSUPPORTED, "EXPLAIN ANALYZE is not supported.");
                }
                return recordResult(executionRequest, jdbcExecutionAdapter.execute(executionRequest, classificationResult),
                        classificationResult.getStatementType());
            default:
                return recordFailure(executionRequest, classificationResult.getStatementType(), MCPErrorCode.UNSUPPORTED, "Statement class is not supported.");
        }
    }
    
    private ExecuteQueryResponse executeAndRefreshMetadata(final ExecutionRequest executionRequest, final ClassificationResult classificationResult) {
        ExecuteQueryResponse result = jdbcExecutionAdapter.execute(executionRequest, classificationResult);
        if (result.isSuccessful()) {
            metadataRefreshCoordinator.refresh(executionRequest.getDatabase());
        }
        return result;
    }
    
    private ExecuteQueryResponse recordResult(final ExecutionRequest executionRequest, final ExecuteQueryResponse response, final String transactionMarker) {
        if (response.isSuccessful()) {
            auditRecorder.recordQueryExecution(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), true, transactionMarker);
            return response;
        }
        MCPErrorCode errorCode = response.getError().get().getErrorCode();
        auditRecorder.recordQueryExecution(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), false, errorCode, transactionMarker);
        return response;
    }
    
    private ExecuteQueryResponse recordFailure(final ExecutionRequest executionRequest, final String transactionMarker, final MCPErrorCode errorCode, final String message) {
        ExecuteQueryResponse result = ExecuteQueryResponse.error(errorCode, message);
        auditRecorder.recordQueryExecution(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), false, errorCode, transactionMarker);
        return result;
    }
}
