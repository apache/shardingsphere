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
import java.util.List;
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
        repository.persistEphemeral(ComputeNode.getOnlineInstanceNodePath(instanceDefinition.getInstanceId().getId(), instanceDefinition.getInstanceType()), "");
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
     * Persist instance xa recovery id.
     *
     * @param instanceId instance id
     * @param xaRecoveryId xa recovery id
     */
    public void persistInstanceXaRecoveryId(final String instanceId, final String xaRecoveryId) {
        loadXaRecoveryId(instanceId).ifPresent(each -> repository.delete(ComputeNode.getInstanceXaRecoveryIdNodePath(each, instanceId)));
        repository.persistEphemeral(ComputeNode.getInstanceXaRecoveryIdNodePath(xaRecoveryId, instanceId), "");
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
     * Load instance xa recovery id.
     *
     * @param instanceId instance id
     * @return xa recovery id
     */
    public Optional<String> loadXaRecoveryId(final String instanceId) {
        List<String> xaRecoveryIds = repository.getChildrenKeys(ComputeNode.getXaRecoveryIdNodePath());
        for (String xaRecoveryId : xaRecoveryIds) {
            if (repository.getChildrenKeys(String.join("/", ComputeNode.getXaRecoveryIdNodePath(), xaRecoveryId)).contains(instanceId)) {
                return Optional.of(xaRecoveryId);
            }
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
            onlineComputeNodes.forEach(each -> result.add(loadComputeNodeInstance(new InstanceDefinition(instanceType, each))));
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
        result.setLabels(loadInstanceLabels(instanceDefinition.getInstanceId().getId()));
        result.switchState(loadInstanceStatus(instanceDefinition.getInstanceId().getId()));
        loadInstanceWorkerId(instanceDefinition.getInstanceId().getId()).ifPresent(result::setWorkerId);
        loadXaRecoveryId(instanceDefinition.getInstanceId().getId()).ifPresent(result::setXaRecoveryId);
        return result;
    }
}
