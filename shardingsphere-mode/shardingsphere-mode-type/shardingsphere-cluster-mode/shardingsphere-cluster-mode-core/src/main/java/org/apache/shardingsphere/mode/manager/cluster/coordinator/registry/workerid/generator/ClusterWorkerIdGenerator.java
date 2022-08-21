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
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.exception.WorkIdAssignedException;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.node.WorkerIdNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Set;

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
    public long generate(final Properties props) {
        long result = registryCenter.getComputeNodeStatusService().loadInstanceWorkerId(instanceMetaData.getId()).orElseGet(this::reGenerate);
        checkIneffectiveConfiguration(result, props);
        return result;
    }
    
    private Long reGenerate() {
        Optional<Long> result;
        do {
            result = generateAvailableWorkerId();
        } while (!result.isPresent());
        Long generatedWorkId = result.get();
        registryCenter.getComputeNodeStatusService().persistInstanceWorkerId(instanceMetaData.getId(), generatedWorkId);
        return generatedWorkId;
    }
    
    private Optional<Long> generateAvailableWorkerId() {
        Set<Long> assignedWorkerIds = registryCenter.getComputeNodeStatusService().getAssignedWorkerIds();
        if (assignedWorkerIds.size() > 1024) {
            throw new WorkIdAssignedException();
        }
        Collection<Long> maxAvailableIds = new ArrayList<>(1024);
        for (int i = 0; i < 1024; i++) {
            maxAvailableIds.add((long) i);
        }
        PriorityQueue<Long> priorityQueue = new PriorityQueue<>(maxAvailableIds);
        for (Long each : assignedWorkerIds) {
            priorityQueue.remove(each);
        }
        Long preselectedWorkerId = priorityQueue.poll();
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
