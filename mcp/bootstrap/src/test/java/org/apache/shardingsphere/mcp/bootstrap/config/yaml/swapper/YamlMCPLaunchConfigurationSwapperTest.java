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
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.runtime.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.constructor.ConstructorException;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlMCPLaunchConfigurationSwapperTest {
    
    private final YamlMCPLaunchConfigurationSwapper swapper = new YamlMCPLaunchConfigurationSwapper();
    
    @Test
    void assertSwapToObject() {
        String yamlContent = "transport:\n"
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
                + "    driverClassName: org.h2.Driver\n";
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class));
        
        assertThat(actual.getHttpTransport().getBindHost(), is("0.0.0.0"));
        assertThat(actual.getHttpTransport().getPort(), is(9090));
        assertThat(actual.getHttpTransport().getEndpointPath(), is("/gateway"));
        assertTrue(actual.getHttpTransport().isEnabled());
        assertFalse(actual.getStdioTransport().isEnabled());
        assertThat(actual.getRuntimeConfiguration().get("logic_db").getDatabaseType(), is("H2"));
        assertThat(actual.getRuntimeConfiguration().get("logic_db").getUsername(), is("demo"));
    }
    
    @Test
    void assertSwapToObjectWithNullConfiguration() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject((YamlMCPLaunchConfiguration) null));
        
        assertThat(actual.getMessage(), is("MCP launch configuration cannot be null."));
    }
    
    @Test
    void assertSwapToObjectWithEmptyContent() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal("", YamlMCPLaunchConfiguration.class)));
        
        assertThat(actual.getMessage(), is("Property `transport` is required."));
    }
    
    @Test
    void assertSwapToObjectWithMissingTransportSection() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal("runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: jdbc:h2:mem:logic\n"
                + "    username: ''\n"
                + "    password: ''\n"
                + "    driverClassName: org.h2.Driver\n", YamlMCPLaunchConfiguration.class)));
        
        assertThat(actual.getMessage(), is("Property `transport` is required."));
    }
    
    @Test
    void assertSwapToObjectWithoutRuntimeDatabasesSection() {
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal(
                "transport:\n" + "  http:\n" + "    enabled: false\n" + "    bindHost: 127.0.0.1\n" + "    port: 18088\n" + "    endpointPath: /mcp\n" + "  stdio:\n" + "    enabled: true\n",
                YamlMCPLaunchConfiguration.class));
        
        assertTrue(actual.getRuntimeConfiguration().isEmpty());
    }
    
    @Test
    void assertSwapToObjectWithNullRuntimeDatabases() {
        YamlMCPLaunchConfiguration yamlConfig = YamlEngine.unmarshal(
                "transport:\n" + "  http:\n" + "    enabled: false\n" + "    bindHost: 127.0.0.1\n" + "    port: 18088\n" + "    endpointPath: /mcp\n" + "  stdio:\n" + "    enabled: true\n",
                YamlMCPLaunchConfiguration.class);
        yamlConfig.setRuntimeDatabases(null);
        
        MCPLaunchConfiguration actual = swapper.swapToObject(yamlConfig);
        
        assertTrue(actual.getRuntimeConfiguration().isEmpty());
    }
    
    @Test
    void assertSwapToObjectWithDisabledTransports() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal("transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: false\n"
                + "runtimeDatabases: {}\n", YamlMCPLaunchConfiguration.class)));
        
        assertThat(actual.getMessage(), is("Exactly one transport must be explicitly enabled. Set either `transport.http.enabled` or `transport.stdio.enabled` to true."));
    }
    
    @Test
    void assertSwapToObjectWithMultipleEnabledTransports() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal("transport:\n"
                + "  http:\n"
                + "    enabled: true\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: true\n"
                + "runtimeDatabases: {}\n", YamlMCPLaunchConfiguration.class)));
        
        assertThat(actual.getMessage(), is("HTTP and STDIO transports cannot be enabled at the same time. Choose exactly one transport."));
    }
    
    @Test
    void assertSwapToObjectWithOmittedTransportEnabled() {
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal("transport:\n"
                + "  http:\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: true\n"
                + "runtimeDatabases: {}\n", YamlMCPLaunchConfiguration.class));
        
        assertFalse(actual.getHttpTransport().isEnabled());
        assertTrue(actual.getStdioTransport().isEnabled());
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
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        
        assertThat(actual.getMessage(), is("Runtime database property `databaseType` is required."));
    }
    
    @Test
    void assertSwapToObjectWithNullRuntimeDatabaseConfiguration() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal("transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: true\n"
                + "runtimeDatabases:\n"
                + "  logic_db: null\n", YamlMCPLaunchConfiguration.class)));
        
        assertThat(actual.getMessage(), is("Runtime database configuration cannot be null."));
    }
    
    @Test
    void assertSwapToObjectWithUnsupportedRootProperty() {
        ConstructorException actual = assertThrows(ConstructorException.class, () -> swapper.swapToObject(YamlEngine.unmarshal("runtime:\n"
                + "  props:\n"
                + "    databaseName: logic_db\n", YamlMCPLaunchConfiguration.class)));
        
        assertThat(actual.getMessage(), containsString("Unable to find property 'runtime'"));
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
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        
        assertThat(actual.getMessage(), is("Unsupported runtime database property `supportsExplainAnalyze`."));
    }
    
    @Test
    void assertSwapToObjectWithNumericRuntimeDatabaseName() {
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
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class));
        
        assertThat(actual.getRuntimeConfiguration().get("1").getDatabaseType(), is("H2"));
    }
    
    @Test
    void assertSwapToObjectWithBlankRuntimeDatabaseName() {
        String yamlContent = "transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: true\n"
                + "runtimeDatabases:\n"
                + "  '':\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: jdbc:h2:mem:logic\n"
                + "    username: ''\n"
                + "    password: ''\n"
                + "    driverClassName: org.h2.Driver\n";
        ConstructorException actual = assertThrows(ConstructorException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        
        assertThat(actual.getMessage(), containsString("YAML map key cannot be blank."));
    }
    
    @Test
    void assertSwapToObjectWithNullRuntimeDatabaseName() {
        String yamlContent = "transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: true\n"
                + "runtimeDatabases:\n"
                + "  null:\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: jdbc:h2:mem:logic\n"
                + "    username: ''\n"
                + "    password: ''\n"
                + "    driverClassName: org.h2.Driver\n";
        ConstructorException actual = assertThrows(ConstructorException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        
        assertThat(actual.getMessage(), containsString("YAML map key cannot be null."));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> databases = new LinkedHashMap<>(1, 1F);
        databases.put("logic_db", new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:logic", "", "", "org.h2.Driver"));
        MCPLaunchConfiguration launchConfig = new MCPLaunchConfiguration(
                new HttpTransportConfiguration(true, "127.0.0.1", 18088, "/mcp"), new StdioTransportConfiguration(true), databases);
        
        YamlMCPLaunchConfiguration actual = swapper.swapToYamlConfiguration(launchConfig);
        
        assertThat(String.valueOf(actual.getRuntimeDatabases().get("logic_db").get("databaseType")), is("H2"));
        assertThat(String.valueOf(actual.getRuntimeDatabases().get("logic_db").get("username")), is(""));
        assertThat(actual.getTransport().getHttp().getBindHost(), is("127.0.0.1"));
        assertTrue(actual.getTransport().getStdio().isEnabled());
    }
}
