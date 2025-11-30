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

package org.apache.shardingsphere.driver.jdbc.core.connection;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.apache.shardingsphere.driver.jdbc.adapter.executor.ForceExecuteTemplate;
import org.apache.shardingsphere.driver.jdbc.adapter.invocation.MethodInvocationRecorder;
import org.apache.shardingsphere.driver.jdbc.core.savepoint.ShardingSphereSavepoint;
import org.apache.shardingsphere.infra.exception.kernel.connection.OverallConnectionNotEnoughException;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DatabaseConnectionManager;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.transaction.savepoint.ConnectionSavepointManager;
import org.apache.shardingsphere.transaction.ConnectionTransaction;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Database connection manager of ShardingSphere-JDBC.
 */
public final class DriverDatabaseConnectionManager implements DatabaseConnectionManager<Connection>, AutoCloseable {
    
    private final String currentDatabaseName;
    
    private final ContextManager contextManager;
    
    private final Map<String, DataSource> dataSourceMap;
    
    @Getter
    private final ConnectionContext connectionContext;
    
    private final Multimap<String, Connection> cachedConnections = LinkedHashMultimap.create();
    
    private final MethodInvocationRecorder<Connection> methodInvocationRecorder = new MethodInvocationRecorder<>();
    
    private final ForceExecuteTemplate<Connection> forceExecuteTemplate = new ForceExecuteTemplate<>();
    
    public DriverDatabaseConnectionManager(final String currentDatabaseName, final ContextManager contextManager) {
        this.currentDatabaseName = currentDatabaseName;
        this.contextManager = contextManager;
        dataSourceMap = contextManager.getStorageUnits(currentDatabaseName).entrySet()
                .stream().collect(Collectors.toMap(entry -> getKey(currentDatabaseName, entry.getKey()), entry -> entry.getValue().getDataSource()));
        connectionContext = new ConnectionContext(cachedConnections::keySet);
        connectionContext.setCurrentDatabaseName(currentDatabaseName);
    }
    
    private String getKey(final String databaseName, final String dataSourceName) {
        return databaseName.toLowerCase() + "." + dataSourceName;
    }
    
