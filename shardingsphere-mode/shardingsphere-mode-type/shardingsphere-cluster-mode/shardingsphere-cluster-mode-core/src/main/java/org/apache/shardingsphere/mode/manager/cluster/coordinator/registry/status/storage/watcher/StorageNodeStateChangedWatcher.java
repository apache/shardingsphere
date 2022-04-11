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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.watcher;

import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.infra.metadata.schema.QualifiedSchema;
import org.apache.shardingsphere.infra.storage.StorageNodeDataSource;
import org.apache.shardingsphere.infra.storage.StorageNodeRole;
import org.apache.shardingsphere.infra.storage.StorageNodeStatus;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.PrimaryStateChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node.StorageStatusNode;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Storage node state changed watcher.
 */
public final class StorageNodeStateChangedWatcher implements GovernanceWatcher<GovernanceEvent> {
    
    @Override
    public Collection<String> getWatchingKeys() {
        return Collections.singletonList(StorageStatusNode.getRootPath());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        if (StringUtils.isEmpty(event.getValue())) {
            return Optional.empty();
        }
        Optional<QualifiedSchema> qualifiedSchema = StorageStatusNode.extractQualifiedSchema(event.getKey());
        if (qualifiedSchema.isPresent()) {
            QualifiedSchema schema = qualifiedSchema.get();
            StorageNodeDataSource storageNodeDataSource = YamlEngine.unmarshal(event.getValue(), StorageNodeDataSource.class);
            if (StorageNodeRole.PRIMARY.name().toLowerCase().equals(storageNodeDataSource.getRole())) {
                return Optional.of(new PrimaryStateChangedEvent(schema));
            }
            return Optional.of(new DisabledStateChangedEvent(schema, Type.DELETED == event.getType()
                    || StorageNodeStatus.DISABLED.name().toLowerCase().equals(storageNodeDataSource.getStatus())));
        }
        return Optional.empty();
    }
}
