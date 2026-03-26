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
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityView;
import org.apache.shardingsphere.mcp.metadata.MetadataRefreshCoordinator;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.session.TransactionCommandExecutor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Execute the unified MCP {@code execute_query} contract against an in-memory runtime model.
 */
public final class ExecuteQueryFacade {
    
    private final StatementClassifier statementClassifier;
    
    private final DatabaseCapabilityAssembler capabilityAssembler;
    
    private final TransactionCommandExecutor transactionCommandExecutor;
    
    private final AuditRecorder auditRecorder;
    
    private final MetadataRefreshCoordinator metadataRefreshCoordinator;
    
    /**
     * Construct an execute-query facade.
     *
     * @param statementClassifier statement classifier
     * @param capabilityAssembler database capability assembler
     * @param transactionCommandExecutor transaction command executor
     * @param auditRecorder audit recorder
     * @param metadataRefreshCoordinator metadata refresh coordinator
     */
    public ExecuteQueryFacade(final StatementClassifier statementClassifier, final DatabaseCapabilityAssembler capabilityAssembler,
                              final TransactionCommandExecutor transactionCommandExecutor,
                              final AuditRecorder auditRecorder, final MetadataRefreshCoordinator metadataRefreshCoordinator) {
        this.statementClassifier = statementClassifier;
        this.capabilityAssembler = capabilityAssembler;
        this.transactionCommandExecutor = transactionCommandExecutor;
        this.auditRecorder = auditRecorder;
        this.metadataRefreshCoordinator = metadataRefreshCoordinator;
    }
    
    /**
     * Execute one MCP SQL request.
     *
     * @param executionRequest execution request
     * @return execution response
     */
    public ExecuteQueryResponse execute(final ExecutionRequest executionRequest) {
        ExecutionRequest actualExecutionRequest = Objects.requireNonNull(executionRequest, "executionRequest cannot be null");
        Optional<DatabaseCapabilityView> databaseCapability = capabilityAssembler.assembleDatabaseCapability(actualExecutionRequest.getDatabase(), actualExecutionRequest.getDatabaseType());
        if (databaseCapability.isEmpty()) {
            return recordFailure(actualExecutionRequest, "QUERY", ErrorCode.NOT_FOUND, "Database capability does not exist.");
        }
        ClassificationResult classificationResult;
        try {
            classificationResult = statementClassifier.classify(actualExecutionRequest.getSql());
        } catch (final UnsupportedOperationException ex) {
            return recordFailure(actualExecutionRequest, "QUERY", ErrorCode.UNSUPPORTED, ex.getMessage());
        } catch (final IllegalArgumentException ex) {
            return recordFailure(actualExecutionRequest, "QUERY", ErrorCode.INVALID_REQUEST, ex.getMessage());
        }
        if (!databaseCapability.get().getSupportedStatementClasses().contains(classificationResult.getStatementClass())) {
            return recordFailure(actualExecutionRequest, classificationResult.getStatementType(), ErrorCode.UNSUPPORTED, "Statement class is not supported.");
        }
        switch (classificationResult.getStatementClass()) {
            case TRANSACTION_CONTROL:
            case SAVEPOINT:
                return recordResult(actualExecutionRequest, transactionCommandExecutor.execute(actualExecutionRequest.getSessionId(),
                        actualExecutionRequest.getDatabase(), actualExecutionRequest.getDatabaseType(), classificationResult),
                        classificationResult.getStatementType());
            case QUERY:
                return recordResult(actualExecutionRequest, executeQuery(actualExecutionRequest, classificationResult), classificationResult.getStatementType());
            case DML:
                return recordResult(actualExecutionRequest, executeUpdate(actualExecutionRequest, classificationResult), classificationResult.getStatementType());
            case DDL:
                if (actualExecutionRequest.getDatabaseRuntime().isAdapterBacked()) {
                    ExecuteQueryResponse ddlResponse = actualExecutionRequest.getDatabaseRuntime().execute(actualExecutionRequest, classificationResult);
                    if (ddlResponse.isSuccessful()) {
                        actualExecutionRequest.getDatabaseRuntime().refreshMetadata(actualExecutionRequest.getDatabase());
                        metadataRefreshCoordinator.markStructureChangeCommitted(actualExecutionRequest.getSessionId(), actualExecutionRequest.getDatabase());
                    }
                    return recordResult(actualExecutionRequest, ddlResponse, classificationResult.getStatementType());
                }
                metadataRefreshCoordinator.markStructureChangeCommitted(actualExecutionRequest.getSessionId(), actualExecutionRequest.getDatabase());
                return recordResult(actualExecutionRequest, ExecuteQueryResponse.statementAck(classificationResult.getStatementType(), "Statement executed."),
                        classificationResult.getStatementType());
            case DCL:
                if (actualExecutionRequest.getDatabaseRuntime().isAdapterBacked()) {
                    ExecuteQueryResponse dclResponse = actualExecutionRequest.getDatabaseRuntime().execute(actualExecutionRequest, classificationResult);
                    if (dclResponse.isSuccessful()) {
                        actualExecutionRequest.getDatabaseRuntime().refreshMetadata(actualExecutionRequest.getDatabase());
                        metadataRefreshCoordinator.markDclChangeCommitted(actualExecutionRequest.getSessionId(), actualExecutionRequest.getDatabase());
                    }
                    return recordResult(actualExecutionRequest, dclResponse, classificationResult.getStatementType());
                }
                metadataRefreshCoordinator.markDclChangeCommitted(actualExecutionRequest.getSessionId(), actualExecutionRequest.getDatabase());
                return recordResult(actualExecutionRequest, ExecuteQueryResponse.statementAck(classificationResult.getStatementType(), "Statement executed."),
                        classificationResult.getStatementType());
            case EXPLAIN_ANALYZE:
                if (!databaseCapability.get().isSupportsExplainAnalyze()) {
                    return recordFailure(actualExecutionRequest, classificationResult.getStatementType(), ErrorCode.UNSUPPORTED, "EXPLAIN ANALYZE is not supported.");
                }
                return recordResult(actualExecutionRequest, executeQuery(actualExecutionRequest, classificationResult), classificationResult.getStatementType());
            default:
                return recordFailure(actualExecutionRequest, classificationResult.getStatementType(), ErrorCode.UNSUPPORTED, "Statement class is not supported.");
        }
    }
    
