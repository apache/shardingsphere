/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.internal;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.config.ConfigurationService;
import io.shardingjdbc.orchestration.internal.listener.ListenerFactory;
import io.shardingjdbc.orchestration.internal.state.datasource.DataSourceService;
import io.shardingjdbc.orchestration.internal.state.instance.InstanceStateService;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Orchestration service facade.
 *
 * @author zhangliang
 * @author caohao
 */
public final class OrchestrationFacade {
    
    private final OrchestrationConfiguration config;
    
    private final ConfigurationService configurationService;
    
    private final InstanceStateService instanceStateService;
    
    private final DataSourceService dataSourceService;
    
    private final ListenerFactory listenerManager;
    
    public OrchestrationFacade(final OrchestrationConfiguration config) {
        this.config = config;
        configurationService = new ConfigurationService(config);
        instanceStateService = new InstanceStateService(config);
        dataSourceService = new DataSourceService(config);
        listenerManager = new ListenerFactory(config);
    }
    
    /**
     * Initial all orchestration actions for sharding data source.
     * 
     * @param dataSourceMap data source map
     * @param shardingRuleConfig sharding rule configuration
     * @param configMap config map
     * @param props sharding properties
     * @param shardingDataSource sharding datasource
     * @throws SQLException SQL exception
     */
    public void initShardingOrchestration(
            final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig, final Map<String, Object> configMap, 
            final Properties props, final ShardingDataSource shardingDataSource) throws SQLException {
        config.getRegistryCenter().init();
        if (shardingRuleConfig.getMasterSlaveRuleConfigs().isEmpty()) {
            reviseShardingRuleConfigurationForMasterSlave(dataSourceMap, shardingRuleConfig);
        }
        configurationService.persistShardingConfiguration(getActualDataSourceMapForMasterSlave(dataSourceMap), shardingRuleConfig, configMap, props);
        instanceStateService.persistShardingInstanceOnline();
        dataSourceService.persistDataSourcesNode();
        listenerManager.initShardingListeners(shardingDataSource);
        if (dataSourceService.hasDisabledDataSource()) {
            shardingDataSource.renew(dataSourceService.getAvailableShardingRule(), props);
        }
    }
    
    private void reviseShardingRuleConfigurationForMasterSlave(final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            if (entry.getValue() instanceof MasterSlaveDataSource) {
                MasterSlaveDataSource masterSlaveDataSource = (MasterSlaveDataSource) entry.getValue();
                shardingRuleConfig.getMasterSlaveRuleConfigs().add(getMasterSlaveRuleConfiguration(masterSlaveDataSource));
            }
        }
    }
    
    private Map<String, DataSource> getActualDataSourceMapForMasterSlave(final Map<String, DataSource> dataSourceMap) {
        Map<String, DataSource> result = new HashMap<>();
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            if (entry.getValue() instanceof MasterSlaveDataSource) {
                MasterSlaveDataSource masterSlaveDataSource = (MasterSlaveDataSource) entry.getValue();
                result.putAll(masterSlaveDataSource.getAllDataSources());
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private MasterSlaveRuleConfiguration getMasterSlaveRuleConfiguration(final MasterSlaveDataSource masterSlaveDataSource) {
        MasterSlaveRuleConfiguration result = new MasterSlaveRuleConfiguration();
        result.setName(masterSlaveDataSource.getMasterSlaveRule().getName());
        result.setMasterDataSourceName(masterSlaveDataSource.getMasterSlaveRule().getMasterDataSourceName());
        result.setSlaveDataSourceNames(masterSlaveDataSource.getMasterSlaveRule().getSlaveDataSourceMap().keySet());
        result.setLoadBalanceAlgorithmClassName(masterSlaveDataSource.getMasterSlaveRule().getStrategy().getClass().getName());
        return result;
    }
    
    /**
     * Initial all orchestration actions for master-slave data source.
     * 
     * @param dataSourceMap data source map
     * @param masterSlaveRuleConfig sharding rule configuration
     * @param masterSlaveDataSource master-slave datasource
     * @param configMap config map
     */
    public void initMasterSlaveOrchestration(
            final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig, 
            final MasterSlaveDataSource masterSlaveDataSource, final Map<String, Object> configMap) {
        config.getRegistryCenter().init();
        configurationService.persistMasterSlaveConfiguration(dataSourceMap, masterSlaveRuleConfig, configMap);
        instanceStateService.persistMasterSlaveInstanceOnline();
        dataSourceService.persistDataSourcesNode();
        listenerManager.initMasterSlaveListeners(masterSlaveDataSource);
        if (dataSourceService.hasDisabledDataSource()) {
            masterSlaveDataSource.renew(dataSourceService.getAvailableMasterSlaveRule());
        }
    }
}
