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

import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.loader.MCPConfigurationLoader;
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
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackagedDistributionTestSupportTest {
    
    private static final String HTTP_CONFIGURATION = """
            transport:
              http:
                enabled: true
                bindHost: 127.0.0.1
                allowRemoteAccess: false
                port: 18088
                endpointPath: /mcp
              stdio:
                enabled: false
            runtimeDatabases:
              orders:
                databaseType: H2
                jdbcUrl: "jdbc:h2:mem:orders"
                username: ""
                password: ""
                driverClassName: org.h2.Driver
            """;
    
    private static final String STDIO_CONFIGURATION = """
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
              orders:
                databaseType: H2
                jdbcUrl: "jdbc:h2:mem:orders"
                username: ""
                password: ""
                driverClassName: org.h2.Driver
            """;
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertFindDistributionHomeWithConfiguredHome() throws IOException {
        final Path configuredHome = Files.createDirectories(tempDir.resolve("configured-home"));
        final String actualOriginalHome = System.getProperty("mcp.distribution.home");
        System.setProperty("mcp.distribution.home", configuredHome.toString());
        try {
            final Optional<Path> actual = PackagedDistributionTestSupport.findDistributionHome();
            assertThat(actual.orElseThrow(), is(configuredHome.toAbsolutePath().normalize()));
        } finally {
            restoreDistributionHome(actualOriginalHome);
        }
    }
    
    @Test
    void assertFindDistributionHomeWithMissingConfiguredHome() {
        final Path configuredHome = tempDir.resolve("missing-home");
        final String actualOriginalHome = System.getProperty("mcp.distribution.home");
        System.setProperty("mcp.distribution.home", configuredHome.toString());
        try {
            assertThrows(IllegalStateException.class, PackagedDistributionTestSupport::findDistributionHome);
        } finally {
            restoreDistributionHome(actualOriginalHome);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("prepareCases")
    void assertPrepareWithTransport(final String caseName, final RuntimeTransport transport, final boolean httpEnabled, final boolean stdioEnabled) throws IOException {
        final Path distributionHome = createDistributionHome(tempDir.resolve(caseName));
        final String actualOriginalHome = System.getProperty("mcp.distribution.home");
        System.setProperty("mcp.distribution.home", distributionHome.toString());
        try {
            final PreparedPackagedDistribution actual = PackagedDistributionTestSupport.prepare(tempDir.resolve(caseName + "-working"), transport);
            final MCPLaunchConfiguration actualConfig = MCPConfigurationLoader.load(actual.configFile().toString());
            assertThat(actual.transport(), is(transport));
            assertFalse(Files.exists(actual.home().resolve("data")));
            assertFalse(Files.exists(actual.home().resolve("logs")));
            assertFalse(Files.exists(actual.home().resolve("plugins")));
            assertThat(actual.getStartScript(), is(PackagedDistributionProcessSupport.resolveStartScript(actual.home())));
            assertTrue(Files.exists(actual.getStartScript()));
            if ("start.sh".equals(actual.getStartScript().getFileName().toString())) {
                assertTrue(actual.getStartScript().toFile().canExecute());
            }
            assertThat(actualConfig.getHttpTransport().isEnabled(), is(httpEnabled));
            assertThat(actualConfig.getStdioTransport().isEnabled(), is(stdioEnabled));
            if (httpEnabled) {
                assertThat(actual.httpPort(), greaterThan(0));
                assertThat(actualConfig.getHttpTransport().getPort(), is(actual.httpPort()));
            } else {
                assertThat(actual.httpPort(), is(-1));
                assertThat(actualConfig.getHttpTransport().getPort(), is(18088));
            }
        } finally {
            restoreDistributionHome(actualOriginalHome);
        }
    }
    
    private static Stream<Arguments> prepareCases() {
        return Stream.of(
                Arguments.of("http transport", RuntimeTransport.HTTP, true, false),
                Arguments.of("stdio transport", RuntimeTransport.STDIO, false, true));
    }
    
    private Path createDistributionHome(final Path target) throws IOException {
        Files.createDirectories(target.resolve("bin"));
        Files.createDirectories(target.resolve("conf"));
        Files.createDirectories(target.resolve("data"));
        Files.createDirectories(target.resolve("logs"));
        Files.createDirectories(target.resolve("plugins"));
        Files.writeString(target.resolve("bin/start.sh"), "#!/bin/sh\nexit 0\n");
        Files.writeString(target.resolve("bin/start.bat"), "@echo off\r\nexit /b 0\r\n");
        Files.writeString(target.resolve("conf/mcp.yaml"), HTTP_CONFIGURATION);
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
}