    private ExecuteQueryResponse executeQuery(final ExecutionRequest executionRequest, final ClassificationResult classificationResult) {
        if (executionRequest.getDatabaseRuntime().isAdapterBacked()) {
            return executionRequest.getDatabaseRuntime().execute(executionRequest, classificationResult);
        }
        Optional<QueryResult> queryResult = executionRequest.getDatabaseRuntime().findQueryResult(executionRequest.getDatabase(),
                classificationResult.getTargetObjectName().orElse("RESULT"));
        if (queryResult.isEmpty()) {
            return ExecuteQueryResponse.error(ErrorCode.NOT_FOUND, "Query target does not exist.");
        }
        int effectiveMaxRows = getEffectiveMaxRows(executionRequest);
        List<List<Object>> rows = queryResult.get().getRows();
        boolean truncated = rows.size() > effectiveMaxRows;
        List<List<Object>> actualRows = truncated ? rows.subList(0, effectiveMaxRows) : rows;
        return ExecuteQueryResponse.resultSet(queryResult.get().getColumns(), actualRows, truncated);
    }
    
    private ExecuteQueryResponse executeUpdate(final ExecutionRequest executionRequest, final ClassificationResult classificationResult) {
        if (executionRequest.getDatabaseRuntime().isAdapterBacked()) {
            return executionRequest.getDatabaseRuntime().execute(executionRequest, classificationResult);
        }
        Optional<Integer> updateCount = executionRequest.getDatabaseRuntime().findUpdateCount(executionRequest.getDatabase(),
                classificationResult.getTargetObjectName().orElse("RESULT"));
        return updateCount.map(integer -> ExecuteQueryResponse.updateCount(classificationResult.getStatementType(), integer))
                .orElseGet(() -> ExecuteQueryResponse.error(ErrorCode.NOT_FOUND, "Update target does not exist."));
    }
    
    private int getEffectiveMaxRows(final ExecutionRequest executionRequest) {
        int result = executionRequest.getMaxRows();
        if (0 >= result) {
            result = capabilityAssembler.assembleDatabaseCapability(executionRequest.getDatabase(), executionRequest.getDatabaseType()).get().getMaxRowsDefault();
        }
        return result;
    }
    
    private ExecuteQueryResponse recordResult(final ExecutionRequest executionRequest, final ExecuteQueryResponse response, final String transactionMarker) {
        if (response.isSuccessful()) {
            auditRecorder.recordQueryExecution(executionRequest.getSessionId(), executionRequest.getDatabase(),
                    executionRequest.getSql(), true, transactionMarker);
            return response;
        }
        ErrorCode errorCode = response.getError().get().getErrorCode();
        auditRecorder.recordQueryExecution(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), false, errorCode, transactionMarker);
        return response;
    }
    
    private ExecuteQueryResponse recordFailure(final ExecutionRequest executionRequest, final String transactionMarker, final ErrorCode errorCode, final String message) {
        ExecuteQueryResponse result = ExecuteQueryResponse.error(errorCode, message);
        auditRecorder.recordQueryExecution(executionRequest.getSessionId(), executionRequest.getDatabase(),
                executionRequest.getSql(), false, errorCode, transactionMarker);
        return result;
    }
}
