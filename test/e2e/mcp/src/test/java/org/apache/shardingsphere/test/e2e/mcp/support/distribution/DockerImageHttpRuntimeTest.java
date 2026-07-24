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
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DockerImageHttpRuntimeTest {
    
    @Test
    void assertCreateDockerCommandUsesRuntimeAssignedPort() {
        List<String> actual = DockerImageHttpRuntime.createDockerCommand("shardingsphere-mcp:test", Path.of("mcp.yaml"), "mcp-e2e-test");
        assertThat(actual.get(actual.indexOf("-p") + 1), is("127.0.0.1::18088"));
        assertThat(actual, hasItems("--name", "mcp-e2e-test", "SHARDINGSPHERE_MCP_TRANSPORT=http", "shardingsphere-mcp:test"));
    }
    
    @Test
    void assertParsePublishedIPv4Port() {
        assertThat(DockerImageHttpRuntime.parsePublishedPort("127.0.0.1:49152\n").orElseThrow(), is(49152));
    }
    
    @Test
    void assertParsePublishedIPv6Port() {
        assertThat(DockerImageHttpRuntime.parsePublishedPort("[::1]:49153\n").orElseThrow(), is(49153));
    }
    
    @Test
    void assertParsePublishedPortRejectsMissingPort() {
        assertTrue(DockerImageHttpRuntime.parsePublishedPort("published port is unavailable").isEmpty());
    }
    
    @Test
    void assertParsePublishedPortRejectsOutOfRangePort() {
        assertTrue(DockerImageHttpRuntime.parsePublishedPort("127.0.0.1:70000").isEmpty());
    }
}
