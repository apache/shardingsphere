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

package org.apache.shardingsphere.governance.core.registry;

import lombok.Getter;
import org.apache.shardingsphere.governance.core.GovernanceInstance;
import org.apache.shardingsphere.governance.core.lock.service.LockRegistryService;
import org.apache.shardingsphere.governance.core.registry.cache.subscriber.ScalingRegistrySubscriber;
import org.apache.shardingsphere.governance.core.registry.config.service.impl.DataSourcePersistService;
import org.apache.shardingsphere.governance.core.registry.config.service.impl.GlobalRulePersistService;
import org.apache.shardingsphere.governance.core.registry.config.service.impl.PropertiesPersistService;
import org.apache.shardingsphere.governance.core.registry.config.service.impl.SchemaRulePersistService;
import org.apache.shardingsphere.governance.core.registry.config.subscriber.DataSourceRegistrySubscriber;
import org.apache.shardingsphere.governance.core.registry.config.subscriber.GlobalRuleRegistrySubscriber;
import org.apache.shardingsphere.governance.core.registry.config.subscriber.SchemaRuleRegistrySubscriber;
import org.apache.shardingsphere.governance.core.registry.metadata.service.SchemaRegistryService;
import org.apache.shardingsphere.governance.core.registry.process.subscriber.ProcessRegistrySubscriber;
import org.apache.shardingsphere.governance.core.registry.state.service.DataSourceStatusRegistryService;
import org.apache.shardingsphere.governance.core.registry.state.service.InstanceStatusRegistryService;
import org.apache.shardingsphere.governance.core.registry.state.service.UserStatusRegistryService;
import org.apache.shardingsphere.governance.core.registry.state.subscriber.DataSourceStatusRegistrySubscriber;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Registry center.
 */
@Getter
public final class RegistryCenter {
    
    private final String instanceId;
    
    private final DataSourcePersistService dataSourceService;
    
    private final SchemaRulePersistService schemaRuleService;
    
    private final GlobalRulePersistService globalRuleService;
    
    private final PropertiesPersistService propsService;
    
    private final SchemaRegistryService schemaService;
    
    private final DataSourceStatusRegistryService dataSourceStatusService;
    
    private final InstanceStatusRegistryService instanceStatusService;
    
    private final LockRegistryService lockService;
    
    public RegistryCenter(final RegistryCenterRepository repository) {
        instanceId = GovernanceInstance.getInstance().getId();
        dataSourceService = new DataSourcePersistService(repository);
        schemaRuleService = new SchemaRulePersistService(repository);
        globalRuleService = new GlobalRulePersistService(repository);
        propsService = new PropertiesPersistService(repository);
        schemaService = new SchemaRegistryService(repository);
        dataSourceStatusService = new DataSourceStatusRegistryService(repository);
        instanceStatusService = new InstanceStatusRegistryService(repository);
        lockService = new LockRegistryService(repository);
        createSubscribers(repository);
    }
    
    private void createSubscribers(final RegistryCenterRepository repository) {
        new DataSourceRegistrySubscriber(dataSourceService);
        new GlobalRuleRegistrySubscriber(globalRuleService, new UserStatusRegistryService(repository));
        new SchemaRuleRegistrySubscriber(schemaRuleService);
        new DataSourceStatusRegistrySubscriber(repository);
        new ScalingRegistrySubscriber(repository, schemaRuleService);
        new ProcessRegistrySubscriber(repository);
    }
    
    /**
     * Persist configurations.
     *
     * @param dataSourceConfigs schema and data source configuration map
     * @param schemaRuleConfigs schema and rule configuration map
     * @param globalRuleConfigs global rule configurations
     * @param props properties
     * @param isOverwrite whether overwrite registry center's configuration if existed
     */
    public void persistConfigurations(final Map<String, Map<String, DataSourceConfiguration>> dataSourceConfigs, final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, 
                                      final Collection<RuleConfiguration> globalRuleConfigs, final Properties props, final boolean isOverwrite) {
        globalRuleService.persist(globalRuleConfigs, isOverwrite);
        propsService.persist(props, isOverwrite);
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : dataSourceConfigs.entrySet()) {
            String schemaName = entry.getKey();
            dataSourceService.persist(schemaName, dataSourceConfigs.get(schemaName), isOverwrite);
            schemaRuleService.persist(schemaName, schemaRuleConfigs.get(schemaName), isOverwrite);
        }
    }
    
    /**
     * Register instance online.
     */
    public void registerInstanceOnline() {
        instanceStatusService.registerInstanceOnline(instanceId);
    }
}
