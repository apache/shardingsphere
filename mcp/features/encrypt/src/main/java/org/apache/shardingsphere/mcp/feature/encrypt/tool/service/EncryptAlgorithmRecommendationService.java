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

import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Algorithm recommendation service.
 */
public final class EncryptAlgorithmRecommendationService {
    
    private static final Map<String, Boolean> UNKNOWN_ENCRYPT_CAPABILITY = createUnknownEncryptCapability();
    
    private static final Set<String> KNOWN_ENCRYPT_ALGORITHMS = Set.of("AES", "MD5");
    
    private static final Map<String, Map<String, Boolean>> ENCRYPT_CAPABILITIES = createEncryptCapabilities();
    
    /**
     * Judge whether encrypt algorithm is known built-in.
     *
     * @param algorithmType algorithm type
     * @return known built-in or not
     */
    public static boolean isKnownEncryptAlgorithm(final String algorithmType) {
        return KNOWN_ENCRYPT_ALGORITHMS.contains(algorithmType);
    }
    
    /**
     * Find encrypt capability map.
     *
     * @param algorithmType algorithm type
     * @return capability map
     */
    public static Map<String, Boolean> findEncryptCapability(final String algorithmType) {
        return ENCRYPT_CAPABILITIES.getOrDefault(algorithmType, UNKNOWN_ENCRYPT_CAPABILITY);
    }
    
