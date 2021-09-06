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

package org.apache.shardingsphere.mode.manager.cluster.governance.registry.cache.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.cache.RegistryCacheManager;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.cache.event.StartScalingEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.config.event.rule.ClusterSwitchConfigurationEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.config.event.rule.RuleConfigurationCachedEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.config.event.rule.ScalingTaskFinishedEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.config.event.rule.SwitchRuleConfigurationEvent;
import org.apache.shardingsphere.mode.persist.service.impl.DataSourcePersistService;
import org.apache.shardingsphere.mode.persist.service.impl.SchemaRulePersistService;
import org.apache.shardingsphere.mode.persist.node.SchemaMetadataNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Scaling registry subscriber.
 */
// TODO move to scaling module
public final class ScalingRegistrySubscriber {
    
    private final ClusterPersistRepository repository;
    
    private final SchemaRulePersistService persistService;
    
    private final DataSourcePersistService dataSourcePersistService;
    
    private final RegistryCacheManager registryCacheManager;
    
    public ScalingRegistrySubscriber(final ClusterPersistRepository repository) {
        this.repository = repository;
        this.persistService = new SchemaRulePersistService(repository);
        dataSourcePersistService = new DataSourcePersistService(repository);
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
        persistService.persist(event.getSchemaName(), loadCachedRuleConfigurations(event.getSchemaName(), event.getRuleConfigurationCacheId()));
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
    
    /**
     * Scaling task finished.
     *
     * @param event scaling task finished event
     */
    @Subscribe
    public void scalingTaskFinished(final ScalingTaskFinishedEvent event) {
        YamlRootConfiguration yamlRootConfiguration = YamlEngine.unmarshal(event.getTargetParameter(), YamlRootConfiguration.class);
        Map<String, DataSourceConfiguration> dataSourceConfigs = yamlRootConfiguration.getDataSources().entrySet().stream().collect(Collectors.toMap(
                Entry::getKey, entry -> new YamlDataSourceConfigurationSwapper().swapToDataSourceConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        Collection<RuleConfiguration> ruleConfigs = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(yamlRootConfiguration.getRules());
        ClusterSwitchConfigurationEvent switchEvent = new ClusterSwitchConfigurationEvent(event.getTargetSchemaName(), dataSourceConfigs, ruleConfigs);
        ShardingSphereEventBus.getInstance().post(switchEvent);
    }
    
    /**
     * Cluster switch configuration.
     *
     * @param event cluster switch configuration event
     */
    @Subscribe
    public void clusterSwitchConfiguration(final ClusterSwitchConfigurationEvent event) {
        String schemaName = event.getTargetSchemaName();
        dataSourcePersistService.persist(schemaName, event.getTargetDataSourceConfigs());
        persistService.persist(schemaName, event.getTargetRuleConfigs());
    }
}
