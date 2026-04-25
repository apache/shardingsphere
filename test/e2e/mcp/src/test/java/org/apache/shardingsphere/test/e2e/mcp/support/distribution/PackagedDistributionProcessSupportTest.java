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
        assertThat(actual.command(), is(List.of(distributionHome.resolve("bin/start.sh").toString(), configFile.toString())));
        assertThat(actual.directory(), is(distributionHome.toFile()));
        assertThat(actual.environment().get("JAVA_HOME"), is(System.getProperty("java.home")));
    }
}
