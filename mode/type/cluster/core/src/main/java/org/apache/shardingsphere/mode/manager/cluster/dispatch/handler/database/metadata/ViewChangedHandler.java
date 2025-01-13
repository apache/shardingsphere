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
import org.apache.shardingsphere.mode.node.path.metadata.ViewMetaDataNodePath;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.checker.ActiveVersionChecker;
import org.apache.shardingsphere.mode.metadata.refresher.ShardingSphereStatisticsRefreshEngine;

/**
 * View changed handler.
 */
public final class ViewChangedHandler {
    
    private final ContextManager contextManager;
    
    private final ShardingSphereStatisticsRefreshEngine statisticsRefreshEngine;
    
    public ViewChangedHandler(final ContextManager contextManager) {
        this.contextManager = contextManager;
        statisticsRefreshEngine = new ShardingSphereStatisticsRefreshEngine(contextManager);
    }
    
    /**
     * Handle view created or altered.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param event data changed event
     */
    public void handleCreatedOrAltered(final String databaseName, final String schemaName, final DataChangedEvent event) {
        String viewName = ViewMetaDataNodePath.getViewNameByActiveVersionPath(event.getKey()).orElseThrow(() -> new IllegalStateException("View name not found."));
        ActiveVersionChecker.checkActiveVersion(contextManager, event);
        ShardingSphereView view = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataFacade().getView().load(databaseName, schemaName, viewName);
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(databaseName, schemaName, null, view);
        statisticsRefreshEngine.asyncRefresh();
    }
    
    /**
     * Handle view dropped.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param event data changed event
     */
    public void handleDropped(final String databaseName, final String schemaName, final DataChangedEvent event) {
        String viewName = ViewMetaDataNodePath.findViewName(event.getKey()).orElseThrow(() -> new IllegalStateException("View name not found."));
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(databaseName, schemaName, null, viewName);
        statisticsRefreshEngine.asyncRefresh();
    }
}
