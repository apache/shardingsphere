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
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;

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
    
    private static final String SEARCH_METADATA = "database_gateway_search_metadata";
    
    private static final String EXECUTE_QUERY = "database_gateway_execute_query";
    
    private static final String EXECUTE_UPDATE = "database_gateway_execute_update";
    
    private static final String VALIDATE_RUNTIME_DATABASE = "database_gateway_validate_runtime_database";
    
    private static final Collection<String> SECRET_WORKFLOW_OUTPUT_FIELDS = List.of("masked_property_preview", "secret_reference_summary");
    
    private MCPToolDescriptorCatalogValidator() {
    }
    
    /**
     * Validate tool descriptors in one descriptor catalog.
     *
     * @param catalog descriptor catalog
     */
    public static void validate(final MCPDescriptorCatalog catalog) {
        Collection<MCPToolDescriptor> descriptors = catalog.getProtocolDescriptors().getToolDescriptors();
        Collection<MCPToolRuntimeDescriptor> runtimeDescriptors = catalog.getShardingSphereDescriptors().getToolRuntimeDescriptors();
        Map<String, MCPToolDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        Map<String, MCPToolRuntimeDescriptor> runtimes = runtimeDescriptors.stream()
                .collect(Collectors.toMap(MCPToolRuntimeDescriptor::getToolName, each -> each));
        Set<String> resourceIdentifiers = catalog.getProtocolDescriptors().getAllResourceDescriptors().stream().map(MCPResourceDescriptor::getUriTemplate).collect(Collectors.toSet());
        Set<String> shardingSphereResourceIdentifiers = catalog.getShardingSphereDescriptors().getResourceMetadata().stream()
                .map(ShardingSphereMCPResourceMetadata::getUriTemplate).collect(Collectors.toSet());
        for (MCPToolDescriptor each : descriptors) {
            ShardingSpherePreconditions.checkState(null == registered.putIfAbsent(each.getName(), each),
                    () -> new IllegalStateException(String.format("Duplicate MCP tool descriptor `%s`.", each.getName())));
            validateToolInputSchema(each);
            MCPToolOutputSchemaValidator.validate(each);
            validateToolDescriptorContract(each, runtimes.get(each.getName()));
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
    
    private static void validateToolDescriptorContract(final MCPToolDescriptor descriptor, final MCPToolRuntimeDescriptor runtimeDescriptor) {
        if (SEARCH_METADATA.equals(descriptor.getName())) {
            validateSearchMetadataDescriptor(descriptor);
            return;
        }
        if (EXECUTE_QUERY.equals(descriptor.getName())) {
            validateExecuteQueryDescriptor(descriptor);
            return;
        }
        if (EXECUTE_UPDATE.equals(descriptor.getName())) {
            validateExecuteUpdateDescriptor(descriptor);
            return;
        }
        if (VALIDATE_RUNTIME_DATABASE.equals(descriptor.getName())) {
            validateRuntimeDatabaseDescriptor(descriptor);
            return;
        }
        if (WorkflowToolDescriptors.APPLY_TOOL_NAME.equals(descriptor.getName())) {
            validateApplyWorkflowDescriptor(descriptor);
            return;
        }
        if (WorkflowToolDescriptors.VALIDATE_TOOL_NAME.equals(descriptor.getName())) {
            validateValidateWorkflowDescriptor(descriptor);
            return;
        }
        if (null != runtimeDescriptor && "plan".equals(runtimeDescriptor.getWorkflowRole())) {
            validatePlanningWorkflowDescriptor(descriptor);
        }
    }
    
    private static void validateSearchMetadataDescriptor(final MCPToolDescriptor descriptor) {
        MCPToolDescriptorValidationUtils.validateRequiredOutputFields(descriptor,
                List.of("response_mode", MCPPayloadFieldNames.ITEMS, "count", "search_context", "total_match_count"));
        Map<?, ?> properties = (Map<?, ?>) descriptor.getOutputSchema().get("properties");
        Object items = properties.get(MCPPayloadFieldNames.ITEMS);
        ShardingSpherePreconditions.checkState(items instanceof Map,
                () -> new IllegalStateException("Tool `database_gateway_search_metadata` outputSchema property `items` must be an object."));
        Object itemSchema = ((Map<?, ?>) items).get("items");
        ShardingSpherePreconditions.checkState(itemSchema instanceof Map,
                () -> new IllegalStateException("Tool `database_gateway_search_metadata` outputSchema property `items.items` must be an object."));
        Object itemProperties = ((Map<?, ?>) itemSchema).get("properties");
        ShardingSpherePreconditions.checkState(itemProperties instanceof Map && !((Map<?, ?>) itemProperties).isEmpty(),
                () -> new IllegalStateException("Tool `database_gateway_search_metadata` outputSchema property `items.items.properties` must declare properties."));
        validateSearchMetadataItemFields((Map<?, ?>) itemProperties);
    }
    
    private static void validateSearchMetadataItemFields(final Map<?, ?> properties) {
        for (String each : List.of("database", "schema", "objectType", "table", "view", "name", MCPPayloadFieldNames.RESOURCE, MCPPayloadFieldNames.PARENT_RESOURCE,
                MCPPayloadFieldNames.NEXT_RESOURCES, "derivation_status", "match_kind", "matched_fields", "matched_value")) {
            ShardingSpherePreconditions.checkState(properties.containsKey(each),
                    () -> new IllegalStateException(String.format("Tool `database_gateway_search_metadata` outputSchema item must declare `%s`.", each)));
            Object property = properties.get(each);
            ShardingSpherePreconditions.checkState(property instanceof Map,
                    () -> new IllegalStateException(String.format("Tool `database_gateway_search_metadata` outputSchema item property `%s` must be an object.", each)));
            Object description = ((Map<?, ?>) property).get("description");
            MCPToolDescriptorValidationUtils.checkDescription(null == description ? "" : description.toString(),
                    String.format("Tool output item field `database_gateway_search_metadata.%s` description", each));
        }
    }
    
    private static void validateExecuteQueryDescriptor(final MCPToolDescriptor descriptor) {
        MCPToolDescriptorValidationUtils.validateRequiredOutputFields(descriptor, List.of("response_mode", "result_kind", "statement_class", "statement_type", "status", "returned_row_count",
                "applied_max_rows", "applied_timeout_ms", "truncated", MCPPayloadFieldNames.NEXT_ACTIONS));
    }
    
    private static void validateExecuteUpdateDescriptor(final MCPToolDescriptor descriptor) {
        MCPToolDescriptorValidationUtils.validateRequiredOutputFields(descriptor, List.of("response_mode", "result_kind", "statement_class", "statement_type", "status", "returned_row_count",
                "applied_max_rows", "applied_timeout_ms", "suggested_arguments", MCPPayloadFieldNames.NEXT_ACTIONS));
        Map<?, ?> executionMode = MCPToolDescriptorValidationUtils.findToolInputProperty(descriptor, MCPPayloadFieldNames.EXECUTION_MODE).orElseThrow(
                () -> new IllegalStateException("Tool `database_gateway_execute_update` must declare execution_mode."));
        ShardingSpherePreconditions.checkState(MCPToolDescriptorValidationUtils.isRequiredToolInput(descriptor, MCPPayloadFieldNames.EXECUTION_MODE),
                () -> new IllegalStateException("Tool `database_gateway_execute_update` execution_mode must be required."));
        Object executionModes = executionMode.get("enum");
        ShardingSpherePreconditions.checkState(executionModes instanceof Collection && ((Collection<?>) executionModes).containsAll(List.of("execute", "preview")),
                () -> new IllegalStateException("Tool `database_gateway_execute_update` execution_mode must allow execute and preview."));
    }
    
    private static void validateRuntimeDatabaseDescriptor(final MCPToolDescriptor descriptor) {
        MCPToolDescriptorValidationUtils.validateRequiredOutputFields(descriptor, List.of("response_mode", "status", "database", "checks", "category", MCPPayloadFieldNames.RECOVERY));
        Map<?, ?> responseMode = MCPToolDescriptorValidationUtils.findToolOutputProperty(descriptor, "response_mode").orElseThrow(
                () -> new IllegalStateException("Tool `database_gateway_validate_runtime_database` must declare response_mode."));
        Object responseModes = responseMode.get("enum");
        ShardingSpherePreconditions.checkState(responseModes instanceof Collection && ((Collection<?>) responseModes).contains("validation"),
                () -> new IllegalStateException("Tool `database_gateway_validate_runtime_database` response_mode must allow validation."));
        ShardingSpherePreconditions.checkState(MCPToolDescriptorValidationUtils.findToolInputProperty(descriptor, "database").isPresent(),
                () -> new IllegalStateException("Tool `database_gateway_validate_runtime_database` must declare `database`."));
        ShardingSpherePreconditions.checkState(MCPToolDescriptorValidationUtils.isRequiredToolInput(descriptor, "database"),
                () -> new IllegalStateException("Tool `database_gateway_validate_runtime_database` database must be required."));
        for (String each : List.of("databaseType", "jdbcUrl", "username", "password", "driverClassName")) {
            ShardingSpherePreconditions.checkState(MCPToolDescriptorValidationUtils.findToolInputProperty(descriptor, each).isEmpty(),
                    () -> new IllegalStateException(String.format("Tool `database_gateway_validate_runtime_database` must not expose `%s`.", each)));
        }
        validateRuntimeDatabaseChecks(descriptor);
    }
    
    private static void validateRuntimeDatabaseChecks(final MCPToolDescriptor descriptor) {
        Map<?, ?> properties = (Map<?, ?>) descriptor.getOutputSchema().get("properties");
        Object checks = properties.get("checks");
        ShardingSpherePreconditions.checkState(checks instanceof Map,
                () -> new IllegalStateException("Tool `database_gateway_validate_runtime_database` outputSchema property `checks` must be an object."));
        Object checkItemSchema = ((Map<?, ?>) checks).get("items");
        ShardingSpherePreconditions.checkState(checkItemSchema instanceof Map,
                () -> new IllegalStateException("Tool `database_gateway_validate_runtime_database` outputSchema property `checks.items` must be an object."));
        Object checkItemProperties = ((Map<?, ?>) checkItemSchema).get("properties");
        ShardingSpherePreconditions.checkState(checkItemProperties instanceof Map && !((Map<?, ?>) checkItemProperties).isEmpty(),
                () -> new IllegalStateException("Tool `database_gateway_validate_runtime_database` outputSchema property `checks.items.properties` must declare properties."));
        for (String each : List.of("name", "status", "category", "message")) {
            ShardingSpherePreconditions.checkState(((Map<?, ?>) checkItemProperties).containsKey(each),
                    () -> new IllegalStateException(String.format("Tool `database_gateway_validate_runtime_database` outputSchema check item must declare `%s`.", each)));
        }
    }
    
    private static void validateApplyWorkflowDescriptor(final MCPToolDescriptor descriptor) {
        MCPToolDescriptorValidationUtils.validateRequiredOutputFields(descriptor,
                List.of("response_mode", MCPPayloadFieldNames.SUMMARY, WorkflowFieldNames.PLAN_ID, "status", WorkflowFieldNames.EXECUTION_MODE, MCPPayloadFieldNames.NEXT_ACTIONS,
                        "manual_artifact_summary", "category", "message", "secret_reference_summary"));
    }
    
    private static void validateValidateWorkflowDescriptor(final MCPToolDescriptor descriptor) {
        MCPToolDescriptorValidationUtils.validateRequiredOutputFields(descriptor,
                List.of("response_mode", MCPPayloadFieldNames.SUMMARY, WorkflowFieldNames.PLAN_ID, "status", "overall_status", "issues", MCPPayloadFieldNames.NEXT_ACTIONS));
    }
    
    private static void validatePlanningWorkflowDescriptor(final MCPToolDescriptor descriptor) {
        Map<?, ?> properties = (Map<?, ?>) descriptor.getOutputSchema().get("properties");
        if (properties.containsKey("masked_property_preview") || properties.containsKey("secret_reference_summary")) {
            MCPToolDescriptorValidationUtils.validateRequiredWorkflowPlanOutputFields(descriptor, SECRET_WORKFLOW_OUTPUT_FIELDS);
        } else {
            MCPToolDescriptorValidationUtils.validateRequiredWorkflowPlanOutputFields(descriptor);
        }
        MCPToolDescriptorValidationUtils.validateRequiredWorkflowPlanMetaFields(descriptor);
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
    
}
