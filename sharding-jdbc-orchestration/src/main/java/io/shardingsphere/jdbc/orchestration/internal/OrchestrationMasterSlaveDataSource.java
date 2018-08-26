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

package io.shardingsphere.jdbc.orchestration.internal;

import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.api.ConfigMapContext;
import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.event.orche.config.MasterSlaveConfigurationEventBusEvent;
import io.shardingsphere.core.event.orche.state.CircuitStateEventBusEvent;
import io.shardingsphere.core.event.orche.state.DisabledStateEventBusEvent;
import io.shardingsphere.core.jdbc.adapter.AbstractDataSourceAdapter;
import io.shardingsphere.core.jdbc.core.connection.MasterSlaveConnection;
import io.shardingsphere.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingsphere.core.orche.datasource.CircuitBreakerDataSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration master-slave datasource.
 *
 * @author caohao
 * @author panjuan
 */
@Slf4j
public final class OrchestrationMasterSlaveDataSource extends AbstractDataSourceAdapter implements AutoCloseable {
    
    private final OrchestrationFacade orchestrationFacade;
    
    @Getter
    private MasterSlaveDataSource dataSource;
    
    private Collection<String> disabledDataSourceNames = new LinkedList<>();
    
    private boolean isCircuitBreak;
    
    public OrchestrationMasterSlaveDataSource(final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig,
                                              final Map<String, Object> configMap, final Properties props, final OrchestrationFacade orchestrationFacade) throws SQLException {
        super(getAllDataSources(dataSourceMap, masterSlaveRuleConfig.getMasterDataSourceName(), masterSlaveRuleConfig.getSlaveDataSourceNames()));
        this.dataSource = new MasterSlaveDataSource(dataSourceMap, masterSlaveRuleConfig, configMap, props);
        this.orchestrationFacade = orchestrationFacade;
    }
    
    private static Collection<DataSource> getAllDataSources(final Map<String, DataSource> dataSourceMap, final String masterDataSourceName, final Collection<String> slaveDataSourceNames) {
        Collection<DataSource> result = new LinkedList<>();
        result.add(dataSourceMap.get(masterDataSourceName));
        for (String each : slaveDataSourceNames) {
            result.add(dataSourceMap.get(each));
        }
        return result;
    }
    
    /**
     * Initialize for master-slave orchestration.
     */
    public void init() {
        dataSource.close();
        orchestrationFacade.init(dataSource.getDataSourceMap(), new MasterSlaveRuleConfiguration(dataSource.getMasterSlaveRule()), ConfigMapContext.getInstance().getMasterSlaveConfig(), dataSource.getShardingProperties().getProps());
    }
    
    @Override
    public MasterSlaveConnection getConnection() {
        return dataSource.getConnection();
    }
    
    @Override
    public void close() {
        orchestrationFacade.close();
    }
    
    /**
     * Renew master-slave data source.
     *
     * @param masterSlaveEvent master slave configuration event bus event
     * @throws SQLException sql exception
     */
    @Subscribe
    public void renew(final MasterSlaveConfigurationEventBusEvent masterSlaveEvent) throws SQLException {
        this.dataSource.close();
        this.dataSource = new MasterSlaveDataSource(masterSlaveEvent.getDataSourceMap(), masterSlaveEvent.getMasterSlaveRuleConfig(), ConfigMapContext.getInstance().getMasterSlaveConfig(), masterSlaveEvent.getProps());
        MasterSlaveRuleConfiguration masterSlaveRuleConfig = masterSlaveEvent.getMasterSlaveRuleConfig();
        super.renew(getAllDataSources(masterSlaveEvent.getDataSourceMap(), masterSlaveRuleConfig.getMasterDataSourceName(), masterSlaveRuleConfig.getSlaveDataSourceNames()));
    }
    
    /**
     * Get available data source map.
     *
     * @return available data source map
     */
    public Map<String, DataSource> getDataSourceMap() {
        if (isCircuitBreak) {
            return getCircuitBreakerDataSourceMap();
        }
        
        if (!disabledDataSourceNames.isEmpty()) {
            return getAvailableDataSourceMap();
        }
        return dataSource.getDataSourceMap();
    }
    
    private Map<String, DataSource> getAvailableDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSource.getDataSourceMap());
        for (String each : disabledDataSourceNames) {
            result.remove(each);
        }
        return result;
    }
    
    private Map<String, DataSource> getCircuitBreakerDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>();
        for (String each : dataSource.getDataSourceMap().keySet()) {
            result.put(each, new CircuitBreakerDataSource());
        }
        return result;
    }
    
    /**
     * Renew disable dataSource names.
     *
     * @param disabledStateEventBusEvent jdbc disabled event bus event
     */
    @Subscribe
    public void renewDisabledDataSourceNames(final DisabledStateEventBusEvent disabledStateEventBusEvent) {
        disabledDataSourceNames = disabledStateEventBusEvent.getDisabledDataSourceNames();
    }
    
    /**
     * Renew circuit breaker dataSource names.
     *
     * @param circuitStateEventBusEvent jdbc circuit event bus event
     */
    @Subscribe
    public void renewCircuitBreakerDataSourceNames(final CircuitStateEventBusEvent circuitStateEventBusEvent) {
        isCircuitBreak = circuitStateEventBusEvent.isCircuitBreak();
    }
}
