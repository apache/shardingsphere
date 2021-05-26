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

package org.apache.shardingsphere.governance.core.registry.service.scaling;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.registry.RegistryCacheManager;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNode;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationCachedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.SwitchRuleConfigurationEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.scaling.StartScalingEvent;
import org.apache.shardingsphere.governance.core.registry.service.config.impl.SchemaRuleRegistryService;
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
    
    private final RegistryCenterNode node;
    
    private final SchemaRuleRegistryService schemaRuleService;
    
    private final RegistryCacheManager registryCacheManager;
    
    public ScalingRegistrySubscriber(final RegistryCenterRepository repository, final SchemaRuleRegistryService schemaRuleService) {
        this.repository = repository;
        node = new RegistryCenterNode();
        this.schemaRuleService = schemaRuleService;
        registryCacheManager = new RegistryCacheManager(repository, node);
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
        registryCacheManager.deleteCache(node.getRulePath(event.getSchemaName()), event.getRuleConfigurationCacheId());
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> loadCachedRuleConfigurations(final String schemaName, final String ruleConfigCacheId) {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(
                YamlEngine.unmarshal(registryCacheManager.loadCache(node.getRulePath(schemaName), ruleConfigCacheId), Collection.class));
    }
    
    /**
     * Cache rule configuration.
     *
     * @param event rule configuration cached event
     */
    @Subscribe
    public void cacheRuleConfiguration(final RuleConfigurationCachedEvent event) {
        StartScalingEvent startScalingEvent = new StartScalingEvent(event.getSchemaName(),
                repository.get(node.getMetadataDataSourcePath(event.getSchemaName())),
                repository.get(node.getRulePath(event.getSchemaName())),
                registryCacheManager.loadCache(node.getRulePath(event.getSchemaName()), event.getCacheId()), event.getCacheId());
        ShardingSphereEventBus.getInstance().post(startScalingEvent);
    }
}
