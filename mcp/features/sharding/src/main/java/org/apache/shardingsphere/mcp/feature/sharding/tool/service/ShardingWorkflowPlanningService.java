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

package org.apache.shardingsphere.mcp.feature.sharding.tool.service;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Sharding workflow planning service.
 */
public final class ShardingWorkflowPlanningService {
    
    private static final List<String> SUPPORTED_LIFECYCLE_OPERATION_TYPES = List.of(WorkflowLifecycle.OPERATION_CREATE, WorkflowLifecycle.OPERATION_DROP);
    
    private static final List<String> INTERACTION_STEPS = List.of(
            "Confirm logical database and sharding intent",
            "Inspect DistSQL-visible sharding state",
            "Generate reviewable sharding DistSQL artifact",
            "Review artifact and choose execution mode",
            "Execute or export artifact",
            "Validate DistSQL-visible state");
    
    private static final List<String> VALIDATION_LAYERS = List.of("rules", "algorithms", "key-generators", "auditors");
    
    private final WorkflowPlanningSupport planningSupport = new WorkflowPlanningSupport();
    
    private final ShardingWorkflowInputValidator inputValidator = new ShardingWorkflowInputValidator();
    
    private final ShardingInspectionService inspectionService = new ShardingInspectionService();
    
    private final ShardingAlgorithmRecommendationService algorithmRecommendationService = new ShardingAlgorithmRecommendationService();
    
    private final ShardingAlgorithmPropertyTemplateService algorithmPropertyTemplateService = new ShardingAlgorithmPropertyTemplateService();
    
    private final ShardingDistSQLPlanningService distSQLPlanningService = new ShardingDistSQLPlanningService();
    
