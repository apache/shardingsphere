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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.connection;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.db.protocol.parameter.TypeUnspecifiedSQLParameter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.ExecutorJDBCManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.federation.executor.FederationExecutor;
import org.apache.shardingsphere.proxy.backend.communication.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.StatementMemoryStrictlyFetchSizeSetter;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.JDBCBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.TransactionUtil;
import org.apache.shardingsphere.spi.singleton.SingletonSPIRegistry;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDBC backend connection.
 */
@Getter
@Setter
public final class JDBCBackendConnection implements BackendConnection<Void>, ExecutorJDBCManager {
    
    private final ConnectionSession connectionSession;
    
    private volatile FederationExecutor federationExecutor;
    
    private final Multimap<String, Connection> cachedConnections = LinkedHashMultimap.create();
    
    private final Collection<JDBCDatabaseCommunicationEngine> databaseCommunicationEngines = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    
    private final Collection<JDBCDatabaseCommunicationEngine> inUseDatabaseCommunicationEngines = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    
    private final Collection<ConnectionPostProcessor<Connection>> connectionPostProcessors = new LinkedList<>();
    
    private final ResourceLock resourceLock = new ResourceLock();
    
    private final ConnectionStatus connectionStatus = new ConnectionStatus();
    
    private final Map<String, StatementMemoryStrictlyFetchSizeSetter> fetchSizeSetters;
    
    public JDBCBackendConnection(final ConnectionSession connectionSession) {
        this.connectionSession = connectionSession;
        fetchSizeSetters = SingletonSPIRegistry.getTypedSingletonInstancesMap(StatementMemoryStrictlyFetchSizeSetter.class);
    }
    
    @Override
    public List<Connection> getConnections(final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        Collection<Connection> connections;
        synchronized (cachedConnections) {
            connections = cachedConnections.get(dataSourceName);
        }
        List<Connection> result;
        if (connections.size() >= connectionSize) {
            result = new ArrayList<>(connections).subList(0, connectionSize);
        } else if (!connections.isEmpty()) {
            result = new ArrayList<>(connectionSize);
            result.addAll(connections);
            List<Connection> newConnections = createNewConnections(dataSourceName, connectionSize - connections.size(), connectionMode);
            result.addAll(newConnections);
            synchronized (cachedConnections) {
                cachedConnections.putAll(dataSourceName, newConnections);
            }
        } else {
            result = createNewConnections(dataSourceName, connectionSize, connectionMode);
            synchronized (cachedConnections) {
                cachedConnections.putAll(dataSourceName, result);
            }
        }
        return result;
    }
    
    private List<Connection> createNewConnections(final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        Preconditions.checkNotNull(connectionSession.getSchemaName(), "Current schema is null.");
        List<Connection> result = ProxyContext.getInstance().getBackendDataSource().getConnections(connectionSession.getSchemaName(), dataSourceName, connectionSize, connectionMode);
        for (Connection each : result) {
            replayTransactionOption(each);
        }
        if (connectionSession.getTransactionStatus().isInTransaction()) {
            for (Connection each : result) {
                replayMethodsInvocation(each);
            }
        }
        return result;
    }
    
    private void replayMethodsInvocation(final Connection target) {
        for (ConnectionPostProcessor<Connection> each : connectionPostProcessors) {
            each.process(target);
        }
    }

    private void replayTransactionOption(final Connection connection) throws SQLException {
        if (null == connection) {
            return;
        }
        if (connectionSession.isReadOnly()) {
            connection.setReadOnly(true);
        }
        if (null != connectionSession.getIsolationLevel()) {
            connection.setTransactionIsolation(TransactionUtil.getTransactionIsolationLevel(connectionSession.getIsolationLevel()));
        }
    }
    
    @Override
    public Statement createStorageResource(final Connection connection, final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        Statement result = connection.createStatement();
        if (ConnectionMode.MEMORY_STRICTLY == connectionMode) {
            setFetchSize(result);
        }
        return result;
    }
    
    @Override
    public PreparedStatement createStorageResource(final String sql, final List<Object> parameters, 
                                                   final Connection connection, final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        PreparedStatement result = option.isReturnGeneratedKeys()
                ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(sql);
        for (int i = 0; i < parameters.size(); i++) {
            Object parameter = parameters.get(i);
            if (parameter instanceof TypeUnspecifiedSQLParameter) {
                result.setObject(i + 1, parameter, Types.OTHER);
            } else {
                result.setObject(i + 1, parameter);
            }
        }
        if (ConnectionMode.MEMORY_STRICTLY == connectionMode) {
            setFetchSize(result);
        }
        return result;
    }
    
    private void setFetchSize(final Statement statement) throws SQLException {
        DatabaseType databaseType = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(connectionSession.getSchemaName()).getResource().getDatabaseType();
        if (fetchSizeSetters.containsKey(databaseType.getName())) {
            fetchSizeSetters.get(databaseType.getName()).setFetchSize(statement);
        }
    }
    
