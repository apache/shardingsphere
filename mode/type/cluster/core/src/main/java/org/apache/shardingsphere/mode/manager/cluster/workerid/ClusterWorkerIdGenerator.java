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

package org.apache.shardingsphere.mode.manager.cluster.workerid;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdAssignedException;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.mode.manager.cluster.persist.service.ClusterComputeNodePersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Worker ID generator for cluster mode.
 */
@Slf4j
public final class ClusterWorkerIdGenerator implements WorkerIdGenerator {
    
    private final String instanceId;
    
    private final ClusterComputeNodePersistService computeNodePersistService;
    
    private final ReservationPersistService reservationPersistService;
    
    private final AtomicBoolean isWarned = new AtomicBoolean(false);
    
    public ClusterWorkerIdGenerator(final ClusterPersistRepository repository, final String instanceId) {
        this.instanceId = instanceId;
        computeNodePersistService = new ClusterComputeNodePersistService(repository);
        reservationPersistService = new ReservationPersistService(repository);
    }
    
    @Override
    public int generate(final Properties props) {
        int result = computeNodePersistService.loadWorkerId(instanceId).orElseGet(this::generateNewWorkerId);
        logWarning(result, props);
        return result;
    }
    
    private int generateNewWorkerId() {
        Optional<Integer> generatedWorkId;
        do {
            generatedWorkId = generateAvailableWorkerId();
        } while (!generatedWorkId.isPresent());
        int result = generatedWorkId.get();
        computeNodePersistService.persistWorkerId(instanceId, result);
        return result;
    }
    
    private Optional<Integer> generateAvailableWorkerId() {
        Collection<Integer> assignedWorkerIds = computeNodePersistService.getAssignedWorkerIds();
        ShardingSpherePreconditions.checkState(assignedWorkerIds.size() <= MAX_WORKER_ID + 1, WorkerIdAssignedException::new);
        PriorityQueue<Integer> availableWorkerIds = IntStream.range(0, MAX_WORKER_ID + 1)
                .boxed().filter(each -> !assignedWorkerIds.contains(each)).collect(Collectors.toCollection(PriorityQueue::new));
        Integer preselectedWorkerId = availableWorkerIds.poll();
        Preconditions.checkNotNull(preselectedWorkerId, "Preselected worker-id can not be null.");
        return reservationPersistService.reserveWorkerId(preselectedWorkerId, instanceId);
    }
    
    private void logWarning(final int generatedWorkerId, final Properties props) {
        if (!isWarned.get() && props.containsKey(WORKER_ID_KEY)) {
            isWarned.set(true);
            log.warn("It is unnecessary to configure {} in cluster mode, system assigned {} was {}", WORKER_ID_KEY, WORKER_ID_KEY, generatedWorkerId);
        }
    }
}
