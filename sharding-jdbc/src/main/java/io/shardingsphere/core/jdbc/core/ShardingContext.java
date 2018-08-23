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
import io.shardingsphere.core.jdbc.metadata.JDBCTableMetaDataConnectionManager;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.orche.datasource.CircuitBreakerDataSource;
import io.shardingsphere.core.orche.eventbus.state.CircuitStateEventBusEvent;
import io.shardingsphere.core.orche.eventbus.state.disabled.DisabledStateEventBusEvent;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.Getter;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
public final class ShardingContext implements AutoCloseable {
    
    private Map<String, DataSource> dataSourceMap;
    
    private ShardingRule shardingRule;
    
    private DatabaseType databaseType;
    
    private ShardingExecuteEngine executeEngine;
    
    private ShardingMetaData metaData;
    
    private ConnectionMode connectionMode;
    
    private boolean showSQL;
    
    private Collection<String> disabledDataSourceNames = new LinkedList<>();
    
    private Collection<String> circuitBreakerDataSourceNames = new LinkedList<>();
    
    public ShardingContext(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule, final DatabaseType databaseType, final ShardingExecuteEngine executeEngine,
                           final ConnectionMode connectionMode, final boolean showSQL) {
        init(dataSourceMap, shardingRule, databaseType, executeEngine, connectionMode, showSQL);
    }
    
    private void init(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule, final DatabaseType databaseType, final ShardingExecuteEngine executeEngine,
                      final ConnectionMode connectionMode, final boolean showSQL) {
        this.dataSourceMap = dataSourceMap;
        this.shardingRule = shardingRule;
        this.executeEngine = executeEngine;
        this.databaseType = databaseType;
        this.connectionMode = connectionMode;
        this.showSQL = showSQL;
        metaData = new ShardingMetaData(
                getDataSourceURLs(getDataSourceMap()), shardingRule, getDatabaseType(), executeEngine, new JDBCTableMetaDataConnectionManager(getDataSourceMap()));
    }
    
    /**
     * Renew sharding context.
     *
     * @param dataSourceMap data source map
     * @param shardingRule sharding rule
     * @param databaseType data type
     * @param executeEngine sharding executor engine
     * @param connectionMode connection mode
     * @param showSQL show sql
     */
    public void renew(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule, final DatabaseType databaseType, final ShardingExecuteEngine executeEngine,
                      final ConnectionMode connectionMode, final boolean showSQL) {
        close();
        init(dataSourceMap, shardingRule, databaseType, executeEngine, connectionMode, showSQL);
    }
    
    /**
     * Renew disable dataSource names.
     *
     * @param disabledStateEventBusEvent jdbc disabled event bus event
     */
    @Subscribe
    public void renewDisabledDataSourceNames(final DisabledStateEventBusEvent disabledStateEventBusEvent) {
        disabledDataSourceNames = disabledStateEventBusEvent.getDisabledDataSourceNames();
        metaData = new ShardingMetaData(
                getDataSourceURLs(getDataSourceMap()), shardingRule, getDatabaseType(), executeEngine, new JDBCTableMetaDataConnectionManager(getDataSourceMap()));
    }
    
    /**
     * Renew circuit breaker dataSource names.
     *
     * @param circuitStateEventBusEvent jdbc disabled event bus event
     */
    @Subscribe
    public void renewCircuitBreakerDataSourceNames(final CircuitStateEventBusEvent circuitStateEventBusEvent) {
        circuitBreakerDataSourceNames = circuitStateEventBusEvent.getCircuitBreakerDataSourceNames();
        metaData = new ShardingMetaData(
                getDataSourceURLs(getDataSourceMap()), shardingRule, getDatabaseType(), executeEngine, new JDBCTableMetaDataConnectionManager(getDataSourceMap()));
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
    
    @Override
    public void close() {
        closeOriginalDataSources();
        executeEngine.close();
    }
    
    private void closeOriginalDataSources() {
        for (DataSource each : dataSourceMap.values()) {
            try {
                Method method = each.getClass().getDeclaredMethod("close");
                method.invoke(each);
            } catch (final NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
            }
        }
    }
}
