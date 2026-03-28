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
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPTransportConfiguration;
import org.apache.shardingsphere.mcp.jdbc.config.yaml.swapper.YamlRuntimeDatabaseConfigurationsSwapper;

/**
 * YAML MCP launch configuration swapper.
 */
public final class YamlMCPLaunchConfigurationSwapper implements YamlConfigurationSwapper<YamlMCPLaunchConfiguration, MCPLaunchConfiguration> {
    
    private final YamlHttpTransportConfigurationSwapper httpTransportConfigSwapper = new YamlHttpTransportConfigurationSwapper();
    
    private final YamlStdioTransportConfigurationSwapper stdioTransportConfigSwapper = new YamlStdioTransportConfigurationSwapper();
    
    private final YamlRuntimeDatabaseConfigurationsSwapper runtimeDatabasesSwapper = new YamlRuntimeDatabaseConfigurationsSwapper();
    
    @Override
    public YamlMCPLaunchConfiguration swapToYamlConfiguration(final MCPLaunchConfiguration data) {
        YamlMCPLaunchConfiguration result = new YamlMCPLaunchConfiguration();
        result.setTransport(createYamlTransportConfiguration(data.getHttpTransport(), data.getStdioTransport()));
        result.setRuntimeDatabases(runtimeDatabasesSwapper.swapToYamlConfiguration(data.getDatabases()));
        return result;
    }
    
    @Override
    public MCPLaunchConfiguration swapToObject(final YamlMCPLaunchConfiguration yamlConfig) {
        ShardingSpherePreconditions.checkNotNull(yamlConfig, () -> new IllegalArgumentException("MCP launch configuration cannot be null."));
        YamlMCPTransportConfiguration yamlTransportConfig = resolveRequiredTransportConfiguration(yamlConfig);
        MCPLaunchConfiguration result = new MCPLaunchConfiguration(httpTransportConfigSwapper.swapToObject(yamlTransportConfig.getHttp()),
                stdioTransportConfigSwapper.swapToObject(yamlTransportConfig.getStdio()), runtimeDatabasesSwapper.swapToObject(yamlConfig.getRuntimeDatabases()));
        result.validate();
        return result;
    }
    
    private YamlMCPTransportConfiguration createYamlTransportConfiguration(final HttpTransportConfiguration httpTransport, final StdioTransportConfiguration stdioTransport) {
        YamlMCPTransportConfiguration result = new YamlMCPTransportConfiguration();
        result.setHttp(httpTransportConfigSwapper.swapToYamlConfiguration(httpTransport));
        result.setStdio(stdioTransportConfigSwapper.swapToYamlConfiguration(stdioTransport));
        return result;
    }
    
    private YamlMCPTransportConfiguration resolveRequiredTransportConfiguration(final YamlMCPLaunchConfiguration yamlConfig) {
        YamlMCPTransportConfiguration result = yamlConfig.getTransport();
        ShardingSpherePreconditions.checkNotNull(result, () -> new IllegalArgumentException("Property `transport` is required."));
        return result;
    }
}
