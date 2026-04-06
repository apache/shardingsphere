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

package org.apache.shardingsphere.mcp.bootstrap.config.yaml.swapper;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * YAML runtime databases configuration swapper.
 */
public final class YamlRuntimeDatabaseConfigurationsSwapper {
    
    private final YamlRuntimeDatabaseConfigurationSwapper runtimeDatabaseConfigSwapper = new YamlRuntimeDatabaseConfigurationSwapper();
    
    /**
     * Swap YAML runtime configuration to runtime configuration object.
     *
     * @param yamlRuntimeConfiguration YAML runtime configuration
     * @return runtime configuration object
     */
    public Map<String, RuntimeDatabaseConfiguration> swapToObject(final Map<String, Map<String, Object>> yamlRuntimeConfiguration) {
        if (null == yamlRuntimeConfiguration) {
            return Collections.emptyMap();
        }
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(yamlRuntimeConfiguration.size(), 1F);
        for (Entry<String, Map<String, Object>> entry : yamlRuntimeConfiguration.entrySet()) {
            validateYamlRuntimeDatabaseConfiguration(entry.getValue());
            result.put(entry.getKey(), runtimeDatabaseConfigSwapper.swapToObject(createYamlRuntimeDatabaseConfiguration(entry.getValue())));
        }
        return result;
    }
    
    /**
     * Swap runtime configuration object to YAML runtime configuration.
     *
     * @param runtimeConfiguration runtime configuration
     * @return YAML runtime configuration
     */
    public Map<String, Map<String, Object>> swapToYamlConfiguration(final Map<String, RuntimeDatabaseConfiguration> runtimeConfiguration) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>(runtimeConfiguration.size(), 1F);
        for (Entry<String, RuntimeDatabaseConfiguration> entry : runtimeConfiguration.entrySet()) {
            ShardingSpherePreconditions.checkNotNull(entry.getValue(), () -> new IllegalArgumentException("Runtime database configuration cannot be null."));
            result.put(entry.getKey(), createYamlRuntimeDatabaseConfiguration(runtimeDatabaseConfigSwapper.swapToYamlConfiguration(entry.getValue())));
        }
        return result;
    }
    
    private YamlRuntimeDatabaseConfiguration createYamlRuntimeDatabaseConfiguration(final Map<String, Object> yamlProperties) {
        if (null == yamlProperties) {
            return null;
        }
        YamlRuntimeDatabaseConfiguration result = new YamlRuntimeDatabaseConfiguration();
        for (RuntimeDatabaseProperty each : RuntimeDatabaseProperty.values()) {
            each.setter.setValue(result, getYamlText(yamlProperties, each.key));
        }
        return result;
    }
    
    private Map<String, Object> createYamlRuntimeDatabaseConfiguration(final YamlRuntimeDatabaseConfiguration yamlRuntimeDatabaseConfig) {
        Map<String, Object> result = new LinkedHashMap<>(RuntimeDatabaseProperty.values().length, 1F);
        for (RuntimeDatabaseProperty each : RuntimeDatabaseProperty.values()) {
            result.put(each.key, each.getter.getValue(yamlRuntimeDatabaseConfig));
        }
        return result;
    }
    
    private void validateYamlRuntimeDatabaseConfiguration(final Map<String, Object> yamlProperties) {
        if (null == yamlProperties) {
            return;
        }
        for (String each : yamlProperties.keySet()) {
            ShardingSpherePreconditions.checkState(RuntimeDatabaseProperty.contains(each),
                    () -> new IllegalArgumentException(String.format("Unsupported runtime database property `%s`.", each)));
        }
    }
    
    private String getYamlText(final Map<String, Object> yamlProperties, final String key) {
        Object result = yamlProperties.get(key);
        return null == result ? null : result.toString();
    }
    
    private enum RuntimeDatabaseProperty {
        
        DATABASE_TYPE("databaseType", YamlRuntimeDatabaseConfiguration::setDatabaseType, YamlRuntimeDatabaseConfiguration::getDatabaseType),
        
        JDBC_URL("jdbcUrl", YamlRuntimeDatabaseConfiguration::setJdbcUrl, YamlRuntimeDatabaseConfiguration::getJdbcUrl),
        
        USERNAME("username", YamlRuntimeDatabaseConfiguration::setUsername, YamlRuntimeDatabaseConfiguration::getUsername),
        
        PASSWORD("password", YamlRuntimeDatabaseConfiguration::setPassword, YamlRuntimeDatabaseConfiguration::getPassword),
        
        DRIVER_CLASS_NAME("driverClassName", YamlRuntimeDatabaseConfiguration::setDriverClassName, YamlRuntimeDatabaseConfiguration::getDriverClassName);
        
        private final String key;
        
        private final YamlRuntimeDatabasePropertySetter setter;
        
        private final YamlRuntimeDatabasePropertyGetter getter;
        
        RuntimeDatabaseProperty(final String key, final YamlRuntimeDatabasePropertySetter setter, final YamlRuntimeDatabasePropertyGetter getter) {
            this.key = key;
            this.setter = setter;
            this.getter = getter;
        }
        
        private static boolean contains(final String key) {
            for (RuntimeDatabaseProperty each : values()) {
                if (each.key.equals(key)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    @FunctionalInterface
    private interface YamlRuntimeDatabasePropertySetter {
        
        void setValue(YamlRuntimeDatabaseConfiguration yamlRuntimeDatabaseConfiguration, String value);
    }
    
    @FunctionalInterface
    private interface YamlRuntimeDatabasePropertyGetter {
        
        String getValue(YamlRuntimeDatabaseConfiguration yamlRuntimeDatabaseConfiguration);
    }
}
