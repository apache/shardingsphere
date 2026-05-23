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

package org.apache.shardingsphere.mcp.bootstrap.transport.capability.tool;

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;

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
    
    private static final String ALLOWED_VALUES_FIELD = "allowed_values";
    
    private static final String SECRET_FIELD = "secret";
    
    private static final String PROPERTIES_FIELD = "properties";
    
    private static final String ADDITIONAL_PROPERTIES_FIELD = "additionalProperties";
    
    private static final String TYPE_FIELD = "type";
    
    private static final String DOT = ".";
    
    private static final String FORM_PROPERTY_PREFIX = "field_";
    
    private static final String CAMEL_CASE_SEPARATOR_PATTERN = "([a-z])([A-Z])";
    
    private static final String NON_ALPHANUMERIC_PATTERN = "[^a-z0-9]+";
    
    private static final List<String> SENSITIVE_FIELD_NAME_MARKERS = List.of(
            "password", "passwd", "passphrase", "secret", "token", "accesstoken", "apikey", "privatekey", "credential", "card", "cvv", "payment", "key");
    
    boolean isPlanningTool(final MCPToolDescriptor toolDescriptor) {
        return MCPDescriptorCatalogIndex.findToolRuntimeDescriptor(toolDescriptor.getName())
                .map(optional -> PLANNING_WORKFLOW_ROLE.equals(optional.getWorkflowRole())).orElse(false);
    }
    
    boolean hasClarificationQuestions(final Map<String, Object> payload) {
        Object clarificationQuestions = payload.get(CLARIFICATION_QUESTIONS_FIELD);
        return clarificationQuestions instanceof List<?> questions && !questions.isEmpty();
    }
    
    Optional<ClarificationForm> createClarificationForm(final Map<String, Object> payload, final MCPToolDescriptor toolDescriptor) {
        String planId = getPlanId(payload).trim();
        if (planId.isEmpty()) {
            return Optional.empty();
        }
        Object clarificationQuestions = payload.get(CLARIFICATION_QUESTIONS_FIELD);
        if (!(clarificationQuestions instanceof List<?> questions) || questions.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Object> properties = new LinkedHashMap<>(questions.size(), 1F);
        List<String> required = new LinkedList<>();
        Map<String, ArgumentBinding> fieldBindings = new LinkedHashMap<>(questions.size(), 1F);
        int questionIndex = 1;
        for (Object each : questions) {
            if (!(each instanceof Map<?, ?> question) || isSensitiveClarificationQuestion(question)) {
                return Optional.empty();
            }
            Optional<ArgumentBinding> binding = createArgumentBinding(question, toolDescriptor, questionIndex);
            if (binding.isEmpty()) {
                return Optional.empty();
            }
            ArgumentBinding argumentBinding = binding.get();
            if (fieldBindings.containsKey(argumentBinding.formPropertyName())) {
                return Optional.empty();
            }
            properties.put(argumentBinding.formPropertyName(), createRequestedPropertySchema(question, argumentBinding));
            required.add(argumentBinding.formPropertyName());
            fieldBindings.put(argumentBinding.formPropertyName(), argumentBinding);
            questionIndex++;
        }
        return properties.isEmpty() ? Optional.empty() : Optional.of(new ClarificationForm(createObjectSchema(properties, required), fieldBindings, planId));
    }
    
    boolean hasSensitiveClarificationQuestions(final Map<String, Object> payload) {
        Object clarificationQuestions = payload.get(CLARIFICATION_QUESTIONS_FIELD);
        if (!(clarificationQuestions instanceof List<?> questions)) {
            return false;
        }
        for (Object each : questions) {
            if (each instanceof Map<?, ?> question && isSensitiveClarificationQuestion(question)) {
                return true;
            }
        }
        return false;
    }
    
    private Optional<ArgumentBinding> createArgumentBinding(final Map<?, ?> question, final MCPToolDescriptor toolDescriptor, final int questionIndex) {
        String field = getField(question);
        if (field.isEmpty()) {
            return Optional.empty();
        }
        String formPropertyName = FORM_PROPERTY_PREFIX + questionIndex;
        return findArgumentBindingTarget(field, toolDescriptor)
                .map(target -> new ArgumentBinding(formPropertyName, target.argumentName(), target.fieldName(), getInputType(question), getAllowedValues(question)));
    }
    
    private boolean isSensitiveClarificationQuestion(final Map<?, ?> question) {
        return Boolean.TRUE.equals(question.get(SECRET_FIELD)) || isSecretInputType(question) || isSensitiveFieldName(question) || isUnknownAlgorithmPropertySensitivity(question);
    }
    
    private boolean isSecretInputType(final Map<?, ?> question) {
        return "secret".equals(normalizeInputType(question));
    }
    
    private boolean isSensitiveFieldName(final Map<?, ?> question) {
        String fieldName = normalizeSensitiveName(getField(question));
        for (String each : SENSITIVE_FIELD_NAME_MARKERS) {
            if (fieldName.contains(each)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isUnknownAlgorithmPropertySensitivity(final Map<?, ?> question) {
        return normalizeSensitiveName(getField(question)).contains("algorithmproperties") && !question.containsKey(SECRET_FIELD);
    }
    
    private String normalizeSensitiveName(final String value) {
        return value.replaceAll(CAMEL_CASE_SEPARATOR_PATTERN, "$1 $2").toLowerCase(Locale.ENGLISH).replaceAll(NON_ALPHANUMERIC_PATTERN, "");
    }
    
    String getPlanId(final Map<String, Object> payload) {
        return Objects.toString(payload.get(PLAN_ID_FIELD), "");
    }
    
    private Map<String, Object> createRequestedPropertySchema(final Map<?, ?> question, final ArgumentBinding binding) {
        Map<String, Object> result = new LinkedHashMap<>(binding.allowedValues().isEmpty() ? 2 : 3, 1F);
        result.put(TYPE_FIELD, binding.inputType());
        result.put("description", Objects.toString(question.get(DISPLAY_MESSAGE_FIELD), ""));
        if (!binding.allowedValues().isEmpty()) {
            result.put("enum", binding.allowedValues());
        }
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
    
    boolean isValidElicitedContent(final ClarificationForm form, final Map<String, Object> elicitedContent) {
        for (String each : elicitedContent.keySet()) {
            if (!form.fieldBindings().containsKey(each)) {
                return false;
            }
        }
        for (ArgumentBinding each : form.fieldBindings().values()) {
            if (!elicitedContent.containsKey(each.formPropertyName()) || !isValidElicitedValue(elicitedContent.get(each.formPropertyName()), each)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isValidElicitedValue(final Object value, final ArgumentBinding binding) {
        if (null == value) {
            return false;
        }
        if (!binding.allowedValues().isEmpty() && !binding.allowedValues().contains(value)) {
            return false;
        }
        return "boolean".equals(binding.inputType()) ? value instanceof Boolean : value instanceof String stringValue && !stringValue.trim().isEmpty();
    }
    
    Map<String, Object> mergeArguments(final Map<String, Object> args, final ClarificationForm form, final Map<String, Object> elicitedContent) {
        Map<String, Object> result = new LinkedHashMap<>(args);
        if (!result.containsKey(PLAN_ID_FIELD)) {
            result.put(PLAN_ID_FIELD, form.planId());
        }
        for (ArgumentBinding each : form.fieldBindings().values()) {
            putElicitedArgument(result, each, elicitedContent.get(each.formPropertyName()));
        }
        return result;
    }
    
    private void putElicitedArgument(final Map<String, Object> args, final ArgumentBinding binding, final Object value) {
        if (binding.isTopLevel()) {
            args.put(binding.argumentName(), value);
        } else {
            putNestedArgument(args, binding.argumentName(), binding.fieldName(), value);
        }
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
    
    private Optional<ArgumentBindingTarget> findArgumentBindingTarget(final String field, final MCPToolDescriptor toolDescriptor) {
        int separatorIndex = field.indexOf(DOT);
        if (0 < separatorIndex && separatorIndex < field.length() - 1 && isBindableObjectProperty(toolDescriptor, field.substring(0, separatorIndex), field.substring(separatorIndex + 1))) {
            return Optional.of(new ArgumentBindingTarget(field.substring(0, separatorIndex), field.substring(separatorIndex + 1)));
        }
        if (hasArgument(toolDescriptor, field) && !isObjectArgument(toolDescriptor, field)) {
            return Optional.of(new ArgumentBindingTarget(field, ""));
        }
        return findObjectArgumentName(toolDescriptor, field).map(objectArgumentName -> new ArgumentBindingTarget(objectArgumentName, field));
    }
    
    private boolean isBindableObjectProperty(final MCPToolDescriptor toolDescriptor, final String argumentName, final String fieldName) {
        Object property = getInputProperties(toolDescriptor).get(argumentName);
        return acceptsObjectProperty(property, fieldName);
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
            if (!acceptsDeclaredObjectProperty(entry.getValue(), fieldName)) {
                continue;
            }
            if (null != result) {
                return Optional.empty();
            }
            result = entry.getKey();
        }
        return Optional.ofNullable(result);
    }
    
    private boolean acceptsDeclaredObjectProperty(final Object value, final String fieldName) {
        if (!(value instanceof Map<?, ?> objectProperty) || !"object".equals(objectProperty.get(TYPE_FIELD))) {
            return false;
        }
        Object properties = objectProperty.get(PROPERTIES_FIELD);
        return properties instanceof Map<?, ?> propertyMap && propertyMap.containsKey(fieldName);
    }
    
    private boolean acceptsObjectProperty(final Object value, final String fieldName) {
        if (!(value instanceof Map<?, ?> objectProperty) || !"object".equals(objectProperty.get(TYPE_FIELD))) {
            return false;
        }
        Object properties = objectProperty.get(PROPERTIES_FIELD);
        boolean hasDeclaredProperty = properties instanceof Map<?, ?> propertyMap && propertyMap.containsKey(fieldName);
        return hasDeclaredProperty || Boolean.TRUE.equals(objectProperty.get(ADDITIONAL_PROPERTIES_FIELD));
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getInputProperties(final MCPToolDescriptor toolDescriptor) {
        Object properties = toolDescriptor.getInputSchema().get(PROPERTIES_FIELD);
        return properties instanceof Map<?, ?> ? (Map<String, Object>) properties : Map.of();
    }
    
    private String getField(final Map<?, ?> question) {
        return Objects.toString(question.get(FIELD_FIELD), "").trim();
    }
    
    private String getInputType(final Map<?, ?> question) {
        return "boolean".equals(normalizeInputType(question)) ? "boolean" : "string";
    }
    
    private List<?> getAllowedValues(final Map<?, ?> question) {
        Object allowedValues = question.get(ALLOWED_VALUES_FIELD);
        return allowedValues instanceof List<?> ? (List<?>) allowedValues : List.of();
    }
    
    private String normalizeInputType(final Map<?, ?> question) {
        return Objects.toString(question.get(INPUT_TYPE_FIELD), "").trim().toLowerCase(Locale.ENGLISH);
    }
    
    record ClarificationForm(Map<String, Object> requestedSchema, Map<String, ArgumentBinding> fieldBindings, String planId) {
    }
    
    private record ArgumentBinding(String formPropertyName, String argumentName, String fieldName, String inputType, List<?> allowedValues) {
        
        private boolean isTopLevel() {
            return fieldName.isEmpty();
        }
    }
    
    private record ArgumentBindingTarget(String argumentName, String fieldName) {
    }
}
