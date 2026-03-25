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
import org.apache.shardingsphere.infra.util.yaml.constructor.ShardingSphereYamlConstructor;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlStdioTransportConfiguration;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * YAML MCP launch configuration swapper.
 */
public final class YamlMCPLaunchConfigurationSwapper implements YamlConfigurationSwapper<YamlMCPLaunchConfiguration, MCPLaunchConfiguration> {
    
    private static final Set<String> ROOT_PROPERTIES = Set.of("transport", "runtimeDatabases");
    
    private static final Set<String> TRANSPORT_PROPERTIES = Set.of("http", "stdio");
    
    private static final Set<String> HTTP_PROPERTIES = Set.of("enabled", "bindHost", "port", "endpointPath");
    
    private static final Set<String> STDIO_PROPERTIES = Set.of("enabled");
    
    private static final Set<String> RUNTIME_DATABASE_PROPERTIES = Set.of("databaseType", "jdbcUrl", "username", "password", "driverClassName");
    
    private static final String NULL_CONFIG_ERROR_MESSAGE = "MCP launch configuration cannot be null.";
    
    private static final String TRANSPORT_VALIDATION_ERROR_MESSAGE = "At least one transport must be explicitly enabled. Set `transport.http.enabled` or `transport.stdio.enabled` to true.";
    
    private static final String TRANSPORT_REQUIRED_ERROR_MESSAGE = "Property `transport` is required.";
    
    private static final String TRANSPORT_MAPPING_ERROR_MESSAGE = "Property `transport` must be a mapping.";
    
    private static final String RUNTIME_DATABASES_REQUIRED_ERROR_MESSAGE = "Property `runtimeDatabases` is required.";
    
    private static final String RUNTIME_DATABASES_MAPPING_ERROR_MESSAGE = "Property `runtimeDatabases` must be a mapping.";
    
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
    
    @Override
    public MCPLaunchConfiguration swapToObject(final YamlMCPLaunchConfiguration yamlConfig) {
        ShardingSpherePreconditions.checkNotNull(yamlConfig, () -> new IllegalArgumentException(NULL_CONFIG_ERROR_MESSAGE));
        MCPTransportConfiguration transportConfig = transportConfigSwapper.swapToObject(yamlConfig.getTransport());
        validateTransportConfiguration(transportConfig);
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = swapToRuntimeDatabases(yamlConfig.getRuntimeDatabases());
        return new MCPLaunchConfiguration(transportConfig, runtimeDatabases);
    }
    
    /**
     * Swap from YAML content to launch configuration.
     *
     * @param yamlContent YAML content
     * @return launch configuration
     */
    public MCPLaunchConfiguration swapToObject(final String yamlContent) {
        return swapToObject(parseYamlConfiguration(loadYamlRoot(yamlContent)));
    }
    
    private Map<?, ?> loadYamlRoot(final String yamlContent) {
        LoaderOptions loaderOptions = ShardingSphereYamlConstructor.createLoaderOptions();
        Object yamlRoot = new Yaml(loaderOptions).load(yamlContent);
        return yamlRoot instanceof Map ? (Map<?, ?>) yamlRoot : Map.of();
    }
    
    private YamlMCPLaunchConfiguration parseYamlConfiguration(final Map<?, ?> yamlRoot) {
        validateRootProperties(yamlRoot);
        YamlMCPLaunchConfiguration result = new YamlMCPLaunchConfiguration();
        result.setTransport(parseYamlTransportConfig(getRequiredMapping(yamlRoot, "transport", TRANSPORT_REQUIRED_ERROR_MESSAGE, TRANSPORT_MAPPING_ERROR_MESSAGE)));
        result.setRuntimeDatabases(parseYamlRuntimeDatabases(getRequiredMapping(yamlRoot, "runtimeDatabases",
                RUNTIME_DATABASES_REQUIRED_ERROR_MESSAGE, RUNTIME_DATABASES_MAPPING_ERROR_MESSAGE)));
        return result;
    }
    
