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

package org.apache.shardingsphere.governance.core.registry.cache.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.registry.cache.RegistryCacheManager;
import org.apache.shardingsphere.governance.core.registry.cache.event.StartScalingEvent;
import org.apache.shardingsphere.governance.core.registry.config.event.rule.RuleConfigurationCachedEvent;
import org.apache.shardingsphere.governance.core.registry.config.event.rule.SwitchRuleConfigurationEvent;
import org.apache.shardingsphere.governance.core.registry.config.service.impl.SchemaRuleRegistryService;
import org.apache.shardingsphere.governance.core.registry.config.node.SchemaMetadataNode;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.Collection;

/**
 * Scaling registry subscriber.
 */
// TODO move to scaling module
public final class ScalingRegistrySubscriber {
    
    private final RegistryCenterRepository repository;
    
    private final SchemaRuleRegistryService schemaRuleService;
    
    private final RegistryCacheManager registryCacheManager;
    
    public ScalingRegistrySubscriber(final RegistryCenterRepository repository, final SchemaRuleRegistryService schemaRuleService) {
        this.repository = repository;
        this.schemaRuleService = schemaRuleService;
        registryCacheManager = new RegistryCacheManager(repository);
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Switch rule configuration.
     *
     * @param event switch rule configuration event
     */
    @Subscribe
    public void switchRuleConfiguration(final SwitchRuleConfigurationEvent event) {
        schemaRuleService.persist(event.getSchemaName(), loadCachedRuleConfigurations(event.getSchemaName(), event.getRuleConfigurationCacheId()));
        registryCacheManager.deleteCache(SchemaMetadataNode.getRulePath(event.getSchemaName()), event.getRuleConfigurationCacheId());
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> loadCachedRuleConfigurations(final String schemaName, final String ruleConfigCacheId) {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(
                YamlEngine.unmarshal(registryCacheManager.loadCache(SchemaMetadataNode.getRulePath(schemaName), ruleConfigCacheId), Collection.class));
    }
    
    /**
     * Cache rule configuration.
     *
     * @param event rule configuration cached event
     */
    @Subscribe
    public void cacheRuleConfiguration(final RuleConfigurationCachedEvent event) {
        StartScalingEvent startScalingEvent = new StartScalingEvent(event.getSchemaName(),
                repository.get(SchemaMetadataNode.getMetadataDataSourcePath(event.getSchemaName())),
                repository.get(SchemaMetadataNode.getRulePath(event.getSchemaName())),
                registryCacheManager.loadCache(SchemaMetadataNode.getRulePath(event.getSchemaName()), event.getCacheId()), event.getCacheId());
        ShardingSphereEventBus.getInstance().post(startScalingEvent);
    }
}
