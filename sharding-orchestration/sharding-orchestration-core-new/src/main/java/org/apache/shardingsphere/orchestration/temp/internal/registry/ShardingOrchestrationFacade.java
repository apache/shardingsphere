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

package org.apache.shardingsphere.orchestration.temp.internal.registry;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.config.RuleConfiguration;
import org.apache.shardingsphere.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.orchestration.center.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.center.api.RegistryCenter;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.configuration.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.temp.constant.OrchestrationType;
import org.apache.shardingsphere.orchestration.temp.internal.configcenter.ConfigCenterServiceLoader;
import org.apache.shardingsphere.orchestration.temp.internal.registry.config.service.ConfigurationService;
import org.apache.shardingsphere.orchestration.temp.internal.registry.listener.ShardingOrchestrationListenerManager;
import org.apache.shardingsphere.orchestration.temp.internal.registry.state.service.StateService;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Sharding orchestration facade.
 *
 * @author zhangliang
 * @author caohao
 * @author panjuan
 * @author wangguangyuan 
 */
@Slf4j
public final class ShardingOrchestrationFacade implements AutoCloseable {
    
    private final RegistryCenter regCenter;
    
    private final ConfigCenter configCenter;
    
    private final boolean isOverwrite;
    
    @Getter
    private final ConfigurationService configService;
    
    private final StateService stateService;
    
    private final ShardingOrchestrationListenerManager listenerManager;
    
    public ShardingOrchestrationFacade(final OrchestrationConfiguration orchestrationConfig, final Collection<String> shardingSchemaNames) {
        InstanceConfiguration registryCenterConfiguration = getConfigurationByOrchestrationType(orchestrationConfig.getInstanceConfigurationMap(),
                OrchestrationType.REGISTRY_CENTER.getValue());
        regCenter = new RegistryCenterServiceLoader().load(registryCenterConfiguration);
        isOverwrite = Boolean.valueOf(registryCenterConfiguration.getProperties().getProperty("isOverwrite"));
        stateService = new StateService(registryCenterConfiguration.getNamespace(), regCenter);
        InstanceConfiguration configCenterConfiguration = getConfigurationByOrchestrationType(orchestrationConfig.getInstanceConfigurationMap(),
                OrchestrationType.CONFIG_CENTER.getValue());
        configCenter = new ConfigCenterServiceLoader().load(configCenterConfiguration);
        configService = new ConfigurationService(configCenterConfiguration.getNamespace(), configCenter);
        listenerManager = shardingSchemaNames.isEmpty() 
                ? new ShardingOrchestrationListenerManager(registryCenterConfiguration.getNamespace(), regCenter,
                        configCenterConfiguration.getNamespace(), configCenter, configService.getAllShardingSchemaNames())
                : new ShardingOrchestrationListenerManager(registryCenterConfiguration.getNamespace(), regCenter, 
                configCenterConfiguration.getNamespace(), configCenter, shardingSchemaNames);        
    }
    
    /**
     * Initialize for orchestration.
     *
     * @param dataSourceConfigurationMap schema data source configuration map
     * @param schemaRuleMap schema rule map
     * @param authentication authentication
     * @param props properties
     */
    public void init(final Map<String, Map<String, DataSourceConfiguration>> dataSourceConfigurationMap,
                     final Map<String, RuleConfiguration> schemaRuleMap, final Authentication authentication, final Properties props) {
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : dataSourceConfigurationMap.entrySet()) {
            configService.persistConfiguration(entry.getKey(), dataSourceConfigurationMap.get(entry.getKey()), schemaRuleMap.get(entry.getKey()), authentication, props, isOverwrite);
        }
        stateService.persistInstanceOnline();
        stateService.persistDataSourcesNode();
        listenerManager.initListeners();
    }
    
    /**
     * Initialize for orchestration.
     */
    public void init() {
        stateService.persistInstanceOnline();
        stateService.persistDataSourcesNode();
        listenerManager.initListeners();
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
    
    private InstanceConfiguration getConfigurationByOrchestrationType(final Map<String, InstanceConfiguration> map, final String type) {
        if (null == map || null == type) {
            return null;
        }
        for (Entry<String, InstanceConfiguration> entry: map.entrySet()) {
            InstanceConfiguration configuration = entry.getValue();
            if (type.equals(configuration.getOrchestrationType())) {
                return configuration;
            }
        }
        return null;
    }
}
