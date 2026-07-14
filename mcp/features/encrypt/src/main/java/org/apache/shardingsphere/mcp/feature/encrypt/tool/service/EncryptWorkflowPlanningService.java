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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowState;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningSupport;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Encrypt workflow planning service.
 */
public final class EncryptWorkflowPlanningService {
    
    private static final List<String> SUPPORTED_OPERATION_TYPES = List.of(WorkflowLifecycle.OPERATION_CREATE, WorkflowLifecycle.OPERATION_DROP);
    
    private static final List<String> INTERACTION_STEPS = List.of(
            "Confirm database, table, column and target operation",
            "Inspect existing encrypt rules and algorithm metadata",
            "Clarify missing encrypt requirements and choose algorithms",
            "Collect explicit rule column names and algorithm properties",
            "Generate rule DistSQL artifacts",
            "Review artifacts and choose execution mode",
            "Execute or export rule DistSQL artifacts",
            "Validate rule state and summarize");
    
    private static final List<String> VALIDATION_LAYERS = List.of("rules");
    
    private final WorkflowPlanningSupport planningSupport;
    
    private final EncryptWorkflowIntentResolver intentResolver;
    
    private final EncryptRuleInspectionService ruleInspectionService;
    
    private final EncryptAlgorithmRecommendationService algorithmRecommendationService;
    
    private final EncryptAlgorithmPropertyTemplateService algorithmPropertyTemplateService;
    
    private final EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService;
    
    public EncryptWorkflowPlanningService() {
        planningSupport = new WorkflowPlanningSupport();
        intentResolver = new EncryptWorkflowIntentResolver();
        ruleInspectionService = new EncryptRuleInspectionService();
        algorithmRecommendationService = new EncryptAlgorithmRecommendationService();
        algorithmPropertyTemplateService = new EncryptAlgorithmPropertyTemplateService();
        ruleDistSQLPlanningService = new EncryptRuleDistSQLPlanningService();
    }
    
