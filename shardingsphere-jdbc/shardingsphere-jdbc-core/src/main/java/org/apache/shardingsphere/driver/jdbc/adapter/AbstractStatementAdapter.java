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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationStatement;
import org.apache.shardingsphere.driver.jdbc.adapter.executor.ForceExecuteTemplate;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Collection;

/**
 * Adapter for {@code Statement}.
 */
@RequiredArgsConstructor
public abstract class AbstractStatementAdapter extends AbstractUnsupportedOperationStatement {
    
    private final Class<? extends Statement> targetClass;
    
    private boolean closed;
    
    private boolean poolable;
    
    private int fetchSize;
    
    private final ForceExecuteTemplate<Statement> forceExecuteTemplate = new ForceExecuteTemplate<>();
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void close() throws SQLException {
        closed = true;
        try {
            forceExecuteTemplate.execute((Collection) getRoutedStatements(), Statement::close);
        } finally {
            getRoutedStatements().clear();
        }
    }
    
    @Override
    public final boolean isClosed() {
        return closed;
    }
    
    @Override
    public final boolean isPoolable() {
        return poolable;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void setPoolable(final boolean poolable) throws SQLException {
        this.poolable = poolable;
        recordMethodInvocation(targetClass, "setPoolable", new Class[] {boolean.class}, new Object[] {poolable});
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), statement -> statement.setPoolable(poolable));
    }
    
    @Override
    public final int getFetchSize() {
        return fetchSize;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void setFetchSize(final int rows) throws SQLException {
        fetchSize = rows;
        recordMethodInvocation(targetClass, "setFetchSize", new Class[] {int.class}, new Object[] {rows});
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), statement -> statement.setFetchSize(rows));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void setEscapeProcessing(final boolean enable) throws SQLException {
        recordMethodInvocation(targetClass, "setEscapeProcessing", new Class[] {boolean.class}, new Object[] {enable});
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), statement -> statement.setEscapeProcessing(enable));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void cancel() throws SQLException {
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), Statement::cancel);
    }
    
    @Override
    public final int getUpdateCount() throws SQLException {
        if (isAccumulate()) {
            return accumulate();
        } else {
            Collection<? extends Statement> statements = getRoutedStatements();
            if (statements.isEmpty()) {
                return -1;
            }
            return getRoutedStatements().iterator().next().getUpdateCount();
        }
    }
    
    private int accumulate() throws SQLException {
        long result = 0;
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
        return hasResult ? Long.valueOf(result).intValue() : -1;
    }
    
    @Override
    public final SQLWarning getWarnings() {
        return null;
    }
    
    @Override
    public final void clearWarnings() {
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
    public final int getMaxFieldSize() throws SQLException {
        return getRoutedStatements().isEmpty() ? 0 : getRoutedStatements().iterator().next().getMaxFieldSize();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void setMaxFieldSize(final int max) throws SQLException {
        recordMethodInvocation(targetClass, "setMaxFieldSize", new Class[] {int.class}, new Object[] {max});
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
        recordMethodInvocation(targetClass, "setMaxRows", new Class[] {int.class}, new Object[] {max});
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), statement -> statement.setMaxRows(max));
    }
    
    @Override
    public final int getQueryTimeout() throws SQLException {
        return getRoutedStatements().isEmpty() ? 0 : getRoutedStatements().iterator().next().getQueryTimeout();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void setQueryTimeout(final int seconds) throws SQLException {
        recordMethodInvocation(targetClass, "setQueryTimeout", new Class[] {int.class}, new Object[] {seconds});
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), statement -> statement.setQueryTimeout(seconds));
    }
    
    protected abstract boolean isAccumulate();
    
    protected abstract Collection<? extends Statement> getRoutedStatements();
}
