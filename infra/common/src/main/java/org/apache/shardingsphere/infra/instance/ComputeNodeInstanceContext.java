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

import com.google.errorprone.annotations.ThreadSafe;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;

import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Compute node instance context.
 */
@Getter
@ThreadSafe
public final class ComputeNodeInstanceContext {
    
    private final ComputeNodeInstance instance;
    
    private final ModeConfiguration modeConfiguration;
    
    private final EventBusContext eventBusContext;
    
    @Getter(AccessLevel.NONE)
    private final AtomicReference<WorkerIdGenerator> workerIdGenerator;
    
    private final ClusterInstanceRegistry clusterInstanceRegistry;
    
    public ComputeNodeInstanceContext(final ComputeNodeInstance instance, final ModeConfiguration modeConfiguration, final EventBusContext eventBusContext) {
        this.instance = instance;
        this.modeConfiguration = modeConfiguration;
        this.eventBusContext = eventBusContext;
        workerIdGenerator = new AtomicReference<>();
        clusterInstanceRegistry = new ClusterInstanceRegistry();
    }
    
    /**
     * Initialize compute node instance context.
     *
     * @param workerIdGenerator worker id generator
     */
    public void init(final WorkerIdGenerator workerIdGenerator) {
        this.workerIdGenerator.set(workerIdGenerator);
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
        clusterInstanceRegistry.find(instanceId).ifPresent(optional -> optional.switchState(instanceState.get()));
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
        clusterInstanceRegistry.find(instanceId).ifPresent(optional -> updateLabels(optional, labels));
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
        clusterInstanceRegistry.find(instanceId).ifPresent(optional -> optional.setWorkerId(workerId));
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
}
