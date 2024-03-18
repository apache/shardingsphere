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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.watcher;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.instance.ComputeNodeData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaDataFactory;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOnlineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.KillLocalProcessEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.KillLocalProcessCompletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ReportLocalProcessesCompletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ReportLocalProcessesEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.StateEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.WorkerIdEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compute node state changed watcher.
 */
public final class ComputeNodeStateChangedWatcher implements GovernanceWatcher<GovernanceEvent> {
    
    @Override
    public Collection<String> getWatchingKeys(final String databaseName) {
        return Collections.singleton(ComputeNode.getComputeNodePath());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        String instanceId = ComputeNode.getInstanceIdByComputeNode(event.getKey());
        if (!Strings.isNullOrEmpty(instanceId)) {
            Optional<GovernanceEvent> result = createInstanceGovernanceEvent(event, instanceId);
            if (result.isPresent()) {
                return result;
            }
        }
        if (event.getKey().startsWith(ComputeNode.getOnlineInstanceNodePath())) {
            return createInstanceEvent(event);
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
    private Optional<GovernanceEvent> createInstanceGovernanceEvent(final DataChangedEvent event, final String instanceId) {
        if (event.getKey().equals(ComputeNode.getInstanceStatusNodePath(instanceId)) && Type.DELETED != event.getType()) {
            return Optional.of(new StateEvent(instanceId, event.getValue()));
        }
        if (event.getKey().equals(ComputeNode.getInstanceLabelsNodePath(instanceId)) && Type.DELETED != event.getType()) {
            return Optional.of(new LabelsEvent(instanceId, Strings.isNullOrEmpty(event.getValue()) ? new ArrayList<>() : YamlEngine.unmarshal(event.getValue(), Collection.class)));
        }
        if (event.getKey().equals(ComputeNode.getInstanceWorkerIdNodePath(instanceId))) {
            return Optional.of(new WorkerIdEvent(instanceId, Strings.isNullOrEmpty(event.getValue()) ? null : Integer.valueOf(event.getValue())));
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createInstanceEvent(final DataChangedEvent event) {
        Matcher matcher = getInstanceOnlinePathMatcher(event.getKey());
        if (matcher.find()) {
            ComputeNodeData computeNodeData = YamlEngine.unmarshal(event.getValue(), ComputeNodeData.class);
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
    
    private Optional<GovernanceEvent> createReportLocalProcessesEvent(final DataChangedEvent event) {
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
    
    private Optional<GovernanceEvent> createKillLocalProcessEvent(final DataChangedEvent event) {
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
