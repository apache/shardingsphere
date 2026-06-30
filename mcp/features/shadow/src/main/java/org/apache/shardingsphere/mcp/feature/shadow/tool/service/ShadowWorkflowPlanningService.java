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

package org.apache.shardingsphere.mcp.feature.shadow.tool.service;

import org.apache.shardingsphere.mcp.feature.shadow.ShadowFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowAlgorithmCleanupWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowDefaultAlgorithmWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningSupport;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Shadow workflow planning service.
 */
public final class ShadowWorkflowPlanningService {
    
    private static final List<String> RULE_INTERACTION_STEPS = List.of(
            "Confirm database, rule name, storage units, table and algorithm",
            "Inspect DistSQL-visible shadow rules, table rules and algorithms",
            "Generate shadow rule DistSQL artifacts",
            "Review artifacts and choose execution mode",
            "Execute or export artifacts",
            "Validate and summarize");
    
    private static final List<String> DEFAULT_ALGORITHM_INTERACTION_STEPS = List.of(
            "Confirm database and default shadow algorithm intent",
            "Inspect configured and default shadow algorithms",
            "Generate default shadow algorithm DistSQL artifacts",
            "Review artifacts and choose execution mode",
            "Execute or export artifacts",
            "Validate and summarize");
    
    private static final List<String> CLEANUP_INTERACTION_STEPS = List.of(
            "Confirm database and shadow algorithm name",
            "Inspect configured algorithms, table-rule references and default algorithm",
            "Generate safe shadow algorithm cleanup DistSQL only when unused",
            "Review artifacts and choose execution mode",
            "Execute or export artifacts",
            "Validate and summarize");
    
    private static final List<String> VALIDATION_LAYERS = List.of("rules", "algorithms");
    
    private final WorkflowPlanningSupport planningSupport = new WorkflowPlanningSupport();
    
    private final ShadowInspectionService inspectionService;
    
    private final ShadowAlgorithmRecommendationService algorithmRecommendationService;
    
    private final ShadowAlgorithmPropertyTemplateService algorithmPropertyTemplateService;
    
    private final ShadowDistSQLPlanningService distSQLPlanningService;
    
    public ShadowWorkflowPlanningService() {
        inspectionService = new ShadowInspectionService();
        algorithmRecommendationService = new ShadowAlgorithmRecommendationService();
        algorithmPropertyTemplateService = new ShadowAlgorithmPropertyTemplateService();
        distSQLPlanningService = new ShadowDistSQLPlanningService();
    }
    
    ShadowWorkflowPlanningService(final ShadowInspectionService inspectionService, final ShadowDistSQLPlanningService distSQLPlanningService) {
        this.inspectionService = inspectionService;
        algorithmRecommendationService = new ShadowAlgorithmRecommendationService();
        algorithmPropertyTemplateService = new ShadowAlgorithmPropertyTemplateService();
        this.distSQLPlanningService = distSQLPlanningService;
    }
    
