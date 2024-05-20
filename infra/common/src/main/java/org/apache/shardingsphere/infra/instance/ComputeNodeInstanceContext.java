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

package org.apache.shardingsphere.infra.instance;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Compute node instance context.
 */
@RequiredArgsConstructor
@Getter
@ThreadSafe
public final class ComputeNodeInstanceContext {
    
    private final ComputeNodeInstance instance;
    
    @Getter(AccessLevel.NONE)
    private final WorkerIdGenerator workerIdGenerator;
    
    private final ModeConfiguration modeConfiguration;
    
    private final ModeContextManager modeContextManager;
    
    @SuppressWarnings("rawtypes")
    private final LockContext lockContext;
    
    private final EventBusContext eventBusContext;
    
    private final Collection<ComputeNodeInstance> allClusterInstances = new CopyOnWriteArrayList<>();
    
    /**
     * Update instance status.
     *
     * @param id instance ID
     * @param status status
     */
    public void updateStatus(final String id, final String status) {
        Optional<InstanceState> instanceState = InstanceState.get(status);
        if (!instanceState.isPresent()) {
            return;
        }
        if (instance.getMetaData().getId().equals(id)) {
            instance.switchState(instanceState.get());
        }
        updateRelatedComputeNodeInstancesStatus(id, instanceState.get());
    }
    
    private void updateRelatedComputeNodeInstancesStatus(final String instanceId, final InstanceState instanceState) {
        for (ComputeNodeInstance each : allClusterInstances) {
            if (each.getMetaData().getId().equals(instanceId)) {
                each.switchState(instanceState);
            }
        }
    }
    
    /**
     * Update instance worker id.
     *
     * @param instanceId instance id
     * @param workerId worker id
     */
    public void updateWorkerId(final String instanceId, final Integer workerId) {
        if (instance.getMetaData().getId().equals(instanceId)) {
            instance.setWorkerId(workerId);
        }
        allClusterInstances.stream().filter(each -> each.getMetaData().getId().equals(instanceId)).forEach(each -> each.setWorkerId(workerId));
    }
    
    /**
     * Update instance label.
     * 
     * @param instanceId instance id
     * @param labels collection of label
     */
    public void updateLabel(final String instanceId, final Collection<String> labels) {
        if (instance.getMetaData().getId().equals(instanceId)) {
            instance.getLabels().clear();
            instance.getLabels().addAll(labels);
        }
        for (ComputeNodeInstance each : allClusterInstances) {
            if (each.getMetaData().getId().equals(instanceId)) {
                each.getLabels().clear();
                each.getLabels().addAll(labels);
            }
        }
    }
    
    /**
     * Get worker id.
     *
     * @return worker id
     */
    public int getWorkerId() {
        return instance.getWorkerId();
    }
    
    /**
     * Generate worker id.
     *
     * @param props properties
     * @return worker id
     */
    public int generateWorkerId(final Properties props) {
        int result = workerIdGenerator.generate(props);
        instance.setWorkerId(result);
        return result;
    }
    
    /**
     * Add compute node instance.
     * 
     * @param instance compute node instance
     */
    public void addComputeNodeInstance(final ComputeNodeInstance instance) {
        allClusterInstances.removeIf(each -> each.getMetaData().getId().equalsIgnoreCase(instance.getMetaData().getId()));
        allClusterInstances.add(instance);
    }
    
    /**
     * Delete compute node instance.
     *
     * @param instance compute node instance
     */
    public void deleteComputeNodeInstance(final ComputeNodeInstance instance) {
        allClusterInstances.removeIf(each -> each.getMetaData().getId().equalsIgnoreCase(instance.getMetaData().getId()));
    }
    
    /**
     * Get compute node instances by instance type and labels.
     *
     * @param instanceType instance type
     * @param labels collection of contained label
     * @return compute node instances
     */
    public Map<String, InstanceMetaData> getAllClusterInstances(final InstanceType instanceType, final Collection<String> labels) {
        Map<String, InstanceMetaData> result = new LinkedHashMap<>(allClusterInstances.size(), 1F);
        for (ComputeNodeInstance each : allClusterInstances) {
            if (each.getMetaData().getType() == instanceType && labels.stream().anyMatch(((Collection<String>) each.getLabels())::contains)) {
                result.put(each.getMetaData().getId(), each.getMetaData());
            }
        }
        return result;
    }
    
    /**
     * Get compute node instance by instance id.
     * 
     * @param instanceId instance id
     * @return compute node instance
     */
    public Optional<ComputeNodeInstance> getComputeNodeInstanceById(final String instanceId) {
        return allClusterInstances.stream().filter(each -> instanceId.equals(each.getMetaData().getId())).findFirst();
    }
    
    /**
     * Is cluster instance or not.
     * 
     * @return true if is cluster, else false
     */
    public boolean isCluster() {
        return "Cluster".equals(modeConfiguration.getType());
    }
}
