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
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.ComputeNodeInstanceStateChangedEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.KillLocalProcessCompletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.KillLocalProcessEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.LabelsEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.ReportLocalProcessesCompletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.ReportLocalProcessesEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.WorkerIdEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compute node state dispatch event builder.
 */
public final class ComputeNodeStateDispatchEventBuilder implements DispatchEventBuilder<DispatchEvent> {
    
    @Override
    public Collection<String> getSubscribedKeys() {
        return Collections.singleton(ComputeNode.getComputeNodePath());
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public Optional<DispatchEvent> build(final DataChangedEvent event) {
        String instanceId = ComputeNode.getInstanceIdByComputeNode(event.getKey());
        if (!Strings.isNullOrEmpty(instanceId)) {
            Optional<DispatchEvent> result = createInstanceDispatchEvent(event, instanceId);
            if (result.isPresent()) {
                return result;
            }
        }
        if (event.getKey().startsWith(ComputeNode.getShowProcessListTriggerNodePath())) {
            return createReportLocalProcessesEvent(event);
        }
        if (event.getKey().startsWith(ComputeNode.getKillProcessTriggerNodePath())) {
            return createKillLocalProcessEvent(event);
        }
        return Optional.empty();
    }
    
    @SuppressWarnings("unchecked")
    private Optional<DispatchEvent> createInstanceDispatchEvent(final DataChangedEvent event, final String instanceId) {
        if (event.getKey().equals(ComputeNode.getComputeNodeStateNodePath(instanceId)) && Type.DELETED != event.getType()) {
            return Optional.of(new ComputeNodeInstanceStateChangedEvent(instanceId, event.getValue()));
        }
        if (event.getKey().equals(ComputeNode.getInstanceLabelsNodePath(instanceId)) && Type.DELETED != event.getType()) {
            return Optional.of(new LabelsEvent(instanceId, Strings.isNullOrEmpty(event.getValue()) ? new ArrayList<>() : YamlEngine.unmarshal(event.getValue(), Collection.class)));
        }
        if (event.getKey().equals(ComputeNode.getInstanceWorkerIdNodePath(instanceId))) {
            return Optional.of(new WorkerIdEvent(instanceId, Strings.isNullOrEmpty(event.getValue()) ? null : Integer.valueOf(event.getValue())));
        }
        return Optional.empty();
    }
    
    private Optional<DispatchEvent> createReportLocalProcessesEvent(final DataChangedEvent event) {
        Matcher matcher = getShowProcessListTriggerMatcher(event);
        if (!matcher.find()) {
            return Optional.empty();
        }
        if (Type.ADDED == event.getType()) {
            return Optional.of(new ReportLocalProcessesEvent(matcher.group(1), matcher.group(2)));
        }
        if (Type.DELETED == event.getType()) {
            return Optional.of(new ReportLocalProcessesCompletedEvent(matcher.group(2)));
        }
        return Optional.empty();
    }
    
    private Matcher getShowProcessListTriggerMatcher(final DataChangedEvent event) {
        return Pattern.compile(ComputeNode.getShowProcessListTriggerNodePath() + "/([\\S]+):([\\S]+)$", Pattern.CASE_INSENSITIVE).matcher(event.getKey());
    }
    
    private Optional<DispatchEvent> createKillLocalProcessEvent(final DataChangedEvent event) {
        Matcher matcher = getKillProcessTriggerMatcher(event);
        if (!matcher.find()) {
            return Optional.empty();
        }
        if (Type.ADDED == event.getType()) {
            return Optional.of(new KillLocalProcessEvent(matcher.group(1), matcher.group(2)));
        }
        if (Type.DELETED == event.getType()) {
            return Optional.of(new KillLocalProcessCompletedEvent(matcher.group(2)));
        }
        return Optional.empty();
    }
    
    private Matcher getKillProcessTriggerMatcher(final DataChangedEvent event) {
        Pattern pattern = Pattern.compile(ComputeNode.getKillProcessTriggerNodePath() + "/([\\S]+):([\\S]+)$", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(event.getKey());
    }
}
