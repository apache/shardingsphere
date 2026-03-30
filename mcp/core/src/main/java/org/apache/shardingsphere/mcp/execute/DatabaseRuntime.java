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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * In-memory execution runtime for tests and bootstrap adapters.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class DatabaseRuntime {
    
    private final Map<String, QueryResult> queryResults;
    
    private final Map<String, Integer> updateCounts;
    
    @Getter(AccessLevel.NONE)
    private final ShardingSphereExecutionAdapter executionAdapter;
    
    private final Consumer<String> metadataRefresher;
    
    public DatabaseRuntime(final Map<String, QueryResult> queryResults, final Map<String, Integer> updateCounts) {
        this(queryResults, updateCounts, null, ignored -> {
        });
    }
    
    public DatabaseRuntime(final ShardingSphereExecutionAdapter executionAdapter, final Consumer<String> metadataRefresher) {
        this(Collections.emptyMap(), Collections.emptyMap(), executionAdapter, metadataRefresher);
    }
    
    /**
     * Find one query result definition.
     *
     * @param databaseName logical database name
     * @param objectName object name
     * @return query result when present
     */
    public Optional<QueryResult> findQueryResult(final String databaseName, final String objectName) {
        return Optional.ofNullable(queryResults.get(buildKey(databaseName, objectName)));
    }
    
    /**
     * Find one update count definition.
     *
     * @param databaseName logical database name
     * @param objectName object name
     * @return update count when present
     */
    public Optional<Integer> findUpdateCount(final String databaseName, final String objectName) {
        return Optional.ofNullable(updateCounts.get(buildKey(databaseName, objectName)));
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
     * @param databaseName logical database name
     */
    public void beginTransaction(final String sessionId, final String databaseName) {
        if (isAdapterBacked()) {
            getRequiredExecutionAdapter().beginTransaction(sessionId, databaseName);
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
     * @param databaseName logical database name
     */
    public void refreshMetadata(final String databaseName) {
        metadataRefresher.accept(databaseName);
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
        ShardingSpherePreconditions.checkState(isAdapterBacked(), () -> new IllegalStateException("Execution adapter does not exist."));
        return executionAdapter;
    }
    
    private String buildKey(final String databaseName, final String objectName) {
        return databaseName + ":" + objectName.toLowerCase();
    }
}
