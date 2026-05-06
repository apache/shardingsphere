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
import org.apache.shardingsphere.mcp.api.completion.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceNavigationDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceParameterDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPDescriptorCatalog;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPCompletionTargetDescriptor;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MCP descriptor catalog loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPDescriptorCatalogLoader {
    
    private static final String DESCRIPTOR_DIRECTORY = "META-INF/shardingsphere-mcp/descriptors";
    
    private static final Pattern URI_VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");
    
    private static final Collection<String> LEGACY_RECOMMENDATION_FIELDS = List.of("recommended_next_tool", "suggested_next_tool", "suggested_next_tools");
    
    /**
     * Load MCP descriptor catalog from classpath.
     *
     * @return MCP descriptor catalog
     */
    public static MCPDescriptorCatalog load() {
        Collection<MCPResourceDescriptor> resourceDescriptors = new LinkedList<>();
        Collection<MCPToolDescriptor> toolDescriptors = new LinkedList<>();
        Collection<MCPPromptDescriptor> promptDescriptors = new LinkedList<>();
        Collection<MCPCompletionTargetDescriptor> completionTargetDescriptors = new LinkedList<>();
        Collection<MCPResourceNavigationDescriptor> resourceNavigationDescriptors = new LinkedList<>();
        for (YamlMCPDescriptorCatalog each : loadYamlCatalogs()) {
            resourceDescriptors.addAll(swapResourceDescriptors(each.getResources()));
            toolDescriptors.addAll(swapToolDescriptors(each.getTools()));
            promptDescriptors.addAll(swapPromptDescriptors(each.getPrompts()));
            completionTargetDescriptors.addAll(swapCompletionTargetDescriptors(each.getCompletionTargets()));
            resourceNavigationDescriptors.addAll(swapResourceNavigationDescriptors(each.getResourceNavigation()));
        }
        MCPDescriptorCatalog result = new MCPDescriptorCatalog(resourceDescriptors, toolDescriptors, promptDescriptors, completionTargetDescriptors, resourceNavigationDescriptors);
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
            result.add(new MCPToolDescriptor(each.getName(), each.getTitle(), each.getDescription(), swapToolFields(each.getFields()),
                    emptyMapIfNull(each.getOutputSchema()), swapToolAnnotations(each.getAnnotations()), emptyMapIfNull(each.getMeta())));
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
        validatePromptDescriptors(catalog.getPromptDescriptors());
        validateCompletionTargetDescriptors(catalog.getCompletionTargetDescriptors(), catalog.getPromptDescriptors(), catalog.getResourceDescriptors());
        validateResourceNavigationDescriptors(catalog.getResourceNavigationDescriptors(), catalog);
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
            validateToolOutputSchema(each);
            validateDestructiveToolDescriptor(each);
            validateExecuteUpdateDescriptor(each);
            validatePlanningExecutionMode(each);
        }
    }
    
    private static void validateToolFields(final MCPToolDescriptor descriptor) {
        for (MCPToolFieldDefinition each : descriptor.getFields()) {
            checkNotBlank(each.getName(), String.format("Tool field name for `%s`", descriptor.getName()));
            checkDescription(each.getValueDefinition().getDescription(), String.format("Tool field `%s.%s` description", descriptor.getName(), each.getName()));
            validateKnownEnumField(descriptor, each);
            if ("structured_intent_evidence".equals(each.getName()) || "user_overrides".equals(each.getName())) {
                checkState(!each.getValueDefinition().getObjectProperties().isEmpty(),
                        String.format("Tool field `%s.%s` must declare structured object properties.", descriptor.getName(), each.getName()));
            }
        }
    }
    
    private static void validateKnownEnumField(final MCPToolDescriptor descriptor, final MCPToolFieldDefinition field) {
        if (!List.of("execution_mode", "operation_type", "delivery_mode", "object_types").contains(field.getName())) {
            return;
        }
        MCPToolValueDefinition valueDefinition = "object_types".equals(field.getName()) ? field.getValueDefinition().getItemDefinition() : field.getValueDefinition();
        checkState(null != valueDefinition && !valueDefinition.getEnumValues().isEmpty(),
                String.format("Tool field `%s.%s` must declare enum values.", descriptor.getName(), field.getName()));
    }
    
    private static void validateToolOutputSchema(final MCPToolDescriptor descriptor) {
        Map<String, Object> outputSchema = descriptor.getOutputSchema();
        checkState("object".equals(outputSchema.get("type")), String.format("Tool `%s` outputSchema must be an object.", descriptor.getName()));
        Object properties = outputSchema.get("properties");
        checkState(properties instanceof Map && !((Map<?, ?>) properties).isEmpty(), String.format("Tool `%s` outputSchema must declare properties.", descriptor.getName()));
        validateNoLegacyRecommendationFields(descriptor, outputSchema);
        validateRequiredOutputFields(descriptor, (Map<?, ?>) properties);
        validateSearchMetadataOutputItems(descriptor, (Map<?, ?>) properties);
    }
    
    private static void validateNoLegacyRecommendationFields(final MCPToolDescriptor descriptor, final Object value) {
        if (value instanceof Map) {
            validateNoLegacyRecommendationFieldMap(descriptor, (Map<?, ?>) value);
        } else if (value instanceof Collection) {
            for (Object each : (Collection<?>) value) {
                validateNoLegacyRecommendationFields(descriptor, each);
            }
        }
    }
    
    private static void validateNoLegacyRecommendationFieldMap(final MCPToolDescriptor descriptor, final Map<?, ?> value) {
        for (Entry<?, ?> entry : value.entrySet()) {
            String key = String.valueOf(entry.getKey());
            checkState(!LEGACY_RECOMMENDATION_FIELDS.contains(key), String.format("Tool `%s` outputSchema must use next_actions instead of legacy `%s`.", descriptor.getName(), key));
            validateNoLegacyRecommendationFields(descriptor, entry.getValue());
        }
    }
    
    private static void validateRequiredOutputFields(final MCPToolDescriptor descriptor, final Map<?, ?> properties) {
        for (String each : createRequiredOutputFields(descriptor.getName())) {
            checkState(properties.containsKey(each), String.format("Tool `%s` outputSchema must declare `%s`.", descriptor.getName(), each));
            Object property = properties.get(each);
            checkState(property instanceof Map, String.format("Tool `%s` outputSchema property `%s` must be an object.", descriptor.getName(), each));
            Object description = ((Map<?, ?>) property).get("description");
            checkDescription(null == description ? "" : description.toString(), String.format("Tool output field `%s.%s` description", descriptor.getName(), each));
        }
    }
    
    private static Collection<String> createRequiredOutputFields(final String toolName) {
        if ("search_metadata".equals(toolName)) {
            return List.of("items", "count", "next_page_token", "has_more", "search_context");
        }
        if ("execute_query".equals(toolName)) {
            return List.of("result_kind", "statement_class", "statement_type", "status", "returned_row_count",
                    "applied_max_rows", "applied_timeout_ms", "truncated", "next_actions");
        }
        if ("execute_update".equals(toolName)) {
            return List.of("response_mode", "result_kind", "statement_class", "statement_type", "status", "returned_row_count",
                    "applied_max_rows", "applied_timeout_ms", "suggested_arguments", "next_actions");
        }
        if ("apply_workflow".equals(toolName)) {
            return List.of("plan_id", "status", "execution_mode", "next_actions", "requires_user_approval");
        }
        if ("validate_workflow".equals(toolName)) {
            return List.of("plan_id", "status", "overall_status", "issues", "next_actions");
        }
        if ("plan_encrypt_rule".equals(toolName) || "plan_mask_rule".equals(toolName)) {
            return List.of("plan_id", "workflow_kind", "status", "missing_required_inputs", "resources_to_read", "next_actions");
        }
        return List.of();
    }
    
    private static void validateSearchMetadataOutputItems(final MCPToolDescriptor descriptor, final Map<?, ?> properties) {
        if (!"search_metadata".equals(descriptor.getName())) {
            return;
        }
        final Object items = properties.get("items");
        checkState(items instanceof Map, "Tool `search_metadata` outputSchema property `items` must be an object.");
        final Object itemSchema = ((Map<?, ?>) items).get("items");
        checkState(itemSchema instanceof Map, "Tool `search_metadata` outputSchema property `items.items` must be an object.");
        final Object itemProperties = ((Map<?, ?>) itemSchema).get("properties");
        checkState(itemProperties instanceof Map && !((Map<?, ?>) itemProperties).isEmpty(), "Tool `search_metadata` outputSchema property `items.items.properties` must declare properties.");
        validateSearchMetadataItemFields((Map<?, ?>) itemProperties);
    }
    
    private static void validateSearchMetadataItemFields(final Map<?, ?> properties) {
        for (String each : List.of("database", "schema", "objectType", "table", "view", "name", "resource_uri", "parent_resource_uri", "next_resource_uris", "derivation_status",
                "match_kind", "matched_fields", "matched_value")) {
            checkState(properties.containsKey(each), String.format("Tool `search_metadata` outputSchema item must declare `%s`.", each));
            final Object property = properties.get(each);
            checkState(property instanceof Map, String.format("Tool `search_metadata` outputSchema item property `%s` must be an object.", each));
            final Object description = ((Map<?, ?>) property).get("description");
            checkDescription(null == description ? "" : description.toString(), String.format("Tool output item field `search_metadata.%s` description", each));
        }
    }
    
    private static void validatePromptDescriptors(final Collection<MCPPromptDescriptor> descriptors) {
        Map<String, MCPPromptDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        for (MCPPromptDescriptor each : descriptors) {
            checkNotBlank(each.getName(), "Prompt name");
            checkNotBlank(each.getTitle(), String.format("Prompt title for `%s`", each.getName()));
            checkDescription(each.getDescription(), String.format("Prompt description for `%s`", each.getName()));
            checkNotBlank(each.getTemplateResource(), String.format("Prompt template resource for `%s`", each.getName()));
            checkState(null == registered.putIfAbsent(each.getName(), each), String.format("Duplicate MCP prompt descriptor `%s`.", each.getName()));
            validatePromptArguments(each);
            validatePromptTemplate(each);
            validatePromptGuidanceMeta(each);
        }
    }
    
    private static void validatePromptArguments(final MCPPromptDescriptor descriptor) {
        Map<String, MCPPromptArgumentDescriptor> registered = new LinkedHashMap<>(descriptor.getArguments().size(), 1F);
        for (MCPPromptArgumentDescriptor each : descriptor.getArguments()) {
            checkNotBlank(each.getName(), String.format("Prompt argument name for `%s`", descriptor.getName()));
            checkNotBlank(each.getTitle(), String.format("Prompt argument title for `%s.%s`", descriptor.getName(), each.getName()));
            checkDescription(each.getDescription(), String.format("Prompt argument `%s.%s` description", descriptor.getName(), each.getName()));
            checkState(null == registered.putIfAbsent(each.getName(), each), String.format("Duplicate MCP prompt argument `%s.%s`.", descriptor.getName(), each.getName()));
        }
    }
    
    private static void validatePromptTemplate(final MCPPromptDescriptor descriptor) {
        Set<String> declaredArguments = new HashSet<>(descriptor.getArguments().stream().map(MCPPromptArgumentDescriptor::getName).toList());
        for (String each : MCPPromptTemplateLoader.extractPlaceholders(MCPPromptTemplateLoader.load(descriptor.getTemplateResource()))) {
            checkState(declaredArguments.contains(each), String.format("Prompt template `%s` has undeclared placeholder `%s`.", descriptor.getTemplateResource(), each));
        }
    }
    
    private static void validateCompletionTargetDescriptors(final Collection<MCPCompletionTargetDescriptor> descriptors, final Collection<MCPPromptDescriptor> prompts,
                                                            final Collection<MCPResourceDescriptor> resources) {
        Set<String> promptNames = prompts.stream().map(MCPPromptDescriptor::getName).collect(Collectors.toSet());
        Set<String> resourceUris = resources.stream().map(MCPResourceDescriptor::getUriPattern).collect(Collectors.toSet());
        Map<String, MCPCompletionTargetDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        for (MCPCompletionTargetDescriptor each : descriptors) {
            checkNotBlank(each.getReferenceType(), "Completion reference type");
            checkNotBlank(each.getReference(), String.format("Completion reference for `%s`", each.getReferenceType()));
            checkState(!each.getArguments().isEmpty(), String.format("Completion target `%s:%s` must declare arguments.", each.getReferenceType(), each.getReference()));
            checkState(0 <= each.getMaxValues(), String.format("Completion target `%s:%s` maxValues must not be negative.", each.getReferenceType(), each.getReference()));
            validateCompletionArguments(each);
            validateCompletionReference(each, promptNames, resourceUris);
            checkState(null == registered.putIfAbsent(each.getReferenceType() + ":" + each.getReference(), each),
                    String.format("Duplicate MCP completion target `%s:%s`.", each.getReferenceType(), each.getReference()));
        }
    }
    
    private static void validateCompletionArguments(final MCPCompletionTargetDescriptor descriptor) {
        Set<String> registered = new HashSet<>();
        for (String each : descriptor.getArguments()) {
            checkNotBlank(each, String.format("Completion argument for `%s:%s`", descriptor.getReferenceType(), descriptor.getReference()));
            checkState(registered.add(each), String.format("Duplicate MCP completion argument `%s` for `%s:%s`.", each, descriptor.getReferenceType(), descriptor.getReference()));
        }
    }
    
    private static void validateCompletionReference(final MCPCompletionTargetDescriptor descriptor, final Set<String> promptNames, final Set<String> resourceUris) {
        if ("prompt".equals(descriptor.getReferenceType())) {
            checkState(promptNames.contains(descriptor.getReference()), String.format("Completion target references unknown prompt `%s`.", descriptor.getReference()));
            return;
        }
        if ("resource".equals(descriptor.getReferenceType())) {
            checkState(resourceUris.contains(descriptor.getReference()), String.format("Completion target references unknown resource `%s`.", descriptor.getReference()));
            return;
        }
        throw new IllegalStateException(String.format("Unsupported completion reference type `%s`.", descriptor.getReferenceType()));
    }
    
    private static void validateResourceNavigationDescriptors(final Collection<MCPResourceNavigationDescriptor> descriptors, final MCPDescriptorCatalog catalog) {
        Set<String> publicIdentifiers = createPublicIdentifiers(catalog);
        Set<String> registered = new HashSet<>();
        for (MCPResourceNavigationDescriptor each : descriptors) {
            checkNotBlank(each.getFrom(), "Resource navigation from");
            checkNotBlank(each.getTo(), String.format("Resource navigation target from `%s`", each.getFrom()));
            checkDescription(each.getDescription(), String.format("Resource navigation `%s` to `%s` description", each.getFrom(), each.getTo()));
            checkState(publicIdentifiers.contains(each.getFrom()), String.format("Resource navigation references unknown source `%s`.", each.getFrom()));
            checkState(publicIdentifiers.contains(each.getTo()), String.format("Resource navigation references unknown target `%s`.", each.getTo()));
            validateNavigationArguments(each);
            checkState(registered.add(each.getFrom() + "->" + each.getTo()),
                    String.format("Duplicate MCP resource navigation `%s` to `%s`.", each.getFrom(), each.getTo()));
        }
    }
    
    private static Set<String> createPublicIdentifiers(final MCPDescriptorCatalog catalog) {
        Set<String> result = new HashSet<>();
        catalog.getResourceDescriptors().stream().map(MCPResourceDescriptor::getUriPattern).forEach(result::add);
        catalog.getToolDescriptors().stream().map(MCPToolDescriptor::getName).forEach(result::add);
        catalog.getPromptDescriptors().stream().map(MCPPromptDescriptor::getName).forEach(result::add);
        return result;
    }
    
    private static void validateNavigationArguments(final MCPResourceNavigationDescriptor descriptor) {
        Set<String> registered = new HashSet<>();
        for (String each : descriptor.getRequiredArguments()) {
            checkNotBlank(each, String.format("Required argument for resource navigation `%s` to `%s`", descriptor.getFrom(), descriptor.getTo()));
            checkState(registered.add(each), String.format("Duplicate required argument `%s` for resource navigation `%s` to `%s`.", each, descriptor.getFrom(), descriptor.getTo()));
        }
        registered.clear();
        for (String each : descriptor.getCarriedArguments()) {
            checkNotBlank(each, String.format("Carried argument for resource navigation `%s` to `%s`", descriptor.getFrom(), descriptor.getTo()));
            checkState(registered.add(each), String.format("Duplicate carried argument `%s` for resource navigation `%s` to `%s`.", each, descriptor.getFrom(), descriptor.getTo()));
        }
    }
    
    private static void validateDestructiveToolDescriptor(final MCPToolDescriptor descriptor) {
        if (!Boolean.TRUE.equals(descriptor.getAnnotations().getDestructiveHint())) {
            return;
        }
        MCPToolFieldDefinition executionMode = findToolField(descriptor, "execution_mode").orElseThrow(
                () -> new IllegalStateException(String.format("Destructive tool `%s` must declare execution_mode.", descriptor.getName())));
        checkState(executionMode.isRequired(), String.format("Destructive tool `%s` execution_mode must be required.", descriptor.getName()));
        Collection<String> executionModes = executionMode.getValueDefinition().getEnumValues();
        checkState(executionModes.contains("preview"), String.format("Destructive tool `%s` execution_mode must allow preview.", descriptor.getName()));
        checkState(!executionModes.contains("auto-execute"), String.format("Destructive tool `%s` execution_mode must not expose auto-execute.", descriptor.getName()));
        checkState(Boolean.TRUE.equals(descriptor.getMeta().get("requiresUserApproval")),
                String.format("Destructive tool `%s` must declare requiresUserApproval=true in meta.", descriptor.getName()));
        checkState(descriptor.getMeta().get("sideEffectScope") instanceof Collection && !((Collection<?>) descriptor.getMeta().get("sideEffectScope")).isEmpty(),
                String.format("Destructive tool `%s` must declare sideEffectScope in meta.", descriptor.getName()));
    }
    
    private static void validateExecuteUpdateDescriptor(final MCPToolDescriptor descriptor) {
        if (!"execute_update".equals(descriptor.getName())) {
            return;
        }
        MCPToolFieldDefinition executionMode = findToolField(descriptor, "execution_mode").orElseThrow(
                () -> new IllegalStateException("Tool `execute_update` must declare execution_mode."));
        checkState(executionMode.isRequired(), "Tool `execute_update` execution_mode must be required.");
        checkState(executionMode.getValueDefinition().getEnumValues().containsAll(List.of("execute", "preview")),
                "Tool `execute_update` execution_mode must allow execute and preview.");
    }
    
    private static void validatePlanningExecutionMode(final MCPToolDescriptor descriptor) {
        if ("apply_workflow".equals(descriptor.getName()) || "execute_update".equals(descriptor.getName())) {
            return;
        }
        Optional<MCPToolFieldDefinition> executionMode = findToolField(descriptor, "execution_mode");
        if (executionMode.isEmpty()) {
            return;
        }
        Collection<String> executionModes = executionMode.get().getValueDefinition().getEnumValues();
        checkState(!executionModes.contains("preview"), String.format("Planning tool `%s` execution_mode must not expose preview.", descriptor.getName()));
        checkState(!executionModes.contains("auto-execute"), String.format("Planning tool `%s` execution_mode must not expose auto-execute.", descriptor.getName()));
    }
    
    private static Optional<MCPToolFieldDefinition> findToolField(final MCPToolDescriptor descriptor, final String fieldName) {
        return descriptor.getFields().stream().filter(each -> fieldName.equals(each.getName())).findFirst();
    }
    
    private static void validatePromptGuidanceMeta(final MCPPromptDescriptor descriptor) {
        checkState(isNonEmptyCollection(descriptor.getMeta().get("stopConditions")),
                String.format("Prompt `%s` must declare stopConditions in meta.", descriptor.getName()));
        checkState(isNonEmptyCollection(descriptor.getMeta().get("askUserConditions")),
                String.format("Prompt `%s` must declare askUserConditions in meta.", descriptor.getName()));
    }
    
    private static boolean isNonEmptyCollection(final Object value) {
        return value instanceof Collection && !((Collection<?>) value).isEmpty();
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
