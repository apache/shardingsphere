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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Algorithm property template service.
 */
public final class AlgorithmPropertyTemplateService {
    
    private static final Map<String, List<AlgorithmPropertyRequirement>> ENCRYPT_TEMPLATES = createEncryptTemplates();
    
    private static final Map<String, List<AlgorithmPropertyRequirement>> MASK_TEMPLATES = createMaskTemplates();
    
    /**
     * Find property requirements.
     *
     * @param intentType intent type
     * @param primaryAlgorithmType primary algorithm type
     * @param assistedQueryAlgorithmType assisted query algorithm type
     * @param likeQueryAlgorithmType like query algorithm type
     * @return property requirements
     */
    public List<AlgorithmPropertyRequirement> findRequirements(final String intentType, final String primaryAlgorithmType,
                                                               final String assistedQueryAlgorithmType, final String likeQueryAlgorithmType) {
        List<AlgorithmPropertyRequirement> result = new LinkedList<>();
        if ("encrypt".equalsIgnoreCase(intentType)) {
            result.addAll(findTemplate("primary", primaryAlgorithmType, ENCRYPT_TEMPLATES));
            result.addAll(findTemplate("assisted_query", assistedQueryAlgorithmType, ENCRYPT_TEMPLATES));
            result.addAll(findTemplate("like_query", likeQueryAlgorithmType, ENCRYPT_TEMPLATES));
        } else if ("mask".equalsIgnoreCase(intentType)) {
            result.addAll(findTemplate("primary", primaryAlgorithmType, MASK_TEMPLATES));
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
    
    /**
     * Find missing required properties.
     *
     * @param requirements requirements
     * @param actualProperties actual properties
     * @return missing property keys
     */
    public List<String> findMissingRequiredProperties(final List<AlgorithmPropertyRequirement> requirements, final Map<String, String> actualProperties) {
        List<String> result = new LinkedList<>();
        for (AlgorithmPropertyRequirement each : requirements) {
            if (each.isRequired() && WorkflowSqlUtils.trimToEmpty(actualProperties.get(each.getPropertyKey())).isEmpty()) {
                result.add(each.getPropertyKey());
            }
        }
        return result;
    }
    
    private boolean isSecret(final List<AlgorithmPropertyRequirement> requirements, final String propertyKey) {
        return requirements.stream().filter(each -> each.getPropertyKey().equals(propertyKey)).findFirst().map(AlgorithmPropertyRequirement::isSecret).orElse(false);
    }
    
    private List<AlgorithmPropertyRequirement> findTemplate(final String role, final String algorithmType, final Map<String, List<AlgorithmPropertyRequirement>> templates) {
        List<AlgorithmPropertyRequirement> result = new LinkedList<>();
        String actualAlgorithmType = WorkflowSqlUtils.trimToEmpty(algorithmType).toUpperCase(Locale.ENGLISH);
        if (actualAlgorithmType.isEmpty()) {
            return result;
        }
        for (AlgorithmPropertyRequirement each : templates.getOrDefault(actualAlgorithmType, List.of())) {
            result.add(new AlgorithmPropertyRequirement(role, each.getPropertyKey(), each.isRequired(), each.isSecret(), each.getDescription(), each.getDefaultValue()));
        }
        return result;
    }
    
    private static Map<String, List<AlgorithmPropertyRequirement>> createEncryptTemplates() {
        Map<String, List<AlgorithmPropertyRequirement>> result = new LinkedHashMap<>(4, 1F);
        result.put("AES", List.of(
                new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "AES secret key.", ""),
                new AlgorithmPropertyRequirement("primary", "digest-algorithm-name", false, false, "Digest algorithm name.", "SHA-1")));
        result.put("MD5", List.of(new AlgorithmPropertyRequirement("primary", "salt", false, true, "Optional salt.", "")));
        return result;
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
