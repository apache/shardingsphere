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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata.type;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.checker.ActiveVersionChecker;
import org.apache.shardingsphere.mode.metadata.refresher.statistics.StatisticsRefreshEngine;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.metadata.database.TableMetadataNodePath;

/**
 * Table changed handler.
 */
public final class TableChangedHandler {
    
    private final ContextManager contextManager;
    
    private final StatisticsRefreshEngine statisticsRefreshEngine;
    
    public TableChangedHandler(final ContextManager contextManager) {
        this.contextManager = contextManager;
        statisticsRefreshEngine = new StatisticsRefreshEngine(contextManager);
    }
    
    /**
     * Handle table created or altered.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param event data changed event
     */
    public void handleCreatedOrAltered(final String databaseName, final String schemaName, final DataChangedEvent event) {
        String tableName = NodePathSearcher.getVersion(new TableMetadataNodePath())
                .findIdentifierByActiveVersionPath(event.getKey(), 3).orElseThrow(() -> new IllegalStateException("Table name not found."));
        ActiveVersionChecker.checkActiveVersion(contextManager, event);
        ShardingSphereTable table = contextManager.getPersistServiceFacade().getMetaDataPersistFacade().getDatabaseMetaDataFacade().getTable().load(databaseName, schemaName, tableName);
        contextManager.getMetaDataContextManager().getDatabaseMetaDataManager().alterTable(databaseName, schemaName, table);
        statisticsRefreshEngine.asyncRefresh();
    }
    
    /**
     * Handle table dropped.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param event data changed event
     */
    public void handleDropped(final String databaseName, final String schemaName, final DataChangedEvent event) {
        String tableName = NodePathSearcher.find(event.getKey(), TableMetadataNodePath.createTableSearchCriteria()).orElseThrow(() -> new IllegalStateException("Table name not found."));
        contextManager.getMetaDataContextManager().getDatabaseMetaDataManager().dropTable(databaseName, schemaName, tableName);
        statisticsRefreshEngine.asyncRefresh();
    }
}
