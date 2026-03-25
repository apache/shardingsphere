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
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeDatabaseConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * YAML MCP launch configuration swapper.
 */
public final class YamlMCPLaunchConfigurationSwapper implements YamlConfigurationSwapper<YamlMCPLaunchConfiguration, MCPLaunchConfiguration> {
    
    private static final String TRANSPORT_VALIDATION_ERROR_MESSAGE = "At least one transport must be explicitly enabled. Set `transport.http.enabled` or `transport.stdio.enabled` to true.";
    
    private final YamlMCPTransportConfigurationSwapper transportConfigSwapper = new YamlMCPTransportConfigurationSwapper();
    
    private final YamlRuntimeDatabaseConfigurationSwapper runtimeDatabaseConfigSwapper = new YamlRuntimeDatabaseConfigurationSwapper();
    
    @Override
    public YamlMCPLaunchConfiguration swapToYamlConfiguration(final MCPLaunchConfiguration data) {
        YamlMCPLaunchConfiguration result = new YamlMCPLaunchConfiguration();
        result.setTransport(transportConfigSwapper.swapToYamlConfiguration(data.getTransport()));
        result.setRuntimeDatabases(swapToYamlRuntimeDatabases(data.getRuntimeDatabases()));
        return result;
    }
    
    private Map<String, YamlRuntimeDatabaseConfiguration> swapToYamlRuntimeDatabases(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        Map<String, YamlRuntimeDatabaseConfiguration> result = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
        for (Entry<String, RuntimeDatabaseConfiguration> entry : runtimeDatabases.entrySet()) {
            result.put(entry.getKey(), runtimeDatabaseConfigSwapper.swapToYamlConfiguration(entry.getValue()));
        }
        return result;
    }
    
    /**
     * Swap from YAML content to launch configuration.
     *
     * @param yamlContent YAML content
     * @return launch configuration
     */
    public MCPLaunchConfiguration swapToObject(final String yamlContent) {
        Map<String, Object> yamlRoot = loadYamlRoot(yamlContent);
        validateRootSections(yamlRoot);
        YamlMCPLaunchConfiguration yamlConfig = YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class, true);
        return swapToObject(yamlConfig);
    }
    
    @Override
    public MCPLaunchConfiguration swapToObject(final YamlMCPLaunchConfiguration yamlConfig) {
        YamlMCPLaunchConfiguration actualYamlConfig = null == yamlConfig ? new YamlMCPLaunchConfiguration() : yamlConfig;
        MCPTransportConfiguration transportConfig = transportConfigSwapper.swapToObject(actualYamlConfig.getTransport());
        validateTransportConfiguration(transportConfig);
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = swapToRuntimeDatabases(actualYamlConfig.getRuntimeDatabases());
        return new MCPLaunchConfiguration(transportConfig, runtimeDatabases);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadYamlRoot(final String yamlContent) {
        Object yamlRoot = new Yaml().load(yamlContent);
        return yamlRoot instanceof Map ? (Map<String, Object>) yamlRoot : Collections.emptyMap();
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getConfiguredSection(final Map<String, Object> yamlRoot, final String sectionName) {
        Object section = yamlRoot.get(sectionName);
        return section instanceof Map ? (Map<String, Object>) section : Collections.emptyMap();
    }
    
    private void validateRootSections(final Map<String, Object> yamlRoot) {
        validateLegacyRuntimeSection(getConfiguredSection(yamlRoot, "runtime"));
    }
    
    private void validateLegacyRuntimeSection(final Map<String, Object> runtimeSection) {
        if (runtimeSection.isEmpty()) {
            return;
        }
        if (runtimeSection.containsKey("props")) {
            throw new IllegalArgumentException("`runtime.props` is no longer supported. Configure direct runtime databases with `runtimeDatabases`.");
        }
        if (runtimeSection.containsKey("defaults")) {
            throw new IllegalArgumentException("`runtime.defaults` is no longer supported. Configure direct runtime databases with `runtimeDatabases`.");
        }
        if (runtimeSection.containsKey("databaseDefaults")) {
            throw new IllegalArgumentException("`runtime.databaseDefaults` is no longer supported. Configure each runtime database explicitly under `runtimeDatabases`.");
        }
        if (runtimeSection.containsKey("databases")) {
            throw new IllegalArgumentException("`runtime.databases` is no longer supported. Configure direct runtime databases with `runtimeDatabases`.");
        }
    }
    
    private void validateTransportConfiguration(final MCPTransportConfiguration transportConfig) {
        if (!transportConfig.hasEnabledTransport()) {
            throw new IllegalArgumentException(TRANSPORT_VALIDATION_ERROR_MESSAGE);
        }
    }
    
    private Map<String, RuntimeDatabaseConfiguration> swapToRuntimeDatabases(final Map<String, YamlRuntimeDatabaseConfiguration> yamlRuntimeDatabases) {
        Map<String, YamlRuntimeDatabaseConfiguration> actualYamlRuntimeDatabases = null == yamlRuntimeDatabases ? new LinkedHashMap<>() : yamlRuntimeDatabases;
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(actualYamlRuntimeDatabases.size(), 1F);
        for (Entry<String, YamlRuntimeDatabaseConfiguration> entry : actualYamlRuntimeDatabases.entrySet()) {
            String databaseName = normalizeText(entry.getKey());
            ShardingSpherePreconditions.checkState(!databaseName.isEmpty(), () -> new IllegalArgumentException("Runtime logical database name cannot be blank."));
            ShardingSpherePreconditions.checkState(!result.containsKey(databaseName), () -> new IllegalArgumentException(String.format("Runtime logical database `%s` is duplicated.", databaseName)));
            validateRuntimeDatabaseConfiguration(databaseName, entry.getValue());
            result.put(databaseName, runtimeDatabaseConfigSwapper.swapToObject(entry.getValue()));
        }
        return result;
    }
    
    private void validateRuntimeDatabaseConfiguration(final String databaseName, final YamlRuntimeDatabaseConfiguration yamlConfig) {
        YamlRuntimeDatabaseConfiguration actualYamlConfig = null == yamlConfig ? new YamlRuntimeDatabaseConfiguration() : yamlConfig;
        validateRequiredText(actualYamlConfig.getDatabaseType(), "databaseType", databaseName);
        validateRequiredText(actualYamlConfig.getJdbcUrl(), "jdbcUrl", databaseName);
    }
    
    private void validateRequiredText(final String value, final String fieldName, final String databaseName) {
        ShardingSpherePreconditions.checkState(!normalizeText(value).isEmpty(),
                () -> new IllegalArgumentException(String.format("Runtime database `%s` property `%s` is required.", databaseName, fieldName)));
    }
    
    private String normalizeText(final Object value) {
        return null == value ? "" : String.valueOf(value).trim();
    }
}
