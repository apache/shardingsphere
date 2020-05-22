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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.kernal.context.schema.DataSourceParameter;
import org.apache.shardingsphere.proxy.backend.BackendDataSource;
import org.apache.shardingsphere.proxy.backend.schema.ShardingSphereSchemas;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingTransactionManager;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Backend data source for JDBC.
 */
public final class JDBCBackendDataSource implements BackendDataSource, AutoCloseable {
    
    @Getter
    private Map<String, DataSource> dataSources;
    
    @Getter
    private final Map<String, DataSourceParameter> dataSourceParameters; 
    
    private final JDBCBackendDataSourceFactory dataSourceFactory = JDBCRawBackendDataSourceFactory.getInstance();
    
    @Getter
    private ShardingTransactionManagerEngine shardingTransactionManagerEngine = new ShardingTransactionManagerEngine();
    
    public JDBCBackendDataSource(final Map<String, DataSourceParameter> dataSourceParameters) {
        this.dataSources = createDataSources(dataSourceParameters);
        this.dataSourceParameters = dataSourceParameters;
        shardingTransactionManagerEngine.init(ShardingSphereSchemas.getInstance().getDatabaseType(), dataSources);
    }
    
    private Map<String, DataSource> createDataSources(final Map<String, DataSourceParameter> dataSourceParameters) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceParameters.size(), 1);
        for (Entry<String, DataSourceParameter> entry : dataSourceParameters.entrySet()) {
            try {
                result.put(entry.getKey(), dataSourceFactory.build(entry.getKey(), entry.getValue()));
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                throw new ShardingSphereException(String.format("Can not build data source, name is `%s`.", entry.getKey()), ex);
            }
        }
        return result;
    }
    
    /**
     * Get connection.
     *
     * @param dataSourceName data source name
     * @return connection
     * @throws SQLException SQL exception
     */
    public Connection getConnection(final String dataSourceName) throws SQLException {
        return getConnections(dataSourceName, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
    }
    
    /**
     * Get connections.
     *
     * @param dataSourceName data source name
     * @param connectionSize size of connections to get
     * @param connectionMode connection mode
     * @return connections
     * @throws SQLException SQL exception
     */
    public List<Connection> getConnections(final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        return getConnections(dataSourceName, connectionSize, connectionMode, TransactionType.LOCAL);
    }
    
    /**
     * Get connections.
     *
     * @param dataSourceName data source name
     * @param connectionSize size of connections to be get
     * @param connectionMode connection mode
     * @param transactionType transaction type
     * @return connections
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public List<Connection> getConnections(final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode, final TransactionType transactionType) throws SQLException {
        DataSource dataSource = dataSources.get(dataSourceName);
        if (1 == connectionSize) {
            return Collections.singletonList(createConnection(dataSourceName, dataSource, transactionType));
        }
        if (ConnectionMode.CONNECTION_STRICTLY == connectionMode) {
            return createConnections(dataSourceName, dataSource, connectionSize, transactionType);
        }
        synchronized (dataSource) {
            return createConnections(dataSourceName, dataSource, connectionSize, transactionType);
        }
    }
    
    private List<Connection> createConnections(final String dataSourceName, final DataSource dataSource, final int connectionSize, final TransactionType transactionType) throws SQLException {
        List<Connection> result = new ArrayList<>(connectionSize);
        for (int i = 0; i < connectionSize; i++) {
            try {
                result.add(createConnection(dataSourceName, dataSource, transactionType));
            } catch (final SQLException ex) {
                for (Connection each : result) {
                    each.close();
                }
                throw new SQLException(String.format("Could't get %d connections one time, partition succeed connection(%d) have released!", connectionSize, result.size()), ex);
            }
        }
        return result;
    }
    
    private Connection createConnection(final String dataSourceName, final DataSource dataSource, final TransactionType transactionType) throws SQLException {
        ShardingTransactionManager shardingTransactionManager = shardingTransactionManagerEngine.getTransactionManager(transactionType);
        return isInShardingTransaction(shardingTransactionManager) ? shardingTransactionManager.getConnection(dataSourceName) : dataSource.getConnection();
    }
    
    private boolean isInShardingTransaction(final ShardingTransactionManager shardingTransactionManager) {
        return null != shardingTransactionManager && shardingTransactionManager.isInTransaction();
    }
    
    @Override
    public void close() throws Exception {
        if (null == dataSources) {
            return;
        }
        dataSourceParameters.clear();
        close(dataSources.keySet());
        shardingTransactionManagerEngine.close();
    }
    
    private void close(final Collection<String> dataSourceNames) {
        for (String each :dataSourceNames) {
            close(dataSources.get(each));
        }
    }
    
    private void close(final DataSource dataSource) { 
        try { 
            Method method = dataSource.getClass().getDeclaredMethod("close");
            method.setAccessible(true);
            method.invoke(dataSource); 
        } catch (final ReflectiveOperationException ignored) {
        }
    }
    
    /**
     * Renew JDBC backend data source.
     * 
     * @param dataSourceParameters data source parameters
     * @throws Exception exception
     */
    public void renew(final Map<String, DataSourceParameter> dataSourceParameters) throws Exception {
        List<String> deletedDataSources = getDeletedDataSources(dataSourceParameters);
        Map<String, DataSourceParameter> modifiedDataSources = getModifiedDataSources(dataSourceParameters);
        close(deletedDataSources);
        close(modifiedDataSources.keySet());
        dataSources = getChangedDataSources(deletedDataSources, getAddedDataSources(dataSourceParameters), modifiedDataSources);
        this.dataSourceParameters.clear();
        this.dataSourceParameters.putAll(dataSourceParameters);
        shardingTransactionManagerEngine.close();
        shardingTransactionManagerEngine.init(ShardingSphereSchemas.getInstance().getDatabaseType(), dataSources);
    }
    
    private synchronized List<String> getDeletedDataSources(final Map<String, DataSourceParameter> dataSourceParameters) {
        List<String> result = new LinkedList<>(this.dataSourceParameters.keySet());
        result.removeAll(dataSourceParameters.keySet());
        return result;
    }
    
    private synchronized Map<String, DataSourceParameter> getModifiedDataSources(final Map<String, DataSourceParameter> dataSourceParameters) {
        Map<String, DataSourceParameter> result = new LinkedHashMap<>();
        for (Entry<String, DataSourceParameter> entry : dataSourceParameters.entrySet()) {
            if (isModifiedDataSource(entry)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private synchronized boolean isModifiedDataSource(final Entry<String, DataSourceParameter> dataSourceNameAndParameters) {
        return dataSourceParameters.containsKey(dataSourceNameAndParameters.getKey()) && !dataSourceParameters.get(dataSourceNameAndParameters.getKey()).equals(dataSourceNameAndParameters.getValue());
    }
    
    private synchronized Map<String, DataSource> getChangedDataSources(final List<String> deletedDataSources, 
                                                                       final Map<String, DataSourceParameter> addedDataSources, final Map<String, DataSourceParameter> modifiedDataSources) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSources);
        result.keySet().removeAll(deletedDataSources);
        result.keySet().removeAll(modifiedDataSources.keySet());
        result.putAll(createDataSources(modifiedDataSources));
        result.putAll(createDataSources(getAddedDataSources(addedDataSources)));
        return result;
    }
    
    private synchronized Map<String, DataSourceParameter> getAddedDataSources(final Map<String, DataSourceParameter> dataSourceParameters) {
        return Maps.filterEntries(dataSourceParameters, input -> !getDataSourceParameters().containsKey(input.getKey()));
    }
}
