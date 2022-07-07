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
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.node.WorkerIdNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Optional;
import java.util.Properties;

/**
 * Worker id generator for cluster mode.
 */
@Slf4j
@RequiredArgsConstructor
public final class ClusterWorkerIdGenerator implements WorkerIdGenerator {
    
    private final ClusterPersistRepository repository;
    
    private final RegistryCenter registryCenter;
    
    private final InstanceMetaData instanceMetaData;
    
    @Override
    public long generate() {
        return registryCenter.getComputeNodeStatusService().loadInstanceWorkerId(instanceMetaData.getId()).orElseGet(this::reGenerate);
    }
    
    @Override
    public long generate(final Properties props) {
        long result = generate();
        checkConfigured(result, props);
        return result;
    }
    
    private long reGenerate() {
        long result = Long.parseLong(Optional.ofNullable(repository.getSequentialId(WorkerIdNode.getWorkerIdGeneratorPath(instanceMetaData.getId()), "")).orElse("0"));
        registryCenter.getComputeNodeStatusService().persistInstanceWorkerId(instanceMetaData.getId(), result);
        return result;
    }
    
    private void checkConfigured(final long generatedWorkerId, final Properties props) {
        Optional<Long> configuredWorkerId = parseWorkerId(props);
        if (configuredWorkerId.isPresent()) {
            log.warn("No need to configured {} in cluster mode, system assigned work-id was {}", WORKER_ID_KEY, generatedWorkerId);
        }
    }
}
