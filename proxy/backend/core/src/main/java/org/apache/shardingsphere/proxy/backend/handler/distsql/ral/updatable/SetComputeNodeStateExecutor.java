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

import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorClusterModeRequired;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.statement.ral.updatable.SetComputeNodeStateStatement;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.mode.manager.ContextManager;

/**
 * Set compute node state executor.
 */
@DistSQLExecutorClusterModeRequired
public final class SetComputeNodeStateExecutor implements DistSQLUpdateExecutor<SetComputeNodeStateStatement> {
    
    @Override
    public void executeUpdate(final SetComputeNodeStateStatement sqlStatement, final ContextManager contextManager) {
        if ("DISABLE".equals(sqlStatement.getState())) {
            checkDisablingIsValid(contextManager, sqlStatement.getInstanceId());
        } else {
            checkEnablingIsValid(contextManager, sqlStatement.getInstanceId());
        }
        contextManager.getPersistServiceFacade().getComputeNodePersistService().updateComputeNodeState(sqlStatement.getInstanceId(),
                "DISABLE".equals(sqlStatement.getState()) ? InstanceState.CIRCUIT_BREAK : InstanceState.OK);
    }
    
    private void checkEnablingIsValid(final ContextManager contextManager, final String instanceId) {
        ShardingSpherePreconditions.checkState(contextManager.getComputeNodeInstanceContext().getComputeNodeInstanceById(instanceId).isPresent(),
                () -> new UnsupportedSQLOperationException(String.format("`%s` does not exist", instanceId)));
    }
    
    private void checkDisablingIsValid(final ContextManager contextManager, final String instanceId) {
        ShardingSpherePreconditions.checkState(!contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getId().equals(instanceId),
                () -> new UnsupportedSQLOperationException(String.format("`%s` is the currently in use instance and cannot be disabled", instanceId)));
        ShardingSpherePreconditions.checkState(contextManager.getComputeNodeInstanceContext().getComputeNodeInstanceById(instanceId).isPresent(),
                () -> new UnsupportedSQLOperationException(String.format("`%s` does not exist", instanceId)));
        ShardingSpherePreconditions.checkState(InstanceState.CIRCUIT_BREAK != contextManager.getComputeNodeInstanceContext().getComputeNodeInstanceById(instanceId).get().getState().getCurrentState(),
                () -> new UnsupportedSQLOperationException(String.format("`%s` compute node has been disabled", instanceId)));
    }
    
    @Override
    public Class<SetComputeNodeStateStatement> getType() {
        return SetComputeNodeStateStatement.class;
    }
}
