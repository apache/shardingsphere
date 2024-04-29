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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.infra.executor.sql.process.lock.ProcessOperationLockRegistry;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.swapper.YamlProcessListSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.metadata.persist.node.ProcessNode;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.KillLocalProcessCompletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.KillLocalProcessEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ReportLocalProcessesCompletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ReportLocalProcessesEvent;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * TODO replace the old ProcessListChangedSubscriber after meta data refactor completed
 * New process list changed subscriber.
 */
public final class ProcessListChangedSubscriber {
    
    private final RegistryCenter registryCenter;
    
    private final ContextManager contextManager;
    
    private final YamlProcessListSwapper swapper = new YamlProcessListSwapper();
    
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
    public void reportLocalProcesses(final ReportLocalProcessesEvent event) {
        if (!event.getInstanceId().equals(contextManager.getInstanceContext().getInstance().getMetaData().getId())) {
            return;
        }
        Collection<Process> processes = ProcessRegistry.getInstance().listAll();
        if (!processes.isEmpty()) {
            registryCenter.getRepository().persist(
                    ProcessNode.getProcessListInstancePath(event.getTaskId(), event.getInstanceId()), YamlEngine.marshal(swapper.swapToYamlConfiguration(processes)));
        }
        registryCenter.getRepository().delete(ComputeNode.getProcessTriggerInstanceNodePath(event.getInstanceId(), event.getTaskId()));
    }
    
    /**
     * Complete to report local processes.
     *
     * @param event report local processes completed event
     */
    @Subscribe
    public synchronized void completeToReportLocalProcesses(final ReportLocalProcessesCompletedEvent event) {
        ProcessOperationLockRegistry.getInstance().notify(event.getTaskId());
    }
    
    /**
     * Kill local process.
     *
     * @param event kill local process event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void killLocalProcess(final KillLocalProcessEvent event) throws SQLException {
        if (!event.getInstanceId().equals(contextManager.getInstanceContext().getInstance().getMetaData().getId())) {
            return;
        }
        Process process = ProcessRegistry.getInstance().get(event.getProcessId());
        if (null != process) {
            process.setInterrupted(true);
            for (Statement each : process.getProcessStatements().values()) {
                each.cancel();
            }
        }
        registryCenter.getRepository().delete(ComputeNode.getProcessKillInstanceIdNodePath(event.getInstanceId(), event.getProcessId()));
    }
    
    /**
     * Complete to kill local process.
     *
     * @param event kill local process completed event
     */
    @Subscribe
    public synchronized void completeToKillLocalProcess(final KillLocalProcessCompletedEvent event) {
        ProcessOperationLockRegistry.getInstance().notify(event.getProcessId());
    }
}
