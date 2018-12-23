/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal.registry.config.listener;

import com.google.common.base.Strings;
import io.shardingsphere.api.config.rule.RuleConfiguration;
import io.shardingsphere.orchestration.internal.registry.config.event.DataSourceChangedEvent;
import io.shardingsphere.orchestration.internal.registry.config.event.IgnoredChangedEvent;
import io.shardingsphere.orchestration.internal.registry.config.event.MasterSlaveRuleChangedEvent;
import io.shardingsphere.orchestration.internal.registry.config.event.SchemaAddChangedEvent;
import io.shardingsphere.orchestration.internal.registry.config.event.SchemaDeleteChangedEvent;
import io.shardingsphere.orchestration.internal.registry.config.event.ShardingRuleChangedEvent;
import io.shardingsphere.orchestration.internal.registry.config.node.ConfigurationNode;
import io.shardingsphere.orchestration.internal.registry.config.service.ConfigurationService;
import io.shardingsphere.orchestration.internal.registry.listener.PostShardingOrchestrationEventListener;
import io.shardingsphere.orchestration.internal.registry.listener.ShardingOrchestrationEvent;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent.ChangedType;
import io.shardingsphere.orchestration.yaml.ConfigurationYamlConverter;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Schema changed listener.
 *
 * @author panjuan
 */
public final class SchemaChangedListener extends PostShardingOrchestrationEventListener {
    
    private final ConfigurationService configurationService;
    
    private final ConfigurationNode configurationNode;
    
    private final Collection<String> existedSchemaNames = new LinkedList<>();
    
    public SchemaChangedListener(final String name, final RegistryCenter regCenter, final Collection<String> shardingSchemaNames, final ConfigurationService configurationService) {
        super(regCenter, new ConfigurationNode(name).getSchemaPath());
        this.configurationService = configurationService;
        configurationNode = new ConfigurationNode(name);
        existedSchemaNames.addAll(shardingSchemaNames);
    }
    
    @Override
    protected ShardingOrchestrationEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
        String shardingSchemaName = configurationNode.getSchemaName(event.getKey());
        if (Strings.isNullOrEmpty(shardingSchemaName)) {
            return new IgnoredChangedEvent();
        }
        if (ChangedType.UPDATED == event.getChangedType()) {
            return createUpdateChangedEvent(shardingSchemaName, event);
        }
        if (ChangedType.DELETED == event.getChangedType()) {
            return createDeleteChangedEvent(shardingSchemaName);
        }
        return new IgnoredChangedEvent();
    }
    
    private ShardingOrchestrationEvent createUpdateChangedEvent(final String shardingSchemaName, final DataChangedEvent event) {
        if (existedSchemaNames.contains(shardingSchemaName)) {
            return createChangedEventForExistedSchema(event, shardingSchemaName);
        } else {
            return createChangedEventForNewSchema(shardingSchemaName);
        }
    }
    
    private ShardingOrchestrationEvent createDeleteChangedEvent(final String shardingSchemaName) {
        existedSchemaNames.remove(shardingSchemaName);
        return new SchemaDeleteChangedEvent(shardingSchemaName);
    }
    
    private ShardingOrchestrationEvent createChangedEventForNewSchema(final String shardingSchemaName) {
        if (isSufficientToInitialize(shardingSchemaName)) {
            existedSchemaNames.add(shardingSchemaName);
            return createSchemaChangedEvent(shardingSchemaName);
        }
        return new IgnoredChangedEvent();
    }
    
    private ShardingOrchestrationEvent createChangedEventForExistedSchema(final DataChangedEvent event, final String shardingSchemaName) {
        if (event.getKey().equals(configurationNode.getDataSourcePath(shardingSchemaName))) {
            return createDataSourceChangedEvent(shardingSchemaName, event);
        }
        return createRuleChangedEvent(shardingSchemaName, event);
    }
    
    private DataSourceChangedEvent createDataSourceChangedEvent(final String shardingSchemaName, final DataChangedEvent event) {
        return new DataSourceChangedEvent(shardingSchemaName, ConfigurationYamlConverter.loadDataSourceConfigurations(event.getValue()));
    }
    
    private ShardingOrchestrationEvent createRuleChangedEvent(final String shardingSchemaName, final DataChangedEvent event) {
        return isShardingRule(event) ? getShardingRuleChangedEvent(shardingSchemaName, event.getValue()) : getMasterSlaveRuleChangedEvent(shardingSchemaName, event.getValue());
    }
    
    private boolean isShardingRule(final DataChangedEvent event) {
        return event.getValue().contains("tables:\n");
    }
    
    private ShardingRuleChangedEvent getShardingRuleChangedEvent(final String shardingSchemaName, final String ruleValue) {
        return new ShardingRuleChangedEvent(shardingSchemaName, ConfigurationYamlConverter.loadShardingRuleConfiguration(ruleValue));
    }
    
    private MasterSlaveRuleChangedEvent getMasterSlaveRuleChangedEvent(final String shardingSchemaName, final String ruleValue) {
        return new MasterSlaveRuleChangedEvent(shardingSchemaName, ConfigurationYamlConverter.loadMasterSlaveRuleConfiguration(ruleValue));
    }
    
    private boolean isSufficientToInitialize(final String shardingSchemaName) {
        return configurationService.hasDataSourceConfiguration(shardingSchemaName) && configurationService.hasRuleConfiguration(shardingSchemaName);
    }
    
    private SchemaAddChangedEvent createSchemaChangedEvent(final String shardingSchemaName) {
        return new SchemaAddChangedEvent(shardingSchemaName, configurationService.loadDataSourceConfigurations(shardingSchemaName), createRuleConfiguration(shardingSchemaName));
    }
    
    private RuleConfiguration createRuleConfiguration(final String shardingSchemaName) {
        return configurationService.isShardingRule(shardingSchemaName) ? configurationService.loadShardingRuleConfiguration(shardingSchemaName) : configurationService.loadMasterSlaveRuleConfiguration(shardingSchemaName);
    }
}
