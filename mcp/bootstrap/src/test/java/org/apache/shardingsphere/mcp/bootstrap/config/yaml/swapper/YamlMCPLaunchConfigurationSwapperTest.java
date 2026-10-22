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
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
        String yamlContent = """
                transport:
                  type: HTTP
                  http:
                    bindHost: 127.0.0.1
                    port: 9090
                    endpointPath: /gateway
                runtimeDatabases:
                  logic_db:
                    jdbcUrl: jdbc:mysql://localhost:3306/logic_db
                    username: demo
                    password: secret
                    driverClassName: com.mysql.cj.jdbc.Driver
                """;
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class));
        assertThat(actual.getTransportType(), is(MCPTransportType.HTTP));
        assertThat(actual.getHttpTransport().getBindHost(), is("127.0.0.1"));
        assertThat(actual.getHttpTransport().getPort(), is(9090));
        assertThat(actual.getHttpTransport().getEndpointPath(), is("/gateway"));
        assertThat(actual.getDatabases().get("logic_db").getUsername(), is("demo"));
    }
    
    @Test
    void assertSwapToObjectWithPasswordMissing() {
        String yamlContent = """
                transport:
                  type: STDIO
                runtimeDatabases:
                  logic_db:
                    jdbcUrl: jdbc:mysql://localhost:3306/logic_db
                    username: demo
                    driverClassName: com.mysql.cj.jdbc.Driver
                """;
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class));
        assertThat(actual.getDatabases().get("logic_db").getPassword(), is(""));
    }
    
    @Test
    void assertSwapToObjectWithBlankDriverClassName() {
        String yamlContent = """
                transport:
                  type: STDIO
                runtimeDatabases:
                  logic_db:
                    jdbcUrl: jdbc:mysql://localhost:3306/logic_db
                    username: demo
                    driverClassName: ''
                """;
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases[logic_db].driverClassName` is required."));
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
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal("""
                runtimeDatabases:
                  logic_db:
                    jdbcUrl: jdbc:mysql://localhost:3306/logic_db
                    username: demo
                    password: ''
                    driverClassName: com.mysql.cj.jdbc.Driver
                """, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport` is required."));
    }
    
    @Test
    void assertSwapToObjectWithoutRuntimeDatabasesSection() {
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal("""
                transport:
                  type: STDIO
                """, YamlMCPLaunchConfiguration.class));
        assertThat(actual.getDatabases(), is(Collections.emptyMap()));
    }
    
    @Test
    void assertSwapToObjectWithNullRuntimeDatabases() {
        YamlMCPLaunchConfiguration yamlConfig = YamlEngine.unmarshal("""
                transport:
                  type: STDIO
                """, YamlMCPLaunchConfiguration.class);
        yamlConfig.setRuntimeDatabases(null);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases` is required."));
    }
    
    @Test
    void assertSwapToObjectWithEmptyRuntimeDatabases() {
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal("""
                transport:
                  type: STDIO
                runtimeDatabases: {}
                """, YamlMCPLaunchConfiguration.class));
        assertThat(actual.getDatabases(), is(Collections.emptyMap()));
    }
    
    @Test
    void assertSwapToObjectWithMissingTransportType() {
        YamlMCPLaunchConfiguration yamlConfig = YamlEngine.unmarshal("transport:\n"
                + "  http:\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + createRuntimeDatabasesYaml(), YamlMCPLaunchConfiguration.class);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.type` is required."));
    }
    
    @Test
    void assertSwapToObjectWithStdioTransportAndHttpConfiguration() {
        YamlMCPLaunchConfiguration yamlConfig = YamlEngine.unmarshal("transport:\n"
                + "  type: STDIO\n"
                + "  http:\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + createRuntimeDatabasesYaml(), YamlMCPLaunchConfiguration.class);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        assertThat(actual.getMessage(), is("transport.http is only valid when `transport.type` is HTTP."));
    }
    
    @Test
    void assertSwapToObjectWithHttpDefaults() {
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal("transport:\n"
                + "  type: HTTP\n"
                + createRuntimeDatabasesYaml(), YamlMCPLaunchConfiguration.class));
        assertThat(actual.getTransportType(), is(MCPTransportType.HTTP));
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
        ConstructorException actual = assertThrows(ConstructorException.class, () -> swapper.swapToObject(YamlEngine.unmarshal("""
                runtime:
                  props:
                    databaseName: logic_db
                """, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), containsString("Unable to find property 'runtime'"));
    }
    
    @Test
    void assertSwapToObjectWithNumericRuntimeDatabaseName() {
        String yamlContent = """
                transport:
                  type: STDIO
                runtimeDatabases:
                  1:
                    jdbcUrl: jdbc:mysql://localhost:3306/logic_db
                    username: demo
                    password: ''
                    driverClassName: com.mysql.cj.jdbc.Driver
                """;
        MCPLaunchConfiguration actual = swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class));
        assertThat(actual.getDatabases().get("1").getJdbcUrl(), is("jdbc:mysql://localhost:3306/logic_db"));
    }
    
    @Test
    void assertSwapToObjectWithBlankRuntimeDatabaseName() {
        String yamlContent = """
                transport:
                  type: STDIO
                runtimeDatabases:
                  '':
                    jdbcUrl: jdbc:mysql://localhost:3306/logic_db
                    username: demo
                    password: ''
                    driverClassName: com.mysql.cj.jdbc.Driver
                """;
        ConstructorException actual = assertThrows(ConstructorException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), containsString("YAML map key cannot be blank."));
    }
    
    @Test
    void assertSwapToObjectWithNullRuntimeDatabaseName() {
        String yamlContent = """
                transport:
                  type: STDIO
                runtimeDatabases:
                  null:
                    jdbcUrl: jdbc:mysql://localhost:3306/logic_db
                    username: demo
                    password: ''
                    driverClassName: com.mysql.cj.jdbc.Driver
                """;
        ConstructorException actual = assertThrows(ConstructorException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), containsString("YAML map key cannot be null."));
    }
    
    @Test
    void assertSwapToObjectWithNullRuntimeDatabaseConfiguration() {
        String yamlContent = """
                transport:
                  type: STDIO
                runtimeDatabases:
                  foo_{message}:
                """;
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases` contains null database configuration."));
    }
    
    @Test
    void assertSwapToObjectWithUnsupportedRuntimeDatabaseProperty() {
        String yamlContent = """
                transport:
                  type: STDIO
                runtimeDatabases:
                  logic_db:
                    jdbcUrl: jdbc:mysql://localhost:3306/logic_db
                    username: demo
                    password: ''
                    driverClassName: com.mysql.cj.jdbc.Driver
                    unsupported: true
                """;
        ConstructorException actual = assertThrows(ConstructorException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), containsString("Unable to find property 'unsupported'"));
    }
    
    @Test
    void assertSwapToObjectWithPlaceholderRuntimeDatabaseName() {
        String yamlContent = """
                transport:
                  type: STDIO
                runtimeDatabases:
                  <foo_{message}>:
                    jdbcUrl: jdbc:mysql://localhost:3306/logic_db
                    username: demo
                    password: ''
                    driverClassName: com.mysql.cj.jdbc.Driver
                """;
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases` contains placeholder database name."));
    }
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "JDBC URL, jdbc:mysql://<unresolved-host>:3306/logic_db, demo, com.mysql.cj.jdbc.Driver, jdbcUrl",
            "username, jdbc:mysql://localhost:3306/logic_db, <unresolved-username>, com.mysql.cj.jdbc.Driver, username",
            "driver class name, jdbc:mysql://localhost:3306/logic_db, demo, <unresolved-driver>, driverClassName"
    })
    void assertSwapToObjectWithPlaceholderRuntimeDatabaseProperty(final String name, final String jdbcUrl, final String username, final String driverClassName, final String propertyName) {
        String yamlContent = "transport:\n" + "  type: STDIO\n" + "runtimeDatabases:\n" + "  foo_{message}:\n" + "    jdbcUrl: " + jdbcUrl + "\n" + "    username: " + username + "\n"
                + "    password: ''\n" + "    driverClassName: " + driverClassName + "\n";
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), is(String.format("MCP launch configuration property `runtimeDatabases` contains placeholder database property `%s`.", propertyName)));
    }
    
    @Test
    void assertSwapToObjectWithPlaceholderPassword() {
        String yamlContent = """
                transport:
                  type: STDIO
                runtimeDatabases:
                  foo_{message}:
                    jdbcUrl: jdbc:mysql://localhost:3306/logic_db
                    username: demo
                    password: <unresolved-password>
                    driverClassName: com.mysql.cj.jdbc.Driver
                """;
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases` contains placeholder database property `password`."));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> databases = new LinkedHashMap<>(1, 1F);
        databases.put("logic_db", new RuntimeDatabaseConfiguration("jdbc:mysql://localhost:3306/logic_db", "demo", "", "com.mysql.cj.jdbc.Driver"));
        MCPLaunchConfiguration launchConfig = new MCPLaunchConfiguration(new HttpTransportConfiguration("127.0.0.1", 18088, "/mcp"), databases);
        YamlMCPLaunchConfiguration actual = swapper.swapToYamlConfiguration(launchConfig);
        assertThat(actual.getRuntimeDatabases().get("logic_db").getUsername(), is("demo"));
        assertThat(actual.getTransport().getType(), is(MCPTransportType.HTTP));
        assertThat(actual.getTransport().getHttp().getBindHost(), is("127.0.0.1"));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithStdioTransport() {
        MCPLaunchConfiguration launchConfig = new MCPLaunchConfiguration(Collections.emptyMap());
        YamlMCPLaunchConfiguration actual = swapper.swapToYamlConfiguration(launchConfig);
        assertThat(actual.getTransport().getType(), is(MCPTransportType.STDIO));
        assertNull(actual.getTransport().getHttp());
    }
    
    private String createRuntimeDatabasesYaml() {
        return """
                runtimeDatabases:
                  logic_db:
                    jdbcUrl: jdbc:mysql://localhost:3306/logic_db
                    username: demo
                    password: ''
                    driverClassName: com.mysql.cj.jdbc.Driver
                """;
    }
}
