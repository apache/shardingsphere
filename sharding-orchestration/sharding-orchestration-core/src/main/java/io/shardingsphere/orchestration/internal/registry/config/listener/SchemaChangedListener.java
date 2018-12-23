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
import io.shardingsphere.orchestration.internal.registry.config.event.ConfigMapChangedEvent;
import io.shardingsphere.orchestration.internal.registry.config.event.DataSourceChangedEvent;
import io.shardingsphere.orchestration.internal.registry.config.event.IgnoredChangedEvent;
import io.shardingsphere.orchestration.internal.registry.config.event.MasterSlaveRuleChangedEvent;
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
import java.util.Collections;
import java.util.LinkedList;

/**
 * Schema changed listener.
 *
 * @author panjuan
 */
public final class SchemaChangedListener extends PostShardingOrchestrationEventListener {
    
    private final ConfigurationService configurationService;
    
    private final ConfigurationNode configurationNode;
    
    private final Collection<String> existedSchemas = new LinkedList<>();
    
    public SchemaChangedListener(final String name, final RegistryCenter regCenter, final ConfigurationService configurationService) {
        super(regCenter, new ConfigurationNode(name).getSchemaPath());
        this.configurationService = configurationService;
        configurationNode = new ConfigurationNode(name);
    }
    
    @Override
    protected ShardingOrchestrationEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
        String schemaName = configurationNode.getSchemaName(event.getKey());
        if (Strings.isNullOrEmpty(schemaName)) {
            return new IgnoredChangedEvent();
        }
        if (ChangedType.UPDATED == event.getChangedType()) {
            if (existedSchemas.contains(schemaName)) {
                if (event.getKey().equals(configurationNode.getDataSourcePath(schemaName))) {
                    return createDataSourceChangedEvent(schemaName, event);
                } else {
                    return createRuleChangedEvent(schemaName, event);
                }
            } else {
                
            }
        }
        
        
        System.out.println(event.getKey());
        System.out.println(event.getValue());
        if (ChangedType.DELETED == event.getChangedType()) {
            event.getKey();
        }
        return new ConfigMapChangedEvent(Collections.EMPTY_MAP);
    }
    
    private DataSourceChangedEvent createDataSourceChangedEvent(final String schemaName, final DataChangedEvent event) {
        return new DataSourceChangedEvent(schemaName, ConfigurationYamlConverter.loadDataSourceConfigurations(event.getValue()));
    }
    
    private ShardingOrchestrationEvent createRuleChangedEvent(final String schemaName, final DataChangedEvent event) {
        return isShardingRule(event) ? getShardingRuleChangedEvent(schemaName, event.getValue()) : getMasterSlaveRuleChangedEvent(schemaName, event.getValue());
    }
    
    private boolean isShardingRule(final DataChangedEvent event) {
        return event.getValue().contains("tables:\n");
    }
    
    private ShardingRuleChangedEvent getShardingRuleChangedEvent(final String schemaName, final String ruleValue) {
        return new ShardingRuleChangedEvent(schemaName, ConfigurationYamlConverter.loadShardingRuleConfiguration(ruleValue));
    }
    
    private MasterSlaveRuleChangedEvent getMasterSlaveRuleChangedEvent(final String schemaName, final String ruleValue) {
        return new MasterSlaveRuleChangedEvent(schemaName, ConfigurationYamlConverter.loadMasterSlaveRuleConfiguration(ruleValue));
    }
    
    private boolean isSufficientToInitialize()
}
