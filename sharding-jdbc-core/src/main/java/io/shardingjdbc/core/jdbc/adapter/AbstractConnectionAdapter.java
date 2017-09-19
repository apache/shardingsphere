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

package io.shardingjdbc.core.jdbc.adapter;

import io.shardingjdbc.core.jdbc.unsupported.AbstractUnsupportedOperationConnection;
import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Adapter for {@code Connection}.
 * 
 * @author zhangliang
 */
public abstract class AbstractConnectionAdapter extends AbstractUnsupportedOperationConnection {
    
    @Getter
    private final Map<String, Connection> cachedConnections = new HashMap<>();
    
    private boolean autoCommit = true;
    
    private boolean readOnly = true;
    
    private boolean closed;
    
    private int transactionIsolation = TRANSACTION_READ_UNCOMMITTED;
    
    @Override
    public final boolean getAutoCommit() throws SQLException {
        return autoCommit;
    }
    
    @Override
    public final void setAutoCommit(final boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
        if (cachedConnections.isEmpty()) {
            recordMethodInvocation(Connection.class, "setAutoCommit", new Class[] {boolean.class}, new Object[] {autoCommit});
            return;
        }
        for (Connection each : cachedConnections.values()) {
            each.setAutoCommit(autoCommit);
        }
    }
    
    @Override
    public final void commit() throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        for (Connection each : cachedConnections.values()) {
            try {
                each.commit();
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }
    
    @Override
    public final void rollback() throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        for (Connection each : cachedConnections.values()) {
            try {
                each.rollback();
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }
    
    @Override
    public void close() throws SQLException {
        closed = true;
        Collection<SQLException> exceptions = new LinkedList<>();
        for (Connection each : cachedConnections.values()) {
            try {
                each.close();
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
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
        if (cachedConnections.isEmpty()) {
            recordMethodInvocation(Connection.class, "setReadOnly", new Class[] {boolean.class}, new Object[] {readOnly});
            return;
        }
        for (Connection each : cachedConnections.values()) {
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
        if (cachedConnections.isEmpty()) {
            recordMethodInvocation(Connection.class, "setTransactionIsolation", new Class[] {int.class}, new Object[] {level});
            return;
        }
        for (Connection each : cachedConnections.values()) {
            each.setTransactionIsolation(level);
        }
    }
    
    // ------- Consist with MySQL driver implementation -------
    
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
