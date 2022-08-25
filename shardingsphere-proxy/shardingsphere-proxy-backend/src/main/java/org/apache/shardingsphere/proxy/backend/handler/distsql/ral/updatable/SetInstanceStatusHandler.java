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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetInstanceStatusStatement;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.ComputeNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ComputeNodeStatusChangedEvent;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.UpdatableRALBackendHandler;

/**
 * Set instance status handler.
 */
public final class SetInstanceStatusHandler extends UpdatableRALBackendHandler<SetInstanceStatusStatement> {
    
    @Override
    protected void update(final ContextManager contextManager) {
        if (!contextManager.getInstanceContext().isCluster()) {
            throw new UnsupportedOperationException("Only allowed in cluster mode");
        }
        String instanceId = getSqlStatement().getInstanceId();
        boolean isDisable = "DISABLE".equals(getSqlStatement().getStatus());
        if (isDisable) {
            checkDisablingIsValid(contextManager, instanceId);
        } else {
            checkEnablingIsValid(contextManager, instanceId);
        }
        contextManager.getInstanceContext().getEventBusContext().post(new ComputeNodeStatusChangedEvent(isDisable ? ComputeNodeStatus.CIRCUIT_BREAK : ComputeNodeStatus.ONLINE, instanceId));
    }
    
    private void checkEnablingIsValid(final ContextManager contextManager, final String instanceId) {
        if (!contextManager.getInstanceContext().getComputeNodeInstanceById(instanceId).isPresent()) {
            throw new UnsupportedOperationException(String.format("`%s` does not exist", instanceId));
        }
    }
    
    private void checkDisablingIsValid(final ContextManager contextManager, final String instanceId) {
        if (contextManager.getInstanceContext().getInstance().getCurrentInstanceId().equals(instanceId)) {
            throw new UnsupportedOperationException(String.format("`%s` is the currently in use instance and cannot be disabled", instanceId));
        }
        if (!contextManager.getInstanceContext().getComputeNodeInstanceById(instanceId).isPresent()) {
            throw new UnsupportedOperationException(String.format("`%s` does not exist", instanceId));
        }
        if (contextManager.getInstanceContext().getComputeNodeInstanceById(instanceId).get().getState().getCurrentState() == StateType.CIRCUIT_BREAK) {
            throw new UnsupportedOperationException(String.format("`%s` compute node has been disabled", instanceId));
        }
    }
}
