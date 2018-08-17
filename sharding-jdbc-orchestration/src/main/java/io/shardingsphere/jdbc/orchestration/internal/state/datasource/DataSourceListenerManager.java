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

package io.shardingsphere.jdbc.orchestration.internal.state.datasource;

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationProxyConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.jdbc.state.JdbcStateEventBusEvent;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.jdbc.state.JdbcStateEventBusInstance;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.proxy.ProxyEventBusEvent;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.proxy.ProxyEventBusInstance;
import io.shardingsphere.jdbc.orchestration.internal.listener.ListenerManager;
import io.shardingsphere.jdbc.orchestration.internal.state.StateNode;
import io.shardingsphere.jdbc.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.jdbc.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.jdbc.orchestration.reg.listener.EventListener;

/**
 * Data source listener manager.
 *
 * @author caohao
 * @author panjuan
 */
public final class DataSourceListenerManager implements ListenerManager {
    
    private final StateNode stateNode;
    
    private final RegistryCenter regCenter;
    
    private final DataSourceService dataSourceService;
    
    public DataSourceListenerManager(final String name, final RegistryCenter regCenter) {
        stateNode = new StateNode(name);
        this.regCenter = regCenter;
        dataSourceService = new DataSourceService(name, regCenter);
    }
    
    @Override
    public void shardingStart() {
        regCenter.watch(stateNode.getDataSourcesNodeFullPath(), new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType() || DataChangedEvent.Type.DELETED == event.getEventType()) {
                    JdbcStateEventBusEvent jdbcStateEventBusEvent = new JdbcStateEventBusEvent();
                    jdbcStateEventBusEvent.getDisabledDataSourceNames().addAll(dataSourceService.getDisabledDataSourceNames());
                    JdbcStateEventBusInstance.getInstance().post(jdbcStateEventBusEvent);
                }
            }
        });
    }
    
    @Override
    public void masterSlaveStart() {
        regCenter.watch(stateNode.getDataSourcesNodeFullPath(), new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType() || DataChangedEvent.Type.DELETED == event.getEventType()) {
                    JdbcStateEventBusEvent jdbcStateEventBusEvent = new JdbcStateEventBusEvent();
                    jdbcStateEventBusEvent.getDisabledDataSourceNames().addAll(dataSourceService.getDisabledDataSourceNames());
                    JdbcStateEventBusInstance.getInstance().post(jdbcStateEventBusEvent);
                }
            }
        });
    }
    
    @Override
    public void proxyStart() {
        regCenter.watch(stateNode.getDataSourcesNodeFullPath(), new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType() || DataChangedEvent.Type.DELETED == event.getEventType()) {
                    OrchestrationProxyConfiguration availableYamlProxyConfiguration = dataSourceService.getAvailableYamlProxyConfiguration();
                    if (availableYamlProxyConfiguration.getShardingRule().getTables().isEmpty() && availableYamlProxyConfiguration.getMasterSlaveRule().getSlaveDataSourceNames().isEmpty()) {
                        throw new ShardingException("No available slave datasource, can't apply the configuration!");
                    }
                    ProxyEventBusInstance.getInstance().post(new ProxyEventBusEvent(dataSourceService.getAvailableDataSourceParameters(), availableYamlProxyConfiguration));
                }
            }
        });
    }
}
