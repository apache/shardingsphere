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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MCP descriptor catalog index.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPDescriptorCatalogIndex {
    
    private static final MCPDescriptorCatalog CATALOG = MCPDescriptorCatalogLoader.load();
    
    private static final Map<String, MCPResourceDescriptor> RESOURCE_DESCRIPTORS = createResourceDescriptors();
    
    private static final Map<String, MCPResourceExtensionDescriptor> RESOURCE_EXTENSION_DESCRIPTORS = createResourceExtensionDescriptors();
    
    private static final Map<String, MCPToolDescriptor> TOOL_DESCRIPTORS = createToolDescriptors();
    
    private static final Collection<MCPPromptDescriptor> PROMPT_DESCRIPTORS = CATALOG.getPromptDescriptors();
    
    private static final Collection<MCPCompletionTargetDescriptor> COMPLETION_TARGET_DESCRIPTORS = CATALOG.getCompletionTargetDescriptors();
    
    private static final Map<String, Collection<MCPResourceNavigationDescriptor>> RESOURCE_NAVIGATION_DESCRIPTORS_BY_FROM = createResourceNavigationDescriptors();
    
    private static final Map<String, MCPPromptTemplateBinding> PROMPT_TEMPLATE_BINDINGS = createPromptTemplateBindings();
    
    private static final Map<String, MCPToolRuntimeDescriptor> TOOL_RUNTIME_DESCRIPTORS = createToolRuntimeDescriptors();
    
    private static Map<String, MCPResourceDescriptor> createResourceDescriptors() {
        Map<String, MCPResourceDescriptor> result = new LinkedHashMap<>(CATALOG.getResourceDescriptors().size() + CATALOG.getResourceTemplateDescriptors().size(), 1F);
        for (MCPResourceDescriptor each : CATALOG.getResourceDescriptors()) {
            result.put(each.getUriTemplate(), each);
        }
        for (MCPResourceDescriptor each : CATALOG.getResourceTemplateDescriptors()) {
            result.put(each.getUriTemplate(), each);
        }
        return result;
    }
    
    private static Map<String, MCPResourceExtensionDescriptor> createResourceExtensionDescriptors() {
        return CATALOG.getResourceExtensionDescriptors().stream()
                .collect(Collectors.toMap(MCPResourceExtensionDescriptor::getUriOrTemplate, each -> each, (a, b) -> b,
                        () -> new LinkedHashMap<>(CATALOG.getResourceExtensionDescriptors().size(), 1F)));
    }
    
    private static Map<String, MCPToolDescriptor> createToolDescriptors() {
        return CATALOG.getToolDescriptors().stream()
                .collect(Collectors.toMap(MCPToolDescriptor::getName, each -> each, (a, b) -> b, () -> new LinkedHashMap<>(CATALOG.getToolDescriptors().size(), 1F)));
    }
    
    private static Map<String, Collection<MCPResourceNavigationDescriptor>> createResourceNavigationDescriptors() {
        Map<String, Collection<MCPResourceNavigationDescriptor>> result = new LinkedHashMap<>(CATALOG.getResourceNavigationDescriptors().size(), 1F);
        for (MCPResourceNavigationDescriptor each : CATALOG.getResourceNavigationDescriptors()) {
            result.computeIfAbsent(each.getFrom(), unused -> new LinkedList<>()).add(each);
        }
        return result;
    }
    
    private static Map<String, MCPPromptTemplateBinding> createPromptTemplateBindings() {
        return CATALOG.getPromptTemplateBindings().stream()
                .collect(Collectors.toMap(MCPPromptTemplateBinding::getPromptName, each -> each, (a, b) -> b, () -> new LinkedHashMap<>(CATALOG.getPromptTemplateBindings().size(), 1F)));
    }
    
    private static Map<String, MCPToolRuntimeDescriptor> createToolRuntimeDescriptors() {
        return CATALOG.getToolRuntimeDescriptors().stream()
                .collect(Collectors.toMap(MCPToolRuntimeDescriptor::getToolName, each -> each, (a, b) -> b, () -> new LinkedHashMap<>(CATALOG.getToolRuntimeDescriptors().size(), 1F)));
    }
    
    /**
     * Get resource descriptors.
     *
     * @return resource descriptors
     */
    public static Collection<MCPResourceDescriptor> getResourceDescriptors() {
        return RESOURCE_DESCRIPTORS.values().stream().toList();
    }
    
    /**
     * Get required resource descriptor.
     *
     * @param uriOrTemplate resource URI or resource template URI template
     * @return resource descriptor
     */
    public static MCPResourceDescriptor getRequiredResourceDescriptor(final String uriOrTemplate) {
        return Optional.ofNullable(RESOURCE_DESCRIPTORS.get(uriOrTemplate)).orElseThrow(() -> new IllegalStateException(String.format("MCP resource descriptor is required for `%s`.", uriOrTemplate)));
    }
    
    /**
     * Get required resource extension descriptor.
     *
     * @param uriOrTemplate resource URI or resource template URI template
     * @return resource extension descriptor
     */
    public static MCPResourceExtensionDescriptor getRequiredResourceExtensionDescriptor(final String uriOrTemplate) {
        return Optional.ofNullable(RESOURCE_EXTENSION_DESCRIPTORS.get(uriOrTemplate)).orElseThrow(
                () -> new IllegalStateException(String.format("MCP resource extension descriptor is required for `%s`.", uriOrTemplate)));
    }
    
    /**
     * Get tool descriptors.
     *
     * @return tool descriptors
     */
    public static Collection<MCPToolDescriptor> getToolDescriptors() {
        return TOOL_DESCRIPTORS.values().stream().toList();
    }
    
    /**
     * Get required tool descriptor.
     *
     * @param toolName tool name
     * @return tool descriptor
     */
    public static MCPToolDescriptor getRequiredToolDescriptor(final String toolName) {
        return Optional.ofNullable(TOOL_DESCRIPTORS.get(toolName)).orElseThrow(() -> new IllegalStateException(String.format("MCP tool descriptor is required for `%s`.", toolName)));
    }
    
    /**
     * Get prompt descriptors.
     *
     * @return prompt descriptors
     */
    public static Collection<MCPPromptDescriptor> getPromptDescriptors() {
        return PROMPT_DESCRIPTORS.stream().toList();
    }
    
    /**
     * Get required prompt template binding.
     *
     * @param promptName prompt name
     * @return prompt template binding
     */
    public static MCPPromptTemplateBinding getRequiredPromptTemplateBinding(final String promptName) {
        return Optional.ofNullable(PROMPT_TEMPLATE_BINDINGS.get(promptName)).orElseThrow(
                () -> new IllegalStateException(String.format("MCP prompt template binding is required for `%s`.", promptName)));
    }
    
    /**
     * Find tool runtime descriptor.
     *
     * @param toolName tool name
     * @return found tool runtime descriptor
     */
    public static Optional<MCPToolRuntimeDescriptor> findToolRuntimeDescriptor(final String toolName) {
        return Optional.ofNullable(TOOL_RUNTIME_DESCRIPTORS.get(toolName));
    }
    
    /**
     * Get completion target descriptors.
     *
     * @return completion target descriptors
     */
    public static Collection<MCPCompletionTargetDescriptor> getCompletionTargetDescriptors() {
        return COMPLETION_TARGET_DESCRIPTORS.stream().toList();
    }
    
    /**
     * Get resource navigation descriptors.
     *
     * @return resource navigation descriptors
     */
    public static Collection<MCPResourceNavigationDescriptor> getResourceNavigationDescriptors() {
        return RESOURCE_NAVIGATION_DESCRIPTORS_BY_FROM.values().stream().flatMap(Collection::stream).toList();
    }
    
    /**
     * Get resource navigation descriptors by source.
     *
     * @param from source resource URI, resource template, tool name or prompt name
     * @return resource navigation descriptors
     */
    public static Collection<MCPResourceNavigationDescriptor> getResourceNavigationDescriptors(final String from) {
        return RESOURCE_NAVIGATION_DESCRIPTORS_BY_FROM.getOrDefault(from, List.of()).stream().toList();
    }
    
    /**
     * Create model-facing capability payload.
     *
     * @param supportedResources supported resource URI templates
     * @param supportedTools supported tool names
     * @param supportedStatements supported statement classes
     * @return capability payload
     */
    public static Map<String, Object> createCapabilityPayload(final Collection<String> supportedResources, final Collection<String> supportedTools, final Collection<?> supportedStatements) {
        return MCPDescriptorCatalogPayloadBuilder.build(CATALOG, supportedResources, supportedTools, supportedStatements);
    }
    
    /**
     * Get descriptor catalog fingerprint.
     *
     * @return descriptor catalog fingerprint
     */
    public static String getDescriptorCatalogFingerprint() {
        return MCPDescriptorCatalogPayloadBuilder.createDescriptorCatalogFingerprint(CATALOG);
    }
}
