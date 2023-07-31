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

package org.apache.shardingsphere.proxy.backend.connector;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DatabaseConnectionManager;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.connection.ConnectionPostProcessor;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.connection.ResourceLock;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.BackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.TransactionUtils;
import org.apache.shardingsphere.transaction.spi.TransactionHook;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Database connection manager of ShardingSphere-Proxy.
 */
@RequiredArgsConstructor
@Getter
public final class ProxyDatabaseConnectionManager implements DatabaseConnectionManager<Connection> {
    
    private final ConnectionSession connectionSession;
    
    private final Multimap<String, Connection> cachedConnections = LinkedHashMultimap.create();
    
    private final Collection<ProxyBackendHandler> backendHandlers = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    
    private final Collection<ProxyBackendHandler> inUseBackendHandlers = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    
    private final Collection<ConnectionPostProcessor> connectionPostProcessors = new LinkedList<>();
    
    private final ResourceLock resourceLock = new ResourceLock();
    
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    private final Collection<TransactionHook> transactionHooks = ShardingSphereServiceLoader.getServiceInstances(TransactionHook.class);
    
    @Override
    public List<Connection> getConnections(final String dataSourceName, final int connectionOffset, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        Preconditions.checkNotNull(connectionSession.getDatabaseName(), "Current database name is null.");
        Collection<Connection> connections;
        synchronized (cachedConnections) {
            connections = cachedConnections.get(connectionSession.getDatabaseName().toLowerCase() + "." + dataSourceName);
        }
        List<Connection> result;
        int maxConnectionSize = connectionOffset + connectionSize;
        if (connections.size() >= maxConnectionSize) {
            result = new ArrayList<>(connections).subList(connectionOffset, maxConnectionSize);
        } else if (connections.isEmpty()) {
            Collection<Connection> newConnections = createNewConnections(dataSourceName, maxConnectionSize, connectionMode);
            result = new ArrayList<>(newConnections).subList(connectionOffset, maxConnectionSize);
            synchronized (cachedConnections) {
                cachedConnections.putAll(connectionSession.getDatabaseName().toLowerCase() + "." + dataSourceName, newConnections);
            }
            executeTransactionHooksAfterCreateConnections(result);
        } else {
            List<Connection> allConnections = new ArrayList<>(maxConnectionSize);
            allConnections.addAll(connections);
            List<Connection> newConnections = createNewConnections(dataSourceName, maxConnectionSize - connections.size(), connectionMode);
            allConnections.addAll(newConnections);
            result = allConnections.subList(connectionOffset, maxConnectionSize);
            synchronized (cachedConnections) {
                cachedConnections.putAll(connectionSession.getDatabaseName().toLowerCase() + "." + dataSourceName, newConnections);
            }
        }
        return result;
    }
    
    private void executeTransactionHooksAfterCreateConnections(final List<Connection> result) throws SQLException {
        if (connectionSession.getTransactionStatus().isInTransaction()) {
            for (TransactionHook each : transactionHooks) {
                each.afterCreateConnections(result, connectionSession.getConnectionContext().getTransactionContext());
            }
        }
    }
    
