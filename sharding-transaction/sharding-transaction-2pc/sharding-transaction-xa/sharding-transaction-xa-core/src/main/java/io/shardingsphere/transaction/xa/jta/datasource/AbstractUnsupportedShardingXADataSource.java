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

package io.shardingsphere.transaction.xa.jta.datasource;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Abstract unsupported sharding XA data source.
 *
 * @author zhaojun
 */
public abstract class AbstractUnsupportedShardingXADataSource implements XADataSource {
    
    @Override
    public final XAConnection getXAConnection(final String user, final String password) throws SQLException {
        throw new SQLFeatureNotSupportedException("getXAConnection by user and password");
    }
    
    @Override
    public final PrintWriter getLogWriter() throws SQLException {
        throw new SQLFeatureNotSupportedException("getLogWriter");
    }
    
    @Override
    public final void setLogWriter(final PrintWriter out) throws SQLException {
        throw new SQLFeatureNotSupportedException("setLogWriter");
    }
    
    @Override
    public final void setLoginTimeout(final int seconds) throws SQLException {
        throw new SQLFeatureNotSupportedException("setLoginTimeout");
    }
    
    @Override
    public final int getLoginTimeout() throws SQLException {
        throw new SQLFeatureNotSupportedException("getLoginTimeout");
    }
    
    @Override
    public final Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("getParentLogger");
    }
}
