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

import org.apache.shardingsphere.mcp.api.common.descriptor.MCPAnnotations;
import org.apache.shardingsphere.mcp.api.common.descriptor.MCPIcon;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPFixedResourceDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceTemplateDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

final class MCPDescriptorCatalogPayloadBuilder {
    
    private final MCPDescriptorCatalog catalog;
    
    private final MCPModelFirstContractPayloadBuilder modelFirstContractPayloadBuilder;
    
    private MCPDescriptorCatalogPayloadBuilder(final MCPDescriptorCatalog catalog) {
        this.catalog = catalog;
        modelFirstContractPayloadBuilder = new MCPModelFirstContractPayloadBuilder(catalog);
    }
    
    static Map<String, Object> build(final MCPDescriptorCatalog catalog, final Collection<String> supportedResources, final Collection<String> supportedTools,
                                     final Collection<?> supportedStatements) {
        return new MCPDescriptorCatalogPayloadBuilder(catalog).build(supportedResources, supportedTools, supportedStatements);
    }
    
    private Map<String, Object> build(final Collection<String> supportedResources, final Collection<String> supportedTools, final Collection<?> supportedStatements) {
        Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        List<Map<String, Object>> resources = catalog.getResourceDescriptors().stream().map(this::toResourcePayload).toList();
        List<Map<String, Object>> resourceTemplates = catalog.getResourceTemplateDescriptors().stream().map(this::toResourceTemplatePayload).toList();
        List<Map<String, Object>> tools = catalog.getToolDescriptors().stream().map(this::toToolPayload).toList();
        List<Map<String, Object>> prompts = catalog.getPromptDescriptors().stream().map(this::toPromptPayload).toList();
        List<Map<String, Object>> completionTargets = catalog.getCompletionTargetDescriptors().stream().map(this::toCompletionTargetPayload).toList();
        List<Map<String, Object>> resourceNavigation = catalog.getResourceNavigationDescriptors().stream().map(this::toResourceNavigationPayload).toList();
        result.put("response_mode", MCPResponseMode.CATALOG);
        result.put("model_first_summary", modelFirstContractPayloadBuilder.createModelFirstSummary());
        result.put("supportedResources", supportedResources);
        result.put("supportedTools", supportedTools);
        result.put("supportedStatementClasses", supportedStatements);
        result.put("model_contract", modelFirstContractPayloadBuilder.createModelContract());
        result.put("surface_summary", modelFirstContractPayloadBuilder.createSurfaceSummary());
        result.put("field_naming_contract", modelFirstContractPayloadBuilder.createFieldNamingContract());
        result.put("next_action_contract", modelFirstContractPayloadBuilder.createNextActionContract());
        result.put("common_flows", modelFirstContractPayloadBuilder.createCommonFlows());
        result.put("security_hints", modelFirstContractPayloadBuilder.createSecurityHints());
        result.put("resources", resources);
        result.put("resourceTemplates", resourceTemplates);
        result.put("tools", tools);
        result.put("prompts", prompts);
        result.put("completionTargets", completionTargets);
        result.put("resourceNavigation", resourceNavigation);
        result.put("protocolAvailability", createProtocolAvailability(!resourceNavigation.isEmpty()));
        result.put("fingerprints", createFingerprints(resources, resourceTemplates, tools, prompts, completionTargets, resourceNavigation));
        return result;
    }
    
    static String createDescriptorCatalogFingerprint(final MCPDescriptorCatalog catalog) {
        return new MCPDescriptorCatalogPayloadBuilder(catalog).createDescriptorCatalogFingerprint();
    }
    
    private String createDescriptorCatalogFingerprint() {
        List<Map<String, Object>> resources = catalog.getResourceDescriptors().stream().map(this::toResourcePayload).toList();
        List<Map<String, Object>> resourceTemplates = catalog.getResourceTemplateDescriptors().stream().map(this::toResourceTemplatePayload).toList();
        List<Map<String, Object>> tools = catalog.getToolDescriptors().stream().map(this::toToolPayload).toList();
        List<Map<String, Object>> prompts = catalog.getPromptDescriptors().stream().map(this::toPromptPayload).toList();
        List<Map<String, Object>> completionTargets = catalog.getCompletionTargetDescriptors().stream().map(this::toCompletionTargetPayload).toList();
        List<Map<String, Object>> resourceNavigation = catalog.getResourceNavigationDescriptors().stream().map(this::toResourceNavigationPayload).toList();
        return String.valueOf(createFingerprints(resources, resourceTemplates, tools, prompts, completionTargets, resourceNavigation).get("descriptorCatalog"));
    }
    
