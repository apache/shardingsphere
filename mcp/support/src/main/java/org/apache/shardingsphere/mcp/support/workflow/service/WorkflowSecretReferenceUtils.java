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

package org.apache.shardingsphere.mcp.support.workflow.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowPropertySource;
import org.apache.shardingsphere.mcp.support.workflow.model.SecretReferenceValue;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Workflow secret reference utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowSecretReferenceUtils {
    
    private static final String SECRET_REF = "secret_ref";
    
    private static final String LABEL = "label";
    
    /**
     * Create algorithm properties from a raw argument value.
     *
     * @param rawValue raw argument value
     * @param algorithmRole algorithm role
     * @return algorithm properties
     */
    public static Map<String, String> createAlgorithmProperties(final Object rawValue, final String algorithmRole) {
        if (rawValue instanceof Map) {
            return createAlgorithmProperties((Map<?, ?>) rawValue, algorithmRole);
        }
        if (rawValue instanceof Collection) {
            return createAlgorithmProperties((Collection<?>) rawValue);
        }
        return Map.of();
    }
    
    private static Map<String, String> createAlgorithmProperties(final Map<?, ?> rawValue, final String algorithmRole) {
        Map<String, String> result = new LinkedHashMap<>(rawValue.size(), 1F);
        for (Entry<?, ?> entry : rawValue.entrySet()) {
            String key = Objects.toString(entry.getKey(), "").trim();
            if (key.isEmpty()) {
                continue;
            }
            result.put(key, isSecretReferenceObject(entry.getValue())
                    ? SecretReferenceValue.createPlaceholder(algorithmRole, key)
                    : Objects.toString(entry.getValue(), "").trim());
        }
        return result;
    }
    
    private static Map<String, String> createAlgorithmProperties(final Collection<?> rawValue) {
        Map<String, String> result = new LinkedHashMap<>(rawValue.size(), 1F);
        for (Object each : rawValue) {
            String actualEntry = Objects.toString(each, "").trim();
            int separatorIndex = actualEntry.indexOf('=');
            if (-1 == separatorIndex) {
                continue;
            }
            String key = actualEntry.substring(0, separatorIndex).trim();
            if (!key.isEmpty()) {
                result.put(key, actualEntry.substring(separatorIndex + 1).trim());
            }
        }
        return result;
    }
    
    /**
     * Create secret references from a raw argument value.
     *
     * @param rawValue raw argument value
     * @return secret references
     */
    public static Map<String, SecretReferenceValue> createSecretReferences(final Object rawValue) {
        if (!(rawValue instanceof final Map<?, ?> rawMap)) {
            return Map.of();
        }
        Map<String, SecretReferenceValue> result = new LinkedHashMap<>(rawMap.size(), 1F);
        for (Entry<?, ?> entry : rawMap.entrySet()) {
            String key = Objects.toString(entry.getKey(), "").trim();
            if (!key.isEmpty() && isSecretReferenceObject(entry.getValue())) {
                result.put(key, createSecretReferenceValue((Map<?, ?>) entry.getValue()));
            }
        }
        return result;
    }
    
    /**
     * Create safe summaries for secret references.
     *
     * @param propertySource workflow property source
     * @return safe summaries
     */
    public static List<Map<String, Object>> createSafeSummaries(final WorkflowPropertySource propertySource) {
        List<Map<String, Object>> result = new LinkedList<>();
        propertySource.getSecretReferences().forEach((algorithmRole, references) -> references.forEach(
                (propertyKey, reference) -> result.add(reference.toSafeSummary(algorithmRole, propertyKey))));
        return result;
    }
    
    /**
     * Replace secret reference placeholders with manual placeholders in SQL.
     *
     * @param sql SQL text
     * @param propertySource workflow property source
     * @return SQL text with manual placeholders
     */
    public static String replacePlaceholdersWithManualPlaceholders(final String sql, final WorkflowPropertySource propertySource) {
        String result = sql;
        if (null == propertySource) {
            return result;
        }
        for (Entry<String, Map<String, SecretReferenceValue>> groupEntry : propertySource.getSecretReferences().entrySet()) {
            for (String each : groupEntry.getValue().keySet()) {
                result = result.replace(SecretReferenceValue.createPlaceholder(groupEntry.getKey(), each), SecretReferenceValue.createManualPlaceholder(groupEntry.getKey(), each));
            }
        }
        return result;
    }
    
    /**
     * Judge whether a workflow property source has secret references.
     *
     * @param propertySource workflow property source
     * @return whether secret references exist
     */
    public static boolean hasSecretReferences(final WorkflowPropertySource propertySource) {
        return null != propertySource && propertySource.getSecretReferences().values().stream().anyMatch(each -> !each.isEmpty());
    }
    
    /**
     * Judge whether a workflow property source has malformed secret references.
     *
     * @param propertySource workflow property source
     * @return whether malformed secret references exist
     */
    public static boolean hasMalformedSecretReferences(final WorkflowPropertySource propertySource) {
        if (null == propertySource) {
            return false;
        }
        return propertySource.getSecretReferences().values().stream().flatMap(each -> each.values().stream()).anyMatch(SecretReferenceValue::isMalformed);
    }
    
    /**
     * Match property maps while accepting manually replaced secret-reference properties.
     *
     * @param expectedProperties expected properties
     * @param actualProperties actual properties
     * @param propertySource workflow property source
     * @param algorithmRole algorithm role
     * @return whether property maps match
     */
    public static boolean matchesManualPlaceholderProperties(final Map<String, String> expectedProperties, final Map<String, String> actualProperties,
                                                             final WorkflowPropertySource propertySource, final String algorithmRole) {
        if (null == propertySource) {
            return expectedProperties.equals(actualProperties);
        }
        Map<String, String> comparableExpected = new LinkedHashMap<>(expectedProperties);
        Map<String, String> comparableActual = new LinkedHashMap<>(actualProperties);
        for (String each : propertySource.getSecretReferences(algorithmRole).keySet()) {
            String expectedPlaceholder = SecretReferenceValue.createPlaceholder(algorithmRole, each);
            if (!expectedPlaceholder.equals(expectedProperties.get(each))) {
                continue;
            }
            String actualValue = actualProperties.get(each);
            if (null == actualValue || actualValue.isBlank() || expectedPlaceholder.equals(actualValue)
                    || SecretReferenceValue.createManualPlaceholder(algorithmRole, each).equals(actualValue)) {
                return false;
            }
            comparableExpected.remove(each);
            comparableActual.remove(each);
        }
        return comparableExpected.equals(comparableActual);
    }
    
    private static boolean isSecretReferenceObject(final Object value) {
        return value instanceof final Map<?, ?> map && (map.containsKey(SECRET_REF) || map.containsKey(LABEL));
    }
    
    private static SecretReferenceValue createSecretReferenceValue(final Map<?, ?> value) {
        Object secretRef = value.get(SECRET_REF);
        return secretRef instanceof String && !((String) secretRef).trim().isEmpty()
                ? SecretReferenceValue.create()
                : SecretReferenceValue.malformed();
    }
}
