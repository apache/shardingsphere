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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.BatchYamlExecuteProcessContext;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.KillProcessListIdEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.KillProcessListIdUnitCompleteEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessListTriggerEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessListUnitCompleteEvent;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.process.ShowProcessListManager;
import org.apache.shardingsphere.mode.process.lock.ShowProcessListSimpleLock;
import org.apache.shardingsphere.mode.process.node.ProcessNode;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * Process list changed subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ProcessListChangedSubscriber {
    
    private final RegistryCenter registryCenter;
    
    private final ContextManager contextManager;
    
    public ProcessListChangedSubscriber(final RegistryCenter registryCenter, final ContextManager contextManager) {
        this.registryCenter = registryCenter;
        this.contextManager = contextManager;
        contextManager.getInstanceContext().getEventBusContext().register(this);
    }
    
    /**
     * Trigger show process list.
     *
     * @param event show process list trigger event
     */
    @Subscribe
    public synchronized void triggerShowProcessList(final ShowProcessListTriggerEvent event) {
        if (!event.getInstanceId().equals(contextManager.getInstanceContext().getInstance().getMetaData().getId())) {
            return;
        }
        Collection<ExecuteProcessContext> processContexts = ShowProcessListManager.getInstance().getAllProcessContext();
        if (!processContexts.isEmpty()) {
            registryCenter.getRepository().persist(ProcessNode.getProcessListInstancePath(event.getProcessListId(), event.getInstanceId()),
                    YamlEngine.marshal(new BatchYamlExecuteProcessContext(processContexts)));
        }
        registryCenter.getRepository().delete(ComputeNode.getProcessTriggerInstanceIdNodePath(event.getInstanceId(), event.getProcessListId()));
    }
    
    /**
     * Kill process list id.
     *
     * @param event kill process list id event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void killProcessListId(final KillProcessListIdEvent event) throws SQLException {
        if (!event.getInstanceId().equals(contextManager.getInstanceContext().getInstance().getMetaData().getId())) {
            return;
        }
        Collection<Statement> statements = ShowProcessListManager.getInstance().getProcessStatement(event.getProcessListId());
        for (Statement statement : statements) {
            statement.cancel();
        }
        registryCenter.getRepository().delete(ComputeNode.getProcessKillInstanceIdNodePath(event.getInstanceId(), event.getProcessListId()));
    }
    
    /**
     * Complete unit show process list.
     *
     * @param event show process list unit complete event
     */
    @Subscribe
    public synchronized void completeUnitShowProcessList(final ShowProcessListUnitCompleteEvent event) {
        ShowProcessListSimpleLock simpleLock = ShowProcessListManager.getInstance().getLocks().get(event.getProcessListId());
        if (null != simpleLock) {
            simpleLock.doNotify();
        }
    }
    
    /**
     * Complete unit kill process list id.
     *
     * @param event kill process list id unit complete event
     */
    @Subscribe
    public synchronized void completeUnitKillProcessListId(final KillProcessListIdUnitCompleteEvent event) {
        ShowProcessListSimpleLock simpleLock = ShowProcessListManager.getInstance().getLocks().get(event.getProcessListId());
        if (null != simpleLock) {
            simpleLock.doNotify();
        }
    }
}
