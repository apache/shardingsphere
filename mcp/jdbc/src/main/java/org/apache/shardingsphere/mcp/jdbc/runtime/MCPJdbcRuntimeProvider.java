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

package org.apache.shardingsphere.mcp.jdbc.runtime;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.jdbc.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.jdbc.config.yaml.config.YamlRuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.jdbc.config.yaml.swapper.YamlRuntimeDatabaseConfigurationSwapper;
import org.apache.shardingsphere.mcp.runtime.MCPRuntimeProvider;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * JDBC runtime provider.
 */
public final class MCPJdbcRuntimeProvider implements MCPRuntimeProvider {
    
    private static final Set<String> SUPPORTED_RUNTIME_DATABASE_PROPERTIES = Set.of("databaseType", "jdbcUrl", "username", "password", "driverClassName");
    
    private final MCPJdbcRuntimeContextFactory runtimeContextFactory = new MCPJdbcRuntimeContextFactory();
    
    private final YamlRuntimeDatabaseConfigurationSwapper runtimeDatabaseConfigSwapper = new YamlRuntimeDatabaseConfigurationSwapper();
    
    @Override
    public String getType() {
        return "JDBC";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
    
    @Override
    public MCPRuntimeContext createRuntimeContext(final MCPSessionManager sessionManager, final Object runtimeConfiguration) {
        return runtimeContextFactory.create(sessionManager, castRuntimeDatabases(runtimeConfiguration));
    }
    
    @Override
    public Object swapToObject(final Map<String, Map<String, Object>> yamlRuntimeConfiguration) {
        ShardingSpherePreconditions.checkNotNull(yamlRuntimeConfiguration, () -> new IllegalArgumentException("Runtime configuration cannot be null."));
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(yamlRuntimeConfiguration.size(), 1F);
        for (Entry<String, Map<String, Object>> entry : yamlRuntimeConfiguration.entrySet()) {
            validateYamlRuntimeDatabaseConfiguration(entry.getValue());
            result.put(entry.getKey(), runtimeDatabaseConfigSwapper.swapToObject(createYamlRuntimeDatabaseConfiguration(entry.getValue())));
        }
        return result;
    }
    
    @Override
    public Map<String, Map<String, Object>> swapToYamlConfiguration(final Object runtimeConfiguration) {
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = castRuntimeDatabases(runtimeConfiguration);
        Map<String, Map<String, Object>> result = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
        for (Entry<String, RuntimeDatabaseConfiguration> entry : runtimeDatabases.entrySet()) {
            ShardingSpherePreconditions.checkNotNull(entry.getValue(), () -> new IllegalArgumentException("Runtime database configuration cannot be null."));
            result.put(entry.getKey(), createYamlRuntimeDatabaseConfiguration(runtimeDatabaseConfigSwapper.swapToYamlConfiguration(entry.getValue())));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, RuntimeDatabaseConfiguration> castRuntimeDatabases(final Object runtimeConfiguration) {
        ShardingSpherePreconditions.checkState(runtimeConfiguration instanceof Map, () -> new IllegalArgumentException("Runtime configuration must be a map of runtime databases."));
        return (Map<String, RuntimeDatabaseConfiguration>) runtimeConfiguration;
    }
    
    private YamlRuntimeDatabaseConfiguration createYamlRuntimeDatabaseConfiguration(final Map<String, Object> yamlProperties) {
        YamlRuntimeDatabaseConfiguration result = new YamlRuntimeDatabaseConfiguration();
        result.setDatabaseType(getYamlText(yamlProperties, "databaseType"));
        result.setJdbcUrl(getYamlText(yamlProperties, "jdbcUrl"));
        result.setUsername(getYamlText(yamlProperties, "username"));
        result.setPassword(getYamlText(yamlProperties, "password"));
        result.setDriverClassName(getYamlText(yamlProperties, "driverClassName"));
        return result;
    }
    
    private Map<String, Object> createYamlRuntimeDatabaseConfiguration(final YamlRuntimeDatabaseConfiguration yamlRuntimeDatabaseConfig) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("databaseType", yamlRuntimeDatabaseConfig.getDatabaseType());
        result.put("jdbcUrl", yamlRuntimeDatabaseConfig.getJdbcUrl());
        result.put("username", yamlRuntimeDatabaseConfig.getUsername());
        result.put("password", yamlRuntimeDatabaseConfig.getPassword());
        result.put("driverClassName", yamlRuntimeDatabaseConfig.getDriverClassName());
        return result;
    }
    
    private void validateYamlRuntimeDatabaseConfiguration(final Map<String, Object> yamlProperties) {
        ShardingSpherePreconditions.checkNotNull(yamlProperties, () -> new IllegalArgumentException("Runtime database configuration cannot be null."));
        for (String each : yamlProperties.keySet()) {
            ShardingSpherePreconditions.checkState(SUPPORTED_RUNTIME_DATABASE_PROPERTIES.contains(each),
                    () -> new IllegalArgumentException(String.format("Unsupported runtime database property `%s`.", each)));
        }
    }
    
    private String getYamlText(final Map<String, Object> yamlProperties, final String key) {
        Object result = yamlProperties.get(key);
        return null == result ? null : result.toString();
    }
}
