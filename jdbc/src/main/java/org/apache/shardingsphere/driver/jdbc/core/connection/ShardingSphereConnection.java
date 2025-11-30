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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.driver.exception.ConnectionClosedException;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractConnectionAdapter;
import org.apache.shardingsphere.driver.jdbc.adapter.executor.ForceExecuteTemplate;
import org.apache.shardingsphere.driver.jdbc.core.datasource.metadata.ShardingSphereDatabaseMetaData;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSpherePreparedStatement;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSphereStatement;
import org.apache.shardingsphere.driver.jdbc.core.statement.StatementManager;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.transaction.ConnectionTransaction.DistributedTransactionOperationType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

/**
 * ShardingSphere connection.
 */
@HighFrequencyInvocation
public final class ShardingSphereConnection extends AbstractConnectionAdapter {
    
    private final ProcessEngine processEngine = new ProcessEngine();
    
    private final ForceExecuteTemplate<StatementManager> forceExecuteTemplate = new ForceExecuteTemplate<>();
    
    @Getter
    private final String currentDatabaseName;
    
    @Getter
    private final ContextManager contextManager;
    
    @Getter
    private final DriverDatabaseConnectionManager databaseConnectionManager;
    
    @Getter
    private final Collection<StatementManager> statementManagers = new ConcurrentLinkedQueue<>();
    
    @Getter
    private final String processId;
    
    private boolean autoCommit = true;
    
    private int transactionIsolation = TRANSACTION_READ_UNCOMMITTED;
    
    private boolean readOnly;
    
    private volatile boolean closed;
    
    public ShardingSphereConnection(final String currentDatabaseName, final ContextManager contextManager) {
        this.currentDatabaseName = currentDatabaseName;
        this.contextManager = contextManager;
        databaseConnectionManager = new DriverDatabaseConnectionManager(currentDatabaseName, contextManager);
        processId = processEngine.connect(currentDatabaseName);
    }
    
