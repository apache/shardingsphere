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

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.metadata.schema.builder.SystemSchemaBuilderRule;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlTableMetaData;
import org.apache.shardingsphere.infra.yaml.schema.swapper.TableMetaDataYamlSwapper;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.schema.SchemaChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.version.SchemaVersionChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;

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
    public Collection<String> getWatchingKeys() {
        return Collections.singleton(DatabaseMetaDataNode.getMetaDataNodePath());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        // TODO Maybe can reduce once regular
        if (isLogicDatabaseChanged(event)) {
            return buildLogicDatabaseChangedEvent(event);
        } else if (isLogicSchemaChanged(event)) {
            return buildLogicSchemaChangedEvent(event);
        } else if (isTableMetaDataChanged(event)) {
            return buildTableMetaDataChangedEvent(event);
        } else if (Type.UPDATED == event.getType()) {
            return buildGovernanceEvent(event);
        }
        return Optional.empty();
    }
    
    private boolean isLogicDatabaseChanged(final DataChangedEvent event) {
        return DatabaseMetaDataNode.getDatabaseName(event.getKey()).isPresent();
    }
    
    private boolean isLogicSchemaChanged(final DataChangedEvent event) {
        return DatabaseMetaDataNode.getSchemaName(event.getKey()).isPresent();
    }
    
    private boolean isTableMetaDataChanged(final DataChangedEvent event) {
        Optional<String> databaseName = DatabaseMetaDataNode.getDatabaseNameByDatabasePath(event.getKey());
        Optional<String> schemaName = DatabaseMetaDataNode.getSchemaNameBySchemaPath(event.getKey());
        Optional<String> tableName = DatabaseMetaDataNode.getTableName(event.getKey());
        return databaseName.isPresent() && tableName.isPresent() && schemaName.isPresent()
                && !SystemSchemaBuilderRule.isSystemTable(databaseName.get(), tableName.get()) && !Strings.isNullOrEmpty(event.getValue());
    }
    
    private Optional<GovernanceEvent> buildLogicDatabaseChangedEvent(final DataChangedEvent event) {
        String databaseName = DatabaseMetaDataNode.getDatabaseName(event.getKey()).get();
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new DatabaseAddedEvent(databaseName));
        }
        if (Type.DELETED == event.getType()) {
            return Optional.of(new DatabaseDeletedEvent(databaseName));
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> buildLogicSchemaChangedEvent(final DataChangedEvent event) {
        String databaseName = DatabaseMetaDataNode.getDatabaseNameByDatabasePath(event.getKey()).get();
        String schemaName = DatabaseMetaDataNode.getSchemaName(event.getKey()).get();
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new SchemaAddedEvent(databaseName, schemaName));
        }
        if (Type.DELETED == event.getType()) {
            return Optional.of(new SchemaDeletedEvent(databaseName, schemaName));
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> buildGovernanceEvent(final DataChangedEvent event) {
        Optional<String> databaseName = DatabaseMetaDataNode.getDatabaseNameByDatabasePath(event.getKey());
        if (!databaseName.isPresent() || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        if (event.getKey().equals(DatabaseMetaDataNode.getActiveVersionPath(databaseName.get()))) {
            return Optional.of(new SchemaVersionChangedEvent(databaseName.get(), event.getValue()));
        }
        Optional<String> schemaVersion = DatabaseMetaDataNode.getVersionByDataSourcesPath(event.getKey());
        if (schemaVersion.isPresent()) {
            return Optional.of(createDataSourceChangedEvent(databaseName.get(), schemaVersion.get(), event));
        }
        schemaVersion = DatabaseMetaDataNode.getVersionByRulesPath(event.getKey());
        if (schemaVersion.isPresent()) {
            return Optional.of(createRuleChangedEvent(databaseName.get(), schemaVersion.get(), event));
        }
        return Optional.empty();
    }
    
    @SuppressWarnings("unchecked")
    private DataSourceChangedEvent createDataSourceChangedEvent(final String databaseName, final String schemaVersion, final DataChangedEvent event) {
        Map<String, Map<String, Object>> yamlDataSources = YamlEngine.unmarshal(event.getValue(), Map.class);
        Map<String, DataSourceProperties> dataSourcePropertiesMap = yamlDataSources.isEmpty()
                ? new HashMap<>()
                : yamlDataSources.entrySet().stream().collect(Collectors.toMap(
                        Entry::getKey, entry -> new YamlDataSourceConfigurationSwapper().swapToDataSourceProperties(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        return new DataSourceChangedEvent(databaseName, schemaVersion, dataSourcePropertiesMap);
    }
    
    private GovernanceEvent createRuleChangedEvent(final String databaseName, final String schemaVersion, final DataChangedEvent event) {
        return new RuleConfigurationsChangedEvent(databaseName, schemaVersion, getRuleConfigurations(event.getValue()));
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> getRuleConfigurations(final String yamlContent) {
        Collection<YamlRuleConfiguration> rules = Strings.isNullOrEmpty(yamlContent)
                ? new LinkedList<>()
                : YamlEngine.unmarshal(yamlContent, Collection.class, true);
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(rules);
    }
    
    private Optional<GovernanceEvent> buildTableMetaDataChangedEvent(final DataChangedEvent event) {
        String databaseName = DatabaseMetaDataNode.getDatabaseNameByDatabasePath(event.getKey()).get();
        String schemaName = DatabaseMetaDataNode.getSchemaNameBySchemaPath(event.getKey()).get();
        String tableName = DatabaseMetaDataNode.getTableName(event.getKey()).get();
        if (Type.DELETED == event.getType()) {
            return Optional.of(new SchemaChangedEvent(databaseName, schemaName, null, tableName));
        }
        return Optional.of(new SchemaChangedEvent(databaseName, schemaName, new TableMetaDataYamlSwapper().swapToObject(YamlEngine.unmarshal(event.getValue(), YamlTableMetaData.class)), null));
    }
}
