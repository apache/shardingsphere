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

package org.apache.shardingsphere.driver.jdbc.core.connection;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.shardingsphere.driver.jdbc.adapter.executor.ForceExecuteTemplate;
import org.apache.shardingsphere.driver.jdbc.adapter.invocation.MethodInvocationRecorder;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.ExecutorJDBCManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.definition.InstanceId;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.transaction.ConnectionTransaction;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Connection manager.
 */
public final class ConnectionManager implements ExecutorJDBCManager, AutoCloseable {
    
    private final Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
    
    private final Map<String, DataSource> physicalDataSourceMap = new LinkedHashMap<>();
    
    @Getter
    private final ConnectionTransaction connectionTransaction;
    
    private final Multimap<String, Connection> cachedConnections = LinkedHashMultimap.create();
    
    private final MethodInvocationRecorder methodInvocationRecorder = new MethodInvocationRecorder();
    
    private final ForceExecuteTemplate<Connection> forceExecuteTemplate = new ForceExecuteTemplate<>();
    
    private final Random random = new SecureRandom();
    
    public ConnectionManager(final String schema, final ContextManager contextManager) {
        dataSourceMap.putAll(contextManager.getDataSourceMap(schema));
        dataSourceMap.putAll(getTrafficDataSourceMap(schema, contextManager));
        physicalDataSourceMap.putAll(contextManager.getDataSourceMap(schema));
        connectionTransaction = createConnectionTransaction(schema, contextManager);
    }
    
    private Map<String, DataSource> getTrafficDataSourceMap(final String schema, final ContextManager contextManager) {
        Optional<TrafficRule> trafficRule = contextManager.getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(TrafficRule.class);
        Optional<MetaDataPersistService> metaDataPersistService = contextManager.getMetaDataContexts().getMetaDataPersistService();
        if (!trafficRule.isPresent() || !metaDataPersistService.isPresent()) {
            return Collections.emptyMap();
        }
        Map<String, DataSourceProperties> dataSourcePropsMap = metaDataPersistService.get().getDataSourceService().load(schema);
        Preconditions.checkState(!dataSourcePropsMap.isEmpty(), "Can not get data source properties from meta data.");
        DataSourceProperties dataSourcePropsSample = dataSourcePropsMap.values().iterator().next();
        Collection<ShardingSphereUser> users = metaDataPersistService.get().getGlobalRuleService().loadUsers();
        Collection<ComputeNodeInstance> instances = metaDataPersistService.get().getComputeNodePersistService().loadComputeNodeInstances(InstanceType.PROXY, trafficRule.get().getLabels());
        return DataSourcePoolCreator.create(createDataSourcePropertiesMap(instances, users, dataSourcePropsSample, schema));
    }
    
    private Map<String, DataSourceProperties> createDataSourcePropertiesMap(final Collection<ComputeNodeInstance> instances, final Collection<ShardingSphereUser> users,
                                                                            final DataSourceProperties dataSourcePropsSample, final String schema) {
        Map<String, DataSourceProperties> result = new LinkedHashMap<>();
        for (ComputeNodeInstance each : instances) {
            result.put(each.getInstanceDefinition().getInstanceId().getId(), createDataSourceProperties(each, users, dataSourcePropsSample, schema));
        }
        return result;
    }
    
    private DataSourceProperties createDataSourceProperties(final ComputeNodeInstance instance, final Collection<ShardingSphereUser> users,
                                                            final DataSourceProperties dataSourcePropsSample, final String schema) {
        Map<String, Object> props = dataSourcePropsSample.getAllLocalProperties();
        props.put("jdbcUrl", createJdbcUrl(instance, schema, props));
        ShardingSphereUser user = users.iterator().next();
        props.put("username", user.getGrantee().getUsername());
        props.put("password", user.getPassword());
        return new DataSourceProperties(HikariDataSource.class.getName(), props);
    }
    