    private YamlMCPTransportConfiguration parseYamlTransportConfig(final Map<?, ?> transportSection) {
        validateAllowedProperties(transportSection, "transport", TRANSPORT_PROPERTIES);
        YamlMCPTransportConfiguration result = new YamlMCPTransportConfiguration();
        result.setHttp(parseYamlHttpConfig(getRequiredMapping(transportSection, "http", "Property `transport.http` is required.", "Property `transport.http` must be a mapping.")));
        result.setStdio(parseYamlStdioConfig(getRequiredMapping(transportSection, "stdio", "Property `transport.stdio` is required.", "Property `transport.stdio` must be a mapping.")));
        return result;
    }
    
    private YamlHttpTransportConfiguration parseYamlHttpConfig(final Map<?, ?> httpSection) {
        validateAllowedProperties(httpSection, "transport.http", HTTP_PROPERTIES);
        YamlHttpTransportConfiguration result = new YamlHttpTransportConfiguration();
        result.setEnabled(getBooleanValue(httpSection.get("enabled"), "transport.http.enabled"));
        result.setBindHost(getStringValue(httpSection.get("bindHost"), "transport.http.bindHost"));
        result.setPort(getIntegerValue(httpSection.get("port"), "transport.http.port"));
        result.setEndpointPath(getStringValue(httpSection.get("endpointPath"), "transport.http.endpointPath"));
        return result;
    }
    
    private YamlStdioTransportConfiguration parseYamlStdioConfig(final Map<?, ?> stdioSection) {
        validateAllowedProperties(stdioSection, "transport.stdio", STDIO_PROPERTIES);
        YamlStdioTransportConfiguration result = new YamlStdioTransportConfiguration();
        result.setEnabled(getBooleanValue(stdioSection.get("enabled"), "transport.stdio.enabled"));
        return result;
    }
    
    private Map<String, YamlRuntimeDatabaseConfiguration> parseYamlRuntimeDatabases(final Map<?, ?> runtimeDatabases) {
        Map<String, YamlRuntimeDatabaseConfiguration> result = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
        for (Entry<?, ?> entry : runtimeDatabases.entrySet()) {
            String databaseName = validateRuntimeDatabaseName(entry.getKey());
            result.put(databaseName, parseYamlRuntimeDatabaseConfig(databaseName, entry.getValue()));
        }
        return result;
    }
    
    private Map<?, ?> getRequiredMapping(final Map<?, ?> yamlRoot, final String propertyName, final String requiredErrorMessage, final String mappingErrorMessage) {
        ShardingSpherePreconditions.checkState(yamlRoot.containsKey(propertyName), () -> new IllegalArgumentException(requiredErrorMessage));
        ShardingSpherePreconditions.checkState(yamlRoot.get(propertyName) instanceof Map, () -> new IllegalArgumentException(mappingErrorMessage));
        return (Map<?, ?>) yamlRoot.get(propertyName);
    }
    
    private YamlRuntimeDatabaseConfiguration parseYamlRuntimeDatabaseConfig(final String databaseName, final Object value) {
        ShardingSpherePreconditions.checkState(value instanceof Map,
                () -> new IllegalArgumentException(String.format("Property `runtimeDatabases.%s` must be a mapping.", databaseName)));
        Map<?, ?> runtimeDatabaseSection = (Map<?, ?>) value;
        validateAllowedProperties(runtimeDatabaseSection, "runtimeDatabases." + databaseName, RUNTIME_DATABASE_PROPERTIES);
        YamlRuntimeDatabaseConfiguration result = new YamlRuntimeDatabaseConfiguration();
        result.setDatabaseType(getStringValue(runtimeDatabaseSection.get("databaseType"), "runtimeDatabases." + databaseName + ".databaseType"));
        result.setJdbcUrl(getStringValue(runtimeDatabaseSection.get("jdbcUrl"), "runtimeDatabases." + databaseName + ".jdbcUrl"));
        result.setUsername(getStringValue(runtimeDatabaseSection.get("username"), "runtimeDatabases." + databaseName + ".username"));
        result.setPassword(getStringValue(runtimeDatabaseSection.get("password"), "runtimeDatabases." + databaseName + ".password"));
        result.setDriverClassName(getStringValue(runtimeDatabaseSection.get("driverClassName"), "runtimeDatabases." + databaseName + ".driverClassName"));
        return result;
    }
    
