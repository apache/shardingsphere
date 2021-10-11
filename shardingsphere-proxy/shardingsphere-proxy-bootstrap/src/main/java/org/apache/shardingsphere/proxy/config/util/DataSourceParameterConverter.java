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

package org.apache.shardingsphere.proxy.config.util;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Data source parameter converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceParameterConverter {
    
    /**
     * Get data source parameter map from YAML configuration.
     *
     * @param dataSourceParameters yaml data source parameters
     * @return data source parameter map
     */
    public static Map<String, DataSourceParameter> getDataSourceParameterMapFromYamlConfiguration(final Map<String, YamlDataSourceParameter> dataSourceParameters) {
        return dataSourceParameters.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> createDataSourceParameter(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static DataSourceParameter createDataSourceParameter(final YamlDataSourceParameter yamlDataSourceParameter) {
        DataSourceParameter result = new DataSourceParameter();
        result.setConnectionTimeoutMilliseconds(yamlDataSourceParameter.getConnectionTimeoutMilliseconds());
        result.setIdleTimeoutMilliseconds(yamlDataSourceParameter.getIdleTimeoutMilliseconds());
        result.setMaxLifetimeMilliseconds(yamlDataSourceParameter.getMaxLifetimeMilliseconds());
        result.setMaxPoolSize(yamlDataSourceParameter.getMaxPoolSize());
        result.setMinPoolSize(yamlDataSourceParameter.getMinPoolSize());
        result.setUsername(yamlDataSourceParameter.getUsername());
        result.setPassword(yamlDataSourceParameter.getPassword());
        result.setReadOnly(yamlDataSourceParameter.isReadOnly());
        result.setUrl(yamlDataSourceParameter.getUrl());
        if (null != yamlDataSourceParameter.getCustomPoolProps()) {
            result.setCustomPoolProps(yamlDataSourceParameter.getCustomPoolProps());
        }
        return result;
    }
    
    /**
     * Get data source configuration map.
     *
     * @param dataSourceParameterMap data source parameter map
     * @return data source configuration map
     */
    public static Map<String, DataSourceConfiguration> getDataSourceConfigurationMap(final Map<String, DataSourceParameter> dataSourceParameterMap) {
        return dataSourceParameterMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> createDataSourceConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static DataSourceConfiguration createDataSourceConfiguration(final DataSourceParameter dataSourceParameter) {
        DataSourceConfiguration result = new DataSourceConfiguration(HikariDataSource.class.getName());
        result.getProps().put("jdbcUrl", dataSourceParameter.getUrl());
        result.getProps().put("username", dataSourceParameter.getUsername());
        result.getProps().put("password", dataSourceParameter.getPassword());
        result.getProps().put("connectionTimeout", dataSourceParameter.getConnectionTimeoutMilliseconds());
        result.getProps().put("idleTimeout", dataSourceParameter.getIdleTimeoutMilliseconds());
        result.getProps().put("maxLifetime", dataSourceParameter.getMaxLifetimeMilliseconds());
        result.getProps().put("maximumPoolSize", dataSourceParameter.getMaxPoolSize());
        result.getProps().put("minimumIdle", dataSourceParameter.getMinPoolSize());
        result.getProps().put("readOnly", dataSourceParameter.isReadOnly());
        if (null != dataSourceParameter.getCustomPoolProps()) {
            result.getCustomPoolProps().putAll(dataSourceParameter.getCustomPoolProps());
        }
        return result;
    }
}
