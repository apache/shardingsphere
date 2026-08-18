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

package org.apache.shardingsphere.test.e2e.mcp.functionality;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.mcp.bootstrap.MCPBootstrap;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportJsonMapperFactory;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionProtocolSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPStdioLogbackConfiguration;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.function.BiFunction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPClientTransportFactory {
    
    private static final String STDIO_LOGBACK_CONFIG_FILE_NAME = "mcp-functionality-e2e-sdk-stdio-logback.xml";
    
    private static final List<String> SUPPORTED_PROTOCOL_VERSIONS = List.of(MCPInteractionProtocolSupport.PROTOCOL_VERSION);
    
    static McpSyncClient createElicitationClient(final McpClientTransport clientTransport, final List<McpSchema.ElicitRequest> elicitationRequests,
                                                 final BiFunction<List<McpSchema.ElicitRequest>, McpSchema.ElicitRequest, McpSchema.ElicitResult> elicitationHandler) {
        return McpClient.sync(clientTransport)
                .clientInfo(new McpSchema.Implementation("mcp-functionality-e2e-elicitation", "MCP Functionality E2E Elicitation", "1.0.0"))
                .capabilities(McpSchema.ClientCapabilities.builder().elicitation().build())
                .requestTimeout(Duration.ofSeconds(30L))
                .initializationTimeout(Duration.ofSeconds(30L))
                .elicitation(request -> elicitationHandler.apply(elicitationRequests, request))
                .build();
    }
    
    static McpClientTransport createHttpClientTransport(final URI endpointUri) {
        return HttpClientStreamableHttpTransport.builder(String.format("%s://%s:%d", endpointUri.getScheme(), endpointUri.getHost(), endpointUri.getPort()))
                .endpoint(endpointUri.getPath()).supportedProtocolVersions(SUPPORTED_PROTOCOL_VERSIONS).build();
    }
    
    static StdioClientTransport createStdioClientTransport(final Path configFile) throws IOException {
        return new ProtocolAwareStdioClientTransport(ServerParameters.builder(Paths.get(System.getProperty("java.home"), "bin", "java").toString())
                .args("-Dlogback.configurationFile=" + MCPStdioLogbackConfiguration.createForConfig(configFile, STDIO_LOGBACK_CONFIG_FILE_NAME),
                        "-cp", System.getProperty("java.class.path"), MCPBootstrap.class.getName(), configFile.toString())
                .build());
    }
    
    private static final class ProtocolAwareStdioClientTransport extends StdioClientTransport {
        
        private ProtocolAwareStdioClientTransport(final ServerParameters params) {
            super(params, MCPTransportJsonMapperFactory.create());
        }
        
        @Override
        public List<String> protocolVersions() {
            return SUPPORTED_PROTOCOL_VERSIONS;
        }
    }
}
