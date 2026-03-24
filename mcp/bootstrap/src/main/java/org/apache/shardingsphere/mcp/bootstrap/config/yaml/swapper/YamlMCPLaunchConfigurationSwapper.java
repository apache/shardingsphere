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
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/**
 * YAML MCP launch configuration swapper.
 */
public final class YamlMCPLaunchConfigurationSwapper implements YamlConfigurationSwapper<YamlMCPLaunchConfiguration, MCPLaunchConfiguration> {
    
    private final YamlMCPTransportConfigurationSwapper transportConfigSwapper = new YamlMCPTransportConfigurationSwapper();
    
    private final YamlRuntimeConfigurationSwapper runtimeConfigSwapper = new YamlRuntimeConfigurationSwapper();
    
    @Override
    public YamlMCPLaunchConfiguration swapToYamlConfiguration(final MCPLaunchConfiguration data) {
        YamlMCPLaunchConfiguration result = new YamlMCPLaunchConfiguration();
        result.setTransport(transportConfigSwapper.swapToYamlConfiguration(data.getTransport()));
        result.setRuntime(runtimeConfigSwapper.swapToYamlConfiguration(data.getRuntimeDatabases()));
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
        validateRuntimeSection(getConfiguredSection(yamlRoot, "runtime"));
        YamlMCPLaunchConfiguration yamlConfig = YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class, true);
        return swapToObject(yamlConfig, getConfiguredSection(yamlRoot, "transport"));
    }
    
    @Override
    public MCPLaunchConfiguration swapToObject(final YamlMCPLaunchConfiguration yamlConfig) {
        return swapToObject(yamlConfig, Collections.emptyMap());
    }
    
    private MCPLaunchConfiguration swapToObject(final YamlMCPLaunchConfiguration yamlConfig, final Map<String, Object> configuredTransportSections) {
        YamlMCPLaunchConfiguration actualYamlConfig = null == yamlConfig ? new YamlMCPLaunchConfiguration() : yamlConfig;
        MCPTransportConfiguration transportConfig = transportConfigSwapper.swapToObject(actualYamlConfig.getTransport(), configuredTransportSections);
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = runtimeConfigSwapper.swapToObject(actualYamlConfig.getRuntime());
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
    
    private void validateRuntimeSection(final Map<String, Object> runtimeSection) {
        if (runtimeSection.isEmpty()) {
            return;
        }
        if (runtimeSection.containsKey("props")) {
            throw new IllegalArgumentException("`runtime.props` is no longer supported. Configure direct runtime databases with `runtime.databases`.");
        }
        if (runtimeSection.containsKey("defaults")) {
            throw new IllegalArgumentException("`runtime.defaults` is no longer supported. Configure shared defaults with `runtime.databaseDefaults`.");
        }
        Object databaseDefaults = runtimeSection.get("databaseDefaults");
        if (databaseDefaults instanceof Map && containsLegacyCapabilityBooleans((Map<?, ?>) databaseDefaults)) {
            throw new IllegalArgumentException("Legacy capability booleans are no longer supported under `runtime.databaseDefaults`. Capabilities are derived automatically.");
        }
        Object databases = runtimeSection.get("databases");
        if (databases instanceof Map) {
            for (Entry<?, ?> entry : ((Map<?, ?>) databases).entrySet()) {
                if (entry.getValue() instanceof Map && containsLegacyCapabilityBooleans((Map<?, ?>) entry.getValue())) {
                    throw new IllegalArgumentException(String.format(
                            "Legacy capability booleans are no longer supported for runtime database `%s`. Capabilities are derived automatically.", entry.getKey()));
                }
            }
        }
    }
    
    private boolean containsLegacyCapabilityBooleans(final Map<?, ?> databaseConfig) {
        return databaseConfig.containsKey("supportsCrossSchemaSql") || databaseConfig.containsKey("supportsExplainAnalyze");
    }
}
