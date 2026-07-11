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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.protocol.MCPModelFacingPayloadContract;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Validator for model-facing MCP tool output schema contracts.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPToolOutputSchemaValidator {
    
    private static final Collection<String> CONTINUATION_MODES = List.of("none", "pagination", "metadata_search");
    
    private static final Collection<String> RECOVERY_CATEGORIES = List.of("not_found", "ambiguous", "empty_scope", "missing_context", "validation", "terminal",
            "unsupported_target", "invalid_enum", "unsafe_sql", "invalid_explain_sql", "stale_workflow", "unavailable_runtime", "terminal_operator_action");
    
    /**
     * Validate one tool output schema.
     *
     * @param descriptor tool descriptor
     */
    public static void validate(final MCPToolDescriptor descriptor) {
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
    }
    
    /**
     * Validate that model-facing input schema fields do not use removed aliases.
     *
     * @param descriptor tool descriptor
     */
    public static void validateInputSchemaFields(final MCPToolDescriptor descriptor) {
        validateNoRemovedModelFacingFields(descriptor, descriptor.getInputSchema());
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
        ShardingSpherePreconditions.checkState(!MCPModelFacingPayloadContract.isRemovedFieldName(fieldName),
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
        Collection<String> allowedFields = MCPModelFacingPayloadContract.getNextActionAllowedFields(type);
        ShardingSpherePreconditions.checkState(!allowedFields.isEmpty(),
                () -> new IllegalStateException(String.format("Tool `%s` next_actions example uses unknown type `%s`.", descriptor.getName(), type)));
        for (String each : MCPModelFacingPayloadContract.getNextActionRequiredFields(type)) {
            ShardingSpherePreconditions.checkState(action.containsKey(each),
                    () -> new IllegalStateException(String.format("Tool `%s` next_actions example `%s` must contain `%s`.", descriptor.getName(), type, each)));
        }
        for (Object each : action.keySet()) {
            String fieldName = String.valueOf(each);
            ShardingSpherePreconditions.checkState(allowedFields.contains(fieldName),
                    () -> new IllegalStateException(String.format("Tool `%s` next_actions example `%s` contains unsupported field `%s`.", descriptor.getName(), type, fieldName)));
        }
    }
    
    private static void validateModelCriticalOutputHints(final MCPToolDescriptor descriptor, final Map<?, ?> properties) {
        for (Entry<?, ?> entry : properties.entrySet()) {
            String fieldName = String.valueOf(entry.getKey());
            if (MCPModelFacingPayloadContract.getModelCriticalFieldNames().contains(fieldName)) {
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
            ShardingSpherePreconditions.checkState(MCPModelFacingPayloadContract.getNextActionSchemaAllowedFields().contains(fieldName),
                    () -> new IllegalStateException(String.format("Tool `%s` next_actions item contains unsupported field `%s`.", descriptor.getName(), fieldName)));
        }
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
