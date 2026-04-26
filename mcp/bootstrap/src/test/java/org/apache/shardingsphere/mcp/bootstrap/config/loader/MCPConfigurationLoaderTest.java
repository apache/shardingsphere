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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPConfigurationLoaderTest {
    
    private static final String HTTP_CONFIGURATION_YAML = """
            transport:
              http:
                enabled: true
                bindHost: 127.0.0.1
                allowRemoteAccess: false
                port: 9090
                endpointPath: /gateway
              stdio:
                enabled: false
            runtimeDatabases: {}
            """;
    
    private static final String RUNTIME_DATABASE_CONFIGURATION_YAML = """
            transport:
              http:
                enabled: false
                bindHost: 127.0.0.1
                allowRemoteAccess: false
                port: 18088
                endpointPath: /mcp
              stdio:
                enabled: true
            runtimeDatabases:
              logic_db:
                databaseType: H2
                jdbcUrl: jdbc:h2:mem:logic
                username: ''
                password: ''
                driverClassName: org.h2.Driver
            """;
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertLoadWithExistingConfigurationFile() throws IOException {
        Path configFile = createConfigFile(tempDir, "mcp.yaml", RUNTIME_DATABASE_CONFIGURATION_YAML);
        MCPLaunchConfiguration actual = MCPConfigurationLoader.load(configFile.toString());
        assertFalse(actual.getHttpTransport().isEnabled());
        assertTrue(actual.getStdioTransport().isEnabled());
        assertThat(actual.getDatabases().size(), is(1));
        assertThat(actual.getDatabases().get("logic_db").getDatabaseType(), is("H2"));
        assertThat(actual.getDatabases().get("logic_db").getJdbcUrl(), is("jdbc:h2:mem:logic"));
    }
    
    @Test
    void assertLoadWithBlankConfigurationPath() {
        FileNotFoundException actual = assertThrows(FileNotFoundException.class, () -> MCPConfigurationLoader.load("   "));
        assertThat(actual.getMessage(), is("MCP configuration path cannot be blank."));
    }
    
    @Test
    void assertLoadWithMissingConfigurationFile() {
        String actualConfigPath = tempDir.resolve("missing.yaml").toString();
        FileNotFoundException actual = assertThrows(FileNotFoundException.class, () -> MCPConfigurationLoader.load(actualConfigPath));
        assertThat(actual.getMessage(), is(String.format("MCP configuration file `%s` does not exist.", actualConfigPath)));
    }
    
    @Test
    void assertLoadWithConfigurationFileInParentDirectory() throws IOException {
        Path searchBaseDirectory = Files.createTempDirectory(Path.of("").toAbsolutePath().resolve("..").normalize(), "mcp-config-loader-");
        try {
            createConfigFile(searchBaseDirectory, "conf/mcp.yaml", HTTP_CONFIGURATION_YAML);
            String actualConfigPath = searchBaseDirectory.getFileName().resolve("conf").resolve("mcp.yaml").toString();
            MCPLaunchConfiguration actual = MCPConfigurationLoader.load(actualConfigPath);
            assertTrue(actual.getHttpTransport().isEnabled());
            assertFalse(actual.getStdioTransport().isEnabled());
            assertThat(actual.getHttpTransport().getBindHost(), is("127.0.0.1"));
            assertThat(actual.getHttpTransport().getPort(), is(9090));
            assertTrue(actual.getDatabases().isEmpty());
        } finally {
            deleteDirectory(searchBaseDirectory);
        }
    }
    
    @Test
    void assertLoadPackagedDistributionConfiguration() throws IOException {
        MCPLaunchConfiguration actual = MCPConfigurationLoader.load("distribution/mcp/src/main/resources/conf/mcp.yaml");
        assertTrue(actual.getHttpTransport().isEnabled());
        assertFalse(actual.getStdioTransport().isEnabled());
        assertFalse(actual.getHttpTransport().isAllowRemoteAccess());
        assertThat(actual.getHttpTransport().getAccessToken(), is(""));
        assertThat(actual.getDatabases().size(), is(2));
        assertThat(actual.getDatabases().get("orders").getUsername(), is(""));
        assertThat(actual.getDatabases().get("billing").getPassword(), is(""));
    }
    
    private Path createConfigFile(final Path baseDirectory, final String relativePath, final String yamlContent) throws IOException {
        Path result = baseDirectory.resolve(relativePath);
        if (null != result.getParent()) {
            Files.createDirectories(result.getParent());
        }
        Files.writeString(result, yamlContent);
        return result;
    }
    
    private void deleteDirectory(final Path directory) throws IOException {
        if (Files.notExists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path each : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(each);
            }
        }
    }
}
