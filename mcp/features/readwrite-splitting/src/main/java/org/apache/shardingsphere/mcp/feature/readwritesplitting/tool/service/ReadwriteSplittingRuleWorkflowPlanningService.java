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

import org.apache.shardingsphere.mcp.feature.readwritesplitting.ReadwriteSplittingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningSupport;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Readwrite-splitting rule workflow planning service.
 */
public final class ReadwriteSplittingRuleWorkflowPlanningService {
    
    private static final List<String> INTERACTION_STEPS = List.of(
            "Confirm database, rule name, storage units, strategy and lifecycle",
            "Inspect DistSQL-visible readwrite-splitting rules",
            "Generate readwrite-splitting rule DistSQL artifacts",
            "Review artifacts and choose execution mode",
            "Execute or export artifacts",
            "Validate and summarize");
    
    private static final List<String> VALIDATION_LAYERS = List.of("rules");
    
    private final WorkflowPlanningSupport planningSupport = new WorkflowPlanningSupport();
    
    private final ReadwriteSplittingWorkflowIntentResolver intentResolver = new ReadwriteSplittingWorkflowIntentResolver();
    
    private final ReadwriteSplittingInspectionService inspectionService;
    
    private final ReadwriteSplittingAlgorithmRecommendationService algorithmRecommendationService;
    
    private final ReadwriteSplittingAlgorithmPropertyTemplateService algorithmPropertyTemplateService;
    
    private final ReadwriteSplittingRuleDistSQLPlanningService distSQLPlanningService;
    
    public ReadwriteSplittingRuleWorkflowPlanningService() {
        inspectionService = new ReadwriteSplittingInspectionService();
        algorithmRecommendationService = new ReadwriteSplittingAlgorithmRecommendationService();
        algorithmPropertyTemplateService = new ReadwriteSplittingAlgorithmPropertyTemplateService();
        distSQLPlanningService = new ReadwriteSplittingRuleDistSQLPlanningService();
    }
    
    ReadwriteSplittingRuleWorkflowPlanningService(final ReadwriteSplittingInspectionService inspectionService,
                                                  final ReadwriteSplittingRuleDistSQLPlanningService distSQLPlanningService) {
        this.inspectionService = inspectionService;
        algorithmRecommendationService = new ReadwriteSplittingAlgorithmRecommendationService();
        algorithmPropertyTemplateService = new ReadwriteSplittingAlgorithmPropertyTemplateService();
        this.distSQLPlanningService = distSQLPlanningService;
    }
    
