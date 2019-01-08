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
import io.shardingsphere.transaction.xa.convert.swap.DataSourceSwapperRegistry;
import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnection;
import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnectionFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
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
    
    private final DatabaseType databaseType;
    
    private final String resourceName;
    
    private final DataSource originalDataSource;
    
    private final XADataSource xaDataSource;
    
    private boolean isOriginalXADataSource;
    
    public ShardingXADataSource(final DatabaseType databaseType, final String resourceName, final DataSource dataSource) {
        this.databaseType = databaseType;
        this.resourceName = resourceName;
        this.originalDataSource = dataSource;
        if (dataSource instanceof XADataSource) {
            this.xaDataSource = (XADataSource) dataSource;
            this.isOriginalXADataSource = true;
        } else {
            this.xaDataSource = buildXADataSource(dataSource);
        }
    }
    
    private XADataSource buildXADataSource(final DataSource dataSource) {
        try {
            DataSourceParameter dataSourceParameter = DataSourceSwapperRegistry.getSwapper(dataSource.getClass()).swap(dataSource);
            XADataSource result = XADataSourceFactory.build(databaseType);
            Properties xaProperties = XAPropertiesFactory.createXAProperties(databaseType).build(dataSourceParameter);
            PropertyUtils.setProperties(result, xaProperties);
            return result;
        } catch (final PropertyException ex) {
            log.error("Failed to create ShardingXADataSource");
            throw new ShardingException(ex);
        }
    }
    
    @Override
    public ShardingXAConnection getXAConnection() throws SQLException {
        return isOriginalXADataSource ? new ShardingXAConnection(resourceName, xaDataSource.getXAConnection())
            : ShardingXAConnectionFactory.createShardingXAConnection(databaseType, resourceName, xaDataSource, originalDataSource.getConnection());
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
    
    /**
     * Get connection from original data source.
     *
     * @return connection
     * @throws SQLException SQL exception
     */
    public Connection getConnection() throws SQLException {
        return originalDataSource.getConnection();
    }
}
