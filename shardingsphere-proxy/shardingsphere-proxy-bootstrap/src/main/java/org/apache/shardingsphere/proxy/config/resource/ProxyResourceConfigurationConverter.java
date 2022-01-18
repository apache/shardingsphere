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
import org.apache.shardingsphere.infra.config.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyResourceConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * ShardingSphere-Proxy resource configuration converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyResourceConfigurationConverter {
    
    /**
     * Get resource configuration map.
     *
     * @param yamlResourceConfigMap yaml resource configuration map
     * @return resource configuration map
     */
    public static Map<String, ProxyResourceConfiguration> getResourceConfigurationMap(final Map<String, YamlProxyResourceConfiguration> yamlResourceConfigMap) {
        return yamlResourceConfigMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> createResourceConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static ProxyResourceConfiguration createResourceConfiguration(final YamlProxyResourceConfiguration yamlConfig) {
        ConnectionConfiguration connectionConfig = new ConnectionConfiguration(yamlConfig.getUrl(), yamlConfig.getUsername(), yamlConfig.getPassword());
        PoolConfiguration poolConfig = new PoolConfiguration(yamlConfig.getConnectionTimeoutMilliseconds(), yamlConfig.getIdleTimeoutMilliseconds(), 
                yamlConfig.getMaxLifetimeMilliseconds(), yamlConfig.getMaxPoolSize(), yamlConfig.getMinPoolSize(), yamlConfig.getReadOnly(), 
                yamlConfig.getCustomPoolProps());
        return new ProxyResourceConfiguration(connectionConfig, poolConfig);
    }
    
    /**
     * Get data source configuration map.
     *
     * @param resourceConfigMap resource configuration map
     * @return data source properties map
     */
    public static Map<String, DataSourceProperties> getDataSourceConfigurationMap(final Map<String, ProxyResourceConfiguration> resourceConfigMap) {
        return resourceConfigMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, 
            entry -> new DataSourceProperties(HikariDataSource.class.getName(), createProperties(entry.getValue())), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, Object> createProperties(final ProxyResourceConfiguration resourceConfig) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("jdbcUrl", resourceConfig.getConnection().getUrl());
        result.put("username", resourceConfig.getConnection().getUsername());
        result.put("password", resourceConfig.getConnection().getPassword());
        result.put("connectionTimeout", resourceConfig.getPool().getConnectionTimeoutMilliseconds());
        result.put("idleTimeout", resourceConfig.getPool().getIdleTimeoutMilliseconds());
        result.put("maxLifetime", resourceConfig.getPool().getMaxLifetimeMilliseconds());
        result.put("maximumPoolSize", resourceConfig.getPool().getMaxPoolSize());
        result.put("minimumIdle", resourceConfig.getPool().getMinPoolSize());
        result.put("readOnly", resourceConfig.getPool().getReadOnly());
        if (null != resourceConfig.getPool().getCustomProperties()) {
            result.putAll((Map) resourceConfig.getPool().getCustomProperties());
        }
        return result;
    }
}
