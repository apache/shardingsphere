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

import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowAlgorithmUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Mask algorithm recommendation service.
 */
public final class MaskAlgorithmRecommendationService {
    
    /**
     * Recommend mask algorithm.
     *
     * @param intent clarified intent
     * @param request workflow request
     * @param maskAlgorithms mask algorithm plugins
     * @param issues workflow issues
     * @return selected candidates
     */
    public List<AlgorithmCandidate> recommendMaskAlgorithms(final ClarifiedIntent intent, final WorkflowRequest request,
                                                            final List<Map<String, Object>> maskAlgorithms, final List<WorkflowIssue> issues) {
        String actualAlgorithmType = WorkflowAlgorithmUtils.normalizeAlgorithmType(request.getAlgorithmType());
        if (!actualAlgorithmType.isEmpty()) {
            if (WorkflowAlgorithmUtils.containsAlgorithm(maskAlgorithms, actualAlgorithmType)) {
                return List.of(AlgorithmCandidate.builder().algorithmRole("primary").algorithmType(actualAlgorithmType).recommendationScore(100)
                        .recommendationReason("User specified algorithm.").riskNotes("").build());
            }
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", WorkflowLifecycle.STEP_SELECTING_ALGORITHM,
                    String.format("Mask algorithm `%s` is not visible from the current Proxy.", actualAlgorithmType), "Choose an available mask algorithm.", false, Map.of()));
            return List.of();
        }
        String recommendedType = resolveRecommendedAlgorithm(intent, request, maskAlgorithms);
        if (recommendedType.isEmpty()) {
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", WorkflowLifecycle.STEP_SELECTING_ALGORITHM,
                    "No mask algorithm is available from the current Proxy.", "Install or register at least one visible mask algorithm.", false, Map.of()));
            return List.of();
        }
        return List.of(AlgorithmCandidate.builder().algorithmRole("primary").algorithmType(recommendedType).recommendationScore(100)
                .recommendationReason("Recommended by field semantics and current algorithm availability.").riskNotes("").build());
    }
    
    private String resolveRecommendedAlgorithm(final ClarifiedIntent intent, final WorkflowRequest request, final List<Map<String, Object>> maskAlgorithms) {
        String fieldSemantics = intent.getFieldSemantics().toLowerCase(Locale.ENGLISH);
        String naturalLanguageIntent = request.getNaturalLanguageIntent().toLowerCase(Locale.ENGLISH);
        if ((fieldSemantics.contains("phone") || containsAny(naturalLanguageIntent, "first", "last", "前", "后", "保留"))
                && WorkflowAlgorithmUtils.containsAlgorithm(maskAlgorithms, "MASK_FROM_X_TO_Y")) {
            return "MASK_FROM_X_TO_Y";
        }
        if (WorkflowAlgorithmUtils.containsAlgorithm(maskAlgorithms, "KEEP_FIRST_N_LAST_M")) {
            return "KEEP_FIRST_N_LAST_M";
        }
        return maskAlgorithms.isEmpty() ? "" : WorkflowAlgorithmUtils.getAlgorithmType(maskAlgorithms.get(0));
    }
    
    private boolean containsAny(final String value, final String... candidates) {
        for (String each : candidates) {
            if (value.contains(each)) {
                return true;
            }
        }
        return false;
    }
    
}
