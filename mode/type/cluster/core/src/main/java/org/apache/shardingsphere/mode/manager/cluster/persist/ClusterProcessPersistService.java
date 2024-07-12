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

package org.apache.shardingsphere.mode.manager.cluster.persist;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.executor.sql.process.lock.ProcessOperationLockRegistry;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.YamlProcessList;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.swapper.YamlProcessListSwapper;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.metadata.persist.node.ProcessNode;
import org.apache.shardingsphere.mode.persist.service.ProcessPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Cluster process persist service.
 */
@RequiredArgsConstructor
public final class ClusterProcessPersistService implements ProcessPersistService {
    
    private final PersistRepository repository;
    
    @Override
    public Collection<Process> getProcessList() {
        String taskId = new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong()).toString().replace("-", "");
        Collection<String> triggerPaths = getShowProcessListTriggerPaths(taskId);
        boolean isCompleted = false;
        try {
            triggerPaths.forEach(each -> repository.persist(each, ""));
            isCompleted = ProcessOperationLockRegistry.getInstance().waitUntilReleaseReady(taskId, () -> isReady(triggerPaths));
            return getShowProcessListData(taskId);
        } finally {
            repository.delete(ProcessNode.getProcessIdPath(taskId));
            if (!isCompleted) {
                triggerPaths.forEach(repository::delete);
            }
        }
    }
    
    private Collection<Process> getShowProcessListData(final String taskId) {
        YamlProcessList yamlProcessList = new YamlProcessList();
        for (String each : repository.getChildrenKeys(ProcessNode.getProcessIdPath(taskId)).stream()
                .map(each -> repository.query(ProcessNode.getProcessListInstancePath(taskId, each))).collect(Collectors.toList())) {
            yamlProcessList.getProcesses().addAll(YamlEngine.unmarshal(each, YamlProcessList.class).getProcesses());
        }
        return new YamlProcessListSwapper().swapToObject(yamlProcessList);
    }
    
    private Collection<String> getShowProcessListTriggerPaths(final String taskId) {
        return Stream.of(InstanceType.values())
                .flatMap(each -> repository.getChildrenKeys(ComputeNode.getOnlineNodePath(each)).stream().map(onlinePath -> ComputeNode.getProcessTriggerInstanceNodePath(onlinePath, taskId)))
                .collect(Collectors.toList());
    }
    
    private boolean isReady(final Collection<String> paths) {
        return paths.stream().noneMatch(each -> null != repository.query(each));
    }
    
    @Override
    public void killProcess(final String processId) {
        Collection<String> triggerPaths = getKillProcessTriggerPaths(processId);
        boolean isCompleted = false;
        try {
            triggerPaths.forEach(each -> repository.persist(each, ""));
            isCompleted = ProcessOperationLockRegistry.getInstance().waitUntilReleaseReady(processId, () -> isReady(triggerPaths));
        } finally {
            if (!isCompleted) {
                triggerPaths.forEach(repository::delete);
            }
        }
    }
    
    private Collection<String> getKillProcessTriggerPaths(final String processId) {
        return Stream.of(InstanceType.values())
                .flatMap(each -> repository.getChildrenKeys(ComputeNode.getOnlineNodePath(each)).stream().map(onlinePath -> ComputeNode.getProcessKillInstanceIdNodePath(onlinePath, processId)))
                .collect(Collectors.toList());
    }
}
