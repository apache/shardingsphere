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
import lombok.Getter;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractConnectionAdapter;
import org.apache.shardingsphere.driver.jdbc.adapter.executor.ForceExecuteTemplate;
import org.apache.shardingsphere.driver.jdbc.core.datasource.metadata.ShardingSphereDatabaseMetaData;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSpherePreparedStatement;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSphereStatement;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.ExecutorJDBCManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.readwritesplitting.route.impl.PrimaryVisitedManager;
import org.apache.shardingsphere.transaction.ConnectionTransaction;
import org.apache.shardingsphere.transaction.TransactionHolder;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * ShardingSphere Connection.
 */
public final class ShardingSphereConnection extends AbstractConnectionAdapter implements ExecutorJDBCManager {
    
    @Getter
    private final String schema;
    
    @Getter
    private final ContextManager contextManager;
    
    private final Multimap<String, Connection> cachedConnections = LinkedHashMultimap.create();
    
    private final ConnectionTransaction connectionTransaction;
    
    private final ForceExecuteTemplate<Connection> forceExecuteTemplate = new ForceExecuteTemplate<>();
    
    private final Random random = new SecureRandom();
    
    private boolean autoCommit = true;
    
    private boolean readOnly;
    
    private int transactionIsolation = TRANSACTION_READ_UNCOMMITTED;
    
    private volatile boolean closed;
    
    public ShardingSphereConnection(final String schema, final ContextManager contextManager) {
        this.schema = schema;
        this.contextManager = contextManager;
        connectionTransaction = createConnectionTransaction(schema, contextManager);
    }
    
    private ConnectionTransaction createConnectionTransaction(final String schemaName, final ContextManager contextManager) {
        Optional<TransactionRule> transactionRule = contextManager.getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(TransactionRule.class);
        return transactionRule.map(optional -> new ConnectionTransaction(schemaName, optional, contextManager.getTransactionContexts()))
                .orElseGet(() -> new ConnectionTransaction(schemaName, contextManager.getTransactionContexts()));
    }
    
    /**
     * Get random physical data source name.
     * 
     * @return random physical data source name
     */
    public String getRandomPhysicalDataSourceName() {
        Collection<String> datasourceNames = cachedConnections.isEmpty() ? contextManager.getDataSourceMap(schema).keySet() : cachedConnections.keySet();
        return new ArrayList<>(datasourceNames).get(random.nextInt(datasourceNames.size()));
    }
    
    /**
     * Get connection.
     *
     * @param dataSourceName data source name
     * @return connection
     * @throws SQLException SQL exception
     */
    public Connection getConnection(final String dataSourceName) throws SQLException {
        return getConnections(dataSourceName, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
    }
    
    @Override
    public List<Connection> getConnections(final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        DataSource dataSource = contextManager.getDataSourceMap(schema).get(dataSourceName);
        Preconditions.checkState(null != dataSource, "Missing the data source name: '%s'", dataSourceName);
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
            List<Connection> newConnections = createConnections(dataSourceName, dataSource, connectionSize - connections.size(), connectionMode);
            result.addAll(newConnections);
            synchronized (cachedConnections) {
                cachedConnections.putAll(dataSourceName, newConnections);
            }
        } else {
            result = new ArrayList<>(createConnections(dataSourceName, dataSource, connectionSize, connectionMode));
            synchronized (cachedConnections) {
                cachedConnections.putAll(dataSourceName, result);
            }
        }
        return result;
    }
    
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private List<Connection> createConnections(final String dataSourceName, final DataSource dataSource, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        if (1 == connectionSize) {
            Connection connection = createConnection(dataSourceName, dataSource);
            replayMethodsInvocation(connection);
            return Collections.singletonList(connection);
        }
        if (ConnectionMode.CONNECTION_STRICTLY == connectionMode) {
            return createConnections(dataSourceName, dataSource, connectionSize);
        }
        synchronized (dataSource) {
            return createConnections(dataSourceName, dataSource, connectionSize);
        }
    }
    
    private List<Connection> createConnections(final String dataSourceName, final DataSource dataSource, final int connectionSize) throws SQLException {
        List<Connection> result = new ArrayList<>(connectionSize);
        for (int i = 0; i < connectionSize; i++) {
            try {
                Connection connection = createConnection(dataSourceName, dataSource);
                replayMethodsInvocation(connection);
                result.add(connection);
            } catch (final SQLException ex) {
                for (Connection each : result) {
                    each.close();
                }
                throw new SQLException(String.format("Can not get %d connections one time, partition succeed connection(%d) have released!", connectionSize, result.size()), ex);
            }
        }
        return result;
    }
    
    private Connection createConnection(final String dataSourceName, final DataSource dataSource) throws SQLException {
        Optional<Connection> connectionInTransaction = connectionTransaction.getConnection(dataSourceName);
        return connectionInTransaction.isPresent() ? connectionInTransaction.get() : dataSource.getConnection();
    }
    
