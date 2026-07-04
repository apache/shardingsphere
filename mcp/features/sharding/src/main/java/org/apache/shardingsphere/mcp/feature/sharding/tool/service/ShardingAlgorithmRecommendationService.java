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

import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Sharding algorithm and key-generator recommendation service.
 */
public final class ShardingAlgorithmRecommendationService {
    
    /**
     * Recommend sharding algorithm and key generator.
     *
     * @param request workflow request
     * @param algorithmRows algorithm rows
     * @param keyGeneratorRows key-generator rows
     * @param includeShardingAlgorithm whether to include sharding algorithm recommendation
     * @param includeKeyGenerator whether to include key-generator recommendation
     * @param issues workflow issues
     * @return selected candidates
     */
    public List<AlgorithmCandidate> recommend(final ShardingWorkflowRequest request, final List<Map<String, Object>> algorithmRows,
                                              final List<Map<String, Object>> keyGeneratorRows, final boolean includeShardingAlgorithm,
                                              final boolean includeKeyGenerator, final List<WorkflowIssue> issues) {
        List<Map<String, Object>> actualAlgorithmRows = null == algorithmRows ? List.of() : algorithmRows;
        List<Map<String, Object>> actualKeyGeneratorRows = null == keyGeneratorRows ? List.of() : keyGeneratorRows;
        List<AlgorithmCandidate> result = new LinkedList<>();
        if (includeShardingAlgorithm) {
            recommendAlgorithm(request.getAlgorithmType(), actualAlgorithmRows, issues).forEach(result::add);
        }
        if (includeKeyGenerator) {
            recommendKeyGenerator(resolveKeyGeneratorType(request), actualKeyGeneratorRows, issues).forEach(result::add);
        }
        return result;
    }
    
    private List<AlgorithmCandidate> recommendAlgorithm(final String algorithmType, final List<Map<String, Object>> algorithmRows, final List<WorkflowIssue> issues) {
        String actualAlgorithmType = Objects.toString(algorithmType, "").trim().toUpperCase(Locale.ENGLISH);
        if (!actualAlgorithmType.isEmpty()) {
            if (algorithmRows.isEmpty() || containsAlgorithm(algorithmRows, actualAlgorithmType)) {
                return List.of(createCandidate("primary", actualAlgorithmType, 100, "User specified sharding algorithm."));
            }
            addAlgorithmNotFoundIssue(issues, "Sharding algorithm", actualAlgorithmType);
            return List.of();
        }
        String recommendedType = resolveRecommendedAlgorithm(algorithmRows, List.of("INLINE", "MOD", "HASH_MOD"));
        if (recommendedType.isEmpty()) {
            addNoAlgorithmIssue(issues, "sharding algorithm");
            return List.of();
        }
        return List.of(createCandidate("primary", recommendedType, 90, "Recommended from current sharding algorithm availability."));
    }
    
    private List<AlgorithmCandidate> recommendKeyGenerator(final String keyGeneratorType, final List<Map<String, Object>> keyGeneratorRows, final List<WorkflowIssue> issues) {
        String actualKeyGeneratorType = Objects.toString(keyGeneratorType, "").trim().toUpperCase(Locale.ENGLISH);
        if (!actualKeyGeneratorType.isEmpty()) {
            if (keyGeneratorRows.isEmpty() || containsAlgorithm(keyGeneratorRows, actualKeyGeneratorType)) {
                return List.of(createCandidate("key_generator", actualKeyGeneratorType, 100, "User specified key-generator algorithm."));
            }
            addAlgorithmNotFoundIssue(issues, "Key-generator algorithm", actualKeyGeneratorType);
            return List.of();
        }
        String recommendedType = resolveRecommendedAlgorithm(keyGeneratorRows, List.of("SNOWFLAKE", "UUID"));
        if (recommendedType.isEmpty()) {
            addNoAlgorithmIssue(issues, "key-generator algorithm");
            return List.of();
        }
        return List.of(createCandidate("key_generator", recommendedType, 90, "Recommended from current key-generator availability."));
    }
    
    private String resolveKeyGeneratorType(final ShardingWorkflowRequest request) {
        return request.getKeyGeneratorType();
    }
    
    private AlgorithmCandidate createCandidate(final String role, final String algorithmType, final int score, final String reason) {
        return new AlgorithmCandidate(role, algorithmType, null, null, null, score, reason, "");
    }
    
    private String resolveRecommendedAlgorithm(final List<Map<String, Object>> algorithmRows, final List<String> preferredTypes) {
        for (String each : preferredTypes) {
            if (containsAlgorithm(algorithmRows, each)) {
                return each;
            }
        }
        return algorithmRows.isEmpty() ? "" : getAlgorithmType(algorithmRows.getFirst());
    }
    
    private boolean containsAlgorithm(final List<Map<String, Object>> algorithmRows, final String algorithmType) {
        return algorithmRows.stream().map(this::getAlgorithmType).anyMatch(algorithmType::equals);
    }
    
    private String getAlgorithmType(final Map<String, Object> algorithmRow) {
        return Objects.toString(algorithmRow.getOrDefault("type", algorithmRow.getOrDefault("name", "")), "").trim().toUpperCase(Locale.ENGLISH);
    }
    
    private void addAlgorithmNotFoundIssue(final List<WorkflowIssue> issues, final String label, final String algorithmType) {
        issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm",
                String.format("%s `%s` is not visible from the current Proxy.", label, algorithmType),
                "Choose an available algorithm.", false, Map.of()));
    }
    
    private void addNoAlgorithmIssue(final List<WorkflowIssue> issues, final String label) {
        issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm",
                String.format("No %s is available from the current Proxy.", label), "Install or expose an available algorithm.", false, Map.of()));
    }
}
