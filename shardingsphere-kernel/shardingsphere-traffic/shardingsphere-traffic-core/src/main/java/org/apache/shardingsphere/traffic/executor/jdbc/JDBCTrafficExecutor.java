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

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.traffic.executor.TrafficExecutor;
import org.apache.shardingsphere.traffic.executor.TrafficExecutorCallback;
import org.apache.shardingsphere.traffic.executor.context.builder.TrafficExecutorContextBuilder;
import org.apache.shardingsphere.traffic.executor.context.TrafficExecutorContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDBC traffic executor.
 */
public final class JDBCTrafficExecutor implements TrafficExecutor {
    
    @SuppressWarnings("rawtypes")
    private static final Map<String, TrafficExecutorContextBuilder> TYPE_TO_BUILDER_MAP = new ConcurrentHashMap<>(8, 1);
    
    private final Map<String, DataSource> dataSources = new LinkedHashMap<>();
    
    private Statement statement;
    
    static {
        ShardingSphereServiceLoader.register(TrafficExecutorContextBuilder.class);
    }
    
    public JDBCTrafficExecutor(final MetaDataContexts metaDataContexts) {
        dataSources.putAll(DataSourceConverter.getDataSourceMap(createDataSourceConfigs(metaDataContexts)));
    }
    
    private Map<String, DataSourceConfiguration> createDataSourceConfigs(final MetaDataContexts metaDataContexts) {
        // TODO Use governance API to create data source configuration
        return Collections.emptyMap(); 
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public TrafficExecutorContext<Statement> prepare(final LogicSQL logicSQL, final String dataSourceName, final String type) throws SQLException {
        if (!dataSources.containsKey(dataSourceName)) {
            throw new ShardingSphereException("Can not get dataSource by %.", dataSourceName);
        }
        DataSource dataSource = dataSources.get(dataSourceName);
        TrafficExecutorContextBuilder builder = getCachedTrafficExecutorContextBuilder(type);
        return builder.build(logicSQL, dataSource.getConnection());
    }
    
    @SuppressWarnings("rawtypes")
    private TrafficExecutorContextBuilder getCachedTrafficExecutorContextBuilder(final String type) {
        TrafficExecutorContextBuilder result;
        if (null == (result = TYPE_TO_BUILDER_MAP.get(type))) {
            result = TYPE_TO_BUILDER_MAP.computeIfAbsent(type, key -> TypedSPIRegistry.getRegisteredService(TrafficExecutorContextBuilder.class, key, new Properties()));
        }
        return result;
    }
    
    @Override
    public ResultSet executeQuery(final LogicSQL logicSQL, final TrafficExecutorContext<Statement> context, 
                                  final TrafficExecutorCallback<ResultSet> callback) throws SQLException {
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
