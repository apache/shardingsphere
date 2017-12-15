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

import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.internal.config.ConfigurationService;
import io.shardingjdbc.orchestration.internal.jdbc.datasource.CircuitBreakerDataSource;
import io.shardingjdbc.orchestration.internal.listener.ListenerManager;
import io.shardingjdbc.orchestration.internal.state.StateNode;
import io.shardingjdbc.orchestration.internal.state.StateNodeStatus;
import io.shardingjdbc.orchestration.reg.api.RegistryCenter;
import io.shardingjdbc.orchestration.reg.listener.DataChangedEvent;
import io.shardingjdbc.orchestration.reg.listener.EventListener;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * Instance listener manager.
 *
 * @author caohao
 */
public final class InstanceListenerManager implements ListenerManager {
    
    private final StateNode stateNode;
    
    private final RegistryCenter regCenter;
    
    private final ConfigurationService configService;
    
    public InstanceListenerManager(final String name, final RegistryCenter regCenter) {
        stateNode = new StateNode(name);
        this.regCenter = regCenter;
        configService = new ConfigurationService(name, regCenter);
    }
    
    @Override
    public void start(final ShardingDataSource shardingDataSource) {
        regCenter.watch(stateNode.getInstancesNodeFullPath(new OrchestrationInstance().getInstanceId()), new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType()) {
                    Map<String, DataSource> dataSourceMap = configService.loadDataSourceMap();
                    if (StateNodeStatus.DISABLED.toString().equalsIgnoreCase(regCenter.get(event.getKey()))) {
                        for (String each : dataSourceMap.keySet()) {
                            dataSourceMap.put(each, new CircuitBreakerDataSource());
                        }
                    }
                    try {
                        shardingDataSource.renew(configService.loadShardingRuleConfiguration().build(dataSourceMap), configService.loadShardingProperties());
                    } catch (final SQLException ex) {
                        throw new ShardingJdbcException(ex);
                    }
                }
            }
        });
    }
    
    @Override
    public void start(final MasterSlaveDataSource masterSlaveDataSource) {
        regCenter.watch(stateNode.getInstancesNodeFullPath(new OrchestrationInstance().getInstanceId()), new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType()) {
                    Map<String, DataSource> dataSourceMap = configService.loadDataSourceMap();
                    if (StateNodeStatus.DISABLED.toString().equalsIgnoreCase(regCenter.get(event.getKey()))) {
                        for (String each : dataSourceMap.keySet()) {
                            dataSourceMap.put(each, new CircuitBreakerDataSource());
                        }
                    }
                    masterSlaveDataSource.renew(configService.loadMasterSlaveRuleConfiguration().build(dataSourceMap));
                }
            }
        });
    }
}
