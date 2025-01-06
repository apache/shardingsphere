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
import org.apache.shardingsphere.metadata.persist.node.ComputeNodePath;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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
        String instanceId = computeNodeInstance.getMetaData().getId();
        repository.persistEphemeral(ComputeNodePath.getOnlinePath(instanceId, computeNodeInstance.getMetaData().getType()), YamlEngine.marshal(
                new YamlComputeNodeDataSwapper().swapToYamlConfiguration(new ComputeNodeData(computeNodeInstance.getMetaData().getAttributes(),
                        computeNodeInstance.getMetaData().getVersion(), computeNodeInstance.getMetaData().getDatabaseName()))));
        repository.persistEphemeral(ComputeNodePath.getStatePath(instanceId), computeNodeInstance.getState().getCurrentState().name());
        persistInstanceLabels(instanceId, computeNodeInstance.getLabels());
    }
    
    /**
     * Persist instance labels.
     *
     * @param instanceId instance ID
     * @param labels instance labels
     */
    public void persistInstanceLabels(final String instanceId, final Collection<String> labels) {
        repository.persistEphemeral(ComputeNodePath.getLabelsPath(instanceId), YamlEngine.marshal(labels));
    }
    
    /**
     * Persist instance worker ID.
     *
     * @param instanceId instance ID
     * @param workerId worker ID
     */
    public void persistInstanceWorkerId(final String instanceId, final int workerId) {
        repository.persistEphemeral(ComputeNodePath.getWorkerIdPath(instanceId), String.valueOf(workerId));
    }
    
    /**
     * Load instance labels.
     *
     * @param instanceId instance ID
     * @return labels
     */
    @SuppressWarnings("unchecked")
    public Collection<String> loadInstanceLabels(final String instanceId) {
        String yamlContent = repository.query(ComputeNodePath.getLabelsPath(instanceId));
        return Strings.isNullOrEmpty(yamlContent) ? new LinkedList<>() : YamlEngine.unmarshal(yamlContent, Collection.class);
    }
    
    /**
     * Load compute node state.
     *
     * @param instanceId instance ID
     * @return state
     */
    public String loadComputeNodeState(final String instanceId) {
        return repository.query(ComputeNodePath.getStatePath(instanceId));
    }
    
    /**
     * Load instance worker ID.
     *
     * @param instanceId instance ID
     * @return worker ID
     */
    public Optional<Integer> loadInstanceWorkerId(final String instanceId) {
        try {
            String workerId = repository.query(ComputeNodePath.getWorkerIdPath(instanceId));
            return Strings.isNullOrEmpty(workerId) ? Optional.empty() : Optional.of(Integer.valueOf(workerId));
        } catch (final NumberFormatException ex) {
            log.error("Invalid worker id for instance: {}", instanceId);
        }
        return Optional.empty();
    }
    
    /**
     * Load all compute node instances.
     *
     * @return loaded compute node instances
     */
    public Collection<ComputeNodeInstance> loadAllComputeNodeInstances() {
        return Arrays.stream(InstanceType.values()).flatMap(each -> loadComputeNodeInstances(each).stream()).collect(Collectors.toList());
    }
    
    private Collection<ComputeNodeInstance> loadComputeNodeInstances(final InstanceType instanceType) {
        Collection<ComputeNodeInstance> result = new LinkedList<>();
        for (String each : repository.getChildrenKeys(ComputeNodePath.getOnlinePath(instanceType))) {
            String value = repository.query(ComputeNodePath.getOnlinePath(each, instanceType));
            if (Strings.isNullOrEmpty(value)) {
                continue;
            }
            ComputeNodeData computeNodeData = new YamlComputeNodeDataSwapper().swapToObject(YamlEngine.unmarshal(value, YamlComputeNodeData.class));
            result.add(loadComputeNodeInstance(InstanceMetaDataFactory.create(each, instanceType, computeNodeData.getAttribute(), computeNodeData.getVersion(), computeNodeData.getDatabaseName())));
        }
        return result;
    }
    
    /**
     * Load compute node instance by instance meta data.
     *
     * @param instanceMetaData instance meta data
     * @return compute node instance
     */
    public ComputeNodeInstance loadComputeNodeInstance(final InstanceMetaData instanceMetaData) {
        ComputeNodeInstance result = new ComputeNodeInstance(instanceMetaData);
        result.getLabels().addAll(loadInstanceLabels(instanceMetaData.getId()));
        InstanceState.get(loadComputeNodeState(instanceMetaData.getId())).ifPresent(result::switchState);
        loadInstanceWorkerId(instanceMetaData.getId()).ifPresent(result::setWorkerId);
        return result;
    }
    
    /**
     * Get assigned worker IDs.
     *
     * @return assigned worker IDs
     */
    public Collection<Integer> getAssignedWorkerIds() {
        Collection<String> childrenKeys = repository.getChildrenKeys(ComputeNodePath.getWorkerIdRootPath());
        Collection<Integer> result = new LinkedHashSet<>(childrenKeys.size(), 1F);
        for (String each : childrenKeys) {
            String workerId = repository.query(ComputeNodePath.getWorkerIdPath(each));
            if (null != workerId) {
                result.add(Integer.parseInt(workerId));
            }
        }
        return result;
    }
    
    /**
     * Update compute node state.
     *
     * @param instanceId instance ID
     * @param instanceState instance state
     */
    public void updateComputeNodeState(final String instanceId, final InstanceState instanceState) {
        repository.persistEphemeral(ComputeNodePath.getStatePath(instanceId), instanceState.name());
    }
    
    /**
     * Compute node offline.
     *
     * @param computeNodeInstance compute node instance
     */
    public void offline(final ComputeNodeInstance computeNodeInstance) {
        repository.delete(ComputeNodePath.getOnlinePath(computeNodeInstance.getMetaData().getId(), computeNodeInstance.getMetaData().getType()));
    }
}
