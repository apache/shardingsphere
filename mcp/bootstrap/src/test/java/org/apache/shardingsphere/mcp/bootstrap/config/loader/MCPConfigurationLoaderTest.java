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
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
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
                + "    endpointPath: gateway\n"
                + "  stdio:\n"
                + "    enabled: false\n");
        
        MCPLaunchConfiguration actual = MCPConfigurationLoader.load(configFile.toString());
        
        assertTrue(actual.getTransport().getHttp().isEnabled());
        assertFalse(actual.getTransport().getStdio().isEnabled());
        assertThat(actual.getTransport().getHttp().getBindHost(), is("0.0.0.0"));
        assertThat(actual.getTransport().getHttp().getPort(), is(9090));
        assertThat(actual.getTransport().getHttp().getEndpointPath(), is("/gateway"));
        assertTrue(actual.getRuntimeProps().isEmpty());
    }
    
    @Test
    void assertLoadWithDefaults() throws IOException {
        Path configFile = createConfigFile("");
        
        MCPLaunchConfiguration actual = MCPConfigurationLoader.load(configFile.toString());
        
        assertTrue(actual.getTransport().getHttp().isEnabled());
        assertTrue(actual.getTransport().getStdio().isEnabled());
        assertThat(actual.getTransport().getHttp().getBindHost(), is("127.0.0.1"));
        assertThat(actual.getTransport().getHttp().getPort(), is(18088));
        assertThat(actual.getTransport().getHttp().getEndpointPath(), is("/mcp"));
        assertTrue(actual.getRuntimeProps().isEmpty());
    }
    
    @Test
    void assertLoadWithRuntimeProps() throws IOException {
        Path configFile = createConfigFile("runtime:\n"
                + "  props:\n"
                + "    databaseName: logic_db\n"
                + "    databaseType: H2\n");
        
        MCPLaunchConfiguration actual = MCPConfigurationLoader.load(configFile.toString());
        Properties actualProps = actual.getRuntimeProps();
        
        assertThat(actualProps.getProperty("databaseName"), is("logic_db"));
        assertThat(actualProps.getProperty("databaseType"), is("H2"));
    }
    
    @Test
    void assertLoadWithRuntimeDatabases() throws IOException {
        Path configFile = createConfigFile("runtime:\n"
                + "  defaults:\n"
                + "    databaseType: H2\n"
                + "    driverClassName: org.h2.Driver\n"
                + "    schemaPattern: public\n"
                + "    defaultSchema: public\n"
                + "  databases:\n"
                + "    logic_db:\n"
                + "      jdbcUrl: jdbc:h2:mem:logic\n"
                + "    analytics_db:\n"
                + "      jdbcUrl: jdbc:h2:mem:analytics\n"
                + "      supportsCrossSchemaSql: true\n");
        
        MCPLaunchConfiguration actual = MCPConfigurationLoader.load(configFile.toString());
        Map<String, RuntimeDatabaseConfiguration> actualDatabases = actual.getRuntimeTopologyConfiguration().getDatabases();
        
        assertTrue(actual.getRuntimeProps().isEmpty());
        assertThat(actualDatabases.size(), is(2));
        assertThat(actualDatabases.get("logic_db").getDatabaseType(), is("H2"));
        assertThat(actualDatabases.get("logic_db").getDriverClassName(), is("org.h2.Driver"));
        assertThat(actualDatabases.get("logic_db").getSchemaPattern(), is("public"));
        assertFalse(actualDatabases.get("logic_db").isSupportsCrossSchemaSql());
        assertTrue(actualDatabases.get("analytics_db").isSupportsCrossSchemaSql());
    }
    
    @Test
    void assertLoadWithRuntimePropsAndDatabases() throws IOException {
        Path configFile = createConfigFile("runtime:\n"
                + "  props:\n"
                + "    databaseName: logic_db\n"
                + "  databases:\n"
                + "    logic_db:\n"
                + "      databaseType: H2\n"
                + "      jdbcUrl: jdbc:h2:mem:logic\n");
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> MCPConfigurationLoader.load(configFile.toString()));
        
        assertThat(actual.getMessage(), is("`runtime.props` and `runtime.databases` cannot be configured together."));
    }
    
    @Test
    void assertLoadWithNegativePort() throws IOException {
        Path configFile = createConfigFile("transport:\n"
                + "  http:\n"
                + "    enabled: true\n"
                + "    port: -1\n");
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> MCPConfigurationLoader.load(configFile.toString()));
        
        assertThat(actual.getMessage(), is("MCP server port cannot be negative."));
    }
    
    @Test
    void assertLoadWithDisabledHttpIgnoresServerConfiguration() throws IOException {
        Path configFile = createConfigFile("transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    port: -1\n"
                + "  stdio:\n"
                + "    enabled: true\n");
        
        MCPLaunchConfiguration actual = MCPConfigurationLoader.load(configFile.toString());
        
        assertFalse(actual.getTransport().getHttp().isEnabled());
        assertTrue(actual.getTransport().getStdio().isEnabled());
        assertThat(actual.getTransport().getHttp().getPort(), is(18088));
    }
    
    private Path createConfigFile(final String yamlContent) throws IOException {
        Path result = tempDir.resolve("mcp.yaml");
        Files.writeString(result, yamlContent);
        return result;
    }
}