    /**
     * Plan readwrite-splitting rule workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade query facade
     * @param sessionId session id
     * @param request workflow request
     * @return workflow snapshot
     */
    public WorkflowContextSnapshot plan(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade, final String sessionId,
                                        final ReadwriteSplittingRuleWorkflowRequest request) {
        WorkflowContextSnapshot result = workflowSessionContext.getOrCreate(sessionId, request.getPlanId());
        ReadwriteSplittingRuleWorkflowRequest mergedRequest = prepareSnapshot(result, request);
        ClarifiedIntent clarifiedIntent = result.getClarifiedIntent();
        planningSupport.applyResolvedIntent(mergedRequest, clarifiedIntent);
        if (!ensurePlanningContext(mergedRequest, clarifiedIntent, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        String databaseType = queryFacade.getDatabaseType(mergedRequest.getDatabase());
        List<Map<String, Object>> existingRules = inspectionService.queryRules(queryFacade, mergedRequest.getDatabase());
        if (!ensureLifecycleState(clarifiedIntent, mergedRequest, existingRules, result, databaseType)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        if (!WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(clarifiedIntent.getOperationType())) {
            planAlgorithms(queryFacade, mergedRequest, result);
            if (!mergedRequest.getLoadBalancerType().isEmpty() && !planningSupport.isReadyForArtifactPlanning(mergedRequest, clarifiedIntent, result, findPropertyRequirements(mergedRequest),
                    "Please use a load-balance algorithm visible in the current Proxy and provide required properties.")) {
                return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
            }
        }
        addRuleArtifact(result, mergedRequest, clarifiedIntent.getOperationType());
        return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    private ReadwriteSplittingRuleWorkflowRequest prepareSnapshot(final WorkflowContextSnapshot snapshot, final ReadwriteSplittingRuleWorkflowRequest request) {
        ReadwriteSplittingRuleWorkflowRequest result = ReadwriteSplittingRuleWorkflowRequest.merge(snapshot.getRequest(), request);
        return planningSupport.prepareSnapshot(snapshot, ReadwriteSplittingFeatureDefinition.RULE_WORKFLOW_KIND, result, null,
                intentResolver.resolveRuleIntent(result), "Readwrite-splitting rule workflow plan.", INTERACTION_STEPS, VALIDATION_LAYERS);
    }
    
    private boolean ensurePlanningContext(final ReadwriteSplittingRuleWorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        if (request.getDatabase().isEmpty()) {
            clarifiedIntent.getClarificationMessages().add("Please provide logical database first.");
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DATABASE_REQUIRED, "error", "intaking",
                    "Database is required before planning readwrite-splitting rule DistSQL.", "Provide the logical database name.", true, Map.of()));
            snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
            return false;
        }
        if (!ensureSupportedIdentifier("database", request.getDatabase(), snapshot) || !ensureSupportedIdentifier(ReadwriteSplittingFeatureDefinition.RULE_FIELD, request.getRuleName(), snapshot)
                || !ensureSupportedIdentifier(ReadwriteSplittingFeatureDefinition.WRITE_STORAGE_UNIT_FIELD, request.getWriteStorageUnit(), snapshot)
                || !ensureSupportedIdentifiers(ReadwriteSplittingFeatureDefinition.READ_STORAGE_UNITS_FIELD, request.getReadStorageUnits(), snapshot)) {
            snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
            return false;
        }
        addMissingInputs(request, clarifiedIntent, snapshot);
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            return !request.getDatabase().isEmpty() && !request.getRuleName().isEmpty();
        }
        return !request.getRuleName().isEmpty() && !request.getWriteStorageUnit().isEmpty() && !request.getReadStorageUnits().isEmpty()
                && !request.getTransactionalReadQueryStrategy().isEmpty();
    }
    
    private void addMissingInputs(final ReadwriteSplittingRuleWorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        List<String> missingInputs = new LinkedList<>();
        addMissingInput(missingInputs, request.getRuleName(), ReadwriteSplittingFeatureDefinition.RULE_FIELD);
        if (!WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            addMissingInput(missingInputs, request.getWriteStorageUnit(), ReadwriteSplittingFeatureDefinition.WRITE_STORAGE_UNIT_FIELD);
            if (request.getReadStorageUnits().isEmpty()) {
                missingInputs.add(ReadwriteSplittingFeatureDefinition.READ_STORAGE_UNITS_FIELD);
            }
            addMissingInput(missingInputs, request.getTransactionalReadQueryStrategy(), ReadwriteSplittingFeatureDefinition.TRANSACTIONAL_READ_QUERY_STRATEGY_FIELD);
        }
        if (!missingInputs.isEmpty()) {
            clarifiedIntent.getClarificationMessages().add("Please provide readwrite-splitting rule name, storage units and strategy fields required by DistSQL.");
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_INPUT_REQUIRED, "error", "intaking",
                    "Readwrite-splitting rule DistSQL requires explicit structured inputs.", "Provide the missing fields instead of relying on inferred storage units.", true,
                    Map.of("missing_inputs", missingInputs)));
            snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
        }
    }
    
    private void addMissingInput(final Collection<String> missingInputs, final String value, final String fieldName) {
        if (value.isEmpty()) {
            missingInputs.add(fieldName);
        }
    }
    
    private boolean ensureSupportedIdentifiers(final String fieldName, final Collection<String> identifiers, final WorkflowContextSnapshot snapshot) {
        for (String each : identifiers) {
            if (!ensureSupportedIdentifier(fieldName, each, snapshot)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean ensureSupportedIdentifier(final String fieldName, final String identifier, final WorkflowContextSnapshot snapshot) {
        if (identifier.isEmpty() || WorkflowSQLUtils.isSupportedIdentifier(identifier)) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER, "error", "intaking",
                String.format("%s identifier `%s` contains unsupported characters.", fieldName, identifier),
                "Use a reviewable logical identifier without NUL or line terminators.", false, Map.of("field", fieldName, "identifier", identifier)));
        return false;
    }
    
    private boolean ensureLifecycleState(final ClarifiedIntent clarifiedIntent, final ReadwriteSplittingRuleWorkflowRequest request, final List<Map<String, Object>> rules,
                                         final WorkflowContextSnapshot snapshot, final String databaseType) {
        boolean ruleExists = containsRule(rules, databaseType, request.getRuleName());
        String operationType = clarifiedIntent.getOperationType().toLowerCase(Locale.ENGLISH);
        if ("create".equals(operationType) && ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
                    String.format("Readwrite-splitting rule `%s` already exists.", request.getRuleName()), "Use alter instead of create.", false, Map.of("rule", request.getRuleName())));
            return false;
        }
        if ("alter".equals(operationType) && !ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
                    String.format("Readwrite-splitting rule `%s` does not exist.", request.getRuleName()), "Use create instead of alter.", false, Map.of("rule", request.getRuleName())));
            return false;
        }
        if (WorkflowLifecycle.OPERATION_DROP.equals(operationType) && !ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DROP_TARGET_RULE_NOT_FOUND, "error", "discovering",
                    String.format("Readwrite-splitting rule `%s` does not exist.", request.getRuleName()), "Confirm target rule or skip the drop request.", false,
                    Map.of("rule", request.getRuleName())));
            return false;
        }
        return true;
    }
    
    private boolean containsRule(final List<Map<String, Object>> rules, final String databaseType, final String ruleName) {
        return rules.stream().anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, ruleName, WorkflowRuleValueUtils.getRuleValue(each, "name")));
    }
    
    private void planAlgorithms(final MCPFeatureQueryFacade queryFacade, final ReadwriteSplittingRuleWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        List<AlgorithmCandidate> algorithmCandidates = algorithmRecommendationService.recommendLoadBalanceAlgorithms(
                request, inspectionService.queryLoadBalanceAlgorithmPlugins(queryFacade), snapshot.getIssues());
        snapshot.getAlgorithmCandidates().addAll(algorithmCandidates);
        if (!request.getLoadBalancerType().isEmpty() && !algorithmCandidates.isEmpty()) {
            request.setLoadBalancerType(algorithmCandidates.getFirst().getAlgorithmType());
        }
    }
    
    private List<AlgorithmPropertyRequirement> findPropertyRequirements(final ReadwriteSplittingRuleWorkflowRequest request) {
        return algorithmPropertyTemplateService.findRequirements(request.getLoadBalancerType(), request.getReadStorageUnits());
    }
    
    private void addRuleArtifact(final WorkflowContextSnapshot snapshot, final ReadwriteSplittingRuleWorkflowRequest request, final String operationType) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(operationType)) {
            snapshot.getRuleArtifacts().add(distSQLPlanningService.planDropRule(request.getRuleName()));
        } else if ("alter".equalsIgnoreCase(operationType)) {
            snapshot.getRuleArtifacts().add(distSQLPlanningService.planAlterRule(request));
        } else {
            snapshot.getRuleArtifacts().add(distSQLPlanningService.planCreateRule(request));
        }
    }
}
