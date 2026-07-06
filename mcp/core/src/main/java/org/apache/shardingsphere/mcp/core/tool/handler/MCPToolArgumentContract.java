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

package org.apache.shardingsphere.mcp.core.tool.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPExecutionModeRequiredException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidApprovedStepsException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidExecutionModeException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidToolArgumentException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPMissingToolArgumentException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPToolArgumentContractViolationException;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class MCPToolArgumentContract {
    
    private static final String APPROVED_STEPS = "approved_steps";
    
    private static final String TYPE = "type";
    
    private static final String PROPERTIES = "properties";
    
    private static final String REQUIRED = "required";
    
    private static final String ADDITIONAL_PROPERTIES = "additionalProperties";
    
    private static final String ITEMS = "items";
    
    private static final String ENUM = "enum";
    
    private static final String MINIMUM = "minimum";
    
    private static final String MAXIMUM = "maximum";
    
    private static final String DEFAULT_VALUE = "default";
    
    private static final String STRING = "string";
    
    private static final String INTEGER = "integer";
    
    private static final String NUMBER = "number";
    
    private static final String BOOLEAN = "boolean";
    
    private static final String ARRAY = "array";
    
    private static final String OBJECT = "object";
    
    private final String toolName;
    
    private final Map<String, Object> inputSchema;
    
    void validate(final Map<String, Object> arguments) {
        validateObject(arguments, inputSchema, "", arguments);
    }
    
    private void validateObject(final Map<?, ?> arguments, final Map<?, ?> schema, final String path, final Map<String, Object> rootArguments) {
        validateRequiredArguments(arguments, schema, path, rootArguments);
        for (Entry<?, ?> entry : arguments.entrySet()) {
            String argumentName = Objects.toString(entry.getKey(), "");
            validatePropertyArgument(entry.getValue(), schema, appendPath(path, argumentName), rootArguments, argumentName);
        }
    }
    
    private void validateRequiredArguments(final Map<?, ?> arguments, final Map<?, ?> schema, final String path, final Map<String, Object> rootArguments) {
        for (String each : getRequiredArgumentNames(schema)) {
            ShardingSpherePreconditions.checkState(arguments.containsKey(each), () -> createMissingArgumentException(rootArguments, appendPath(path, each)));
            if (isStringArgument(schema, each)) {
                checkRequiredTextArgument(arguments, rootArguments, appendPath(path, each), each);
            }
        }
    }
    
    private Collection<String> getRequiredArgumentNames(final Map<?, ?> schema) {
        Object required = schema.get(REQUIRED);
        return required instanceof Collection<?> ? ((Collection<?>) required).stream().filter(String.class::isInstance).map(String.class::cast).toList() : List.of();
    }
    
    private boolean isStringArgument(final Map<?, ?> schema, final String argumentName) {
        return STRING.equals(findProperty(schema, argumentName).get(TYPE));
    }
    
    private void checkRequiredTextArgument(final Map<?, ?> arguments, final Map<String, Object> rootArguments, final String path, final String argumentName) {
        String actualValue = Objects.toString(arguments.get(argumentName), "").trim();
        ShardingSpherePreconditions.checkState(!actualValue.isEmpty(), () -> createMissingArgumentException(rootArguments, path));
    }
    
    private void validatePropertyArgument(final Object value, final Map<?, ?> schema, final String argumentPath, final Map<String, Object> rootArguments, final String argumentName) {
        Map<?, ?> property = findProperty(schema, argumentName);
        if (!property.isEmpty()) {
            validateArgument(value, property, argumentPath, rootArguments);
            return;
        }
        Object additionalProperties = schema.get(ADDITIONAL_PROPERTIES);
        if (Boolean.FALSE.equals(additionalProperties)) {
            throw createContractViolationException(rootArguments, argumentPath, "unknown_argument", "", List.of());
        }
        if (additionalProperties instanceof Map<?, ?>) {
            validateArgument(value, (Map<?, ?>) additionalProperties, argumentPath, rootArguments);
        }
    }
    
    private void validateArgument(final Object value, final Map<?, ?> schema, final String path, final Map<String, Object> rootArguments) {
        String expectedType = Objects.toString(schema.get(TYPE), "");
        if (!expectedType.isEmpty()) {
            ShardingSpherePreconditions.checkState(isValidType(value, expectedType),
                    () -> createContractViolationException(rootArguments, path, "invalid_argument_type", expectedType, List.of()));
        }
        if (INTEGER.equals(expectedType)) {
            validateIntegerRange(value, schema, path);
        }
        validateEnumValue(value, schema, path, rootArguments);
        if (ARRAY.equals(expectedType)) {
            validateArrayArgument((Collection<?>) value, schema, path, rootArguments);
        }
        if (OBJECT.equals(expectedType)) {
            validateObject((Map<?, ?>) value, schema, path, rootArguments);
        }
    }
    
    private boolean isValidType(final Object value, final String expectedType) {
        if (STRING.equals(expectedType)) {
            return value instanceof String;
        }
        if (INTEGER.equals(expectedType)) {
            return value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof BigInteger;
        }
        if (NUMBER.equals(expectedType)) {
            return value instanceof Number;
        }
        if (BOOLEAN.equals(expectedType)) {
            return value instanceof Boolean;
        }
        if (ARRAY.equals(expectedType)) {
            return value instanceof Collection<?>;
        }
        return !OBJECT.equals(expectedType) || value instanceof Map<?, ?>;
    }
    
    private void validateIntegerRange(final Object value, final Map<?, ?> schema, final String path) {
        if (!(schema.get(MINIMUM) instanceof Number) || !(schema.get(MAXIMUM) instanceof Number)) {
            return;
        }
        BigInteger actualValue = toBigInteger(value);
        BigInteger minimumValue = toBigInteger(schema.get(MINIMUM));
        BigInteger maximumValue = toBigInteger(schema.get(MAXIMUM));
        ShardingSpherePreconditions.checkState(isIntegerWithinRange(actualValue, minimumValue, maximumValue), () -> createInvalidIntegerArgumentException(value, schema, path));
    }
    
    private RuntimeException createInvalidIntegerArgumentException(final Object value, final Map<?, ?> schema, final String path) {
        int minimum = getIntegerSchemaValue(schema, MINIMUM);
        int maximum = getIntegerSchemaValue(schema, MAXIMUM);
        return new MCPInvalidToolArgumentException(toolName, toolName, path, minimum, maximum, getSuggestedIntegerValue(value, schema, minimum, maximum),
                new MCPInvalidRequestException(String.format("%s is out of range.", path)));
    }
    
    private BigInteger toBigInteger(final Object value) {
        return value instanceof BigInteger ? (BigInteger) value : BigInteger.valueOf(((Number) value).longValue());
    }
    
    private int getIntegerSchemaValue(final Map<?, ?> schema, final String key) {
        return ((Number) schema.get(key)).intValue();
    }
    
    private int getSuggestedIntegerValue(final Object value, final Map<?, ?> schema, final int minimumValue, final int maximumValue) {
        Object defaultValue = schema.get(DEFAULT_VALUE);
        if (isValidType(defaultValue, INTEGER) && isIntegerWithinRange(defaultValue, minimumValue, maximumValue)) {
            return ((Number) defaultValue).intValue();
        }
        return toBigInteger(value).compareTo(BigInteger.valueOf(minimumValue)) < 0 ? minimumValue : maximumValue;
    }
    
    private boolean isIntegerWithinRange(final Object value, final int minimumValue, final int maximumValue) {
        return isIntegerWithinRange(toBigInteger(value), BigInteger.valueOf(minimumValue), BigInteger.valueOf(maximumValue));
    }
    
    private boolean isIntegerWithinRange(final BigInteger value, final BigInteger minimumValue, final BigInteger maximumValue) {
        return value.compareTo(minimumValue) >= 0 && value.compareTo(maximumValue) <= 0;
    }
    
    private void validateEnumValue(final Object value, final Map<?, ?> schema, final String path, final Map<String, Object> rootArguments) {
        Object enumValues = schema.get(ENUM);
        if (enumValues instanceof Collection<?> && !((Collection<?>) enumValues).contains(value)) {
            throw createInvalidEnumException(rootArguments, path, getEnumValues(schema));
        }
    }
    
    private void validateArrayArgument(final Collection<?> values, final Map<?, ?> schema, final String path, final Map<String, Object> rootArguments) {
        if (!(schema.get(ITEMS) instanceof Map<?, ?>)) {
            return;
        }
        int index = 0;
        for (Object each : values) {
            validateArgument(each, (Map<?, ?>) schema.get(ITEMS), path + "[" + index + "]", rootArguments);
            index++;
        }
    }
    
    private RuntimeException createMissingArgumentException(final Map<String, Object> arguments, final String argumentName) {
        if (!MCPPayloadFieldNames.EXECUTION_MODE.equals(argumentName)) {
            return new MCPMissingToolArgumentException(argumentName);
        }
        List<String> allowedValues = getEnumValues(findProperty(inputSchema, MCPPayloadFieldNames.EXECUTION_MODE));
        return new MCPExecutionModeRequiredException(toolName, allowedValues, createExecutionModeSuggestedArguments(arguments, allowedValues));
    }
    
    private RuntimeException createInvalidEnumException(final Map<String, Object> arguments, final String argumentName, final List<String> allowedValues) {
        if (MCPPayloadFieldNames.EXECUTION_MODE.equals(argumentName)) {
            return new MCPInvalidExecutionModeException(toolName, allowedValues, createExecutionModeSuggestedArguments(arguments, allowedValues));
        }
        if (argumentName.startsWith(APPROVED_STEPS + "[")) {
            return new MCPInvalidApprovedStepsException(allowedValues, createApprovedStepsSuggestedArguments(arguments));
        }
        return createContractViolationException(arguments, argumentName, "invalid_enum_value", "", allowedValues);
    }
    
    private RuntimeException createContractViolationException(final Map<String, Object> arguments, final String argumentName, final String category, final String expectedType,
                                                              final List<String> allowedValues) {
        return new MCPToolArgumentContractViolationException(toolName, argumentName, category, expectedType, allowedValues, createSuggestedArguments(arguments, argumentName));
    }
    
    private List<String> getEnumValues(final Map<?, ?> schema) {
        Object enumValues = schema.get(ENUM);
        return enumValues instanceof Collection<?> ? ((Collection<?>) enumValues).stream().filter(String.class::isInstance).map(String.class::cast).toList() : List.of();
    }
    
    private Map<String, Object> createExecutionModeSuggestedArguments(final Map<String, Object> arguments, final List<String> allowedValues) {
        Map<String, Object> result = new LinkedHashMap<>(arguments);
        result.remove(MCPPayloadFieldNames.EXECUTION_MODE);
        String suggestedExecutionMode = findSafestExecutionMode(allowedValues);
        if (!suggestedExecutionMode.isEmpty()) {
            result.put(MCPPayloadFieldNames.EXECUTION_MODE, suggestedExecutionMode);
        }
        return result;
    }
    
    private String findSafestExecutionMode(final List<String> allowedValues) {
        if (allowedValues.contains("preview")) {
            return "preview";
        }
        if (allowedValues.contains("manual-only")) {
            return "manual-only";
        }
        return allowedValues.isEmpty() ? "" : allowedValues.get(0);
    }
    
    private Map<String, Object> createApprovedStepsSuggestedArguments(final Map<String, Object> arguments) {
        Map<String, Object> result = new LinkedHashMap<>(arguments);
        result.remove(APPROVED_STEPS);
        String suggestedExecutionMode = findSafestExecutionMode(getEnumValues(findProperty(inputSchema, MCPPayloadFieldNames.EXECUTION_MODE)));
        if (!suggestedExecutionMode.isEmpty()) {
            result.put(MCPPayloadFieldNames.EXECUTION_MODE, suggestedExecutionMode);
        }
        return result;
    }
    
    private Map<String, Object> createSuggestedArguments(final Map<String, Object> arguments, final String argumentPath) {
        String argumentName = getRootArgumentName(argumentPath);
        if (getRequiredArgumentNames(inputSchema).contains(argumentName)) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>(arguments);
        result.remove(argumentName);
        return result;
    }
    
    private String getRootArgumentName(final String argumentPath) {
        int dotIndex = argumentPath.indexOf('.');
        int arrayIndex = argumentPath.indexOf('[');
        int endIndex = getRootArgumentNameEndIndex(dotIndex, arrayIndex);
        return -1 == endIndex ? argumentPath : argumentPath.substring(0, endIndex);
    }
    
    private int getRootArgumentNameEndIndex(final int dotIndex, final int arrayIndex) {
        if (-1 == dotIndex) {
            return arrayIndex;
        }
        if (-1 == arrayIndex) {
            return dotIndex;
        }
        return Math.min(dotIndex, arrayIndex);
    }
    
    private Map<?, ?> findProperty(final Map<?, ?> schema, final String argumentName) {
        Object properties = schema.get(PROPERTIES);
        if (!(properties instanceof Map<?, ?>) || !(((Map<?, ?>) properties).get(argumentName) instanceof Map<?, ?>)) {
            return Map.of();
        }
        return (Map<?, ?>) ((Map<?, ?>) properties).get(argumentName);
    }
    
    private String appendPath(final String path, final String argumentName) {
        return path.isEmpty() ? argumentName : path + "." + argumentName;
    }
}
