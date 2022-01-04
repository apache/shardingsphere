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
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlTableMetaData;
import org.apache.shardingsphere.infra.yaml.schema.swapper.TableMetaDataYamlSwapper;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.RuleConfigurationCachedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.schema.SchemaChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.metadata.persist.node.CacheNode;
import org.apache.shardingsphere.mode.metadata.persist.node.SchemaMetaDataNode;
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
        return Collections.singleton(SchemaMetaDataNode.getMetaDataNodePath());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        String schemaName = SchemaMetaDataNode.getSchemaNameBySchemaPath(event.getKey());
        if (!Strings.isNullOrEmpty(schemaName)) {
            return buildGovernanceEvent(schemaName, event);
        }
        schemaName = SchemaMetaDataNode.getSchemaName(event.getKey());
        String tableName = SchemaMetaDataNode.getTableName(schemaName, event.getKey());
        if (!Strings.isNullOrEmpty(tableName) && !Strings.isNullOrEmpty(event.getValue())) {
            return createTableMetaDataChangedEvent(schemaName, tableName, event);
        }
        if (Type.ADDED == event.getType() && !Strings.isNullOrEmpty(event.getValue())) {
            Optional<String> ruleCacheId = getRuleCacheId(schemaName, event.getKey());
            return ruleCacheId.isPresent() ? Optional.of(new RuleConfigurationCachedEvent(ruleCacheId.get(), schemaName)) : Optional.empty();
        }
        if (DataChangedEvent.Type.UPDATED == event.getType()) {
            return buildGovernanceEvent(event);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> buildGovernanceEvent(final String schemaName, final DataChangedEvent event) {
        if (DataChangedEvent.Type.ADDED == event.getType() || DataChangedEvent.Type.UPDATED == event.getType()) {
            return Optional.of(new SchemaAddedEvent(schemaName));
        }
        if (DataChangedEvent.Type.DELETED == event.getType()) {
            return Optional.of(new SchemaDeletedEvent(schemaName));
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> buildGovernanceEvent(final DataChangedEvent event) {
        String schemaName = SchemaMetaDataNode.getSchemaName(event.getKey());
        if (Strings.isNullOrEmpty(schemaName) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        if (isDataSourceChangedEvent(schemaName, event.getKey())) {
            return Optional.of(createDataSourceChangedEvent(schemaName, event));
        }
        if (isRuleChangedEvent(schemaName, event.getKey())) {
            return Optional.of(createRuleChangedEvent(schemaName, event));
        }
        return Optional.empty();
    }

    private boolean isDataSourceChangedEvent(final String schemaName, final String eventPath) {
        return SchemaMetaDataNode.getMetaDataDataSourcePath(schemaName).equals(eventPath);
    }
    
    @SuppressWarnings("unchecked")
    private DataSourceChangedEvent createDataSourceChangedEvent(final String schemaName, final DataChangedEvent event) {
        Map<String, Map<String, Object>> yamlDataSources = YamlEngine.unmarshal(event.getValue(), Map.class);
        Map<String, DataSourceConfiguration> dataSourceConfigs = yamlDataSources.isEmpty() ? new HashMap<>()
                : yamlDataSources.entrySet().stream().collect(Collectors.toMap(
                    Entry::getKey, entry -> new YamlDataSourceConfigurationSwapper().swapToDataSourceConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        return new DataSourceChangedEvent(schemaName, dataSourceConfigs);
    }
    
    private boolean isRuleChangedEvent(final String schemaName, final String eventPath) {
        return SchemaMetaDataNode.getRulePath(schemaName).equals(eventPath);
    }
    
    private Optional<String> getRuleCacheId(final String schemaName, final String key) {
        return CacheNode.getCacheId(SchemaMetaDataNode.getRulePath(schemaName), key);
    }
    
    private GovernanceEvent createRuleChangedEvent(final String schemaName, final DataChangedEvent event) {
        return new RuleConfigurationsChangedEvent(schemaName, getRuleConfigurations(event.getValue()));
    }

    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> getRuleConfigurations(final String yamlContent) {
        Collection<YamlRuleConfiguration> rules = Strings.isNullOrEmpty(yamlContent)
                ? new LinkedList<>() : YamlEngine.unmarshal(yamlContent, Collection.class);
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(rules);
    }
    
    private Optional<GovernanceEvent> createTableMetaDataChangedEvent(final String schemaName, final String table, final DataChangedEvent event) {
        if (DataChangedEvent.Type.DELETED == event.getType()) {
            return Optional.of(new SchemaChangedEvent(schemaName, null, table));
        }
        return Optional.of(new SchemaChangedEvent(schemaName, new TableMetaDataYamlSwapper().swapToObject(YamlEngine.unmarshal(event.getValue(), YamlTableMetaData.class)), null));
    }
}
