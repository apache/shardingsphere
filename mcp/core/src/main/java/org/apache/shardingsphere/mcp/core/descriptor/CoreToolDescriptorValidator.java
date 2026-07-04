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

package org.apache.shardingsphere.mcp.core.descriptor;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidator;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidationUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Core MCP tool descriptor validator.
 */
public final class CoreToolDescriptorValidator implements MCPToolDescriptorValidator {
    
    private static final String SEARCH_METADATA = "database_gateway_search_metadata";
    
    private static final String EXECUTE_QUERY = "database_gateway_execute_query";
    
    private static final String EXECUTE_UPDATE = "database_gateway_execute_update";
    
    private static final String VALIDATE_RUNTIME_DATABASE = "database_gateway_validate_runtime_database";
    
    private static final Set<String> SUPPORTED_TOOLS = Set.of(SEARCH_METADATA, VALIDATE_RUNTIME_DATABASE, EXECUTE_QUERY, EXECUTE_UPDATE);
    
    @Override
    public boolean supports(final MCPToolDescriptor toolDescriptor) {
        return SUPPORTED_TOOLS.contains(toolDescriptor.getName());
    }
    
    @Override
    public void validate(final MCPToolDescriptor toolDescriptor) {
        if (SEARCH_METADATA.equals(toolDescriptor.getName())) {
            MCPToolDescriptorValidationUtils.validateRequiredOutputFields(toolDescriptor,
                    List.of("response_mode", MCPPayloadFieldNames.ITEMS, "count", "search_context", "total_match_count"));
            validateSearchMetadataOutputItems(toolDescriptor);
            return;
        }
        if (EXECUTE_QUERY.equals(toolDescriptor.getName())) {
            MCPToolDescriptorValidationUtils.validateRequiredOutputFields(toolDescriptor, List.of("response_mode", "result_kind", "statement_class", "statement_type", "status", "returned_row_count",
                    "applied_max_rows", "applied_timeout_ms", "truncated", MCPPayloadFieldNames.NEXT_ACTIONS));
            return;
        }
        if (VALIDATE_RUNTIME_DATABASE.equals(toolDescriptor.getName())) {
            MCPToolDescriptorValidationUtils.validateRequiredOutputFields(toolDescriptor, List.of("response_mode", "status", "database", "checks", "category", MCPPayloadFieldNames.RECOVERY));
            validateRuntimeDatabaseContract(toolDescriptor);
            validateRuntimeDatabaseChecks(toolDescriptor);
            return;
        }
        MCPToolDescriptorValidationUtils.validateRequiredOutputFields(toolDescriptor, List.of("response_mode", "result_kind", "statement_class", "statement_type", "status", "returned_row_count",
                "applied_max_rows", "applied_timeout_ms", "suggested_arguments", MCPPayloadFieldNames.NEXT_ACTIONS));
        validateExecuteUpdateDescriptor(toolDescriptor);
    }
    
    private void validateSearchMetadataOutputItems(final MCPToolDescriptor descriptor) {
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
    
    private void validateSearchMetadataItemFields(final Map<?, ?> properties) {
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
    
    private void validateExecuteUpdateDescriptor(final MCPToolDescriptor descriptor) {
        Map<?, ?> executionMode = MCPToolDescriptorValidationUtils.findToolInputProperty(descriptor, MCPPayloadFieldNames.EXECUTION_MODE).orElseThrow(
                () -> new IllegalStateException("Tool `database_gateway_execute_update` must declare execution_mode."));
        ShardingSpherePreconditions.checkState(MCPToolDescriptorValidationUtils.isRequiredToolInput(descriptor, MCPPayloadFieldNames.EXECUTION_MODE),
                () -> new IllegalStateException("Tool `database_gateway_execute_update` execution_mode must be required."));
        Object executionModes = executionMode.get("enum");
        ShardingSpherePreconditions.checkState(executionModes instanceof Collection && ((Collection<?>) executionModes).containsAll(List.of("execute", "preview")),
                () -> new IllegalStateException("Tool `database_gateway_execute_update` execution_mode must allow execute and preview."));
    }
    
    private void validateRuntimeDatabaseContract(final MCPToolDescriptor descriptor) {
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
    }
    
    private void validateRuntimeDatabaseChecks(final MCPToolDescriptor descriptor) {
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
    
}
