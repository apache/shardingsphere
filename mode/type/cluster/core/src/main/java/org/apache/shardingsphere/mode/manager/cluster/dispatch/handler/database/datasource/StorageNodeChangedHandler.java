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
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.DatabaseLeafValueChangedHandler;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.datasource.StorageNodeNodePath;

/**
 * Storage node changed handler.
 */
@RequiredArgsConstructor
public final class StorageNodeChangedHandler implements DatabaseLeafValueChangedHandler {
    
    private final ContextManager contextManager;
    
    @Override
    public NodePath getSubscribedNodePath(final String databaseName) {
        return new StorageNodeNodePath(databaseName, NodePathPattern.IDENTIFIER);
    }
    
    @Override
    public void handle(final String databaseName, final DataChangedEvent event) {
        String storageNodeName = NodePathSearcher.get(event.getKey(), StorageNodeNodePath.createStorageNodeSearchCriteria(databaseName));
        switch (event.getType()) {
            case ADDED:
                handleRegistered(databaseName, storageNodeName);
                break;
            case UPDATED:
                handleAltered(databaseName, storageNodeName);
                break;
            case DELETED:
                handleUnregistered(databaseName, storageNodeName);
                break;
            default:
                break;
        }
    }
    
    private void handleRegistered(final String databaseName, final String storageNodeName) {
        // TODO
    }
    
    private void handleAltered(final String databaseName, final String storageNodeName) {
        // TODO
    }
    
    private void handleUnregistered(final String databaseName, final String storageNodeName) {
        // TODO
    }
}
