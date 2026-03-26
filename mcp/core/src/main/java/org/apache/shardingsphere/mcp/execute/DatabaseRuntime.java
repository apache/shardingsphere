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
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * In-memory execution runtime for tests and bootstrap adapters.
 */
@Getter
public final class DatabaseRuntime {
    
    private final Map<String, QueryResult> queryResults;
    
    private final Map<String, Integer> updateCounts;
    
    @Getter(AccessLevel.NONE)
    private final ShardingSphereExecutionAdapter executionAdapter;
    
    private final Consumer<String> metadataRefresher;
    
    /**
     * Construct one in-memory execution runtime.
     *
     * @param queryResults query results keyed by {@code database:object}
     * @param updateCounts update counts keyed by {@code database:object}
     */
    public DatabaseRuntime(final Map<String, QueryResult> queryResults, final Map<String, Integer> updateCounts) {
        this(queryResults, updateCounts, null, ignored -> {
        });
    }
    
    /**
     * Construct one adapter-backed execution runtime.
     *
     * @param executionAdapter execution adapter
     * @param metadataRefresher metadata refresh callback
     */
    public DatabaseRuntime(final ShardingSphereExecutionAdapter executionAdapter, final Consumer<String> metadataRefresher) {
        this(Collections.emptyMap(), Collections.emptyMap(), Objects.requireNonNull(executionAdapter, "executionAdapter cannot be null"), metadataRefresher);
    }
    
    private DatabaseRuntime(final Map<String, QueryResult> queryResults, final Map<String, Integer> updateCounts,
                            final ShardingSphereExecutionAdapter executionAdapter, final Consumer<String> metadataRefresher) {
        this.queryResults = queryResults;
        this.updateCounts = updateCounts;
        this.executionAdapter = executionAdapter;
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
        return null != executionAdapter;
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
        return executionAdapter;
    }
    
    private String buildKey(final String database, final String objectName) {
        return database + ":" + objectName.toLowerCase();
    }
}
