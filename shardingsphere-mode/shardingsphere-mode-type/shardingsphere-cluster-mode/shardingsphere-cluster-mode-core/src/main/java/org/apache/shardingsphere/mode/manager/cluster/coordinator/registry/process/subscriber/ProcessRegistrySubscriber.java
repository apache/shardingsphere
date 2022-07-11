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

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessUnit;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.ShowProcessListManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessSummaryReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessUnitReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ShowProcessListRequestEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ShowProcessListResponseEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.lock.ShowProcessListSimpleLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.node.ProcessNode;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Process registry subscriber.
 */
public final class ProcessRegistrySubscriber {
    
    private final ClusterPersistRepository repository;
    
    private final EventBusContext eventBusContext;
    
    public ProcessRegistrySubscriber(final ClusterPersistRepository repository, final EventBusContext eventBusContext) {
        this.repository = repository;
        this.eventBusContext = eventBusContext;
        eventBusContext.register(this);
    }
    
    /**
     * Load show process list data.
     *
     * @param event get children request event.
     */
    @Subscribe
    public void loadShowProcessListData(final ShowProcessListRequestEvent event) {
        String showProcessListId = new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong()).toString();
        boolean triggerIsComplete = false;
        Collection<String> triggerPaths = getTriggerPaths(showProcessListId);
        try {
            triggerPaths.forEach(each -> repository.persist(each, ""));
            triggerIsComplete = waitUntilShowProcessIsReady(showProcessListId, triggerPaths);
            sendShowProcessList(showProcessListId);
        } finally {
            repository.delete(ProcessNode.getShowProcessListIdPath(showProcessListId));
            if (!triggerIsComplete) {
                triggerPaths.forEach(repository::delete);
            }
        }
    }
    
    private Collection<String> getTriggerPaths(final String showProcessListId) {
        return Stream.of(InstanceType.values())
                .flatMap(each -> repository.getChildrenKeys(ComputeNode.getOnlineNodePath(each)).stream()
                        .map(onlinePath -> ComputeNode.getProcessTriggerInstanceIdNodePath(onlinePath, showProcessListId)))
                .collect(Collectors.toList());
    }
    
    private boolean waitUntilShowProcessIsReady(final String showProcessListId, final Collection<String> triggerPaths) {
        ShowProcessListSimpleLock simpleLock = new ShowProcessListSimpleLock();
        ShowProcessListManager.getInstance().getLocks().put(showProcessListId, simpleLock);
        simpleLock.lock();
        try {
            while (!isReady(triggerPaths)) {
                if (!simpleLock.awaitDefaultTime()) {
                    return false;
                }
            }
            return true;
        } finally {
            simpleLock.unlock();
            ShowProcessListManager.getInstance().getLocks().remove(showProcessListId);
        }
    }
    
    private boolean isReady(final Collection<String> triggerPaths) {
        return triggerPaths.stream().noneMatch(each -> null != repository.get(each));
    }
    
    private void sendShowProcessList(final String showProcessListId) {
        List<String> childrenKeys = repository.getChildrenKeys(ProcessNode.getShowProcessListIdPath(showProcessListId));
        Collection<String> batchProcessContexts = new LinkedList<>();
        for (String each : childrenKeys) {
            batchProcessContexts.add(repository.get(ProcessNode.getShowProcessListInstancePath(showProcessListId, each)));
        }
        eventBusContext.post(new ShowProcessListResponseEvent(batchProcessContexts));
    }
    
    /**
     * Report execute process summary.
     *
     * @param event execute process summary report event.
     */
    @Subscribe
    @AllowConcurrentEvents
    public void reportExecuteProcessSummary(final ExecuteProcessSummaryReportEvent event) {
        ExecuteProcessContext executeProcessContext = event.getExecuteProcessContext();
        ShowProcessListManager.getInstance().putProcessContext(executeProcessContext.getExecutionID(), new YamlExecuteProcessContext(executeProcessContext));
    }
    
    /**
     * Report execute process unit.
     *
     * @param event execute process unit report event.
     */
    @Subscribe
    @AllowConcurrentEvents
    public void reportExecuteProcessUnit(final ExecuteProcessUnitReportEvent event) {
        String executionID = event.getExecutionID();
        YamlExecuteProcessContext yamlExecuteProcessContext = ShowProcessListManager.getInstance().getProcessContext(executionID);
        ExecuteProcessUnit executeProcessUnit = event.getExecuteProcessUnit();
        for (YamlExecuteProcessUnit each : yamlExecuteProcessContext.getUnitStatuses()) {
            if (each.getUnitID().equals(executeProcessUnit.getUnitID())) {
                each.setStatus(executeProcessUnit.getStatus());
            }
        }
    }
    
    /**
     * Report execute process.
     *
     * @param event execute process report event.
     */
    @Subscribe
    @AllowConcurrentEvents
    public void reportExecuteProcess(final ExecuteProcessReportEvent event) {
        YamlExecuteProcessContext yamlExecuteProcessContext = ShowProcessListManager.getInstance().getProcessContext(event.getExecutionID());
        for (YamlExecuteProcessUnit each : yamlExecuteProcessContext.getUnitStatuses()) {
            if (each.getStatus() != ExecuteProcessConstants.EXECUTE_STATUS_DONE) {
                return;
            }
        }
        ShowProcessListManager.getInstance().removeProcessContext(event.getExecutionID());
    }
}
