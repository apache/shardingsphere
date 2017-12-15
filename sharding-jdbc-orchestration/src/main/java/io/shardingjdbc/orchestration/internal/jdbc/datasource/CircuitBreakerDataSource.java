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

package io.shardingjdbc.orchestration.internal.jdbc.datasource;

import io.shardingjdbc.core.jdbc.unsupported.AbstractUnsupportedOperationDataSource;
import io.shardingjdbc.orchestration.internal.jdbc.connection.CircuitBreakerConnection;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Circuit breaker datasource.
 * 
 * @author caohao
 */
public final class CircuitBreakerDataSource extends AbstractUnsupportedOperationDataSource implements AutoCloseable {
    
    @Override
    public void close() throws Exception {
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return new CircuitBreakerConnection();
    }
    
    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return new CircuitBreakerConnection();
    }
    
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }
    
    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
    }
    
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
