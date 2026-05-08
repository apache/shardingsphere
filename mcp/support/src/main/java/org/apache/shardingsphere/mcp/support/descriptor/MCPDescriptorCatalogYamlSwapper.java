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
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceParameterDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPDescriptorCatalog;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPPromptDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPResourceAnnotations;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPResourceNavigationDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPResourceParameterDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPToolAnnotations;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPToolValueDefinition;

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
        Collection<MCPToolDescriptor> toolDescriptors = new LinkedList<>();
        Collection<MCPPromptDescriptor> promptDescriptors = new LinkedList<>();
        Collection<MCPCompletionTargetDescriptor> completionTargetDescriptors = new LinkedList<>();
        Collection<MCPResourceNavigationDescriptor> resourceNavigationDescriptors = new LinkedList<>();
        for (YamlMCPDescriptorCatalog each : yamlCatalogs) {
            resourceDescriptors.addAll(swapResourceDescriptors(each.getResources()));
            toolDescriptors.addAll(swapToolDescriptors(each.getTools()));
            promptDescriptors.addAll(swapPromptDescriptors(each.getPrompts()));
            completionTargetDescriptors.addAll(swapCompletionTargetDescriptors(each.getCompletionTargets()));
            resourceNavigationDescriptors.addAll(swapResourceNavigationDescriptors(each.getResourceNavigation()));
        }
        return new MCPDescriptorCatalog(resourceDescriptors, toolDescriptors, promptDescriptors, completionTargetDescriptors, resourceNavigationDescriptors);
    }

    private static Collection<MCPResourceDescriptor> swapResourceDescriptors(final Collection<YamlMCPResourceDescriptor> yamlDescriptors) {
        Collection<MCPResourceDescriptor> result = new LinkedList<>();
        for (YamlMCPResourceDescriptor each : emptyIfNull(yamlDescriptors)) {
            result.add(new MCPResourceDescriptor(each.getUriTemplate(), each.getName(), each.getTitle(), each.getDescription(), each.getMimeType(),
                    swapResourceParameters(each.getParameters()), swapResourceAnnotations(each.getAnnotations()), each.getResourceKind(), each.getObjectScope(), each.getFeature(),
                    List.copyOf(emptyIfNull(each.getRelatedTools())), List.copyOf(emptyIfNull(each.getRelatedResources())), List.copyOf(emptyIfNull(each.getUseBefore())),
                    emptyMapIfNull(each.getMeta())));
        }
        return result;
    }

    private static List<MCPResourceParameterDescriptor> swapResourceParameters(final Collection<YamlMCPResourceParameterDescriptor> yamlParameters) {
        List<MCPResourceParameterDescriptor> result = new LinkedList<>();
        for (YamlMCPResourceParameterDescriptor each : emptyIfNull(yamlParameters)) {
            result.add(new MCPResourceParameterDescriptor(each.getName(), each.getTitle(), each.getDescription(), each.isRequired(), each.getScope()));
        }
        return result;
    }

    private static MCPResourceAnnotations swapResourceAnnotations(final YamlMCPResourceAnnotations yamlAnnotations) {
        return null == yamlAnnotations ? MCPResourceAnnotations.EMPTY
                : new MCPResourceAnnotations(yamlAnnotations.getAudience(), yamlAnnotations.getPriority(), yamlAnnotations.getLastModified());
    }

    private static Collection<MCPToolDescriptor> swapToolDescriptors(final Collection<YamlMCPToolDescriptor> yamlDescriptors) {
        Collection<MCPToolDescriptor> result = new LinkedList<>();
        for (YamlMCPToolDescriptor each : emptyIfNull(yamlDescriptors)) {
            result.add(new MCPToolDescriptor(each.getName(), each.getTitle(), each.getDescription(), swapToolFields(each.getFields()), emptyMapIfNull(each.getOutputSchema()),
                    swapToolAnnotations(each.getAnnotations()), emptyMapIfNull(each.getMeta())));
        }
        return result;
    }

    private static Collection<MCPPromptDescriptor> swapPromptDescriptors(final Collection<YamlMCPPromptDescriptor> yamlDescriptors) {
        Collection<MCPPromptDescriptor> result = new LinkedList<>();
        for (YamlMCPPromptDescriptor each : emptyIfNull(yamlDescriptors)) {
            result.add(new MCPPromptDescriptor(each.getName(), each.getTitle(), each.getDescription(), swapPromptArguments(each.getArguments()), each.getTemplateResource(),
                    emptyMapIfNull(each.getMeta())));
        }
        return result;
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
            result.add(new MCPCompletionTargetDescriptor(each.getReferenceType(), each.getReference(), List.copyOf(emptyIfNull(each.getArguments())), each.getMaxValues(),
                    emptyMapIfNull(each.getMeta())));
        }
        return result;
    }

    private static Collection<MCPResourceNavigationDescriptor> swapResourceNavigationDescriptors(final Collection<YamlMCPResourceNavigationDescriptor> yamlDescriptors) {
        Collection<MCPResourceNavigationDescriptor> result = new LinkedList<>();
        for (YamlMCPResourceNavigationDescriptor each : emptyIfNull(yamlDescriptors)) {
            result.add(new MCPResourceNavigationDescriptor(each.getFrom(), each.getTo(), List.copyOf(emptyIfNull(each.getRequiredArguments())),
                    List.copyOf(emptyIfNull(each.getCarriedArguments())), each.getDescription()));
        }
        return result;
    }

    private static List<MCPToolFieldDefinition> swapToolFields(final Collection<YamlMCPToolFieldDefinition> yamlFields) {
        List<MCPToolFieldDefinition> result = new LinkedList<>();
        for (YamlMCPToolFieldDefinition each : emptyIfNull(yamlFields)) {
            result.add(new MCPToolFieldDefinition(each.getName(), swapValueDefinition(each.getValueDefinition()), each.isRequired()));
        }
        return result;
    }

    private static MCPToolValueDefinition swapValueDefinition(final YamlMCPToolValueDefinition yamlValueDefinition) {
        if (null == yamlValueDefinition) {
            throw new IllegalStateException("MCP tool value definition is required.");
        }
        boolean additionalProperties = null == yamlValueDefinition.getAdditionalProperties() || yamlValueDefinition.getAdditionalProperties();
        return MCPToolValueDefinition.builder()
                .type(yamlValueDefinition.getType())
                .description(yamlValueDefinition.getDescription())
                .itemDefinition(swapNullableValueDefinition(yamlValueDefinition.getItemDefinition()))
                .enumValues(emptyIfNull(yamlValueDefinition.getEnumValues()))
                .objectProperties(swapToolFields(yamlValueDefinition.getObjectProperties()))
                .additionalProperties(additionalProperties)
                .defaultValue(yamlValueDefinition.getDefaultValue())
                .minimumValue(yamlValueDefinition.getMinimumValue())
                .maximumValue(yamlValueDefinition.getMaximumValue())
                .examples(emptyIfNull(yamlValueDefinition.getExamples()))
                .pattern(yamlValueDefinition.getPattern())
                .build();
    }

    private static MCPToolValueDefinition swapNullableValueDefinition(final YamlMCPToolValueDefinition yamlValueDefinition) {
        return null == yamlValueDefinition ? null : swapValueDefinition(yamlValueDefinition);
    }

    private static MCPToolAnnotations swapToolAnnotations(final YamlMCPToolAnnotations yamlAnnotations) {
        return null == yamlAnnotations ? MCPToolAnnotations.EMPTY
                : new MCPToolAnnotations(yamlAnnotations.getTitle(), yamlAnnotations.getReadOnlyHint(), yamlAnnotations.getDestructiveHint(), yamlAnnotations.getIdempotentHint(),
                        yamlAnnotations.getOpenWorldHint(), yamlAnnotations.getReturnDirect());
    }

    private static <T> Collection<T> emptyIfNull(final Collection<T> values) {
        return null == values ? Collections.emptyList() : values;
    }

    private static Map<String, Object> emptyMapIfNull(final Map<String, Object> values) {
        return null == values ? Collections.emptyMap() : values;
    }
}
