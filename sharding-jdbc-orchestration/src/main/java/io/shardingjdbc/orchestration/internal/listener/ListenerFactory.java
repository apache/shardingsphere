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

package io.shardingjdbc.orchestration.internal.listener;

import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.internal.config.ConfigMapListenerManager;
import io.shardingjdbc.orchestration.internal.config.ConfigurationListenerManager;
import io.shardingjdbc.orchestration.internal.state.datasource.DataSourceListenerManager;
import io.shardingjdbc.orchestration.internal.state.instance.InstanceListenerManager;
import io.shardingjdbc.orchestration.reg.api.RegistryCenter;

/**
 * Registry center's listener factory.
 *
 * @author caohao
 */
public final class ListenerFactory {
    
    private final ConfigurationListenerManager configurationListenerManager;
    
    private final InstanceListenerManager instanceListenerManager;
    
    private final ConfigMapListenerManager configMapListenerManager;
    
    private final DataSourceListenerManager dataSourceListenerManager;
    
    public ListenerFactory(final String name, final RegistryCenter regCenter) {
        configurationListenerManager = new ConfigurationListenerManager(name, regCenter);
        instanceListenerManager = new InstanceListenerManager(name, regCenter);
        configMapListenerManager = new ConfigMapListenerManager(name, regCenter);
        dataSourceListenerManager = new DataSourceListenerManager(name, regCenter);
    }
    
    /**
     * Initialize listeners for sharding data source.
     * 
     * @param shardingDataSource sharding data source
     */
    public void initShardingListeners(final ShardingDataSource shardingDataSource) {
        configurationListenerManager.start(shardingDataSource);
        instanceListenerManager.start(shardingDataSource);
        dataSourceListenerManager.start(shardingDataSource);
        configMapListenerManager.start(shardingDataSource);
    }
    
    /**
     * Initialize listeners for master-slave data source.
     *
     * @param masterSlaveDataSource master-slave data source
     */
    public void initMasterSlaveListeners(final MasterSlaveDataSource masterSlaveDataSource) {
        configurationListenerManager.start(masterSlaveDataSource);
        instanceListenerManager.start(masterSlaveDataSource);
        dataSourceListenerManager.start(masterSlaveDataSource);
        configMapListenerManager.start(masterSlaveDataSource);
    }
}
