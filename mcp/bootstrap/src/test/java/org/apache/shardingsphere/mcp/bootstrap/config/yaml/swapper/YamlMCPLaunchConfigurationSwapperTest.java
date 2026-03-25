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
import org.apache.shardingsphere.mcp.bootstrap.config.MCPTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlMCPLaunchConfigurationSwapperTest {
    
    private final YamlMCPLaunchConfigurationSwapper swapper = new YamlMCPLaunchConfigurationSwapper();
    
    @Test
    void assertSwapToObject() {
        MCPLaunchConfiguration actual = swapper.swapToObject("transport:\n"
                + "  http:\n"
                + "    enabled: true\n"
                + "    bindHost: 0.0.0.0\n"
                + "    port: 9090\n"
                + "    endpointPath: /gateway\n"
                + "  stdio:\n"
                + "    enabled: false\n"
                + "runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: jdbc:h2:mem:logic\n"
                + "    username: demo\n"
                + "    password: secret\n"
                + "    driverClassName: org.h2.Driver\n");
        
        assertThat(actual.getTransport().getHttp().getBindHost(), is("0.0.0.0"));
        assertThat(actual.getTransport().getHttp().getPort(), is(9090));
        assertThat(actual.getTransport().getHttp().getEndpointPath(), is("/gateway"));
        assertTrue(actual.getTransport().getHttp().isEnabled());
        assertFalse(actual.getTransport().getStdio().isEnabled());
        assertThat(actual.getRuntimeDatabases().get("logic_db").getDatabaseType(), is("H2"));
        assertThat(actual.getRuntimeDatabases().get("logic_db").getUsername(), is("demo"));
    }
    
    @Test
    void assertSwapToObjectWithNullConfiguration() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject((YamlMCPLaunchConfiguration) null));
        
        assertThat(actual.getMessage(), is("MCP launch configuration cannot be null."));
    }
    
    @Test
    void assertSwapToObjectWithMissingTransportSection() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject("runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: jdbc:h2:mem:logic\n"
                + "    username: ''\n"
                + "    password: ''\n"
                + "    driverClassName: org.h2.Driver\n"));
        
        assertThat(actual.getMessage(), is("Property `transport` is required."));
    }
    
    @Test
    void assertSwapToObjectWithMissingRuntimeDatabasesSection() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject("transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: true\n"));
        
        assertThat(actual.getMessage(), is("Property `runtimeDatabases` is required."));
    }
    
    @Test
    void assertSwapToObjectWithDisabledTransports() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject("transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: false\n"
                + "runtimeDatabases: {}\n"));
        
        assertThat(actual.getMessage(), is("At least one transport must be explicitly enabled. Set `transport.http.enabled` or `transport.stdio.enabled` to true."));
    }
    
    @Test
    void assertSwapToObjectWithRuntimeDatabaseTypeMissing() {
        String yamlContent = "transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: true\n"
                + "runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    jdbcUrl: jdbc:h2:mem:logic\n"
                + "    username: ''\n"
                + "    password: ''\n"
                + "    driverClassName: org.h2.Driver\n";
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlContent));
        
        assertThat(actual.getMessage(), is("Runtime database property `databaseType` is required."));
    }
    
    @Test
    void assertSwapToObjectWithNullRuntimeDatabaseConfiguration() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject("transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: true\n"
                + "runtimeDatabases:\n"
                + "  logic_db: null\n"));
        
        assertThat(actual.getMessage(), is("Property `runtimeDatabases.logic_db` must be a mapping."));
    }
    
    @Test
    void assertSwapToObjectWithUnsupportedRootProperty() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject("runtime:\n"
                + "  props:\n"
                + "    databaseName: logic_db\n"));
        
        assertThat(actual.getMessage(), is("Unsupported YAML property `runtime`."));
    }
    
    @Test
    void assertSwapToObjectWithUnsupportedRuntimeDatabaseProperty() {
        String yamlContent = "transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: true\n"
                + "runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: jdbc:h2:mem:logic\n"
                + "    username: ''\n"
                + "    password: ''\n"
                + "    driverClassName: org.h2.Driver\n"
                + "    supportsExplainAnalyze: true\n";
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlContent));
        
        assertThat(actual.getMessage(), is("Unsupported YAML property `runtimeDatabases.logic_db.supportsExplainAnalyze`."));
    }
    
    @Test
    void assertSwapToObjectWithNonStringRuntimeDatabaseName() {
        String yamlContent = "transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: true\n"
                + "runtimeDatabases:\n"
                + "  1:\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: jdbc:h2:mem:logic\n"
                + "    username: ''\n"
                + "    password: ''\n"
                + "    driverClassName: org.h2.Driver\n";
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlContent));
        
        assertThat(actual.getMessage(), is("Runtime logical database name must be a string."));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> databases = new LinkedHashMap<>(1, 1F);
        databases.put("logic_db", new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:logic", "", "", "org.h2.Driver"));
        MCPLaunchConfiguration launchConfig = new MCPLaunchConfiguration(new MCPTransportConfiguration(
                new HttpTransportConfiguration(true, "127.0.0.1", 18088, "/mcp"), new StdioTransportConfiguration(true)),
                databases);
        
        YamlMCPLaunchConfiguration actual = swapper.swapToYamlConfiguration(launchConfig);
        
        assertThat(actual.getRuntimeDatabases().get("logic_db").getDatabaseType(), is("H2"));
        assertThat(actual.getRuntimeDatabases().get("logic_db").getUsername(), is(""));
        assertThat(actual.getTransport().getHttp().getBindHost(), is("127.0.0.1"));
        assertThat(actual.getTransport().getStdio().getEnabled(), is(Boolean.TRUE));
    }
}
