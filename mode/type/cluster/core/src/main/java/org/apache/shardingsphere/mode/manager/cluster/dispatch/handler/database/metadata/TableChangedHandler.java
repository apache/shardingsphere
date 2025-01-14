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

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.node.path.metadata.TableMetaDataNodePath;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.checker.ActiveVersionChecker;
import org.apache.shardingsphere.mode.metadata.refresher.ShardingSphereStatisticsRefreshEngine;

/**
 * Table changed handler.
 */
public final class TableChangedHandler {
    
    private final ContextManager contextManager;
    
    private final ShardingSphereStatisticsRefreshEngine statisticsRefreshEngine;
    
    public TableChangedHandler(final ContextManager contextManager) {
        this.contextManager = contextManager;
        statisticsRefreshEngine = new ShardingSphereStatisticsRefreshEngine(contextManager);
    }
    
    /**
     * Handle table created or altered.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param event data changed event
     */
    public void handleCreatedOrAltered(final String databaseName, final String schemaName, final DataChangedEvent event) {
        String tableName = TableMetaDataNodePath.getTableNameByActiveVersionPath(event.getKey()).orElseThrow(() -> new IllegalStateException("Table name not found."));
        ActiveVersionChecker.checkActiveVersion(contextManager, event);
        ShardingSphereTable table = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataFacade().getTable().load(databaseName, schemaName, tableName);
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(databaseName, schemaName, table, null);
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
        String tableName = TableMetaDataNodePath.findTableName(event.getKey()).orElseThrow(() -> new IllegalStateException("Table name not found."));
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(databaseName, schemaName, tableName, null);
        statisticsRefreshEngine.asyncRefresh();
    }
}
