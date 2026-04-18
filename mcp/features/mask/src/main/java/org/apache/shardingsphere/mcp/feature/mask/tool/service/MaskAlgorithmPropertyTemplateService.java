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

package org.apache.shardingsphere.mcp.feature.mask.tool.service;

import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Mask algorithm property template service.
 */
public final class MaskAlgorithmPropertyTemplateService {
    
    private static final Map<String, List<AlgorithmPropertyRequirement>> MASK_TEMPLATES = createMaskTemplates();
    
    /**
     * Find property requirements.
     *
     * @param algorithmType primary algorithm type
     * @return property requirements
     */
    public List<AlgorithmPropertyRequirement> findRequirements(final String algorithmType) {
        List<AlgorithmPropertyRequirement> result = new LinkedList<>();
        String actualAlgorithmType = WorkflowSqlUtils.trimToEmpty(algorithmType).toUpperCase(Locale.ENGLISH);
        if (actualAlgorithmType.isEmpty()) {
            return result;
        }
        for (AlgorithmPropertyRequirement each : MASK_TEMPLATES.getOrDefault(actualAlgorithmType, List.of())) {
            result.add(new AlgorithmPropertyRequirement("primary", each.getPropertyKey(), each.isRequired(), each.isSecret(), each.getDescription(), each.getDefaultValue()));
        }
        return result;
    }
    
    /**
     * Mask properties for review.
     *
     * @param requirements requirements
     * @param actualProperties actual properties
     * @return masked properties
     */
    public Map<String, String> maskProperties(final List<AlgorithmPropertyRequirement> requirements, final Map<String, String> actualProperties) {
        Map<String, String> result = new LinkedHashMap<>(actualProperties.size(), 1F);
        for (Entry<String, String> entry : actualProperties.entrySet()) {
            result.put(entry.getKey(), isSecret(requirements, entry.getKey()) ? "******" : entry.getValue());
        }
        return result;
    }
    
    private boolean isSecret(final List<AlgorithmPropertyRequirement> requirements, final String propertyKey) {
        return requirements.stream().filter(each -> each.getPropertyKey().equals(propertyKey)).findFirst().map(AlgorithmPropertyRequirement::isSecret).orElse(false);
    }
    
    private static Map<String, List<AlgorithmPropertyRequirement>> createMaskTemplates() {
        Map<String, List<AlgorithmPropertyRequirement>> result = new LinkedHashMap<>(8, 1F);
        result.put("KEEP_FIRST_N_LAST_M", List.of(
                new AlgorithmPropertyRequirement("primary", "first-n", true, false, "Characters to keep from the left.", ""),
                new AlgorithmPropertyRequirement("primary", "last-m", true, false, "Characters to keep from the right.", ""),
                new AlgorithmPropertyRequirement("primary", "replace-char", true, false, "Replacement character.", "*")));
        result.put("KEEP_FROM_X_TO_Y", List.of(
                new AlgorithmPropertyRequirement("primary", "from-x", true, false, "Start position.", ""),
                new AlgorithmPropertyRequirement("primary", "to-y", true, false, "End position.", ""),
                new AlgorithmPropertyRequirement("primary", "replace-char", true, false, "Replacement character.", "*")));
        result.put("MASK_FIRST_N_LAST_M", List.of(
                new AlgorithmPropertyRequirement("primary", "first-n", true, false, "Characters to mask from the left.", ""),
                new AlgorithmPropertyRequirement("primary", "last-m", true, false, "Characters to mask from the right.", ""),
                new AlgorithmPropertyRequirement("primary", "replace-char", true, false, "Replacement character.", "*")));
        result.put("MASK_FROM_X_TO_Y", List.of(
                new AlgorithmPropertyRequirement("primary", "from-x", true, false, "Start position.", ""),
                new AlgorithmPropertyRequirement("primary", "to-y", true, false, "End position.", ""),
                new AlgorithmPropertyRequirement("primary", "replace-char", true, false, "Replacement character.", "*")));
        result.put("MASK_AFTER_SPECIAL_CHARS", List.of(
                new AlgorithmPropertyRequirement("primary", "special-chars", true, false, "Special chars anchor.", ""),
                new AlgorithmPropertyRequirement("primary", "replace-char", true, false, "Replacement character.", "*")));
        result.put("MASK_BEFORE_SPECIAL_CHARS", List.of(
                new AlgorithmPropertyRequirement("primary", "special-chars", true, false, "Special chars anchor.", ""),
                new AlgorithmPropertyRequirement("primary", "replace-char", true, false, "Replacement character.", "*")));
        result.put("GENERIC_TABLE_RANDOM_REPLACE", List.of(
                new AlgorithmPropertyRequirement("primary", "uppercase-letter-codes", false, false, "Uppercase replacement set.", ""),
                new AlgorithmPropertyRequirement("primary", "lowercase-letter-codes", false, false, "Lowercase replacement set.", ""),
                new AlgorithmPropertyRequirement("primary", "digital-codes", false, false, "Digital replacement set.", ""),
                new AlgorithmPropertyRequirement("primary", "special-codes", false, false, "Special replacement set.", "")));
        result.put("MD5", List.of());
        return result;
    }
}
