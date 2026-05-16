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

import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPResourceAnnotations;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPDescriptorCatalog;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPPromptDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPResourceExtensionDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPResourceNavigationDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPToolAnnotations;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPToolRuntimeDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPUriVariableDescriptor;
import org.apache.shardingsphere.mcp.support.yaml.MCPYamlConfigurationValidator;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

final class MCPDescriptorCatalogYamlSwapper {
    
    private MCPDescriptorCatalogYamlSwapper() {
    }
    
    static MCPDescriptorCatalog swap(final Collection<YamlMCPDescriptorCatalog> yamlCatalogs) {
        Collection<MCPResourceDescriptor> resourceDescriptors = new LinkedList<>();
        Collection<MCPResourceDescriptor> resourceTemplateDescriptors = new LinkedList<>();
        Collection<MCPResourceExtensionDescriptor> resourceExtensionDescriptors = new LinkedList<>();
        Collection<MCPToolDescriptor> toolDescriptors = new LinkedList<>();
        Collection<MCPPromptDescriptor> promptDescriptors = new LinkedList<>();
        Collection<MCPPromptTemplateBinding> promptTemplateBindings = new LinkedList<>();
        Collection<MCPCompletionTargetDescriptor> completionTargetDescriptors = new LinkedList<>();
        Collection<MCPResourceNavigationDescriptor> resourceNavigationDescriptors = new LinkedList<>();
        Collection<MCPToolRuntimeDescriptor> toolRuntimeDescriptors = new LinkedList<>();
        for (YamlMCPDescriptorCatalog each : yamlCatalogs) {
            MCPYamlConfigurationValidator.validate(each, "MCP descriptor catalog");
            swapFixedResourceDescriptors(each.getResources(), resourceDescriptors, resourceExtensionDescriptors);
            swapResourceTemplateDescriptors(each.getResourceTemplates(), resourceTemplateDescriptors, resourceExtensionDescriptors);
            toolDescriptors.addAll(swapToolDescriptors(each.getTools()));
            swapPromptDescriptors(each.getPrompts(), promptDescriptors, promptTemplateBindings);
            completionTargetDescriptors.addAll(swapCompletionTargetDescriptors(each.getCompletionTargets()));
            resourceNavigationDescriptors.addAll(swapResourceNavigationDescriptors(each.getResourceNavigation()));
            toolRuntimeDescriptors.addAll(swapToolRuntimeDescriptors(each.getTools()));
        }
        return new MCPDescriptorCatalog(resourceDescriptors, resourceTemplateDescriptors, resourceExtensionDescriptors, toolDescriptors, promptDescriptors, promptTemplateBindings,
                completionTargetDescriptors, resourceNavigationDescriptors, toolRuntimeDescriptors);
    }
    
    private static void swapFixedResourceDescriptors(final Collection<YamlMCPResourceDescriptor> yamlDescriptors, final Collection<MCPResourceDescriptor> resources,
                                                     final Collection<MCPResourceExtensionDescriptor> resourceExtensions) {
        for (YamlMCPResourceDescriptor each : emptyIfNull(yamlDescriptors)) {
            resources.add(new MCPResourceDescriptor(each.getUri(), each.getName(), each.getTitle(), each.getDescription(), each.getMimeType(),
                    swapResourceAnnotations(each.getAnnotations()), emptyMapIfNull(each.getMeta())));
            resourceExtensions.add(swapResourceExtension(each.getUri(), each.getExtension()));
        }
    }
    
    private static void swapResourceTemplateDescriptors(final Collection<YamlMCPResourceDescriptor> yamlDescriptors, final Collection<MCPResourceDescriptor> resourceTemplates,
                                                        final Collection<MCPResourceExtensionDescriptor> resourceExtensions) {
        for (YamlMCPResourceDescriptor each : emptyIfNull(yamlDescriptors)) {
            resourceTemplates.add(new MCPResourceDescriptor(each.getUriTemplate(), each.getName(), each.getTitle(), each.getDescription(), each.getMimeType(),
                    swapResourceAnnotations(each.getAnnotations()), emptyMapIfNull(each.getMeta())));
            resourceExtensions.add(swapResourceExtension(each.getUriTemplate(), each.getExtension()));
        }
    }
    
    private static MCPResourceExtensionDescriptor swapResourceExtension(final String uriOrTemplate, final YamlMCPResourceExtensionDescriptor yamlExtension) {
        if (null == yamlExtension) {
            return new MCPResourceExtensionDescriptor(uriOrTemplate, List.of(), null, null, null, List.of(), List.of(), List.of());
        }
        return new MCPResourceExtensionDescriptor(uriOrTemplate, swapUriVariables(yamlExtension.getUriVariables()), yamlExtension.getResourceKind(), yamlExtension.getObjectScope(),
                yamlExtension.getFeature(), emptyIfNull(yamlExtension.getRelatedTools()), emptyIfNull(yamlExtension.getRelatedResources()), emptyIfNull(yamlExtension.getUseBefore()));
    }
    
    private static List<MCPUriVariableDescriptor> swapUriVariables(final Collection<YamlMCPUriVariableDescriptor> yamlUriVariables) {
        List<MCPUriVariableDescriptor> result = new LinkedList<>();
        for (YamlMCPUriVariableDescriptor each : emptyIfNull(yamlUriVariables)) {
            result.add(new MCPUriVariableDescriptor(each.getName(), each.getTitle(), each.getDescription(), each.isRequired(), each.getScope()));
        }
        return result;
    }
    
