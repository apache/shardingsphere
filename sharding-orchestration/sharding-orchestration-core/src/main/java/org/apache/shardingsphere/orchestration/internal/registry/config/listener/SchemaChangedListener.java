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

package org.apache.shardingsphere.orchestration.internal.registry.config.listener;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.api.config.RuleConfiguration;
import org.apache.shardingsphere.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.core.yaml.config.encrypt.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.EncryptRuleChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.IgnoredShardingOrchestrationEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.MasterSlaveRuleChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.SchemaAddedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.SchemaDeletedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.ShardingRuleChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.node.ConfigurationNode;
import org.apache.shardingsphere.orchestration.internal.registry.config.service.ConfigurationService;
import org.apache.shardingsphere.orchestration.internal.registry.listener.PostShardingOrchestrationEventListener;
import org.apache.shardingsphere.orchestration.internal.registry.listener.ShardingOrchestrationEvent;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.orchestration.yaml.config.YamlDataSourceConfiguration;
import org.apache.shardingsphere.orchestration.yaml.swapper.DataSourceConfigurationYamlSwapper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Schema changed listener.
 *
 * @author panjuan
 */
public final class SchemaChangedListener extends PostShardingOrchestrationEventListener {
    
    private final ConfigurationService configurationService;
    
    private final ConfigurationNode configurationNode;
    
    private final Collection<String> existedSchemaNames = new LinkedList<>();
    
    public SchemaChangedListener(final String name, final RegistryCenter regCenter, final Collection<String> shardingSchemaNames) {
        super(regCenter, new ConfigurationNode(name).getSchemaPath());
        configurationService = new ConfigurationService(name, regCenter);
        configurationNode = new ConfigurationNode(name);
        existedSchemaNames.addAll(shardingSchemaNames);
    }
    
    @Override
    protected ShardingOrchestrationEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
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
        return new DataSourceChangedEvent(shardingSchemaName, Maps.transformValues(dataSourceConfigurations, new Function<YamlDataSourceConfiguration, DataSourceConfiguration>() {
            
            @Override
            public DataSourceConfiguration apply(final YamlDataSourceConfiguration input) {
                return new DataSourceConfigurationYamlSwapper().swap(input);
            }
        }));
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
        return new ShardingRuleChangedEvent(shardingSchemaName, new ShardingRuleConfigurationYamlSwapper().swap(YamlEngine.unmarshal(ruleValue, YamlShardingRuleConfiguration.class)));
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
