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

import lombok.Getter;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractConnectionAdapter;
import org.apache.shardingsphere.driver.jdbc.core.datasource.metadata.ShardingSphereDatabaseMetaData;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSpherePreparedStatement;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSphereStatement;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.ExecutorJDBCManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.readwritesplitting.route.impl.PrimaryVisitedManager;
import org.apache.shardingsphere.transaction.TransactionHolder;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * ShardingSphere connection.
 */
public final class ShardingSphereConnection extends AbstractConnectionAdapter implements ExecutorJDBCManager {
    
    @Getter
    private final String schema;
    
    @Getter
    private final ContextManager contextManager;
    
    private final ConnectionManager connectionManager;
    
    private boolean autoCommit = true;
    
    private int transactionIsolation = TRANSACTION_READ_UNCOMMITTED;
    
    private boolean readOnly;
    
    private volatile boolean closed;
    
    public ShardingSphereConnection(final String schema, final ContextManager contextManager) {
        this.schema = schema;
        this.contextManager = contextManager;
        connectionManager = new ConnectionManager(schema, contextManager);
    }
    
    /**
     * Get random physical data source name.
     *
     * @return random physical data source name
     */
    public String getRandomPhysicalDataSourceName() {
        return connectionManager.getRandomPhysicalDataSourceName();
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
        return connectionManager.getConnections(dataSourceName, connectionSize, connectionMode);
    }
    
    /**
     * Whether hold transaction or not.
     *
     * @return true or false
     */
    public boolean isHoldTransaction() {
        return connectionManager.getConnectionTransaction().isHoldTransaction(autoCommit);
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
        this.autoCommit = autoCommit;
        if (connectionManager.getConnectionTransaction().isLocalTransaction()) {
            processLocalTransaction();
        } else {
            processDistributeTransaction();
        }
    }
    
    private void processLocalTransaction() throws SQLException {
        connectionManager.setAutoCommit(autoCommit);
        if (!autoCommit) {
            TransactionHolder.setInTransaction();
        }
    }
    
    private void processDistributeTransaction() throws SQLException {
        switch (connectionManager.getConnectionTransaction().getDistributedTransactionOperationType(autoCommit)) {
            case BEGIN:
                connectionManager.close();
                connectionManager.getConnectionTransaction().begin();
                TransactionHolder.setInTransaction();
                break;
            case COMMIT:
                connectionManager.getConnectionTransaction().commit();
                break;
            default:
                break;
        }
    }
    
    @Override
    public void commit() throws SQLException {
        try {
            connectionManager.commit();
        } finally {
            TransactionHolder.clear();
        }
    }
    
    @Override
    public void rollback() throws SQLException {
        try {
            connectionManager.rollback();
        } finally {
            TransactionHolder.clear();
        }
    }
    
    @SuppressWarnings("MagicConstant")
    @Override
    public int getTransactionIsolation() throws SQLException {
        return connectionManager.getTransactionIsolation().orElseGet(() -> transactionIsolation);
    }
    
    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        transactionIsolation = level;
        connectionManager.setTransactionIsolation(level);
    }
    
    @Override
    public boolean isReadOnly() {
        return readOnly;
    }
    
    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException {
        this.readOnly = readOnly;
        connectionManager.setReadOnly(readOnly);
    }
    
    @Override
    public boolean isValid(final int timeout) throws SQLException {
        return connectionManager.isValid(timeout);
    }
    
    @Override
    public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
        return getConnection(getRandomPhysicalDataSourceName()).createArrayOf(typeName, elements);
    }
    
    @Override
    public boolean isClosed() {
        return closed;
    }
    
    @Override
    public void close() throws SQLException {
        closed = true;
        PrimaryVisitedManager.clear();
        connectionManager.close();
    }
}