    private static MCPResourceAnnotations swapResourceAnnotations(final YamlMCPResourceAnnotations yamlAnnotations) {
        return null == yamlAnnotations ? MCPResourceAnnotations.EMPTY : new MCPResourceAnnotations(yamlAnnotations.getAudience(), yamlAnnotations.getPriority(), yamlAnnotations.getLastModified());
    }
    
    private static Collection<MCPToolDescriptor> swapToolDescriptors(final Collection<YamlMCPToolDescriptor> yamlDescriptors) {
        Collection<MCPToolDescriptor> result = new LinkedList<>();
        for (YamlMCPToolDescriptor each : emptyIfNull(yamlDescriptors)) {
            result.add(new MCPToolDescriptor(each.getName(), each.getTitle(), each.getDescription(), emptyMapIfNull(each.getInputSchema()), emptyMapIfNull(each.getOutputSchema()),
                    swapToolAnnotations(each.getAnnotations()), emptyMapIfNull(each.getMeta())));
        }
        return result;
    }
    
    private static void swapPromptDescriptors(final Collection<YamlMCPPromptDescriptor> yamlDescriptors, final Collection<MCPPromptDescriptor> prompts,
                                              final Collection<MCPPromptTemplateBinding> promptTemplateBindings) {
        for (YamlMCPPromptDescriptor each : emptyIfNull(yamlDescriptors)) {
            prompts.add(new MCPPromptDescriptor(each.getName(), each.getTitle(), each.getDescription(), swapPromptArguments(each.getArguments()), emptyMapIfNull(each.getMeta())));
            promptTemplateBindings.add(new MCPPromptTemplateBinding(each.getName(), null == each.getBinding() ? null : each.getBinding().getTemplateResource()));
        }
    }
    
    private static List<MCPPromptArgumentDescriptor> swapPromptArguments(final Collection<YamlMCPPromptArgumentDescriptor> yamlArguments) {
        List<MCPPromptArgumentDescriptor> result = new LinkedList<>();
        for (YamlMCPPromptArgumentDescriptor each : emptyIfNull(yamlArguments)) {
            result.add(new MCPPromptArgumentDescriptor(each.getName(), each.getTitle(), each.getDescription(), each.isRequired()));
        }
        return result;
    }
    
    private static Collection<MCPCompletionTargetDescriptor> swapCompletionTargetDescriptors(final Collection<YamlMCPCompletionTargetDescriptor> yamlDescriptors) {
        Collection<MCPCompletionTargetDescriptor> result = new LinkedList<>();
        for (YamlMCPCompletionTargetDescriptor each : emptyIfNull(yamlDescriptors)) {
            result.add(new MCPCompletionTargetDescriptor(each.getReferenceType(), each.getReference(), emptyIfNull(each.getArguments()), each.getMaxValues(), emptyMapIfNull(each.getMeta())));
        }
        return result;
    }
    
    private static Collection<MCPResourceNavigationDescriptor> swapResourceNavigationDescriptors(final Collection<YamlMCPResourceNavigationDescriptor> yamlDescriptors) {
        Collection<MCPResourceNavigationDescriptor> result = new LinkedList<>();
        for (YamlMCPResourceNavigationDescriptor each : emptyIfNull(yamlDescriptors)) {
            result.add(new MCPResourceNavigationDescriptor(each.getFrom(), each.getTo(), emptyIfNull(each.getRequiredArguments()), emptyIfNull(each.getCarriedArguments()), each.getDescription()));
        }
        return result;
    }
    
    private static MCPToolAnnotations swapToolAnnotations(final YamlMCPToolAnnotations yamlAnnotations) {
        return new MCPToolAnnotations(yamlAnnotations.getTitle(), Boolean.TRUE.equals(yamlAnnotations.getReadOnlyHint()), Boolean.TRUE.equals(yamlAnnotations.getDestructiveHint()),
                Boolean.TRUE.equals(yamlAnnotations.getIdempotentHint()), Boolean.TRUE.equals(yamlAnnotations.getOpenWorldHint()));
    }
    
    private static Collection<MCPToolRuntimeDescriptor> swapToolRuntimeDescriptors(final Collection<YamlMCPToolDescriptor> yamlDescriptors) {
        Collection<MCPToolRuntimeDescriptor> result = new LinkedList<>();
        for (YamlMCPToolDescriptor each : emptyIfNull(yamlDescriptors)) {
            result.add(swapToolRuntimeDescriptor(each.getName(), each.getRuntime()));
        }
        return result;
    }
    
    private static MCPToolRuntimeDescriptor swapToolRuntimeDescriptor(final String toolName, final YamlMCPToolRuntimeDescriptor yamlRuntime) {
        return null == yamlRuntime ? new MCPToolRuntimeDescriptor(toolName, "", false, List.of())
                : new MCPToolRuntimeDescriptor(toolName, yamlRuntime.getWorkflowRole(), yamlRuntime.isRequiresUserApproval(), emptyIfNull(yamlRuntime.getSideEffectScope()));
    }
    
    private static <T> Collection<T> emptyIfNull(final Collection<T> values) {
        return null == values ? Collections.emptyList() : values;
    }
    
    private static Map<String, Object> emptyMapIfNull(final Map<String, Object> values) {
        return null == values ? Collections.emptyMap() : values;
    }
}
