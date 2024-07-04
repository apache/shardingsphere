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

import org.apache.shardingsphere.infra.instance.ComputeNodeData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaDataFactory;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.instance.yaml.YamlComputeNodeData;
import org.apache.shardingsphere.infra.instance.yaml.YamlComputeNodeDataSwapper;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.instance.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.instance.InstanceOnlineEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Compute node online dispatch event builder.
 */
public final class ComputeNodeOnlineDispatchEventBuilder implements DispatchEventBuilder<DispatchEvent> {
    
    @Override
    public Collection<String> getSubscribedKeys() {
        return Collections.singleton(ComputeNode.getOnlineInstanceNodePath());
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public Optional<DispatchEvent> build(final DataChangedEvent event) {
        if (event.getKey().startsWith(ComputeNode.getOnlineInstanceNodePath())) {
            return createInstanceEvent(event);
        }
        return Optional.empty();
    }
    
    private Optional<DispatchEvent> createInstanceEvent(final DataChangedEvent event) {
        Matcher matcher = getInstanceOnlinePathMatcher(event.getKey());
        if (matcher.find()) {
            ComputeNodeData computeNodeData = new YamlComputeNodeDataSwapper().swapToObject(YamlEngine.unmarshal(event.getValue(), YamlComputeNodeData.class));
            InstanceMetaData instanceMetaData = InstanceMetaDataFactory.create(matcher.group(2),
                    InstanceType.valueOf(matcher.group(1).toUpperCase()), computeNodeData.getAttribute(), computeNodeData.getVersion());
            if (Type.ADDED == event.getType()) {
                return Optional.of(new InstanceOnlineEvent(instanceMetaData));
            }
            if (Type.DELETED == event.getType()) {
                return Optional.of(new InstanceOfflineEvent(instanceMetaData));
            }
        }
        return Optional.empty();
    }
    
    private Matcher getInstanceOnlinePathMatcher(final String onlineInstancePath) {
        return Pattern.compile(ComputeNode.getOnlineInstanceNodePath() + "/([\\S]+)/([\\S]+)$", Pattern.CASE_INSENSITIVE).matcher(onlineInstancePath);
    }
}
