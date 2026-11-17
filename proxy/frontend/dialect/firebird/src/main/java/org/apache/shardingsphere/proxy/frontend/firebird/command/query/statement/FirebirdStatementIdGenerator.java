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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.frontend.firebird.resource.FirebirdObjectHandleGenerator;

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
        FirebirdObjectHandleGenerator.getInstance().registerConnection(connectionId, FirebirdCommandPacketType.ALLOCATE_STATEMENT);
    }
    
    /**
     * Generate next statement ID for connection.
     *
     * @param connectionId connection ID
     * @return generated statement ID
     */
    @HighFrequencyInvocation
    public int nextStatementId(final int connectionId) {
        AtomicInteger counter = getStatementCounter(connectionId);
        int result = FirebirdObjectHandleGenerator.getInstance().nextHandle(connectionId, FirebirdCommandPacketType.ALLOCATE_STATEMENT, counter.get() + 1);
        counter.set(result);
        return result;
    }
    
    private AtomicInteger getStatementCounter(final int connectionId) {
        AtomicInteger result = connectionRegistry.get(connectionId);
        ShardingSpherePreconditions.checkNotNull(result, () -> new IllegalStateException(new StringBuilder("No statement ID generator found for connectionId: ").append(connectionId).toString()));
        return result;
    }
    
    /**
     * Release an allocated statement handle.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     */
    @HighFrequencyInvocation
    public void releaseStatementId(final int connectionId, final int statementId) {
        FirebirdObjectHandleGenerator.getInstance().releaseHandle(connectionId, FirebirdCommandPacketType.ALLOCATE_STATEMENT, statementId);
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
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    public void unregisterConnection(final int connectionId) {
        connectionRegistry.remove(connectionId);
        FirebirdObjectHandleGenerator.getInstance().unregisterConnection(connectionId, FirebirdCommandPacketType.ALLOCATE_STATEMENT);
    }
}
