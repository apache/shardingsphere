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

package io.shardingsphere.transaction.xa.convert.datasource;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.util.ReflectiveUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Sharding XA data source.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
@Getter
public final class ShardingXADataSource implements XADataSource {
    
    private final XADataSource xaDataSource;
    
    private final String datasourceName;
    
    /**
     * Wrap physical connection to XA connection.
     * @param connection connection
     * @param databaseType databaseType
     * @return XA connection
     * @throws NoSuchMethodException No such method exception
     * @throws InvocationTargetException invocation target exception
     * @throws IllegalAccessException illegal access exception
     */
    public XAConnection wrapPhysicalConnection(final Connection connection, final DatabaseType databaseType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, SQLException {
        Class<?> clazz = xaDataSource.getClass();
        switch (databaseType) {
            case MySQL:
                Connection mysqlPhysicalConnection = (Connection) connection.unwrap(Class.forName("com.mysql.jdbc.Connection"));
                return (XAConnection) ReflectiveUtil.findMethod(xaDataSource, "wrapConnection", Connection.class).invoke(xaDataSource, mysqlPhysicalConnection);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database type: `%s`", databaseType));
        }
    }
    
    @Override
    public XAConnection getXAConnection() throws SQLException {
        return xaDataSource.getXAConnection();
    }
    
    @Override
    public XAConnection getXAConnection(final String user, final String password) throws SQLException {
        return xaDataSource.getXAConnection(user, password);
    }
    
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return xaDataSource.getLogWriter();
    }
    
    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        xaDataSource.setLogWriter(out);
    }
    
    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        xaDataSource.setLoginTimeout(seconds);
    }
    
    @Override
    public int getLoginTimeout() throws SQLException {
        return xaDataSource.getLoginTimeout();
    }
    
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return xaDataSource.getParentLogger();
    }
}
