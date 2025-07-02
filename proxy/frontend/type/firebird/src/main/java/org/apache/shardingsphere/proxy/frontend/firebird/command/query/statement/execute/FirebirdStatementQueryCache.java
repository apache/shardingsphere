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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.execute;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.db.protocol.firebird.packet.generic.FirebirdFetchResponsePacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Firebird proxy backend handler cache for statements.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdStatementQueryCache {
    
    private static final FirebirdStatementQueryCache INSTANCE = new FirebirdStatementQueryCache();
    
    private final Map<Integer, Map<Integer, List<FirebirdFetchResponsePacket>>> queryStatementsCache = new ConcurrentHashMap<>();
    
    /**
     * Get proxy backend handler statement cache instance.
     *
     * @return proxy backend handler statement cache instance
     */
    public static FirebirdStatementQueryCache getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register separate cache for connection.
     *
     * @param connectionId connection ID
     */
    public void registerConnection(final int connectionId) {
        queryStatementsCache.put(connectionId, new ConcurrentHashMap<>());
    }
    
    /**
     * Register statement for connection.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     */
    public void registerStatement(final int connectionId, final int statementId) {
        queryStatementsCache.get(connectionId).put(statementId, new ArrayList<>());
    }
    
    /**
     * Add Firebird fetch packet to cache and binds it to statement.
     *
     * @param statementId statement ID
     * @param connectionId connection ID
     * @param packet Firebird fetch packet
     */
    public void add(final int connectionId, final int statementId, final FirebirdFetchResponsePacket packet) {
        queryStatementsCache.get(connectionId).get(statementId).add(packet);
    }
    
    /**
     * Get Firebird fetch packet for statement.
     *
     * @param statementId statement ID
     * @param connectionId connection ID
     * @return Firebird fetch statement packet for statement
     */
    public List<FirebirdFetchResponsePacket> get(final int connectionId, final int statementId) {
        return queryStatementsCache.get(connectionId).get(statementId);
    }
    
    /**
     * Clear statement cache for connection.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     */
    public void clearStatement(final int connectionId, final int statementId) {
        queryStatementsCache.get(connectionId).get(statementId).clear();
    }
    
    /**
     * Unregister statement for connection.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     */
    public void unregisterStatement(final int connectionId, final int statementId) {
        queryStatementsCache.get(connectionId).remove(statementId);
    }
    
    /**
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    public void unregisterConnection(final int connectionId) {
        queryStatementsCache.remove(connectionId);
    }
}
