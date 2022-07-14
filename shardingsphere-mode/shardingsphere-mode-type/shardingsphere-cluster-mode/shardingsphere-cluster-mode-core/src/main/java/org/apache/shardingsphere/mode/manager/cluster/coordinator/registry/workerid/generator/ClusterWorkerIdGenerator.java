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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.node.WorkerIdNode;

import java.util.Properties;

/**
 * Worker id generator for cluster mode.
 */
@RequiredArgsConstructor
@Slf4j
public final class ClusterWorkerIdGenerator implements WorkerIdGenerator {
    
    private static final int MAX_RETRY = 3;
    
    private final RegistryCenter registryCenter;
    
    private final InstanceMetaData instanceMetaData;
    
    private volatile boolean isWarned;
    
    @Override
    public long generate(final Properties props) {
        long result = registryCenter.getComputeNodeStatusService().loadInstanceWorkerId(instanceMetaData.getId()).orElseGet(this::generate);
        checkIneffectiveConfiguration(result, props);
        return result;
    }
    
    private long generate() {
        long result;
        int retryTimes = 0;
        do {
            retryTimes++;
            result = generateSequentialId();
            if (result > MAX_WORKER_ID) {
                result = result % MAX_WORKER_ID + 1;
            }
            // TODO check may retry should in the front of id generate
            if (retryTimes > MAX_RETRY) {
                throw new ShardingSphereException("System assigned %s failed, assigned %s was %s", WORKER_ID_KEY, WORKER_ID_KEY, result);
            }
        } while (isAssignedWorkerId(result));
        registryCenter.getComputeNodeStatusService().persistInstanceWorkerId(instanceMetaData.getId(), result);
        return result;
    }
    
    private long generateSequentialId() {
        String sequentialId = registryCenter.getRepository().getSequentialId(WorkerIdNode.getWorkerIdGeneratorPath(instanceMetaData.getId()), "");
        // TODO maybe throw exception is better if `null == sequentialId`
        return null == sequentialId ? DEFAULT_WORKER_ID : Long.parseLong(sequentialId);
    }
    
    private boolean isAssignedWorkerId(final long workerId) {
        return registryCenter.getComputeNodeStatusService().getUsedWorkerIds().contains(workerId);
    }
    
    private void checkIneffectiveConfiguration(final long generatedWorkerId, final Properties props) {
        if (!isWarned && null != props && props.containsKey(WORKER_ID_KEY)) {
            isWarned = true;
            log.warn("No need to configured {} in cluster mode, system assigned {} was {}", WORKER_ID_KEY, WORKER_ID_KEY, generatedWorkerId);
        }
    }
}
