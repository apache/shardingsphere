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
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Compute node instance context.
 */
@Getter
@ThreadSafe
public final class ComputeNodeInstanceContext {
    
    private final ComputeNodeInstance instance;
    
    @Getter(AccessLevel.NONE)
    private final AtomicReference<WorkerIdGenerator> workerIdGenerator = new AtomicReference<>();
    
    private final ModeConfiguration modeConfiguration;
    
    @Getter(AccessLevel.NONE)
    private final AtomicReference<LockContext<?>> lockContext = new AtomicReference<>();
    
    private final EventBusContext eventBusContext;
    
    private final Collection<ComputeNodeInstance> allClusterInstances = new CopyOnWriteArrayList<>();
    
    public ComputeNodeInstanceContext(final ComputeNodeInstance instance, final WorkerIdGenerator workerIdGenerator,
                                      final ModeConfiguration modeConfig, final LockContext<?> lockContext, final EventBusContext eventBusContext) {
        this.instance = instance;
        this.workerIdGenerator.set(workerIdGenerator);
        this.modeConfiguration = modeConfig;
        this.lockContext.set(lockContext);
        this.eventBusContext = eventBusContext;
    }
    
    public ComputeNodeInstanceContext(final ComputeNodeInstance instance, final ModeConfiguration modeConfig, final EventBusContext eventBusContext) {
        this(instance, null, modeConfig, null, eventBusContext);
    }
    
    /**
     * Initialize compute node instance context.
     *
     * @param workerIdGenerator worker id generator
     * @param lockContext lock context
     */
    public void init(final WorkerIdGenerator workerIdGenerator, final LockContext<?> lockContext) {
        this.workerIdGenerator.set(workerIdGenerator);
        this.lockContext.set(lockContext);
    }
    
    /**
     * Update instance status.
     *
     * @param instanceId instance ID
     * @param status status
     */
    public void updateStatus(final String instanceId, final String status) {
        Optional<InstanceState> instanceState = InstanceState.get(status);
        if (!instanceState.isPresent()) {
            return;
        }
        if (instance.getMetaData().getId().equals(instanceId)) {
            instance.switchState(instanceState.get());
        }
        allClusterInstances.stream().filter(each -> each.getMetaData().getId().equals(instanceId)).forEach(each -> each.switchState(instanceState.get()));
    }
    
    /**
     * Update instance labels.
     *
     * @param instanceId instance ID
     * @param labels labels
     */
    public void updateLabels(final String instanceId, final Collection<String> labels) {
        if (instance.getMetaData().getId().equals(instanceId)) {
            updateLabels(instance, labels);
        }
        allClusterInstances.stream().filter(each -> each.getMetaData().getId().equals(instanceId)).forEach(each -> updateLabels(each, labels));
    }
    
    private void updateLabels(final ComputeNodeInstance computeNodeInstance, final Collection<String> labels) {
        computeNodeInstance.getLabels().clear();
        computeNodeInstance.getLabels().addAll(labels);
    }
    
    /**
     * Update instance worker ID.
     *
     * @param instanceId instance ID
     * @param workerId worker ID
     */
    public void updateWorkerId(final String instanceId, final Integer workerId) {
        if (instance.getMetaData().getId().equals(instanceId)) {
            instance.setWorkerId(workerId);
        }
        allClusterInstances.stream().filter(each -> each.getMetaData().getId().equals(instanceId)).forEach(each -> each.setWorkerId(workerId));
    }
    
    /**
     * Get worker ID.
     *
     * @return worker ID
     */
    public int getWorkerId() {
        return instance.getWorkerId();
    }
    
    /**
     * Generate worker ID.
     *
     * @param props properties
     * @return worker ID
     */
    public int generateWorkerId(final Properties props) {
        ShardingSpherePreconditions.checkNotNull(workerIdGenerator.get(), () -> new IllegalArgumentException("Worker id generator is not initialized."));
        int result = workerIdGenerator.get().generate(props);
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
     * Get compute node instance.
     *
     * @param instanceId instance ID
     * @return compute node instance
     */
    public Optional<ComputeNodeInstance> getComputeNodeInstanceById(final String instanceId) {
        return allClusterInstances.stream().filter(each -> instanceId.equals(each.getMetaData().getId())).findFirst();
    }
    
    /**
     *  Get lock context.
     *
     * @return lock context
     * @throws IllegalStateException if lock context is not initialized
     */
    public LockContext<?> getLockContext() throws IllegalStateException {
        return Optional.ofNullable(lockContext.get()).orElseThrow(() -> new IllegalStateException("Lock context is not initialized."));
    }
}
