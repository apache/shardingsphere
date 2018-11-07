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

package io.shardingsphere.orchestration.internal.config.listener;

import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.orchestration.internal.config.node.ConfigurationNode;
import io.shardingsphere.orchestration.internal.config.service.ConfigurationService;
import io.shardingsphere.orchestration.internal.config.event.MasterSlaveRuleChangedEvent;
import io.shardingsphere.orchestration.internal.config.event.ShardingRuleChangedEvent;
import io.shardingsphere.orchestration.internal.listener.OrchestrationListener;
import io.shardingsphere.orchestration.internal.state.service.DataSourceService;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.reg.listener.EventListener;

/**
 * Rule listener manager.
 *
 * @author caohao
 * @author panjuan
 */
public final class RuleOrchestrationListener implements OrchestrationListener {
    
    private final ConfigurationNode configNode;
    
    private final RegistryCenter regCenter;
    
    private final String shardingSchemaName;
    
    private final ConfigurationService configService;
    
    private final DataSourceService dataSourceService;
    
    public RuleOrchestrationListener(final String name, final RegistryCenter regCenter, final String shardingSchemaName) {
        configNode = new ConfigurationNode(name);
        this.regCenter = regCenter;
        this.shardingSchemaName = shardingSchemaName;
        configService = new ConfigurationService(name, regCenter);
        dataSourceService = new DataSourceService(name, regCenter);
    }
    
    @Override
    public void watch() {
        regCenter.watch(configNode.getRulePath(shardingSchemaName), new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType()) {
                    ShardingEventBusInstance.getInstance().post(configService.isShardingRule(shardingSchemaName) ? getShardingConfigurationChangedEvent() : getMasterSlaveConfigurationChangedEvent());
                }
            }
            
            private MasterSlaveRuleChangedEvent getMasterSlaveConfigurationChangedEvent() {
                return new MasterSlaveRuleChangedEvent(shardingSchemaName, dataSourceService.getAvailableMasterSlaveRuleConfiguration(shardingSchemaName));
            }
            
            private ShardingRuleChangedEvent getShardingConfigurationChangedEvent() {
                return new ShardingRuleChangedEvent(shardingSchemaName, dataSourceService.getAvailableShardingRuleConfiguration(shardingSchemaName));
            }
        });
    }
}
