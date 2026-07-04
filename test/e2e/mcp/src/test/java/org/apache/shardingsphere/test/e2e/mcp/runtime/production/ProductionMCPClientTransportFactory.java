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

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.ProtocolVersions;
import org.apache.shardingsphere.mcp.bootstrap.MCPBootstrap;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportJsonMapperFactory;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPStdioLogbackConfiguration;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ProductionMCPClientTransportFactory {
    
    private static final String STDIO_LOGBACK_CONFIG_FILE_NAME = "mcp-e2e-sdk-stdio-logback.xml";
    
    private static final String MASK_PLAN_TOOL_NAME = "database_gateway_plan_mask_rule";
    
    private ProductionMCPClientTransportFactory() {
    }
    
    static McpSyncClient createElicitationClient(final McpClientTransport clientTransport, final List<McpSchema.ElicitRequest> elicitationRequests,
                                                 final BiFunction<List<McpSchema.ElicitRequest>, McpSchema.ElicitRequest, McpSchema.ElicitResult> elicitationHandler) {
        return McpClient.sync(clientTransport)
                .clientInfo(new McpSchema.Implementation("mcp-e2e-elicitation", "MCP E2E Elicitation", "1.0.0"))
                .capabilities(McpSchema.ClientCapabilities.builder().elicitation().build())
                .requestTimeout(Duration.ofSeconds(30L))
                .initializationTimeout(Duration.ofSeconds(30L))
                .elicitation(request -> elicitationHandler.apply(elicitationRequests, request))
                .build();
    }
    
    static McpClientTransport createHttpClientTransport(final URI endpointUri) {
        return HttpClientStreamableHttpTransport.builder(String.format("%s://%s:%d", endpointUri.getScheme(), endpointUri.getHost(), endpointUri.getPort()))
                .endpoint(endpointUri.getPath()).build();
    }
    
    static StdioClientTransport createStdioClientTransport(final Path configFile) throws IOException {
        return new ProtocolAwareStdioClientTransport(ServerParameters.builder(Paths.get(System.getProperty("java.home"), "bin", "java").toString())
                .args("-Dlogback.configurationFile=" + MCPStdioLogbackConfiguration.createForConfig(configFile, STDIO_LOGBACK_CONFIG_FILE_NAME),
                        "-cp", System.getProperty("java.class.path"), MCPBootstrap.class.getName(), configFile.toString())
                .build());
    }
    
    static void assertElicitationRequest(final List<McpSchema.ElicitRequest> actualRequests) {
        assertThat(actualRequests.size(), is(1));
        McpSchema.ElicitRequest actual = actualRequests.getFirst();
        assertThat(actual.meta().get(MCPShardingSphereMetadataKeys.TOOL), is(MASK_PLAN_TOOL_NAME));
        assertFalse(String.valueOf(actual.meta().get(MCPShardingSphereMetadataKeys.PLAN_ID)).isBlank());
        Map<String, Object> actualRequestedSchema = actual.requestedSchema();
        assertThat(actualRequestedSchema.get("type"), is("object"));
        assertFalse((Boolean) actualRequestedSchema.get("additionalProperties"));
        Map<String, Object> actualProperties = castToMap(actualRequestedSchema.get("properties"));
        assertTrue(actualProperties.containsKey("field_1"));
        assertTrue(actualProperties.containsKey("field_2"));
        assertThat(String.valueOf(castToMap(actualProperties.get("field_1")).get("description")), is("Please provide property `from-x`."));
        assertThat(String.valueOf(castToMap(actualProperties.get("field_2")).get("description")), is("Please provide property `to-y`."));
        assertFalse(actualProperties.keySet().stream().map(String::valueOf).anyMatch(each -> each.contains("secret") || each.contains("password") || each.contains("token")));
        assertThat(getStringList(actualRequestedSchema.get("required")), hasItems("field_1", "field_2"));
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, Object> castToMap(final Object value) {
        return (Map<String, Object>) value;
    }
    
    private static List<String> getStringList(final Object value) {
        return ((List<?>) value).stream().map(String::valueOf).toList();
    }
    
    private static final class ProtocolAwareStdioClientTransport extends StdioClientTransport {
        
        private ProtocolAwareStdioClientTransport(final ServerParameters params) {
            super(params, MCPTransportJsonMapperFactory.create());
        }
        
        @Override
        public List<String> protocolVersions() {
            return List.of(ProtocolVersions.MCP_2025_06_18, ProtocolVersions.MCP_2025_11_25);
        }
    }
}
