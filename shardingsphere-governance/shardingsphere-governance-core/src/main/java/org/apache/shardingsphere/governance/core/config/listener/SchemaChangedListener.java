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

package org.apache.shardingsphere.governance.core.config.listener;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.collections4.SetUtils;
import org.apache.shardingsphere.governance.core.config.ConfigCenterNode;
import org.apache.shardingsphere.governance.core.event.listener.PostGovernanceRepositoryEventListener;
import org.apache.shardingsphere.governance.core.event.model.GovernanceEvent;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataAddedEvent;
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataDeletedEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationCachedEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.schema.SchemaChangedEvent;
import org.apache.shardingsphere.governance.core.yaml.config.YamlDataSourceConfigurationWrap;
import org.apache.shardingsphere.governance.core.yaml.config.schema.YamlSchema;
import org.apache.shardingsphere.governance.core.yaml.swapper.DataSourceConfigurationYamlSwapper;
import org.apache.shardingsphere.governance.core.yaml.swapper.SchemaYamlSwapper;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Schema changed listener.
 */
public final class SchemaChangedListener extends PostGovernanceRepositoryEventListener<GovernanceEvent> {
    
    private final ConfigCenterNode configurationNode;
    
    private final Collection<String> existedSchemaNames;
    
    public SchemaChangedListener(final ConfigurationRepository configurationRepository, final Collection<String> schemaNames) {
        super(configurationRepository, new ConfigCenterNode().getAllSchemaConfigPaths(schemaNames));
        configurationNode = new ConfigCenterNode();
        existedSchemaNames = new LinkedHashSet<>(schemaNames);
    }
    
    @Override
    protected Optional<GovernanceEvent> createEvent(final DataChangedEvent event) {
        // TODO Consider removing the following one.
        if (configurationNode.getMetadataNodePath().equals(event.getKey())) {
            return createSchemaNamesUpdatedEvent(event.getValue());
        }
        String schemaName = configurationNode.getSchemaName(event.getKey());
        if (Strings.isNullOrEmpty(schemaName) || !isValidNodeChangedEvent(schemaName, event.getKey())) {
            return Optional.empty();
        }
        if (Type.ADDED == event.getType()) {
            if (event.getKey().startsWith(configurationNode.getCachePath(configurationNode.getRulePath(schemaName)))) {
                return Optional.of(createRuleConfigurationCachedEvent(schemaName, event));
            }
            return Optional.of(createAddedEvent(schemaName));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(createUpdatedEvent(schemaName, event));
        }
        if (Type.DELETED == event.getType()) {
            if (event.getKey().startsWith(configurationNode.getCachePath(configurationNode.getRulePath(schemaName)))) {
                return Optional.empty();
            }
            existedSchemaNames.remove(schemaName);
            return Optional.of(new MetaDataDeletedEvent(schemaName));
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createSchemaNamesUpdatedEvent(final String schemaNames) {
        Collection<String> persistedSchemaNames = configurationNode.splitSchemaName(schemaNames);
        Set<String> addedSchemaNames = SetUtils.difference(new HashSet<>(persistedSchemaNames), new HashSet<>(existedSchemaNames));
        if (!addedSchemaNames.isEmpty()) {
            return Optional.of(createAddedEvent(addedSchemaNames.iterator().next()));
        }
        Set<String> deletedSchemaNames = SetUtils.difference(new HashSet<>(existedSchemaNames), new HashSet<>(persistedSchemaNames));
        if (!deletedSchemaNames.isEmpty()) {
            String schemaName = deletedSchemaNames.iterator().next();
            existedSchemaNames.remove(schemaName);
            return Optional.of(new MetaDataDeletedEvent(schemaName));
        }
        return Optional.empty();
    }
    
    private boolean isValidNodeChangedEvent(final String schemaName, final String nodeFullPath) {
        return !existedSchemaNames.contains(schemaName) || configurationNode.getDataSourcePath(schemaName).equals(nodeFullPath) 
                || configurationNode.getRulePath(schemaName).equals(nodeFullPath)
                || configurationNode.getSchemaPath(schemaName).equals(nodeFullPath)
                || nodeFullPath.startsWith(configurationNode.getCachePath(configurationNode.getRulePath(schemaName)));
    }
    
    private GovernanceEvent createAddedEvent(final String schemaName) {
        existedSchemaNames.add(schemaName);
        return new MetaDataAddedEvent(schemaName, Collections.emptyMap(), Collections.emptyList());
    }
    
    private GovernanceEvent createUpdatedEvent(final String schemaName, final DataChangedEvent event) {
        // TODO Consider remove judgement.
        return existedSchemaNames.contains(schemaName) ? createUpdatedEventForExistedSchema(schemaName, event) : createAddedEvent(schemaName);
    }
    
    private GovernanceEvent createUpdatedEventForExistedSchema(final String schemaName, final DataChangedEvent event) {
        if (event.getKey().equals(configurationNode.getDataSourcePath(schemaName))) {
            return createDataSourceChangedEvent(schemaName, event);
        } else if (event.getKey().equals(configurationNode.getRulePath(schemaName))) {
            return createRuleChangedEvent(schemaName, event);
        }
        return createSchemaChangedEvent(schemaName, event);
    }
    
    private DataSourceChangedEvent createDataSourceChangedEvent(final String schemaName, final DataChangedEvent event) {
        YamlDataSourceConfigurationWrap result = YamlEngine.unmarshal(event.getValue(), YamlDataSourceConfigurationWrap.class);
        Preconditions.checkState(null != result && !result.getDataSources().isEmpty(), "No available data sources to load for governance.");
        return new DataSourceChangedEvent(schemaName, result.getDataSources().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> new DataSourceConfigurationYamlSwapper().swapToObject(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
    }
    
    private GovernanceEvent createRuleChangedEvent(final String schemaName, final DataChangedEvent event) {
        return new RuleConfigurationsChangedEvent(schemaName, getRuleConfigurations(event.getValue()));
    }
    
    private GovernanceEvent createSchemaChangedEvent(final String schemaName, final DataChangedEvent event) {
        return new SchemaChangedEvent(schemaName, new SchemaYamlSwapper().swapToObject(YamlEngine.unmarshal(event.getValue(), YamlSchema.class)));
    }
    
    private GovernanceEvent createRuleConfigurationCachedEvent(final String schemaName, final DataChangedEvent event) {
        return new RuleConfigurationCachedEvent(getCacheId(event.getKey()), schemaName);
    }
    
    private String getCacheId(final String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }
    
    private Collection<RuleConfiguration> getRuleConfigurations(final String yamlContent) {
        YamlRootRuleConfigurations configurations = YamlEngine.unmarshal(yamlContent, YamlRootRuleConfigurations.class);
        Preconditions.checkState(null != configurations, "No available rule to load for governance.");
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(configurations.getRules());
    }
}
