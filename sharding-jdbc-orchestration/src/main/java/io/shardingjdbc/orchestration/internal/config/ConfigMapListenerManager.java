/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.internal.config;

import io.shardingjdbc.core.api.ConfigMapContext;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.listener.ListenerManager;
import io.shardingjdbc.orchestration.reg.base.ChangeEvent;
import io.shardingjdbc.orchestration.reg.base.ChangeListener;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

/**
 * Config map listener manager.
 *
 * @author caohao
 */
public final class ConfigMapListenerManager implements ListenerManager {
    
    private final ConfigurationNode configNode;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ConfigurationService configurationService;
    
    public ConfigMapListenerManager(final OrchestrationConfiguration config) {
        configNode = new ConfigurationNode(config.getName());
        regCenter = config.getRegistryCenter();
        configurationService = new ConfigurationService(config);
    }
    
    @Override
    public void start(final ShardingDataSource shardingDataSource) {
        String cachePath = configNode.getFullPath(ConfigurationNode.SHARDING_CONFIG_MAP_NODE_PATH);
        regCenter.watch(cachePath, new ChangeListener() {
            @Override
            public void onChange(ChangeEvent changeEvent) throws Exception {
                // only handles the reg center updated event
                if (ChangeEvent.ChangeType.UPDATED == changeEvent.getChangeType() && changeEvent.getChangeData().isPresent()) {
                    ConfigMapContext.getInstance().getShardingConfig().clear();
                    ConfigMapContext.getInstance().getShardingConfig().putAll(configurationService.loadShardingConfigMap());
                }
            }
        });
    }
    
    @Override
    public void start(final MasterSlaveDataSource masterSlaveDataSource) {
        String cachePath = configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_CONFIG_MAP_NODE_PATH);
        regCenter.watch(cachePath, new ChangeListener() {
            @Override
            public void onChange(ChangeEvent event) throws Exception {
                // only handles the reg center update event
                if (ChangeEvent.ChangeType.UPDATED == event.getChangeType() && event.getChangeData().isPresent()) {
                    ConfigMapContext.getInstance().getMasterSlaveConfig().clear();
                    ConfigMapContext.getInstance().getMasterSlaveConfig().putAll(configurationService.loadMasterSlaveConfigMap());
                }
            }
        });
    }
}
