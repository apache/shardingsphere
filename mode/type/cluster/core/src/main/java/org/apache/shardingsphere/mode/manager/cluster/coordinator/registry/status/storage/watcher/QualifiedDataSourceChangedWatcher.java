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

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.StorageNodeChangedEvent;
import org.apache.shardingsphere.mode.storage.node.QualifiedDataSourceNode;
import org.apache.shardingsphere.mode.storage.yaml.YamlQualifiedDataSourceStatus;
import org.apache.shardingsphere.mode.storage.yaml.YamlQualifiedDataSourceStatusSwapper;
import org.apache.shardingsphere.mode.storage.QualifiedDataSourceStatus;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Qualified data source changed watcher.
 */
public final class QualifiedDataSourceChangedWatcher implements GovernanceWatcher<GovernanceEvent> {
    
    @Override
    public Collection<String> getWatchingKeys(final Collection<String> databaseNames) {
        return Collections.singleton(QualifiedDataSourceNode.getRootPath());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED);
    }
    
    @Override
    public Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        if (Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        Optional<QualifiedDataSource> qualifiedDataSource = QualifiedDataSourceNode.extractQualifiedDataSource(event.getKey());
        if (qualifiedDataSource.isPresent()) {
            QualifiedDataSourceStatus status = new YamlQualifiedDataSourceStatusSwapper().swapToObject(YamlEngine.unmarshal(event.getValue(), YamlQualifiedDataSourceStatus.class));
            return Optional.of(new StorageNodeChangedEvent(qualifiedDataSource.get(), status));
        }
        return Optional.empty();
    }
}
