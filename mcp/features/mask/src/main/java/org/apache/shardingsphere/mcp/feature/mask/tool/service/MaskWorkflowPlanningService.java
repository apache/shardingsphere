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

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowContextStore;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowPlanningSupport;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Mask workflow planning service.
 */
public final class MaskWorkflowPlanningService {
    
    private static final List<String> INTERACTION_STEPS = List.of(
            "Confirm database, table, column and target lifecycle",
            "Inspect existing mask rules and logical metadata",
            "Clarify masking intent and choose algorithm",
            "Collect algorithm properties",
            "Generate DistSQL artifacts",
            "Review artifacts and choose execution mode",
            "Execute or export artifacts",
            "Validate and summarize");
    
    private static final List<String> VALIDATION_LAYERS = List.of("rules", "logical_metadata", "sql_executability");
    
    private final WorkflowPlanningSupport planningSupport = new WorkflowPlanningSupport();
    
    private final MaskWorkflowIntentResolver intentResolver = new MaskWorkflowIntentResolver();
    
    private final WorkflowContextStore contextStore;
    
    private final MaskRuleInspectionService ruleInspectionService;
    
    private final MaskAlgorithmRecommendationService algorithmRecommendationService;
    
    private final MaskAlgorithmPropertyTemplateService algorithmPropertyTemplateService;
    
    private final MaskRuleDistSQLPlanningService ruleDistSQLPlanningService;
    
    public MaskWorkflowPlanningService() {
        this(null, new MaskRuleInspectionService(), new MaskAlgorithmRecommendationService(),
                new MaskAlgorithmPropertyTemplateService(), new MaskRuleDistSQLPlanningService());
    }
    
    MaskWorkflowPlanningService(final WorkflowContextStore contextStore, final MaskRuleInspectionService ruleInspectionService,
                                final MaskAlgorithmRecommendationService algorithmRecommendationService,
                                final MaskAlgorithmPropertyTemplateService algorithmPropertyTemplateService,
                                final MaskRuleDistSQLPlanningService ruleDistSQLPlanningService) {
        this.contextStore = contextStore;
        this.ruleInspectionService = ruleInspectionService;
        this.algorithmRecommendationService = algorithmRecommendationService;
        this.algorithmPropertyTemplateService = algorithmPropertyTemplateService;
        this.ruleDistSQLPlanningService = ruleDistSQLPlanningService;
    }
    
