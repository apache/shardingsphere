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

package org.apache.shardingsphere.mode.manager.cluster.event.dispatch.handler.database.metadata;

import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.metadata.DataSourceMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.metadata.TableMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.metadata.ViewMetaDataNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Optional;

/**
 * Meta data changed handler.
 */
public final class MetaDataChangedHandler {
    
    private final SchemaChangedHandler schemaChangedHandler;
    
    private final TableChangedHandler tableChangedHandler;
    
    private final ViewChangedHandler viewChangedHandler;
    
    private final StorageUnitChangedHandler storageUnitChangedHandler;
    
    private final StorageNodeChangedHandler storageNodeChangedHandler;
    
    public MetaDataChangedHandler(final ContextManager contextManager) {
        schemaChangedHandler = new SchemaChangedHandler(contextManager);
        tableChangedHandler = new TableChangedHandler(contextManager);
        viewChangedHandler = new ViewChangedHandler(contextManager);
        storageUnitChangedHandler = new StorageUnitChangedHandler(contextManager);
        storageNodeChangedHandler = new StorageNodeChangedHandler(contextManager);
    }
    
    /**
     * Handle meta data changed.
     *
     * @param databaseName database name
     * @param event data changed event
     * @return handle completed or not
     */
    public boolean handle(final String databaseName, final DataChangedEvent event) {
        String eventKey = event.getKey();
        Optional<String> schemaName = DatabaseMetaDataNode.getSchemaName(eventKey);
        if (schemaName.isPresent()) {
            handleSchemaChanged(databaseName, schemaName.get(), event);
            return true;
        }
        schemaName = DatabaseMetaDataNode.getSchemaNameByTableNode(eventKey);
        if (schemaName.isPresent() && isTableMetaDataChanged(eventKey)) {
            handleTableChanged(databaseName, schemaName.get(), event);
            return true;
        }
        if (schemaName.isPresent() && isViewMetaDataChanged(eventKey)) {
            handleViewChanged(databaseName, schemaName.get(), event);
            return true;
        }
        if (DataSourceMetaDataNode.isDataSourcesNode(eventKey)) {
            handleDataSourceChanged(databaseName, event);
            return true;
        }
        return false;
    }
    
    private void handleSchemaChanged(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            schemaChangedHandler.handleCreated(databaseName, schemaName);
        } else if (Type.DELETED == event.getType()) {
            schemaChangedHandler.handleDropped(databaseName, schemaName);
        }
    }
    
    private boolean isTableMetaDataChanged(final String key) {
        return TableMetaDataNode.isTableActiveVersionNode(key) || TableMetaDataNode.isTableNode(key);
    }
    
    private void handleTableChanged(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType()) && TableMetaDataNode.isTableActiveVersionNode(event.getKey())) {
            tableChangedHandler.handleCreatedOrAltered(databaseName, schemaName, event);
        } else if (Type.DELETED == event.getType() && TableMetaDataNode.isTableNode(event.getKey())) {
            tableChangedHandler.handleDropped(databaseName, schemaName, event);
        }
    }
    
    private boolean isViewMetaDataChanged(final String key) {
        return ViewMetaDataNode.isViewActiveVersionNode(key) || ViewMetaDataNode.isViewNode(key);
    }
    
    private void handleViewChanged(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType()) && ViewMetaDataNode.isViewActiveVersionNode(event.getKey())) {
            viewChangedHandler.handleCreatedOrAltered(databaseName, schemaName, event);
        } else if (Type.DELETED == event.getType() && ViewMetaDataNode.isViewNode(event.getKey())) {
            viewChangedHandler.handleDropped(databaseName, schemaName, event);
        }
    }
    
    private void handleDataSourceChanged(final String databaseName, final DataChangedEvent event) {
        if (DataSourceMetaDataNode.isDataSourceUnitActiveVersionNode(event.getKey()) || DataSourceMetaDataNode.isDataSourceUnitNode(event.getKey())) {
            handleStorageUnitChanged(databaseName, event);
        } else if (DataSourceMetaDataNode.isDataSourceNodeActiveVersionNode(event.getKey()) || DataSourceMetaDataNode.isDataSourceNodeNode(event.getKey())) {
            handleStorageNodeChanged(databaseName, event);
        }
    }
    
    private void handleStorageUnitChanged(final String databaseName, final DataChangedEvent event) {
        Optional<String> dataSourceUnitName = DataSourceMetaDataNode.getDataSourceNameByDataSourceUnitActiveVersionNode(event.getKey());
        if (dataSourceUnitName.isPresent()) {
            if (Type.ADDED == event.getType()) {
                storageUnitChangedHandler.handleRegistered(databaseName, dataSourceUnitName.get(), event);
            } else if (Type.UPDATED == event.getType()) {
                storageUnitChangedHandler.handleAltered(databaseName, dataSourceUnitName.get(), event);
            }
            return;
        }
        dataSourceUnitName = DataSourceMetaDataNode.getDataSourceNameByDataSourceUnitNode(event.getKey());
        if (Type.DELETED == event.getType() && dataSourceUnitName.isPresent()) {
            storageUnitChangedHandler.handleUnregistered(databaseName, dataSourceUnitName.get());
        }
    }
    
    private void handleStorageNodeChanged(final String databaseName, final DataChangedEvent event) {
        Optional<String> dataSourceNodeName = DataSourceMetaDataNode.getDataSourceNameByDataSourceNodeActiveVersionNode(event.getKey());
        if (dataSourceNodeName.isPresent()) {
            if (Type.ADDED == event.getType()) {
                storageNodeChangedHandler.handleRegistered(databaseName, dataSourceNodeName.get(), event);
            } else if (Type.UPDATED == event.getType()) {
                storageNodeChangedHandler.handleAltered(databaseName, dataSourceNodeName.get(), event);
            }
            return;
        }
        dataSourceNodeName = DataSourceMetaDataNode.getDataSourceNameByDataSourceNodeNode(event.getKey());
        if (Type.DELETED == event.getType() && dataSourceNodeName.isPresent()) {
            storageNodeChangedHandler.handleUnregistered(databaseName, dataSourceNodeName.get());
        }
    }
}
