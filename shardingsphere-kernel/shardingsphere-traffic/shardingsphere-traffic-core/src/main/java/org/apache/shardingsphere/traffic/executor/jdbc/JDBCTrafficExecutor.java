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

package org.apache.shardingsphere.traffic.executor.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.pool.creator.DataSourcePoolCreatorUtil;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceType;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.traffic.executor.TrafficExecutor;
import org.apache.shardingsphere.traffic.executor.TrafficExecutorCallback;
import org.apache.shardingsphere.traffic.executor.context.TrafficExecutorContext;
import org.apache.shardingsphere.traffic.executor.context.builder.TrafficExecutorContextBuilder;
import org.apache.shardingsphere.traffic.rule.TrafficRule;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDBC traffic executor.
 */
public final class JDBCTrafficExecutor implements TrafficExecutor {
    
    private static final Map<String, TrafficExecutorContextBuilder<?>> TYPE_CONTEXT_BUILDERS = new ConcurrentHashMap<>();
    
    private static final String JDBC_URL = "jdbcUrl";
    
    private static final String USER_NAME = "username";
    
    private static final String PASSWORD = "password";
    
    private final Map<String, DataSource> dataSources = new LinkedHashMap<>();
    
    private Statement statement;
    
    static {
        ShardingSphereServiceLoader.register(TrafficExecutorContextBuilder.class);
    }
    
    public JDBCTrafficExecutor(final String schema, final MetaDataContexts metaDataContexts) {
        Optional<TrafficRule> trafficRule = metaDataContexts.getGlobalRuleMetaData().findSingleRule(TrafficRule.class);
        if (trafficRule.isPresent() && metaDataContexts.getMetaDataPersistService().isPresent()) {
            Map<String, DataSourceConfiguration> dataSourceConfigs = metaDataContexts.getMetaDataPersistService().get().getDataSourceService().load(schema);
            if (dataSourceConfigs.isEmpty()) {
                throw new ShardingSphereException("Can not get dataSource configs from meta data.");
            }
            DataSourceConfiguration dataSourceConfigSample = dataSourceConfigs.values().iterator().next();
            Collection<ComputeNodeInstance> instances = metaDataContexts.getMetaDataPersistService().get().loadComputeNodeInstances(InstanceType.PROXY, trafficRule.get().getLabels());
            dataSources.putAll(DataSourcePoolCreatorUtil.getDataSourceMap(createDataSourceConfigs(instances, dataSourceConfigSample, schema)));
        }
    }
    
    private Map<String, DataSourceConfiguration> createDataSourceConfigs(final Collection<ComputeNodeInstance> instances, 
                                                                         final DataSourceConfiguration dataSourceConfigSample, final String schema) {
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>();
        for (ComputeNodeInstance each : instances) {
            result.put(each.getIp() + "@" + each.getPort(), createDataSourceConfig(each, dataSourceConfigSample, schema));
        }
        return result;
    }
    
    private DataSourceConfiguration createDataSourceConfig(final ComputeNodeInstance instance, 
                                                           final DataSourceConfiguration dataSourceConfigSample, final String schema) {
        Map<String, Object> props = dataSourceConfigSample.getProps();
        props.put(JDBC_URL, createJdbcUrl(instance, schema, props));
        if (instance.getUsers().isEmpty()) {
            throw new ShardingSphereException("Can not get users from meta data.");
        }
        ShardingSphereUser user = instance.getUsers().iterator().next();
        props.put(USER_NAME, user.getGrantee().getUsername());
        props.put(PASSWORD, user.getPassword());
        DataSourceConfiguration result = new DataSourceConfiguration(HikariDataSource.class.getName());
        result.getProps().putAll(props);
        return result;
    }
    
    private String createJdbcUrl(final ComputeNodeInstance instance, final String schema, final Map<String, Object> props) {
        String jdbcUrl = String.valueOf(props.get(JDBC_URL));
        String username = String.valueOf(props.get(USER_NAME));
        DataSourceMetaData dataSourceMetaData = DatabaseTypeRegistry.getDatabaseTypeByURL(jdbcUrl).getDataSourceMetaData(jdbcUrl, username);
        return jdbcUrl.replace(dataSourceMetaData.getHostname(), instance.getIp())
                .replace(String.valueOf(dataSourceMetaData.getPort()), instance.getPort()).replace(dataSourceMetaData.getCatalog(), schema);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public TrafficExecutorContext<Statement> prepare(final LogicSQL logicSQL, final String dataSourceName, final String type) throws SQLException {
        if (!dataSources.containsKey(dataSourceName)) {
            throw new ShardingSphereException("Can not get dataSource of %.", dataSourceName);
        }
        DataSource dataSource = dataSources.get(dataSourceName);
        TrafficExecutorContextBuilder builder = getCachedTrafficExecutorContextBuilder(type);
        return builder.build(logicSQL, dataSource.getConnection());
    }
    
    private TrafficExecutorContextBuilder<?> getCachedTrafficExecutorContextBuilder(final String type) {
        TrafficExecutorContextBuilder<?> result;
        if (null == (result = TYPE_CONTEXT_BUILDERS.get(type))) {
            result = TYPE_CONTEXT_BUILDERS.computeIfAbsent(type, key -> TypedSPIRegistry.getRegisteredService(TrafficExecutorContextBuilder.class, key, new Properties()));
        }
        return result;
    }
    
    @Override
    public ResultSet executeQuery(final LogicSQL logicSQL, final TrafficExecutorContext<Statement> context, final TrafficExecutorCallback<ResultSet> callback) throws SQLException {
        cacheStatement(logicSQL.getParameters(), context.getStatement());
        return callback.execute(statement, logicSQL.getSql());
    }
    
    private void cacheStatement(final List<Object> parameters, final Statement statement) throws SQLException {
        this.statement = statement;
        setParameters(statement, parameters);
    }
    
    private void setParameters(final Statement statement, final List<Object> parameters) throws SQLException {
        if (statement instanceof PreparedStatement) {
            int index = 1;
            for (Object each : parameters) {
                ((PreparedStatement) statement).setObject(index++, each);
            }
        }
    }
    
    @Override
    public int executeUpdate(final LogicSQL logicSQL, final TrafficExecutorContext<Statement> context, final TrafficExecutorCallback<Integer> callback) throws SQLException {
        cacheStatement(logicSQL.getParameters(), context.getStatement());
        return callback.execute(statement, logicSQL.getSql());
    }
    
    @Override
    public boolean execute(final LogicSQL logicSQL, final TrafficExecutorContext<Statement> context, final TrafficExecutorCallback<Boolean> callback) throws SQLException {
        cacheStatement(logicSQL.getParameters(), context.getStatement());
        return callback.execute(statement, logicSQL.getSql());
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        return statement.getResultSet();
    }
    
    @Override
    public void close() throws SQLException {
        if (null != statement) {
            Connection connection = statement.getConnection();
            statement.close();
            connection.close();
        }
    }
}
