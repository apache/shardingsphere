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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.StaticDataSourceContainedRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.version.DatabaseVersionChangedEvent;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.metadata.storage.event.StorageNodeDataSourceChangedEvent;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuration changed subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ConfigurationChangedSubscriber {
    
    private final MetaDataPersistService persistService;
    
    private final RegistryCenter registryCenter;
    
    private final ContextManager contextManager;
    
    public ConfigurationChangedSubscriber(final MetaDataPersistService persistService, final RegistryCenter registryCenter, final ContextManager contextManager) {
        this.persistService = persistService;
        this.registryCenter = registryCenter;
        this.contextManager = contextManager;
        contextManager.getInstanceContext().getEventBusContext().register(this);
        disableDataSources();
    }
    
    /**
     * Renew data source configuration.
     *
     * @param event data source changed event.
     */
    @Subscribe
    public synchronized void renew(final DataSourceChangedEvent event) {
        if (persistService.getMetaDataVersionPersistService().isActiveVersion(event.getDatabaseName(), event.getDatabaseVersion())) {
            contextManager.alterDataSourceConfiguration(event.getDatabaseName(), event.getDataSourcePropertiesMap());
            disableDataSources();
        }
    }
    
    /**
     * Renew rule configurations.
     *
     * @param event rule configurations changed event
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsChangedEvent event) {
        if (persistService.getMetaDataVersionPersistService().isActiveVersion(event.getDatabaseName(), event.getDatabaseVersion())) {
            contextManager.alterRuleConfiguration(event.getDatabaseName(), event.getRuleConfigurations());
            disableDataSources();
        }
    }
    
    /**
     * Renew global rule configurations.
     *
     * @param event global rule configurations changed event
     */
    @Subscribe
    public synchronized void renew(final GlobalRuleConfigurationsChangedEvent event) {
        contextManager.alterGlobalRuleConfiguration(event.getRuleConfigurations());
        disableDataSources();
    }
    
    /**
     * Renew with new database version.
     *
     * @param event database version changed event
     */
    @Subscribe
    public synchronized void renew(final DatabaseVersionChangedEvent event) {
        Map<String, DataSourceProperties> dataSourcePropertiesMap = persistService.getDataSourceService().load(event.getDatabaseName(), event.getActiveVersion());
        Collection<RuleConfiguration> ruleConfigs = persistService.getDatabaseRulePersistService().load(event.getDatabaseName(), event.getActiveVersion());
        contextManager.alterDataSourceAndRuleConfiguration(event.getDatabaseName(), dataSourcePropertiesMap, ruleConfigs);
        disableDataSources();
    }
    
    /**
     * Renew properties.
     *
     * @param event properties changed event
     */
    @Subscribe
    public synchronized void renew(final PropertiesChangedEvent event) {
        contextManager.alterProperties(event.getProps());
    }
    
    private void disableDataSources() {
        contextManager.getMetaDataContexts().getMetaData().getDatabases().forEach((key, value) -> value.getRuleMetaData().getRules().forEach(each -> {
            if (each instanceof StaticDataSourceContainedRule) {
                disableDataSources((StaticDataSourceContainedRule) each);
            }
        }));
    }
    
    private void disableDataSources(final StaticDataSourceContainedRule rule) {
        Map<String, StorageNodeDataSource> storageNodes = registryCenter.getStorageNodeStatusService().loadStorageNodes();
        Map<String, StorageNodeDataSource> disableDataSources = storageNodes.entrySet().stream().filter(entry -> StorageNodeStatus.isDisable(entry.getValue().getStatus()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        disableDataSources.forEach((key, value) -> rule.updateStatus(new StorageNodeDataSourceChangedEvent(new QualifiedDatabase(key), value)));
    }
}
