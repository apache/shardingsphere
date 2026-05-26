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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
        assertFalse(policy.requiresPlanningClarification(createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"), Map.of("plan_id", "plan-1", "status", "planned")));
    }

    @Test
    void assertRequiresPlanningClarificationWithEmptyQuestions() {
        assertFalse(policy.requiresPlanningClarification(createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"), Map.of(
                "plan_id", "plan-1",
                "status", "clarifying",
                "clarification_questions", List.of())));
    }
}
