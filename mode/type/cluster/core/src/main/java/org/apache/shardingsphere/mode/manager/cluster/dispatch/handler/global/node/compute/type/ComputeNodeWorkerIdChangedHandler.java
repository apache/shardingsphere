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

import com.google.common.base.Strings;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.node.compute.ComputeNodeChangedHandler;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.global.node.compute.workerid.ComputeNodeWorkerIDNodePath;

import java.util.Arrays;
import java.util.Collection;

/**
 * Compute node work ID changed handler.
 */
public final class ComputeNodeWorkerIdChangedHandler implements ComputeNodeChangedHandler {
    
    @Override
    public NodePath getSubscribedNodePath() {
        return new ComputeNodeWorkerIDNodePath(null);
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        if (!Strings.isNullOrEmpty(event.getValue())) {
            NodePathSearcher.find(event.getKey(), ComputeNodeWorkerIDNodePath.createInstanceIdSearchCriteria())
                    .ifPresent(optional -> contextManager.getComputeNodeInstanceContext().updateWorkerId(optional, Integer.valueOf(event.getValue())));
        }
    }
}
