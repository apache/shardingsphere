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

package org.apache.shardingsphere.proxy.config.resource;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
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
     * Get resource configuration map from YAML configuration.
     *
     * @param dataSourceParameters yaml data source parameters
     * @return data source parameter map
     */
    public static Map<String, ResourceConfiguration> getResourceConfigurationMapFromYamlConfiguration(final Map<String, YamlDataSourceParameter> dataSourceParameters) {
        return dataSourceParameters.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> createResourceConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static ResourceConfiguration createResourceConfiguration(final YamlDataSourceParameter yamlDataSourceParameter) {
        ConnectionConfiguration connectionConfig = new ConnectionConfiguration(yamlDataSourceParameter.getUrl(), yamlDataSourceParameter.getUsername(), yamlDataSourceParameter.getPassword());
        PoolConfiguration poolConfig = new PoolConfiguration(yamlDataSourceParameter.getConnectionTimeoutMilliseconds(), yamlDataSourceParameter.getIdleTimeoutMilliseconds(), 
                yamlDataSourceParameter.getMaxLifetimeMilliseconds(), yamlDataSourceParameter.getMaxPoolSize(), yamlDataSourceParameter.getMinPoolSize(), yamlDataSourceParameter.getReadOnly(), 
                yamlDataSourceParameter.getCustomPoolProps());
        return new ResourceConfiguration(connectionConfig, poolConfig);
    }
    
    /**
     * Get data source configuration map.
     *
     * @param resourceConfigMap resource configuration map
     * @return data source configuration map
     */
    public static Map<String, DataSourceConfiguration> getDataSourceConfigurationMap(final Map<String, ResourceConfiguration> resourceConfigMap) {
        return resourceConfigMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> createDataSourceConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static DataSourceConfiguration createDataSourceConfiguration(final ResourceConfiguration resourceConfig) {
        DataSourceConfiguration result = new DataSourceConfiguration(HikariDataSource.class.getName());
        result.getProps().put("jdbcUrl", resourceConfig.getConnection().getUrl());
        result.getProps().put("username", resourceConfig.getConnection().getUsername());
        result.getProps().put("password", resourceConfig.getConnection().getPassword());
        result.getProps().put("connectionTimeout", resourceConfig.getPool().getConnectionTimeoutMilliseconds());
        result.getProps().put("idleTimeout", resourceConfig.getPool().getIdleTimeoutMilliseconds());
        result.getProps().put("maxLifetime", resourceConfig.getPool().getMaxLifetimeMilliseconds());
        result.getProps().put("maximumPoolSize", resourceConfig.getPool().getMaxPoolSize());
        result.getProps().put("minimumIdle", resourceConfig.getPool().getMinPoolSize());
        result.getProps().put("readOnly", resourceConfig.getPool().getReadOnly());
        if (null != resourceConfig.getPool().getCustomProperties()) {
            result.getCustomPoolProps().putAll(resourceConfig.getPool().getCustomProperties());
        }
        return result;
    }
}
