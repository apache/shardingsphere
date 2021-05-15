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

package org.apache.shardingsphere.governance.core.facade;

import lombok.Getter;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.listener.GovernanceListenerManager;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Governance facade.
 */
public final class GovernanceFacade implements AutoCloseable {
    
    private boolean isOverwrite;
    
    private RegistryCenterRepository registryCenterRepository;
    
    @Getter
    private RegistryCenter registryCenter;
    
    private GovernanceListenerManager listenerManager;
    
    /**
     * Initialize governance facade.
     *
     * @param config governance configuration
     * @param schemaNames schema names
     */
    public void init(final GovernanceConfiguration config, final Collection<String> schemaNames) {
        isOverwrite = config.isOverwrite();
        registryCenterRepository = RegistryCenterRepositoryFactory.newInstance(config);
        registryCenter = new RegistryCenter(registryCenterRepository);
        listenerManager = new GovernanceListenerManager(registryCenterRepository, 
                Stream.of(registryCenter.getAllSchemaNames(), schemaNames).flatMap(Collection::stream).distinct().collect(Collectors.toList()));
    }
    
    /**
     * Online instance.
     *
     * @param dataSourceConfigMap schema and data source configuration map
     * @param schemaRuleMap schema and rule map
     * @param globalRuleConfigs global rule configurations
     * @param props properties
     */
    public void onlineInstance(final Map<String, Map<String, DataSourceConfiguration>> dataSourceConfigMap,
                               final Map<String, Collection<RuleConfiguration>> schemaRuleMap, final Collection<RuleConfiguration> globalRuleConfigs, final Properties props) {
        registryCenter.persistGlobalConfiguration(globalRuleConfigs, props, isOverwrite);
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : dataSourceConfigMap.entrySet()) {
            registryCenter.persistConfigurations(entry.getKey(), dataSourceConfigMap.get(entry.getKey()), schemaRuleMap.get(entry.getKey()), isOverwrite);
        }
        onlineInstance();
    }
    
    /**
     * Online instance.
     */
    public void onlineInstance() {
        registryCenter.persistInstanceOnline();
        registryCenter.persistDataNodes();
        registryCenter.persistPrimaryNodes();
        listenerManager.initListeners();
    }
    
    @Override
    public void close() {
        registryCenterRepository.close();
    }
}
