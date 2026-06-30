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

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.resource.MCPUriTemplate;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class MCPDescriptorCatalogValidator {
    
    private static final Collection<String> REMOVED_MODEL_FACING_FIELDS = Set.of(
            "target_tool", "target_resource", "required_arguments", "action_kind", "suggested_next_tool", "suggested_next_tools", "recommended_next_tool",
            "recommended_recovery", "suggested_next_action", "approved_by_user", "requires_user_approval", "approval_required", "user_overrides");
    
    private static final Collection<String> SUPPORTED_INPUT_SCHEMA_TOP_LEVEL_FIELDS = Set.of("type", "properties", "required", "additionalProperties");
    
    private static final Map<String, Collection<String>> NEXT_ACTION_ALLOWED_FIELDS = createNextActionAllowedFields();
    
    private static final Collection<String> NEXT_ACTION_SCHEMA_ALLOWED_FIELDS = createNextActionSchemaAllowedFields();
    
    private static final Collection<String> MODEL_CRITICAL_HINT_FIELDS = List.of(
            MCPPayloadFieldNames.NEXT_ACTIONS, MCPPayloadFieldNames.RESOURCES_TO_READ, MCPPayloadFieldNames.RESOURCE, MCPPayloadFieldNames.PARENT_RESOURCE,
            MCPPayloadFieldNames.NEXT_RESOURCES, "manual_artifact_summary", "manual_follow_up", "empty_state", "ambiguity_state", MCPPayloadFieldNames.RECOVERY, "recovery_guidance",
            "remediation");
    
    private static final Collection<String> CONTINUATION_MODES = List.of("none", "pagination", "metadata_search");
    
    private static final Collection<String> RECOVERY_CATEGORIES = List.of("not_found", "ambiguous", "empty_scope", "missing_context", "validation", "terminal",
            "unsupported_target", "invalid_enum", "unsafe_sql", "stale_workflow", "unavailable_runtime", "terminal_operator_action");
    
    private static final String CLIENT_FORM_ONLY_ARGUMENTS = "org.apache.shardingsphere/client-form-only-arguments";
    
    private static final Pattern SINGLE_BRACE_PLACEHOLDER_PATTERN = Pattern.compile("(?<!\\{)\\{\\s*([a-zA-Z0-9_.-]+)\\s*}(?!})");
    
    private MCPDescriptorCatalogValidator() {
    }
    
    private static Map<String, Collection<String>> createNextActionAllowedFields() {
        return Map.of(
                "resource_read", Set.of("order", "type", "title", "resource_uri", "reason", "depends_on"),
                "tool_call", Set.of("order", "type", "title", "tool_name", "arguments", "reason", "depends_on"),
                "completion", Set.of("order", "type", "title", "ref", "argument", "context", "missing_context_arguments", "resume_ref", "resume_arguments", "reason", "depends_on"),
                "ask_user", Set.of("order", "type", "title", "question", "required_inputs", "reason", "depends_on"),
                "terminal", Set.of("order", "type", "title", "reason", "depends_on"));
    }
    
    private static Collection<String> createNextActionSchemaAllowedFields() {
        Set<String> result = new HashSet<>();
        for (Collection<String> each : NEXT_ACTION_ALLOWED_FIELDS.values()) {
            result.addAll(each);
        }
        return result;
    }
    
    static void validate(final MCPDescriptorCatalog catalog) {
        validateResourceDescriptors(catalog);
        validateToolDescriptors(catalog);
        validatePromptDescriptors(catalog.getPromptDescriptors(), catalog.getPromptTemplateBindings());
        validateCompletionTargetDescriptors(catalog.getCompletionTargetDescriptors(), catalog.getPromptDescriptors(), catalog.getAllResourceDescriptors());
        validateResourceNavigationDescriptors(catalog.getResourceNavigationDescriptors(), catalog);
    }
    
    private static void validateResourceDescriptors(final MCPDescriptorCatalog catalog) {
        Map<String, MCPResourceDescriptor> registered = new LinkedHashMap<>(catalog.getResourceDescriptors().size() + catalog.getResourceTemplateDescriptors().size(), 1F);
        for (MCPResourceDescriptor each : catalog.getResourceDescriptors()) {
            checkNotBlank(each.getUriTemplate(), "Resource URI");
            ShardingSpherePreconditions.checkState(!each.isTemplated(),
                    () -> new IllegalStateException(String.format("Fixed resource `%s` must not contain template variables.", each.getUriTemplate())));
            validateResourceDescriptor(each, registered);
        }
        for (MCPResourceDescriptor each : catalog.getResourceTemplateDescriptors()) {
            checkNotBlank(each.getUriTemplate(), "Resource template URI template");
            ShardingSpherePreconditions.checkState(each.isTemplated(),
                    () -> new IllegalStateException(String.format("Resource template `%s` must contain template variables.", each.getUriTemplate())));
            validateResourceDescriptor(each, registered);
            validateResourceVariables(each, findShardingSphereResourceMetadata(catalog, each.getUriTemplate()).map(ShardingSphereMCPResourceMetadata::getUriVariables).orElse(List.of()));
        }
    }
    
    private static void validateResourceDescriptor(final MCPResourceDescriptor descriptor, final Map<String, MCPResourceDescriptor> registered) {
        ShardingSpherePreconditions.checkState(null == registered.putIfAbsent(descriptor.getUriTemplate(), descriptor),
                () -> new IllegalStateException(String.format("Duplicate MCP resource descriptor `%s`.", descriptor.getUriTemplate())));
    }
    
    private static Optional<ShardingSphereMCPResourceMetadata> findShardingSphereResourceMetadata(final MCPDescriptorCatalog catalog, final String uriOrTemplate) {
        return catalog.getShardingSphereResourceMetadata().stream().filter(each -> uriOrTemplate.equals(each.getUriOrTemplate())).findFirst();
    }
    
    private static void validateResourceVariables(final MCPResourceDescriptor descriptor, final Collection<MCPUriVariableDescriptor> uriVariables) {
        List<String> templateVariables = new MCPUriTemplate(descriptor.getUriTemplate()).getVariableNames();
        Set<String> registeredTemplateVariables = new HashSet<>();
        for (String each : templateVariables) {
            ShardingSpherePreconditions.checkState(registeredTemplateVariables.add(each),
                    () -> new IllegalStateException(String.format("Duplicate URI template variable `%s` in resource descriptor `%s`.", each, descriptor.getUriTemplate())));
        }
        Set<String> templateVariableSet = new HashSet<>(templateVariables);
        Map<String, MCPUriVariableDescriptor> declaredParameters = new LinkedHashMap<>(uriVariables.size(), 1F);
        for (MCPUriVariableDescriptor each : uriVariables) {
            ShardingSpherePreconditions.checkState(each.isRequired(), () -> new IllegalStateException(
                    String.format("Resource parameter `%s.%s` must be required because URI template variables are required.", descriptor.getUriTemplate(), each.getName())));
            ShardingSpherePreconditions.checkState(templateVariableSet.contains(each.getName()),
                    () -> new IllegalStateException(String.format("Resource descriptor `%s` declares non-template parameter `%s`.", descriptor.getUriTemplate(), each.getName())));
            ShardingSpherePreconditions.checkState(null == declaredParameters.putIfAbsent(each.getName(), each),
                    () -> new IllegalStateException(String.format("Duplicate MCP resource parameter `%s.%s`.", descriptor.getUriTemplate(), each.getName())));
        }
        for (String variableName : templateVariables) {
            ShardingSpherePreconditions.checkState(declaredParameters.containsKey(variableName),
                    () -> new IllegalStateException(String.format("Resource descriptor `%s` must describe URI template variable `%s`.", descriptor.getUriTemplate(), variableName)));
        }
    }
    
    private static void validateToolDescriptors(final MCPDescriptorCatalog catalog) {
        Collection<MCPToolDescriptor> descriptors = catalog.getToolDescriptors();
        Collection<MCPToolRuntimeDescriptor> runtimeDescriptors = catalog.getToolRuntimeDescriptors();
        Collection<MCPToolDescriptorValidator> descriptorValidators = MCPToolDescriptorValidatorLoader.load();
        Map<String, MCPToolDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        Map<String, MCPToolRuntimeDescriptor> runtimes = runtimeDescriptors.stream()
                .collect(Collectors.toMap(MCPToolRuntimeDescriptor::getToolName, each -> each));
        Set<String> resourceIdentifiers = catalog.getAllResourceDescriptors().stream().map(MCPResourceDescriptor::getUriTemplate).collect(Collectors.toSet());
        Set<String> shardingSphereResourceIdentifiers = catalog.getShardingSphereResourceMetadata().stream()
                .map(ShardingSphereMCPResourceMetadata::getUriOrTemplate).collect(Collectors.toSet());
        for (MCPToolDescriptor each : descriptors) {
            ShardingSpherePreconditions.checkState(null == registered.putIfAbsent(each.getName(), each),
                    () -> new IllegalStateException(String.format("Duplicate MCP tool descriptor `%s`.", each.getName())));
            validateToolInputSchema(each);
            validateToolOutputSchema(each, descriptorValidators);
            validateDestructiveToolDescriptor(each, runtimes.get(each.getName()));
            validatePlanningExecutionMode(each);
            validateRelatedResourceUris(each, resourceIdentifiers, shardingSphereResourceIdentifiers);
        }
        validatePlanningToolRuntimeDescriptors(registered, runtimeDescriptors);
    }
    
    private static void validateToolInputSchema(final MCPToolDescriptor descriptor) {
        Map<String, Object> inputSchema = descriptor.getInputSchema();
        for (String each : inputSchema.keySet()) {
            ShardingSpherePreconditions.checkState(SUPPORTED_INPUT_SCHEMA_TOP_LEVEL_FIELDS.contains(each),
                    () -> new IllegalStateException(String.format("Tool `%s` inputSchema contains unsupported top-level field `%s`.", descriptor.getName(), each)));
        }
        ShardingSpherePreconditions.checkState("object".equals(inputSchema.get("type")),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema must be an object.", descriptor.getName())));
        Object properties = inputSchema.get("properties");
        ShardingSpherePreconditions.checkState(properties instanceof Map, () -> new IllegalStateException(String.format("Tool `%s` inputSchema must declare properties.", descriptor.getName())));
        validateNoRemovedModelFacingFields(descriptor, inputSchema);
    }
    
    private static void validateRelatedResourceUris(final MCPToolDescriptor descriptor, final Set<String> resourceIdentifiers, final Set<String> shardingSphereResourceIdentifiers) {
        Object value = descriptor.getMeta().get(MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS);
        if (null == value) {
            return;
        }
        ShardingSpherePreconditions.checkState(value instanceof Collection,
                () -> new IllegalStateException(String.format("Tool `%s` metadata `%s` must be a list.", descriptor.getName(), MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS)));
        for (Object each : (Collection<?>) value) {
            String uri = String.valueOf(each);
            ShardingSpherePreconditions.checkState(resourceIdentifiers.contains(uri),
                    () -> new IllegalStateException(String.format("Tool `%s` metadata `%s` references unknown resource `%s`.",
                            descriptor.getName(), MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS, uri)));
            ShardingSpherePreconditions.checkState(shardingSphereResourceIdentifiers.contains(uri),
                    () -> new IllegalStateException(String.format("Tool `%s` metadata `%s` references resource `%s` without ShardingSphere metadata.",
                            descriptor.getName(), MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS, uri)));
        }
    }
    
    private static void validatePlanningToolRuntimeDescriptors(final Map<String, MCPToolDescriptor> descriptors, final Collection<MCPToolRuntimeDescriptor> runtimeDescriptors) {
        Map<String, String> workflowKinds = new LinkedHashMap<>(runtimeDescriptors.size(), 1F);
        for (MCPToolRuntimeDescriptor each : runtimeDescriptors) {
            if (!"plan".equals(each.getWorkflowRole())) {
                continue;
            }
            MCPToolDescriptor descriptor = descriptors.get(each.getToolName());
            ShardingSpherePreconditions.checkState(null != descriptor,
                    () -> new IllegalStateException(String.format("Planning runtime tool `%s` must reference a registered tool descriptor.", each.getToolName())));
            Object workflowKind = descriptor.getMeta().get(MCPShardingSphereMetadataKeys.WORKFLOW_KIND);
            ShardingSpherePreconditions.checkState(null != workflowKind && !workflowKind.toString().isBlank(),
                    () -> new IllegalStateException(String.format("Planning tool `%s` metadata must declare `%s`.", each.getToolName(), MCPShardingSphereMetadataKeys.WORKFLOW_KIND)));
            String previousToolName = workflowKinds.putIfAbsent(workflowKind.toString(), each.getToolName());
            ShardingSpherePreconditions.checkState(null == previousToolName, () -> new IllegalStateException(
                    String.format("Planning workflow kind `%s` is used by both `%s` and `%s`.", workflowKind, previousToolName, each.getToolName())));
        }
    }
    
    private static void validateToolOutputSchema(final MCPToolDescriptor descriptor, final Collection<MCPToolDescriptorValidator> descriptorValidators) {
        Map<String, Object> outputSchema = descriptor.getOutputSchema();
        ShardingSpherePreconditions.checkState("object".equals(outputSchema.get("type")),
                () -> new IllegalStateException(String.format("Tool `%s` outputSchema must be an object.", descriptor.getName())));
        Object properties = outputSchema.get("properties");
        ShardingSpherePreconditions.checkState(properties instanceof Map && !((Map<?, ?>) properties).isEmpty(),
                () -> new IllegalStateException(String.format("Tool `%s` outputSchema must declare properties.", descriptor.getName())));
        validateNoRemovedModelFacingFields(descriptor, outputSchema);
        validateOutputExamples(descriptor, outputSchema);
        validateOutputExampleContractValues(descriptor, outputSchema);
        validateModelCriticalOutputHints(descriptor, (Map<?, ?>) properties);
        validateToolDescriptorExtensions(descriptor, descriptorValidators);
    }
    
    private static void validateToolDescriptorExtensions(final MCPToolDescriptor descriptor, final Collection<MCPToolDescriptorValidator> descriptorValidators) {
        for (MCPToolDescriptorValidator each : descriptorValidators) {
            if (each.supports(descriptor)) {
                each.validate(descriptor);
            }
        }
    }
    
    private static void validateOutputExamples(final MCPToolDescriptor descriptor, final Map<String, Object> outputSchema) {
        ShardingSpherePreconditions.checkState(isNonEmptyCollection(outputSchema.get("examples")),
                () -> new IllegalStateException(String.format("Tool `%s` outputSchema must declare examples.", descriptor.getName())));
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
            ShardingSpherePreconditions.checkState(MCPResponseMode.isAllowed(responseMode.toString()),
                    () -> new IllegalStateException(String.format("Tool `%s` output example uses unknown response_mode `%s`.", descriptor.getName(), responseMode)));
        }
        Object continuationMode = value.get("continuation_mode");
        if (null != continuationMode) {
            ShardingSpherePreconditions.checkState(CONTINUATION_MODES.contains(continuationMode.toString()),
                    () -> new IllegalStateException(String.format("Tool `%s` output example uses unknown continuation_mode `%s`.", descriptor.getName(), continuationMode)));
        }
        Object recoveryCategory = value.get("recovery_category");
        if (null != recoveryCategory) {
            ShardingSpherePreconditions.checkState(RECOVERY_CATEGORIES.contains(recoveryCategory.toString()),
                    () -> new IllegalStateException(String.format("Tool `%s` output example uses unknown recovery_category `%s`.", descriptor.getName(), recoveryCategory)));
        }
        for (Object each : value.values()) {
            validateExampleContractValue(descriptor, each);
        }
        Object nextActions = value.get(MCPPayloadFieldNames.NEXT_ACTIONS);
        if (null != nextActions) {
            validateConcreteNextActions(descriptor, nextActions);
        }
    }
    
    private static void validateNoRemovedModelFacingFields(final MCPToolDescriptor descriptor, final Object value) {
        if (value instanceof Map) {
            validateNoRemovedModelFacingFieldMap(descriptor, (Map<?, ?>) value);
        } else if (value instanceof Collection) {
            for (Object each : (Collection<?>) value) {
                validateNoRemovedModelFacingFields(descriptor, each);
            }
        }
    }
    
    private static void validateNoRemovedModelFacingFieldMap(final MCPToolDescriptor descriptor, final Map<?, ?> value) {
        for (Entry<?, ?> entry : value.entrySet()) {
            String key = String.valueOf(entry.getKey());
            validateNoRemovedModelFacingField(descriptor, key);
            if ("required".equals(key)) {
                validateNoRemovedModelFacingRequiredFields(descriptor, entry.getValue());
            }
            validateNoRemovedModelFacingFields(descriptor, entry.getValue());
        }
    }
    
    private static void validateNoRemovedModelFacingRequiredFields(final MCPToolDescriptor descriptor, final Object value) {
        if (!(value instanceof Collection)) {
            return;
        }
        for (Object each : (Collection<?>) value) {
            validateNoRemovedModelFacingField(descriptor, String.valueOf(each));
        }
    }
    
    private static void validateNoRemovedModelFacingField(final MCPToolDescriptor descriptor, final String fieldName) {
        ShardingSpherePreconditions.checkState(!REMOVED_MODEL_FACING_FIELDS.contains(fieldName),
                () -> new IllegalStateException(String.format("Tool `%s` model-facing contract must use canonical fields instead of removed `%s`.", descriptor.getName(), fieldName)));
    }
    
    private static void validateConcreteNextActions(final MCPToolDescriptor descriptor, final Object value) {
        ShardingSpherePreconditions.checkState(value instanceof Collection,
                () -> new IllegalStateException(String.format("Tool `%s` next_actions example must be an array.", descriptor.getName())));
        for (Object each : (Collection<?>) value) {
            ShardingSpherePreconditions.checkState(each instanceof Map,
                    () -> new IllegalStateException(String.format("Tool `%s` next_actions example item must be an object.", descriptor.getName())));
            validateConcreteNextAction(descriptor, (Map<?, ?>) each);
        }
    }
    
    private static void validateConcreteNextAction(final MCPToolDescriptor descriptor, final Map<?, ?> action) {
        String type = String.valueOf(action.get("type"));
        Collection<String> allowedFields = NEXT_ACTION_ALLOWED_FIELDS.get(type);
        ShardingSpherePreconditions.checkState(null != allowedFields,
                () -> new IllegalStateException(String.format("Tool `%s` next_actions example uses unknown type `%s`.", descriptor.getName(), type)));
        for (Object each : action.keySet()) {
            String fieldName = String.valueOf(each);
            ShardingSpherePreconditions.checkState(allowedFields.contains(fieldName),
                    () -> new IllegalStateException(String.format("Tool `%s` next_actions example `%s` contains unsupported field `%s`.", descriptor.getName(), type, fieldName)));
        }
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
        ShardingSpherePreconditions.checkState(property instanceof Map,
                () -> new IllegalStateException(String.format("Tool `%s` model-critical output field `%s` must be an object.", descriptor.getName(), fieldName)));
        Object description = ((Map<?, ?>) property).get("description");
        checkDescription(null == description ? "" : description.toString(), String.format("Tool model-critical output field `%s.%s` description", descriptor.getName(), fieldName));
        if (MCPPayloadFieldNames.NEXT_ACTIONS.equals(fieldName)) {
            validateNextActionsSchema(descriptor, (Map<?, ?>) property);
        }
    }
    
    private static void validateNextActionsSchema(final MCPToolDescriptor descriptor, final Map<?, ?> property) {
        ShardingSpherePreconditions.checkState("array".equals(property.get("type")), () -> new IllegalStateException(String.format("Tool `%s` next_actions must be an array.", descriptor.getName())));
        Object items = property.get("items");
        ShardingSpherePreconditions.checkState(items instanceof Map, () -> new IllegalStateException(String.format("Tool `%s` next_actions items must be an object.", descriptor.getName())));
        Object properties = ((Map<?, ?>) items).get("properties");
        ShardingSpherePreconditions.checkState(properties instanceof Map,
                () -> new IllegalStateException(String.format("Tool `%s` next_actions items must declare properties.", descriptor.getName())));
        for (String each : List.of("order", "type", "title")) {
            ShardingSpherePreconditions.checkState(((Map<?, ?>) properties).containsKey(each),
                    () -> new IllegalStateException(String.format("Tool `%s` next_actions item must declare `%s`.", descriptor.getName(), each)));
            Object field = ((Map<?, ?>) properties).get(each);
            ShardingSpherePreconditions.checkState(field instanceof Map,
                    () -> new IllegalStateException(String.format("Tool `%s` next_actions item field `%s` must be an object.", descriptor.getName(), each)));
            Object description = ((Map<?, ?>) field).get("description");
            checkDescription(null == description ? "" : description.toString(), String.format("Tool next_actions item field `%s.%s` description", descriptor.getName(), each));
        }
        for (Object each : ((Map<?, ?>) properties).keySet()) {
            String fieldName = String.valueOf(each);
            ShardingSpherePreconditions.checkState(NEXT_ACTION_SCHEMA_ALLOWED_FIELDS.contains(fieldName),
                    () -> new IllegalStateException(String.format("Tool `%s` next_actions item contains unsupported field `%s`.", descriptor.getName(), fieldName)));
        }
    }
    
    private static void validatePromptDescriptors(final Collection<MCPPromptDescriptor> descriptors, final Collection<MCPPromptTemplateBinding> templateBindings) {
        Map<String, MCPPromptDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        Map<String, MCPPromptTemplateBinding> bindings = templateBindings.stream()
                .collect(Collectors.toMap(MCPPromptTemplateBinding::getPromptName, each -> each));
        for (MCPPromptDescriptor each : descriptors) {
            MCPPromptTemplateBinding binding = bindings.get(each.getName());
            ShardingSpherePreconditions.checkState(null != binding, () -> new IllegalStateException(String.format("Prompt `%s` must declare an internal template binding.", each.getName())));
            ShardingSpherePreconditions.checkState(null == registered.putIfAbsent(each.getName(), each),
                    () -> new IllegalStateException(String.format("Duplicate MCP prompt descriptor `%s`.", each.getName())));
            validatePromptTemplate(each, binding);
        }
    }
    
    private static void validatePromptTemplate(final MCPPromptDescriptor descriptor, final MCPPromptTemplateBinding binding) {
        String template = MCPPromptTemplateLoader.load(binding.getTemplateResource());
        validateNoUnsupportedModelFacingPlaceholders(binding, template);
        Set<String> declaredArguments = descriptor.getArguments().stream().map(MCPPromptArgumentDescriptor::getName).collect(Collectors.toSet());
        Set<String> renderedArguments = MCPPromptTemplateLoader.extractPlaceholders(template);
        for (String each : renderedArguments) {
            ShardingSpherePreconditions.checkState(declaredArguments.contains(each),
                    () -> new IllegalStateException(String.format("Prompt template `%s` has undeclared placeholder `%s`.", binding.getTemplateResource(), each)));
        }
        validateDeclaredPromptArgumentsRendered(descriptor, binding, declaredArguments, renderedArguments);
    }
    
    private static void validateNoUnsupportedModelFacingPlaceholders(final MCPPromptTemplateBinding binding, final String template) {
        for (String each : template.lines().toList()) {
            validateNoUnsupportedModelFacingPlaceholderInLine(binding, each);
        }
    }
    
    private static void validateNoUnsupportedModelFacingPlaceholderInLine(final MCPPromptTemplateBinding binding, final String line) {
        Matcher matcher = SINGLE_BRACE_PLACEHOLDER_PATTERN.matcher(line);
        while (matcher.find()) {
            if (!isResourceUriTemplateVariable(line, matcher.start())) {
                throw new IllegalStateException(String.format("Prompt template `%s` contains unsupported model-facing placeholder `{%s}`.",
                        binding.getTemplateResource(), matcher.group(1)));
            }
        }
    }
    
    private static boolean isResourceUriTemplateVariable(final String line, final int placeholderStartIndex) {
        int tokenStartIndex = findTokenStartIndex(line, placeholderStartIndex);
        int resourceUriStartIndex = line.lastIndexOf("shardingsphere://", placeholderStartIndex);
        return resourceUriStartIndex >= tokenStartIndex;
    }
    
    private static int findTokenStartIndex(final String line, final int index) {
        for (int i = index - 1; i >= 0; i--) {
            if (Character.isWhitespace(line.charAt(i))) {
                return i + 1;
            }
        }
        return 0;
    }
    
    private static void validateDeclaredPromptArgumentsRendered(final MCPPromptDescriptor descriptor, final MCPPromptTemplateBinding binding,
                                                                final Set<String> declaredArguments, final Set<String> renderedArguments) {
        Set<String> clientFormOnlyArguments = getClientFormOnlyArguments(descriptor);
        for (String each : declaredArguments) {
            ShardingSpherePreconditions.checkState(renderedArguments.contains(each) || clientFormOnlyArguments.contains(each),
                    () -> new IllegalStateException(String.format("Prompt `%s` declares argument `%s` but template `%s` does not render it.",
                            descriptor.getName(), each, binding.getTemplateResource())));
        }
    }
    
    private static Set<String> getClientFormOnlyArguments(final MCPPromptDescriptor descriptor) {
        Object value = descriptor.getMeta().get(CLIENT_FORM_ONLY_ARGUMENTS);
        if (null == value) {
            return Set.of();
        }
        ShardingSpherePreconditions.checkState(value instanceof Collection,
                () -> new IllegalStateException(String.format("Prompt `%s` metadata `%s` must be a list.", descriptor.getName(), CLIENT_FORM_ONLY_ARGUMENTS)));
        return ((Collection<?>) value).stream().map(String::valueOf).collect(Collectors.toSet());
    }
    
    private static void validateCompletionTargetDescriptors(final Collection<MCPCompletionTargetDescriptor> descriptors, final Collection<MCPPromptDescriptor> prompts,
                                                            final Collection<MCPResourceDescriptor> resources) {
        Map<String, Set<String>> promptArguments = prompts.stream().collect(Collectors.toMap(MCPPromptDescriptor::getName,
                each -> each.getArguments().stream().map(MCPPromptArgumentDescriptor::getName).collect(Collectors.toSet())));
        Set<String> promptNames = promptArguments.keySet();
        Map<String, MCPResourceDescriptor> resourceDescriptors = resources.stream().collect(Collectors.toMap(MCPResourceDescriptor::getUriTemplate, each -> each));
        Map<String, MCPCompletionTargetDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        for (MCPCompletionTargetDescriptor each : descriptors) {
            validateCompletionReference(each, promptNames, resourceDescriptors.keySet());
            validatePromptCompletionArguments(each, promptArguments);
            validateResourceCompletionArguments(each, resourceDescriptors);
            validateCompletionRequiredContextArguments(each);
            ShardingSpherePreconditions.checkState(null == registered.putIfAbsent(each.getReferenceType() + ":" + each.getReference(), each),
                    () -> new IllegalStateException(String.format("Duplicate MCP completion target `%s:%s`.", each.getReferenceType(), each.getReference())));
        }
    }
    
    private static void validateCompletionRequiredContextArguments(final MCPCompletionTargetDescriptor descriptor) {
        Object value = descriptor.getMeta().get(MCPShardingSphereMetadataKeys.REQUIRED_CONTEXT_ARGUMENTS);
        if (null == value) {
            return;
        }
        ShardingSpherePreconditions.checkState(value instanceof Map,
                () -> new IllegalStateException(String.format("Completion target `%s:%s` metadata `%s` must be an object.",
                        descriptor.getReferenceType(), descriptor.getReference(), MCPShardingSphereMetadataKeys.REQUIRED_CONTEXT_ARGUMENTS)));
        Set<String> argumentNames = new HashSet<>(descriptor.getArguments());
        for (Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            String argumentName = String.valueOf(entry.getKey());
            ShardingSpherePreconditions.checkState(argumentNames.contains(argumentName),
                    () -> new IllegalStateException(String.format("Completion target `%s:%s` required context argument `%s` is not declared by the target.",
                            descriptor.getReferenceType(), descriptor.getReference(), argumentName)));
            ShardingSpherePreconditions.checkState(entry.getValue() instanceof Collection,
                    () -> new IllegalStateException(String.format("Completion target `%s:%s` required context for `%s` must be a list.",
                            descriptor.getReferenceType(), descriptor.getReference(), argumentName)));
            for (Object each : (Collection<?>) entry.getValue()) {
                ShardingSpherePreconditions.checkState(argumentNames.contains(String.valueOf(each)),
                        () -> new IllegalStateException(String.format("Completion target `%s:%s` context argument `%s` for `%s` is not declared by the target.",
                                descriptor.getReferenceType(), descriptor.getReference(), each, argumentName)));
            }
        }
    }
    
    private static void validatePromptCompletionArguments(final MCPCompletionTargetDescriptor descriptor, final Map<String, Set<String>> promptArguments) {
        if (!"prompt".equals(descriptor.getReferenceType())) {
            return;
        }
        Set<String> argumentNames = promptArguments.getOrDefault(descriptor.getReference(), Set.of());
        for (String each : descriptor.getArguments()) {
            ShardingSpherePreconditions.checkState(argumentNames.contains(each), () -> new IllegalStateException(
                    String.format("Completion target `prompt:%s` argument `%s` is not declared by prompt `%s`.", descriptor.getReference(), each, descriptor.getReference())));
        }
    }
    
    private static void validateResourceCompletionArguments(final MCPCompletionTargetDescriptor descriptor, final Map<String, MCPResourceDescriptor> resources) {
        if (!"resource".equals(descriptor.getReferenceType())) {
            return;
        }
        MCPResourceDescriptor resource = resources.get(descriptor.getReference());
        ShardingSpherePreconditions.checkState(resource.isTemplated(),
                () -> new IllegalStateException(String.format("Completion target `resource:%s` must reference a resource template.", descriptor.getReference())));
        Set<String> templateVariables = new HashSet<>(new MCPUriTemplate(resource.getUriTemplate()).getVariableNames());
        for (String each : descriptor.getArguments()) {
            ShardingSpherePreconditions.checkState(templateVariables.contains(each), () -> new IllegalStateException(
                    String.format("Completion target `resource:%s` argument `%s` is not a URI template variable.", descriptor.getReference(), each)));
        }
    }
    
    private static void validateCompletionReference(final MCPCompletionTargetDescriptor descriptor, final Set<String> promptNames, final Set<String> resourceUris) {
        if ("prompt".equals(descriptor.getReferenceType())) {
            ShardingSpherePreconditions.checkState(promptNames.contains(descriptor.getReference()),
                    () -> new IllegalStateException(String.format("Completion target references unknown prompt `%s`.", descriptor.getReference())));
            return;
        }
        ShardingSpherePreconditions.checkState("resource".equals(descriptor.getReferenceType()),
                () -> new IllegalStateException(String.format("Unsupported completion reference type `%s`.", descriptor.getReferenceType())));
        ShardingSpherePreconditions.checkState(resourceUris.contains(descriptor.getReference()),
                () -> new IllegalStateException(String.format("Completion target references unknown resource `%s`.", descriptor.getReference())));
    }
    
    private static void validateResourceNavigationDescriptors(final Collection<MCPResourceNavigationDescriptor> descriptors, final MCPDescriptorCatalog catalog) {
        Set<String> publicIdentifiers = createPublicIdentifiers(catalog);
        Set<String> registered = new HashSet<>();
        for (MCPResourceNavigationDescriptor each : descriptors) {
            ShardingSpherePreconditions.checkState(publicIdentifiers.contains(each.getFrom()),
                    () -> new IllegalStateException(String.format("Resource navigation references unknown source `%s`.", each.getFrom())));
            ShardingSpherePreconditions.checkState(publicIdentifiers.contains(each.getTo()),
                    () -> new IllegalStateException(String.format("Resource navigation references unknown target `%s`.", each.getTo())));
            ShardingSpherePreconditions.checkState(registered.add(each.getFrom() + "->" + each.getTo()),
                    () -> new IllegalStateException(String.format("Duplicate MCP resource navigation `%s` to `%s`.", each.getFrom(), each.getTo())));
        }
    }
    
    private static Set<String> createPublicIdentifiers(final MCPDescriptorCatalog catalog) {
        Set<String> result = new HashSet<>();
        catalog.getAllResourceDescriptors().stream().map(MCPResourceDescriptor::getUriTemplate).forEach(result::add);
        catalog.getToolDescriptors().stream().map(MCPToolDescriptor::getName).forEach(result::add);
        catalog.getPromptDescriptors().stream().map(MCPPromptDescriptor::getName).forEach(result::add);
        return result;
    }
    
    private static void validateDestructiveToolDescriptor(final MCPToolDescriptor descriptor, final MCPToolRuntimeDescriptor runtimeDescriptor) {
        if (!descriptor.getAnnotations().isDestructiveHint()) {
            return;
        }
        Map<?, ?> executionMode = findToolInputProperty(descriptor, MCPPayloadFieldNames.EXECUTION_MODE).orElseThrow(
                () -> new IllegalStateException(String.format("Destructive tool `%s` must declare execution_mode.", descriptor.getName())));
        ShardingSpherePreconditions.checkState(isRequiredToolInput(descriptor, MCPPayloadFieldNames.EXECUTION_MODE),
                () -> new IllegalStateException(String.format("Destructive tool `%s` execution_mode must be required.", descriptor.getName())));
        Collection<?> executionModes = executionMode.get("enum") instanceof Collection ? (Collection<?>) executionMode.get("enum") : List.of();
        ShardingSpherePreconditions.checkState(executionModes.contains("preview"),
                () -> new IllegalStateException(String.format("Destructive tool `%s` execution_mode must allow preview.", descriptor.getName())));
        ShardingSpherePreconditions.checkState(!executionModes.contains("auto-execute"),
                () -> new IllegalStateException(String.format("Destructive tool `%s` execution_mode must not expose auto-execute.", descriptor.getName())));
        ShardingSpherePreconditions.checkState(null != runtimeDescriptor && !runtimeDescriptor.getSideEffectScope().isEmpty(),
                () -> new IllegalStateException(String.format("Destructive tool `%s` must declare sideEffectScope in internal runtime.", descriptor.getName())));
    }
    
    private static void validatePlanningExecutionMode(final MCPToolDescriptor descriptor) {
        if (descriptor.getAnnotations().isDestructiveHint()) {
            return;
        }
        Optional<Map<?, ?>> executionMode = findToolInputProperty(descriptor, MCPPayloadFieldNames.EXECUTION_MODE);
        if (executionMode.isEmpty()) {
            return;
        }
        Collection<?> executionModes = executionMode.get().get("enum") instanceof Collection ? (Collection<?>) executionMode.get().get("enum") : List.of();
        ShardingSpherePreconditions.checkState(!executionModes.contains("preview"),
                () -> new IllegalStateException(String.format("Planning tool `%s` execution_mode must not expose preview.", descriptor.getName())));
        ShardingSpherePreconditions.checkState(!executionModes.contains("auto-execute"),
                () -> new IllegalStateException(String.format("Planning tool `%s` execution_mode must not expose auto-execute.", descriptor.getName())));
    }
    
    private static Optional<Map<?, ?>> findToolInputProperty(final MCPToolDescriptor descriptor, final String fieldName) {
        Object properties = descriptor.getInputSchema().get("properties");
        if (!(properties instanceof Map)) {
            return Optional.empty();
        }
        Object property = ((Map<?, ?>) properties).get(fieldName);
        return property instanceof Map ? Optional.of((Map<?, ?>) property) : Optional.empty();
    }
    
    private static boolean isRequiredToolInput(final MCPToolDescriptor descriptor, final String fieldName) {
        Object required = descriptor.getInputSchema().get("required");
        return required instanceof Collection && ((Collection<?>) required).contains(fieldName);
    }
    
    private static boolean isNonEmptyCollection(final Object value) {
        return value instanceof Collection && !((Collection<?>) value).isEmpty();
    }
    
    private static void checkDescription(final String value, final String label) {
        checkNotBlank(value, label);
        ShardingSpherePreconditions.checkState(!value.startsWith(createPlaceholderPrefix("resource:")),
                () -> new IllegalStateException(String.format("%s must not be a placeholder description.", label)));
        ShardingSpherePreconditions.checkState(!value.startsWith(createPlaceholderPrefix("resource template:")),
                () -> new IllegalStateException(String.format("%s must not be a placeholder description.", label)));
    }
    
    private static String createPlaceholderPrefix(final String suffix) {
        return "ShardingSphere MCP " + suffix;
    }
    
    private static void checkNotBlank(final String value, final String label) {
        ShardingSpherePreconditions.checkState(null != value && !value.isBlank(), () -> new IllegalStateException(String.format("%s is required.", label)));
    }
}
