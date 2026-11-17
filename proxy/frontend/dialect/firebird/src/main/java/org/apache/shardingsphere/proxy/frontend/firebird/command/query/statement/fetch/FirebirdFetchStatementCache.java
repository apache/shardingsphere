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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Statement ID generator for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdFetchStatementCache {
    
    private static final FirebirdFetchStatementCache INSTANCE = new FirebirdFetchStatementCache();
    
    private final Map<Integer, Map<Integer, Statement>> statementRegistry = new ConcurrentHashMap<>();
    
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
     * @param connectionSession transaction session
     */
    public void registerStatement(final int connectionId, final int statementId, final ProxyBackendHandler proxyBackendHandler, final ConnectionSession connectionSession) {
        Map<Integer, Statement> statements = statementRegistry.get(connectionId);
        if (null != statements) {
            statements.put(statementId, new Statement(proxyBackendHandler, connectionSession));
        }
    }
    
    /**
     * Get fetch response packets for statement ID.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @return fetch response packets
     */
    public ProxyBackendHandler getFetchBackendHandler(final int connectionId, final int statementId) {
        Map<Integer, Statement> statements = statementRegistry.get(connectionId);
        Statement statement = null == statements ? null : statements.get(statementId);
        return null == statement ? null : statement.getBackendHandler();
    }
    
    /**
     * Get the transaction session that owns a cursor.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @return transaction session, or null when the cursor is absent
     */
    @HighFrequencyInvocation
    public ConnectionSession getConnectionSession(final int connectionId, final int statementId) {
        Map<Integer, Statement> statements = statementRegistry.get(connectionId);
        Statement statement = null == statements ? null : statements.get(statementId);
        return null == statement ? null : statement.getConnectionSession();
    }
    
    /**
     * Unregister cursors owned by a completed transaction.
     *
     * @param connectionId connection ID
     * @param connectionSession transaction session
     */
    @HighFrequencyInvocation
    public void unregisterTransaction(final int connectionId, final ConnectionSession connectionSession) {
        Map<Integer, Statement> statements = statementRegistry.get(connectionId);
        if (null != statements) {
            statements.values().removeIf(each -> each.getConnectionSession() == connectionSession);
        }
    }
    
    /**
     * Unregister statement.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     */
    public void unregisterStatement(final int connectionId, final int statementId) {
        Map<Integer, Statement> statements = statementRegistry.get(connectionId);
        if (null != statements) {
            statements.remove(statementId);
        }
    }
    
    /**
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    @SneakyThrows(SQLException.class)
    public void unregisterConnection(final int connectionId) {
        Map<Integer, Statement> statements = statementRegistry.remove(connectionId);
        if (null == statements) {
            return;
        }
        for (Statement each : statements.values()) {
            each.getBackendHandler().close();
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class Statement {
        
        private final ProxyBackendHandler backendHandler;
        
        private final ConnectionSession connectionSession;
    }
}
