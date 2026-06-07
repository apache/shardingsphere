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

import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.feature.mask.MaskFeatureDefinition;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleWorkflowFeatureData;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningSupport;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Mask workflow planning service.
 */
public final class MaskWorkflowPlanningService {
    
    private static final List<String> INTERACTION_STEPS = List.of(
            "Confirm database, table, column and target lifecycle",
            "Inspect mask algorithm plugins and DistSQL-visible rules",
            "Clarify masking intent and choose algorithm",
            "Collect algorithm properties",
            "Generate mask rule DistSQL artifacts",
            "Review artifacts and choose execution mode",
            "Execute or export artifacts",
            "Validate and summarize");
    
    private static final List<String> VALIDATION_LAYERS = List.of("rules");
    
    private final WorkflowPlanningSupport planningSupport;
    
    private final MaskWorkflowIntentResolver intentResolver;
    
    private final MaskRuleInspectionService ruleInspectionService;
    
    private final MaskAlgorithmRecommendationService algorithmRecommendationService;
    
    private final MaskAlgorithmPropertyTemplateService algorithmPropertyTemplateService;
    
    private final MaskRuleDistSQLPlanningService ruleDistSQLPlanningService;
    
    /**
     * Create mask workflow planning service.
     */
    public MaskWorkflowPlanningService() {
        planningSupport = new WorkflowPlanningSupport();
        intentResolver = new MaskWorkflowIntentResolver();
        ruleInspectionService = new MaskRuleInspectionService();
        algorithmRecommendationService = new MaskAlgorithmRecommendationService();
        algorithmPropertyTemplateService = new MaskAlgorithmPropertyTemplateService();
        ruleDistSQLPlanningService = new MaskRuleDistSQLPlanningService();
    }
    
