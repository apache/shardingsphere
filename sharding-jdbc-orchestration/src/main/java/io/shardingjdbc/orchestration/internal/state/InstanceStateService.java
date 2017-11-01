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

package io.shardingjdbc.orchestration.internal.state;

import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.config.ConfigurationService;
import io.shardingjdbc.orchestration.internal.jdbc.datasource.CircuitBreakerDataSource;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import lombok.Getter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Instance state service.
 * 
 * @author caohao
 */
@Getter
public final class InstanceStateService {
    
    private final InstanceStateNode instanceStateNode;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ConfigurationService configurationService;
    
    public InstanceStateService(final OrchestrationConfiguration config) {
        instanceStateNode = new InstanceStateNode(config.getName());
        regCenter = config.getRegistryCenter();
        configurationService = new ConfigurationService(config);
    }
    
    /**
     * Persist sharding instance online.
     *
     * @param shardingDataSource sharding datasource
     */
    public void persistShardingInstanceOnline(final ShardingDataSource shardingDataSource) {
        String instanceNodePath = instanceStateNode.getFullPath();
        regCenter.persistEphemeral(instanceNodePath, "");
        regCenter.addCacheData(instanceNodePath);
        addShardingInstancesStateChangeListener(instanceNodePath, shardingDataSource);
    }
    
    private void addShardingInstancesStateChangeListener(final String instanceNodePath, final ShardingDataSource shardingDataSource) {
        TreeCache cache = (TreeCache) regCenter.getRawCache(instanceNodePath);
        cache.getListenable().addListener(new TreeCacheListener() {
            
            @Override
            public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
                ChildData childData = event.getData();
                if (null == childData || null == childData.getData() || childData.getPath().isEmpty() || TreeCacheEvent.Type.NODE_UPDATED != event.getType()) {
                    return;
                }
                Map<String, DataSource> dataSourceMap = configurationService.loadDataSourceMap();
                if (InstanceState.DISABLED.toString().equalsIgnoreCase(regCenter.get(childData.getPath()))) {
                    for (String each : dataSourceMap.keySet()) {
                        dataSourceMap.put(each, new CircuitBreakerDataSource());
                    }
                }
                shardingDataSource.renew(configurationService.loadShardingRuleConfiguration().build(dataSourceMap), configurationService.loadShardingProperties());
            }
        });
    }
    
    /**
     * Persist master-salve instance online.
     *
     * @param masterSlaveDataSource master-slave datasource
     */
    public void persistMasterSlaveInstanceOnline(final MasterSlaveDataSource masterSlaveDataSource) {
        String instanceNodePath = instanceStateNode.getFullPath();
        regCenter.persistEphemeral(instanceNodePath, "");
        regCenter.addCacheData(instanceNodePath);
        addMasterSlaveInstancesStateChangeListener(instanceNodePath, masterSlaveDataSource);
    }
    
    private void addMasterSlaveInstancesStateChangeListener(final String instanceNodePath, final MasterSlaveDataSource masterSlaveDataSource) {
        TreeCache cache = (TreeCache) regCenter.getRawCache(instanceNodePath);
        cache.getListenable().addListener(new TreeCacheListener() {
            
            @Override
            public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
                ChildData childData = event.getData();
                if (null == childData || null == childData.getData() || childData.getPath().isEmpty() || TreeCacheEvent.Type.NODE_UPDATED != event.getType()) {
                    return;
                }
                Map<String, DataSource> dataSourceMap = configurationService.loadDataSourceMap();
                if (InstanceState.DISABLED.toString().equalsIgnoreCase(regCenter.get(childData.getPath()))) {
                    for (String each : dataSourceMap.keySet()) {
                        dataSourceMap.put(each, new CircuitBreakerDataSource());
                    }
                }
                masterSlaveDataSource.renew(configurationService.loadMasterSlaveRuleConfiguration().build(dataSourceMap));
            }
        });
    }
}
