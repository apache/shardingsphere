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
import org.apache.shardingsphere.mcp.support.workflow.model.DDLArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.IndexPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.SecretReferenceValue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
        assertThat(actualReviewFocus.get("next_review_action"), is("answer_clarification_questions"));
        List<?> actualClarificationQuestions = (List<?>) actual.get("clarification_questions");
        assertThat(((Map<?, ?>) actualClarificationQuestions.getFirst()).get("input_type"), is("boolean"));
        assertThat(((Map<?, ?>) actualClarificationQuestions.getFirst()).get("allowed_values"), is(List.of(true, false)));
        assertThat(((Map<?, ?>) actualClarificationQuestions.getFirst()).get("display_message"), is("Do you need LIKE query?"));
        assertThat(((Map<?, ?>) actualClarificationQuestions.get(1)).get("input_type"), is("secret"));
        assertTrue((Boolean) ((Map<?, ?>) actualClarificationQuestions.get(1)).get("secret"));
        assertThat(((Map<?, ?>) actual.get("proxy_topology_hint")).get("expected_runtime_view"), is("proxy_rule_distsql"));
        assertTrue(extractResourceUris((List<?>) actual.get("resources_to_read")).contains("shardingsphere://features/encrypt/algorithms"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("next_actions")).getFirst()).get("type"), is("ask_user"));
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
        assertThat(((Map<?, ?>) actualClarificationQuestions.getFirst()).get("field"), is("primary_algorithm_properties.aes-key-value"));
        assertThat(((Map<?, ?>) actualClarificationQuestions.get(1)).get("field"), is("assisted_query_algorithm_properties.salt"));
        assertThat(((Map<?, ?>) actualClarificationQuestions.get(2)).get("field"), is("like_query_algorithm_properties.token"));
        assertTrue((Boolean) ((Map<?, ?>) actualClarificationQuestions.getFirst()).get("secret"));
    }
    
    @Test
    void assertBuildIncludesSecretReferenceSummary() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        snapshot.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        snapshot.setClarifiedIntent(new ClarifiedIntent());
        WorkflowRequest request = new WorkflowRequest();
        request.getPrimaryAlgorithmSecretReferences().put("aes-key-value", SecretReferenceValue.create());
        snapshot.setRequest(request);
        snapshot.setInteractionPlan(InteractionPlan.create("plan-1", request, "Encrypt workflow plan.", List.of("review"), List.of("rules")));
        Map<String, Object> actual = WorkflowPlanPayloadBuilder.build(snapshot);
        Map<?, ?> actualSummary = (Map<?, ?>) actual.get("secret_reference_summary");
        assertTrue((Boolean) actualSummary.get("required"));
        assertThat(actualSummary.get("reference_count"), is(1));
        assertThat(actualSummary.get("value_handling"), is("manual_execution"));
        assertThat(((Map<?, ?>) ((List<?>) actualSummary.get("references")).getFirst()).get("label"), is("secret_placeholder:primary.aes-key-value"));
        assertThat(((Map<?, ?>) ((List<?>) actualSummary.get("references")).getFirst()).get("manual_placeholder"), is("<SECRET_VALUE_PRIMARY_AES_KEY_VALUE>"));
        assertFalse(String.valueOf(actualSummary).contains("placeholder://secret-value-1"));
        assertFalse(String.valueOf(actualSummary).contains("user label"));
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
        assertTrue(actualResourceUris.contains("shardingsphere://features/mask/databases/logic_db/rules"));
        assertTrue(actualResourceUris.contains("shardingsphere://features/mask/databases/logic_db/tables/orders/rules"));
        assertFalse(actualResourceUris.contains("shardingsphere://databases/logic_db/schemas/public/tables/orders/columns"));
        assertFalse(actualResourceUris.contains("shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes"));
        Map<?, ?> actualReviewFocus = (Map<?, ?>) actual.get("review_focus");
        assertThat(actualReviewFocus.get("next_review_action"), is("call_database_gateway_apply_workflow_preview"));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.get("next_actions")).getFirst();
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_apply_workflow"));
        assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("execution_mode"), is("preview"));
    }
    
    @Test
    void assertBuildIncludesBroadcastResources() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("broadcast.rule"));
        snapshot.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        snapshot.setClarifiedIntent(new ClarifiedIntent());
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        snapshot.setRequest(request);
        snapshot.setInteractionPlan(InteractionPlan.create("plan-1", request, "Broadcast workflow plan.", List.of("Review"), List.of("rules")));
        Map<String, Object> actual = WorkflowPlanPayloadBuilder.build(snapshot);
        List<String> actualResourceUris = extractResourceUris((List<?>) actual.get("resources_to_read"));
        assertTrue(actualResourceUris.contains("shardingsphere://features/broadcast/databases/logic_db/rules"));
        assertFalse(actualResourceUris.contains("shardingsphere://databases/logic_db/schemas/public/tables/orders/columns"));
        assertThat(((Map<?, ?>) actual.get("proxy_topology_hint")).get("expected_runtime_view"), is("proxy_rule_distsql"));
    }
    
    @Test
    void assertBuildIncludesReadwriteResources() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("readwrite.rule"));
        snapshot.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        snapshot.setClarifiedIntent(new ClarifiedIntent());
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        snapshot.setRequest(request);
        snapshot.setInteractionPlan(InteractionPlan.create("plan-1", request, "Readwrite workflow plan.", List.of("Review"), List.of("rules")));
        Map<String, Object> actual = WorkflowPlanPayloadBuilder.build(snapshot);
        List<String> actualResourceUris = extractResourceUris((List<?>) actual.get("resources_to_read"));
        assertTrue(actualResourceUris.contains("shardingsphere://features/readwrite-splitting/load-balance-algorithm-plugins"));
        assertTrue(actualResourceUris.contains("shardingsphere://features/readwrite-splitting/databases/logic_db/rules"));
        assertTrue(actualResourceUris.contains("shardingsphere://databases/logic_db/storage-units"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("next_actions")).getFirst()).get("tool_name"), is("database_gateway_apply_workflow"));
    }
    
    @Test
    void assertBuildIncludesReadwriteStatusResources() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("readwrite.status"));
        snapshot.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        snapshot.setClarifiedIntent(new ClarifiedIntent());
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        snapshot.setRequest(request);
        snapshot.setInteractionPlan(InteractionPlan.create("plan-1", request, "Readwrite status workflow plan.", List.of("Review"), List.of("rules")));
        Map<String, Object> actual = WorkflowPlanPayloadBuilder.build(snapshot);
        List<String> actualResourceUris = extractResourceUris((List<?>) actual.get("resources_to_read"));
        assertTrue(actualResourceUris.contains("shardingsphere://features/readwrite-splitting/databases/logic_db/status"));
        assertTrue(actualResourceUris.contains("shardingsphere://databases/logic_db/storage-units"));
    }
    
    @Test
    void assertBuildIncludesShadowResources() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("shadow.rule"));
        snapshot.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        snapshot.setClarifiedIntent(new ClarifiedIntent());
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setTable("t_order");
        snapshot.setRequest(request);
        snapshot.setInteractionPlan(InteractionPlan.create("plan-1", request, "Shadow workflow plan.", List.of("Review"), List.of("rules")));
        Map<String, Object> actual = WorkflowPlanPayloadBuilder.build(snapshot);
        List<String> actualResourceUris = extractResourceUris((List<?>) actual.get("resources_to_read"));
        assertTrue(actualResourceUris.contains("shardingsphere://features/shadow/algorithm-plugins"));
        assertTrue(actualResourceUris.contains("shardingsphere://features/shadow/databases/logic_db/rules"));
        assertTrue(actualResourceUris.contains("shardingsphere://features/shadow/databases/logic_db/tables/t_order/rules"));
        assertTrue(actualResourceUris.contains("shardingsphere://databases/logic_db/storage-units"));
        assertTrue(actualResourceUris.contains("shardingsphere://databases/logic_db/single-tables"));
        assertTrue(actualResourceUris.contains("shardingsphere://databases/logic_db/single-tables/t_order"));
    }
    
    @Test
    void assertBuildIncludesShardingTableRuleResources() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("sharding.table.rule"));
        snapshot.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        snapshot.setClarifiedIntent(new ClarifiedIntent());
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        snapshot.setRequest(request);
        snapshot.setInteractionPlan(InteractionPlan.create("plan-1", request, "Sharding workflow plan.", List.of("Review"), List.of("rules")));
        Map<String, Object> actual = WorkflowPlanPayloadBuilder.build(snapshot);
        List<String> actualResourceUris = extractResourceUris((List<?>) actual.get("resources_to_read"));
        assertTrue(actualResourceUris.contains("shardingsphere://features/sharding/algorithm-plugins"));
        assertTrue(actualResourceUris.contains("shardingsphere://features/sharding/databases/logic_db/table-rules"));
        assertTrue(actualResourceUris.contains("shardingsphere://features/sharding/databases/logic_db/table-nodes"));
        assertTrue(actualResourceUris.contains("shardingsphere://databases/logic_db/storage-units"));
        assertTrue(actualResourceUris.contains("shardingsphere://databases/logic_db/single-tables"));
        assertTrue(actualResourceUris.contains("shardingsphere://databases/logic_db/single-tables/orders"));
        assertTrue(actualResourceUris.contains("shardingsphere://features/sharding/databases/logic_db/tables/orders/table-rule"));
        assertTrue(actualResourceUris.contains("shardingsphere://features/sharding/databases/logic_db/tables/orders/nodes"));
        assertFalse(actualResourceUris.contains("shardingsphere://databases/logic_db/schemas/public/tables/orders/columns"));
        assertThat(((Map<?, ?>) actual.get("proxy_topology_hint")).get("expected_runtime_view"), is("proxy_rule_distsql"));
    }
    
    @Test
    void assertBuildRuleDistSQLOnly() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("sharding.table.rule"));
        snapshot.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        snapshot.setClarifiedIntent(new ClarifiedIntent());
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setTable("orders");
        snapshot.setRequest(request);
        snapshot.setInteractionPlan(InteractionPlan.create("plan-1", request, "Sharding workflow plan.", List.of("Review"), List.of("rules")));
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id BIGINT", 1));
        snapshot.getIndexPlans().add(new IndexPlan("idx_order_id", "order_id", "lookup", "CREATE INDEX idx_order_id ON orders(order_id)"));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE SHARDING TABLE RULE orders(DATANODES('ds.orders'))"));
        Map<String, Object> actual = WorkflowPlanPayloadBuilder.buildRuleDistSQLOnly(snapshot, request);
        assertThat(actual.get("workflow_kind"), is("sharding.table.rule"));
        assertFalse(actual.containsKey(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DDL_ARTIFACTS));
        assertFalse(actual.containsKey(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_INDEX_PLAN));
        assertThat(((List<?>) actual.get(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DISTSQL_ARTIFACTS)).size(), is(1));
        assertThat(((Map<?, ?>) ((List<?>) actual.get(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DISTSQL_ARTIFACTS)).getFirst()).get("sql"),
                is("CREATE SHARDING TABLE RULE orders(DATANODES('ds.orders'))"));
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
        assertThat(((Map<?, ?>) actual.get("argument_provenance")).get("execution_mode"), is("inferred_from_intent"));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.get("next_actions")).getFirst();
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_apply_workflow"));
        assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("execution_mode"), is("preview"));
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
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.get("next_actions")).getFirst();
        assertThat(actual.get("response_mode"), is("terminal"));
        assertThat(actualNextAction.get("type"), is("tool_call"));
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_plan_encrypt_rule"));
    }
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "table rule,sharding.table.rule,database_gateway_plan_sharding_table_rule",
            "table reference,sharding.table.reference,database_gateway_plan_sharding_table_reference_rule",
            "default strategy,sharding.default.strategy,database_gateway_plan_sharding_default_strategy",
            "key generator,sharding.key.generator,database_gateway_plan_sharding_key_generator",
            "key generate strategy,sharding.key.generate.strategy,database_gateway_plan_sharding_key_generate_strategy",
            "component cleanup,sharding.component.cleanup,database_gateway_plan_sharding_rule_component_cleanup"
    })
    void assertBuildRecommendsShardingPlanningToolAfterFailure(final String displayName, final String workflowKind, final String expectedToolName) {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setWorkflowKind(WorkflowKind.valueOf(workflowKind));
        snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
        snapshot.setClarifiedIntent(new ClarifiedIntent());
        WorkflowRequest request = new WorkflowRequest();
        snapshot.setRequest(request);
        snapshot.setInteractionPlan(InteractionPlan.create("plan-1", request, "Sharding workflow plan.", List.of("Review"), List.of("rules")));
        Map<String, Object> actual = WorkflowPlanPayloadBuilder.build(snapshot);
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.get("next_actions")).getFirst();
        assertThat(actualNextAction.get("tool_name"), is(expectedToolName));
    }
    
    private List<String> extractResourceUris(final List<?> resources) {
        return resources.stream().map(each -> (String) ((Map<?, ?>) each).get("uri")).toList();
    }
}
