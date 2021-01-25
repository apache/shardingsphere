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

package org.apache.shardingsphere.scaling.core.datasource;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Data source wrapper is for abstract standard jdbc and sharding jdbc.
 */
@RequiredArgsConstructor
public final class DataSourceWrapper implements DataSource, AutoCloseable {
    
    private final DataSource dataSource;
    
    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return dataSource.getConnection(username, password);
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return dataSource.unwrap(iface);
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return dataSource.isWrapperFor(iface);
    }
    
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }
    
    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }
    
    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }
    
    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }
    
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }
    
    @Override
    public void close() throws SQLException {
        if (null == dataSource) {
            return;
        }
        if (dataSource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) dataSource).close();
            } catch (final SQLException ex) {
                throw ex;
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                throw new SQLException("data source close failed.", ex);
            }
        }
    }
}
