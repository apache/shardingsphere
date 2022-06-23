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
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
     * @param instanceDefinition instance definition
     */
    public void registerOnline(final InstanceDefinition instanceDefinition) {
        repository.persistEphemeral(ComputeNode.getOnlineInstanceNodePath(instanceDefinition.getInstanceId(), instanceDefinition.getInstanceType()),
                instanceDefinition.getAttributes());
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
     * Persist instance worker id.
     *
     * @param instanceId instance id
     * @param workerId worker id
     */
    public void persistInstanceWorkerId(final String instanceId, final Long workerId) {
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
        String yamlContent = repository.get(ComputeNode.getInstanceLabelsNodePath(instanceId));
        return Strings.isNullOrEmpty(yamlContent) ? new ArrayList<>() : YamlEngine.unmarshal(yamlContent, Collection.class);
    }
    
    /**
     * Load instance status.
     *
     * @param instanceId instance id
     * @return status
     */
    @SuppressWarnings("unchecked")
    public Collection<String> loadInstanceStatus(final String instanceId) {
        String yamlContent = repository.get(ComputeNode.getInstanceStatusNodePath(instanceId));
        return Strings.isNullOrEmpty(yamlContent) ? new ArrayList<>() : YamlEngine.unmarshal(yamlContent, Collection.class);
    }
    
    /**
     * Load instance worker id.
     *
     * @param instanceId instance id
     * @return worker id
     */
    public Optional<Long> loadInstanceWorkerId(final String instanceId) {
        try {
            String workerId = repository.get(ComputeNode.getInstanceWorkerIdNodePath(instanceId));
            return Strings.isNullOrEmpty(workerId) ? Optional.empty() : Optional.of(Long.valueOf(workerId));
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
        Collection<ComputeNodeInstance> result = new ArrayList<>();
        Arrays.stream(InstanceType.values()).forEach(instanceType -> {
            Collection<String> onlineComputeNodes = repository.getChildrenKeys(ComputeNode.getOnlineNodePath(instanceType));
            onlineComputeNodes.forEach(each -> {
                InstanceDefinition instanceDefinition = new InstanceDefinition(instanceType, each, repository.get(ComputeNode.getOnlineInstanceNodePath(each, instanceType)));
                result.add(loadComputeNodeInstance(instanceDefinition));
            });
        });
        return result;
    }
    
    /**
     * Load compute node instance by instance definition.
     *
     * @param instanceDefinition instance definition
     * @return compute node instance
     */
    public ComputeNodeInstance loadComputeNodeInstance(final InstanceDefinition instanceDefinition) {
        ComputeNodeInstance result = new ComputeNodeInstance(instanceDefinition);
        result.setLabels(loadInstanceLabels(instanceDefinition.getInstanceId()));
        result.switchState(loadInstanceStatus(instanceDefinition.getInstanceId()));
        loadInstanceWorkerId(instanceDefinition.getInstanceId()).ifPresent(result::setWorkerId);
        return result;
    }
}
