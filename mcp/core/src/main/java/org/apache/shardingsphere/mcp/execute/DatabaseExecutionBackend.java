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

import java.util.function.Consumer;

/**
 * Database execution backend.
 */
@RequiredArgsConstructor
public final class DatabaseExecutionBackend {
    
    private final ShardingSphereExecutionAdapter executionAdapter;
    
    private final Consumer<String> metadataRefresher;
    
    /**
     * Execute one classified request.
     *
     * @param executionRequest execution request
     * @param classificationResult classification result
     * @param databaseCapability resolved database capability
     * @return execution response
     */
    public ExecuteQueryResponse execute(final ExecutionRequest executionRequest, final ClassificationResult classificationResult, final DatabaseCapability databaseCapability) {
        return executionAdapter.execute(executionRequest, classificationResult);
    }
    
    /**
     * Begin one transaction on the backend.
     *
     * @param sessionId session identifier
     * @param databaseName logical database name
     */
    public void beginTransaction(final String sessionId, final String databaseName) {
        executionAdapter.beginTransaction(sessionId, databaseName);
    }
    
    /**
     * Commit one backend transaction.
     *
     * @param sessionId session identifier
     */
    public void commitTransaction(final String sessionId) {
        executionAdapter.commitTransaction(sessionId);
    }
    
    /**
     * Roll back one backend transaction.
     *
     * @param sessionId session identifier
     */
    public void rollbackTransaction(final String sessionId) {
        executionAdapter.rollbackTransaction(sessionId);
    }
    
    /**
     * Create one backend savepoint.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     */
    public void createSavepoint(final String sessionId, final String savepointName) {
        executionAdapter.createSavepoint(sessionId, savepointName);
    }
    
    /**
     * Roll back one backend savepoint.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     */
    public void rollbackToSavepoint(final String sessionId, final String savepointName) {
        executionAdapter.rollbackToSavepoint(sessionId, savepointName);
    }
    
    /**
     * Release one backend savepoint.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     */
    public void releaseSavepoint(final String sessionId, final String savepointName) {
        executionAdapter.releaseSavepoint(sessionId, savepointName);
    }
    
    /**
     * Refresh backend metadata after committed changes.
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
        executionAdapter.closeSession(sessionId);
    }
}
