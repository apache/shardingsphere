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

package org.apache.shardingsphere.mode.manager.cluster.event.dispatch.listener.type;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.metadata.DataSourceMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.metadata.TableMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.metadata.ViewMetaDataNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.builder.RuleConfigurationEventBuilder;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.node.AlterStorageNodeEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.node.RegisterStorageNodeEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.node.UnregisterStorageNodeEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.unit.AlterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.unit.RegisterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.unit.UnregisterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.SchemaAddedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.table.CreateOrAlterTableEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.table.DropTableEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.view.CreateOrAlterViewEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.view.DropViewEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;

import java.util.Optional;

/**
 * Database meta data changed listener.
 */
@RequiredArgsConstructor
public final class DatabaseMetaDataChangedListener implements DataChangedEventListener {
    
    private final EventBusContext eventBusContext;
    
    private final RuleConfigurationEventBuilder eventBuilder = new RuleConfigurationEventBuilder();
    
    @Override
    public void onChange(final DataChangedEvent event) {
        createDispatchEvent(event).ifPresent(eventBusContext::post);
    }
    
    private Optional<DispatchEvent> createDispatchEvent(final DataChangedEvent event) {
        String key = event.getKey();
        Optional<String> databaseName = DatabaseMetaDataNode.getDatabaseNameBySchemaNode(key);
        if (!databaseName.isPresent()) {
            return Optional.empty();
        }
        Optional<String> schemaName = DatabaseMetaDataNode.getSchemaName(key);
        if (schemaName.isPresent()) {
            return createSchemaChangedEvent(databaseName.get(), schemaName.get(), event);
        }
        schemaName = DatabaseMetaDataNode.getSchemaNameByTableNode(key);
        if (schemaName.isPresent() && isTableMetaDataChanged(event.getKey())) {
            return createTableChangedEvent(databaseName.get(), schemaName.get(), event);
        }
        if (schemaName.isPresent() && isViewMetaDataChanged(event.getKey())) {
            return createViewChangedEvent(databaseName.get(), schemaName.get(), event);
        }
        if (DataSourceMetaDataNode.isDataSourcesNode(key)) {
            return createDataSourceChangedEvent(databaseName.get(), event);
        }
        return eventBuilder.build(databaseName.get(), event);
    }
    
    private Optional<DispatchEvent> createSchemaChangedEvent(final String databaseName, final String schemaName, final DataChangedEvent event) {
        switch (event.getType()) {
            case ADDED:
            case UPDATED:
                return Optional.of(new SchemaAddedEvent(databaseName, schemaName));
            case DELETED:
                return Optional.of(new SchemaDeletedEvent(databaseName, schemaName));
            default:
                return Optional.empty();
        }
    }
    
    private boolean isTableMetaDataChanged(final String key) {
        return TableMetaDataNode.isTableActiveVersionNode(key) || TableMetaDataNode.isTableNode(key);
    }
    
    private Optional<DispatchEvent> createTableChangedEvent(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType()) && TableMetaDataNode.isTableActiveVersionNode(event.getKey())) {
            String tableName = TableMetaDataNode.getTableNameByActiveVersionNode(event.getKey()).orElseThrow(() -> new IllegalStateException("Table name not found."));
            return Optional.of(new CreateOrAlterTableEvent(databaseName, schemaName, tableName, event.getKey(), event.getValue()));
        }
        if (Type.DELETED == event.getType() && TableMetaDataNode.isTableNode(event.getKey())) {
            String tableName = TableMetaDataNode.getTableName(event.getKey()).orElseThrow(() -> new IllegalStateException("Table name not found."));
            return Optional.of(new DropTableEvent(databaseName, schemaName, tableName));
        }
        return Optional.empty();
    }
    
    private boolean isViewMetaDataChanged(final String key) {
        return ViewMetaDataNode.isViewActiveVersionNode(key) || ViewMetaDataNode.isViewNode(key);
    }
    
    private Optional<DispatchEvent> createViewChangedEvent(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType()) && ViewMetaDataNode.isViewActiveVersionNode(event.getKey())) {
            String viewName = ViewMetaDataNode.getViewNameByActiveVersionNode(event.getKey()).orElseThrow(() -> new IllegalStateException("View name not found."));
            return Optional.of(new CreateOrAlterViewEvent(databaseName, schemaName, viewName, event.getKey(), event.getValue()));
        }
        if (Type.DELETED == event.getType() && ViewMetaDataNode.isViewNode(event.getKey())) {
            String viewName = ViewMetaDataNode.getViewName(event.getKey()).orElseThrow(() -> new IllegalStateException("View name not found."));
            return Optional.of(new DropViewEvent(databaseName, schemaName, viewName, event.getKey(), event.getValue()));
        }
        return Optional.empty();
    }
    
    private Optional<DispatchEvent> createDataSourceChangedEvent(final String databaseName, final DataChangedEvent event) {
        if (DataSourceMetaDataNode.isDataSourceUnitActiveVersionNode(event.getKey()) || DataSourceMetaDataNode.isDataSourceUnitNode(event.getKey())) {
            return createStorageUnitChangedEvent(databaseName, event);
        }
        if (DataSourceMetaDataNode.isDataSourceNodeActiveVersionNode(event.getKey()) || DataSourceMetaDataNode.isDataSourceNodeNode(event.getKey())) {
            return createStorageNodeChangedEvent(databaseName, event);
        }
        return Optional.empty();
    }
    
    private Optional<DispatchEvent> createStorageUnitChangedEvent(final String databaseName, final DataChangedEvent event) {
        Optional<String> dataSourceUnitName = DataSourceMetaDataNode.getDataSourceNameByDataSourceUnitActiveVersionNode(event.getKey());
        if (dataSourceUnitName.isPresent()) {
            if (Type.ADDED == event.getType()) {
                return Optional.of(new RegisterStorageUnitEvent(databaseName, dataSourceUnitName.get(), event.getKey(), event.getValue()));
            }
            if (Type.UPDATED == event.getType()) {
                return Optional.of(new AlterStorageUnitEvent(databaseName, dataSourceUnitName.get(), event.getKey(), event.getValue()));
            }
        }
        dataSourceUnitName = DataSourceMetaDataNode.getDataSourceNameByDataSourceUnitNode(event.getKey());
        if (Type.DELETED == event.getType() && dataSourceUnitName.isPresent()) {
            return Optional.of(new UnregisterStorageUnitEvent(databaseName, dataSourceUnitName.get()));
        }
        return Optional.empty();
    }
    
    private Optional<DispatchEvent> createStorageNodeChangedEvent(final String databaseName, final DataChangedEvent event) {
        Optional<String> dataSourceNodeName = DataSourceMetaDataNode.getDataSourceNameByDataSourceNodeActiveVersionNode(event.getKey());
        if (dataSourceNodeName.isPresent()) {
            if (Type.ADDED == event.getType()) {
                return Optional.of(new RegisterStorageNodeEvent(databaseName, dataSourceNodeName.get(), event.getKey(), event.getValue()));
            }
            if (Type.UPDATED == event.getType()) {
                return Optional.of(new AlterStorageNodeEvent(databaseName, dataSourceNodeName.get(), event.getKey(), event.getValue()));
            }
        }
        dataSourceNodeName = DataSourceMetaDataNode.getDataSourceNameByDataSourceNodeNode(event.getKey());
        if (Type.DELETED == event.getType() && dataSourceNodeName.isPresent()) {
            return Optional.of(new UnregisterStorageNodeEvent(databaseName, dataSourceNodeName.get()));
        }
        return Optional.empty();
    }
}
