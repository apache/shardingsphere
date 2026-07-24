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
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LLMMCPConversationInstructionFactoryTest {
    
    @Test
    void assertCreateExpectedQueryInstruction() {
        LLMStructuredAnswer expectedAnswer = new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) FROM orders", 42, List.of());
        String actual = new LLMMCPConversationInstructionFactory().createExpectedQueryInstruction(expectedAnswer);
        assertThat(actual, is(
                "Required MCP tool coverage is present, but the latest successful database_gateway_execute_query did not use database `logic_db`, "
                        + "schema `public`, and query `SELECT COUNT(*) FROM orders`. "
                        + "Call database_gateway_execute_query now with exactly those arguments before returning the final JSON."));
    }
    
    @Test
    void assertCreateTraceDrivenInstructionWithImmediateToolAction() {
        Map<String, Object> arguments = new LinkedHashMap<>(2, 1F);
        arguments.put("plan_id", "plan-1");
        arguments.put("execution_mode", "preview");
        Map<String, Object> nextAction = Map.of("type", "tool_call", "tool_name", "database_gateway_apply_workflow", "arguments", arguments);
        String actual = new LLMMCPConversationInstructionFactory().createTraceDrivenInstruction(createScenario(), List.of(createTraceRecord("database_gateway_plan_mask_rule",
                Map.of("next_actions", List.of(nextAction)))));
        assertThat(actual, is("The latest MCP response gave an immediate next_action. Call `database_gateway_apply_workflow` now with exactly these arguments: "
                + "{\"plan_id\":\"plan-1\",\"execution_mode\":\"preview\"}. Do not replace values with placeholders."));
    }
    
    @Test
    void assertCreateFinalAnswerInstruction() {
        Map<String, Object> structuredContent = Map.of("row_objects", List.of(Map.of("total_orders", 42)));
        String actual = new LLMMCPConversationInstructionFactory().createFinalAnswerInstruction(createScenario(),
                List.of(createTraceRecord("database_gateway_execute_query", structuredContent)));
        assertThat(actual, is("Return JSON only with keys database, schema, table, query, totalOrders, interactionSequence. "
                + "Use database `logic_db`, schema `public`, table `orders`, and query `SELECT COUNT(*) FROM orders`; set totalOrders to `42` "
                + "from the latest successful database_gateway_execute_query result. "
                + "Set interactionSequence exactly to this JSON array: [\"database_gateway_execute_query\"]. "
                + "Do not add inferred, expected, available, or failed MCP action names. Required tools are `database_gateway_execute_query`."));
    }
    
    private MCPInteractionTraceRecord createTraceRecord(final String targetName, final Map<String, Object> structuredContent) {
        return new MCPInteractionTraceRecord(1, "tool_call", MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN, targetName, Map.of(), structuredContent, true, 0L);
    }
    
    private LLME2EScenario createScenario() {
        LLMStructuredAnswer expectedAnswer = new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) FROM orders", 42, List.of());
        return new LLME2EScenario("scenario", "system", "user", expectedAnswer, List.of("database_gateway_execute_query"), List.of("database_gateway_execute_query"));
    }
}