    private List<Connection> createNewConnections(final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        List<Connection> result = ProxyContext.getInstance().getBackendDataSource().getConnections(connectionSession.getDatabaseName().toLowerCase(), dataSourceName, connectionSize, connectionMode);
        setSessionVariablesIfNecessary(result);
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
    
    private void setSessionVariablesIfNecessary(final List<Connection> connections) throws SQLException {
        if (connectionSession.getRequiredSessionVariableRecorder().isEmpty() || connections.isEmpty()) {
            return;
        }
        String databaseType = connections.iterator().next().getMetaData().getDatabaseProductName();
        List<String> setSQLs = connectionSession.getRequiredSessionVariableRecorder().toSetSQLs(databaseType);
        try {
            executeSetSessionVariables(connections, setSQLs);
        } catch (final SQLException ex) {
            releaseConnection(connections, ex);
            throw ex;
        }
    }
    
    private void executeSetSessionVariables(final List<Connection> connections, final List<String> setSQLs) throws SQLException {
        for (Connection each : connections) {
            try (Statement statement = each.createStatement()) {
                for (String eachSetSQL : setSQLs) {
                    statement.execute(eachSetSQL);
                }
            }
        }
    }
    
    private void releaseConnection(final List<Connection> connections, final SQLException sqlException) {
        for (Connection each : connections) {
            try {
                each.close();
            } catch (final SQLException ex) {
                sqlException.setNextException(ex);
            }
        }
    }
    
    private void replayMethodsInvocation(final Connection target) throws SQLException {
        for (ConnectionPostProcessor each : connectionPostProcessors) {
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
            connection.setTransactionIsolation(TransactionUtils.getTransactionIsolationLevel(connectionSession.getIsolationLevel()));
        }
    }
    
    /**
     * Get used data source names.
     * 
     * @return used data source names
     */
    public Collection<String> getUsedDataSourceNames() {
        Collection<String> result = new ArrayList<>(cachedConnections.size());
        String databaseName = connectionSession.getDatabaseName().toLowerCase();
        for (String each : cachedConnections.keySet()) {
            String[] split = each.split("\\.", 2);
            String cachedDatabaseName = split[0];
            String cachedDataSourceName = split[1];
            if (databaseName.equals(cachedDatabaseName)) {
                result.add(cachedDataSourceName);
            }
        }
        return result;
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
     * Add handler.
     *
     * @param handler handler to be added
     */
    public void add(final ProxyBackendHandler handler) {
        backendHandlers.add(handler);
    }
    
    /**
     * Mark a handler as in use.
     *
     * @param handler handler to be marked
     */
    public void markResourceInUse(final ProxyBackendHandler handler) {
        inUseBackendHandlers.add(handler);
    }
    
    /**
     * Unmark a in use proxy backend handler.
     *
     * @param handler proxy backend handler to be added
     */
    public void unmarkResourceInUse(final ProxyBackendHandler handler) {
        inUseBackendHandlers.remove(handler);
    }
    
    /**
     * Handle auto commit.
     */
    public void handleAutoCommit() {
        if (!connectionSession.isAutoCommit() && !connectionSession.getTransactionStatus().isInTransaction()) {
            BackendTransactionManager transactionManager = new BackendTransactionManager(this);
            transactionManager.begin();
        }
    }
    
    /**
     * Close resources used in execution.
     *
     * @throws BackendConnectionException backend connection exception
     */
    public void closeExecutionResources() throws BackendConnectionException {
        synchronized (this) {
            Collection<Exception> result = new LinkedList<>(closeHandlers(false));
            if (!connectionSession.getTransactionStatus().isInConnectionHeldTransaction()) {
                result.addAll(closeHandlers(true));
                result.addAll(closeConnections(false));
            } else if (closed.get()) {
                result.addAll(closeHandlers(true));
                result.addAll(closeConnections(true));
            }
            if (result.isEmpty()) {
                return;
            }
            throw new BackendConnectionException(result);
        }
    }
    
    /**
     * Close all resources.
     */
    public void closeAllResources() {
        synchronized (this) {
            closed.set(true);
            closeHandlers(true);
            closeConnections(true);
        }
    }
    
    /**
     * Close handlers.
     *
     * @param includeInUse include handlers in use
     * @return SQL exception when handler close
     */
    public Collection<SQLException> closeHandlers(final boolean includeInUse) {
        Collection<SQLException> result = new LinkedList<>();
        for (ProxyBackendHandler each : backendHandlers) {
            if (!includeInUse && inUseBackendHandlers.contains(each)) {
                continue;
            }
            try {
                each.close();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        if (includeInUse) {
            inUseBackendHandlers.clear();
        }
        backendHandlers.retainAll(inUseBackendHandlers);
        return result;
    }
    
    /**
     * Close connections.
     * 
     * @param forceRollback is force rollback
     * @return SQL exception when connections close
     */
    public Collection<SQLException> closeConnections(final boolean forceRollback) {
        Collection<SQLException> result = new LinkedList<>();
        synchronized (cachedConnections) {
            resetSessionVariablesIfNecessary(cachedConnections.values(), result);
            for (Connection each : cachedConnections.values()) {
                try {
                    if (forceRollback && connectionSession.getTransactionStatus().isInTransaction()) {
                        each.rollback();
                    }
                    each.close();
                } catch (final SQLException ex) {
                    result.add(ex);
                }
            }
            cachedConnections.clear();
        }
        if (!forceRollback) {
            connectionPostProcessors.clear();
        }
        return result;
    }
    
    private void resetSessionVariablesIfNecessary(final Collection<Connection> values, final Collection<SQLException> exceptions) {
        if (connectionSession.getRequiredSessionVariableRecorder().isEmpty() || values.isEmpty()) {
            return;
        }
        String databaseType;
        try {
            databaseType = values.iterator().next().getMetaData().getDatabaseProductName();
        } catch (final SQLException ex) {
            exceptions.add(ex);
            return;
        }
        List<String> resetSQLs = connectionSession.getRequiredSessionVariableRecorder().toResetSQLs(databaseType);
        for (Connection each : values) {
            try (Statement statement = each.createStatement()) {
                for (String eachResetSQL : resetSQLs) {
                    statement.execute(eachResetSQL);
                }
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        connectionSession.getRequiredSessionVariableRecorder().removeVariablesWithDefaultValue();
    }
}
