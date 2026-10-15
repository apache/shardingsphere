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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Statement ID generator for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdStatementIdGenerator {
    
    private static final FirebirdStatementIdGenerator INSTANCE = new FirebirdStatementIdGenerator();
    
    private final Map<Integer, AtomicInteger> connectionRegistry = new ConcurrentHashMap<>();
    
    private final Map<Integer, Collection<Integer>> allocatedStatementIds = new ConcurrentHashMap<>();
    
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
    public void registerConnection(final int connectionId) {
        connectionRegistry.put(connectionId, new AtomicInteger());
        allocatedStatementIds.put(connectionId, ConcurrentHashMap.newKeySet());
    }
    
    /**
     * Generate next statement ID for connection.
     *
     * @param connectionId connection ID
     * @return generated statement ID
     */
    public int nextStatementId(final int connectionId) {
        int result = getStatementCounter(connectionId).incrementAndGet();
        allocatedStatementIds.get(connectionId).add(result);
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
     * Judge whether statement ID is allocated for connection.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @return whether statement ID is allocated and not released
     */
    @HighFrequencyInvocation
    public boolean isAllocated(final int connectionId, final int statementId) {
        Collection<Integer> statementIds = allocatedStatementIds.get(connectionId);
        return null != statementIds && statementIds.contains(statementId);
    }
    
    /**
     * Release statement ID for connection.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     */
    @HighFrequencyInvocation
    public void releaseStatementId(final int connectionId, final int statementId) {
        Collection<Integer> statementIds = allocatedStatementIds.get(connectionId);
        if (null != statementIds) {
            statementIds.remove(statementId);
        }
    }
    
    /**
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    public void unregisterConnection(final int connectionId) {
        connectionRegistry.remove(connectionId);
        allocatedStatementIds.remove(connectionId);
    }
    
    private AtomicInteger getStatementCounter(final int connectionId) {
        AtomicInteger result = connectionRegistry.get(connectionId);
        if (null == result) {
            throw new IllegalStateException("No statement ID generator found for connectionId: " + connectionId);
        }
        return result;
    }
    
}
