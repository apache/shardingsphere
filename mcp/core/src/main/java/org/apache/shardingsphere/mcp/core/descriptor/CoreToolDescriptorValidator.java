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

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidator;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidationUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Core MCP tool descriptor validator.
 */
public final class CoreToolDescriptorValidator implements MCPToolDescriptorValidator {
    
    private static final String SEARCH_METADATA = "database_gateway_search_metadata";
    
    private static final String EXECUTE_QUERY = "database_gateway_execute_query";
    
    private static final String EXECUTE_UPDATE = "database_gateway_execute_update";
    
    private static final Set<String> SUPPORTED_TOOLS = Set.of(SEARCH_METADATA, EXECUTE_QUERY, EXECUTE_UPDATE);
    
    @Override
    public boolean supports(final MCPToolDescriptor toolDescriptor) {
        return SUPPORTED_TOOLS.contains(toolDescriptor.getName());
    }
    
    @Override
    public void validate(final MCPToolDescriptor toolDescriptor) {
        if (SEARCH_METADATA.equals(toolDescriptor.getName())) {
            MCPToolDescriptorValidationUtils.validateRequiredOutputFields(toolDescriptor,
                    List.of("response_mode", "items", "count", "next_page_token", "has_more", "continuation_mode", "search_context", "total_match_count"));
            validateSearchMetadataOutputItems(toolDescriptor);
            return;
        }
        if (EXECUTE_QUERY.equals(toolDescriptor.getName())) {
            MCPToolDescriptorValidationUtils.validateRequiredOutputFields(toolDescriptor, List.of("response_mode", "result_kind", "statement_class", "statement_type", "status", "returned_row_count",
                    "applied_max_rows", "applied_timeout_ms", "truncated", "next_actions"));
            return;
        }
        MCPToolDescriptorValidationUtils.validateRequiredOutputFields(toolDescriptor, List.of("response_mode", "result_kind", "statement_class", "statement_type", "status", "returned_row_count",
                "applied_max_rows", "applied_timeout_ms", "suggested_arguments", "next_actions"));
        validateExecuteUpdateDescriptor(toolDescriptor);
    }
    
    private void validateSearchMetadataOutputItems(final MCPToolDescriptor descriptor) {
        Map<?, ?> properties = (Map<?, ?>) descriptor.getOutputSchema().get("properties");
        Object items = properties.get("items");
        MCPToolDescriptorValidationUtils.checkState(items instanceof Map, "Tool `database_gateway_search_metadata` outputSchema property `items` must be an object.");
        Object itemSchema = ((Map<?, ?>) items).get("items");
        MCPToolDescriptorValidationUtils.checkState(itemSchema instanceof Map, "Tool `database_gateway_search_metadata` outputSchema property `items.items` must be an object.");
        Object itemProperties = ((Map<?, ?>) itemSchema).get("properties");
        MCPToolDescriptorValidationUtils.checkState(itemProperties instanceof Map && !((Map<?, ?>) itemProperties).isEmpty(),
                "Tool `database_gateway_search_metadata` outputSchema property `items.items.properties` must declare properties.");
        validateSearchMetadataItemFields((Map<?, ?>) itemProperties);
    }
    
    private void validateSearchMetadataItemFields(final Map<?, ?> properties) {
        for (String each : List.of("database", "schema", "objectType", "table", "view", "name", "resource", "parent_resource", "next_resources", "derivation_status",
                "match_kind", "matched_fields", "matched_value")) {
            MCPToolDescriptorValidationUtils.checkState(properties.containsKey(each), String.format("Tool `database_gateway_search_metadata` outputSchema item must declare `%s`.", each));
            Object property = properties.get(each);
            MCPToolDescriptorValidationUtils.checkState(property instanceof Map, String.format("Tool `database_gateway_search_metadata` outputSchema item property `%s` must be an object.", each));
            Object description = ((Map<?, ?>) property).get("description");
            MCPToolDescriptorValidationUtils.checkDescription(null == description ? "" : description.toString(),
                    String.format("Tool output item field `database_gateway_search_metadata.%s` description", each));
        }
    }
    
    private void validateExecuteUpdateDescriptor(final MCPToolDescriptor descriptor) {
        Map<?, ?> executionMode = findToolInputProperty(descriptor, "execution_mode").orElseThrow(
                () -> new IllegalStateException("Tool `database_gateway_execute_update` must declare execution_mode."));
        MCPToolDescriptorValidationUtils.checkState(isRequiredToolInput(descriptor, "execution_mode"), "Tool `database_gateway_execute_update` execution_mode must be required.");
        Object executionModes = executionMode.get("enum");
        MCPToolDescriptorValidationUtils.checkState(executionModes instanceof Collection && ((Collection<?>) executionModes).containsAll(List.of("execute", "preview")),
                "Tool `database_gateway_execute_update` execution_mode must allow execute and preview.");
    }
    
    private Optional<Map<?, ?>> findToolInputProperty(final MCPToolDescriptor descriptor, final String fieldName) {
        Object properties = descriptor.getInputSchema().get("properties");
        if (!(properties instanceof Map)) {
            return Optional.empty();
        }
        Object property = ((Map<?, ?>) properties).get(fieldName);
        return property instanceof Map ? Optional.of((Map<?, ?>) property) : Optional.empty();
    }
    
    private boolean isRequiredToolInput(final MCPToolDescriptor descriptor, final String fieldName) {
        Object required = descriptor.getInputSchema().get("required");
        return required instanceof Collection && ((Collection<?>) required).contains(fieldName);
    }
}
