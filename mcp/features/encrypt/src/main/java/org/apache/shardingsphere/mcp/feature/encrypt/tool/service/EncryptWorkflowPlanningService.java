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

import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowState;
import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.DDLArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowArtifactPayloadUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowContextStore;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowPlanningSupport;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    
    private final WorkflowPlanningSupport planningSupport = new WorkflowPlanningSupport();
    
    private final EncryptWorkflowIntentResolver intentResolver = new EncryptWorkflowIntentResolver();
    
    private final WorkflowContextStore contextStore;
    
    private final EncryptRuleInspectionService ruleInspectionService;
    
    private final EncryptAlgorithmRecommendationService algorithmRecommendationService;
    
    private final EncryptAlgorithmPropertyTemplateService algorithmPropertyTemplateService;
    
    private final DerivedColumnNamingService derivedColumnNamingService;
    
    private final PhysicalDDLPlanningService physicalDDLPlanningService;
    
    private final IndexPlanningService indexPlanningService;
    
    private final EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService;
    
    public EncryptWorkflowPlanningService() {
        this(null, new EncryptRuleInspectionService(), new EncryptAlgorithmRecommendationService(), new EncryptAlgorithmPropertyTemplateService(),
                new DerivedColumnNamingService(), new PhysicalDDLPlanningService(), new IndexPlanningService(), new EncryptRuleDistSQLPlanningService());
    }
    
    EncryptWorkflowPlanningService(final WorkflowContextStore contextStore, final EncryptRuleInspectionService ruleInspectionService,
                                   final EncryptAlgorithmRecommendationService algorithmRecommendationService,
                                   final EncryptAlgorithmPropertyTemplateService algorithmPropertyTemplateService,
                                   final DerivedColumnNamingService derivedColumnNamingService, final PhysicalDDLPlanningService physicalDDLPlanningService,
                                   final IndexPlanningService indexPlanningService, final EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService) {
        this.contextStore = contextStore;
        this.ruleInspectionService = ruleInspectionService;
        this.algorithmRecommendationService = algorithmRecommendationService;
        this.algorithmPropertyTemplateService = algorithmPropertyTemplateService;
        this.derivedColumnNamingService = derivedColumnNamingService;
        this.physicalDDLPlanningService = physicalDDLPlanningService;
        this.indexPlanningService = indexPlanningService;
        this.ruleDistSQLPlanningService = ruleDistSQLPlanningService;
    }
    
    /**
     * Plan encrypt workflow.
     *
     * @param requestContextStore workflow context store
     * @param metadataQueryFacade metadata query facade
     * @param queryFacade query facade
     * @param sessionId session id
     * @param request workflow request
     * @return workflow snapshot
     */
    public WorkflowContextSnapshot plan(final WorkflowContextStore requestContextStore, final MCPMetadataQueryFacade metadataQueryFacade,
                                        final MCPFeatureQueryFacade queryFacade, final String sessionId, final EncryptWorkflowRequest request) {
        WorkflowContextStore actualContextStore = WorkflowLifecycleUtils.resolveContextStore(contextStore, requestContextStore);
        WorkflowContextSnapshot result = actualContextStore.getOrCreate(sessionId, request.getPlanId());
        EncryptWorkflowRequest mergedRequest = prepareSnapshot(result, request);
        ClarifiedIntent clarifiedIntent = result.getClarifiedIntent();
        planningSupport.applyResolvedIntent(mergedRequest, clarifiedIntent);
        if (!planningSupport.ensurePlanningContext(metadataQueryFacade, mergedRequest, clarifiedIntent, result)) {
            String currentStep = WorkflowLifecycle.STATUS_FAILED.equals(result.getStatus()) ? WorkflowLifecycle.STEP_FAILED : WorkflowLifecycle.STEP_CLARIFYING;
            return actualContextStore.persist(result, currentStep, result.getStatus());
        }
        List<Map<String, Object>> existingRules = ruleInspectionService.queryEncryptRules(queryFacade, mergedRequest.getDatabase(), mergedRequest.getTable());
        if (!ensureLifecycleState(clarifiedIntent, mergedRequest, existingRules, result)) {
            return actualContextStore.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        EncryptWorkflowState workflowState = getWorkflowState(result);
        if (isDropWorkflow(clarifiedIntent)) {
            planDrop(metadataQueryFacade, queryFacade, workflowState, clarifiedIntent, mergedRequest, existingRules, result);
            return actualContextStore.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
        }
        if (!planNonDrop(metadataQueryFacade, queryFacade, workflowState, clarifiedIntent, mergedRequest, existingRules, result)) {
            return actualContextStore.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        return actualContextStore.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    private EncryptWorkflowRequest prepareSnapshot(final WorkflowContextSnapshot snapshot, final EncryptWorkflowRequest request) {
        EncryptWorkflowRequest result = EncryptWorkflowRequest.merge(snapshot.getRequest(), request);
        EncryptWorkflowState workflowState = getWorkflowState(snapshot);
        workflowState.setDerivedColumnPlan(null);
        return planningSupport.prepareSnapshot(snapshot, result, workflowState, intentResolver.resolve(result), "Encrypt workflow plan.", INTERACTION_STEPS, VALIDATION_LAYERS);
    }
    
    private EncryptWorkflowState getWorkflowState(final WorkflowContextSnapshot snapshot) {
        return snapshot.getFeatureData() instanceof EncryptWorkflowState ? (EncryptWorkflowState) snapshot.getFeatureData() : new EncryptWorkflowState();
    }
    
    private boolean ensureLifecycleState(final ClarifiedIntent clarifiedIntent, final EncryptWorkflowRequest request,
                                         final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot) {
        boolean ruleExists = encryptRules.stream().anyMatch(each -> request.getColumn().equalsIgnoreCase(WorkflowRuleValueUtils.findRuleValue(each, "logic_column", "column")));
        return planningSupport.ensureLifecycleState("Encrypt rule", clarifiedIntent, ruleExists, snapshot);
    }
    
    private boolean isDropWorkflow(final ClarifiedIntent clarifiedIntent) {
        return WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(clarifiedIntent.getOperationType());
    }
    
    private void planDrop(final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade, final EncryptWorkflowState workflowState,
                          final ClarifiedIntent clarifiedIntent, final EncryptWorkflowRequest request, final List<Map<String, Object>> existingRules,
                          final WorkflowContextSnapshot snapshot) {
        addLifecycleWarnings(request, clarifiedIntent, existingRules, snapshot);
        planArtifacts(metadataQueryFacade, queryFacade, workflowState, clarifiedIntent, request, existingRules, snapshot);
    }
    
    private boolean planNonDrop(final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade, final EncryptWorkflowState workflowState,
                                final ClarifiedIntent clarifiedIntent, final EncryptWorkflowRequest request, final List<Map<String, Object>> existingRules,
                                final WorkflowContextSnapshot snapshot) {
        planAlgorithms(queryFacade, request, snapshot);
        if (!planningSupport.isReadyForArtifactPlanning(request, clarifiedIntent, snapshot, findPropertyRequirements(request), "请改用当前 Proxy 可见且满足需求的加密算法。")) {
            return false;
        }
        planArtifacts(metadataQueryFacade, queryFacade, workflowState, clarifiedIntent, request, existingRules, snapshot);
        return true;
    }
    
    private void planAlgorithms(final MCPFeatureQueryFacade queryFacade, final EncryptWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        List<Map<String, Object>> encryptAlgorithms = ruleInspectionService.enrichEncryptAlgorithms(ruleInspectionService.queryEncryptAlgorithms(queryFacade));
        List<AlgorithmCandidate> algorithmCandidates = algorithmRecommendationService.recommendEncryptAlgorithms(request, encryptAlgorithms, snapshot.getIssues());
        snapshot.getAlgorithmCandidates().addAll(algorithmCandidates);
        applyRecommendedAlgorithms(request, algorithmCandidates);
    }
    
    private List<AlgorithmPropertyRequirement> findPropertyRequirements(final EncryptWorkflowRequest request) {
        return algorithmPropertyTemplateService.findRequirements(request.getAlgorithmType(), request.getOptions().getAssistedQueryAlgorithmType(), request.getOptions().getLikeQueryAlgorithmType());
    }
    
    private void addLifecycleWarnings(final EncryptWorkflowRequest request, final ClarifiedIntent clarifiedIntent,
                                      final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot) {
        if (isDropWorkflow(clarifiedIntent)) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.ENCRYPT_DROP_SCOPE_LIMITED, "warning", "planning-artifacts",
                    "Encrypt drop only removes the rule. MCP will not restore historical plaintext data.", "Review business impact before execution.", true, Map.of()));
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.PHYSICAL_CLEANUP_REQUIRED, "warning", "planning-artifacts",
                    "Encrypt drop does not clean up legacy physical derived columns or indexes in V1.",
                    "Clean up obsolete physical artifacts manually if they are no longer needed.", true, Map.of()));
            return;
        }
        addShrinkAlterCleanupWarning(request, clarifiedIntent, encryptRules, snapshot);
    }
    
    private void planArtifacts(final MCPMetadataQueryFacade metadataQueryService, final MCPFeatureQueryFacade queryFacade, final EncryptWorkflowState workflowState,
                               final ClarifiedIntent clarifiedIntent, final EncryptWorkflowRequest request, final List<Map<String, Object>> encryptRules,
                               final WorkflowContextSnapshot snapshot) {
        if (isDropWorkflow(clarifiedIntent)) {
            snapshot.getRuleArtifacts().add(ruleDistSQLPlanningService.planEncryptDropRule(request, encryptRules));
            return;
        }
        planEncryptArtifacts(metadataQueryService, queryFacade, workflowState, clarifiedIntent, request, encryptRules, snapshot);
    }
    
    private void planEncryptArtifacts(final MCPMetadataQueryFacade metadataQueryService, final MCPFeatureQueryFacade queryFacade, final EncryptWorkflowState workflowState,
                                      final ClarifiedIntent clarifiedIntent, final EncryptWorkflowRequest request, final List<Map<String, Object>> encryptRules,
                                      final WorkflowContextSnapshot snapshot) {
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan(metadataQueryService, request, encryptRules, snapshot);
        workflowState.setDerivedColumnPlan(derivedColumnPlan);
        addShrinkAlterCleanupWarning(request, clarifiedIntent, encryptRules, snapshot);
        Set<String> existingNames = createExistingPhysicalNames(metadataQueryService, request, encryptRules);
        String derivedColumnDefinition = resolveDerivedColumnDefinition(queryFacade, request, snapshot);
        List<DDLArtifact> ddlArtifacts = physicalDDLPlanningService.planAddColumnArtifacts(request.getTable(), derivedColumnPlan, existingNames, derivedColumnDefinition);
        snapshot.getDdlArtifacts().addAll(ddlArtifacts);
        if (!Boolean.FALSE.equals(request.getOptions().getAllowIndexDDL())) {
            snapshot.getIndexPlans().addAll(indexPlanningService.planIndexes(request.getTable(), derivedColumnPlan, createExistingIndexes(metadataQueryService, request)));
        }
        snapshot.getRuleArtifacts().add(ruleDistSQLPlanningService.planEncryptRule(request, derivedColumnPlan, encryptRules));
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
    
    private DerivedColumnPlan createDerivedColumnPlan(final MCPMetadataQueryFacade metadataQueryService, final EncryptWorkflowRequest request,
                                                      final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot) {
        Set<String> existingNames = createExistingPhysicalNames(metadataQueryService, request, encryptRules);
        DerivedColumnPlan result = derivedColumnNamingService.createPlan(request, existingNames, snapshot.getIssues());
        Map<String, Object> existingRule = findEncryptRule(encryptRules, request.getColumn()).orElse(Map.of());
        String actualCipherColumn = WorkflowRuleValueUtils.findRuleValue(existingRule, "cipher_column");
        String actualAssistedQueryColumn = WorkflowRuleValueUtils.findRuleValue(existingRule, "assisted_query_column", "assisted_query");
        String actualLikeQueryColumn = WorkflowRuleValueUtils.findRuleValue(existingRule, "like_query_column", "like_query");
        if ("alter".equalsIgnoreCase(request.getOperationType())) {
            if (WorkflowSqlUtils.trimToEmpty(request.getOptions().getCipherColumnName()).isEmpty() && !actualCipherColumn.isEmpty()) {
                result.setCipherColumnName(actualCipherColumn);
            }
            if (result.isAssistedQueryColumnRequired() && WorkflowSqlUtils.trimToEmpty(request.getOptions().getAssistedQueryColumnName()).isEmpty()
                    && !actualAssistedQueryColumn.isEmpty()) {
                result.setAssistedQueryColumnName(actualAssistedQueryColumn);
            }
            if (result.isLikeQueryColumnRequired() && WorkflowSqlUtils.trimToEmpty(request.getOptions().getLikeQueryColumnName()).isEmpty()
                    && !actualLikeQueryColumn.isEmpty()) {
                result.setLikeQueryColumnName(actualLikeQueryColumn);
            }
        }
        request.getOptions().setCipherColumnName(result.getCipherColumnName());
        request.getOptions().setAssistedQueryColumnName(result.getAssistedQueryColumnName());
        request.getOptions().setLikeQueryColumnName(result.getLikeQueryColumnName());
        return result;
    }
    
    private void addShrinkAlterCleanupWarning(final EncryptWorkflowRequest request, final ClarifiedIntent clarifiedIntent,
                                              final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot) {
        if (!"alter".equalsIgnoreCase(clarifiedIntent.getOperationType())) {
            return;
        }
        Optional<Map<String, Object>> existingRule = findEncryptRule(encryptRules, request.getColumn());
        if (existingRule.isEmpty()) {
            return;
        }
        boolean removesAssistedQuery = Boolean.FALSE.equals(request.getOptions().getRequiresEqualityFilter())
                && !WorkflowRuleValueUtils.findRuleValue(existingRule.get(), "assisted_query_column", "assisted_query").isEmpty();
        boolean removesLikeQuery = Boolean.FALSE.equals(request.getOptions().getRequiresLikeQuery())
                && !WorkflowRuleValueUtils.findRuleValue(existingRule.get(), "like_query_column", "like_query").isEmpty();
        if (!removesAssistedQuery && !removesLikeQuery) {
            return;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.PHYSICAL_CLEANUP_REQUIRED, "warning", "planning-artifacts",
                "This encrypt alter removes rule bindings but does not clean up obsolete physical derived columns or indexes in V1.",
                "Clean up obsolete physical artifacts manually after the rule change if needed.", true, Map.of()));
    }
    
    private Set<String> createExistingPhysicalNames(final MCPMetadataQueryFacade metadataQueryService, final EncryptWorkflowRequest request,
                                                    final List<Map<String, Object>> encryptRules) {
        Set<String> result = new LinkedHashSet<>();
        for (MCPColumnMetadata each : metadataQueryService.queryTableColumns(request.getDatabase(), request.getSchema(), request.getTable())) {
            result.add(each.getColumn());
        }
        for (Map<String, Object> each : encryptRules) {
            addIfPresent(result, WorkflowRuleValueUtils.findRuleValue(each, "cipher_column"));
            addIfPresent(result, WorkflowRuleValueUtils.findRuleValue(each, "assisted_query_column", "assisted_query"));
            addIfPresent(result, WorkflowRuleValueUtils.findRuleValue(each, "like_query_column", "like_query"));
        }
        return result;
    }
    
    private void addIfPresent(final Set<String> target, final Object value) {
        String actualValue = null == value ? "" : WorkflowSqlUtils.trimToEmpty(String.valueOf(value));
        if (!actualValue.isEmpty()) {
            target.add(actualValue);
        }
    }
    
    private Set<String> createExistingIndexes(final MCPMetadataQueryFacade metadataQueryService, final EncryptWorkflowRequest request) {
        Set<String> result = new LinkedHashSet<>();
        try {
            for (MCPIndexMetadata each : metadataQueryService.queryIndexes(request.getDatabase(), request.getSchema(), request.getTable())) {
                result.add(each.getIndex());
            }
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ignored) {
            // CHECKSTYLE:ON
        }
        return result;
    }
    
    private Optional<Map<String, Object>> findEncryptRule(final List<Map<String, Object>> encryptRules, final String columnName) {
        return encryptRules.stream().filter(each -> columnName.equalsIgnoreCase(WorkflowRuleValueUtils.findRuleValue(each, "logic_column", "column"))).findFirst();
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