    /**
     * Get connection transaction.
     *
     * @return connection transaction
     */
    public ConnectionTransaction getConnectionTransaction() {
        TransactionRule rule = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class);
        return new ConnectionTransaction(rule, connectionContext.getTransactionContext());
    }
    
    /**
     * Set auto commit.
     *
     * @param autoCommit auto commit
     * @throws SQLException SQL exception
     */
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        methodInvocationRecorder.record("setAutoCommit", connection -> connection.setAutoCommit(autoCommit));
        forceExecuteTemplate.execute(getCachedConnections(), connection -> connection.setAutoCommit(autoCommit));
        if (autoCommit) {
            clearCachedConnections();
        }
    }
    
    private Collection<Connection> getCachedConnections() {
        return cachedConnections.values();
    }
    
    /**
     * Clear cached connections.
     *
     * @throws SQLException SQL exception
     */
    public void clearCachedConnections() throws SQLException {
        try {
            forceExecuteTemplate.execute(cachedConnections.values(), Connection::close);
        } finally {
            cachedConnections.clear();
        }
    }
    
    /**
     * Begin transaction.
     *
     * @throws SQLException SQL exception
     */
    public void begin() throws SQLException {
        ConnectionTransaction connectionTransaction = getConnectionTransaction();
        connectionContext.getTransactionContext().beginTransaction(connectionTransaction.getTransactionType().name(), connectionTransaction.getDistributedTransactionManager());
        if (!connectionTransaction.isLocalTransaction()) {
            close();
        }
        doBegin(connectionTransaction);
    }
    
    private void doBegin(final ConnectionTransaction connectionTransaction) throws SQLException {
        if (connectionTransaction.isLocalTransaction()) {
            setAutoCommit(false);
        } else {
            connectionTransaction.begin();
        }
    }
    
    /**
     * Commit.
     *
     * @throws SQLException SQL exception
     */
    public void commit() throws SQLException {
        ConnectionTransaction connectionTransaction = getConnectionTransaction();
        if (!connectionContext.getTransactionContext().isInTransaction() && !connectionTransaction.isInDistributedTransaction()) {
            return;
        }
        try {
            if (connectionTransaction.isLocalTransaction() && connectionContext.getTransactionContext().isExceptionOccur()) {
                forceExecuteTemplate.execute(getCachedConnections(), Connection::rollback);
            } else if (connectionTransaction.isLocalTransaction()) {
                forceExecuteTemplate.execute(getCachedConnections(), Connection::commit);
            } else {
                connectionTransaction.commit();
            }
        } finally {
            clear();
        }
    }
    
    /**
     * Rollback.
     *
     * @throws SQLException SQL exception
     */
    public void rollback() throws SQLException {
        ConnectionTransaction connectionTransaction = getConnectionTransaction();
        if (!connectionContext.getTransactionContext().isInTransaction() && !connectionTransaction.isInDistributedTransaction()) {
            return;
        }
        try {
            if (connectionTransaction.isLocalTransaction()) {
                forceExecuteTemplate.execute(getCachedConnections(), Connection::rollback);
            } else {
                connectionTransaction.rollback();
            }
        } finally {
            clear();
        }
    }
    
    /**
     * Rollback to savepoint.
     *
     * @param savepoint savepoint
     * @throws SQLException SQL exception
     */
    public void rollback(final Savepoint savepoint) throws SQLException {
        for (Connection each : getCachedConnections()) {
            ConnectionSavepointManager.getInstance().rollbackToSavepoint(each, savepoint.getSavepointName());
        }
    }
    
    private void clear() {
        methodInvocationRecorder.remove("setSavepoint");
        for (Connection each : getCachedConnections()) {
            ConnectionSavepointManager.getInstance().transactionFinished(each);
        }
        connectionContext.close();
    }
    
    /**
     * Set savepoint.
     *
     * @param savepointName savepoint name
     * @return savepoint savepoint
     * @throws SQLException SQL exception
     */
    public Savepoint setSavepoint(final String savepointName) throws SQLException {
        ShardingSphereSavepoint result = new ShardingSphereSavepoint(savepointName);
        for (Connection each : getCachedConnections()) {
            ConnectionSavepointManager.getInstance().setSavepoint(each, savepointName);
        }
        methodInvocationRecorder.record("setSavepoint", target -> ConnectionSavepointManager.getInstance().setSavepoint(target, savepointName));
        return result;
    }
    
    /**
     * Set savepoint.
     *
     * @return savepoint savepoint
     * @throws SQLException SQL exception
     */
    public Savepoint setSavepoint() throws SQLException {
        ShardingSphereSavepoint result = new ShardingSphereSavepoint();
        for (Connection each : getCachedConnections()) {
            ConnectionSavepointManager.getInstance().setSavepoint(each, result.getSavepointName());
        }
        methodInvocationRecorder.record("setSavepoint", target -> ConnectionSavepointManager.getInstance().setSavepoint(target, result.getSavepointName()));
        return result;
    }
    
    /**
     * Release savepoint.
     *
     * @param savepoint savepoint
     * @throws SQLException SQL exception
     */
    public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
        methodInvocationRecorder.remove("setSavepoint");
        for (Connection each : getCachedConnections()) {
            ConnectionSavepointManager.getInstance().releaseSavepoint(each, savepoint.getSavepointName());
        }
    }
    
    /**
     * Get transaction isolation.
     *
     * @return transaction isolation level
     * @throws SQLException SQL exception
     */
    public Optional<Integer> getTransactionIsolation() throws SQLException {
        return cachedConnections.values().isEmpty() ? Optional.empty() : Optional.of(cachedConnections.values().iterator().next().getTransactionIsolation());
    }
    
    /**
     * Set transaction isolation.
     *
     * @param level transaction isolation level
     * @throws SQLException SQL exception
     */
    public void setTransactionIsolation(final int level) throws SQLException {
        methodInvocationRecorder.record("setTransactionIsolation", connection -> connection.setTransactionIsolation(level));
        forceExecuteTemplate.execute(cachedConnections.values(), connection -> connection.setTransactionIsolation(level));
    }
    
    /**
     * Set read only.
     *
     * @param readOnly read only
     * @throws SQLException SQL exception
     */
    public void setReadOnly(final boolean readOnly) throws SQLException {
        methodInvocationRecorder.record("setReadOnly", connection -> connection.setReadOnly(readOnly));
        forceExecuteTemplate.execute(cachedConnections.values(), connection -> connection.setReadOnly(readOnly));
    }
    
    /**
     * Whether connection valid.
     *
     * @param timeout timeout
     * @return connection valid or not
     * @throws SQLException SQL exception
     */
    public boolean isValid(final int timeout) throws SQLException {
        for (Connection each : cachedConnections.values()) {
            if (!each.isValid(timeout)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get random physical data source name.
     *
     * @return random physical data source name
     */
    public String getRandomPhysicalDataSourceName() {
        return getRandomPhysicalDatabaseAndDataSourceName()[1];
    }
    
    private String[] getRandomPhysicalDatabaseAndDataSourceName() {
        Collection<String> cachedPhysicalDataSourceNames = Sets.intersection(dataSourceMap.keySet(), cachedConnections.keySet());
        Collection<String> databaseAndDatasourceNames = cachedPhysicalDataSourceNames.isEmpty() ? dataSourceMap.keySet() : cachedPhysicalDataSourceNames;
        return new ArrayList<>(databaseAndDatasourceNames).get(ThreadLocalRandom.current().nextInt(databaseAndDatasourceNames.size())).split("\\.");
    }
    
    /**
     * Get random connection.
     *
     * @return random connection
     * @throws SQLException SQL exception
     */
    public Connection getRandomConnection() throws SQLException {
        String[] databaseAndDataSourceName = getRandomPhysicalDatabaseAndDataSourceName();
        return getConnections0(databaseAndDataSourceName[0], databaseAndDataSourceName[1], 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
    }
    
    @Override
    public List<Connection> getConnections(final String databaseName, final String dataSourceName, final int connectionOffset, final int connectionSize,
                                           final ConnectionMode connectionMode) throws SQLException {
        return getConnections0(databaseName, dataSourceName, connectionOffset, connectionSize, connectionMode);
    }
    
    private List<Connection> getConnections0(final String databaseName, final String dataSourceName, final int connectionOffset, final int connectionSize,
                                             final ConnectionMode connectionMode) throws SQLException {
        String cacheKey = getKey(databaseName, dataSourceName);
        DataSource dataSource = currentDatabaseName.equals(databaseName) ? dataSourceMap.get(cacheKey) : contextManager.getStorageUnits(databaseName).get(dataSourceName).getDataSource();
        Preconditions.checkNotNull(dataSource, "Missing the data source name: '%s'", dataSourceName);
        Collection<Connection> connections;
        synchronized (cachedConnections) {
            connections = cachedConnections.get(cacheKey);
        }
        List<Connection> result;
        int maxConnectionSize = connectionOffset + connectionSize;
        if (connections.size() >= maxConnectionSize) {
            result = new ArrayList<>(connections).subList(connectionOffset, maxConnectionSize);
        } else if (connections.isEmpty()) {
            Collection<Connection> newConnections = createConnections(databaseName, dataSourceName, dataSource, maxConnectionSize, connectionMode);
            result = new ArrayList<>(newConnections).subList(connectionOffset, maxConnectionSize);
            synchronized (cachedConnections) {
                cachedConnections.putAll(cacheKey, newConnections);
            }
        } else {
            List<Connection> allConnections = new ArrayList<>(maxConnectionSize);
            allConnections.addAll(connections);
            Collection<Connection> newConnections = createConnections(databaseName, dataSourceName, dataSource, maxConnectionSize - connections.size(), connectionMode);
            allConnections.addAll(newConnections);
            result = allConnections.subList(connectionOffset, maxConnectionSize);
            synchronized (cachedConnections) {
                cachedConnections.putAll(cacheKey, newConnections);
            }
        }
        return result;
    }
    
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private List<Connection> createConnections(final String databaseName, final String dataSourceName, final DataSource dataSource, final int connectionSize,
                                               final ConnectionMode connectionMode) throws SQLException {
        if (1 == connectionSize) {
            Connection connection = createConnection(databaseName, dataSourceName, dataSource, connectionContext.getTransactionContext());
            try {
                methodInvocationRecorder.replay(connection);
            } catch (final SQLException ex) {
                connection.close();
                throw ex;
            }
            return Collections.singletonList(connection);
        }
        if (ConnectionMode.CONNECTION_STRICTLY == connectionMode) {
            return createConnections(databaseName, dataSourceName, dataSource, connectionSize, connectionContext.getTransactionContext());
        }
        synchronized (dataSource) {
            return createConnections(databaseName, dataSourceName, dataSource, connectionSize, connectionContext.getTransactionContext());
        }
    }
    
    private List<Connection> createConnections(final String databaseName, final String dataSourceName, final DataSource dataSource, final int connectionSize,
                                               final TransactionConnectionContext transactionConnectionContext) throws SQLException {
        List<Connection> result = new ArrayList<>(connectionSize);
        for (int i = 0; i < connectionSize; i++) {
            try {
                Connection connection = createConnection(databaseName, dataSourceName, dataSource, transactionConnectionContext);
                methodInvocationRecorder.replay(connection);
                result.add(connection);
            } catch (final SQLException ex) {
                for (Connection each : result) {
                    each.close();
                }
                throw new OverallConnectionNotEnoughException(connectionSize, result.size(), ex).toSQLException();
            }
        }
        return result;
    }
    
    private Connection createConnection(final String databaseName, final String dataSourceName, final DataSource dataSource,
                                        final TransactionConnectionContext transactionConnectionContext) throws SQLException {
        Optional<Connection> connectionInTransaction = getConnectionTransaction().getConnection(databaseName, dataSourceName, transactionConnectionContext);
        return connectionInTransaction.isPresent() ? connectionInTransaction.get() : dataSource.getConnection();
    }
    
    @Override
    public void close() throws SQLException {
        clearCachedConnections();
    }
}
