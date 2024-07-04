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

package org.apache.shardingsphere.mode.manager.cluster.event.builder;

import com.google.common.base.Strings;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.state.cluster.ClusterStateEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Cluster state dispatch event builder.
 */
public final class ClusterStateDispatchEventBuilder implements DispatchEventBuilder<DispatchEvent> {
    
    @Override
    public Collection<String> getSubscribedKeys() {
        return Collections.singleton(ComputeNode.getClusterStateNodePath());
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public Optional<DispatchEvent> build(final DataChangedEvent event) {
        String clusterStatePath = ComputeNode.getClusterStateNodePath();
        return Strings.isNullOrEmpty(clusterStatePath) || Type.DELETED == event.getType() || !event.getKey().equals(ComputeNode.getClusterStateNodePath())
                ? Optional.empty()
                : Optional.of(new ClusterStateEvent(getClusterState(event)));
    }
    
    private ClusterState getClusterState(final DataChangedEvent event) {
        try {
            return ClusterState.valueOf(event.getValue());
        } catch (final IllegalArgumentException ignore) {
            return ClusterState.OK;
        }
    }
}
