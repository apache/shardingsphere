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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Last allocated object registry for Firebird.
 *
 * <p>On a connection that accepted the lazy send protocol type, a client can send the invalid object handle {@code 0xFFFF}
 * instead of the handle of the object allocated last on the connection, which it has not received yet. The Firebird server
 * replaces it with the handle of that object, so it resolves to a statement only when the object allocated last is a statement
 * rather than, for example, a transaction or a BLOB.</p>
 *
 * @see <a href="https://github.com/FirebirdSQL/firebird/blob/v5.0.4/src/remote/remote.h#L1494-L1506">Firebird remote port getHandle</a>
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdLastObjectRegistry {
    
    private static final FirebirdLastObjectRegistry INSTANCE = new FirebirdLastObjectRegistry();
    
    private static final int INVALID_OBJECT_HANDLE = 0xFFFF;
    
    private final Set<Integer> lazySendConnections = ConcurrentHashMap.newKeySet();
    
    private final Map<Integer, Integer> lastStatementHandles = new ConcurrentHashMap<>();
    
    /**
     * Get last allocated object registry instance.
     *
     * @return last allocated object registry instance
     */
    public static FirebirdLastObjectRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection which accepted the lazy send protocol type.
     *
     * @param connectionId connection ID
     */
    public void registerLazySendConnection(final int connectionId) {
        lazySendConnections.add(connectionId);
    }
    
    /**
     * Set statement as the object allocated last on connection.
     *
     * @param connectionId connection ID
     * @param statementHandle statement handle
     */
    public void setLastStatement(final int connectionId, final int statementHandle) {
        lastStatementHandles.put(connectionId, statementHandle);
    }
    
    /**
     * Set an object other than a statement, such as a transaction or a BLOB, as the object allocated last on connection.
     *
     * @param connectionId connection ID
     */
    public void setLastNonStatementObject(final int connectionId) {
        lastStatementHandles.remove(connectionId);
    }
    
    /**
     * Resolve statement handle, replacing the invalid object handle with the statement allocated last on a lazy send connection.
     *
     * @param connectionId connection ID
     * @param statementHandle statement handle received from the client
     * @return resolved statement handle, or the received handle when it is not replaced
     */
    public int resolveStatementHandle(final int connectionId, final int statementHandle) {
        if (INVALID_OBJECT_HANDLE != statementHandle || !lazySendConnections.contains(connectionId)) {
            return statementHandle;
        }
        return lastStatementHandles.getOrDefault(connectionId, statementHandle);
    }
    
    /**
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    public void unregisterConnection(final int connectionId) {
        lazySendConnections.remove(connectionId);
        lastStatementHandles.remove(connectionId);
    }
}
