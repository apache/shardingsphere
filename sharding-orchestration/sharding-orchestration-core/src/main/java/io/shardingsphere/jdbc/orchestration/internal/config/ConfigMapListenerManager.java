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

package io.shardingsphere.jdbc.orchestration.internal.config;

import io.shardingsphere.core.api.ConfigMapContext;
import io.shardingsphere.jdbc.orchestration.internal.listener.ListenerManager;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.reg.listener.EventListener;

/**
 * Config map listener manager.
 *
 * @author caohao
 * @author panjuan
 */
public final class ConfigMapListenerManager implements ListenerManager {
    
    private final ConfigurationNode configNode;
    
    private final RegistryCenter regCenter;
    
    private final ConfigurationService configService;
    
    public ConfigMapListenerManager(final String name, final RegistryCenter regCenter) {
        configNode = new ConfigurationNode(name);
        this.regCenter = regCenter;
        configService = new ConfigurationService(name, regCenter);
    }
    
    @Override
    public void watchSharding() {
        String cachePath = configNode.getFullPath(ConfigurationNode.SHARDING_CONFIG_MAP_NODE_PATH);
        regCenter.watch(cachePath, new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType()) {
                    ConfigMapContext.getInstance().getShardingConfig().clear();
                    ConfigMapContext.getInstance().getShardingConfig().putAll(configService.loadShardingConfigMap());
                }
            }
        });
    }
    
    @Override
    public void watchMasterSlave() {
        String cachePath = configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_CONFIG_MAP_NODE_PATH);
        regCenter.watch(cachePath, new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType()) {
                    ConfigMapContext.getInstance().getMasterSlaveConfig().clear();
                    ConfigMapContext.getInstance().getMasterSlaveConfig().putAll(configService.loadMasterSlaveConfigMap());
                }
            }
        });
    }
    
    @Override
    public void watchProxy() {
    }
}
