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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Encrypt algorithm catalog.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptAlgorithmCatalog {
    
    private static final Map<String, EncryptAlgorithmDefinition> DEFINITIONS = createDefinitions();
    
    /**
     * Find encrypt capability map.
     *
     * @param algorithmType algorithm type
     * @return capability map
     */
    public static Map<String, Boolean> findCapability(final String algorithmType) {
        EncryptAlgorithmDefinition definition = DEFINITIONS.get(normalize(algorithmType));
        return null == definition ? createCapability(null, null, null) : definition.createCapability();
    }
    
    /**
     * Judge whether encrypt capability is confirmed by the catalog.
     *
     * @param algorithmType algorithm type
     * @return capability confirmed or not
     */
    public static boolean isCapabilityConfirmed(final String algorithmType) {
        return DEFINITIONS.containsKey(normalize(algorithmType));
    }
    
    /**
     * Find property requirements.
     *
     * @param algorithmRole algorithm role
     * @param algorithmType algorithm type
     * @return property requirements
     */
    public static List<AlgorithmPropertyRequirement> findRequirements(final String algorithmRole, final String algorithmType) {
        EncryptAlgorithmDefinition definition = DEFINITIONS.get(normalize(algorithmType));
        return null == definition ? List.of() : definition.createRequirements(algorithmRole);
    }
    
    /**
     * Get supported fallback algorithm types.
     *
     * @return supported algorithm types
     */
    public static List<String> getSupportedAlgorithmTypes() {
        return new LinkedList<>(DEFINITIONS.keySet());
    }
    
    private static String normalize(final String algorithmType) {
        return Objects.toString(algorithmType, "").trim().toUpperCase(Locale.ENGLISH);
    }
    
    private static Map<String, EncryptAlgorithmDefinition> createDefinitions() {
        Map<String, EncryptAlgorithmDefinition> result = new LinkedHashMap<>(4, 1F);
        result.put("AES", new EncryptAlgorithmDefinition(true, true, false, List.of(
                new AlgorithmPropertyRequirement(EncryptFeatureDefinition.ALGORITHM_ROLE_PRIMARY, "aes-key-value", true, true, "AES secret key.", ""),
                new AlgorithmPropertyRequirement(EncryptFeatureDefinition.ALGORITHM_ROLE_PRIMARY, "digest-algorithm-name", false, false, "Digest algorithm name.", "SHA-1"))));
        result.put("MD5", new EncryptAlgorithmDefinition(false, true, false,
                List.of(new AlgorithmPropertyRequirement(EncryptFeatureDefinition.ALGORITHM_ROLE_PRIMARY, "salt", false, true, "Optional salt.", ""))));
        return result;
    }
    
    private static Map<String, Boolean> createCapability(final Boolean supportsDecrypt, final Boolean supportsEquivalentFilter, final Boolean supportsLike) {
        Map<String, Boolean> result = new LinkedHashMap<>(3, 1F);
        result.put(EncryptFeatureDefinition.ALGORITHM_CAPABILITY_DECRYPT, supportsDecrypt);
        result.put(EncryptFeatureDefinition.ALGORITHM_CAPABILITY_EQUIVALENT_FILTER, supportsEquivalentFilter);
        result.put(EncryptFeatureDefinition.ALGORITHM_CAPABILITY_LIKE, supportsLike);
        return result;
    }
    
    private record EncryptAlgorithmDefinition(Boolean supportsDecrypt, Boolean supportsEquivalentFilter, Boolean supportsLike,
                                              List<AlgorithmPropertyRequirement> propertyRequirements) {
        
        private Map<String, Boolean> createCapability() {
            return EncryptAlgorithmCatalog.createCapability(supportsDecrypt, supportsEquivalentFilter, supportsLike);
        }
        
        private List<AlgorithmPropertyRequirement> createRequirements(final String algorithmRole) {
            List<AlgorithmPropertyRequirement> result = new LinkedList<>();
            for (AlgorithmPropertyRequirement each : propertyRequirements) {
                result.add(new AlgorithmPropertyRequirement(algorithmRole, each.getPropertyKey(), each.isRequired(), each.isSecret(), each.getDescription(), each.getDefaultValue()));
            }
            return result;
        }
    }
}
