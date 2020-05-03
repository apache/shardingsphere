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

package org.apache.shardingsphere.orchestration.core.configcenter.listener;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.collections4.SetUtils;
import org.apache.shardingsphere.core.rule.builder.ConfigurationBuilder;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.core.yaml.constructor.YamlRootShardingConfigurationConstructor;
import org.apache.shardingsphere.core.yaml.swapper.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.orchestration.center.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.orchestration.core.configcenter.ConfigCenterNode;
import org.apache.shardingsphere.orchestration.core.configcenter.ConfigCenter;
import org.apache.shardingsphere.orchestration.core.common.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.EncryptRuleChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.IgnoredShardingOrchestrationEvent;
import org.apache.shardingsphere.orchestration.core.common.event.MasterSlaveRuleChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.SchemaAddedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.SchemaDeletedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.ShardingOrchestrationEvent;
import org.apache.shardingsphere.orchestration.core.common.event.ShardingRuleChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.listener.PostShardingCenterRepositoryEventListener;
import org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration;
import org.apache.shardingsphere.orchestration.core.configuration.DataSourceConfigurationYamlSwapper;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Schema changed listener.
 */
public final class SchemaChangedListener extends PostShardingCenterRepositoryEventListener {
    
    private final ConfigCenter configurationService;
    
    private final ConfigCenterNode configurationNode;
    
    private final Collection<String> existedSchemaNames = new LinkedList<>();
    
    public SchemaChangedListener(final String name, final ConfigCenterRepository configCenterRepository, final Collection<String> shardingSchemaNames) {
        super(configCenterRepository, new ConfigCenterNode(name).getAllSchemaConfigPaths(shardingSchemaNames));
        configurationService = new ConfigCenter(name, configCenterRepository);
        configurationNode = new ConfigCenterNode(name);
        existedSchemaNames.addAll(shardingSchemaNames);
    }
    
