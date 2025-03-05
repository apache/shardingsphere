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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.DatabaseChangedHandler;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.metadata.storage.StorageNodeNodePath;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePathParser;

import java.util.Optional;

/**
 * Storage node changed handler.
 */
@RequiredArgsConstructor
public final class StorageNodeChangedHandler implements DatabaseChangedHandler {
    
    private final ContextManager contextManager;
    
    @Override
    public boolean isSubscribed(final String databaseName, final DataChangedEvent event) {
        return new VersionNodePathParser(new StorageNodeNodePath(databaseName, NodePathPattern.IDENTIFIER)).isActiveVersionPath(event.getKey());
    }
    
    @Override
    public void handle(final String databaseName, final DataChangedEvent event) {
        Optional<String> storageNodeName = NodePathSearcher.find(event.getKey(), StorageNodeNodePath.createStorageNodeSearchCriteria());
        if (!storageNodeName.isPresent()) {
            return;
        }
        switch (event.getType()) {
            case ADDED:
                handleRegistered(databaseName, storageNodeName.get(), event);
                break;
            case UPDATED:
                handleAltered(databaseName, storageNodeName.get(), event);
                break;
            case DELETED:
                handleUnregistered(databaseName, storageNodeName.get());
                break;
            default:
                break;
        }
    }
    
    private void handleRegistered(final String databaseName, final String storageNodeName, final DataChangedEvent event) {
        // TODO
    }
    
    private void handleAltered(final String databaseName, final String storageNodeName, final DataChangedEvent event) {
        // TODO
    }
    
    private void handleUnregistered(final String databaseName, final String storageNodeName) {
        // TODO
    }
}
