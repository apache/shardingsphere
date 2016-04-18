/**
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Collection;

import com.dangdang.ddframe.rdb.sharding.jdbc.unsupported.AbstractUnsupportedOperationConnection;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;

/**
 * 数据库连接适配类.
 * 
 * @author zhangliang
 */
public abstract class AbstractConnectionAdapter extends AbstractUnsupportedOperationConnection {
    
    private boolean autoCommit = true;
    
    private boolean readOnly = true;
    
    private boolean closed;
    
    private int transactionIsolation = TRANSACTION_READ_UNCOMMITTED;
    
    protected abstract Collection<Connection> getConnections();
    
    @Override
    public final boolean getAutoCommit() throws SQLException {
        return autoCommit;
    }
    
    @Override
    public final void setAutoCommit(final boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
        if (getConnections().isEmpty()) {
            recordMethodInvocation(Connection.class, "setAutoCommit", new Class[] {boolean.class}, new Object[] {autoCommit});
            return;
        }
        for (Connection each : getConnections()) {
            each.setAutoCommit(autoCommit);
        }
    }
    
    @Override
    public final void commit() throws SQLException {
        for (Connection each : getConnections()) {
            each.commit();
        }
    }
    
    @Override
    public final void rollback() throws SQLException {
        for (Connection each : getConnections()) {
            each.rollback();
        }
    }
    
    @Override
    public final void close() throws SQLException {
        for (Connection each : getConnections()) {
            each.close();
        }
        closed = true;
        MetricsContext.clear();
    }
    
    @Override
    public final boolean isClosed() throws SQLException {
        return closed;
    }
    
    @Override
    public final boolean isReadOnly() throws SQLException {
        return readOnly;
    }
    
    @Override
    public final void setReadOnly(final boolean readOnly) throws SQLException {
        this.readOnly = readOnly;
        if (getConnections().isEmpty()) {
            recordMethodInvocation(Connection.class, "setReadOnly", new Class[] {boolean.class}, new Object[] {readOnly});
            return;
        }
        for (Connection each : getConnections()) {
            each.setReadOnly(readOnly);
        }
    }
    
    @Override
    public final int getTransactionIsolation() throws SQLException {
        return transactionIsolation;
    }
    
    @Override
    public final void setTransactionIsolation(final int level) throws SQLException {
        transactionIsolation = level;
        if (getConnections().isEmpty()) {
            recordMethodInvocation(Connection.class, "setTransactionIsolation", new Class[] {int.class}, new Object[] {level});
            return;
        }
        for (Connection each : getConnections()) {
            each.setTransactionIsolation(level);
        }
    }
    
    // -------以下代码与MySQL实现保持一致.-------
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }
    
    @Override
    public void clearWarnings() throws SQLException {
    }
    
    @Override
    public final int getHoldability() throws SQLException {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }
    
    @Override
    public final void setHoldability(final int holdability) throws SQLException {
    }
}
