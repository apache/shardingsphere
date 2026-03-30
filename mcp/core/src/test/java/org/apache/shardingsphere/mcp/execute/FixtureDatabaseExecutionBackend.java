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
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fixture database execution backend for tests.
 */
@RequiredArgsConstructor
public final class FixtureDatabaseExecutionBackend implements DatabaseExecutionBackend {
    
    private final Map<String, QueryResult> queryResults;
    
    private final Map<String, Integer> updateCounts;
    
    @Override
    public ExecuteQueryResponse execute(final ExecutionRequest executionRequest, final ClassificationResult classificationResult, final DatabaseCapability databaseCapability) {
        switch (classificationResult.getStatementClass()) {
            case QUERY:
            case EXPLAIN_ANALYZE:
                return executeQuery(executionRequest, classificationResult, databaseCapability);
            case DML:
                return executeUpdate(executionRequest, classificationResult);
            case DDL:
            case DCL:
                return ExecuteQueryResponse.statementAck(classificationResult.getStatementType(), "Statement executed.");
            default:
                return ExecuteQueryResponse.error(MCPErrorCode.UNSUPPORTED, "Statement class is not supported.");
        }
    }
    
    @Override
    public void beginTransaction(final String sessionId, final String databaseName) {
    }
    
    @Override
    public void commitTransaction(final String sessionId) {
    }
    
    @Override
    public void rollbackTransaction(final String sessionId) {
    }
    
    @Override
    public void createSavepoint(final String sessionId, final String savepointName) {
    }
    
    @Override
    public void rollbackToSavepoint(final String sessionId, final String savepointName) {
    }
    
    @Override
    public void releaseSavepoint(final String sessionId, final String savepointName) {
    }
    
    @Override
    public void refreshMetadata(final String databaseName) {
    }
    
    @Override
    public void closeSession(final String sessionId) {
    }
    
    private ExecuteQueryResponse executeQuery(final ExecutionRequest executionRequest, final ClassificationResult classificationResult,
                                              final DatabaseCapability databaseCapability) {
        Optional<QueryResult> queryResult = findQueryResult(executionRequest.getDatabase(), classificationResult.getTargetObjectName().orElse("RESULT"));
        if (queryResult.isEmpty()) {
            return ExecuteQueryResponse.error(MCPErrorCode.NOT_FOUND, "Query target does not exist.");
        }
        int effectiveMaxRows = getEffectiveMaxRows(executionRequest, databaseCapability);
        List<List<Object>> rows = queryResult.get().getRows();
        boolean truncated = rows.size() > effectiveMaxRows;
        List<List<Object>> actualRows = truncated ? rows.subList(0, effectiveMaxRows) : rows;
        return ExecuteQueryResponse.resultSet(queryResult.get().getColumns(), actualRows, truncated);
    }
    
    private ExecuteQueryResponse executeUpdate(final ExecutionRequest executionRequest, final ClassificationResult classificationResult) {
        Optional<Integer> updateCount = findUpdateCount(executionRequest.getDatabase(), classificationResult.getTargetObjectName().orElse("RESULT"));
        return updateCount.map(integer -> ExecuteQueryResponse.updateCount(classificationResult.getStatementType(), integer))
                .orElseGet(() -> ExecuteQueryResponse.error(MCPErrorCode.NOT_FOUND, "Update target does not exist."));
    }
    
    private Optional<QueryResult> findQueryResult(final String databaseName, final String objectName) {
        return Optional.ofNullable(queryResults.get(buildKey(databaseName, objectName)));
    }
    
    private Optional<Integer> findUpdateCount(final String databaseName, final String objectName) {
        return Optional.ofNullable(updateCounts.get(buildKey(databaseName, objectName)));
    }
    
    private int getEffectiveMaxRows(final ExecutionRequest executionRequest, final DatabaseCapability databaseCapability) {
        int result = executionRequest.getMaxRows();
        if (0 >= result) {
            result = databaseCapability.getMaxRowsDefault();
        }
        return result;
    }
    
    private String buildKey(final String databaseName, final String objectName) {
        return databaseName + ":" + objectName.toLowerCase();
    }
}