    private String createJdbcUrl(final ComputeNodeInstance instance, final String schema, final Map<String, Object> props) {
        String jdbcUrl = String.valueOf(props.get("jdbcUrl"));
        String username = String.valueOf(props.get("username"));
        DataSourceMetaData dataSourceMetaData = DatabaseTypeRegistry.getDatabaseTypeByURL(jdbcUrl).getDataSourceMetaData(jdbcUrl, username);
        InstanceId instanceId = instance.getInstanceDefinition().getInstanceId();
        return jdbcUrl.replace(dataSourceMetaData.getHostname(), instanceId.getIp())
                .replace(String.valueOf(dataSourceMetaData.getPort()), String.valueOf(instanceId.getUniqueSign())).replace(dataSourceMetaData.getCatalog(), schema);
    }
    
    private ConnectionTransaction createConnectionTransaction(final String schemaName, final ContextManager contextManager) {
        TransactionType type = TransactionTypeHolder.get();
        if (null == type) {
            Optional<TransactionRule> transactionRule = contextManager.getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(TransactionRule.class);
            return transactionRule.map(optional -> new ConnectionTransaction(schemaName, optional, contextManager.getTransactionContexts()))
                    .orElseGet(() -> new ConnectionTransaction(schemaName, contextManager.getTransactionContexts()));
        }
        return new ConnectionTransaction(schemaName, type, contextManager.getTransactionContexts());
    }
    
