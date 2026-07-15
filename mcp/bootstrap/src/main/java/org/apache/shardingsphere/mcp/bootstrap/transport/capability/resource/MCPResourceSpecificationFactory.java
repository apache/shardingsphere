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
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportErrorFactory;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.resource.MCPResourceController;
import org.apache.shardingsphere.mcp.core.resource.handler.ResourceDefinitionRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MCP resource specification factory.
 */
public final class MCPResourceSpecificationFactory {
    
    private static final String JSON_CONTENT_TYPE = "application/json";
    
    private final Collection<MCPResourceDescriptor> descriptors;
    
    private final MCPResourceController controller;
    
    public MCPResourceSpecificationFactory(final MCPRuntimeContext runtimeContext) {
        descriptors = ResourceDefinitionRegistry.getSupportedResourceDescriptors();
        controller = new MCPResourceController(runtimeContext);
    }
    
    /**
     * Create MCP resource specifications.
     *
     * @return resource specifications
     */
    public List<SyncResourceSpecification> createResourceSpecifications() {
        return descriptors.stream()
                .filter(each -> !each.isTemplated())
                .map(each -> new SyncResourceSpecification(createResource(each), (exchange, request) -> readResource(exchange.sessionId(), request))).toList();
    }
    
    private McpSchema.Resource createResource(final MCPResourceDescriptor descriptor) {
        McpSchema.Resource.Builder result = McpSchema.Resource.builder()
                .uri(descriptor.getUriTemplate())
                .name(descriptor.getName())
                .title(descriptor.getTitle())
                .description(descriptor.getDescription())
                .mimeType(descriptor.getMimeType());
        if (!descriptor.getAnnotations().isEmpty()) {
            result.annotations(createAnnotations(descriptor.getAnnotations()));
        }
        if (!descriptor.getMeta().isEmpty()) {
            result.meta(descriptor.getMeta());
        }
        return result.build();
    }
    
    /**
     * Create MCP resource template specifications.
     *
     * @return resource template specifications
     */
    public List<SyncResourceTemplateSpecification> createResourceTemplateSpecifications() {
        return descriptors.stream()
                .filter(MCPResourceDescriptor::isTemplated)
                .map(each -> new SyncResourceTemplateSpecification(createResourceTemplate(each), (exchange, request) -> readResource(exchange.sessionId(), request))).toList();
    }
    
    private McpSchema.ResourceTemplate createResourceTemplate(final MCPResourceDescriptor descriptor) {
        McpSchema.ResourceTemplate.Builder result = McpSchema.ResourceTemplate.builder()
                .uriTemplate(descriptor.getUriTemplate())
                .name(descriptor.getName())
                .title(descriptor.getTitle())
                .description(descriptor.getDescription())
                .mimeType(descriptor.getMimeType());
        if (!descriptor.getAnnotations().isEmpty()) {
            result.annotations(createAnnotations(descriptor.getAnnotations()));
        }
        if (!descriptor.getMeta().isEmpty()) {
            result.meta(descriptor.getMeta());
        }
        return result.build();
    }
    
    private McpSchema.Annotations createAnnotations(final MCPResourceAnnotations annotations) {
        List<McpSchema.Role> audience = annotations.getAudience().stream().map(each -> McpSchema.Role.valueOf(each.toUpperCase(Locale.ENGLISH))).toList();
        return new McpSchema.Annotations(audience, annotations.getPriority(), annotations.getLastModified());
    }
    
    private ReadResourceResult readResource(final String sessionId, final McpSchema.ReadResourceRequest request) {
        try {
            Map<String, Object> payload = controller.handle(sessionId, request.uri()).toPayload();
            return new ReadResourceResult(Collections.singletonList(new TextResourceContents(request.uri(), JSON_CONTENT_TYPE, JsonUtils.toJsonString(payload))));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw MCPTransportErrorFactory.createResourceError(ex);
        }
    }
}
