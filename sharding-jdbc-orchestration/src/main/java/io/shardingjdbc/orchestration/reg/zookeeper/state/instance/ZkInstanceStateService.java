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

package io.shardingjdbc.orchestration.reg.zookeeper.state.instance;

import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.reg.base.*;
import io.shardingjdbc.orchestration.internal.jdbc.datasource.CircuitBreakerDataSource;
import io.shardingjdbc.orchestration.reg.zookeeper.state.StateNodeStatus;
import lombok.Getter;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Instance state service.
 * 
 * @author caohao
 */
@Getter
public final class ZkInstanceStateService implements InstanceStateService {
    
    private final InstanceStateNode instanceStateNode;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ConfigurationService configurationService;
    
    public ZkInstanceStateService(final String name, final ConfigurationService configurationService, final CoordinatorRegistryCenter registryCenter) {
        this.regCenter = registryCenter;
        this.configurationService = configurationService;
        this.instanceStateNode = new InstanceStateNode(name);
    }
    
    /**
     * Persist sharding instance online.
     *
     * @param shardingDataSource sharding datasource
     */
    @Override
    public void persistShardingInstanceOnline(final ShardingDataSource shardingDataSource) {
        String instanceNodePath = instanceStateNode.getFullPath();
        regCenter.persistEphemeral(instanceNodePath, "");
        regCenter.addCacheData(instanceNodePath);
        addShardingInstancesStateChangeListener(instanceNodePath, shardingDataSource);
    }
    
    private void addShardingInstancesStateChangeListener(final String instanceNodePath, final ShardingDataSource shardingDataSource) {
        regCenter.addRegistryChangeListener(instanceNodePath, new RegistryChangeListener() {
            @Override
            public void onRegistryChange(RegistryChangeEvent registryChangeEvent) throws Exception {
                if (RegistryChangeType.UPDATED == registryChangeEvent.getType() && registryChangeEvent.getPayload().isPresent()) {
                    String instanceStateKey = registryChangeEvent.getPayload().get().getKey();
                    Map<String, DataSource> dataSourceMap = configurationService.loadDataSourceMap();
                    if (StateNodeStatus.DISABLED.toString().equalsIgnoreCase(regCenter.get(instanceStateKey))) {
                        for (String each : dataSourceMap.keySet()) {
                            dataSourceMap.put(each, new CircuitBreakerDataSource());
                        }
                    }
                    shardingDataSource.renew(configurationService.loadShardingRuleConfiguration().build(dataSourceMap), configurationService.loadShardingProperties());
                }
            }
        });
    }
    
    /**
     * Persist master-salve instance online.
     *
     * @param masterSlaveDataSource master-slave datasource
     */
    @Override
    public void persistMasterSlaveInstanceOnline(final MasterSlaveDataSource masterSlaveDataSource) {
        String instanceNodePath = instanceStateNode.getFullPath();
        regCenter.persistEphemeral(instanceNodePath, "");
        regCenter.addCacheData(instanceNodePath);
        addMasterSlaveInstancesStateChangeListener(instanceNodePath, masterSlaveDataSource);
    }
    
    private void addMasterSlaveInstancesStateChangeListener(final String instanceNodePath, final MasterSlaveDataSource masterSlaveDataSource) {
        regCenter.addRegistryChangeListener(instanceNodePath, new RegistryChangeListener() {
            @Override
            public void onRegistryChange(RegistryChangeEvent registryChangeEvent) throws Exception {
                if (RegistryChangeType.UPDATED == registryChangeEvent.getType() && registryChangeEvent.getPayload().isPresent()) {
                    String instanceKey = registryChangeEvent.getPayload().get().getKey();
                    Map<String, DataSource> dataSourceMap = configurationService.loadDataSourceMap();
                    if (StateNodeStatus.DISABLED.toString().equalsIgnoreCase(regCenter.get(instanceKey))) {
                        for (String each : dataSourceMap.keySet()) {
                            dataSourceMap.put(each, new CircuitBreakerDataSource());
                        }
                    }
                    masterSlaveDataSource.renew(configurationService.loadMasterSlaveRuleConfiguration().build(dataSourceMap));
                }
            }
        });
    }
}