    /**
     * Plan mask workflow.
     *
     * @param requestContext runtime context
     * @param sessionId session id
     * @param request workflow request
     * @return workflow snapshot
     */
    public WorkflowContextSnapshot plan(final MCPFeatureContext requestContext, final String sessionId, final WorkflowRequest request) {
        WorkflowContextStore actualContextStore = WorkflowLifecycleUtils.resolveContextStore(contextStore, requestContext);
        WorkflowContextSnapshot result = actualContextStore.getOrCreate(sessionId, request.getPlanId());
        WorkflowRequest mergedRequest = prepareSnapshot(result, request);
        ClarifiedIntent clarifiedIntent = result.getClarifiedIntent();
        applyResolvedIntent(mergedRequest, clarifiedIntent);
        MCPMetadataQueryFacade metadataQueryService = requestContext.getMetadataQueryFacade();
        if (!planningSupport.ensurePlanningContext(metadataQueryService, mergedRequest, clarifiedIntent, result)) {
            return actualContextStore.persist(result,
                    WorkflowLifecycle.STATUS_FAILED.equals(result.getStatus()) ? WorkflowLifecycle.STEP_FAILED : WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        List<Map<String, Object>> maskRules = ruleInspectionService.queryMaskRules(requestContext, mergedRequest.getDatabase(), mergedRequest.getTable());
        if (!ensureLifecycleState(clarifiedIntent, mergedRequest, maskRules, result)) {
            return actualContextStore.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        if (isDropWorkflow(clarifiedIntent)) {
            planArtifacts(clarifiedIntent, mergedRequest, maskRules, result);
            return actualContextStore.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
        }
        planAlgorithms(requestContext, clarifiedIntent, mergedRequest, result);
        if (hasBlockingAlgorithmIssues(clarifiedIntent, result)) {
            return actualContextStore.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        if (!clarifiedIntent.getPendingQuestions().isEmpty()) {
            return actualContextStore.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        if (!collectPropertyRequirements(mergedRequest, clarifiedIntent, result)) {
            return actualContextStore.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        planArtifacts(clarifiedIntent, mergedRequest, maskRules, result);
        return actualContextStore.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    private void applyResolvedIntent(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        request.setOperationType(clarifiedIntent.getOperationType());
        request.setFieldSemantics(clarifiedIntent.getFieldSemantics());
    }
    
    private WorkflowRequest prepareSnapshot(final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        WorkflowRequest result = WorkflowRequest.merge(snapshot.getRequest(), request);
        snapshot.setRequest(result);
        snapshot.setInteractionPlan(InteractionPlan.create(snapshot.getPlanId(), result, "Mask workflow plan.", INTERACTION_STEPS, VALIDATION_LAYERS));
        snapshot.clearPlanningState();
        snapshot.setClarifiedIntent(intentResolver.resolve(result));
        return result;
    }
    
    private boolean ensureLifecycleState(final ClarifiedIntent clarifiedIntent, final WorkflowRequest request,
                                         final List<Map<String, Object>> maskRules, final WorkflowContextSnapshot snapshot) {
        boolean ruleExists = maskRules.stream().anyMatch(each -> request.getColumn().equalsIgnoreCase(WorkflowRuleValueUtils.findRuleValue(each, "column", "logic_column")));
        if ("create".equalsIgnoreCase(clarifiedIntent.getOperationType()) && ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
                    "Mask rule already exists for the target column.", "Use alter instead of create.", false, Map.of()));
            return false;
        }
        if ("alter".equalsIgnoreCase(clarifiedIntent.getOperationType()) && !ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
                    "Mask rule does not exist for the target column.", "Use create instead of alter or confirm the target column.", false, Map.of()));
            return false;
        }
        if ("drop".equalsIgnoreCase(clarifiedIntent.getOperationType()) && !ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DROP_TARGET_RULE_NOT_FOUND, "error", "discovering",
                    "Mask rule does not exist for the target column.", "Confirm target table and column or skip the drop request.", false, Map.of()));
            return false;
        }
        return true;
    }
    
    private boolean isDropWorkflow(final ClarifiedIntent clarifiedIntent) {
        return WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(clarifiedIntent.getOperationType());
    }
    
    private void planAlgorithms(final MCPFeatureContext requestContext, final ClarifiedIntent clarifiedIntent, final WorkflowRequest request,
                                final WorkflowContextSnapshot snapshot) {
        List<Map<String, Object>> maskAlgorithms = ruleInspectionService.enrichMaskAlgorithms(ruleInspectionService.queryMaskAlgorithms(requestContext));
        List<AlgorithmCandidate> algorithmCandidates = algorithmRecommendationService.recommendMaskAlgorithms(clarifiedIntent, request, maskAlgorithms, snapshot.getIssues());
        snapshot.getAlgorithmCandidates().addAll(algorithmCandidates);
        if (!algorithmCandidates.isEmpty()) {
            request.setAlgorithmType(algorithmCandidates.get(0).getAlgorithmType());
        }
    }
    
    private boolean collectPropertyRequirements(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        List<AlgorithmPropertyRequirement> propertyRequirements = algorithmPropertyTemplateService.findRequirements(request.getAlgorithmType());
        snapshot.getPropertyRequirements().addAll(propertyRequirements);
        applyDefaultProperties(request, propertyRequirements);
        List<String> missingRequiredProperties = findMissingRequiredProperties(request, propertyRequirements);
        if (missingRequiredProperties.isEmpty()) {
            return true;
        }
        for (String each : missingRequiredProperties) {
            clarifiedIntent.getPendingQuestions().add(String.format("请提供属性 `%s`。", each));
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING, "error", "collecting-properties",
                "Required algorithm properties are still missing.", "Provide all required algorithm properties.", true, Map.of("missing_properties", missingRequiredProperties)));
        return false;
    }
    
    private void applyDefaultProperties(final WorkflowRequest request, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        for (AlgorithmPropertyRequirement each : propertyRequirements) {
            if (!each.getDefaultValue().isEmpty()) {
                request.getPrimaryAlgorithmProperties().putIfAbsent(each.getPropertyKey(), each.getDefaultValue());
            }
        }
    }
    
    private List<String> findMissingRequiredProperties(final WorkflowRequest request, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        List<String> result = new LinkedList<>();
        for (AlgorithmPropertyRequirement each : propertyRequirements) {
            if (each.isRequired() && WorkflowSqlUtils.trimToEmpty(request.getPrimaryAlgorithmProperties().get(each.getPropertyKey())).isEmpty()) {
                result.add(each.getPropertyKey());
            }
        }
        return result;
    }
    
    private boolean hasBlockingAlgorithmIssues(final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        boolean hasBlockingIssue = snapshot.getIssues().stream()
                .anyMatch(each -> "selecting-algorithm".equals(each.getStage()) && "error".equals(each.getSeverity()));
        if (hasBlockingIssue && clarifiedIntent.getPendingQuestions().isEmpty()) {
            clarifiedIntent.getPendingQuestions().add("请改用当前 Proxy 可见的脱敏算法。");
        }
        return hasBlockingIssue;
    }
    
    private void planArtifacts(final ClarifiedIntent clarifiedIntent, final WorkflowRequest request,
                               final List<Map<String, Object>> maskRules, final WorkflowContextSnapshot snapshot) {
        snapshot.getRuleArtifacts().add(isDropWorkflow(clarifiedIntent)
                ? ruleDistSQLPlanningService.planMaskDropRule(request, maskRules)
                : ruleDistSQLPlanningService.planMaskRule(request, maskRules));
    }
}
