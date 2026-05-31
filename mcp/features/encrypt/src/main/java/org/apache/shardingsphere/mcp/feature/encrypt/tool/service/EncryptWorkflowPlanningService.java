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

import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowState;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.DDLArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactPayloadUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningSupport;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Encrypt workflow planning service.
 */
public final class EncryptWorkflowPlanningService {
    
    private static final List<String> INTERACTION_STEPS = List.of(
            "Confirm database, table, column and target lifecycle",
            "Inspect existing encrypt rules and logical metadata",
            "Clarify missing encrypt requirements and choose algorithms",
            "Collect algorithm properties and create derived naming plan",
            "Generate DDL, DistSQL and index artifacts",
            "Review artifacts and choose execution mode",
            "Execute or export artifacts",
            "Validate and summarize");
    
    private static final List<String> VALIDATION_LAYERS = List.of(WorkflowArtifactPayloadUtils.STEP_DDL, "rules", "logical_metadata", "sql_executability");
    
    private final WorkflowPlanningSupport planningSupport;
    
    private final EncryptWorkflowIntentResolver intentResolver;
    
    private final EncryptRuleInspectionService ruleInspectionService;
    
    private final EncryptAlgorithmRecommendationService algorithmRecommendationService;
    
    private final EncryptAlgorithmPropertyTemplateService algorithmPropertyTemplateService;
    
    private final DerivedColumnNamingService derivedColumnNamingService;
    
    private final PhysicalDDLPlanningService physicalDDLPlanningService;
    
    private final IndexPlanningService indexPlanningService;
    
    private final EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService;
    