    /**
     * Plan encrypt workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param metadataQueryFacade metadata query facade
     * @param queryFacade query facade
     * @param sessionId session id
     * @param request workflow request
     * @return workflow snapshot
     */
    public WorkflowContextSnapshot plan(final WorkflowSessionContext workflowSessionContext, final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade,
                                        final String sessionId, final EncryptWorkflowRequest request) {
        WorkflowContextSnapshot result = workflowSessionContext.getOrCreate(sessionId, request.getPlanId());
        EncryptWorkflowRequest mergedRequest = prepareSnapshot(result, request);
        ClarifiedIntent clarifiedIntent = result.getClarifiedIntent();
        planningSupport.applyResolvedIntent(mergedRequest, clarifiedIntent);
        if (!planningSupport.ensureSupportedOperationType(clarifiedIntent, SUPPORTED_OPERATION_TYPES, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        if (!planningSupport.ensurePlanningContext(metadataQueryFacade, queryFacade, mergedRequest, clarifiedIntent, result)) {
            String currentStep = WorkflowLifecycle.STATUS_FAILED.equals(result.getStatus()) ? WorkflowLifecycle.STEP_FAILED : WorkflowLifecycle.STEP_CLARIFYING;
            return workflowSessionContext.persist(result, currentStep, result.getStatus());
        }
        queryFacade.checkDatabaseCapability(mergedRequest.getDatabase());
        List<Map<String, Object>> existingRules = ruleInspectionService.queryEncryptRules(queryFacade, mergedRequest.getDatabase(), mergedRequest.getTable());
        if (!ensureLifecycleState(clarifiedIntent, mergedRequest, existingRules, result, queryFacade)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        if (isDropWorkflow(clarifiedIntent)) {
            if (!ensureSupportedRuleRewrite(queryFacade, mergedRequest, existingRules, result)) {
                return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
            }
            planDrop(mergedRequest, existingRules, result);
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
        }
        if (!planNonDrop(queryFacade, clarifiedIntent, mergedRequest, existingRules, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    private EncryptWorkflowRequest prepareSnapshot(final WorkflowContextSnapshot snapshot, final EncryptWorkflowRequest request) {
        EncryptWorkflowRequest result = EncryptWorkflowRequest.merge(snapshot.getRequest(), request);
        EncryptWorkflowState workflowState = getWorkflowState(snapshot);
        return planningSupport.prepareSnapshot(snapshot, EncryptFeatureDefinition.WORKFLOW_KIND, result, workflowState,
                intentResolver.resolve(result), "Encrypt workflow plan.", INTERACTION_STEPS, VALIDATION_LAYERS);
    }
    
    private EncryptWorkflowState getWorkflowState(final WorkflowContextSnapshot snapshot) {
        return snapshot.getFeatureData() instanceof EncryptWorkflowState ? (EncryptWorkflowState) snapshot.getFeatureData() : new EncryptWorkflowState();
    }
    
    private boolean ensureLifecycleState(final ClarifiedIntent clarifiedIntent, final EncryptWorkflowRequest request,
                                         final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade) {
        boolean ruleExists = encryptRules.stream().anyMatch(each -> queryFacade.isSameIdentifier(
                request.getDatabase(), IdentifierScope.COLUMN, request.getColumn(), WorkflowRuleValueUtils.getRuleValue(each, "logic_column")));
        return planningSupport.ensureLifecycleState("Encrypt rule", clarifiedIntent, ruleExists, snapshot);
    }
    
    private boolean isDropWorkflow(final ClarifiedIntent clarifiedIntent) {
        return WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(clarifiedIntent.getOperationType());
    }
    
    private void planDrop(final EncryptWorkflowRequest request, final List<Map<String, Object>> existingRules, final WorkflowContextSnapshot snapshot) {
        addDropLifecycleWarnings(snapshot);
        snapshot.getRuleArtifacts().addAll(ruleDistSQLPlanningService.planEncryptDropRule(request));
        snapshot.setFeatureData(new EncryptWorkflowState(existingRules, List.of()));
    }
    
    private boolean planNonDrop(final MCPFeatureQueryFacade queryFacade, final ClarifiedIntent clarifiedIntent, final EncryptWorkflowRequest request,
                                final List<Map<String, Object>> existingRules, final WorkflowContextSnapshot snapshot) {
        planAlgorithms(queryFacade, request, snapshot);
        if (!planningSupport.isReadyForArtifactPlanning(request, clarifiedIntent, snapshot, findPropertyRequirements(request),
                "Please use an encrypt algorithm that is visible in the current Proxy and satisfies the requirements.")) {
            return false;
        }
        if (!ensureSupportedRuleRewrite(queryFacade, request, existingRules, snapshot)) {
            return false;
        }
        if (!ensureRequiredRuleInputs(request, clarifiedIntent, snapshot)) {
            return false;
        }
        planEncryptArtifacts(request, snapshot);
        snapshot.setFeatureData(new EncryptWorkflowState(existingRules, List.of(createExpectedTargetRule(request))));
        return true;
    }
    
    private boolean ensureSupportedRuleRewrite(final MCPFeatureQueryFacade queryFacade, final EncryptWorkflowRequest request,
                                               final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot) {
        long remainingRuleCount = encryptRules.stream()
                .filter(each -> !queryFacade.isSameIdentifier(
                        request.getDatabase(), IdentifierScope.COLUMN, request.getColumn(), WorkflowRuleValueUtils.getRuleValue(each, "logic_column")))
                .count();
        if (0L == remainingRuleCount) {
            return true;
        }
        snapshot.getClarifiedIntent().getClarificationMessages().add(
                "Current Proxy DistSQL cannot automatically rewrite an existing encrypt table rule with a partial column set. "
                        + "Recreate the rule manually during a maintenance window.");
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.ENCRYPT_RULE_REWRITE_LIMITED, "error", WorkflowLifecycle.STEP_PLANNING_ARTIFACTS,
                "Encrypt planning cannot automatically rewrite an existing table rule while preserving other columns.",
                "Manually recreate the encrypt rule with the complete column set after reviewing data impact.", true, Map.of("requires_table_rule_rewrite", true)));
        return false;
    }
    
    private void planEncryptArtifacts(final EncryptWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        snapshot.getRuleArtifacts().addAll(ruleDistSQLPlanningService.planEncryptRule(request));
    }
    
    private void addDropLifecycleWarnings(final WorkflowContextSnapshot snapshot) {
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.ENCRYPT_DROP_SCOPE_LIMITED, "warning", WorkflowLifecycle.STEP_PLANNING_ARTIFACTS,
                "Encrypt drop only removes the rule. MCP will not restore historical plaintext data.", "Review business impact before execution.", true, Map.of()));
    }
    
    private Map<String, Object> createExpectedTargetRule(final EncryptWorkflowRequest request) {
        Map<String, Object> result = new LinkedHashMap<>(10, 1F);
        result.put("logic_column", request.getColumn());
        result.put("cipher_column", request.getOptions().getCipherColumnName());
        result.put("assisted_query_column", Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter()) ? request.getOptions().getAssistedQueryColumnName() : "");
        result.put("like_query_column", Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery()) ? request.getOptions().getLikeQueryColumnName() : "");
        result.put("encryptor_type", request.getAlgorithmType());
        result.put("encryptor_props", request.getPrimaryAlgorithmProperties());
        result.put("assisted_query_type", Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter()) ? request.getOptions().getAssistedQueryAlgorithmType() : "");
        result.put("assisted_query_props", Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter()) ? request.getOptions().getAssistedQueryAlgorithmProperties() : Map.of());
        result.put("like_query_type", Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery()) ? request.getOptions().getLikeQueryAlgorithmType() : "");
        result.put("like_query_props", Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery()) ? request.getOptions().getLikeQueryAlgorithmProperties() : Map.of());
        return result;
    }
    
    private void planAlgorithms(final MCPFeatureQueryFacade queryFacade, final EncryptWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        List<Map<String, Object>> encryptAlgorithms = ruleInspectionService.queryEncryptAlgorithms(queryFacade);
        List<AlgorithmCandidate> algorithmCandidates = algorithmRecommendationService.recommendEncryptAlgorithms(request, encryptAlgorithms, snapshot.getIssues());
        snapshot.getAlgorithmCandidates().addAll(algorithmCandidates);
        applyRecommendedAlgorithms(request, algorithmCandidates);
    }
    
    private List<AlgorithmPropertyRequirement> findPropertyRequirements(final EncryptWorkflowRequest request) {
        return algorithmPropertyTemplateService.findRequirements(request.getAlgorithmType(), request.getOptions().getAssistedQueryAlgorithmType(), request.getOptions().getLikeQueryAlgorithmType());
    }
    
    private void applyRecommendedAlgorithms(final EncryptWorkflowRequest request, final List<AlgorithmCandidate> algorithmCandidates) {
        for (AlgorithmCandidate each : algorithmCandidates) {
            if (EncryptFeatureDefinition.ALGORITHM_ROLE_PRIMARY.equals(each.getAlgorithmRole())) {
                request.setAlgorithmType(each.getAlgorithmType());
                continue;
            }
            if (EncryptFeatureDefinition.ALGORITHM_ROLE_ASSISTED_QUERY.equals(each.getAlgorithmRole())) {
                request.getOptions().setAssistedQueryAlgorithmType(each.getAlgorithmType());
                continue;
            }
            if (EncryptFeatureDefinition.ALGORITHM_ROLE_LIKE_QUERY.equals(each.getAlgorithmRole())) {
                request.getOptions().setLikeQueryAlgorithmType(each.getAlgorithmType());
            }
        }
    }
    
    private boolean ensureRequiredRuleInputs(final EncryptWorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        List<String> missingInputs = new LinkedList<>();
        addMissingInput(missingInputs, WorkflowFieldNames.CIPHER_COLUMN_NAME, request.getOptions().getCipherColumnName());
        if (Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter())) {
            addMissingInput(missingInputs, WorkflowFieldNames.ASSISTED_QUERY_COLUMN_NAME, request.getOptions().getAssistedQueryColumnName());
            addMissingInput(missingInputs, WorkflowFieldNames.ASSISTED_QUERY_ALGORITHM_TYPE, request.getOptions().getAssistedQueryAlgorithmType());
        }
        if (Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery())) {
            addMissingInput(missingInputs, WorkflowFieldNames.LIKE_QUERY_COLUMN_NAME, request.getOptions().getLikeQueryColumnName());
            addMissingInput(missingInputs, WorkflowFieldNames.LIKE_QUERY_ALGORITHM_TYPE, request.getOptions().getLikeQueryAlgorithmType());
        }
        if (missingInputs.isEmpty()) {
            return true;
        }
        for (String each : missingInputs) {
            clarifiedIntent.getClarificationMessages().add(String.format("Please provide `%s` for encrypt rule DistSQL.", each));
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_INPUT_REQUIRED, "error", WorkflowLifecycle.STEP_COLLECTING_RULE_INPUTS,
                "Encrypt rule DistSQL requires explicit rule column and query algorithm inputs.", "Provide the missing rule inputs and retry planning.", true,
                Map.of("missing_inputs", missingInputs)));
        return false;
    }
    
    private void addMissingInput(final List<String> missingInputs, final String fieldName, final String value) {
        if (value.isEmpty()) {
            missingInputs.add(fieldName);
        }
    }
}
