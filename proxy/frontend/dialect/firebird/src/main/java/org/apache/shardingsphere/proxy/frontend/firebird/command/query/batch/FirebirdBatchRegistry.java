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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Batch registry for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdBatchRegistry {
    
    private static final FirebirdBatchRegistry INSTANCE = new FirebirdBatchRegistry();
    
    private final Map<Integer, Map<Integer, FirebirdBatchStatement>> batchRegistry = new ConcurrentHashMap<>();
    
    /**
     * Get batch registry instance.
     *
     * @return batch registry instance
     */
    public static FirebirdBatchRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection.
     *
     * @param connectionId connection ID
     */
    public void registerConnection(final int connectionId) {
        batchRegistry.put(connectionId, new LinkedHashMap<>());
    }
    
    /**
     * Register batch metadata.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @param batchStatement batch statement
     */
    public void registerBatchStatement(final int connectionId, final int statementId, final FirebirdBatchStatement batchStatement) {
        batchRegistry.get(connectionId).put(statementId, batchStatement);
    }
    
    /**
     * Get batch metadata.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @return batch statement
     */
    public FirebirdBatchStatement getBatchStatement(final int connectionId, final int statementId) {
        return batchRegistry.get(connectionId).get(statementId);
    }
    
    /**
     * Unregister batch metadata.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     */
    public void unregisterBatchStatement(final int connectionId, final int statementId) {
        batchRegistry.get(connectionId).remove(statementId);
    }
    
    /**
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    public void unregisterConnection(final int connectionId) {
        batchRegistry.remove(connectionId);
    }
}
