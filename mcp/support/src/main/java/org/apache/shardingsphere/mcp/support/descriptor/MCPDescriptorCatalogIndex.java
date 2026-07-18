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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MCP descriptor catalog index.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPDescriptorCatalogIndex {
    
    private static final MCPDescriptorCatalog CATALOG = MCPDescriptorCatalogLoader.load();
    
    private static final Map<String, MCPResourceDescriptor> RESOURCE_DESCRIPTORS = createResourceDescriptors();
    
    private static final Map<String, ShardingSphereMCPResourceMetadata> SHARDINGSPHERE_RESOURCE_METADATA = createShardingSphereResourceMetadata();
    
    private static final Map<String, MCPToolDescriptor> TOOL_DESCRIPTORS = createToolDescriptors();
    
    private static final Collection<MCPPromptDescriptor> PROMPT_DESCRIPTORS = CATALOG.getProtocolDescriptors().getPromptDescriptors();
    
    private static final Collection<MCPCompletionTargetDescriptor> COMPLETION_TARGET_DESCRIPTORS = CATALOG.getShardingSphereDescriptors().getCompletionTargetDescriptors();
    
    private static final Map<String, Collection<MCPResourceNavigationDescriptor>> RESOURCE_NAVIGATION_DESCRIPTORS_BY_FROM = createResourceNavigationDescriptors();
    
    private static final Map<String, MCPPromptTemplateBinding> PROMPT_TEMPLATE_BINDINGS = createPromptTemplateBindings();
    
    private static final Map<String, MCPToolRuntimeDescriptor> TOOL_RUNTIME_DESCRIPTORS = createToolRuntimeDescriptors();
    
    private static final Map<String, String> PLANNING_TOOL_NAMES_BY_WORKFLOW_KIND = createPlanningToolNamesByWorkflowKind();
    
    private static final Map<String, Collection<String>> WORKFLOW_KINDS_BY_PROMPT_NAME = createWorkflowKindsByPromptName();
    
    private static Map<String, MCPResourceDescriptor> createResourceDescriptors() {
        int expectedSize = CATALOG.getProtocolDescriptors().getResourceDescriptors().size() + CATALOG.getProtocolDescriptors().getResourceTemplateDescriptors().size();
        Map<String, MCPResourceDescriptor> result = new LinkedHashMap<>(expectedSize, 1F);
        for (MCPResourceDescriptor each : CATALOG.getProtocolDescriptors().getResourceDescriptors()) {
            result.put(each.getUriTemplate(), each);
        }
        for (MCPResourceDescriptor each : CATALOG.getProtocolDescriptors().getResourceTemplateDescriptors()) {
            result.put(each.getUriTemplate(), each);
        }
        return result;
    }
    
    private static Map<String, ShardingSphereMCPResourceMetadata> createShardingSphereResourceMetadata() {
        return CATALOG.getShardingSphereDescriptors().getResourceMetadata().stream()
                .collect(Collectors.toMap(ShardingSphereMCPResourceMetadata::getUriTemplate, each -> each));
    }
    
    private static Map<String, MCPToolDescriptor> createToolDescriptors() {
        return CATALOG.getProtocolDescriptors().getToolDescriptors().stream()
                .collect(Collectors.toMap(MCPToolDescriptor::getName, each -> each, (a, b) -> b, () -> new LinkedHashMap<>(CATALOG.getProtocolDescriptors().getToolDescriptors().size(), 1F)));
    }
    
    private static Map<String, Collection<MCPResourceNavigationDescriptor>> createResourceNavigationDescriptors() {
        Map<String, Collection<MCPResourceNavigationDescriptor>> result = new LinkedHashMap<>(CATALOG.getShardingSphereDescriptors().getResourceNavigationDescriptors().size(), 1F);
        for (MCPResourceNavigationDescriptor each : CATALOG.getShardingSphereDescriptors().getResourceNavigationDescriptors()) {
            result.computeIfAbsent(each.getFrom(), unused -> new LinkedList<>()).add(each);
        }
        return result;
    }
    
    private static Map<String, MCPPromptTemplateBinding> createPromptTemplateBindings() {
        return CATALOG.getShardingSphereDescriptors().getPromptTemplateBindings().stream().collect(Collectors.toMap(MCPPromptTemplateBinding::getPromptName, each -> each));
    }
    
    private static Map<String, MCPToolRuntimeDescriptor> createToolRuntimeDescriptors() {
        return CATALOG.getShardingSphereDescriptors().getToolRuntimeDescriptors().stream().collect(Collectors.toMap(MCPToolRuntimeDescriptor::getToolName, each -> each));
    }
    
    private static Map<String, String> createPlanningToolNamesByWorkflowKind() {
        Map<String, String> result = new LinkedHashMap<>(CATALOG.getShardingSphereDescriptors().getToolRuntimeDescriptors().size(), 1F);
        for (MCPToolRuntimeDescriptor each : CATALOG.getShardingSphereDescriptors().getToolRuntimeDescriptors()) {
            if (!"plan".equals(each.getWorkflowRole())) {
                continue;
            }
            MCPToolDescriptor toolDescriptor = TOOL_DESCRIPTORS.get(each.getToolName());
            if (null != toolDescriptor && toolDescriptor.getMeta().containsKey(MCPShardingSphereMetadataKeys.WORKFLOW_KIND)) {
                result.put(String.valueOf(toolDescriptor.getMeta().get(MCPShardingSphereMetadataKeys.WORKFLOW_KIND)), each.getToolName());
            }
        }
        return result;
    }
    
    private static Map<String, Collection<String>> createWorkflowKindsByPromptName() {
        Map<String, Collection<String>> result = new LinkedHashMap<>(PROMPT_DESCRIPTORS.size(), 1F);
        for (MCPPromptDescriptor each : PROMPT_DESCRIPTORS) {
            Collection<String> workflowKinds = findPromptPlanningWorkflowKind(each);
            if (!workflowKinds.isEmpty()) {
                result.put(each.getName(), workflowKinds);
            }
        }
        return result;
    }
    
    private static Collection<String> findPromptPlanningWorkflowKind(final MCPPromptDescriptor prompt) {
        String planningToolName = "database_gateway_" + prompt.getName();
        Object relatedTools = prompt.getMeta().get(MCPShardingSphereMetadataKeys.RELATED_TOOLS);
        MCPToolDescriptor toolDescriptor = TOOL_DESCRIPTORS.get(planningToolName);
        if (!(relatedTools instanceof Collection<?>)) {
            return List.of();
        }
        if (!((Collection<?>) relatedTools).contains(planningToolName) || null == toolDescriptor || !isPlanningTool(planningToolName)) {
            return List.of();
        }
        String workflowKind = Objects.toString(toolDescriptor.getMeta().get(MCPShardingSphereMetadataKeys.WORKFLOW_KIND), "");
        return workflowKind.isEmpty() ? List.of() : List.of(workflowKind);
    }
    
    private static boolean isPlanningTool(final String toolName) {
        MCPToolRuntimeDescriptor runtimeDescriptor = TOOL_RUNTIME_DESCRIPTORS.get(toolName);
        return null != runtimeDescriptor && "plan".equals(runtimeDescriptor.getWorkflowRole());
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
     * @param uriTemplate resource URI template
     * @return resource descriptor
     */
    public static MCPResourceDescriptor getRequiredResourceDescriptor(final String uriTemplate) {
        return Optional.ofNullable(RESOURCE_DESCRIPTORS.get(uriTemplate)).orElseThrow(() -> new IllegalStateException(String.format("MCP resource descriptor is required for `%s`.", uriTemplate)));
    }
    
    /**
     * Get required ShardingSphere MCP resource metadata descriptor.
     *
     * @param uriTemplate resource URI template
     * @return ShardingSphere MCP resource metadata descriptor
     */
    public static ShardingSphereMCPResourceMetadata getRequiredShardingSphereResourceMetadata(final String uriTemplate) {
        return Optional.ofNullable(SHARDINGSPHERE_RESOURCE_METADATA.get(uriTemplate)).orElseThrow(
                () -> new IllegalStateException(String.format("ShardingSphere MCP resource metadata descriptor is required for `%s`.", uriTemplate)));
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
     * Find planning tool name by workflow kind.
     *
     * @param workflowKind workflow kind
     * @return found planning tool name
     */
    public static Optional<String> findPlanningToolNameByWorkflowKind(final String workflowKind) {
        return Optional.ofNullable(PLANNING_TOOL_NAMES_BY_WORKFLOW_KIND.get(workflowKind));
    }
    
    /**
     * Find workflow kinds related to a completion target.
     *
     * @param descriptor completion target descriptor
     * @return related workflow kinds
     */
    public static Collection<String> findWorkflowKindsByCompletionTarget(final MCPCompletionTargetDescriptor descriptor) {
        if (!"prompt".equals(descriptor.getReferenceType())) {
            return List.of();
        }
        return WORKFLOW_KINDS_BY_PROMPT_NAME.getOrDefault(descriptor.getReference(), List.of()).stream().toList();
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
     * Create capability catalog payload.
     *
     * @param supportedStatements supported statement classes
     * @return capability catalog payload
     */
    public static Map<String, Object> createCapabilityPayload(final Collection<?> supportedStatements) {
        return MCPDescriptorCatalogPayloadBuilder.build(CATALOG, supportedStatements);
    }
    
    /**
     * Create model-facing guidance payload.
     *
     * @return guidance payload
     */
    public static Map<String, Object> createGuidancePayload() {
        return MCPGuidancePayloadBuilder.build();
    }
}
