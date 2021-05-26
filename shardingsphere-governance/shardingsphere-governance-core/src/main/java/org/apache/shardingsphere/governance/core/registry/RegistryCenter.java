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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.governance.core.registry.instance.GovernanceInstance;
import org.apache.shardingsphere.governance.core.registry.service.config.impl.DataSourceRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.config.impl.GlobalRuleRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.config.impl.PropertiesRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.config.impl.SchemaRuleRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.process.ProcessRegistrySubscriber;
import org.apache.shardingsphere.governance.core.registry.service.scaling.ScalingRegistrySubscriber;
import org.apache.shardingsphere.governance.core.registry.service.schema.SchemaRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.state.DataSourceStatusRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.state.LockRegistryService;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Registry center.
 */
public final class RegistryCenter {
    
    private final String instanceId;
    
    private final RegistryCenterRepository repository;
    
    private final RegistryCenterNode node;
    
    @Getter
    private final DataSourceRegistryService dataSourceService;
    
    @Getter
    private final SchemaRuleRegistryService schemaRuleService;
    
    @Getter
    private final GlobalRuleRegistryService globalRuleService;
    
    @Getter
    private final PropertiesRegistryService propsService;
    
    @Getter
    private final SchemaRegistryService schemaService;
    
    @Getter
    private final DataSourceStatusRegistryService dataSourceStatusService;
    
    @Getter
    private final LockRegistryService lockService;
    
    public RegistryCenter(final RegistryCenterRepository repository) {
        instanceId = GovernanceInstance.getInstance().getId();
        this.repository = repository;
        node = new RegistryCenterNode();
        dataSourceService = new DataSourceRegistryService(repository);
        schemaRuleService = new SchemaRuleRegistryService(repository);
        globalRuleService = new GlobalRuleRegistryService(repository);
        propsService = new PropertiesRegistryService(repository);
        schemaService = new SchemaRegistryService(repository);
        dataSourceStatusService = new DataSourceStatusRegistryService(repository);
        lockService = new LockRegistryService(repository);
        new ScalingRegistrySubscriber(repository, schemaRuleService);
        new ProcessRegistrySubscriber(repository);
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Initialize nodes.
     */
    public void initNodes() {
        repository.persist(node.getDataNodesPath(), "");
        repository.persist(node.getPrimaryNodesPath(), "");
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
            // TODO persistSchemaName is for etcd to get SchemaName which is for impl details, should move the logic into etcd repository, just keep reg center clear and high abstract.
            persistSchemaName(entry.getKey());
        }
    }
    
    private void persistSchemaName(final String schemaName) {
        String schemaNamesStr = repository.get(node.getMetadataNodePath());
        if (Strings.isNullOrEmpty(schemaNamesStr)) {
            repository.persist(node.getMetadataNodePath(), schemaName);
            return;
        }
        Collection<String> schemaNames = Splitter.on(",").splitToList(schemaNamesStr);
        if (schemaNames.contains(schemaName)) {
            return;
        }
        Collection<String> newSchemaNames = new ArrayList<>(schemaNames);
        newSchemaNames.add(schemaName);
        repository.persist(node.getMetadataNodePath(), String.join(",", newSchemaNames));
    }
    
    /**
     * Register instance online.
     */
    public void registerInstanceOnline() {
        repository.persistEphemeral(node.getProxyNodePath(instanceId), "");
    }
}
