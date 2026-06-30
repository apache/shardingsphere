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

import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Sharding workflow planning kernel.
 */
final class ShardingWorkflowPlanningKernel {
    
    private static final List<String> INTERACTION_STEPS = List.of(
            "Confirm logical database and sharding intent",
            "Inspect DistSQL-visible sharding state",
            "Generate reviewable sharding DistSQL artifact",
            "Review artifact and choose execution mode",
            "Execute or export artifact",
            "Validate DistSQL-visible state");
    
    private static final List<String> VALIDATION_LAYERS = List.of("rules", "algorithms", "key-generators", "auditors");
    
    private final WorkflowPlanningSupport planningSupport = new WorkflowPlanningSupport();
    
    private final ShardingInspectionService inspectionService;
    
    private final ShardingAlgorithmRecommendationService algorithmRecommendationService;
    
    private final ShardingAlgorithmPropertyTemplateService algorithmPropertyTemplateService;
    
    private final ShardingDistSQLPlanningService distSQLPlanningService;
    
    ShardingWorkflowPlanningKernel() {
        inspectionService = new ShardingInspectionService();
        algorithmRecommendationService = new ShardingAlgorithmRecommendationService();
        algorithmPropertyTemplateService = new ShardingAlgorithmPropertyTemplateService();
        distSQLPlanningService = new ShardingDistSQLPlanningService();
    }
    
    ShardingWorkflowPlanningKernel(final ShardingInspectionService inspectionService, final ShardingDistSQLPlanningService distSQLPlanningService) {
        this.inspectionService = inspectionService;
        algorithmRecommendationService = new ShardingAlgorithmRecommendationService();
        algorithmPropertyTemplateService = new ShardingAlgorithmPropertyTemplateService();
        this.distSQLPlanningService = distSQLPlanningService;
    }
    
