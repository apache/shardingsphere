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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.SetInstanceStatusStatement;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.instance.definition.InstanceId;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.ComputeNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ComputeNodeStatusChangedEvent;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.UpdatableRALBackendHandler;

/**
 * Set instance status handler.
 */
public final class SetInstanceStatusHandler extends UpdatableRALBackendHandler<SetInstanceStatusStatement, SetInstanceStatusHandler> {
    
    @Override
    protected void update(final ContextManager contextManager, final SetInstanceStatusStatement sqlStatement) {
        if (!"Cluster".equals(contextManager.getInstanceContext().getModeConfiguration().getType())) {
            throw new UnsupportedOperationException("Only allowed in cluster mode");
        }
        InstanceId operationInstanceId = new InstanceId(sqlStatement.getIp(), String.valueOf(sqlStatement.getPort()));
        boolean isDisable = "DISABLE".equals(sqlStatement.getStatus());
        if (isDisable) {
            checkDisablingIsValid(contextManager, operationInstanceId);
        } else {
            checkEnablingIsValid(contextManager, operationInstanceId);
        }
        ShardingSphereEventBus.getInstance().post(new ComputeNodeStatusChangedEvent(isDisable ? ComputeNodeStatus.CIRCUIT_BREAK : ComputeNodeStatus.ONLINE,
                sqlStatement.getIp(), sqlStatement.getPort()));
    }
    
    private void checkEnablingIsValid(final ContextManager contextManager, final InstanceId operationInstanceId) {
        if (!contextManager.getInstanceContext().getComputeNodeInstanceById(operationInstanceId.getId()).isPresent()) {
            throw new UnsupportedOperationException(String.format("`%s` does not exist", operationInstanceId.getId()));
        }
    }
    
    private void checkDisablingIsValid(final ContextManager contextManager, final InstanceId operationInstanceId) {
        if (contextManager.getInstanceContext().getInstance().getCurrentInstanceId().equals(operationInstanceId.getId())) {
            throw new UnsupportedOperationException(String.format("`%s` is the currently in use instance and cannot be disabled", operationInstanceId.getId()));
        }
        if (!contextManager.getInstanceContext().getComputeNodeInstanceById(operationInstanceId.getId()).isPresent()) {
            throw new UnsupportedOperationException(String.format("`%s` does not exist", operationInstanceId.getId()));
        }
        if (contextManager.getInstanceContext().getComputeNodeInstanceById(operationInstanceId.getId()).get().getState().getCurrentState() == StateType.CIRCUIT_BREAK) {
            throw new UnsupportedOperationException(String.format("`%s` compute node has been disabled", operationInstanceId.getId()));
        }
    }
}
