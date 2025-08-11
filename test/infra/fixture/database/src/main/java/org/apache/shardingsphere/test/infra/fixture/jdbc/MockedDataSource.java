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

package org.apache.shardingsphere.test.infra.fixture.jdbc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mocked data source.
 */
@NoArgsConstructor
@Getter
@Setter
public final class MockedDataSource implements DataSource, AutoCloseable {
    
    private String url = "jdbc:mock://127.0.0.1/foo_ds";
    
    private String driverClassName;
    
    private String username = "root";
    
    private String password = "root";
    
    private Integer maxPoolSize;
    
    private Integer minPoolSize;
    
    private Duration connectionTimeout;
    
    private List<String> connectionInitSqls;
    
    private Properties jdbcUrlProperties;
    
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Connection connection;
    
    @Setter(AccessLevel.NONE)
    private boolean closed;
    
    private final Collection<Connection> openedConnections = new HashSet<>();
    
    public MockedDataSource(final Connection connection) {
        this.connection = connection;
    }
    
    @SuppressWarnings("MagicConstant")
    @Override
    public Connection getConnection() throws SQLException {
        if (null != connection) {
            return connection;
        }
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getURL()).thenReturn(url);
        when(result.createStatement(anyInt(), anyInt(), anyInt()).getConnection()).thenReturn(result);
        doAnswer(invocation -> openedConnections.remove(result)).when(result).close();
        openedConnections.add(result);
        return result;
    }
    
    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return getConnection();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        throw new SQLException("Wrapped DataSource is not an instance of " + iface);
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return false;
    }
    
    @SuppressWarnings("ReturnOfNull")
    @Override
    public PrintWriter getLogWriter() {
        return null;
    }
    
    @Override
    public void setLogWriter(final PrintWriter out) {
    }
    
    @Override
    public void setLoginTimeout(final int seconds) {
    }
    
    @Override
    public int getLoginTimeout() {
        return 0;
    }
    
    @SuppressWarnings("ReturnOfNull")
    @Override
    public Logger getParentLogger() {
        return null;
    }
    
    @Override
    public void close() {
        closed = true;
    }
}
