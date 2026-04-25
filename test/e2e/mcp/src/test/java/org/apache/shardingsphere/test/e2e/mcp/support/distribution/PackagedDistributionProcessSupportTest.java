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

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class PackagedDistributionProcessSupportTest {
    
    @Test
    void assertCreateProcessBuilderUsesDistributionHomeAndConfigFile() {
        Path distributionHome = Path.of("/tmp/mcp-dist");
        Path configFile = distributionHome.resolve("conf/mcp.yaml");
        ProcessBuilder actual = PackagedDistributionProcessSupport.createProcessBuilder(distributionHome, configFile);
        assertThat(actual.command(), is(PackagedDistributionProcessSupport.createCommand(
                distributionHome, configFile, System.getProperty("os.name", ""), getWindowsCommandInterpreter())));
        assertThat(actual.directory(), is(distributionHome.toFile()));
        assertThat(actual.environment().get("JAVA_HOME"), is(System.getProperty("java.home")));
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
    void assertCreateCommandForWindows() {
        Path distributionHome = Path.of("/tmp/mcp-dist");
        Path configFile = distributionHome.resolve("conf/mcp.yaml");
        List<String> actual = PackagedDistributionProcessSupport.createCommand(distributionHome, configFile, "Windows Server 2025", "cmd.exe");
        assertThat(actual, is(List.of("cmd.exe", "/c", distributionHome.resolve("bin/start.bat").toString(), configFile.toString())));
    }
    
    private String getWindowsCommandInterpreter() {
        String actual = System.getenv("ComSpec");
        return null == actual || actual.trim().isEmpty() ? "cmd.exe" : actual.trim();
    }
}
