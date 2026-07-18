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
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.SecretReferenceValue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Workflow artifact masking utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowArtifactMaskUtils {
    
    /**
     * Create masked rule artifact map.
     *
     * @param ruleArtifact rule artifact
     * @param propertySource workflow property source
     * @param propertyRequirements property requirements
     * @return masked rule artifact map
     */
    public static Map<String, Object> createMaskedRuleArtifactMap(final RuleArtifact ruleArtifact, final WorkflowPropertySource propertySource,
                                                                  final List<AlgorithmPropertyRequirement> propertyRequirements) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put(WorkflowFieldNames.OPERATION_TYPE, ruleArtifact.getOperationType());
        result.put("sql", maskSensitiveSql(ruleArtifact.getSql(), propertySource, propertyRequirements));
        result.put("redaction", createRedactionPayload(propertySource, propertyRequirements));
        return result;
    }
    
    /**
     * Mask sensitive values inside SQL text.
     *
     * @param sql SQL text
     * @param propertySource workflow property source
     * @param propertyRequirements property requirements
     * @return masked SQL text
     */
    public static String maskSensitiveSql(final String sql, final WorkflowPropertySource propertySource, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        String result = WorkflowSecretReferenceUtils.replacePlaceholdersWithManualPlaceholders(sql, propertySource);
        for (String each : collectSecretValues(propertySource, propertyRequirements)) {
            if (each.isEmpty()) {
                continue;
            }
            result = result.replace(each, "******");
            result = result.replace(WorkflowSQLUtils.escapeLiteral(each), "******");
        }
        return result;
    }
    
    /**
     * Mask sensitive property values.
     *
     * @param properties property values
     * @param propertyRequirements property requirements
     * @return masked property values
     */
    public static Map<String, String> maskPropertyMap(final Map<String, String> properties, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        Map<String, String> result = new LinkedHashMap<>(properties.size(), 1F);
        properties.forEach((key, value) -> result.put(key, isSecretProperty(propertyRequirements, key) || isSecretReferencePlaceholder(value) ? "******" : value));
        return result;
    }
    
    /**
     * Mask sensitive property values.
     *
     * @param properties property values
     * @param propertyRequirements property requirements
     * @param propertySource workflow property source
     * @param algorithmRole algorithm role
     * @return masked property values
     */
    public static Map<String, String> maskPropertyMap(final Map<String, String> properties, final List<AlgorithmPropertyRequirement> propertyRequirements,
                                                      final WorkflowPropertySource propertySource, final String algorithmRole) {
        Map<String, String> result = new LinkedHashMap<>(properties.size(), 1F);
        properties.forEach((key, value) -> result.put(key, isSecretProperty(propertyRequirements, key) || isSecretReferenceProperty(propertySource, algorithmRole, key)
                || isSecretReferencePlaceholder(value) ? "******" : value));
        return result;
    }
    
    /**
     * Create secret reference summary payload.
     *
     * @param propertySource workflow property source
     * @return secret reference summary payload
     */
    public static Map<String, Object> createSecretReferenceSummary(final WorkflowPropertySource propertySource) {
        List<Map<String, Object>> references = WorkflowSecretReferenceUtils.createSafeSummaries(propertySource);
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("required", !references.isEmpty());
        result.put("reference_count", references.size());
        result.put("value_handling", "manual_execution");
        result.put("references", references);
        return result;
    }
    
    private static Map<String, Object> createRedactionPayload(final WorkflowPropertySource propertySource, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        List<String> redactedProperties = collectSecretPropertyNames(propertySource, propertyRequirements).stream().toList();
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("applied", !redactedProperties.isEmpty());
        result.put("marker", "******");
        result.put("redacted_properties", redactedProperties);
        result.put("redacted_count", redactedProperties.size());
        result.put("categories", redactedProperties.stream().map(WorkflowArtifactMaskUtils::createRedactedCategory).distinct().toList());
        return result;
    }
    
    private static String createRedactedCategory(final String redactedProperty) {
        int separatorIndex = redactedProperty.indexOf('.');
        return 0 > separatorIndex ? redactedProperty : redactedProperty.substring(separatorIndex + 1);
    }
    
    private static Set<String> collectSecretValues(final WorkflowPropertySource propertySource, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        Set<String> result = new LinkedHashSet<>();
        for (AlgorithmPropertyRequirement each : propertyRequirements) {
            if (!each.isSecret()) {
                continue;
            }
            String value = getPropertyValue(propertySource, each);
            if (null != value && !value.isBlank()) {
                result.add(value.trim());
            }
        }
        propertySource.getSecretReferences().forEach((algorithmRole, references) -> references.keySet().forEach(
                propertyKey -> result.add(SecretReferenceValue.createPlaceholder(algorithmRole, propertyKey))));
        return result;
    }
    
    private static Set<String> collectSecretPropertyNames(final WorkflowPropertySource propertySource, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        Set<String> result = new LinkedHashSet<>();
        for (AlgorithmPropertyRequirement each : propertyRequirements) {
            if (!each.isSecret()) {
                continue;
            }
            String value = getPropertyValue(propertySource, each);
            if (null != value && !value.isBlank()) {
                result.add(each.getAlgorithmRole() + "." + each.getPropertyKey());
            }
        }
        propertySource.getSecretReferences().forEach((algorithmRole, references) -> references.keySet().forEach(
                propertyKey -> result.add(algorithmRole + "." + propertyKey)));
        return result;
    }
    
    private static String getPropertyValue(final WorkflowPropertySource propertySource, final AlgorithmPropertyRequirement propertyRequirement) {
        return propertySource.getAlgorithmProperties(propertyRequirement.getAlgorithmRole()).get(propertyRequirement.getPropertyKey());
    }
    
    private static boolean isSecretProperty(final List<AlgorithmPropertyRequirement> propertyRequirements, final String propertyKey) {
        for (AlgorithmPropertyRequirement each : propertyRequirements) {
            if (each.isSecret() && propertyKey.equals(each.getPropertyKey())) {
                return true;
            }
        }
        String actualPropertyKey = propertyKey.toLowerCase();
        return actualPropertyKey.contains("password") || actualPropertyKey.contains("secret") || actualPropertyKey.contains("token")
                || "key".equals(actualPropertyKey) || actualPropertyKey.contains("-key") || actualPropertyKey.contains("key-")
                || actualPropertyKey.contains("_key") || actualPropertyKey.contains("key_");
    }
    
    private static boolean isSecretReferencePlaceholder(final String value) {
        return null != value && value.startsWith("secret_reference:");
    }
    
    private static boolean isSecretReferenceProperty(final WorkflowPropertySource propertySource, final String algorithmRole, final String propertyKey) {
        return null != propertySource && propertySource.getSecretReferences(algorithmRole).containsKey(propertyKey);
    }
}
