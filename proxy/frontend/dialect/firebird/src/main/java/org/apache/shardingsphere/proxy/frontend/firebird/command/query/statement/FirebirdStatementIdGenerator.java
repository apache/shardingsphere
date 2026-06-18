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
    }
    
    /**
     * Generate next statement ID for connection.
     *
     * @param connectionId connection ID
     * @return generated statement ID
     */
    public int nextStatementId(final int connectionId) {
        return connectionRegistry.get(connectionId).incrementAndGet();
    }
    
    /**
     * Get current statement ID for connection.
     *
     * @param connectionId connection ID
     * @return statement ID
     */
    public int getStatementId(final int connectionId) {
        return connectionRegistry.get(connectionId).get();
    }
    
    /**
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    public void unregisterConnection(final int connectionId) {
        connectionRegistry.remove(connectionId);
    }
}
