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

import com.atomikos.beans.PropertyException;
import com.atomikos.beans.PropertyUtils;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnection;
import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnectionFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Sharding XA data source.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public final class ShardingXADataSource implements XADataSource {
    
    private final String resourceName;
    
    private final XADataSource xaDataSource;
    
    public ShardingXADataSource(final DatabaseType databaseType, final String resourceName, final DataSourceParameter dataSourceParameter) {
        try {
            XADataSource xaDataSource = XADataSourceFactory.build(databaseType);
            Properties xaProperties = XAPropertiesFactory.createXAProperties(databaseType).build(dataSourceParameter);
            PropertyUtils.setProperties(xaDataSource, xaProperties);
            this.resourceName = resourceName;
            this.xaDataSource = xaDataSource;
        } catch (final PropertyException ex) {
            log.error("Failed to create ShardingXADataSource");
            throw new ShardingException(ex);
        }
    }
    
    /**
     * Wrap a physical connection to sharding XA connection.
     *
     * @param databaseType databaseType
     * @param connection connection
     * @return sharding XA connection
     */
    public ShardingXAConnection wrapPhysicalConnection(final DatabaseType databaseType, final Connection connection) {
        return ShardingXAConnectionFactory.createShardingXAConnection(databaseType, resourceName, xaDataSource, connection);
    }
    
    @Override
    public XAConnection getXAConnection() throws SQLException {
        return new ShardingXAConnection(resourceName, xaDataSource.getXAConnection());
    }
    
    @Override
    public XAConnection getXAConnection(final String user, final String password) throws SQLException {
        return new ShardingXAConnection(resourceName, xaDataSource.getXAConnection(user, password));
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
