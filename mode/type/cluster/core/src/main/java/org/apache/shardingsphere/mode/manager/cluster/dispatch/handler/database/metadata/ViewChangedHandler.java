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

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.DatabaseLeafValueChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.statistics.StatisticsRefreshEngine;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.schema.SchemaMetaDataNodePath;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.schema.ViewMetaDataNodePath;

/**
 * View changed handler.
 */
public final class ViewChangedHandler implements DatabaseLeafValueChangedHandler {
    
    private final ContextManager contextManager;
    
    private final StatisticsRefreshEngine statisticsRefreshEngine;
    
    public ViewChangedHandler(final ContextManager contextManager) {
        this.contextManager = contextManager;
        statisticsRefreshEngine = new StatisticsRefreshEngine(contextManager);
    }
    
    @Override
    public NodePath getSubscribedNodePath(final String databaseName) {
        return new ViewMetaDataNodePath(databaseName, NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER);
    }
    
    @Override
    public void handle(final String databaseName, final DataChangedEvent event) {
        String schemaName = NodePathSearcher.get(event.getKey(), SchemaMetaDataNodePath.createSchemaSearchCriteria(databaseName, true));
        String viewName = NodePathSearcher.get(event.getKey(), ViewMetaDataNodePath.createViewSearchCriteria(databaseName, schemaName));
        switch (event.getType()) {
            case ADDED:
            case UPDATED:
                handleCreatedOrAltered(databaseName, schemaName, viewName);
                break;
            case DELETED:
                handleDropped(databaseName, schemaName, viewName);
                break;
            default:
                break;
        }
    }
    
    private void handleCreatedOrAltered(final String databaseName, final String schemaName, final String viewName) {
        ShardingSphereView view = contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseMetaDataFacade().getView().load(databaseName, schemaName, viewName);
        contextManager.getMetaDataContextManager().getDatabaseMetaDataManager().alterView(databaseName, schemaName, view);
        statisticsRefreshEngine.asyncRefresh();
    }
    
    private void handleDropped(final String databaseName, final String schemaName, final String viewName) {
        contextManager.getMetaDataContextManager().getDatabaseMetaDataManager().dropView(databaseName, schemaName, viewName);
        statisticsRefreshEngine.asyncRefresh();
    }
}
