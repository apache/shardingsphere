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
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolDescriptor;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Validator for MCP tool input schema contracts supported by the runtime.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPToolInputSchemaValidator {
    
    private static final Collection<String> SUPPORTED_TOP_LEVEL_FIELDS = Set.of("type", "properties", "required", "additionalProperties");
    
    private static final Collection<String> SUPPORTED_FIELDS = Set.of(
            "type", "properties", "required", "additionalProperties", "items", "enum", "minimum", "maximum", "default", "description", "examples");
    
    private static final Collection<String> SUPPORTED_TYPES = Set.of("string", "integer", "number", "boolean", "array", "object");
    
    /**
     * Validate one tool input schema.
     *
     * @param descriptor tool descriptor
     */
    public static void validate(final MCPToolDescriptor descriptor) {
        Map<String, Object> inputSchema = descriptor.getInputSchema();
        for (String each : inputSchema.keySet()) {
            ShardingSpherePreconditions.checkState(SUPPORTED_TOP_LEVEL_FIELDS.contains(each),
                    () -> new IllegalStateException(String.format("Tool `%s` inputSchema contains unsupported top-level field `%s`.", descriptor.getName(), each)));
        }
        ShardingSpherePreconditions.checkState("object".equals(inputSchema.get("type")),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema must be an object.", descriptor.getName())));
        ShardingSpherePreconditions.checkState(inputSchema.get("properties") instanceof Map,
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema must declare properties.", descriptor.getName())));
        ShardingSpherePreconditions.checkState(inputSchema.get("required") instanceof Collection,
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema required must be an array.", descriptor.getName())));
        ShardingSpherePreconditions.checkState(inputSchema.get("additionalProperties") instanceof Boolean,
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema additionalProperties must be a boolean.", descriptor.getName())));
        MCPToolDescriptorValidationUtils.validateModelFacingSchemaFields(descriptor, inputSchema);
        validateSchema(descriptor, inputSchema, "inputSchema");
    }
    
    private static void validateSchema(final MCPToolDescriptor descriptor, final Map<?, ?> schema, final String path) {
        for (Object each : schema.keySet()) {
            String fieldName = String.valueOf(each);
            ShardingSpherePreconditions.checkState(SUPPORTED_FIELDS.contains(fieldName),
                    () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s` contains unsupported field `%s`.", descriptor.getName(), path, fieldName)));
        }
        validateType(descriptor, schema, path);
        validateProperties(descriptor, schema, path);
        validateRequired(descriptor, schema, path);
        validateItems(descriptor, schema, path);
        validateAdditionalProperties(descriptor, schema, path);
        validateEnum(descriptor, schema, path);
        validateInteger(descriptor, schema, path);
        validateAnnotations(descriptor, schema, path);
    }
    
    private static void validateType(final MCPToolDescriptor descriptor, final Map<?, ?> schema, final String path) {
        if (!schema.containsKey("type")) {
            return;
        }
        Object type = schema.get("type");
        ShardingSpherePreconditions.checkState(type instanceof String && SUPPORTED_TYPES.contains(type),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s` uses unsupported type `%s`.", descriptor.getName(), path, type)));
    }
    
    private static void validateProperties(final MCPToolDescriptor descriptor, final Map<?, ?> schema, final String path) {
        if (!schema.containsKey("properties")) {
            return;
        }
        ShardingSpherePreconditions.checkState("object".equals(schema.get("type")),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s` must use type `object` with properties.", descriptor.getName(), path)));
        Object properties = schema.get("properties");
        ShardingSpherePreconditions.checkState(properties instanceof Map,
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s.properties` must be an object.", descriptor.getName(), path)));
        for (Entry<?, ?> entry : ((Map<?, ?>) properties).entrySet()) {
            ShardingSpherePreconditions.checkState(entry.getKey() instanceof String,
                    () -> new IllegalStateException(String.format("Tool `%s` inputSchema property name at `%s.properties` must be a string.", descriptor.getName(), path)));
            String propertyPath = path + ".properties." + entry.getKey();
            ShardingSpherePreconditions.checkState(entry.getValue() instanceof Map,
                    () -> new IllegalStateException(String.format("Tool `%s` inputSchema property `%s` must be an object.", descriptor.getName(), propertyPath)));
            Map<?, ?> property = (Map<?, ?>) entry.getValue();
            validateSchema(descriptor, property, propertyPath);
            Object description = property.get("description");
            MCPToolDescriptorValidationUtils.checkDescription(null == description ? "" : (String) description,
                    String.format("Tool `%s` inputSchema property `%s` description", descriptor.getName(), propertyPath));
        }
    }
    
    private static void validateRequired(final MCPToolDescriptor descriptor, final Map<?, ?> schema, final String path) {
        if (!schema.containsKey("required")) {
            return;
        }
        ShardingSpherePreconditions.checkState("object".equals(schema.get("type")),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s` must use type `object` with required properties.", descriptor.getName(), path)));
        Object required = schema.get("required");
        ShardingSpherePreconditions.checkState(required instanceof Collection,
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s.required` must be an array.", descriptor.getName(), path)));
        Collection<?> requiredProperties = (Collection<?>) required;
        ShardingSpherePreconditions.checkState(requiredProperties.stream().allMatch(String.class::isInstance),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s.required` must contain only strings.", descriptor.getName(), path)));
        ShardingSpherePreconditions.checkState(requiredProperties.stream().distinct().count() == requiredProperties.size(),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s.required` must contain unique property names.", descriptor.getName(), path)));
        Object properties = schema.get("properties");
        ShardingSpherePreconditions.checkState(properties instanceof Map && ((Map<?, ?>) properties).keySet().containsAll(requiredProperties),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s.required` references an undeclared property.", descriptor.getName(), path)));
    }
    
    private static void validateItems(final MCPToolDescriptor descriptor, final Map<?, ?> schema, final String path) {
        if (!schema.containsKey("items")) {
            return;
        }
        ShardingSpherePreconditions.checkState("array".equals(schema.get("type")),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s` must use type `array` with items.", descriptor.getName(), path)));
        Object items = schema.get("items");
        ShardingSpherePreconditions.checkState(items instanceof Map,
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s.items` must be an object.", descriptor.getName(), path)));
        validateSchema(descriptor, (Map<?, ?>) items, path + ".items");
    }
    
    private static void validateAdditionalProperties(final MCPToolDescriptor descriptor, final Map<?, ?> schema, final String path) {
        if (!schema.containsKey("additionalProperties")) {
            return;
        }
        ShardingSpherePreconditions.checkState("object".equals(schema.get("type")),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s` must use type `object` with additionalProperties.", descriptor.getName(), path)));
        Object additionalProperties = schema.get("additionalProperties");
        ShardingSpherePreconditions.checkState(additionalProperties instanceof Boolean || additionalProperties instanceof Map,
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s.additionalProperties` must be a boolean or object.", descriptor.getName(), path)));
        if (additionalProperties instanceof Map) {
            validateSchema(descriptor, (Map<?, ?>) additionalProperties, path + ".additionalProperties");
        }
    }
    
    private static void validateEnum(final MCPToolDescriptor descriptor, final Map<?, ?> schema, final String path) {
        if (!schema.containsKey("enum")) {
            return;
        }
        Object enumValues = schema.get("enum");
        ShardingSpherePreconditions.checkState(enumValues instanceof Collection && !((Collection<?>) enumValues).isEmpty(),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s.enum` must be a non-empty array.", descriptor.getName(), path)));
        Collection<?> values = (Collection<?>) enumValues;
        ShardingSpherePreconditions.checkState(values.stream().distinct().count() == values.size(),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s.enum` must contain unique values.", descriptor.getName(), path)));
    }
    
    private static void validateInteger(final MCPToolDescriptor descriptor, final Map<?, ?> schema, final String path) {
        boolean hasMinimum = schema.containsKey("minimum");
        boolean hasMaximum = schema.containsKey("maximum");
        boolean hasDefault = schema.containsKey("default");
        if (!hasMinimum && !hasMaximum) {
            if (hasDefault && "integer".equals(schema.get("type"))) {
                validateIntegerDefault(descriptor, schema, path, false);
            }
            return;
        }
        ShardingSpherePreconditions.checkState("integer".equals(schema.get("type")),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s` only supports minimum, maximum and default for type `integer`.", descriptor.getName(), path)));
        ShardingSpherePreconditions.checkState(hasMinimum == hasMaximum,
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s` must declare minimum and maximum together.", descriptor.getName(), path)));
        validateIntegerRange(descriptor, schema, path);
        if (hasDefault) {
            validateIntegerDefault(descriptor, schema, path, true);
        }
    }
    
    private static void validateIntegerRange(final MCPToolDescriptor descriptor, final Map<?, ?> schema, final String path) {
        Object minimum = schema.get("minimum");
        Object maximum = schema.get("maximum");
        ShardingSpherePreconditions.checkState(isSupportedIntegerValue(minimum) && isSupportedIntegerValue(maximum),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s` minimum and maximum must be integers in the Java int range.", descriptor.getName(), path)));
        ShardingSpherePreconditions.checkState(toBigInteger(minimum).compareTo(toBigInteger(maximum)) <= 0,
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s` minimum must not exceed maximum.", descriptor.getName(), path)));
    }
    
    private static void validateIntegerDefault(final MCPToolDescriptor descriptor, final Map<?, ?> schema, final String path, final boolean hasRange) {
        Object defaultValue = schema.get("default");
        ShardingSpherePreconditions.checkState(isSupportedIntegerValue(defaultValue),
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s.default` must be an integer in the Java int range.", descriptor.getName(), path)));
        if (!hasRange) {
            return;
        }
        BigInteger actual = toBigInteger(defaultValue);
        BigInteger minimum = toBigInteger(schema.get("minimum"));
        BigInteger maximum = toBigInteger(schema.get("maximum"));
        ShardingSpherePreconditions.checkState(actual.compareTo(minimum) >= 0 && actual.compareTo(maximum) <= 0,
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s.default` must be within the declared range.", descriptor.getName(), path)));
    }
    
    private static void validateAnnotations(final MCPToolDescriptor descriptor, final Map<?, ?> schema, final String path) {
        ShardingSpherePreconditions.checkState(!schema.containsKey("description") || schema.get("description") instanceof String,
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s.description` must be a string.", descriptor.getName(), path)));
        ShardingSpherePreconditions.checkState(!schema.containsKey("examples") || schema.get("examples") instanceof Collection,
                () -> new IllegalStateException(String.format("Tool `%s` inputSchema at `%s.examples` must be an array.", descriptor.getName(), path)));
    }
    
    private static boolean isSupportedIntegerValue(final Object value) {
        if (!(value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof BigInteger)) {
            return false;
        }
        BigInteger actual = toBigInteger(value);
        return actual.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0 && actual.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0;
    }
    
    private static BigInteger toBigInteger(final Object value) {
        return value instanceof BigInteger ? (BigInteger) value : BigInteger.valueOf(((Number) value).longValue());
    }
}
