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

package org.apache.shardingsphere.mcp.tool.model.workflow;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WorkflowContextSnapshotTest {
    
    @Test
    void assertToPlanPayload() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("planned");
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.getPendingQuestions().add("question-1");
        snapshot.setClarifiedIntent(clarifiedIntent);
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DATABASE_REQUIRED, "error", "intaking", "missing", "fix", true, Map.of()));
        snapshot.getAlgorithmCandidates().add(new AlgorithmCandidate("primary", "AES", "builtin", true, true, false, 100, "reason", ""));
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "key", ""));
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("review");
        interactionPlan.setDeliveryMode("all-at-once");
        interactionPlan.setExecutionMode("review-then-execute");
        interactionPlan.getSteps().add("inspect");
        interactionPlan.getValidationStrategy().put("layers", List.of("ddl"));
        snapshot.setInteractionPlan(interactionPlan);
        Map<String, Object> actual = snapshot.toPlanPayload();
        assertThat(actual.get("plan_id"), is("plan-1"));
        assertThat(actual.get("status"), is("planned"));
        assertThat(actual.get("pending_questions"), is(List.of("question-1")));
        assertThat(((List<?>) actual.get("issues")).size(), is(1));
        assertThat(actual.get("global_steps"), is(List.of("inspect")));
        assertThat(actual.get("current_step"), is("review"));
        assertThat(((List<?>) actual.get("algorithm_recommendations")).size(), is(1));
        assertThat(((List<?>) actual.get("property_requirements")).size(), is(1));
        assertThat(actual.get("validation_strategy"), is(Map.of("layers", List.of("ddl"))));
        assertThat(actual.get("delivery_mode"), is("all-at-once"));
        assertThat(actual.get("execution_mode"), is("review-then-execute"));
    }
    
    @Test
    void assertToPlanPayloadWithoutInteractionPlan() {
        Map<String, Object> actual = new WorkflowContextSnapshot().toPlanPayload();
        assertThat(actual.get("pending_questions"), is(List.of()));
        assertThat(actual.get("issues"), is(List.of()));
        assertThat(actual.get("global_steps"), is(List.of()));
        assertThat(actual.get("current_step"), is(""));
        assertThat(actual.get("algorithm_recommendations"), is(List.of()));
        assertThat(actual.get("property_requirements"), is(List.of()));
        assertThat(actual.get("validation_strategy"), is(Map.of()));
        assertThat(actual.get("delivery_mode"), is(""));
        assertThat(actual.get("execution_mode"), is(""));
    }
}