    /**
     * Whether hold transaction or not.
     *
     * @return true or false
     */
    public boolean isHoldTransaction() {
        return connectionTransaction.isHoldTransaction(autoCommit);
    }
    
    @SuppressWarnings("MagicConstant")
    @Override
    public Statement createStorageResource(final Connection connection, final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        return connection.createStatement(option.getResultSetType(), option.getResultSetConcurrency(), option.getResultSetHoldability());
    }

    @SuppressWarnings("MagicConstant")
    @Override
    public PreparedStatement createStorageResource(final String sql, final List<Object> parameters,
                                                   final Connection connection, final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        return option.isReturnGeneratedKeys() ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : connection.prepareStatement(sql, option.getResultSetType(), option.getResultSetConcurrency(), option.getResultSetHoldability());
    }
    
    @Override
    public DatabaseMetaData getMetaData() {
        return new ShardingSphereDatabaseMetaData(this);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        return new ShardingSpherePreparedStatement(this, sql);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return new ShardingSpherePreparedStatement(this, sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return new ShardingSpherePreparedStatement(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
        return new ShardingSpherePreparedStatement(this, sql, autoGeneratedKeys);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
        return new ShardingSpherePreparedStatement(this, sql, Statement.RETURN_GENERATED_KEYS);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
        return new ShardingSpherePreparedStatement(this, sql, Statement.RETURN_GENERATED_KEYS);
    }
    
    @Override
    public Statement createStatement() {
        return new ShardingSphereStatement(this);
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) {
        return new ShardingSphereStatement(this, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) {
        return new ShardingSphereStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public boolean getAutoCommit() {
        return autoCommit;
    }
    
    @Override
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        if (connectionTransaction.isLocalTransaction()) {
            processLocalTransaction(autoCommit);
        } else {
            processDistributeTransaction(autoCommit);
        }
    }
    
    private void processLocalTransaction(final boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
        recordMethodInvocation(Connection.class, "setAutoCommit", new Class[]{boolean.class}, new Object[]{autoCommit});
        forceExecuteTemplate.execute(cachedConnections.values(), connection -> connection.setAutoCommit(autoCommit));
        if (!autoCommit) {
            TransactionHolder.setInTransaction();
        }
    }
    
    private void processDistributeTransaction(final boolean autoCommit) throws SQLException {
        switch (connectionTransaction.getDistributedTransactionOperationType(autoCommit)) {
            case BEGIN:
                closeCachedConnections();
                connectionTransaction.begin();
                TransactionHolder.setInTransaction();
                break;
            case COMMIT:
                connectionTransaction.commit();
                break;
            default:
                break;
        }
    }
    
    private void closeCachedConnections() throws SQLException {
        forceExecuteTemplate.execute(cachedConnections.values(), Connection::close);
        cachedConnections.clear();
    }
    
    @Override
    public void commit() throws SQLException {
        try {
            if (connectionTransaction.isLocalTransaction()) {
                forceExecuteTemplate.execute(cachedConnections.values(), Connection::commit);
            } else {
                connectionTransaction.commit();
            }
        } finally {
            TransactionHolder.clear();
        }
    }
    
    @Override
    public void rollback() throws SQLException {
        try {
            if (connectionTransaction.isLocalTransaction()) {
                forceExecuteTemplate.execute(cachedConnections.values(), Connection::rollback);
            } else {
                connectionTransaction.rollback();
            }
        } finally {
            TransactionHolder.clear();
        }
    }
    
    @Override
    public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
        String dataSourceName = contextManager.getDataSourceMap(schema).keySet().iterator().next();
        Connection connection = getConnection(dataSourceName);
        return connection.createArrayOf(typeName, elements);
    }
    
    @Override
    public boolean isReadOnly() {
        return readOnly;
    }
    
    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException {
        this.readOnly = readOnly;
        recordMethodInvocation(Connection.class, "setReadOnly", new Class[]{boolean.class}, new Object[]{readOnly});
        forceExecuteTemplate.execute(cachedConnections.values(), connection -> connection.setReadOnly(readOnly));
    }
    
    @Override
    public int getTransactionIsolation() throws SQLException {
        return cachedConnections.values().isEmpty() ? transactionIsolation : cachedConnections.values().iterator().next().getTransactionIsolation();
    }
    
    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        transactionIsolation = level;
        recordMethodInvocation(Connection.class, "setTransactionIsolation", new Class[]{int.class}, new Object[]{level});
        forceExecuteTemplate.execute(cachedConnections.values(), connection -> connection.setTransactionIsolation(level));
    }
    
    @Override
    public boolean isValid(final int timeout) throws SQLException {
        for (Connection connection : cachedConnections.values()) {
            if (!connection.isValid(timeout)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean isClosed() {
        return closed;
    }
    
    @Override
    public void close() throws SQLException {
        closed = true;
        PrimaryVisitedManager.clear();
        try {
            forceExecuteTemplate.execute(cachedConnections.values(), Connection::close);
        } finally {
            cachedConnections.clear();
        }
    }
}
