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

package org.apache.shardingsphere.mcp.bootstrap.config.loader;

import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.runtime.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.constructor.ConstructorException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPConfigurationLoaderTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertLoadWithExplicitTransportConfiguration() throws IOException {
        Path configFile = createConfigFile("transport:\n"
                + "  http:\n"
                + "    enabled: true\n"
                + "    bindHost: 0.0.0.0\n"
                + "    port: 9090\n"
                + "    endpointPath: /gateway\n"
                + "  stdio:\n"
                + "    enabled: false\n"
                + "runtimeDatabases: {}\n");
        
        MCPLaunchConfiguration actual = MCPConfigurationLoader.load(configFile.toString());
        assertTrue(actual.getHttpTransport().isEnabled());
        assertFalse(actual.getStdioTransport().isEnabled());
        assertThat(actual.getHttpTransport().getBindHost(), is("0.0.0.0"));
        assertThat(actual.getHttpTransport().getPort(), is(9090));
        assertThat(actual.getHttpTransport().getEndpointPath(), is("/gateway"));
        assertTrue(actual.getDatabases().isEmpty());
    }
    
    @Test
    void assertLoadWithEmptyContent() throws IOException {
        Path configFile = createConfigFile("");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> MCPConfigurationLoader.load(configFile.toString()));
        assertThat(actual.getMessage(), is("Property `transport` is required."));
    }
    
    @Test
    void assertLoadWithRuntimeDatabasesAndNoTransport() throws IOException {
        Path configFile = createConfigFile("runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: jdbc:h2:mem:logic\n"
                + "    username: ''\n"
                + "    password: ''\n"
                + "    driverClassName: org.h2.Driver\n");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> MCPConfigurationLoader.load(configFile.toString()));
        assertThat(actual.getMessage(), is("Property `transport` is required."));
    }
    
    @Test
    void assertLoadWithUnsupportedLegacyRuntimeSection() throws IOException {
        Path configFile = createConfigFile("runtime:\n"
                + "  props:\n"
                + "    databaseName: logic_db\n");
        ConstructorException actual = assertThrows(ConstructorException.class, () -> MCPConfigurationLoader.load(configFile.toString()));
        assertThat(actual.getMessage(), containsString("Unable to find property 'runtime'"));
    }
    
    @Test
    void assertLoadWithRuntimeDatabases() throws IOException {
        Path configFile = createConfigFile("transport:\n"
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
                + "  analytics_db:\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: jdbc:h2:mem:analytics\n"
                + "    username: analytics\n"
                + "    password: analytics-pass\n"
                + "    driverClassName: org.h2.Driver\n");
        MCPLaunchConfiguration actual = MCPConfigurationLoader.load(configFile.toString());
        Map<String, RuntimeDatabaseConfiguration> actualDatabases = actual.getDatabases();
        assertThat(actualDatabases.size(), is(2));
        assertThat(actualDatabases.get("logic_db").getDatabaseType(), is("H2"));
        assertThat(actualDatabases.get("logic_db").getUsername(), is(""));
        assertThat(actualDatabases.get("analytics_db").getUsername(), is("analytics"));
    }
    
    @Test
    void assertLoadPackagedDistributionConfiguration() throws IOException {
        MCPLaunchConfiguration actual = MCPConfigurationLoader.load("distribution/mcp/src/main/resources/conf/mcp.yaml");
        assertTrue(actual.getHttpTransport().isEnabled());
        assertFalse(actual.getStdioTransport().isEnabled());
        assertThat(actual.getDatabases().size(), is(2));
        assertThat(actual.getDatabases().get("orders").getUsername(), is(""));
        assertThat(actual.getDatabases().get("billing").getPassword(), is(""));
    }
    
    @Test
    void assertLoadWithoutRuntimeDatabasesSection() throws IOException {
        Path configFile = createConfigFile("transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: true\n");
        MCPLaunchConfiguration actual = MCPConfigurationLoader.load(configFile.toString());
        assertTrue(actual.getDatabases().isEmpty());
    }
    
    @Test
    void assertLoadWithUnsupportedRuntimeDatabaseProperty() throws IOException {
        Path configFile = createConfigFile("transport:\n"
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
                + "    supportsCrossSchemaSql: true\n");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> MCPConfigurationLoader.load(configFile.toString()));
        assertThat(actual.getMessage(), is("Unsupported runtime database property `supportsCrossSchemaSql`."));
    }
    
    @Test
    void assertLoadWithNegativePort() throws IOException {
        Path configFile = createConfigFile("transport:\n"
                + "  http:\n"
                + "    enabled: true\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: -1\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: false\n"
                + "runtimeDatabases: {}\n");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> MCPConfigurationLoader.load(configFile.toString()));
        assertThat(actual.getMessage(), is("Property `transport.http.port` cannot be negative."));
    }
    
    @Test
    void assertLoadWithDisabledHttpStillValidatesServerConfiguration() throws IOException {
        Path configFile = createConfigFile("transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    bindHost: 127.0.0.1\n"
                + "    port: -1\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: true\n"
                + "runtimeDatabases: {}\n");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> MCPConfigurationLoader.load(configFile.toString()));
        assertThat(actual.getMessage(), is("Property `transport.http.port` cannot be negative."));
    }
    
    private Path createConfigFile(final String yamlContent) throws IOException {
        Path result = tempDir.resolve("mcp.yaml");
        Files.writeString(result, yamlContent);
        return result;
    }
}
