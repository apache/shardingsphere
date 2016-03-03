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

package com.dangdang.ddframe.rdb.sharding.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import com.codahale.metrics.Timer.Context;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractConnectionAdapter;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 支持分片的数据库连接.
 * 
 * @author zhangliang
 * @author gaohongtao
 */
@RequiredArgsConstructor
public final class ShardingConnection extends AbstractConnectionAdapter {
    
    @Getter(AccessLevel.PACKAGE)
    private final ShardingContext shardingContext;
    
    private Map<String, Connection> connectionMap = new HashMap<>();
    
    /**
     * 根据数据源名称获取相应的数据库连接.
     * 
     * @param dataSourceName 数据源名称
     * @return 数据库连接
     */
    public Connection getConnection(final String dataSourceName) throws SQLException {
        if (connectionMap.containsKey(dataSourceName)) {
            return connectionMap.get(dataSourceName);
        }
        Context metricsContext = MetricsContext.start("ShardingConnection-getConnection", dataSourceName);
        Connection connection = shardingContext.getShardingRule().getDataSourceRule().getDataSource(dataSourceName).getConnection();
        MetricsContext.stop(metricsContext);
        replayMethodsInvovation(connection);
        connectionMap.put(dataSourceName, connection);
        return connection;
    }
    
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        if (connectionMap.isEmpty()) {
            DataSourceRule dataSourceRule = shardingContext.getShardingRule().getDataSourceRule();
            String dsName = dataSourceRule.getDataSourceNames().iterator().next();
            connectionMap.put(dsName, dataSourceRule.getDataSource(dsName).getConnection());
        }
        return getDatabaseMetaDataFromConnection(connectionMap.values().iterator().next());
    }
    
    public static DatabaseMetaData getDatabaseMetaDataFromDataSource(final Collection<DataSource> dataSources) {
        Collection<Connection> connections = null;
        try {
            connections = Collections2.transform(dataSources, new Function<DataSource, Connection>() {
                
                @Override
                public Connection apply(final DataSource input) {
                    try {
                        return input.getConnection();
                    } catch (final SQLException ex) {
                        throw new ShardingJdbcException(ex);
                    }
                }
            });
            return getDatabaseMetaDataFromConnection(connections);
        } finally {
            if (null != connections) {
                for (Connection each : connections) {
                    try {
                        each.close();
                    } catch (final SQLException ignored) {
                    }
                }
            }
        }
    }
    
    private static DatabaseMetaData getDatabaseMetaDataFromConnection(final Connection connection) {
        return getDatabaseMetaDataFromConnection(Lists.newArrayList(connection));
    }
    
    private static DatabaseMetaData getDatabaseMetaDataFromConnection(final Collection<Connection> connections) {
        String databaseProductName = null;
        DatabaseMetaData result = null;
        for (Connection each : connections) {
            String databaseProductNameInEach;
            DatabaseMetaData metaDataInEach;
            try {
                metaDataInEach = each.getMetaData();
                databaseProductNameInEach = metaDataInEach.getDatabaseProductName();
            } catch (final SQLException ex) {
                throw new ShardingJdbcException("Can not get data source DatabaseProductName", ex);
            }
            Preconditions.checkState(null == databaseProductName || databaseProductName.equals(databaseProductNameInEach),
                    String.format("Database type inconsistent with '%s' and '%s'", databaseProductName, databaseProductNameInEach));
            databaseProductName = databaseProductNameInEach;
            result = metaDataInEach;
        }
        return result;
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        return new ShardingPreparedStatement(this, sql);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return new ShardingPreparedStatement(this, sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return new ShardingPreparedStatement(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
        return new ShardingPreparedStatement(this, sql, autoGeneratedKeys);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
        return new ShardingPreparedStatement(this, sql, columnIndexes);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
        return new ShardingPreparedStatement(this, sql, columnNames);
    }
    
    @Override
    public Statement createStatement() throws SQLException {
        return new ShardingStatement(this);
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return new ShardingStatement(this, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return new ShardingStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public Collection<Connection> getConnections() {
        return connectionMap.values();
    }
}
