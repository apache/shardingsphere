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
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOnlineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessListTriggerEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessListUnitCompleteEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.StateEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.WorkerIdEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.XaRecoveryIdAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.XaRecoveryIdDeletedEvent;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;

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
    public Collection<String> getWatchingKeys() {
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
            if (event.getKey().equals(ComputeNode.getInstanceStatusNodePath(instanceId))) {
                Collection<String> status = Strings.isNullOrEmpty(event.getValue()) ? new ArrayList<>() : YamlEngine.unmarshal(event.getValue(), Collection.class);
                return Optional.of(new StateEvent(instanceId, status));
            } else if (event.getKey().equals(ComputeNode.getInstanceWorkerIdNodePath(instanceId))) {
                return Optional.of(new WorkerIdEvent(instanceId, Strings.isNullOrEmpty(event.getValue()) ? null : Long.valueOf(event.getValue())));
            } else if (event.getKey().equals(ComputeNode.getInstanceLabelsNodePath(instanceId))) {
                return Optional.of(new LabelsEvent(instanceId, Strings.isNullOrEmpty(event.getValue()) ? new ArrayList<>() : YamlEngine.unmarshal(event.getValue(), Collection.class)));
            }
        } else if (event.getKey().startsWith(ComputeNode.getOnlineInstanceNodePath())) {
            Optional<InstanceDefinition> instanceDefinition = ComputeNode.getInstanceDefinitionByInstanceOnlinePath(event.getKey());
            return instanceDefinition.isPresent() ? createInstanceEvent(instanceDefinition.get(), event.getType(), event.getValue()) : Optional.empty();
        } else if (event.getKey().startsWith(ComputeNode.getProcessTriggerNodePatch())) {
            return createShowProcessListTriggerEvent(event);
        } else if (event.getKey().startsWith(ComputeNode.getXaRecoveryIdNodePath())) {
            return createXaRecoveryIdEvent(event);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createShowProcessListTriggerEvent(final DataChangedEvent event) {
        Matcher matcher = getShowProcessTriggerMatcher(event);
        if (!matcher.find()) {
            return Optional.empty();
        }
        if (Type.ADDED == event.getType()) {
            return Optional.of(new ShowProcessListTriggerEvent(matcher.group(1), matcher.group(2)));
        } else if (Type.DELETED == event.getType()) {
            return Optional.of(new ShowProcessListUnitCompleteEvent(matcher.group(2)));
        }
        return Optional.empty();
    }
    
    private static Matcher getShowProcessTriggerMatcher(final DataChangedEvent event) {
        Pattern pattern = Pattern.compile(ComputeNode.getProcessTriggerNodePatch() + "/([\\S]+):([\\S]+)$", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(event.getKey());
    }
    
    private Optional<GovernanceEvent> createInstanceEvent(final InstanceDefinition instanceDefinition, final Type type, final String value) {
        if (Type.ADDED == type) {
            instanceDefinition.setAttributes(value);
            return Optional.of(new InstanceOnlineEvent(instanceDefinition));
        } else if (Type.DELETED == type) {
            return Optional.of(new InstanceOfflineEvent(instanceDefinition));
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createXaRecoveryIdEvent(final DataChangedEvent event) {
        Matcher matcher = Pattern.compile(ComputeNode.getXaRecoveryIdNodePath() + "/([\\S]+)/([\\S]+)$", Pattern.CASE_INSENSITIVE).matcher(event.getKey());
        if (matcher.find()) {
            if (Type.ADDED == event.getType()) {
                return Optional.of(new XaRecoveryIdAddedEvent(matcher.group(2), matcher.group(1)));
            } else if (Type.DELETED == event.getType()) {
                return Optional.of(new XaRecoveryIdDeletedEvent(matcher.group(2), matcher.group(1)));
            }
        }
        return Optional.empty();
    }
}
