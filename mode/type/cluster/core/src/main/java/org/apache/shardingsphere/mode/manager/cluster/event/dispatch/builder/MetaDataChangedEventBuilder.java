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

package org.apache.shardingsphere.mode.manager.cluster.event.dispatch.builder;

import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.metadata.DataSourceMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.metadata.TableMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.metadata.ViewMetaDataNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.node.StorageNodeAlteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.node.StorageNodeRegisteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.node.StorageNodeUnregisteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.unit.StorageUnitAlteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.unit.StorageUnitRegisteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.unit.StorageUnitUnregisteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.SchemaAddedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.table.TableCreatedOrAlteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.table.TableDroppedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.view.ViewCreatedOrAlteredEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.view.ViewDroppedEvent;

import java.util.Optional;

/**
 * Meta data changed event builder.
 */
public final class MetaDataChangedEventBuilder {
    
    /**
     * Build meta data changed event.
     *
     * @param databaseName database name
     * @param event data changed event
     * @return built event
     */
    public Optional<DispatchEvent> build(final String databaseName, final DataChangedEvent event) {
        String key = event.getKey();
        Optional<String> schemaName = DatabaseMetaDataNode.getSchemaName(key);
        if (schemaName.isPresent()) {
            return buildSchemaChangedEvent(databaseName, schemaName.get(), event);
        }
        schemaName = DatabaseMetaDataNode.getSchemaNameByTableNode(key);
        if (schemaName.isPresent() && isTableMetaDataChanged(event.getKey())) {
            return buildTableChangedEvent(databaseName, schemaName.get(), event);
        }
        if (schemaName.isPresent() && isViewMetaDataChanged(event.getKey())) {
            return buildViewChangedEvent(databaseName, schemaName.get(), event);
        }
        if (DataSourceMetaDataNode.isDataSourcesNode(key)) {
            return buildDataSourceChangedEvent(databaseName, event);
        }
        return Optional.empty();
    }
    
    private Optional<DispatchEvent> buildSchemaChangedEvent(final String databaseName, final String schemaName, final DataChangedEvent event) {
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
    
    private Optional<DispatchEvent> buildTableChangedEvent(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType()) && TableMetaDataNode.isTableActiveVersionNode(event.getKey())) {
            String tableName = TableMetaDataNode.getTableNameByActiveVersionNode(event.getKey()).orElseThrow(() -> new IllegalStateException("Table name not found."));
            return Optional.of(new TableCreatedOrAlteredEvent(databaseName, schemaName, tableName, event.getKey(), event.getValue()));
        }
        if (Type.DELETED == event.getType() && TableMetaDataNode.isTableNode(event.getKey())) {
            String tableName = TableMetaDataNode.getTableName(event.getKey()).orElseThrow(() -> new IllegalStateException("Table name not found."));
            return Optional.of(new TableDroppedEvent(databaseName, schemaName, tableName));
        }
        return Optional.empty();
    }
    
    private boolean isViewMetaDataChanged(final String key) {
        return ViewMetaDataNode.isViewActiveVersionNode(key) || ViewMetaDataNode.isViewNode(key);
    }
    
    private Optional<DispatchEvent> buildViewChangedEvent(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType()) && ViewMetaDataNode.isViewActiveVersionNode(event.getKey())) {
            String viewName = ViewMetaDataNode.getViewNameByActiveVersionNode(event.getKey()).orElseThrow(() -> new IllegalStateException("View name not found."));
            return Optional.of(new ViewCreatedOrAlteredEvent(databaseName, schemaName, viewName, event.getKey(), event.getValue()));
        }
        if (Type.DELETED == event.getType() && ViewMetaDataNode.isViewNode(event.getKey())) {
            String viewName = ViewMetaDataNode.getViewName(event.getKey()).orElseThrow(() -> new IllegalStateException("View name not found."));
            return Optional.of(new ViewDroppedEvent(databaseName, schemaName, viewName, event.getKey(), event.getValue()));
        }
        return Optional.empty();
    }
    
    private Optional<DispatchEvent> buildDataSourceChangedEvent(final String databaseName, final DataChangedEvent event) {
        if (DataSourceMetaDataNode.isDataSourceUnitActiveVersionNode(event.getKey()) || DataSourceMetaDataNode.isDataSourceUnitNode(event.getKey())) {
            return buildStorageUnitChangedEvent(databaseName, event);
        }
        if (DataSourceMetaDataNode.isDataSourceNodeActiveVersionNode(event.getKey()) || DataSourceMetaDataNode.isDataSourceNodeNode(event.getKey())) {
            return buildStorageNodeChangedEvent(databaseName, event);
        }
        return Optional.empty();
    }
    
    private Optional<DispatchEvent> buildStorageUnitChangedEvent(final String databaseName, final DataChangedEvent event) {
        Optional<String> dataSourceUnitName = DataSourceMetaDataNode.getDataSourceNameByDataSourceUnitActiveVersionNode(event.getKey());
        if (dataSourceUnitName.isPresent()) {
            if (Type.ADDED == event.getType()) {
                return Optional.of(new StorageUnitRegisteredEvent(databaseName, dataSourceUnitName.get(), event.getKey(), event.getValue()));
            }
            if (Type.UPDATED == event.getType()) {
                return Optional.of(new StorageUnitAlteredEvent(databaseName, dataSourceUnitName.get(), event.getKey(), event.getValue()));
            }
        }
        dataSourceUnitName = DataSourceMetaDataNode.getDataSourceNameByDataSourceUnitNode(event.getKey());
        if (Type.DELETED == event.getType() && dataSourceUnitName.isPresent()) {
            return Optional.of(new StorageUnitUnregisteredEvent(databaseName, dataSourceUnitName.get()));
        }
        return Optional.empty();
    }
    
    private Optional<DispatchEvent> buildStorageNodeChangedEvent(final String databaseName, final DataChangedEvent event) {
        Optional<String> dataSourceNodeName = DataSourceMetaDataNode.getDataSourceNameByDataSourceNodeActiveVersionNode(event.getKey());
        if (dataSourceNodeName.isPresent()) {
            if (Type.ADDED == event.getType()) {
                return Optional.of(new StorageNodeRegisteredEvent(databaseName, dataSourceNodeName.get(), event.getKey(), event.getValue()));
            }
            if (Type.UPDATED == event.getType()) {
                return Optional.of(new StorageNodeAlteredEvent(databaseName, dataSourceNodeName.get(), event.getKey(), event.getValue()));
            }
        }
        dataSourceNodeName = DataSourceMetaDataNode.getDataSourceNameByDataSourceNodeNode(event.getKey());
        if (Type.DELETED == event.getType() && dataSourceNodeName.isPresent()) {
            return Optional.of(new StorageNodeUnregisteredEvent(databaseName, dataSourceNodeName.get()));
        }
        return Optional.empty();
    }
}