    /**
     * Whether execute SQL serial or not.
     *
     * @return true or false
     */
    public boolean isSerialExecute() {
        return connectionSession.getTransactionStatus().isInTransaction()
                && (TransactionType.LOCAL == connectionSession.getTransactionStatus().getTransactionType() || TransactionType.XA == connectionSession.getTransactionStatus().getTransactionType());
    }
    
    /**
     * Get connection size.
     *
     * @return connection size
     */
    public int getConnectionSize() {
        return cachedConnections.values().size();
    }
    
    /**
     * Add database communication engine.
     *
     * @param databaseCommunicationEngine database communication engine to be added
     */
    public void add(final JDBCDatabaseCommunicationEngine databaseCommunicationEngine) {
        databaseCommunicationEngines.add(databaseCommunicationEngine);
    }
    
    /**
     * Mark a database communication engine as in use.
     *
     * @param databaseCommunicationEngine database communication engine to be added
     */
    public void markResourceInUse(final JDBCDatabaseCommunicationEngine databaseCommunicationEngine) {
        inUseDatabaseCommunicationEngines.add(databaseCommunicationEngine);
    }
    
    /**
     * Unmark an in use database communication engine.
     *
     * @param databaseCommunicationEngine database communication engine to be added
     */
    public void unmarkResourceInUse(final JDBCDatabaseCommunicationEngine databaseCommunicationEngine) {
        inUseDatabaseCommunicationEngines.remove(databaseCommunicationEngine);
    }
    
    @Override
    public Void prepareForTaskExecution() throws BackendConnectionException {
        if (!connectionSession.getTransactionStatus().isInConnectionHeldTransaction()) {
            connectionStatus.waitUntilConnectionRelease();
            connectionStatus.switchToUsing();
        }
        if (!connectionSession.isAutoCommit() && !connectionSession.getTransactionStatus().isInTransaction()) {
            JDBCBackendTransactionManager transactionManager = new JDBCBackendTransactionManager(this);
            try {
                transactionManager.begin();
            } catch (SQLException ex) {
                throw new BackendConnectionException(ex);
            }
        }
        return null;
    }
    
    @Override
    public Void closeExecutionResources() throws BackendConnectionException {
        Collection<Exception> result = new LinkedList<>();
        result.addAll(closeDatabaseCommunicationEngines(false));
        result.addAll(closeFederationExecutor());
        if (!connectionSession.getTransactionStatus().isInConnectionHeldTransaction()) {
            result.addAll(closeDatabaseCommunicationEngines(true));
            result.addAll(closeConnections(false));
            connectionStatus.switchToReleased();
        }
        if (result.isEmpty()) {
            return null;
        }
        throw new BackendConnectionException(result);
    }
    
    @Override
    public Void closeAllResources() {
        closeDatabaseCommunicationEngines(true);
        closeConnections(true);
        closeFederationExecutor();
        return null;
    }
    
    /**
     * Close database communication engines.
     *
     * @param includeInUse include engines in use
     * @return SQL exception when engine close
     */
    public synchronized Collection<SQLException> closeDatabaseCommunicationEngines(final boolean includeInUse) {
        Collection<SQLException> result = new LinkedList<>();
        for (JDBCDatabaseCommunicationEngine each : databaseCommunicationEngines) {
            if (!includeInUse && inUseDatabaseCommunicationEngines.contains(each)) {
                continue;
            }
            try {
                each.close();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        if (includeInUse) {
            inUseDatabaseCommunicationEngines.clear();
        }
        databaseCommunicationEngines.retainAll(inUseDatabaseCommunicationEngines);
        return result;
    }
    
    /**
     * Close connections.
     * 
     * @param forceRollback is force rollback
     * @return SQL exception when connections close
     */
    public synchronized Collection<SQLException> closeConnections(final boolean forceRollback) {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : cachedConnections.values()) {
            try {
                if (forceRollback && connectionSession.getTransactionStatus().isInTransaction()) {
                    each.rollback();
                }
                resetConnection(each);
                each.close();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        cachedConnections.clear();
        connectionPostProcessors.clear();
        return result;
    }

    private void resetConnection(final Connection connection) throws SQLException {
        if (null == connection) {
            return;
        }
        if (connectionSession.isReadOnly()) {
            connection.setReadOnly(false);
        }
        if (null != connectionSession.getDefaultIsolationLevel()) {
            connection.setTransactionIsolation(TransactionUtil.getTransactionIsolationLevel(connectionSession.getIsolationLevel()));
        }
    }

    /**
     * Close federation executor.
     * 
     * @return SQL exception when federation executor close
     */
    public synchronized Collection<SQLException> closeFederationExecutor() {
        Collection<SQLException> result = new LinkedList<>();
        if (null != federationExecutor) {
            try {
                federationExecutor.close();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        return result;
    }
}
