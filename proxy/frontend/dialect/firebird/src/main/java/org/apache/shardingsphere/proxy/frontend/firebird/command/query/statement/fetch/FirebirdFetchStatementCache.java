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
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Statement ID generator for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdFetchStatementCache {
    
    private static final FirebirdFetchStatementCache INSTANCE = new FirebirdFetchStatementCache();
    
    private final Map<Integer, Map<Integer, ProxyBackendHandler>> statementRegistry = new ConcurrentHashMap<>();
    
    private final Map<Integer, Map<Integer, ConnectionSession>> owners = new ConcurrentHashMap<>();
    
    private final Map<Integer, Map<Integer, List<FirebirdBinaryColumnType>>> columnTypes = new ConcurrentHashMap<>();
    
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
    @HighFrequencyInvocation
    public void registerConnection(final int connectionId) {
        statementRegistry.put(connectionId, new LinkedHashMap<>());
        owners.remove(connectionId);
        columnTypes.remove(connectionId);
    }
    
    /**
     * Register statement.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @param proxyBackendHandler proxy backend handler
     */
    public void registerStatement(final int connectionId, final int statementId, final ProxyBackendHandler proxyBackendHandler) {
        Map<Integer, ProxyBackendHandler> statements = statementRegistry.get(connectionId);
        if (null != statements) {
            statements.put(statementId, proxyBackendHandler);
        }
        Map<Integer, List<FirebirdBinaryColumnType>> connectionColumnTypes = columnTypes.get(connectionId);
        if (null != connectionColumnTypes) {
            connectionColumnTypes.remove(statementId);
        }
    }
    
    /**
     * Associate a statement with its transaction session.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @param owner transaction session
     */
    @HighFrequencyInvocation
    public void registerOwner(final int connectionId, final int statementId, final ConnectionSession owner) {
        if (statementRegistry.containsKey(connectionId)) {
            owners.computeIfAbsent(connectionId, ignored -> new LinkedHashMap<>()).put(statementId, owner);
        }
    }
    
    /**
     * Get the session that owns a statement's active cursor.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @return transaction session or null
     */
    @HighFrequencyInvocation
    public ConnectionSession getOwner(final int connectionId, final int statementId) {
        Map<Integer, ConnectionSession> connectionOwners = owners.get(connectionId);
        return null == connectionOwners ? null : connectionOwners.get(statementId);
    }
    
    /**
     * Register column types from the client message BLR.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @param types column types from the client message BLR
     */
    @HighFrequencyInvocation
    void registerColumnTypes(final int connectionId, final int statementId, final List<FirebirdBinaryColumnType> types) {
        columnTypes.computeIfAbsent(connectionId, ignored -> new LinkedHashMap<>()).put(statementId, types);
    }
    
    /**
     * Get column types from the client message BLR.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @return column types from the client message BLR, or an empty list when the client did not send it
     */
    @HighFrequencyInvocation
    List<FirebirdBinaryColumnType> getColumnTypes(final int connectionId, final int statementId) {
        Map<Integer, List<FirebirdBinaryColumnType>> connectionColumnTypes = columnTypes.get(connectionId);
        return null == connectionColumnTypes ? Collections.emptyList() : connectionColumnTypes.getOrDefault(statementId, Collections.emptyList());
    }
    
    /**
     * Get fetch response packets for statement ID.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @return fetch response packets
     */
    public ProxyBackendHandler getFetchBackendHandler(final int connectionId, final int statementId) {
        Map<Integer, ProxyBackendHandler> statements = statementRegistry.get(connectionId);
        return null == statements ? null : statements.get(statementId);
    }
    
    /**
     * Unregister statement.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     */
    @HighFrequencyInvocation
    public void unregisterStatement(final int connectionId, final int statementId) {
        Map<Integer, ProxyBackendHandler> statements = statementRegistry.get(connectionId);
        if (null != statements) {
            statements.remove(statementId);
        }
        Map<Integer, ConnectionSession> connectionOwners = owners.get(connectionId);
        if (null != connectionOwners) {
            connectionOwners.remove(statementId);
        }
        Map<Integer, List<FirebirdBinaryColumnType>> connectionColumnTypes = columnTypes.get(connectionId);
        if (null != connectionColumnTypes) {
            connectionColumnTypes.remove(statementId);
        }
    }
    
    /**
     * Forget cursors belonging to a closed transaction session.
     *
     * @param connectionId connection ID
     * @param owner transaction session
     */
    @HighFrequencyInvocation
    public void unregisterOwner(final int connectionId, final ConnectionSession owner) {
        Map<Integer, ConnectionSession> connectionOwners = owners.get(connectionId);
        Map<Integer, ProxyBackendHandler> statements = statementRegistry.get(connectionId);
        Map<Integer, List<FirebirdBinaryColumnType>> connectionColumnTypes = columnTypes.get(connectionId);
        if (null != connectionOwners && null != statements) {
            connectionOwners.entrySet().removeIf(entry -> {
                if (entry.getValue() != owner) {
                    return false;
                }
                statements.remove(entry.getKey());
                if (null != connectionColumnTypes) {
                    connectionColumnTypes.remove(entry.getKey());
                }
                return true;
            });
        }
    }
    
    /**
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    @SneakyThrows(SQLException.class)
    @HighFrequencyInvocation
    public void unregisterConnection(final int connectionId) {
        owners.remove(connectionId);
        columnTypes.remove(connectionId);
        Map<Integer, ProxyBackendHandler> statements = statementRegistry.remove(connectionId);
        if (null == statements) {
            return;
        }
        for (ProxyBackendHandler each : statements.values()) {
            each.close();
        }
    }
}