    /**
     * Plan mask workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param metadataQueryFacade metadata query facade
     * @param queryFacade query facade
     * @param sessionId session id
     * @param request workflow request
     * @return workflow snapshot
     */
    public WorkflowContextSnapshot plan(final WorkflowSessionContext workflowSessionContext, final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade,
                                        final String sessionId, final WorkflowRequest request) {
        WorkflowContextSnapshot result = workflowSessionContext.getOrCreate(sessionId, request.getPlanId());
        WorkflowRequest mergedRequest = prepareSnapshot(result, request);
        ClarifiedIntent clarifiedIntent = result.getClarifiedIntent();
        planningSupport.applyResolvedIntent(mergedRequest, clarifiedIntent);
        if (!planningSupport.ensurePlanningContext(metadataQueryFacade, mergedRequest, clarifiedIntent, result)) {
            String currentStep = WorkflowLifecycle.STATUS_FAILED.equals(result.getStatus()) ? WorkflowLifecycle.STEP_FAILED : WorkflowLifecycle.STEP_CLARIFYING;
            return workflowSessionContext.persist(result, currentStep, result.getStatus());
        }
        String databaseType = queryFacade.getDatabaseType(mergedRequest.getDatabase());
        List<Map<String, Object>> existingRules = ruleInspectionService.queryMaskRules(queryFacade, mergedRequest.getDatabase(), mergedRequest.getTable());
        if (!ensureLifecycleState(clarifiedIntent, mergedRequest, existingRules, result, databaseType)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        if (isDropWorkflow(clarifiedIntent)) {
            planArtifacts(clarifiedIntent, mergedRequest, existingRules, result, databaseType);
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
        }
        if (!planNonDrop(queryFacade, clarifiedIntent, mergedRequest, existingRules, result, databaseType)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    private WorkflowRequest prepareSnapshot(final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        WorkflowRequest result = WorkflowRequest.merge(snapshot.getRequest(), request);
        return planningSupport.prepareSnapshot(snapshot, MaskFeatureDefinition.WORKFLOW_KIND, result, null,
                intentResolver.resolve(result), "Mask workflow plan.", INTERACTION_STEPS, VALIDATION_LAYERS);
    }
    
    private boolean ensureLifecycleState(final ClarifiedIntent clarifiedIntent, final WorkflowRequest request,
                                         final List<Map<String, Object>> maskRules, final WorkflowContextSnapshot snapshot, final String databaseType) {
        boolean ruleExists = maskRules.stream().anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, request.getColumn(), WorkflowRuleValueUtils.getRuleValue(each, "column")));
        return planningSupport.ensureLifecycleState("Mask rule", clarifiedIntent, ruleExists, snapshot);
    }
    
    private boolean isDropWorkflow(final ClarifiedIntent clarifiedIntent) {
        return WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(clarifiedIntent.getOperationType());
    }
    
    private boolean planNonDrop(final MCPFeatureQueryFacade queryFacade, final ClarifiedIntent clarifiedIntent, final WorkflowRequest request,
                                final List<Map<String, Object>> existingRules, final WorkflowContextSnapshot snapshot, final String databaseType) {
        planAlgorithms(queryFacade, clarifiedIntent, request, snapshot);
        if (!planningSupport.isReadyForArtifactPlanning(request, clarifiedIntent, snapshot, findPropertyRequirements(request), "Please use a mask algorithm visible in the current Proxy.")) {
            return false;
        }
        planArtifacts(clarifiedIntent, request, existingRules, snapshot, databaseType);
        return true;
    }
    
    private void planAlgorithms(final MCPFeatureQueryFacade queryFacade, final ClarifiedIntent clarifiedIntent, final WorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        List<Map<String, Object>> maskAlgorithms = ruleInspectionService.queryMaskAlgorithms(queryFacade);
        List<AlgorithmCandidate> algorithmCandidates = algorithmRecommendationService.recommendMaskAlgorithms(clarifiedIntent, request, maskAlgorithms, snapshot.getIssues());
        snapshot.getAlgorithmCandidates().addAll(algorithmCandidates);
        if (!algorithmCandidates.isEmpty()) {
            request.setAlgorithmType(algorithmCandidates.getFirst().getAlgorithmType());
        }
    }
    
    private List<AlgorithmPropertyRequirement> findPropertyRequirements(final WorkflowRequest request) {
        return algorithmPropertyTemplateService.findRequirements(request.getAlgorithmType());
    }
    
    private void planArtifacts(final ClarifiedIntent clarifiedIntent, final WorkflowRequest request,
                               final List<Map<String, Object>> maskRules, final WorkflowContextSnapshot snapshot, final String databaseType) {
        snapshot.getRuleArtifacts().addAll(isDropWorkflow(clarifiedIntent)
                ? ruleDistSQLPlanningService.planMaskDropRule(request, maskRules, databaseType)
                : ruleDistSQLPlanningService.planMaskRule(request, maskRules, databaseType));
        snapshot.setFeatureData(new RuleWorkflowFeatureData(maskRules, isDropWorkflow(clarifiedIntent)
                ? createExpectedDropRules(request, maskRules, databaseType)
                : createExpectedMaskRules(request, maskRules, databaseType)));
    }
    
    private List<Map<String, Object>> createExpectedDropRules(final WorkflowRequest request, final List<Map<String, Object>> existingRules, final String databaseType) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : existingRules) {
            if (!WorkflowSQLUtils.isSameIdentifier(databaseType, request.getColumn(), WorkflowRuleValueUtils.getRuleValue(each, "column"))) {
                result.add(new LinkedHashMap<>(each));
            }
        }
        return result;
    }
    
    private List<Map<String, Object>> createExpectedMaskRules(final WorkflowRequest request, final List<Map<String, Object>> existingRules, final String databaseType) {
        List<Map<String, Object>> result = new LinkedList<>();
        boolean targetRuleHandled = false;
        for (Map<String, Object> each : existingRules) {
            if (WorkflowSQLUtils.isSameIdentifier(databaseType, request.getColumn(), WorkflowRuleValueUtils.getRuleValue(each, "column"))) {
                result.add(createExpectedTargetRule(request));
                targetRuleHandled = true;
            } else {
                result.add(new LinkedHashMap<>(each));
            }
        }
        if (!targetRuleHandled) {
            result.add(createExpectedTargetRule(request));
        }
        return result;
    }
    
    private Map<String, Object> createExpectedTargetRule(final WorkflowRequest request) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("column", request.getColumn());
        result.put("algorithm_type", request.getAlgorithmType());
        result.put("algorithm_props", request.getPrimaryAlgorithmProperties());
        return result;
    }
}
