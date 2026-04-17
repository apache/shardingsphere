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

package org.apache.shardingsphere.mcp.tool.handler.workflow;

import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.service.workflow.AlgorithmPropertyTemplateService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WorkflowToolResponseBuilderTest {
    
    @Test
    void assertBuildPlanResponseUsesEmptyStructuresForMinimalSnapshot() {
        WorkflowToolResponseBuilder builder = new WorkflowToolResponseBuilder(new AlgorithmPropertyTemplateService());
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("clarifying");
        Map<String, Object> actual = builder.buildPlanResponse(snapshot);
        assertThat(actual.get("plan_id"), is("plan-1"));
        assertThat(((List<?>) actual.get("pending_questions")).size(), is(0));
        assertThat(actual.get("masked_property_preview"), is(Map.of()));
        assertThat(actual.get("validation_strategy"), is(Map.of()));
    }
    
    @Test
    void assertBuildPlanResponseMasksPropertiesAndDistSql() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("planned");
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("review");
        interactionPlan.setExecutionMode("review-then-execute");
        snapshot.setInteractionPlan(interactionPlan);
        WorkflowRequest request = new WorkflowRequest();
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
        snapshot.setRequest(request);
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "AES key", ""));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='123456'))"));
        WorkflowToolResponseBuilder builder = new WorkflowToolResponseBuilder(new AlgorithmPropertyTemplateService());
        Map<String, Object> actual = builder.buildPlanResponse(snapshot);
        Map<?, ?> actualPreview = (Map<?, ?>) ((Map<?, ?>) actual.get("masked_property_preview")).get("primary");
        Map<?, ?> actualArtifact = (Map<?, ?>) ((List<?>) actual.get("distsql_artifacts")).get(0);
        assertThat(actualPreview.get("aes-key-value"), is("******"));
        assertThat(actualArtifact.get("sql"), is("CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='******'))"));
    }
}
