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
import lombok.Getter;
import org.apache.shardingsphere.driver.jdbc.adapter.executor.ForceExecuteTemplate;
import org.apache.shardingsphere.driver.jdbc.adapter.invocation.MethodInvocationRecorder;
import org.apache.shardingsphere.driver.jdbc.core.ShardingSphereSavepoint;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.exception.OverallConnectionNotEnoughException;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DatabaseConnectionManager;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.metadata.persist.MetaDataBasedPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.transaction.ConnectionSavepointManager;
import org.apache.shardingsphere.transaction.ConnectionTransaction;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Database connection manager of ShardingSphere-JDBC.
 */
public final class DriverDatabaseConnectionManager implements DatabaseConnectionManager<Connection>, AutoCloseable {
    
    private final Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
    
    private final Map<String, DataSource> physicalDataSourceMap = new LinkedHashMap<>();
    
    @Getter
    private final ConnectionTransaction connectionTransaction;
    
    private final Multimap<String, Connection> cachedConnections = LinkedHashMultimap.create();
    
    private final MethodInvocationRecorder<Connection> methodInvocationRecorder = new MethodInvocationRecorder<>();
    
    private final ForceExecuteTemplate<Connection> forceExecuteTemplate = new ForceExecuteTemplate<>();
    
    private final Random random = new SecureRandom();
    
    @Getter
    private final ConnectionContext connectionContext;
    
    public DriverDatabaseConnectionManager(final String databaseName, final ContextManager contextManager) {
        dataSourceMap.putAll(contextManager.getDataSourceMap(databaseName));
        dataSourceMap.putAll(getTrafficDataSourceMap(databaseName, contextManager));
        physicalDataSourceMap.putAll(contextManager.getDataSourceMap(databaseName));
        connectionTransaction = createConnectionTransaction(databaseName, contextManager);
        connectionContext = new ConnectionContext(cachedConnections::keySet);
    }
    
