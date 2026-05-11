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

import org.apache.shardingsphere.mcp.api.resource.MCPUriTemplateUtils;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceParameterDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

final class MCPDescriptorCatalogValidator {
    
    private static final Collection<String> BANNED_PUBLIC_ALIAS_FIELDS = List.of(
            "recommended_next_tool", "suggested_next_tool", "suggested_next_tools", "recommended_recovery", "suggested_next_action");
    
    private static final Collection<String> BANNED_NEXT_ACTION_FIELDS = List.of("action_kind", "target_tool", "target_resource", "required_arguments");
    
    private static final Collection<String> RESERVED_RESOURCE_META_FIELDS = List.of("resourceKind", "kind", "objectScope", "feature", "relatedTools", "relatedResources", "useBefore");
    
    private static final Collection<String> MODEL_CRITICAL_HINT_FIELDS = List.of(
            "next_actions", "resources_to_read", "resource", "parent_resource", "next_resources", "manual_artifact_summary", "manual_follow_up", "empty_state", "ambiguity_state",
            "recovery", "recovery_guidance", "remediation");
    
    private static final Collection<String> CONTINUATION_MODES = List.of("none", "pagination", "search_metadata");
    
    private static final Collection<String> RECOVERY_CATEGORIES = List.of("not_found", "ambiguous", "empty_scope", "missing_context", "validation", "terminal",
            "unsupported_target", "invalid_enum", "unsafe_sql", "stale_workflow", "unavailable_runtime", "terminal_operator_action");
    
    private MCPDescriptorCatalogValidator() {
    }
    
    static void validate(final MCPDescriptorCatalog catalog) {
        validateResourceDescriptors(catalog.getResourceDescriptors());
        validateToolDescriptors(catalog.getToolDescriptors());
        validatePromptDescriptors(catalog.getPromptDescriptors());
        validateCompletionTargetDescriptors(catalog.getCompletionTargetDescriptors(), catalog.getPromptDescriptors(), catalog.getResourceDescriptors());
        validateResourceNavigationDescriptors(catalog.getResourceNavigationDescriptors(), catalog);
    }
    
    private static void validateResourceDescriptors(final Collection<MCPResourceDescriptor> descriptors) {
        Map<String, MCPResourceDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        for (MCPResourceDescriptor each : descriptors) {
            checkNotBlank(each.getUriTemplate(), "Resource URI template");
            checkNotBlank(each.getName(), String.format("Resource name for `%s`", each.getUriTemplate()));
            checkNotBlank(each.getTitle(), String.format("Resource title for `%s`", each.getUriTemplate()));
            checkDescription(each.getDescription(), String.format("Resource description for `%s`", each.getUriTemplate()));
            checkNotBlank(each.getMimeType(), String.format("Resource MIME type for `%s`", each.getUriTemplate()));
            checkState(null == registered.putIfAbsent(each.getUriTemplate(), each), String.format("Duplicate MCP resource descriptor `%s`.", each.getUriTemplate()));
            validateResourceMeta(each);
            validateResourceParameters(each);
        }
    }
    
    private static void validateResourceMeta(final MCPResourceDescriptor descriptor) {
        for (String each : RESERVED_RESOURCE_META_FIELDS) {
            checkState(!descriptor.getMeta().containsKey(each),
                    String.format("Resource `%s` must declare `%s` as a typed YAML field, not inside meta.", descriptor.getUriTemplate(), each));
        }
    }
    
