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
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

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
        repository.persistEphemeral(ComputeNode.getOnlineInstanceNodePath(instanceMetaData.getInstanceId(), instanceMetaData.getInstanceType()), instanceMetaData.getAttributes());
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
        Collection<ComputeNodeInstance> result = new LinkedList<>();
        for (InstanceType each : InstanceType.values()) {
            result.addAll(loadComputeNodeInstances(each));
        }
        return result;
    }
    
    private Collection<ComputeNodeInstance> loadComputeNodeInstances(final InstanceType instanceType) {
        Collection<String> onlineComputeNodes = repository.getChildrenKeys(ComputeNode.getOnlineNodePath(instanceType));
        return onlineComputeNodes.stream().map(each -> loadComputeNodeInstance(createInstanceMetaData(each, instanceType))).collect(Collectors.toList());
    }
    
    private InstanceMetaData createInstanceMetaData(final String instanceId, final InstanceType type) {
        return InstanceType.JDBC == type
                ? new JDBCInstanceMetaData(instanceId)
                : new ProxyInstanceMetaData(instanceId, repository.get(ComputeNode.getOnlineInstanceNodePath(instanceId, type)));
    }
    
    /**
     * Load compute node instance by instance definition.
     *
     * @param instanceMetaData instance definition
     * @return compute node instance
     */
    public ComputeNodeInstance loadComputeNodeInstance(final InstanceMetaData instanceMetaData) {
        ComputeNodeInstance result = new ComputeNodeInstance(instanceMetaData);
        result.setLabels(loadInstanceLabels(instanceMetaData.getInstanceId()));
        result.switchState(loadInstanceStatus(instanceMetaData.getInstanceId()));
        loadInstanceWorkerId(instanceMetaData.getInstanceId()).ifPresent(result::setWorkerId);
        return result;
    }
}
