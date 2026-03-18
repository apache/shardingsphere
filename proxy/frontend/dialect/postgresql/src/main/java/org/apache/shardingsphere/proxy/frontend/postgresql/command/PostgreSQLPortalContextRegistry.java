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

package org.apache.shardingsphere.proxy.frontend.postgresql.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * PostgreSQL portal context registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLPortalContextRegistry {
    
    private static final PostgreSQLPortalContextRegistry INSTANCE = new PostgreSQLPortalContextRegistry();
    
    private final ConcurrentMap<Integer, PortalContext> portalContexts = new ConcurrentHashMap<>(1024);
    
    /**
     * Get instance of PostgreSQL portal context registry.
     *
     * @return instance of PostgreSQL portal context registry.
     */
    public static PostgreSQLPortalContextRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get PostgreSQL portal context.
     *
     * @param connectionId connection id
     * @return PostgreSQL portal context
     */
    public PortalContext get(final int connectionId) {
        return portalContexts.computeIfAbsent(connectionId, unused -> new PortalContext());
    }
    
    /**
     * Remove PostgreSQL portal context.
     *
     * @param connectionId connection id
     * @return Removed PostgreSQL portal context
     */
    public PortalContext remove(final int connectionId) {
        return portalContexts.remove(connectionId);
    }
}
