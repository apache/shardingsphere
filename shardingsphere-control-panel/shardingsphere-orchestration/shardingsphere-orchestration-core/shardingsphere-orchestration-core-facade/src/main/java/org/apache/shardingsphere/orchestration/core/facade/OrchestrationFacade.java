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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.TypedSPIRegistry;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.orchestration.core.config.ConfigCenter;
import org.apache.shardingsphere.orchestration.core.facade.listener.OrchestrationListenerManager;
import org.apache.shardingsphere.orchestration.core.facade.properties.OrchestrationProperties;
import org.apache.shardingsphere.orchestration.core.facade.properties.OrchestrationPropertyKey;
import org.apache.shardingsphere.orchestration.core.metadata.MetaDataCenter;
import org.apache.shardingsphere.orchestration.core.registry.RegistryCenter;
import org.apache.shardingsphere.orchestration.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.orchestration.repository.api.RegistryRepository;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationRepositoryConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Orchestration facade.
 */
@Slf4j
public final class OrchestrationFacade implements AutoCloseable {
    
    static {
        // TODO avoid multiple loading
        ShardingSphereServiceLoader.register(ConfigurationRepository.class);
        ShardingSphereServiceLoader.register(RegistryRepository.class);
    }
    
    private ConfigurationRepository configurationRepository;
    
    private RegistryRepository registryRepository;
    
    private boolean isOverwrite;
    
    @Getter
    private ConfigCenter configCenter;
    
    @Getter
    private RegistryCenter registryCenter;
    
    @Getter
    private MetaDataCenter metaDataCenter;
    
    private OrchestrationListenerManager listenerManager;
    
    private String name;
    
    /**
     * Initialize orchestration facade.
     *
     * @param orchestrationConfig orchestration configuration
     * @param shardingSchemaNames sharding schema names
     */
    public void init(final OrchestrationConfiguration orchestrationConfig, final Collection<String> shardingSchemaNames) {
        name = orchestrationConfig.getName();
        initConfigCenter(orchestrationConfig);
        initRegistryCenter(orchestrationConfig);
        initMetaDataCenter();
        initListenerManager(shardingSchemaNames);
    }
    
    private void initConfigCenter(final OrchestrationConfiguration orchestrationConfig) {
        OrchestrationRepositoryConfiguration configRepositoryConfiguration
                = orchestrationConfig.getAdditionalConfigurationRepositoryConfiguration().orElse(orchestrationConfig.getRegistryRepositoryConfiguration());
        Preconditions.checkNotNull(configRepositoryConfiguration, "Config center configuration cannot be null.");
        configurationRepository = TypedSPIRegistry.getRegisteredService(ConfigurationRepository.class, configRepositoryConfiguration.getType(), configRepositoryConfiguration.getProps());
        configurationRepository.init(configRepositoryConfiguration);
        isOverwrite = new OrchestrationProperties(configRepositoryConfiguration.getProps()).getValue(OrchestrationPropertyKey.OVERWRITE);
        configCenter = new ConfigCenter(name, configurationRepository);
    }
    
    private void initRegistryCenter(final OrchestrationConfiguration orchestrationConfig) {
        OrchestrationRepositoryConfiguration regRepositoryConfiguration = orchestrationConfig.getRegistryRepositoryConfiguration();
        Preconditions.checkNotNull(regRepositoryConfiguration, "Registry center configuration cannot be null.");
        registryRepository = TypedSPIRegistry.getRegisteredService(RegistryRepository.class, regRepositoryConfiguration.getType(), regRepositoryConfiguration.getProps());
        registryRepository.init(regRepositoryConfiguration);
        registryCenter = new RegistryCenter(name, registryRepository);
    }
    
    private void initMetaDataCenter() {
        metaDataCenter = new MetaDataCenter(name, configurationRepository);
    }
    
    private void initListenerManager(final Collection<String> shardingSchemaNames) {
        listenerManager = new OrchestrationListenerManager(
                name, registryRepository, configurationRepository, shardingSchemaNames.isEmpty() ? configCenter.getAllShardingSchemaNames() : shardingSchemaNames);
    }
    
    /**
     * Initialize configurations of orchestration.
     *
     * @param dataSourceConfigurationMap schema data source configuration map
     * @param schemaRuleMap schema rule map
     * @param authentication authentication
     * @param props properties
     */
    public void initConfigurations(final Map<String, Map<String, DataSourceConfiguration>> dataSourceConfigurationMap, 
                                   final Map<String, Collection<RuleConfiguration>> schemaRuleMap, final Authentication authentication, final Properties props) {
        configCenter.persistGlobalConfiguration(authentication, props, isOverwrite);
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : dataSourceConfigurationMap.entrySet()) {
            configCenter.persistConfigurations(entry.getKey(), dataSourceConfigurationMap.get(entry.getKey()), schemaRuleMap.get(entry.getKey()), isOverwrite);
        }
        initConfigurations();
    }
    
    /**
     * Initialize configurations of orchestration.
     */
    public void initConfigurations() {
        registryCenter.persistInstanceOnline();
        registryCenter.persistDataSourcesNode();
        listenerManager.initListeners();
    }
    
    /**
     * Initialize metrics configuration to config center.
     *
     * @param metricsConfiguration metrics configuration
     */
    public void initMetricsConfiguration(final MetricsConfiguration metricsConfiguration) {
        configCenter.persistMetricsConfiguration(metricsConfiguration, isOverwrite);
    }
    
    /**
     * Initialize cluster configuration to config center.
     *
     * @param clusterConfiguration cluster configuration
     */
    public void initClusterConfiguration(final ClusterConfiguration clusterConfiguration) {
        configCenter.persistClusterConfiguration(clusterConfiguration, isOverwrite);
    }
    
    @Override
    public void close() {
        try {
            configurationRepository.close();
            registryRepository.close();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.warn("RegCenter exception for: {}", ex.getMessage());
        }
    }
    
    /**
     * Get orchestration facade instance.
     *
     * @return orchestration facade instance
     */
    public static OrchestrationFacade getInstance() {
        return OrchestrationFacadeHolder.INSTANCE;
    }
    
    private static final class OrchestrationFacadeHolder {
        
        public static final OrchestrationFacade INSTANCE = new OrchestrationFacade();
    }
}
