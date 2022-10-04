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

package org.apache.shardingsphere.mode.manager.cluster.process.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.apache.shardingsphere.mode.process.ShowProcessListManager;
import org.apache.shardingsphere.mode.process.event.KillProcessListIdRequestEvent;
import org.apache.shardingsphere.mode.process.event.ShowProcessListRequestEvent;
import org.apache.shardingsphere.mode.process.event.ShowProcessListResponseEvent;
import org.apache.shardingsphere.mode.process.lock.ShowProcessListSimpleLock;
import org.apache.shardingsphere.mode.process.node.ProcessNode;

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
        String processListId = new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong()).toString().replace("-", "");
        boolean triggerIsComplete = false;
        Collection<String> triggerPaths = getTriggerPaths(processListId);
        try {
            triggerPaths.forEach(each -> repository.persist(each, ""));
            triggerIsComplete = waitAllNodeDataReady(processListId, triggerPaths);
            sendShowProcessList(processListId);
        } finally {
            repository.delete(ProcessNode.getProcessListIdPath(processListId));
            if (!triggerIsComplete) {
                triggerPaths.forEach(repository::delete);
            }
        }
    }
    
    /**
     * Kill process list id.
     *
     * @param event get children request event.
     */
    @Subscribe
    public void killProcessListId(final KillProcessListIdRequestEvent event) {
        String processListId = event.getProcessListId();
        boolean killProcessListIdIsComplete = false;
        Collection<String> processKillPaths = getProcessKillPaths(processListId);
        try {
            processKillPaths.forEach(each -> repository.persist(each, ""));
            killProcessListIdIsComplete = waitAllNodeDataReady(processListId, processKillPaths);
        } finally {
            if (!killProcessListIdIsComplete) {
                processKillPaths.forEach(repository::delete);
            }
        }
    }
    
    private Collection<String> getProcessKillPaths(final String processListId) {
        return Stream.of(InstanceType.values())
                .flatMap(each -> repository.getChildrenKeys(ComputeNode.getOnlineNodePath(each)).stream()
                        .map(onlinePath -> ComputeNode.getProcessKillInstanceIdNodePath(onlinePath, processListId)))
                .collect(Collectors.toList());
    }
    
    private Collection<String> getTriggerPaths(final String processListId) {
        return Stream.of(InstanceType.values())
                .flatMap(each -> repository.getChildrenKeys(ComputeNode.getOnlineNodePath(each)).stream()
                        .map(onlinePath -> ComputeNode.getProcessTriggerInstanceIdNodePath(onlinePath, processListId)))
                .collect(Collectors.toList());
    }
    
    private boolean waitAllNodeDataReady(final String processListId, final Collection<String> paths) {
        ShowProcessListSimpleLock simpleLock = new ShowProcessListSimpleLock();
        ShowProcessListManager.getInstance().getLocks().put(processListId, simpleLock);
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
            ShowProcessListManager.getInstance().getLocks().remove(processListId);
        }
    }
    
    private boolean isReady(final Collection<String> paths) {
        return paths.stream().noneMatch(each -> null != repository.get(each));
    }
    
    private void sendShowProcessList(final String processListId) {
        List<String> childrenKeys = repository.getChildrenKeys(ProcessNode.getProcessListIdPath(processListId));
        Collection<String> batchProcessContexts = new LinkedList<>();
        for (String each : childrenKeys) {
            batchProcessContexts.add(repository.get(ProcessNode.getProcessListInstancePath(processListId, each)));
        }
        eventBusContext.post(new ShowProcessListResponseEvent(batchProcessContexts));
    }
}
