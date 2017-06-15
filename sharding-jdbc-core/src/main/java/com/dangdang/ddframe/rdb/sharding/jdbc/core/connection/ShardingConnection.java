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

package com.dangdang.ddframe.rdb.sharding.jdbc.core.connection;

import com.codahale.metrics.Timer.Context;
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.hint.HintManagerHolder;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.ShardingContext;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractConnectionAdapter;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.statement.ShardingPreparedStatement;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.statement.ShardingStatement;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.MasterSlaveDataSource;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 支持分片的数据库连接.
 * 
 * @author zhangliang
 * @author gaohongtao
 */
@RequiredArgsConstructor
public final class ShardingConnection extends AbstractConnectionAdapter {
    
    @Getter
    private final ShardingContext shardingContext;
    
    private final Map<String, Connection> connectionMap = new HashMap<>();
    
    /**
     * 根据数据源名称获取相应的数据库连接.
     * 
     * @param dataSourceName 数据源名称
     * @param sqlType SQL语句类型
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    public Connection getConnection(final String dataSourceName, final SQLType sqlType) throws SQLException {
        Optional<Connection> connection = getCachedConnection(dataSourceName, sqlType);
        if (connection.isPresent()) {
            return connection.get();
        }
        Context metricsContext = MetricsContext.start(Joiner.on("-").join("ShardingConnection-getConnection", dataSourceName));
        DataSource dataSource = shardingContext.getShardingRule().getDataSourceRule().getDataSource(dataSourceName);
        Preconditions.checkState(null != dataSource, "Missing the rule of %s in DataSourceRule", dataSourceName);
        String realDataSourceName;
        if (dataSource instanceof MasterSlaveDataSource) {
            dataSource = ((MasterSlaveDataSource) dataSource).getDataSource(sqlType);
            realDataSourceName = MasterSlaveDataSource.getDataSourceName(dataSourceName, sqlType);
        } else {
            realDataSourceName = dataSourceName;
        }
        Connection result = dataSource.getConnection();
        MetricsContext.stop(metricsContext);
        connectionMap.put(realDataSourceName, result);
        replayMethodsInvocation(result);
        return result;
    }
    
    private Optional<Connection> getCachedConnection(final String dataSourceName, final SQLType sqlType) {
        String key = connectionMap.containsKey(dataSourceName) ? dataSourceName : MasterSlaveDataSource.getDataSourceName(dataSourceName, sqlType);
        return Optional.fromNullable(connectionMap.get(key));
    }
    
    /**
     * 释放数据库连接.
     *
     * @param connection 待释放的数据库连接
     */
    public void release(final Connection connection) {
        connectionMap.values().remove(connection);
        try {
            connection.close();
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return getConnection(shardingContext.getShardingRule().getDataSourceRule().getDataSourceNames().iterator().next(), SQLType.SELECT).getMetaData();
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
        return new ShardingPreparedStatement(this, sql, Statement.RETURN_GENERATED_KEYS);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
        return new ShardingPreparedStatement(this, sql, Statement.RETURN_GENERATED_KEYS);
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
    
    @Override
    public void close() throws SQLException {
        HintManagerHolder.clear();
        MasterSlaveDataSource.resetDMLFlag();
        super.close();
    }
}