    /**
     * Plan shadow rule workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade query facade
     * @param sessionId session id
     * @param request workflow request
     * @return workflow snapshot
     */
    public WorkflowContextSnapshot planRule(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade, final String sessionId,
                                            final ShadowRuleWorkflowRequest request) {
        WorkflowContextSnapshot result = workflowSessionContext.getOrCreate(sessionId, request.getPlanId());
        ShadowRuleWorkflowRequest mergedRequest = prepareSnapshot(result, request, ShadowFeatureDefinition.RULE_WORKFLOW_KIND,
                resolveIntent(request, "create"), "Shadow rule workflow plan.", RULE_INTERACTION_STEPS);
        planningSupport.applyResolvedIntent(mergedRequest, result.getClarifiedIntent());
        if (!WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(result.getClarifiedIntent().getOperationType()) && !mergedRequest.getDatabase().isEmpty()) {
            planAlgorithms(queryFacade, mergedRequest, result);
        }
        if (!ensureRulePlanningContext(mergedRequest, result.getClarifiedIntent(), result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        if (!WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(result.getClarifiedIntent().getOperationType())
                && !planningSupport.isReadyForArtifactPlanning(mergedRequest, result.getClarifiedIntent(), result, findPropertyRequirements(mergedRequest),
                        "Please use a shadow algorithm visible in the current Proxy and provide required properties.")) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        String databaseType = queryFacade.getDatabaseType(mergedRequest.getDatabase());
        if (!ensureRuleLifecycle(result.getClarifiedIntent(), mergedRequest, inspectionService.queryRules(queryFacade, mergedRequest.getDatabase()), result, databaseType)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        addRuleArtifact(result, mergedRequest, result.getClarifiedIntent().getOperationType());
        return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    /**
     * Plan default shadow algorithm workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade query facade
     * @param sessionId session id
     * @param request workflow request
     * @return workflow snapshot
     */
    public WorkflowContextSnapshot planDefaultAlgorithm(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                        final String sessionId, final ShadowDefaultAlgorithmWorkflowRequest request) {
        WorkflowContextSnapshot result = workflowSessionContext.getOrCreate(sessionId, request.getPlanId());
        ShadowDefaultAlgorithmWorkflowRequest mergedRequest = prepareSnapshot(result, request, ShadowFeatureDefinition.DEFAULT_ALGORITHM_WORKFLOW_KIND,
                resolveIntent(request, "create"), "Default shadow algorithm workflow plan.", DEFAULT_ALGORITHM_INTERACTION_STEPS);
        planningSupport.applyResolvedIntent(mergedRequest, result.getClarifiedIntent());
        if (!WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(result.getClarifiedIntent().getOperationType()) && !mergedRequest.getDatabase().isEmpty()) {
            planAlgorithms(queryFacade, mergedRequest, result);
        }
        if (!ensureDefaultAlgorithmPlanningContext(mergedRequest, result.getClarifiedIntent(), result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        if (!WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(result.getClarifiedIntent().getOperationType())
                && !planningSupport.isReadyForArtifactPlanning(mergedRequest, result.getClarifiedIntent(), result, findPropertyRequirements(mergedRequest),
                        "Please use a shadow algorithm visible in the current Proxy and provide required properties.")) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        boolean exists = !inspectionService.queryDefaultAlgorithm(queryFacade, mergedRequest.getDatabase()).isEmpty();
        if (!planningSupport.ensureLifecycleState("Default shadow algorithm", result.getClarifiedIntent(), exists, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        addDefaultAlgorithmArtifact(result, mergedRequest, result.getClarifiedIntent().getOperationType());
        return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    /**
     * Plan shadow algorithm cleanup workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade query facade
     * @param sessionId session id
     * @param request workflow request
     * @return workflow snapshot
     */
    public WorkflowContextSnapshot planAlgorithmCleanup(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                        final String sessionId, final ShadowAlgorithmCleanupWorkflowRequest request) {
        WorkflowContextSnapshot result = workflowSessionContext.getOrCreate(sessionId, request.getPlanId());
        ShadowAlgorithmCleanupWorkflowRequest mergedRequest = prepareSnapshot(result, request, ShadowFeatureDefinition.ALGORITHM_CLEANUP_WORKFLOW_KIND,
                resolveIntent(request, WorkflowLifecycle.OPERATION_DROP), "Shadow algorithm cleanup workflow plan.", CLEANUP_INTERACTION_STEPS);
        planningSupport.applyResolvedIntent(mergedRequest, result.getClarifiedIntent());
        if (!ensureCleanupPlanningContext(mergedRequest, result.getClarifiedIntent(), result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        List<Map<String, Object>> algorithms = inspectionService.queryAlgorithms(queryFacade, mergedRequest.getDatabase());
        List<Map<String, Object>> tableRules = inspectionService.queryTableRules(queryFacade, mergedRequest.getDatabase());
        List<Map<String, Object>> defaultAlgorithm = inspectionService.queryDefaultAlgorithm(queryFacade, mergedRequest.getDatabase());
        if (!ensureAlgorithmCleanupState(mergedRequest, algorithms, tableRules, defaultAlgorithm, result, queryFacade.getDatabaseType(mergedRequest.getDatabase()))) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        result.getRuleArtifacts().add(distSQLPlanningService.planDropAlgorithm(mergedRequest.getAlgorithmName()));
        return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    private ShadowRuleWorkflowRequest prepareSnapshot(final WorkflowContextSnapshot snapshot, final ShadowRuleWorkflowRequest request, final WorkflowKind workflowKind,
                                                      final ClarifiedIntent clarifiedIntent, final String summary, final List<String> interactionSteps) {
        return planningSupport.prepareSnapshot(snapshot, workflowKind, ShadowRuleWorkflowRequest.merge(snapshot.getRequest(), request), null,
                clarifiedIntent, summary, interactionSteps, VALIDATION_LAYERS);
    }
    
    private ShadowDefaultAlgorithmWorkflowRequest prepareSnapshot(final WorkflowContextSnapshot snapshot, final ShadowDefaultAlgorithmWorkflowRequest request,
                                                                  final WorkflowKind workflowKind, final ClarifiedIntent clarifiedIntent, final String summary,
                                                                  final List<String> interactionSteps) {
        return planningSupport.prepareSnapshot(snapshot, workflowKind, ShadowDefaultAlgorithmWorkflowRequest.merge(snapshot.getRequest(), request), null,
                clarifiedIntent, summary, interactionSteps, VALIDATION_LAYERS);
    }
    
    private ShadowAlgorithmCleanupWorkflowRequest prepareSnapshot(final WorkflowContextSnapshot snapshot, final ShadowAlgorithmCleanupWorkflowRequest request,
                                                                  final WorkflowKind workflowKind, final ClarifiedIntent clarifiedIntent, final String summary,
                                                                  final List<String> interactionSteps) {
        return planningSupport.prepareSnapshot(snapshot, workflowKind, ShadowAlgorithmCleanupWorkflowRequest.merge(snapshot.getRequest(), request), null,
                clarifiedIntent, summary, interactionSteps, VALIDATION_LAYERS);
    }
    
    private ClarifiedIntent resolveIntent(final WorkflowRequest request, final String defaultOperationType) {
        ClarifiedIntent result = new ClarifiedIntent();
        String operationType = request.getOperationType();
        result.setOperationType(operationType.isEmpty() ? defaultOperationType : operationType);
        result.setFieldSemantics("DistSQL-visible shadow rule and algorithm fields only.");
        return result;
    }
    
    private boolean ensureRulePlanningContext(final ShadowRuleWorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        if (!ensureDatabase(request, clarifiedIntent, snapshot) || !ensureSupportedIdentifiers(snapshot, request.getDatabase(), request.getRuleName(), request.getSourceStorageUnit(),
                request.getShadowStorageUnit(), request.getTableName())) {
            return false;
        }
        List<String> missingInputs = new LinkedList<>();
        addMissingInput(missingInputs, request.getRuleName(), ShadowFeatureDefinition.RULE_FIELD);
        if (!WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            addMissingInput(missingInputs, request.getSourceStorageUnit(), ShadowFeatureDefinition.SOURCE_STORAGE_UNIT_FIELD);
            addMissingInput(missingInputs, request.getShadowStorageUnit(), ShadowFeatureDefinition.SHADOW_STORAGE_UNIT_FIELD);
            addMissingInput(missingInputs, request.getTableName(), ShadowFeatureDefinition.TABLE_FIELD);
            addMissingInput(missingInputs, request.getAlgorithmType(), ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD);
        }
        return ensureNoMissingInputs(missingInputs, clarifiedIntent, snapshot, "Shadow rule DistSQL requires explicit rule, storage unit, table and algorithm inputs.");
    }
    
    private boolean ensureDefaultAlgorithmPlanningContext(final ShadowDefaultAlgorithmWorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        if (!ensureDatabase(request, clarifiedIntent, snapshot)) {
            return false;
        }
        List<String> missingInputs = new LinkedList<>();
        if (!WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            addMissingInput(missingInputs, request.getAlgorithmType(), ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD);
        }
        return ensureNoMissingInputs(missingInputs, clarifiedIntent, snapshot, "Default shadow algorithm DistSQL requires an explicit algorithm type and properties.");
    }
    
    private boolean ensureCleanupPlanningContext(final ShadowAlgorithmCleanupWorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        return ensureDatabase(request, clarifiedIntent, snapshot) && ensureSupportedIdentifiers(snapshot, request.getDatabase(), request.getAlgorithmName())
                && ensureNoMissingInputs(request.getAlgorithmName().isEmpty() ? List.of(ShadowFeatureDefinition.ALGORITHM_NAME_FIELD) : List.of(), clarifiedIntent, snapshot,
                        "Shadow algorithm cleanup requires the configured algorithm name.");
    }
    
    private boolean ensureDatabase(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        if (request.getDatabase().isEmpty()) {
            clarifiedIntent.getClarificationMessages().add("Please provide logical database first.");
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DATABASE_REQUIRED, "error", "intaking",
                    "Database is required before planning shadow DistSQL.", "Provide the logical database name.", true, Map.of()));
            snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
            return false;
        }
        if (!ensureSupportedIdentifiers(snapshot, request.getDatabase())) {
            snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
            return false;
        }
        return true;
    }
    
    private boolean ensureSupportedIdentifiers(final WorkflowContextSnapshot snapshot, final String... identifiers) {
        for (String each : identifiers) {
            if (!each.isEmpty() && !WorkflowSQLUtils.isSupportedIdentifier(each)) {
                snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER, "error", "intaking",
                        String.format("Identifier `%s` contains unsupported characters.", each),
                        "Use reviewable logical identifiers without NUL or line terminators.", false, Map.of("identifier", each)));
                snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
                return false;
            }
        }
        return true;
    }
    
    private void addMissingInput(final Collection<String> missingInputs, final String value, final String fieldName) {
        if (null == value || value.trim().isEmpty()) {
            missingInputs.add(fieldName);
        }
    }
    
    private boolean ensureNoMissingInputs(final Collection<String> missingInputs, final ClarifiedIntent clarifiedIntent,
                                          final WorkflowContextSnapshot snapshot, final String message) {
        if (missingInputs.isEmpty()) {
            return true;
        }
        clarifiedIntent.getClarificationMessages().add(message);
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_INPUT_REQUIRED, "error", "intaking", message,
                "Provide the missing fields instead of guessing storage units, tables or algorithms.", true, Map.of("missing_inputs", missingInputs)));
        snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
        return false;
    }
    
    private boolean ensureRuleLifecycle(final ClarifiedIntent clarifiedIntent, final ShadowRuleWorkflowRequest request, final List<Map<String, Object>> rules,
                                        final WorkflowContextSnapshot snapshot, final String databaseType) {
        return planningSupport.ensureLifecycleState("Shadow rule", clarifiedIntent,
                rules.stream().anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, request.getRuleName(), WorkflowRuleValueUtils.getRuleValue(each, "rule_name"))), snapshot);
    }
    
    private boolean ensureAlgorithmCleanupState(final ShadowAlgorithmCleanupWorkflowRequest request, final List<Map<String, Object>> algorithms,
                                                final List<Map<String, Object>> tableRules, final List<Map<String, Object>> defaultAlgorithm,
                                                final WorkflowContextSnapshot snapshot, final String databaseType) {
        boolean configured = algorithms.stream().anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, request.getAlgorithmName(),
                WorkflowRuleValueUtils.getRuleValue(each, "shadow_algorithm_name")));
        if (!configured) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
                    String.format("Shadow algorithm `%s` is not configured.", request.getAlgorithmName()),
                    "Inspect configured shadow algorithms before cleanup.", false, Map.of("algorithm", request.getAlgorithmName())));
            return false;
        }
        if (isReferencedByTableRule(request.getAlgorithmName(), tableRules, databaseType) || isDefaultAlgorithm(request.getAlgorithmName(), defaultAlgorithm, databaseType)) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
                    String.format("Shadow algorithm `%s` is still referenced.", request.getAlgorithmName()),
                    "Remove table-rule or default-algorithm references before cleanup.", false, Map.of("algorithm", request.getAlgorithmName())));
            return false;
        }
        return true;
    }
    
    private boolean isReferencedByTableRule(final String algorithmName, final List<Map<String, Object>> tableRules, final String databaseType) {
        return tableRules.stream().map(each -> WorkflowRuleValueUtils.getRuleValue(each, "shadow_algorithm_name"))
                .flatMap(each -> Arrays.stream(each.split(","))).map(String::trim).filter(each -> !each.isEmpty())
                .anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, algorithmName, each));
    }
    
    private boolean isDefaultAlgorithm(final String algorithmName, final List<Map<String, Object>> defaultAlgorithm, final String databaseType) {
        return defaultAlgorithm.stream().anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, algorithmName,
                WorkflowRuleValueUtils.getRuleValue(each, "shadow_algorithm_name")));
    }
    
    private void planAlgorithms(final MCPFeatureQueryFacade queryFacade, final WorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        List<AlgorithmCandidate> algorithmCandidates = algorithmRecommendationService.recommendShadowAlgorithms(
                request, inspectionService.queryAlgorithmPlugins(queryFacade), snapshot.getIssues());
        snapshot.getAlgorithmCandidates().addAll(algorithmCandidates);
        if (!algorithmCandidates.isEmpty()) {
            request.setAlgorithmType(algorithmCandidates.getFirst().getAlgorithmType());
        }
    }
    
    private List<AlgorithmPropertyRequirement> findPropertyRequirements(final WorkflowRequest request) {
        return algorithmPropertyTemplateService.findRequirements(request.getAlgorithmType());
    }
    
    private void addRuleArtifact(final WorkflowContextSnapshot snapshot, final ShadowRuleWorkflowRequest request, final String operationType) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(operationType)) {
            snapshot.getRuleArtifacts().add(distSQLPlanningService.planDropRule(request.getRuleName()));
        } else if ("alter".equalsIgnoreCase(operationType)) {
            snapshot.getRuleArtifacts().add(distSQLPlanningService.planAlterRule(request));
        } else {
            snapshot.getRuleArtifacts().add(distSQLPlanningService.planCreateRule(request));
        }
    }
    
    private void addDefaultAlgorithmArtifact(final WorkflowContextSnapshot snapshot, final ShadowDefaultAlgorithmWorkflowRequest request, final String operationType) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(operationType)) {
            snapshot.getRuleArtifacts().add(distSQLPlanningService.planDropDefaultAlgorithm());
        } else if ("alter".equalsIgnoreCase(operationType)) {
            snapshot.getRuleArtifacts().add(distSQLPlanningService.planAlterDefaultAlgorithm(request));
        } else {
            snapshot.getRuleArtifacts().add(distSQLPlanningService.planCreateDefaultAlgorithm(request));
        }
    }
}
