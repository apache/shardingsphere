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

package org.apache.shardingsphere.mode.manager.cluster.event.dispatch.handler.database;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.metadata.DataSourceMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.metadata.TableMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.metadata.ViewMetaDataNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.refresher.ShardingSphereStatisticsRefreshEngine;

import java.util.Collections;
import java.util.Optional;

/**
 * Meta data changed handler.
 */
@RequiredArgsConstructor
public final class MetaDataChangedHandler {
    
    private final ContextManager contextManager;
    
    /**
     * Handle meta data changed.
     *
     * @param databaseName database name
     * @param event data changed event
     * @return handle completed or not
     */
    public boolean handle(final String databaseName, final DataChangedEvent event) {
        String key = event.getKey();
        Optional<String> schemaName = DatabaseMetaDataNode.getSchemaName(key);
        if (schemaName.isPresent()) {
            handleSchemaChanged(databaseName, schemaName.get(), event.getType());
            return true;
        }
        schemaName = DatabaseMetaDataNode.getSchemaNameByTableNode(key);
        if (schemaName.isPresent() && isTableMetaDataChanged(event.getKey())) {
            handleTableChanged(databaseName, schemaName.get(), event);
            return true;
        }
        if (schemaName.isPresent() && isViewMetaDataChanged(event.getKey())) {
            handleViewChanged(databaseName, schemaName.get(), event);
            return true;
        }
        if (DataSourceMetaDataNode.isDataSourcesNode(key)) {
            handleDataSourceChanged(databaseName, event);
            return true;
        }
        return false;
    }
    
    private void handleSchemaChanged(final String databaseName, final String schemaName, final Type type) {
        switch (type) {
            case ADDED:
            case UPDATED:
                handleSchemaCreated(databaseName, schemaName);
                return;
            case DELETED:
                handleSchemaDropped(databaseName, schemaName);
                return;
            default:
        }
    }
    
    private void handleSchemaCreated(final String databaseName, final String schemaName) {
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().addSchema(databaseName, schemaName);
        refreshStatisticsData();
    }
    
    private void handleSchemaDropped(final String databaseName, final String schemaName) {
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().dropSchema(databaseName, schemaName);
        refreshStatisticsData();
    }
    
    private boolean isTableMetaDataChanged(final String key) {
        return TableMetaDataNode.isTableActiveVersionNode(key) || TableMetaDataNode.isTableNode(key);
    }
    
