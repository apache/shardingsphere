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

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
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
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowArtifactPayloadUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowContextStore;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowPlanningContextUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;

import java.util.LinkedHashSet;
import java.util.LinkedList;
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
    
    private final EncryptWorkflowRequestMerger requestMerger = new EncryptWorkflowRequestMerger();
    
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
     * @param requestContext runtime context
     * @param sessionId session id
     * @param request workflow request
     * @return workflow snapshot
     */
    public WorkflowContextSnapshot plan(final MCPFeatureContext requestContext, final String sessionId, final EncryptWorkflowRequest request) {
        WorkflowContextStore actualContextStore = WorkflowLifecycleUtils.resolveContextStore(contextStore, requestContext);
        WorkflowContextSnapshot result = WorkflowPlanningContextUtils.getOrCreateSnapshot(actualContextStore, sessionId, request.getPlanId());
        EncryptWorkflowRequest mergedRequest = prepareSnapshot(result, request);
        ClarifiedIntent clarifiedIntent = result.getClarifiedIntent();
        applyResolvedIntent(mergedRequest, clarifiedIntent);
        MCPMetadataQueryFacade metadataQueryService = requestContext.getMetadataQueryFacade();
        if (!WorkflowPlanningContextUtils.ensurePlanningContext(metadataQueryService, mergedRequest, clarifiedIntent, result)) {
            return WorkflowPlanningContextUtils.persistSnapshot(actualContextStore, result,
                    WorkflowLifecycle.STATUS_FAILED.equals(result.getStatus()) ? WorkflowLifecycle.STEP_FAILED : WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        List<Map<String, Object>> encryptRules = ruleInspectionService.queryEncryptRules(requestContext, mergedRequest.getDatabase(), mergedRequest.getTable());
        if (!ensureLifecycleState(clarifiedIntent, mergedRequest, encryptRules, result)) {
            return WorkflowPlanningContextUtils.persistSnapshot(actualContextStore, result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        EncryptWorkflowState workflowState = getEncryptWorkflowState(result);
        if (isDropWorkflow(clarifiedIntent)) {
            addLifecycleWarnings(workflowState, clarifiedIntent, mergedRequest, encryptRules, result);
            planArtifacts(requestContext, metadataQueryService, workflowState, clarifiedIntent, mergedRequest, encryptRules, result);
            return WorkflowPlanningContextUtils.persistSnapshot(actualContextStore, result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
        }
        planAlgorithms(requestContext, workflowState, mergedRequest, result);
        if (hasBlockingAlgorithmIssues(clarifiedIntent, result)) {
            return WorkflowPlanningContextUtils.persistSnapshot(actualContextStore, result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        if (!clarifiedIntent.getPendingQuestions().isEmpty()) {
            return WorkflowPlanningContextUtils.persistSnapshot(actualContextStore, result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        if (!collectPropertyRequirements(mergedRequest, workflowState, clarifiedIntent, result)) {
            return WorkflowPlanningContextUtils.persistSnapshot(actualContextStore, result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        planArtifacts(requestContext, metadataQueryService, workflowState, clarifiedIntent, mergedRequest, encryptRules, result);
        return WorkflowPlanningContextUtils.persistSnapshot(actualContextStore, result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    private void applyResolvedIntent(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        request.setOperationType(clarifiedIntent.getOperationType());
        request.setFieldSemantics(clarifiedIntent.getFieldSemantics());
    }
    
    private EncryptWorkflowRequest prepareSnapshot(final WorkflowContextSnapshot snapshot, final EncryptWorkflowRequest request) {
        EncryptWorkflowRequest result = requestMerger.merge(snapshot.getRequest(), getEncryptWorkflowState(snapshot), request);
        EncryptWorkflowState workflowState = requestMerger.createState(result);
        clearEncryptPlanningState(workflowState);
        snapshot.setRequest(result);
        snapshot.setFeatureData(workflowState);
        snapshot.setInteractionPlan(InteractionPlan.create(snapshot.getPlanId(), result, "Encrypt workflow plan.", INTERACTION_STEPS, VALIDATION_LAYERS));
        WorkflowPlanningContextUtils.clearPlanningState(snapshot);
        snapshot.setClarifiedIntent(intentResolver.resolve(result, workflowState));
        return result;
    }
    
    private EncryptWorkflowState getEncryptWorkflowState(final WorkflowContextSnapshot snapshot) {
        return snapshot.getFeatureData() instanceof EncryptWorkflowState ? (EncryptWorkflowState) snapshot.getFeatureData() : new EncryptWorkflowState();
    }
    
    private void clearEncryptPlanningState(final EncryptWorkflowState workflowState) {
        workflowState.setDerivedColumnPlan(null);
    }
    
    private boolean ensureLifecycleState(final ClarifiedIntent clarifiedIntent, final WorkflowRequest request,
                                         final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot) {
        boolean ruleExists = encryptRules.stream().anyMatch(each -> request.getColumn().equalsIgnoreCase(WorkflowRuleValueUtils.findRuleValue(each, "logic_column", "column")));
        if ("create".equalsIgnoreCase(clarifiedIntent.getOperationType()) && ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
                    "Encrypt rule already exists for the target column.", "Use alter instead of create.", false, Map.of()));
            return false;
        }
        if ("alter".equalsIgnoreCase(clarifiedIntent.getOperationType()) && !ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
                    "Encrypt rule does not exist for the target column.", "Use create instead of alter or confirm the target column.", false, Map.of()));
            return false;
        }
        if ("drop".equalsIgnoreCase(clarifiedIntent.getOperationType()) && !ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DROP_TARGET_RULE_NOT_FOUND, "error", "discovering",
                    "Encrypt rule does not exist for the target column.", "Confirm target table and column or skip the drop request.", false, Map.of()));
            return false;
        }
        return true;
    }
    
    private boolean isDropWorkflow(final ClarifiedIntent clarifiedIntent) {
        return WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(clarifiedIntent.getOperationType());
    }
    
    private void planAlgorithms(final MCPFeatureContext requestContext, final EncryptWorkflowState workflowState, final WorkflowRequest request,
                                final WorkflowContextSnapshot snapshot) {
        List<Map<String, Object>> encryptAlgorithms = ruleInspectionService.enrichEncryptAlgorithms(ruleInspectionService.queryEncryptAlgorithms(requestContext));
        List<AlgorithmCandidate> algorithmCandidates = algorithmRecommendationService.recommendEncryptAlgorithms(workflowState, request, encryptAlgorithms, snapshot.getIssues());
        snapshot.getAlgorithmCandidates().addAll(algorithmCandidates);
        applyRecommendedAlgorithms(request, workflowState, algorithmCandidates);
    }
    
    private boolean collectPropertyRequirements(final WorkflowRequest request, final EncryptWorkflowState workflowState,
                                                final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        List<AlgorithmPropertyRequirement> propertyRequirements = algorithmPropertyTemplateService.findRequirements(
                request.getAlgorithmType(), workflowState.getAssistedQueryAlgorithmType(), workflowState.getLikeQueryAlgorithmType());
        snapshot.getPropertyRequirements().addAll(propertyRequirements);
        applyDefaultProperties(request, workflowState, propertyRequirements);
        List<String> missingRequiredProperties = findMissingRequiredProperties(request, workflowState, propertyRequirements);
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
    
    private boolean hasBlockingAlgorithmIssues(final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        boolean hasBlockingIssue = snapshot.getIssues().stream()
                .anyMatch(each -> "selecting-algorithm".equals(each.getStage()) && "error".equals(each.getSeverity()));
        if (hasBlockingIssue && clarifiedIntent.getPendingQuestions().isEmpty()) {
            clarifiedIntent.getPendingQuestions().add("请改用当前 Proxy 可见且满足需求的加密算法。");
        }
        return hasBlockingIssue;
    }
    
    private void addLifecycleWarnings(final EncryptWorkflowState workflowState, final ClarifiedIntent clarifiedIntent, final WorkflowRequest request,
                                      final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot) {
        if (isDropWorkflow(clarifiedIntent)) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.ENCRYPT_DROP_SCOPE_LIMITED, "warning", "planning-artifacts",
                    "Encrypt drop only removes the rule. MCP will not restore historical plaintext data.", "Review business impact before execution.", true, Map.of()));
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.PHYSICAL_CLEANUP_REQUIRED, "warning", "planning-artifacts",
                    "Encrypt drop does not clean up legacy physical derived columns or indexes in V1.",
                    "Clean up obsolete physical artifacts manually if they are no longer needed.", true, Map.of()));
            return;
        }
        addShrinkAlterCleanupWarning(workflowState, clarifiedIntent, request, encryptRules, snapshot);
    }
    
    private void planArtifacts(final MCPFeatureContext requestContext, final MCPMetadataQueryFacade metadataQueryService, final EncryptWorkflowState workflowState,
                               final ClarifiedIntent clarifiedIntent, final WorkflowRequest request, final List<Map<String, Object>> encryptRules,
                               final WorkflowContextSnapshot snapshot) {
        if (isDropWorkflow(clarifiedIntent)) {
            snapshot.getRuleArtifacts().add(ruleDistSQLPlanningService.planEncryptDropRule(request, encryptRules));
            return;
        }
        planEncryptArtifacts(requestContext, metadataQueryService, workflowState, clarifiedIntent, request, encryptRules, snapshot);
    }
    
    private void planEncryptArtifacts(final MCPFeatureContext requestContext, final MCPMetadataQueryFacade metadataQueryService, final EncryptWorkflowState workflowState,
                                      final ClarifiedIntent clarifiedIntent, final WorkflowRequest request, final List<Map<String, Object>> encryptRules,
                                      final WorkflowContextSnapshot snapshot) {
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan(metadataQueryService, request, workflowState, encryptRules, snapshot);
        workflowState.setDerivedColumnPlan(derivedColumnPlan);
        addShrinkAlterCleanupWarning(workflowState, clarifiedIntent, request, encryptRules, snapshot);
        Set<String> existingNames = createExistingPhysicalNames(metadataQueryService, request, encryptRules);
        String derivedColumnDefinition = resolveDerivedColumnDefinition(requestContext, request, snapshot);
        List<DDLArtifact> ddlArtifacts = physicalDDLPlanningService.planAddColumnArtifacts(request.getTable(), derivedColumnPlan, existingNames, derivedColumnDefinition);
        snapshot.getDdlArtifacts().addAll(ddlArtifacts);
        if (!Boolean.FALSE.equals(workflowState.getAllowIndexDDL())) {
            snapshot.getIndexPlans().addAll(indexPlanningService.planIndexes(request.getTable(), derivedColumnPlan, createExistingIndexes(metadataQueryService, request)));
        }
        snapshot.getRuleArtifacts().add(ruleDistSQLPlanningService.planEncryptRule(request, workflowState, encryptRules));
    }
    
    private void applyRecommendedAlgorithms(final WorkflowRequest request, final EncryptWorkflowState workflowState, final List<AlgorithmCandidate> algorithmCandidates) {
        for (AlgorithmCandidate each : algorithmCandidates) {
            if ("primary".equals(each.getAlgorithmRole())) {
                request.setAlgorithmType(each.getAlgorithmType());
                continue;
            }
            if ("assisted_query".equals(each.getAlgorithmRole())) {
                workflowState.setAssistedQueryAlgorithmType(each.getAlgorithmType());
                continue;
            }
            if ("like_query".equals(each.getAlgorithmRole())) {
                workflowState.setLikeQueryAlgorithmType(each.getAlgorithmType());
            }
        }
    }
    
    private void applyDefaultProperties(final WorkflowRequest request, final EncryptWorkflowState workflowState,
                                        final List<AlgorithmPropertyRequirement> propertyRequirements) {
        for (AlgorithmPropertyRequirement each : propertyRequirements) {
            if (each.getDefaultValue().isEmpty()) {
                continue;
            }
            Map<String, String> targetProperties = getProperties(request, workflowState, each.getAlgorithmRole());
            targetProperties.putIfAbsent(each.getPropertyKey(), each.getDefaultValue());
        }
    }
    
    private List<String> findMissingRequiredProperties(final WorkflowRequest request, final EncryptWorkflowState workflowState,
                                                       final List<AlgorithmPropertyRequirement> propertyRequirements) {
        List<String> result = new LinkedList<>();
        for (AlgorithmPropertyRequirement each : propertyRequirements) {
            Map<String, String> targetProperties = getProperties(request, workflowState, each.getAlgorithmRole());
            if (each.isRequired() && WorkflowSqlUtils.trimToEmpty(targetProperties.get(each.getPropertyKey())).isEmpty()) {
                result.add(each.getPropertyKey());
            }
        }
        return result;
    }
    
    private Map<String, String> getProperties(final WorkflowRequest request, final EncryptWorkflowState workflowState, final String role) {
        if ("assisted_query".equals(role)) {
            return workflowState.getAssistedQueryAlgorithmProperties();
        }
        if ("like_query".equals(role)) {
            return workflowState.getLikeQueryAlgorithmProperties();
        }
        return request.getPrimaryAlgorithmProperties();
    }
    
    private DerivedColumnPlan createDerivedColumnPlan(final MCPMetadataQueryFacade metadataQueryService, final WorkflowRequest request,
                                                      final EncryptWorkflowState workflowState, final List<Map<String, Object>> encryptRules,
                                                      final WorkflowContextSnapshot snapshot) {
        Set<String> existingNames = createExistingPhysicalNames(metadataQueryService, request, encryptRules);
        DerivedColumnPlan result = derivedColumnNamingService.createPlan(request, workflowState, existingNames, snapshot.getIssues());
        Map<String, Object> existingRule = findEncryptRule(encryptRules, request.getColumn()).orElse(Map.of());
        String actualCipherColumn = WorkflowRuleValueUtils.findRuleValue(existingRule, "cipher_column");
        String actualAssistedQueryColumn = WorkflowRuleValueUtils.findRuleValue(existingRule, "assisted_query_column", "assisted_query");
        String actualLikeQueryColumn = WorkflowRuleValueUtils.findRuleValue(existingRule, "like_query_column", "like_query");
        if ("alter".equalsIgnoreCase(request.getOperationType())) {
            if (WorkflowSqlUtils.trimToEmpty(workflowState.getCipherColumnName()).isEmpty() && !actualCipherColumn.isEmpty()) {
                result.setCipherColumnName(actualCipherColumn);
            }
            if (result.isAssistedQueryColumnRequired() && WorkflowSqlUtils.trimToEmpty(workflowState.getAssistedQueryColumnName()).isEmpty() && !actualAssistedQueryColumn.isEmpty()) {
                result.setAssistedQueryColumnName(actualAssistedQueryColumn);
            }
            if (result.isLikeQueryColumnRequired() && WorkflowSqlUtils.trimToEmpty(workflowState.getLikeQueryColumnName()).isEmpty() && !actualLikeQueryColumn.isEmpty()) {
                result.setLikeQueryColumnName(actualLikeQueryColumn);
            }
        }
        workflowState.setCipherColumnName(result.getCipherColumnName());
        workflowState.setAssistedQueryColumnName(result.getAssistedQueryColumnName());
        workflowState.setLikeQueryColumnName(result.getLikeQueryColumnName());
        return result;
    }
    
    private void addShrinkAlterCleanupWarning(final EncryptWorkflowState workflowState, final ClarifiedIntent clarifiedIntent, final WorkflowRequest request,
                                              final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot) {
        if (!"alter".equalsIgnoreCase(clarifiedIntent.getOperationType())) {
            return;
        }
        Optional<Map<String, Object>> existingRule = findEncryptRule(encryptRules, request.getColumn());
        if (existingRule.isEmpty()) {
            return;
        }
        boolean removesAssistedQuery = Boolean.FALSE.equals(workflowState.getRequiresEqualityFilter())
                && !WorkflowRuleValueUtils.findRuleValue(existingRule.get(), "assisted_query_column", "assisted_query").isEmpty();
        boolean removesLikeQuery = Boolean.FALSE.equals(workflowState.getRequiresLikeQuery())
                && !WorkflowRuleValueUtils.findRuleValue(existingRule.get(), "like_query_column", "like_query").isEmpty();
        if (!removesAssistedQuery && !removesLikeQuery) {
            return;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.PHYSICAL_CLEANUP_REQUIRED, "warning", "planning-artifacts",
                "This encrypt alter removes rule bindings but does not clean up obsolete physical derived columns or indexes in V1.",
                "Clean up obsolete physical artifacts manually after the rule change if needed.", true, Map.of()));
    }
    
    private Set<String> createExistingPhysicalNames(final MCPMetadataQueryFacade metadataQueryService, final WorkflowRequest request,
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
    
    private Set<String> createExistingIndexes(final MCPMetadataQueryFacade metadataQueryService, final WorkflowRequest request) {
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
    
    private String resolveDerivedColumnDefinition(final MCPFeatureContext requestContext, final WorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        try {
            return requestContext.getQueryFacade().queryColumnDefinition(request.getDatabase(), request.getSchema(), request.getTable(), request.getColumn());
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.LOGICAL_METADATA_UNAVAILABLE, "warning", "planning-artifacts",
                    "Failed to derive the source column definition from Proxy metadata.", "Review the generated DDL before execution.", true, Map.of("reason", ex.getMessage())));
            return "";
        }
    }
}
