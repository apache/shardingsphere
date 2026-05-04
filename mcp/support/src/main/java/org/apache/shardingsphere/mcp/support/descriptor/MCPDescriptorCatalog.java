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

package org.apache.shardingsphere.mcp.support.descriptor;

import lombok.Getter;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceParameterDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP descriptor catalog.
 */
@Getter
public final class MCPDescriptorCatalog {
    
    private final List<MCPResourceDescriptor> resourceDescriptors;
    
    private final List<MCPToolDescriptor> toolDescriptors;
    
    public MCPDescriptorCatalog(final Collection<MCPResourceDescriptor> resourceDescriptors, final Collection<MCPToolDescriptor> toolDescriptors) {
        this.resourceDescriptors = null == resourceDescriptors ? Collections.emptyList() : List.copyOf(resourceDescriptors);
        this.toolDescriptors = null == toolDescriptors ? Collections.emptyList() : List.copyOf(toolDescriptors);
    }
    
    /**
     * Convert descriptors to model-facing capability payload.
     *
     * @param supportedResources supported resource URI patterns
     * @param supportedTools supported tool names
     * @param supportedStatements supported statement classes
     * @return capability payload
     */
    public Map<String, Object> toPayload(final Collection<String> supportedResources, final Collection<String> supportedTools, final Collection<?> supportedStatements) {
        Map<String, Object> result = new LinkedHashMap<>(12, 1F);
        result.put("supportedResources", List.copyOf(supportedResources));
        result.put("supportedTools", List.copyOf(supportedTools));
        result.put("supportedStatementClasses", List.copyOf(supportedStatements));
        result.put("resources", resourceDescriptors.stream().filter(each -> !each.isTemplated()).map(this::toResourcePayload).toList());
        result.put("resourceTemplates", resourceDescriptors.stream().filter(MCPResourceDescriptor::isTemplated).map(this::toResourcePayload).toList());
        result.put("tools", toolDescriptors.stream().map(this::toToolPayload).toList());
        result.put("protocolAvailability", createProtocolAvailability());
        result.put("deferredRequirements", createDeferredRequirements());
        return result;
    }
    
    private Map<String, Object> toResourcePayload(final MCPResourceDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("uriPattern", descriptor.getUriPattern());
        result.put("name", descriptor.getName());
        result.put("title", descriptor.getTitle());
        result.put("description", descriptor.getDescription());
        result.put("mimeType", descriptor.getMimeType());
        result.put("parameters", descriptor.getParameters().stream().map(this::toParameterPayload).toList());
        if (!descriptor.getAnnotations().isEmpty()) {
            result.put("annotations", toResourceAnnotationsPayload(descriptor.getAnnotations()));
        }
        if (!descriptor.getMeta().isEmpty()) {
            result.put("meta", descriptor.getMeta());
        }
        return result;
    }
    
    private Map<String, Object> toParameterPayload(final MCPResourceParameterDescriptor parameter) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("name", parameter.getName());
        result.put("title", parameter.getTitle());
        result.put("description", parameter.getDescription());
        result.put("required", parameter.isRequired());
        result.put("scope", parameter.getScope());
        return result;
    }
    
    private Map<String, Object> toToolPayload(final MCPToolDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("name", descriptor.getName());
        result.put("title", descriptor.getTitle());
        result.put("description", descriptor.getDescription());
        result.put("inputFields", descriptor.getFields().stream().map(this::toFieldPayload).toList());
        result.put("outputSchema", descriptor.getOutputSchema());
        if (!descriptor.getAnnotations().isEmpty()) {
            result.put("annotations", toToolAnnotationsPayload(descriptor.getAnnotations()));
        }
        if (!descriptor.getMeta().isEmpty()) {
            result.put("meta", descriptor.getMeta());
        }
        return result;
    }
    
    private Map<String, Object> toFieldPayload(final MCPToolFieldDefinition field) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("name", field.getName());
        result.put("required", field.isRequired());
        result.put("schema", field.getValueDefinition().toSchemaFragment());
        return result;
    }
    
    private Map<String, Object> toResourceAnnotationsPayload(final MCPResourceAnnotations annotations) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        if (!annotations.getAudience().isEmpty()) {
            result.put("audience", annotations.getAudience());
        }
        if (null != annotations.getPriority()) {
            result.put("priority", annotations.getPriority());
        }
        if (null != annotations.getLastModified() && !annotations.getLastModified().isBlank()) {
            result.put("lastModified", annotations.getLastModified());
        }
        return result;
    }
    
    private Map<String, Object> toToolAnnotationsPayload(final MCPToolAnnotations annotations) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        putIfPresent(result, "title", annotations.getTitle());
        putIfPresent(result, "readOnlyHint", annotations.getReadOnlyHint());
        putIfPresent(result, "destructiveHint", annotations.getDestructiveHint());
        putIfPresent(result, "idempotentHint", annotations.getIdempotentHint());
        putIfPresent(result, "openWorldHint", annotations.getOpenWorldHint());
        putIfPresent(result, "returnDirect", annotations.getReturnDirect());
        return result;
    }
    
    private void putIfPresent(final Map<String, Object> target, final String key, final Object value) {
        if (null != value) {
            target.put(key, value);
        }
    }
    
    private Map<String, Object> createProtocolAvailability() {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("resources", true);
        result.put("resourceTemplates", true);
        result.put("tools", true);
        result.put("toolAnnotations", true);
        result.put("toolOutputSchemas", true);
        result.put("prompts", false);
        result.put("completions", false);
        return result;
    }
    
    private Map<String, Object> createDeferredRequirements() {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("prompts", "Recorded as a requirement for guided MCP workflows; not implemented in this phase.");
        result.put("completions", "Recorded as a requirement for argument completion; not implemented in this phase.");
        return result;
    }
}
