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
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP resource specification factory.
 */
public final class MCPResourceSpecificationFactory {
    
    private static final String JSON_CONTENT_TYPE = "application/json";
    
    private final MCPRuntimeContext runtimeContext;
    
    private final MCPResourcePayloadResolver resourcePayloadResolver;
    
    /**
     * Create MCP resource specification factory.
     *
     * @param runtimeContext runtime context
     */
    public MCPResourceSpecificationFactory(final MCPRuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
        resourcePayloadResolver = new MCPResourcePayloadResolver(runtimeContext);
    }
    
    /**
     * Create MCP resource specifications.
     *
     * @return resource specifications
     */
    public List<SyncResourceSpecification> createResourceSpecifications() {
        List<SyncResourceSpecification> result = new ArrayList<>();
        for (String each : runtimeContext.getResourceUriResolver().getSupportedResources()) {
            if (!isTemplatedResource(each)) {
                result.add(new SyncResourceSpecification(createResource(each), this::handleReadResource));
            }
        }
        return result;
    }
    
    /**
     * Create MCP resource template specifications.
     *
     * @return resource template specifications
     */
    public List<SyncResourceTemplateSpecification> createResourceTemplateSpecifications() {
        List<SyncResourceTemplateSpecification> result = new ArrayList<>();
        for (String each : runtimeContext.getResourceUriResolver().getSupportedResources()) {
            if (isTemplatedResource(each)) {
                result.add(new SyncResourceTemplateSpecification(createResourceTemplate(each), this::handleReadResource));
            }
        }
        return result;
    }
    
    private McpSchema.Resource createResource(final String uri) {
        return McpSchema.Resource.builder()
                .uri(uri)
                .name(uri.substring(uri.lastIndexOf('/') + 1))
                .description("ShardingSphere MCP resource: " + uri)
                .mimeType(JSON_CONTENT_TYPE)
                .build();
    }
    
    private McpSchema.ResourceTemplate createResourceTemplate(final String uriTemplate) {
        return McpSchema.ResourceTemplate.builder()
                .uriTemplate(uriTemplate)
                .name(uriTemplate.substring(uriTemplate.lastIndexOf('/') + 1))
                .description("ShardingSphere MCP resource template: " + uriTemplate)
                .mimeType(JSON_CONTENT_TYPE)
                .build();
    }
    
    private McpSchema.ReadResourceResult handleReadResource(final McpSyncServerExchange exchange, final McpSchema.ReadResourceRequest request) {
        Object payload = resourcePayloadResolver.resolve(request.uri());
        return new McpSchema.ReadResourceResult(List.of(new McpSchema.TextResourceContents(request.uri(), JSON_CONTENT_TYPE, JsonUtils.toJsonString(payload))));
    }
    
    private boolean isTemplatedResource(final String resourceUri) {
        return resourceUri.contains("{");
    }
}
