/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.xa.jta.connection;

import io.shardingsphere.transaction.xa.jta.ShardingXAResource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Sharding XA Connection.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
@Getter
public final class ShardingXAConnection implements XAConnection {
    
    private final String resourceName;
    
    private final XAConnection delegate;
    
    @Override
    public XAResource getXAResource() throws SQLException {
        return new ShardingXAResource(resourceName, delegate.getXAResource());
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }
    
    @Override
    public void close() throws SQLException {
        delegate.close();
    }
    
    @Override
    public void addConnectionEventListener(final ConnectionEventListener listener) {
        delegate.addConnectionEventListener(listener);
    }
    
    @Override
    public void removeConnectionEventListener(final ConnectionEventListener listener) {
        delegate.removeConnectionEventListener(listener);
    }
    
    @Override
    public void addStatementEventListener(final StatementEventListener listener) {
        delegate.addStatementEventListener(listener);
    }
    
    @Override
    public void removeStatementEventListener(final StatementEventListener listener) {
        delegate.removeStatementEventListener(listener);
    }
}
