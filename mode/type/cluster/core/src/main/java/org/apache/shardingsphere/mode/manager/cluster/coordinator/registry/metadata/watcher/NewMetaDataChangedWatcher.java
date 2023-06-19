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

import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereView;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlTableSwapper;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlViewSwapper;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.NewDatabaseMetaDataNode;
import org.apache.shardingsphere.mode.event.datasource.AlterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.datasource.RegisterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.datasource.UnregisterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.schema.table.AlterTableEvent;
import org.apache.shardingsphere.mode.event.schema.table.DropTableEvent;
import org.apache.shardingsphere.mode.event.schema.view.AlterViewEvent;
import org.apache.shardingsphere.mode.event.schema.view.DropViewEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.spi.RuleConfigurationEventBuilder;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.NewGovernanceWatcher;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * TODO Rename MetaDataChangedWatcher when metadata structure adjustment completed. #25485
 * Meta data changed watcher.
 */
public final class NewMetaDataChangedWatcher implements NewGovernanceWatcher<GovernanceEvent> {
    
    private static final Collection<RuleConfigurationEventBuilder> EVENT_BUILDERS = ShardingSphereServiceLoader.getServiceInstances(RuleConfigurationEventBuilder.class);
    
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
        Optional<String> tableNameVersion = NewDatabaseMetaDataNode.getTableNameVersion(key);
        if (databaseName.isPresent() && schemaName.isPresent() && tableNameVersion.isPresent()) {
            return createTableChangedEvent(databaseName.get(), schemaName.get(), tableNameVersion.get(), event);
        }
        Optional<String> viewNameVersion = NewDatabaseMetaDataNode.getViewNameVersion(key);
        if (databaseName.isPresent() && schemaName.isPresent() && viewNameVersion.isPresent()) {
            return createViewChangedEvent(databaseName.get(), schemaName.get(), viewNameVersion.get(), event);
        }
        if (!databaseName.isPresent()) {
            return Optional.empty();
        }
        if (NewDatabaseMetaDataNode.isDataSourcesNode(key)) {
            return createDataSourceEvent(databaseName.get(), event);
        }
        return createDatabaseRuleEvent(databaseName.get(), event);
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
    
    private Optional<GovernanceEvent> createTableChangedEvent(final String databaseName, final String schemaName, final String tableNameVersion, final DataChangedEvent event) {
        if (Type.DELETED == event.getType()) {
            return Optional.of(new DropTableEvent(databaseName, schemaName, NewDatabaseMetaDataNode.getTableName(event.getKey()).orElse(""), Integer.parseInt(tableNameVersion), event.getKey()));
        }
        return Optional.of(new AlterTableEvent(databaseName, schemaName,
                new YamlTableSwapper().swapToObject(YamlEngine.unmarshal(event.getValue(), YamlShardingSphereTable.class)), Integer.parseInt(tableNameVersion), event.getKey()));
    }
    
    private Optional<GovernanceEvent> createViewChangedEvent(final String databaseName, final String schemaName, final String viewNameVersion, final DataChangedEvent event) {
        if (Type.DELETED == event.getType()) {
            return Optional.of(new DropViewEvent(databaseName, schemaName, NewDatabaseMetaDataNode.getViewName(event.getKey()).orElse(""), Integer.parseInt(viewNameVersion), event.getKey()));
        }
        return Optional.of(new AlterViewEvent(databaseName, schemaName,
                new YamlViewSwapper().swapToObject(YamlEngine.unmarshal(event.getValue(), YamlShardingSphereView.class)), Integer.parseInt(viewNameVersion), event.getKey()));
    }
    
    @SuppressWarnings("unchecked")
    private Optional<GovernanceEvent> createDataSourceEvent(final String databaseName, final DataChangedEvent event) {
        Optional<String> dataSourceName = NewDatabaseMetaDataNode.getDataSourceNameByDataSourceNode(event.getKey());
        if (!dataSourceName.isPresent()) {
            return Optional.empty();
        }
        Optional<String> version = NewDatabaseMetaDataNode.getVersionByDataSourceNode(event.getKey());
        if (!version.isPresent()) {
            return Optional.empty();
        }
        if (Type.ADDED == event.getType()) {
            return Optional.of(new RegisterStorageUnitEvent(databaseName, dataSourceName.get(),
                    new YamlDataSourceConfigurationSwapper().swapToDataSourceProperties(YamlEngine.unmarshal(event.getValue(), Map.class)), event.getKey(), Integer.parseInt(version.get())));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterStorageUnitEvent(databaseName, dataSourceName.get(),
                    new YamlDataSourceConfigurationSwapper().swapToDataSourceProperties(YamlEngine.unmarshal(event.getValue(), Map.class)), event.getKey(), Integer.parseInt(version.get())));
        }
        return Optional.of(new UnregisterStorageUnitEvent(databaseName, dataSourceName.get(), event.getKey(), Integer.parseInt(version.get())));
    }
    
    private Optional<GovernanceEvent> createDatabaseRuleEvent(final String databaseName, final DataChangedEvent event) {
        for (RuleConfigurationEventBuilder each : EVENT_BUILDERS) {
            Optional<GovernanceEvent> result = each.build(databaseName, event);
            if (!result.isPresent()) {
                continue;
            }
            return result;
        }
        return Optional.empty();
    }
}
