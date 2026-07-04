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

import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class MCPDescriptorCatalogPayloadBuilder {
    
    private final MCPDescriptorCatalog catalog;
    
    private MCPDescriptorCatalogPayloadBuilder(final MCPDescriptorCatalog catalog) {
        this.catalog = catalog;
    }
    
    static Map<String, Object> build(final MCPDescriptorCatalog catalog, final Collection<String> supportedResources, final Collection<String> supportedTools,
                                     final Collection<?> supportedStatements) {
        return new MCPDescriptorCatalogPayloadBuilder(catalog).build(supportedResources, supportedTools, supportedStatements);
    }
    
    private Map<String, Object> build(final Collection<String> supportedResources, final Collection<String> supportedTools, final Collection<?> supportedStatements) {
        Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        List<Map<String, Object>> resources = catalog.getProtocolDescriptors().getResourceDescriptors().stream().map(this::toResourcePayload).toList();
        List<Map<String, Object>> resourceTemplates = catalog.getProtocolDescriptors().getResourceTemplateDescriptors().stream().map(this::toResourceTemplatePayload).toList();
        List<Map<String, Object>> tools = catalog.getProtocolDescriptors().getToolDescriptors().stream().map(this::toToolPayload).toList();
        List<Map<String, Object>> prompts = catalog.getProtocolDescriptors().getPromptDescriptors().stream().map(this::toPromptPayload).toList();
        List<Map<String, Object>> completionTargets = catalog.getShardingSphereDescriptors().getCompletionTargetDescriptors().stream().map(this::toCompletionTargetPayload).toList();
        List<Map<String, Object>> resourceNavigation = catalog.getShardingSphereDescriptors().getResourceNavigationDescriptors().stream().map(this::toResourceNavigationPayload).toList();
        result.put("response_mode", MCPResponseMode.CATALOG);
        result.put("supportedResources", supportedResources);
        result.put("supportedTools", supportedTools);
        result.put("supportedStatementClasses", supportedStatements);
        result.put("guidanceResource", "shardingsphere://guidance");
        result.put("resources", resources);
        result.put("resourceTemplates", resourceTemplates);
        result.put("tools", tools);
        result.put("prompts", prompts);
        result.put("completionTargets", completionTargets);
        result.put("resourceNavigation", resourceNavigation);
        result.put("protocolAvailability", createProtocolAvailability(!resourceNavigation.isEmpty()));
        return result;
    }
    
    private Map<String, Object> toResourcePayload(final MCPResourceDescriptor descriptor) {
        return createResourcePayload(descriptor, MCPPayloadFieldNames.URI);
    }
    
    private Map<String, Object> toResourceTemplatePayload(final MCPResourceDescriptor descriptor) {
        return createResourcePayload(descriptor, "uriTemplate");
    }
    
    private Map<String, Object> createResourcePayload(final MCPResourceDescriptor descriptor, final String uriFieldName) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put(uriFieldName, descriptor.getUriOrTemplate());
        result.put("name", descriptor.getName());
        result.put("title", descriptor.getTitle());
        result.put("description", descriptor.getDescription());
        result.put("mimeType", descriptor.getMimeType());
        if (!descriptor.getAnnotations().isEmpty()) {
            result.put("annotations", toResourceAnnotationsPayload(descriptor.getAnnotations()));
        }
        if (!descriptor.getMeta().isEmpty()) {
            result.put("_meta", descriptor.getMeta());
        }
        return result;
    }
    
    private Map<String, Object> toToolPayload(final MCPToolDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("name", descriptor.getName());
        result.put("title", descriptor.getTitle());
        result.put("description", descriptor.getDescription());
        result.put("inputSchema", descriptor.getInputSchema());
        result.put("outputSchema", descriptor.getOutputSchema());
        result.put("annotations", toToolAnnotationsPayload(descriptor.getAnnotations()));
        if (!descriptor.getMeta().isEmpty()) {
            result.put("meta", descriptor.getMeta());
        }
        return result;
    }
    
    private Map<String, Object> toPromptPayload(final MCPPromptDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(7, 1F);
        result.put("name", descriptor.getName());
        result.put("title", descriptor.getTitle());
        result.put("description", descriptor.getDescription());
        result.put("arguments", descriptor.getArguments().stream().map(this::toPromptArgumentPayload).toList());
        if (!descriptor.getMeta().isEmpty()) {
            result.put("meta", descriptor.getMeta());
        }
        return result;
    }
    
    private Map<String, Object> toPromptArgumentPayload(final MCPPromptArgumentDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("name", descriptor.getName());
        result.put("title", descriptor.getTitle());
        result.put("description", descriptor.getDescription());
        result.put("required", descriptor.isRequired());
        Map<String, Object> completionHint = createCompletionHint(descriptor.getName());
        if (!completionHint.isEmpty()) {
            result.put("completion", completionHint);
        }
        return result;
    }
    
    private Map<String, Object> toCompletionTargetPayload(final MCPCompletionTargetDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("referenceType", descriptor.getReferenceType());
        result.put("reference", descriptor.getReference());
        result.put("arguments", descriptor.getArguments());
        result.put("maxValues", descriptor.getMaxValues());
        if (!descriptor.getMeta().isEmpty()) {
            result.put("meta", descriptor.getMeta());
        }
        return result;
    }
    
    private Map<String, Object> toResourceNavigationPayload(final MCPResourceNavigationDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(7, 1F);
        result.put("from", descriptor.getFrom());
        result.put("from_type", resolveReferenceType(descriptor.getFrom()));
        result.put("to", descriptor.getTo());
        result.put("to_type", resolveReferenceType(descriptor.getTo()));
        result.put("requiredArguments", descriptor.getRequiredArguments());
        result.put("carriedArguments", descriptor.getCarriedArguments());
        result.put("description", descriptor.getDescription());
        return result;
    }
    
    private Map<String, Object> createCompletionHint(final String argumentName) {
        List<MCPCompletionTargetDescriptor> descriptors = catalog.getShardingSphereDescriptors().getCompletionTargetDescriptors().stream()
                .filter(each -> each.getArguments().contains(argumentName)).toList();
        List<Map<String, Object>> references = descriptors.stream().map(this::createCompletionReferenceHint).toList();
        if (references.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("available", true);
        result.put("references", references);
        List<String> requiredContextArguments = createCompletionRequiredContextArguments(descriptors, argumentName);
        if (!requiredContextArguments.isEmpty()) {
            result.put("required_context_arguments", requiredContextArguments);
        }
        return result;
    }
    
    private Map<String, Object> createCompletionReferenceHint(final MCPCompletionTargetDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("referenceType", descriptor.getReferenceType());
        result.put("reference", descriptor.getReference());
        result.put("maxValues", descriptor.getMaxValues());
        return result;
    }
    
    private List<String> createCompletionRequiredContextArguments(final Collection<MCPCompletionTargetDescriptor> descriptors, final String argumentName) {
        Set<String> result = new LinkedHashSet<>();
        for (MCPCompletionTargetDescriptor each : descriptors) {
            Object requiredContextArguments = each.getMeta().get(MCPShardingSphereMetadataKeys.REQUIRED_CONTEXT_ARGUMENTS);
            if (!(requiredContextArguments instanceof Map)) {
                continue;
            }
            Object arguments = ((Map<?, ?>) requiredContextArguments).get(argumentName);
            if (arguments instanceof Collection) {
                ((Collection<?>) arguments).stream().map(String::valueOf).forEach(result::add);
            }
        }
        return result.stream().toList();
    }
    
    private String resolveReferenceType(final String reference) {
        if (catalog.getProtocolDescriptors().getAllResourceDescriptors().stream().anyMatch(each -> each.getUriOrTemplate().equals(reference))) {
            return reference.contains("{") ? "resource_template" : "resource";
        }
        if (catalog.getProtocolDescriptors().getToolDescriptors().stream().anyMatch(each -> each.getName().equals(reference))) {
            return "tool";
        }
        if (catalog.getProtocolDescriptors().getPromptDescriptors().stream().anyMatch(each -> each.getName().equals(reference))) {
            return "prompt";
        }
        return "unknown";
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
        result.put("readOnlyHint", annotations.isReadOnlyHint());
        result.put("destructiveHint", annotations.isDestructiveHint());
        result.put("idempotentHint", annotations.isIdempotentHint());
        result.put("openWorldHint", annotations.isOpenWorldHint());
        return result;
    }
    
    private void putIfPresent(final Map<String, Object> target, final String key, final Object value) {
        if (null != value) {
            target.put(key, value);
        }
    }
    
    private Map<String, Object> createProtocolAvailability(final boolean hasResourceNavigation) {
        Map<String, Object> result = new LinkedHashMap<>(12, 1F);
        result.put("resources", true);
        result.put("resourceTemplates", true);
        result.put("tools", true);
        result.put("toolAnnotations", true);
        result.put("toolOutputSchemas", true);
        result.put("prompts", !catalog.getProtocolDescriptors().getPromptDescriptors().isEmpty());
        result.put("completions", !catalog.getShardingSphereDescriptors().getCompletionTargetDescriptors().isEmpty());
        result.put("resourceNavigation", hasResourceNavigation);
        return result;
    }
}
