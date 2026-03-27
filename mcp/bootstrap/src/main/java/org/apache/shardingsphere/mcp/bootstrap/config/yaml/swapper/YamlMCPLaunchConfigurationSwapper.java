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

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.runtime.MCPRuntimeProvider;

import java.util.Collections;

/**
 * YAML MCP launch configuration swapper.
 */
public final class YamlMCPLaunchConfigurationSwapper implements YamlConfigurationSwapper<YamlMCPLaunchConfiguration, MCPLaunchConfiguration<?>> {
    
    private static final String NULL_CONFIG_ERROR_MESSAGE = "MCP launch configuration cannot be null.";
    
    private final MCPRuntimeProvider runtimeProvider;
    
    private final YamlMCPTransportConfigurationSwapper transportConfigSwapper = new YamlMCPTransportConfigurationSwapper();
    
    public YamlMCPLaunchConfigurationSwapper() {
        this(TypedSPILoader.getService(MCPRuntimeProvider.class, null));
    }
    
    public YamlMCPLaunchConfigurationSwapper(final MCPRuntimeProvider runtimeProvider) {
        this.runtimeProvider = runtimeProvider;
    }
    
    @Override
    public YamlMCPLaunchConfiguration swapToYamlConfiguration(final MCPLaunchConfiguration<?> data) {
        YamlMCPLaunchConfiguration result = new YamlMCPLaunchConfiguration();
        result.setTransport(transportConfigSwapper.swapToYamlConfiguration(data.getTransport()));
        result.setRuntimeDatabases(runtimeProvider.swapToYamlConfiguration(data.getRuntimeConfiguration()));
        return result;
    }
    
    @Override
    public MCPLaunchConfiguration<?> swapToObject(final YamlMCPLaunchConfiguration yamlConfig) {
        ShardingSpherePreconditions.checkNotNull(yamlConfig, () -> new IllegalArgumentException(NULL_CONFIG_ERROR_MESSAGE));
        MCPTransportConfiguration transportConfig = transportConfigSwapper.swapToObject(yamlConfig.getTransport());
        transportConfig.validate();
        return new MCPLaunchConfiguration<>(transportConfig, runtimeProvider.swapToObject(null == yamlConfig.getRuntimeDatabases() ? Collections.emptyMap() : yamlConfig.getRuntimeDatabases()));
    }
}
