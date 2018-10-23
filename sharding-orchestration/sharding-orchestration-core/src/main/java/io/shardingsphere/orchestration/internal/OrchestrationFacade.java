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

package io.shardingsphere.orchestration.internal;

import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.yaml.YamlRuleConfiguration;
import io.shardingsphere.core.yaml.other.YamlServerConfiguration;
import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.orchestration.config.OrchestrationType;
import io.shardingsphere.orchestration.internal.config.ConfigurationService;
import io.shardingsphere.orchestration.internal.listener.ListenerFactory;
import io.shardingsphere.orchestration.internal.state.datasource.DataSourceService;
import io.shardingsphere.orchestration.internal.state.instance.InstanceStateService;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
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
    
    private final RegistryCenter regCenter;
    
    private final boolean isOverwrite;
    
    @Getter
    private final ConfigurationService configService;
    
    private final InstanceStateService instanceStateService;
    
    private final DataSourceService dataSourceService;
    
    private final ListenerFactory listenerManager;
    
    public OrchestrationFacade(final OrchestrationConfiguration orchestrationConfig) {
        regCenter = RegistryCenterLoader.load(orchestrationConfig.getRegCenterConfig());
        isOverwrite = orchestrationConfig.isOverwrite();
        configService = new ConfigurationService(orchestrationConfig.getName(), regCenter);
        instanceStateService = new InstanceStateService(orchestrationConfig.getName(), regCenter);
        dataSourceService = new DataSourceService(orchestrationConfig.getName(), regCenter);
        listenerManager = new ListenerFactory(orchestrationConfig.getName(), regCenter);
    }
    
    /**
     * Initialize for sharding orchestration.
     *
     * @param dataSourceMap data source map
     * @param shardingRuleConfig sharding rule configuration
     * @param configMap config map
     * @param props sharding properties
     */
    public void init(final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig, final Map<String, Object> configMap, final Properties props) {
        if (shardingRuleConfig.getMasterSlaveRuleConfigs().isEmpty()) {
            reviseShardingRuleConfigurationForMasterSlave(dataSourceMap, shardingRuleConfig);
        }
        configService.persistShardingConfiguration(getActualDataSourceMapForMasterSlave(dataSourceMap), shardingRuleConfig, configMap, props, isOverwrite);
        instanceStateService.persistShardingInstanceOnline();
        dataSourceService.persistDataSourcesNode();
        listenerManager.initShardingListeners();
    }
    
    /**
     * Initialize for master-slave orchestration.
     * 
     * @param dataSourceMap data source map
     * @param masterSlaveRuleConfig master-slave rule configuration
     * @param configMap config map
     * @param props properties
     */
    public void init(final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final Map<String, Object> configMap, final Properties props) {
        configService.persistMasterSlaveConfiguration(dataSourceMap, masterSlaveRuleConfig, configMap, props, isOverwrite);
        instanceStateService.persistMasterSlaveInstanceOnline();
        dataSourceService.persistDataSourcesNode();
        listenerManager.initMasterSlaveListeners();
    }
    
    /**
     * Initialize for proxy orchestration.
     *
     * @param serverConfig server configuration
     * @param schemaDataSourceMap schema data source map
     * @param schemaRuleMap schema rule map
     */
    public void init(final YamlServerConfiguration serverConfig, final Map<String, Map<String, DataSourceParameter>> schemaDataSourceMap, final Map<String, YamlRuleConfiguration> schemaRuleMap) {
        configService.persistProxyConfiguration(serverConfig, schemaDataSourceMap, schemaRuleMap, isOverwrite);
        instanceStateService.persistProxyInstanceOnline();
        dataSourceService.persistDataSourcesNode();
        listenerManager.initProxyListeners();
    }
    
    /**
     * Initialize for proxy orchestration.
     *
     *@param orchestrationType orchestration type
     */
    public void init(final OrchestrationType orchestrationType) {
        switch (orchestrationType) {
            case MASTER_SLAVE:
                listenerManager.initMasterSlaveListeners();
                break;
            case SHARDING:
                listenerManager.initShardingListeners();
                break;
            case PROXY:
                listenerManager.initProxyListeners();
                break;
            default:
                throw new UnsupportedOperationException(orchestrationType.name());
        }
    }
    
    private void reviseShardingRuleConfigurationForMasterSlave(final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            if (entry.getValue() instanceof MasterSlaveDataSource) {
                MasterSlaveDataSource masterSlaveDataSource = (MasterSlaveDataSource) entry.getValue();
                shardingRuleConfig.getMasterSlaveRuleConfigs().add(getMasterSlaveRuleConfiguration(masterSlaveDataSource.getMasterSlaveRule()));
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
    
    private MasterSlaveRuleConfiguration getMasterSlaveRuleConfiguration(final MasterSlaveRule masterSlaveRule) {
        return new MasterSlaveRuleConfiguration(
                masterSlaveRule.getName(), masterSlaveRule.getMasterDataSourceName(), masterSlaveRule.getSlaveDataSourceNames(), masterSlaveRule.getLoadBalanceAlgorithm());
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
