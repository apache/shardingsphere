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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.type;

import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.state.ClusterNodePath;
import org.apache.shardingsphere.mode.state.cluster.ClusterState;

import java.util.Arrays;
import java.util.Collection;

/**
 * Cluster state changed handler.
 */
public final class ClusterStateChangedHandler implements GlobalDataChangedEventHandler {
    
    @Override
    public String getSubscribedKey() {
        return NodePathGenerator.toPath(new ClusterNodePath(), false);
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED);
    }
    
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        contextManager.getStateContext().switchState(getClusterState(event));
    }
    
    private ClusterState getClusterState(final DataChangedEvent event) {
        try {
            return ClusterState.valueOf(event.getValue());
        } catch (final IllegalArgumentException ignore) {
            return ClusterState.OK;
        }
    }
}
