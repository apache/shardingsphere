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

import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.listener.ListenerManager;
import io.shardingjdbc.orchestration.internal.state.datasource.DataSourceService;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

/**
 * Configuration listener manager.
 *
 * @author caohao
 */
public final class ConfigurationListenerManager implements ListenerManager {
    
    private final ConfigurationNode configNode;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ConfigurationService configurationService;
    
    private final DataSourceService dataSourceService;
    
    public ConfigurationListenerManager(final OrchestrationConfiguration config) {
        configNode = new ConfigurationNode(config.getName());
        regCenter = config.getRegistryCenter();
        configurationService = new ConfigurationService(config);
        dataSourceService = new DataSourceService(config);
    }
    
    @Override
    public void start(final ShardingDataSource shardingDataSource) {
        start(ConfigurationNode.DATA_SOURCE_NODE_PATH, shardingDataSource);
        start(ConfigurationNode.SHARDING_RULE_NODE_PATH, shardingDataSource);
        start(ConfigurationNode.SHARDING_PROPS_NODE_PATH, shardingDataSource);
    }
    
    private void start(final String node, final ShardingDataSource shardingDataSource) {
        String cachePath = configNode.getFullPath(node);
        regCenter.addCacheData(cachePath);
        TreeCache cache = (TreeCache) regCenter.getRawCache(cachePath);
        cache.getListenable().addListener(new TreeCacheListener() {
            
            @Override
            public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
                ChildData childData = event.getData();
                if (null == childData || childData.getPath().isEmpty() || null == childData.getData() || TreeCacheEvent.Type.NODE_UPDATED != event.getType()) {
                    return;
                }
                shardingDataSource.renew(dataSourceService.getAvailableShardingRule(), configurationService.loadShardingProperties());
            }
        });
    }
    
    @Override
    public void start(final MasterSlaveDataSource masterSlaveDataSource) {
        start(ConfigurationNode.DATA_SOURCE_NODE_PATH, masterSlaveDataSource);
        start(ConfigurationNode.MASTER_SLAVE_RULE_NODE_PATH, masterSlaveDataSource);
    }
    
    private void start(final String node, final MasterSlaveDataSource masterSlaveDataSource) {
        String cachePath = configNode.getFullPath(node);
        regCenter.addCacheData(cachePath);
        TreeCache cache = (TreeCache) regCenter.getRawCache(cachePath);
        cache.getListenable().addListener(new TreeCacheListener() {
            
            @Override
            public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
                ChildData childData = event.getData();
                if (null == childData || childData.getPath().isEmpty() || null == childData.getData() || TreeCacheEvent.Type.NODE_UPDATED != event.getType()) {
                    return;
                }
                masterSlaveDataSource.renew(dataSourceService.getAvailableMasterSlaveRule());
            }
        });
    }
}