    /**
     * Begin transaction if needed when auto commit is false.
     *
     * @throws SQLException SQL exception
     */
    public void beginTransactionIfNeededWhenAutoCommitFalse() throws SQLException {
        if (autoCommit || databaseConnectionManager.getConnectionContext().getTransactionContext().isInTransaction()) {
            return;
        }
        databaseConnectionManager.begin();
    }
    
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
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
        return new ShardingSpherePreparedStatement(this, sql, columnNames);
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
        if (databaseConnectionManager.getConnectionTransaction().isLocalTransaction()) {
            processLocalTransaction();
        } else {
            processDistributedTransaction();
        }
    }
    
    private void processLocalTransaction() throws SQLException {
        databaseConnectionManager.setAutoCommit(autoCommit);
        TransactionConnectionContext transactionContext = databaseConnectionManager.getConnectionContext().getTransactionContext();
        if (autoCommit && transactionContext.isInTransaction()) {
            transactionContext.close();
            return;
        }
        if (!autoCommit && !transactionContext.isInTransaction()) {
            transactionContext.beginTransaction(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class).getDefaultType().name(),
                    databaseConnectionManager.getConnectionTransaction().getDistributedTransactionManager());
        }
    }
    
    private void processDistributedTransaction() throws SQLException {
        Optional<DistributedTransactionOperationType> operationType = databaseConnectionManager.getConnectionTransaction().getDistributedTransactionOperationType(autoCommit);
        if (!operationType.isPresent()) {
            return;
        }
        if (DistributedTransactionOperationType.BEGIN == operationType.get()) {
            databaseConnectionManager.begin();
        } else {
            databaseConnectionManager.commit();
        }
    }
    
    @Override
    public void commit() throws SQLException {
        databaseConnectionManager.commit();
    }
    
    @Override
    public void rollback() throws SQLException {
        databaseConnectionManager.rollback();
    }
    
    @Override
    public void rollback(final Savepoint savepoint) throws SQLException {
        checkClose();
        ShardingSpherePreconditions.checkState(databaseConnectionManager.getConnectionTransaction().isHoldTransaction(autoCommit) || !isSchemaSupportedDatabaseType(),
                () -> new SQLFeatureNotSupportedException("ROLLBACK TO SAVEPOINT can only be used in transaction blocks"));
        databaseConnectionManager.rollback(savepoint);
    }
    
    @Override
    public Savepoint setSavepoint(final String name) throws SQLException {
        checkClose();
        ShardingSpherePreconditions.checkState(databaseConnectionManager.getConnectionTransaction().isHoldTransaction(autoCommit) || !isSchemaSupportedDatabaseType(),
                () -> new SQLFeatureNotSupportedException("Savepoint can only be used in transaction blocks"));
        return databaseConnectionManager.setSavepoint(name);
    }
    
    @Override
    public Savepoint setSavepoint() throws SQLException {
        checkClose();
        ShardingSpherePreconditions.checkState(databaseConnectionManager.getConnectionTransaction().isHoldTransaction(autoCommit) || !isSchemaSupportedDatabaseType(),
                () -> new SQLFeatureNotSupportedException("Savepoint can only be used in transaction blocks"));
        return databaseConnectionManager.setSavepoint();
    }
    
    @Override
    public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
        checkClose();
        ShardingSpherePreconditions.checkState(databaseConnectionManager.getConnectionTransaction().isHoldTransaction(autoCommit) || !isSchemaSupportedDatabaseType(),
                () -> new SQLFeatureNotSupportedException("RELEASE SAVEPOINT can only be used in transaction blocks"));
        if (databaseConnectionManager.getConnectionTransaction().isHoldTransaction(autoCommit)) {
            databaseConnectionManager.releaseSavepoint(savepoint);
        }
    }
    
    private void checkClose() throws SQLException {
        ShardingSpherePreconditions.checkState(!isClosed(), () -> new ConnectionClosedException().toSQLException());
    }
    
    private boolean isSchemaSupportedDatabaseType() {
        DatabaseType databaseType = contextManager.getMetaDataContexts().getMetaData().getDatabase(currentDatabaseName).getProtocolType();
        return new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getSchemaOption().getDefaultSchema().isPresent();
    }
    
    @SuppressWarnings("MagicConstant")
    @Override
    public int getTransactionIsolation() throws SQLException {
        return databaseConnectionManager.getTransactionIsolation().orElseGet(() -> transactionIsolation);
    }
    
    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        transactionIsolation = level;
        databaseConnectionManager.setTransactionIsolation(level);
    }
    
    @Override
    public boolean isReadOnly() {
        return readOnly;
    }
    
    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException {
        this.readOnly = readOnly;
        databaseConnectionManager.setReadOnly(readOnly);
    }
    
    /*
     * This is just to avoid the Warning in <a href="https://github.com/brettwooldridge/HikariCP/issues/2196">brettwooldridge/HikariCP#2196</a>. ShardingSphere does not propagate this property to the
     * real JDBC Driver. `0` is actually the default value of {@link java.net.Socket#getSoTimeout()}.
     */
    @Override
    public int getNetworkTimeout() {
        return 0;
    }
    
    /*
     * This is just to avoid the Warning in <a href="https://github.com/brettwooldridge/HikariCP/issues/2196">brettwooldridge/HikariCP#2196</a>. ShardingSphere does not propagate this property to the
     * real JDBC Driver.
     */
    @Override
    public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
        ShardingSpherePreconditions.checkState(0 <= milliseconds, () -> new SQLException("Network timeout must be a value greater than or equal to 0."));
    }
    
    @Override
    public boolean isValid(final int timeout) throws SQLException {
        return databaseConnectionManager.isValid(timeout);
    }
    
    @Override
    public String getSchema() {
        return currentDatabaseName;
    }
    
    @Override
    public boolean isClosed() {
        return closed;
    }
    
    @Override
    public void close() throws SQLException {
        if (databaseConnectionManager.getConnectionTransaction().isInDistributedTransaction(databaseConnectionManager.getConnectionContext().getTransactionContext())) {
            databaseConnectionManager.getConnectionTransaction().rollback();
        }
        closed = true;
        processEngine.disconnect(processId);
        try {
            forceExecuteTemplate.execute(statementManagers, StatementManager::close);
        } finally {
            statementManagers.clear();
            databaseConnectionManager.close();
        }
    }
}
