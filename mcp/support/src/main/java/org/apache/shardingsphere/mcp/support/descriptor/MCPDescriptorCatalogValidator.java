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

import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.resource.MCPUriTemplateUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
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
    
    private static final Collection<String> CONTINUATION_MODES = List.of("none", "pagination", "metadata_search");
    
    private static final Collection<String> MCP_ROLES = List.of("user", "assistant");
    
    private static final Collection<String> RECOVERY_CATEGORIES = List.of("not_found", "ambiguous", "empty_scope", "missing_context", "validation", "terminal",
            "unsupported_target", "invalid_enum", "unsafe_sql", "stale_workflow", "unavailable_runtime", "terminal_operator_action");
    
    private static final int MAX_COMPLETION_VALUES = 100;
    
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
            checkState(!each.isTemplated(), String.format("Fixed resource `%s` must not contain template variables.", each.getUriTemplate()));
            validateResourceDescriptor(each, registered);
        }
        for (MCPResourceDescriptor each : catalog.getResourceTemplateDescriptors()) {
            checkNotBlank(each.getUriTemplate(), "Resource template URI template");
            checkState(each.isTemplated(), String.format("Resource template `%s` must contain template variables.", each.getUriTemplate()));
            validateResourceDescriptor(each, registered);
            validateResourceVariables(each, findResourceExtension(catalog, each.getUriTemplate()));
        }
    }
    
    private static void validateResourceDescriptor(final MCPResourceDescriptor descriptor, final Map<String, MCPResourceDescriptor> registered) {
        String uriOrTemplate = descriptor.getUriTemplate();
        checkNotBlank(descriptor.getName(), String.format("Resource name for `%s`", uriOrTemplate));
        checkNotBlank(descriptor.getTitle(), String.format("Resource title for `%s`", uriOrTemplate));
        checkDescription(descriptor.getDescription(), String.format("Resource description for `%s`", uriOrTemplate));
        checkNotBlank(descriptor.getMimeType(), String.format("Resource MIME type for `%s`", uriOrTemplate));
        checkState(null == registered.putIfAbsent(uriOrTemplate, descriptor), String.format("Duplicate MCP resource descriptor `%s`.", uriOrTemplate));
        validateResourceAnnotations(descriptor);
        validateResourceMeta(descriptor);
    }
    
    private static void validateResourceAnnotations(final MCPResourceDescriptor descriptor) {
        MCPResourceAnnotations annotations = descriptor.getAnnotations();
        for (String each : annotations.getAudience()) {
            checkState(MCP_ROLES.contains(each), String.format("Resource `%s` annotations audience `%s` is not an MCP role.", descriptor.getUriTemplate(), each));
        }
        if (null != annotations.getPriority()) {
            checkState(Double.isFinite(annotations.getPriority()), String.format("Resource `%s` annotations priority must be finite.", descriptor.getUriTemplate()));
            checkState(annotations.getPriority() >= 0D && annotations.getPriority() <= 1D,
                    String.format("Resource `%s` annotations priority must be between 0.0 and 1.0.", descriptor.getUriTemplate()));
        }
        if (null != annotations.getLastModified()) {
            checkNotBlank(annotations.getLastModified(), String.format("Resource `%s` annotations lastModified", descriptor.getUriTemplate()));
            checkIsoOffsetDateTime(annotations.getLastModified(), String.format("Resource `%s` annotations lastModified", descriptor.getUriTemplate()));
        }
    }
    
    private static void checkIsoOffsetDateTime(final String value, final String label) {
        try {
            OffsetDateTime.parse(value);
        } catch (final DateTimeParseException ignored) {
            throw new IllegalStateException(String.format("%s must be an ISO 8601 timestamp with an explicit offset or UTC marker.", label));
        }
    }
    
    private static MCPResourceExtensionDescriptor findResourceExtension(final MCPDescriptorCatalog catalog, final String uriOrTemplate) {
        return catalog.getResourceExtensionDescriptors().stream().filter(each -> uriOrTemplate.equals(each.getUriOrTemplate())).findFirst().orElse(null);
    }
    
    private static void validateResourceMeta(final MCPResourceDescriptor descriptor) {
        String uriOrTemplate = descriptor.getUriTemplate();
        for (String each : RESERVED_RESOURCE_META_FIELDS) {
            checkState(!descriptor.getMeta().containsKey(each), String.format("Resource `%s` must not expose un-namespaced metadata `%s`.", uriOrTemplate, each));
        }
        validateMetaKeys(String.format("Resource `%s`", uriOrTemplate), descriptor.getMeta());
    }
    
    private static void validateResourceVariables(final MCPResourceDescriptor descriptor, final MCPResourceExtensionDescriptor extension) {
        List<String> templateVariables = MCPUriTemplateUtils.extractVariableNames(descriptor.getUriTemplate());
        Set<String> registeredTemplateVariables = new HashSet<>();
        for (String each : templateVariables) {
            checkState(registeredTemplateVariables.add(each), String.format("Duplicate URI template variable `%s` in resource descriptor `%s`.", each, descriptor.getUriTemplate()));
        }
        Set<String> templateVariableSet = new HashSet<>(templateVariables);
        Collection<MCPUriVariableDescriptor> uriVariables = null == extension ? List.of() : extension.getUriVariables();
        Map<String, MCPUriVariableDescriptor> declaredParameters = new LinkedHashMap<>(uriVariables.size(), 1F);
        for (MCPUriVariableDescriptor each : uriVariables) {
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
    
    private static void validateToolDescriptors(final Collection<MCPToolDescriptor> descriptors, final Collection<MCPToolRuntimeDescriptor> runtimeDescriptors) {
        Collection<MCPToolDescriptorValidator> descriptorValidators = MCPToolDescriptorValidatorLoader.load();
        Map<String, MCPToolDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        Map<String, MCPToolRuntimeDescriptor> runtimes = runtimeDescriptors.stream()
                .collect(Collectors.toMap(MCPToolRuntimeDescriptor::getToolName, each -> each, (a, b) -> b, () -> new LinkedHashMap<>(runtimeDescriptors.size(), 1F)));
        for (MCPToolDescriptor each : descriptors) {
            checkNotBlank(each.getName(), "Tool name");
            checkNotBlank(each.getTitle(), String.format("Tool title for `%s`", each.getName()));
            checkDescription(each.getDescription(), String.format("Tool description for `%s`", each.getName()));
            checkState(null == registered.putIfAbsent(each.getName(), each), String.format("Duplicate MCP tool descriptor `%s`.", each.getName()));
            validateMetaKeys(String.format("Tool `%s`", each.getName()), each.getMeta());
            validateToolInputSchema(each);
            validateToolOutputSchema(each, descriptorValidators);
            validateToolAnnotations(each);
            validateDestructiveToolDescriptor(each, runtimes.get(each.getName()));
            validatePlanningExecutionMode(each);
        }
    }
    
    private static void validateToolInputSchema(final MCPToolDescriptor descriptor) {
        Map<String, Object> inputSchema = descriptor.getInputSchema();
        checkState("object".equals(inputSchema.get("type")), String.format("Tool `%s` inputSchema must be an object.", descriptor.getName()));
        Object properties = inputSchema.get("properties");
        checkState(properties instanceof Map, String.format("Tool `%s` inputSchema must declare properties.", descriptor.getName()));
    }
    
    private static void validateToolOutputSchema(final MCPToolDescriptor descriptor, final Collection<MCPToolDescriptorValidator> descriptorValidators) {
        Map<String, Object> outputSchema = descriptor.getOutputSchema();
        checkState("object".equals(outputSchema.get("type")), String.format("Tool `%s` outputSchema must be an object.", descriptor.getName()));
        Object properties = outputSchema.get("properties");
        checkState(properties instanceof Map && !((Map<?, ?>) properties).isEmpty(), String.format("Tool `%s` outputSchema must declare properties.", descriptor.getName()));
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
    
    private static void validatePromptDescriptors(final Collection<MCPPromptDescriptor> descriptors, final Collection<MCPPromptTemplateBinding> templateBindings) {
        Map<String, MCPPromptDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        Map<String, MCPPromptTemplateBinding> bindings = templateBindings.stream()
                .collect(Collectors.toMap(MCPPromptTemplateBinding::getPromptName, each -> each, (a, b) -> b, () -> new LinkedHashMap<>(templateBindings.size(), 1F)));
        for (MCPPromptDescriptor each : descriptors) {
            checkNotBlank(each.getName(), "Prompt name");
            checkNotBlank(each.getTitle(), String.format("Prompt title for `%s`", each.getName()));
            checkDescription(each.getDescription(), String.format("Prompt description for `%s`", each.getName()));
            MCPPromptTemplateBinding binding = bindings.get(each.getName());
            checkState(null != binding, String.format("Prompt `%s` must declare an internal template binding.", each.getName()));
            checkNotBlank(binding.getTemplateResource(), String.format("Prompt template resource for `%s`", each.getName()));
            checkState(null == registered.putIfAbsent(each.getName(), each), String.format("Duplicate MCP prompt descriptor `%s`.", each.getName()));
            validatePromptArguments(each);
            validatePromptTemplate(each, binding);
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
    
    private static void validatePromptTemplate(final MCPPromptDescriptor descriptor, final MCPPromptTemplateBinding binding) {
        Set<String> declaredArguments = new HashSet<>(descriptor.getArguments().stream().map(MCPPromptArgumentDescriptor::getName).toList());
        for (String each : MCPPromptTemplateLoader.extractPlaceholders(MCPPromptTemplateLoader.load(binding.getTemplateResource()))) {
            checkState(declaredArguments.contains(each), String.format("Prompt template `%s` has undeclared placeholder `%s`.", binding.getTemplateResource(), each));
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
            checkNotBlank(each.getReferenceType(), "Completion reference type");
            checkNotBlank(each.getReference(), String.format("Completion reference for `%s`", each.getReferenceType()));
            checkState(!each.getArguments().isEmpty(), String.format("Completion target `%s:%s` must declare arguments.", each.getReferenceType(), each.getReference()));
            checkState(0 <= each.getMaxValues(), String.format("Completion target `%s:%s` maxValues must not be negative.", each.getReferenceType(), each.getReference()));
            checkState(each.getMaxValues() <= MAX_COMPLETION_VALUES,
                    String.format("Completion target `%s:%s` maxValues must not exceed %d.", each.getReferenceType(), each.getReference(), MAX_COMPLETION_VALUES));
            validateMetaKeys(String.format("Completion target `%s:%s`", each.getReferenceType(), each.getReference()), each.getMeta());
            validateCompletionArguments(each);
            validateCompletionReference(each, promptNames, resourceUris);
            validatePromptCompletionArguments(each, promptArguments);
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
    
    private static void validatePromptCompletionArguments(final MCPCompletionTargetDescriptor descriptor, final Map<String, Set<String>> promptArguments) {
        if (!"prompt".equals(descriptor.getReferenceType())) {
            return;
        }
        Set<String> argumentNames = promptArguments.getOrDefault(descriptor.getReference(), Set.of());
        for (String each : descriptor.getArguments()) {
            checkState(argumentNames.contains(each),
                    String.format("Completion target `prompt:%s` argument `%s` is not declared by prompt `%s`.", descriptor.getReference(), each, descriptor.getReference()));
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
        MCPToolAnnotations annotations = descriptor.getAnnotations();
        checkState(null != annotations, String.format("Tool `%s` must declare MCP annotations.", descriptor.getName()));
        checkState(!annotations.isReadOnlyHint() || !annotations.isDestructiveHint(), String.format("Tool `%s` annotations cannot be both read-only and destructive.", descriptor.getName()));
    }
    
    private static Set<String> createPublicIdentifiers(final MCPDescriptorCatalog catalog) {
        Set<String> result = new HashSet<>();
        catalog.getAllResourceDescriptors().stream().map(MCPResourceDescriptor::getUriTemplate).forEach(result::add);
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
    
    private static void validateDestructiveToolDescriptor(final MCPToolDescriptor descriptor, final MCPToolRuntimeDescriptor runtimeDescriptor) {
        if (!descriptor.getAnnotations().isDestructiveHint()) {
            return;
        }
        Map<?, ?> executionMode = findToolInputProperty(descriptor, "execution_mode").orElseThrow(
                () -> new IllegalStateException(String.format("Destructive tool `%s` must declare execution_mode.", descriptor.getName())));
        checkState(isRequiredToolInput(descriptor, "execution_mode"), String.format("Destructive tool `%s` execution_mode must be required.", descriptor.getName()));
        Collection<?> executionModes = executionMode.get("enum") instanceof Collection ? (Collection<?>) executionMode.get("enum") : List.of();
        checkState(executionModes.contains("preview"), String.format("Destructive tool `%s` execution_mode must allow preview.", descriptor.getName()));
        checkState(!executionModes.contains("auto-execute"), String.format("Destructive tool `%s` execution_mode must not expose auto-execute.", descriptor.getName()));
        Map<?, ?> approvedByUser = findToolInputProperty(descriptor, "approved_by_user").orElseThrow(
                () -> new IllegalStateException(String.format("Destructive tool `%s` must declare approved_by_user.", descriptor.getName())));
        checkState(!isRequiredToolInput(descriptor, "approved_by_user"), String.format("Destructive tool `%s` approved_by_user must not be required for preview.", descriptor.getName()));
        checkState("boolean".equals(approvedByUser.get("type")),
                String.format("Destructive tool `%s` approved_by_user must be boolean.", descriptor.getName()));
        checkState(null != runtimeDescriptor && runtimeDescriptor.isRequiresUserApproval(),
                String.format("Destructive tool `%s` must declare requiresUserApproval=true in internal runtime.", descriptor.getName()));
        checkState(null != runtimeDescriptor && !runtimeDescriptor.getSideEffectScope().isEmpty(),
                String.format("Destructive tool `%s` must declare sideEffectScope in internal runtime.", descriptor.getName()));
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
        checkState(!executionModes.contains("preview"), String.format("Planning tool `%s` execution_mode must not expose preview.", descriptor.getName()));
        checkState(!executionModes.contains("auto-execute"), String.format("Planning tool `%s` execution_mode must not expose auto-execute.", descriptor.getName()));
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
    
    private static void validatePromptGuidanceMeta(final MCPPromptDescriptor descriptor) {
        validateMetaKeys(String.format("Prompt `%s`", descriptor.getName()), descriptor.getMeta());
        checkState(isNonEmptyCollection(descriptor.getMeta().get(MCPShardingSphereMetadataKeys.STOP_CONDITIONS)),
                String.format("Prompt `%s` must declare %s in meta.", descriptor.getName(), MCPShardingSphereMetadataKeys.STOP_CONDITIONS));
        checkState(isNonEmptyCollection(descriptor.getMeta().get(MCPShardingSphereMetadataKeys.ASK_USER_CONDITIONS)),
                String.format("Prompt `%s` must declare %s in meta.", descriptor.getName(), MCPShardingSphereMetadataKeys.ASK_USER_CONDITIONS));
    }
    
    private static void validateMetaKeys(final String owner, final Map<String, Object> meta) {
        for (String each : meta.keySet()) {
            checkState(each.startsWith(MCPShardingSphereMetadataKeys.PREFIX), String.format("%s meta key `%s` must use the ShardingSphere MCP namespace.", owner, each));
        }
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