    private void validateRootProperties(final Map<?, ?> yamlRoot) {
        for (Object each : yamlRoot.keySet()) {
            String propertyName = String.valueOf(each);
            ShardingSpherePreconditions.checkState(ROOT_PROPERTIES.contains(propertyName), () -> new IllegalArgumentException(String.format("Unsupported YAML property `%s`.", propertyName)));
        }
    }
    
    private void validateAllowedProperties(final Map<?, ?> section, final String sectionName, final Set<String> allowedProperties) {
        for (Object each : section.keySet()) {
            String propertyName = String.valueOf(each);
            ShardingSpherePreconditions.checkState(allowedProperties.contains(propertyName),
                    () -> new IllegalArgumentException(String.format("Unsupported YAML property `%s`.", sectionName + "." + propertyName)));
        }
    }
    
    private String validateRuntimeDatabaseName(final Object key) {
        ShardingSpherePreconditions.checkState(key instanceof String, () -> new IllegalArgumentException("Runtime logical database name must be a string."));
        return (String) key;
    }
    
    private Boolean getBooleanValue(final Object value, final String propertyName) {
        ShardingSpherePreconditions.checkState(null == value || value instanceof Boolean,
                () -> new IllegalArgumentException(String.format("Property `%s` must be a boolean.", propertyName)));
        return (Boolean) value;
    }
    
    private String getStringValue(final Object value, final String propertyName) {
        ShardingSpherePreconditions.checkState(null == value || value instanceof String,
                () -> new IllegalArgumentException(String.format("Property `%s` must be a string.", propertyName)));
        return (String) value;
    }
    
    private Integer getIntegerValue(final Object value, final String propertyName) {
        if (null == value) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long && (Long) value <= Integer.MAX_VALUE && (Long) value >= Integer.MIN_VALUE) {
            return ((Long) value).intValue();
        }
        throw new IllegalArgumentException(String.format("Property `%s` must be an integer.", propertyName));
    }
    
    private void validateTransportConfiguration(final MCPTransportConfiguration transportConfig) {
        if (!transportConfig.hasEnabledTransport()) {
            throw new IllegalArgumentException(TRANSPORT_VALIDATION_ERROR_MESSAGE);
        }
    }
    
    private Map<String, RuntimeDatabaseConfiguration> swapToRuntimeDatabases(final Map<String, YamlRuntimeDatabaseConfiguration> yamlRuntimeDatabases) {
        ShardingSpherePreconditions.checkNotNull(yamlRuntimeDatabases, () -> new IllegalArgumentException(RUNTIME_DATABASES_REQUIRED_ERROR_MESSAGE));
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(yamlRuntimeDatabases.size(), 1F);
        for (Entry<String, YamlRuntimeDatabaseConfiguration> entry : yamlRuntimeDatabases.entrySet()) {
            String databaseName = entry.getKey();
            ShardingSpherePreconditions.checkNotNull(databaseName, () -> new IllegalArgumentException("Runtime logical database name cannot be null."));
            ShardingSpherePreconditions.checkState(!databaseName.isBlank(), () -> new IllegalArgumentException("Runtime logical database name cannot be blank."));
            ShardingSpherePreconditions.checkState(!result.containsKey(databaseName), () -> new IllegalArgumentException(String.format("Runtime logical database `%s` is duplicated.", databaseName)));
            result.put(databaseName, runtimeDatabaseConfigSwapper.swapToObject(entry.getValue()));
        }
        return result;
    }
}
