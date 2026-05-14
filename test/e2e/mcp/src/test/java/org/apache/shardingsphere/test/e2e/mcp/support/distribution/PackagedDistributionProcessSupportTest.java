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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("UseOfProcessBuilder")
class PackagedDistributionProcessSupportTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertCreateProcessBuilderUsesDistributionHomeAndConfigFile() {
        Path distributionHome = Path.of("/tmp/mcp-dist");
        Path configFile = distributionHome.resolve("conf/mcp-http.yaml");
        ProcessBuilder actual = PackagedDistributionProcessSupport.createProcessBuilder(distributionHome, configFile);
        assertThat(actual.command(), is(PackagedDistributionProcessSupport.createCommand(distributionHome, configFile, System.getProperty("os.name", ""))));
        assertThat(actual.directory(), is(distributionHome.toFile()));
    }
    
    @Test
    void assertCreateCommandForUnix() {
        Path distributionHome = Path.of("/tmp/mcp-dist");
        Path configFile = distributionHome.resolve("conf/mcp-http.yaml");
        List<String> actual = PackagedDistributionProcessSupport.createCommand(distributionHome, configFile, "Linux");
        assertThat(actual, is(List.of(distributionHome.resolve("bin/start.sh").toString(), configFile.toString())));
    }
    
    @Test
    void assertCreateCommandForWindows() {
        Path distributionHome = Path.of("/tmp/mcp-dist");
        Path configFile = distributionHome.resolve("conf/mcp-http.yaml");
        List<String> actual = PackagedDistributionProcessSupport.createCommand(distributionHome, configFile, "Windows 11");
        assertThat(actual, is(List.of("cmd", "/c", distributionHome.resolve("bin/start.bat").toString(), configFile.toString())));
    }
    
    @Test
    void assertResolveStartScriptForUnix() {
        Path distributionHome = Path.of("/tmp/mcp-dist");
        Path actual = PackagedDistributionProcessSupport.resolveStartScript(distributionHome, "Linux");
        assertThat(actual, is(distributionHome.resolve("bin/start.sh")));
    }
    
    @Test
    void assertResolveStartScriptForWindows() {
        Path distributionHome = Path.of("/tmp/mcp-dist");
        Path actual = PackagedDistributionProcessSupport.resolveStartScript(distributionHome, "Windows 11");
        assertThat(actual, is(distributionHome.resolve("bin/start.bat")));
    }
    
    @Test
    void assertPrepareRuntimeLayoutCreatesRuntimeDirectories() throws IOException {
        Path distributionHome = tempDir.resolve("distribution");
        Path configFile = distributionHome.resolve("conf/mcp-http.yaml");
        Files.createDirectories(distributionHome.resolve("conf"));
        Files.createDirectories(distributionHome.resolve("lib"));
        Files.writeString(configFile, "runtimeDatabases: {}\n");
        PackagedDistributionProcessSupport.prepareRuntimeLayout(distributionHome, configFile);
        assertTrue(Files.isDirectory(distributionHome.resolve("data")));
        assertTrue(Files.isDirectory(distributionHome.resolve("logs")));
        assertTrue(Files.isDirectory(distributionHome.resolve("plugins")));
    }
    
    @Test
    void assertPrepareRuntimeLayoutFailsWithMissingConfigFile() throws IOException {
        Path distributionHome = tempDir.resolve("distribution");
        Path configFile = distributionHome.resolve("conf/mcp-http.yaml");
        Files.createDirectories(distributionHome.resolve("lib"));
        IOException actual = assertThrows(IOException.class, () -> PackagedDistributionProcessSupport.prepareRuntimeLayout(distributionHome, configFile));
        assertThat(actual.getMessage(), is("MCP configuration file `" + configFile + "` does not exist."));
    }
    
    @Test
    void assertPrepareRuntimeLayoutFailsWithMissingRuntimeLibraries() throws IOException {
        Path distributionHome = tempDir.resolve("distribution");
        Path configFile = distributionHome.resolve("conf/mcp-http.yaml");
        Files.createDirectories(distributionHome.resolve("conf"));
        Files.writeString(configFile, "runtimeDatabases: {}\n");
        IOException actual = assertThrows(IOException.class, () -> PackagedDistributionProcessSupport.prepareRuntimeLayout(distributionHome, configFile));
        assertThat(actual.getMessage(), is("MCP runtime libraries are missing under `" + distributionHome.resolve("lib") + "`."));
    }
}
