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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Binary statement registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BinaryStatementRegistry {
    
    private static final BinaryStatementRegistry INSTANCE = new BinaryStatementRegistry();
    
    private final ConcurrentMap<Integer, ConnectionScopeBinaryStatementRegistry> registries = new ConcurrentHashMap<>();
    
    /**
     * Get instance of binary statement registry.
     *
     * @return instance of binary statement registry.
     */
    public static BinaryStatementRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register.
     *
     * @param connectionId connection id
     */
    public void register(final int connectionId) {
        registries.put(connectionId, new ConnectionScopeBinaryStatementRegistry());
    }
    
    /**
     * Unregister.
     *
     * @param connectionId connection id
     */
    public void unregister(final int connectionId) {
        registries.remove(connectionId);
    }
    
    /**
     * Get connection scope binary statement registry.
     *
     * @param connectionId connection id
     * @return connection scope binary statement registry
     */
    public ConnectionScopeBinaryStatementRegistry get(final int connectionId) {
        return registries.get(connectionId);
    }
}
