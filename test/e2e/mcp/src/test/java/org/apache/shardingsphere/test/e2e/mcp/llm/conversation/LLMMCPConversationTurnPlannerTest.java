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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation;

import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LLMMCPConversationTurnPlannerTest {
    
    @Test
    void assertCreateTurnToolNamesWithImmediateResourceAction() {
        LLMMCPConversationInstructionFactory instructionFactory = new LLMMCPConversationInstructionFactory();
        LLMMCPConversationTurnPlanner planner = new LLMMCPConversationTurnPlanner(instructionFactory);
        List<String> actual = planner.createTurnToolNames(createScenario(List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query"),
                List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query")),
                List.of(createTraceRecord("database_gateway_plan_mask_rule",
                        Map.of("next_actions", List.of(Map.of("type", "resource_read", "resource_uri", "shardingsphere://databases"))))));
        assertThat(actual, is(List.of(MCPInteractionActionNames.READ_RESOURCE)));
    }
    
    @Test
    void assertCreateTurnToolNamesWithImmediateCompletionAction() {
        LLMMCPConversationInstructionFactory instructionFactory = new LLMMCPConversationInstructionFactory();
        LLMMCPConversationTurnPlanner planner = new LLMMCPConversationTurnPlanner(instructionFactory);
        List<String> actual = planner.createTurnToolNames(createScenario(List.of("database_gateway_plan_mask_rule", "database_gateway_execute_query"),
                List.of("database_gateway_plan_mask_rule", "database_gateway_execute_query")),
                List.of(createTraceRecord("database_gateway_plan_mask_rule",
                        Map.of("recovery", Map.of("next_actions", List.of(Map.of("type", "completion")))))));
        assertThat(actual, is(List.of(MCPInteractionActionNames.COMPLETE)));
    }
    
    @Test
    void assertCreateTurnToolNamesPrefersReadOnlyAfterSideEffectNextAction() {
        LLMMCPConversationInstructionFactory instructionFactory = new LLMMCPConversationInstructionFactory();
        LLMMCPConversationTurnPlanner planner = new LLMMCPConversationTurnPlanner(instructionFactory);
        Map<String, Object> nextAction = Map.of("type", "tool_call", "tool_name", "database_gateway_execute_update", "arguments", Map.of("execution_mode", "execute"));
        List<String> actual = planner.createTurnToolNames(createScenario(List.of("database_gateway_execute_update", "database_gateway_execute_query"),
                List.of("database_gateway_execute_update", "database_gateway_execute_query")),
                List.of(createTraceRecord("database_gateway_execute_update", Map.of("next_actions", List.of(nextAction)))));
        assertThat(actual, is(List.of("database_gateway_execute_query")));
    }
    
    @Test
    void assertCreateToolChoiceWithMissingCoverage() {
        LLMMCPConversationInstructionFactory instructionFactory = new LLMMCPConversationInstructionFactory();
        LLMMCPConversationTurnPlanner planner = new LLMMCPConversationTurnPlanner(instructionFactory);
        assertThat(planner.createToolChoice(createScenario(List.of("database_gateway_execute_query"), List.of("database_gateway_execute_query")), List.of(), false), is("required"));
    }
    
    @Test
    void assertCreateToolChoiceWithCoveredRequiredTools() {
        LLMMCPConversationInstructionFactory instructionFactory = new LLMMCPConversationInstructionFactory();
        LLMMCPConversationTurnPlanner planner = new LLMMCPConversationTurnPlanner(instructionFactory);
        assertThat(planner.createToolChoice(createScenario(List.of("database_gateway_execute_query"), List.of("database_gateway_execute_query")),
                List.of(createTraceRecord("database_gateway_execute_query", Map.of("row_objects", List.of()))), false), is("auto"));
    }
    
    @Test
    void assertCreateToolChoiceWithFinalAnswer() {
        LLMMCPConversationInstructionFactory instructionFactory = new LLMMCPConversationInstructionFactory();
        LLMMCPConversationTurnPlanner planner = new LLMMCPConversationTurnPlanner(instructionFactory);
        assertThat(planner.createToolChoice(createScenario(List.of("database_gateway_execute_query"), List.of("database_gateway_execute_query")), List.of(), true), is("none"));
    }
    
    private MCPInteractionTraceRecord createTraceRecord(final String targetName, final Map<String, Object> structuredContent) {
        return new MCPInteractionTraceRecord(1, "tool_call", MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN, targetName, Map.of(), structuredContent, true, 0L);
    }
    
    private LLME2EScenario createScenario(final List<String> allowedToolNames, final List<String> requiredToolNames) {
        return new LLME2EScenario("scenario", "system", "user", new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) FROM orders", 1, List.of()),
                allowedToolNames, requiredToolNames);
    }
}