    private void handleTableChanged(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType()) && TableMetaDataNode.isTableActiveVersionNode(event.getKey())) {
            handleTableCreatedOrAltered(databaseName, schemaName, event);
        } else if (Type.DELETED == event.getType() && TableMetaDataNode.isTableNode(event.getKey())) {
            handleTableDropped(databaseName, schemaName, event);
        }
    }
    
    private void handleTableCreatedOrAltered(final String databaseName, final String schemaName, final DataChangedEvent event) {
        String tableName = TableMetaDataNode.getTableNameByActiveVersionNode(event.getKey()).orElseThrow(() -> new IllegalStateException("Table name not found."));
        Preconditions.checkArgument(event.getValue().equals(
                contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath(event.getKey())),
                "Invalid active version: %s of key: %s", event.getValue(), event.getKey());
        ShardingSphereTable table = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataFacade().getTable().load(databaseName, schemaName, tableName);
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(databaseName, schemaName, table, null);
        refreshStatisticsData();
    }
    
    private void handleTableDropped(final String databaseName, final String schemaName, final DataChangedEvent event) {
        String tableName = TableMetaDataNode.getTableName(event.getKey()).orElseThrow(() -> new IllegalStateException("Table name not found."));
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(databaseName, schemaName, tableName, null);
        refreshStatisticsData();
    }
    
    private boolean isViewMetaDataChanged(final String key) {
        return ViewMetaDataNode.isViewActiveVersionNode(key) || ViewMetaDataNode.isViewNode(key);
    }
    
    private void handleViewChanged(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType()) && ViewMetaDataNode.isViewActiveVersionNode(event.getKey())) {
            handleViewCreatedOrAltered(databaseName, schemaName, event);
        } else if (Type.DELETED == event.getType() && ViewMetaDataNode.isViewNode(event.getKey())) {
            handleViewDropped(databaseName, schemaName, event);
        }
    }
    
    private void handleViewCreatedOrAltered(final String databaseName, final String schemaName, final DataChangedEvent event) {
        String viewName = ViewMetaDataNode.getViewNameByActiveVersionNode(event.getKey()).orElseThrow(() -> new IllegalStateException("View name not found."));
        Preconditions.checkArgument(event.getValue().equals(
                contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath(event.getKey())),
                "Invalid active version: %s of key: %s", event.getValue(), event.getKey());
        ShardingSphereView view = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataFacade().getView().load(databaseName, schemaName, viewName);
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(databaseName, schemaName, null, view);
        refreshStatisticsData();
    }
    
    private void handleViewDropped(final String databaseName, final String schemaName, final DataChangedEvent event) {
        String viewName = ViewMetaDataNode.getViewName(event.getKey()).orElseThrow(() -> new IllegalStateException("View name not found."));
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(databaseName, schemaName, null, viewName);
        refreshStatisticsData();
    }
    
    private void handleDataSourceChanged(final String databaseName, final DataChangedEvent event) {
        if (DataSourceMetaDataNode.isDataSourceUnitActiveVersionNode(event.getKey()) || DataSourceMetaDataNode.isDataSourceUnitNode(event.getKey())) {
            handleStorageUnitChanged(databaseName, event);
        } else if (DataSourceMetaDataNode.isDataSourceNodeActiveVersionNode(event.getKey()) || DataSourceMetaDataNode.isDataSourceNodeNode(event.getKey())) {
            handleStorageNodeChanged(databaseName, event);
        }
    }
    
    private void handleStorageUnitChanged(final String databaseName, final DataChangedEvent event) {
        Optional<String> dataSourceUnitName = DataSourceMetaDataNode.getDataSourceNameByDataSourceUnitActiveVersionNode(event.getKey());
        if (dataSourceUnitName.isPresent()) {
            if (Type.ADDED == event.getType()) {
                handleStorageUnitRegistered(databaseName, dataSourceUnitName.get(), event);
            } else if (Type.UPDATED == event.getType()) {
                handleStorageUnitAltered(databaseName, dataSourceUnitName.get(), event);
            }
            return;
        }
        dataSourceUnitName = DataSourceMetaDataNode.getDataSourceNameByDataSourceUnitNode(event.getKey());
        if (Type.DELETED == event.getType() && dataSourceUnitName.isPresent()) {
            handleStorageUnitUnregistered(databaseName, dataSourceUnitName.get());
        }
    }
    
    private void handleStorageUnitRegistered(final String databaseName, final String dataSourceUnitName, final DataChangedEvent event) {
        Preconditions.checkArgument(event.getValue().equals(
                contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath(event.getKey())),
                "Invalid active version: %s of key: %s", event.getValue(), event.getKey());
        DataSourcePoolProperties dataSourcePoolProps = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDataSourceUnitService().load(databaseName, dataSourceUnitName);
        contextManager.getMetaDataContextManager().getStorageUnitManager().registerStorageUnit(databaseName, Collections.singletonMap(dataSourceUnitName, dataSourcePoolProps));
    }
    
    private void handleStorageUnitAltered(final String databaseName, final String dataSourceUnitName, final DataChangedEvent event) {
        Preconditions.checkArgument(event.getValue().equals(
                contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath(event.getKey())),
                "Invalid active version: %s of key: %s", event.getValue(), event.getKey());
        DataSourcePoolProperties dataSourcePoolProps = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDataSourceUnitService().load(databaseName, dataSourceUnitName);
        contextManager.getMetaDataContextManager().getStorageUnitManager().alterStorageUnit(databaseName, Collections.singletonMap(dataSourceUnitName, dataSourcePoolProps));
    }
    
    private void handleStorageUnitUnregistered(final String databaseName, final String dataSourceUnitName) {
        Preconditions.checkState(contextManager.getMetaDataContexts().getMetaData().containsDatabase(databaseName), "No database '%s' exists.", databaseName);
        contextManager.getMetaDataContextManager().getStorageUnitManager().unregisterStorageUnit(databaseName, dataSourceUnitName);
    }
    
    private void handleStorageNodeChanged(final String databaseName, final DataChangedEvent event) {
        Optional<String> dataSourceNodeName = DataSourceMetaDataNode.getDataSourceNameByDataSourceNodeActiveVersionNode(event.getKey());
        if (dataSourceNodeName.isPresent()) {
            if (Type.ADDED == event.getType()) {
                handleStorageNodeRegistered(databaseName, dataSourceNodeName.get(), event);
            } else if (Type.UPDATED == event.getType()) {
                handleStorageNodeAltered(databaseName, dataSourceNodeName.get(), event);
            }
            return;
        }
        dataSourceNodeName = DataSourceMetaDataNode.getDataSourceNameByDataSourceNodeNode(event.getKey());
        if (Type.DELETED == event.getType() && dataSourceNodeName.isPresent()) {
            handleStorageNodeUnregistered(databaseName, dataSourceNodeName.get());
        }
    }
    
    private void handleStorageNodeRegistered(final String databaseName, final String dataSourceNodeName, final DataChangedEvent event) {
        // TODO
    }
    
    private void handleStorageNodeAltered(final String databaseName, final String dataSourceNodeName, final DataChangedEvent event) {
        // TODO
    }
    
    private void handleStorageNodeUnregistered(final String databaseName, final String dataSourceNodeName) {
        // TODO
    }
    
    private void refreshStatisticsData() {
        if (InstanceType.PROXY == contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()) {
            new ShardingSphereStatisticsRefreshEngine(contextManager).asyncRefresh();
        }
    }
}
