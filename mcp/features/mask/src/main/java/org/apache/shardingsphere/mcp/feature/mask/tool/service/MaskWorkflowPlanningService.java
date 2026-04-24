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
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowContextStore;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowPlanningSupport;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowRuleValueUtils;

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
        planningSupport.applyResolvedIntent(mergedRequest, clarifiedIntent);
        MCPMetadataQueryFacade metadataQueryFacade = requestContext.getMetadataQueryFacade();
        if (!planningSupport.ensurePlanningContext(metadataQueryFacade, mergedRequest, clarifiedIntent, result)) {
            String currentStep = WorkflowLifecycle.STATUS_FAILED.equals(result.getStatus()) ? WorkflowLifecycle.STEP_FAILED : WorkflowLifecycle.STEP_CLARIFYING;
            return actualContextStore.persist(result, currentStep, result.getStatus());
        }
        List<Map<String, Object>> existingRules = ruleInspectionService.queryMaskRules(requestContext, mergedRequest.getDatabase(), mergedRequest.getTable());
        if (!ensureLifecycleState(clarifiedIntent, mergedRequest, existingRules, result)) {
            return actualContextStore.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        if (isDropWorkflow(clarifiedIntent)) {
            planArtifacts(clarifiedIntent, mergedRequest, existingRules, result);
            return actualContextStore.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
        }
        if (!planNonDrop(requestContext, clarifiedIntent, mergedRequest, existingRules, result)) {
            return actualContextStore.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        return actualContextStore.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    private WorkflowRequest prepareSnapshot(final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        WorkflowRequest result = WorkflowRequest.merge(snapshot.getRequest(), request);
        return planningSupport.prepareSnapshot(snapshot, result, null, intentResolver.resolve(result), "Mask workflow plan.", INTERACTION_STEPS, VALIDATION_LAYERS);
    }
    
    private boolean ensureLifecycleState(final ClarifiedIntent clarifiedIntent, final WorkflowRequest request,
                                         final List<Map<String, Object>> maskRules, final WorkflowContextSnapshot snapshot) {
        boolean ruleExists = maskRules.stream().anyMatch(each -> request.getColumn().equalsIgnoreCase(WorkflowRuleValueUtils.findRuleValue(each, "column", "logic_column")));
        return planningSupport.ensureLifecycleState("Mask rule", clarifiedIntent, ruleExists, snapshot);
    }
    
    private boolean isDropWorkflow(final ClarifiedIntent clarifiedIntent) {
        return WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(clarifiedIntent.getOperationType());
    }
    
    private boolean planNonDrop(final MCPFeatureContext requestContext, final ClarifiedIntent clarifiedIntent, final WorkflowRequest request,
                                final List<Map<String, Object>> existingRules, final WorkflowContextSnapshot snapshot) {
        planAlgorithms(requestContext, clarifiedIntent, request, snapshot);
        if (planningSupport.hasBlockingAlgorithmIssues(clarifiedIntent, snapshot, "请改用当前 Proxy 可见的脱敏算法。")
                || !clarifiedIntent.getPendingQuestions().isEmpty()
                || !collectPropertyRequirements(request, clarifiedIntent, snapshot)) {
            return false;
        }
        planArtifacts(clarifiedIntent, request, existingRules, snapshot);
        return true;
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
        return planningSupport.collectPropertyRequirements(request, clarifiedIntent, snapshot, propertyRequirements);
    }
    
    private void planArtifacts(final ClarifiedIntent clarifiedIntent, final WorkflowRequest request,
                               final List<Map<String, Object>> maskRules, final WorkflowContextSnapshot snapshot) {
        snapshot.getRuleArtifacts().add(isDropWorkflow(clarifiedIntent)
                ? ruleDistSQLPlanningService.planMaskDropRule(request, maskRules)
                : ruleDistSQLPlanningService.planMaskRule(request, maskRules));
    }
}
