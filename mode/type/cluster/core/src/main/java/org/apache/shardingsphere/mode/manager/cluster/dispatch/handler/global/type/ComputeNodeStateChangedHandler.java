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

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.node.compute.ComputeNodePathParser;
import org.apache.shardingsphere.mode.node.path.type.node.compute.label.LabelNodePath;
import org.apache.shardingsphere.mode.node.path.type.node.compute.status.StatusNodePath;
import org.apache.shardingsphere.mode.node.path.type.node.compute.workerid.ComputeNodeWorkerIDNodePath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Compute node state changed handler.
 */
public final class ComputeNodeStateChangedHandler implements GlobalDataChangedEventHandler {
    
    @Override
    public String getSubscribedKey() {
        return "/nodes/compute_nodes";
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        ComputeNodePathParser.findInstanceId(event.getKey()).ifPresent(optional -> handle(contextManager, event, optional));
    }
    
    @SuppressWarnings("unchecked")
    private void handle(final ContextManager contextManager, final DataChangedEvent event, final String instanceId) {
        ComputeNodeInstanceContext computeNodeInstanceContext = contextManager.getComputeNodeInstanceContext();
        if (event.getKey().equals(NodePathGenerator.toPath(new StatusNodePath(instanceId), false)) && Type.DELETED != event.getType()) {
            computeNodeInstanceContext.updateStatus(instanceId, event.getValue());
        } else if (event.getKey().equals(NodePathGenerator.toPath(new LabelNodePath(instanceId), false)) && Type.DELETED != event.getType()) {
            // TODO labels may be empty
            computeNodeInstanceContext.updateLabels(instanceId, Strings.isNullOrEmpty(event.getValue()) ? new ArrayList<>() : YamlEngine.unmarshal(event.getValue(), Collection.class));
        } else if (event.getKey().equals(NodePathGenerator.toPath(new ComputeNodeWorkerIDNodePath(instanceId), false))) {
            computeNodeInstanceContext.updateWorkerId(instanceId, Strings.isNullOrEmpty(event.getValue()) ? null : Integer.valueOf(event.getValue()));
        }
    }
}
