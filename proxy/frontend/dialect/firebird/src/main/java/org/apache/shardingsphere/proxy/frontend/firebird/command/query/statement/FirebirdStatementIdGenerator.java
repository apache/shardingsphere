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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdObjectHandleGenerator;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Statement ID generator for Firebird.
 */
@HighFrequencyInvocation
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdStatementIdGenerator {
    
    private static final FirebirdStatementIdGenerator INSTANCE = new FirebirdStatementIdGenerator();
    
    private final Map<Integer, AtomicInteger> connectionRegistry = new ConcurrentHashMap<>();
    
    private final Map<Integer, Set<Integer>> allocatedStatementRegistry = new ConcurrentHashMap<>();
    
    /**
     * Get prepared statement registry instance.
     *
     * @return prepared statement registry instance
     */
    public static FirebirdStatementIdGenerator getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection.
     *
     * @param connectionId connection ID
     */
    @HighFrequencyInvocation
    public void registerConnection(final int connectionId) {
        connectionRegistry.put(connectionId, new AtomicInteger());
        Set<Integer> previous = allocatedStatementRegistry.put(connectionId, ConcurrentHashMap.newKeySet());
        if (null != previous) {
            for (int each : previous) {
                FirebirdObjectHandleGenerator.getInstance().releaseHandle(connectionId, each);
            }
        }
    }
    
    /**
     * Generate next statement ID for connection.
     *
     * @param connectionId connection ID
     * @return generated statement ID
     */
    @HighFrequencyInvocation
    public int nextStatementId(final int connectionId) {
        AtomicInteger lastStatementId = getStatementCounter(connectionId);
        int result = FirebirdObjectHandleGenerator.getInstance().nextHandle(connectionId);
        allocatedStatementRegistry.get(connectionId).add(result);
        lastStatementId.set(result);
        return result;
    }
    
    /**
     * Get current statement ID for connection.
     *
     * @param connectionId connection ID
     * @return statement ID
     */
    public int getStatementId(final int connectionId) {
        return getStatementCounter(connectionId).get();
    }
    
    /**
     * Resolve a lazy statement handle using the last allocated connection object.
     *
     * @param connectionId connection ID
     * @param statementId statement ID received on the wire
     * @param lazySend whether the connection uses lazy sends
     * @return resolved statement ID
     */
    public int resolveStatementId(final int connectionId, final int statementId, final boolean lazySend) {
        int normalized = statementId & 0xFFFF;
        return lazySend && 0xFFFF == normalized ? FirebirdObjectHandleGenerator.getInstance().getLastHandle(connectionId) : normalized;
    }
    
    /**
     * Check whether a statement ID is allocated for a connection.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @return whether the statement ID is allocated
     */
    public boolean isStatementAllocated(final int connectionId, final int statementId) {
        Set<Integer> allocatedStatements = allocatedStatementRegistry.get(connectionId);
        return null != allocatedStatements && allocatedStatements.contains(statementId & 0xFFFF);
    }
    
    /**
     * Release an allocated statement ID.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     */
    @HighFrequencyInvocation
    public void releaseStatementId(final int connectionId, final int statementId) {
        Set<Integer> allocatedStatements = allocatedStatementRegistry.get(connectionId);
        int normalized = statementId & 0xFFFF;
        if (null != allocatedStatements && allocatedStatements.remove(normalized)) {
            FirebirdObjectHandleGenerator.getInstance().releaseHandle(connectionId, normalized);
        }
    }
    
    /**
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    @HighFrequencyInvocation
    public void unregisterConnection(final int connectionId) {
        connectionRegistry.remove(connectionId);
        Set<Integer> allocatedStatements = allocatedStatementRegistry.remove(connectionId);
        if (null != allocatedStatements) {
            for (int each : allocatedStatements) {
                FirebirdObjectHandleGenerator.getInstance().releaseHandle(connectionId, each);
            }
        }
    }
    
    private AtomicInteger getStatementCounter(final int connectionId) {
        AtomicInteger result = connectionRegistry.get(connectionId);
        ShardingSpherePreconditions.checkNotNull(result, () -> new IllegalStateException("No statement ID generator found for connectionId: " + connectionId));
        return result;
    }
}
