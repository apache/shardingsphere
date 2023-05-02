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
import org.apache.shardingsphere.infra.executor.sql.process.ProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.infra.executor.sql.process.lock.ShowProcessListLock;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.swapper.YamlProcessListContextsSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.metadata.persist.node.ProcessNode;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.KillProcessEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.KillProcessUnitCompleteEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessListTriggerEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessUnitCompleteEvent;

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
    
    private final YamlProcessListContextsSwapper swapper = new YamlProcessListContextsSwapper();
    
    public ProcessListChangedSubscriber(final RegistryCenter registryCenter, final ContextManager contextManager) {
        this.registryCenter = registryCenter;
        this.contextManager = contextManager;
        contextManager.getInstanceContext().getEventBusContext().register(this);
    }
    
    /**
     * Report local processes.
     *
     * @param event show process list trigger event
     */
    @Subscribe
    public synchronized void reportLocalProcesses(final ShowProcessListTriggerEvent event) {
        if (!event.getInstanceId().equals(contextManager.getInstanceContext().getInstance().getMetaData().getId())) {
            return;
        }
        Collection<ProcessContext> processContexts = ProcessRegistry.getInstance().getAllProcessContexts();
        if (!processContexts.isEmpty()) {
            registryCenter.getRepository().persist(
                    ProcessNode.getProcessListInstancePath(event.getProcessId(), event.getInstanceId()), YamlEngine.marshal(swapper.swapToYamlConfiguration(processContexts)));
        }
        registryCenter.getRepository().delete(ComputeNode.getProcessTriggerInstanceIdNodePath(event.getInstanceId(), event.getProcessId()));
    }
    
    /**
     * Kill process.
     *
     * @param event kill process id event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void killProcess(final KillProcessEvent event) throws SQLException {
        if (!event.getInstanceId().equals(contextManager.getInstanceContext().getInstance().getMetaData().getId())) {
            return;
        }
        ProcessContext processContext = ProcessRegistry.getInstance().getProcessContext(event.getProcessId());
        if (null != processContext) {
            for (Statement each : processContext.getProcessStatements()) {
                each.cancel();
            }
        }
        registryCenter.getRepository().delete(ComputeNode.getProcessKillInstanceIdNodePath(event.getInstanceId(), event.getProcessId()));
    }
    
    /**
     * Complete show process unit.
     *
     * @param event show process unit complete event
     */
    @Subscribe
    public synchronized void completeShowProcessUnit(final ShowProcessUnitCompleteEvent event) {
        ShowProcessListLock lock = ProcessRegistry.getInstance().getLocks().get(event.getProcessId());
        if (null != lock) {
            lock.doNotify();
        }
    }
    
    /**
     * Complete to kill process unit.
     *
     * @param event kill process unit complete event
     */
    @Subscribe
    public synchronized void completeKillProcessUnit(final KillProcessUnitCompleteEvent event) {
        ShowProcessListLock lock = ProcessRegistry.getInstance().getLocks().get(event.getProcessId());
        if (null != lock) {
            lock.doNotify();
        }
    }
}
