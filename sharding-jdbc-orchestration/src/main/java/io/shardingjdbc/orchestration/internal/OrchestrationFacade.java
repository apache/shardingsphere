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

import com.google.common.base.Preconditions;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.config.ConfigurationService;
import io.shardingjdbc.orchestration.internal.listener.ListenerFactory;
import io.shardingjdbc.orchestration.internal.state.datasource.DataSourceService;
import io.shardingjdbc.orchestration.internal.state.instance.InstanceStateService;
import io.shardingjdbc.orchestration.reg.api.RegistryCenter;
import io.shardingjdbc.orchestration.reg.api.RegistryCenterConfiguration;
import io.shardingjdbc.orchestration.reg.etcd.EtcdConfiguration;
import io.shardingjdbc.orchestration.reg.etcd.EtcdRegistryCenter;
import io.shardingjdbc.orchestration.reg.zookeeper.ZookeeperConfiguration;
import io.shardingjdbc.orchestration.reg.zookeeper.ZookeeperRegistryCenter;

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
    
    private final boolean isOverwrite;
    
    private final ConfigurationService configService;
    
    private final InstanceStateService instanceStateService;
    
    private final DataSourceService dataSourceService;
    
    private final ListenerFactory listenerManager;
    
    public OrchestrationFacade(final OrchestrationConfiguration config) {
        RegistryCenter regCenter = createRegistryCenter(config.getRegCenterConfig());
        isOverwrite = config.isOverwrite();
        configService = new ConfigurationService(config.getName(), regCenter);
        instanceStateService = new InstanceStateService(config.getName(), regCenter);
        dataSourceService = new DataSourceService(config.getName(), regCenter);
        listenerManager = new ListenerFactory(config.getName(), regCenter);
    }
    
    private RegistryCenter createRegistryCenter(final RegistryCenterConfiguration regCenterConfig) {
        Preconditions.checkNotNull(regCenterConfig, "Registry center configuration cannot be null.");
        if (regCenterConfig instanceof ZookeeperConfiguration) {
            return new ZookeeperRegistryCenter((ZookeeperConfiguration) regCenterConfig);
        }
        if (regCenterConfig instanceof EtcdConfiguration) {
            return new EtcdRegistryCenter((EtcdConfiguration) regCenterConfig);
        }
        throw new UnsupportedOperationException(regCenterConfig.getClass().getName());
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
    public void initShardingOrchestration(final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig, 
                                          final Map<String, Object> configMap, final Properties props, final ShardingDataSource shardingDataSource) throws SQLException {
        if (shardingRuleConfig.getMasterSlaveRuleConfigs().isEmpty()) {
            reviseShardingRuleConfigurationForMasterSlave(dataSourceMap, shardingRuleConfig);
        }
        configService.persistShardingConfiguration(getActualDataSourceMapForMasterSlave(dataSourceMap), shardingRuleConfig, configMap, props, isOverwrite);
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
    public void initMasterSlaveOrchestration(final Map<String, DataSource> dataSourceMap, 
                                             final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final MasterSlaveDataSource masterSlaveDataSource, final Map<String, Object> configMap) {
        configService.persistMasterSlaveConfiguration(dataSourceMap, masterSlaveRuleConfig, configMap, isOverwrite);
        instanceStateService.persistMasterSlaveInstanceOnline();
        dataSourceService.persistDataSourcesNode();
        listenerManager.initMasterSlaveListeners(masterSlaveDataSource);
        if (dataSourceService.hasDisabledDataSource()) {
            masterSlaveDataSource.renew(dataSourceService.getAvailableMasterSlaveRule());
        }
    }
}
