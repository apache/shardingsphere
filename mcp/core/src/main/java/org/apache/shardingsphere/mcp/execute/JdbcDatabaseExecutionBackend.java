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
 * JDBC-backed database execution backend.
 */
@RequiredArgsConstructor
public final class JdbcDatabaseExecutionBackend implements DatabaseExecutionBackend {
    
    private final ShardingSphereExecutionAdapter executionAdapter;
    
    private final Consumer<String> metadataRefresher;
    
    @Override
    public ExecuteQueryResponse execute(final ExecutionRequest executionRequest, final ClassificationResult classificationResult, final DatabaseCapability databaseCapability) {
        return executionAdapter.execute(executionRequest, classificationResult);
    }
    
    @Override
    public void beginTransaction(final String sessionId, final String databaseName) {
        executionAdapter.beginTransaction(sessionId, databaseName);
    }
    
    @Override
    public void commitTransaction(final String sessionId) {
        executionAdapter.commitTransaction(sessionId);
    }
    
    @Override
    public void rollbackTransaction(final String sessionId) {
        executionAdapter.rollbackTransaction(sessionId);
    }
    
    @Override
    public void createSavepoint(final String sessionId, final String savepointName) {
        executionAdapter.createSavepoint(sessionId, savepointName);
    }
    
    @Override
    public void rollbackToSavepoint(final String sessionId, final String savepointName) {
        executionAdapter.rollbackToSavepoint(sessionId, savepointName);
    }
    
    @Override
    public void releaseSavepoint(final String sessionId, final String savepointName) {
        executionAdapter.releaseSavepoint(sessionId, savepointName);
    }
    
    @Override
    public void refreshMetadata(final String databaseName) {
        metadataRefresher.accept(databaseName);
    }
    
    @Override
    public void closeSession(final String sessionId) {
        executionAdapter.closeSession(sessionId);
    }
}
