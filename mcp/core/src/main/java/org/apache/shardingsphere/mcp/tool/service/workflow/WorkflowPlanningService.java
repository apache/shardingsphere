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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.query.MetadataQueryService;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.DDLArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Workflow planning service.
 */
public final class WorkflowPlanningService {
    
    private final WorkflowRequestMerger requestMerger = new WorkflowRequestMerger();
    
    private final WorkflowIntentResolver intentResolver = new WorkflowIntentResolver();
    
    private final WorkflowContextStore contextStore;
    
    private final RuleInspectionService ruleInspectionService;
    
    private final AlgorithmRecommendationService algorithmRecommendationService;
    
    private final AlgorithmPropertyTemplateService algorithmPropertyTemplateService;
    
    private final DerivedColumnNamingService derivedColumnNamingService;
    
    private final PhysicalDDLPlanningService physicalDDLPlanningService;
    
    private final IndexPlanningService indexPlanningService;
    
    private final RuleDistSQLPlanningService ruleDistSQLPlanningService;
    
    private final WorkflowProxyQueryService proxyQueryService;
    
    public WorkflowPlanningService() {
        this(WorkflowContextStore.getInstance(), new RuleInspectionService(), new AlgorithmRecommendationService(), new AlgorithmPropertyTemplateService(),
                new DerivedColumnNamingService(), new PhysicalDDLPlanningService(), new IndexPlanningService(), new RuleDistSQLPlanningService(), new WorkflowProxyQueryService());
    }
    
    WorkflowPlanningService(final WorkflowContextStore contextStore, final RuleInspectionService ruleInspectionService, final AlgorithmRecommendationService algorithmRecommendationService,
                            final AlgorithmPropertyTemplateService algorithmPropertyTemplateService, final DerivedColumnNamingService derivedColumnNamingService,
                            final PhysicalDDLPlanningService physicalDDLPlanningService, final IndexPlanningService indexPlanningService,
                            final RuleDistSQLPlanningService ruleDistSQLPlanningService, final WorkflowProxyQueryService proxyQueryService) {
        this.contextStore = contextStore;
        this.ruleInspectionService = ruleInspectionService;
        this.algorithmRecommendationService = algorithmRecommendationService;
        this.algorithmPropertyTemplateService = algorithmPropertyTemplateService;
        this.derivedColumnNamingService = derivedColumnNamingService;
        this.physicalDDLPlanningService = physicalDDLPlanningService;
        this.indexPlanningService = indexPlanningService;
        this.ruleDistSQLPlanningService = ruleDistSQLPlanningService;
        this.proxyQueryService = proxyQueryService;
    }
    
