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
        result.put("operation_type", ruleArtifact.getOperationType());
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
        String result = sql;
        for (String each : collectSecretValues(propertySource, propertyRequirements)) {
            if (each.isEmpty()) {
                continue;
            }
            result = result.replace(each, "******");
            result = result.replace(WorkflowSQLUtils.escapeLiteral(each), "******");
        }
        return result;
    }
    
    private static Map<String, Object> createRedactionPayload(final WorkflowPropertySource propertySource, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        List<String> redactedProperties = collectSecretPropertyNames(propertySource, propertyRequirements).stream().toList();
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("applied", !redactedProperties.isEmpty());
        result.put("marker", "******");
        result.put("redacted_properties", redactedProperties);
        result.put("redacted_count", redactedProperties.size());
        result.put("redaction_summary", createRedactionSummary(redactedProperties));
        return result;
    }
    
    private static Map<String, Object> createRedactionSummary(final List<String> redactedProperties) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("categories", redactedProperties.stream().map(WorkflowArtifactMaskUtils::createRedactedCategory).distinct().toList());
        result.put("marker", "******");
        result.put("redacted_count", redactedProperties.size());
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
        return result;
    }
    
    private static String getPropertyValue(final WorkflowPropertySource propertySource, final AlgorithmPropertyRequirement propertyRequirement) {
        return propertySource.getAlgorithmProperties(propertyRequirement.getAlgorithmRole()).get(propertyRequirement.getPropertyKey());
    }
}
