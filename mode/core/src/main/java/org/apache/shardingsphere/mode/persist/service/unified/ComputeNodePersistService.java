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

package org.apache.shardingsphere.mode.persist.service.unified;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.instance.ComputeNodeData;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaDataFactory;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.instance.yaml.YamlComputeNodeData;
import org.apache.shardingsphere.infra.instance.yaml.YamlComputeNodeDataSwapper;
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.node.path.metadata.ComputeNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Compute node persist service.
 */
@RequiredArgsConstructor
@Slf4j
public final class ComputeNodePersistService {
    
    private final PersistRepository repository;
    
    /**
     * Register compute node online.
     *
     * @param computeNodeInstance compute node instance
     */
    public void registerOnline(final ComputeNodeInstance computeNodeInstance) {
        persistOnline(computeNodeInstance);
        updateState(computeNodeInstance.getMetaData().getId(), computeNodeInstance.getState().getCurrentState());
        persistLabels(computeNodeInstance.getMetaData().getId(), computeNodeInstance.getLabels());
    }
    
    private void persistOnline(final ComputeNodeInstance computeNodeInstance) {
        ComputeNodeData computeNodeData = new ComputeNodeData(
                computeNodeInstance.getMetaData().getDatabaseName(), computeNodeInstance.getMetaData().getAttributes(), computeNodeInstance.getMetaData().getVersion());
        repository.persistEphemeral(ComputeNodePath.getOnlinePath(computeNodeInstance.getMetaData().getId(), computeNodeInstance.getMetaData().getType()),
                YamlEngine.marshal(new YamlComputeNodeDataSwapper().swapToYamlConfiguration(computeNodeData)));
    }
    
    /**
     * Compute node offline.
     *
     * @param computeNodeInstance compute node instance
     */
    public void offline(final ComputeNodeInstance computeNodeInstance) {
        repository.delete(ComputeNodePath.getOnlinePath(computeNodeInstance.getMetaData().getId(), computeNodeInstance.getMetaData().getType()));
    }
    
    /**
     * Load all compute node instances.
     *
     * @return loaded instances
     */
    public Collection<ComputeNodeInstance> loadAllInstances() {
        return Arrays.stream(InstanceType.values()).flatMap(each -> loadInstances(each).stream()).collect(Collectors.toList());
    }
    
    private Collection<ComputeNodeInstance> loadInstances(final InstanceType instanceType) {
        Collection<ComputeNodeInstance> result = new LinkedList<>();
        for (String each : repository.getChildrenKeys(ComputeNodePath.getOnlinePath(instanceType))) {
            String value = repository.query(ComputeNodePath.getOnlinePath(each, instanceType));
            if (!Strings.isNullOrEmpty(value)) {
                ComputeNodeData computeNodeData = new YamlComputeNodeDataSwapper().swapToObject(YamlEngine.unmarshal(value, YamlComputeNodeData.class));
                result.add(loadInstance(InstanceMetaDataFactory.create(each, instanceType, computeNodeData)));
            }
        }
        return result;
    }
    
    /**
     * Load compute node instance.
     *
     * @param instanceMetaData instance meta data
     * @return loaded instance
     */
    public ComputeNodeInstance loadInstance(final InstanceMetaData instanceMetaData) {
        ComputeNodeInstance result = new ComputeNodeInstance(instanceMetaData);
        InstanceState.get(loadState(instanceMetaData.getId())).ifPresent(result::switchState);
        result.getLabels().addAll(loadLabels(instanceMetaData.getId()));
        loadWorkerId(instanceMetaData.getId()).ifPresent(result::setWorkerId);
        return result;
    }
    
    private String loadState(final String instanceId) {
        return repository.query(ComputeNodePath.getStatePath(instanceId));
    }
    
    @SuppressWarnings("unchecked")
    private Collection<String> loadLabels(final String instanceId) {
        String yamlContent = repository.query(ComputeNodePath.getLabelsPath(instanceId));
        return Strings.isNullOrEmpty(yamlContent) ? Collections.emptyList() : YamlEngine.unmarshal(yamlContent, Collection.class);
    }
    
    /**
     * Update state.
     *
     * @param instanceId instance ID
     * @param instanceState instance state
     */
    public void updateState(final String instanceId, final InstanceState instanceState) {
        repository.persistEphemeral(ComputeNodePath.getStatePath(instanceId), instanceState.name());
    }
    
    /**
     * Persist labels.
     *
     * @param instanceId instance ID
     * @param labels instance labels
     */
    public void persistLabels(final String instanceId, final Collection<String> labels) {
        repository.persistEphemeral(ComputeNodePath.getLabelsPath(instanceId), YamlEngine.marshal(labels));
    }
    
    /**
     * Persist worker ID.
     *
     * @param instanceId instance ID
     * @param workerId worker ID
     */
    public void persistWorkerId(final String instanceId, final int workerId) {
        repository.persistEphemeral(ComputeNodePath.getWorkerIdPath(instanceId), String.valueOf(workerId));
    }
    
    /**
     * Load worker ID.
     *
     * @param instanceId instance ID
     * @return worker ID
     */
    public Optional<Integer> loadWorkerId(final String instanceId) {
        try {
            String workerId = repository.query(ComputeNodePath.getWorkerIdPath(instanceId));
            return Strings.isNullOrEmpty(workerId) ? Optional.empty() : Optional.of(Integer.valueOf(workerId));
        } catch (final NumberFormatException ex) {
            log.error("Invalid worker id for instance: {}", instanceId);
            return Optional.empty();
        }
    }
    
    /**
     * Get assigned worker IDs.
     *
     * @return assigned worker IDs
     */
    public Collection<Integer> getAssignedWorkerIds() {
        Collection<String> instanceIds = repository.getChildrenKeys(ComputeNodePath.getWorkerIdRootPath());
        return instanceIds.stream().map(each -> repository.query(ComputeNodePath.getWorkerIdPath(each))).filter(Objects::nonNull).map(Integer::parseInt).collect(Collectors.toSet());
    }
}
