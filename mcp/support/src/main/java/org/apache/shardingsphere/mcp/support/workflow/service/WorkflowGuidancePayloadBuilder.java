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
import org.apache.shardingsphere.mcp.api.resource.MCPUriTemplateUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
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

    private static final String APPLY_WORKFLOW = "apply_workflow";

    private static final String VALIDATE_WORKFLOW = "validate_workflow";

    private static final String EXECUTION_MODE_PREVIEW = "preview";

    private static final String EXECUTION_MODE_MANUAL_ONLY = "manual-only";

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
        payload.put("clarification_questions", clarificationQuestions);
        payload.put("resources_to_read", createResourcesToRead(snapshot));
        payload.put("proxy_topology_hint", createProxyTopologyHint(snapshot));
        payload.put("next_actions", createPlanningNextActions(snapshot, missingRequiredInputs));
        payload.put("requires_user_approval", false);
    }

    private static Map<String, Object> createProxyTopologyHint(final WorkflowContextSnapshot snapshot) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("expected_runtime_view", "proxy_logical_database");
        result.put("workflow_kind", resolveWorkflowKind(snapshot));
        result.put("reason", "Encrypt and mask workflow planning must use Proxy logical metadata; physical-database metadata can hide or misrepresent rule-visible objects.");
        result.put("safe_recovery", "Reconnect the MCP runtime to ShardingSphere Proxy for this logical database if metadata appears to be physical-table-first.");
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
        if (WorkflowLifecycle.STATUS_COMPLETED.equals(status) || WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION.equals(status)) {
            nextActions.add(createToolAction(VALIDATE_WORKFLOW, "Validate the runtime state after workflow artifacts are applied or exported.",
                    Map.of("plan_id", Objects.toString(payload.get("plan_id"), "")), false));
        }
        if (WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION.equals(status)) {
            nextActions.add(0, createUserAction("Confirm the manual artifacts were executed outside MCP before validation.", true, List.of("manual_artifacts_executed")));
        }
        if (WorkflowLifecycle.STATUS_FAILED.equals(status)) {
            nextActions.add(createUserAction("Inspect issues and retry apply_workflow only after the failed artifact is corrected.", true, List.of("issues")));
        }
        payload.put("next_actions", addSequencing(nextActions));
        payload.put("requires_user_approval", WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION.equals(status) || WorkflowLifecycle.STATUS_FAILED.equals(status));
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
        payload.put("recommended_recovery", failed ? createValidationRecovery(snapshot) : "");
        payload.put("next_actions", failed ? createValidationFailureActions(snapshot) : List.of(createStopAction()));
        payload.put("requires_user_approval", failed && isManualOnlyWorkflow(snapshot));
    }

    private static String createValidationRecovery(final WorkflowContextSnapshot snapshot) {
        return isManualOnlyWorkflow(snapshot)
                ? "Manual-only artifacts are exported but not executed by MCP. Execute them manually, then run validate_workflow again."
                : "Inspect mismatches, adjust the plan or runtime state, then run validate_workflow again.";
    }

    private static List<Map<String, Object>> createValidationFailureActions(final WorkflowContextSnapshot snapshot) {
        if (isManualOnlyWorkflow(snapshot)) {
            return List.of(createUserAction("Confirm the manual artifacts were executed outside MCP, then run validate_workflow again.", true, List.of("manual_artifacts_executed")));
        }
        String planningTool = resolvePlanningTool(snapshot);
        return planningTool.isEmpty()
                ? List.of(createUserAction("Confirm the workflow kind before re-planning with the existing plan_id.", false, List.of("workflow_kind", "mismatches")))
                : List.of(createToolAction(planningTool, "Re-plan with corrected metadata or algorithm choices.", Map.of("plan_id", snapshot.getPlanId()), false));
    }

    private static boolean isManualOnlyWorkflow(final WorkflowContextSnapshot snapshot) {
        return null != snapshot.getRequest() && EXECUTION_MODE_MANUAL_ONLY.equalsIgnoreCase(snapshot.getRequest().getExecutionMode());
    }

    private static List<String> createMissingRequiredInputs(final WorkflowContextSnapshot snapshot) {
        final List<String> result = new LinkedList<>();
        final ClarifiedIntent clarifiedIntent = snapshot.getClarifiedIntent();
        for (final String each : clarifiedIntent.getUnresolvedFields()) {
            final String missingInput = normalizeMissingInput(snapshot, each);
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
        final List<Map<String, Object>> result = new LinkedList<>();
        final List<String> clarificationMessages = snapshot.getClarifiedIntent().getClarificationMessages();
        for (int i = 0; i < missingRequiredInputs.size(); i++) {
            final String fieldName = missingRequiredInputs.get(i);
            result.add(createClarificationQuestion(snapshot, fieldName, i < clarificationMessages.size() ? clarificationMessages.get(i) : ""));
        }
        return result;
    }

    private static Map<String, Object> createClarificationQuestion(final WorkflowContextSnapshot snapshot, final String fieldName, final String clarificationMessage) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        String inputType = resolveClarificationInputType(snapshot, fieldName);
        result.put("field", fieldName);
        result.put("question_key", fieldName.replace('.', '_'));
        result.put("input_type", inputType);
        if ("boolean".equals(inputType)) {
            result.put("allowed_values", List.of(true, false));
        }
        result.put("secret", isSecretClarificationField(snapshot, fieldName));
        result.put("display_message", clarificationMessage.isBlank() ? String.format("Please provide `%s`.", fieldName) : clarificationMessage);
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

    private static void addMissingInputsFromIssue(final Collection<String> result, final WorkflowContextSnapshot snapshot, final WorkflowIssue issue) {
        if (WorkflowIssueCode.DATABASE_REQUIRED.equals(issue.getCode()) && !result.contains("database")) {
            result.add("database");
        }
        Object missingProperties = issue.getDetails().get("missing_properties");
        if (missingProperties instanceof Collection) {
            for (Object each : (Collection<?>) missingProperties) {
                String missingInput = resolveAlgorithmPropertyInput(snapshot, String.valueOf(each));
                if (!result.contains(missingInput)) {
                    result.add(missingInput);
                }
            }
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
        return String.format("primary_algorithm_properties.%s", propertyKey);
    }

    private static String resolveAlgorithmPropertiesArgument(final String algorithmRole) {
        if ("assisted_query".equals(algorithmRole)) {
            return "assisted_query_algorithm_properties";
        }
        if ("like_query".equals(algorithmRole)) {
            return "like_query_algorithm_properties";
        }
        return "primary_algorithm_properties";
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
        final List<Map<String, Object>> result = new LinkedList<>();
        addFeatureResources(result, snapshot);
        final WorkflowRequest request = snapshot.getRequest();
        if (!request.getDatabase().isEmpty()) {
            addRuleResources(result, snapshot, request);
            if (!request.getSchema().isEmpty() && !request.getTable().isEmpty()) {
                addTableResources(result, snapshot, request);
            }
        }
        return result;
    }

    private static void addFeatureResources(final Collection<Map<String, Object>> result, final WorkflowContextSnapshot snapshot) {
        String workflowKind = resolveWorkflowKind(snapshot);
        if ("encrypt.rule".equals(workflowKind)) {
            result.add(MCPResourceHintUtils.create("shardingsphere://features/encrypt/algorithms", "algorithm", "read_first",
                    "Read encrypt algorithm metadata before choosing algorithm arguments.", "resources_to_read"));
        } else if ("mask.rule".equals(workflowKind)) {
            result.add(MCPResourceHintUtils.create("shardingsphere://features/mask/algorithms", "algorithm", "read_first",
                    "Read mask algorithm metadata before choosing algorithm arguments.", "resources_to_read"));
        }
    }

    private static void addRuleResources(final Collection<Map<String, Object>> result, final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        String workflowKind = resolveWorkflowKind(snapshot);
        if ("encrypt.rule".equals(workflowKind)) {
            result.add(MCPResourceHintUtils.create(String.format("shardingsphere://features/encrypt/databases/%s/rules",
                    MCPUriTemplateUtils.encodePathSegment(request.getDatabase())), "rule", "inspect_detail",
                    "Inspect current encrypt rules before planning changes.", "resources_to_read"));
        } else if ("mask.rule".equals(workflowKind)) {
            result.add(MCPResourceHintUtils.create(String.format("shardingsphere://features/mask/databases/%s/rules",
                    MCPUriTemplateUtils.encodePathSegment(request.getDatabase())), "rule", "inspect_detail",
                    "Inspect current mask rules before planning changes.", "resources_to_read"));
        }
    }

    private static void addTableResources(final Collection<Map<String, Object>> result, final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        result.add(MCPResourceHintUtils.create(String.format("shardingsphere://databases/%s/schemas/%s/tables/%s/columns", MCPUriTemplateUtils.encodePathSegment(request.getDatabase()),
                MCPUriTemplateUtils.encodePathSegment(request.getSchema()), MCPUriTemplateUtils.encodePathSegment(request.getTable())),
                "column", "validate_scope", "Read table columns before planning column-level workflow changes.", "resources_to_read"));
        if ("encrypt.rule".equals(resolveWorkflowKind(snapshot))) {
            result.add(MCPResourceHintUtils.create(String.format("shardingsphere://databases/%s/schemas/%s/tables/%s/indexes", MCPUriTemplateUtils.encodePathSegment(request.getDatabase()),
                    MCPUriTemplateUtils.encodePathSegment(request.getSchema()), MCPUriTemplateUtils.encodePathSegment(request.getTable())),
                    "index", "validate_scope", "Read table indexes before planning assisted-query encrypt rules.", "resources_to_read"));
        }
    }

    private static List<Map<String, Object>> createPlanningNextActions(final WorkflowContextSnapshot snapshot, final List<String> missingRequiredInputs) {
        if (WorkflowLifecycle.STATUS_CLARIFYING.equals(snapshot.getStatus())) {
            return List.of(createUserAction("Ask for the missing inputs, then call the same planning tool with the existing plan_id.", false, missingRequiredInputs));
        }
        if (WorkflowLifecycle.STATUS_PLANNED.equals(snapshot.getStatus())) {
            return List.of(createToolAction(APPLY_WORKFLOW, "Preview workflow artifacts before asking the user to approve execution.",
                    Map.of("plan_id", snapshot.getPlanId(), "execution_mode", EXECUTION_MODE_PREVIEW), false));
        }
        if (WorkflowLifecycle.STATUS_FAILED.equals(snapshot.getStatus())) {
            return createRecoveryPlanningActions(snapshot);
        }
        return List.of();
    }

    private static List<Map<String, Object>> createRecoveryPlanningActions(final WorkflowContextSnapshot snapshot) {
        String planningTool = resolvePlanningTool(snapshot);
        return planningTool.isEmpty()
                ? List.of(createUserAction("Confirm the workflow kind, then call the matching planning tool with the existing plan_id.", false, List.of("workflow_kind", "issues")))
                : List.of(createToolAction(planningTool, "Re-plan after resolving the reported issues.", Map.of("plan_id", snapshot.getPlanId()), false));
    }

    private static Map<String, Object> createToolAction(final String targetTool, final String reason, final Map<String, Object> requiredArguments, final boolean requiresUserApproval) {
        return MCPNextActionUtils.callTool(targetTool, reason, requiredArguments, requiresUserApproval);
    }

    private static Map<String, Object> createUserAction(final String reason, final boolean requiresUserApproval, final List<String> requiredInputs) {
        return MCPNextActionUtils.askUser(reason, requiredInputs, requiresUserApproval);
    }

    private static Map<String, Object> createStopAction() {
        return MCPNextActionUtils.stop("Validation passed. Report the confirmed workflow result to the user.");
    }

    private static List<Map<String, Object>> addSequencing(final List<Map<String, Object>> nextActions) {
        final List<Map<String, Object>> result = new LinkedList<>(nextActions);
        for (int index = 0; index < result.size(); index++) {
            result.get(index).put("order", index + 1);
            if (0 < index && "ask_user".equals(result.get(index - 1).get("action_kind"))) {
                result.get(index).put("depends_on", List.of(index));
            }
        }
        return result;
    }

    private static String resolvePlanningTool(final WorkflowContextSnapshot snapshot) {
        String workflowKind = resolveWorkflowKind(snapshot);
        if ("encrypt.rule".equals(workflowKind)) {
            return "plan_encrypt_rule";
        }
        return "mask.rule".equals(workflowKind) ? "plan_mask_rule" : "";
    }

    private static String resolveWorkflowKind(final WorkflowContextSnapshot snapshot) {
        WorkflowKind workflowKind = snapshot.getWorkflowKind();
        return null == workflowKind ? "" : workflowKind.getValue();
    }

}
