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
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.SystemSchemaBuilderRule;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereView;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlTableSwapper;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlViewSwapper;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceNodesChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceUnitsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.event.schema.TableMetaDataChangedEvent;
import org.apache.shardingsphere.mode.event.schema.ViewMetaDataChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Meta data changed watcher.
 */
public final class MetaDataChangedWatcher implements GovernanceWatcher<GovernanceEvent> {
    
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
        if (databaseChanged(event)) {
            return createDatabaseChangedEvent(event);
        }
        if (schemaChanged(event)) {
            return createSchemaChangedEvent(event);
        }
        if (schemaMetaDataChanged(event)) {
            return createSchemaMetaDataChangedEvent(event);
        }
        return createRuleAndDataSourceChangedEvent(event);
    }
    
    private boolean databaseChanged(final DataChangedEvent event) {
        return DatabaseMetaDataNode.getDatabaseName(event.getKey()).isPresent();
    }
    
    private boolean schemaChanged(final DataChangedEvent event) {
        return DatabaseMetaDataNode.getDatabaseNameByDatabasePath(event.getKey()).isPresent() && DatabaseMetaDataNode.getSchemaName(event.getKey()).isPresent();
    }
    
    private boolean schemaMetaDataChanged(final DataChangedEvent event) {
        if (Strings.isNullOrEmpty(event.getValue())) {
            return false;
        }
        Optional<String> databaseName = DatabaseMetaDataNode.getDatabaseNameByDatabasePath(event.getKey());
        if (!databaseName.isPresent()) {
            return false;
        }
        Optional<String> schemaName = DatabaseMetaDataNode.getSchemaNameBySchemaPath(event.getKey());
        if (!schemaName.isPresent()) {
            return false;
        }
        Optional<String> tableName = DatabaseMetaDataNode.getTableName(event.getKey());
        Optional<String> viewName = DatabaseMetaDataNode.getViewName(event.getKey());
        return tableName.isPresent() && !SystemSchemaBuilderRule.isSystemTable(databaseName.get(), tableName.get()) || viewName.isPresent();
    }
    
    private Optional<GovernanceEvent> createDatabaseChangedEvent(final DataChangedEvent event) {
        Optional<String> databaseName = DatabaseMetaDataNode.getDatabaseName(event.getKey());
        Preconditions.checkState(databaseName.isPresent());
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new DatabaseAddedEvent(databaseName.get()));
        }
        if (Type.DELETED == event.getType()) {
            return Optional.of(new DatabaseDeletedEvent(databaseName.get()));
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createSchemaChangedEvent(final DataChangedEvent event) {
        Optional<String> databaseName = DatabaseMetaDataNode.getDatabaseNameByDatabasePath(event.getKey());
        Preconditions.checkState(databaseName.isPresent());
        Optional<String> schemaName = DatabaseMetaDataNode.getSchemaName(event.getKey());
        Preconditions.checkState(schemaName.isPresent());
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new SchemaAddedEvent(databaseName.get(), schemaName.get()));
        }
        if (Type.DELETED == event.getType()) {
            return Optional.of(new SchemaDeletedEvent(databaseName.get(), schemaName.get()));
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createRuleAndDataSourceChangedEvent(final DataChangedEvent event) {
        Optional<String> databaseName = DatabaseMetaDataNode.getDatabaseNameByDatabasePath(event.getKey());
        if (!databaseName.isPresent() || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        Optional<String> databaseVersion = DatabaseMetaDataNode.getVersionByDataSourceUnitsPath(event.getKey());
        if (databaseVersion.isPresent() && event.getType() != Type.DELETED) {
            return Optional.of(createDataSourceUnitsChangedEvent(databaseName.get(), databaseVersion.get(), event));
        }
        databaseVersion = DatabaseMetaDataNode.getVersionByDataSourceNodesPath(event.getKey());
        if (databaseVersion.isPresent() && event.getType() != Type.DELETED) {
            return Optional.of(createDataSourceNodesChangedEvent(databaseName.get(), databaseVersion.get(), event));
        }
        databaseVersion = DatabaseMetaDataNode.getVersionByRulesPath(event.getKey());
        if (databaseVersion.isPresent() && event.getType() != Type.DELETED) {
            return Optional.of(new RuleConfigurationsChangedEvent(databaseName.get(), databaseVersion.get(), getRuleConfigurations(event.getValue())));
        }
        return Optional.empty();
    }
    
    @SuppressWarnings("unchecked")
    private DataSourceUnitsChangedEvent createDataSourceUnitsChangedEvent(final String databaseName, final String databaseVersion, final DataChangedEvent event) {
        Map<String, Map<String, Object>> yamlDataSources = YamlEngine.unmarshal(event.getValue(), Map.class);
        Map<String, DataSourceProperties> dataSourcePropertiesMap = yamlDataSources.isEmpty()
                ? new HashMap<>()
                : yamlDataSources.entrySet().stream().collect(Collectors.toMap(
                        Entry::getKey, entry -> new YamlDataSourceConfigurationSwapper().swapToDataSourceProperties(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        return new DataSourceUnitsChangedEvent(databaseName, databaseVersion, dataSourcePropertiesMap);
    }
    
    @SuppressWarnings("unchecked")
    private DataSourceNodesChangedEvent createDataSourceNodesChangedEvent(final String databaseName, final String databaseVersion, final DataChangedEvent event) {
        Map<String, Map<String, Object>> yamlDataSources = YamlEngine.unmarshal(event.getValue(), Map.class);
        Map<String, DataSourceProperties> dataSourcePropertiesMap = yamlDataSources.isEmpty()
                ? new HashMap<>()
                : yamlDataSources.entrySet().stream().collect(Collectors.toMap(
                        Entry::getKey, entry -> new YamlDataSourceConfigurationSwapper().swapToDataSourceProperties(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        return new DataSourceNodesChangedEvent(databaseName, databaseVersion, dataSourcePropertiesMap);
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> getRuleConfigurations(final String yamlContent) {
        Collection<YamlRuleConfiguration> rules = Strings.isNullOrEmpty(yamlContent) ? new LinkedList<>() : YamlEngine.unmarshal(yamlContent, Collection.class, true);
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(rules);
    }
    
    private Optional<GovernanceEvent> createSchemaMetaDataChangedEvent(final DataChangedEvent event) {
        Optional<String> databaseName = DatabaseMetaDataNode.getDatabaseNameByDatabasePath(event.getKey());
        Preconditions.checkState(databaseName.isPresent());
        Optional<String> schemaName = DatabaseMetaDataNode.getSchemaNameBySchemaPath(event.getKey());
        Preconditions.checkState(schemaName.isPresent());
        return Optional.of(createSchemaMetaDataChangedEvent(event, databaseName.get(), schemaName.get()));
    }
    
    private GovernanceEvent createSchemaMetaDataChangedEvent(final DataChangedEvent event, final String databaseName, final String schemaName) {
        Optional<String> tableName = DatabaseMetaDataNode.getTableName(event.getKey());
        Optional<String> viewName = DatabaseMetaDataNode.getViewName(event.getKey());
        Preconditions.checkState(tableName.isPresent() || viewName.isPresent());
        if (tableName.isPresent()) {
            return Type.DELETED == event.getType()
                    ? new TableMetaDataChangedEvent(databaseName, schemaName, null, tableName.get())
                    : new TableMetaDataChangedEvent(databaseName, schemaName, new YamlTableSwapper().swapToObject(YamlEngine.unmarshal(event.getValue(), YamlShardingSphereTable.class)), null);
        }
        return Type.DELETED == event.getType()
                ? new ViewMetaDataChangedEvent(databaseName, schemaName, null, viewName.get())
                : new ViewMetaDataChangedEvent(databaseName, schemaName, new YamlViewSwapper().swapToObject(YamlEngine.unmarshal(event.getValue(), YamlShardingSphereView.class)), null);
    }
}
