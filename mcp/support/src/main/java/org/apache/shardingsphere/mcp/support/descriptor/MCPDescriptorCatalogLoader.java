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
import org.apache.shardingsphere.infra.util.directory.ClasspathResourceDirectoryReader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceParameterDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPDescriptorCatalog;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPResourceAnnotations;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPResourceParameterDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPToolAnnotations;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPToolValueDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * MCP descriptor catalog loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPDescriptorCatalogLoader {
    
    private static final String DESCRIPTOR_DIRECTORY = "META-INF/shardingsphere-mcp/descriptors";
    
    private static final Pattern URI_VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");
    
    /**
     * Load MCP descriptor catalog from classpath.
     *
     * @return MCP descriptor catalog
     */
    public static MCPDescriptorCatalog load() {
        Collection<MCPResourceDescriptor> resourceDescriptors = new LinkedList<>();
        Collection<MCPToolDescriptor> toolDescriptors = new LinkedList<>();
        for (YamlMCPDescriptorCatalog each : loadYamlCatalogs()) {
            resourceDescriptors.addAll(swapResourceDescriptors(each.getResources()));
            toolDescriptors.addAll(swapToolDescriptors(each.getTools()));
        }
        MCPDescriptorCatalog result = new MCPDescriptorCatalog(resourceDescriptors, toolDescriptors);
        validate(result);
        return result;
    }
    
    private static Collection<YamlMCPDescriptorCatalog> loadYamlCatalogs() {
        try (Stream<String> resources = ClasspathResourceDirectoryReader.read(DESCRIPTOR_DIRECTORY)) {
            return resources.filter(each -> each.endsWith(".yaml") || each.endsWith(".yml")).sorted().map(MCPDescriptorCatalogLoader::loadYamlCatalog).toList();
        }
    }
    
    private static YamlMCPDescriptorCatalog loadYamlCatalog(final String resourceName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
            if (null == inputStream) {
                throw new IllegalStateException(String.format("MCP descriptor resource `%s` is not found.", resourceName));
            }
            return YamlEngine.unmarshal(inputStream.readAllBytes(), YamlMCPDescriptorCatalog.class);
        } catch (final IOException ex) {
            throw new IllegalStateException(String.format("Failed to load MCP descriptor resource `%s`.", resourceName), ex);
        }
    }
    
    private static Collection<MCPResourceDescriptor> swapResourceDescriptors(final Collection<YamlMCPResourceDescriptor> yamlDescriptors) {
        Collection<MCPResourceDescriptor> result = new LinkedList<>();
        for (YamlMCPResourceDescriptor each : emptyIfNull(yamlDescriptors)) {
            result.add(new MCPResourceDescriptor(each.getUriPattern(), each.getName(), each.getTitle(), each.getDescription(), each.getMimeType(),
                    swapResourceParameters(each.getParameters()), swapResourceAnnotations(each.getAnnotations()), emptyMapIfNull(each.getMeta())));
        }
        return result;
    }
    
    private static List<MCPResourceParameterDescriptor> swapResourceParameters(final Collection<YamlMCPResourceParameterDescriptor> yamlParameters) {
        Collection<MCPResourceParameterDescriptor> result = new LinkedList<>();
        for (YamlMCPResourceParameterDescriptor each : emptyIfNull(yamlParameters)) {
            result.add(new MCPResourceParameterDescriptor(each.getName(), each.getTitle(), each.getDescription(), each.isRequired(), each.getScope()));
        }
        return List.copyOf(result);
    }
    
    private static MCPResourceAnnotations swapResourceAnnotations(final YamlMCPResourceAnnotations yamlAnnotations) {
        return null == yamlAnnotations ? MCPResourceAnnotations.EMPTY
                : new MCPResourceAnnotations(yamlAnnotations.getAudience(), yamlAnnotations.getPriority(), yamlAnnotations.getLastModified());
    }
    
    private static Collection<MCPToolDescriptor> swapToolDescriptors(final Collection<YamlMCPToolDescriptor> yamlDescriptors) {
        Collection<MCPToolDescriptor> result = new LinkedList<>();
        for (YamlMCPToolDescriptor each : emptyIfNull(yamlDescriptors)) {
            result.add(new MCPToolDescriptor(each.getName(), each.getTitle(), each.getDescription(), swapToolFields(each.getFields()),
                    emptyMapIfNull(each.getOutputSchema()), swapToolAnnotations(each.getAnnotations()), emptyMapIfNull(each.getMeta())));
        }
        return result;
    }
    
    private static List<MCPToolFieldDefinition> swapToolFields(final Collection<YamlMCPToolFieldDefinition> yamlFields) {
        Collection<MCPToolFieldDefinition> result = new LinkedList<>();
        for (YamlMCPToolFieldDefinition each : emptyIfNull(yamlFields)) {
            result.add(new MCPToolFieldDefinition(each.getName(), swapValueDefinition(each.getValueDefinition()), each.isRequired()));
        }
        return List.copyOf(result);
    }
    
    private static MCPToolValueDefinition swapValueDefinition(final YamlMCPToolValueDefinition yamlValueDefinition) {
        if (null == yamlValueDefinition) {
            throw new IllegalStateException("MCP tool value definition is required.");
        }
        boolean additionalProperties = null == yamlValueDefinition.getAdditionalProperties() || yamlValueDefinition.getAdditionalProperties();
        return new MCPToolValueDefinition(yamlValueDefinition.getType(), yamlValueDefinition.getDescription(), swapNullableValueDefinition(yamlValueDefinition.getItemDefinition()),
                emptyIfNull(yamlValueDefinition.getEnumValues()), swapToolFields(yamlValueDefinition.getObjectProperties()), additionalProperties);
    }
    
    private static MCPToolValueDefinition swapNullableValueDefinition(final YamlMCPToolValueDefinition yamlValueDefinition) {
        return null == yamlValueDefinition ? null : swapValueDefinition(yamlValueDefinition);
    }
    
    private static MCPToolAnnotations swapToolAnnotations(final YamlMCPToolAnnotations yamlAnnotations) {
        return null == yamlAnnotations ? MCPToolAnnotations.EMPTY
                : new MCPToolAnnotations(yamlAnnotations.getTitle(), yamlAnnotations.getReadOnlyHint(), yamlAnnotations.getDestructiveHint(), yamlAnnotations.getIdempotentHint(),
                        yamlAnnotations.getOpenWorldHint(), yamlAnnotations.getReturnDirect());
    }
    
    private static void validate(final MCPDescriptorCatalog catalog) {
        validateResourceDescriptors(catalog.getResourceDescriptors());
        validateToolDescriptors(catalog.getToolDescriptors());
    }
    
    private static void validateResourceDescriptors(final Collection<MCPResourceDescriptor> descriptors) {
        Map<String, MCPResourceDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        for (MCPResourceDescriptor each : descriptors) {
            checkNotBlank(each.getUriPattern(), "Resource URI pattern");
            checkNotBlank(each.getName(), String.format("Resource name for `%s`", each.getUriPattern()));
            checkNotBlank(each.getTitle(), String.format("Resource title for `%s`", each.getUriPattern()));
            checkDescription(each.getDescription(), String.format("Resource description for `%s`", each.getUriPattern()));
            checkNotBlank(each.getMimeType(), String.format("Resource MIME type for `%s`", each.getUriPattern()));
            checkState(null == registered.putIfAbsent(each.getUriPattern(), each), String.format("Duplicate MCP resource descriptor `%s`.", each.getUriPattern()));
            validateResourceParameters(each);
        }
    }
    
    private static void validateResourceParameters(final MCPResourceDescriptor descriptor) {
        Collection<String> declaredParameters = descriptor.getParameters().stream().map(MCPResourceParameterDescriptor::getName).toList();
        Matcher matcher = URI_VARIABLE_PATTERN.matcher(descriptor.getUriPattern());
        while (matcher.find()) {
            String variableName = matcher.group(1);
            checkState(declaredParameters.contains(variableName),
                    String.format("Resource descriptor `%s` must describe URI template variable `%s`.", descriptor.getUriPattern(), variableName));
        }
    }
    
    private static void validateToolDescriptors(final Collection<MCPToolDescriptor> descriptors) {
        Map<String, MCPToolDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        for (MCPToolDescriptor each : descriptors) {
            checkNotBlank(each.getName(), "Tool name");
            checkNotBlank(each.getTitle(), String.format("Tool title for `%s`", each.getName()));
            checkDescription(each.getDescription(), String.format("Tool description for `%s`", each.getName()));
            checkState(null == registered.putIfAbsent(each.getName(), each), String.format("Duplicate MCP tool descriptor `%s`.", each.getName()));
            validateToolFields(each);
        }
    }
    
    private static void validateToolFields(final MCPToolDescriptor descriptor) {
        for (MCPToolFieldDefinition each : descriptor.getFields()) {
            checkNotBlank(each.getName(), String.format("Tool field name for `%s`", descriptor.getName()));
            checkDescription(each.getValueDefinition().getDescription(), String.format("Tool field `%s.%s` description", descriptor.getName(), each.getName()));
            if ("structured_intent_evidence".equals(each.getName()) || "user_overrides".equals(each.getName())) {
                checkState(!each.getValueDefinition().getObjectProperties().isEmpty(),
                        String.format("Tool field `%s.%s` must declare structured object properties.", descriptor.getName(), each.getName()));
            }
        }
    }
    
    private static void checkDescription(final String value, final String label) {
        checkNotBlank(value, label);
        checkState(!value.startsWith(createPlaceholderPrefix("resource:")), String.format("%s must not be a placeholder description.", label));
        checkState(!value.startsWith(createPlaceholderPrefix("resource template:")), String.format("%s must not be a placeholder description.", label));
    }
    
    private static String createPlaceholderPrefix(final String suffix) {
        return "ShardingSphere MCP " + suffix;
    }
    
    private static void checkNotBlank(final String value, final String label) {
        checkState(null != value && !value.isBlank(), String.format("%s is required.", label));
    }
    
    private static void checkState(final boolean expression, final String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }
    
    private static <T> Collection<T> emptyIfNull(final Collection<T> values) {
        return null == values ? Collections.emptyList() : values;
    }
    
    private static Map<String, Object> emptyMapIfNull(final Map<String, Object> values) {
        return null == values ? Collections.emptyMap() : values;
    }
}
