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

package org.apache.shardingsphere.mcp.feature.sharding.tool.service;

import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Sharding algorithm and key-generator property template service.
 */
public final class ShardingAlgorithmPropertyTemplateService {
    
    /**
     * Find sharding algorithm requirements.
     *
     * @param algorithmType sharding algorithm type
     * @return property requirements
     */
    public List<AlgorithmPropertyRequirement> findAlgorithmRequirements(final String algorithmType) {
        String actualAlgorithmType = Objects.toString(algorithmType, "").trim().toUpperCase(Locale.ENGLISH);
        if ("MOD".equals(actualAlgorithmType) || "HASH_MOD".equals(actualAlgorithmType)) {
            return List.of(new AlgorithmPropertyRequirement("primary", "sharding-count", true, false, "Number of shards.", ""));
        }
        if ("INLINE".equals(actualAlgorithmType) || "COMPLEX_INLINE".equals(actualAlgorithmType) || "HINT_INLINE".equals(actualAlgorithmType)) {
            return List.of(new AlgorithmPropertyRequirement("primary", "algorithm-expression", true, false, "Inline sharding expression.", ""));
        }
        return List.of();
    }
    
    /**
     * Find key-generator requirements.
     *
     * @param keyGeneratorType key-generator type
     * @return property requirements
     */
    public List<AlgorithmPropertyRequirement> findKeyGeneratorRequirements(final String keyGeneratorType) {
        String actualKeyGeneratorType = Objects.toString(keyGeneratorType, "").trim().toUpperCase(Locale.ENGLISH);
        if (!"SNOWFLAKE".equals(actualKeyGeneratorType)) {
            return List.of();
        }
        return List.of(
                new AlgorithmPropertyRequirement("key_generator", "worker-id", false, false, "Worker ID for Snowflake when deployment requires a fixed worker.", ""),
                new AlgorithmPropertyRequirement("key_generator", "max-vibration-offset", false, false, "Maximum vibration offset for Snowflake.", ""));
    }
    
    /**
     * Find combined requirements.
     *
     * @param algorithmType sharding algorithm type
     * @param keyGeneratorType key-generator type
     * @return property requirements
     */
    public List<AlgorithmPropertyRequirement> findRequirements(final String algorithmType, final String keyGeneratorType) {
        List<AlgorithmPropertyRequirement> result = new LinkedList<>();
        result.addAll(findAlgorithmRequirements(algorithmType));
        result.addAll(findKeyGeneratorRequirements(keyGeneratorType));
        return result;
    }
}
