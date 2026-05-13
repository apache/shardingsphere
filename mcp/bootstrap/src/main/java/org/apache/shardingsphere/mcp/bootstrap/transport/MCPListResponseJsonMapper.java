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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.mcp.api.common.descriptor.MCPIcon;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPFixedResourceDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceTemplateDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.resource.handler.ResourceHandlerRegistry;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolHandlerRegistry;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorRegistry;
import org.apache.shardingsphere.mcp.support.descriptor.MCPResourceDescriptorUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP JSON mapper that preserves official descriptor fields missing from the current Java SDK records.
 */
public final class MCPListResponseJsonMapper implements McpJsonMapper {
    
    private final McpJsonMapper delegate;
    
    private final Map<String, Map<String, Object>> resourceExtraFields;
    
    private final Map<String, Map<String, Object>> resourceTemplateExtraFields;
    
    private final Map<String, Map<String, Object>> toolExtraFields;
    
    private final Map<String, Map<String, Object>> promptExtraFields;
    
    MCPListResponseJsonMapper(final McpJsonMapper delegate, final Map<String, Map<String, Object>> resourceExtraFields,
                              final Map<String, Map<String, Object>> resourceTemplateExtraFields, final Map<String, Map<String, Object>> toolExtraFields,
                              final Map<String, Map<String, Object>> promptExtraFields) {
        this.delegate = delegate;
        this.resourceExtraFields = resourceExtraFields;
        this.resourceTemplateExtraFields = resourceTemplateExtraFields;
        this.toolExtraFields = toolExtraFields;
        this.promptExtraFields = promptExtraFields;
    }
    
    /**
     * Create list response JSON mapper.
     *
     * @param delegate delegated JSON mapper
     * @return list response JSON mapper
     */
    public static MCPListResponseJsonMapper create(final McpJsonMapper delegate) {
        return new MCPListResponseJsonMapper(delegate, createResourceExtraFields(), createResourceTemplateExtraFields(), createToolExtraFields(), createPromptExtraFields());
    }
    
    @Override
    public <T> T readValue(final String content, final Class<T> valueType) throws IOException {
        return delegate.readValue(content, valueType);
    }
    
    @Override
    public <T> T readValue(final byte[] content, final Class<T> valueType) throws IOException {
        return delegate.readValue(content, valueType);
    }
    
    @Override
    public <T> T readValue(final String content, final TypeRef<T> valueTypeRef) throws IOException {
        return delegate.readValue(content, valueTypeRef);
    }
    
    @Override
    public <T> T readValue(final byte[] content, final TypeRef<T> valueTypeRef) throws IOException {
        return delegate.readValue(content, valueTypeRef);
    }
    
    @Override
    public <T> T convertValue(final Object sourceValue, final Class<T> targetType) {
        return delegate.convertValue(sourceValue, targetType);
    }
    
    @Override
    public <T> T convertValue(final Object sourceValue, final TypeRef<T> targetTypeRef) {
        return delegate.convertValue(sourceValue, targetTypeRef);
    }
    
    @Override
    public String writeValueAsString(final Object value) throws IOException {
        return delegate.writeValueAsString(adaptListResponse(value));
    }
    
    @Override
    public byte[] writeValueAsBytes(final Object value) throws IOException {
        return delegate.writeValueAsBytes(adaptListResponse(value));
    }
    
    private Object adaptListResponse(final Object value) throws IOException {
        if (value instanceof McpSchema.JSONRPCResponse response) {
            return adaptJsonRpcResponse(response);
        }
        return adaptListResult(value);
    }
    
    @SuppressWarnings("unchecked")
    private Object adaptJsonRpcResponse(final McpSchema.JSONRPCResponse response) throws IOException {
        Object adaptedResult = adaptListResult(response.result());
        if (adaptedResult == response.result()) {
            return response;
        }
        Map<String, Object> result = delegate.readValue(delegate.writeValueAsString(response), Map.class);
        result.put("result", adaptedResult);
        return result;
    }
    
    private Object adaptListResult(final Object value) throws IOException {
        if (value instanceof McpSchema.ListResourcesResult listResult) {
            return MCPListResponsePayloadAdapter.adaptResources(delegate, listResult, resourceExtraFields);
        }
        if (value instanceof McpSchema.ListResourceTemplatesResult listResult) {
            return MCPListResponsePayloadAdapter.adaptResourceTemplates(delegate, listResult, resourceTemplateExtraFields);
        }
        if (value instanceof McpSchema.ListToolsResult listResult) {
            return MCPListResponsePayloadAdapter.adaptTools(delegate, listResult, toolExtraFields);
        }
        if (value instanceof McpSchema.ListPromptsResult listResult) {
            return MCPListResponsePayloadAdapter.adaptPrompts(delegate, listResult, promptExtraFields);
        }
        return value;
    }
    
    private static Map<String, Map<String, Object>> createResourceExtraFields() {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (MCPResourceDescriptor each : ResourceHandlerRegistry.getSupportedResourceDescriptors()) {
            if (each instanceof MCPFixedResourceDescriptor) {
                putExtraFields(result, MCPResourceDescriptorUtils.getUriOrTemplate(each), each.getIcons());
            }
        }
        return result;
    }
    
    private static Map<String, Map<String, Object>> createResourceTemplateExtraFields() {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (MCPResourceDescriptor each : ResourceHandlerRegistry.getSupportedResourceDescriptors()) {
            if (each instanceof MCPResourceTemplateDescriptor) {
                putExtraFields(result, MCPResourceDescriptorUtils.getUriOrTemplate(each), each.getIcons());
            }
        }
        return result;
    }
    
    private static Map<String, Map<String, Object>> createToolExtraFields() {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (MCPToolDescriptor each : ToolHandlerRegistry.getSupportedToolDescriptors()) {
            putExtraFields(result, each.getName(), each.getIcons());
        }
        return result;
    }
    
    private static Map<String, Map<String, Object>> createPromptExtraFields() {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (MCPPromptDescriptor each : MCPDescriptorRegistry.getPromptDescriptors()) {
            putExtraFields(result, each.getName(), each.getIcons());
        }
        return result;
    }
    
    private static void putExtraFields(final Map<String, Map<String, Object>> target, final String identifier, final List<MCPIcon> icons) {
        Map<String, Object> extraFields = new LinkedHashMap<>(2, 1F);
        if (!icons.isEmpty()) {
            extraFields.put("icons", icons.stream().map(MCPListResponseJsonMapper::toIconPayload).toList());
        }
        if (!extraFields.isEmpty()) {
            target.put(identifier, extraFields);
        }
    }
    
    private static Map<String, Object> toIconPayload(final MCPIcon icon) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        putIfPresent(result, "src", icon.getSrc());
        putIfPresent(result, "mimeType", icon.getMimeType());
        if (!icon.getSizes().isEmpty()) {
            result.put("sizes", icon.getSizes());
        }
        putIfPresent(result, "theme", icon.getTheme());
        return result;
    }
    
    private static void putIfPresent(final Map<String, Object> target, final String key, final Object value) {
        if (null != value) {
            target.put(key, value);
        }
    }
}
