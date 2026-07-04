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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
        batchRegistry.put(connectionId, new ConcurrentHashMap<>());
    }
    
    /**
     * Register batch metadata.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @param batchStatement batch statement
     * @throws IllegalStateException if connection is not registered
     */
    public void registerBatchStatement(final int connectionId, final int statementId, final FirebirdBatchStatement batchStatement) {
        Map<Integer, FirebirdBatchStatement> statements = batchRegistry.get(connectionId);
        if (statements == null) {
            throw new IllegalStateException("Connection [" + connectionId + "] is not registered.");
        }
        statements.put(statementId, batchStatement);
    }
    
    /**
     * Get batch metadata.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @return batch statement or null if not found
     */
    public FirebirdBatchStatement getBatchStatement(final int connectionId, final int statementId) {
        Map<Integer, FirebirdBatchStatement> statements = batchRegistry.get(connectionId);
        if (statements == null) {
            return null;
        }
        return statements.get(statementId);
    }
    
    /**
     * Unregister batch metadata.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     */
    public void unregisterBatchStatement(final int connectionId, final int statementId) {
        Map<Integer, FirebirdBatchStatement> statements = batchRegistry.get(connectionId);
        if (statements != null) {
            statements.remove(statementId);
        }
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
