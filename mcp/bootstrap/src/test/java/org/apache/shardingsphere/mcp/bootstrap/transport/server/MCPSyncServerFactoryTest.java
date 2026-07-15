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

import io.modelcontextprotocol.server.McpServerFeatures.SyncCompletionSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
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
import org.apache.shardingsphere.mcp.bootstrap.transport.capability.completion.MCPCompletionSpecificationFactory;
import org.apache.shardingsphere.mcp.bootstrap.transport.capability.prompt.MCPPromptSpecificationFactory;
import org.apache.shardingsphere.mcp.bootstrap.transport.capability.resource.MCPResourceSpecificationFactory;
import org.apache.shardingsphere.mcp.bootstrap.transport.capability.tool.MCPToolSpecificationFactory;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class MCPSyncServerFactoryTest {
    
    @Test
    void assertCreateWithTransportProvider() {
        TestServerTransportProvider transportProvider = new TestServerTransportProvider();
        McpSyncServer actual = createFactory().create(transportProvider);
        assertNotNull(transportProvider.sessionFactory);
        assertThat(actual.getServerInfo().name(), is(MCPTransportConstants.SERVER_NAME));
        assertThat(actual.getServerInfo().version(), is("development"));
        assertThat(actual.listTools().size(), is(1));
        assertThat(actual.listResources().size(), is(1));
        assertThat(actual.listResourceTemplates().size(), is(1));
        assertThat(actual.listPrompts().size(), is(1));
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
    
    @Test
    void assertCreateAdvertisesImplementedCapabilitiesAndSdkLoggingOnly() {
        TestServerTransportProvider transportProvider = new TestServerTransportProvider();
        McpSyncServer actual = createFactory().create(transportProvider);
        McpSchema.ServerCapabilities actualCapabilities = actual.getServerCapabilities();
        assertNull(actualCapabilities.experimental());
        assertNotNull(actualCapabilities.logging());
        assertNotNull(actualCapabilities.resources());
        assertFalse(actualCapabilities.resources().subscribe());
        assertFalse(actualCapabilities.resources().listChanged());
        assertNotNull(actualCapabilities.tools());
        assertFalse(actualCapabilities.tools().listChanged());
        assertNotNull(actualCapabilities.prompts());
        assertFalse(actualCapabilities.prompts().listChanged());
        assertNotNull(actualCapabilities.completions());
        actual.closeGracefully();
    }
    
    private MCPSyncServerFactory createFactory() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        when(runtimeContext.getSessionManager()).thenReturn(mock(MCPSessionManager.class));
        try (
                MockedConstruction<MCPToolSpecificationFactory> ignoredToolFactory = mockConstruction(MCPToolSpecificationFactory.class,
                        (mock, context) -> when(mock.createToolSpecifications()).thenReturn(List.of(new SyncToolSpecification(
                                createToolDescriptor(), (exchange, request) -> createFixtureToolResult()))));
                MockedConstruction<MCPResourceSpecificationFactory> ignoredResourceFactory = mockConstruction(MCPResourceSpecificationFactory.class, (mock, context) -> {
                    when(mock.createResourceSpecifications()).thenReturn(List.of(new SyncResourceSpecification(
                            createResourceDescriptor(), (exchange, request) -> createFixtureReadResourceResult(request.uri()))));
                    when(mock.createResourceTemplateSpecifications()).thenReturn(List.of(new SyncResourceTemplateSpecification(
                            createResourceTemplateDescriptor(), (exchange, request) -> createFixtureReadResourceResult(request.uri()))));
                });
                MockedConstruction<MCPPromptSpecificationFactory> ignoredPromptFactory = mockConstruction(MCPPromptSpecificationFactory.class,
                        (mock, context) -> when(mock.createPromptSpecifications()).thenReturn(List.of(new SyncPromptSpecification(
                                createPromptDescriptor(), (exchange, request) -> new McpSchema.GetPromptResult("Fixture prompt", List.of())))));
                MockedConstruction<MCPCompletionSpecificationFactory> ignoredCompletionFactory = mockConstruction(MCPCompletionSpecificationFactory.class,
                        (mock, context) -> when(mock.createCompletionSpecifications()).thenReturn(List.of(new SyncCompletionSpecification(new McpSchema.PromptReference("fixture_prompt"),
                                (exchange, request) -> new McpSchema.CompleteResult(new McpSchema.CompleteResult.CompleteCompletion(List.of(), 0, false))))))) {
            return new MCPSyncServerFactory(runtimeContext, MCPTransportJsonMapperFactory.create());
        }
    }
    
    private McpSchema.CallToolResult createFixtureToolResult() {
        return McpSchema.CallToolResult.builder().addTextContent("ok").build();
    }
    
    private McpSchema.ReadResourceResult createFixtureReadResourceResult(final String uri) {
        return new McpSchema.ReadResourceResult(List.of(new McpSchema.TextResourceContents(uri, "application/json", "ok")));
    }
    
    private McpSchema.Tool createToolDescriptor() {
        return McpSchema.Tool.builder()
                .name("fixture_tool")
                .description("Fixture tool")
                .inputSchema(new McpSchema.JsonSchema("object", Map.of(), List.of(), false, Map.of(), Map.of()))
                .build();
    }
    
    private McpSchema.Resource createResourceDescriptor() {
        return McpSchema.Resource.builder()
                .uri("fixture://resource")
                .name("fixture_resource")
                .description("Fixture resource")
                .mimeType("application/json")
                .build();
    }
    
    private McpSchema.ResourceTemplate createResourceTemplateDescriptor() {
        return McpSchema.ResourceTemplate.builder()
                .uriTemplate("fixture://resources/{name}")
                .name("fixture_template")
                .description("Fixture resource template")
                .mimeType("application/json")
                .build();
    }
    
    private McpSchema.Prompt createPromptDescriptor() {
        return new McpSchema.Prompt("fixture_prompt", "Fixture Prompt", "Fixture prompt", List.of(), Map.of());
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
