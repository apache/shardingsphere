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
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Workflow plan payload builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowPlanPayloadBuilder {

    private static final String DELIVERY_MODE_ALL_AT_ONCE = "all-at-once";

    private static final String EXECUTION_MODE_REVIEW_THEN_EXECUTE = "review-then-execute";

    private static final String EXECUTION_MODE_MANUAL_ONLY = "manual-only";

    /**
     * Build one workflow-plan payload map.
     *
     * @param snapshot workflow snapshot
     * @return workflow-plan payload
     */
    public static Map<String, Object> build(final WorkflowContextSnapshot snapshot) {
        Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        result.put("response_mode", resolveResponseMode(snapshot));
        result.put("plan_id", snapshot.getPlanId());
        result.put("workflow_kind", snapshot.getWorkflowKind().getValue());
        result.put("status", snapshot.getStatus());
        result.put("issues", snapshot.getIssues().stream().map(WorkflowIssue::toMap).toList());
        result.put("global_steps", snapshot.getInteractionPlan().getSteps());
        result.put("current_step", snapshot.getInteractionPlan().getCurrentStep());
        result.put("algorithm_recommendations", snapshot.getAlgorithmCandidates().stream().map(AlgorithmCandidate::toMap).toList());
        result.put("property_requirements", snapshot.getPropertyRequirements().stream().map(AlgorithmPropertyRequirement::toMap).toList());
        result.put("validation_strategy", snapshot.getInteractionPlan().getValidationStrategy());
        result.put("delivery_mode", snapshot.getInteractionPlan().getDeliveryMode());
        result.put("execution_mode", snapshot.getInteractionPlan().getExecutionMode());
        result.put("intent_inference", createIntentInference(snapshot.getClarifiedIntent()));
        result.put("argument_provenance", createArgumentProvenance(snapshot));
        result.put("review_focus", createReviewFocus(snapshot));
        WorkflowGuidancePayloadBuilder.appendPlanningGuidance(result, snapshot);
        return result;
    }

    private static String resolveResponseMode(final WorkflowContextSnapshot snapshot) {
        return WorkflowLifecycle.STATUS_FAILED.equals(snapshot.getStatus()) ? "terminal" : "planning";
    }

    private static Map<String, Object> createIntentInference(final ClarifiedIntent clarifiedIntent) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("operation_type", clarifiedIntent.getOperationType());
        result.put("field_semantics", clarifiedIntent.getFieldSemantics());
        result.put("inferred_values", clarifiedIntent.getInferredValues());
        result.put("unresolved_fields", clarifiedIntent.getUnresolvedFields());
        result.put("reasoning_notes", clarifiedIntent.getReasoningNotes());
        return result;
    }

    private static Map<String, Object> createReviewFocus(final WorkflowContextSnapshot snapshot) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        boolean manualOnly = EXECUTION_MODE_MANUAL_ONLY.equals(snapshot.getInteractionPlan().getExecutionMode());
        result.put("artifact_categories", createReviewArtifactCategories(snapshot));
        result.put("side_effect_scope", createReviewSideEffectScope(snapshot));
        result.put("manual_only", manualOnly);
        result.put("requires_user_approval", WorkflowLifecycle.STATUS_PLANNED.equals(snapshot.getStatus()) && !manualOnly);
        result.put("next_review_action", createNextReviewAction(snapshot));
        return result;
    }

    private static Map<String, Object> createArgumentProvenance(final WorkflowContextSnapshot snapshot) {
        WorkflowRequest request = snapshot.getRequest();
        if (null == request) {
            return Map.of("plan_id", "server_generated");
        }
        Map<String, Object> result = new LinkedHashMap<>(10, 1F);
        result.put("plan_id", "server_generated");
        putUserProvided(result, "database", request.getDatabase());
        Map<String, Object> inferredValues = getInferredValues(snapshot);
        putArgumentProvenance(result, "schema", request.getSchema(), inferredValues.containsKey("schema") ? "inferred_from_intent" : "user_provided");
        putUserProvided(result, "table", request.getTable());
        putUserProvided(result, "column", request.getColumn());
        putArgumentProvenance(result, "operation_type", request.getOperationType(), inferredValues.containsKey("operation_type") ? "inferred_from_intent" : "user_provided");
        putUserProvided(result, "natural_language_intent", request.getNaturalLanguageIntent());
        putArgumentProvenance(result, "field_semantics", request.getFieldSemantics(), inferredValues.containsKey("field_semantics") ? "inferred_from_intent" : "user_provided");
        result.put("delivery_mode", resolveModeProvenance("delivery_mode", request.getDeliveryMode(), inferredValues));
        result.put("execution_mode", resolveModeProvenance("execution_mode", request.getExecutionMode(), inferredValues));
        putUserProvided(result, "algorithm_type", request.getAlgorithmType());
        return result;
    }

    private static String resolveModeProvenance(final String fieldName, final String value, final Map<String, Object> inferredValues) {
        if (inferredValues.containsKey(fieldName)) {
            return "inferred_from_intent";
        }
        if (value.isEmpty() || isDefaultMode(fieldName, value)) {
            return "server_defaulted";
        }
        return "user_provided";
    }

    private static boolean isDefaultMode(final String fieldName, final String value) {
        return "delivery_mode".equals(fieldName) ? DELIVERY_MODE_ALL_AT_ONCE.equals(value) : EXECUTION_MODE_REVIEW_THEN_EXECUTE.equals(value);
    }

    private static Map<String, Object> getInferredValues(final WorkflowContextSnapshot snapshot) {
        return null == snapshot.getClarifiedIntent() ? Map.of() : snapshot.getClarifiedIntent().getInferredValues();
    }

    private static void putUserProvided(final Map<String, Object> target, final String key, final String value) {
        putArgumentProvenance(target, key, value, "user_provided");
    }

    private static void putArgumentProvenance(final Map<String, Object> target, final String key, final String value, final String provenance) {
        if (!value.isEmpty()) {
            target.put(key, provenance);
        }
    }

    private static List<String> createReviewArtifactCategories(final WorkflowContextSnapshot snapshot) {
        List<String> result = new LinkedList<>();
        if (!snapshot.getDdlArtifacts().isEmpty()) {
            result.add("ddl_artifacts");
        }
        if (!snapshot.getIndexPlans().isEmpty()) {
            result.add("index_plan");
        }
        if (!snapshot.getRuleArtifacts().isEmpty()) {
            result.add("distsql_artifacts");
        }
        if (!snapshot.getPropertyRequirements().isEmpty()) {
            result.add("algorithm_properties");
        }
        return result;
    }

    private static List<String> createReviewSideEffectScope(final WorkflowContextSnapshot snapshot) {
        List<String> result = new LinkedList<>();
        if (!snapshot.getDdlArtifacts().isEmpty() || !snapshot.getIndexPlans().isEmpty()) {
            result.add("physical-structure");
        }
        if (!snapshot.getRuleArtifacts().isEmpty()) {
            result.add("rule-metadata");
        }
        return result;
    }

    private static String createNextReviewAction(final WorkflowContextSnapshot snapshot) {
        if (WorkflowLifecycle.STATUS_CLARIFYING.equals(snapshot.getStatus())) {
            return "answer_clarification_questions";
        }
        return WorkflowLifecycle.STATUS_PLANNED.equals(snapshot.getStatus()) ? "call_apply_workflow_preview" : "inspect_issues";
    }
}
