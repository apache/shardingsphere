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

package org.apache.shardingsphere.driver.jdbc.adapter;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.driver.jdbc.adapter.executor.ForceExecuteTemplate;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.StatementManager;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.transaction.util.AutoCommitUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Collection;

/**
 * Adapter for {@code Statement}.
 */
@Getter
public abstract class AbstractStatementAdapter extends WrapperAdapter implements Statement {
    
    @Getter(AccessLevel.NONE)
    private final ForceExecuteTemplate<Statement> forceExecuteTemplate = new ForceExecuteTemplate<>();
    
    private boolean poolable;
    
    private int fetchSize;
    
    private int fetchDirection;
    
    private boolean closeOnCompletion;
    
    private boolean closed;
    
    protected final void handleAutoCommitBeforeExecution(final SQLStatement sqlStatement, final ShardingSphereConnection connection) throws SQLException {
        if (AutoCommitUtils.isNeedStartTransaction(sqlStatement)) {
            connection.beginTransactionIfNeededWhenAutoCommitFalse();
        }
    }
    
    protected final void handleAutoCommitAfterExecution(final ShardingSphereConnection connection) throws SQLException {
        if (connection.getAutoCommit()) {
            connection.getDatabaseConnectionManager().clearCachedConnections();
        }
    }
    
    protected final void handleExceptionInTransaction(final ShardingSphereConnection connection, final ShardingSphereMetaData metaData) {
        if (connection.getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isInTransaction()) {
            DatabaseType databaseType = metaData.getDatabase(connection.getCurrentDatabaseName()).getProtocolType();
            DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
            if (dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent()) {
                connection.getDatabaseConnectionManager().getConnectionContext().getTransactionContext().setExceptionOccur(true);
            }
        }
    }
    
    protected abstract boolean isAccumulate();
    
    protected abstract Collection<? extends Statement> getRoutedStatements();
    
    protected abstract StatementManager getStatementManager();
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void setPoolable(final boolean poolable) throws SQLException {
        this.poolable = poolable;
        getMethodInvocationRecorder().record("setPoolable", statement -> statement.setPoolable(poolable));
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), statement -> statement.setPoolable(poolable));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void setFetchSize(final int rows) throws SQLException {
        fetchSize = rows;
        getMethodInvocationRecorder().record("setFetchSize", statement -> statement.setFetchSize(rows));
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), statement -> statement.setFetchSize(rows));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void setFetchDirection(final int direction) throws SQLException {
        fetchDirection = direction;
        getMethodInvocationRecorder().record("setFetchDirection", statement -> statement.setFetchDirection(direction));
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), statement -> statement.setFetchDirection(direction));
    }
    
    @Override
    public final int getMaxFieldSize() throws SQLException {
        return getRoutedStatements().isEmpty() ? 0 : getRoutedStatements().iterator().next().getMaxFieldSize();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void setMaxFieldSize(final int max) throws SQLException {
        getMethodInvocationRecorder().record("setMaxFieldSize", statement -> statement.setMaxFieldSize(max));
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), statement -> statement.setMaxFieldSize(max));
    }
    
    // TODO Confirm MaxRows for multiple databases is need special handle. eg: 10 statements maybe MaxRows / 10
    @Override
    public final int getMaxRows() throws SQLException {
        return getRoutedStatements().isEmpty() ? -1 : getRoutedStatements().iterator().next().getMaxRows();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void setMaxRows(final int max) throws SQLException {
        getMethodInvocationRecorder().record("setMaxRows", statement -> statement.setMaxRows(max));
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), statement -> statement.setMaxRows(max));
    }
    
    @Override
    public final int getQueryTimeout() throws SQLException {
        return getRoutedStatements().isEmpty() ? 0 : getRoutedStatements().iterator().next().getQueryTimeout();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void setQueryTimeout(final int seconds) throws SQLException {
        getMethodInvocationRecorder().record("setQueryTimeout", statement -> statement.setQueryTimeout(seconds));
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), statement -> statement.setQueryTimeout(seconds));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void setEscapeProcessing(final boolean enable) throws SQLException {
        getMethodInvocationRecorder().record("setEscapeProcessing", statement -> statement.setEscapeProcessing(enable));
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), statement -> statement.setEscapeProcessing(enable));
    }
    
    @Override
    public final int getUpdateCount() throws SQLException {
        if (isAccumulate()) {
            return accumulate();
        }
        Collection<? extends Statement> statements = getRoutedStatements();
        if (statements.isEmpty()) {
            return -1;
        }
        return getRoutedStatements().iterator().next().getUpdateCount();
    }
    
    private int accumulate() throws SQLException {
        long result = 0L;
        boolean hasResult = false;
        for (Statement each : getRoutedStatements()) {
            int updateCount = each.getUpdateCount();
            if (updateCount > -1) {
                hasResult = true;
            }
            result += updateCount;
        }
        if (result > Integer.MAX_VALUE) {
            result = Integer.MAX_VALUE;
        }
        return hasResult ? (int) result : -1;
    }
    
    @Override
    public final boolean getMoreResults() throws SQLException {
        boolean result = false;
        for (Statement each : getRoutedStatements()) {
            result = each.getMoreResults();
        }
        return result;
    }
    
    @Override
    public final boolean getMoreResults(final int current) {
        return false;
    }
    
    @Override
    public final boolean isCloseOnCompletion() {
        return closeOnCompletion;
    }
    
    @Override
    public final void closeOnCompletion() {
        closeOnCompletion = true;
    }
    
    @Override
    public final void setCursorName(final String name) throws SQLException {
        ShardingSpherePreconditions.checkState(1 == getRoutedStatements().size(), () -> new SQLFeatureNotSupportedException("setCursorName"));
        getRoutedStatements().iterator().next().setCursorName(name);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void cancel() throws SQLException {
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), Statement::cancel);
    }
    
    @Override
    public final SQLWarning getWarnings() {
        return null;
    }
    
    @Override
    public final void clearWarnings() {
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void close() throws SQLException {
        closed = true;
        try {
            forceExecuteTemplate.execute((Collection) getRoutedStatements(), Statement::close);
            closeExecutor();
            if (null != getStatementManager()) {
                getStatementManager().close();
                Connection connection = getConnection();
                if (connection instanceof ShardingSphereConnection) {
                    ShardingSphereConnection logicalConnection = (ShardingSphereConnection) connection;
                    logicalConnection.getStatementManagers().remove(getStatementManager());
                    handleAutoCommitAfterExecution(logicalConnection);
                }
            }
        } finally {
            getRoutedStatements().clear();
        }
    }
    
    protected abstract void closeExecutor() throws SQLException;
}