    /**
     * Plan sharding table rule workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param sessionId MCP session ID
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planTableRule(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                 final String sessionId, final ShardingWorkflowRequest request) {
        return planLifecycleWorkflow(workflowSessionContext, sessionId, request, ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                "create", "Sharding table rule workflow plan.", actual -> !inspectionService.queryTableRule(queryFacade, actual.getDatabase(), actual.getTable()).isEmpty(),
                this::hasRequiredTableRuleInputs, (actual, snapshot) -> planAlgorithms(queryFacade, actual,
                        shouldPlanShardingAlgorithm(actual), shouldPlanTableRuleKeyGenerator(actual), snapshot),
                actual -> distSQLPlanningService.planTableRule(actual, actual.getOperationType()));
    }
    
    /**
     * Plan sharding table reference rule workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param sessionId MCP session ID
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planTableReferenceRule(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                          final String sessionId, final ShardingWorkflowRequest request) {
        return planLifecycleWorkflow(workflowSessionContext, sessionId, request, ShardingFeatureDefinition.TABLE_REFERENCE_WORKFLOW_KIND,
                "create", "Sharding table reference rule workflow plan.", actual -> !inspectionService.queryTableReferenceRule(queryFacade, actual.getDatabase(), actual.getRuleName()).isEmpty(),
                this::hasRequiredReferenceRuleInputs, (actual, snapshot) -> true, actual -> distSQLPlanningService.planTableReferenceRule(actual, actual.getOperationType()));
    }
    
    /**
     * Plan default sharding strategy workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param sessionId MCP session ID
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planDefaultStrategy(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                       final String sessionId, final ShardingWorkflowRequest request) {
        return planLifecycleWorkflow(workflowSessionContext, sessionId, request, ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND,
                "create", "Default sharding strategy workflow plan.", actual -> containsDefaultStrategy(inspectionService.queryDefaultStrategy(queryFacade, actual.getDatabase()),
                        queryFacade.getDatabaseType(actual.getDatabase()), actual.getDefaultStrategyType()),
                this::hasRequiredDefaultStrategyInputs,
                (actual, snapshot) -> planAlgorithms(queryFacade, actual, shouldPlanShardingAlgorithm(actual), false, snapshot),
                actual -> distSQLPlanningService.planDefaultStrategy(actual, actual.getOperationType()));
    }
    
    /**
     * Plan sharding key generator workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param sessionId MCP session ID
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planKeyGenerator(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                    final String sessionId, final ShardingWorkflowRequest request) {
        return planLifecycleWorkflow(workflowSessionContext, sessionId, request, ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND,
                "create", "Sharding key generator workflow plan.", actual -> !inspectionService.queryKeyGenerator(queryFacade, actual.getDatabase(), actual.getKeyGeneratorName()).isEmpty(),
                this::hasRequiredKeyGeneratorInputs, (actual, snapshot) -> planAlgorithms(queryFacade, actual, false, true, snapshot),
                actual -> distSQLPlanningService.planKeyGenerator(actual, actual.getOperationType()));
    }
    
    /**
     * Plan sharding key generate strategy workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param sessionId MCP session ID
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planKeyGenerateStrategy(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                           final String sessionId, final ShardingWorkflowRequest request) {
        return planLifecycleWorkflow(workflowSessionContext, sessionId, request, ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND,
                "create", "Sharding key generate strategy workflow plan.",
                actual -> !inspectionService.queryKeyGenerateStrategy(queryFacade, actual.getDatabase(), actual.getKeyGenerateStrategyName()).isEmpty(),
                this::hasRequiredKeyGenerateStrategyInputs, (actual, snapshot) -> planAlgorithms(queryFacade, actual, false,
                        shouldPlanKeyGenerateStrategyGenerator(actual), snapshot),
                actual -> distSQLPlanningService.planKeyGenerateStrategy(actual, actual.getOperationType()));
    }
    
    /**
     * Plan unused sharding rule component cleanup workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param sessionId MCP session ID
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planComponentCleanup(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                        final String sessionId, final ShardingWorkflowRequest request) {
        WorkflowContextSnapshot result = prepareSnapshot(workflowSessionContext, sessionId, request, ShardingFeatureDefinition.COMPONENT_CLEANUP_WORKFLOW_KIND,
                WorkflowLifecycle.OPERATION_DROP, "Sharding rule component cleanup workflow plan.");
        ShardingWorkflowRequest mergedRequest = (ShardingWorkflowRequest) result.getRequest();
        if (!hasDatabase(mergedRequest, result) || !hasRequiredCleanupInputs(mergedRequest, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        if (!ensureCleanupDropOnly(mergedRequest, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        if (!isUnusedComponent(queryFacade, mergedRequest) || !queryUsedBy(queryFacade, mergedRequest).isEmpty()) {
            result.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
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
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.WORKFLOW_STATUS_INVALID, "error", "intaking",
                "Sharding component cleanup only supports dropping unused components.", "Use operation_type=drop or omit operation_type for cleanup.", false,
                Map.of("operation_type", request.getOperationType())));
        snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
        return false;
    }
    
    private WorkflowContextSnapshot planLifecycleWorkflow(final WorkflowSessionContext workflowSessionContext, final String sessionId, final ShardingWorkflowRequest request,
                                                          final WorkflowKind workflowKind, final String defaultOperationType, final String summary,
                                                          final Function<ShardingWorkflowRequest, Boolean> existsSupplier, final Function<ShardingWorkflowRequest, Boolean> requiredInputSupplier,
                                                          final BiFunction<ShardingWorkflowRequest, WorkflowContextSnapshot, Boolean> algorithmPlanSupplier,
                                                          final Function<ShardingWorkflowRequest, RuleArtifact> artifactSupplier) {
        WorkflowContextSnapshot result = prepareSnapshot(workflowSessionContext, sessionId, request, workflowKind, defaultOperationType, summary);
        ShardingWorkflowRequest mergedRequest = (ShardingWorkflowRequest) result.getRequest();
        if (!hasDatabase(mergedRequest, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        if (!ensureIdentifiers(mergedRequest, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, result.getStatus());
        }
        if (!requiredInputSupplier.apply(mergedRequest)) {
            result.getClarifiedIntent().getClarificationMessages().add(
                    mergedRequest.getFieldSemantics().isEmpty() ? "Please provide the missing sharding planning inputs." : mergedRequest.getFieldSemantics());
            result.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        if (!planningSupport.ensureLifecycleState(summary, result.getClarifiedIntent(), existsSupplier.apply(mergedRequest), result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        if (!algorithmPlanSupplier.apply(mergedRequest, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        result.getRuleArtifacts().add(artifactSupplier.apply(mergedRequest));
        return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    private WorkflowContextSnapshot prepareSnapshot(final WorkflowSessionContext workflowSessionContext, final String sessionId, final ShardingWorkflowRequest request,
                                                    final WorkflowKind workflowKind, final String defaultOperationType, final String summary) {
        WorkflowContextSnapshot result = workflowSessionContext.getOrCreate(sessionId, request.getPlanId());
        ShardingWorkflowRequest mergedRequest = ShardingWorkflowRequest.merge(result.getRequest(), request);
        ClarifiedIntent clarifiedIntent = resolveIntent(mergedRequest, defaultOperationType);
        planningSupport.prepareSnapshot(result, workflowKind, mergedRequest, null, clarifiedIntent, summary, INTERACTION_STEPS, VALIDATION_LAYERS);
        planningSupport.applyResolvedIntent(mergedRequest, clarifiedIntent);
        return result;
    }
    
    private ClarifiedIntent resolveIntent(final WorkflowRequest request, final String defaultOperationType) {
        ClarifiedIntent result = new ClarifiedIntent();
        result.setOperationType(request.getOperationType().isEmpty() ? defaultOperationType : request.getOperationType());
        result.setFieldSemantics("DistSQL-visible sharding rule fields only.");
        return result;
    }
    
    private boolean hasDatabase(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (!request.getDatabase().isEmpty()) {
            return true;
        }
        snapshot.getClarifiedIntent().getClarificationMessages().add("Please provide logical database first.");
        snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
        return false;
    }
    
    private boolean ensureIdentifiers(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (areSupportedIdentifiers(List.of(
                request.getDatabase(), request.getTable(), request.getColumn(), request.getRuleName(), request.getKeyGeneratorName(), request.getKeyGenerateStrategyName()))
                && areSupportedIdentifiers(splitCsv(request.getShardingColumns()))) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER, "error", "intaking",
                "A sharding identifier contains unsupported characters.", "Use reviewable DistSQL identifiers only.", false, Map.of()));
        snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
        return false;
    }
    
    private boolean hasRequiredTableRuleInputs(final ShardingWorkflowRequest request) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            return require(request, request.getTable(), "Please provide target logical table.");
        }
        if (!require(request, request.getTable(), "Please provide target logical table.")
                || !require(request, request.getDataNodes().isEmpty() ? request.getStorageUnits() : request.getDataNodes(), "Please provide data nodes or storage units.")) {
            return false;
        }
        return (request.getStorageUnits().isEmpty() ? hasRequiredStrategyInputs(request) : hasRequiredAutoTableRuleInputs(request))
                && hasRequiredTableRuleKeyGenerateInputs(request);
    }
    
    private boolean hasRequiredAutoTableRuleInputs(final ShardingWorkflowRequest request) {
        return require(request, request.getColumn(), "Please provide auto table sharding column.")
                && require(request, request.getAlgorithmType(), "Please provide auto table sharding algorithm type.");
    }
    
    private boolean hasRequiredTableRuleKeyGenerateInputs(final ShardingWorkflowRequest request) {
        if (request.getKeyGenerateColumn().isEmpty() || !request.getKeyGeneratorName().isEmpty()) {
            return true;
        }
        return require(request, request.getKeyGeneratorType(), "Please provide key generator type or key generator name for key generate strategy.");
    }
    
    private boolean hasRequiredReferenceRuleInputs(final ShardingWorkflowRequest request) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            return require(request, request.getRuleName(), "Please provide table reference rule name.");
        }
        return require(request, request.getRuleName(), "Please provide table reference rule name.")
                && require(request, request.getReferenceTables().isEmpty() ? "" : "ok", "Please provide reference tables.");
    }
    
    private boolean hasRequiredDefaultStrategyInputs(final ShardingWorkflowRequest request) {
        if (!require(request, request.getDefaultStrategyType(), "Please provide DATABASE or TABLE default strategy type.")) {
            return false;
        }
        if (!isDefaultStrategyType(request.getDefaultStrategyType())) {
            request.setFieldSemantics("Please provide default_strategy_type as DATABASE or TABLE.");
            return false;
        }
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType()) || "none".equalsIgnoreCase(request.getStrategyType())) {
            return true;
        }
        return hasRequiredStrategyInputs(request);
    }
    
    private boolean isDefaultStrategyType(final String defaultStrategyType) {
        return "DATABASE".equalsIgnoreCase(defaultStrategyType) || "TABLE".equalsIgnoreCase(defaultStrategyType);
    }
    
    private boolean hasRequiredStrategyInputs(final ShardingWorkflowRequest request) {
        switch (normalizeStrategyType(request)) {
            case "standard":
                return require(request, request.getColumn(), "Please provide sharding column.")
                        && require(request, request.getAlgorithmType(), "Please provide sharding algorithm type.");
            case "complex":
                return hasRequiredComplexStrategyInputs(request);
            case "hint":
                return require(request, request.getAlgorithmType(), "Please provide sharding algorithm type.");
            case "none":
                return true;
            default:
                request.setFieldSemantics("Please provide strategy_type as standard, complex, hint, or none.");
                return false;
        }
    }
    
    private boolean hasRequiredComplexStrategyInputs(final ShardingWorkflowRequest request) {
        if (!require(request, request.getShardingColumns(), "Please provide at least two sharding columns for complex strategy.")) {
            return false;
        }
        if (splitCsv(request.getShardingColumns()).size() < 2) {
            request.setFieldSemantics("Please provide at least two sharding columns for complex strategy.");
            return false;
        }
        return require(request, request.getAlgorithmType(), "Please provide sharding algorithm type.");
    }
    
    private boolean hasRequiredKeyGeneratorInputs(final ShardingWorkflowRequest request) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            return require(request, request.getKeyGeneratorName(), "Please provide key generator name.");
        }
        return require(request, request.getKeyGeneratorName(), "Please provide key generator name.")
                && require(request, request.getKeyGeneratorType(), "Please provide key generator type.");
    }
    
    private boolean hasRequiredKeyGenerateStrategyInputs(final ShardingWorkflowRequest request) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            return require(request, request.getKeyGenerateStrategyName(), "Please provide key generate strategy name.");
        }
        return require(request, request.getKeyGenerateStrategyName(), "Please provide key generate strategy name.")
                && require(request, request.getSequenceName().isEmpty() ? request.getTable() : request.getSequenceName(), "Please provide table or sequence.")
                && require(request, request.getSequenceName().isEmpty() ? request.getColumn() : "ok", "Please provide key generate column.")
                && require(request, request.getKeyGeneratorName().isEmpty() ? request.getKeyGeneratorType() : request.getKeyGeneratorName(), "Please provide generator or key generator type.");
    }
    
    private boolean hasRequiredCleanupInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (!request.getComponentType().isEmpty() && !request.getComponentName().isEmpty()) {
            return true;
        }
        snapshot.getClarifiedIntent().getClarificationMessages().add("Please provide component type and component name.");
        snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
        return false;
    }
    
    private boolean require(final ShardingWorkflowRequest request, final String value, final String message) {
        if (!value.isEmpty()) {
            return true;
        }
        request.setFieldSemantics(message);
        return false;
    }
    
    private boolean containsDefaultStrategy(final List<Map<String, Object>> rows, final String databaseType, final String defaultStrategyType) {
        return rows.stream().anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(
                databaseType, defaultStrategyType, WorkflowRuleValueUtils.getRuleValue(each, "name")) && !WorkflowRuleValueUtils.getRuleValue(each, "type").isEmpty());
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
        return !WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType()) && !"none".equalsIgnoreCase(normalizeStrategyType(request));
    }
    
    private boolean shouldPlanTableRuleKeyGenerator(final ShardingWorkflowRequest request) {
        return !WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType()) && !request.getKeyGenerateColumn().isEmpty()
                && request.getKeyGeneratorName().isEmpty();
    }
    
    private boolean shouldPlanKeyGenerateStrategyGenerator(final ShardingWorkflowRequest request) {
        return !WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType()) && request.getKeyGeneratorName().isEmpty();
    }
    
    private boolean isUnusedComponent(final MCPFeatureQueryFacade queryFacade, final ShardingWorkflowRequest request) {
        return switch (normalizeComponentType(request.getComponentType())) {
            case "algorithm" -> containsNamedRow(inspectionService.queryUnusedAlgorithms(queryFacade, request.getDatabase()),
                    "name", request.getComponentName(), queryFacade.getDatabaseType(request.getDatabase()));
            case "key-generator" -> containsNamedRow(inspectionService.queryUnusedKeyGenerators(queryFacade, request.getDatabase()), "name", request.getComponentName(),
                    queryFacade.getDatabaseType(request.getDatabase()));
            case "auditor" -> containsNamedRow(inspectionService.queryUnusedAuditors(queryFacade, request.getDatabase()),
                    "name", request.getComponentName(), queryFacade.getDatabaseType(request.getDatabase()));
            default -> false;
        };
    }
    
    private List<Map<String, Object>> queryUsedBy(final MCPFeatureQueryFacade queryFacade, final ShardingWorkflowRequest request) {
        switch (normalizeComponentType(request.getComponentType())) {
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
    
    private boolean containsNamedRow(final Collection<Map<String, Object>> rows, final String fieldName, final String expected, final String databaseType) {
        return rows.stream().anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, expected, WorkflowRuleValueUtils.getRuleValue(each, fieldName)));
    }
    
    private boolean areSupportedIdentifiers(final Collection<String> identifiers) {
        return identifiers.stream().allMatch(WorkflowSQLUtils::isSupportedIdentifier);
    }
    
    private List<String> splitCsv(final String value) {
        return Arrays.stream(value.split(",")).map(String::trim).filter(each -> !each.isEmpty()).toList();
    }
    
    private String normalizeStrategyType(final ShardingWorkflowRequest request) {
        return request.getStrategyType().isEmpty() ? "standard" : request.getStrategyType().trim().toLowerCase(Locale.ENGLISH);
    }
    
    private String normalizeComponentType(final String componentType) {
        return componentType.trim().toLowerCase(Locale.ENGLISH).replace('_', '-');
    }
}
