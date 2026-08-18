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
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPTransportConfiguration;
import org.apache.shardingsphere.mcp.support.configuration.MCPConfigurationValidator;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;

import java.util.Map;

/**
 * YAML MCP launch configuration swapper.
 */
public final class YamlMCPLaunchConfigurationSwapper implements YamlConfigurationSwapper<YamlMCPLaunchConfiguration, MCPLaunchConfiguration> {
    
    private final YamlHttpTransportConfigurationSwapper httpTransportConfigSwapper = new YamlHttpTransportConfigurationSwapper();
    
    private final YamlRuntimeDatabaseConfigurationsSwapper runtimeDatabasesSwapper = new YamlRuntimeDatabaseConfigurationsSwapper();
    
    @Override
    public YamlMCPLaunchConfiguration swapToYamlConfiguration(final MCPLaunchConfiguration data) {
        YamlMCPLaunchConfiguration result = new YamlMCPLaunchConfiguration();
        result.setTransport(createYamlTransportConfiguration(data));
        result.setRuntimeDatabases(runtimeDatabasesSwapper.swapToYamlConfiguration(data.getDatabases()));
        return result;
    }
    
    @Override
    public MCPLaunchConfiguration swapToObject(final YamlMCPLaunchConfiguration yamlConfig) {
        MCPConfigurationValidator.validate(yamlConfig, "MCP launch configuration");
        YamlMCPTransportConfiguration yamlTransportConfig = yamlConfig.getTransport();
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = runtimeDatabasesSwapper.swapToObject(yamlConfig.getRuntimeDatabases());
        return MCPTransportType.HTTP == yamlTransportConfig.getType()
                ? new MCPLaunchConfiguration(httpTransportConfigSwapper.swapToObject(yamlTransportConfig.getHttp()), runtimeDatabases)
                : new MCPLaunchConfiguration(runtimeDatabases);
    }
    
    private YamlMCPTransportConfiguration createYamlTransportConfiguration(final MCPLaunchConfiguration data) {
        YamlMCPTransportConfiguration result = new YamlMCPTransportConfiguration();
        result.setType(data.getTransportType());
        if (MCPTransportType.HTTP == data.getTransportType()) {
            result.setHttp(httpTransportConfigSwapper.swapToYamlConfiguration(data.getHttpTransport()));
        }
        return result;
    }
}
