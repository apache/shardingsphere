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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.excutor;

import lombok.AllArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.SetInstanceStatusStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceId;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.ComputeNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ComputeNodeStatusChangedEvent;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.SetStatementExecutor;

import java.util.Collection;
import java.util.Optional;

/**
 * Set instance status executor.
 */
@AllArgsConstructor
public final class SetInstanceStatusExecutor implements SetStatementExecutor {
    
    private final SetInstanceStatusStatement sqlStatement;
    
    @Override
    public ResponseHeader execute() throws DistSQLException {
        InstanceId operationInstanceId = new InstanceId(sqlStatement.getIp(), Integer.valueOf(sqlStatement.getPort()));
        boolean isDisable = "DISABLE".equals(sqlStatement.getStatus());
        if (isDisable) {
            checkDisablingIsValid(operationInstanceId);
        } else {
            checkEnablingIsValid(operationInstanceId);
        }
        ShardingSphereEventBus.getInstance().post(new ComputeNodeStatusChangedEvent(isDisable ? ComputeNodeStatus.CIRCUIT_BREAK : ComputeNodeStatus.ONLINE,
                sqlStatement.getIp(), sqlStatement.getPort()));
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void checkEnablingIsValid(final InstanceId operationInstanceId) {
        checkExist(operationInstanceId);
    }
    
    private void checkDisablingIsValid(final InstanceId operationInstanceId) {
        InstanceContext instanceContext = ProxyContext.getInstance().getContextManager().getInstanceContext();
        if (isIdenticalInstance(instanceContext.getInstance().getInstanceDefinition(), operationInstanceId)) {
            throw new UnsupportedOperationException(String.format("`%s` is the currently in use instance and cannot be disabled", operationInstanceId.getId()));
        }
        checkExist(operationInstanceId);
        checkExistDisabled(operationInstanceId);
    }
    
    private void checkExistDisabled(final InstanceId operationInstanceId) {
        Optional<MetaDataPersistService> metaDataPersistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService();
        if (metaDataPersistService.isPresent()) {
            metaDataPersistService.get().getComputeNodePersistService().loadAllComputeNodeInstances().forEach(each -> {
                if (each.getStatus().contains(StateType.CIRCUIT_BREAK.name()) && isIdenticalInstance(each.getInstanceDefinition(), operationInstanceId)) {
                    throw new UnsupportedOperationException(String.format("`%s` compute node has been disabled", operationInstanceId.getId()));
                }
            });
        }
    }
    
    private void checkExist(final InstanceId operationInstanceId) {
        Optional<MetaDataPersistService> metaDataPersistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService();
        metaDataPersistService.ifPresent(op -> {
            Collection<ComputeNodeInstance> computeNodeInstances = op.getComputeNodePersistService().loadAllComputeNodeInstances();
            if (computeNodeInstances.stream().noneMatch(each -> isIdenticalInstance(each.getInstanceDefinition(), operationInstanceId))) {
                throw new UnsupportedOperationException(String.format("`%s` does not exist", operationInstanceId.getId()));
            }
        });
    }
    
    private boolean isIdenticalInstance(final InstanceDefinition definition, final InstanceId instanceId) {
        return definition.getInstanceId().getId().equals(instanceId.getId());
    }
}
