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
import org.apache.shardingsphere.infra.executor.sql.process.ShowProcessListManager;
import org.apache.shardingsphere.infra.executor.sql.process.lock.ShowProcessListSimpleLock;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.event.process.KillProcessIdRequestEvent;
import org.apache.shardingsphere.mode.event.process.ShowProcessListRequestEvent;
import org.apache.shardingsphere.mode.event.process.ShowProcessListResponseEvent;
import org.apache.shardingsphere.metadata.persist.node.ProcessNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;

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
@SuppressWarnings("UnstableApiUsage")
public final class ProcessRegistrySubscriber {
    
    private final PersistRepository repository;
    
    private final EventBusContext eventBusContext;
    
    public ProcessRegistrySubscriber(final PersistRepository repository, final EventBusContext eventBusContext) {
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
        String processId = new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong()).toString().replace("-", "");
        boolean triggerIsComplete = false;
        Collection<String> triggerPaths = getTriggerPaths(processId);
        try {
            triggerPaths.forEach(each -> repository.persist(each, ""));
            triggerIsComplete = waitAllNodeDataReady(processId, triggerPaths);
            sendShowProcessList(processId);
        } finally {
            repository.delete(ProcessNode.getProcessIdPath(processId));
            if (!triggerIsComplete) {
                triggerPaths.forEach(repository::delete);
            }
        }
    }
    
    private Collection<String> getTriggerPaths(final String processId) {
        return Stream.of(InstanceType.values())
                .flatMap(each -> repository.getChildrenKeys(ComputeNode.getOnlineNodePath(each)).stream().map(onlinePath -> ComputeNode.getProcessTriggerInstanceIdNodePath(onlinePath, processId)))
                .collect(Collectors.toList());
    }
    
    private void sendShowProcessList(final String processId) {
        List<String> childrenKeys = repository.getChildrenKeys(ProcessNode.getProcessIdPath(processId));
        Collection<String> batchProcessContexts = new LinkedList<>();
        for (String each : childrenKeys) {
            batchProcessContexts.add(repository.getDirectly(ProcessNode.getProcessListInstancePath(processId, each)));
        }
        eventBusContext.post(new ShowProcessListResponseEvent(batchProcessContexts));
    }
    
    /**
     * Kill process id.
     *
     * @param event get children request event.
     */
    @Subscribe
    public void killProcessId(final KillProcessIdRequestEvent event) {
        String processId = event.getProcessId();
        boolean killProcessIdIsComplete = false;
        Collection<String> processKillPaths = getProcessKillPaths(processId);
        try {
            processKillPaths.forEach(each -> repository.persist(each, ""));
            killProcessIdIsComplete = waitAllNodeDataReady(processId, processKillPaths);
        } finally {
            if (!killProcessIdIsComplete) {
                processKillPaths.forEach(repository::delete);
            }
        }
    }
    
    private Collection<String> getProcessKillPaths(final String processId) {
        return Stream.of(InstanceType.values())
                .flatMap(each -> repository.getChildrenKeys(ComputeNode.getOnlineNodePath(each)).stream().map(onlinePath -> ComputeNode.getProcessKillInstanceIdNodePath(onlinePath, processId)))
                .collect(Collectors.toList());
    }
    
    private boolean waitAllNodeDataReady(final String processId, final Collection<String> paths) {
        ShowProcessListSimpleLock simpleLock = new ShowProcessListSimpleLock();
        ShowProcessListManager.getInstance().getLocks().put(processId, simpleLock);
        simpleLock.lock();
        try {
            while (!isReady(paths)) {
                if (!simpleLock.awaitDefaultTime()) {
                    return false;
                }
            }
            return true;
        } finally {
            simpleLock.unlock();
            ShowProcessListManager.getInstance().getLocks().remove(processId);
        }
    }
    
    private boolean isReady(final Collection<String> paths) {
        return paths.stream().noneMatch(each -> null != repository.getDirectly(each));
    }
}
