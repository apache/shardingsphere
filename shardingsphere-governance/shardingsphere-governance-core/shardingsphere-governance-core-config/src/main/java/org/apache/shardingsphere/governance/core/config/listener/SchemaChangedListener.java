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
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.governance.core.common.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.governance.core.common.event.GovernanceEvent;
import org.apache.shardingsphere.governance.core.common.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.common.event.schema.SchemaAddedEvent;
import org.apache.shardingsphere.governance.core.common.event.schema.SchemaDeletedEvent;
import org.apache.shardingsphere.governance.core.common.listener.PostGovernanceRepositoryEventListener;
import org.apache.shardingsphere.governance.core.common.yaml.config.YamlDataSourceConfiguration;
import org.apache.shardingsphere.governance.core.common.yaml.swapper.DataSourceConfigurationYamlSwapper;
import org.apache.shardingsphere.governance.core.config.ConfigCenter;
import org.apache.shardingsphere.governance.core.config.ConfigCenterNode;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.ChangedType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Schema changed listener.
 */
public final class SchemaChangedListener extends PostGovernanceRepositoryEventListener {
    
    private final ConfigCenter configurationService;
    
    private final ConfigCenterNode configurationNode;
    
    private final Collection<String> existedSchemaNames = new LinkedHashSet<>();
    
    public SchemaChangedListener(final ConfigurationRepository configurationRepository, final Collection<String> schemaNames) {
        super(configurationRepository, new ConfigCenterNode().getAllSchemaConfigPaths(schemaNames));
        configurationService = new ConfigCenter(configurationRepository);
        configurationNode = new ConfigCenterNode();
        existedSchemaNames.addAll(schemaNames);
    }
    
    @Override
    protected Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        // TODO Consider removing the following one.
        if (configurationNode.getSchemaPath().equals(event.getKey())) {
            return createSchemaNamesUpdatedEvent(event.getValue());
        }
        String shardingSchemaName = configurationNode.getSchemaName(event.getKey());
        if (Strings.isNullOrEmpty(shardingSchemaName) || !isValidNodeChangedEvent(shardingSchemaName, event.getKey())) {
            return Optional.empty();
        }
        if (ChangedType.ADDED == event.getChangedType()) {
            return Optional.of(createAddedEvent(shardingSchemaName));
        }
        if (ChangedType.UPDATED == event.getChangedType()) {
            return Optional.of(createUpdatedEvent(shardingSchemaName, event));
        }
        if (ChangedType.DELETED == event.getChangedType()) {
            return Optional.of(createDeletedEvent(shardingSchemaName));
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createSchemaNamesUpdatedEvent(final String shardingSchemaNames) {
        Collection<String> persistShardingSchemaNames = configurationNode.splitSchemaName(shardingSchemaNames);
        Set<String> addedSchemaNames = SetUtils.difference(new HashSet<>(persistShardingSchemaNames), new HashSet<>(existedSchemaNames));
        if (!addedSchemaNames.isEmpty()) {
            return Optional.of(createAddedEvent(addedSchemaNames.iterator().next()));
        }
        Set<String> deletedSchemaNames = SetUtils.difference(new HashSet<>(existedSchemaNames), new HashSet<>(persistShardingSchemaNames));
        if (!deletedSchemaNames.isEmpty()) {
            return Optional.of(createDeletedEvent(deletedSchemaNames.iterator().next()));
        }
        return Optional.empty();
    }
    
    private boolean isValidNodeChangedEvent(final String shardingSchemaName, final String nodeFullPath) {
        return !existedSchemaNames.contains(shardingSchemaName)
                || configurationNode.getDataSourcePath(shardingSchemaName).equals(nodeFullPath) || configurationNode.getRulePath(shardingSchemaName).equals(nodeFullPath);
    }
    
    private GovernanceEvent createAddedEvent(final String shardingSchemaName) {
        existedSchemaNames.add(shardingSchemaName);
        if (!isOwnCompleteConfigurations(shardingSchemaName)) {
            return new SchemaAddedEvent(shardingSchemaName, Collections.emptyMap(), Collections.emptyList());
        }
        return new SchemaAddedEvent(shardingSchemaName, configurationService.loadDataSourceConfigurations(shardingSchemaName), createRuleConfigurations(shardingSchemaName));
    }
    
    private GovernanceEvent createUpdatedEvent(final String shardingSchemaName, final DataChangedEvent event) {
        // TODO Consider remove judgement.
        return existedSchemaNames.contains(shardingSchemaName) ? createUpdatedEventForExistedSchema(event, shardingSchemaName) : createAddedEvent(shardingSchemaName);
    }
    
    private GovernanceEvent createUpdatedEventForExistedSchema(final DataChangedEvent event, final String shardingSchemaName) {
        return event.getKey().equals(configurationNode.getDataSourcePath(shardingSchemaName))
                ? createDataSourceChangedEvent(shardingSchemaName, event) : createRuleChangedEvent(shardingSchemaName, event);
    }
    
    @SuppressWarnings("unchecked")
    private DataSourceChangedEvent createDataSourceChangedEvent(final String shardingSchemaName, final DataChangedEvent event) {
        Map<String, YamlDataSourceConfiguration> dataSourceConfigurations = (Map) YamlEngine.unmarshal(event.getValue());
        Preconditions.checkState(null != dataSourceConfigurations && !dataSourceConfigurations.isEmpty(), "No available data sources to load for governance.");
        return new DataSourceChangedEvent(shardingSchemaName, dataSourceConfigurations.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> new DataSourceConfigurationYamlSwapper().swapToObject(entry.getValue()))));
    }
    
    private GovernanceEvent createRuleChangedEvent(final String shardingSchemaName, final DataChangedEvent event) {
        YamlRootRuleConfigurations configurations = YamlEngine.unmarshal(event.getValue(), YamlRootRuleConfigurations.class);
        Preconditions.checkState(null != configurations, "No available rule to load for governance.");
        return new RuleConfigurationsChangedEvent(
                shardingSchemaName, new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(configurations.getRules()));
    }
    
    private boolean isOwnCompleteConfigurations(final String shardingSchemaName) {
        return configurationService.hasDataSourceConfiguration(shardingSchemaName) && configurationService.hasRuleConfiguration(shardingSchemaName);
    }
    
    private Collection<RuleConfiguration> createRuleConfigurations(final String shardingSchemaName) {
        return configurationService.loadRuleConfigurations(shardingSchemaName);
    }
    
    private GovernanceEvent createDeletedEvent(final String shardingSchemaName) {
        existedSchemaNames.remove(shardingSchemaName);
        return new SchemaDeletedEvent(shardingSchemaName);
    }
}
