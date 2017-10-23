/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.orchestration.api;

import com.google.common.base.Charsets;
import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.instance.InstanceNode;
import io.shardingjdbc.orchestration.jdbc.datasource.CircuitBreakerDataSource;
import io.shardingjdbc.orchestration.json.DataSourceJsonConverter;
import io.shardingjdbc.orchestration.json.ShardingRuleConfigurationConverter;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration sharding data source factory.
 * 
 * @author zhangliang 
 * @author caohao 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrchestrationShardingDataSourceFactory {
    
    /**
     * Create sharding data source.
     *
     * @param config orchestration sharding configuration
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final OrchestrationShardingConfiguration config) throws SQLException {
        initRegistryCenter(config);
        ShardingDataSource result = (ShardingDataSource) ShardingDataSourceFactory.createDataSource(config.getDataSourceMap(), config.getShardingRuleConfig());
        addConfigurationChangeListener(config.getName(), config.getRegistryCenter(), result);
        addInstancesStateChangeListener(config.getName(), config.getRegistryCenter(), result);
        return result;
    }
    
    /**
     * Create sharding data source.
     * 
     * @param config orchestration sharding configuration
     * @param props properties for data source
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final OrchestrationShardingConfiguration config, final Properties props) throws SQLException {
        initRegistryCenter(config);
        // TODO props
        ShardingDataSource result = (ShardingDataSource) ShardingDataSourceFactory.createDataSource(config.getDataSourceMap(), config.getShardingRuleConfig(), props);
        addConfigurationChangeListener(config.getName(), config.getRegistryCenter(), result);
        return result;
    }
    
    private static void initRegistryCenter(final OrchestrationShardingConfiguration config) throws SQLException {
        String name = config.getName();
        CoordinatorRegistryCenter registryCenter = config.getRegistryCenter();
        registryCenter.init();
        persist(config);
        registryCenter.persistEphemeral("/" + name + "/state/instances/" + new InstanceNode().getInstanceId(), "");
        registryCenter.addCacheData("/" + name + "/config");
        registryCenter.addCacheData("/" + name + "/state/instances");
    }
    
    private static void persist(final OrchestrationShardingConfiguration config) throws SQLException {
        String name = config.getName();
        CoordinatorRegistryCenter registryCenter = config.getRegistryCenter();
        if (config.isOverwrite() || registryCenter.getChildrenKeys("/" + name + "/config").isEmpty()) {
            registryCenter.persist("/" + name + "/config/datasource", DataSourceJsonConverter.toJson(config.getDataSourceMap()));
            registryCenter.persist("/" + name + "/config/sharding", ShardingRuleConfigurationConverter.toJson(config.getShardingRuleConfig()));
        }
    }
    
    private static void addConfigurationChangeListener(final String name, final CoordinatorRegistryCenter registryCenter, final ShardingDataSource shardingDataSource) {
        TreeCache cache = (TreeCache) registryCenter.getRawCache("/" + name + "/config");
        cache.getListenable().addListener(new TreeCacheListener() {
            
            @Override
            public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
                ChildData childData = event.getData();
                if (null == childData || null == childData.getData()) {
                    return;
                }
                String path = childData.getPath();
                if (path.isEmpty()) {
                    return;
                }
                if (("/" + name + "/config/datasource").equals(path)) {
                    Map<String, DataSource> newDataSourceMap = DataSourceJsonConverter.fromJson(new String(childData.getData(), Charsets.UTF_8));
                    ShardingRuleConfiguration shardingRuleConfig = ShardingRuleConfigurationConverter.fromJson(registryCenter.get("/" + name + "/config/sharding"));
                    // TODO props
                    shardingDataSource.renew(shardingRuleConfig.build(newDataSourceMap), new Properties());
                } else if (("/" + name + "/config/sharding").equals(path)) {
                    ShardingRuleConfiguration newShardingRuleConfig = ShardingRuleConfigurationConverter.fromJson(new String(childData.getData(), Charsets.UTF_8));
                    Map<String, DataSource> dataSourceMap = DataSourceJsonConverter.fromJson(registryCenter.get("/" + name + "/config/datasource"));
                    // TODO props
                    shardingDataSource.renew(newShardingRuleConfig.build(dataSourceMap), new Properties());
                }
            }
        });
    }
    
    private static void addInstancesStateChangeListener(final String name, final CoordinatorRegistryCenter registryCenter, final ShardingDataSource shardingDataSource) {
        TreeCache cache = (TreeCache) registryCenter.getRawCache("/" + name + "/state/instances");
        cache.getListenable().addListener(new TreeCacheListener() {
            
            @Override
            public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
                ChildData childData = event.getData();
                if (null == childData || null == childData.getData()) {
                    return;
                }
                String path = childData.getPath();
                if (path.isEmpty()) {
                    return;
                }
                ShardingRuleConfiguration shardingRuleConfig = ShardingRuleConfigurationConverter.fromJson(registryCenter.get("/" + name + "/config/sharding"));
                Map<String, DataSource> dataSourceMap = DataSourceJsonConverter.fromJson(registryCenter.get("/" + name + "/config/datasource"));
                String pathValue = registryCenter.get(path);
                if ("disabled".equals(pathValue)) {
                    for (String each : dataSourceMap.keySet()) {
                        dataSourceMap.put(each, new CircuitBreakerDataSource());
                    }
                }
                shardingDataSource.renew(shardingRuleConfig.build(dataSourceMap), new Properties());
            }
        });
    }
}
