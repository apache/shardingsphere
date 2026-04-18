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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.service;

import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Algorithm property template service.
 */
public final class EncryptAlgorithmPropertyTemplateService {
    
    private static final Map<String, List<AlgorithmPropertyRequirement>> ENCRYPT_TEMPLATES = createEncryptTemplates();
    
    /**
     * Find property requirements.
     *
     * @param primaryAlgorithmType primary algorithm type
     * @param assistedQueryAlgorithmType assisted query algorithm type
     * @param likeQueryAlgorithmType like query algorithm type
     * @return property requirements
     */
    public List<AlgorithmPropertyRequirement> findRequirements(final String primaryAlgorithmType, final String assistedQueryAlgorithmType, final String likeQueryAlgorithmType) {
        List<AlgorithmPropertyRequirement> result = new LinkedList<>();
        result.addAll(findTemplate("primary", primaryAlgorithmType));
        result.addAll(findTemplate("assisted_query", assistedQueryAlgorithmType));
        result.addAll(findTemplate("like_query", likeQueryAlgorithmType));
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
        return actualProperties.entrySet().stream().collect(Collectors.toMap(Entry::getKey,
                entry -> isSecret(requirements, entry.getKey()) ? "******" : entry.getValue(), (a, b) -> b, () -> new LinkedHashMap<>(actualProperties.size(), 1F)));
    }
    
    private boolean isSecret(final List<AlgorithmPropertyRequirement> requirements, final String propertyKey) {
        return requirements.stream().filter(each -> each.getPropertyKey().equals(propertyKey)).findFirst().map(AlgorithmPropertyRequirement::isSecret).orElse(false);
    }
    
    private List<AlgorithmPropertyRequirement> findTemplate(final String role, final String algorithmType) {
        List<AlgorithmPropertyRequirement> result = new LinkedList<>();
        String actualAlgorithmType = WorkflowSqlUtils.trimToEmpty(algorithmType).toUpperCase(Locale.ENGLISH);
        if (actualAlgorithmType.isEmpty()) {
            return result;
        }
        for (AlgorithmPropertyRequirement each : ENCRYPT_TEMPLATES.getOrDefault(actualAlgorithmType, List.of())) {
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
}
