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
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeDatabaseConfiguration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * YAML MCP launch configuration swapper.
 */
public final class YamlMCPLaunchConfigurationSwapper implements YamlConfigurationSwapper<YamlMCPLaunchConfiguration, MCPLaunchConfiguration> {
    
    private static final String NULL_CONFIG_ERROR_MESSAGE = "MCP launch configuration cannot be null.";
    
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
    
    @Override
    public MCPLaunchConfiguration swapToObject(final YamlMCPLaunchConfiguration yamlConfig) {
        ShardingSpherePreconditions.checkNotNull(yamlConfig, () -> new IllegalArgumentException(NULL_CONFIG_ERROR_MESSAGE));
        MCPTransportConfiguration transportConfig = transportConfigSwapper.swapToObject(yamlConfig.getTransport());
        ShardingSpherePreconditions.checkState(transportConfig.hasEnabledTransport(), () -> new IllegalArgumentException(TRANSPORT_VALIDATION_ERROR_MESSAGE));
        return new MCPLaunchConfiguration(transportConfig, swapToRuntimeDatabases(null == yamlConfig.getRuntimeDatabases() ? Collections.emptyMap() : yamlConfig.getRuntimeDatabases()));
    }
    
    private Map<String, RuntimeDatabaseConfiguration> swapToRuntimeDatabases(final Map<String, YamlRuntimeDatabaseConfiguration> yamlRuntimeDatabases) {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(yamlRuntimeDatabases.size(), 1F);
        for (Entry<String, YamlRuntimeDatabaseConfiguration> entry : yamlRuntimeDatabases.entrySet()) {
            result.put(entry.getKey(), runtimeDatabaseConfigSwapper.swapToObject(entry.getValue()));
        }
        return result;
    }
}
