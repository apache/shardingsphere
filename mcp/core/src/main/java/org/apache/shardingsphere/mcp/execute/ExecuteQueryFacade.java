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
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler;
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.session.TransactionCommandExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Execute the unified MCP {@code execute_query} contract against an in-memory runtime model.
 */
@RequiredArgsConstructor
public final class ExecuteQueryFacade {
    
    private final StatementClassifier statementClassifier;
    
    private final DatabaseCapabilityAssembler capabilityAssembler;
    
    private final TransactionCommandExecutor transactionCommandExecutor;
    
    private final AuditRecorder auditRecorder;
    
    /**
     * Execute one MCP SQL request.
     *
     * @param executionRequest execution request
     * @return execution response
     */
    public ExecuteQueryResponse execute(final ExecutionRequest executionRequest) {
        Optional<DatabaseCapability> databaseCapability = capabilityAssembler.assembleDatabaseCapability(executionRequest.getDatabase());
        if (databaseCapability.isEmpty()) {
            return recordFailure(executionRequest, "QUERY", ErrorCode.NOT_FOUND, "Database capability does not exist.");
        }
        DatabaseCapability actualDatabaseCapability = databaseCapability.get();
        ClassificationResult classificationResult;
        try {
            classificationResult = statementClassifier.classify(executionRequest.getSql());
        } catch (final UnsupportedOperationException ex) {
            return recordFailure(executionRequest, "QUERY", ErrorCode.UNSUPPORTED, ex.getMessage());
        } catch (final IllegalArgumentException ex) {
            return recordFailure(executionRequest, "QUERY", ErrorCode.INVALID_REQUEST, ex.getMessage());
        }
        if (!actualDatabaseCapability.getSupportedStatementClasses().contains(classificationResult.getStatementClass())) {
            return recordFailure(executionRequest, classificationResult.getStatementType(), ErrorCode.UNSUPPORTED, "Statement class is not supported.");
        }
        switch (classificationResult.getStatementClass()) {
            case TRANSACTION_CONTROL:
            case SAVEPOINT:
                return recordResult(executionRequest, transactionCommandExecutor.execute(executionRequest.getSessionId(),
                        executionRequest.getDatabase(), actualDatabaseCapability, classificationResult),
                        classificationResult.getStatementType());
            case QUERY:
                return recordResult(executionRequest, executeQuery(executionRequest, classificationResult, actualDatabaseCapability), classificationResult.getStatementType());
            case DML:
                return recordResult(executionRequest, executeUpdate(executionRequest, classificationResult), classificationResult.getStatementType());
            case DDL:
                if (executionRequest.getDatabaseRuntime().isAdapterBacked()) {
                    ExecuteQueryResponse ddlResponse = executionRequest.getDatabaseRuntime().execute(executionRequest, classificationResult);
                    if (ddlResponse.isSuccessful()) {
                        executionRequest.getDatabaseRuntime().refreshMetadata(executionRequest.getDatabase());
                    }
                    return recordResult(executionRequest, ddlResponse, classificationResult.getStatementType());
                }
                return recordResult(executionRequest, ExecuteQueryResponse.statementAck(classificationResult.getStatementType(), "Statement executed."),
                        classificationResult.getStatementType());
            case DCL:
                if (executionRequest.getDatabaseRuntime().isAdapterBacked()) {
                    ExecuteQueryResponse dclResponse = executionRequest.getDatabaseRuntime().execute(executionRequest, classificationResult);
                    if (dclResponse.isSuccessful()) {
                        executionRequest.getDatabaseRuntime().refreshMetadata(executionRequest.getDatabase());
                    }
                    return recordResult(executionRequest, dclResponse, classificationResult.getStatementType());
                }
                return recordResult(executionRequest, ExecuteQueryResponse.statementAck(classificationResult.getStatementType(), "Statement executed."),
                        classificationResult.getStatementType());
            case EXPLAIN_ANALYZE:
                if (!actualDatabaseCapability.isSupportsExplainAnalyze()) {
                    return recordFailure(executionRequest, classificationResult.getStatementType(), ErrorCode.UNSUPPORTED, "EXPLAIN ANALYZE is not supported.");
                }
                return recordResult(executionRequest, executeQuery(executionRequest, classificationResult, actualDatabaseCapability), classificationResult.getStatementType());
            default:
                return recordFailure(executionRequest, classificationResult.getStatementType(), ErrorCode.UNSUPPORTED, "Statement class is not supported.");
        }
    }
    
    private ExecuteQueryResponse executeQuery(final ExecutionRequest executionRequest, final ClassificationResult classificationResult, final DatabaseCapability databaseCapability) {
        if (executionRequest.getDatabaseRuntime().isAdapterBacked()) {
            return executionRequest.getDatabaseRuntime().execute(executionRequest, classificationResult);
        }
        Optional<QueryResult> queryResult = executionRequest.getDatabaseRuntime().findQueryResult(executionRequest.getDatabase(),
                classificationResult.getTargetObjectName().orElse("RESULT"));
        if (queryResult.isEmpty()) {
            return ExecuteQueryResponse.error(ErrorCode.NOT_FOUND, "Query target does not exist.");
        }
        int effectiveMaxRows = getEffectiveMaxRows(executionRequest, databaseCapability);
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
    
    private int getEffectiveMaxRows(final ExecutionRequest executionRequest, final DatabaseCapability databaseCapability) {
        int result = executionRequest.getMaxRows();
        if (0 >= result) {
            result = databaseCapability.getMaxRowsDefault();
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
