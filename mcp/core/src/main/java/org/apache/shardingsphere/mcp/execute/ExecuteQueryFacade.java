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

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.mcp.audit.AuditRecorder;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler.DatabaseCapabilityView;
import org.apache.shardingsphere.mcp.execute.StatementClassifier.ClassificationResult;
import org.apache.shardingsphere.mcp.metadata.MetadataRefreshCoordinator;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse.ColumnDefinition;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse.ErrorCode;
import org.apache.shardingsphere.mcp.session.TransactionCommandExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

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
        this.statementClassifier = Objects.requireNonNull(statementClassifier, "statementClassifier cannot be null");
        this.capabilityAssembler = Objects.requireNonNull(capabilityAssembler, "capabilityAssembler cannot be null");
        this.transactionCommandExecutor = Objects.requireNonNull(transactionCommandExecutor, "transactionCommandExecutor cannot be null");
        this.auditRecorder = Objects.requireNonNull(auditRecorder, "auditRecorder cannot be null");
        this.metadataRefreshCoordinator = Objects.requireNonNull(metadataRefreshCoordinator, "metadataRefreshCoordinator cannot be null");
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
                        actualExecutionRequest.getDatabase(), actualExecutionRequest.getDatabaseType(), classificationResult.getNormalizedSql()),
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
    
    /**
     * Execute-query request contract.
     */
    @Getter
    public static final class ExecutionRequest {
        
        private final String sessionId;
        
        private final String database;
        
        private final String databaseType;
        
        private final String schema;
        
        private final String sql;
        
        private final int maxRows;
        
        private final int timeoutMs;
        
        private final DatabaseRuntime databaseRuntime;
        
        private final long nowMillis;
        
        /**
         * Construct an execute-query request.
         *
         * @param sessionId session identifier
         * @param database logical database name
         * @param databaseType database type
         * @param schema schema name
         * @param sql SQL text
         * @param maxRows max rows
         * @param timeoutMs timeout milliseconds
         * @param databaseRuntime database runtime
         * @param nowMillis current time
         */
        public ExecutionRequest(final String sessionId, final String database, final String databaseType, final String schema,
                                final String sql, final int maxRows, final int timeoutMs, final DatabaseRuntime databaseRuntime, final long nowMillis) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null");
            this.database = Objects.requireNonNull(database, "database cannot be null");
            this.databaseType = Objects.requireNonNull(databaseType, "databaseType cannot be null");
            this.schema = Objects.requireNonNull(schema, "schema cannot be null");
            this.sql = Objects.requireNonNull(sql, "sql cannot be null");
            this.maxRows = maxRows;
            this.timeoutMs = timeoutMs;
            this.databaseRuntime = Objects.requireNonNull(databaseRuntime, "databaseRuntime cannot be null");
            this.nowMillis = nowMillis;
        }
    }
    
    /**
     * In-memory execution runtime for tests and bootstrap adapters.
     */
    @Getter
    public static final class DatabaseRuntime {
        
        private final Map<String, QueryResult> queryResults;
        
        private final Map<String, Integer> updateCounts;
        
        @Getter(AccessLevel.NONE)
        private final List<ShardingSphereExecutionAdapter> executionAdapters;
        
        private final Consumer<String> metadataRefresher;
        
        /**
         * Construct one in-memory execution runtime.
         *
         * @param queryResults query results keyed by {@code database:object}
         * @param updateCounts update counts keyed by {@code database:object}
         */
        public DatabaseRuntime(final Map<String, QueryResult> queryResults, final Map<String, Integer> updateCounts) {
            this(queryResults, updateCounts, Collections.emptyList(), ignored -> {
            });
        }
        
        /**
         * Construct one adapter-backed execution runtime.
         *
         * @param executionAdapter execution adapter
         * @param metadataRefresher metadata refresh callback
         */
        public DatabaseRuntime(final ShardingSphereExecutionAdapter executionAdapter, final Consumer<String> metadataRefresher) {
            this(Collections.emptyMap(), Collections.emptyMap(), Collections.singletonList(Objects.requireNonNull(executionAdapter, "executionAdapter cannot be null")),
                    Objects.requireNonNull(metadataRefresher, "metadataRefresher cannot be null"));
        }
        
        private DatabaseRuntime(final Map<String, QueryResult> queryResults, final Map<String, Integer> updateCounts,
                                final Collection<ShardingSphereExecutionAdapter> executionAdapters, final Consumer<String> metadataRefresher) {
            this.queryResults = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(queryResults, "queryResults cannot be null")));
            this.updateCounts = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(updateCounts, "updateCounts cannot be null")));
            this.executionAdapters = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(executionAdapters, "executionAdapters cannot be null")));
            this.metadataRefresher = Objects.requireNonNull(metadataRefresher, "metadataRefresher cannot be null");
        }
        
        /**
         * Find one query result definition.
         *
         * @param database logical database name
         * @param objectName object name
         * @return query result when present
         */
        public Optional<QueryResult> findQueryResult(final String database, final String objectName) {
            return Optional.ofNullable(queryResults.get(buildKey(database, objectName)));
        }
        
        /**
         * Find one update count definition.
         *
         * @param database logical database name
         * @param objectName object name
         * @return update count when present
         */
        public Optional<Integer> findUpdateCount(final String database, final String objectName) {
            return Optional.ofNullable(updateCounts.get(buildKey(database, objectName)));
        }
        
        /**
         * Determine whether the runtime delegates to one real execution adapter.
         *
         * @return {@code true} when backed by one execution adapter
         */
        public boolean isAdapterBacked() {
            return !executionAdapters.isEmpty();
        }
        
        /**
         * Execute one classified request through the adapter.
         *
         * @param executionRequest execution request
         * @param classificationResult classification result
         * @return execution response
         */
        public ExecuteQueryResponse execute(final ExecutionRequest executionRequest, final ClassificationResult classificationResult) {
            return getRequiredExecutionAdapter().execute(executionRequest, classificationResult);
        }
        
        /**
         * Begin one transaction on the runtime backend.
         *
         * @param sessionId session identifier
         * @param database logical database name
         */
        public void beginTransaction(final String sessionId, final String database) {
            if (isAdapterBacked()) {
                getRequiredExecutionAdapter().beginTransaction(sessionId, database);
            }
        }
        
        /**
         * Commit one backend transaction.
         *
         * @param sessionId session identifier
         */
        public void commitTransaction(final String sessionId) {
            if (isAdapterBacked()) {
                getRequiredExecutionAdapter().commitTransaction(sessionId);
            }
        }
        
        /**
         * Roll back one backend transaction.
         *
         * @param sessionId session identifier
         */
        public void rollbackTransaction(final String sessionId) {
            if (isAdapterBacked()) {
                getRequiredExecutionAdapter().rollbackTransaction(sessionId);
            }
        }
        
        /**
         * Create one backend savepoint.
         *
         * @param sessionId session identifier
         * @param savepointName savepoint name
         */
        public void createSavepoint(final String sessionId, final String savepointName) {
            if (isAdapterBacked()) {
                getRequiredExecutionAdapter().createSavepoint(sessionId, savepointName);
            }
        }
        
        /**
         * Roll back one backend savepoint.
         *
         * @param sessionId session identifier
         * @param savepointName savepoint name
         */
        public void rollbackToSavepoint(final String sessionId, final String savepointName) {
            if (isAdapterBacked()) {
                getRequiredExecutionAdapter().rollbackToSavepoint(sessionId, savepointName);
            }
        }
        
        /**
         * Release one backend savepoint.
         *
         * @param sessionId session identifier
         * @param savepointName savepoint name
         */
        public void releaseSavepoint(final String sessionId, final String savepointName) {
            if (isAdapterBacked()) {
                getRequiredExecutionAdapter().releaseSavepoint(sessionId, savepointName);
            }
        }
        
        /**
         * Refresh runtime metadata after committed changes.
         *
         * @param database logical database name
         */
        public void refreshMetadata(final String database) {
            metadataRefresher.accept(Objects.requireNonNull(database, "database cannot be null"));
        }
        
        /**
         * Close one backend session and release resources.
         *
         * @param sessionId session identifier
         */
        public void closeSession(final String sessionId) {
            if (isAdapterBacked()) {
                getRequiredExecutionAdapter().closeSession(sessionId);
            }
        }
        
        private ShardingSphereExecutionAdapter getRequiredExecutionAdapter() {
            if (!isAdapterBacked()) {
                throw new IllegalStateException("Execution adapter does not exist.");
            }
            return executionAdapters.get(0);
        }
        
        private String buildKey(final String database, final String objectName) {
            return database + ":" + objectName.toLowerCase();
        }
    }
    
    /**
     * Query result definition for the in-memory runtime.
     */
    @Getter
    public static final class QueryResult {
        
        private final List<ColumnDefinition> columns;
        
        private final List<List<Object>> rows;
        
        /**
         * Construct one query result definition.
         *
         * @param columns columns
         * @param rows rows
         */
        public QueryResult(final Collection<ColumnDefinition> columns, final Collection<List<Object>> rows) {
            this.columns = Collections.unmodifiableList(new LinkedList<>(Objects.requireNonNull(columns, "columns cannot be null")));
            this.rows = Collections.unmodifiableList(new LinkedList<>(Objects.requireNonNull(rows, "rows cannot be null")));
        }
    }
}
