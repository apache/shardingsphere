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
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.node.WorkerIdNode;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

/**
 * Worker id generator for cluster mode.
 */
@RequiredArgsConstructor
public final class ClusterWorkerIdGenerator implements WorkerIdGenerator {
    
    private final ClusterPersistRepository repository;
    
    private final MetaDataPersistService metaDataPersistService;
    
    private final InstanceDefinition instanceDefinition;
    
    @Override
    public long generate() {
        Long result = metaDataPersistService.getComputeNodePersistService().loadInstanceWorkerId(instanceDefinition.getInstanceId().getId());
        if (null == result) {
            result = Long.valueOf(repository.getSequentialId(WorkerIdNode.getWorkerIdGeneratorPath(instanceDefinition.getInstanceId().getId()), ""));
            metaDataPersistService.getComputeNodePersistService().persistInstanceWorkerId(instanceDefinition.getInstanceId().getId(), result);        
        }
        return result;
    }
}
