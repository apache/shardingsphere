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

package org.apache.shardingsphere.mcp.bootstrap.transport.capability.tool;

import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPToolClarificationPolicyTest extends AbstractMCPToolSpecificationFactoryTest {
    
    private final MCPToolClarificationPolicy policy = new MCPToolClarificationPolicy();
    
    @Test
    void assertRequiresPlanningClarification() {
        assertTrue(policy.requiresPlanningClarification(createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"), createClarifyingPayload()));
    }
    
    @Test
    void assertRequiresPlanningClarificationWithApplyTool() {
        assertFalse(policy.requiresPlanningClarification(createToolDescriptor("database_gateway_apply_workflow"), createClarifyingPayload()));
    }
    
    @Test
    void assertRequiresPlanningClarificationWithoutRuntimeDescriptor() {
        assertFalse(policy.requiresPlanningClarification(createToolDescriptor("fixture_ping"), createClarifyingPayload()));
    }
    
    @Test
    void assertRequiresPlanningClarificationWithoutQuestions() {
        assertFalse(policy.requiresPlanningClarification(createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"),
                Map.of(WorkflowFieldNames.PLAN_ID, "plan-1", "status", "planned")));
    }
    
    @Test
    void assertRequiresPlanningClarificationWithEmptyQuestions() {
        assertFalse(policy.requiresPlanningClarification(createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"), Map.of(
                WorkflowFieldNames.PLAN_ID, "plan-1",
                "status", "clarifying",
                MCPPayloadFieldNames.CLARIFICATION_QUESTIONS, List.of())));
    }
    
    @Test
    void assertCreateClarificationForm() {
        Optional<MCPToolClarificationPolicy.ClarificationForm> actual = policy.createClarificationForm(createClarifyingPayload(),
                createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
        assertTrue(actual.isPresent());
        assertThat(actual.get().planId(), is("plan-1"));
        assertThat(actual.get().requestedSchema(), is(createExpectedElicitRequestedSchema()));
    }
    
    @Test
    void assertCreateClarificationFormWithoutPlanId() {
        Optional<MCPToolClarificationPolicy.ClarificationForm> actual = policy.createClarificationForm(createClarifyingPayloadWithoutPlanId(),
                createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertCreateClarificationFormWithSensitiveQuestion() {
        Map<String, Object> payload = createClarifyingPayload(createClarifyingQuestion("custom_properties.access-token", "string", false, "Provide access token."));
        Optional<MCPToolClarificationPolicy.ClarificationForm> actual = policy.createClarificationForm(payload,
                createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertCreateClarificationFormWithAmbiguousFieldBinding() {
        Map<String, Object> payload = createClarifyingPayload(createClarifyingQuestion("requires_review", "boolean", false, "Require review?"));
        Optional<MCPToolClarificationPolicy.ClarificationForm> actual = policy.createClarificationForm(payload,
                createAmbiguousPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
        assertTrue(actual.isEmpty());
    }
}
