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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DatabaseConnectionManager;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.connection.ConnectionPostProcessor;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.connection.ConnectionResourceLock;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Database connection manager of ShardingSphere-Proxy.
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public final class ProxyDatabaseConnectionManager implements DatabaseConnectionManager<Connection> {
    
    private final ConnectionSession connectionSession;
    
    private final Multimap<String, Connection> cachedConnections = LinkedHashMultimap.create();
    
    private final Collection<ProxyBackendHandler> proxyBackendHandlers = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    
    private final Collection<ProxyBackendHandler> inUseProxyBackendHandlers = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    
    private final Collection<ConnectionPostProcessor> connectionPostProcessors = new LinkedList<>();
    
    private final ConnectionResourceLock connectionResourceLock = new ConnectionResourceLock();
    
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    private final Object closeLock = new Object();
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, TransactionHook> transactionHooks = OrderedSPILoader.getServices(
            TransactionHook.class, ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules());
    
    @Override
    public List<Connection> getConnections(final String databaseName, final String dataSourceName, final int connectionOffset, final int connectionSize,
                                           final ConnectionMode connectionMode) throws SQLException {
        Preconditions.checkNotNull(databaseName, "Current database name is null.");
        Collection<Connection> connections;
        String cacheKey = getKey(databaseName, dataSourceName);
        synchronized (cachedConnections) {
            connections = cachedConnections.get(cacheKey);
        }
        List<Connection> result;
        int maxConnectionSize = connectionOffset + connectionSize;
        if (connections.size() >= maxConnectionSize) {
            result = new ArrayList<>(connections).subList(connectionOffset, maxConnectionSize);
        } else if (connections.isEmpty()) {
            Collection<Connection> newConnections = createNewConnections(databaseName, dataSourceName, maxConnectionSize, connectionMode);
            result = new ArrayList<>(newConnections).subList(connectionOffset, maxConnectionSize);
            synchronized (cachedConnections) {
                cachedConnections.putAll(cacheKey, newConnections);
            }
            executeTransactionHooksAfterCreateConnections(result);
        } else {
            List<Connection> allConnections = new ArrayList<>(maxConnectionSize);
            allConnections.addAll(connections);
            List<Connection> newConnections = createNewConnections(databaseName, dataSourceName, maxConnectionSize - connections.size(), connectionMode);
            allConnections.addAll(newConnections);
            result = allConnections.subList(connectionOffset, maxConnectionSize);
            synchronized (cachedConnections) {
                cachedConnections.putAll(cacheKey, newConnections);
            }
        }
        return result;
    }
    
    private String getKey(final String databaseName, final String dataSourceName) {
        return databaseName.toLowerCase() + "." + dataSourceName;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void executeTransactionHooksAfterCreateConnections(final List<Connection> connections) throws SQLException {
        if (connectionSession.getTransactionStatus().isInTransaction()) {
            DatabaseType databaseType = ProxyContext.getInstance().getContextManager().getDatabaseType();
            for (Entry<ShardingSphereRule, TransactionHook> entry : transactionHooks.entrySet()) {
                entry.getValue().afterCreateConnections(entry.getKey(), databaseType, connections, connectionSession.getConnectionContext().getTransactionContext());
            }
        }
    }
    
    private List<Connection> createNewConnections(final String databaseName, final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        List<Connection> result = ProxyContext.getInstance().getBackendDataSource().getConnections(databaseName.toLowerCase(), dataSourceName, connectionSize, connectionMode);
        setSessionVariablesIfNecessary(result);
        for (Connection each : result) {
            replayTransactionOption(each);
        }
        if (connectionSession.getConnectionContext().getTransactionContext().isTransactionStarted()) {
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
    
    @SuppressWarnings("MagicConstant")
    private void replayTransactionOption(final Connection connection) throws SQLException {
        if (null == connection) {
            return;
        }
        if (connectionSession.isReadOnly()) {
            connection.setReadOnly(true);
        }
        if (connectionSession.getIsolationLevel().isPresent()) {
            connection.setTransactionIsolation(TransactionUtils.getTransactionIsolationLevel(connectionSession.getIsolationLevel().get()));
        }
    }
    
    /**
     * Get used data source names.
     *
     * @return used data source names
     */
    public Collection<String> getUsedDataSourceNames() {
        Collection<String> result = new ArrayList<>(cachedConnections.size());
        String databaseName = connectionSession.getUsedDatabaseName().toLowerCase();
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
        proxyBackendHandlers.add(handler);
    }
    
    /**
     * Mark a handler as in use.
     *
     * @param handler handler to be marked
     */
    public void markResourceInUse(final ProxyBackendHandler handler) {
        inUseProxyBackendHandlers.add(handler);
    }
    
    /**
     * Unmark a in use proxy backend handler.
     *
     * @param handler proxy backend handler to be added
     */
    public void unmarkResourceInUse(final ProxyBackendHandler handler) {
        inUseProxyBackendHandlers.remove(handler);
    }
    
    /**
     * Handle auto commit.
     */
    public void handleAutoCommit() {
        if (!connectionSession.isAutoCommit() && !connectionSession.getTransactionStatus().isInTransaction()) {
            ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(this);
            transactionManager.begin();
        }
    }
    
    /**
     * Close resources used in execution.
     *
     * @throws BackendConnectionException backend connection exception
     */
    public void closeExecutionResources() throws BackendConnectionException {
        synchronized (closeLock) {
            Collection<Exception> result = new LinkedList<>(closeHandlers(false));
            if (!connectionSession.getTransactionStatus().isInConnectionHeldTransaction(TransactionUtils.getTransactionType(connectionSession.getConnectionContext().getTransactionContext()))) {
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
     *
     * @return exceptions occurred during closing resources
     */
    public Collection<SQLException> closeAllResources() {
        synchronized (closeLock) {
            closed.set(true);
            Collection<SQLException> result = new LinkedList<>();
            result.addAll(closeHandlers(true));
            result.addAll(closeConnections(true));
            return result;
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
        for (ProxyBackendHandler each : proxyBackendHandlers) {
            if (!includeInUse && inUseProxyBackendHandlers.contains(each)) {
                continue;
            }
            try {
                each.close();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        if (includeInUse) {
            inUseProxyBackendHandlers.clear();
        }
        proxyBackendHandlers.retainAll(inUseProxyBackendHandlers);
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
                } catch (final SQLException ignored) {
                } finally {
                    try {
                        each.close();
                    } catch (final SQLException ex) {
                        if (!isClosed(each)) {
                            log.warn("Close connection {} failed.", each, ex);
                            result.add(ex);
                        }
                    }
                }
            }
            cachedConnections.clear();
        }
        if (!forceRollback) {
            connectionPostProcessors.clear();
        }
        return result;
    }
    
    private boolean isClosed(final Connection connection) {
        try {
            if (connection.isClosed()) {
                return true;
            }
        } catch (final SQLException ignored) {
        }
        return false;
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
