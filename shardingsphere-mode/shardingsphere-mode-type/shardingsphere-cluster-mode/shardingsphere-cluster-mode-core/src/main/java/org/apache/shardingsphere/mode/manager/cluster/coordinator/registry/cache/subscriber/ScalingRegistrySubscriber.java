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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.RegistryCacheManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.event.StartScalingEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.ClusterSwitchConfigurationEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.RuleConfigurationCachedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.ScalingTaskFinishedEvent;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.DataSourcePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.SchemaRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.node.SchemaMetaDataNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Scaling registry subscriber.
 */
@Slf4j
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
     * Rule configuration cached.
     *
     * @param event rule configuration cached event
     */
    @Subscribe
    public void ruleConfigurationCached(final RuleConfigurationCachedEvent event) {
        String sourceDataSource = repository.get(SchemaMetaDataNode.getMetaDataDataSourcePath(event.getSchemaName()));
        String sourceRule = repository.get(SchemaMetaDataNode.getRulePath(event.getSchemaName()));
        String targetRule = registryCacheManager.loadCache(SchemaMetaDataNode.getRulePath(event.getSchemaName()), event.getCacheId());
        String ruleCacheId = event.getCacheId();
        StartScalingEvent startScalingEvent = new StartScalingEvent(event.getSchemaName(), sourceDataSource, sourceRule, targetRule, ruleCacheId);
        ShardingSphereEventBus.getInstance().post(startScalingEvent);
    }
    
    /**
     * Scaling task finished.
     *
     * @param event scaling task finished event
     */
    @Subscribe
    public void scalingTaskFinished(final ScalingTaskFinishedEvent event) {
        log.info("scalingTaskFinished, event={}", event);
        YamlRootConfiguration yamlRootConfiguration = event.getTargetRootConfig();
        Map<String, DataSourceConfiguration> dataSourceConfigs = yamlRootConfiguration.getDataSources().entrySet().stream().collect(Collectors.toMap(
                Entry::getKey, entry -> new YamlDataSourceConfigurationSwapper().swapToDataSourceConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        Collection<RuleConfiguration> ruleConfigs = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(yamlRootConfiguration.getRules());
        ClusterSwitchConfigurationEvent switchEvent = new ClusterSwitchConfigurationEvent(event.getTargetSchemaName(), dataSourceConfigs, ruleConfigs);
        ShardingSphereEventBus.getInstance().post(switchEvent);
        String ruleCacheId = event.getRuleCacheId();
        if (null != ruleCacheId) {
            log.info("start to delete cache, ruleCacheId={}", ruleCacheId);
            registryCacheManager.deleteCache(SchemaMetaDataNode.getRulePath(event.getTargetSchemaName()), ruleCacheId);
        }
    }
    
    /**
     * Cluster switch configuration.
     *
     * @param event cluster switch configuration event
     */
    @Subscribe
    public void clusterSwitchConfiguration(final ClusterSwitchConfigurationEvent event) {
        String schemaName = event.getTargetSchemaName();
        log.info("clusterSwitchConfiguration, schemaName={}", schemaName);
        dataSourcePersistService.persist(schemaName, event.getTargetDataSourceConfigs());
        persistService.persist(schemaName, event.getTargetRuleConfigs());
    }
}
