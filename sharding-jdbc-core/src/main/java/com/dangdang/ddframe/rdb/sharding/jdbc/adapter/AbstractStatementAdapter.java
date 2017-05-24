/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.jdbc.adapter;

import com.dangdang.ddframe.rdb.sharding.jdbc.unsupported.AbstractUnsupportedOperationStatement;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.dangdang.ddframe.rdb.sharding.util.ThrowableSQLExceptionMethod;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Collection;

/**
 * 静态语句对象适配类.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
public abstract class AbstractStatementAdapter extends AbstractUnsupportedOperationStatement {
    
    private final Class<? extends Statement> recordTargetClass;
    
    private boolean closed;
    
    private boolean poolable;
    
    private int fetchSize;
    
    protected abstract void clearRouteStatements();
    
    @Override
    @SuppressWarnings("unchecked")
    public final void close() throws SQLException {
        SQLUtil.safeInvoke(getRoutedStatements(), new ThrowableSQLExceptionMethod() {
            @Override
            public void apply(final Object object) throws SQLException {
                ((Statement) object).close();
            }
        });
        closed = true;
        clearRouteStatements();
    }
    
    @Override
    public final boolean isClosed() throws SQLException {
        return closed;
    }
    
    @Override
    public final boolean isPoolable() throws SQLException {
        return poolable;
    }
    
    @Override
    public final void setPoolable(final boolean poolable) throws SQLException {
        this.poolable = poolable;
        if (getRoutedStatements().isEmpty()) {
            recordMethodInvocation(recordTargetClass, "setPoolable", new Class[] {boolean.class}, new Object[] {poolable});
            return;
        }
        for (Statement each : getRoutedStatements()) {
            each.setPoolable(poolable);
        }
    }
    
    @Override
    public final int getFetchSize() throws SQLException {
        return fetchSize;
    }
    
    @Override
    public final void setFetchSize(final int rows) throws SQLException {
        this.fetchSize = rows;
        if (getRoutedStatements().isEmpty()) {
            recordMethodInvocation(recordTargetClass, "setFetchSize", new Class[] {int.class}, new Object[] {rows});
            return;
        }
        for (Statement each : getRoutedStatements()) {
            each.setFetchSize(rows);
        }
    }
    
    @Override
    public final void setEscapeProcessing(final boolean enable) throws SQLException {
        if (getRoutedStatements().isEmpty()) {
            recordMethodInvocation(recordTargetClass, "setEscapeProcessing", new Class[] {boolean.class}, new Object[] {enable});
            return;
        }
        for (Statement each : getRoutedStatements()) {
            each.setEscapeProcessing(enable);
        }
    }
    
    @Override
    public final void cancel() throws SQLException {
        for (Statement each : getRoutedStatements()) {
            each.cancel();
        }
    }
    
    @Override
    public final void setCursorName(final String name) throws SQLException {
        if (getRoutedStatements().isEmpty()) {
            recordMethodInvocation(recordTargetClass, "setCursorName", new Class[] {String.class}, new Object[] {name});
            return;
        }
        for (Statement each : getRoutedStatements()) {
            each.setCursorName(name);
        }
    }
    
    @Override
    public final int getUpdateCount() throws SQLException {
        long result = 0;
        boolean hasResult = false;
        for (Statement each : getRoutedStatements()) {
            if (each.getUpdateCount() > -1) {
                hasResult = true;
            }
            result += each.getUpdateCount();
        }
        if (result > Integer.MAX_VALUE) {
            result = Integer.MAX_VALUE;
        }
        return hasResult ? Long.valueOf(result).intValue() : -1;
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }
    
    @Override
    public void clearWarnings() throws SQLException {
    }
    
    /* 
     * 只有存储过程会出现多结果集, 因此不支持.
     */
    @Override
    public final boolean getMoreResults() throws SQLException {
        return false;
    }
    
    @Override
    public final boolean getMoreResults(final int current) throws SQLException {
        return false;
    }
    
    @Override
    public final int getMaxFieldSize() throws SQLException {
        return getRoutedStatements().isEmpty() ? 0 : getRoutedStatements().iterator().next().getMaxFieldSize();
    }
    
    @Override
    public final void setMaxFieldSize(final int max) throws SQLException {
        if (getRoutedStatements().isEmpty()) {
            recordMethodInvocation(recordTargetClass, "setMaxFieldSize", new Class[] {int.class}, new Object[] {max});
            return;
        }
        for (Statement each : getRoutedStatements()) {
            each.setMaxFieldSize(max);
        }
    }
    
    // TODO 未来需要确认MaxRows是否在多数据库情况下需要特殊处理,以满足校验需要. 如: 10个statement可能需要将MaxRows / 10
    @Override
    public final int getMaxRows() throws SQLException {
        return getRoutedStatements().isEmpty() ? -1 : getRoutedStatements().iterator().next().getMaxRows();
    }
    
    @Override
    public final void setMaxRows(final int max) throws SQLException {
        if (getRoutedStatements().isEmpty()) {
            recordMethodInvocation(recordTargetClass, "setMaxRows", new Class[] {int.class}, new Object[] {max});
            return;
        }
        for (Statement each : getRoutedStatements()) {
            each.setMaxRows(max);
        }
    }
    
    @Override
    public final int getQueryTimeout() throws SQLException {
        return getRoutedStatements().isEmpty() ? 0 : getRoutedStatements().iterator().next().getQueryTimeout();
    }
    
    @Override
    public final void setQueryTimeout(final int seconds) throws SQLException {
        if (getRoutedStatements().isEmpty()) {
            recordMethodInvocation(recordTargetClass, "setQueryTimeout", new Class[] {int.class}, new Object[] {seconds});
            return;
        }
        for (Statement each : getRoutedStatements()) {
            each.setQueryTimeout(seconds);
        }
    }
    
    /**
     * 获取路由的静态语句对象集合.
     * 
     * @return 路由的静态语句对象集合
     */
    protected abstract Collection<? extends Statement> getRoutedStatements();
}
