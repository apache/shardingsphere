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

package com.dangdang.ddframe.rdb.sharding.jdbc;

import com.codahale.metrics.Timer.Context;
import com.dangdang.ddframe.rdb.sharding.hint.HintManagerHolder;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractConnectionAdapter;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.contstant.SQLType;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
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
    
    @Getter(AccessLevel.PACKAGE)
    private final ShardingContext shardingContext;
    
    private final Map<String, Connection> connectionMap = new HashMap<>();
    
    /**
     * 根据数据源名称获取相应的数据库连接.
     * 
     * @param dataSourceName 数据源名称
     * @param sqlStatementType SQL语句类型
     * @return 数据库连接
     */
    public Connection getConnection(final String dataSourceName, final SQLType sqlStatementType) throws SQLException {
        Connection result = getConnectionInternal(dataSourceName, sqlStatementType);
        replayMethodsInvocation(result);
        return result;
    }
    
    /**
     * 释放缓存中已经中断的数据库连接.
     * 
     * @param brokenConnection 已经中断的数据库连接
     */
    public void releaseBrokenConnection(final Connection brokenConnection) {
        Preconditions.checkNotNull(brokenConnection);
        closeConnection(brokenConnection);
        connectionMap.values().remove(brokenConnection);
    }
    
    private void closeConnection(final Connection connection) {
        if (null != connection) {
            try {
                connection.close();
            } catch (final SQLException ignored) {
            }
        }
    }
    
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return getConnection(shardingContext.getShardingRule().getDataSourceRule().getDataSourceNames().iterator().next(), SQLType.SELECT).getMetaData();
    }
    
    private Connection getConnectionInternal(final String dataSourceName, final SQLType sqlStatementType) throws SQLException {
        Optional<Connection> connectionOptional = fetchCachedConnectionBySqlStatementType(dataSourceName, sqlStatementType);
        if (connectionOptional.isPresent()) {
            return connectionOptional.get();
        }
        Context metricsContext = MetricsContext.start(Joiner.on("-").join("ShardingConnection-getConnection", dataSourceName));
        DataSource dataSource = shardingContext.getShardingRule().getDataSourceRule().getDataSource(dataSourceName);
        Preconditions.checkState(null != dataSource, "Missing the rule of %s in DataSourceRule", dataSourceName);
        String realDataSourceName = dataSourceName;
        if (dataSource instanceof MasterSlaveDataSource) {
            dataSource = ((MasterSlaveDataSource) dataSource).getDataSource(sqlStatementType);
            realDataSourceName = getRealDataSourceName(dataSourceName, sqlStatementType);
        }
        Connection result = dataSource.getConnection();
        MetricsContext.stop(metricsContext);
        connectionMap.put(realDataSourceName, result);
        return result;
    }
    
    private String getRealDataSourceName(final String dataSourceName, final SQLType sqlStatementType) {
        String slaveDataSourceName = getSlaveDataSourceName(dataSourceName);
        if (!MasterSlaveDataSource.isDML(sqlStatementType)) {
            return slaveDataSourceName;
        }
        closeConnection(connectionMap.remove(slaveDataSourceName));
        return getMasterDataSourceName(dataSourceName);
    }
    
    private Optional<Connection> fetchCachedConnectionBySqlStatementType(final String dataSourceName, final SQLType sqlStatementType) {
        if (connectionMap.containsKey(dataSourceName)) {
            return Optional.of(connectionMap.get(dataSourceName));
        }
        String masterDataSourceName = getMasterDataSourceName(dataSourceName);
        if (connectionMap.containsKey(masterDataSourceName)) {
            return Optional.of(connectionMap.get(masterDataSourceName));
        }
        if (MasterSlaveDataSource.isDML(sqlStatementType)) {
            return Optional.absent();
        }
        String slaveDataSourceName = getSlaveDataSourceName(dataSourceName);
        if (connectionMap.containsKey(slaveDataSourceName)) {
            return Optional.of(connectionMap.get(slaveDataSourceName));
        }
        return Optional.absent();
    }
    
    private String getMasterDataSourceName(final String dataSourceName) {
        return Joiner.on("-").join(dataSourceName, "SHARDING-JDBC", "MASTER");
    }
    
    private String getSlaveDataSourceName(final String dataSourceName) {
        return Joiner.on("-").join(dataSourceName, "SHARDING-JDBC", "SLAVE");
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
    
    @Override
    public void close() throws SQLException {
        super.close();
        HintManagerHolder.clear();
        MasterSlaveDataSource.resetDMLFlag();
    }
}
