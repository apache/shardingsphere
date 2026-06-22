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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Readwrite-splitting load-balance algorithm recommendation service.
 */
public final class ReadwriteSplittingAlgorithmRecommendationService {
    
    /**
     * Recommend load-balance algorithms.
     *
     * @param request workflow request
     * @param algorithmRows load-balance algorithm rows
     * @param issues workflow issues
     * @return selected candidates
     */
    public List<AlgorithmCandidate> recommendLoadBalanceAlgorithms(final ReadwriteSplittingRuleWorkflowRequest request,
                                                                   final List<Map<String, Object>> algorithmRows, final List<WorkflowIssue> issues) {
        List<Map<String, Object>> actualAlgorithmRows = null == algorithmRows ? List.of() : algorithmRows;
        String actualAlgorithmType = request.getLoadBalancerType().toUpperCase(Locale.ENGLISH);
        if (!actualAlgorithmType.isEmpty()) {
            if (actualAlgorithmRows.isEmpty() || containsAlgorithm(actualAlgorithmRows, actualAlgorithmType)) {
                return List.of(createCandidate(actualAlgorithmType, 100, "User specified load-balance algorithm."));
            }
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm",
                    String.format("Load-balance algorithm `%s` is not visible from the current Proxy.", actualAlgorithmType),
                    "Choose an available load-balance algorithm.", false, Map.of()));
            return List.of();
        }
        String recommendedType = resolveRecommendedAlgorithm(actualAlgorithmRows);
        if (recommendedType.isEmpty()) {
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm",
                    "No load-balance algorithm is available from the current Proxy.", "Install or expose at least one load-balance algorithm.", false, Map.of()));
            return List.of();
        }
        return List.of(createCandidate(recommendedType, 90, "Recommended from current load-balance algorithm availability."));
    }
    
    private AlgorithmCandidate createCandidate(final String algorithmType, final int score, final String reason) {
        return new AlgorithmCandidate("primary", algorithmType, null, null, null, score, reason, "");
    }
    
    private String resolveRecommendedAlgorithm(final List<Map<String, Object>> algorithmRows) {
        for (String each : List.of("ROUND_ROBIN", "RANDOM", "WEIGHT")) {
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
}
