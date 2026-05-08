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
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MCP descriptor registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPDescriptorRegistry {
    
    private static final MCPDescriptorCatalog CATALOG = MCPDescriptorCatalogLoader.load();
    
    private static final Map<String, MCPResourceDescriptor> RESOURCE_DESCRIPTORS = createResourceDescriptors();
    
    private static final Map<String, MCPToolDescriptor> TOOL_DESCRIPTORS = createToolDescriptors();
    
    private static final Map<String, MCPPromptDescriptor> PROMPT_DESCRIPTORS = createPromptDescriptors();
    
    private static Map<String, MCPResourceDescriptor> createResourceDescriptors() {
        return CATALOG.getResourceDescriptors().stream()
                .collect(Collectors.toMap(MCPResourceDescriptor::getUriTemplate, each -> each, (a, b) -> b, () -> new LinkedHashMap<>(CATALOG.getResourceDescriptors().size(), 1F)));
    }
    
    private static Map<String, MCPToolDescriptor> createToolDescriptors() {
        return CATALOG.getToolDescriptors().stream()
                .collect(Collectors.toMap(MCPToolDescriptor::getName, each -> each, (a, b) -> b, () -> new LinkedHashMap<>(CATALOG.getToolDescriptors().size(), 1F)));
    }
    
    private static Map<String, MCPPromptDescriptor> createPromptDescriptors() {
        return CATALOG.getPromptDescriptors().stream()
                .collect(Collectors.toMap(MCPPromptDescriptor::getName, each -> each, (a, b) -> b, () -> new LinkedHashMap<>(CATALOG.getPromptDescriptors().size(), 1F)));
    }
    
    /**
     * Get required resource descriptor.
     *
     * @param uriTemplate URI template
     * @return resource descriptor
     */
    public static MCPResourceDescriptor getRequiredResourceDescriptor(final String uriTemplate) {
        return Optional.ofNullable(RESOURCE_DESCRIPTORS.get(uriTemplate)).orElseThrow(() -> new IllegalStateException(String.format("MCP resource descriptor is required for `%s`.", uriTemplate)));
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
        return CATALOG.getPromptDescriptors();
    }
    
    /**
     * Get completion target descriptors.
     *
     * @return completion target descriptors
     */
    public static Collection<MCPCompletionTargetDescriptor> getCompletionTargetDescriptors() {
        return CATALOG.getCompletionTargetDescriptors();
    }
    
    /**
     * Get resource navigation descriptors.
     *
     * @return resource navigation descriptors
     */
    public static Collection<MCPResourceNavigationDescriptor> getResourceNavigationDescriptors() {
        return CATALOG.getResourceNavigationDescriptors();
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
        return CATALOG.toPayload(supportedResources, supportedTools, supportedStatements);
    }
    
    /**
     * Get descriptor catalog fingerprint.
     *
     * @return descriptor catalog fingerprint
     */
    public static String getDescriptorCatalogFingerprint() {
        return CATALOG.getDescriptorCatalogFingerprint();
    }
}
