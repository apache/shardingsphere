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

package io.shardingsphere.orchestration.internal.state.listener;

import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.orchestration.internal.state.event.CircuitStateEventBusEvent;
import io.shardingsphere.orchestration.internal.listener.OrchestrationListener;
import io.shardingsphere.orchestration.internal.state.node.StateNode;
import io.shardingsphere.orchestration.internal.state.node.StateNodeStatus;
import io.shardingsphere.orchestration.internal.state.instance.OrchestrationInstance;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.reg.listener.EventListener;

/**
 * Instance listener manager.
 *
 * @author caohao
 * @author panjuan
 */
public final class InstanceStateOrchestrationListener implements OrchestrationListener {
    
    private final StateNode stateNode;
    
    private final RegistryCenter regCenter;
    
    public InstanceStateOrchestrationListener(final String name, final RegistryCenter regCenter) {
        stateNode = new StateNode(name);
        this.regCenter = regCenter;
    }
    
    @Override
    public void watch() {
        regCenter.watch(stateNode.getInstancesNodeFullPath(OrchestrationInstance.getInstance().getInstanceId()), new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType()) {
                    if (StateNodeStatus.DISABLED.toString().equalsIgnoreCase(regCenter.get(event.getKey()))) {
                        ShardingEventBusInstance.getInstance().post(new CircuitStateEventBusEvent(true));
                    } else {
                        ShardingEventBusInstance.getInstance().post(new CircuitStateEventBusEvent(false));
                    }
                }
            }
        });
    }
}
