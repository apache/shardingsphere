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
import io.shardingjdbc.core.api.MasterSlaveDataSourceFactory;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.orchestration.api.config.OrchestrationMasterSlaveConfiguration;
import io.shardingjdbc.orchestration.instance.OrchestrationInstance;
import io.shardingjdbc.orchestration.jdbc.datasource.CircuitBreakerDataSource;
import io.shardingjdbc.orchestration.json.DataSourceJsonConverter;
import io.shardingjdbc.orchestration.json.GsonFactory;
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

/**
 * Orchestration master slave data source factory.
 * 
 * @author zhangliang 
 * @author caohao  
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrchestrationMasterSlaveDataSourceFactory {
    
    /**
     * Create sharding data source.
     *
     * @param config orchestration master slave configuration
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final OrchestrationMasterSlaveConfiguration config) throws SQLException {
        String instanceId = new OrchestrationInstance().getInstanceId();
        initRegistryCenter(config, instanceId);
        MasterSlaveDataSource result = (MasterSlaveDataSource) MasterSlaveDataSourceFactory.createDataSource(config.getDataSourceMap(), config.getMasterSlaveRuleConfiguration());
        addConfigurationChangeListener(config.getName(), config.getRegistryCenter(), result);
        addInstancesStateChangeListener(config.getName(), instanceId, config.getRegistryCenter(), result);
        return result;
    }
    
    private static void initRegistryCenter(final OrchestrationMasterSlaveConfiguration config, final String instanceId) throws SQLException {
        CoordinatorRegistryCenter registryCenter = config.getRegistryCenter();
        registryCenter.init();
        persistConfig(config);
        persistState(config, instanceId);
    }
    
    private static void persistConfig(final OrchestrationMasterSlaveConfiguration config) throws SQLException {
        String name = config.getName();
        CoordinatorRegistryCenter registryCenter = config.getRegistryCenter();
        if (config.isOverwrite() || registryCenter.getChildrenKeys("/" + name + "/config").isEmpty()) {
            registryCenter.persist("/" + name + "/config/datasource", DataSourceJsonConverter.toJson(config.getDataSourceMap()));
            registryCenter.persist("/" + name + "/config/masterslave", GsonFactory.getGson().toJson(config.getMasterSlaveRuleConfiguration()));
        }
        registryCenter.addCacheData("/" + name + "/config");
    }
    
    private static void persistState(final OrchestrationMasterSlaveConfiguration config, final String instanceId) throws SQLException {
        config.getRegistryCenter().persistEphemeral("/" + config.getName() + "/state/instances/" + instanceId, "");
        config.getRegistryCenter().addCacheData("/" + config.getName() + "/state/instances/" + instanceId);
    }
    
    private static void addConfigurationChangeListener(final String name, final CoordinatorRegistryCenter registryCenter, final MasterSlaveDataSource masterSlaveDataSource) {
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
                    MasterSlaveRuleConfiguration masterSlaveRuleConfig = GsonFactory.getGson().fromJson(registryCenter.get("/" + name + "/config/masterslave"), MasterSlaveRuleConfiguration.class);
                    masterSlaveDataSource.renew(masterSlaveRuleConfig.build(newDataSourceMap));
                } else if (("/" + name + "/config/masterslave").equals(path)) {
                    MasterSlaveRuleConfiguration newMasterSlaveRuleConfig = GsonFactory.getGson().fromJson(new String(childData.getData(), Charsets.UTF_8), MasterSlaveRuleConfiguration.class);
                    Map<String, DataSource> dataSourceMap = DataSourceJsonConverter.fromJson(registryCenter.get("/" + name + "/config/datasource"));
                    masterSlaveDataSource.renew(newMasterSlaveRuleConfig.build(dataSourceMap));
                }
            }
        });
    }
    
    private static void addInstancesStateChangeListener(final String name, final String instanceId, final CoordinatorRegistryCenter registryCenter, final MasterSlaveDataSource masterSlaveDataSource) {
        TreeCache cache = (TreeCache) registryCenter.getRawCache("/" + name + "/state/instances/" + instanceId);
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
