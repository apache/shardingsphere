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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.service;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaDataFactory;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.state.instance.InstanceStateContext;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.instance.ComputeNodeData;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Compute node status service.
 */
@RequiredArgsConstructor
@Slf4j
public final class ComputeNodeStatusService {
    
    private final ClusterPersistRepository repository;
    
    /**
     * Register online.
     * 
     * @param instanceMetaData instance definition
     */
    public void registerOnline(final InstanceMetaData instanceMetaData) {
        repository.persistEphemeral(ComputeNode.getOnlineInstanceNodePath(instanceMetaData.getId(), instanceMetaData.getType()),
                YamlEngine.marshal(new ComputeNodeData(instanceMetaData.getAttributes(), ShardingSphereVersion.VERSION)));
    }
    
    /**
     * Persist instance labels.
     *
     * @param instanceId instance id
     * @param labels collection of label
     */
    public void persistInstanceLabels(final String instanceId, final Collection<String> labels) {
        if (null != labels) {
            repository.persistEphemeral(ComputeNode.getInstanceLabelsNodePath(instanceId), YamlEngine.marshal(labels));
        }
    }
    
    /**
     * Persist instance state.
     *
     * @param instanceId instance id
     * @param state state context
     */
    public void persistInstanceState(final String instanceId, final InstanceStateContext state) {
        repository.persistEphemeral(ComputeNode.getInstanceStatusNodePath(instanceId), state.getCurrentState().name());
    }
    
    /**
     * Persist instance worker id.
     *
     * @param instanceId instance id
     * @param workerId worker id
     */
    public void persistInstanceWorkerId(final String instanceId, final Integer workerId) {
        repository.persistEphemeral(ComputeNode.getInstanceWorkerIdNodePath(instanceId), String.valueOf(workerId));
    }
    
    /**
     * Load instance labels.
     *
     * @param instanceId instance id
     * @return labels
     */
    @SuppressWarnings("unchecked")
    public Collection<String> loadInstanceLabels(final String instanceId) {
        String yamlContent = repository.getDirectly(ComputeNode.getInstanceLabelsNodePath(instanceId));
        return Strings.isNullOrEmpty(yamlContent) ? new ArrayList<>() : YamlEngine.unmarshal(yamlContent, Collection.class);
    }
    
    /**
     * Load instance status.
     *
     * @param instanceId instance id
     * @return status
     */
    public String loadInstanceStatus(final String instanceId) {
        return repository.getDirectly(ComputeNode.getInstanceStatusNodePath(instanceId));
    }
    
    /**
     * Load instance worker id.
     *
     * @param instanceId instance id
     * @return worker id
     */
    public Optional<Integer> loadInstanceWorkerId(final String instanceId) {
        try {
            String workerId = repository.getDirectly(ComputeNode.getInstanceWorkerIdNodePath(instanceId));
            return Strings.isNullOrEmpty(workerId) ? Optional.empty() : Optional.of(Integer.valueOf(workerId));
        } catch (final NumberFormatException ex) {
            log.error("Invalid worker id for instance: {}", instanceId);
        }
        return Optional.empty();
    }
    
    /**
     * Load all compute node instances.
     *
     * @return compute node instances
     */
    public Collection<ComputeNodeInstance> loadAllComputeNodeInstances() {
        Collection<ComputeNodeInstance> result = new LinkedList<>();
        for (InstanceType each : InstanceType.values()) {
            result.addAll(loadComputeNodeInstances(each));
        }
        return result;
    }
    
    private Collection<ComputeNodeInstance> loadComputeNodeInstances(final InstanceType instanceType) {
        Collection<ComputeNodeInstance> result = new LinkedList<>();
        for (String each : repository.getChildrenKeys(ComputeNode.getOnlineNodePath(instanceType))) {
            String value = repository.getDirectly(ComputeNode.getOnlineInstanceNodePath(each, instanceType));
            if (Strings.isNullOrEmpty(value)) {
                continue;
            }
            ComputeNodeData computeNodeData = YamlEngine.unmarshal(value, ComputeNodeData.class);
            result.add(loadComputeNodeInstance(InstanceMetaDataFactory.create(each, instanceType, computeNodeData.getAttribute(), computeNodeData.getVersion())));
        }
        return result;
    }
    
    /**
     * Load compute node instance by instance definition.
     *
     * @param instanceMetaData instance definition
     * @return compute node instance
     */
    public ComputeNodeInstance loadComputeNodeInstance(final InstanceMetaData instanceMetaData) {
        ComputeNodeInstance result = new ComputeNodeInstance(instanceMetaData);
        result.setLabels(loadInstanceLabels(instanceMetaData.getId()));
        result.switchState(loadInstanceStatus(instanceMetaData.getId()));
        loadInstanceWorkerId(instanceMetaData.getId()).ifPresent(result::setWorkerId);
        return result;
    }
    
    /**
     * Get assigned worker ids.
     *
     * @return assigned worker ids
     */
    public Collection<Integer> getAssignedWorkerIds() {
        Collection<Integer> result = new LinkedHashSet<>();
        Collection<String> childrenKeys = repository.getChildrenKeys(ComputeNode.getInstanceWorkerIdRootNodePath());
        for (String each : childrenKeys) {
            String workerId = repository.getDirectly(ComputeNode.getInstanceWorkerIdNodePath(each));
            if (null != workerId) {
                result.add(Integer.parseInt(workerId));
            }
        }
        return result;
    }
}
