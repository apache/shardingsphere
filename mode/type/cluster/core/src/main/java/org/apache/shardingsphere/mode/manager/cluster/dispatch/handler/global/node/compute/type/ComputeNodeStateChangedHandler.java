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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.node.compute.type;

import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.node.compute.ComputeNodeChangedHandler;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.global.node.compute.status.StatusNodePath;

import java.util.Arrays;
import java.util.Collection;

/**
 * Compute node state changed handler.
 */
public final class ComputeNodeStateChangedHandler implements ComputeNodeChangedHandler {
    
    @Override
    public NodePath getSubscribedNodePath() {
        return new StatusNodePath(null);
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED);
    }
    
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        NodePathSearcher.find(event.getKey(), StatusNodePath.createInstanceIdSearchCriteria()).ifPresent(optional -> handle(contextManager, event, optional));
    }
    
    private void handle(final ContextManager contextManager, final DataChangedEvent event, final String instanceId) {
        contextManager.getComputeNodeInstanceContext().updateStatus(instanceId, event.getValue());
    }
}
