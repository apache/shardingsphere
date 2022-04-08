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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.definition.InstanceId;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.lock.LockContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Instance context.
 */
@Getter
@RequiredArgsConstructor
public final class InstanceContext {
    
    private final ComputeNodeInstance instance;
    
    private final WorkerIdGenerator workerIdGenerator;
    
    private final ModeConfiguration modeConfiguration;
    
    private final LockContext lockContext;
    
    private final Collection<ComputeNodeInstance> computeNodeInstances = new LinkedList<>();
    
    /**
     * Update instance status.
     *
     * @param instanceId instance id
     * @param status collection of status
     */
    public void updateInstanceStatus(final String instanceId, final Collection<String> status) {
        if (instance.getInstanceDefinition().getInstanceId().getId().equals(instanceId)) {
            instance.switchState(status);
        }
        updateRelatedComputeNodeInstancesStatus(instanceId, status);
    }
    
    private void updateRelatedComputeNodeInstancesStatus(final String instanceId, final Collection<String> status) {
        for (ComputeNodeInstance each : computeNodeInstances) {
            if (each.getInstanceDefinition().getInstanceId().getId().equals(instanceId)) {
                each.switchState(status);
            }
        }
    }
    
    /**
     * Update instance worker id.
     * 
     * @param workerId worker id
     */
    public void updateWorkerId(final Long workerId) {
        if (!Objects.equals(workerId, instance.getWorkerId())) {
            instance.setWorkerId(workerId);
        }
    }
    
    /**
     * Update instance label.
     * 
     * @param instanceId instance id
     * @param labels collection of label
     */
    public void updateLabel(final String instanceId, final Collection<String> labels) {
        if (instance.getInstanceDefinition().getInstanceId().getId().equals(instanceId)) {
            instance.setLabels(labels);
        }
        computeNodeInstances.stream().filter(each -> each.getInstanceDefinition().getInstanceId().getId().equals(instanceId)).forEach(each -> each.setLabels(labels));
    }
    
    /**
     * Update instance xa recovery id.
     *
     * @param xaRecoveryId xa recovery id
     */
    public void updateXaRecoveryId(final String xaRecoveryId) {
        if (!Objects.equals(xaRecoveryId, instance.getXaRecoveryId())) {
            instance.setXaRecoveryId(xaRecoveryId);
        }
    }
    
    /**
     * Get worker id.
     *
     * @return worker id
     */
    public long getWorkerId() {
        if (null == instance.getWorkerId()) {
            // TODO process generate failed
            Optional.of(workerIdGenerator.generate()).ifPresent(instance::setWorkerId);
        }
        return instance.getWorkerId();
    }
    
    /**
     * Add compute node instance.
     * 
     * @param instance compute node instance
     */
    public void addComputeNodeInstance(final ComputeNodeInstance instance) {
        computeNodeInstances.removeIf(each -> each.getInstanceDefinition().getInstanceId().getId().equalsIgnoreCase(instance.getInstanceDefinition().getInstanceId().getId()));
        computeNodeInstances.add(instance);
    }
    
    /**
     * Delete compute node instance.
     *
     * @param instance compute node instance
     */
    public void deleteComputeNodeInstance(final ComputeNodeInstance instance) {
        computeNodeInstances.removeIf(each -> each.getInstanceDefinition().getInstanceId().getId().equalsIgnoreCase(instance.getInstanceDefinition().getInstanceId().getId()));
    }
    
    /**
     * Get compute node instances by instance type and labels.
     *
     * @param instanceType instance type
     * @param labels collection of contained label
     * @return compute node instances
     */
    public List<InstanceId> getComputeNodeInstanceIds(final InstanceType instanceType, final Collection<String> labels) {
        List<InstanceId> result = new ArrayList<>(computeNodeInstances.size());
        for (ComputeNodeInstance each : computeNodeInstances) {
            if (each.getInstanceDefinition().getInstanceType() == instanceType && labels.stream().anyMatch(((Collection<String>) each.getLabels())::contains)) {
                result.add(each.getInstanceDefinition().getInstanceId());
            }
        }
        return result;
    }
    
    /**
     * Init lock context.
     */
    public void initLockContext() {
        lockContext.initLockState(this);
    }
}
