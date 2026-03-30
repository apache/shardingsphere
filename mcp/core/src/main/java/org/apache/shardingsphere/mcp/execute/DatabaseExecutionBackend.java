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

import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;

/**
 * Database execution backend.
 */
public interface DatabaseExecutionBackend {
    
    /**
     * Execute one classified request.
     *
     * @param executionRequest execution request
     * @param classificationResult classification result
     * @param databaseCapability resolved database capability
     * @return execution response
     */
    ExecuteQueryResponse execute(ExecutionRequest executionRequest, ClassificationResult classificationResult, DatabaseCapability databaseCapability);
    
    /**
     * Begin one transaction on the backend.
     *
     * @param sessionId session identifier
     * @param databaseName logical database name
     */
    void beginTransaction(String sessionId, String databaseName);
    
    /**
     * Commit one backend transaction.
     *
     * @param sessionId session identifier
     */
    void commitTransaction(String sessionId);
    
    /**
     * Roll back one backend transaction.
     *
     * @param sessionId session identifier
     */
    void rollbackTransaction(String sessionId);
    
    /**
     * Create one backend savepoint.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     */
    void createSavepoint(String sessionId, String savepointName);
    
    /**
     * Roll back one backend savepoint.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     */
    void rollbackToSavepoint(String sessionId, String savepointName);
    
    /**
     * Release one backend savepoint.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     */
    void releaseSavepoint(String sessionId, String savepointName);
    
    /**
     * Refresh backend metadata after committed changes.
     *
     * @param databaseName logical database name
     */
    void refreshMetadata(String databaseName);
    
    /**
     * Close one backend session and release resources.
     *
     * @param sessionId session identifier
     */
    void closeSession(String sessionId);
}
