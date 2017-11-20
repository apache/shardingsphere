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

package io.shardingjdbc.orchestration.internal.state.instance;

import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.config.ConfigurationService;
import io.shardingjdbc.orchestration.internal.jdbc.datasource.CircuitBreakerDataSource;
import io.shardingjdbc.orchestration.internal.listener.ListenerManager;
import io.shardingjdbc.orchestration.internal.state.StateNode;
import io.shardingjdbc.orchestration.internal.state.StateNodeStatus;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Instance listener manager.
 *
 * @author caohao
 */
public final class InstanceListenerManager implements ListenerManager {
    
    private final OrchestrationConfiguration config;
    
    private final StateNode stateNode;
    
    private final ConfigurationService configurationService;
    
    public InstanceListenerManager(final OrchestrationConfiguration config) {
        this.config = config;
        stateNode = new StateNode(config.getName());
        configurationService = new ConfigurationService(config);
    }
    
    @Override
    public void start(final ShardingDataSource shardingDataSource) {
        TreeCache cache = (TreeCache) config.getRegistryCenter().getRawCache(stateNode.getInstancesNodeFullPath(new OrchestrationInstance().getInstanceId()));
        cache.getListenable().addListener(new TreeCacheListener() {
            
            @Override
            public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
                ChildData childData = event.getData();
                if (null == childData || null == childData.getData() || childData.getPath().isEmpty() || TreeCacheEvent.Type.NODE_UPDATED != event.getType()) {
                    return;
                }
                Map<String, DataSource> dataSourceMap = configurationService.loadDataSourceMap();
                if (StateNodeStatus.DISABLED.toString().equalsIgnoreCase(config.getRegistryCenter().get(childData.getPath()))) {
                    for (String each : dataSourceMap.keySet()) {
                        dataSourceMap.put(each, new CircuitBreakerDataSource());
                    }
                }
                shardingDataSource.renew(configurationService.loadShardingRuleConfiguration().build(dataSourceMap), configurationService.loadShardingProperties());
            }
        });
    }
    
    @Override
    public void start(final MasterSlaveDataSource masterSlaveDataSource) {
        TreeCache cache = (TreeCache) config.getRegistryCenter().getRawCache(stateNode.getInstancesNodeFullPath(new OrchestrationInstance().getInstanceId()));
        cache.getListenable().addListener(new TreeCacheListener() {
            
            @Override
            public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
                ChildData childData = event.getData();
                if (null == childData || null == childData.getData() || childData.getPath().isEmpty() || TreeCacheEvent.Type.NODE_UPDATED != event.getType()) {
                    return;
                }
                Map<String, DataSource> dataSourceMap = configurationService.loadDataSourceMap();
                if (StateNodeStatus.DISABLED.toString().equalsIgnoreCase(config.getRegistryCenter().get(childData.getPath()))) {
                    for (String each : dataSourceMap.keySet()) {
                        dataSourceMap.put(each, new CircuitBreakerDataSource());
                    }
                }
                masterSlaveDataSource.renew(configurationService.loadMasterSlaveRuleConfiguration().build(dataSourceMap));
            }
        });
    }
}
