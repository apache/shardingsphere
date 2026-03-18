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
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.DatabaseNodeValueChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.statistics.StatisticsRefreshEngine;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.schema.SchemaMetaDataNodePath;

/**
 * Schema changed handler.
 */
public final class SchemaChangedHandler implements DatabaseNodeValueChangedHandler {
    
    private final ContextManager contextManager;
    
    private final StatisticsRefreshEngine statisticsRefreshEngine;
    
    public SchemaChangedHandler(final ContextManager contextManager) {
        this.contextManager = contextManager;
        statisticsRefreshEngine = new StatisticsRefreshEngine(contextManager);
    }
    
    @Override
    public NodePath getSubscribedNodePath(final String databaseName) {
        return new SchemaMetaDataNodePath(databaseName, NodePathPattern.IDENTIFIER);
    }
    
    @Override
    public void handle(final String databaseName, final DataChangedEvent event) {
        String schemaName = NodePathSearcher.get(event.getKey(), SchemaMetaDataNodePath.createSchemaSearchCriteria(databaseName, false));
        switch (event.getType()) {
            case ADDED:
            case UPDATED:
                handleCreated(databaseName, schemaName);
                break;
            case DELETED:
                handleDropped(databaseName, schemaName);
                break;
            default:
                break;
        }
    }
    
    private void handleCreated(final String databaseName, final String schemaName) {
        contextManager.getMetaDataContextManager().getDatabaseMetaDataManager().addSchema(databaseName, schemaName);
        statisticsRefreshEngine.asyncRefresh();
    }
    
    private void handleDropped(final String databaseName, final String schemaName) {
        contextManager.getMetaDataContextManager().getDatabaseMetaDataManager().dropSchema(databaseName, schemaName);
        statisticsRefreshEngine.asyncRefresh();
    }
}
