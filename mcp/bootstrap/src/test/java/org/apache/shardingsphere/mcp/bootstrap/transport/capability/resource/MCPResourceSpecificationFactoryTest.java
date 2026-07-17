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

package org.apache.shardingsphere.mcp.bootstrap.transport.capability.resource;

import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.core.context.MCPFeatureRuntimeRequestContext;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.resource.handler.ResourceDefinitionRegistry;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPResourceSpecificationFactoryTest {
    
    @Test
    void assertCreateResourceSpecifications() {
        Collection<SyncResourceSpecification> actual = new MCPResourceSpecificationFactory(mock(MCPRuntimeContext.class)).createResourceSpecifications();
        SyncResourceSpecification actualSpecification = findResourceSpecification(actual, "shardingsphere://capabilities");
        assertThat(actualSpecification.resource().name(), is("server-capability-catalog"));
        assertThat(actualSpecification.resource().title(), is("ShardingSphere MCP Capability Catalog"));
        assertFalse(actualSpecification.resource().description().isBlank());
        assertThat(actualSpecification.resource().mimeType(), is("application/json"));
        assertThat(actualSpecification.resource().meta().get(MCPShardingSphereMetadataKeys.RESOURCE_KIND), is("capability-catalog"));
        assertNotNull(actualSpecification.readHandler());
    }
    
    @Test
    void assertCreateResourceSpecificationsMapAnnotationPriority() {
        Collection<SyncResourceSpecification> actual = new MCPResourceSpecificationFactory(mock(MCPRuntimeContext.class)).createResourceSpecifications();
        assertThat(findResourceSpecification(actual, "shardingsphere://capabilities").resource().annotations().priority(), is(1.0D));
        assertThat(findResourceSpecification(actual, "shardingsphere://guidance").resource().annotations().priority(), is(0.95D));
    }
    
    @Test
    void assertCreateResourceSpecificationsHandleReadResource() {
        SyncResourceSpecification actualSpecification = findResourceSpecification(
                new MCPResourceSpecificationFactory(createRuntimeContext()).createResourceSpecifications(), "shardingsphere://capabilities");
        ReadResourceResult actual = actualSpecification.readHandler().apply(mock(McpSyncServerExchange.class), new ReadResourceRequest("shardingsphere://capabilities"));
        assertThat(actual.contents().get(0), isA(TextResourceContents.class));
        TextResourceContents actualContents = (TextResourceContents) actual.contents().get(0);
        assertThat(actualContents.uri(), is("shardingsphere://capabilities"));
        assertThat(actualContents.mimeType(), is("application/json"));
        assertTrue(actualContents.text().contains("\"response_mode\":\"catalog\""));
        assertTrue(actualContents.text().contains("\"guidanceResource\":\"shardingsphere://guidance\""));
    }
    
    @Test
    void assertReadResourceUsesExchangeSession() {
        MCPResourceDescriptor descriptor = new MCPResourceDescriptor("shardingsphere://session", "session", "Session", "Read session.", "application/json",
                MCPResourceAnnotations.EMPTY, Map.of());
        McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
        when(exchange.sessionId()).thenReturn("session-1");
        ArgumentCaptor<MCPFeatureRuntimeRequestContext> requestContextCaptor = ArgumentCaptor.forClass(MCPFeatureRuntimeRequestContext.class);
        try (MockedStatic<ResourceDefinitionRegistry> mocked = mockStatic(ResourceDefinitionRegistry.class)) {
            mocked.when(ResourceDefinitionRegistry::getSupportedResourceDescriptors).thenReturn(List.of(descriptor));
            mocked.when(() -> ResourceDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq("shardingsphere://session")))
                    .thenReturn(Optional.of(new MCPMapPayload(Map.of())));
            SyncResourceSpecification actualSpecification = findResourceSpecification(
                    new MCPResourceSpecificationFactory(createRuntimeContext()).createResourceSpecifications(), "shardingsphere://session");
            actualSpecification.readHandler().apply(exchange, new ReadResourceRequest("shardingsphere://session"));
            mocked.verify(() -> ResourceDefinitionRegistry.dispatch(requestContextCaptor.capture(), eq("shardingsphere://session")));
        }
        assertThat(requestContextCaptor.getValue().getSessionId(), is("session-1"));
    }
    
    @Test
    void assertCreateResourceSpecificationsHandleReadResourceError() {
        SyncResourceSpecification actualSpecification = findResourceSpecification(
                new MCPResourceSpecificationFactory(createRuntimeContext()).createResourceSpecifications(), "shardingsphere://capabilities");
        McpError actual = assertThrows(McpError.class,
                () -> actualSpecification.readHandler().apply(mock(McpSyncServerExchange.class), new ReadResourceRequest("shardingsphere://unknown")));
        assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.RESOURCE_NOT_FOUND));
        assertThat(actual.getJsonRpcError().message(), is("Unsupported resource URI `shardingsphere://unknown`."));
        @SuppressWarnings("unchecked")
        Map<String, Object> actualData = (Map<String, Object>) actual.getJsonRpcError().data();
        assertThat(actualData.get("message"), is("Unsupported resource URI `shardingsphere://unknown`."));
    }
    
    @Test
    void assertReadResourceWithMissingDatabaseCapability() {
        SyncResourceTemplateSpecification actualSpecification = findResourceTemplateSpecification(
                new MCPResourceSpecificationFactory(createRuntimeContext()).createResourceTemplateSpecifications(), "shardingsphere://databases/{database}/capabilities");
        McpError actual = assertThrows(McpError.class,
                () -> actualSpecification.readHandler().apply(mock(McpSyncServerExchange.class), new ReadResourceRequest("shardingsphere://databases/logic_db/capabilities")));
        assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.RESOURCE_NOT_FOUND));
        assertThat(actual.getJsonRpcError().message(), is("Database capability does not exist."));
    }
    
    @Test
    void assertReadResourceWithInvalidRequest() {
        MCPResourceDescriptor descriptor = new MCPResourceDescriptor("shardingsphere://invalid", "invalid", "Invalid", "Read invalid request.", "application/json",
                MCPResourceAnnotations.EMPTY, Map.of());
        try (MockedStatic<ResourceDefinitionRegistry> mocked = mockStatic(ResourceDefinitionRegistry.class)) {
            mocked.when(ResourceDefinitionRegistry::getSupportedResourceDescriptors).thenReturn(List.of(descriptor));
            mocked.when(() -> ResourceDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq("shardingsphere://invalid")))
                    .thenThrow(new MCPInvalidRequestException("Invalid resource request."));
            SyncResourceSpecification actualSpecification = findResourceSpecification(
                    new MCPResourceSpecificationFactory(createRuntimeContext()).createResourceSpecifications(), "shardingsphere://invalid");
            McpError actual = assertThrows(McpError.class,
                    () -> actualSpecification.readHandler().apply(mock(McpSyncServerExchange.class), new ReadResourceRequest("shardingsphere://invalid")));
            assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.INVALID_PARAMS));
            assertThat(actual.getJsonRpcError().message(), is("Invalid resource request."));
        }
    }
    
    @Test
    void assertReadResourceSanitizesRuntimeFailure() {
        MCPResourceDescriptor descriptor = new MCPResourceDescriptor("shardingsphere://runtime-error", "runtime-error", "Runtime Error", "Read runtime error.", "application/json",
                MCPResourceAnnotations.EMPTY, Map.of());
        try (MockedStatic<ResourceDefinitionRegistry> mocked = mockStatic(ResourceDefinitionRegistry.class)) {
            mocked.when(ResourceDefinitionRegistry::getSupportedResourceDescriptors).thenReturn(List.of(descriptor));
            mocked.when(() -> ResourceDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq("shardingsphere://runtime-error"))).thenThrow(new RuntimeException("runtime failure"));
            SyncResourceSpecification actualSpecification = findResourceSpecification(
                    new MCPResourceSpecificationFactory(createRuntimeContext()).createResourceSpecifications(), "shardingsphere://runtime-error");
            McpError actual = assertThrows(McpError.class,
                    () -> actualSpecification.readHandler().apply(mock(McpSyncServerExchange.class), new ReadResourceRequest("shardingsphere://runtime-error")));
            assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.INTERNAL_ERROR));
            assertThat(actual.getJsonRpcError().message(), is("Service is temporarily unavailable."));
            assertFalse(String.valueOf(actual.getJsonRpcError().data()).contains("runtime failure"));
        }
    }
    
    @Test
    void assertCreateResourceTemplateSpecifications() {
        Collection<SyncResourceTemplateSpecification> actual = new MCPResourceSpecificationFactory(mock(MCPRuntimeContext.class)).createResourceTemplateSpecifications();
        SyncResourceTemplateSpecification actualSpecification = findResourceTemplateSpecification(actual, "shardingsphere://databases/{database}");
        assertThat(actualSpecification.resourceTemplate().name(), is("logical-database-detail"));
        assertThat(actualSpecification.resourceTemplate().title(), is("Logical Database Detail"));
        assertTrue(actualSpecification.resourceTemplate().description().contains("logical database"));
        assertThat(actualSpecification.resourceTemplate().mimeType(), is("application/json"));
        assertThat(actualSpecification.resourceTemplate().meta().get(MCPShardingSphereMetadataKeys.RESOURCE_KIND), is("detail"));
        assertNotNull(actualSpecification.readHandler());
    }
    
    private SyncResourceSpecification findResourceSpecification(final Collection<SyncResourceSpecification> specifications, final String uri) {
        return specifications.stream().filter(each -> uri.equals(each.resource().uri())).findFirst().orElseThrow();
    }
    
    private MCPRuntimeContext createRuntimeContext() {
        MCPRuntimeContext result = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
        when(result.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
        when(result.getDatabaseCapabilityProvider().provide(anyString())).thenReturn(Optional.empty());
        return result;
    }
    
    private SyncResourceTemplateSpecification findResourceTemplateSpecification(final Collection<SyncResourceTemplateSpecification> specifications, final String uriTemplate) {
        return specifications.stream().filter(each -> uriTemplate.equals(each.resourceTemplate().uriTemplate())).findFirst().orElseThrow();
    }
}
