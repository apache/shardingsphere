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

package org.apache.shardingsphere.governance.core.registry.config.service.impl;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.registry.config.service.SchemaBasedRegistryService;
import org.apache.shardingsphere.governance.core.registry.config.event.datasource.DataSourceAddedSQLNotificationEvent;
import org.apache.shardingsphere.governance.core.registry.config.event.datasource.DataSourceDroppedSQLNotificationEvent;
import org.apache.shardingsphere.governance.core.registry.config.node.SchemaMetadataNode;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlDataSourceConfigurationSwapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Data source registry service.
 */
public final class DataSourceRegistryService implements SchemaBasedRegistryService<Map<String, DataSourceConfiguration>> {
    
    private final RegistryCenterRepository repository;
    
    public DataSourceRegistryService(final RegistryCenterRepository repository) {
        this.repository = repository;
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    @Override
    public void persist(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigs, final boolean isOverwrite) {
        if (!dataSourceConfigs.isEmpty() && (isOverwrite || !isExisted(schemaName))) {
            persist(schemaName, dataSourceConfigs);
        }
    }
    
    @Override
    public void persist(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigs) {
        repository.persist(SchemaMetadataNode.getMetadataDataSourcePath(schemaName), YamlEngine.marshal(swapYamlDataSourceConfiguration(dataSourceConfigs)));
    }
    
    private Map<String, Map<String, Object>> swapYamlDataSourceConfiguration(final Map<String, DataSourceConfiguration> dataSourceConfigs) {
        return dataSourceConfigs.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> new YamlDataSourceConfigurationSwapper().swapToMap(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    @Override
    public Map<String, DataSourceConfiguration> load(final String schemaName) {
        return isExisted(schemaName) ? getDataSourceConfigurations(repository.get(SchemaMetadataNode.getMetadataDataSourcePath(schemaName))) : new LinkedHashMap<>();
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, DataSourceConfiguration> getDataSourceConfigurations(final String yamlContent) {
        Map<String, Map<String, Object>> yamlDataSources = YamlEngine.unmarshal(yamlContent, Map.class);
        if (yamlDataSources.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(yamlDataSources.size());
        yamlDataSources.forEach((key, value) -> result.put(key, new YamlDataSourceConfigurationSwapper().swapToDataSourceConfiguration(value)));
        return result;
    }
    
    @Override
    public boolean isExisted(final String schemaName) {
        return !Strings.isNullOrEmpty(repository.get(SchemaMetadataNode.getMetadataDataSourcePath(schemaName)));
    }
    
    /**
     * Update data source configurations for add.
     *
     * @param event data source added event
     */
    @Subscribe
    public void update(final DataSourceAddedSQLNotificationEvent event) {
        Map<String, DataSourceConfiguration> dataSourceConfigs = load(event.getSchemaName());
        dataSourceConfigs.putAll(event.getDataSourceConfigurations());
        persist(event.getSchemaName(), dataSourceConfigs);
    }
    
    /**
     * Update data source configurations for drop.
     *
     * @param event data source dropped event
     */
    @Subscribe
    public void update(final DataSourceDroppedSQLNotificationEvent event) {
        Map<String, DataSourceConfiguration> dataSourceConfigs = load(event.getSchemaName());
        for (String each : event.getDataSourceNames()) {
            dataSourceConfigs.remove(each);
        }
        persist(event.getSchemaName(), dataSourceConfigs);
    }
}
