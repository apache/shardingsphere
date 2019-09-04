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

package org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.api.config.RuleConfiguration;
import org.apache.shardingsphere.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.core.constant.ShardingConstant;
import org.apache.shardingsphere.orchestration.internal.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.internal.registry.ShardingOrchestrationFacade;
import org.apache.shardingsphere.orchestration.internal.registry.state.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractDataSourceAdapter;
import org.apache.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.circuit.datasource.CircuitBreakerDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.util.DataSourceConverter;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Abstract orchestration data source.
 *
 * @author panjuan
 */
public abstract class AbstractOrchestrationDataSource extends AbstractUnsupportedOperationDataSource implements AutoCloseable {
    
    @Getter
    @Setter
    private PrintWriter logWriter = new PrintWriter(System.out);
    
    @Getter(AccessLevel.PROTECTED)
    private final ShardingOrchestrationFacade shardingOrchestrationFacade;
    
    private boolean isCircuitBreak;
    
    @Getter(AccessLevel.PROTECTED)
    private final Map<String, DataSourceConfiguration> dataSourceConfigurations = new LinkedHashMap<>();
    
    public AbstractOrchestrationDataSource(final ShardingOrchestrationFacade shardingOrchestrationFacade) {
        this.shardingOrchestrationFacade = shardingOrchestrationFacade;
        ShardingOrchestrationEventBus.getInstance().register(this);
    }
    
    protected abstract DataSource getDataSource();
    
    @Override
    public final Connection getConnection() throws SQLException {
        return isCircuitBreak ? new CircuitBreakerDataSource().getConnection() : getDataSource().getConnection();
    }
    
    @Override
    public final Connection getConnection(final String username, final String password) throws SQLException {
        return getConnection();
    }
    
    @Override
    public final Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
    
    @Override
    public final void close() throws Exception {
        ((AbstractDataSourceAdapter) getDataSource()).close();
        shardingOrchestrationFacade.close();
    }
    
    /**
     /**
     * Renew circuit breaker state.
     *
     * @param circuitStateChangedEvent circuit state changed event
     */
    @Subscribe
    public final synchronized void renew(final CircuitStateChangedEvent circuitStateChangedEvent) {
        isCircuitBreak = circuitStateChangedEvent.isCircuitBreak();
    }
    
    protected final void initShardingOrchestrationFacade() {
        shardingOrchestrationFacade.init();
        dataSourceConfigurations.putAll(shardingOrchestrationFacade.getConfigService().loadDataSourceConfigurations(ShardingConstant.LOGIC_SCHEMA_NAME));
    }
    
    protected final void initShardingOrchestrationFacade(
            final Map<String, Map<String, DataSourceConfiguration>> dataSourceConfigurations, final Map<String, RuleConfiguration> schemaRuleMap, final Properties props) {
        shardingOrchestrationFacade.init(dataSourceConfigurations, schemaRuleMap, null, props);
        this.dataSourceConfigurations.putAll(dataSourceConfigurations.get(ShardingConstant.LOGIC_SCHEMA_NAME));
    }
    
    protected final synchronized Map<String, DataSource> getChangedDataSources(final Map<String, DataSource> oldDataSources, final Map<String, DataSourceConfiguration> newDataSources) {
        Map<String, DataSource> result = new LinkedHashMap<>(oldDataSources);
        Map<String, DataSourceConfiguration> modifiedDataSources = getModifiedDataSources(newDataSources);
        result.keySet().removeAll(getDeletedDataSources(newDataSources));
        result.keySet().removeAll(modifiedDataSources.keySet());
        result.putAll(DataSourceConverter.getDataSourceMap(modifiedDataSources));
        result.putAll(DataSourceConverter.getDataSourceMap(getAddedDataSources(newDataSources)));
        return result;
    }
    
    protected final synchronized Map<String, DataSourceConfiguration> getModifiedDataSources(final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>();
        for (Entry<String, DataSourceConfiguration> entry : dataSourceConfigurations.entrySet()) {
            if (isModifiedDataSource(entry)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private synchronized boolean isModifiedDataSource(final Entry<String, DataSourceConfiguration> dataSourceNameAndConfig) {
        return dataSourceConfigurations.containsKey(dataSourceNameAndConfig.getKey()) && !dataSourceConfigurations.get(dataSourceNameAndConfig.getKey()).equals(dataSourceNameAndConfig.getValue());
    }
    
    protected final synchronized List<String> getDeletedDataSources(final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        List<String> result = new LinkedList<>(this.dataSourceConfigurations.keySet());
        result.removeAll(dataSourceConfigurations.keySet());
        return result;
    }
    
    private synchronized Map<String, DataSourceConfiguration> getAddedDataSources(final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        return Maps.filterEntries(dataSourceConfigurations, new Predicate<Entry<String, DataSourceConfiguration>>() {
            
            @Override
            public boolean apply(final Entry<String, DataSourceConfiguration> input) {
                return !AbstractOrchestrationDataSource.this.dataSourceConfigurations.containsKey(input.getKey());
            }
        });
    }
}
