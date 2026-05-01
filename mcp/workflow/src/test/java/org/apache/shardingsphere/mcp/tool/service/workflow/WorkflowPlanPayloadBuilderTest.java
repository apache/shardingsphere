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

import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowPlanPayloadBuilderTest {
    
    @Test
    void assertBuildIncludesIntentInference() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("clarifying");
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType("create");
        clarifiedIntent.setFieldSemantics("phone");
        clarifiedIntent.setReasoningNotes("Resolved from explicit arguments, heuristic inference for requires_decrypt.");
        clarifiedIntent.getInferredValues().put("requires_decrypt", true);
        clarifiedIntent.getUnresolvedFields().add("requires_like_query");
        clarifiedIntent.getPendingQuestions().add("是否需要 LIKE 查询？");
        snapshot.setClarifiedIntent(clarifiedIntent);
        snapshot.getIssues().add(new WorkflowIssue("code", "warning", "clarifying", "message", "action", true, Map.of()));
        snapshot.getAlgorithmCandidates().add(new AlgorithmCandidate("primary", "AES", "heuristic", true, true, false, 95, "reason", ""));
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "desc", ""));
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("review");
        interactionPlan.setDeliveryMode("interactive");
        interactionPlan.setExecutionMode("review-then-execute");
        interactionPlan.getSteps().add("review");
        interactionPlan.getValidationStrategy().put("layers", "rules");
        snapshot.setInteractionPlan(interactionPlan);
        Map<String, Object> actual = WorkflowPlanPayloadBuilder.build(snapshot);
        assertThat(actual.get("plan_id"), is("plan-1"));
        assertThat(actual.get("pending_questions"), is(clarifiedIntent.getPendingQuestions()));
        assertThat(((Map<?, ?>) actual.get("intent_inference")).get("operation_type"), is("create"));
        assertThat(((Map<?, ?>) actual.get("intent_inference")).get("field_semantics"), is("phone"));
        assertTrue((Boolean) ((Map<?, ?>) ((Map<?, ?>) actual.get("intent_inference")).get("inferred_values")).get("requires_decrypt"));
        assertThat(((Map<?, ?>) actual.get("intent_inference")).get("unresolved_fields"), is(clarifiedIntent.getUnresolvedFields()));
    }
}
