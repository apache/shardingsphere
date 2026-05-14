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
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * MCP descriptor catalog.
 */
@Getter
public final class MCPDescriptorCatalog {
    
    private final Collection<MCPResourceDescriptor> resourceDescriptors;
    
    private final Collection<MCPResourceDescriptor> resourceTemplateDescriptors;
    
    private final Collection<MCPResourceExtensionDescriptor> resourceExtensionDescriptors;
    
    private final Collection<MCPToolDescriptor> toolDescriptors;
    
    private final Collection<MCPPromptDescriptor> promptDescriptors;
    
    private final Collection<MCPPromptTemplateBinding> promptTemplateBindings;
    
    private final Collection<MCPToolRuntimeDescriptor> toolRuntimeDescriptors;
    
    private final Collection<MCPCompletionTargetDescriptor> completionTargetDescriptors;
    
    private final Collection<MCPResourceNavigationDescriptor> resourceNavigationDescriptors;
    
    public MCPDescriptorCatalog(final Collection<MCPResourceDescriptor> resourceDescriptors, final Collection<MCPResourceDescriptor> resourceTemplateDescriptors,
                                final Collection<MCPResourceExtensionDescriptor> resourceExtensionDescriptors, final Collection<MCPToolDescriptor> toolDescriptors,
                                final Collection<MCPPromptDescriptor> promptDescriptors, final Collection<MCPPromptTemplateBinding> promptTemplateBindings,
                                final Collection<MCPCompletionTargetDescriptor> completionTargetDescriptors, final Collection<MCPResourceNavigationDescriptor> resourceNavigationDescriptors,
                                final Collection<MCPToolRuntimeDescriptor> toolRuntimeDescriptors) {
        this.resourceDescriptors = null == resourceDescriptors ? Collections.emptyList() : resourceDescriptors;
        this.resourceTemplateDescriptors = null == resourceTemplateDescriptors ? Collections.emptyList() : resourceTemplateDescriptors;
        this.resourceExtensionDescriptors = null == resourceExtensionDescriptors ? Collections.emptyList() : resourceExtensionDescriptors;
        this.toolDescriptors = null == toolDescriptors ? Collections.emptyList() : toolDescriptors;
        this.promptDescriptors = null == promptDescriptors ? Collections.emptyList() : promptDescriptors;
        this.promptTemplateBindings = null == promptTemplateBindings ? Collections.emptyList() : promptTemplateBindings;
        this.completionTargetDescriptors = null == completionTargetDescriptors ? Collections.emptyList() : completionTargetDescriptors;
        this.resourceNavigationDescriptors = null == resourceNavigationDescriptors ? Collections.emptyList() : resourceNavigationDescriptors;
        this.toolRuntimeDescriptors = null == toolRuntimeDescriptors ? Collections.emptyList() : toolRuntimeDescriptors;
    }
    
    /**
     * Get all resource descriptors.
     *
     * @return all resource descriptors
     */
    public Collection<MCPResourceDescriptor> getAllResourceDescriptors() {
        Collection<MCPResourceDescriptor> result = new LinkedList<>();
        result.addAll(resourceDescriptors);
        result.addAll(resourceTemplateDescriptors);
        return result;
    }
}
