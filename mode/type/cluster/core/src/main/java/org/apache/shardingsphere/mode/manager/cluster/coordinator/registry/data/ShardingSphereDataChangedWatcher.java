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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereTableData;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlShardingSphereTableDataSwapper;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.DatabaseDataAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.DatabaseDataDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.SchemaDataAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.SchemaDataDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.TableDataChangedEvent;
import org.apache.shardingsphere.mode.metadata.persist.node.ShardingSphereDataNode;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * ShardingSphere data changed watcher.
 */
public final class ShardingSphereDataChangedWatcher implements GovernanceWatcher<GovernanceEvent> {
    
    @Override
    public Collection<String> getWatchingKeys(final String databaseName) {
        return Collections.singleton(ShardingSphereDataNode.getShardingSphereDataNodePath());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        if (isDatabaseChanged(event)) {
            return createDatabaseChangedEvent(event);
        }
        if (isSchemaChanged(event)) {
            return createSchemaChangedEvent(event);
        }
        if (isSchemaDataChanged(event)) {
            return createSchemaDataChangedEvent(event);
        }
        return Optional.empty();
    }
    
    private boolean isDatabaseChanged(final DataChangedEvent event) {
        return ShardingSphereDataNode.getDatabaseName(event.getKey()).isPresent();
    }
    
    private boolean isSchemaChanged(final DataChangedEvent event) {
        return ShardingSphereDataNode.getDatabaseNameByDatabasePath(event.getKey()).isPresent() && ShardingSphereDataNode.getSchemaName(event.getKey()).isPresent();
    }
    
    private boolean isSchemaDataChanged(final DataChangedEvent event) {
        Optional<String> databaseName = ShardingSphereDataNode.getDatabaseNameByDatabasePath(event.getKey());
        Optional<String> schemaName = ShardingSphereDataNode.getSchemaNameBySchemaPath(event.getKey());
        Optional<String> tableName = ShardingSphereDataNode.getTableName(event.getKey());
        return databaseName.isPresent() && schemaName.isPresent() && null != event.getValue() && !event.getValue().isEmpty() && tableName.isPresent();
    }
    
    private Optional<GovernanceEvent> createDatabaseChangedEvent(final DataChangedEvent event) {
        Optional<String> databaseName = ShardingSphereDataNode.getDatabaseName(event.getKey());
        Preconditions.checkState(databaseName.isPresent());
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new DatabaseDataAddedEvent(databaseName.get()));
        }
        if (Type.DELETED == event.getType()) {
            return Optional.of(new DatabaseDataDeletedEvent(databaseName.get()));
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createSchemaChangedEvent(final DataChangedEvent event) {
        Optional<String> databaseName = ShardingSphereDataNode.getDatabaseNameByDatabasePath(event.getKey());
        Preconditions.checkState(databaseName.isPresent());
        Optional<String> schemaName = ShardingSphereDataNode.getSchemaName(event.getKey());
        Preconditions.checkState(schemaName.isPresent());
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new SchemaDataAddedEvent(databaseName.get(), schemaName.get()));
        }
        if (Type.DELETED == event.getType()) {
            return Optional.of(new SchemaDataDeletedEvent(databaseName.get(), schemaName.get()));
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createSchemaDataChangedEvent(final DataChangedEvent event) {
        Optional<String> databaseName = ShardingSphereDataNode.getDatabaseNameByDatabasePath(event.getKey());
        Preconditions.checkState(databaseName.isPresent());
        Optional<String> schemaName = ShardingSphereDataNode.getSchemaNameBySchemaPath(event.getKey());
        Preconditions.checkState(schemaName.isPresent());
        return Optional.of(doCreateSchemaDataChangedEvent(event, databaseName.get(), schemaName.get()));
    }
    
    private GovernanceEvent doCreateSchemaDataChangedEvent(final DataChangedEvent event, final String databaseName, final String schemaName) {
        Optional<String> tableName = ShardingSphereDataNode.getTableName(event.getKey());
        Preconditions.checkState(tableName.isPresent());
        return Type.DELETED == event.getType()
                ? new TableDataChangedEvent(databaseName, schemaName, null, tableName.get())
                : new TableDataChangedEvent(databaseName, schemaName, new YamlShardingSphereTableDataSwapper()
                        .swapToObject(YamlEngine.unmarshal(event.getValue(), YamlShardingSphereTableData.class)), null);
    }
}
