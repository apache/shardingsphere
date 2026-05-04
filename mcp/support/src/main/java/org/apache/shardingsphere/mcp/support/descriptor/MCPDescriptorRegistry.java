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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MCP descriptor registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPDescriptorRegistry {
    
    private static final MCPDescriptorCatalog CATALOG = MCPDescriptorCatalogLoader.load();
    
    private static final Map<String, MCPResourceDescriptor> RESOURCE_DESCRIPTORS = createResourceDescriptors();
    
    private static final Map<String, MCPToolDescriptor> TOOL_DESCRIPTORS = createToolDescriptors();
    
    private static Map<String, MCPResourceDescriptor> createResourceDescriptors() {
        Map<String, MCPResourceDescriptor> result = new LinkedHashMap<>(CATALOG.getResourceDescriptors().size(), 1F);
        for (MCPResourceDescriptor each : CATALOG.getResourceDescriptors()) {
            result.put(each.getUriPattern(), each);
        }
        return Collections.unmodifiableMap(result);
    }
    
    private static Map<String, MCPToolDescriptor> createToolDescriptors() {
        Map<String, MCPToolDescriptor> result = new LinkedHashMap<>(CATALOG.getToolDescriptors().size(), 1F);
        for (MCPToolDescriptor each : CATALOG.getToolDescriptors()) {
            result.put(each.getName(), each);
        }
        return Collections.unmodifiableMap(result);
    }
    
    /**
     * Get resource descriptors.
     *
     * @return resource descriptors
     */
    public static Collection<MCPResourceDescriptor> getResourceDescriptors() {
        return RESOURCE_DESCRIPTORS.values();
    }
    
    /**
     * Get tool descriptors.
     *
     * @return tool descriptors
     */
    public static Collection<MCPToolDescriptor> getToolDescriptors() {
        return TOOL_DESCRIPTORS.values();
    }
    
    /**
     * Get required resource descriptor.
     *
     * @param uriPattern URI pattern
     * @return resource descriptor
     */
    public static MCPResourceDescriptor getRequiredResourceDescriptor(final String uriPattern) {
        return Optional.ofNullable(RESOURCE_DESCRIPTORS.get(uriPattern))
                .orElseThrow(() -> new IllegalStateException(String.format("MCP resource descriptor is required for `%s`.", uriPattern)));
    }
    
    /**
     * Get required tool descriptor.
     *
     * @param toolName tool name
     * @return tool descriptor
     */
    public static MCPToolDescriptor getRequiredToolDescriptor(final String toolName) {
        return Optional.ofNullable(TOOL_DESCRIPTORS.get(toolName))
                .orElseThrow(() -> new IllegalStateException(String.format("MCP tool descriptor is required for `%s`.", toolName)));
    }
    
    /**
     * Create model-facing capability payload.
     *
     * @param supportedResources supported resource URI patterns
     * @param supportedTools supported tool names
     * @param supportedStatements supported statement classes
     * @return capability payload
     */
    public static Map<String, Object> createCapabilityPayload(final Collection<String> supportedResources, final Collection<String> supportedTools,
                                                              final Collection<?> supportedStatements) {
        return CATALOG.toPayload(supportedResources, supportedTools, supportedStatements);
    }
}
