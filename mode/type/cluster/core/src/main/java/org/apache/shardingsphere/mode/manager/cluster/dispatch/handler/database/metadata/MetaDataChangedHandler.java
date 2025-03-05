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

import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.DatabaseChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata.type.SchemaChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata.type.TableChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata.type.ViewChangedHandler;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.metadata.database.TableMetadataNodePath;
import org.apache.shardingsphere.mode.node.path.type.metadata.database.ViewMetadataNodePath;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePathParser;

import java.util.Optional;

/**
 * Meta data changed handler.
 */
public final class MetaDataChangedHandler implements DatabaseChangedHandler {
    
    private final SchemaChangedHandler schemaChangedHandler;
    
    private final TableChangedHandler tableChangedHandler;
    
    private final ViewChangedHandler viewChangedHandler;
    
    public MetaDataChangedHandler(final ContextManager contextManager) {
        schemaChangedHandler = new SchemaChangedHandler(contextManager);
        tableChangedHandler = new TableChangedHandler(contextManager);
        viewChangedHandler = new ViewChangedHandler(contextManager);
    }
    
    @Override
    public boolean isSubscribed(final String databaseName, final DataChangedEvent event) {
        return NodePathSearcher.isMatchedPath(event.getKey(), TableMetadataNodePath.createSchemaSearchCriteria(databaseName, true));
    }
    
    @Override
    public void handle(final String databaseName, final DataChangedEvent event) {
        String eventKey = event.getKey();
        Optional<String> schemaName = NodePathSearcher.find(eventKey, TableMetadataNodePath.createSchemaSearchCriteria(databaseName, false));
        if (schemaName.isPresent()) {
            handleSchemaChanged(databaseName, schemaName.get(), event);
            return;
        }
        schemaName = NodePathSearcher.find(eventKey, TableMetadataNodePath.createSchemaSearchCriteria(databaseName, true));
        if (schemaName.isPresent() && isTableMetaDataChanged(eventKey)) {
            handleTableChanged(databaseName, schemaName.get(), event);
            return;
        }
        if (schemaName.isPresent() && isViewMetaDataChanged(eventKey)) {
            handleViewChanged(databaseName, schemaName.get(), event);
        }
    }
    
    private void handleSchemaChanged(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            schemaChangedHandler.handleCreated(databaseName, schemaName);
        } else if (Type.DELETED == event.getType()) {
            schemaChangedHandler.handleDropped(databaseName, schemaName);
        }
    }
    
    private boolean isTableMetaDataChanged(final String key) {
        return NodePathSearcher.isMatchedPath(key, TableMetadataNodePath.createTableSearchCriteria()) || new VersionNodePathParser(new TableMetadataNodePath()).isActiveVersionPath(key);
    }
    
    private void handleTableChanged(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType()) && new VersionNodePathParser(new TableMetadataNodePath()).isActiveVersionPath(event.getKey())) {
            tableChangedHandler.handleCreatedOrAltered(databaseName, schemaName, event);
        } else if (Type.DELETED == event.getType() && NodePathSearcher.isMatchedPath(event.getKey(), TableMetadataNodePath.createTableSearchCriteria())) {
            tableChangedHandler.handleDropped(databaseName, schemaName, event);
        }
    }
    
    private boolean isViewMetaDataChanged(final String key) {
        return new VersionNodePathParser(new ViewMetadataNodePath()).isActiveVersionPath(key) || NodePathSearcher.isMatchedPath(key, ViewMetadataNodePath.createViewSearchCriteria());
    }
    
    private void handleViewChanged(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType())
                && new VersionNodePathParser(new ViewMetadataNodePath()).isActiveVersionPath(event.getKey())) {
            viewChangedHandler.handleCreatedOrAltered(databaseName, schemaName, event);
        } else if (Type.DELETED == event.getType() && NodePathSearcher.isMatchedPath(event.getKey(), ViewMetadataNodePath.createViewSearchCriteria())) {
            viewChangedHandler.handleDropped(databaseName, schemaName, event);
        }
    }
}