    /**
     * Plan workflow.
     *
     * @param runtimeContext runtime context
     * @param sessionId session id
     * @param request workflow request
     * @return workflow snapshot
     */
    public WorkflowContextSnapshot plan(final MCPRuntimeContext runtimeContext, final String sessionId, final WorkflowRequest request) {
        WorkflowContextSnapshot result = getOrCreateSnapshot(sessionId, request);
        WorkflowRequest mergedRequest = prepareSnapshot(result, request);
        ClarifiedIntent clarifiedIntent = result.getClarifiedIntent();
        applyResolvedIntent(mergedRequest, clarifiedIntent);
        if (WorkflowSqlUtils.trimToEmpty(clarifiedIntent.getFeatureType()).isEmpty()) {
            clarifiedIntent.getPendingQuestions().add("请明确这是加密还是脱敏。");
            result.getIssues().add(new WorkflowIssue(WorkflowIssueCode.FEATURE_TYPE_UNCLEAR, "error", "intaking",
                    "Feature type is still unclear.", "Specify whether the workflow is for encrypt or mask.", true, Map.of()));
            return persistSnapshot(result, "clarifying", "clarifying");
        }
        MetadataQueryService metadataQueryService = new MetadataQueryService(runtimeContext.getMetadataCatalog());
        if (!ensurePlanningContext(metadataQueryService, mergedRequest, clarifiedIntent, result)) {
            return persistSnapshot(result, "failed".equals(result.getStatus()) ? "failed" : "clarifying", result.getStatus());
        }
        List<Map<String, Object>> encryptRules = ruleInspectionService.queryEncryptRules(runtimeContext, mergedRequest.getDatabase(), mergedRequest.getTable());
        List<Map<String, Object>> maskRules = ruleInspectionService.queryMaskRules(runtimeContext, mergedRequest.getDatabase(), mergedRequest.getTable());
        if (!ensureLifecycleState(clarifiedIntent, mergedRequest, encryptRules, maskRules, result)) {
            return persistSnapshot(result, "failed", "failed");
        }
        if (isDropWorkflow(clarifiedIntent)) {
            addLifecycleWarnings(clarifiedIntent, mergedRequest, encryptRules, result);
            planArtifacts(runtimeContext, metadataQueryService, clarifiedIntent, mergedRequest, encryptRules, maskRules, result);
            return persistSnapshot(result, "review", "planned");
        }
        planAlgorithms(runtimeContext, clarifiedIntent, mergedRequest, result);
        if (hasBlockingAlgorithmIssues(clarifiedIntent, result)) {
            return persistSnapshot(result, "clarifying", "clarifying");
        }
        if (!clarifiedIntent.getPendingQuestions().isEmpty()) {
            return persistSnapshot(result, "clarifying", "clarifying");
        }
        if (!collectPropertyRequirements(clarifiedIntent, mergedRequest, result)) {
            return persistSnapshot(result, "clarifying", "clarifying");
        }
        planArtifacts(runtimeContext, metadataQueryService, clarifiedIntent, mergedRequest, encryptRules, maskRules, result);
        return persistSnapshot(result, "review", "planned");
    }
    
    private void applyResolvedIntent(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        request.setFeatureType(clarifiedIntent.getFeatureType());
        request.setOperationType(clarifiedIntent.getOperationType());
        request.setFieldSemantics(clarifiedIntent.getFieldSemantics());
        request.setRequiresDecrypt(clarifiedIntent.getRequiresDecrypt());
        request.setRequiresEqualityFilter(clarifiedIntent.getRequiresEqualityFilter());
        request.setRequiresLikeQuery(clarifiedIntent.getRequiresLikeQuery());
    }
    
    private WorkflowContextSnapshot getOrCreateSnapshot(final String sessionId, final WorkflowRequest request) {
        String planId = WorkflowSqlUtils.trimToEmpty(request.getPlanId());
        if (!planId.isEmpty()) {
            return contextStore.getRequired(planId);
        }
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(contextStore.createPlanId());
        result.setSessionId(sessionId);
        result.setStatus("clarifying");
        return result;
    }
    
    private WorkflowRequest prepareSnapshot(final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        WorkflowRequest result = requestMerger.merge(snapshot.getRequest(), request);
        snapshot.setRequest(result);
        snapshot.setInteractionPlan(createInteractionPlan(snapshot.getPlanId(), result));
        clearPlanningState(snapshot);
        snapshot.setClarifiedIntent(intentResolver.resolve(result));
        return result;
    }
    
    private void clearPlanningState(final WorkflowContextSnapshot snapshot) {
        snapshot.getIssues().clear();
        snapshot.getAlgorithmCandidates().clear();
        snapshot.getPropertyRequirements().clear();
        snapshot.getDdlArtifacts().clear();
        snapshot.getRuleArtifacts().clear();
        snapshot.getIndexPlans().clear();
        snapshot.setDerivedColumnPlan(null);
        snapshot.setValidationReport(null);
    }
    
    private WorkflowContextSnapshot persistSnapshot(final WorkflowContextSnapshot snapshot, final String currentStep, final String status) {
        snapshot.getInteractionPlan().setCurrentStep(currentStep);
        snapshot.setStatus(status);
        contextStore.save(snapshot);
        return snapshot;
    }
    
