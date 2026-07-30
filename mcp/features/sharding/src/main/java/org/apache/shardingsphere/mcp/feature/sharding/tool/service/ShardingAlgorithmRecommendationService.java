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
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowQueryResult;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowAlgorithmUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Sharding algorithm and key-generator recommendation service.
 */
public final class ShardingAlgorithmRecommendationService {
    
    /**
     * Recommend sharding algorithm and key generator.
     *
     * @param request workflow request
     * @param algorithmResult algorithm query result
     * @param keyGeneratorResult key-generator query result
     * @param includeShardingAlgorithm whether to include sharding algorithm recommendation
     * @param includeKeyGenerator whether to include key-generator recommendation
     * @param issues workflow issues
     * @return selected candidates
     */
    public List<AlgorithmCandidate> recommend(final ShardingWorkflowRequest request, final WorkflowQueryResult algorithmResult,
                                              final WorkflowQueryResult keyGeneratorResult, final boolean includeShardingAlgorithm,
                                              final boolean includeKeyGenerator, final List<WorkflowIssue> issues) {
        List<AlgorithmCandidate> result = new LinkedList<>();
        if (includeShardingAlgorithm) {
            recommendAlgorithm(request.getAlgorithmType(), algorithmResult, issues).forEach(result::add);
        }
        if (includeKeyGenerator) {
            recommendKeyGenerator(request.getKeyGeneratorType(), keyGeneratorResult, issues).forEach(result::add);
        }
        return result;
    }
    
    private List<AlgorithmCandidate> recommendAlgorithm(final String algorithmType, final WorkflowQueryResult algorithmResult, final List<WorkflowIssue> issues) {
        String actualAlgorithmType = WorkflowAlgorithmUtils.normalizeAlgorithmType(algorithmType);
        if (!actualAlgorithmType.isEmpty()) {
            if (!algorithmResult.isAvailabilityConfirmed() || WorkflowAlgorithmUtils.containsAlgorithm(algorithmResult.getRows(), actualAlgorithmType, "type", "name")) {
                return List.of(createCandidate("primary", actualAlgorithmType, 100, "User specified sharding algorithm."));
            }
            addAlgorithmNotFoundIssue(issues, "Sharding algorithm", actualAlgorithmType);
            return List.of();
        }
        String recommendedType = resolveRecommendedAlgorithm(algorithmResult.getRows(), List.of("INLINE", "MOD", "HASH_MOD"));
        if (recommendedType.isEmpty()) {
            addNoAlgorithmIssue(issues, "sharding algorithm");
            return List.of();
        }
        return List.of(createCandidate("primary", recommendedType, 90, "Recommended from current sharding algorithm availability."));
    }
    
    private List<AlgorithmCandidate> recommendKeyGenerator(final String keyGeneratorType, final WorkflowQueryResult keyGeneratorResult, final List<WorkflowIssue> issues) {
        String actualKeyGeneratorType = WorkflowAlgorithmUtils.normalizeAlgorithmType(keyGeneratorType);
        if (!actualKeyGeneratorType.isEmpty()) {
            if (!keyGeneratorResult.isAvailabilityConfirmed()
                    || WorkflowAlgorithmUtils.containsAlgorithm(keyGeneratorResult.getRows(), actualKeyGeneratorType, "type", "name")) {
                return List.of(createCandidate("key_generator", actualKeyGeneratorType, 100, "User specified key-generator algorithm."));
            }
            addAlgorithmNotFoundIssue(issues, "Key-generator algorithm", actualKeyGeneratorType);
            return List.of();
        }
        String recommendedType = resolveRecommendedAlgorithm(keyGeneratorResult.getRows(), List.of("SNOWFLAKE", "UUID"));
        if (recommendedType.isEmpty()) {
            addNoAlgorithmIssue(issues, "key-generator algorithm");
            return List.of();
        }
        return List.of(createCandidate("key_generator", recommendedType, 90, "Recommended from current key-generator availability."));
    }
    
    private AlgorithmCandidate createCandidate(final String role, final String algorithmType, final int score, final String reason) {
        return AlgorithmCandidate.builder().algorithmRole(role).algorithmType(algorithmType).recommendationScore(score).recommendationReason(reason).riskNotes("").build();
    }
    
    private String resolveRecommendedAlgorithm(final List<Map<String, Object>> algorithmRows, final List<String> preferredTypes) {
        for (String each : preferredTypes) {
            if (WorkflowAlgorithmUtils.containsAlgorithm(algorithmRows, each, "type", "name")) {
                return each;
            }
        }
        return algorithmRows.isEmpty() ? "" : WorkflowAlgorithmUtils.getAlgorithmType(algorithmRows.getFirst(), "type", "name");
    }
    
    private void addAlgorithmNotFoundIssue(final List<WorkflowIssue> issues, final String label, final String algorithmType) {
        issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", WorkflowLifecycle.STEP_SELECTING_ALGORITHM,
                String.format("%s `%s` is not visible from the current Proxy.", label, algorithmType),
                "Choose an available algorithm.", false, Map.of()));
    }
    
    private void addNoAlgorithmIssue(final List<WorkflowIssue> issues, final String label) {
        issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", WorkflowLifecycle.STEP_SELECTING_ALGORITHM,
                String.format("No %s is available from the current Proxy.", label), "Install or expose an available algorithm.", false, Map.of()));
    }
}
