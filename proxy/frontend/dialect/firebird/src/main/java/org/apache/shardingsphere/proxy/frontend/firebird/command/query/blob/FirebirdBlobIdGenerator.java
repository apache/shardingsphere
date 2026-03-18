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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * p_resp_blob_id generator for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdBlobIdGenerator {
    
    private static final FirebirdBlobIdGenerator INSTANCE = new FirebirdBlobIdGenerator();
    
    private final Map<Integer, AtomicLong> connectionRegistry = new ConcurrentHashMap<>();
    
    public static FirebirdBlobIdGenerator getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection.
     *
     * @param connectionId connection ID
     */
    public void registerConnection(final int connectionId) {
        connectionRegistry.put(connectionId, new AtomicLong(0L));
    }
    
    /**
     * Generate next blob ID for connection.
     *
     * @param connectionId connection ID
     * @return generated statement ID
     */
    public long nextBlobId(final int connectionId) {
        return connectionRegistry.get(connectionId).incrementAndGet();
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
