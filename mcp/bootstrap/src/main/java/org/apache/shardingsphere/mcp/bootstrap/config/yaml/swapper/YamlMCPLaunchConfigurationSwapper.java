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
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeTopologyConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.TransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * YAML MCP launch configuration swapper.
 */
public final class YamlMCPLaunchConfigurationSwapper implements YamlConfigurationSwapper<YamlMCPLaunchConfiguration, MCPLaunchConfiguration> {
    
    private final YamlHttpServerConfigurationSwapper httpServerConfigSwapper = new YamlHttpServerConfigurationSwapper();
    
    private final YamlTransportConfigurationSwapper transportConfigSwapper = new YamlTransportConfigurationSwapper();
    
    private final YamlRuntimeConfigurationSwapper runtimeConfigSwapper = new YamlRuntimeConfigurationSwapper();
    
    @Override
    public YamlMCPLaunchConfiguration swapToYamlConfiguration(final MCPLaunchConfiguration data) {
        YamlMCPLaunchConfiguration result = new YamlMCPLaunchConfiguration();
        result.setServer(httpServerConfigSwapper.swapToYamlConfiguration(data.getHttpServerConfiguration()));
        result.setTransport(transportConfigSwapper.swapToYamlConfiguration(new TransportConfiguration(data.isHttpEnabled(), data.isStdioEnabled())));
        result.setRuntime(runtimeConfigSwapper.swapToYamlConfiguration(new RuntimeConfiguration(data.getRuntimeProps(),
                data.getRuntimeTopologyConfiguration().getDatabases().isEmpty() ? createEmptyRuntimeTopologyConfiguration() : data.getRuntimeTopologyConfiguration())));
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
        return swapToObject(yamlConfig, getConfiguredFieldNames(yamlRoot, "server"), getConfiguredFieldNames(yamlRoot, "transport"),
                getConfiguredRuntimeBooleanFields(yamlRoot));
    }
    
    @Override
    public MCPLaunchConfiguration swapToObject(final YamlMCPLaunchConfiguration yamlConfig) {
        return swapToObject(yamlConfig, Collections.emptySet(), Collections.emptySet(), Collections.emptyMap());
    }
    
    private MCPLaunchConfiguration swapToObject(final YamlMCPLaunchConfiguration yamlConfig, final Collection<String> configuredServerFields,
                                                final Collection<String> configuredTransportFields,
                                                final Map<String, Collection<String>> configuredRuntimeBooleanFields) {
        YamlMCPLaunchConfiguration actualYamlConfig = null == yamlConfig ? new YamlMCPLaunchConfiguration() : yamlConfig;
        TransportConfiguration transportConfig = transportConfigSwapper.swapToObject(actualYamlConfig.getTransport(), configuredTransportFields);
        RuntimeConfiguration runtimeConfig = runtimeConfigSwapper.swapToObject(actualYamlConfig.getRuntime(), configuredRuntimeBooleanFields);
        return new MCPLaunchConfiguration(httpServerConfigSwapper.swapToObject(actualYamlConfig.getServer(), configuredServerFields),
                transportConfig.isHttpEnabled(), transportConfig.isStdioEnabled(), runtimeConfig.getProps(), runtimeConfig.getTopologyConfiguration());
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadYamlRoot(final String yamlContent) {
        Object yamlRoot = new Yaml().load(yamlContent);
        return yamlRoot instanceof Map ? (Map<String, Object>) yamlRoot : Collections.emptyMap();
    }
    
    private Collection<String> getConfiguredFieldNames(final Map<String, Object> yamlRoot, final String sectionName) {
        Object section = yamlRoot.get(sectionName);
        return section instanceof Map ? new LinkedHashSet<>(((Map<?, ?>) section).keySet().stream().map(String::valueOf).toList()) : Collections.emptySet();
    }
    
    private Map<String, Collection<String>> getConfiguredRuntimeBooleanFields(final Map<String, Object> yamlRoot) {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        Object runtimeSection = yamlRoot.get("runtime");
        if (!(runtimeSection instanceof Map)) {
            return result;
        }
        Object databasesSection = ((Map<?, ?>) runtimeSection).get("databases");
        if (!(databasesSection instanceof Map)) {
            return result;
        }
        for (Entry<?, ?> entry : ((Map<?, ?>) databasesSection).entrySet()) {
            if (!(entry.getValue() instanceof Map)) {
                continue;
            }
            Collection<String> configuredFieldNames = new LinkedHashSet<>();
            if (((Map<?, ?>) entry.getValue()).containsKey("supportsCrossSchemaSql")) {
                configuredFieldNames.add("supportsCrossSchemaSql");
            }
            if (((Map<?, ?>) entry.getValue()).containsKey("supportsExplainAnalyze")) {
                configuredFieldNames.add("supportsExplainAnalyze");
            }
            result.put(String.valueOf(entry.getKey()).trim(), configuredFieldNames);
        }
        return result;
    }
    
    private RuntimeTopologyConfiguration createEmptyRuntimeTopologyConfiguration() {
        return new RuntimeTopologyConfiguration(new LinkedHashMap<>());
    }
}
