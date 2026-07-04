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
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validator for MCP tool descriptors and their runtime bindings.
 */
public final class MCPToolDescriptorCatalogValidator {
    
    private static final Collection<String> SUPPORTED_INPUT_SCHEMA_TOP_LEVEL_FIELDS = Set.of("type", "properties", "required", "additionalProperties");
    
    private MCPToolDescriptorCatalogValidator() {
    }
    
    /**
     * Validate tool descriptors in one descriptor catalog.
     *
     * @param catalog descriptor catalog
     */
    public static void validate(final MCPDescriptorCatalog catalog) {
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
            MCPToolOutputSchemaValidator.validate(each);
            validateToolDescriptorExtensions(each, descriptorValidators);
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
        MCPToolOutputSchemaValidator.validateInputSchemaFields(descriptor);
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
    
    private static void validateDestructiveToolDescriptor(final MCPToolDescriptor descriptor, final MCPToolRuntimeDescriptor runtimeDescriptor) {
        if (!descriptor.getAnnotations().isDestructiveHint()) {
            return;
        }
        Map<?, ?> executionMode = MCPToolDescriptorValidationUtils.findToolInputProperty(descriptor, MCPPayloadFieldNames.EXECUTION_MODE).orElseThrow(
                () -> new IllegalStateException(String.format("Destructive tool `%s` must declare execution_mode.", descriptor.getName())));
        ShardingSpherePreconditions.checkState(MCPToolDescriptorValidationUtils.isRequiredToolInput(descriptor, MCPPayloadFieldNames.EXECUTION_MODE),
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
        Optional<Map<?, ?>> executionMode = MCPToolDescriptorValidationUtils.findToolInputProperty(descriptor, MCPPayloadFieldNames.EXECUTION_MODE);
        if (executionMode.isEmpty()) {
            return;
        }
        Collection<?> executionModes = executionMode.get().get("enum") instanceof Collection ? (Collection<?>) executionMode.get().get("enum") : List.of();
        ShardingSpherePreconditions.checkState(!executionModes.contains("preview"),
                () -> new IllegalStateException(String.format("Planning tool `%s` execution_mode must not expose preview.", descriptor.getName())));
        ShardingSpherePreconditions.checkState(!executionModes.contains("auto-execute"),
                () -> new IllegalStateException(String.format("Planning tool `%s` execution_mode must not expose auto-execute.", descriptor.getName())));
    }
    
    private static void validateToolDescriptorExtensions(final MCPToolDescriptor descriptor, final Collection<MCPToolDescriptorValidator> descriptorValidators) {
        for (MCPToolDescriptorValidator each : descriptorValidators) {
            if (each.supports(descriptor)) {
                each.validate(descriptor);
            }
        }
    }
}
