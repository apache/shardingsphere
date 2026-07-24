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

package org.apache.shardingsphere.test.e2e.mcp.support.distribution;

import static org.hamcrest.Matchers.is;

import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.bootstrap.config.loader.MCPConfigurationLoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionTestSupport.PreparedPackagedDistribution;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackagedDistributionTestSupportTest {
    
    private static final String HTTP_CONFIGURATION = """
            transport:
              type: HTTP
            runtimeDatabases:
              orders:
                jdbcUrl: "jdbc:mysql://127.0.0.1:3306/orders"
                username: mcp
                password: mcp
                driverClassName: com.mysql.cj.jdbc.Driver
            """;
    
    private static final String STDIO_CONFIGURATION = """
            transport:
              type: STDIO
            runtimeDatabases:
              orders:
                jdbcUrl: "jdbc:mysql://127.0.0.1:3306/orders"
                username: mcp
                password: mcp
                driverClassName: com.mysql.cj.jdbc.Driver
            """;
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertFindDistributionHomeWithConfiguredHome() throws IOException {
        Path configuredHome = Files.createDirectories(tempDir.resolve("configured-home"));
        String actualOriginalHome = System.getProperty("mcp.distribution.home");
        System.setProperty("mcp.distribution.home", configuredHome.toString());
        try {
            Optional<Path> actual = PackagedDistributionTestSupport.findDistributionHome();
            assertThat(actual.orElseThrow(), is(configuredHome.toAbsolutePath().normalize()));
        } finally {
            restoreDistributionHome(actualOriginalHome);
        }
    }
    
    @Test
    void assertFindDistributionHomeWithRelativeConfiguredHome() throws IOException {
        Path configuredHome = Files.createDirectories(tempDir.resolve("distribution-home"));
        String actualOriginalHome = System.getProperty("mcp.distribution.home");
        String actualOriginalProjectDirectory = System.getProperty("maven.multiModuleProjectDirectory");
        System.setProperty("mcp.distribution.home", "distribution-home");
        System.setProperty("maven.multiModuleProjectDirectory", tempDir.toString());
        try {
            Optional<Path> actual = PackagedDistributionTestSupport.findDistributionHome();
            assertThat(actual.orElseThrow(), is(configuredHome.toAbsolutePath().normalize()));
        } finally {
            restoreDistributionHome(actualOriginalHome);
            restoreProjectDirectory(actualOriginalProjectDirectory);
        }
    }
    
    @Test
    void assertFindDistributionHomeWithMissingConfiguredHome() {
        Path configuredHome = tempDir.resolve("missing-home");
        String actualOriginalHome = System.getProperty("mcp.distribution.home");
        System.setProperty("mcp.distribution.home", configuredHome.toString());
        try {
            assertThrows(IllegalStateException.class, PackagedDistributionTestSupport::findDistributionHome);
        } finally {
            restoreDistributionHome(actualOriginalHome);
        }
    }
    
    @Test
    void assertPrepareFailsWithMissingDistributionHome() throws IOException {
        Path repositoryRoot = tempDir.resolve("repo");
        Files.createDirectories(repositoryRoot.resolve("distribution/mcp"));
        String actualOriginalHome = System.getProperty("mcp.distribution.home");
        String actualOriginalProjectDirectory = System.getProperty("maven.multiModuleProjectDirectory");
        System.clearProperty("mcp.distribution.home");
        System.setProperty("maven.multiModuleProjectDirectory", repositoryRoot.toString());
        try {
            IllegalStateException actual = assertThrows(IllegalStateException.class,
                    () -> PackagedDistributionTestSupport.prepare(tempDir.resolve("missing-distribution"), RuntimeTransport.HTTP));
            assertThat(actual.getMessage(), is("Packaged MCP distribution was not found. Run `./mvnw -pl distribution/mcp -am -DskipTests package` first"
                    + " or set `mcp.distribution.home` in env/e2e-env.properties or pass `-Dmcp.distribution.home=/path/to/apache-shardingsphere-mcp-*`. Checked `"
                    + repositoryRoot.resolve("distribution/mcp/target").toAbsolutePath().normalize() + "`."));
        } finally {
            restoreDistributionHome(actualOriginalHome);
            restoreProjectDirectory(actualOriginalProjectDirectory);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transportCases")
    void assertPrepareWithTransport(final String caseName, final RuntimeTransport transport, final MCPTransportType expectedTransportType) throws IOException {
        Path distributionHome = createDistributionHome(tempDir.resolve(caseName));
        String actualOriginalHome = System.getProperty("mcp.distribution.home");
        System.setProperty("mcp.distribution.home", distributionHome.toString());
        try {
            PreparedPackagedDistribution actual = PackagedDistributionTestSupport.prepare(tempDir.resolve(caseName + "-working"), transport);
            assertThat(actual.transport(), is(transport));
            assertFalse(Files.exists(actual.home().resolve("data")));
            assertFalse(Files.exists(actual.home().resolve("logs")));
            assertFalse(Files.exists(actual.home().resolve("plugins")));
            assertThat(actual.getStartScript(), is(PackagedDistributionProcessSupport.resolveStartScript(actual.home())));
            assertTrue(Files.exists(actual.getStartScript()));
            if ("start.sh".equals(actual.getStartScript().getFileName().toString())) {
                assertTrue(actual.getStartScript().toFile().canExecute());
            }
            MCPLaunchConfiguration actualConfig = MCPConfigurationLoader.load(actual.configFile().toString());
            assertThat(actualConfig.getTransportType(), is(expectedTransportType));
            if (RuntimeTransport.HTTP == transport) {
                assertThat(actualConfig.getHttpTransport().getPort(), is(0));
            } else {
                assertThat(actualConfig.getHttpTransport().getPort(), is(18088));
            }
        } finally {
            restoreDistributionHome(actualOriginalHome);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transportCases")
    void assertCreateDockerConfigurationFile(final String caseName, final RuntimeTransport transport, final MCPTransportType expectedTransportType) throws IOException {
        Path targetFile = tempDir.resolve(caseName + ".yaml");
        RuntimeDatabaseConfiguration expectedRuntimeDatabase = new RuntimeDatabaseConfiguration("jdbc:mysql://127.0.0.1:3306/orders", "mcp", "mcp", "com.mysql.cj.jdbc.Driver");
        Path actual = PackagedDistributionTestSupport.createDockerConfigurationFile(targetFile, transport, Map.of("logic_db", expectedRuntimeDatabase));
        assertThat(actual, is(targetFile));
        MCPLaunchConfiguration actualConfig = MCPConfigurationLoader.load(actual.toString());
        assertThat(actualConfig.getTransportType(), is(expectedTransportType));
        if (RuntimeTransport.HTTP == transport) {
            assertThat(actualConfig.getHttpTransport().getBindHost(), is("0.0.0.0"));
            assertThat(actualConfig.getHttpTransport().getPort(), is(18088));
            assertThat(actualConfig.getHttpTransport().getEndpointPath(), is("/mcp"));
        }
        RuntimeDatabaseConfiguration actualRuntimeDatabase = actualConfig.getDatabases().get("logic_db");
        assertThat(actualRuntimeDatabase.getJdbcUrl(), is(expectedRuntimeDatabase.getJdbcUrl()));
        assertThat(actualRuntimeDatabase.getUsername(), is(expectedRuntimeDatabase.getUsername()));
        assertThat(actualRuntimeDatabase.getPassword(), is(expectedRuntimeDatabase.getPassword()));
        assertThat(actualRuntimeDatabase.getDriverClassName(), is(expectedRuntimeDatabase.getDriverClassName()));
    }
    
    private static Stream<Arguments> transportCases() {
        return Stream.of(
                Arguments.of("http transport", RuntimeTransport.HTTP, MCPTransportType.HTTP),
                Arguments.of("stdio transport", RuntimeTransport.STDIO, MCPTransportType.STDIO));
    }
    
    private Path createDistributionHome(final Path target) throws IOException {
        Files.createDirectories(target.resolve("bin"));
        Files.createDirectories(target.resolve("conf"));
        Files.createDirectories(target.resolve("data"));
        Files.createDirectories(target.resolve("logs"));
        Files.createDirectories(target.resolve("plugins"));
        Files.writeString(target.resolve("bin/start.sh"), "#!/bin/sh\nexit 0\n");
        Files.writeString(target.resolve("bin/start.bat"), "@echo off\r\nexit /b 0\r\n");
        Files.writeString(target.resolve("conf/mcp-http.yaml"), HTTP_CONFIGURATION);
        Files.writeString(target.resolve("conf/mcp-stdio.yaml"), STDIO_CONFIGURATION);
        return target;
    }
    
    private void restoreDistributionHome(final String originalHome) {
        if (null == originalHome) {
            System.clearProperty("mcp.distribution.home");
            return;
        }
        System.setProperty("mcp.distribution.home", originalHome);
    }
    
    private void restoreProjectDirectory(final String originalProjectDirectory) {
        if (null == originalProjectDirectory) {
            System.clearProperty("maven.multiModuleProjectDirectory");
            return;
        }
        System.setProperty("maven.multiModuleProjectDirectory", originalProjectDirectory);
    }
}