    @Override
    protected ShardingOrchestrationEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
        if (configurationNode.getSchemaPath().equals(event.getKey())) {
            return createSchemaNamesUpdatedEvent(event.getValue());
        }
        String shardingSchemaName = configurationNode.getSchemaName(event.getKey());
        if (Strings.isNullOrEmpty(shardingSchemaName) || !isValidNodeChangedEvent(shardingSchemaName, event.getKey())) {
            return new IgnoredShardingOrchestrationEvent();
        }
        if (ChangedType.UPDATED == event.getChangedType()) {
            return createUpdatedEvent(shardingSchemaName, event);
        }
        if (ChangedType.DELETED == event.getChangedType()) {
            return createDeletedEvent(shardingSchemaName);
        }
        return new IgnoredShardingOrchestrationEvent();
    }
    
    private ShardingOrchestrationEvent createSchemaNamesUpdatedEvent(final String shardingSchemaNames) {
        Collection<String> persistShardingSchemaNames = configurationNode.splitShardingSchemaName(shardingSchemaNames);
        Set<String> addedSchemaNames = SetUtils.difference(new HashSet<>(persistShardingSchemaNames), new HashSet<>(existedSchemaNames));
        if (!addedSchemaNames.isEmpty()) {
            return createUpdatedEventForNewSchema(addedSchemaNames.iterator().next());
        }
        Set<String> deletedSchemaNames = SetUtils.difference(new HashSet<>(existedSchemaNames), new HashSet<>(persistShardingSchemaNames));
        if (!deletedSchemaNames.isEmpty()) {
            return createDeletedEvent(deletedSchemaNames.iterator().next());
        }
        return new IgnoredShardingOrchestrationEvent();
    }
    
    private boolean isValidNodeChangedEvent(final String shardingSchemaName, final String nodeFullPath) {
        return configurationNode.getDataSourcePath(shardingSchemaName).equals(nodeFullPath) || configurationNode.getRulePath(shardingSchemaName).equals(nodeFullPath);
    }
    
    private ShardingOrchestrationEvent createUpdatedEvent(final String shardingSchemaName, final DataChangedEvent event) {
        return existedSchemaNames.contains(shardingSchemaName) ? createUpdatedEventForExistedSchema(event, shardingSchemaName) : createUpdatedEventForNewSchema(shardingSchemaName);
    }
    
    private ShardingOrchestrationEvent createUpdatedEventForExistedSchema(final DataChangedEvent event, final String shardingSchemaName) {
        return event.getKey().equals(configurationNode.getDataSourcePath(shardingSchemaName)) 
                ? createDataSourceChangedEvent(shardingSchemaName, event) : createRuleChangedEvent(shardingSchemaName, event);
    }
    
    @SuppressWarnings("unchecked")
    private DataSourceChangedEvent createDataSourceChangedEvent(final String shardingSchemaName, final DataChangedEvent event) {
        Map<String, YamlDataSourceConfiguration> dataSourceConfigurations = (Map) YamlEngine.unmarshal(event.getValue());
        Preconditions.checkState(null != dataSourceConfigurations && !dataSourceConfigurations.isEmpty(), "No available data sources to load for orchestration.");
        return new DataSourceChangedEvent(shardingSchemaName, dataSourceConfigurations.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new DataSourceConfigurationYamlSwapper().swap(e.getValue()))));
    }
    
    private ShardingOrchestrationEvent createRuleChangedEvent(final String shardingSchemaName, final DataChangedEvent event) {
        return isEncryptRule(event)
                ? createEncryptRuleChangedEvent(shardingSchemaName, event.getValue()) : isShardingRule(event)
                    ? createShardingRuleChangedEvent(shardingSchemaName, event.getValue()) : createMasterSlaveRuleChangedEvent(shardingSchemaName, event.getValue());
    }
    
    private boolean isShardingRule(final DataChangedEvent event) {
        return event.getValue().contains("tables:\n") || event.getValue().contains("tables:\r\n");
    }
    
    private boolean isEncryptRule(final DataChangedEvent event) {
        return event.getValue().contains("encryptors:\n");
    }
    
    private ShardingRuleChangedEvent createShardingRuleChangedEvent(final String shardingSchemaName, final String ruleValue) {
        return new ShardingRuleChangedEvent(shardingSchemaName, ConfigurationBuilder.buildSharding(new ShardingRuleConfigurationYamlSwapper().swap(
            YamlEngine.unmarshal(ruleValue, YamlShardingRuleConfiguration.class, new YamlRootShardingConfigurationConstructor()))));
    }
    
    private EncryptRuleChangedEvent createEncryptRuleChangedEvent(final String shardingSchemaName, final String ruleValue) {
        return new EncryptRuleChangedEvent(shardingSchemaName, new EncryptRuleConfigurationYamlSwapper().swap(YamlEngine.unmarshal(ruleValue, YamlEncryptRuleConfiguration.class)));
    }
    
    private MasterSlaveRuleChangedEvent createMasterSlaveRuleChangedEvent(final String shardingSchemaName, final String ruleValue) {
        return new MasterSlaveRuleChangedEvent(shardingSchemaName, new MasterSlaveRuleConfigurationYamlSwapper().swap(YamlEngine.unmarshal(ruleValue, YamlMasterSlaveRuleConfiguration.class)));
    }
    
    private ShardingOrchestrationEvent createUpdatedEventForNewSchema(final String shardingSchemaName) {
        return isOwnCompleteConfigurations(shardingSchemaName) ? createSchemaAddedEvent(shardingSchemaName) : new IgnoredShardingOrchestrationEvent();
    }
    
    private boolean isOwnCompleteConfigurations(final String shardingSchemaName) {
        return configurationService.hasDataSourceConfiguration(shardingSchemaName) && configurationService.hasRuleConfiguration(shardingSchemaName);
    }
    
    private SchemaAddedEvent createSchemaAddedEvent(final String shardingSchemaName) {
        existedSchemaNames.add(shardingSchemaName);
        return new SchemaAddedEvent(shardingSchemaName, configurationService.loadDataSourceConfigurations(shardingSchemaName), createRuleConfiguration(shardingSchemaName));
    }
    
    private RuleConfiguration createRuleConfiguration(final String shardingSchemaName) {
        return configurationService.isEncryptRule(shardingSchemaName) 
                ? configurationService.loadEncryptRuleConfiguration(shardingSchemaName) : configurationService.isShardingRule(shardingSchemaName)
                    ? configurationService.loadShardingRuleConfiguration(shardingSchemaName) : configurationService.loadMasterSlaveRuleConfiguration(shardingSchemaName);
    }
    
    private ShardingOrchestrationEvent createDeletedEvent(final String shardingSchemaName) {
        existedSchemaNames.remove(shardingSchemaName);
        return new SchemaDeletedEvent(shardingSchemaName);
    }
}
