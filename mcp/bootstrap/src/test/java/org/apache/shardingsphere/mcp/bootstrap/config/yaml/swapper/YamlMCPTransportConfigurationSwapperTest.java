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

import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPTransportType;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPTransportConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlMCPTransportConfigurationSwapperTest {
    
    private final YamlMCPLaunchConfigurationSwapper swapper = new YamlMCPLaunchConfigurationSwapper();
    
    @Test
    void assertSwapToObjectWithMissingType() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig();
        yamlConfig.getTransport().setType(null);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.type` is required."));
    }
    
    @Test
    void assertSwapToObjectWithStdioTransportRejectsHttp() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig();
        yamlConfig.getTransport().setType(MCPTransportType.STDIO);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        assertThat(actual.getMessage(), is("transport.http is only valid when `transport.type` is STREAMABLE_HTTP."));
    }
    
    @Test
    void assertSwapToObject() {
        MCPLaunchConfiguration actual = swapper.swapToObject(createYamlConfig());
        assertThat(actual.getTransportType(), is(MCPTransportType.STREAMABLE_HTTP));
        assertThat(actual.getHttpTransport().getBindHost(), is("127.0.0.1"));
        assertThat(actual.getHttpTransport().getPort(), is(18088));
        assertThat(actual.getHttpTransport().getEndpointPath(), is("/mcp"));
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlMCPLaunchConfiguration actual = swapper.swapToYamlConfiguration(
                new MCPLaunchConfiguration(MCPTransportType.STREAMABLE_HTTP, new HttpTransportConfiguration("127.0.0.1", 18088, "/mcp"), Map.of()));
        assertThat(actual.getTransport().getType(), is(MCPTransportType.STREAMABLE_HTTP));
        assertThat(actual.getTransport().getHttp().getPort(), is(18088));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithStdioTransport() {
        YamlMCPLaunchConfiguration actual = swapper.swapToYamlConfiguration(
                new MCPLaunchConfiguration(MCPTransportType.STDIO, new HttpTransportConfiguration("127.0.0.1", 18088, "/mcp"), Map.of()));
        assertThat(actual.getTransport().getType(), is(MCPTransportType.STDIO));
        assertNull(actual.getTransport().getHttp());
    }
    
    private YamlMCPLaunchConfiguration createYamlConfig() {
        YamlHttpTransportConfiguration http = new YamlHttpTransportConfiguration();
        http.setBindHost("127.0.0.1");
        http.setPort(18088);
        http.setEndpointPath("/mcp");
        YamlMCPTransportConfiguration transport = new YamlMCPTransportConfiguration();
        transport.setType(MCPTransportType.STREAMABLE_HTTP);
        transport.setHttp(http);
        YamlMCPLaunchConfiguration result = new YamlMCPLaunchConfiguration();
        result.setTransport(transport);
        result.setRuntimeDatabases(Map.of("logic_db", Map.of(
                "databaseType", "H2",
                "jdbcUrl", "jdbc:h2:mem:logic",
                "username", "",
                "password", "",
                "driverClassName", "org.h2.Driver")));
        return result;
    }
}
