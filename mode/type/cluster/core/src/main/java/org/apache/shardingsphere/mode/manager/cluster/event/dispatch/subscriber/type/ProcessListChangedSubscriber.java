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

package org.apache.shardingsphere.mode.manager.cluster.event.dispatch.subscriber.type;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.infra.executor.sql.process.lock.ProcessOperationLockRegistry;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.event.state.compute.KillLocalProcessCompletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.event.state.compute.KillLocalProcessEvent;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.event.state.compute.ReportLocalProcessesCompletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.event.state.compute.ReportLocalProcessesEvent;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.subscriber.DispatchEventSubscriber;
import org.apache.shardingsphere.mode.persist.coordinator.ProcessPersistCoordinator;

import java.sql.SQLException;

/**
 * Process list changed subscriber.
 */
public final class ProcessListChangedSubscriber implements DispatchEventSubscriber {
    
    private final String instanceId;
    
    private final ProcessPersistCoordinator processPersistService;
    
    public ProcessListChangedSubscriber(final ContextManager contextManager) {
        instanceId = contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getId();
        processPersistService = contextManager.getPersistCoordinatorFacade().getProcessPersistCoordinator();
    }
    
    /**
     * Report local processes.
     *
     * @param event report local processes event
     */
    @Subscribe
    public void reportLocalProcesses(final ReportLocalProcessesEvent event) {
        if (event.getInstanceId().equals(instanceId)) {
            processPersistService.reportLocalProcesses(instanceId, event.getTaskId());
        }
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
        if (!event.getInstanceId().equals(instanceId)) {
            return;
        }
        ProcessRegistry.getInstance().kill(event.getProcessId());
        processPersistService.cleanProcess(instanceId, event.getProcessId());
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
