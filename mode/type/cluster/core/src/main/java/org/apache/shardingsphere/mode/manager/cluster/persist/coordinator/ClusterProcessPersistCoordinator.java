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

package org.apache.shardingsphere.mode.manager.cluster.persist.coordinator;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.swapper.YamlProcessListSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.node.path.metadata.ComputeNodePath;
import org.apache.shardingsphere.mode.node.path.metadata.ProcessNodePath;
import org.apache.shardingsphere.mode.persist.coordinator.ProcessPersistCoordinator;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;

/**
 * Cluster process persist coordinator.
 */
@RequiredArgsConstructor
public final class ClusterProcessPersistCoordinator implements ProcessPersistCoordinator {
    
    private final PersistRepository repository;
    
    private final YamlProcessListSwapper swapper = new YamlProcessListSwapper();
    
    @Override
    public void reportLocalProcesses(final String instanceId, final String taskId) {
        Collection<Process> processes = ProcessRegistry.getInstance().listAll();
        if (!processes.isEmpty()) {
            repository.persist(ProcessNodePath.getInstanceProcessList(taskId, instanceId), YamlEngine.marshal(swapper.swapToYamlConfiguration(processes)));
        }
        repository.delete(ComputeNodePath.getShowProcessListTriggerPath(instanceId, taskId));
    }
    
    @Override
    public void cleanProcess(final String instanceId, final String processId) {
        repository.delete(ComputeNodePath.getKillProcessTriggerPath(instanceId, processId));
    }
}
