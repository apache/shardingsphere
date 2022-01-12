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

package org.apache.shardingsphere.infra.yaml.config.swapper;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.config.datasource.DataSourceProperties;
import org.apache.shardingsphere.infra.config.datasource.creator.DataSourcePoolCreatorUtil;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * YAML data source configuration swapper.
 */
public final class YamlDataSourceConfigurationSwapper {
    
    private static final String DATA_SOURCE_CLASS_NAME_KEY = "dataSourceClassName";
    
    /**
     * Swap to data sources from YAML data sources.
     *
     * @param yamlDataSources YAML data sources map
     * @return data sources
     */
    public Map<String, DataSource> swapToDataSources(final Map<String, Map<String, Object>> yamlDataSources) {
        return DataSourcePoolCreatorUtil.getDataSourceMap(yamlDataSources.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> swapToDataSourceProperties(entry.getValue()))));
    }
    
    /**
     * Get data source properties.
     *
     * @param yamlRootConfig yaml root configuration
     * @return data source name to data source properties map
     */
    public Map<String, DataSourceProperties> getDataSourcePropertiesMap(final YamlRootConfiguration yamlRootConfig) {
        Map<String, Map<String, Object>> yamlDataSourceConfigs = yamlRootConfig.getDataSources();
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(yamlDataSourceConfigs.size());
        yamlDataSourceConfigs.forEach((key, value) -> result.put(key, swapToDataSourceProperties(value)));
        return result;
    }
    
    /**
     * Swap to data source properties.
     * 
     * @param yamlConfig YAML configurations
     * @return data source properties
     */
    @SuppressWarnings("rawtypes")
    public DataSourceProperties swapToDataSourceProperties(final Map<String, Object> yamlConfig) {
        Preconditions.checkState(yamlConfig.containsKey(DATA_SOURCE_CLASS_NAME_KEY), "%s can not be null.", DATA_SOURCE_CLASS_NAME_KEY);
        Map<String, Object> newDataSourceMap = new HashMap<>(yamlConfig);
        newDataSourceMap.remove(DATA_SOURCE_CLASS_NAME_KEY);
        DataSourceProperties result = new DataSourceProperties(yamlConfig.get(DATA_SOURCE_CLASS_NAME_KEY).toString());
        if (null != newDataSourceMap.get(DataSourceProperties.CUSTOM_POOL_PROPS_KEY)) {
            result.getCustomPoolProps().putAll((Map) newDataSourceMap.get(DataSourceProperties.CUSTOM_POOL_PROPS_KEY));
            newDataSourceMap.remove(DataSourceProperties.CUSTOM_POOL_PROPS_KEY);
        }
        result.getProps().putAll(newDataSourceMap);
        return result;
    }
    
    /**
     * Swap to map from data source properties.
     * 
     * @param dataSourceProps data source properties
     * @return data source map
     */
    public Map<String, Object> swapToMap(final DataSourceProperties dataSourceProps) {
        Map<String, Object> result = new HashMap<>(dataSourceProps.getProps());
        if (!dataSourceProps.getCustomPoolProps().isEmpty()) {
            result.put(DataSourceProperties.CUSTOM_POOL_PROPS_KEY, dataSourceProps.getCustomPoolProps());
        }
        result.put(DATA_SOURCE_CLASS_NAME_KEY, dataSourceProps.getDataSourceClassName());
        return result;
    }
}
