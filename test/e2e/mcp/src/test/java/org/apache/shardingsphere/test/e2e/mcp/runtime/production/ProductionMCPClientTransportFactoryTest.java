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

package org.apache.shardingsphere.test.e2e.mcp.runtime.production;

import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.ProtocolVersions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProductionMCPClientTransportFactoryTest {
    
    @Test
    void assertCreateHttpClientTransport() {
        McpClientTransport actual = ProductionMCPClientTransportFactory.createHttpClientTransport(URI.create("http://127.0.0.1:8080/mcp"));
        try {
            assertThat(actual.protocolVersions(), is(List.of(ProtocolVersions.MCP_2025_11_25)));
        } finally {
            actual.closeGracefully().block();
        }
    }
    
    @Test
    void assertCreateStdioClientTransport(@TempDir final Path tempDir) throws IOException {
        StdioClientTransport actual = ProductionMCPClientTransportFactory.createStdioClientTransport(tempDir.resolve("mcp.yaml"));
        try {
            assertThat(actual.protocolVersions(), is(List.of(ProtocolVersions.MCP_2025_11_25)));
        } finally {
            actual.closeGracefully().block();
        }
    }
}
