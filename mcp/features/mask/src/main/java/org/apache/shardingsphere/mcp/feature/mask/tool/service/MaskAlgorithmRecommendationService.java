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

import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Mask algorithm recommendation service.
 */
public final class MaskAlgorithmRecommendationService {
    
    private static final Set<String> KNOWN_MASK_ALGORITHMS = Set.of(
            "MD5", "KEEP_FIRST_N_LAST_M", "KEEP_FROM_X_TO_Y", "MASK_FIRST_N_LAST_M", "MASK_FROM_X_TO_Y",
            "MASK_AFTER_SPECIAL_CHARS", "MASK_BEFORE_SPECIAL_CHARS", "GENERIC_TABLE_RANDOM_REPLACE");
    
    /**
     * Judge whether mask algorithm is known built-in.
     *
     * @param algorithmType algorithm type
     * @return known built-in or not
     */
    public static boolean isKnownMaskAlgorithm(final String algorithmType) {
        return KNOWN_MASK_ALGORITHMS.contains(algorithmType);
    }
    
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
        String actualAlgorithmType = WorkflowSqlUtils.trimToEmpty(request.getAlgorithmType()).toUpperCase(Locale.ENGLISH);
        if (!actualAlgorithmType.isEmpty()) {
            if (containsAlgorithm(maskAlgorithms, actualAlgorithmType)) {
                return List.of(new AlgorithmCandidate("primary", actualAlgorithmType, detectSource(actualAlgorithmType), null, null, null, 100, "User specified algorithm.", ""));
            }
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm",
                    String.format("Mask algorithm `%s` is not visible from the current Proxy.", actualAlgorithmType), "Choose an available mask algorithm.", false, Map.of()));
            return List.of();
        }
        String recommendedType = resolveRecommendedAlgorithm(intent, request, maskAlgorithms);
        if (recommendedType.isEmpty()) {
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm",
                    "No mask algorithm is available from the current Proxy.", "Install or register at least one visible mask algorithm.", false, Map.of()));
            return List.of();
        }
        return List.of(new AlgorithmCandidate("primary", recommendedType, detectSource(recommendedType), null, null, null, 100,
                "Recommended by field semantics and current algorithm availability.", ""));
    }
    
    private String resolveRecommendedAlgorithm(final ClarifiedIntent intent, final WorkflowRequest request, final List<Map<String, Object>> maskAlgorithms) {
        String fieldSemantics = WorkflowSqlUtils.trimToEmpty(intent.getFieldSemantics()).toLowerCase(Locale.ENGLISH);
        String naturalLanguageIntent = WorkflowSqlUtils.trimToEmpty(request.getNaturalLanguageIntent()).toLowerCase(Locale.ENGLISH);
        if ((fieldSemantics.contains("phone") || naturalLanguageIntent.contains("前") || naturalLanguageIntent.contains("后"))
                && containsAlgorithm(maskAlgorithms, "MASK_FROM_X_TO_Y")) {
            return "MASK_FROM_X_TO_Y";
        }
        if (containsAlgorithm(maskAlgorithms, "KEEP_FIRST_N_LAST_M")) {
            return "KEEP_FIRST_N_LAST_M";
        }
        return maskAlgorithms.isEmpty() ? "" : String.valueOf(maskAlgorithms.get(0).get("type")).toUpperCase(Locale.ENGLISH);
    }
    
    private boolean containsAlgorithm(final List<Map<String, Object>> algorithmRows, final String algorithmType) {
        return algorithmRows.stream().map(each -> String.valueOf(each.get("type")).toUpperCase(Locale.ENGLISH)).anyMatch(algorithmType::equals);
    }
    
    private String detectSource(final String algorithmType) {
        return isKnownMaskAlgorithm(algorithmType) ? "builtin" : "custom-spi";
    }
}
