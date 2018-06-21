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

import com.google.common.base.Preconditions;
import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.jdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.config.ConfigurationService;
import io.shardingsphere.jdbc.orchestration.internal.listener.ListenerFactory;
import io.shardingsphere.jdbc.orchestration.internal.state.datasource.DataSourceService;
import io.shardingsphere.jdbc.orchestration.internal.state.instance.InstanceStateService;
import io.shardingsphere.jdbc.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.jdbc.orchestration.reg.api.RegistryCenterConfiguration;
import io.shardingsphere.jdbc.orchestration.reg.etcd.EtcdConfiguration;
import io.shardingsphere.jdbc.orchestration.reg.etcd.EtcdRegistryCenter;
import io.shardingsphere.jdbc.orchestration.reg.newzk.NewZookeeperRegistryCenter;
import io.shardingsphere.jdbc.orchestration.reg.zookeeper.ZookeeperConfiguration;
import io.shardingsphere.jdbc.orchestration.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Orchestration service facade.
 *
 * @author zhangliang
 * @author caohao
 * @author panjuan
 */
@Slf4j
public final class OrchestrationFacade implements AutoCloseable {
    
    private final boolean isOverwrite;
    
    @Getter
    private final ConfigurationService configService;
    
    private final InstanceStateService instanceStateService;
    
    private final DataSourceService dataSourceService;
    
    private final ListenerFactory listenerManager;
    
    private final RegistryCenter regCenter;
    
    public OrchestrationFacade(final OrchestrationConfiguration config) {
        regCenter = createRegistryCenter(config.getRegCenterConfig());
        isOverwrite = config.isOverwrite();
        configService = new ConfigurationService(config.getName(), regCenter);
        instanceStateService = new InstanceStateService(config.getName(), regCenter);
        dataSourceService = new DataSourceService(config.getName(), regCenter);
        listenerManager = new ListenerFactory(config.getName(), regCenter);
    }
    
    private RegistryCenter createRegistryCenter(final RegistryCenterConfiguration regCenterConfig) {
        Preconditions.checkNotNull(regCenterConfig, "Registry center configuration cannot be null.");
        if (regCenterConfig instanceof ZookeeperConfiguration) {
            return getZookeeperRegistryCenter((ZookeeperConfiguration) regCenterConfig);
        }
        if (regCenterConfig instanceof EtcdConfiguration) {
            return new EtcdRegistryCenter((EtcdConfiguration) regCenterConfig);
        }
        throw new UnsupportedOperationException(regCenterConfig.getClass().getName());
    }
    
    private RegistryCenter getZookeeperRegistryCenter(final ZookeeperConfiguration regCenterConfig) {
        if (regCenterConfig.isUseNative()) {
            return new NewZookeeperRegistryCenter(regCenterConfig);
        } else {
            return new ZookeeperRegistryCenter(regCenterConfig);
        }
    }
    
    /**
     * Initialize for sharding orchestration.
     *
     * @param dataSourceMap data source map
     * @param shardingRuleConfig sharding rule configuration
     * @param configMap config map
     * @param props sharding properties
     * @param shardingDataSource sharding data source
     */
    public void init(final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig, 
                     final Map<String, Object> configMap, final Properties props, final ShardingDataSource shardingDataSource) {
        if (shardingRuleConfig.getMasterSlaveRuleConfigs().isEmpty()) {
            reviseShardingRuleConfigurationForMasterSlave(dataSourceMap, shardingRuleConfig);
        }
        configService.persistShardingConfiguration(getActualDataSourceMapForMasterSlave(dataSourceMap), shardingRuleConfig, configMap, props, isOverwrite);
        instanceStateService.persistShardingInstanceOnline();
        dataSourceService.persistDataSourcesNode();
        listenerManager.initShardingListeners(shardingDataSource);
    }
    
    /**
     * Initialize for master-slave orchestration.
     *
     * @param dataSourceMap data source map
     * @param masterSlaveRuleConfig master-slave rule configuration
     * @param configMap config map
     * @param masterSlaveDataSource master-slave source
     */
    public void init(final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig, 
                     final Map<String, Object> configMap, final MasterSlaveDataSource masterSlaveDataSource) {
        configService.persistMasterSlaveConfiguration(dataSourceMap, masterSlaveRuleConfig, configMap, isOverwrite);
        instanceStateService.persistMasterSlaveInstanceOnline();
        dataSourceService.persistDataSourcesNode();
        listenerManager.initMasterSlaveListeners(masterSlaveDataSource);
    }
    
    /**
     * Initialize for proxy orchestration.
     *
     * @param orchestrationProxyConfiguration yaml proxy configuration
     */
    public void init(final OrchestrationProxyConfiguration orchestrationProxyConfiguration) {
        configService.persistProxyConfiguration(orchestrationProxyConfiguration, isOverwrite);
        instanceStateService.persistProxyInstanceOnline();
        dataSourceService.persistDataSourcesNode();
        listenerManager.initProxyListeners();
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
        Map<String, DataSource> result = new LinkedHashMap<>();
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
        return new MasterSlaveRuleConfiguration(
                masterSlaveDataSource.getMasterSlaveRule().getName(), masterSlaveDataSource.getMasterSlaveRule().getMasterDataSourceName(), 
                masterSlaveDataSource.getMasterSlaveRule().getSlaveDataSourceNames(), masterSlaveDataSource.getMasterSlaveRule().getLoadBalanceAlgorithm());
    }
    
    @Override
    public void close() {
        try {
            regCenter.close();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.warn("RegCenter exception for: {}", ex.getMessage());
        }
    }
}
