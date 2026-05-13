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
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.mcp.api.common.descriptor.MCPAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPFixedResourceDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceTemplateDescriptor;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportPayloadUtils;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.resource.MCPResourceController;
import org.apache.shardingsphere.mcp.core.resource.handler.ResourceHandlerRegistry;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * MCP resource specification factory.
 */
public final class MCPResourceSpecificationFactory {
    
    private final List<MCPResourceDescriptor> resourceDescriptors;
    
    private final MCPResourceController controller;
    
    public MCPResourceSpecificationFactory(final MCPRuntimeContext runtimeContext) {
        resourceDescriptors = ResourceHandlerRegistry.getSupportedResourceDescriptors();
        controller = new MCPResourceController(runtimeContext);
    }
    
    /**
     * Create MCP resource specifications.
     *
     * @return resource specifications
     */
    public List<SyncResourceSpecification> createResourceSpecifications() {
        return resourceDescriptors.stream()
                .filter(MCPFixedResourceDescriptor.class::isInstance).map(MCPFixedResourceDescriptor.class::cast)
                .map(each -> new SyncResourceSpecification(createResource(each), this::handleReadResource)).collect(Collectors.toList());
    }
    
    /**
     * Create MCP resource template specifications.
     *
     * @return resource template specifications
     */
    public List<SyncResourceTemplateSpecification> createResourceTemplateSpecifications() {
        return resourceDescriptors.stream()
                .filter(MCPResourceTemplateDescriptor.class::isInstance).map(MCPResourceTemplateDescriptor.class::cast)
                .map(each -> new SyncResourceTemplateSpecification(createResourceTemplate(each), this::handleReadResource))
                .collect(Collectors.toList());
    }
    
    private McpSchema.Resource createResource(final MCPFixedResourceDescriptor descriptor) {
        McpSchema.Resource.Builder result = McpSchema.Resource.builder()
                .uri(descriptor.getUri())
                .name(descriptor.getName())
                .title(descriptor.getTitle())
                .description(descriptor.getDescription())
                .mimeType(descriptor.getMimeType());
        appendResourceAnnotations(result, descriptor.getAnnotations());
        if (null != descriptor.getSize()) {
            result.size(descriptor.getSize());
        }
        if (!descriptor.getMeta().isEmpty()) {
            result.meta(descriptor.getMeta());
        }
        return result.build();
    }
    
    private McpSchema.ResourceTemplate createResourceTemplate(final MCPResourceTemplateDescriptor descriptor) {
        McpSchema.ResourceTemplate.Builder result = McpSchema.ResourceTemplate.builder()
                .uriTemplate(descriptor.getUriTemplate())
                .name(descriptor.getName())
                .title(descriptor.getTitle())
                .description(descriptor.getDescription())
                .mimeType(descriptor.getMimeType());
        appendResourceTemplateAnnotations(result, descriptor.getAnnotations());
        if (!descriptor.getMeta().isEmpty()) {
            result.meta(descriptor.getMeta());
        }
        return result.build();
    }
    
    private void appendResourceAnnotations(final McpSchema.Resource.Builder builder, final MCPAnnotations annotations) {
        if (!annotations.isEmpty()) {
            builder.annotations(createAnnotations(annotations));
        }
    }
    
    private void appendResourceTemplateAnnotations(final McpSchema.ResourceTemplate.Builder builder, final MCPAnnotations annotations) {
        if (!annotations.isEmpty()) {
            builder.annotations(createAnnotations(annotations));
        }
    }
    
    private McpSchema.Annotations createAnnotations(final MCPAnnotations annotations) {
        List<McpSchema.Role> audience = annotations.getAudience().stream().map(each -> McpSchema.Role.valueOf(each.toUpperCase(Locale.ENGLISH))).toList();
        return new McpSchema.Annotations(audience, annotations.getPriority(), annotations.getLastModified());
    }
    
    private McpSchema.ReadResourceResult handleReadResource(final McpSyncServerExchange exchange, final McpSchema.ReadResourceRequest request) {
        return MCPTransportPayloadUtils.createReadResourceResult(request.uri(), controller.handle(request.uri()).toPayload());
    }
}
