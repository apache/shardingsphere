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

package io.shardingsphere.shardingproxy.backend.jdbc.datasource;

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.util.ReflectiveUtil;
import io.shardingsphere.shardingproxy.backend.BackendDataSource;
import io.shardingsphere.shardingproxy.util.DataSourceParameter;
import io.shardingsphere.transaction.ShardingTransactionManagerEngine;
import io.shardingsphere.transaction.core.TransactionType;
import io.shardingsphere.transaction.spi.ShardingTransactionManager;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Backend data source for JDBC.
 *
 * @author zhaojun
 * @author zhangliang
 * @author panjuan
 * @author maxiaoguang
 */
@Slf4j
@NoArgsConstructor
public final class JDBCBackendDataSource implements BackendDataSource, AutoCloseable {
    
    private Map<String, DataSource> dataSources;
    
    private JDBCBackendDataSourceFactory hikariDataSourceFactory = JDBCRawBackendDataSourceFactory.getInstance();
    
    public JDBCBackendDataSource(final Map<String, DataSourceParameter> dataSourceParameters) {
        createDataSourceMap(dataSourceParameters);
    }
    
    private void createDataSourceMap(final Map<String, DataSourceParameter> dataSourceParameters) {
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(dataSourceParameters.size(), 1);
        for (Entry<String, DataSourceParameter> entry : dataSourceParameters.entrySet()) {
            try {
                dataSourceMap.put(entry.getKey(), hikariDataSourceFactory.build(entry.getKey(), entry.getValue()));
            // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                throw new ShardingException(String.format("Can not build data source, name is `%s`.", entry.getKey()), ex);
            }
        }
        this.dataSources = dataSourceMap;
        ShardingTransactionManagerEngine.init(DatabaseType.MySQL, dataSourceMap);
    }
    
    /**
     * Get connection.
     *
     * @param dataSourceName data source name
     * @return connection
     * @throws SQLException SQL exception
     */
    public Connection getConnection(final String dataSourceName) throws SQLException {
        return getConnections(ConnectionMode.MEMORY_STRICTLY, dataSourceName, 1).get(0);
    }
    
    /**
     * Get connections.
     *
     * @param connectionMode connection mode
     * @param dataSourceName data source name
     * @param connectionSize size of connections to get
     * @return connections
     * @throws SQLException SQL exception
     */
    public List<Connection> getConnections(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize) throws SQLException {
        return getConnections(connectionMode, dataSourceName, connectionSize, TransactionType.LOCAL);
    }
    
    /**
     * Get connections.
     *
     * @param connectionMode connection mode 
     * @param dataSourceName data source name
     * @param connectionSize size of connections to be get
     * @param transactionType transaction type
     * @return connections
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public List<Connection> getConnections(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize, final TransactionType transactionType) throws SQLException {
        DataSource dataSource = dataSources.get(dataSourceName);
        if (1 == connectionSize) {
            return Collections.singletonList(createConnection(transactionType, dataSourceName, dataSource));
        }
        if (ConnectionMode.CONNECTION_STRICTLY == connectionMode) {
            return createConnections(transactionType, dataSourceName, dataSource, connectionSize);
        }
        synchronized (dataSource) {
            return createConnections(transactionType, dataSourceName, dataSource, connectionSize);
        }
    }
    
    private List<Connection> createConnections(final TransactionType transactionType, final String dataSourceName, final DataSource dataSource, final int connectionSize) throws SQLException {
        List<Connection> result = new ArrayList<>(connectionSize);
        for (int i = 0; i < connectionSize; i++) {
            try {
                result.add(createConnection(transactionType, dataSourceName, dataSource));
            } catch (final SQLException ex) {
                for (Connection each : result) {
                    each.close();
                }
                throw new SQLException(String.format("Could't get %d connections one time, partition succeed connection(%d) have released!", connectionSize, result.size()), ex);
            }
        }
        return result;
    }
    
    private Connection createConnection(final TransactionType transactionType, final String dataSourceName, final DataSource dataSource) throws SQLException {
        ShardingTransactionManager shardingTransactionManager = ShardingTransactionManagerEngine.getTransactionManager(transactionType);
        return isInShardingTransaction(shardingTransactionManager) ? shardingTransactionManager.getConnection(dataSourceName) : dataSource.getConnection();
    }
    
    private boolean isInShardingTransaction(final ShardingTransactionManager shardingTransactionManager) {
        return null != shardingTransactionManager && shardingTransactionManager.isInTransaction();
    }
    
    @Override
    public void close() throws Exception {
        if (null != dataSources) {
            closeDataSource(dataSources);
        }
        ShardingTransactionManagerEngine.close();
    }
    
    private void closeDataSource(final Map<String, DataSource> dataSourceMap) {
        for (DataSource each : dataSourceMap.values()) {
            try {
                ReflectiveUtil.findMethod(each, "close").invoke(each);
            } catch (final ReflectiveOperationException ignored) {
            }
        }
    }
}
