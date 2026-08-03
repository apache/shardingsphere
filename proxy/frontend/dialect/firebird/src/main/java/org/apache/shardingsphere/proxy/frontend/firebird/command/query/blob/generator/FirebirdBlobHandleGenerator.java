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
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BLOB handle (p_resp_object) generator for Firebird.
 *
 * <p>Firebird carries object handles as 16 bit values and reserves {@code 0xFFFF} as the invalid object handle,
 * so handles are issued within the range 1 to {@code 0xFFFE}. BLOB operations sent immediately after
 * {@code op_create_blob2} or {@code op_open_blob2}, without waiting for its response, can use the invalid object
 * handle instead of the not yet received BLOB handle, which refers to the latest object created.</p>
 *
 * @see <a href="https://firebirdsql.org/file/documentation/html/en/firebirddocs/wireprotocol/firebird-wire-protocol.html#wireprotocol-blobs-create-v11">Firebird wire protocol - blobs</a>
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdBlobHandleGenerator {
    
    private static final FirebirdBlobHandleGenerator INSTANCE = new FirebirdBlobHandleGenerator();
    
    private static final int INVALID_OBJECT_HANDLE = 0xFFFF;
    
    private static final int MAX_OBJECT_HANDLE = INVALID_OBJECT_HANDLE - 1;
    
    private final Map<Integer, ConnectionBlobHandles> connectionRegistry = new ConcurrentHashMap<>();
    
    public static FirebirdBlobHandleGenerator getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection.
     *
     * @param connectionId connection ID
     */
    public void registerConnection(final int connectionId) {
        connectionRegistry.put(connectionId, new ConnectionBlobHandles());
    }
    
    /**
     * Generate next BLOB handle for connection.
     *
     * <p>The search starts at the lowest free handle, so a handle released by close or cancel is reused at once.</p>
     *
     * @param connectionId connection ID
     * @return generated BLOB handle within the range 1 to {@code 0xFFFE}
     * @throws FirebirdProtocolException when connection is not registered or no free BLOB handle is available
     */
    public int nextBlobHandle(final int connectionId) {
        ConnectionBlobHandles connectionBlobHandles = connectionRegistry.get(connectionId);
        ShardingSpherePreconditions.checkState(null != connectionBlobHandles,
                () -> new FirebirdProtocolException("Connection %d is not registered.", connectionId));
        synchronized (connectionBlobHandles) {
            int handleIndex = connectionBlobHandles.handles.nextClearBit(connectionBlobHandles.searchPosition);
            ShardingSpherePreconditions.checkState(MAX_OBJECT_HANDLE > handleIndex,
                    () -> new FirebirdProtocolException("No free BLOB handles are available for connection %d.", connectionId));
            connectionBlobHandles.handles.set(handleIndex);
            connectionBlobHandles.lastBlobHandle = handleIndex + 1;
            connectionBlobHandles.searchPosition = connectionBlobHandles.lastBlobHandle;
            return connectionBlobHandles.lastBlobHandle;
        }
    }
    
    /**
     * Resolve a BLOB handle, mapping the deferred placeholder handle to the most recently generated one.
     *
     * @param connectionId connection ID
     * @param blobHandle blob handle received from the client
     * @return resolved blob handle
     */
    public int resolveBlobHandle(final int connectionId, final int blobHandle) {
        if (INVALID_OBJECT_HANDLE != blobHandle) {
            return blobHandle;
        }
        ConnectionBlobHandles connectionBlobHandles = connectionRegistry.get(connectionId);
        if (null == connectionBlobHandles) {
            return blobHandle;
        }
        synchronized (connectionBlobHandles) {
            return 0 == connectionBlobHandles.lastBlobHandle ? blobHandle : connectionBlobHandles.lastBlobHandle;
        }
    }
    
    /**
     * Release BLOB handle for connection.
     *
     * @param connectionId connection ID
     * @param blobHandle BLOB handle
     * @throws FirebirdProtocolException when BLOB handle is invalid
     */
    public void releaseBlobHandle(final int connectionId, final int blobHandle) {
        ConnectionBlobHandles connectionBlobHandles = connectionRegistry.get(connectionId);
        if (null == connectionBlobHandles) {
            return;
        }
        ShardingSpherePreconditions.checkState(0 < blobHandle && MAX_OBJECT_HANDLE >= blobHandle,
                () -> new FirebirdProtocolException("Invalid BLOB handle %d.", blobHandle));
        synchronized (connectionBlobHandles) {
            ShardingSpherePreconditions.checkState(connectionBlobHandles.handles.get(blobHandle - 1),
                    () -> new FirebirdProtocolException("Invalid BLOB handle %d.", blobHandle));
            int position = blobHandle - 1;
            connectionBlobHandles.handles.clear(position);
            connectionBlobHandles.searchPosition = Math.min(connectionBlobHandles.searchPosition, position);
            if (blobHandle == connectionBlobHandles.lastBlobHandle) {
                connectionBlobHandles.lastBlobHandle = 0;
            }
        }
    }
    
    /**
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    public void unregisterConnection(final int connectionId) {
        connectionRegistry.remove(connectionId);
    }
    
    private static final class ConnectionBlobHandles {
        
        private final BitSet handles = new BitSet();
        
        private int searchPosition;
        
        private int lastBlobHandle;
    }
    
}
