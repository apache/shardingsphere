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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.generator;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.exception.WorkIdAssignedException;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.node.WorkerIdNode;
import org.apache.shardingsphere.mode.repository.cluster.exception.ClusterPersistRepositoryException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Properties;

/**
 * Worker id generator for cluster mode.
 */
@RequiredArgsConstructor
@Slf4j
public final class ClusterWorkerIdGenerator implements WorkerIdGenerator {
    
    private final RegistryCenter registryCenter;
    
    private final InstanceMetaData instanceMetaData;
    
    private volatile boolean isWarned;
    
    @Override
    public int generate(final Properties props) {
        int result = registryCenter.getComputeNodeStatusService().loadInstanceWorkerId(instanceMetaData.getId()).orElseGet(this::reGenerate);
        checkIneffectiveConfiguration(result, props);
        return result;
    }
    
    private Integer reGenerate() {
        Optional<Integer> result;
        do {
            result = generateAvailableWorkerId();
        } while (!result.isPresent());
        Integer generatedWorkId = result.get();
        registryCenter.getComputeNodeStatusService().persistInstanceWorkerId(instanceMetaData.getId(), generatedWorkId);
        return generatedWorkId;
    }
    
    private Optional<Integer> generateAvailableWorkerId() {
        Collection<Integer> assignedWorkerIds = registryCenter.getComputeNodeStatusService().getAssignedWorkerIds();
        ShardingSpherePreconditions.checkState(assignedWorkerIds.size() <= 1024, WorkIdAssignedException::new);
        Collection<Integer> availableWorkerIds = new LinkedList<>();
        for (int i = 0; i < 1024; i++) {
            availableWorkerIds.add(i);
        }
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>(availableWorkerIds);
        for (Integer each : assignedWorkerIds) {
            priorityQueue.remove(each);
        }
        Integer preselectedWorkerId = priorityQueue.poll();
        Preconditions.checkState(null != preselectedWorkerId, "Preselected worker-id can not be null.");
        try {
            registryCenter.getRepository().persistExclusiveEphemeral(WorkerIdNode.getWorkerIdGeneratorPath(preselectedWorkerId.toString()), instanceMetaData.getId());
            return Optional.of(preselectedWorkerId);
        } catch (final ClusterPersistRepositoryException ignore) {
            return Optional.empty();
        }
    }
    
    private void checkIneffectiveConfiguration(final long generatedWorkerId, final Properties props) {
        if (!isWarned && null != props && props.containsKey(WORKER_ID_KEY)) {
            isWarned = true;
            log.warn("No need to configured {} in cluster mode, system assigned {} was {}", WORKER_ID_KEY, WORKER_ID_KEY, generatedWorkerId);
        }
    }
}
