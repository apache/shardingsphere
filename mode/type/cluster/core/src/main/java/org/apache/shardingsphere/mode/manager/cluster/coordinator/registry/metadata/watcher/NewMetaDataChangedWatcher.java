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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.watcher;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.NewDatabaseMetaDataNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.datasource.nodes.AlterStorageNodeEvent;
import org.apache.shardingsphere.mode.event.datasource.nodes.RegisterStorageNodeEvent;
import org.apache.shardingsphere.mode.event.datasource.nodes.UnregisterStorageNodeEvent;
import org.apache.shardingsphere.mode.event.datasource.unit.AlterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.datasource.unit.RegisterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.datasource.unit.UnregisterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.schema.table.AlterTableEvent;
import org.apache.shardingsphere.mode.event.schema.table.DropTableEvent;
import org.apache.shardingsphere.mode.event.schema.view.AlterViewEvent;
import org.apache.shardingsphere.mode.event.schema.view.DropViewEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.NewGovernanceWatcher;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * TODO Rename MetaDataChangedWatcher when metadata structure adjustment completed. #25485
 * Meta data changed watcher.
 */
public final class NewMetaDataChangedWatcher implements NewGovernanceWatcher<GovernanceEvent> {
    
    private final RuleConfigurationEventBuilder ruleConfigurationEventBuilder = new RuleConfigurationEventBuilder();
    
    @Override
    public Collection<String> getWatchingKeys(final String databaseName) {
        return null == databaseName ? Collections.singleton(DatabaseMetaDataNode.getMetaDataNodePath())
                : Collections.singleton(DatabaseMetaDataNode.getDatabaseNamePath(databaseName));
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        String key = event.getKey();
        Optional<String> databaseName = NewDatabaseMetaDataNode.getDatabaseName(key);
        if (databaseName.isPresent()) {
            return createDatabaseChangedEvent(databaseName.get(), event);
        }
        databaseName = NewDatabaseMetaDataNode.getDatabaseNameBySchemaNode(key);
        Optional<String> schemaName = NewDatabaseMetaDataNode.getSchemaName(key);
        if (databaseName.isPresent() && schemaName.isPresent()) {
            return createSchemaChangedEvent(databaseName.get(), schemaName.get(), event);
        }
        schemaName = NewDatabaseMetaDataNode.getSchemaNameByTableNode(key);
        if (databaseName.isPresent() && schemaName.isPresent() && NewDatabaseMetaDataNode.isTableActiveVersionNode(event.getKey())) {
            return createTableChangedEvent(databaseName.get(), schemaName.get(), event);
        }
        if (databaseName.isPresent() && schemaName.isPresent() && NewDatabaseMetaDataNode.isViewActiveVersionNode(event.getKey())) {
            return createViewChangedEvent(databaseName.get(), schemaName.get(), event);
        }
        if (!databaseName.isPresent()) {
            return Optional.empty();
        }
        if (NewDatabaseMetaDataNode.isDataSourcesNode(key)) {
            return createDataSourceEvent(databaseName.get(), event);
        }
        return ruleConfigurationEventBuilder.build(databaseName.get(), event);
    }
    
    private Optional<GovernanceEvent> createDatabaseChangedEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new DatabaseAddedEvent(databaseName));
        }
        if (Type.DELETED == event.getType()) {
            return Optional.of(new DatabaseDeletedEvent(databaseName));
        }
        return Optional.empty();
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
    
    private Optional<GovernanceEvent> createTableChangedEvent(final String databaseName, final String schemaName, final DataChangedEvent event) {
        Optional<String> tableName = NewDatabaseMetaDataNode.getTableName(event.getKey());
        Preconditions.checkState(tableName.isPresent(), "Not found table name.");
        if (Type.DELETED == event.getType()) {
            return Optional.of(new DropTableEvent(databaseName, schemaName, tableName.get()));
        }
        return Optional.of(new AlterTableEvent(databaseName, schemaName, tableName.get(), event.getKey(), event.getValue()));
    }
    
    private Optional<GovernanceEvent> createViewChangedEvent(final String databaseName, final String schemaName, final DataChangedEvent event) {
        Optional<String> viewName = NewDatabaseMetaDataNode.getViewName(event.getKey());
        Preconditions.checkState(viewName.isPresent(), "Not found view name.");
        if (Type.DELETED == event.getType()) {
            return Optional.of(new DropViewEvent(databaseName, schemaName, viewName.get(), event.getKey(), event.getValue()));
        }
        return Optional.of(new AlterViewEvent(databaseName, schemaName, viewName.get(), event.getKey(), event.getValue()));
    }
    
    private Optional<GovernanceEvent> createDataSourceEvent(final String databaseName, final DataChangedEvent event) {
        if (NewDatabaseMetaDataNode.isDataSourceUnitActiveVersionNode(event.getKey())) {
            return createStorageUnitChangedEvent(databaseName, event);
        }
        if (NewDatabaseMetaDataNode.isDataSourceNodeActiveVersionNode(event.getKey())) {
            return createStorageNodeChangedEvent(databaseName, event);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createStorageUnitChangedEvent(final String databaseName, final DataChangedEvent event) {
        Optional<String> dataSourceUnitName = NewDatabaseMetaDataNode.getDataSourceNameByDataSourceUnitNode(event.getKey());
        if (!dataSourceUnitName.isPresent()) {
            return Optional.empty();
        }
        if (Type.ADDED == event.getType()) {
            return Optional.of(new RegisterStorageUnitEvent(databaseName, dataSourceUnitName.get(), event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterStorageUnitEvent(databaseName, dataSourceUnitName.get(), event.getKey(), event.getValue()));
        }
        return Optional.of(new UnregisterStorageUnitEvent(databaseName, dataSourceUnitName.get()));
    }
    
    private Optional<GovernanceEvent> createStorageNodeChangedEvent(final String databaseName, final DataChangedEvent event) {
        Optional<String> dataSourceNodeName = NewDatabaseMetaDataNode.getDataSourceNameByDataSourceNode(event.getKey());
        if (!dataSourceNodeName.isPresent()) {
            return Optional.empty();
        }
        if (Type.ADDED == event.getType()) {
            return Optional.of(new RegisterStorageNodeEvent(databaseName, dataSourceNodeName.get(), event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterStorageNodeEvent(databaseName, dataSourceNodeName.get(), event.getKey(), event.getValue()));
        }
        return Optional.of(new UnregisterStorageNodeEvent(databaseName, dataSourceNodeName.get()));
    }
}
