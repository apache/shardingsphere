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

package org.apache.shardingsphere.database.protocol.firebird.constant.protocol;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Firebird connection protocol version cache.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdConnectionProtocolVersion {
    
    private static final FirebirdConnectionProtocolVersion INSTANCE = new FirebirdConnectionProtocolVersion();
    
    private final Map<Integer, FirebirdProtocolVersion> protocolVersionCache = new ConcurrentHashMap<>();
    
    /**
     * Get prepared statement registry instance.
     *
     * @return prepared statement registry instance
     */
    public static FirebirdConnectionProtocolVersion getInstance() {
        return INSTANCE;
    }
    
    /**
     * Set Firebird protocol version for connection.
     *
     * @param connectionId connection ID
     * @param protocolVersion Firebird protocol version to set
     */
    public void setProtocolVersion(final int connectionId, final FirebirdProtocolVersion protocolVersion) {
        protocolVersionCache.put(connectionId, protocolVersion);
    }
    
    /**
     * Get the current Firebird protocol version for connection.
     *
     * @param connectionId connection ID
     * @return Firebird protocol version
     */
    public FirebirdProtocolVersion getProtocolVersion(final int connectionId) {
        return protocolVersionCache.get(connectionId);
    }
    
    /**
     * Unset protocol version for connection.
     *
     * @param connectionId connection ID
     */
    public void unsetProtocolVersion(final int connectionId) {
        protocolVersionCache.remove(connectionId);
    }
}