    /**
     * Plan sharding table rule workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planTableRule(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                 final ShardingWorkflowRequest request) {
        return planLifecycleWorkflow(workflowSessionContext, request, new ShardingWorkflowLifecycleSpec(
                ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, WorkflowLifecycle.OPERATION_CREATE, "Sharding table rule workflow plan.",
                actual -> !inspectionService.queryTableRule(queryFacade, actual.getDatabase(), actual.getTable()).isEmpty(),
                inputValidator::ensureRequiredTableRuleInputs, (actual, snapshot) -> planAlgorithms(queryFacade, actual,
                        shouldPlanShardingAlgorithm(actual), shouldPlanTableRuleKeyGenerator(actual), snapshot),
                actual -> distSQLPlanningService.planTableRule(actual, actual.getOperationType())));
    }
    
    /**
     * Plan sharding table reference rule workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planTableReferenceRule(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                          final ShardingWorkflowRequest request) {
        return planLifecycleWorkflow(workflowSessionContext, request, new ShardingWorkflowLifecycleSpec(
                ShardingFeatureDefinition.TABLE_REFERENCE_WORKFLOW_KIND, WorkflowLifecycle.OPERATION_CREATE, "Sharding table reference rule workflow plan.",
                actual -> !inspectionService.queryTableReferenceRule(queryFacade, actual.getDatabase(), actual.getRuleName()).isEmpty(),
                inputValidator::ensureRequiredReferenceRuleInputs, (actual, snapshot) -> true, actual -> distSQLPlanningService.planTableReferenceRule(actual, actual.getOperationType())));
    }
    
    /**
     * Plan default sharding strategy workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planDefaultStrategy(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                       final ShardingWorkflowRequest request) {
        return planLifecycleWorkflow(workflowSessionContext, request, new ShardingWorkflowLifecycleSpec(
                ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND, WorkflowLifecycle.OPERATION_CREATE, "Default sharding strategy workflow plan.",
                actual -> containsDefaultStrategy(inspectionService.queryDefaultStrategy(queryFacade, actual.getDatabase()),
                        queryFacade, actual.getDatabase(), actual.getDefaultStrategyType()),
                inputValidator::ensureRequiredDefaultStrategyInputs,
                (actual, snapshot) -> planAlgorithms(queryFacade, actual, shouldPlanShardingAlgorithm(actual), false, snapshot),
                actual -> distSQLPlanningService.planDefaultStrategy(actual, actual.getOperationType())));
    }
    
    /**
     * Plan sharding key generator workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planKeyGenerator(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                    final ShardingWorkflowRequest request) {
        return planLifecycleWorkflow(workflowSessionContext, request, new ShardingWorkflowLifecycleSpec(
                ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND, WorkflowLifecycle.OPERATION_CREATE, "Sharding key generator workflow plan.",
                actual -> !inspectionService.queryKeyGenerator(queryFacade, actual.getDatabase(), actual.getKeyGeneratorName()).isEmpty(),
                inputValidator::ensureRequiredKeyGeneratorInputs, (actual, snapshot) -> planAlgorithms(queryFacade, actual, false, true, snapshot),
                actual -> distSQLPlanningService.planKeyGenerator(actual, actual.getOperationType())));
    }
    
    /**
     * Plan sharding key generate strategy workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planKeyGenerateStrategy(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                           final ShardingWorkflowRequest request) {
        return planLifecycleWorkflow(workflowSessionContext, request, new ShardingWorkflowLifecycleSpec(
                ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND, WorkflowLifecycle.OPERATION_CREATE, "Sharding key generate strategy workflow plan.",
                actual -> !inspectionService.queryKeyGenerateStrategy(queryFacade, actual.getDatabase(), actual.getKeyGenerateStrategyName()).isEmpty(),
                inputValidator::ensureRequiredKeyGenerateStrategyInputs, (actual, snapshot) -> planAlgorithms(queryFacade, actual, false,
                        shouldPlanKeyGenerateStrategyGenerator(actual), snapshot),
                actual -> distSQLPlanningService.planKeyGenerateStrategy(actual, actual.getOperationType())));
    }
    
    /**
     * Plan unused sharding rule component cleanup workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planComponentCleanup(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                        final ShardingWorkflowRequest request) {
        WorkflowContextSnapshot result = prepareSnapshot(workflowSessionContext, request, ShardingFeatureDefinition.COMPONENT_CLEANUP_WORKFLOW_KIND,
                WorkflowLifecycle.OPERATION_DROP, "Sharding rule component cleanup workflow plan.");
        ShardingWorkflowRequest mergedRequest = (ShardingWorkflowRequest) result.getRequest();
        if (!inputValidator.ensureDatabase(mergedRequest, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        if (!inputValidator.ensureRequiredCleanupInputs(mergedRequest, result)) {
            addRequiredInputClarification(mergedRequest, result);
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        if (!inputValidator.ensureCleanupIdentifiers(mergedRequest, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, result.getStatus());
        }
        if (!ensureCleanupDropOnly(mergedRequest, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        if (!isUnusedComponent(queryFacade, mergedRequest) || !queryUsedBy(queryFacade, mergedRequest).isEmpty()) {
            result.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", WorkflowLifecycle.STEP_DISCOVERING,
                    "Sharding component is still used or is not present in unused DistSQL-visible state.",
                    "Inspect unused and used-by resources before cleanup.", false, Map.of()));
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        result.getRuleArtifacts().add(distSQLPlanningService.planComponentCleanup(mergedRequest));
        return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    private boolean ensureCleanupDropOnly(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.WORKFLOW_STATUS_INVALID, "error", WorkflowLifecycle.STEP_INTAKING,
                "Sharding component cleanup only supports dropping unused components.", "Use operation_type=drop or omit operation_type for cleanup.", false,
                Map.of("operation_type", request.getOperationType())));
        snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
        return false;
    }
    
    private WorkflowContextSnapshot planLifecycleWorkflow(final WorkflowSessionContext workflowSessionContext, final ShardingWorkflowRequest request,
                                                          final ShardingWorkflowLifecycleSpec spec) {
        WorkflowContextSnapshot result = prepareSnapshot(workflowSessionContext, request, spec.getWorkflowKind(), spec.getDefaultOperationType(), spec.getSummary());
        ShardingWorkflowRequest mergedRequest = (ShardingWorkflowRequest) result.getRequest();
        if (!planningSupport.ensureSupportedOperationType(result.getClarifiedIntent(), SUPPORTED_LIFECYCLE_OPERATION_TYPES, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        if (!inputValidator.ensureDatabase(mergedRequest, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        if (!inputValidator.ensureIdentifiers(mergedRequest, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, result.getStatus());
        }
        if (!inputValidator.ensureCompatibleInputs(spec.getWorkflowKind(), mergedRequest, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, result.getStatus());
        }
        if (!spec.getRequiredInputSupplier().apply(mergedRequest, result)) {
            addRequiredInputClarification(mergedRequest, result);
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        if (!planningSupport.ensureLifecycleState(spec.getSummary(), result.getClarifiedIntent(), spec.getExistsSupplier().apply(mergedRequest), result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        if (!spec.getAlgorithmPlanSupplier().apply(mergedRequest, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        result.getRuleArtifacts().add(spec.getArtifactSupplier().apply(mergedRequest));
        return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    private void addRequiredInputClarification(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        inputValidator.addRequiredInputIssue(request, snapshot);
        snapshot.getClarifiedIntent().getClarificationMessages().add(
                request.getFieldSemantics().isEmpty() ? "Please provide the missing sharding planning inputs." : request.getFieldSemantics());
    }
    
    private WorkflowContextSnapshot prepareSnapshot(final WorkflowSessionContext workflowSessionContext, final ShardingWorkflowRequest request,
                                                    final WorkflowKind workflowKind, final String defaultOperationType, final String summary) {
        WorkflowContextSnapshot result = workflowSessionContext.getOrCreate(request.getPlanId());
        ShardingWorkflowRequest mergedRequest = ShardingWorkflowRequest.merge(result.getRequest(), request);
        ClarifiedIntent clarifiedIntent = resolveIntent(mergedRequest, defaultOperationType);
        planningSupport.prepareSnapshot(result, workflowKind, mergedRequest, null, clarifiedIntent, summary, INTERACTION_STEPS, VALIDATION_LAYERS);
        if (ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND.equals(workflowKind)) {
            result.getResourceUriTemplates().addAll(List.of(ShardingFeatureDefinition.STORAGE_UNITS_RESOURCE_URI,
                    ShardingFeatureDefinition.SINGLE_TABLES_RESOURCE_URI, ShardingFeatureDefinition.SINGLE_TABLE_RESOURCE_URI));
        }
        planningSupport.applyResolvedIntent(mergedRequest, clarifiedIntent);
        return result;
    }
    
    private ClarifiedIntent resolveIntent(final WorkflowRequest request, final String defaultOperationType) {
        ClarifiedIntent result = new ClarifiedIntent();
        result.setOperationType(request.getOperationType().isEmpty() ? defaultOperationType : request.getOperationType());
        result.setFieldSemantics("DistSQL-visible sharding rule fields only.");
        return result;
    }
    
    private boolean containsDefaultStrategy(final List<Map<String, Object>> rows, final MCPFeatureQueryFacade queryFacade,
                                            final String databaseName, final String defaultStrategyType) {
        queryFacade.checkDatabaseCapability(databaseName);
        return rows.stream().anyMatch(each -> queryFacade.isSameIdentifier(
                databaseName, IdentifierScope.TABLE, defaultStrategyType, WorkflowRuleValueUtils.getRuleValue(each, "name"))
                && !WorkflowRuleValueUtils.getRuleValue(each, "type").isEmpty());
    }
    
    private boolean planAlgorithms(final MCPFeatureQueryFacade queryFacade, final ShardingWorkflowRequest request, final boolean includeShardingAlgorithm,
                                   final boolean includeKeyGenerator, final WorkflowContextSnapshot snapshot) {
        if (!includeShardingAlgorithm && !includeKeyGenerator) {
            return true;
        }
        List<AlgorithmCandidate> algorithmCandidates = algorithmRecommendationService.recommend(
                request, inspectionService.queryAlgorithmPlugins(queryFacade), inspectionService.queryKeyGenerateAlgorithmPlugins(queryFacade),
                includeShardingAlgorithm, includeKeyGenerator, snapshot.getIssues());
        snapshot.getAlgorithmCandidates().addAll(algorithmCandidates);
        applyRecommendedAlgorithms(request, algorithmCandidates);
        return planningSupport.isReadyForArtifactPlanning(request, snapshot.getClarifiedIntent(), snapshot,
                findPropertyRequirements(request, includeShardingAlgorithm, includeKeyGenerator),
                "Please use sharding algorithms visible in the current Proxy and provide required properties.");
    }
    
    private void applyRecommendedAlgorithms(final ShardingWorkflowRequest request, final List<AlgorithmCandidate> algorithmCandidates) {
        for (AlgorithmCandidate each : algorithmCandidates) {
            if ("primary".equals(each.getAlgorithmRole())) {
                request.setAlgorithmType(each.getAlgorithmType());
            } else if ("key_generator".equals(each.getAlgorithmRole())) {
                request.setKeyGeneratorType(each.getAlgorithmType());
            }
        }
    }
    
    private List<AlgorithmPropertyRequirement> findPropertyRequirements(final ShardingWorkflowRequest request, final boolean includeShardingAlgorithm,
                                                                        final boolean includeKeyGenerator) {
        if (includeShardingAlgorithm && includeKeyGenerator) {
            return algorithmPropertyTemplateService.findRequirements(request.getAlgorithmType(), request.getKeyGeneratorType());
        }
        return includeShardingAlgorithm
                ? algorithmPropertyTemplateService.findAlgorithmRequirements(request.getAlgorithmType())
                : algorithmPropertyTemplateService.findKeyGeneratorRequirements(request.getKeyGeneratorType());
    }
    
    private boolean shouldPlanShardingAlgorithm(final ShardingWorkflowRequest request) {
        return !WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType()) && !"none".equalsIgnoreCase(inputValidator.normalizeStrategyType(request));
    }
    
    private boolean shouldPlanTableRuleKeyGenerator(final ShardingWorkflowRequest request) {
        return !WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType()) && !request.getKeyGenerateColumn().isEmpty()
                && request.getKeyGeneratorName().isEmpty();
    }
    
    private boolean shouldPlanKeyGenerateStrategyGenerator(final ShardingWorkflowRequest request) {
        return !WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType()) && request.getKeyGeneratorName().isEmpty();
    }
    
    private boolean isUnusedComponent(final MCPFeatureQueryFacade queryFacade, final ShardingWorkflowRequest request) {
        return switch (inputValidator.normalizeComponentType(request.getComponentType())) {
            case "algorithm" -> containsNamedRow(inspectionService.queryUnusedAlgorithms(queryFacade, request.getDatabase()),
                    queryFacade, request.getDatabase(), "name", request.getComponentName());
            case "key-generator" -> containsNamedRow(inspectionService.queryUnusedKeyGenerators(queryFacade, request.getDatabase()),
                    queryFacade, request.getDatabase(), "name", request.getComponentName());
            case "auditor" -> containsNamedRow(inspectionService.queryUnusedAuditors(queryFacade, request.getDatabase()),
                    queryFacade, request.getDatabase(), "name", request.getComponentName());
            default -> false;
        };
    }
    
    private List<Map<String, Object>> queryUsedBy(final MCPFeatureQueryFacade queryFacade, final ShardingWorkflowRequest request) {
        switch (inputValidator.normalizeComponentType(request.getComponentType())) {
            case "algorithm":
                return inspectionService.queryTableRulesUsedAlgorithm(queryFacade, request.getDatabase(), request.getComponentName());
            case "key-generator":
                return inspectionService.queryTableRulesUsedKeyGenerator(queryFacade, request.getDatabase(), request.getComponentName());
            case "auditor":
                return inspectionService.queryTableRulesUsedAuditor(queryFacade, request.getDatabase(), request.getComponentName());
            default:
                return List.of(Map.of("unsupported_component_type", request.getComponentType()));
        }
    }
    
    private boolean containsNamedRow(final Collection<Map<String, Object>> rows, final MCPFeatureQueryFacade queryFacade, final String databaseName,
                                     final String fieldName, final String expected) {
        queryFacade.checkDatabaseCapability(databaseName);
        return rows.stream().anyMatch(each -> queryFacade.isSameIdentifier(databaseName, IdentifierScope.TABLE, expected, WorkflowRuleValueUtils.getRuleValue(each, fieldName)));
    }
    
}
