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

import org.apache.shardingsphere.mcp.bootstrap.lifecycle.MCPRuntimeLauncher.RuntimeConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        Path configFile = createConfigFile("server:\n"
                + "  bindHost: 0.0.0.0\n"
                + "  port: 9090\n"
                + "  endpointPath: gateway\n"
                + "transport:\n"
                + "  http:\n"
                + "    enabled: true\n"
                + "  stdio:\n"
                + "    enabled: false\n");
        
        RuntimeConfiguration actual = MCPConfigurationLoader.load(configFile.toString());
        
        assertTrue(actual.isHttpEnabled());
        assertFalse(actual.isStdioEnabled());
        assertThat(actual.getServerConfiguration().getBindHost(), is("0.0.0.0"));
        assertThat(actual.getServerConfiguration().getPort(), is(9090));
        assertThat(actual.getServerConfiguration().getEndpointPath(), is("/gateway"));
        assertFalse(actual.getRuntimeProps().isPresent());
    }
    
    @Test
    void assertLoadWithDefaults() throws IOException {
        Path configFile = createConfigFile("");
        
        RuntimeConfiguration actual = MCPConfigurationLoader.load(configFile.toString());
        
        assertTrue(actual.isHttpEnabled());
        assertTrue(actual.isStdioEnabled());
        assertThat(actual.getServerConfiguration().getBindHost(), is("127.0.0.1"));
        assertThat(actual.getServerConfiguration().getPort(), is(8088));
        assertThat(actual.getServerConfiguration().getEndpointPath(), is("/mcp"));
        assertFalse(actual.getRuntimeProps().isPresent());
    }
    
    @Test
    void assertLoadWithRuntimeProps() throws IOException {
        Path configFile = createConfigFile("runtime:\n"
                + "  props:\n"
                + "    databaseName: logic_db\n"
                + "    databaseType: H2\n");
        
        RuntimeConfiguration actual = MCPConfigurationLoader.load(configFile.toString());
        Properties actualProps = actual.getRuntimeProps().orElseThrow();
        
        assertThat(actualProps.getProperty("databaseName"), is("logic_db"));
        assertThat(actualProps.getProperty("databaseType"), is("H2"));
    }
    
    @Test
    void assertLoadWithNegativePort() throws IOException {
        Path configFile = createConfigFile("server:\n"
                + "  port: -1\n");
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> MCPConfigurationLoader.load(configFile.toString()));
        
        assertThat(actual.getMessage(), is("MCP server port cannot be negative."));
    }
    
    private Path createConfigFile(final String yamlContent) throws IOException {
        Path result = tempDir.resolve("mcp.yaml");
        Files.writeString(result, yamlContent);
        return result;
    }
}
