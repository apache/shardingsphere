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

import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowPlanPayloadBuilderTest {
    
    @Test
    void assertBuildIncludesIntentInference() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        snapshot.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType("create");
        clarifiedIntent.setFieldSemantics("phone");
        clarifiedIntent.setReasoningNotes("Resolved from explicit arguments, heuristic inference for requires_decrypt.");
        clarifiedIntent.getInferredValues().put("requires_decrypt", true);
        clarifiedIntent.getUnresolvedFields().add("requires_like_query");
        clarifiedIntent.getClarificationMessages().add("Do you need LIKE query?");
        snapshot.setClarifiedIntent(clarifiedIntent);
        snapshot.getIssues().add(new WorkflowIssue("code", "warning", "clarifying", "message", "action", true, Map.of("missing_properties", List.of("aes-key-value"))));
        snapshot.getAlgorithmCandidates().add(new AlgorithmCandidate("primary", "AES", true, true, false, 95, "reason", ""));
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "desc", ""));
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("review");
        interactionPlan.setDeliveryMode("interactive");
        interactionPlan.setExecutionMode("review-then-execute");
        interactionPlan.getSteps().add("review");
        interactionPlan.getValidationStrategy().put("layers", "rules");
        snapshot.setInteractionPlan(interactionPlan);
        Map<String, Object> actual = WorkflowPlanPayloadBuilder.build(snapshot);
        assertThat(actual.get("response_mode"), is("planning"));
        assertThat(actual.get("plan_id"), is("plan-1"));
        assertThat(actual.get("workflow_kind"), is("encrypt.rule"));
        assertThat(((Map<?, ?>) actual.get("intent_inference")).get("operation_type"), is("create"));
        assertThat(((Map<?, ?>) actual.get("intent_inference")).get("field_semantics"), is("phone"));
        assertTrue((Boolean) ((Map<?, ?>) ((Map<?, ?>) actual.get("intent_inference")).get("inferred_values")).get("requires_decrypt"));
        assertThat(((Map<?, ?>) actual.get("intent_inference")).get("unresolved_fields"), is(clarifiedIntent.getUnresolvedFields()));
        assertThat(actual.get("missing_required_inputs"), is(List.of("requires_like_query", "primary_algorithm_properties.aes-key-value")));
        Map<?, ?> actualReviewFocus = (Map<?, ?>) actual.get("review_focus");
        assertThat(actualReviewFocus.get("artifact_categories"), is(List.of("algorithm_properties")));
        assertThat(actualReviewFocus.get("side_effect_scope"), is(List.of()));
        assertFalse((Boolean) actualReviewFocus.get("manual_only"));
        assertFalse((Boolean) actualReviewFocus.get("requires_user_approval"));
        assertThat(actualReviewFocus.get("next_review_action"), is("answer_clarification_questions"));
        List<?> actualClarificationQuestions = (List<?>) actual.get("clarification_questions");
        assertThat(((Map<?, ?>) actualClarificationQuestions.get(0)).get("input_type"), is("boolean"));
        assertThat(((Map<?, ?>) actualClarificationQuestions.get(0)).get("allowed_values"), is(List.of(true, false)));
        assertThat(((Map<?, ?>) actualClarificationQuestions.get(0)).get("display_message"), is("Do you need LIKE query?"));
        assertThat(((Map<?, ?>) actualClarificationQuestions.get(1)).get("input_type"), is("secret"));
        assertTrue((Boolean) ((Map<?, ?>) actualClarificationQuestions.get(1)).get("secret"));
        assertFalse(actual.containsKey("recommended_next_tool"));
        assertFalse((Boolean) actual.get("requires_user_approval"));
        assertThat(((Map<?, ?>) actual.get("proxy_topology_hint")).get("expected_runtime_view"), is("proxy_logical_database"));
        assertTrue(extractResourceUris((List<?>) actual.get("resources_to_read")).contains("shardingsphere://features/encrypt/algorithms"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("next_actions")).get(0)).get("type"), is("ask_user"));
    }
    
    @Test
    void assertBuildUsesPublicAlgorithmPropertyInputs() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
        WorkflowRequest request = new WorkflowRequest();
        snapshot.setRequest(request);
        snapshot.setInteractionPlan(InteractionPlan.create("plan-1", request, "Encrypt workflow plan.", List.of("review"), List.of("rules")));
        snapshot.setClarifiedIntent(new ClarifiedIntent());
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "primary key", ""));
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("assisted_query", "salt", true, true, "assist key", ""));
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("like_query", "token", true, true, "like key", ""));
        snapshot.getIssues().add(new WorkflowIssue("code", "warning", "clarifying", "message", "action", true,
                Map.of("missing_properties", List.of("aes-key-value", "salt", "token"))));
        Map<String, Object> actual = WorkflowPlanPayloadBuilder.build(snapshot);
        assertThat(actual.get("missing_required_inputs"), is(List.of("primary_algorithm_properties.aes-key-value", "assisted_query_algorithm_properties.salt",
                "like_query_algorithm_properties.token")));
        List<?> actualClarificationQuestions = (List<?>) actual.get("clarification_questions");
        assertThat(((Map<?, ?>) actualClarificationQuestions.get(0)).get("field"), is("primary_algorithm_properties.aes-key-value"));
        assertThat(((Map<?, ?>) actualClarificationQuestions.get(1)).get("field"), is("assisted_query_algorithm_properties.salt"));
        assertThat(((Map<?, ?>) actualClarificationQuestions.get(2)).get("field"), is("like_query_algorithm_properties.token"));
        assertTrue((Boolean) ((Map<?, ?>) actualClarificationQuestions.get(0)).get("secret"));
    }
    
    @Test
    void assertBuildIncludesMaskResources() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("mask.rule"));
        snapshot.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        snapshot.setClarifiedIntent(new ClarifiedIntent());
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        snapshot.setRequest(request);
        snapshot.setInteractionPlan(InteractionPlan.create("plan-1", request, "Mask workflow plan.", List.of("Review"), List.of("rules")));
        Map<String, Object> actual = WorkflowPlanPayloadBuilder.build(snapshot);
        List<?> actualResourcesToRead = (List<?>) actual.get("resources_to_read");
        List<String> actualResourceUris = extractResourceUris(actualResourcesToRead);
        assertTrue(actualResourceUris.contains("shardingsphere://features/mask/algorithms"));
        assertTrue(actualResourceUris.contains("shardingsphere://databases/logic_db/schemas/public/tables/orders/columns"));
        assertFalse(actualResourceUris.contains("shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes"));
        assertFalse(actual.containsKey("recommended_next_tool"));
        assertFalse((Boolean) actual.get("requires_user_approval"));
        Map<?, ?> actualReviewFocus = (Map<?, ?>) actual.get("review_focus");
        assertTrue((Boolean) actualReviewFocus.get("requires_user_approval"));
        assertThat(actualReviewFocus.get("next_review_action"), is("call_database_gateway_apply_workflow_preview"));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.get("next_actions")).get(0);
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_apply_workflow"));
        assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("execution_mode"), is("preview"));
        assertFalse((Boolean) actualNextAction.get("requires_user_approval"));
    }
    
    @Test
    void assertBuildKeepsManualOnlyPlanFreeOfRuntimeApproval() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("mask.rule"));
        snapshot.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.getInferredValues().put("execution_mode", "manual-only");
        snapshot.setClarifiedIntent(clarifiedIntent);
        WorkflowRequest request = new WorkflowRequest();
        request.setExecutionMode("manual-only");
        snapshot.setRequest(request);
        snapshot.setInteractionPlan(InteractionPlan.create("plan-1", request, "Mask workflow plan.", List.of("Review"), List.of("rules")));
        Map<String, Object> actual = WorkflowPlanPayloadBuilder.build(snapshot);
        Map<?, ?> actualReviewFocus = (Map<?, ?>) actual.get("review_focus");
        assertTrue((Boolean) actualReviewFocus.get("manual_only"));
        assertFalse((Boolean) actualReviewFocus.get("requires_user_approval"));
        assertThat(((Map<?, ?>) actual.get("argument_provenance")).get("execution_mode"), is("inferred_from_intent"));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.get("next_actions")).get(0);
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_apply_workflow"));
        assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("execution_mode"), is("preview"));
        assertFalse((Boolean) actualNextAction.get("requires_user_approval"));
    }
    
    @Test
    void assertBuildRecommendsPlanningToolAfterFailure() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
        snapshot.setClarifiedIntent(new ClarifiedIntent());
        WorkflowRequest request = new WorkflowRequest();
        snapshot.setRequest(request);
        snapshot.setInteractionPlan(InteractionPlan.create("plan-1", request, "Encrypt workflow plan.", List.of("Review"), List.of("rules")));
        Map<String, Object> actual = WorkflowPlanPayloadBuilder.build(snapshot);
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.get("next_actions")).get(0);
        assertThat(actual.get("response_mode"), is("terminal"));
        assertFalse(actual.containsKey("recommended_next_tool"));
        assertThat(actualNextAction.get("type"), is("tool_call"));
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_plan_encrypt_rule"));
    }
    
    private List<String> extractResourceUris(final List<?> resources) {
        return resources.stream().map(each -> (String) ((Map<?, ?>) each).get("uri")).toList();
    }
}
