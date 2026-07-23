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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service;

import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowQueryResult;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowAlgorithmUtils;

import java.util.List;
import java.util.Map;

/**
 * Readwrite-splitting load-balance algorithm recommendation service.
 */
public final class ReadwriteSplittingAlgorithmRecommendationService {
    
    /**
     * Recommend load-balance algorithms.
     *
     * @param request workflow request
     * @param algorithmResult load-balance algorithm query result
     * @param issues workflow issues
     * @return selected candidates
     */
    public List<AlgorithmCandidate> recommendLoadBalanceAlgorithms(final ReadwriteSplittingRuleWorkflowRequest request,
                                                                   final WorkflowQueryResult algorithmResult, final List<WorkflowIssue> issues) {
        String actualAlgorithmType = WorkflowAlgorithmUtils.normalizeAlgorithmType(request.getLoadBalancerType());
        if (!actualAlgorithmType.isEmpty()) {
            if (!algorithmResult.isAvailabilityConfirmed() || WorkflowAlgorithmUtils.containsAlgorithm(algorithmResult.getRows(), actualAlgorithmType, "type", "name")) {
                return List.of(createCandidate(actualAlgorithmType, 100, "User specified load-balance algorithm."));
            }
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", WorkflowLifecycle.STEP_SELECTING_ALGORITHM,
                    String.format("Load-balance algorithm `%s` is not visible from the current Proxy.", actualAlgorithmType),
                    "Choose an available load-balance algorithm.", false, Map.of()));
            return List.of();
        }
        String recommendedType = resolveRecommendedAlgorithm(algorithmResult.getRows());
        if (recommendedType.isEmpty()) {
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", WorkflowLifecycle.STEP_SELECTING_ALGORITHM,
                    "No load-balance algorithm is available from the current Proxy.", "Install or expose at least one load-balance algorithm.", false, Map.of()));
            return List.of();
        }
        return List.of(createCandidate(recommendedType, 90, "Recommended from current load-balance algorithm availability."));
    }
    
    private AlgorithmCandidate createCandidate(final String algorithmType, final int score, final String reason) {
        return AlgorithmCandidate.builder().algorithmRole("primary").algorithmType(algorithmType).recommendationScore(score).recommendationReason(reason).riskNotes("").build();
    }
    
    private String resolveRecommendedAlgorithm(final List<Map<String, Object>> algorithmRows) {
        for (String each : List.of("ROUND_ROBIN", "RANDOM", "WEIGHT")) {
            if (WorkflowAlgorithmUtils.containsAlgorithm(algorithmRows, each, "type", "name")) {
                return each;
            }
        }
        return algorithmRows.isEmpty() ? "" : WorkflowAlgorithmUtils.getAlgorithmType(algorithmRows.getFirst(), "type", "name");
    }
}
