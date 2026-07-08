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

package org.apache.shardingsphere.mcp.bootstrap.config;

import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPTransportConfiguration;
import org.apache.shardingsphere.mcp.support.yaml.MCPYamlConfigurationValidator;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPLaunchConfigurationTest {
    
    @Test
    void assertValidateWhenHttpTransportSelected() {
        assertDoesNotThrow(() -> validate(createYamlConfig(MCPTransportType.STREAMABLE_HTTP, createYamlHttpTransportConfiguration())));
    }
    
    @Test
    void assertValidateWhenHttpTransportUsesDefaults() {
        assertDoesNotThrow(() -> validate(createYamlConfig(MCPTransportType.STREAMABLE_HTTP, null)));
    }
    
    @Test
    void assertValidateWhenStdioTransportSelected() {
        assertDoesNotThrow(() -> validate(createYamlConfig(MCPTransportType.STDIO, null)));
    }
    
    @Test
    void assertValidateWhenTransportTypeMissing() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(null, null);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.type` is required."));
    }
    
    @Test
    void assertValidateWhenStdioTransportHasHttpConfiguration() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(MCPTransportType.STDIO, createYamlHttpTransportConfiguration());
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("transport.http is only valid when `transport.type` is STREAMABLE_HTTP."));
    }
    
    @Test
    void assertValidateWhenDatabasesMissing() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(MCPTransportType.STREAMABLE_HTTP, null);
        yamlConfig.setRuntimeDatabases(null);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases` is required."));
    }
    
    @Test
    void assertValidateWhenDatabasesEmpty() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(MCPTransportType.STREAMABLE_HTTP, null);
        yamlConfig.setRuntimeDatabases(Collections.emptyMap());
        assertDoesNotThrow(() -> validate(yamlConfig));
    }
    
    @Test
    void assertValidateWhenHttpBindHostIsUrl() {
        YamlHttpTransportConfiguration http = createYamlHttpTransportConfiguration();
        http.setBindHost("http://127.0.0.1:18088");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(createYamlConfig(MCPTransportType.STREAMABLE_HTTP, http)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.http.bindHost` must be a local bind host or IP address."));
    }
    
    @Test
    void assertValidateWhenHttpPortIsOutOfRange() {
        YamlHttpTransportConfiguration http = createYamlHttpTransportConfiguration();
        http.setPort(65536);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(createYamlConfig(MCPTransportType.STREAMABLE_HTTP, http)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.http.port` must be between 0 and 65535."));
    }
    
    @Test
    void assertValidateWhenHttpEndpointPathMissingLeadingSlash() {
        YamlHttpTransportConfiguration http = createYamlHttpTransportConfiguration();
        http.setEndpointPath("mcp");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(createYamlConfig(MCPTransportType.STREAMABLE_HTTP, http)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.http.endpointPath` must be a single absolute path without query or fragment."));
    }
    
    @Test
    void assertValidateWhenHttpEndpointPathHasQuery() {
        YamlHttpTransportConfiguration http = createYamlHttpTransportConfiguration();
        http.setEndpointPath("/mcp?debug=true");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(createYamlConfig(MCPTransportType.STREAMABLE_HTTP, http)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.http.endpointPath` must be a single absolute path without query or fragment."));
    }
    
    private void validate(final YamlMCPLaunchConfiguration yamlConfig) {
        MCPYamlConfigurationValidator.validate(yamlConfig, "MCP launch configuration");
    }
    
    private YamlMCPLaunchConfiguration createYamlConfig(final MCPTransportType type, final YamlHttpTransportConfiguration http) {
        YamlMCPLaunchConfiguration result = new YamlMCPLaunchConfiguration();
        YamlMCPTransportConfiguration transport = new YamlMCPTransportConfiguration();
        transport.setType(type);
        transport.setHttp(http);
        result.setTransport(transport);
        result.setRuntimeDatabases(createRuntimeDatabases());
        return result;
    }
    
    private YamlHttpTransportConfiguration createYamlHttpTransportConfiguration() {
        YamlHttpTransportConfiguration result = new YamlHttpTransportConfiguration();
        result.setBindHost("127.0.0.1");
        result.setPort(0);
        result.setEndpointPath("/mcp");
        return result;
    }
    
    private Map<String, Map<String, Object>> createRuntimeDatabases() {
        return Collections.singletonMap("logic_db", Map.of(
                "jdbcUrl", "jdbc:mysql://localhost:3306/logic_db",
                "username", "demo",
                "password", "",
                "driverClassName", "com.mysql.cj.jdbc.Driver"));
    }
}
