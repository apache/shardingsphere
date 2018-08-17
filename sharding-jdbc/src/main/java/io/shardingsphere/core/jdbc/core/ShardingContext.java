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

package io.shardingsphere.core.jdbc.core;

import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.executor.ExecutorEngine;
import io.shardingsphere.core.jdbc.metadata.JDBCTableMetaDataConnectionManager;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.jdbc.state.circuit.JdbcCircuitEventBusEvent;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.jdbc.state.disabled.JdbcDisabledEventBusEvent;
import io.shardingsphere.jdbc.orchestration.internal.jdbc.datasource.CircuitBreakerDataSource;
import lombok.Getter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Sharding runtime context.
 * 
 * @author gaohongtao
 * @author panjuan
 */
@Getter
public final class ShardingContext {
    
    private Map<String, DataSource> dataSourceMap;
    
    private ShardingRule shardingRule;
    
    private DatabaseType databaseType;
    
    private ExecutorEngine executorEngine;
    
    private ShardingMetaData metaData;
    
    private ConnectionMode connectionMode;
    
    private boolean showSQL;
    
    private Collection<String> disabledDataSourceNames = new LinkedList<>();
    
    private Collection<String> circuitBreakerDataSourceNames = new LinkedList<>();
    
    public ShardingContext(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule, final DatabaseType databaseType, final ExecutorEngine executorEngine,
                           final ConnectionMode connectionMode, final boolean showSQL) {
        init(dataSourceMap, shardingRule, databaseType, executorEngine, connectionMode, showSQL);
    }
    
    private void init(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule, final DatabaseType databaseType, final ExecutorEngine executorEngine,
                      final ConnectionMode connectionMode, final boolean showSQL) {
        this.dataSourceMap = dataSourceMap;
        this.shardingRule = shardingRule;
        this.executorEngine = executorEngine;
        this.databaseType = databaseType;
        this.connectionMode = connectionMode;
        this.showSQL = showSQL;
        metaData = new ShardingMetaData(
                getDataSourceURLs(getDataSourceMap()), shardingRule, getDatabaseType(), executorEngine.getExecutorService(), new JDBCTableMetaDataConnectionManager(getDataSourceMap()));
    }
    
    /**
     * Renew sharding context.
     *
     * @param dataSourceMap data source map
     * @param shardingRule sharding rule
     * @param databaseType data type
     * @param executorEngine executor engine
     * @param connectionMode connection mode
     * @param showSQL show sql
     */
    @Subscribe
    public void renew(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule, final DatabaseType databaseType, final ExecutorEngine executorEngine,
                      final ConnectionMode connectionMode, final boolean showSQL) {
        init(dataSourceMap, shardingRule, databaseType, executorEngine, connectionMode, showSQL);
    }
    
    /**
     * Renew disable dataSource names.
     *
     * @param jdbcDisabledEventBusEvent jdbc disabled event bus event
     */
    @Subscribe
    public void renewDisabledDataSourceNames(final JdbcDisabledEventBusEvent jdbcDisabledEventBusEvent) {
        disabledDataSourceNames = jdbcDisabledEventBusEvent.getDisabledDataSourceNames();
        metaData = new ShardingMetaData(
                getDataSourceURLs(getDataSourceMap()), shardingRule, getDatabaseType(), executorEngine.getExecutorService(), new JDBCTableMetaDataConnectionManager(getDataSourceMap()));
    }
    
    /**
     * Renew circuit breaker dataSource names.
     *
     * @param jdbcCircuitEventBusEvent jdbc disabled event bus event
     */
    @Subscribe
    public void renewCircuitBreakerDataSourceNames(final JdbcCircuitEventBusEvent jdbcCircuitEventBusEvent) {
        circuitBreakerDataSourceNames = jdbcCircuitEventBusEvent.getCircuitBreakerDataSourceNames();
        metaData = new ShardingMetaData(
                getDataSourceURLs(getDataSourceMap()), shardingRule, getDatabaseType(), executorEngine.getExecutorService(), new JDBCTableMetaDataConnectionManager(getDataSourceMap()));
    }
    
    private static Map<String, String> getDataSourceURLs(final Map<String, DataSource> dataSourceMap) {
        Map<String, String> result = new LinkedHashMap<>(dataSourceMap.size(), 1);
        for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            result.put(entry.getKey(), getDataSourceURL(entry.getValue()));
        }
        return result;
    }
    
    private static String getDataSourceURL(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        } catch (final SQLException ex) {
            throw new ShardingException(ex);
        }
    }
    
    /**
     * Get available data source map.
     *
     * @return available data source map
     */
    public Map<String, DataSource> getDataSourceMap() {
        if (!getCircuitBreakerDataSourceNames().isEmpty()) {
            return getCircuitBreakerDataSourceMap();
        }
        
        if (!getDisabledDataSourceNames().isEmpty()) {
            return getAvailableDataSourceMap();
        }
        return dataSourceMap;
    }
    
    private Map<String, DataSource> getAvailableDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceMap);
        for (String each : getDisabledDataSourceNames()) {
            result.remove(each);
        }
        return result;
    }
    
    private Map<String, DataSource> getCircuitBreakerDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>();
        for (String each : getCircuitBreakerDataSourceNames()) {
            result.put(each, new CircuitBreakerDataSource());
        }
        return result;
    }
}
