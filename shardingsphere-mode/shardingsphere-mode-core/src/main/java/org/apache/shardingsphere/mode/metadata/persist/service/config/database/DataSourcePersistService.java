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

package org.apache.shardingsphere.mode.metadata.persist.service.config.database;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Data source persist service.
 */
@RequiredArgsConstructor
public final class DataSourcePersistService implements DatabaseBasedPersistService<Map<String, DataSourceProperties>> {
    
    private static final String DEFAULT_VERSION = "0";
    
    private final PersistRepository repository;
    
    @Override
    public void conditionalPersist(final String databaseName, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        if (!dataSourcePropsMap.isEmpty() && !isExisted(databaseName)) {
            persist(databaseName, dataSourcePropsMap);
        }
    }
    
    @Override
    public void persist(final String databaseName, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        if (Strings.isNullOrEmpty(getDatabaseActiveVersion(databaseName))) {
            repository.persist(DatabaseMetaDataNode.getActiveVersionPath(databaseName), DEFAULT_VERSION);
        }
        repository.persist(DatabaseMetaDataNode.getMetaDataDataSourcePath(databaseName, getDatabaseActiveVersion(databaseName)),
                YamlEngine.marshal(swapYamlDataSourceConfiguration(dataSourcePropsMap)));
    }
    
    @Override
    public void persist(final String databaseName, final String version, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        repository.persist(DatabaseMetaDataNode.getMetaDataDataSourcePath(databaseName, version), YamlEngine.marshal(swapYamlDataSourceConfiguration(dataSourcePropsMap)));
    }
    
    private Map<String, Map<String, Object>> swapYamlDataSourceConfiguration(final Map<String, DataSourceProperties> dataSourcePropsMap) {
        return dataSourcePropsMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> new YamlDataSourceConfigurationSwapper().swapToMap(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    @Override
    public Map<String, DataSourceProperties> load(final String databaseName) {
        return isExisted(databaseName) ? getDataSourceProperties(repository.get(
                DatabaseMetaDataNode.getMetaDataDataSourcePath(databaseName, getDatabaseActiveVersion(databaseName)))) : new LinkedHashMap<>();
    }
    
    @Override
    public Map<String, DataSourceProperties> load(final String databaseName, final String version) {
        String yamlContent = repository.get(DatabaseMetaDataNode.getMetaDataDataSourcePath(databaseName, version));
        return Strings.isNullOrEmpty(yamlContent) ? new LinkedHashMap<>() : getDataSourceProperties(yamlContent);
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
    public boolean isExisted(final String databaseName) {
        return !Strings.isNullOrEmpty(getDatabaseActiveVersion(databaseName)) && !Strings.isNullOrEmpty(repository.get(DatabaseMetaDataNode.getMetaDataDataSourcePath(databaseName,
                getDatabaseActiveVersion(databaseName))));
    }
    
    /**
     * Append data source properties map.
     * 
     * @param databaseName database name
     * @param toBeAppendedDataSourcePropsMap data source properties map to be appended
     */
    public void append(final String databaseName, final Map<String, DataSourceProperties> toBeAppendedDataSourcePropsMap) {
        Map<String, DataSourceProperties> dataSourceConfigs = load(databaseName);
        dataSourceConfigs.putAll(toBeAppendedDataSourcePropsMap);
        persist(databaseName, dataSourceConfigs);
    }
    
    private String getDatabaseActiveVersion(final String databaseName) {
        return repository.get(DatabaseMetaDataNode.getActiveVersionPath(databaseName));
    }
}
