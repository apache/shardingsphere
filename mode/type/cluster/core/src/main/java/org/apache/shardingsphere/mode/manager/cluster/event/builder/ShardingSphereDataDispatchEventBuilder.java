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
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereRowData;
import org.apache.shardingsphere.metadata.persist.node.ShardingSphereDataNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.DatabaseDataAddedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.DatabaseDataDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.SchemaDataAddedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.SchemaDataDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.ShardingSphereRowDataChangedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.ShardingSphereRowDataDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.TableDataChangedEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * ShardingSphere data dispatch event builder.
 */
public final class ShardingSphereDataDispatchEventBuilder implements DispatchEventBuilder<DispatchEvent> {
    
    @Override
    public String getSubscribedKey() {
        return ShardingSphereDataNode.getShardingSphereDataNodePath();
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public Optional<DispatchEvent> build(final DataChangedEvent event) {
        Optional<String> databaseName = ShardingSphereDataNode.getDatabaseName(event.getKey());
        if (databaseName.isPresent()) {
            return createDatabaseChangedEvent(event, databaseName.get());
        }
        databaseName = ShardingSphereDataNode.getDatabaseNameByDatabasePath(event.getKey());
        if (!databaseName.isPresent()) {
            return Optional.empty();
        }
        Optional<String> schemaName = ShardingSphereDataNode.getSchemaName(event.getKey());
        if (schemaName.isPresent()) {
            return createSchemaChangedEvent(event, databaseName.get(), schemaName.get());
        }
        schemaName = ShardingSphereDataNode.getSchemaNameBySchemaPath(event.getKey());
        if (!schemaName.isPresent()) {
            return Optional.empty();
        }
        Optional<String> tableName = ShardingSphereDataNode.getTableName(event.getKey());
        if (tableName.isPresent()) {
            return createTableChangedEvent(event, databaseName.get(), schemaName.get(), tableName.get());
        }
        tableName = ShardingSphereDataNode.getTableNameByRowPath(event.getKey());
        if (!tableName.isPresent()) {
            return Optional.empty();
        }
        Optional<String> rowPath = ShardingSphereDataNode.getRowUniqueKey(event.getKey());
        if (rowPath.isPresent()) {
            return createRowDataChangedEvent(event, databaseName.get(), schemaName.get(), tableName.get(), rowPath.get());
        }
        return Optional.empty();
    }
    
    private Optional<DispatchEvent> createDatabaseChangedEvent(final DataChangedEvent event, final String databaseName) {
        switch (event.getType()) {
            case ADDED:
            case UPDATED:
                return Optional.of(new DatabaseDataAddedEvent(databaseName));
            case DELETED:
                return Optional.of(new DatabaseDataDeletedEvent(databaseName));
            default:
                return Optional.empty();
        }
    }
    
    private Optional<DispatchEvent> createSchemaChangedEvent(final DataChangedEvent event, final String databaseName, final String schemaName) {
        switch (event.getType()) {
            case ADDED:
            case UPDATED:
                return Optional.of(new SchemaDataAddedEvent(databaseName, schemaName));
            case DELETED:
                return Optional.of(new SchemaDataDeletedEvent(databaseName, schemaName));
            default:
                return Optional.empty();
        }
    }
    
    private Optional<DispatchEvent> createTableChangedEvent(final DataChangedEvent event, final String databaseName, final String schemaName, final String tableName) {
        switch (event.getType()) {
            case ADDED:
            case UPDATED:
                return Optional.of(new TableDataChangedEvent(databaseName, schemaName, tableName, null));
            case DELETED:
                return Optional.of(new TableDataChangedEvent(databaseName, schemaName, null, tableName));
            default:
                return Optional.empty();
        }
    }
    
    private Optional<DispatchEvent> createRowDataChangedEvent(final DataChangedEvent event, final String databaseName, final String schemaName, final String tableName, final String rowPath) {
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType()) && !Strings.isNullOrEmpty(event.getValue())) {
            return Optional.of(new ShardingSphereRowDataChangedEvent(databaseName, schemaName, tableName, YamlEngine.unmarshal(event.getValue(), YamlShardingSphereRowData.class)));
        }
        if (Type.DELETED == event.getType()) {
            return Optional.of(new ShardingSphereRowDataDeletedEvent(databaseName, schemaName, tableName, rowPath));
        }
        return Optional.empty();
    }
}
