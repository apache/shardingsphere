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

package org.apache.shardingsphere.infra.yaml.swapper;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.yaml.config.YamlDataSourceConfiguration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Yaml data source configuration swapper.
 */
public final class YamlDataSourceConfigurationSwapper implements YamlConfigurationSwapper<YamlDataSourceConfiguration, DataSourceConfiguration> {
    
    private static final String YAML_DATA_SOURCE_CLASS_NAME = "dataSourceClassName";
    
    @Override
    public YamlDataSourceConfiguration swapToYamlConfiguration(final DataSourceConfiguration config) {
        YamlDataSourceConfiguration result = new YamlDataSourceConfiguration();
        result.setDataSourceClassName(config.getDataSourceClassName());
        result.setProps(config.getProps());
        return result;
    }
    
    @Override
    public DataSourceConfiguration swapToObject(final YamlDataSourceConfiguration yamlConfig) {
        DataSourceConfiguration result = new DataSourceConfiguration(yamlConfig.getDataSourceClassName());
        result.getProps().putAll(yamlConfig.getProps());
        return result;
    }
    
    /**
     * Swap to data sources from YAML data sources.
     *
     * @param yamlDataSources YAML data sources map
     * @return data sources
     */
    public Map<String, DataSource> swapToDataSources(final Map<String, Map<String, Object>> yamlDataSources) {
        Map<String, DataSourceConfiguration> dataSourceConfigMap = yamlDataSources.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> swapToObjectFromMap(entry.getValue())));
        return DataSourceConverter.getDataSourceMap(dataSourceConfigMap);
    }
    
    /**
     * Swap to data source configuration from map.
     * 
     * @param yamlConfig yaml config map
     * @return data source configuration
     */
    public DataSourceConfiguration swapToObjectFromMap(final Map<String, Object> yamlConfig) {
        String dataSourceClassName = Objects.isNull(yamlConfig.get(YAML_DATA_SOURCE_CLASS_NAME)) ? "" : yamlConfig.get(YAML_DATA_SOURCE_CLASS_NAME).toString();
        Preconditions.checkState(!Strings.isNullOrEmpty(dataSourceClassName), String.format("%s can not be null", YAML_DATA_SOURCE_CLASS_NAME));
        Map<String, Object> newDataSourceMap = new HashMap<>(yamlConfig);
        newDataSourceMap.remove(YAML_DATA_SOURCE_CLASS_NAME);
        DataSourceConfiguration result = new DataSourceConfiguration(dataSourceClassName);
        result.getProps().putAll(newDataSourceMap);
        return result;
    }
    
    /**
     * Swap to data sources from YAML data sources.
     *
     * @param yamlDataSources YAML data sources
     * @return data sources
     */
    public Map<String, DataSource> swapToDataSourcesFromYaml(final Map<String, YamlDataSourceConfiguration> yamlDataSources) {
        Map<String, DataSourceConfiguration> dataSourceConfigMap = yamlDataSources.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> swapToObject(entry.getValue())));
        return DataSourceConverter.getDataSourceMap(dataSourceConfigMap);
    }
    
    /**
     * Swap to map from data source configuration.
     * 
     * @param config data source configuration
     * @return data source map
     */
    public Map<String, Object> swapToMap(final DataSourceConfiguration config) {
        Map<String, Object> result = new HashMap<>();
        result.putAll(config.getProps());
        result.put(YAML_DATA_SOURCE_CLASS_NAME, config.getDataSourceClassName());
        return result;
    }
}
