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
import io.shardingjdbc.orchestration.internal.config.ConfigurationService;
import io.shardingjdbc.orchestration.internal.state.InstanceStateNode;
import io.shardingjdbc.orchestration.internal.jdbc.datasource.CircuitBreakerDataSource;
import io.shardingjdbc.orchestration.internal.json.DataSourceJsonConverter;
import io.shardingjdbc.orchestration.internal.json.GsonFactory;
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
        config.getRegistryCenter().init();
        MasterSlaveDataSource result = (MasterSlaveDataSource) MasterSlaveDataSourceFactory.createDataSource(config.getDataSourceMap(), config.getMasterSlaveRuleConfiguration());
        new ConfigurationService(config.getRegistryCenter(), config.getName()).addMasterSlaveConfiguration(config, result);
        addState(config, result);
        return result;
    }
    
    private static void addState(final OrchestrationMasterSlaveConfiguration config, final MasterSlaveDataSource masterSlaveDataSource) throws SQLException {
        String instanceId = new InstanceStateNode().getInstanceId();
        persistState(config, instanceId);
        addInstancesStateChangeListener(config.getName(), instanceId, config.getRegistryCenter(), masterSlaveDataSource);
    }
    
    private static void persistState(final OrchestrationMasterSlaveConfiguration config, final String instanceId) throws SQLException {
        config.getRegistryCenter().persistEphemeral("/" + config.getName() + "/state/instances/" + instanceId, "");
        config.getRegistryCenter().addCacheData("/" + config.getName() + "/state/instances/" + instanceId);
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
