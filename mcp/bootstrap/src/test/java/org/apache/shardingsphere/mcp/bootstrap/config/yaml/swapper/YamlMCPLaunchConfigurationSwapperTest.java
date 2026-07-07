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
import org.apache.shardingsphere.mcp.bootstrap.config.MCPTransportType;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.constructor.ConstructorException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlMCPLaunchConfigurationSwapperTest {
    
    private final YamlMCPLaunchConfigurationSwapper swapper = new YamlMCPLaunchConfigurationSwapper();
    
    @Test
    void assertSwapToObject() {
        String yamlContent = "transport:\n"
                + "  type: STREAMABLE_HTTP\n"
                + "  http:\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 9090\n"
                + "    endpointPath: /gateway\n"
                + "runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    jdbcUrl: jdbc:mysql://localhost:3306/logic_db\n"
                + "    username: demo\n"
                + "    password: secret\n"
                + "    driverClassName: com.mysql.cj.jdbc.Driver\n";
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class));
        assertThat(actual.getTransportType(), is(MCPTransportType.STREAMABLE_HTTP));
        assertThat(actual.getHttpTransport().getBindHost(), is("127.0.0.1"));
        assertThat(actual.getHttpTransport().getPort(), is(9090));
        assertThat(actual.getHttpTransport().getEndpointPath(), is("/gateway"));
        assertThat(actual.getDatabases().get("logic_db").getUsername(), is("demo"));
    }
    
    @Test
    void assertSwapToObjectWithPasswordMissing() {
        String yamlContent = "transport:\n"
                + "  type: STDIO\n"
                + "runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    jdbcUrl: jdbc:mysql://localhost:3306/logic_db\n"
                + "    username: demo\n"
                + "    driverClassName: com.mysql.cj.jdbc.Driver\n";
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class));
        assertThat(actual.getDatabases().get("logic_db").getPassword(), is(""));
    }
    
    @Test
    void assertSwapToObjectWithBlankDriverClassName() {
        String yamlContent = "transport:\n"
                + "  type: STDIO\n"
                + "runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    jdbcUrl: jdbc:mysql://localhost:3306/logic_db\n"
                + "    username: demo\n"
                + "    driverClassName: ''\n";
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases` contains database `logic_db` property `driverClassName` is required."));
    }
    
    @Test
    void assertSwapToObjectWithNullConfiguration() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(null));
        assertThat(actual.getMessage(), is("MCP launch configuration cannot be null."));
    }
    
    @Test
    void assertSwapToObjectWithEmptyContent() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal("", YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases` is required.; MCP launch configuration property `transport` is required."));
    }
    
    @Test
    void assertSwapToObjectWithMissingTransportSection() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal("runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    jdbcUrl: jdbc:mysql://localhost:3306/logic_db\n"
                + "    username: demo\n"
                + "    password: ''\n"
                + "    driverClassName: com.mysql.cj.jdbc.Driver\n", YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport` is required."));
    }
    
    @Test
    void assertSwapToObjectWithoutRuntimeDatabasesSection() {
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal(
                "transport:\n" + "  type: STDIO\n",
                YamlMCPLaunchConfiguration.class));
        assertThat(actual.getDatabases(), is(Collections.emptyMap()));
    }
    
    @Test
    void assertSwapToObjectWithNullRuntimeDatabases() {
        YamlMCPLaunchConfiguration yamlConfig = YamlEngine.unmarshal(
                "transport:\n" + "  type: STDIO\n",
                YamlMCPLaunchConfiguration.class);
        yamlConfig.setRuntimeDatabases(null);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases` is required."));
    }
    
    @Test
    void assertSwapToObjectWithEmptyRuntimeDatabases() {
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal(
                "transport:\n" + "  type: STDIO\n" + "runtimeDatabases: {}\n",
                YamlMCPLaunchConfiguration.class));
        assertThat(actual.getDatabases(), is(Collections.emptyMap()));
    }
    
    @Test
    void assertSwapToObjectWithHttpDefaults() {
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal("transport:\n"
                + "  type: STREAMABLE_HTTP\n"
                + createRuntimeDatabasesYaml(), YamlMCPLaunchConfiguration.class));
        assertThat(actual.getTransportType(), is(MCPTransportType.STREAMABLE_HTTP));
        assertThat(actual.getHttpTransport().getBindHost(), is("127.0.0.1"));
        assertThat(actual.getHttpTransport().getPort(), is(18088));
        assertThat(actual.getHttpTransport().getEndpointPath(), is("/mcp"));
    }
    
    @Test
    void assertSwapToObjectWithStdioTransport() {
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal("transport:\n"
                + "  type: STDIO\n"
                + createRuntimeDatabasesYaml(), YamlMCPLaunchConfiguration.class));
        assertThat(actual.getTransportType(), is(MCPTransportType.STDIO));
    }
    
    @Test
    void assertSwapToObjectWithUnsupportedRootProperty() {
        ConstructorException actual = assertThrows(ConstructorException.class, () -> swapper.swapToObject(YamlEngine.unmarshal("runtime:\n"
                + "  props:\n"
                + "    databaseName: logic_db\n", YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), containsString("Unable to find property 'runtime'"));
    }
    
    @Test
    void assertSwapToObjectWithNumericRuntimeDatabaseName() {
        String yamlContent = "transport:\n"
                + "  type: STDIO\n"
                + "runtimeDatabases:\n"
                + "  1:\n"
                + "    jdbcUrl: jdbc:mysql://localhost:3306/logic_db\n"
                + "    username: demo\n"
                + "    password: ''\n"
                + "    driverClassName: com.mysql.cj.jdbc.Driver\n";
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class));
        assertThat(actual.getDatabases().get("1").getJdbcUrl(), is("jdbc:mysql://localhost:3306/logic_db"));
    }
    
    @Test
    void assertSwapToObjectWithBlankRuntimeDatabaseName() {
        String yamlContent = "transport:\n"
                + "  type: STDIO\n"
                + "runtimeDatabases:\n"
                + "  '':\n"
                + "    jdbcUrl: jdbc:mysql://localhost:3306/logic_db\n"
                + "    username: demo\n"
                + "    password: ''\n"
                + "    driverClassName: com.mysql.cj.jdbc.Driver\n";
        ConstructorException actual = assertThrows(ConstructorException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), containsString("YAML map key cannot be blank."));
    }
    
    @Test
    void assertSwapToObjectWithNullRuntimeDatabaseName() {
        String yamlContent = "transport:\n"
                + "  type: STDIO\n"
                + "runtimeDatabases:\n"
                + "  null:\n"
                + "    jdbcUrl: jdbc:mysql://localhost:3306/logic_db\n"
                + "    username: demo\n"
                + "    password: ''\n"
                + "    driverClassName: com.mysql.cj.jdbc.Driver\n";
        ConstructorException actual = assertThrows(ConstructorException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), containsString("YAML map key cannot be null."));
    }
    
    @Test
    void assertSwapToObjectWithNullRuntimeDatabaseConfiguration() {
        String yamlContent = "transport:\n"
                + "  type: STDIO\n"
                + "runtimeDatabases:\n"
                + "  logic_db:\n";
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases` contains null configuration for database `logic_db`."));
    }
    
    @Test
    void assertSwapToObjectWithUnsupportedRuntimeDatabaseProperty() {
        String yamlContent = "transport:\n"
                + "  type: STDIO\n"
                + "runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    jdbcUrl: jdbc:mysql://localhost:3306/logic_db\n"
                + "    username: demo\n"
                + "    password: ''\n"
                + "    driverClassName: com.mysql.cj.jdbc.Driver\n"
                + "    unsupported: true\n";
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases` contains unsupported property `unsupported` for database `logic_db`."));
    }
    
    @Test
    void assertSwapToObjectWithPlaceholderRuntimeDatabaseName() {
        String yamlContent = "transport:\n"
                + "  type: STDIO\n"
                + "runtimeDatabases:\n"
                + "  <logic-database>:\n"
                + "    jdbcUrl: jdbc:mysql://localhost:3306/logic_db\n"
                + "    username: demo\n"
                + "    password: ''\n"
                + "    driverClassName: com.mysql.cj.jdbc.Driver\n";
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases` contains placeholder database name `<logic-database>`."));
    }
    
    @Test
    void assertSwapToObjectWithPlaceholderJdbcUrl() {
        String yamlContent = "transport:\n"
                + "  type: STDIO\n"
                + "runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    jdbcUrl: jdbc:mysql://<proxy-host>:3306/logic_db\n"
                + "    username: demo\n"
                + "    password: ''\n"
                + "    driverClassName: com.mysql.cj.jdbc.Driver\n";
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases` contains placeholder database `logic_db` property `jdbcUrl`."));
    }
    
    @Test
    void assertSwapToObjectWithPlaceholderPassword() {
        String yamlContent = "transport:\n"
                + "  type: STDIO\n"
                + "runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    jdbcUrl: jdbc:mysql://localhost:3306/logic_db\n"
                + "    username: demo\n"
                + "    password: <proxy-password>\n"
                + "    driverClassName: com.mysql.cj.jdbc.Driver\n";
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases` contains placeholder database `logic_db` property `password`."));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> databases = new LinkedHashMap<>(1, 1F);
        databases.put("logic_db", new RuntimeDatabaseConfiguration("jdbc:mysql://localhost:3306/logic_db", "demo", "", "com.mysql.cj.jdbc.Driver"));
        MCPLaunchConfiguration launchConfig = new MCPLaunchConfiguration(MCPTransportType.STREAMABLE_HTTP, new HttpTransportConfiguration("127.0.0.1", 18088, "/mcp"), databases);
        YamlMCPLaunchConfiguration actual = swapper.swapToYamlConfiguration(launchConfig);
        assertThat(String.valueOf(actual.getRuntimeDatabases().get("logic_db").get("username")), is("demo"));
        assertThat(actual.getTransport().getType(), is(MCPTransportType.STREAMABLE_HTTP));
        assertThat(actual.getTransport().getHttp().getBindHost(), is("127.0.0.1"));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithStdioTransport() {
        MCPLaunchConfiguration launchConfig = new MCPLaunchConfiguration(MCPTransportType.STDIO, new HttpTransportConfiguration("127.0.0.1", 18088, "/mcp"), Collections.emptyMap());
        YamlMCPLaunchConfiguration actual = swapper.swapToYamlConfiguration(launchConfig);
        assertThat(actual.getTransport().getType(), is(MCPTransportType.STDIO));
        assertNull(actual.getTransport().getHttp());
    }
    
    private String createRuntimeDatabasesYaml() {
        return "runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    jdbcUrl: jdbc:mysql://localhost:3306/logic_db\n"
                + "    username: demo\n"
                + "    password: ''\n"
                + "    driverClassName: com.mysql.cj.jdbc.Driver\n";
    }
}