    private Map<String, DataSource> getTrafficDataSourceMap(final String databaseName, final ContextManager contextManager) {
        TrafficRule rule = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TrafficRule.class);
        if (rule.getStrategyRules().isEmpty()) {
            return Collections.emptyMap();
        }
        MetaDataBasedPersistService persistService = contextManager.getMetaDataContexts().getPersistService();
        String actualDatabaseName = contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getName();
        Map<String, DataSourceProperties> dataSourcePropsMap = persistService.getDataSourceUnitService().load(actualDatabaseName);
        Preconditions.checkState(!dataSourcePropsMap.isEmpty(), "Can not get data source properties from meta data.");
        DataSourceProperties dataSourcePropsSample = dataSourcePropsMap.values().iterator().next();
        Collection<ShardingSphereUser> users = persistService.getGlobalRuleService().loadUsers();
        Collection<InstanceMetaData> instances = contextManager.getInstanceContext().getAllClusterInstances(InstanceType.PROXY, rule.getLabels());
        return DataSourcePoolCreator.create(createDataSourcePropertiesMap(instances, users, dataSourcePropsSample, actualDatabaseName));
    }
    
    private Map<String, DataSourceProperties> createDataSourcePropertiesMap(final Collection<InstanceMetaData> instances, final Collection<ShardingSphereUser> users,
                                                                            final DataSourceProperties dataSourcePropsSample, final String schema) {
        Map<String, DataSourceProperties> result = new LinkedHashMap<>();
        for (InstanceMetaData each : instances) {
            result.put(each.getId(), createDataSourceProperties((ProxyInstanceMetaData) each, users, dataSourcePropsSample, schema));
        }
        return result;
    }
    
    private DataSourceProperties createDataSourceProperties(final ProxyInstanceMetaData instanceMetaData, final Collection<ShardingSphereUser> users,
                                                            final DataSourceProperties dataSourcePropsSample, final String schema) {
        Map<String, Object> props = dataSourcePropsSample.getAllLocalProperties();
        props.put("jdbcUrl", createJdbcUrl(instanceMetaData, schema, props));
        ShardingSphereUser user = users.iterator().next();
        props.put("username", user.getGrantee().getUsername());
        props.put("password", user.getPassword());
        return new DataSourceProperties("com.zaxxer.hikari.HikariDataSource", props);
    }
    
    private String createJdbcUrl(final ProxyInstanceMetaData instanceMetaData, final String schema, final Map<String, Object> props) {
        String jdbcUrl = String.valueOf(props.get("jdbcUrl"));
        String jdbcUrlPrefix = jdbcUrl.substring(0, jdbcUrl.indexOf("//"));
        String jdbcUrlSuffix = jdbcUrl.contains("?") ? jdbcUrl.substring(jdbcUrl.indexOf('?')) : "";
        return String.format("%s//%s:%s/%s%s", jdbcUrlPrefix, instanceMetaData.getIp(), instanceMetaData.getPort(), schema, jdbcUrlSuffix);
    }
    
    private ConnectionTransaction createConnectionTransaction(final String databaseName, final ContextManager contextManager) {
        TransactionRule rule = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class);
        return new ConnectionTransaction(databaseName, rule);
    }
    
    /**
     * Set auto commit.
     *
     * @param autoCommit auto commit
     * @throws SQLException SQL exception
     */
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        methodInvocationRecorder.record("setAutoCommit", target -> target.setAutoCommit(autoCommit));
        forceExecuteTemplate.execute(cachedConnections.values(), connection -> connection.setAutoCommit(autoCommit));
    }
    
    /**
     * Commit.
     *
     * @throws SQLException SQL exception
     */
    public void commit() throws SQLException {
        try {
            if (connectionTransaction.isLocalTransaction() && connectionTransaction.isRollbackOnly()) {
                forceExecuteTemplate.execute(cachedConnections.values(), Connection::rollback);
            } else if (connectionTransaction.isLocalTransaction()) {
                forceExecuteTemplate.execute(cachedConnections.values(), Connection::commit);
            } else {
                connectionTransaction.commit();
            }
        } finally {
            for (Connection each : cachedConnections.values()) {
                ConnectionSavepointManager.getInstance().transactionFinished(each);
            }
        }
    }
    
    /**
     * Rollback.
     *
     * @throws SQLException SQL exception
     */
    public void rollback() throws SQLException {
        try {
            if (connectionTransaction.isLocalTransaction()) {
                forceExecuteTemplate.execute(cachedConnections.values(), Connection::rollback);
            } else {
                connectionTransaction.rollback();
            }
        } finally {
            for (Connection each : cachedConnections.values()) {
                ConnectionSavepointManager.getInstance().transactionFinished(each);
            }
        }
    }
    
    /**
     * Rollback to savepoint.
     *
     * @param savepoint savepoint
     * @throws SQLException SQL exception
     */
    public void rollback(final Savepoint savepoint) throws SQLException {
        for (Connection each : cachedConnections.values()) {
            ConnectionSavepointManager.getInstance().rollbackToSavepoint(each, savepoint.getSavepointName());
        }
    }
    
    /**
     * Set savepoint.
     *
     * @param savepointName savepoint name
     * @return savepoint savepoint
     * @throws SQLException SQL exception
     */
    public Savepoint setSavepoint(final String savepointName) throws SQLException {
        ShardingSphereSavepoint result = new ShardingSphereSavepoint(savepointName);
        for (Connection each : cachedConnections.values()) {
            ConnectionSavepointManager.getInstance().setSavepoint(each, savepointName);
        }
        methodInvocationRecorder.record("setSavepoint", target -> ConnectionSavepointManager.getInstance().setSavepoint(target, savepointName));
        return result;
    }
    
    /**
     * Set savepoint.
     *
     * @return savepoint savepoint
     * @throws SQLException SQL exception
     */
    public Savepoint setSavepoint() throws SQLException {
        ShardingSphereSavepoint result = new ShardingSphereSavepoint();
        for (Connection each : cachedConnections.values()) {
            ConnectionSavepointManager.getInstance().setSavepoint(each, result.getSavepointName());
        }
        methodInvocationRecorder.record("setSavepoint", target -> ConnectionSavepointManager.getInstance().setSavepoint(target, result.getSavepointName()));
        return result;
    }
    
    /**
     * Release savepoint.
     *
     * @param savepoint savepoint
     * @throws SQLException SQL exception
     */
    public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
        for (Connection each : cachedConnections.values()) {
            ConnectionSavepointManager.getInstance().releaseSavepoint(each, savepoint.getSavepointName());
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
        methodInvocationRecorder.record("setTransactionIsolation", connection -> connection.setTransactionIsolation(level));
        forceExecuteTemplate.execute(cachedConnections.values(), connection -> connection.setTransactionIsolation(level));
    }
    
    /**
     * Set read only.
     *
     * @param readOnly read only
     * @throws SQLException SQL exception
     */
    public void setReadOnly(final boolean readOnly) throws SQLException {
        methodInvocationRecorder.record("setReadOnly", connection -> connection.setReadOnly(readOnly));
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
        return getConnections(getRandomPhysicalDataSourceName(), 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
    }
    
    @Override
    public List<Connection> getConnections(final String dataSourceName, final int connectionOffset, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        DataSource dataSource = dataSourceMap.get(dataSourceName);
        Preconditions.checkNotNull(dataSource, "Missing the data source name: '%s'", dataSourceName);
        Collection<Connection> connections;
        synchronized (cachedConnections) {
            connections = cachedConnections.get(dataSourceName);
        }
        List<Connection> result;
        int maxConnectionSize = connectionOffset + connectionSize;
        if (connections.size() >= maxConnectionSize) {
            result = new ArrayList<>(connections).subList(connectionOffset, maxConnectionSize);
        } else if (connections.isEmpty()) {
            Collection<Connection> newConnections = createConnections(dataSourceName, dataSource, maxConnectionSize, connectionMode);
            result = new ArrayList<>(newConnections).subList(connectionOffset, maxConnectionSize);
            synchronized (cachedConnections) {
                cachedConnections.putAll(dataSourceName, newConnections);
            }
        } else {
            List<Connection> allConnections = new ArrayList<>(maxConnectionSize);
            allConnections.addAll(connections);
            Collection<Connection> newConnections = createConnections(dataSourceName, dataSource, maxConnectionSize - connections.size(), connectionMode);
            allConnections.addAll(newConnections);
            result = allConnections.subList(connectionOffset, maxConnectionSize);
            synchronized (cachedConnections) {
                cachedConnections.putAll(dataSourceName, newConnections);
            }
        }
        return result;
    }
    
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private List<Connection> createConnections(final String dataSourceName, final DataSource dataSource, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        if (1 == connectionSize) {
            Connection connection = createConnection(dataSourceName, dataSource, connectionContext.getTransactionContext());
            methodInvocationRecorder.replay(connection);
            return Collections.singletonList(connection);
        }
        if (ConnectionMode.CONNECTION_STRICTLY == connectionMode) {
            return createConnections(dataSourceName, dataSource, connectionSize, connectionContext.getTransactionContext());
        }
        synchronized (dataSource) {
            return createConnections(dataSourceName, dataSource, connectionSize, connectionContext.getTransactionContext());
        }
    }
    
    private List<Connection> createConnections(final String dataSourceName, final DataSource dataSource, final int connectionSize,
                                               final TransactionConnectionContext transactionConnectionContext) throws SQLException {
        List<Connection> result = new ArrayList<>(connectionSize);
        for (int i = 0; i < connectionSize; i++) {
            try {
                Connection connection = createConnection(dataSourceName, dataSource, transactionConnectionContext);
                methodInvocationRecorder.replay(connection);
                result.add(connection);
            } catch (final SQLException ignored) {
                for (Connection each : result) {
                    each.close();
                }
                throw new OverallConnectionNotEnoughException(connectionSize, result.size()).toSQLException();
            }
        }
        return result;
    }
    
    private Connection createConnection(final String dataSourceName, final DataSource dataSource, final TransactionConnectionContext transactionConnectionContext) throws SQLException {
        Optional<Connection> connectionInTransaction = isRawJdbcDataSource(dataSourceName) ? connectionTransaction.getConnection(dataSourceName, transactionConnectionContext) : Optional.empty();
        return connectionInTransaction.isPresent() ? connectionInTransaction.get() : dataSource.getConnection();
    }
    
    private boolean isRawJdbcDataSource(final String dataSourceName) {
        return physicalDataSourceMap.containsKey(dataSourceName);
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
