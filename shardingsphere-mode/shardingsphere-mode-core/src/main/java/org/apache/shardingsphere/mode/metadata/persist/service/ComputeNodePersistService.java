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

package org.apache.shardingsphere.mode.metadata.persist.service;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Compute node persist service.
 */
@Slf4j
@RequiredArgsConstructor
public final class ComputeNodePersistService {
    
    private final PersistRepository repository;
    
    /**
     * Persist instance labels.
     * 
     * @param instanceId instance id
     * @param labels collection of label
     * @param isOverwrite whether overwrite registry center's configuration if existed
     */
    public void persistInstanceLabels(final String instanceId, final Collection<String> labels, final boolean isOverwrite) {
        if (null != labels && !labels.isEmpty() && (isOverwrite || !isExisted(instanceId))) {
            repository.persist(ComputeNode.getInstanceLabelsNodePath(instanceId), YamlEngine.marshal(labels));
        }
    }
    
    /**
     * Delete instance labels.
     *
     * @param instanceId instance id
     */
    public void deleteInstanceLabels(final String instanceId) {
        if (isExisted(instanceId)) {
            repository.delete(ComputeNode.getInstanceLabelsNodePath(instanceId));
        }
    }
    
    private boolean isExisted(final String instanceId) {
        return !Strings.isNullOrEmpty(repository.get(ComputeNode.getInstanceLabelsNodePath(instanceId)));
    }
    
    /**
     * Persist instance worker id.
     * 
     * @param instanceId instance id
     * @param workerId worker id
     */
    public void persistInstanceWorkerId(final String instanceId, final Long workerId) {
        repository.persist(ComputeNode.getInstanceWorkerIdNodePath(instanceId), String.valueOf(workerId));
    }
    
    /**
     * Persist instance xa recovery id.
     *
     * @param instanceId instance id
     * @param xaRecoveryId xa recovery id
     */
    public void persistInstanceXaRecoveryId(final String instanceId, final String xaRecoveryId) {
        loadXaRecoveryId(instanceId).ifPresent(each -> repository.delete(ComputeNode.getInstanceXaRecoveryIdNodePath(each, instanceId)));
        repository.persist(ComputeNode.getInstanceXaRecoveryIdNodePath(xaRecoveryId, instanceId), "");
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
