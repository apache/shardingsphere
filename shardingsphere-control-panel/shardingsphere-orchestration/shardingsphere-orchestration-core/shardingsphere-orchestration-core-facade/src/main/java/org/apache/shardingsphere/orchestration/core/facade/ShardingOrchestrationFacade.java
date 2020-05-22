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

package org.apache.shardingsphere.orchestration.core.facade;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.orchestration.center.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.center.RegistryCenterRepository;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.core.common.CenterType;
import org.apache.shardingsphere.orchestration.core.configcenter.ConfigCenter;
import org.apache.shardingsphere.orchestration.core.facade.listener.ShardingOrchestrationListenerManager;
import org.apache.shardingsphere.orchestration.core.facade.properties.OrchestrationProperties;
import org.apache.shardingsphere.orchestration.core.facade.properties.OrchestrationPropertyKey;
import org.apache.shardingsphere.orchestration.core.metadatacenter.MetaDataCenter;
import org.apache.shardingsphere.orchestration.core.registrycenter.RegistryCenter;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.TypedSPIRegistry;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;

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
    
    static {
        // TODO avoid multiple loading
        ShardingSphereServiceLoader.register(ConfigCenterRepository.class);
        ShardingSphereServiceLoader.register(RegistryCenterRepository.class);
    }

    @Getter
    private static ShardingOrchestrationFacade instance;
    
    private final ConfigCenterRepository configCenterRepository;
    
    private final RegistryCenterRepository registryCenterRepository;

    private final ConfigCenterRepository centerRepository;
    
    @Getter
    private final boolean isOverwrite;
    
    @Getter
    private final ConfigCenter configCenter;
    
    @Getter
    private final RegistryCenter registryCenter;

    @Getter
    private final MetaDataCenter metaDataCenter;
    
    private final ShardingOrchestrationListenerManager listenerManager;
    
    public ShardingOrchestrationFacade(final OrchestrationConfiguration orchestrationConfig, final Collection<String> shardingSchemaNames) {
        Optional<String> configCenterName = getInstanceNameByOrchestrationType(orchestrationConfig.getInstanceConfigurationMap(), CenterType.CONFIG_CENTER.getValue());
        Preconditions.checkArgument(configCenterName.isPresent(), "Can not find instance configuration with config center orchestration type.");
        CenterConfiguration configCenterConfiguration = orchestrationConfig.getInstanceConfigurationMap().get(configCenterName.get());
        Preconditions.checkNotNull(configCenterConfiguration, "Config center configuration cannot be null.");
        configCenterRepository = TypedSPIRegistry.getRegisteredService(
                ConfigCenterRepository.class, configCenterConfiguration.getType(), configCenterConfiguration.getProperties());
        configCenterRepository.init(configCenterConfiguration);
        isOverwrite = new OrchestrationProperties(configCenterConfiguration.getProperties()).getValue(OrchestrationPropertyKey.OVERWRITE);
        configCenter = new ConfigCenter(configCenterName.get(), configCenterRepository);
        Optional<String> registryCenterName = getInstanceNameByOrchestrationType(orchestrationConfig.getInstanceConfigurationMap(), CenterType.REGISTRY_CENTER.getValue());
        Preconditions.checkArgument(registryCenterName.isPresent(), "Can not find instance configuration with registry center orchestration type.");
        CenterConfiguration registryCenterConfiguration = orchestrationConfig.getInstanceConfigurationMap().get(registryCenterName.get());
        Preconditions.checkNotNull(registryCenterConfiguration, "Registry center configuration cannot be null.");
        registryCenterRepository = TypedSPIRegistry.getRegisteredService(RegistryCenterRepository.class, registryCenterConfiguration.getType(), registryCenterConfiguration.getProperties());
        registryCenterRepository.init(registryCenterConfiguration);
        registryCenter = new RegistryCenter(registryCenterName.get(), registryCenterRepository);
        Optional<String> metaDataCenterName = getInstanceNameByOrchestrationType(orchestrationConfig.getInstanceConfigurationMap(), CenterType.METADATA_CENTER.getValue());
        Preconditions.checkArgument(metaDataCenterName.isPresent(), "Can not find instance configuration with metadata center orchestration type.");
        CenterConfiguration metaDataCenterConfiguration = orchestrationConfig.getInstanceConfigurationMap().get(metaDataCenterName.get());
        Preconditions.checkNotNull(metaDataCenterConfiguration, "MetaData center configuration cannot be null.");
        centerRepository = TypedSPIRegistry.getRegisteredService(ConfigCenterRepository.class, metaDataCenterConfiguration.getType(), metaDataCenterConfiguration.getProperties());
        centerRepository.init(metaDataCenterConfiguration);
        metaDataCenter = new MetaDataCenter(metaDataCenterName.get(), centerRepository);
        listenerManager = shardingSchemaNames.isEmpty()
                ? new ShardingOrchestrationListenerManager(
                registryCenterName.get(), registryCenterRepository, configCenterName.get(),
                configCenterRepository, metaDataCenterName.get(), centerRepository, configCenter.getAllShardingSchemaNames())
                : new ShardingOrchestrationListenerManager(registryCenterName.get(), registryCenterRepository,
                configCenterName.get(), configCenterRepository, metaDataCenterName.get(), centerRepository, shardingSchemaNames);
        instance = this;
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
                     final Map<String, Collection<RuleConfiguration>> schemaRuleMap, final Authentication authentication, final Properties props) {
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : dataSourceConfigurationMap.entrySet()) {
            configCenter.persistConfigurations(entry.getKey(), dataSourceConfigurationMap.get(entry.getKey()), schemaRuleMap.get(entry.getKey()), authentication, props, isOverwrite);
        }
        init();
    }
    
    /**
     * Initialize for orchestration.
     */
    public void init() {
        registryCenter.persistInstanceOnline();
        registryCenter.persistDataSourcesNode();
        listenerManager.initListeners();
    }
    
    @Override
    public void close() {
        try {
            configCenterRepository.close();
            registryCenterRepository.close();
            centerRepository.close();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.warn("RegCenter exception for: {}", ex.getMessage());
        }
    }
    
    private Optional<String> getInstanceNameByOrchestrationType(final Map<String, CenterConfiguration> map, final String type) {
        return (null == map || null == type) ? Optional.empty() : map.entrySet()
                .stream().filter(entry -> contains(entry.getValue().getOrchestrationType(), type)).findFirst().map(Map.Entry::getKey);
    }
    
    private boolean contains(final String collection, final String element) {
        return Splitter.on(",").omitEmptyStrings().trimResults().splitToList(collection).stream().anyMatch(each -> element.equals(each.trim()));
    }
}