    /**
     * Recommend encrypt algorithms.
     *
     * @param request workflow request
     * @param encryptAlgorithms encrypt algorithm plugins
     * @param issues workflow issues
     * @return selected candidates
     */
    public List<AlgorithmCandidate> recommendEncryptAlgorithms(final EncryptWorkflowRequest request,
                                                               final List<Map<String, Object>> encryptAlgorithms, final List<WorkflowIssue> issues) {
        List<AlgorithmCandidate> result = new LinkedList<>();
        String primaryType = resolvePrimaryEncryptAlgorithm(request, encryptAlgorithms, issues);
        if (!WorkflowSqlUtils.trimToEmpty(primaryType).isEmpty()) {
            AlgorithmCandidate primaryCandidate = createEncryptCandidate("primary", primaryType, request);
            result.add(primaryCandidate);
            addCustomCapabilityWarning(primaryCandidate, issues);
        }
        if (Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter())) {
            addAssistedQueryCandidate(result, request, encryptAlgorithms, issues);
        }
        if (Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery())) {
            addLikeQueryCandidate(result, request, encryptAlgorithms, issues);
        }
        return result;
    }
    
    private String resolvePrimaryEncryptAlgorithm(final EncryptWorkflowRequest request, final List<Map<String, Object>> encryptAlgorithms,
                                                  final List<WorkflowIssue> issues) {
        String actualAlgorithmType = WorkflowSqlUtils.trimToEmpty(request.getAlgorithmType()).toUpperCase(Locale.ENGLISH);
        if (!actualAlgorithmType.isEmpty()) {
            if (containsAlgorithm(encryptAlgorithms, actualAlgorithmType)) {
                return actualAlgorithmType;
            }
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm",
                    String.format("Encrypt algorithm `%s` is not visible from the current Proxy.", actualAlgorithmType), "Choose an available encrypt algorithm.", false, Map.of()));
            return "";
        }
        if (Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery())) {
            for (Map<String, Object> each : encryptAlgorithms) {
                if (Boolean.TRUE.equals(each.get("supports_like"))) {
                    return String.valueOf(each.get("type")).toUpperCase(Locale.ENGLISH);
                }
            }
        }
        if (containsAlgorithm(encryptAlgorithms, "AES")) {
            return "AES";
        }
        return encryptAlgorithms.isEmpty() ? "" : String.valueOf(encryptAlgorithms.get(0).get("type")).toUpperCase(Locale.ENGLISH);
    }
    
    private void addAssistedQueryCandidate(final List<AlgorithmCandidate> result, final EncryptWorkflowRequest request,
                                           final List<Map<String, Object>> encryptAlgorithms, final List<WorkflowIssue> issues) {
        String assistedQueryType = resolveAssistedQueryAlgorithm(request, encryptAlgorithms, issues);
        if (assistedQueryType.isEmpty()) {
            addCapabilityConflictIssue(issues, request.getOptions().getAssistedQueryAlgorithmType(),
                    "No assisted-query algorithm is available for the current requirement.", "Install or specify a supported assisted-query algorithm.");
            return;
        }
        AlgorithmCandidate assistedQueryCandidate = createEncryptCandidate("assisted_query", assistedQueryType, request);
        result.add(assistedQueryCandidate);
        addCustomCapabilityWarning(assistedQueryCandidate, issues);
    }
    
    private void addLikeQueryCandidate(final List<AlgorithmCandidate> result, final EncryptWorkflowRequest request,
                                       final List<Map<String, Object>> encryptAlgorithms, final List<WorkflowIssue> issues) {
        String likeQueryType = resolveLikeQueryAlgorithm(request, encryptAlgorithms, issues);
        if (likeQueryType.isEmpty()) {
            addCapabilityConflictIssue(issues, request.getOptions().getLikeQueryAlgorithmType(),
                    "No like-query algorithm is available for the current requirement.", "Install or specify a supported like-query algorithm.");
            return;
        }
        AlgorithmCandidate likeQueryCandidate = createEncryptCandidate("like_query", likeQueryType, request);
        result.add(likeQueryCandidate);
        addCustomCapabilityWarning(likeQueryCandidate, issues);
    }
    
    private void addCapabilityConflictIssue(final List<WorkflowIssue> issues, final String specifiedAlgorithmType, final String message, final String userAction) {
        if (!WorkflowSqlUtils.trimToEmpty(specifiedAlgorithmType).isEmpty()) {
            return;
        }
        issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_CAPABILITY_CONFLICT, "error", "selecting-algorithm", message, userAction, false, Map.of()));
    }
    
    private String resolveAssistedQueryAlgorithm(final EncryptWorkflowRequest request, final List<Map<String, Object>> encryptAlgorithms,
                                                 final List<WorkflowIssue> issues) {
        String actualAlgorithmType = WorkflowSqlUtils.trimToEmpty(request.getOptions().getAssistedQueryAlgorithmType()).toUpperCase(Locale.ENGLISH);
        if (!actualAlgorithmType.isEmpty()) {
            if (containsAlgorithm(encryptAlgorithms, actualAlgorithmType)) {
                return actualAlgorithmType;
            }
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm",
                    String.format("Assisted-query algorithm `%s` is not visible from the current Proxy.", actualAlgorithmType),
                    "Choose an available assisted-query algorithm.", false, Map.of()));
            return "";
        }
        return containsAlgorithm(encryptAlgorithms, "MD5") ? "MD5" : "";
    }
    
    private String resolveLikeQueryAlgorithm(final EncryptWorkflowRequest request, final List<Map<String, Object>> encryptAlgorithms,
                                             final List<WorkflowIssue> issues) {
        String actualAlgorithmType = WorkflowSqlUtils.trimToEmpty(request.getOptions().getLikeQueryAlgorithmType()).toUpperCase(Locale.ENGLISH);
        if (!actualAlgorithmType.isEmpty()) {
            if (containsAlgorithm(encryptAlgorithms, actualAlgorithmType)) {
                return actualAlgorithmType;
            }
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm",
                    String.format("LIKE-query algorithm `%s` is not visible from the current Proxy.", actualAlgorithmType),
                    "Choose an available LIKE-query algorithm.", false, Map.of()));
            return "";
        }
        for (Map<String, Object> each : encryptAlgorithms) {
            if (Boolean.TRUE.equals(each.get("supports_like"))) {
                return String.valueOf(each.get("type")).toUpperCase(Locale.ENGLISH);
            }
        }
        return "";
    }
    
    private boolean containsAlgorithm(final List<Map<String, Object>> algorithmRows, final String algorithmType) {
        return algorithmRows.stream().map(each -> String.valueOf(each.get("type")).toUpperCase(Locale.ENGLISH)).anyMatch(algorithmType::equals);
    }
    
    private AlgorithmCandidate createEncryptCandidate(final String role, final String algorithmType, final EncryptWorkflowRequest request) {
        Map<String, Boolean> capability = findEncryptCapability(algorithmType);
        return new AlgorithmCandidate(role, algorithmType, detectSource(algorithmType, true), capability.get("supports_decrypt"), capability.get("supports_equivalent_filter"),
                capability.get("supports_like"), calculateEncryptScore(role, capability), createEncryptReason(role, algorithmType, request), createEncryptRisk(role, capability));
    }
    
    private int calculateEncryptScore(final String role, final Map<String, Boolean> capability) {
        int result = "primary".equals(role) ? 100 : 90;
        if (null == capability.get("supports_decrypt")) {
            result -= 20;
        }
        return result;
    }
    
    private String createEncryptReason(final String role, final String algorithmType, final EncryptWorkflowRequest request) {
        if ("primary".equals(role) && "AES".equals(algorithmType)) {
            return Boolean.TRUE.equals(request.getOptions().getRequiresDecrypt())
                    ? "AES is preferred because decrypt support is required."
                    : "AES is the default recommended encryptor.";
        }
        if ("assisted_query".equals(role) && "MD5".equals(algorithmType)) {
            return "MD5 is recommended for assisted query support.";
        }
        if ("like_query".equals(role)) {
            return "Selected as a like-query capable algorithm.";
        }
        return WorkflowSqlUtils.trimToEmpty(request.getAlgorithmType()).isEmpty() ? "Recommended by current intent." : "User specified algorithm.";
    }
    
    private String createEncryptRisk(final String role, final Map<String, Boolean> capability) {
        if (null == capability.get("supports_decrypt") || null == capability.get("supports_equivalent_filter") || null == capability.get("supports_like")) {
            return "Capability is discoverable from plugins but not fully confirmed.";
        }
        return "";
    }
    
    private String detectSource(final String algorithmType, final boolean encrypt) {
        return encrypt && isKnownEncryptAlgorithm(algorithmType) ? "builtin" : "custom-spi";
    }
    
    private void addCustomCapabilityWarning(final AlgorithmCandidate algorithmCandidate, final List<WorkflowIssue> issues) {
        if (!"custom-spi".equals(algorithmCandidate.getSource()) || algorithmCandidate.getRiskNotes().isEmpty()) {
            return;
        }
        issues.add(new WorkflowIssue(WorkflowIssueCode.CUSTOM_ALGORITHM_CAPABILITY_UNCONFIRMED, "warning", "selecting-algorithm",
                String.format("Capability for custom algorithm `%s` cannot be fully confirmed from SPI metadata only.", algorithmCandidate.getAlgorithmType()),
                "Review plugin capability and validate after execution.", true, Map.of("algorithm_role", algorithmCandidate.getAlgorithmRole(),
                        "algorithm_type", algorithmCandidate.getAlgorithmType())));
    }
    
    private static Map<String, Map<String, Boolean>> createEncryptCapabilities() {
        Map<String, Map<String, Boolean>> result = new LinkedHashMap<>(4, 1F);
        result.put("AES", Map.of("supports_decrypt", true, "supports_equivalent_filter", true, "supports_like", false));
        result.put("MD5", Map.of("supports_decrypt", false, "supports_equivalent_filter", true, "supports_like", false));
        return result;
    }
    
    private static Map<String, Boolean> createUnknownEncryptCapability() {
        Map<String, Boolean> result = new LinkedHashMap<>(3, 1F);
        result.put("supports_decrypt", null);
        result.put("supports_equivalent_filter", null);
        result.put("supports_like", null);
        return result;
    }
}