    private static void validateResourceParameters(final MCPResourceDescriptor descriptor) {
        List<String> templateVariables = MCPUriTemplateUtils.extractVariableNames(descriptor.getUriTemplate());
        Set<String> registeredTemplateVariables = new HashSet<>();
        for (String each : templateVariables) {
            checkState(registeredTemplateVariables.add(each), String.format("Duplicate URI template variable `%s` in resource descriptor `%s`.", each, descriptor.getUriTemplate()));
        }
        Set<String> templateVariableSet = new HashSet<>(templateVariables);
        Map<String, MCPResourceParameterDescriptor> declaredParameters = new LinkedHashMap<>(descriptor.getParameters().size(), 1F);
        for (MCPResourceParameterDescriptor each : descriptor.getParameters()) {
            checkNotBlank(each.getName(), String.format("Resource parameter name for `%s`", descriptor.getUriTemplate()));
            checkNotBlank(each.getTitle(), String.format("Resource parameter `%s.%s` title", descriptor.getUriTemplate(), each.getName()));
            checkDescription(each.getDescription(), String.format("Resource parameter `%s.%s` description", descriptor.getUriTemplate(), each.getName()));
            checkNotBlank(each.getScope(), String.format("Resource parameter `%s.%s` scope", descriptor.getUriTemplate(), each.getName()));
            checkState(each.isRequired(), String.format("Resource parameter `%s.%s` must be required because URI template variables are required.", descriptor.getUriTemplate(), each.getName()));
            checkState(templateVariableSet.contains(each.getName()), String.format("Resource descriptor `%s` declares non-template parameter `%s`.", descriptor.getUriTemplate(), each.getName()));
            checkState(null == declaredParameters.putIfAbsent(each.getName(), each), String.format("Duplicate MCP resource parameter `%s.%s`.", descriptor.getUriTemplate(), each.getName()));
        }
        for (String variableName : templateVariables) {
            checkState(declaredParameters.containsKey(variableName),
                    String.format("Resource descriptor `%s` must describe URI template variable `%s`.", descriptor.getUriTemplate(), variableName));
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
            validateToolAnnotations(each);
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
        validateNoBannedPublicAliasFields(descriptor, outputSchema);
        validateOutputExamples(descriptor, outputSchema);
        validateOutputExampleContractValues(descriptor, outputSchema);
        validateRequiredOutputFields(descriptor, (Map<?, ?>) properties);
        validateSearchMetadataOutputItems(descriptor, (Map<?, ?>) properties);
        validateModelCriticalOutputHints(descriptor, (Map<?, ?>) properties);
    }
    
    private static void validateOutputExamples(final MCPToolDescriptor descriptor, final Map<String, Object> outputSchema) {
        checkState(isNonEmptyCollection(outputSchema.get("examples")), String.format("Tool `%s` outputSchema must declare examples.", descriptor.getName()));
    }
    
    private static void validateOutputExampleContractValues(final MCPToolDescriptor descriptor, final Map<String, Object> outputSchema) {
        Object examples = outputSchema.get("examples");
        if (!(examples instanceof Collection)) {
            return;
        }
        for (Object each : (Collection<?>) examples) {
            validateExampleContractValue(descriptor, each);
        }
    }
    
    private static void validateExampleContractValue(final MCPToolDescriptor descriptor, final Object value) {
        if (value instanceof Map) {
            validateExampleContractMap(descriptor, (Map<?, ?>) value);
        } else if (value instanceof Collection) {
            for (Object each : (Collection<?>) value) {
                validateExampleContractValue(descriptor, each);
            }
        }
    }
    
    private static void validateExampleContractMap(final MCPToolDescriptor descriptor, final Map<?, ?> value) {
        Object responseMode = value.get("response_mode");
        if (null != responseMode) {
            checkState(MCPResponseMode.isAllowed(responseMode.toString()), String.format("Tool `%s` output example uses unknown response_mode `%s`.", descriptor.getName(), responseMode));
        }
        Object continuationMode = value.get("continuation_mode");
        if (null != continuationMode) {
            checkState(CONTINUATION_MODES.contains(continuationMode.toString()),
                    String.format("Tool `%s` output example uses unknown continuation_mode `%s`.", descriptor.getName(), continuationMode));
        }
        Object recoveryCategory = value.get("recovery_category");
        if (null != recoveryCategory) {
            checkState(RECOVERY_CATEGORIES.contains(recoveryCategory.toString()),
                    String.format("Tool `%s` output example uses unknown recovery_category `%s`.", descriptor.getName(), recoveryCategory));
        }
        for (Object each : value.values()) {
            validateExampleContractValue(descriptor, each);
        }
    }
    
    private static void validateNoBannedPublicAliasFields(final MCPToolDescriptor descriptor, final Object value) {
        if (value instanceof Map) {
            validateNoBannedPublicAliasFieldMap(descriptor, (Map<?, ?>) value);
        } else if (value instanceof Collection) {
            for (Object each : (Collection<?>) value) {
                validateNoBannedPublicAliasFields(descriptor, each);
            }
        }
    }
    
    private static void validateNoBannedPublicAliasFieldMap(final MCPToolDescriptor descriptor, final Map<?, ?> value) {
        for (Entry<?, ?> entry : value.entrySet()) {
            String key = String.valueOf(entry.getKey());
            checkState(!BANNED_PUBLIC_ALIAS_FIELDS.contains(key), String.format("Tool `%s` outputSchema must use canonical fields instead of banned `%s`.", descriptor.getName(), key));
            validateNoBannedPublicAliasFields(descriptor, entry.getValue());
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
            return List.of("response_mode", "items", "count", "next_page_token", "has_more", "continuation_mode", "search_context", "total_match_count");
        }
        if ("execute_query".equals(toolName)) {
            return List.of("response_mode", "result_kind", "statement_class", "statement_type", "status", "returned_row_count",
                    "applied_max_rows", "applied_timeout_ms", "truncated", "next_actions");
        }
        if ("execute_update".equals(toolName)) {
            return List.of("response_mode", "result_kind", "statement_class", "statement_type", "status", "returned_row_count",
                    "applied_max_rows", "applied_timeout_ms", "suggested_arguments", "next_actions");
        }
        if ("apply_workflow".equals(toolName)) {
            return List.of("response_mode", "plan_id", "status", "execution_mode", "next_actions", "requires_user_approval", "manual_artifact_summary");
        }
        if ("validate_workflow".equals(toolName)) {
            return List.of("response_mode", "plan_id", "status", "overall_status", "issues", "next_actions");
        }
        if ("plan_encrypt_rule".equals(toolName) || "plan_mask_rule".equals(toolName)) {
            return List.of("response_mode", "plan_id", "workflow_kind", "status", "missing_required_inputs", "resources_to_read", "next_actions");
        }
        return List.of();
    }
    
    private static void validateModelCriticalOutputHints(final MCPToolDescriptor descriptor, final Map<?, ?> properties) {
        for (Entry<?, ?> entry : properties.entrySet()) {
            String fieldName = String.valueOf(entry.getKey());
            if (MODEL_CRITICAL_HINT_FIELDS.contains(fieldName)) {
                validateModelCriticalOutputHint(descriptor, fieldName, entry.getValue());
            }
            validateNestedModelCriticalOutputHints(descriptor, entry.getValue());
        }
    }
    
    private static void validateNestedModelCriticalOutputHints(final MCPToolDescriptor descriptor, final Object value) {
        if (!(value instanceof Map)) {
            return;
        }
        Object properties = ((Map<?, ?>) value).get("properties");
        if (properties instanceof Map) {
            validateModelCriticalOutputHints(descriptor, (Map<?, ?>) properties);
        }
        Object items = ((Map<?, ?>) value).get("items");
        if (items instanceof Map) {
            validateNestedModelCriticalOutputHints(descriptor, items);
        }
    }
    
    private static void validateModelCriticalOutputHint(final MCPToolDescriptor descriptor, final String fieldName, final Object property) {
        checkState(property instanceof Map, String.format("Tool `%s` model-critical output field `%s` must be an object.", descriptor.getName(), fieldName));
        Object description = ((Map<?, ?>) property).get("description");
        checkDescription(null == description ? "" : description.toString(), String.format("Tool model-critical output field `%s.%s` description", descriptor.getName(), fieldName));
        if ("next_actions".equals(fieldName)) {
            validateNextActionsSchema(descriptor, (Map<?, ?>) property);
        }
    }
    
    private static void validateNextActionsSchema(final MCPToolDescriptor descriptor, final Map<?, ?> property) {
        checkState("array".equals(property.get("type")), String.format("Tool `%s` next_actions must be an array.", descriptor.getName()));
        Object items = property.get("items");
        checkState(items instanceof Map, String.format("Tool `%s` next_actions items must be an object.", descriptor.getName()));
        Object properties = ((Map<?, ?>) items).get("properties");
        checkState(properties instanceof Map, String.format("Tool `%s` next_actions items must declare properties.", descriptor.getName()));
        for (String each : BANNED_NEXT_ACTION_FIELDS) {
            checkState(!((Map<?, ?>) properties).containsKey(each), String.format("Tool `%s` next_actions item must not declare banned `%s`.", descriptor.getName(), each));
        }
        for (String each : List.of("order", "type", "title", "requires_user_approval")) {
            checkState(((Map<?, ?>) properties).containsKey(each), String.format("Tool `%s` next_actions item must declare `%s`.", descriptor.getName(), each));
            Object field = ((Map<?, ?>) properties).get(each);
            checkState(field instanceof Map, String.format("Tool `%s` next_actions item field `%s` must be an object.", descriptor.getName(), each));
            Object description = ((Map<?, ?>) field).get("description");
            checkDescription(null == description ? "" : description.toString(), String.format("Tool next_actions item field `%s.%s` description", descriptor.getName(), each));
        }
    }
    
    private static void validateSearchMetadataOutputItems(final MCPToolDescriptor descriptor, final Map<?, ?> properties) {
        if (!"search_metadata".equals(descriptor.getName())) {
            return;
        }
        Object items = properties.get("items");
        checkState(items instanceof Map, "Tool `search_metadata` outputSchema property `items` must be an object.");
        Object itemSchema = ((Map<?, ?>) items).get("items");
        checkState(itemSchema instanceof Map, "Tool `search_metadata` outputSchema property `items.items` must be an object.");
        Object itemProperties = ((Map<?, ?>) itemSchema).get("properties");
        checkState(itemProperties instanceof Map && !((Map<?, ?>) itemProperties).isEmpty(), "Tool `search_metadata` outputSchema property `items.items.properties` must declare properties.");
        validateSearchMetadataItemFields((Map<?, ?>) itemProperties);
    }
    
    private static void validateSearchMetadataItemFields(final Map<?, ?> properties) {
        for (String each : List.of("database", "schema", "objectType", "table", "view", "name", "resource", "parent_resource", "next_resources", "derivation_status",
                "match_kind", "matched_fields", "matched_value")) {
            checkState(properties.containsKey(each), String.format("Tool `search_metadata` outputSchema item must declare `%s`.", each));
            Object property = properties.get(each);
            checkState(property instanceof Map, String.format("Tool `search_metadata` outputSchema item property `%s` must be an object.", each));
            Object description = ((Map<?, ?>) property).get("description");
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
        Set<String> resourceUris = resources.stream().map(MCPResourceDescriptor::getUriTemplate).collect(Collectors.toSet());
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
    
    private static void validateToolAnnotations(final MCPToolDescriptor descriptor) {
        checkState(!descriptor.getAnnotations().isEmpty(), String.format("Tool `%s` must declare MCP annotations.", descriptor.getName()));
        checkState(null != descriptor.getAnnotations().getReadOnlyHint(), String.format("Tool `%s` annotations must declare readOnlyHint.", descriptor.getName()));
        checkState(null != descriptor.getAnnotations().getDestructiveHint(), String.format("Tool `%s` annotations must declare destructiveHint.", descriptor.getName()));
        checkState(null != descriptor.getAnnotations().getOpenWorldHint(), String.format("Tool `%s` annotations must declare openWorldHint.", descriptor.getName()));
    }
    
    private static Set<String> createPublicIdentifiers(final MCPDescriptorCatalog catalog) {
        Set<String> result = new HashSet<>();
        catalog.getResourceDescriptors().stream().map(MCPResourceDescriptor::getUriTemplate).forEach(result::add);
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
        MCPToolFieldDefinition approvedByUser = findToolField(descriptor, "approved_by_user").orElseThrow(
                () -> new IllegalStateException(String.format("Destructive tool `%s` must declare approved_by_user.", descriptor.getName())));
        checkState(!approvedByUser.isRequired(), String.format("Destructive tool `%s` approved_by_user must not be required for preview.", descriptor.getName()));
        checkState(MCPToolValueDefinition.Type.BOOLEAN == approvedByUser.getValueDefinition().getType(),
                String.format("Destructive tool `%s` approved_by_user must be boolean.", descriptor.getName()));
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
}
