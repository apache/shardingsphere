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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BLOB handle (p_resp_object) generator for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdBlobHandleGenerator {
    
    private static final FirebirdBlobHandleGenerator INSTANCE = new FirebirdBlobHandleGenerator();
    
    private static final int INVALID_OBJECT_HANDLE = 0xFFFF;
    
    private static final int MAX_OBJECT_HANDLE = INVALID_OBJECT_HANDLE - 1;
    
    private final Map<Integer, AtomicInteger> connectionRegistry = new ConcurrentHashMap<>();
    
    public static FirebirdBlobHandleGenerator getInstance() {
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
     * Generate next BLOB handle for connection.
     *
     * <p>Firebird carries object handles as 16 bit values, and {@code 0xFFFF} is reserved as the deferred
     * placeholder handle, so generated handles wrap within the range 1 to {@code 0xFFFE}. Handle 0 is never
     * issued because it marks a connection that has not generated a handle yet.</p>
     *
     * @param connectionId connection ID
     * @return generated BLOB handle
     */
    public int nextBlobHandle(final int connectionId) {
        return connectionRegistry.get(connectionId).updateAndGet(current -> MAX_OBJECT_HANDLE <= current ? 1 : current + 1);
    }
    
    /**
     * Resolve a BLOB handle, mapping the deferred placeholder handle to the most recently generated one.
     *
     * <p>In the Firebird lazy (deferred) protocol a pipelined operation such as {@code op_put_segment},
     * {@code op_get_segment} or {@code op_info_blob} that is flushed together with its preceding
     * {@code op_create_blob2} or {@code op_open_blob2} carries the placeholder handle {@code 0xFFFF}
     * (INVALID_OBJECT), which the server resolves to the most recently created object. Handles are generated
     * here for both created and opened BLOBs, so the last generated handle mirrors that resolution.</p>
     *
     * @param connectionId connection ID
     * @param blobHandle blob handle received from the client
     * @return resolved blob handle
     */
    public int resolveBlobHandle(final int connectionId, final int blobHandle) {
        if (INVALID_OBJECT_HANDLE != blobHandle) {
            return blobHandle;
        }
        AtomicInteger lastGenerated = connectionRegistry.get(connectionId);
        return null == lastGenerated || 0 == lastGenerated.get() ? blobHandle : lastGenerated.get();
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
