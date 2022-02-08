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

package org.apache.shardingsphere.mode.metadata.persist.service.impl;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.apache.shardingsphere.mode.metadata.persist.node.SchemaMetaDataNode;
import org.apache.shardingsphere.mode.metadata.persist.service.SchemaBasedPersistService;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Data source persist service.
 */
@RequiredArgsConstructor
public final class DataSourcePersistService implements SchemaBasedPersistService<Map<String, DataSourceProperties>> {
    
    private final PersistRepository repository;
    
    @Override
    public void persist(final String schemaName, final Map<String, DataSourceProperties> dataSourcePropsMap, final boolean isOverwrite) {
        if (!dataSourcePropsMap.isEmpty() && (isOverwrite || !isExisted(schemaName))) {
            persist(schemaName, dataSourcePropsMap);
        }
    }
    
    @Override
    public void persist(final String schemaName, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        repository.persist(SchemaMetaDataNode.getMetaDataDataSourcePath(schemaName), YamlEngine.marshal(swapYamlDataSourceConfiguration(dataSourcePropsMap)));
    }
    
    private Map<String, Map<String, Object>> swapYamlDataSourceConfiguration(final Map<String, DataSourceProperties> dataSourcePropsMap) {
        return dataSourcePropsMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> new YamlDataSourceConfigurationSwapper().swapToMap(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    @Override
    public Map<String, DataSourceProperties> load(final String schemaName) {
        return isExisted(schemaName) ? getDataSourceProperties(repository.get(SchemaMetaDataNode.getMetaDataDataSourcePath(schemaName))) : new LinkedHashMap<>();
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, DataSourceProperties> getDataSourceProperties(final String yamlContent) {
        Map<String, Map<String, Object>> yamlDataSources = YamlEngine.unmarshal(yamlContent, Map.class);
        if (yamlDataSources.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(yamlDataSources.size());
        yamlDataSources.forEach((key, value) -> result.put(key, new YamlDataSourceConfigurationSwapper().swapToDataSourceProperties(value)));
        return result;
    }
    
    @Override
    public boolean isExisted(final String schemaName) {
        return !Strings.isNullOrEmpty(repository.get(SchemaMetaDataNode.getMetaDataDataSourcePath(schemaName)));
    }
    
    /**
     * Append data source properties map.
     * 
     * @param schemaName schema name
     * @param toBeAppendedDataSourcePropsMap data source properties map to be appended
     */
    public void append(final String schemaName, final Map<String, DataSourceProperties> toBeAppendedDataSourcePropsMap) {
        Map<String, DataSourceProperties> dataSourceConfigs = load(schemaName);
        dataSourceConfigs.putAll(toBeAppendedDataSourcePropsMap);
        persist(schemaName, dataSourceConfigs);
    }
    
    /**
     * Drop data sources.
     * 
     * @param schemaName schema name
     * @param toBeDroppedDataSourceNames data sources to be dropped
     */
    public void drop(final String schemaName, final Collection<String> toBeDroppedDataSourceNames) {
        Map<String, DataSourceProperties> dataSourcePropsMap = load(schemaName);
        for (String each : toBeDroppedDataSourceNames) {
            dataSourcePropsMap.remove(each);
        }
        persist(schemaName, dataSourcePropsMap);
    }
}
