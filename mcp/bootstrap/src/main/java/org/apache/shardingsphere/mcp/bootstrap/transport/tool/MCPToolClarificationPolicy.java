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

package org.apache.shardingsphere.mcp.bootstrap.transport.tool;

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorRegistry;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

final class MCPToolClarificationPolicy {
    
    private static final String PLANNING_WORKFLOW_ROLE = "plan";
    
    private static final String CLARIFICATION_QUESTIONS_FIELD = "clarification_questions";
    
    private static final String PLAN_ID_FIELD = "plan_id";
    
    private static final String FIELD_FIELD = "field";
    
    private static final String INPUT_TYPE_FIELD = "input_type";
    
    private static final String DISPLAY_MESSAGE_FIELD = "display_message";
    
    private static final String SECRET_FIELD = "secret";
    
    private static final String PROPERTIES_FIELD = "properties";
    
    private static final String TYPE_FIELD = "type";
    
    private static final List<String> SENSITIVE_FIELD_NAME_MARKERS = List.of("password", "token", "secret", "credential", "key");
    
    boolean isPlanningTool(final MCPToolDescriptor toolDescriptor) {
        return MCPDescriptorRegistry.findToolRuntimeDescriptor(toolDescriptor.getName())
                .map(runtimeDescriptor -> PLANNING_WORKFLOW_ROLE.equals(runtimeDescriptor.getWorkflowRole())).orElse(false);
    }
    
    boolean hasFormSafeClarificationQuestions(final Map<String, Object> payload) {
        Object clarificationQuestions = payload.get(CLARIFICATION_QUESTIONS_FIELD);
        return clarificationQuestions instanceof List<?> && !((List<?>) clarificationQuestions).isEmpty() && !hasSensitiveClarificationQuestion((List<?>) clarificationQuestions);
    }
    