    private InteractionPlan createInteractionPlan(final String planId, final WorkflowRequest request) {
        InteractionPlan result = new InteractionPlan();
        result.setPlanId(planId);
        result.setSummary("Encrypt and mask workflow plan.");
        result.setCurrentStep("intaking");
        result.setDeliveryMode(WorkflowSqlUtils.trimToEmpty(request.getDeliveryMode()).isEmpty() ? "all-at-once" : request.getDeliveryMode());
        result.setExecutionMode(WorkflowSqlUtils.trimToEmpty(request.getExecutionMode()).isEmpty() ? "review-then-execute" : request.getExecutionMode());
        result.getSteps().addAll(List.of(
                "Confirm database, table, column and target lifecycle",
                "Inspect existing rules, plugins and logical metadata",
                "Clarify missing requirements and choose algorithms",
                "Collect algorithm properties and create derived naming plan",
                "Generate DDL, DistSQL and index artifacts",
                "Review artifacts and choose execution mode",
                "Execute or export artifacts",
                "Validate and summarize"));
        result.getValidationStrategy().put("layers", List.of("ddl", "rules", "logical_metadata", "sql_executability"));
        return result;
    }
    
    private boolean ensurePlanningContext(final MetadataQueryService metadataQueryService, final WorkflowRequest request, final ClarifiedIntent clarifiedIntent,
                                          final WorkflowContextSnapshot snapshot) {
        if (WorkflowSqlUtils.trimToEmpty(request.getDatabase()).isEmpty()) {
            clarifiedIntent.getPendingQuestions().add("请先提供 logical database。");
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DATABASE_REQUIRED, "error", "intaking",
                    "Database is required before planning.", "Provide the logical database name.", true, Map.of()));
            snapshot.setStatus("clarifying");
            return false;
        }
        if (!ensureSupportedIdentifier("database", request.getDatabase(), snapshot)
                || !ensureSupportedIdentifier("table", request.getTable(), snapshot)
                || !ensureSupportedIdentifier("column", request.getColumn(), snapshot)) {
            snapshot.setStatus("failed");
            return false;
        }
        String actualSchema = resolveSchema(metadataQueryService, request);
        request.setSchema(actualSchema);
        if (!ensureSupportedIdentifier("schema", request.getSchema(), snapshot)) {
            snapshot.setStatus("failed");
            return false;
        }
        if (actualSchema.isEmpty()) {
            clarifiedIntent.getPendingQuestions().add("请明确 schema。");
        }
        if (WorkflowSqlUtils.trimToEmpty(request.getTable()).isEmpty()) {
            clarifiedIntent.getPendingQuestions().add("请明确目标表。");
        }
        if (WorkflowSqlUtils.trimToEmpty(request.getColumn()).isEmpty()) {
            clarifiedIntent.getPendingQuestions().add("请明确目标列。");
        }
        if (actualSchema.isEmpty() || WorkflowSqlUtils.trimToEmpty(request.getTable()).isEmpty() || WorkflowSqlUtils.trimToEmpty(request.getColumn()).isEmpty()) {
            snapshot.setStatus("clarifying");
            return false;
        }
        if (!ensureTableExists(metadataQueryService, request, snapshot) || !ensureColumnExists(metadataQueryService, request, snapshot)) {
            snapshot.setStatus("failed");
            return false;
        }
        return true;
    }
    
    private boolean ensureSupportedIdentifier(final String fieldName, final String identifier, final WorkflowContextSnapshot snapshot) {
        String actualIdentifier = WorkflowSqlUtils.trimToEmpty(identifier);
        if (actualIdentifier.isEmpty() || WorkflowSqlUtils.isSafeIdentifier(actualIdentifier)) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER, "error", "discovering",
                String.format("%s identifier `%s` contains unsupported characters.", fieldName, actualIdentifier),
                "Use unquoted SQL identifiers only in V1.", false, Map.of("field", fieldName, "identifier", actualIdentifier)));
        return false;
    }
    
    private String resolveSchema(final MetadataQueryService metadataQueryService, final WorkflowRequest request) {
        String actualSchema = WorkflowSqlUtils.trimToEmpty(request.getSchema());
        if (!actualSchema.isEmpty()) {
            return actualSchema;
        }
        Optional<MCPDatabaseMetadata> databaseMetadata = metadataQueryService.queryDatabase(request.getDatabase());
        if (databaseMetadata.isEmpty()) {
            return "";
        }
        String tableName = WorkflowSqlUtils.trimToEmpty(request.getTable());
        if (!tableName.isEmpty()) {
            List<String> matchedSchemas = new LinkedList<>();
            for (MCPSchemaMetadata each : databaseMetadata.get().getSchemas()) {
                if (each.getTables().stream().anyMatch(table -> tableName.equals(table.getTable()))) {
                    matchedSchemas.add(each.getSchema());
                }
            }
            if (1 == matchedSchemas.size()) {
                return matchedSchemas.get(0);
            }
        }
        return 1 == databaseMetadata.get().getSchemas().size() ? databaseMetadata.get().getSchemas().iterator().next().getSchema() : "";
    }
    
    private boolean ensureTableExists(final MetadataQueryService metadataQueryService, final WorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (metadataQueryService.queryTable(request.getDatabase(), request.getSchema(), request.getTable()).isPresent()) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.TABLE_NOT_FOUND, "error", "discovering",
                String.format("Table `%s` does not exist in Proxy logical metadata.", request.getTable()), "Check database, schema and table name.", false, Map.of()));
        return false;
    }
    
    private boolean ensureColumnExists(final MetadataQueryService metadataQueryService, final WorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (metadataQueryService.queryTableColumn(request.getDatabase(), request.getSchema(), request.getTable(), request.getColumn()).isPresent()) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.COLUMN_NOT_FOUND, "error", "discovering",
                String.format("Column `%s` does not exist in Proxy logical metadata.", request.getColumn()), "Check column name.", false, Map.of()));
        return false;
    }
    
    private boolean ensureLifecycleState(final ClarifiedIntent clarifiedIntent, final WorkflowRequest request, final List<Map<String, Object>> encryptRules,
                                         final List<Map<String, Object>> maskRules, final WorkflowContextSnapshot snapshot) {
        if ("encrypt".equalsIgnoreCase(clarifiedIntent.getFeatureType())) {
            boolean ruleExists = encryptRules.stream().anyMatch(each -> request.getColumn().equalsIgnoreCase(findRuleValue(each, "logic_column", "column")));
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
        boolean ruleExists = maskRules.stream().anyMatch(each -> request.getColumn().equalsIgnoreCase(findRuleValue(each, "column", "logic_column")));
        if ("create".equalsIgnoreCase(clarifiedIntent.getOperationType()) && ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
                    "Mask rule already exists for the target column.", "Use alter instead of create.", false, Map.of()));
            return false;
        }
        if ("alter".equalsIgnoreCase(clarifiedIntent.getOperationType()) && !ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
                    "Mask rule does not exist for the target column.", "Confirm target table and column or use create.", false, Map.of()));
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
        return "drop".equalsIgnoreCase(clarifiedIntent.getOperationType());
    }
    
    private void planAlgorithms(final MCPRuntimeContext runtimeContext, final ClarifiedIntent clarifiedIntent, final WorkflowRequest request,
                                final WorkflowContextSnapshot snapshot) {
        if ("encrypt".equalsIgnoreCase(clarifiedIntent.getFeatureType())) {
            List<Map<String, Object>> encryptAlgorithms = ruleInspectionService.enrichEncryptAlgorithms(ruleInspectionService.queryEncryptAlgorithms(runtimeContext));
            List<AlgorithmCandidate> algorithmCandidates = algorithmRecommendationService.recommendEncryptAlgorithms(clarifiedIntent, request, encryptAlgorithms, snapshot.getIssues());
            snapshot.getAlgorithmCandidates().addAll(algorithmCandidates);
            applyRecommendedAlgorithms(request, algorithmCandidates);
            return;
        }
        List<Map<String, Object>> maskAlgorithms = ruleInspectionService.enrichMaskAlgorithms(ruleInspectionService.queryMaskAlgorithms(runtimeContext));
        List<AlgorithmCandidate> algorithmCandidates = algorithmRecommendationService.recommendMaskAlgorithms(clarifiedIntent, request, maskAlgorithms, snapshot.getIssues());
        snapshot.getAlgorithmCandidates().addAll(algorithmCandidates);
        applyRecommendedAlgorithms(request, algorithmCandidates);
    }
    
    private boolean collectPropertyRequirements(final ClarifiedIntent clarifiedIntent, final WorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        List<AlgorithmPropertyRequirement> propertyRequirements = algorithmPropertyTemplateService.findRequirements(clarifiedIntent.getFeatureType(),
                request.getAlgorithmType(), request.getAssistedQueryAlgorithmType(), request.getLikeQueryAlgorithmType());
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
    
    private boolean hasBlockingAlgorithmIssues(final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        boolean hasBlockingIssue = snapshot.getIssues().stream()
                .anyMatch(each -> "selecting-algorithm".equals(each.getStage()) && "error".equals(each.getSeverity()));
        if (hasBlockingIssue && clarifiedIntent.getPendingQuestions().isEmpty()) {
            clarifiedIntent.getPendingQuestions().add("请改用当前 Proxy 可见且满足需求的算法。");
        }
        return hasBlockingIssue;
    }
    
    private void addLifecycleWarnings(final ClarifiedIntent clarifiedIntent, final WorkflowRequest request,
                                      final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot) {
        if (!"encrypt".equalsIgnoreCase(clarifiedIntent.getFeatureType())) {
            return;
        }
        if (isDropWorkflow(clarifiedIntent)) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.ENCRYPT_DROP_SCOPE_LIMITED, "warning", "planning-artifacts",
                    "Encrypt drop only removes the rule. MCP will not restore historical plaintext data.", "Review business impact before execution.", true, Map.of()));
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.PHYSICAL_CLEANUP_REQUIRED, "warning", "planning-artifacts",
                    "Encrypt drop does not clean up legacy physical derived columns or indexes in V1.",
                    "Clean up obsolete physical artifacts manually if they are no longer needed.", true, Map.of()));
            return;
        }
        addShrinkAlterCleanupWarning(clarifiedIntent, request, encryptRules, snapshot);
    }
    
    private void planArtifacts(final MCPRuntimeContext runtimeContext, final MetadataQueryService metadataQueryService, final ClarifiedIntent clarifiedIntent,
                               final WorkflowRequest request, final List<Map<String, Object>> encryptRules, final List<Map<String, Object>> maskRules,
                               final WorkflowContextSnapshot snapshot) {
        if ("encrypt".equalsIgnoreCase(clarifiedIntent.getFeatureType())) {
            if (isDropWorkflow(clarifiedIntent)) {
                snapshot.getRuleArtifacts().add(ruleDistSQLPlanningService.planEncryptDropRule(request, encryptRules));
                return;
            }
            planEncryptArtifacts(runtimeContext, metadataQueryService, clarifiedIntent, request, encryptRules, snapshot);
            return;
        }
        if (isDropWorkflow(clarifiedIntent)) {
            snapshot.getRuleArtifacts().add(ruleDistSQLPlanningService.planMaskDropRule(request, maskRules));
            return;
        }
        snapshot.getRuleArtifacts().add(ruleDistSQLPlanningService.planMaskRule(request, maskRules));
    }
    
    private void planEncryptArtifacts(final MCPRuntimeContext runtimeContext, final MetadataQueryService metadataQueryService, final ClarifiedIntent clarifiedIntent,
                                      final WorkflowRequest request, final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot) {
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan(metadataQueryService, request, encryptRules, snapshot);
        snapshot.setDerivedColumnPlan(derivedColumnPlan);
        addShrinkAlterCleanupWarning(clarifiedIntent, request, encryptRules, snapshot);
        Set<String> existingNames = createExistingPhysicalNames(metadataQueryService, request, encryptRules);
        String derivedColumnDefinition = resolveDerivedColumnDefinition(runtimeContext, request, snapshot);
        List<DDLArtifact> ddlArtifacts = physicalDDLPlanningService.planAddColumnArtifacts(request.getTable(), derivedColumnPlan, existingNames, derivedColumnDefinition);
        snapshot.getDdlArtifacts().addAll(ddlArtifacts);
        if (!Boolean.FALSE.equals(request.getAllowIndexDDL())) {
            snapshot.getIndexPlans().addAll(indexPlanningService.planIndexes(request.getTable(), derivedColumnPlan, createExistingIndexes(metadataQueryService, request)));
        }
        snapshot.getRuleArtifacts().add(ruleDistSQLPlanningService.planEncryptRule(request, clarifiedIntent, derivedColumnPlan, encryptRules));
    }
    
    private void applyRecommendedAlgorithms(final WorkflowRequest request, final List<AlgorithmCandidate> algorithmCandidates) {
        for (AlgorithmCandidate each : algorithmCandidates) {
            if ("primary".equals(each.getAlgorithmRole())) {
                request.setAlgorithmType(each.getAlgorithmType());
                continue;
            }
            if ("assisted_query".equals(each.getAlgorithmRole())) {
                request.setAssistedQueryAlgorithmType(each.getAlgorithmType());
                continue;
            }
            if ("like_query".equals(each.getAlgorithmRole())) {
                request.setLikeQueryAlgorithmType(each.getAlgorithmType());
            }
        }
    }
    
    private void applyDefaultProperties(final WorkflowRequest request, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        for (AlgorithmPropertyRequirement each : propertyRequirements) {
            if (each.getDefaultValue().isEmpty()) {
                continue;
            }
            Map<String, String> targetProperties = getProperties(request, each.getAlgorithmRole());
            targetProperties.putIfAbsent(each.getPropertyKey(), each.getDefaultValue());
        }
    }
    
    private List<String> findMissingRequiredProperties(final WorkflowRequest request, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        List<String> result = new LinkedList<>();
        for (AlgorithmPropertyRequirement each : propertyRequirements) {
            Map<String, String> targetProperties = getProperties(request, each.getAlgorithmRole());
            if (each.isRequired() && WorkflowSqlUtils.trimToEmpty(targetProperties.get(each.getPropertyKey())).isEmpty()) {
                result.add(each.getPropertyKey());
            }
        }
        return result;
    }
    
    private Map<String, String> getProperties(final WorkflowRequest request, final String role) {
        if ("assisted_query".equals(role)) {
            return request.getAssistedQueryAlgorithmProperties();
        }
        if ("like_query".equals(role)) {
            return request.getLikeQueryAlgorithmProperties();
        }
        return request.getPrimaryAlgorithmProperties();
    }
    
    private DerivedColumnPlan createDerivedColumnPlan(final MetadataQueryService metadataQueryService, final WorkflowRequest request,
                                                      final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot) {
        Set<String> existingNames = createExistingPhysicalNames(metadataQueryService, request, encryptRules);
        DerivedColumnPlan result = derivedColumnNamingService.createPlan(request, Boolean.TRUE.equals(snapshot.getClarifiedIntent().getRequiresEqualityFilter()),
                Boolean.TRUE.equals(snapshot.getClarifiedIntent().getRequiresLikeQuery()), existingNames, snapshot.getIssues());
        Map<String, Object> existingRule = findEncryptRule(encryptRules, request.getColumn()).orElse(Map.of());
        String actualCipherColumn = findRuleValue(existingRule, "cipher_column");
        String actualAssistedQueryColumn = findRuleValue(existingRule, "assisted_query_column", "assisted_query");
        String actualLikeQueryColumn = findRuleValue(existingRule, "like_query_column", "like_query");
        if ("alter".equalsIgnoreCase(request.getOperationType())) {
            if (WorkflowSqlUtils.trimToEmpty(request.getCipherColumnName()).isEmpty() && !actualCipherColumn.isEmpty()) {
                result.setCipherColumnName(actualCipherColumn);
            }
            if (result.isAssistedQueryColumnRequired() && WorkflowSqlUtils.trimToEmpty(request.getAssistedQueryColumnName()).isEmpty() && !actualAssistedQueryColumn.isEmpty()) {
                result.setAssistedQueryColumnName(actualAssistedQueryColumn);
            }
            if (result.isLikeQueryColumnRequired() && WorkflowSqlUtils.trimToEmpty(request.getLikeQueryColumnName()).isEmpty() && !actualLikeQueryColumn.isEmpty()) {
                result.setLikeQueryColumnName(actualLikeQueryColumn);
            }
        }
        request.setCipherColumnName(result.getCipherColumnName());
        request.setAssistedQueryColumnName(result.getAssistedQueryColumnName());
        request.setLikeQueryColumnName(result.getLikeQueryColumnName());
        return result;
    }
    
    private void addShrinkAlterCleanupWarning(final ClarifiedIntent clarifiedIntent, final WorkflowRequest request,
                                              final List<Map<String, Object>> encryptRules, final WorkflowContextSnapshot snapshot) {
        if (!"alter".equalsIgnoreCase(clarifiedIntent.getOperationType())) {
            return;
        }
        Optional<Map<String, Object>> existingRule = findEncryptRule(encryptRules, request.getColumn());
        if (existingRule.isEmpty()) {
            return;
        }
        boolean removesAssistedQuery = Boolean.FALSE.equals(clarifiedIntent.getRequiresEqualityFilter())
                && !findRuleValue(existingRule.get(), "assisted_query_column", "assisted_query").isEmpty();
        boolean removesLikeQuery = Boolean.FALSE.equals(clarifiedIntent.getRequiresLikeQuery())
                && !findRuleValue(existingRule.get(), "like_query_column", "like_query").isEmpty();
        if (!removesAssistedQuery && !removesLikeQuery) {
            return;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.PHYSICAL_CLEANUP_REQUIRED, "warning", "planning-artifacts",
                "This encrypt alter removes rule bindings but does not clean up obsolete physical derived columns or indexes in V1.",
                "Clean up obsolete physical artifacts manually after the rule change if needed.", true, Map.of()));
    }
    
    private Set<String> createExistingPhysicalNames(final MetadataQueryService metadataQueryService, final WorkflowRequest request, final List<Map<String, Object>> encryptRules) {
        Set<String> result = new LinkedHashSet<>();
        for (MCPColumnMetadata each : metadataQueryService.queryTableColumns(request.getDatabase(), request.getSchema(), request.getTable())) {
            result.add(each.getColumn());
        }
        for (Map<String, Object> each : encryptRules) {
            addIfPresent(result, findRuleValue(each, "cipher_column"));
            addIfPresent(result, findRuleValue(each, "assisted_query_column", "assisted_query"));
            addIfPresent(result, findRuleValue(each, "like_query_column", "like_query"));
        }
        return result;
    }
    
    private void addIfPresent(final Set<String> target, final Object value) {
        String actualValue = null == value ? "" : WorkflowSqlUtils.trimToEmpty(String.valueOf(value));
        if (!actualValue.isEmpty()) {
            target.add(actualValue);
        }
    }
    
    private Set<String> createExistingIndexes(final MetadataQueryService metadataQueryService, final WorkflowRequest request) {
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
        return encryptRules.stream().filter(each -> columnName.equalsIgnoreCase(findRuleValue(each, "logic_column", "column"))).findFirst();
    }
    
    private String findRuleValue(final Map<String, Object> rule, final String... keys) {
        for (String each : keys) {
            Object value = rule.get(each);
            String actualValue = null == value ? "" : WorkflowSqlUtils.trimToEmpty(String.valueOf(value));
            if (!actualValue.isEmpty()) {
                return actualValue;
            }
        }
        return "";
    }
    
    private String resolveDerivedColumnDefinition(final MCPRuntimeContext runtimeContext, final WorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        try {
            return proxyQueryService.queryColumnDefinition(runtimeContext, request.getDatabase(), request.getSchema(), request.getTable(), request.getColumn());
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.LOGICAL_METADATA_UNAVAILABLE, "warning", "planning-artifacts",
                    "Failed to derive the source column definition from Proxy metadata.", "Review the generated DDL before execution.", true, Map.of("reason", ex.getMessage())));
            return "";
        }
    }
}
