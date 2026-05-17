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
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.resource.MCPUriTemplateUtils;

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
    
    private static final Collection<String> MODEL_CRITICAL_HINT_FIELDS = List.of(
            "next_actions", "resources_to_read", "resource", "parent_resource", "next_resources", "manual_artifact_summary", "manual_follow_up", "empty_state", "ambiguity_state",
            "recovery", "recovery_guidance", "remediation");
    
    private static final Collection<String> CONTINUATION_MODES = List.of("none", "pagination", "metadata_search");
    
    private static final Collection<String> RECOVERY_CATEGORIES = List.of("not_found", "ambiguous", "empty_scope", "missing_context", "validation", "terminal",
            "unsupported_target", "invalid_enum", "unsafe_sql", "stale_workflow", "unavailable_runtime", "terminal_operator_action");
    
    private MCPDescriptorCatalogValidator() {
    }
    
    static void validate(final MCPDescriptorCatalog catalog) {
        validateResourceDescriptors(catalog);
        validateToolDescriptors(catalog.getToolDescriptors(), catalog.getToolRuntimeDescriptors());
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
            validateResourceVariables(each, findResourceExtension(catalog, each.getUriTemplate()));
        }
    }
    
    private static void validateResourceDescriptor(final MCPResourceDescriptor descriptor, final Map<String, MCPResourceDescriptor> registered) {
        ShardingSpherePreconditions.checkState(null == registered.putIfAbsent(descriptor.getUriTemplate(), descriptor),
                () -> new IllegalStateException(String.format("Duplicate MCP resource descriptor `%s`.", descriptor.getUriTemplate())));
    }
    
    private static MCPResourceExtensionDescriptor findResourceExtension(final MCPDescriptorCatalog catalog, final String uriOrTemplate) {
        return catalog.getResourceExtensionDescriptors().stream().filter(each -> uriOrTemplate.equals(each.getUriOrTemplate())).findFirst().orElse(null);
    }
    
    private static void validateResourceVariables(final MCPResourceDescriptor descriptor, final MCPResourceExtensionDescriptor extension) {
        List<String> templateVariables = MCPUriTemplateUtils.extractVariableNames(descriptor.getUriTemplate());
        Set<String> registeredTemplateVariables = new HashSet<>();
        for (String each : templateVariables) {
            ShardingSpherePreconditions.checkState(registeredTemplateVariables.add(each),
                    () -> new IllegalStateException(String.format("Duplicate URI template variable `%s` in resource descriptor `%s`.", each, descriptor.getUriTemplate())));
        }
        Set<String> templateVariableSet = new HashSet<>(templateVariables);
        Collection<MCPUriVariableDescriptor> uriVariables = null == extension ? List.of() : extension.getUriVariables();
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
    
    private static void validateToolDescriptors(final Collection<MCPToolDescriptor> descriptors, final Collection<MCPToolRuntimeDescriptor> runtimeDescriptors) {
        Collection<MCPToolDescriptorValidator> descriptorValidators = MCPToolDescriptorValidatorLoader.load();
        Map<String, MCPToolDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        Map<String, MCPToolRuntimeDescriptor> runtimes = runtimeDescriptors.stream()
                .collect(Collectors.toMap(MCPToolRuntimeDescriptor::getToolName, each -> each, (a, b) -> b, () -> new LinkedHashMap<>(runtimeDescriptors.size(), 1F)));
        for (MCPToolDescriptor each : descriptors) {
            ShardingSpherePreconditions.checkState(null == registered.putIfAbsent(each.getName(), each),
                    () -> new IllegalStateException(String.format("Duplicate MCP tool descriptor `%s`.", each.getName())));
            validateToolInputSchema(each);
            validateToolOutputSchema(each, descriptorValidators);
            validateDestructiveToolDescriptor(each, runtimes.get(each.getName()));
            validatePlanningExecutionMode(each);
        }
    }
    
    private static void validateToolInputSchema(final MCPToolDescriptor descriptor) {
        Map<String, Object> inputSchema = descriptor.getInputSchema();
        ShardingSpherePreconditions.checkState("object".equals(inputSchema.get("type")),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema must be an object.", descriptor.getName())));
        Object properties = inputSchema.get("properties");
        ShardingSpherePreconditions.checkState(properties instanceof Map, () -> new IllegalStateException(String.format("Tool `%s` inputSchema must declare properties.", descriptor.getName())));
    }
    
    private static void validateToolOutputSchema(final MCPToolDescriptor descriptor, final Collection<MCPToolDescriptorValidator> descriptorValidators) {
        Map<String, Object> outputSchema = descriptor.getOutputSchema();
        ShardingSpherePreconditions.checkState("object".equals(outputSchema.get("type")),
                () -> new IllegalStateException(String.format("Tool `%s` outputSchema must be an object.", descriptor.getName())));
        Object properties = outputSchema.get("properties");
        ShardingSpherePreconditions.checkState(properties instanceof Map && !((Map<?, ?>) properties).isEmpty(),
                () -> new IllegalStateException(String.format("Tool `%s` outputSchema must declare properties.", descriptor.getName())));
        validateNoBannedPublicAliasFields(descriptor, outputSchema);
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
            ShardingSpherePreconditions.checkState(!BANNED_PUBLIC_ALIAS_FIELDS.contains(key),
                    () -> new IllegalStateException(String.format("Tool `%s` outputSchema must use canonical fields instead of banned `%s`.", descriptor.getName(), key)));
            validateNoBannedPublicAliasFields(descriptor, entry.getValue());
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
        if ("next_actions".equals(fieldName)) {
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
        for (String each : BANNED_NEXT_ACTION_FIELDS) {
            ShardingSpherePreconditions.checkState(!((Map<?, ?>) properties).containsKey(each),
                    () -> new IllegalStateException(String.format("Tool `%s` next_actions item must not declare banned `%s`.", descriptor.getName(), each)));
        }
        for (String each : List.of("order", "type", "title", "requires_user_approval")) {
            ShardingSpherePreconditions.checkState(((Map<?, ?>) properties).containsKey(each),
                    () -> new IllegalStateException(String.format("Tool `%s` next_actions item must declare `%s`.", descriptor.getName(), each)));
            Object field = ((Map<?, ?>) properties).get(each);
            ShardingSpherePreconditions.checkState(field instanceof Map,
                    () -> new IllegalStateException(String.format("Tool `%s` next_actions item field `%s` must be an object.", descriptor.getName(), each)));
            Object description = ((Map<?, ?>) field).get("description");
            checkDescription(null == description ? "" : description.toString(), String.format("Tool next_actions item field `%s.%s` description", descriptor.getName(), each));
        }
    }
    
    private static void validatePromptDescriptors(final Collection<MCPPromptDescriptor> descriptors, final Collection<MCPPromptTemplateBinding> templateBindings) {
        Map<String, MCPPromptDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        Map<String, MCPPromptTemplateBinding> bindings = templateBindings.stream()
                .collect(Collectors.toMap(MCPPromptTemplateBinding::getPromptName, each -> each, (a, b) -> b, () -> new LinkedHashMap<>(templateBindings.size(), 1F)));
        for (MCPPromptDescriptor each : descriptors) {
            MCPPromptTemplateBinding binding = bindings.get(each.getName());
            ShardingSpherePreconditions.checkState(null != binding, () -> new IllegalStateException(String.format("Prompt `%s` must declare an internal template binding.", each.getName())));
            ShardingSpherePreconditions.checkState(null == registered.putIfAbsent(each.getName(), each),
                    () -> new IllegalStateException(String.format("Duplicate MCP prompt descriptor `%s`.", each.getName())));
            validatePromptTemplate(each, binding);
        }
    }
    
    private static void validatePromptTemplate(final MCPPromptDescriptor descriptor, final MCPPromptTemplateBinding binding) {
        Set<String> declaredArguments = new HashSet<>(descriptor.getArguments().stream().map(MCPPromptArgumentDescriptor::getName).toList());
        for (String each : MCPPromptTemplateLoader.extractPlaceholders(MCPPromptTemplateLoader.load(binding.getTemplateResource()))) {
            ShardingSpherePreconditions.checkState(declaredArguments.contains(each),
                    () -> new IllegalStateException(String.format("Prompt template `%s` has undeclared placeholder `%s`.", binding.getTemplateResource(), each)));
        }
    }
    
    private static void validateCompletionTargetDescriptors(final Collection<MCPCompletionTargetDescriptor> descriptors, final Collection<MCPPromptDescriptor> prompts,
                                                            final Collection<MCPResourceDescriptor> resources) {
        Map<String, Set<String>> promptArguments = prompts.stream().collect(Collectors.toMap(MCPPromptDescriptor::getName,
                each -> each.getArguments().stream().map(MCPPromptArgumentDescriptor::getName).collect(Collectors.toSet())));
        Set<String> promptNames = promptArguments.keySet();
        Set<String> resourceUris = resources.stream().map(MCPResourceDescriptor::getUriTemplate).collect(Collectors.toSet());
        Map<String, MCPCompletionTargetDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        for (MCPCompletionTargetDescriptor each : descriptors) {
            validateCompletionReference(each, promptNames, resourceUris);
            validatePromptCompletionArguments(each, promptArguments);
            ShardingSpherePreconditions.checkState(null == registered.putIfAbsent(each.getReferenceType() + ":" + each.getReference(), each),
                    () -> new IllegalStateException(String.format("Duplicate MCP completion target `%s:%s`.", each.getReferenceType(), each.getReference())));
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
        Map<?, ?> executionMode = findToolInputProperty(descriptor, "execution_mode").orElseThrow(
                () -> new IllegalStateException(String.format("Destructive tool `%s` must declare execution_mode.", descriptor.getName())));
        ShardingSpherePreconditions.checkState(isRequiredToolInput(descriptor, "execution_mode"),
                () -> new IllegalStateException(String.format("Destructive tool `%s` execution_mode must be required.", descriptor.getName())));
        Collection<?> executionModes = executionMode.get("enum") instanceof Collection ? (Collection<?>) executionMode.get("enum") : List.of();
        ShardingSpherePreconditions.checkState(executionModes.contains("preview"),
                () -> new IllegalStateException(String.format("Destructive tool `%s` execution_mode must allow preview.", descriptor.getName())));
        ShardingSpherePreconditions.checkState(!executionModes.contains("auto-execute"),
                () -> new IllegalStateException(String.format("Destructive tool `%s` execution_mode must not expose auto-execute.", descriptor.getName())));
        Map<?, ?> approvedByUser = findToolInputProperty(descriptor, "approved_by_user").orElseThrow(
                () -> new IllegalStateException(String.format("Destructive tool `%s` must declare approved_by_user.", descriptor.getName())));
        ShardingSpherePreconditions.checkState(!isRequiredToolInput(descriptor, "approved_by_user"),
                () -> new IllegalStateException(String.format("Destructive tool `%s` approved_by_user must not be required for preview.", descriptor.getName())));
        ShardingSpherePreconditions.checkState("boolean".equals(approvedByUser.get("type")),
                () -> new IllegalStateException(String.format("Destructive tool `%s` approved_by_user must be boolean.", descriptor.getName())));
        ShardingSpherePreconditions.checkState(null != runtimeDescriptor && runtimeDescriptor.isRequiresUserApproval(),
                () -> new IllegalStateException(String.format("Destructive tool `%s` must declare requiresUserApproval=true in internal runtime.", descriptor.getName())));
        ShardingSpherePreconditions.checkState(null != runtimeDescriptor && !runtimeDescriptor.getSideEffectScope().isEmpty(),
                () -> new IllegalStateException(String.format("Destructive tool `%s` must declare sideEffectScope in internal runtime.", descriptor.getName())));
    }
    
    private static void validatePlanningExecutionMode(final MCPToolDescriptor descriptor) {
        if (descriptor.getAnnotations().isDestructiveHint()) {
            return;
        }
        Optional<Map<?, ?>> executionMode = findToolInputProperty(descriptor, "execution_mode");
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
