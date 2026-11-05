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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Object handle generator shared by Firebird transactions, statements and BLOBs.
 */
@HighFrequencyInvocation
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdObjectHandleGenerator {
    
    private static final FirebirdObjectHandleGenerator INSTANCE = new FirebirdObjectHandleGenerator();
    
    private static final int INVALID_OBJECT_HANDLE = 0xFFFF;
    
    private final Map<Integer, ConnectionHandles> connections = new ConcurrentHashMap<>();
    
    /**
     * Get object handle generator instance.
     *
     * @return object handle generator instance
     */
    @HighFrequencyInvocation
    public static FirebirdObjectHandleGenerator getInstance() {
        return INSTANCE;
    }
    
    /**
     * Generate the lowest available object handle for a connection.
     *
     * @param connectionId connection ID
     * @return generated handle
     */
    @HighFrequencyInvocation
    public int nextHandle(final int connectionId) {
        ConnectionHandles handles = connections.computeIfAbsent(connectionId, ignored -> new ConnectionHandles());
        synchronized (handles) {
            int result = handles.allocated.nextClearBit(handles.searchPosition);
            ShardingSpherePreconditions.checkState(result < INVALID_OBJECT_HANDLE, () -> new DatabaseProtocolException("No free object handles are available for connection %d.", connectionId));
            handles.allocated.set(result);
            handles.searchPosition = result + 1;
            handles.lastHandle = result;
            return result;
        }
    }
    
    /**
     * Get the last object handle allocated for a connection.
     *
     * @param connectionId connection ID
     * @return last allocated handle, or zero if none was allocated
     */
    public int getLastHandle(final int connectionId) {
        ConnectionHandles handles = connections.get(connectionId);
        if (null == handles) {
            return 0;
        }
        synchronized (handles) {
            return handles.lastHandle;
        }
    }
    
    /**
     * Release an object handle for a connection.
     *
     * @param connectionId connection ID
     * @param handle object handle
     */
    @HighFrequencyInvocation
    public void releaseHandle(final int connectionId, final int handle) {
        ConnectionHandles handles = connections.get(connectionId);
        if (null == handles) {
            return;
        }
        synchronized (handles) {
            handles.allocated.clear(handle);
            handles.searchPosition = Math.min(handles.searchPosition, handle);
        }
    }
    
    /**
     * Unregister connection resources.
     *
     * @param connectionId connection ID
     */
    @HighFrequencyInvocation
    public void unregisterConnection(final int connectionId) {
        connections.remove(connectionId);
    }
    
    private static final class ConnectionHandles {
        
        private final BitSet allocated = new BitSet();
        
        private int searchPosition = 1;
        
        private int lastHandle;
    }
}