    /**
     * Create encrypt workflow planning service.
     */
    public EncryptWorkflowPlanningService() {
        planningSupport = new WorkflowPlanningSupport();
        intentResolver = new EncryptWorkflowIntentResolver();
        ruleInspectionService = new EncryptRuleInspectionService();
        algorithmRecommendationService = new EncryptAlgorithmRecommendationService();
        algorithmPropertyTemplateService = new EncryptAlgorithmPropertyTemplateService();
        derivedColumnNamingService = new DerivedColumnNamingService();
        physicalDDLPlanningService = new PhysicalDDLPlanningService();
        indexPlanningService = new IndexPlanningService();
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
    public WorkflowContextSnapshot plan(final WorkflowSessionContext workflowSessionContext, final MCPMetadataQueryFacade metadataQueryFacade,
                                        final MCPFeatureQueryFacade queryFacade, final String sessionId, final EncryptWorkflowRequest request) {
        WorkflowContextSnapshot result = workflowSessionContext.getOrCreate(sessionId, request.getPlanId());
        EncryptWorkflowRequest mergedRequest = prepareSnapshot(result, request);
        ClarifiedIntent clarifiedIntent = result.getClarifiedIntent();
        planningSupport.applyResolvedIntent(mergedRequest, clarifiedIntent);
        if (!planningSupport.ensurePlanningContext(metadataQueryFacade, mergedRequest, clarifiedIntent, result)) {
            String currentStep = WorkflowLifecycle.STATUS_FAILED.equals(result.getStatus()) ? WorkflowLifecycle.STEP_FAILED : WorkflowLifecycle.STEP_CLARIFYING;
            return workflowSessionContext.persist(result, currentStep, result.getStatus());
        }
        String databaseType = queryFacade.getDatabaseType(mergedRequest.getDatabase());
        List<Map<String, Object>> existingRules = ruleInspectionService.queryEncryptRules(queryFacade, mergedRequest.getDatabase(), mergedRequest.getTable());
        if (!ensureLifecycleState(clarifiedIntent, mergedRequest, existingRules, result, databaseType)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        EncryptWorkflowState workflowState = getWorkflowState(result);
        if (isDropWorkflow(clarifiedIntent)) {
            planDrop(metadataQueryFacade, queryFacade, workflowState, clarifiedIntent, mergedRequest, existingRules, result, databaseType);
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
        }
        if (!planNonDrop(metadataQueryFacade, queryFacade, workflowState, clarifiedIntent, mergedRequest, existingRules, result, databaseType)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    private EncryptWorkflowRequest prepareSnapshot(final WorkflowContextSnapshot snapshot, final EncryptWorkflowRequest request) {
        EncryptWorkflowRequest result = EncryptWorkflowRequest.merge(snapshot.getRequest(), request);
        EncryptWorkflowState workflowState = getWorkflowState(snapshot);
        workflowState.setDerivedColumnPlan(null);
        return planningSupport.prepareSnapshot(snapshot, EncryptFeatureDefinition.WORKFLOW_KIND, result, workflowState,
                intentResolver.resolve(result), "Encrypt workflow plan.", INTERACTION_STEPS, VALIDATION_LAYERS);
    }
    
    private EncryptWorkflowState getWorkflowState(final WorkflowContextSnapshot snapshot) {
        return snapshot.getFeatureData() instanceof EncryptWorkflowState ? (EncryptWorkflowState) snapshot.getFeatureData() : new EncryptWorkflowState();
    }
    
    private boolean ensureLifecycleState(final ClarifiedIntent clarifiedIntent, final EncryptWorkflowRequest request,
                                         final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot, final String databaseType) {
        boolean ruleExists = encryptRules.stream().anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, request.getColumn(), WorkflowRuleValueUtils.getRuleValue(each, "logic_column")));
        return planningSupport.ensureLifecycleState("Encrypt rule", clarifiedIntent, ruleExists, snapshot);
    }
    
    private boolean isDropWorkflow(final ClarifiedIntent clarifiedIntent) {
        return WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(clarifiedIntent.getOperationType());
    }
    
    private void planDrop(final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade, final EncryptWorkflowState workflowState,
                          final ClarifiedIntent clarifiedIntent, final EncryptWorkflowRequest request, final List<Map<String, Object>> existingRules,
                          final WorkflowContextSnapshot snapshot, final String databaseType) {
        addLifecycleWarnings(request, clarifiedIntent, existingRules, snapshot, databaseType);
        planArtifacts(metadataQueryFacade, queryFacade, workflowState, clarifiedIntent, request, existingRules, snapshot, databaseType);
    }
    
    private boolean planNonDrop(final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade, final EncryptWorkflowState workflowState,
                                final ClarifiedIntent clarifiedIntent, final EncryptWorkflowRequest request, final List<Map<String, Object>> existingRules,
                                final WorkflowContextSnapshot snapshot, final String databaseType) {
        planAlgorithms(queryFacade, request, snapshot);
        if (!planningSupport.isReadyForArtifactPlanning(request, clarifiedIntent, snapshot, findPropertyRequirements(request),
                "Please use an encrypt algorithm that is visible in the current Proxy and satisfies the requirements.")) {
            return false;
        }
        if (!ensureSupportedAlterExpansion(clarifiedIntent, request, existingRules, snapshot, databaseType)) {
            return false;
        }
        planArtifacts(metadataQueryFacade, queryFacade, workflowState, clarifiedIntent, request, existingRules, snapshot, databaseType);
        return true;
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
    
    private void addLifecycleWarnings(final EncryptWorkflowRequest request, final ClarifiedIntent clarifiedIntent,
                                      final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot, final String databaseType) {
        if (isDropWorkflow(clarifiedIntent)) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.ENCRYPT_DROP_SCOPE_LIMITED, "warning", "planning-artifacts",
                    "Encrypt drop only removes the rule. MCP will not restore historical plaintext data.", "Review business impact before execution.", true, Map.of()));
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.PHYSICAL_CLEANUP_REQUIRED, "warning", "planning-artifacts",
                    "Encrypt drop does not clean up pre-existing physical derived columns or indexes in V1.",
                    "Clean up obsolete physical artifacts manually if they are no longer needed.", true, Map.of()));
            return;
        }
        addShrinkAlterCleanupWarning(request, clarifiedIntent, encryptRules, snapshot, databaseType);
    }
    
    private boolean ensureSupportedAlterExpansion(final ClarifiedIntent clarifiedIntent, final EncryptWorkflowRequest request,
                                                  final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot, final String databaseType) {
        Optional<Map<String, Object>> existingRule = findEncryptRule(encryptRules, request.getColumn(), databaseType);
        boolean addsLogicColumn = existingRule.isEmpty() && !encryptRules.isEmpty();
        if (!addsLogicColumn && !"alter".equalsIgnoreCase(clarifiedIntent.getOperationType())) {
            return true;
        }
        if (!addsLogicColumn && existingRule.isEmpty()) {
            return true;
        }
        boolean addsAssistedQuery = existingRule.isPresent() && Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter())
                && WorkflowRuleValueUtils.getRuleValue(existingRule.get(), "assisted_query_column").isEmpty();
        boolean addsLikeQuery = existingRule.isPresent() && Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery())
                && WorkflowRuleValueUtils.getRuleValue(existingRule.get(), "like_query_column").isEmpty();
        if (!addsLogicColumn && !addsAssistedQuery && !addsLikeQuery) {
            return true;
        }
        snapshot.getClarifiedIntent().getClarificationMessages().add(
                "Current Proxy DistSQL cannot automatically expand an existing encrypt table rule with new logic columns, assisted-query bindings, or LIKE-query bindings. "
                        + "Recreate the rule manually during a maintenance window.");
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.ENCRYPT_ALTER_SCOPE_LIMITED, "error", "planning-artifacts",
                "Encrypt planning cannot expand an existing table rule with new logic columns, assisted-query bindings, or LIKE-query bindings in V1.",
                "Manually recreate the encrypt rule with the complete column set after reviewing data impact.", true,
                Map.of("adds_logic_column", addsLogicColumn, "adds_assisted_query", addsAssistedQuery, "adds_like_query", addsLikeQuery)));
        return false;
    }
    
    private void planArtifacts(final MCPMetadataQueryFacade metadataQueryService, final MCPFeatureQueryFacade queryFacade, final EncryptWorkflowState workflowState,
                               final ClarifiedIntent clarifiedIntent, final EncryptWorkflowRequest request, final List<Map<String, Object>> encryptRules,
                               final WorkflowContextSnapshot snapshot, final String databaseType) {
        if (isDropWorkflow(clarifiedIntent)) {
            snapshot.getRuleArtifacts().addAll(ruleDistSQLPlanningService.planEncryptDropRule(request, encryptRules, databaseType));
            return;
        }
        planEncryptArtifacts(metadataQueryService, queryFacade, workflowState, clarifiedIntent, request, encryptRules, snapshot, databaseType);
    }
    
    private void planEncryptArtifacts(final MCPMetadataQueryFacade metadataQueryService, final MCPFeatureQueryFacade queryFacade, final EncryptWorkflowState workflowState,
                                      final ClarifiedIntent clarifiedIntent, final EncryptWorkflowRequest request, final List<Map<String, Object>> encryptRules,
                                      final WorkflowContextSnapshot snapshot, final String databaseType) {
        Set<String> existingPhysicalNames = createExistingPhysicalNames(metadataQueryService, request, databaseType);
        Set<String> reservedNames = createReservedNames(existingPhysicalNames, encryptRules);
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan(request, encryptRules, snapshot, databaseType, reservedNames);
        workflowState.setDerivedColumnPlan(derivedColumnPlan);
        addShrinkAlterCleanupWarning(request, clarifiedIntent, encryptRules, snapshot, databaseType);
        String derivedColumnDefinition = resolveDerivedColumnDefinition(queryFacade, request, snapshot);
        List<DDLArtifact> ddlArtifacts = physicalDDLPlanningService.planAddColumnArtifacts(databaseType, request.getTable(), derivedColumnPlan, existingPhysicalNames, derivedColumnDefinition);
        snapshot.getDdlArtifacts().addAll(ddlArtifacts);
        if (!Boolean.FALSE.equals(request.getOptions().getAllowIndexDDL())) {
            snapshot.getIndexPlans().addAll(indexPlanningService.planIndexes(databaseType, request.getTable(), derivedColumnPlan, createExistingIndexes(metadataQueryService, request, databaseType)));
        }
        snapshot.getRuleArtifacts().addAll(ruleDistSQLPlanningService.planEncryptRule(request, derivedColumnPlan, encryptRules, databaseType));
    }
    
    private void applyRecommendedAlgorithms(final EncryptWorkflowRequest request, final List<AlgorithmCandidate> algorithmCandidates) {
        for (AlgorithmCandidate each : algorithmCandidates) {
            if ("primary".equals(each.getAlgorithmRole())) {
                request.setAlgorithmType(each.getAlgorithmType());
                continue;
            }
            if ("assisted_query".equals(each.getAlgorithmRole())) {
                request.getOptions().setAssistedQueryAlgorithmType(each.getAlgorithmType());
                continue;
            }
            if ("like_query".equals(each.getAlgorithmRole())) {
                request.getOptions().setLikeQueryAlgorithmType(each.getAlgorithmType());
            }
        }
    }
    
    private DerivedColumnPlan createDerivedColumnPlan(final EncryptWorkflowRequest request, final List<Map<String, Object>> encryptRules,
                                                      final WorkflowContextSnapshot snapshot, final String databaseType, final Set<String> existingNames) {
        DerivedColumnPlan result = derivedColumnNamingService.createPlan(request, existingNames, snapshot.getIssues(), databaseType);
        Map<String, Object> existingRule = findEncryptRule(encryptRules, request.getColumn(), databaseType).orElse(Map.of());
        String actualCipherColumn = WorkflowRuleValueUtils.getRuleValue(existingRule, "cipher_column");
        String actualAssistedQueryColumn = WorkflowRuleValueUtils.getRuleValue(existingRule, "assisted_query_column");
        String actualLikeQueryColumn = WorkflowRuleValueUtils.getRuleValue(existingRule, "like_query_column");
        if ("alter".equalsIgnoreCase(request.getOperationType())) {
            if (request.getOptions().getCipherColumnName().isEmpty() && !actualCipherColumn.isEmpty()) {
                result.setCipherColumnName(actualCipherColumn);
            }
            if (result.isAssistedQueryColumnRequired() && request.getOptions().getAssistedQueryColumnName().isEmpty() && !actualAssistedQueryColumn.isEmpty()) {
                result.setAssistedQueryColumnName(actualAssistedQueryColumn);
            }
            if (result.isLikeQueryColumnRequired() && request.getOptions().getLikeQueryColumnName().isEmpty() && !actualLikeQueryColumn.isEmpty()) {
                result.setLikeQueryColumnName(actualLikeQueryColumn);
            }
        }
        request.getOptions().setCipherColumnName(result.getCipherColumnName());
        request.getOptions().setAssistedQueryColumnName(result.getAssistedQueryColumnName());
        request.getOptions().setLikeQueryColumnName(result.getLikeQueryColumnName());
        return result;
    }
    
    private void addShrinkAlterCleanupWarning(final EncryptWorkflowRequest request, final ClarifiedIntent clarifiedIntent,
                                              final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot, final String databaseType) {
        if (!"alter".equalsIgnoreCase(clarifiedIntent.getOperationType())) {
            return;
        }
        Optional<Map<String, Object>> existingRule = findEncryptRule(encryptRules, request.getColumn(), databaseType);
        if (existingRule.isEmpty()) {
            return;
        }
        boolean removesAssistedQuery = Boolean.FALSE.equals(request.getOptions().getRequiresEqualityFilter())
                && !WorkflowRuleValueUtils.getRuleValue(existingRule.get(), "assisted_query_column").isEmpty();
        boolean removesLikeQuery = Boolean.FALSE.equals(request.getOptions().getRequiresLikeQuery())
                && !WorkflowRuleValueUtils.getRuleValue(existingRule.get(), "like_query_column").isEmpty();
        if (!removesAssistedQuery && !removesLikeQuery) {
            return;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.PHYSICAL_CLEANUP_REQUIRED, "warning", "planning-artifacts",
                "This encrypt alter removes rule bindings but does not clean up obsolete physical derived columns or indexes in V1.",
                "Clean up obsolete physical artifacts manually after the rule change if needed.", true, Map.of()));
    }
    
    private Set<String> createExistingPhysicalNames(final MCPMetadataQueryFacade metadataQueryService, final EncryptWorkflowRequest request, final String databaseType) {
        Set<String> result = new LinkedHashSet<>();
        String databaseName = WorkflowSQLUtils.normalizeIdentifier(request.getDatabase());
        String schemaName = WorkflowSQLUtils.canonicalizeIdentifier(databaseType, request.getSchema());
        String tableName = WorkflowSQLUtils.canonicalizeIdentifier(databaseType, request.getTable());
        for (MCPColumnMetadata each : metadataQueryService.queryTableColumns(databaseName, schemaName, tableName)) {
            result.add(each.getColumn());
        }
        return result;
    }
    
    private Set<String> createReservedNames(final Set<String> existingPhysicalNames, final List<Map<String, Object>> encryptRules) {
        Set<String> result = new LinkedHashSet<>(existingPhysicalNames);
        for (Map<String, Object> each : encryptRules) {
            addIfPresent(result, WorkflowRuleValueUtils.getRuleValue(each, "cipher_column"));
            addIfPresent(result, WorkflowRuleValueUtils.getRuleValue(each, "assisted_query_column"));
            addIfPresent(result, WorkflowRuleValueUtils.getRuleValue(each, "like_query_column"));
        }
        return result;
    }
    
    private void addIfPresent(final Set<String> target, final Object value) {
        String actualValue = Objects.toString(value, "").trim();
        if (!actualValue.isEmpty()) {
            target.add(actualValue);
        }
    }
    
    private Set<String> createExistingIndexes(final MCPMetadataQueryFacade metadataQueryService, final EncryptWorkflowRequest request, final String databaseType) {
        Set<String> result = new LinkedHashSet<>();
        try {
            String databaseName = WorkflowSQLUtils.normalizeIdentifier(request.getDatabase());
            String schemaName = WorkflowSQLUtils.canonicalizeIdentifier(databaseType, request.getSchema());
            String tableName = WorkflowSQLUtils.canonicalizeIdentifier(databaseType, request.getTable());
            for (MCPIndexMetadata each : metadataQueryService.queryIndexes(databaseName, schemaName, tableName)) {
                result.add(each.getIndex());
            }
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ignored) {
            // CHECKSTYLE:ON
        }
        return result;
    }
    
    private Optional<Map<String, Object>> findEncryptRule(final List<Map<String, Object>> encryptRules, final String columnName, final String databaseType) {
        return encryptRules.stream().filter(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, columnName, WorkflowRuleValueUtils.getRuleValue(each, "logic_column"))).findFirst();
    }
    
    private String resolveDerivedColumnDefinition(final MCPFeatureQueryFacade queryFacade, final EncryptWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        try {
            return queryFacade.queryColumnDefinition(request.getDatabase(), request.getSchema(), request.getTable(), request.getColumn());
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.LOGICAL_METADATA_UNAVAILABLE, "warning", "planning-artifacts",
                    "Failed to derive the source column definition from Proxy metadata.", "Review the generated DDL before execution.", true, Map.of("reason", ex.getMessage())));
            return "";
        }
    }
}
