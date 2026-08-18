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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Transaction ID generator for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdTransactionIdGenerator {
    
    private static final FirebirdTransactionIdGenerator INSTANCE = new FirebirdTransactionIdGenerator();
    
    private final Map<Integer, AtomicInteger> connectionRegistry = new ConcurrentHashMap<>();
    
    private final Map<Integer, Set<Integer>> activeTransactionRegistry = new ConcurrentHashMap<>();
    
    /**
     * Get prepared transaction registry instance.
     *
     * @return prepared statement registry instance
     */
    public static FirebirdTransactionIdGenerator getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection.
     *
     * @param connectionId connection ID
     */
    public void registerConnection(final int connectionId) {
        connectionRegistry.put(connectionId, new AtomicInteger());
        activeTransactionRegistry.put(connectionId, ConcurrentHashMap.newKeySet());
    }
    
    /**
     * Generate next transaction ID for connection.
     *
     * @param connectionId connection ID
     * @return generated transaction ID
     */
    public int nextTransactionId(final int connectionId) {
        int result = getTransactionCounter(connectionId).incrementAndGet();
        getActiveTransactions(connectionId).add(result);
        return result;
    }
    
    /**
     * Judge whether transaction is active for connection.
     *
     * @param connectionId connection ID
     * @param transactionId transaction ID
     * @return is transaction active or not
     */
    public boolean isTransactionActive(final int connectionId, final int transactionId) {
        return getActiveTransactions(connectionId).contains(transactionId);
    }
    
    /**
     * Judge whether any transaction is active for connection.
     *
     * @param connectionId connection ID
     * @return has active transaction or not
     */
    public boolean hasActiveTransaction(final int connectionId) {
        return !getActiveTransactions(connectionId).isEmpty();
    }
    
    /**
     * Close transaction for connection.
     *
     * @param connectionId connection ID
     * @param transactionId transaction ID
     */
    public void closeTransaction(final int connectionId, final int transactionId) {
        getActiveTransactions(connectionId).remove(transactionId);
    }
    
    /**
     * Get current transaction ID for connection.
     *
     * @param connectionId connection ID
     * @return transaction ID
     */
    public int getTransactionId(final int connectionId) {
        return getTransactionCounter(connectionId).get();
    }
    
    /**
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    public void unregisterConnection(final int connectionId) {
        connectionRegistry.remove(connectionId);
        activeTransactionRegistry.remove(connectionId);
    }
    
    private AtomicInteger getTransactionCounter(final int connectionId) {
        AtomicInteger result = connectionRegistry.get(connectionId);
        if (null == result) {
            throw new IllegalStateException("No transaction ID generator found for connectionId: " + connectionId);
        }
        return result;
    }
    
    private Set<Integer> getActiveTransactions(final int connectionId) {
        Set<Integer> result = activeTransactionRegistry.get(connectionId);
        if (null == result) {
            throw new IllegalStateException("No transaction ID generator found for connectionId: " + connectionId);
        }
        return result;
    }
    
}