    private boolean hasSensitiveClarificationQuestion(final List<?> clarificationQuestions) {
        for (Object each : clarificationQuestions) {
            if (each instanceof Map<?, ?> question && isSensitiveClarificationQuestion(question)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSensitiveClarificationQuestion(final Map<?, ?> question) {
        return Boolean.TRUE.equals(question.get(SECRET_FIELD)) || isSecretInputType(question) || isSensitiveFieldName(question);
    }
    
    private boolean isSecretInputType(final Map<?, ?> question) {
        return "secret".equals(Objects.toString(question.get(INPUT_TYPE_FIELD), "").trim().toLowerCase(Locale.ENGLISH));
    }
    
    private boolean isSensitiveFieldName(final Map<?, ?> question) {
        String fieldName = Objects.toString(question.get(FIELD_FIELD), "").trim().toLowerCase(Locale.ENGLISH);
        for (String each : SENSITIVE_FIELD_NAME_MARKERS) {
            if (fieldName.contains(each)) {
                return true;
            }
        }
        return false;
    }
    
    String getPlanId(final Map<String, Object> payload) {
        return Objects.toString(payload.get(PLAN_ID_FIELD), "");
    }
    
    Map<String, Object> createRequestedSchema(final Map<String, Object> payload) {
        List<?> clarificationQuestions = (List<?>) payload.get(CLARIFICATION_QUESTIONS_FIELD);
        Map<String, Object> properties = new LinkedHashMap<>(clarificationQuestions.size(), 1F);
        List<String> required = new LinkedList<>();
        for (Object each : clarificationQuestions) {
            if (each instanceof Map<?, ?> question) {
                addRequestedProperty(properties, required, question);
            }
        }
        return createObjectSchema(properties, required);
    }
    
    private void addRequestedProperty(final Map<String, Object> properties, final List<String> required, final Map<?, ?> question) {
        String field = Objects.toString(question.get(FIELD_FIELD), "").trim();
        if (!field.isEmpty()) {
            properties.put(field, createRequestedPropertySchema(question));
            required.add(field);
        }
    }
    
    private Map<String, Object> createRequestedPropertySchema(final Map<?, ?> question) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put(TYPE_FIELD, "boolean".equals(Objects.toString(question.get(INPUT_TYPE_FIELD), "")) ? "boolean" : "string");
        result.put("description", Objects.toString(question.get(DISPLAY_MESSAGE_FIELD), ""));
        return result;
    }
    
    private Map<String, Object> createObjectSchema(final Map<String, Object> properties, final List<String> required) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put(TYPE_FIELD, "object");
        result.put(PROPERTIES_FIELD, properties);
        result.put("required", required);
        result.put("additionalProperties", false);
        return result;
    }
    
    Map<String, Object> mergeArguments(final Map<String, Object> args, final Map<String, Object> payload, final Map<String, Object> elicitedContent,
                                       final MCPToolDescriptor toolDescriptor) {
        Map<String, Object> result = new LinkedHashMap<>(args);
        if (!result.containsKey(PLAN_ID_FIELD) && payload.containsKey(PLAN_ID_FIELD)) {
            result.put(PLAN_ID_FIELD, payload.get(PLAN_ID_FIELD));
        }
        for (Entry<String, Object> entry : elicitedContent.entrySet()) {
            putElicitedArgument(result, entry.getKey(), entry.getValue(), toolDescriptor);
        }
        return result;
    }
    
    private void putElicitedArgument(final Map<String, Object> args, final String field, final Object value, final MCPToolDescriptor toolDescriptor) {
        if (null == value || value instanceof String stringValue && stringValue.trim().isEmpty()) {
            return;
        }
        int separatorIndex = field.indexOf('.');
        if (0 < separatorIndex && separatorIndex < field.length() - 1 && isObjectArgument(toolDescriptor, field.substring(0, separatorIndex))) {
            putNestedArgument(args, field.substring(0, separatorIndex), field.substring(separatorIndex + 1), value);
            return;
        }
        if (hasArgument(toolDescriptor, field)) {
            args.put(field, value);
            return;
        }
        Optional<String> objectArgumentName = findObjectArgumentName(toolDescriptor, field);
        if (objectArgumentName.isPresent()) {
            putNestedArgument(args, objectArgumentName.get(), field, value);
            return;
        }
        args.put(field, value);
    }
    
    private boolean isObjectArgument(final MCPToolDescriptor toolDescriptor, final String argumentName) {
        Object property = getInputProperties(toolDescriptor).get(argumentName);
        return property instanceof Map<?, ?> propertyMap && "object".equals(propertyMap.get(TYPE_FIELD));
    }
    
    private boolean hasArgument(final MCPToolDescriptor toolDescriptor, final String argumentName) {
        return getInputProperties(toolDescriptor).containsKey(argumentName);
    }
    
    private Optional<String> findObjectArgumentName(final MCPToolDescriptor toolDescriptor, final String fieldName) {
        String result = null;
        for (Entry<String, Object> entry : getInputProperties(toolDescriptor).entrySet()) {
            if (!hasObjectProperty(entry.getValue(), fieldName)) {
                continue;
            }
            if (null != result) {
                return Optional.empty();
            }
            result = entry.getKey();
        }
        return Optional.ofNullable(result);
    }
    
    private boolean hasObjectProperty(final Object value, final String fieldName) {
        if (!(value instanceof Map<?, ?> objectProperty) || !"object".equals(objectProperty.get(TYPE_FIELD))) {
            return false;
        }
        Object properties = objectProperty.get(PROPERTIES_FIELD);
        return properties instanceof Map<?, ?> propertyMap && propertyMap.containsKey(fieldName);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getInputProperties(final MCPToolDescriptor toolDescriptor) {
        Object properties = toolDescriptor.getInputSchema().get(PROPERTIES_FIELD);
        return properties instanceof Map<?, ?> ? (Map<String, Object>) properties : Map.of();
    }
    
    private void putNestedArgument(final Map<String, Object> args, final String argumentName, final String fieldName, final Object value) {
        Map<String, Object> nestedArguments = createNestedArguments(args.get(argumentName));
        nestedArguments.put(fieldName, value);
        args.put(argumentName, nestedArguments);
    }
    
    private Map<String, Object> createNestedArguments(final Object rawValue) {
        Map<String, Object> result = new LinkedHashMap<>(rawValue instanceof Map<?, ?> ? ((Map<?, ?>) rawValue).size() + 1 : 4, 1F);
        if (rawValue instanceof Map<?, ?> rawMap) {
            for (Entry<?, ?> entry : rawMap.entrySet()) {
                result.put(Objects.toString(entry.getKey(), ""), entry.getValue());
            }
        }
        return result;
    }
}
