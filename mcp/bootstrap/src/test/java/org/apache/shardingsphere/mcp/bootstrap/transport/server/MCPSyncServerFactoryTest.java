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
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        assertThat(actual.listTools().stream().map(McpSchema.Tool::name).toList(), is(List.of("database_gateway_search_metadata")));
        assertThat(actual.listResources().stream().map(McpSchema.Resource::uri).toList(), is(List.of("shardingsphere://capabilities")));
        assertThat(actual.listResourceTemplates().stream().map(McpSchema.ResourceTemplate::uriTemplate).toList(),
                is(List.of("shardingsphere://databases/{database}")));
        assertThat(actual.listPrompts().stream().map(McpSchema.Prompt::name).toList(), is(List.of("inspect_metadata")));
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
    void assertCreateExposesOfficialDiscoveryDescriptors() {
        TestServerTransportProvider transportProvider = new TestServerTransportProvider();
        McpSyncServer actual = createFactory().create(transportProvider);
        assertToolDiscoveryDescriptor(actual.listTools().get(0));
        assertResourceDiscoveryDescriptor(actual.listResources().get(0));
        assertResourceTemplateDiscoveryDescriptor(actual.listResourceTemplates().get(0));
        assertPromptDiscoveryDescriptor(actual.listPrompts().get(0));
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
        MCPToolSpecificationFactory toolSpecificationFactory = mock(MCPToolSpecificationFactory.class);
        MCPResourceSpecificationFactory resourceSpecificationFactory = mock(MCPResourceSpecificationFactory.class);
        MCPPromptSpecificationFactory promptSpecificationFactory = mock(MCPPromptSpecificationFactory.class);
        MCPCompletionSpecificationFactory completionSpecificationFactory = mock(MCPCompletionSpecificationFactory.class);
        when(toolSpecificationFactory.createToolSpecifications()).thenReturn(List.of(new SyncToolSpecification(
                createToolDiscoveryDescriptor(),
                (exchange, request) -> createFixtureToolResult())));
        when(resourceSpecificationFactory.createResourceSpecifications()).thenReturn(List.of(new SyncResourceSpecification(
                createResourceDiscoveryDescriptor(),
                (exchange, request) -> createFixtureReadResourceResult(request.uri()))));
        when(resourceSpecificationFactory.createResourceTemplateSpecifications()).thenReturn(List.of(new SyncResourceTemplateSpecification(
                createResourceTemplateDiscoveryDescriptor(),
                (exchange, request) -> createFixtureReadResourceResult(request.uri()))));
        when(promptSpecificationFactory.createPromptSpecifications()).thenReturn(List.of(new SyncPromptSpecification(
                createPromptDiscoveryDescriptor(),
                (exchange, request) -> new McpSchema.GetPromptResult("Inspect metadata", List.of()))));
        when(completionSpecificationFactory.createCompletionSpecifications()).thenReturn(List.of(new SyncCompletionSpecification(new McpSchema.PromptReference("inspect_metadata"),
                (exchange, request) -> new McpSchema.CompleteResult(new McpSchema.CompleteResult.CompleteCompletion(List.of(), 0, false)))));
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        when(runtimeContext.getSessionManager()).thenReturn(mock(MCPSessionManager.class));
        MCPSyncServerFactory result = new MCPSyncServerFactory(runtimeContext, MCPTransportJsonMapperFactory.create());
        try {
            setField(result, "toolSpecificationFactory", toolSpecificationFactory);
            setField(result, "resourceSpecificationFactory", resourceSpecificationFactory);
            setField(result, "promptSpecificationFactory", promptSpecificationFactory);
            setField(result, "completionSpecificationFactory", completionSpecificationFactory);
            return result;
        } catch (final ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
    
    private McpSchema.CallToolResult createFixtureToolResult() {
        return McpSchema.CallToolResult.builder().addTextContent("ok").build();
    }
    
    private McpSchema.ReadResourceResult createFixtureReadResourceResult(final String uri) {
        return new McpSchema.ReadResourceResult(List.of(new McpSchema.TextResourceContents(uri, "application/json", "ok")));
    }
    
    private McpSchema.Tool createToolDiscoveryDescriptor() {
        return McpSchema.Tool.builder()
                .name("database_gateway_search_metadata")
                .title("Search Metadata")
                .description("Search metadata")
                .inputSchema(new McpSchema.JsonSchema("object", Map.of("query", Map.of("type", "string")), List.of("query"), false, Map.of(), Map.of()))
                .outputSchema(Map.of("type", "object", "required", List.of("items")))
                .annotations(new McpSchema.ToolAnnotations("Search Metadata", true, false, true, false, null))
                .meta(Map.of(MCPShardingSphereMetadataKeys.PURPOSE, "metadata-discovery"))
                .build();
    }
    
    private McpSchema.Resource createResourceDiscoveryDescriptor() {
        return McpSchema.Resource.builder()
                .uri("shardingsphere://capabilities")
                .name("capabilities")
                .title("Capabilities")
                .description("Capabilities")
                .mimeType("application/json")
                .annotations(new McpSchema.Annotations(List.of(McpSchema.Role.ASSISTANT), 0.5D, null))
                .meta(Map.of(MCPShardingSphereMetadataKeys.PURPOSE, "catalog-guidance"))
                .build();
    }
    
    private McpSchema.ResourceTemplate createResourceTemplateDiscoveryDescriptor() {
        return McpSchema.ResourceTemplate.builder()
                .uriTemplate("shardingsphere://databases/{database}")
                .name("{database}")
                .title("Database Resource")
                .description("Database resource")
                .mimeType("application/json")
                .annotations(new McpSchema.Annotations(List.of(McpSchema.Role.ASSISTANT), 0.4D, null))
                .meta(Map.of(MCPShardingSphereMetadataKeys.PURPOSE, "database-detail"))
                .build();
    }
    
    private McpSchema.Prompt createPromptDiscoveryDescriptor() {
        return new McpSchema.Prompt("inspect_metadata", "Inspect Metadata", "Inspect metadata",
                List.of(new McpSchema.PromptArgument("database", "Database", "Logical database", true)), Map.of(MCPShardingSphereMetadataKeys.PURPOSE, "metadata-inspection"));
    }
    
    private void assertToolDiscoveryDescriptor(final McpSchema.Tool actual) {
        assertThat(actual.name(), is("database_gateway_search_metadata"));
        assertThat(actual.title(), is("Search Metadata"));
        assertThat(actual.description(), is("Search metadata"));
        assertThat(actual.inputSchema().required(), is(List.of("query")));
        assertThat(actual.outputSchema(), is(Map.of("type", "object", "required", List.of("items"))));
        assertTrue(actual.annotations().readOnlyHint());
        assertThat(actual.meta(), is(Map.of(MCPShardingSphereMetadataKeys.PURPOSE, "metadata-discovery")));
    }
    
    private void assertResourceDiscoveryDescriptor(final McpSchema.Resource actual) {
        assertThat(actual.uri(), is("shardingsphere://capabilities"));
        assertThat(actual.title(), is("Capabilities"));
        assertThat(actual.annotations().priority(), is(0.5D));
        assertThat(actual.meta(), is(Map.of(MCPShardingSphereMetadataKeys.PURPOSE, "catalog-guidance")));
    }
    
    private void assertResourceTemplateDiscoveryDescriptor(final McpSchema.ResourceTemplate actual) {
        assertThat(actual.uriTemplate(), is("shardingsphere://databases/{database}"));
        assertThat(actual.title(), is("Database Resource"));
        assertThat(actual.annotations().priority(), is(0.4D));
        assertThat(actual.meta(), is(Map.of(MCPShardingSphereMetadataKeys.PURPOSE, "database-detail")));
    }
    
    private void assertPromptDiscoveryDescriptor(final McpSchema.Prompt actual) {
        assertThat(actual.name(), is("inspect_metadata"));
        assertThat(actual.title(), is("Inspect Metadata"));
        assertThat(actual.arguments().get(0).name(), is("database"));
        assertThat(actual.meta(), is(Map.of(MCPShardingSphereMetadataKeys.PURPOSE, "metadata-inspection")));
    }
    
    private void setField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        Plugins.getMemberAccessor().set(field, target, value);
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
