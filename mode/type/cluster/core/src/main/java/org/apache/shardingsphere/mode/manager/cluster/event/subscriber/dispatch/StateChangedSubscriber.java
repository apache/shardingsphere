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

package org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.event.dispatch.state.cluster.ClusterStateEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.ComputeNodeInstanceStateChangedEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.LabelsEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.WorkerIdEvent;

/**
 * State changed subscriber.
 */
@SuppressWarnings("unused")
public final class StateChangedSubscriber implements EventSubscriber {
    
    private final ContextManager contextManager;
    
    public StateChangedSubscriber(final ContextManager contextManager) {
        this.contextManager = contextManager;
    }
    
    /**
     * Renew cluster state.
     *
     * @param event cluster state event
     */
    @Subscribe
    public synchronized void renew(final ClusterStateEvent event) {
        contextManager.getStateContext().switchClusterState(event.getClusterState());
    }
    
    /**
     * Renew compute node instance state.
     *
     * @param event compute node instance state changed event
     */
    @Subscribe
    public synchronized void renew(final ComputeNodeInstanceStateChangedEvent event) {
        contextManager.getComputeNodeInstanceContext().updateStatus(event.getInstanceId(), event.getStatus());
    }
    
    /**
     * Renew instance worker id.
     *
     * @param event worker id event
     */
    @Subscribe
    public synchronized void renew(final WorkerIdEvent event) {
        contextManager.getComputeNodeInstanceContext().updateWorkerId(event.getInstanceId(), event.getWorkerId());
    }
    
    /**
     * Renew instance labels.
     *
     * @param event label event
     */
    @Subscribe
    public synchronized void renew(final LabelsEvent event) {
        // TODO labels may be empty
        contextManager.getComputeNodeInstanceContext().updateLabel(event.getInstanceId(), event.getLabels());
    }
}
