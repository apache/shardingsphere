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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Statement ID generator for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdFetchStatementCache {
    
    private static final FirebirdFetchStatementCache INSTANCE = new FirebirdFetchStatementCache();
    
    private final Map<Integer, Map<Integer, ProxyBackendHandler>> statementRegistry = new ConcurrentHashMap<>();
    
    /**
     * Get fetch statement registry instance.
     *
     * @return fetch statement registry instance
     */
    public static FirebirdFetchStatementCache getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection.
     *
     * @param connectionId connection ID
     */
    public void registerConnection(final int connectionId) {
        statementRegistry.put(connectionId, new LinkedHashMap<>());
    }
    
    /**
     * Register statement.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @param proxyBackendHandler proxy backend handler
     */
    public void registerStatement(final int connectionId, final int statementId, final ProxyBackendHandler proxyBackendHandler) {
        statementRegistry.get(connectionId).put(statementId, proxyBackendHandler);
    }
    
    /**
     * Get fetch response packets for statement ID.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @return fetch response packets
     */
    public ProxyBackendHandler getFetchBackendHandler(final int connectionId, final int statementId) {
        return statementRegistry.get(connectionId).get(statementId);
    }
    
    /**
     * Unregister statement.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     */
    public void unregisterStatement(final int connectionId, final int statementId) {
        statementRegistry.get(connectionId).remove(statementId);
    }
    
    /**
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    public void unregisterConnection(final int connectionId) {
        statementRegistry.remove(connectionId);
    }
}
