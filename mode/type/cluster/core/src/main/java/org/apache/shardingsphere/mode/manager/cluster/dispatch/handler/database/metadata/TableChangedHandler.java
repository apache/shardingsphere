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
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.DatabaseChangedHandler;
import org.apache.shardingsphere.mode.metadata.manager.ActiveVersionChecker;
import org.apache.shardingsphere.mode.metadata.refresher.statistics.StatisticsRefreshEngine;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.metadata.database.TableMetadataNodePath;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePathParser;

import java.util.Optional;

/**
 * Table changed handler.
 */
public final class TableChangedHandler implements DatabaseChangedHandler {
    
    private final ContextManager contextManager;
    
    private final ActiveVersionChecker activeVersionChecker;
    
    private final StatisticsRefreshEngine statisticsRefreshEngine;
    
    public TableChangedHandler(final ContextManager contextManager) {
        this.contextManager = contextManager;
        activeVersionChecker = new ActiveVersionChecker(contextManager.getPersistServiceFacade().getRepository());
        statisticsRefreshEngine = new StatisticsRefreshEngine(contextManager);
    }
    
    @Override
    public boolean isSubscribed(final String databaseName, final DataChangedEvent event) {
        return NodePathSearcher.isMatchedPath(event.getKey(), TableMetadataNodePath.createTableSearchCriteria())
                || new VersionNodePathParser(new TableMetadataNodePath()).isActiveVersionPath(event.getKey());
    }
    
    @Override
    public void handle(final String databaseName, final DataChangedEvent event) {
        String eventKey = event.getKey();
        Optional<String> schemaName = NodePathSearcher.find(eventKey, TableMetadataNodePath.createSchemaSearchCriteria(databaseName, true));
        if (!schemaName.isPresent()) {
            return;
        }
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType()) && new VersionNodePathParser(new TableMetadataNodePath()).isActiveVersionPath(event.getKey())) {
            handleCreatedOrAltered(databaseName, schemaName.get(), event);
        } else if (Type.DELETED == event.getType() && NodePathSearcher.isMatchedPath(event.getKey(), TableMetadataNodePath.createTableSearchCriteria())) {
            handleDropped(databaseName, schemaName.get(), event);
        }
    }
    
    private void handleCreatedOrAltered(final String databaseName, final String schemaName, final DataChangedEvent event) {
        String tableName = new VersionNodePathParser(new TableMetadataNodePath())
                .findIdentifierByActiveVersionPath(event.getKey(), 3).orElseThrow(() -> new IllegalStateException("Table name not found."));
        if (!activeVersionChecker.checkSame(event)) {
            return;
        }
        ShardingSphereTable table = contextManager.getPersistServiceFacade().getMetaDataPersistFacade().getDatabaseMetaDataFacade().getTable().load(databaseName, schemaName, tableName);
        contextManager.getMetaDataContextManager().getDatabaseMetaDataManager().alterTable(databaseName, schemaName, table);
        statisticsRefreshEngine.asyncRefresh();
    }
    
    private void handleDropped(final String databaseName, final String schemaName, final DataChangedEvent event) {
        String tableName = NodePathSearcher.find(event.getKey(), TableMetadataNodePath.createTableSearchCriteria()).orElseThrow(() -> new IllegalStateException("Table name not found."));
        contextManager.getMetaDataContextManager().getDatabaseMetaDataManager().dropTable(databaseName, schemaName, tableName);
        statisticsRefreshEngine.asyncRefresh();
    }
}