    private Map<String, Object> toResourcePayload(final MCPFixedResourceDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("uri", descriptor.getUri());
        result.put("name", descriptor.getName());
        result.put("title", descriptor.getTitle());
        result.put("description", descriptor.getDescription());
        putIfNotEmpty(result, "icons", descriptor.getIcons().stream().map(this::toIconPayload).toList());
        result.put("mimeType", descriptor.getMimeType());
        if (!descriptor.getAnnotations().isEmpty()) {
            result.put("annotations", toResourceAnnotationsPayload(descriptor.getAnnotations()));
        }
        Map<String, Object> meta = createResourceMeta(descriptor);
        if (!meta.isEmpty()) {
            result.put("meta", meta);
        }
        return result;
    }
    
    private Map<String, Object> toResourceTemplatePayload(final MCPResourceTemplateDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("uriTemplate", descriptor.getUriTemplate());
        result.put("name", descriptor.getName());
        result.put("title", descriptor.getTitle());
        result.put("description", descriptor.getDescription());
        putIfNotEmpty(result, "icons", descriptor.getIcons().stream().map(this::toIconPayload).toList());
        result.put("mimeType", descriptor.getMimeType());
        if (!descriptor.getAnnotations().isEmpty()) {
            result.put("annotations", toResourceAnnotationsPayload(descriptor.getAnnotations()));
        }
        Map<String, Object> meta = createResourceMeta(descriptor);
        if (!meta.isEmpty()) {
            result.put("meta", meta);
        }
        return result;
    }
    
    private Map<String, Object> createResourceMeta(final MCPResourceDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(descriptor.getMeta());
        MCPResourceExtensionDescriptor extension = findResourceExtension(MCPResourceDescriptorUtils.getUriOrTemplate(descriptor));
        if (null == extension) {
            return result;
        }
        putIfPresent(result, MCPShardingSphereMetadataKeys.RESOURCE_KIND, extension.getResourceKind());
        putIfPresent(result, MCPShardingSphereMetadataKeys.OBJECT_SCOPE, extension.getObjectScope());
        putIfPresent(result, MCPShardingSphereMetadataKeys.FEATURE, extension.getFeature());
        putIfNotEmpty(result, MCPShardingSphereMetadataKeys.RELATED_TOOLS, extension.getRelatedTools());
        putIfNotEmpty(result, MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS, extension.getRelatedResources());
        putIfNotEmpty(result, MCPShardingSphereMetadataKeys.USE_BEFORE, extension.getUseBefore());
        if (!extension.getUriVariables().isEmpty()) {
            result.put(MCPShardingSphereMetadataKeys.URI_VARIABLES, extension.getUriVariables().stream().map(this::toUriVariablePayload).toList());
        }
        return result;
    }
    
    private MCPResourceExtensionDescriptor findResourceExtension(final String uriOrTemplate) {
        return catalog.getResourceExtensionDescriptors().stream().filter(each -> uriOrTemplate.equals(each.getUriOrTemplate())).findFirst().orElse(null);
    }
    
    private Map<String, Object> toUriVariablePayload(final MCPUriVariableDescriptor parameter) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
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
        putIfNotEmpty(result, "icons", descriptor.getIcons().stream().map(this::toIconPayload).toList());
        result.put("inputSchema", descriptor.getInputSchema());
        result.put("outputSchema", descriptor.getOutputSchema());
        if (!descriptor.getAnnotations().isEmpty()) {
            result.put("annotations", toToolAnnotationsPayload(descriptor.getAnnotations()));
        }
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
        putIfNotEmpty(result, "icons", descriptor.getIcons().stream().map(this::toIconPayload).toList());
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
    
