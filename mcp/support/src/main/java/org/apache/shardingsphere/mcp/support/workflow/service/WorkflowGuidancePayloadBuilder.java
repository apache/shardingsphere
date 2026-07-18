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
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;

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
        payload.put(MCPPayloadFieldNames.RESOURCES_TO_READ, new WorkflowGuidanceResourceHintProvider().createResourcesToRead(snapshot));
        payload.put("proxy_topology_hint", createProxyTopologyHint(snapshot));
        payload.put(MCPPayloadFieldNames.NEXT_ACTIONS, createPlanningNextActions(snapshot, missingRequiredInputs));
    }
    
    private static Map<String, Object> createProxyTopologyHint(final WorkflowContextSnapshot snapshot) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("expected_runtime_view", "proxy_rule_distsql");
        result.put("workflow_kind", resolveWorkflowKind(snapshot));
        result.put(MCPPayloadFieldNames.REASON, "Rule DistSQL workflow planning must use Proxy DistSQL-visible rule state.");
        result.put("safe_recovery", "Read the feature algorithm and rule resources from ShardingSphere Proxy before retrying.");
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
            nextActions.add(createToolAction(WorkflowToolDescriptors.VALIDATE_TOOL_NAME, "Validate the runtime state after workflow artifacts are applied or exported.",
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
        payload.put(MCPPayloadFieldNames.NEXT_ACTIONS, nextActions);
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
        return null != snapshot.getRequest() && WorkflowLifecycle.EXECUTION_MODE_MANUAL_ONLY.equalsIgnoreCase(snapshot.getRequest().getExecutionMode());
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
        boolean secret = isSecretClarificationField(snapshot, fieldName);
        String inputType = resolveClarificationInputType(fieldName, secret);
        result.put(MCPPayloadFieldNames.FIELD, fieldName);
        result.put("question_key", fieldName.replace('.', '_'));
        result.put(MCPPayloadFieldNames.INPUT_TYPE, inputType);
        if ("boolean".equals(inputType)) {
            result.put(MCPPayloadFieldNames.ALLOWED_VALUES, List.of(true, false));
        }
        result.put(MCPPayloadFieldNames.SECRET, secret);
        result.put(MCPPayloadFieldNames.DISPLAY_MESSAGE, clarificationMessage.isBlank() ? String.format("Please provide `%s`.", fieldName) : clarificationMessage);
        return result;
    }
    
    private static String resolveClarificationInputType(final String fieldName, final boolean secret) {
        if (secret) {
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
        return true;
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
    
    private static List<Map<String, Object>> createPlanningNextActions(final WorkflowContextSnapshot snapshot, final List<String> missingRequiredInputs) {
        if (WorkflowLifecycle.STATUS_CLARIFYING.equals(snapshot.getStatus())) {
            return List.of(createUserAction("Ask for the missing inputs, then call the same planning tool with the existing plan_id.", missingRequiredInputs));
        }
        if (WorkflowLifecycle.STATUS_PLANNED.equals(snapshot.getStatus())) {
            return List.of(createToolAction(WorkflowToolDescriptors.APPLY_TOOL_NAME, "Preview workflow artifacts before execution.",
                    Map.of(WorkflowFieldNames.PLAN_ID, snapshot.getPlanId(), WorkflowFieldNames.EXECUTION_MODE, WorkflowLifecycle.EXECUTION_MODE_PREVIEW)));
        }
        if (WorkflowLifecycle.STATUS_FAILED.equals(snapshot.getStatus())) {
            return createRecoveryPlanningActions(snapshot);
        }
        return List.of();
    }
    
    private static List<Map<String, Object>> createRecoveryPlanningActions(final WorkflowContextSnapshot snapshot) {
        if (hasIssue(snapshot, WorkflowIssueCode.CLUSTER_MODE_REQUIRED)) {
            return List.of(MCPNextActionUtils.stop("Connect to a Cluster-mode ShardingSphere Proxy, then start a new workflow plan."));
        }
        String planningTool = resolvePlanningTool(snapshot);
        if (hasIssue(snapshot, WorkflowIssueCode.RULE_INPUT_CONFLICT)) {
            return planningTool.isEmpty()
                    ? List.of(createUserAction("Choose one input mode, remove conflicting inputs, and start a new plan without plan_id.", List.of("workflow_kind", "conflicting_inputs")))
                    : List.of(createToolAction(planningTool, "Choose one input mode, remove conflicting inputs, and start a new plan without plan_id.", Map.of()));
        }
        return planningTool.isEmpty()
                ? List.of(createUserAction("Confirm the workflow kind, then call the matching planning tool with the existing plan_id.", List.of("workflow_kind", "issues")))
                : List.of(createToolAction(planningTool, "Re-plan after resolving the reported issues.", Map.of(WorkflowFieldNames.PLAN_ID, snapshot.getPlanId())));
    }
    
    private static boolean hasIssue(final WorkflowContextSnapshot snapshot, final String issueCode) {
        return snapshot.getIssues().stream().anyMatch(each -> issueCode.equals(each.getCode()));
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
    
    private static String resolvePlanningTool(final WorkflowContextSnapshot snapshot) {
        return MCPDescriptorCatalogIndex.findPlanningToolNameByWorkflowKind(resolveWorkflowKind(snapshot)).orElse("");
    }
    
    private static String resolveWorkflowKind(final WorkflowContextSnapshot snapshot) {
        WorkflowKind workflowKind = snapshot.getWorkflowKind();
        return null == workflowKind ? "" : workflowKind.getValue();
    }
    
}
