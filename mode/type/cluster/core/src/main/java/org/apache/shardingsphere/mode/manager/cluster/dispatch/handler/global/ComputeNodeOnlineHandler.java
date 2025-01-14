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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global;

import org.apache.shardingsphere.infra.instance.ComputeNodeData;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaDataFactory;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.instance.yaml.YamlComputeNodeData;
import org.apache.shardingsphere.infra.instance.yaml.YamlComputeNodeDataSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.node.path.metadata.ComputeNodePath;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.DataChangedEventHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Compute node online handler.
 */
public final class ComputeNodeOnlineHandler implements DataChangedEventHandler {
    
    @Override
    public String getSubscribedKey() {
        return ComputeNodePath.getOnlineRootPath();
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.DELETED);
    }
    
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        Matcher matcher = getInstanceOnlinePathMatcher(event.getKey());
        if (!matcher.find()) {
            return;
        }
        ComputeNodeData computeNodeData = new YamlComputeNodeDataSwapper().swapToObject(YamlEngine.unmarshal(event.getValue(), YamlComputeNodeData.class));
        InstanceMetaData instanceMetaData = InstanceMetaDataFactory.create(matcher.group(2), InstanceType.valueOf(matcher.group(1).toUpperCase()), computeNodeData);
        if (Type.ADDED == event.getType()) {
            contextManager.getComputeNodeInstanceContext().getClusterInstanceRegistry()
                    .add(contextManager.getPersistServiceFacade().getComputeNodePersistService().loadInstance(instanceMetaData));
        } else if (Type.DELETED == event.getType()) {
            contextManager.getComputeNodeInstanceContext().getClusterInstanceRegistry().delete(new ComputeNodeInstance(instanceMetaData));
        }
    }
    
    private Matcher getInstanceOnlinePathMatcher(final String onlineInstancePath) {
        return Pattern.compile(ComputeNodePath.getOnlineRootPath() + "/([\\S]+)/([\\S]+)$", Pattern.CASE_INSENSITIVE).matcher(onlineInstancePath);
    }
}
