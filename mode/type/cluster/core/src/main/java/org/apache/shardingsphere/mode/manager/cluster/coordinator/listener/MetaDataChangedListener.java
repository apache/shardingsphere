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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.listener;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.metadata.DataSourceMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.metadata.TableMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.metadata.ViewMetaDataNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.datasource.nodes.AlterStorageNodeEvent;
import org.apache.shardingsphere.mode.event.datasource.nodes.RegisterStorageNodeEvent;
import org.apache.shardingsphere.mode.event.datasource.nodes.UnregisterStorageNodeEvent;
import org.apache.shardingsphere.mode.event.datasource.unit.AlterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.datasource.unit.RegisterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.datasource.unit.UnregisterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.schema.table.CreateOrAlterTableEvent;
import org.apache.shardingsphere.mode.event.schema.table.DropTableEvent;
import org.apache.shardingsphere.mode.event.schema.view.CreateOrAlterViewEvent;
import org.apache.shardingsphere.mode.event.schema.view.DropViewEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.metadata.builder.RuleConfigurationEventBuilder;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;

import java.util.Optional;

/**
 * Meta data changed listener.
 */
@RequiredArgsConstructor
public final class MetaDataChangedListener implements DataChangedEventListener {
    
    private final EventBusContext eventBusContext;
    
    private final RuleConfigurationEventBuilder ruleConfigurationEventBuilder = new RuleConfigurationEventBuilder();
    
    @Override
    public void onChange(final DataChangedEvent event) {
        createGovernanceEvent(event).ifPresent(eventBusContext::post);
    }
    
