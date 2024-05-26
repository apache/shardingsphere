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
import org.apache.shardingsphere.driver.executor.DriverExecutor;
import org.apache.shardingsphere.driver.jdbc.adapter.executor.ForceExecuteTemplate;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.StatementManager;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.dialect.SQLExceptionTransformEngine;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.implicit.ImplicitTransactionCallback;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

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
    
    private boolean closed;
    
    private boolean closeOnCompletion;
    
    protected final boolean isNeedImplicitCommitTransaction(final ShardingSphereConnection connection, final SQLStatement sqlStatement, final boolean multiExecutionUnits) {
        if (!connection.getAutoCommit()) {
            return false;
        }
        TransactionType transactionType = connection.getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class).getDefaultType();
        boolean isInTransaction = connection.getDatabaseConnectionManager().getConnectionTransaction().isInTransaction();
        if (!TransactionType.isDistributedTransaction(transactionType) || isInTransaction) {
            return false;
        }
        return isWriteDMLStatement(sqlStatement) && multiExecutionUnits;
    }
    
    protected final <T> T executeWithImplicitCommitTransaction(final ImplicitTransactionCallback<T> callback, final Connection connection, final DatabaseType databaseType) throws SQLException {
        T result;
        try {
            connection.setAutoCommit(false);
            result = callback.execute();
            connection.commit();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            connection.rollback();
            throw SQLExceptionTransformEngine.toSQLException(ex, databaseType);
        } finally {
            connection.setAutoCommit(true);
        }
        return result;
    }
    
    private boolean isWriteDMLStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof DMLStatement && !(sqlStatement instanceof SelectStatement);
    }
    
    protected final void handleExceptionInTransaction(final ShardingSphereConnection connection, final MetaDataContexts metaDataContexts) {
        if (connection.getDatabaseConnectionManager().getConnectionTransaction().isInTransaction()) {
            DatabaseType databaseType = metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType();
            DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
            if (dialectDatabaseMetaData.getDefaultSchema().isPresent()) {
                connection.getDatabaseConnectionManager().getConnectionContext().getTransactionContext().setExceptionOccur(true);
            }
        }
    }
    
    protected abstract boolean isAccumulate();
    
    protected abstract Collection<? extends Statement> getRoutedStatements();
    
    protected abstract DriverExecutor getExecutor();
    
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
    public final SQLWarning getWarnings() {
        return null;
    }
    
    @Override
    public final void clearWarnings() {
    }
    
    @Override
    public void closeOnCompletion() {
        closeOnCompletion = true;
    }
    
    @Override
    public boolean isCloseOnCompletion() {
        return closeOnCompletion;
    }
    
    @Override
    public void setCursorName(final String name) throws SQLException {
        if (isTransparent()) {
            getRoutedStatements().iterator().next().setCursorName(name);
        } else {
            throw new SQLFeatureNotSupportedException("setCursorName");
        }
    }
    
    private boolean isTransparent() {
        return 1 == getRoutedStatements().size();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void cancel() throws SQLException {
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), Statement::cancel);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void close() throws SQLException {
        closed = true;
        try {
            forceExecuteTemplate.execute((Collection) getRoutedStatements(), Statement::close);
            if (null != getExecutor()) {
                getExecutor().close();
            }
            if (null != getStatementManager()) {
                getStatementManager().close();
            }
        } finally {
            getRoutedStatements().clear();
        }
    }
}
