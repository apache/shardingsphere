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

package org.apache.shardingsphere.orchestration.internal.registry;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.orchestration.center.api.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.center.api.RegistryCenterRepository;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.configuration.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.constant.OrchestrationType;
import org.apache.shardingsphere.orchestration.internal.configcenter.ConfigCenterServiceLoader;
import org.apache.shardingsphere.orchestration.internal.registry.config.service.ConfigurationService;
import org.apache.shardingsphere.orchestration.internal.registry.listener.ShardingOrchestrationListenerManager;
import org.apache.shardingsphere.orchestration.internal.registry.state.service.StateService;
import org.apache.shardingsphere.underlying.common.config.DataSourceConfiguration;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.constant.properties.OrchestrationProperties;
import org.apache.shardingsphere.underlying.common.constant.properties.OrchestrationPropertiesEnum;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * Sharding orchestration facade.
 */
@Slf4j
public final class ShardingOrchestrationFacade implements AutoCloseable {
    
    private final RegistryCenterRepository registryCenterRepository;
    
    private final ConfigCenterRepository configCenterRepository;
    
    private final boolean isOverwrite;
    
    @Getter
    private final ConfigurationService configService;
    
    private final StateService stateService;
    
    private final ShardingOrchestrationListenerManager listenerManager;
    
    public ShardingOrchestrationFacade(final OrchestrationConfiguration orchestrationConfig, final Collection<String> shardingSchemaNames) {
        Optional<String> registryCenterName = getInstanceNameByOrchestrationType(orchestrationConfig.getInstanceConfigurationMap(), OrchestrationType.REGISTRY_CENTER.getValue());
        Preconditions.checkArgument(registryCenterName.isPresent(), "Can not find instance configuration with registry center orchestration type.");
        InstanceConfiguration registryCenterConfiguration = orchestrationConfig.getInstanceConfigurationMap().get(registryCenterName.get());
        registryCenterRepository = new RegistryCenterServiceLoader().load(registryCenterConfiguration);
        stateService = new StateService(registryCenterName.get(), registryCenterRepository);
        Optional<String> configCenterName = getInstanceNameByOrchestrationType(orchestrationConfig.getInstanceConfigurationMap(), OrchestrationType.CONFIG_CENTER.getValue());
        Preconditions.checkArgument(configCenterName.isPresent(), "Can not find instance configuration with config center orchestration type.");
        InstanceConfiguration configCenterConfiguration = orchestrationConfig.getInstanceConfigurationMap().get(configCenterName.get());
        configCenterRepository = new ConfigCenterServiceLoader().load(configCenterConfiguration);
        isOverwrite = new OrchestrationProperties(configCenterConfiguration.getProperties()).getValue(OrchestrationPropertiesEnum.OVERWRITE);
        configService = new ConfigurationService(configCenterName.get(), configCenterRepository);
        listenerManager = shardingSchemaNames.isEmpty()
                ? new ShardingOrchestrationListenerManager(registryCenterName.get(), registryCenterRepository,
                                                            configCenterName.get(), configCenterRepository, configService.getAllShardingSchemaNames())
                : new ShardingOrchestrationListenerManager(registryCenterName.get(), registryCenterRepository,
                                                            configCenterName.get(), configCenterRepository, shardingSchemaNames);
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
            registryCenterRepository.close();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.warn("RegCenter exception for: {}", ex.getMessage());
        }
    }
    
    private Optional<String> getInstanceNameByOrchestrationType(final Map<String, InstanceConfiguration> map, final String type) {
        if (null == map || 0 == map.size() || null == type) {
            return Optional.empty();
        }
        for (Entry<String, InstanceConfiguration> entry : map.entrySet()) {
            if (contains(entry.getValue().getOrchestrationType(), type)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }
    
    private boolean contains(final String collection, final String element) {
        if (Strings.isNullOrEmpty(collection)) {
            return false;
        }
        for (String each : Splitter.on(",").split(collection)) {
            if (element.equals(each.trim())) {
                return true;
            }
        }
        return false;
    }
}
