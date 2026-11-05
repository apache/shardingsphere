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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.PreparedStatementCacheKey;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Backend sessions belonging to Firebird transactions.
 */
@HighFrequencyInvocation
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdTransactionSessionRegistry {
    
    private static final FirebirdTransactionSessionRegistry INSTANCE = new FirebirdTransactionSessionRegistry();
    
    private final Map<Integer, Transactions> connections = new ConcurrentHashMap<>();
    
    /**
     * Get the transaction session registry.
     *
     * @return registry instance
     */
    public static FirebirdTransactionSessionRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register a transaction and return its backend session.
     *
     * @param parent connection session
     * @param handle transaction handle
     * @return transaction session
     */
    public ConnectionSession register(final ConnectionSession parent, final int handle) {
        int connectionId = parent.getConnectionId();
        while (true) {
            Transactions transactions = connections.computeIfAbsent(connectionId, ignored -> new Transactions(parent));
            synchronized (transactions) {
                if (connections.get(connectionId) != transactions) {
                    continue;
                }
                ConnectionSession result = transactions.sessions.isEmpty() ? parent : newSession(parent);
                transactions.sessions.put(handle & 0xFFFF, result);
                return result;
            }
        }
    }
    
    private ConnectionSession newSession(final ConnectionSession parent) {
        ConnectionSession result = new ConnectionSession(parent.getProtocolType(), parent.getAttributeMap());
        result.setConnectionId(parent.getConnectionId());
        result.setGrantee(parent.getConnectionContext().getGrantee());
        result.setCurrentDatabaseName(parent.getCurrentDatabaseName());
        result.setProcessId(parent.getProcessId());
        result.setConnectionAttributes(parent.getConnectionAttributes());
        return result;
    }
    
    /**
     * Get the backend session for a transaction.
     *
     * @param parent connection session
     * @param handle transaction handle
     * @return transaction session or null
     */
    public ConnectionSession get(final ConnectionSession parent, final int handle) {
        Transactions transactions = connections.get(parent.getConnectionId());
        return null == transactions ? null : transactions.sessions.get(handle & 0xFFFF);
    }
    
    /**
     * Remove a transaction and close its independent session.
     *
     * @param parent connection session
     * @param handle transaction handle
     * @throws SQLException if resources cannot be closed
     */
    public void unregister(final ConnectionSession parent, final int handle) throws SQLException {
        int connectionId = parent.getConnectionId();
        Transactions transactions = connections.get(connectionId);
        if (null == transactions) {
            return;
        }
        ConnectionSession removed;
        synchronized (transactions) {
            if (connections.get(connectionId) != transactions) {
                return;
            }
            removed = transactions.sessions.remove(handle & 0xFFFF);
            if (transactions.sessions.isEmpty()) {
                connections.remove(connectionId, transactions);
            }
        }
        if (null != removed) {
            FirebirdBlobWriteCache.getInstance().unregisterTransaction(connectionId, handle & 0xFFFF);
            FirebirdBlobIdGenerator.getInstance().unregisterTransaction(connectionId, handle & 0xFFFF);
        }
        if (null != removed && removed != parent) {
            try {
                closeSession(removed);
            } finally {
                FirebirdFetchStatementCache.getInstance().unregisterOwner(connectionId, removed);
            }
        }
    }
    
    private void closeSession(final ConnectionSession session) throws SQLException {
        Collection<SQLException> exceptions = session.getDatabaseConnectionManager().closeAllResources();
        if (!exceptions.isEmpty()) {
            SQLException result = new SQLException("Could not close transaction resources");
            for (SQLException each : exceptions) {
                result.setNextException(each);
            }
            throw result;
        }
    }
    
    /**
     * Invalidate a prepared statement in every transaction session.
     *
     * @param parent parent connection session
     * @param cacheKey prepared statement cache key
     */
    public void invalidatePreparedStatementCache(final ConnectionSession parent, final PreparedStatementCacheKey cacheKey) {
        Transactions transactions = connections.get(parent.getConnectionId());
        if (null != transactions) {
            for (ConnectionSession each : transactions.sessions.values()) {
                if (each != parent) {
                    each.invalidatePreparedStatementCache(cacheKey);
                }
            }
        }
    }
    
    /**
     * Close all independent sessions when a connection is released.
     *
     * @param connectionId connection ID
     */
    @SneakyThrows(SQLException.class)
    void unregisterConnection(final int connectionId) {
        Transactions transactions = connections.get(connectionId);
        if (null == transactions) {
            return;
        }
        Collection<ConnectionSession> toClose;
        synchronized (transactions) {
            if (!connections.remove(connectionId, transactions)) {
                return;
            }
            toClose = new ArrayList<>(transactions.sessions.values());
            transactions.sessions.clear();
        }
        SQLException failure = null;
        for (ConnectionSession each : toClose) {
            if (each == transactions.parent) {
                continue;
            }
            try {
                closeSession(each);
            } catch (final SQLException ex) {
                if (null == failure) {
                    failure = ex;
                } else {
                    failure.setNextException(ex);
                }
            } finally {
                FirebirdFetchStatementCache.getInstance().unregisterOwner(connectionId, each);
            }
        }
        if (null != failure) {
            throw failure;
        }
    }
    
    /**
     * Close resources used by a transaction command.
     *
     * @param session transaction session
     * @throws SQLException if resources cannot be closed
     */
    public void closeExecutionResources(final ConnectionSession session) throws SQLException {
        try {
            session.getDatabaseConnectionManager().closeExecutionResources();
        } catch (final BackendConnectionException ex) {
            throw new SQLException(ex);
        }
    }
    
    private static final class Transactions {
        
        private final ConnectionSession parent;
        
        private final Map<Integer, ConnectionSession> sessions = new ConcurrentHashMap<>();
        
        @HighFrequencyInvocation
        private Transactions(final ConnectionSession parent) {
            this.parent = parent;
        }
    }
}
