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

import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeTopologyConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.TransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;

import java.util.LinkedHashMap;
import java.util.Properties;

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
        result.setRuntime(runtimeConfigSwapper.swapToYamlConfiguration(
                new RuntimeConfiguration(data.getRuntimeProps().orElseGet(Properties::new), data.getRuntimeTopologyConfiguration().orElseGet(this::createEmptyRuntimeTopologyConfiguration))));
        return result;
    }
    
    @Override
    public MCPLaunchConfiguration swapToObject(final YamlMCPLaunchConfiguration yamlConfig) {
        YamlMCPLaunchConfiguration actualYamlConfig = null == yamlConfig ? new YamlMCPLaunchConfiguration() : yamlConfig;
        TransportConfiguration transportConfig = transportConfigSwapper.swapToObject(actualYamlConfig.getTransport());
        RuntimeConfiguration runtimeConfig = runtimeConfigSwapper.swapToObject(actualYamlConfig.getRuntime());
        return new MCPLaunchConfiguration(httpServerConfigSwapper.swapToObject(actualYamlConfig.getServer()),
                transportConfig.isHttpEnabled(), transportConfig.isStdioEnabled(), runtimeConfig.getProps(), runtimeConfig.getTopologyConfiguration());
    }
    
    private RuntimeTopologyConfiguration createEmptyRuntimeTopologyConfiguration() {
        return new RuntimeTopologyConfiguration(new LinkedHashMap<>());
    }
}