    private Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        String key = event.getKey();
        Optional<String> databaseName = DatabaseMetaDataNode.getDatabaseNameBySchemaNode(key);
        Optional<String> schemaName = DatabaseMetaDataNode.getSchemaName(key);
        if (databaseName.isPresent() && schemaName.isPresent()) {
            return createSchemaChangedEvent(databaseName.get(), schemaName.get(), event);
        }
        schemaName = DatabaseMetaDataNode.getSchemaNameByTableNode(key);
        if (databaseName.isPresent() && schemaName.isPresent() && tableMetaDataChanged(event.getKey())) {
            return createTableChangedEvent(databaseName.get(), schemaName.get(), event);
        }
        if (databaseName.isPresent() && schemaName.isPresent() && viewMetaDataChanged(event.getKey())) {
            return createViewChangedEvent(databaseName.get(), schemaName.get(), event);
        }
        if (!databaseName.isPresent()) {
            return Optional.empty();
        }
        if (DataSourceMetaDataNode.isDataSourcesNode(key)) {
            return createDataSourceEvent(databaseName.get(), event);
        }
        return ruleConfigurationEventBuilder.build(databaseName.get(), event);
    }
    
    private Optional<GovernanceEvent> createSchemaChangedEvent(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new SchemaAddedEvent(databaseName, schemaName));
        }
        if (Type.DELETED == event.getType()) {
            return Optional.of(new SchemaDeletedEvent(databaseName, schemaName));
        }
        return Optional.empty();
    }
    
    private boolean tableMetaDataChanged(final String key) {
        return TableMetaDataNode.isTableActiveVersionNode(key) || TableMetaDataNode.isTableNode(key);
    }
    
    private boolean viewMetaDataChanged(final String key) {
        return ViewMetaDataNode.isViewActiveVersionNode(key) || ViewMetaDataNode.isViewNode(key);
    }
    
    private Optional<GovernanceEvent> createTableChangedEvent(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if (Type.DELETED == event.getType() && TableMetaDataNode.isTableNode(event.getKey())) {
            Optional<String> tableName = TableMetaDataNode.getTableName(event.getKey());
            Preconditions.checkState(tableName.isPresent(), "Not found table name.");
            return Optional.of(new DropTableEvent(databaseName, schemaName, tableName.get()));
        }
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType()) && TableMetaDataNode.isTableActiveVersionNode(event.getKey())) {
            Optional<String> tableName = TableMetaDataNode.getTableNameByActiveVersionNode(event.getKey());
            Preconditions.checkState(tableName.isPresent(), "Not found table name.");
            return Optional.of(new CreateOrAlterTableEvent(databaseName, schemaName, tableName.get(), event.getKey(), event.getValue()));
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createViewChangedEvent(final String databaseName, final String schemaName, final DataChangedEvent event) {
        if (Type.DELETED == event.getType() && ViewMetaDataNode.isViewNode(event.getKey())) {
            Optional<String> viewName = ViewMetaDataNode.getViewName(event.getKey());
            Preconditions.checkState(viewName.isPresent(), "Not found view name.");
            return Optional.of(new DropViewEvent(databaseName, schemaName, viewName.get(), event.getKey(), event.getValue()));
        }
        if ((Type.ADDED == event.getType() || Type.UPDATED == event.getType()) && ViewMetaDataNode.isViewActiveVersionNode(event.getKey())) {
            Optional<String> viewName = ViewMetaDataNode.getViewNameByActiveVersionNode(event.getKey());
            Preconditions.checkState(viewName.isPresent(), "Not found view name.");
            return Optional.of(new CreateOrAlterViewEvent(databaseName, schemaName, viewName.get(), event.getKey(), event.getValue()));
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createDataSourceEvent(final String databaseName, final DataChangedEvent event) {
        if (DataSourceMetaDataNode.isDataSourceUnitActiveVersionNode(event.getKey()) || DataSourceMetaDataNode.isDataSourceUnitNode(event.getKey())) {
            return createStorageUnitChangedEvent(databaseName, event);
        }
        if (DataSourceMetaDataNode.isDataSourceNodeActiveVersionNode(event.getKey()) || DataSourceMetaDataNode.isDataSourceNodeNode(event.getKey())) {
            return createStorageNodeChangedEvent(databaseName, event);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createStorageUnitChangedEvent(final String databaseName, final DataChangedEvent event) {
        Optional<String> dataSourceUnitName = DataSourceMetaDataNode.getDataSourceNameByDataSourceUnitActiveVersionNode(event.getKey());
        if (dataSourceUnitName.isPresent()) {
            if (Type.ADDED == event.getType()) {
                return Optional.of(new RegisterStorageUnitEvent(databaseName, dataSourceUnitName.get(), event.getKey(), event.getValue()));
            }
            if (Type.UPDATED == event.getType()) {
                return Optional.of(new AlterStorageUnitEvent(databaseName, dataSourceUnitName.get(), event.getKey(), event.getValue()));
            }
        }
        dataSourceUnitName = DataSourceMetaDataNode.getDataSourceNameByDataSourceUnitNode(event.getKey());
        return dataSourceUnitName.map(optional -> new UnregisterStorageUnitEvent(databaseName, optional));
    }
    
    private Optional<GovernanceEvent> createStorageNodeChangedEvent(final String databaseName, final DataChangedEvent event) {
        Optional<String> dataSourceNodeName = DataSourceMetaDataNode.getDataSourceNameByDataSourceNodeActiveVersionNode(event.getKey());
        if (dataSourceNodeName.isPresent()) {
            if (Type.ADDED == event.getType()) {
                return Optional.of(new RegisterStorageNodeEvent(databaseName, dataSourceNodeName.get(), event.getKey(), event.getValue()));
            }
            if (Type.UPDATED == event.getType()) {
                return Optional.of(new AlterStorageNodeEvent(databaseName, dataSourceNodeName.get(), event.getKey(), event.getValue()));
            }
        }
        dataSourceNodeName = DataSourceMetaDataNode.getDataSourceNameByDataSourceNodeNode(event.getKey());
        return dataSourceNodeName.map(optional -> new UnregisterStorageNodeEvent(databaseName, optional));
    }
}
