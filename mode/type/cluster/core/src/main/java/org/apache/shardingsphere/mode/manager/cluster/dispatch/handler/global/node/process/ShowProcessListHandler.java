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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.node.process;

import org.apache.shardingsphere.infra.executor.sql.process.lock.ProcessOperationLockRegistry;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.process.ClusterProcessPersistCoordinator;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.global.node.compute.process.ShowProcessListTriggerNodePath;

import java.util.Arrays;
import java.util.Collection;

/**
 * Show process list handler.
 */
public final class ShowProcessListHandler implements GlobalDataChangedEventHandler {
    
    @Override
    public NodePath getSubscribedNodePath() {
        return new ShowProcessListTriggerNodePath(null);
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.DELETED);
    }
    
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        if (!NodePathSearcher.isMatchedPath(event.getKey(), ShowProcessListTriggerNodePath.createProcessIdSearchCriteria())) {
            return;
        }
        String instanceId = NodePathSearcher.get(event.getKey(), ShowProcessListTriggerNodePath.createInstanceIdSearchCriteria());
        String processId = NodePathSearcher.get(event.getKey(), ShowProcessListTriggerNodePath.createProcessIdSearchCriteria());
        switch (event.getType()) {
            case ADDED:
                if (instanceId.equals(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getId())) {
                    new ClusterProcessPersistCoordinator(contextManager.getPersistServiceFacade().getRepository()).reportLocalProcesses(instanceId, processId);
                }
                break;
            case DELETED:
                ProcessOperationLockRegistry.getInstance().notify(processId);
                break;
            default:
        }
    }
}
