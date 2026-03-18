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

package org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.process;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.swapper.YamlProcessListSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.global.execution.ProcessNodePath;
import org.apache.shardingsphere.mode.node.path.type.global.node.compute.process.InstanceProcessNodeValue;
import org.apache.shardingsphere.mode.node.path.type.global.node.compute.process.KillProcessTriggerNodePath;
import org.apache.shardingsphere.mode.node.path.type.global.node.compute.process.ShowProcessListTriggerNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;

/**
 * Cluster process persist coordinator.
 */
@RequiredArgsConstructor
public final class ClusterProcessPersistCoordinator {
    
    private final PersistRepository repository;
    
    private final YamlProcessListSwapper swapper = new YamlProcessListSwapper();
    
    /**
     * Report local processes.
     *
     * @param instanceId instance ID
     * @param taskId task ID
     */
    public void reportLocalProcesses(final String instanceId, final String taskId) {
        Collection<Process> processes = ProcessRegistry.getInstance().listAll();
        if (!processes.isEmpty()) {
            repository.persist(NodePathGenerator.toPath(new ProcessNodePath(taskId, instanceId)), YamlEngine.marshal(swapper.swapToYamlConfiguration(processes)));
        }
        repository.delete(NodePathGenerator.toPath(new ShowProcessListTriggerNodePath(new InstanceProcessNodeValue(instanceId, taskId))));
    }
    
    /**
     * Clean process.
     *
     * @param instanceId instance ID
     * @param processId process ID
     */
    public void cleanProcess(final String instanceId, final String processId) {
        repository.delete(NodePathGenerator.toPath(new KillProcessTriggerNodePath(new InstanceProcessNodeValue(instanceId, processId))));
    }
}
