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

import com.google.common.base.Charsets;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.internal.config.ConfigurationNode;
import io.shardingjdbc.orchestration.internal.jdbc.datasource.CircuitBreakerDataSource;
import io.shardingjdbc.orchestration.internal.json.DataSourceJsonConverter;
import io.shardingjdbc.orchestration.internal.json.GsonFactory;
import io.shardingjdbc.orchestration.internal.json.ShardingRuleConfigurationConverter;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * Instance state service.
 * 
 * @author caohao
 */
@RequiredArgsConstructor
@Getter
public final class InstanceStateService {
    
    private final String name;
    
    private final CoordinatorRegistryCenter registryCenter;
    
    /**
     * Add sharding state.
     *
     * @param shardingDataSource sharding datasource
     */
    public void addShardingState(final ShardingDataSource shardingDataSource) {
        String instanceNodePath = "/" + name + InstanceStateNode.ROOT + new InstanceStateNode().getInstanceId();
        persistState(instanceNodePath);
        addShardingInstancesStateChangeListener(instanceNodePath, shardingDataSource);
    }
    
    /**
     * Add master salve state.
     *
     * @param masterSlaveDataSource master-slave datasource
     */
    public void addMasterSlaveState(final MasterSlaveDataSource masterSlaveDataSource) {
        String instanceNodePath = "/" + name + InstanceStateNode.ROOT + new InstanceStateNode().getInstanceId();
        persistState(instanceNodePath);
        addMasterSlaveInstancesStateChangeListener(instanceNodePath, masterSlaveDataSource);
    }
    
    private void persistState(final String instanceNodePath) {
        registryCenter.persistEphemeral(instanceNodePath, "");
        registryCenter.addCacheData(instanceNodePath);
    }
    
    private void addShardingInstancesStateChangeListener(final String instanceNodePath, final ShardingDataSource shardingDataSource) {
        TreeCache cache = (TreeCache) registryCenter.getRawCache(instanceNodePath);
        cache.getListenable().addListener(new TreeCacheListener() {
            
            @Override
            public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
                ChildData childData = event.getData();
                if (null == childData || null == childData.getData()) {
                    return;
                }
                String path = childData.getPath();
                if (path.isEmpty() || TreeCacheEvent.Type.NODE_UPDATED != event.getType()) {
                    return;
                }
                ShardingRuleConfiguration shardingRuleConfig = ShardingRuleConfigurationConverter.fromJson(registryCenter.get("/" + name + "/" + ConfigurationNode.SHARDING_NODE_PATH));
                Map<String, DataSource> dataSourceMap = DataSourceJsonConverter.fromJson(registryCenter.get("/" + name + "/" + ConfigurationNode.DATA_SOURCE_NODE_PATH));
                if (InstanceState.DISABLED.toString().equalsIgnoreCase(registryCenter.get(path))) {
                    for (String each : dataSourceMap.keySet()) {
                        dataSourceMap.put(each, new CircuitBreakerDataSource());
                    }
                }
                // TODO props
                shardingDataSource.renew(shardingRuleConfig.build(dataSourceMap), new Properties());
            }
        });
    }
    
    private void addMasterSlaveInstancesStateChangeListener(final String instanceNodePath, final MasterSlaveDataSource masterSlaveDataSource) {
        TreeCache cache = (TreeCache) registryCenter.getRawCache(instanceNodePath);
        cache.getListenable().addListener(new TreeCacheListener() {
            
            @Override
            public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
                ChildData childData = event.getData();
                if (null == childData || null == childData.getData()) {
                    return;
                }
                String path = childData.getPath();
                if (path.isEmpty() || TreeCacheEvent.Type.NODE_UPDATED != event.getType()) {
                    return;
                }
                MasterSlaveRuleConfiguration masterSlaveRuleConfig = GsonFactory.getGson().fromJson(new String(childData.getData(), Charsets.UTF_8), MasterSlaveRuleConfiguration.class);
                Map<String, DataSource> dataSourceMap = DataSourceJsonConverter.fromJson(registryCenter.get("/" + name + "/config/datasource"));
                if ("disabled".equals(registryCenter.get(path))) {
                    for (String each : dataSourceMap.keySet()) {
                        dataSourceMap.put(each, new CircuitBreakerDataSource());
                    }
                }
                // TODO props
                masterSlaveDataSource.renew(masterSlaveRuleConfig.build(dataSourceMap));
            }
        });
    }
}
