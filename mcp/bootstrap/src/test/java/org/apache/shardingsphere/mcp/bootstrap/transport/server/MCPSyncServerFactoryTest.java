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

package org.apache.shardingsphere.mcp.bootstrap.transport.server;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import io.modelcontextprotocol.spec.McpStreamableServerSession;
import io.modelcontextprotocol.spec.McpStreamableServerTransportProvider;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportJsonMapperFactory;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportPayloadUtils;
import org.apache.shardingsphere.mcp.bootstrap.transport.resource.MCPResourceSpecificationFactory;
import org.apache.shardingsphere.mcp.bootstrap.transport.tool.MCPToolSpecificationFactory;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPSyncServerFactoryTest {
    
    @Test
    void assertCreateWithTransportProvider() {
        TestServerTransportProvider transportProvider = new TestServerTransportProvider();
        McpSyncServer actual = createFactory().create(transportProvider);
        assertNotNull(transportProvider.sessionFactory);
        assertThat(actual.getServerInfo().name(), is(MCPTransportConstants.SERVER_NAME));
        assertThat(actual.getServerInfo().version(), is("development"));
        assertFalse(actual.getServerCapabilities().resources().subscribe());
        assertFalse(actual.getServerCapabilities().resources().listChanged());
        assertFalse(actual.getServerCapabilities().tools().listChanged());
        assertThat(actual.listTools().stream().map(McpSchema.Tool::name).toList(), is(List.of("search_metadata")));
        assertThat(actual.listResources().stream().map(McpSchema.Resource::uri).toList(), is(List.of("shardingsphere://capabilities")));
        assertThat(actual.listResourceTemplates().stream().map(McpSchema.ResourceTemplate::uriTemplate).toList(),
                is(List.of("shardingsphere://databases/{database}")));
        actual.closeGracefully();
    }
    
    @Test
    void assertCreateWithStreamableTransportProvider() {
        TestStreamableTransportProvider transportProvider = new TestStreamableTransportProvider();
        McpSyncServer actual = createFactory().create(transportProvider);
        assertNotNull(transportProvider.sessionFactory);
        assertThat(actual.listTools().size(), is(1));
        assertThat(actual.listResources().size(), is(1));
        assertThat(actual.listResourceTemplates().size(), is(1));
        actual.closeGracefully();
    }
    
    private MCPSyncServerFactory createFactory() {
        MCPToolSpecificationFactory toolSpecificationFactory = mock(MCPToolSpecificationFactory.class);
        MCPResourceSpecificationFactory resourceSpecificationFactory = mock(MCPResourceSpecificationFactory.class);
        when(toolSpecificationFactory.createToolSpecifications()).thenReturn(List.of(new SyncToolSpecification(
                McpSchema.Tool.builder().name("search_metadata").title("Search Metadata").description("Search metadata").inputSchema(
                        new McpSchema.JsonSchema("object", Map.of(), List.of(), true, Map.of(), Map.of())).build(),
                (exchange, request) -> MCPTransportPayloadUtils.createCallToolResult(Map.of("status", "ok")))));
        when(resourceSpecificationFactory.createResourceSpecifications()).thenReturn(List.of(new SyncResourceSpecification(
                McpSchema.Resource.builder().uri("shardingsphere://capabilities").name("capabilities").description("Capabilities").mimeType("application/json").build(),
                (exchange, request) -> MCPTransportPayloadUtils.createReadResourceResult(request.uri(), Map.of("status", "ok")))));
        when(resourceSpecificationFactory.createResourceTemplateSpecifications()).thenReturn(List.of(new SyncResourceTemplateSpecification(
                McpSchema.ResourceTemplate.builder().uriTemplate("shardingsphere://databases/{database}").name("{database}")
                        .description("Database resource").mimeType("application/json").build(),
                (exchange, request) -> MCPTransportPayloadUtils.createReadResourceResult(request.uri(), Map.of("status", "ok")))));
        McpJsonMapper jsonMapper = MCPTransportJsonMapperFactory.create();
        return new MCPSyncServerFactory(jsonMapper, toolSpecificationFactory, resourceSpecificationFactory);
    }
    
    private static final class TestServerTransportProvider implements McpServerTransportProvider {
        
        private McpServerSession.Factory sessionFactory;
        
        @Override
        public void setSessionFactory(final McpServerSession.Factory sessionFactory) {
            this.sessionFactory = sessionFactory;
        }
        
        @Override
        public Mono<Void> notifyClients(final String method, final Object params) {
            return Mono.empty();
        }
        
        @Override
        public Mono<Void> closeGracefully() {
            return Mono.empty();
        }
    }
    
    private static final class TestStreamableTransportProvider implements McpStreamableServerTransportProvider {
        
        private McpStreamableServerSession.Factory sessionFactory;
        
        @Override
        public void setSessionFactory(final McpStreamableServerSession.Factory sessionFactory) {
            this.sessionFactory = sessionFactory;
        }
        
        @Override
        public Mono<Void> notifyClients(final String method, final Object params) {
            return Mono.empty();
        }
        
        @Override
        public Mono<Void> closeGracefully() {
            return Mono.empty();
        }
    }
}
