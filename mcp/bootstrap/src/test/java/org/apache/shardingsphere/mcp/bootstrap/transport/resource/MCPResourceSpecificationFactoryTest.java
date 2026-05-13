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

package org.apache.shardingsphere.mcp.bootstrap.transport.resource;

import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.apache.shardingsphere.mcp.api.common.descriptor.MCPAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPFixedResourceDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceTemplateDescriptor;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.resource.handler.ResourceHandlerRegistry;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPResourceSpecificationFactoryTest {
    
    @Test
    void assertCreateResourceSpecifications() {
        try (MockedStatic<ResourceHandlerRegistry> mockedResourceHandlerRegistry = mockStatic(ResourceHandlerRegistry.class)) {
            mockedResourceHandlerRegistry.when(ResourceHandlerRegistry::getSupportedResourceDescriptors).thenReturn(List.of(createResourceDescriptor(), createResourceTemplateDescriptor()));
            MCPResourceSpecificationFactory actualFactory = new MCPResourceSpecificationFactory(mock(MCPRuntimeContext.class));
            List<SyncResourceSpecification> actual = actualFactory.createResourceSpecifications();
            assertThat(actual.size(), is(1));
            assertThat(actual.get(0).resource().uri(), is("shardingsphere://capabilities"));
            assertThat(actual.get(0).resource().name(), is("server-capability-catalog"));
            assertThat(actual.get(0).resource().title(), is("Server Capability Catalog"));
            assertThat(actual.get(0).resource().description(), is("Read the model-facing capability catalog."));
            assertThat(actual.get(0).resource().mimeType(), is("application/json"));
            assertNotNull(actual.get(0).readHandler());
        }
    }
    
    @Test
    void assertCreateResourceSpecificationsHandleReadResource() {
        try (MockedStatic<ResourceHandlerRegistry> mockedResourceHandlerRegistry = mockStatic(ResourceHandlerRegistry.class)) {
            mockedResourceHandlerRegistry.when(ResourceHandlerRegistry::getSupportedResourceDescriptors).thenReturn(List.of(createResourceDescriptor()));
            mockedResourceHandlerRegistry.when(() -> ResourceHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("shardingsphere://capabilities")))
                    .thenReturn(Optional.of(new MCPMapResponse(Map.of("status", "ok"))));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncResourceSpecification actualSpecification = new MCPResourceSpecificationFactory(runtimeContext).createResourceSpecifications().get(0);
            ReadResourceResult actual = actualSpecification.readHandler().apply(mock(McpSyncServerExchange.class), new ReadResourceRequest("shardingsphere://capabilities"));
            assertThat(((TextResourceContents) actual.contents().get(0)).text(), is("{\"status\":\"ok\"}"));
        }
    }
    
    @Test
    void assertCreateResourceTemplateSpecifications() {
        try (MockedStatic<ResourceHandlerRegistry> mockedResourceHandlerRegistry = mockStatic(ResourceHandlerRegistry.class)) {
            mockedResourceHandlerRegistry.when(ResourceHandlerRegistry::getSupportedResourceDescriptors).thenReturn(List.of(createResourceDescriptor(), createResourceTemplateDescriptor()));
            MCPResourceSpecificationFactory actualFactory = new MCPResourceSpecificationFactory(mock(MCPRuntimeContext.class));
            List<SyncResourceTemplateSpecification> actual = actualFactory.createResourceTemplateSpecifications();
            assertThat(actual.size(), is(1));
            assertThat(actual.get(0).resourceTemplate().uriTemplate(), is("shardingsphere://databases/{database}"));
            assertThat(actual.get(0).resourceTemplate().name(), is("logical-database-detail"));
            assertThat(actual.get(0).resourceTemplate().title(), is("Logical Database Detail"));
            assertThat(actual.get(0).resourceTemplate().description(), is("Read one logical database detail."));
            assertThat(actual.get(0).resourceTemplate().mimeType(), is("application/json"));
            assertNotNull(actual.get(0).readHandler());
        }
    }
    
    private MCPResourceDescriptor createResourceDescriptor() {
        return new MCPFixedResourceDescriptor("shardingsphere://capabilities", "server-capability-catalog", "Server Capability Catalog",
                "Read the model-facing capability catalog.", Collections.emptyList(), "application/json", MCPAnnotations.EMPTY, null, Collections.emptyMap());
    }
    
    private MCPResourceDescriptor createResourceTemplateDescriptor() {
        return new MCPResourceTemplateDescriptor("shardingsphere://databases/{database}", "logical-database-detail", "Logical Database Detail",
                "Read one logical database detail.", Collections.emptyList(), "application/json", MCPAnnotations.EMPTY, Collections.emptyMap());
    }
}
