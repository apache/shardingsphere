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
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
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
    
    /**
     * Append model-facing next action guidance to a planning response.
     *
     * @param payload response payload
     * @param snapshot workflow snapshot
     */
    public static void appendPlanningGuidance(final Map<String, Object> payload, final WorkflowContextSnapshot snapshot) {
        List<String> missingRequiredInputs = createMissingRequiredInputs(snapshot);
        payload.put("missing_required_inputs", missingRequiredInputs);
        payload.put("resources_to_read", createResourcesToRead(snapshot));
        payload.put("next_actions", createPlanningNextActions(snapshot, missingRequiredInputs));
        payload.put("recommended_next_tool", createPlanningRecommendedNextTool(snapshot));
        payload.put("requires_user_approval", WorkflowLifecycle.STATUS_PLANNED.equals(snapshot.getStatus()));
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
            nextActions.add(0, createUserAction("Execute the manual artifacts outside MCP, then call validate_workflow with the same plan_id.", true, List.of("manual_artifacts")));
        }
        if (WorkflowLifecycle.STATUS_FAILED.equals(status)) {
            nextActions.add(createUserAction("Inspect issues and retry apply_workflow only after the failed artifact is corrected.", true, List.of("issues")));
        }
        payload.put("next_actions", nextActions);
        payload.put("recommended_next_tool", nextActions.isEmpty() ? "" : resolveTargetTool(nextActions.get(nextActions.size() - 1)));
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
        payload.put("recommended_recovery", failed ? "Inspect mismatches, adjust the plan or runtime state, then run validate_workflow again." : "");
        payload.put("next_actions", failed ? createValidationFailureActions(snapshot) : List.of());
        payload.put("recommended_next_tool", failed ? resolvePlanningTool(snapshot) : "");
        payload.put("requires_user_approval", false);
    }
    
    private static List<Map<String, Object>> createValidationFailureActions(final WorkflowContextSnapshot snapshot) {
        String planningTool = resolvePlanningTool(snapshot);
        return planningTool.isEmpty()
                ? List.of(createUserAction("Confirm the workflow kind before re-planning with the existing plan_id.", false, List.of("workflow_kind", "mismatches")))
                : List.of(createToolAction(planningTool, "Re-plan with corrected metadata or algorithm choices.", Map.of("plan_id", snapshot.getPlanId()), false));
    }
    
    private static List<String> createMissingRequiredInputs(final WorkflowContextSnapshot snapshot) {
        List<String> result = new LinkedList<>();
        ClarifiedIntent clarifiedIntent = snapshot.getClarifiedIntent();
        if (null != clarifiedIntent) {
            for (String each : clarifiedIntent.getUnresolvedFields()) {
                if (!result.contains(each)) {
                    result.add(each);
                }
            }
        }
        for (WorkflowIssue each : snapshot.getIssues()) {
            addMissingInputsFromIssue(result, each);
        }
        if (result.isEmpty() && null != clarifiedIntent && !clarifiedIntent.getPendingQuestions().isEmpty()) {
            result.add("user_clarification");
        }
        return result;
    }
    
    private static void addMissingInputsFromIssue(final Collection<String> result, final WorkflowIssue issue) {
        if (WorkflowIssueCode.DATABASE_REQUIRED.equals(issue.getCode()) && !result.contains("database")) {
            result.add("database");
        }
        Object missingProperties = issue.getDetails().get("missing_properties");
        if (missingProperties instanceof Collection) {
            for (Object each : (Collection<?>) missingProperties) {
                String missingInput = String.format("algorithm_properties.%s", each);
                if (!result.contains(missingInput)) {
                    result.add(missingInput);
                }
            }
        }
    }
    
    private static List<String> createResourcesToRead(final WorkflowContextSnapshot snapshot) {
        List<String> result = new LinkedList<>();
        addFeatureResources(result, snapshot);
        WorkflowRequest request = snapshot.getRequest();
        if (null == request) {
            result.add("shardingsphere://databases");
            return result;
        }
        if (!request.getDatabase().isEmpty()) {
            addRuleResources(result, snapshot, request);
            if (!request.getSchema().isEmpty() && !request.getTable().isEmpty()) {
                addTableResources(result, snapshot, request);
            }
        }
        return result;
    }
    
    private static void addFeatureResources(final Collection<String> result, final WorkflowContextSnapshot snapshot) {
        String workflowKind = resolveWorkflowKind(snapshot);
        if ("encrypt.rule".equals(workflowKind)) {
            result.add("shardingsphere://features/encrypt/algorithms");
        } else if ("mask.rule".equals(workflowKind)) {
            result.add("shardingsphere://features/mask/algorithms");
        }
    }
    
    private static void addRuleResources(final Collection<String> result, final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        String workflowKind = resolveWorkflowKind(snapshot);
        if ("encrypt.rule".equals(workflowKind)) {
            result.add(String.format("shardingsphere://features/encrypt/databases/%s/rules", request.getDatabase()));
        } else if ("mask.rule".equals(workflowKind)) {
            result.add(String.format("shardingsphere://features/mask/databases/%s/rules", request.getDatabase()));
        }
    }
    
    private static void addTableResources(final Collection<String> result, final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        result.add(String.format("shardingsphere://databases/%s/schemas/%s/tables/%s/columns", request.getDatabase(), request.getSchema(), request.getTable()));
        if ("encrypt.rule".equals(resolveWorkflowKind(snapshot))) {
            result.add(String.format("shardingsphere://databases/%s/schemas/%s/tables/%s/indexes", request.getDatabase(), request.getSchema(), request.getTable()));
        }
    }
    
    private static List<Map<String, Object>> createPlanningNextActions(final WorkflowContextSnapshot snapshot, final List<String> missingRequiredInputs) {
        if (WorkflowLifecycle.STATUS_CLARIFYING.equals(snapshot.getStatus())) {
            return List.of(createUserAction("Ask for the missing inputs, then call the same planning tool with the existing plan_id.", false, missingRequiredInputs));
        }
        if (WorkflowLifecycle.STATUS_PLANNED.equals(snapshot.getStatus())) {
            return List.of(createToolAction(APPLY_WORKFLOW, "Apply or export the reviewed workflow artifacts after user approval.",
                    Map.of("plan_id", snapshot.getPlanId(), "execution_mode", resolveExecutionMode(snapshot)), true));
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
    
    private static String createPlanningRecommendedNextTool(final WorkflowContextSnapshot snapshot) {
        if (WorkflowLifecycle.STATUS_CLARIFYING.equals(snapshot.getStatus()) || WorkflowLifecycle.STATUS_FAILED.equals(snapshot.getStatus())) {
            return resolvePlanningTool(snapshot);
        }
        return WorkflowLifecycle.STATUS_PLANNED.equals(snapshot.getStatus()) ? APPLY_WORKFLOW : "";
    }
    
    private static Map<String, Object> createToolAction(final String targetTool, final String reason, final Map<String, Object> requiredArguments, final boolean requiresUserApproval) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("action_kind", "call_tool");
        result.put("target_tool", targetTool);
        result.put("reason", reason);
        result.put("required_arguments", requiredArguments);
        result.put("requires_user_approval", requiresUserApproval);
        return result;
    }
    
    private static Map<String, Object> createUserAction(final String reason, final boolean requiresUserApproval, final List<String> requiredInputs) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("action_kind", "ask_user");
        result.put("reason", reason);
        result.put("required_inputs", requiredInputs);
        result.put("requires_user_approval", requiresUserApproval);
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
        return null == snapshot.getWorkflowKind() ? "" : snapshot.getWorkflowKind().getValue();
    }
    
    private static String resolveExecutionMode(final WorkflowContextSnapshot snapshot) {
        return null == snapshot.getRequest() ? "review-then-execute" : snapshot.getRequest().getExecutionMode();
    }
    
    private static String resolveTargetTool(final Map<String, Object> action) {
        return Objects.toString(action.get("target_tool"), "");
    }
}
