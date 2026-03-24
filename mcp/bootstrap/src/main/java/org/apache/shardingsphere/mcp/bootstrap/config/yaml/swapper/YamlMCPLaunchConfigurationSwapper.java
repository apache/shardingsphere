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

import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.TransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeDatabaseConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/**
 * YAML MCP launch configuration swapper.
 */
public final class YamlMCPLaunchConfigurationSwapper implements YamlConfigurationSwapper<YamlMCPLaunchConfiguration, MCPLaunchConfiguration> {
    
    private static final System.Logger LOGGER = System.getLogger(YamlMCPLaunchConfigurationSwapper.class.getName());
    
    private final YamlTransportConfigurationSwapper transportConfigSwapper = new YamlTransportConfigurationSwapper();
    
    private final YamlRuntimeConfigurationSwapper runtimeConfigSwapper = new YamlRuntimeConfigurationSwapper();
    
    @Override
    public YamlMCPLaunchConfiguration swapToYamlConfiguration(final MCPLaunchConfiguration data) {
        YamlMCPLaunchConfiguration result = new YamlMCPLaunchConfiguration();
        result.setTransport(transportConfigSwapper.swapToYamlConfiguration(data.getTransport()));
        result.setRuntime(runtimeConfigSwapper.swapToYamlConfiguration(new RuntimeConfiguration(data.getRuntimeProps(), data.getRuntimeDatabases())));
        return result;
    }
    
    /**
     * Swap from YAML content to launch configuration.
     *
     * @param yamlContent YAML content
     * @return launch configuration
     */
    public MCPLaunchConfiguration swapToObject(final String yamlContent) {
        YamlMCPLaunchConfiguration yamlConfig = YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class, true);
        Map<String, Object> yamlRoot = loadYamlRoot(yamlContent);
        logLegacyRuntimeWarnings(yamlConfig.getRuntime());
        return swapToObject(yamlConfig, getConfiguredSection(yamlRoot, "transport"));
    }
    
    @Override
    public MCPLaunchConfiguration swapToObject(final YamlMCPLaunchConfiguration yamlConfig) {
        return swapToObject(yamlConfig, Collections.emptyMap());
    }
    
    private MCPLaunchConfiguration swapToObject(final YamlMCPLaunchConfiguration yamlConfig, final Map<String, Object> configuredTransportSections) {
        YamlMCPLaunchConfiguration actualYamlConfig = null == yamlConfig ? new YamlMCPLaunchConfiguration() : yamlConfig;
        TransportConfiguration transportConfig = transportConfigSwapper.swapToObject(actualYamlConfig.getTransport(), configuredTransportSections);
        RuntimeConfiguration runtimeConfig = runtimeConfigSwapper.swapToObject(actualYamlConfig.getRuntime());
        return new MCPLaunchConfiguration(transportConfig, runtimeConfig.getProps(), runtimeConfig.getDatabases());
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
    
    private void logLegacyRuntimeWarnings(final YamlRuntimeConfiguration runtimeConfig) {
        if (null == runtimeConfig) {
            return;
        }
        if (null != runtimeConfig.getProps() && !runtimeConfig.getProps().isEmpty()) {
            LOGGER.log(System.Logger.Level.WARNING, "`runtime.props` is deprecated. Use `runtime.databases` instead.");
        }
        if (null != runtimeConfig.getDefaults() && !runtimeConfig.getDefaults().isEmpty()) {
            LOGGER.log(System.Logger.Level.WARNING, "`runtime.defaults` is deprecated. Use `runtime.databaseDefaults` instead.");
        }
        if (containsLegacyCapabilityBooleans(runtimeConfig.getDatabaseDefaults())) {
            LOGGER.log(System.Logger.Level.WARNING, "Legacy capability booleans are deprecated. Capability values are derived automatically.");
        }
        for (Entry<String, YamlRuntimeDatabaseConfiguration> entry : runtimeConfig.getDatabases().entrySet()) {
            if (containsLegacyCapabilityBooleans(entry.getValue())) {
                LOGGER.log(System.Logger.Level.WARNING, "Legacy capability booleans for runtime database `{0}` are deprecated. Capability values are derived automatically.",
                        entry.getKey());
            }
        }
    }
    
    private boolean containsLegacyCapabilityBooleans(final YamlRuntimeDatabaseConfiguration databaseConfig) {
        return null != databaseConfig && (null != databaseConfig.getSupportsCrossSchemaSql() || null != databaseConfig.getSupportsExplainAnalyze());
    }
}
