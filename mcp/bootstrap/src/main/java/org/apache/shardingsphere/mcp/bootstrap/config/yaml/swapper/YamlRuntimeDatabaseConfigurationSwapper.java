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
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeDatabaseConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * YAML runtime database configuration swapper.
 */
public final class YamlRuntimeDatabaseConfigurationSwapper implements YamlConfigurationSwapper<YamlRuntimeDatabaseConfiguration, RuntimeDatabaseConfiguration> {
    
    @Override
    public YamlRuntimeDatabaseConfiguration swapToYamlConfiguration(final RuntimeDatabaseConfiguration data) {
        YamlRuntimeDatabaseConfiguration result = new YamlRuntimeDatabaseConfiguration();
        result.setDatabaseType(data.getDatabaseType());
        result.setJdbcUrl(data.getJdbcUrl());
        result.setUsername(data.getUsername());
        result.setPassword(data.getPassword());
        result.setDriverClassName(data.getDriverClassName());
        result.setSchemaPattern(data.getSchemaPattern());
        result.setDefaultSchema(data.getDefaultSchema());
        result.setSupportsCrossSchemaSql(data.isSupportsCrossSchemaSql());
        result.setSupportsExplainAnalyze(data.isSupportsExplainAnalyze());
        return result;
    }
    
    @Override
    public RuntimeDatabaseConfiguration swapToObject(final YamlRuntimeDatabaseConfiguration yamlConfig) {
        return swapToObject("", yamlConfig, new LinkedHashMap<>());
    }
    
    RuntimeDatabaseConfiguration swapToObject(final String databaseName, final YamlRuntimeDatabaseConfiguration yamlConfig, final Map<String, String> runtimeDefaults) {
        YamlRuntimeDatabaseConfiguration actualYamlConfig = null == yamlConfig ? new YamlRuntimeDatabaseConfiguration() : yamlConfig;
        Map<String, String> actualRuntimeDefaults = null == runtimeDefaults ? new LinkedHashMap<>() : runtimeDefaults;
        return new RuntimeDatabaseConfiguration(resolveRequiredText(actualYamlConfig.getDatabaseType(), actualRuntimeDefaults, "databaseType", databaseName),
                resolveRequiredText(actualYamlConfig.getJdbcUrl(), actualRuntimeDefaults, "jdbcUrl", databaseName),
                resolveText(actualYamlConfig.getUsername(), actualRuntimeDefaults, "username"),
                resolveText(actualYamlConfig.getPassword(), actualRuntimeDefaults, "password"),
                resolveText(actualYamlConfig.getDriverClassName(), actualRuntimeDefaults, "driverClassName"),
                resolveText(actualYamlConfig.getSchemaPattern(), actualRuntimeDefaults, "schemaPattern"),
                resolveText(actualYamlConfig.getDefaultSchema(), actualRuntimeDefaults, "defaultSchema"),
                resolveBoolean(actualYamlConfig.getSupportsCrossSchemaSql(), actualRuntimeDefaults, "supportsCrossSchemaSql"),
                resolveBoolean(actualYamlConfig.getSupportsExplainAnalyze(), actualRuntimeDefaults, "supportsExplainAnalyze"));
    }
    
    private String resolveRequiredText(final String value, final Map<String, String> runtimeDefaults, final String fieldName, final String databaseName) {
        String result = resolveText(value, runtimeDefaults, fieldName);
        ShardingSpherePreconditions.checkState(!result.isEmpty(),
                () -> new IllegalArgumentException(formatRequiredMessage(databaseName, fieldName)));
        return result;
    }
    
    private String resolveText(final String value, final Map<String, String> runtimeDefaults, final String fieldName) {
        String result = normalizeText(value);
        return result.isEmpty() ? normalizeText(runtimeDefaults.get(fieldName)) : result;
    }
    
    private boolean resolveBoolean(final Boolean value, final Map<String, String> runtimeDefaults, final String fieldName) {
        if (null != value) {
            return value;
        }
        return Boolean.parseBoolean(normalizeText(runtimeDefaults.get(fieldName)));
    }
    
    private String normalizeText(final Object value) {
        return null == value ? "" : String.valueOf(value).trim();
    }
    
    private String formatRequiredMessage(final String databaseName, final String fieldName) {
        return databaseName.isEmpty() ? String.format("Runtime database property `%s` is required.", fieldName)
                : String.format("Runtime database `%s` property `%s` is required.", databaseName, fieldName);
    }
}