    private Map<String, Object> toIconPayload(final MCPIcon icon) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        putIfPresent(result, "src", icon.getSrc());
        putIfPresent(result, "mimeType", icon.getMimeType());
        putIfNotEmpty(result, "sizes", icon.getSizes());
        putIfPresent(result, "theme", icon.getTheme());
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
        List<Map<String, Object>> references = catalog.getCompletionTargetDescriptors().stream()
                .filter(each -> each.getArguments().contains(argumentName)).map(this::createCompletionReferenceHint).toList();
        if (references.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("available", true);
        result.put("references", references);
        List<String> requiredContextArguments = createCompletionRequiredContextArguments(argumentName);
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
    
    private List<String> createCompletionRequiredContextArguments(final String argumentName) {
        if ("schema".equals(argumentName)) {
            return List.of("database");
        }
        if ("table".equals(argumentName) || "sequence".equals(argumentName)) {
            return List.of("database", "schema");
        }
        if ("column".equals(argumentName) || "index".equals(argumentName)) {
            return List.of("database", "schema", "table");
        }
        return List.of();
    }
    
    private String resolveReferenceType(final String reference) {
        if (catalog.getAllResourceDescriptors().stream().anyMatch(each -> MCPResourceDescriptorUtils.getUriOrTemplate(each).equals(reference))) {
            return reference.contains("{") ? "resource_template" : "resource";
        }
        if (catalog.getToolDescriptors().stream().anyMatch(each -> each.getName().equals(reference))) {
            return "tool";
        }
        if (catalog.getPromptDescriptors().stream().anyMatch(each -> each.getName().equals(reference))) {
            return "prompt";
        }
        return "unknown";
    }
    
    private Map<String, Object> toResourceAnnotationsPayload(final MCPAnnotations annotations) {
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
        return result;
    }
    
    private void putIfPresent(final Map<String, Object> target, final String key, final Object value) {
        if (null != value) {
            target.put(key, value);
        }
    }
    
    private void putIfNotEmpty(final Map<String, Object> target, final String key, final Collection<?> values) {
        if (!values.isEmpty()) {
            target.put(key, values);
        }
    }
    
    private Map<String, Object> createProtocolAvailability(final boolean hasResourceNavigation) {
        Map<String, Object> result = new LinkedHashMap<>(12, 1F);
        result.put("resources", true);
        result.put("resourceTemplates", true);
        result.put("tools", true);
        result.put("toolAnnotations", true);
        result.put("toolOutputSchemas", true);
        result.put("prompts", !catalog.getPromptDescriptors().isEmpty());
        result.put("completions", !catalog.getCompletionTargetDescriptors().isEmpty());
        result.put("resourceNavigation", hasResourceNavigation);
        return result;
    }
    
    private Map<String, Object> createFingerprints(final List<Map<String, Object>> resources, final List<Map<String, Object>> resourceTemplates,
                                                   final List<Map<String, Object>> tools, final List<Map<String, Object>> prompts,
                                                   final List<Map<String, Object>> completionTargets, final List<Map<String, Object>> resourceNavigation) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("algorithm", "sha256");
        result.put("descriptorCatalog", createHash(Map.of(
                "resources", resources,
                "resourceTemplates", resourceTemplates,
                "tools", tools,
                "prompts", prompts,
                "completionTargets", completionTargets,
                "resourceNavigation", resourceNavigation)));
        result.put("promptSet", createHash(prompts));
        result.put("resourceNavigation", createHash(resourceNavigation));
        result.put("modelFacingSchemas", createHash(createModelFacingSchemas(resources, resourceTemplates, tools)));
        return result;
    }
    
    private List<Map<String, Object>> createModelFacingSchemas(final List<Map<String, Object>> resources, final List<Map<String, Object>> resourceTemplates,
                                                               final List<Map<String, Object>> tools) {
        List<Map<String, Object>> result = new LinkedList<>();
        resources.stream().map(this::createResourceSchema).forEach(result::add);
        resourceTemplates.stream().map(this::createResourceSchema).forEach(result::add);
        tools.stream().map(this::createToolSchema).forEach(result::add);
        return result;
    }
    
    private Map<String, Object> createResourceSchema(final Map<String, Object> resource) {
        return resource.containsKey("uriTemplate") ? Map.of("uriTemplate", resource.get("uriTemplate")) : Map.of("uri", resource.get("uri"));
    }
    
    private Map<String, Object> createToolSchema(final Map<String, Object> tool) {
        return Map.of("name", tool.get("name"), "inputSchema", tool.get("inputSchema"), "outputSchema", tool.get("outputSchema"));
    }
    
    private String createHash(final Object value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(canonicalize(value).getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }
    
    private String toHex(final byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte each : bytes) {
            int value = each & 0xFF;
            if (value < 16) {
                result.append('0');
            }
            result.append(Integer.toHexString(value));
        }
        return result.toString();
    }
    
    private String canonicalize(final Object value) {
        if (null == value) {
            return "null";
        }
        if (value instanceof Map) {
            return canonicalizeMap((Map<?, ?>) value);
        }
        if (value instanceof Collection) {
            return canonicalizeCollection((Collection<?>) value);
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return "\"" + escape(String.valueOf(value)) + "\"";
    }
    
    private String canonicalizeMap(final Map<?, ?> value) {
        StringBuilder result = new StringBuilder("{");
        List<? extends Entry<?, ?>> entries = value.entrySet().stream()
                .sorted(Comparator.comparing(each -> String.valueOf(each.getKey()))).toList();
        for (int index = 0; index < entries.size(); index++) {
            if (0 < index) {
                result.append(',');
            }
            Entry<?, ?> entry = entries.get(index);
            result.append(canonicalize(String.valueOf(entry.getKey()))).append(':').append(canonicalize(entry.getValue()));
        }
        return result.append('}').toString();
    }
    
    private String canonicalizeCollection(final Collection<?> value) {
        StringBuilder result = new StringBuilder("[");
        int index = 0;
        for (Object each : value) {
            if (0 < index++) {
                result.append(',');
            }
            result.append(canonicalize(each));
        }
        return result.append(']').toString();
    }
    
    private String escape(final String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
