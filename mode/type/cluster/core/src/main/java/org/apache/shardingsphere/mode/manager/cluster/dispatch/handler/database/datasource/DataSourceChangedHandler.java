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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.datasource;

import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.datasource.type.StorageNodeChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.datasource.type.StorageUnitChangedHandler;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.metadata.storage.StorageNodeNodePath;
import org.apache.shardingsphere.mode.node.path.type.metadata.storage.StorageUnitNodePath;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePathParser;

import java.util.Optional;

/**
 * Data source changed handler.
 */
public final class DataSourceChangedHandler {
    
    private final StorageUnitChangedHandler storageUnitChangedHandler;
    
    private final StorageNodeChangedHandler storageNodeChangedHandler;
    
    public DataSourceChangedHandler(final ContextManager contextManager) {
        storageUnitChangedHandler = new StorageUnitChangedHandler(contextManager);
        storageNodeChangedHandler = new StorageNodeChangedHandler(contextManager);
    }
    
    /**
     * Handle data source changed.
     *
     * @param databaseName database name
     * @param event data changed event
     */
    public void handle(final String databaseName, final DataChangedEvent event) {
        if (!NodePathSearcher.isMatchedPath(event.getKey(), StorageUnitNodePath.createDataSourceSearchCriteria())) {
            return;
        }
        Optional<String> storageUnitName = new VersionNodePathParser(new StorageUnitNodePath()).findIdentifierByActiveVersionPath(event.getKey(), 2);
        boolean isActiveVersion = true;
        if (!storageUnitName.isPresent()) {
            storageUnitName = NodePathSearcher.find(event.getKey(), StorageUnitNodePath.createStorageUnitSearchCriteria());
            isActiveVersion = false;
        }
        if (storageUnitName.isPresent()) {
            handleStorageUnitChanged(databaseName, event, storageUnitName.get(), isActiveVersion);
            return;
        }
        Optional<String> storageNodeName = new VersionNodePathParser(new StorageNodeNodePath()).findIdentifierByActiveVersionPath(event.getKey(), 2);
        isActiveVersion = true;
        if (!storageNodeName.isPresent()) {
            storageNodeName = NodePathSearcher.find(event.getKey(), StorageNodeNodePath.createStorageNodeSearchCriteria());
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
