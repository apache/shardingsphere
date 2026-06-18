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

import org.apache.shardingsphere.infra.instance.ClusterInstanceRegistry;
import org.apache.shardingsphere.infra.instance.ComputeNodeData;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaDataFactory;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.instance.yaml.YamlComputeNodeData;
import org.apache.shardingsphere.infra.instance.yaml.YamlComputeNodeDataSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.node.compute.ComputeNodeChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.persist.facade.ClusterPersistServiceFacade;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.global.node.compute.status.OnlineNodePath;

import java.util.Arrays;
import java.util.Collection;

/**
 *  Compute node online handler.
 */
public final class ComputeNodeOnlineHandler implements ComputeNodeChangedHandler {
    
    @Override
    public NodePath getSubscribedNodePath() {
        return new OnlineNodePath(null, null);
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.DELETED);
    }
    
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        if (!NodePathSearcher.isMatchedPath(event.getKey(), OnlineNodePath.createInstanceIdSearchCriteria())) {
            return;
        }
        ClusterInstanceRegistry clusterInstanceRegistry = contextManager.getComputeNodeInstanceContext().getClusterInstanceRegistry();
        InstanceType instanceType = InstanceType.valueOf(NodePathSearcher.get(event.getKey(), OnlineNodePath.createInstanceTypeSearchCriteria()).toUpperCase());
        String instanceId = NodePathSearcher.get(event.getKey(), OnlineNodePath.createInstanceIdSearchCriteria());
        ComputeNodeData computeNodeData = new YamlComputeNodeDataSwapper().swapToObject(YamlEngine.unmarshal(event.getValue(), YamlComputeNodeData.class));
        InstanceMetaData instanceMetaData = InstanceMetaDataFactory.create(instanceId, instanceType, computeNodeData);
        ClusterPersistServiceFacade clusterPersistServiceFacade = (ClusterPersistServiceFacade) contextManager.getPersistServiceFacade().getModeFacade();
        switch (event.getType()) {
            case ADDED:
                clusterInstanceRegistry.add(clusterPersistServiceFacade.getComputeNodeService().loadInstance(instanceMetaData));
                break;
            case DELETED:
                clusterInstanceRegistry.delete(new ComputeNodeInstance(instanceMetaData));
                break;
            default:
        }
    }
}
