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

package org.apache.shardingsphere.mcp.support.workflow.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.support.diagnostic.MCPDiagnosticCategory;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.resource.MCPUriPathSegmentUtils;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Workflow guidance payload builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowGuidancePayloadBuilder {
    
    private static final String APPLY_WORKFLOW = "database_gateway_apply_workflow";
    
    private static final String VALIDATE_WORKFLOW = "database_gateway_validate_workflow";
    
    private static final String EXECUTION_MODE_PREVIEW = "preview";
    
    private static final String EXECUTION_MODE_MANUAL_ONLY = "manual-only";
    
    private static final String ENCRYPT_RULE_WORKFLOW_KIND = "encrypt.rule";
    
    private static final String MASK_RULE_WORKFLOW_KIND = "mask.rule";
    
    private static final String BROADCAST_RULE_WORKFLOW_KIND = "broadcast.rule";
    
    private static final String READWRITE_RULE_WORKFLOW_KIND = "readwrite.rule";
    
    private static final String READWRITE_STATUS_WORKFLOW_KIND = "readwrite.status";
    
    private static final String SHADOW_RULE_WORKFLOW_KIND = "shadow.rule";
    
    private static final String SHADOW_DEFAULT_ALGORITHM_WORKFLOW_KIND = "shadow.default";
    
    private static final String SHADOW_ALGORITHM_CLEANUP_WORKFLOW_KIND = "shadow.cleanup";
    
    private static final String SHARDING_TABLE_RULE_WORKFLOW_KIND = "sharding.table.rule";
    
    private static final String SHARDING_TABLE_REFERENCE_WORKFLOW_KIND = "sharding.table.reference";
    
    private static final String SHARDING_DEFAULT_STRATEGY_WORKFLOW_KIND = "sharding.default.strategy";
    
    private static final String SHARDING_KEY_GENERATOR_WORKFLOW_KIND = "sharding.key.generator";
    
    private static final String SHARDING_KEY_GENERATE_STRATEGY_WORKFLOW_KIND = "sharding.key.generate.strategy";
    
    private static final String SHARDING_COMPONENT_CLEANUP_WORKFLOW_KIND = "sharding.component.cleanup";
    
    private static final String PLAN_ENCRYPT_RULE = "database_gateway_plan_encrypt_rule";
    
    private static final String PLAN_MASK_RULE = "database_gateway_plan_mask_rule";
    
    private static final String PLAN_BROADCAST_RULE = "database_gateway_plan_broadcast_rule";
    
    private static final String PLAN_READWRITE_RULE = "database_gateway_plan_readwrite_splitting_rule";
    
    private static final String PLAN_READWRITE_STATUS = "database_gateway_plan_readwrite_splitting_status";
    
    private static final String PLAN_SHADOW_RULE = "database_gateway_plan_shadow_rule";
    
    private static final String PLAN_DEFAULT_SHADOW_ALGORITHM = "database_gateway_plan_default_shadow_algorithm";
    
    private static final String PLAN_SHADOW_ALGORITHM_CLEANUP = "database_gateway_plan_shadow_algorithm_cleanup";
    
    private static final String PLAN_SHARDING_TABLE_RULE = "database_gateway_plan_sharding_table_rule";
    
    private static final String PLAN_SHARDING_TABLE_REFERENCE_RULE = "database_gateway_plan_sharding_table_reference_rule";
    
    private static final String PLAN_SHARDING_DEFAULT_STRATEGY = "database_gateway_plan_sharding_default_strategy";
    
    private static final String PLAN_SHARDING_KEY_GENERATOR = "database_gateway_plan_sharding_key_generator";
    
    private static final String PLAN_SHARDING_KEY_GENERATE_STRATEGY = "database_gateway_plan_sharding_key_generate_strategy";
    
    private static final String PLAN_SHARDING_COMPONENT_CLEANUP = "database_gateway_plan_sharding_rule_component_cleanup";
    
    private static final Map<String, String> PLANNING_TOOLS = Map.ofEntries(Map.entry(ENCRYPT_RULE_WORKFLOW_KIND, PLAN_ENCRYPT_RULE), Map.entry(MASK_RULE_WORKFLOW_KIND, PLAN_MASK_RULE),
            Map.entry(BROADCAST_RULE_WORKFLOW_KIND, PLAN_BROADCAST_RULE), Map.entry(READWRITE_RULE_WORKFLOW_KIND, PLAN_READWRITE_RULE),
            Map.entry(READWRITE_STATUS_WORKFLOW_KIND, PLAN_READWRITE_STATUS), Map.entry(SHADOW_RULE_WORKFLOW_KIND, PLAN_SHADOW_RULE),
            Map.entry(SHADOW_DEFAULT_ALGORITHM_WORKFLOW_KIND, PLAN_DEFAULT_SHADOW_ALGORITHM), Map.entry(SHADOW_ALGORITHM_CLEANUP_WORKFLOW_KIND, PLAN_SHADOW_ALGORITHM_CLEANUP),
            Map.entry(SHARDING_TABLE_RULE_WORKFLOW_KIND, PLAN_SHARDING_TABLE_RULE), Map.entry(SHARDING_TABLE_REFERENCE_WORKFLOW_KIND, PLAN_SHARDING_TABLE_REFERENCE_RULE),
            Map.entry(SHARDING_DEFAULT_STRATEGY_WORKFLOW_KIND, PLAN_SHARDING_DEFAULT_STRATEGY), Map.entry(SHARDING_KEY_GENERATOR_WORKFLOW_KIND, PLAN_SHARDING_KEY_GENERATOR),
            Map.entry(SHARDING_KEY_GENERATE_STRATEGY_WORKFLOW_KIND, PLAN_SHARDING_KEY_GENERATE_STRATEGY), Map.entry(SHARDING_COMPONENT_CLEANUP_WORKFLOW_KIND, PLAN_SHARDING_COMPONENT_CLEANUP));
    
    /**
     * Append model-facing next action guidance to a planning response.
     *
     * @param payload response payload
     * @param snapshot workflow snapshot
     */
    public static void appendPlanningGuidance(final Map<String, Object> payload, final WorkflowContextSnapshot snapshot) {
        List<String> missingRequiredInputs = createMissingRequiredInputs(snapshot);
        List<Map<String, Object>> clarificationQuestions = createClarificationQuestions(snapshot, missingRequiredInputs);
        payload.put("missing_required_inputs", missingRequiredInputs);
        payload.put(MCPPayloadFieldNames.CLARIFICATION_QUESTIONS, clarificationQuestions);
        payload.put(MCPPayloadFieldNames.RESOURCES_TO_READ, createResourcesToRead(snapshot));
        payload.put("proxy_topology_hint", createProxyTopologyHint(snapshot));
        payload.put(MCPPayloadFieldNames.NEXT_ACTIONS, createPlanningNextActions(snapshot, missingRequiredInputs));
    }
    
    private static Map<String, Object> createProxyTopologyHint(final WorkflowContextSnapshot snapshot) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        boolean ruleDistSQLOnlyWorkflow = WorkflowArtifactPayloadUtils.isRuleDistSQLOnlyWorkflow(snapshot);
        result.put("expected_runtime_view", ruleDistSQLOnlyWorkflow ? "proxy_rule_distsql" : "proxy_logical_database");
        result.put("workflow_kind", resolveWorkflowKind(snapshot));
        result.put(MCPPayloadFieldNames.REASON, ruleDistSQLOnlyWorkflow
                ? "Rule DistSQL workflow planning must use Proxy DistSQL-visible rule state."
                : "Workflow planning must use Proxy logical metadata; physical-database metadata can hide or misrepresent rule-visible objects.");
        result.put("safe_recovery", ruleDistSQLOnlyWorkflow
                ? "Read the feature algorithm and rule resources from ShardingSphere Proxy before retrying."
                : "Reconnect the MCP runtime to ShardingSphere Proxy for this logical database if metadata appears to be physical-table-first.");
        return result;
    }
    
    /**
     * Append model-facing next action guidance to an apply response.
     *
     * @param payload response payload
     * @param status apply status
     */
    public static void appendApplyGuidance(final Map<String, Object> payload, final String status) {
        List<Map<String, Object>> nextActions = new LinkedList<>();
        if (WorkflowLifecycle.STATUS_COMPLETED.equals(status)) {
            nextActions.add(createToolAction(VALIDATE_WORKFLOW, "Validate the runtime state after workflow artifacts are applied or exported.",
                    Map.of(WorkflowFieldNames.PLAN_ID, Objects.toString(payload.get(WorkflowFieldNames.PLAN_ID), ""))));
        }
        if (WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION.equals(status)) {
            nextActions.add(createUserAction("Confirm the manual artifacts were executed outside MCP before validation.", List.of("manual_artifacts_executed")));
        }
        if (WorkflowLifecycle.STATUS_FAILED.equals(status) && isSecretReferenceRecovery(payload)) {
            nextActions.add(createUserAction("Review the manual artifacts, replace neutral secret placeholders outside MCP, and execute them through the normal operational channel.",
                    List.of("manual_artifacts")));
        } else if (WorkflowLifecycle.STATUS_FAILED.equals(status)) {
            nextActions.add(createUserAction("Inspect issues and retry database_gateway_apply_workflow only after the failed artifact is corrected.", List.of("issues")));
        }
        payload.put(MCPPayloadFieldNames.NEXT_ACTIONS, addSequencing(nextActions));
    }
    
    private static boolean isSecretReferenceRecovery(final Map<String, Object> payload) {
        Object category = payload.get("category");
        return MCPDiagnosticCategory.SECRET_REFERENCE_MALFORMED.equals(category)
                || MCPDiagnosticCategory.SECRET_REFERENCE_MISSING.equals(category)
                || MCPDiagnosticCategory.SECRET_REFERENCE_MANUAL_EXECUTION_REQUIRED.equals(category);
    }
    
    /**
     * Append model-facing next action guidance to a validation response.
     *
     * @param payload response payload
     * @param snapshot workflow snapshot
     * @param validationReport validation report
     */
    public static void appendValidationGuidance(final Map<String, Object> payload, final WorkflowContextSnapshot snapshot, final ValidationReport validationReport) {
        boolean failed = WorkflowLifecycle.STATUS_FAILED.equals(validationReport.getOverallStatus());
        payload.put("recovery_guidance", failed ? createValidationRecovery(snapshot) : "");
        payload.put(MCPPayloadFieldNames.NEXT_ACTIONS, failed ? createValidationFailureActions(snapshot) : List.of(createStopAction()));
    }
    
    private static String createValidationRecovery(final WorkflowContextSnapshot snapshot) {
        return isManualOnlyWorkflow(snapshot)
                ? "Manual-only artifacts are exported but not executed by MCP. Execute them manually, then run database_gateway_validate_workflow again."
                : "Inspect mismatches, adjust the plan or runtime state, then run database_gateway_validate_workflow again.";
    }
    
    private static List<Map<String, Object>> createValidationFailureActions(final WorkflowContextSnapshot snapshot) {
        if (isManualOnlyWorkflow(snapshot)) {
            return List.of(createUserAction("Confirm the manual artifacts were executed outside MCP, then run database_gateway_validate_workflow again.", List.of("manual_artifacts_executed")));
        }
        String planningTool = resolvePlanningTool(snapshot);
        return planningTool.isEmpty()
                ? List.of(createUserAction("Confirm the workflow kind before re-planning with the existing plan_id.", List.of("workflow_kind", "mismatches")))
                : List.of(createToolAction(planningTool, "Re-plan with corrected metadata or algorithm choices.", Map.of(WorkflowFieldNames.PLAN_ID, snapshot.getPlanId())));
    }
    
    private static boolean isManualOnlyWorkflow(final WorkflowContextSnapshot snapshot) {
        return null != snapshot.getRequest() && EXECUTION_MODE_MANUAL_ONLY.equalsIgnoreCase(snapshot.getRequest().getExecutionMode());
    }
    
    private static List<String> createMissingRequiredInputs(final WorkflowContextSnapshot snapshot) {
        List<String> result = new LinkedList<>();
        ClarifiedIntent clarifiedIntent = snapshot.getClarifiedIntent();
        for (String each : clarifiedIntent.getUnresolvedFields()) {
            String missingInput = normalizeMissingInput(snapshot, each);
            if (!result.contains(missingInput)) {
                result.add(missingInput);
            }
        }
        for (WorkflowIssue each : snapshot.getIssues()) {
            addMissingInputsFromIssue(result, snapshot, each);
        }
        if (result.isEmpty() && !clarifiedIntent.getClarificationMessages().isEmpty()) {
            result.add("user_clarification");
        }
        return result;
    }
    
    private static List<Map<String, Object>> createClarificationQuestions(final WorkflowContextSnapshot snapshot, final List<String> missingRequiredInputs) {
        List<Map<String, Object>> result = new LinkedList<>();
        List<String> clarificationMessages = snapshot.getClarifiedIntent().getClarificationMessages();
        for (int i = 0; i < missingRequiredInputs.size(); i++) {
            String fieldName = missingRequiredInputs.get(i);
            result.add(createClarificationQuestion(snapshot, fieldName, i < clarificationMessages.size() ? clarificationMessages.get(i) : ""));
        }
        return result;
    }
    
    private static Map<String, Object> createClarificationQuestion(final WorkflowContextSnapshot snapshot, final String fieldName, final String clarificationMessage) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        String inputType = resolveClarificationInputType(snapshot, fieldName);
        result.put(MCPPayloadFieldNames.FIELD, fieldName);
        result.put("question_key", fieldName.replace('.', '_'));
        result.put(MCPPayloadFieldNames.INPUT_TYPE, inputType);
        if ("boolean".equals(inputType)) {
            result.put(MCPPayloadFieldNames.ALLOWED_VALUES, List.of(true, false));
        }
        result.put(MCPPayloadFieldNames.SECRET, isSecretClarificationField(snapshot, fieldName));
        result.put(MCPPayloadFieldNames.DISPLAY_MESSAGE, clarificationMessage.isBlank() ? String.format("Please provide `%s`.", fieldName) : clarificationMessage);
        return result;
    }
    
    private static String resolveClarificationInputType(final WorkflowContextSnapshot snapshot, final String fieldName) {
        if (isSecretClarificationField(snapshot, fieldName)) {
            return "secret";
        }
        return fieldName.startsWith("requires_") ? "boolean" : "string";
    }
    
    private static boolean isSecretClarificationField(final WorkflowContextSnapshot snapshot, final String fieldName) {
        String propertyKey = resolveAlgorithmPropertyKey(fieldName);
        if (propertyKey.isEmpty()) {
            return false;
        }
        for (AlgorithmPropertyRequirement each : snapshot.getPropertyRequirements()) {
            if (propertyKey.equals(each.getPropertyKey())) {
                return each.isSecret();
            }
        }
        return false;
    }
    
    private static void addMissingInputsFromIssue(final Collection<String> missingInputs, final WorkflowContextSnapshot snapshot, final WorkflowIssue issue) {
        addRequiredIdentifierInput(missingInputs, issue, WorkflowIssueCode.DATABASE_REQUIRED, WorkflowFieldNames.DATABASE);
        addRequiredIdentifierInput(missingInputs, issue, WorkflowIssueCode.TABLE_REQUIRED, WorkflowFieldNames.TABLE);
        addRequiredIdentifierInput(missingInputs, issue, WorkflowIssueCode.COLUMN_REQUIRED, WorkflowFieldNames.COLUMN);
        Object missingRuleInputs = issue.getDetails().get("missing_inputs");
        if (missingRuleInputs instanceof Collection) {
            for (Object each : (Collection<?>) missingRuleInputs) {
                String missingInput = String.valueOf(each);
                if (!missingInputs.contains(missingInput)) {
                    missingInputs.add(missingInput);
                }
            }
        }
        Object missingProperties = issue.getDetails().get("missing_properties");
        if (missingProperties instanceof Collection) {
            for (Object each : (Collection<?>) missingProperties) {
                String missingInput = resolveAlgorithmPropertyInput(snapshot, String.valueOf(each));
                if (!missingInputs.contains(missingInput)) {
                    missingInputs.add(missingInput);
                }
            }
        }
    }
    
    private static void addRequiredIdentifierInput(final Collection<String> missingInputs, final WorkflowIssue issue, final String issueCode, final String fieldName) {
        if (issueCode.equals(issue.getCode()) && !missingInputs.contains(fieldName)) {
            missingInputs.add(fieldName);
        }
    }
    
    private static String normalizeMissingInput(final WorkflowContextSnapshot snapshot, final String fieldName) {
        if (!fieldName.startsWith("algorithm_properties.")) {
            return fieldName;
        }
        return resolveAlgorithmPropertyInput(snapshot, fieldName.substring("algorithm_properties.".length()));
    }
    
    private static String resolveAlgorithmPropertyInput(final WorkflowContextSnapshot snapshot, final String propertyKey) {
        for (AlgorithmPropertyRequirement each : snapshot.getPropertyRequirements()) {
            if (propertyKey.equals(each.getPropertyKey())) {
                return String.format("%s.%s", resolveAlgorithmPropertiesArgument(each.getAlgorithmRole()), propertyKey);
            }
        }
        return String.format("%s.%s", WorkflowFieldNames.PRIMARY_ALGORITHM_PROPERTIES, propertyKey);
    }
    
    private static String resolveAlgorithmPropertiesArgument(final String algorithmRole) {
        if ("assisted_query".equals(algorithmRole)) {
            return WorkflowFieldNames.ASSISTED_QUERY_ALGORITHM_PROPERTIES;
        }
        if ("like_query".equals(algorithmRole)) {
            return WorkflowFieldNames.LIKE_QUERY_ALGORITHM_PROPERTIES;
        }
        return WorkflowFieldNames.PRIMARY_ALGORITHM_PROPERTIES;
    }
    
    private static String resolveAlgorithmPropertyKey(final String fieldName) {
        int separatorIndex = fieldName.indexOf('.');
        if (-1 == separatorIndex) {
            return "";
        }
        String argumentName = fieldName.substring(0, separatorIndex);
        if ("algorithm_properties".equals(argumentName) || argumentName.endsWith("_algorithm_properties")) {
            return fieldName.substring(separatorIndex + 1);
        }
        return "";
    }
    
    private static List<Map<String, Object>> createResourcesToRead(final WorkflowContextSnapshot snapshot) {
        List<Map<String, Object>> result = new LinkedList<>();
        addFeatureResources(result, snapshot);
        WorkflowRequest request = snapshot.getRequest();
        if (!request.getDatabase().isEmpty()) {
            addRuleResources(result, snapshot, request);
            addGovernanceMetadataResources(result, snapshot, request);
            if (WorkflowArtifactPayloadUtils.isRuleDistSQLOnlyWorkflow(snapshot) && !request.getTable().isEmpty()) {
                addFeatureTableRuleResources(result, snapshot, request);
            } else if (!request.getSchema().isEmpty() && !request.getTable().isEmpty()) {
                addTableResources(result, request);
            }
        }
        return result;
    }
    
    private static void addFeatureResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowContextSnapshot snapshot) {
        switch (resolveWorkflowKind(snapshot)) {
            case ENCRYPT_RULE_WORKFLOW_KIND -> addResourceHint(resourcesToRead, "shardingsphere://features/encrypt/algorithms", "algorithm", "read_first",
                    "Read encrypt algorithm metadata before choosing algorithm arguments.");
            case MASK_RULE_WORKFLOW_KIND -> addResourceHint(resourcesToRead, "shardingsphere://features/mask/algorithms", "algorithm", "read_first",
                    "Read mask algorithm metadata before choosing algorithm arguments.");
            case READWRITE_RULE_WORKFLOW_KIND -> addResourceHint(resourcesToRead, "shardingsphere://features/readwrite-splitting/load-balance-algorithm-plugins", "algorithm", "read_first",
                    "Read load-balance algorithm plugin metadata before choosing algorithm arguments.");
            case SHADOW_RULE_WORKFLOW_KIND, SHADOW_DEFAULT_ALGORITHM_WORKFLOW_KIND -> addResourceHint(resourcesToRead, "shardingsphere://features/shadow/algorithm-plugins", "algorithm",
                    "read_first", "Read shadow algorithm plugin metadata before choosing algorithm arguments.");
            case SHARDING_TABLE_RULE_WORKFLOW_KIND, SHARDING_DEFAULT_STRATEGY_WORKFLOW_KIND -> addResourceHint(resourcesToRead, "shardingsphere://features/sharding/algorithm-plugins",
                    "algorithm", "read_first", "Read sharding algorithm plugin metadata before choosing algorithm arguments.");
            case SHARDING_KEY_GENERATOR_WORKFLOW_KIND, SHARDING_KEY_GENERATE_STRATEGY_WORKFLOW_KIND -> addResourceHint(resourcesToRead,
                    "shardingsphere://features/sharding/key-generate-algorithm-plugins", "algorithm", "read_first",
                    "Read key-generate algorithm plugin metadata before choosing generator arguments.");
            default -> {
            }
        }
    }
    
    private static void addGovernanceMetadataResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        String workflowKind = resolveWorkflowKind(snapshot);
        switch (workflowKind) {
            case READWRITE_RULE_WORKFLOW_KIND, READWRITE_STATUS_WORKFLOW_KIND, SHADOW_RULE_WORKFLOW_KIND, SHARDING_TABLE_RULE_WORKFLOW_KIND -> addStorageUnitsResourceHint(resourcesToRead,
                    request);
            default -> {
            }
        }
        switch (workflowKind) {
            case SHADOW_RULE_WORKFLOW_KIND, SHARDING_TABLE_RULE_WORKFLOW_KIND -> {
                addSingleTablesResourceHint(resourcesToRead, request);
                if (!request.getTable().isEmpty()) {
                    addSingleTableResourceHint(resourcesToRead, request);
                }
            }
            default -> {
            }
        }
    }
    
    private static void addFeatureTableRuleResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        switch (resolveWorkflowKind(snapshot)) {
            case ENCRYPT_RULE_WORKFLOW_KIND -> addTableResourceHint(resourcesToRead, request, "shardingsphere://features/encrypt/databases/%s/tables/%s/rules",
                    "Inspect current encrypt table rule DistSQL state before planning changes.");
            case MASK_RULE_WORKFLOW_KIND -> addTableResourceHint(resourcesToRead, request, "shardingsphere://features/mask/databases/%s/tables/%s/rules",
                    "Inspect current mask table rule DistSQL state before planning changes.");
            case SHADOW_RULE_WORKFLOW_KIND -> addTableResourceHint(resourcesToRead, request, "shardingsphere://features/shadow/databases/%s/tables/%s/rules",
                    "Inspect current shadow table rule DistSQL state before planning changes.");
            case SHARDING_TABLE_RULE_WORKFLOW_KIND -> {
                addTableResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/tables/%s/table-rule",
                        "Inspect current sharding table rule DistSQL state before planning changes.");
                addTableResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/tables/%s/nodes",
                        "Inspect current sharding table nodes before planning changes.");
            }
            default -> {
            }
        }
    }
    
    private static void addRuleResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        switch (resolveWorkflowKind(snapshot)) {
            case ENCRYPT_RULE_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/encrypt/databases/%s/rules",
                    "Inspect current encrypt rules before planning changes.");
            case MASK_RULE_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/mask/databases/%s/rules",
                    "Inspect current mask rules before planning changes.");
            case BROADCAST_RULE_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/broadcast/databases/%s/rules",
                    "Inspect current broadcast rules before planning changes.");
            case READWRITE_RULE_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/readwrite-splitting/databases/%s/rules",
                    "Inspect current readwrite-splitting rules before planning changes.");
            case READWRITE_STATUS_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/readwrite-splitting/databases/%s/status",
                    "Inspect current readwrite-splitting status before planning changes.");
            case SHADOW_RULE_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/shadow/databases/%s/rules",
                    "Inspect current shadow rules before planning changes.");
            case SHADOW_DEFAULT_ALGORITHM_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/shadow/databases/%s/default-algorithm",
                    "Inspect current default shadow algorithm before planning changes.");
            case SHADOW_ALGORITHM_CLEANUP_WORKFLOW_KIND -> {
                addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/shadow/databases/%s/algorithms",
                        "Inspect configured shadow algorithms before planning cleanup.");
                addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/shadow/databases/%s/table-rules",
                        "Inspect shadow table rule references before planning cleanup.");
                addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/shadow/databases/%s/default-algorithm",
                        "Inspect default shadow algorithm references before planning cleanup.");
            }
            case SHARDING_TABLE_RULE_WORKFLOW_KIND -> addShardingTableRuleResources(resourcesToRead, request);
            case SHARDING_TABLE_REFERENCE_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/table-reference-rules",
                    "Inspect current sharding table reference rules before planning changes.");
            case SHARDING_DEFAULT_STRATEGY_WORKFLOW_KIND -> addShardingDefaultStrategyResources(resourcesToRead, request);
            case SHARDING_KEY_GENERATOR_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/key-generators",
                    "Inspect current sharding key generators before planning changes.");
            case SHARDING_KEY_GENERATE_STRATEGY_WORKFLOW_KIND -> addShardingKeyGenerateStrategyResources(resourcesToRead, request);
            case SHARDING_COMPONENT_CLEANUP_WORKFLOW_KIND -> addShardingComponentCleanupResources(resourcesToRead, request);
            default -> {
            }
        }
    }
    
    private static void addDatabaseResourceHint(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request, final String uriTemplate, final String reason) {
        addResourceHint(resourcesToRead, String.format(uriTemplate, MCPUriPathSegmentUtils.encodePathSegment(request.getDatabase())), "rule", "inspect_detail", reason);
    }
    
    private static void addStorageUnitsResourceHint(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        addResourceHint(resourcesToRead, String.format("shardingsphere://databases/%s/storage-units", MCPUriPathSegmentUtils.encodePathSegment(request.getDatabase())),
                "storage-unit", "validate_scope", "Read storage units before planning DistSQL that references storage units.");
    }
    
    private static void addSingleTablesResourceHint(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        addResourceHint(resourcesToRead, String.format("shardingsphere://databases/%s/single-tables", MCPUriPathSegmentUtils.encodePathSegment(request.getDatabase())),
                "single-table", "validate_scope", "Read single table mappings before planning table-level DistSQL.");
    }
    
    private static void addSingleTableResourceHint(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        addResourceHint(resourcesToRead, String.format("shardingsphere://databases/%s/single-tables/%s", MCPUriPathSegmentUtils.encodePathSegment(request.getDatabase()),
                MCPUriPathSegmentUtils.encodePathSegment(request.getTable())), "single-table", "validate_scope",
                "Read the target single table mapping before planning table-level DistSQL.");
    }
    
    private static void addTableResourceHint(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request, final String uriTemplate, final String reason) {
        addResourceHint(resourcesToRead, String.format(uriTemplate, MCPUriPathSegmentUtils.encodePathSegment(request.getDatabase()), MCPUriPathSegmentUtils.encodePathSegment(request.getTable())),
                "rule", "inspect_detail", reason);
    }
    
    private static void addResourceHint(final Collection<Map<String, Object>> resourcesToRead, final String uri, final String resourceKind, final String action, final String reason) {
        resourcesToRead.add(MCPResourceHintUtils.create(uri, resourceKind, action, reason, MCPPayloadFieldNames.RESOURCES_TO_READ));
    }
    
    private static void addShardingTableRuleResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/table-rules",
                "Inspect current sharding table rules before planning changes.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/table-nodes",
                "Inspect current sharding table nodes before planning changes.");
    }
    
    private static void addShardingDefaultStrategyResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/default-strategy",
                "Inspect current default sharding strategy before planning changes.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/algorithms",
                "Inspect configured sharding algorithms before planning default strategy changes.");
    }
    
    private static void addShardingKeyGenerateStrategyResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/key-generate-strategies",
                "Inspect current sharding key generate strategies before planning changes.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/key-generators",
                "Inspect current sharding key generators before planning key generate strategy changes.");
    }
    
    private static void addShardingComponentCleanupResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/algorithms",
                "Inspect configured sharding algorithms before planning cleanup.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/key-generators",
                "Inspect configured sharding key generators before planning cleanup.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/auditors",
                "Inspect configured sharding auditors before planning cleanup.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/unused-algorithms",
                "Inspect unused sharding algorithms before planning cleanup.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/unused-key-generators",
                "Inspect unused sharding key generators before planning cleanup.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/unused-auditors",
                "Inspect unused sharding auditors before planning cleanup.");
    }
    
    private static void addTableResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        resourcesToRead.add(MCPResourceHintUtils.create(String.format("shardingsphere://databases/%s/schemas/%s/tables/%s/columns", MCPUriPathSegmentUtils.encodePathSegment(request.getDatabase()),
                MCPUriPathSegmentUtils.encodePathSegment(request.getSchema()), MCPUriPathSegmentUtils.encodePathSegment(request.getTable())),
                "column", "validate_scope", "Read table columns before planning column-level workflow changes.", MCPPayloadFieldNames.RESOURCES_TO_READ));
    }
    
    private static List<Map<String, Object>> createPlanningNextActions(final WorkflowContextSnapshot snapshot, final List<String> missingRequiredInputs) {
        if (WorkflowLifecycle.STATUS_CLARIFYING.equals(snapshot.getStatus())) {
            return List.of(createUserAction("Ask for the missing inputs, then call the same planning tool with the existing plan_id.", missingRequiredInputs));
        }
        if (WorkflowLifecycle.STATUS_PLANNED.equals(snapshot.getStatus())) {
            return List.of(createToolAction(APPLY_WORKFLOW, "Preview workflow artifacts before execution.",
                    Map.of(WorkflowFieldNames.PLAN_ID, snapshot.getPlanId(), WorkflowFieldNames.EXECUTION_MODE, EXECUTION_MODE_PREVIEW)));
        }
        if (WorkflowLifecycle.STATUS_FAILED.equals(snapshot.getStatus())) {
            return createRecoveryPlanningActions(snapshot);
        }
        return List.of();
    }
    
    private static List<Map<String, Object>> createRecoveryPlanningActions(final WorkflowContextSnapshot snapshot) {
        String planningTool = resolvePlanningTool(snapshot);
        return planningTool.isEmpty()
                ? List.of(createUserAction("Confirm the workflow kind, then call the matching planning tool with the existing plan_id.", List.of("workflow_kind", "issues")))
                : List.of(createToolAction(planningTool, "Re-plan after resolving the reported issues.", Map.of(WorkflowFieldNames.PLAN_ID, snapshot.getPlanId())));
    }
    
    private static Map<String, Object> createToolAction(final String targetTool, final String reason, final Map<String, Object> requiredArguments) {
        return MCPNextActionUtils.callTool(targetTool, reason, requiredArguments);
    }
    
    private static Map<String, Object> createUserAction(final String reason, final List<String> requiredInputs) {
        return MCPNextActionUtils.askUser(reason, requiredInputs);
    }
    
    private static Map<String, Object> createStopAction() {
        return MCPNextActionUtils.stop("Validation passed. Report the confirmed workflow result to the user.");
    }
    
    private static List<Map<String, Object>> addSequencing(final List<Map<String, Object>> nextActions) {
        List<Map<String, Object>> result = new LinkedList<>(nextActions);
        for (int index = 0; index < result.size(); index++) {
            result.get(index).put("order", index + 1);
            if (0 < index && "ask_user".equals(result.get(index - 1).get("type"))) {
                result.get(index).put("depends_on", List.of(index));
            }
        }
        return result;
    }
    
    private static String resolvePlanningTool(final WorkflowContextSnapshot snapshot) {
        return PLANNING_TOOLS.getOrDefault(resolveWorkflowKind(snapshot), "");
    }
    
    private static String resolveWorkflowKind(final WorkflowContextSnapshot snapshot) {
        WorkflowKind workflowKind = snapshot.getWorkflowKind();
        return null == workflowKind ? "" : workflowKind.getValue();
    }
    
}
