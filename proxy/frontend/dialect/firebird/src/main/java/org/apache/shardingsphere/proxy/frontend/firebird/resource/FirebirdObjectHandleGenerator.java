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

package org.apache.shardingsphere.proxy.frontend.firebird.resource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.BitSet;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared Firebird wire object handle allocator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdObjectHandleGenerator {
    
    private static final FirebirdObjectHandleGenerator INSTANCE = new FirebirdObjectHandleGenerator();
    
    private static final int MAX_OBJECT_HANDLE = 0xFFFE;
    
    private static final int MAX_STATEMENT_TRANSACTION_HANDLE = 65000;
    
    private final Map<Integer, ConnectionHandles> connectionRegistry = new ConcurrentHashMap<>();
    
    /**
     * Get the shared wire handle allocator.
     *
     * @return allocator instance
     */
    public static FirebirdObjectHandleGenerator getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register an object kind for a connection.
     *
     * @param connectionId connection ID
     * @param type object kind
     */
    public void registerConnection(final int connectionId, final FirebirdCommandPacketType type) {
        connectionRegistry.compute(connectionId, (key, value) -> {
            ConnectionHandles result = null == value ? new ConnectionHandles() : value;
            synchronized (result) {
                result.release(type);
                result.owners.put(type, new BitSet());
            }
            return result;
        });
    }
    
    /**
     * Allocate a unique wire object handle.
     *
     * @param connectionId connection ID
     * @param type object kind
     * @param preferredHandle preferred handle before reusing lower free slots
     * @return allocated handle
     */
    @HighFrequencyInvocation
    public int nextHandle(final int connectionId, final FirebirdCommandPacketType type, final int preferredHandle) {
        ConnectionHandles handles = connectionRegistry.get(connectionId);
        synchronized (handles) {
            int maxHandle = FirebirdCommandPacketType.CREATE_BLOB == type ? MAX_OBJECT_HANDLE : MAX_STATEMENT_TRANSACTION_HANDLE;
            int index = handles.allocated.nextClearBit(Math.max(handles.searchPosition, preferredHandle - 1));
            if (index >= maxHandle) {
                index = handles.allocated.nextClearBit(handles.searchPosition);
            }
            ShardingSpherePreconditions.checkState(index < maxHandle,
                    () -> new DatabaseProtocolException("No free %s handles are available for connection %d.", FirebirdCommandPacketType.CREATE_BLOB == type ? "BLOB" : "object", connectionId));
            handles.allocated.set(index);
            handles.owners.get(type).set(index);
            if (index == handles.searchPosition) {
                handles.searchPosition = index + 1;
            }
            return index + 1;
        }
    }
    
    /**
     * Release a handle belonging to the requested object kind.
     *
     * @param connectionId connection ID
     * @param type object kind
     * @param handle object handle
     */
    @HighFrequencyInvocation
    public void releaseHandle(final int connectionId, final FirebirdCommandPacketType type, final int handle) {
        ConnectionHandles handles = connectionRegistry.get(connectionId);
        synchronized (handles) {
            if (handle > 0 && handle <= MAX_OBJECT_HANDLE && handles.owners.get(type).get(handle - 1)) {
                handles.owners.get(type).clear(handle - 1);
                handles.allocated.clear(handle - 1);
                handles.searchPosition = Math.min(handles.searchPosition, handle - 1);
            }
        }
    }
    
    /**
     * Unregister an object kind, releasing its handles.
     *
     * @param connectionId connection ID
     * @param type object kind
     */
    public void unregisterConnection(final int connectionId, final FirebirdCommandPacketType type) {
        connectionRegistry.computeIfPresent(connectionId, (key, value) -> {
            synchronized (value) {
                value.release(type);
                return value.owners.isEmpty() ? null : value;
            }
        });
    }
    
    private static final class ConnectionHandles {
        
        private final BitSet allocated = new BitSet();
        
        private final Map<FirebirdCommandPacketType, BitSet> owners = new EnumMap<>(FirebirdCommandPacketType.class);
        
        private int searchPosition;
        
        private void release(final FirebirdCommandPacketType type) {
            BitSet handles = owners.remove(type);
            if (null != handles) {
                allocated.andNot(handles);
                int firstHandle = handles.nextSetBit(0);
                if (firstHandle >= 0) {
                    searchPosition = Math.min(searchPosition, firstHandle);
                }
            }
        }
    }
}