    /**
     * Set auto commit.
     * 
     * @param autoCommit auto commit
     * @throws SQLException SQL exception
     */
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        methodInvocationRecorder.record(Connection.class, "setAutoCommit", new Class[]{boolean.class}, new Object[]{autoCommit});
        forceExecuteTemplate.execute(cachedConnections.values(), connection -> connection.setAutoCommit(autoCommit));
    }
    
    /**
     * Commit.
     * 
     * @throws SQLException SQL exception
     */
    public void commit() throws SQLException {
        if (connectionTransaction.isLocalTransaction()) {
            forceExecuteTemplate.execute(cachedConnections.values(), Connection::commit);
        } else {
            connectionTransaction.commit();
        }
    }
    
    /**
     * Rollback.
     *
     * @throws SQLException SQL exception
     */
    public void rollback() throws SQLException {
        if (connectionTransaction.isLocalTransaction()) {
            forceExecuteTemplate.execute(cachedConnections.values(), Connection::rollback);
        } else {
            connectionTransaction.rollback();
        }
    }
    
    /**
     * Get transaction isolation.
     * 
     * @return transaction isolation level
     * @throws SQLException SQL exception
     */
    public Optional<Integer> getTransactionIsolation() throws SQLException {
        return cachedConnections.values().isEmpty() ? Optional.empty() : Optional.of(cachedConnections.values().iterator().next().getTransactionIsolation());
    }
    
    /**
     * Set transaction isolation.
     *
     * @param level transaction isolation level
     * @throws SQLException SQL exception
     */
    public void setTransactionIsolation(final int level) throws SQLException {
        methodInvocationRecorder.record(Connection.class, "setTransactionIsolation", new Class[]{int.class}, new Object[]{level});
        forceExecuteTemplate.execute(cachedConnections.values(), connection -> connection.setTransactionIsolation(level));
    }
    
    /**
     * Set read only.
     *
     * @param readOnly read only
     * @throws SQLException SQL exception
     */
    public void setReadOnly(final boolean readOnly) throws SQLException {
        methodInvocationRecorder.record(Connection.class, "setReadOnly", new Class[]{boolean.class}, new Object[]{readOnly});
        forceExecuteTemplate.execute(cachedConnections.values(), connection -> connection.setReadOnly(readOnly));
    }
    
    /**
     * Whether connection valid.
     * 
     * @param timeout timeout
     * @return connection valid or not
     * @throws SQLException SQL exception
     */
    public boolean isValid(final int timeout) throws SQLException {
        for (Connection each : cachedConnections.values()) {
            if (!each.isValid(timeout)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get random physical data source name.
     *
     * @return random physical data source name
     */
    public String getRandomPhysicalDataSourceName() {
        Collection<String> cachedPhysicalDataSourceNames = Sets.intersection(physicalDataSourceMap.keySet(), cachedConnections.keySet());
        Collection<String> datasourceNames = cachedPhysicalDataSourceNames.isEmpty() ? physicalDataSourceMap.keySet() : cachedPhysicalDataSourceNames;
        return new ArrayList<>(datasourceNames).get(random.nextInt(datasourceNames.size()));
    }
    
    /**
     * Get random connection.
     *
     * @return random connection
     * @throws SQLException SQL exception
     */
    public Connection getRandomConnection() throws SQLException {
        return getConnections(getRandomPhysicalDataSourceName(), 1, ConnectionMode.MEMORY_STRICTLY).get(0);
    }
    
    @Override
    public List<Connection> getConnections(final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        DataSource dataSource = dataSourceMap.get(dataSourceName);
        Preconditions.checkState(null != dataSource, "Missing the data source name: '%s'", dataSourceName);
        Collection<Connection> connections;
        synchronized (cachedConnections) {
            connections = cachedConnections.get(dataSourceName);
        }
        List<Connection> result;
        if (connections.size() >= connectionSize) {
            result = new ArrayList<>(connections).subList(0, connectionSize);
        } else if (!connections.isEmpty()) {
            result = new ArrayList<>(connectionSize);
            result.addAll(connections);
            List<Connection> newConnections = createConnections(dataSourceName, dataSource, connectionSize - connections.size(), connectionMode);
            result.addAll(newConnections);
            synchronized (cachedConnections) {
                cachedConnections.putAll(dataSourceName, newConnections);
            }
        } else {
            result = new ArrayList<>(createConnections(dataSourceName, dataSource, connectionSize, connectionMode));
            synchronized (cachedConnections) {
                cachedConnections.putAll(dataSourceName, result);
            }
        }
        return result;
    }
    
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private List<Connection> createConnections(final String dataSourceName, final DataSource dataSource, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        if (1 == connectionSize) {
            Connection connection = createConnection(dataSourceName, dataSource);
            methodInvocationRecorder.replay(connection);
            return Collections.singletonList(connection);
        }
        if (ConnectionMode.CONNECTION_STRICTLY == connectionMode) {
            return createConnections(dataSourceName, dataSource, connectionSize);
        }
        synchronized (dataSource) {
            return createConnections(dataSourceName, dataSource, connectionSize);
        }
    }
    
    private List<Connection> createConnections(final String dataSourceName, final DataSource dataSource, final int connectionSize) throws SQLException {
        List<Connection> result = new ArrayList<>(connectionSize);
        for (int i = 0; i < connectionSize; i++) {
            try {
                Connection connection = createConnection(dataSourceName, dataSource);
                methodInvocationRecorder.replay(connection);
                result.add(connection);
            } catch (final SQLException ex) {
                for (Connection each : result) {
                    each.close();
                }
                throw new SQLException(String.format("Can not get %d connections one time, partition succeed connection(%d) have released!", connectionSize, result.size()), ex);
            }
        }
        return result;
    }
    
    private Connection createConnection(final String dataSourceName, final DataSource dataSource) throws SQLException {
        Optional<Connection> connectionInTransaction = connectionTransaction.getConnection(dataSourceName);
        return connectionInTransaction.isPresent() ? connectionInTransaction.get() : dataSource.getConnection();
    }
    
    @SuppressWarnings("MagicConstant")
    @Override
    public Statement createStorageResource(final Connection connection, final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        return connection.createStatement(option.getResultSetType(), option.getResultSetConcurrency(), option.getResultSetHoldability());
    }
    
    @SuppressWarnings("MagicConstant")
    @Override
    public PreparedStatement createStorageResource(final String sql, final List<Object> parameters,
                                                   final Connection connection, final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        return option.isReturnGeneratedKeys() ? (ArrayUtils.isNotEmpty(option.getColumns()) ? connection.prepareStatement(sql, option.getColumns()) : connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
                : connection.prepareStatement(sql, option.getResultSetType(), option.getResultSetConcurrency(), option.getResultSetHoldability());
    }
    
    @Override
    public void close() throws SQLException {
        try {
            forceExecuteTemplate.execute(cachedConnections.values(), Connection::close);
        } finally {
            cachedConnections.clear();
        }
    }
}
