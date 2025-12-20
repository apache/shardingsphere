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

package org.apache.shardingsphere.infra.yaml.config.swapper.resource;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
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
    
    private static final String CUSTOM_POOL_PROPS_KEY = "customPoolProps";
    
    /**
     * Swap to data sources from YAML data sources.
     *
     * @param yamlDataSources YAML data sources map
     * @return data sources
     */
    public Map<String, DataSource> swapToDataSources(final Map<String, Map<String, Object>> yamlDataSources) {
        return swapToDataSources(yamlDataSources, true);
    }
    
    /**
     * Swap to data sources from YAML data sources.
     *
     * @param yamlDataSources YAML data sources map
     * @param cacheEnabled cache enabled
     * @return data sources
     */
    public Map<String, DataSource> swapToDataSources(final Map<String, Map<String, Object>> yamlDataSources, final boolean cacheEnabled) {
        return DataSourcePoolCreator.create(yamlDataSources.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> swapToDataSourcePoolProperties(entry.getValue()))), cacheEnabled);
    }
    
    /**
     * Get data source pool properties.
     *
     * @param yamlRootConfig YAML root configuration
     * @return data source name to data source pool properties map
     */
    public Map<String, DataSourcePoolProperties> getDataSourcePoolPropertiesMap(final YamlRootConfiguration yamlRootConfig) {
        Map<String, Map<String, Object>> yamlDataSourceConfigs = yamlRootConfig.getDataSources();
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(yamlDataSourceConfigs.size(), 1F);
        yamlDataSourceConfigs.forEach((key, value) -> result.put(key, swapToDataSourcePoolProperties(value)));
        return result;
    }
    
    /**
     * Swap to data source pool properties.
     *
     * @param yamlConfig YAML configurations
     * @return data source pool properties
     */
    public DataSourcePoolProperties swapToDataSourcePoolProperties(final Map<String, Object> yamlConfig) {
        Preconditions.checkState(yamlConfig.containsKey(DATA_SOURCE_CLASS_NAME_KEY), "%s can not be null.", DATA_SOURCE_CLASS_NAME_KEY);
        return new DataSourcePoolProperties(yamlConfig.get(DATA_SOURCE_CLASS_NAME_KEY).toString(), getProperties(yamlConfig));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<String, Object> getProperties(final Map<String, Object> yamlConfig) {
        Map<String, Object> result = new HashMap<>(yamlConfig);
        if ("com.zaxxer.hikari.HikariDataSource".equals(result.get(DATA_SOURCE_CLASS_NAME_KEY).toString())) {
            result.remove(DATA_SOURCE_CLASS_NAME_KEY);
        }
        if (null != yamlConfig.get(CUSTOM_POOL_PROPS_KEY)) {
            result.putAll((Map) yamlConfig.get(CUSTOM_POOL_PROPS_KEY));
        }
        result.remove(CUSTOM_POOL_PROPS_KEY);
        return result;
    }
    
    /**
     * Swap to map from data source properties.
     *
     * @param props data source pool properties
     * @return data source map
     */
    public Map<String, Object> swapToMap(final DataSourcePoolProperties props) {
        Map<String, Object> result = new HashMap<>(props.getAllStandardProperties());
        result.put(DATA_SOURCE_CLASS_NAME_KEY, props.getPoolClassName());
        return result;
    }
}
