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
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.TransactionUtils;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;
import org.apache.shardingsphere.proxy.frontend.firebird.resource.FirebirdObjectHandleGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.resource.FirebirdTransactionConnection;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.transaction.api.TransactionType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Transaction ID generator for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdTransactionIdGenerator {
    
    private static final FirebirdTransactionIdGenerator INSTANCE = new FirebirdTransactionIdGenerator();
    
    private final Map<Integer, AtomicInteger> connectionRegistry = new ConcurrentHashMap<>();
    
    private final Map<Integer, Map<Integer, ConnectionSession>> activeTransactionRegistry = new ConcurrentHashMap<>();
    
    private final Map<Integer, ConnectionSession> attachmentSessionRegistry = new ConcurrentHashMap<>();
    
    /**
     * Get prepared transaction registry instance.
     *
     * @return prepared statement registry instance
     */
    public static FirebirdTransactionIdGenerator getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection.
     *
     * @param connectionId connection ID
     */
    public void registerConnection(final int connectionId) {
        connectionRegistry.put(connectionId, new AtomicInteger());
        activeTransactionRegistry.put(connectionId, new ConcurrentHashMap<>());
        FirebirdObjectHandleGenerator.getInstance().registerConnection(connectionId, FirebirdCommandPacketType.TRANSACTION);
    }
    
    /**
     * Register an independent transaction session and generate its handle.
     *
     * @param transactionSession session owned and released by this registry; it must not be shared with another transaction
     * @return generated transaction ID
     */
    @HighFrequencyInvocation
    public int nextTransactionId(final ConnectionSession transactionSession) {
        int connectionId = transactionSession.getConnectionId();
        AtomicInteger counter = getTransactionCounter(connectionId);
        int result = FirebirdObjectHandleGenerator.getInstance().nextHandle(connectionId, FirebirdCommandPacketType.TRANSACTION, counter.get() + 1);
        counter.set(result);
        getActiveTransactions(connectionId).put(result, transactionSession);
        return result;
    }
    
    private AtomicInteger getTransactionCounter(final int connectionId) {
        AtomicInteger result = connectionRegistry.get(connectionId);
        ShardingSpherePreconditions.checkNotNull(result, () -> new IllegalStateException(new StringBuilder("No transaction ID generator found for connectionId: ").append(connectionId).toString()));
        return result;
    }
    
    private Map<Integer, ConnectionSession> getActiveTransactions(final int connectionId) {
        Map<Integer, ConnectionSession> result = activeTransactionRegistry.get(connectionId);
        ShardingSpherePreconditions.checkNotNull(result, () -> new IllegalStateException(new StringBuilder("No transaction ID generator found for connectionId: ").append(connectionId).toString()));
        return result;
    }
    
    /**
     * Start an independent transaction.
     *
     * @param connectionSession client connection session
     * @param autoCommit whether to commit automatically
     * @param readOnly whether the transaction is read only
     * @param isolationLevel isolation level
     * @return generated transaction ID
     */
    @HighFrequencyInvocation
    @SneakyThrows(SQLException.class)
    public int startTransaction(final ConnectionSession connectionSession, final boolean autoCommit, final boolean readOnly, final TransactionIsolationLevel isolationLevel) {
        synchronized (connectionSession.getDatabaseConnectionManager().getCloseLock()) {
            checkAttachmentOpen(connectionSession);
            return doStartTransaction(connectionSession, autoCommit, readOnly, isolationLevel);
        }
    }
    
    private void checkAttachmentOpen(final ConnectionSession connectionSession) throws SQLException {
        ShardingSpherePreconditions.checkState(!connectionSession.getDatabaseConnectionManager().getClosed().get(), () -> new SQLException("Firebird attachment is closed", "08003"));
    }
    
    private int doStartTransaction(final ConnectionSession connectionSession, final boolean autoCommit, final boolean readOnly, final TransactionIsolationLevel isolationLevel) throws SQLException {
        ConnectionSession transactionSession = new ConnectionSession(connectionSession.getProtocolType(), connectionSession.getAttributeMap());
        transactionSession.setConnectionId(connectionSession.getConnectionId());
        transactionSession.setGrantee(connectionSession.getConnectionContext().getGrantee());
        transactionSession.setCurrentDatabaseName(connectionSession.getCurrentDatabaseName());
        transactionSession.setProcessId(connectionSession.getProcessId());
        transactionSession.setConnectionAttributes(connectionSession.getConnectionAttributes());
        transactionSession.setAutoCommit(autoCommit);
        transactionSession.setReadOnly(readOnly);
        transactionSession.setIsolationLevel(isolationLevel);
        int result = nextTransactionId(transactionSession);
        boolean started = false;
        try {
            if (!autoCommit) {
                new ProxyBackendTransactionManager(transactionSession.getDatabaseConnectionManager()).begin();
                if (TransactionType.LOCAL == TransactionUtils.getTransactionType(transactionSession.getConnectionContext().getTransactionContext())) {
                    createTransactionConnections(connectionSession, transactionSession, result);
                }
            }
            started = true;
            return result;
        } finally {
            if (!started) {
                closeTransaction(connectionSession.getConnectionId(), result);
            }
        }
    }
    
    private void createTransactionConnections(final ConnectionSession connectionSession, final ConnectionSession transactionSession, final int transactionId) throws SQLException {
        attachmentSessionRegistry.put(connectionSession.getConnectionId(), connectionSession);
        connectionSession.getTransactionStatus().setInTransaction(true);
        connectionSession.getConnectionContext().getTransactionContext().beginTransaction(TransactionType.LOCAL.name(), null);
        transactionSession.getDatabaseConnectionManager()
                .setConnectionProvider((databaseName, dataSourceName, connectionOffset, connectionSize, connectionMode) -> createRoutedTransactionConnections(connectionSession, transactionSession,
                        transactionId, databaseName, dataSourceName, connectionOffset, connectionSize, connectionMode));
        String databaseName = connectionSession.getCurrentDatabaseName();
        Collection<String> dataSourceNames = ProxyContext.getInstance().getContextManager().getDatabase(databaseName).getResourceMetaData().getStorageUnits().keySet();
        if (1 == dataSourceNames.size()) {
            transactionSession.getDatabaseConnectionManager().getConnections(databaseName, dataSourceNames.iterator().next(), 0, 1, ConnectionMode.MEMORY_STRICTLY);
        } else {
            createSnapshotTransactions(connectionSession, getCachedAttachmentConnections(connectionSession));
        }
    }
    
    private List<Connection> createRoutedTransactionConnections(final ConnectionSession connectionSession, final ConnectionSession transactionSession, final int transactionId,
                                                                final String databaseName, final String dataSourceName, final int connectionOffset, final int connectionSize,
                                                                final ConnectionMode connectionMode) throws SQLException {
        synchronized (connectionSession.getDatabaseConnectionManager().getCloseLock()) {
            checkAttachmentOpen(connectionSession);
            List<Connection> attachments = getAttachmentConnections(connectionSession, databaseName, dataSourceName, connectionOffset, connectionSize, connectionMode);
            createSnapshotTransactions(connectionSession, attachments);
            List<Connection> result = new ArrayList<>(connectionSize);
            for (Connection each : attachments) {
                result.add(FirebirdTransactionConnection.createTransaction(each, transactionId, transactionSession.isReadOnly(), transactionSession.getIsolationLevel().get()));
            }
            return result;
        }
    }
    
    private List<Connection> getAttachmentConnections(final ConnectionSession connectionSession, final String databaseName, final String dataSourceName,
                                                      final int connectionOffset, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        connectionSession.getDatabaseConnectionManager().getConnections(databaseName, dataSourceName, connectionOffset, connectionSize, connectionMode);
        String key = new StringBuilder(databaseName.toLowerCase()).append('.').append(dataSourceName).toString();
        Collection<Connection> connections = connectionSession.getDatabaseConnectionManager().getCachedConnections().get(key);
        return wrapAttachmentConnections(connections).subList(connectionOffset, connectionOffset + connectionSize);
    }
    
    private List<Connection> wrapAttachmentConnections(final Collection<Connection> connections) throws SQLException {
        List<Connection> result = new ArrayList<>(connections.size());
        for (Connection each : connections) {
            result.add(FirebirdTransactionConnection.createAttachment(each));
        }
        connections.clear();
        connections.addAll(result);
        return result;
    }
    
    private void createSnapshotTransactions(final ConnectionSession connectionSession, final Collection<Connection> attachments) throws SQLException {
        for (Entry<Integer, ConnectionSession> entry : getActiveTransactions(connectionSession.getConnectionId()).entrySet()) {
            if (isSnapshotTransaction(entry.getValue())) {
                createSnapshotTransaction(attachments, entry.getKey(), entry.getValue());
            }
        }
    }
    
    private boolean isSnapshotTransaction(final ConnectionSession transactionSession) {
        if (transactionSession.isAutoCommit() || !transactionSession.getTransactionStatus().isInTransaction()
                || TransactionType.LOCAL != TransactionUtils.getTransactionType(transactionSession.getConnectionContext().getTransactionContext())) {
            return false;
        }
        return TransactionIsolationLevel.REPEATABLE_READ == transactionSession.getIsolationLevel().get() || TransactionIsolationLevel.SERIALIZABLE == transactionSession.getIsolationLevel().get();
    }
    
    private void createSnapshotTransaction(final Collection<Connection> attachments, final int transactionId, final ConnectionSession transactionSession) throws SQLException {
        for (Connection each : attachments) {
            FirebirdTransactionConnection.createTransaction(each, transactionId, transactionSession.isReadOnly(), transactionSession.getIsolationLevel().get());
        }
    }
    
    private Collection<Connection> getCachedAttachmentConnections(final ConnectionSession connectionSession) throws SQLException {
        Collection<Connection> result = new ArrayList<>(connectionSession.getDatabaseConnectionManager().getConnectionSize());
        for (String each : new ArrayList<>(connectionSession.getDatabaseConnectionManager().getCachedConnections().keySet())) {
            result.addAll(wrapAttachmentConnections(connectionSession.getDatabaseConnectionManager().getCachedConnections().get(each)));
        }
        return result;
    }
    
    /**
     * Get transaction session.
     *
     * @param connectionId connection ID
     * @param transactionId transaction ID
     * @return transaction session
     */
    @HighFrequencyInvocation
    public ConnectionSession getTransactionSession(final int connectionId, final int transactionId) {
        ConnectionSession result = getActiveTransactions(connectionId).get(transactionId);
        ShardingSpherePreconditions.checkNotNull(result, () -> new InvalidTransactionHandleException(transactionId));
        return result;
    }
    
    /**
     * Get transaction session for SQL execution.
     *
     * @param connectionId connection ID
     * @param transactionId transaction ID
     * @return transaction session
     */
    @HighFrequencyInvocation
    @SneakyThrows({SQLException.class, BackendConnectionException.class})
    public ConnectionSession getTransactionSessionForExecution(final int connectionId, final int transactionId) {
        ConnectionSession result = getTransactionSession(connectionId, transactionId);
        ConnectionSession attachmentSession = attachmentSessionRegistry.get(connectionId);
        if (null == attachmentSession) {
            return result;
        }
        synchronized (attachmentSession.getDatabaseConnectionManager().getCloseLock()) {
            checkAttachmentOpen(attachmentSession);
            if (!result.isAutoCommit() && !result.getTransactionStatus().isInTransaction()) {
                resumeTransaction(attachmentSession, result, transactionId);
            }
        }
        return result;
    }
    
    private void resumeTransaction(final ConnectionSession attachmentSession, final ConnectionSession transactionSession, final int transactionId) throws SQLException, BackendConnectionException {
        int connectionId = attachmentSession.getConnectionId();
        boolean resumed = false;
        try {
            FirebirdFetchStatementCache.getInstance().unregisterTransaction(connectionId, transactionSession);
            transactionSession.getDatabaseConnectionManager().closeExecutionResources();
            Collection<Exception> exceptions = new LinkedList<>(closeTransactionConnections(connectionId, transactionId));
            ShardingSpherePreconditions.checkMustEmpty(exceptions, () -> new BackendConnectionException(exceptions));
            new ProxyBackendTransactionManager(transactionSession.getDatabaseConnectionManager()).begin();
            if (TransactionType.LOCAL == TransactionUtils.getTransactionType(transactionSession.getConnectionContext().getTransactionContext())) {
                createTransactionConnections(attachmentSession, transactionSession, transactionId);
            }
            resumed = true;
        } finally {
            if (!resumed) {
                closeTransactionResources(connectionId, transactionId, false);
            }
        }
    }
    
    private Collection<SQLException> closeTransactionConnections(final int connectionId, final int transactionId) {
        Collection<SQLException> result = new LinkedList<>();
        ConnectionSession attachmentSession = attachmentSessionRegistry.get(connectionId);
        if (null == attachmentSession) {
            return result;
        }
        synchronized (attachmentSession.getDatabaseConnectionManager().getCloseLock()) {
            for (Connection each : attachmentSession.getDatabaseConnectionManager().getCachedConnections().values()) {
                try {
                    FirebirdTransactionConnection.closeTransaction(each, transactionId);
                } catch (final SQLException ex) {
                    result.add(ex);
                }
            }
        }
        return result;
    }
    
    private void closeTransactionResources(final int connectionId, final int transactionId, final boolean releaseHandle) throws BackendConnectionException {
        Map<Integer, ConnectionSession> transactions = getActiveTransactions(connectionId);
        ConnectionSession transactionSession = transactions.remove(transactionId);
        if (null != transactionSession) {
            try {
                FirebirdFetchStatementCache.getInstance().unregisterTransaction(connectionId, transactionSession);
                Collection<Exception> exceptions = new LinkedList<>(transactionSession.getDatabaseConnectionManager().closeAllResources());
                exceptions.addAll(closeTransactionConnections(connectionId, transactionId));
                ShardingSpherePreconditions.checkMustEmpty(exceptions, () -> new BackendConnectionException(exceptions));
                if (releaseHandle) {
                    FirebirdObjectHandleGenerator.getInstance().releaseHandle(connectionId, FirebirdCommandPacketType.TRANSACTION, transactionId);
                }
            } finally {
                if (transactions.isEmpty()) {
                    releaseAttachmentSession(connectionId);
                }
            }
        }
    }
    
    private void releaseAttachmentSession(final int connectionId) {
        ConnectionSession attachmentSession = attachmentSessionRegistry.remove(connectionId);
        if (null != attachmentSession) {
            attachmentSession.getTransactionStatus().setInTransaction(false);
            attachmentSession.getConnectionContext().close();
        }
    }
    
    /**
     * Get active transaction sessions.
     *
     * @param connectionId connection ID
     * @return active transaction sessions
     */
    @HighFrequencyInvocation
    public Collection<ConnectionSession> getTransactionSessions(final int connectionId) {
        return getActiveTransactions(connectionId).values();
    }
    
    /**
     * Judge whether transaction is active for connection.
     *
     * @param connectionId connection ID
     * @param transactionId transaction ID
     * @return is transaction active or not
     */
    public boolean isTransactionActive(final int connectionId, final int transactionId) {
        return getActiveTransactions(connectionId).containsKey(transactionId);
    }
    
    /**
     * Judge whether any transaction is active for connection.
     *
     * @param connectionId connection ID
     * @return has active transaction or not
     */
    public boolean hasActiveTransaction(final int connectionId) {
        return !getActiveTransactions(connectionId).isEmpty();
    }
    
    /**
     * Close transaction for connection.
     *
     * @param connectionId connection ID
     * @param transactionId transaction ID
     */
    @HighFrequencyInvocation
    @SneakyThrows(BackendConnectionException.class)
    public void closeTransaction(final int connectionId, final int transactionId) {
        closeTransactionResources(connectionId, transactionId, true);
    }
    
    /**
     * Get current transaction ID for connection.
     *
     * @param connectionId connection ID
     * @return transaction ID
     */
    public int getTransactionId(final int connectionId) {
        return getTransactionCounter(connectionId).get();
    }
    
    /**
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    @SneakyThrows(BackendConnectionException.class)
    public void unregisterConnection(final int connectionId) {
        connectionRegistry.remove(connectionId);
        Map<Integer, ConnectionSession> transactions = activeTransactionRegistry.remove(connectionId);
        Collection<Exception> exceptions = new LinkedList<>();
        if (null != transactions) {
            for (Entry<Integer, ConnectionSession> entry : transactions.entrySet()) {
                exceptions.addAll(entry.getValue().getDatabaseConnectionManager().closeAllResources());
                exceptions.addAll(closeTransactionConnections(connectionId, entry.getKey()));
            }
        }
        FirebirdObjectHandleGenerator.getInstance().unregisterConnection(connectionId, FirebirdCommandPacketType.TRANSACTION);
        releaseAttachmentSession(connectionId);
        ShardingSpherePreconditions.checkMustEmpty(exceptions, () -> new BackendConnectionException(exceptions));
    }
}
