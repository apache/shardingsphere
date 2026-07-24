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

import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
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
 * Algorithm recommendation service.
 */
public final class EncryptAlgorithmRecommendationService {
    
    /**
     * Recommend encrypt algorithms.
     *
     * @param request workflow request
     * @param algorithmResult encrypt algorithm query result
     * @param issues workflow issues
     * @return selected candidates
     */
    public List<AlgorithmCandidate> recommendEncryptAlgorithms(final EncryptWorkflowRequest request,
                                                               final WorkflowQueryResult algorithmResult, final List<WorkflowIssue> issues) {
        List<AlgorithmCandidate> result = new LinkedList<>();
        String primaryType = resolvePrimaryEncryptAlgorithm(request, algorithmResult, issues);
        if (!primaryType.isEmpty()) {
            result.add(createEncryptCandidate(EncryptFeatureDefinition.ALGORITHM_ROLE_PRIMARY, primaryType, request));
        }
        if (Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter())) {
            addAssistedQueryCandidate(result, request, algorithmResult, issues);
        }
        if (Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery())) {
            addLikeQueryCandidate(result, request, algorithmResult, issues);
        }
        return result;
    }
    
    private String resolvePrimaryEncryptAlgorithm(final EncryptWorkflowRequest request, final WorkflowQueryResult algorithmResult,
                                                  final List<WorkflowIssue> issues) {
        String actualAlgorithmType = WorkflowAlgorithmUtils.normalizeAlgorithmType(request.getAlgorithmType());
        if (!actualAlgorithmType.isEmpty()) {
            return resolveSpecifiedPrimaryEncryptAlgorithm(request, algorithmResult, issues, actualAlgorithmType);
        }
        if (Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery())) {
            for (Map<String, Object> each : algorithmResult.getRows()) {
                if (Boolean.TRUE.equals(each.get(EncryptFeatureDefinition.ALGORITHM_CAPABILITY_LIKE))) {
                    return WorkflowAlgorithmUtils.getAlgorithmType(each);
                }
            }
        }
        if (WorkflowAlgorithmUtils.containsAlgorithm(algorithmResult.getRows(), "AES")) {
            return "AES";
        }
        return algorithmResult.getRows().isEmpty() ? "" : WorkflowAlgorithmUtils.getAlgorithmType(algorithmResult.getRows().getFirst());
    }
    
    private String resolveSpecifiedPrimaryEncryptAlgorithm(final EncryptWorkflowRequest request, final WorkflowQueryResult algorithmResult,
                                                           final List<WorkflowIssue> issues, final String algorithmType) {
        if (algorithmResult.isAvailabilityConfirmed() && !WorkflowAlgorithmUtils.containsAlgorithm(algorithmResult.getRows(), algorithmType)) {
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", WorkflowLifecycle.STEP_SELECTING_ALGORITHM,
                    String.format("Encrypt algorithm `%s` is not visible from the current Proxy.", algorithmType), "Choose an available encrypt algorithm.", false, Map.of()));
            return "";
        }
        if (Boolean.TRUE.equals(request.getOptions().getRequiresDecrypt()) && isKnownUnsupported(algorithmType, EncryptFeatureDefinition.ALGORITHM_CAPABILITY_DECRYPT)) {
            addSpecifiedCapabilityConflictIssue(issues,
                    String.format("Encrypt algorithm `%s` does not support decrypt but decrypt support is required.", algorithmType),
                    "Choose an algorithm that supports decrypt, such as AES.");
            return "";
        }
        return algorithmType;
    }
    
    private void addAssistedQueryCandidate(final List<AlgorithmCandidate> candidates, final EncryptWorkflowRequest request,
                                           final WorkflowQueryResult algorithmResult, final List<WorkflowIssue> issues) {
        String assistedQueryType = resolveAssistedQueryAlgorithm(request, algorithmResult, issues);
        if (assistedQueryType.isEmpty()) {
            addCapabilityConflictIssue(issues, request.getOptions().getAssistedQueryAlgorithmType(),
                    "No assisted-query algorithm is available for the current requirement.", "Install or specify a supported assisted-query algorithm.");
            return;
        }
        AlgorithmCandidate assistedQueryCandidate = createEncryptCandidate(EncryptFeatureDefinition.ALGORITHM_ROLE_ASSISTED_QUERY, assistedQueryType, request);
        candidates.add(assistedQueryCandidate);
    }
    
    private void addLikeQueryCandidate(final List<AlgorithmCandidate> candidates, final EncryptWorkflowRequest request,
                                       final WorkflowQueryResult algorithmResult, final List<WorkflowIssue> issues) {
        String likeQueryType = resolveLikeQueryAlgorithm(request, algorithmResult, issues);
        if (likeQueryType.isEmpty()) {
            addCapabilityConflictIssue(issues, request.getOptions().getLikeQueryAlgorithmType(),
                    "No like-query algorithm is available for the current requirement.", "Install or specify a supported like-query algorithm.");
            return;
        }
        AlgorithmCandidate likeQueryCandidate = createEncryptCandidate(EncryptFeatureDefinition.ALGORITHM_ROLE_LIKE_QUERY, likeQueryType, request);
        candidates.add(likeQueryCandidate);
    }
    
    private void addCapabilityConflictIssue(final List<WorkflowIssue> issues, final String specifiedAlgorithmType, final String message, final String userAction) {
        if (!specifiedAlgorithmType.isEmpty()) {
            return;
        }
        issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_CAPABILITY_CONFLICT, "error", WorkflowLifecycle.STEP_SELECTING_ALGORITHM, message, userAction, false, Map.of()));
    }
    
    private String resolveAssistedQueryAlgorithm(final EncryptWorkflowRequest request, final WorkflowQueryResult algorithmResult,
                                                 final List<WorkflowIssue> issues) {
        String actualAlgorithmType = WorkflowAlgorithmUtils.normalizeAlgorithmType(request.getOptions().getAssistedQueryAlgorithmType());
        if (!actualAlgorithmType.isEmpty()) {
            return resolveSpecifiedAssistedQueryAlgorithm(algorithmResult, issues, actualAlgorithmType);
        }
        return WorkflowAlgorithmUtils.containsAlgorithm(algorithmResult.getRows(), "MD5") ? "MD5" : "";
    }
    
    private String resolveSpecifiedAssistedQueryAlgorithm(final WorkflowQueryResult algorithmResult, final List<WorkflowIssue> issues, final String algorithmType) {
        if (algorithmResult.isAvailabilityConfirmed() && !WorkflowAlgorithmUtils.containsAlgorithm(algorithmResult.getRows(), algorithmType)) {
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", WorkflowLifecycle.STEP_SELECTING_ALGORITHM,
                    String.format("Assisted-query algorithm `%s` is not visible from the current Proxy.", algorithmType),
                    "Choose an available assisted-query algorithm.", false, Map.of()));
            return "";
        }
        return algorithmType;
    }
    
    private String resolveLikeQueryAlgorithm(final EncryptWorkflowRequest request, final WorkflowQueryResult algorithmResult,
                                             final List<WorkflowIssue> issues) {
        String actualAlgorithmType = WorkflowAlgorithmUtils.normalizeAlgorithmType(request.getOptions().getLikeQueryAlgorithmType());
        if (!actualAlgorithmType.isEmpty()) {
            return resolveSpecifiedLikeQueryAlgorithm(algorithmResult, issues, actualAlgorithmType);
        }
        for (Map<String, Object> each : algorithmResult.getRows()) {
            if (Boolean.TRUE.equals(each.get(EncryptFeatureDefinition.ALGORITHM_CAPABILITY_LIKE))) {
                return WorkflowAlgorithmUtils.getAlgorithmType(each);
            }
        }
        return "";
    }
    
    private String resolveSpecifiedLikeQueryAlgorithm(final WorkflowQueryResult algorithmResult, final List<WorkflowIssue> issues, final String algorithmType) {
        if (algorithmResult.isAvailabilityConfirmed() && !WorkflowAlgorithmUtils.containsAlgorithm(algorithmResult.getRows(), algorithmType)) {
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", WorkflowLifecycle.STEP_SELECTING_ALGORITHM,
                    String.format("LIKE-query algorithm `%s` is not visible from the current Proxy.", algorithmType),
                    "Choose an available LIKE-query algorithm.", false, Map.of()));
            return "";
        }
        if (isKnownUnsupported(algorithmType, EncryptFeatureDefinition.ALGORITHM_CAPABILITY_LIKE)) {
            addSpecifiedCapabilityConflictIssue(issues,
                    String.format("LIKE-query algorithm `%s` does not support LIKE filtering.", algorithmType),
                    "Choose a LIKE-query algorithm that supports LIKE filtering.");
            return "";
        }
        return algorithmType;
    }
    
    private boolean isKnownUnsupported(final String algorithmType, final String capabilityName) {
        return EncryptAlgorithmCatalog.isCapabilityConfirmed(algorithmType) && Boolean.FALSE.equals(findEncryptCapability(algorithmType).get(capabilityName));
    }
    
    private static Map<String, Boolean> findEncryptCapability(final String algorithmType) {
        return EncryptAlgorithmCatalog.findCapability(algorithmType);
    }
    
    private void addSpecifiedCapabilityConflictIssue(final List<WorkflowIssue> issues, final String message, final String userAction) {
        issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_CAPABILITY_CONFLICT, "error", WorkflowLifecycle.STEP_SELECTING_ALGORITHM, message, userAction, false, Map.of()));
    }
    
    private AlgorithmCandidate createEncryptCandidate(final String role, final String algorithmType, final EncryptWorkflowRequest request) {
        Map<String, Boolean> capability = findEncryptCapability(algorithmType);
        return AlgorithmCandidate.builder()
                .algorithmRole(role)
                .algorithmType(algorithmType)
                .supportsDecrypt(capability.get(EncryptFeatureDefinition.ALGORITHM_CAPABILITY_DECRYPT))
                .supportsEquivalentFilter(capability.get(EncryptFeatureDefinition.ALGORITHM_CAPABILITY_EQUIVALENT_FILTER))
                .supportsLike(capability.get(EncryptFeatureDefinition.ALGORITHM_CAPABILITY_LIKE))
                .recommendationScore(calculateEncryptScore(role, capability))
                .recommendationReason(createEncryptReason(role, algorithmType, request))
                .riskNotes(createEncryptRisk(capability))
                .build();
    }
    
    private int calculateEncryptScore(final String role, final Map<String, Boolean> capability) {
        int result = EncryptFeatureDefinition.ALGORITHM_ROLE_PRIMARY.equals(role) ? 100 : 90;
        if (null == capability.get(EncryptFeatureDefinition.ALGORITHM_CAPABILITY_DECRYPT)) {
            result -= 20;
        }
        return result;
    }
    
    private String createEncryptReason(final String role, final String algorithmType, final EncryptWorkflowRequest request) {
        if (EncryptFeatureDefinition.ALGORITHM_ROLE_PRIMARY.equals(role) && "AES".equals(algorithmType)) {
            return Boolean.TRUE.equals(request.getOptions().getRequiresDecrypt())
                    ? "AES is preferred because decrypt support is required."
                    : "AES is the default recommended encryptor.";
        }
        if (EncryptFeatureDefinition.ALGORITHM_ROLE_ASSISTED_QUERY.equals(role) && "MD5".equals(algorithmType)) {
            return "MD5 is recommended for assisted query support.";
        }
        if (EncryptFeatureDefinition.ALGORITHM_ROLE_LIKE_QUERY.equals(role)) {
            return "Selected as a like-query capable algorithm.";
        }
        return request.getAlgorithmType().isEmpty() ? "Recommended by current intent." : "User specified algorithm.";
    }
    
    private String createEncryptRisk(final Map<String, Boolean> capability) {
        return capability.containsValue(null) ? "Capability is discoverable from plugins but not fully confirmed." : "";
    }
    
}
