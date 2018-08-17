/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.jdbc.orchestration.internal.state.instance;

import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.jdbc.orchestration.internal.config.ConfigurationService;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.jdbc.state.JdbcStateEventBusEvent;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.jdbc.state.MasterSlaveStateEventBusInstance;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.jdbc.state.ShardingStateEventBusInstance;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.proxy.ProxyEventBusEvent;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.proxy.ProxyEventBusInstance;
import io.shardingsphere.jdbc.orchestration.internal.listener.ListenerManager;
import io.shardingsphere.jdbc.orchestration.internal.state.StateNode;
import io.shardingsphere.jdbc.orchestration.internal.state.StateNodeStatus;
import io.shardingsphere.jdbc.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.jdbc.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.jdbc.orchestration.reg.listener.EventListener;

import java.util.Map;

/**
 * Instance listener manager.
 *
 * @author caohao
 * @author panjuan
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
    public void shardingStart() {
        regCenter.watch(stateNode.getInstancesNodeFullPath(OrchestrationInstance.getInstance().getInstanceId()), new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType() && StateNodeStatus.DISABLED.toString().equalsIgnoreCase(regCenter.get(event.getKey()))) {
                    JdbcStateEventBusEvent jdbcStateEventBusEvent = new JdbcStateEventBusEvent();
                    jdbcStateEventBusEvent.getCircuitBreakerDataSource().addAll(configService.loadDataSourceMap().keySet());
                    ShardingStateEventBusInstance.getInstance().post(jdbcStateEventBusEvent);
                }
            }
        });
    }
    
    @Override
    public void masterSlaveStart() {
        regCenter.watch(stateNode.getInstancesNodeFullPath(OrchestrationInstance.getInstance().getInstanceId()), new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType() && StateNodeStatus.DISABLED.toString().equalsIgnoreCase(regCenter.get(event.getKey()))) {
                    JdbcStateEventBusEvent jdbcStateEventBusEvent = new JdbcStateEventBusEvent();
                    jdbcStateEventBusEvent.getCircuitBreakerDataSource().addAll(configService.loadDataSourceMap().keySet());
                    MasterSlaveStateEventBusInstance.getInstance().post(jdbcStateEventBusEvent);
                }
            }
        });
    }
    
    @Override
    public void proxyStart() {
        regCenter.watch(stateNode.getInstancesNodeFullPath(OrchestrationInstance.getInstance().getInstanceId()), new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType()) {
                    Map<String, DataSourceParameter> dataSourceParameterMap = configService.loadDataSources();
                    if (StateNodeStatus.DISABLED.toString().equalsIgnoreCase(regCenter.get(event.getKey()))) {
                        dataSourceParameterMap.clear();
                    }
                    ProxyEventBusInstance.getInstance().post(new ProxyEventBusEvent(dataSourceParameterMap, configService.loadProxyConfiguration()));
                }
            }
        });
    }
}
