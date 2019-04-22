/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.orchestration.internal.registry.state.listener;

import org.apache.shardingsphere.orchestration.internal.registry.listener.PostShardingOrchestrationEventListener;
import org.apache.shardingsphere.orchestration.internal.registry.state.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.state.node.StateNode;
import org.apache.shardingsphere.orchestration.internal.registry.state.node.StateNodeStatus;
import org.apache.shardingsphere.orchestration.internal.registry.state.schema.OrchestrationShardingSchema;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEvent.ChangedType;

/**
 * Data source state changed listener.
 *
 * @author caohao
 * @author panjuan
 */
public final class DataSourceStateChangedListener extends PostShardingOrchestrationEventListener {
    
    private final StateNode stateNode;
    
    public DataSourceStateChangedListener(final String name, final RegistryCenter regCenter) {
        super(regCenter, new StateNode(name).getDataSourcesNodeFullRootPath());
        stateNode = new StateNode(name);
    }
    
    @Override
    protected DisabledStateChangedEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
        return new DisabledStateChangedEvent(getShardingSchema(event.getKey()), isDataSourceDisabled(event));
    }
    
    private OrchestrationShardingSchema getShardingSchema(final String dataSourceNodeFullPath) {
        return stateNode.getOrchestrationShardingSchema(dataSourceNodeFullPath);
    }
    
    private boolean isDataSourceDisabled(final DataChangedEvent event) {
        return StateNodeStatus.DISABLED.toString().equalsIgnoreCase(event.getValue()) && ChangedType.UPDATED == event.getChangedType();
    }
}
