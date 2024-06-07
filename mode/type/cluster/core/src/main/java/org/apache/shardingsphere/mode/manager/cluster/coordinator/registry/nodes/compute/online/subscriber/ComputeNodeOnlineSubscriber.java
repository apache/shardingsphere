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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.nodes.compute.online.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.nodes.compute.online.event.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.nodes.compute.online.event.InstanceOnlineEvent;

/**
 * Compute node online subscriber.
 */
@RequiredArgsConstructor
public final class ComputeNodeOnlineSubscriber implements EventSubscriber {
    
    private final ContextManager contextManager;
    
    /**
     * Renew instance list.
     *
     * @param event compute node online event
     */
    @Subscribe
    public synchronized void renew(final InstanceOnlineEvent event) {
        contextManager.getComputeNodeInstanceContext().addComputeNodeInstance(contextManager.getPersistServiceFacade()
                .getComputeNodePersistService().loadComputeNodeInstance(event.getInstanceMetaData()));
    }
    
    /**
     * Renew instance list.
     *
     * @param event compute node offline event
     */
    @Subscribe
    public synchronized void renew(final InstanceOfflineEvent event) {
        contextManager.getComputeNodeInstanceContext().deleteComputeNodeInstance(new ComputeNodeInstance(event.getInstanceMetaData()));
    }
}
