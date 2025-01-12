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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata;

import org.apache.shardingsphere.mode.node.path.metadata.DatabaseMetaDataNodePath;
import org.apache.shardingsphere.mode.node.path.metadata.DataSourceMetaDataNodePath;
import org.apache.shardingsphere.mode.node.path.metadata.TableMetaDataNodePath;
import org.apache.shardingsphere.mode.node.path.metadata.ViewMetaDataNodePath;
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
        Optional<String> schemaName = DatabaseMetaDataNodePath.findSchemaName(eventKey, false);
        if (schemaName.isPresent()) {
            handleSchemaChanged(databaseName, schemaName.get(), event);
            return true;
        }
        schemaName = DatabaseMetaDataNodePath.findSchemaName(eventKey, true);
        if (schemaName.isPresent() && isTableMetaDataChanged(eventKey)) {
            handleTableChanged(databaseName, schemaName.get(), event);
            return true;
        }
        if (schemaName.isPresent() && isViewMetaDataChanged(eventKey)) {
            handleViewChanged(databaseName, schemaName.get(), event);
            return true;
        }
        if (DataSourceMetaDataNodePath.isDataSourceRootPath(eventKey)) {
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
        return TableMetaDataNodePath.isTableActiveVersionPath(key) || TableMetaDataNodePath.isTablePath(key);
    }
    
    private void handleTableChanged(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType()) && TableMetaDataNodePath.isTableActiveVersionPath(event.getKey())) {
            tableChangedHandler.handleCreatedOrAltered(databaseName, schemaName, event);
        } else if (Type.DELETED == event.getType() && TableMetaDataNodePath.isTablePath(event.getKey())) {
            tableChangedHandler.handleDropped(databaseName, schemaName, event);
        }
    }
    
    private boolean isViewMetaDataChanged(final String key) {
        return ViewMetaDataNodePath.isViewActiveVersionPath(key) || ViewMetaDataNodePath.isViewPath(key);
    }
    
    private void handleViewChanged(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType()) && ViewMetaDataNodePath.isViewActiveVersionPath(event.getKey())) {
            viewChangedHandler.handleCreatedOrAltered(databaseName, schemaName, event);
        } else if (Type.DELETED == event.getType() && ViewMetaDataNodePath.isViewPath(event.getKey())) {
            viewChangedHandler.handleDropped(databaseName, schemaName, event);
        }
    }
    
    private void handleDataSourceChanged(final String databaseName, final DataChangedEvent event) {
        Optional<String> storageUnitName = DataSourceMetaDataNodePath.findStorageUnitNameByActiveVersionPath(event.getKey());
        boolean isActiveVersion = true;
        if (!storageUnitName.isPresent()) {
            storageUnitName = DataSourceMetaDataNodePath.findStorageUnitNameByStorageUnitPath(event.getKey());
            isActiveVersion = false;
        }
        if (storageUnitName.isPresent()) {
            handleStorageUnitChanged(databaseName, event, storageUnitName.get(), isActiveVersion);
            return;
        }
        Optional<String> storageNodeName = DataSourceMetaDataNodePath.findStorageNodeNameByActiveVersionPath(event.getKey());
        isActiveVersion = true;
        if (!storageNodeName.isPresent()) {
            storageNodeName = DataSourceMetaDataNodePath.findStorageNodeNameByStorageNodePath(event.getKey());
            isActiveVersion = false;
        }
        if (storageNodeName.isPresent()) {
            handleStorageNodeChanged(databaseName, event, storageNodeName.get(), isActiveVersion);
        }
    }
    
    private void handleStorageUnitChanged(final String databaseName, final DataChangedEvent event, final String storageUnitName, final boolean isActiveVersion) {
        if (isActiveVersion) {
            if (Type.ADDED == event.getType()) {
                storageUnitChangedHandler.handleRegistered(databaseName, storageUnitName, event);
            } else if (Type.UPDATED == event.getType()) {
                storageUnitChangedHandler.handleAltered(databaseName, storageUnitName, event);
            }
            return;
        }
        if (Type.DELETED == event.getType()) {
            storageUnitChangedHandler.handleUnregistered(databaseName, storageUnitName);
        }
    }
    
    private void handleStorageNodeChanged(final String databaseName, final DataChangedEvent event, final String storageNodeName, final boolean isActiveVersion) {
        if (isActiveVersion) {
            if (Type.ADDED == event.getType()) {
                storageNodeChangedHandler.handleRegistered(databaseName, storageNodeName, event);
            } else if (Type.UPDATED == event.getType()) {
                storageNodeChangedHandler.handleAltered(databaseName, storageNodeName, event);
            }
            return;
        }
        if (Type.DELETED == event.getType()) {
            storageNodeChangedHandler.handleUnregistered(databaseName, storageNodeName);
        }
    }
}
