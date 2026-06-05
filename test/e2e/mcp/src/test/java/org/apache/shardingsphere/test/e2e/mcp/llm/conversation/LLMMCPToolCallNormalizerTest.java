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
import static org.junit.jupiter.api.Assertions.assertFalse;

class LLMMCPToolCallNormalizerTest {
    
    @Test
    void assertNormalizeWithReadOnlySqlRoutedToOfferedQueryTool() {
        LLMMCPToolCallNormalizer.NormalizedToolCall actual = LLMMCPToolCallNormalizer.normalize(createScenario(""), "database_gateway_execute_update",
                Map.of("sql", "SELECT COUNT(*) FROM orders", "execution_mode", "preview"), List.of("database_gateway_execute_query"), List.of());
        assertThat(actual.name(), is("database_gateway_execute_query"));
        assertFalse(actual.arguments().containsKey("execution_mode"));
    }
    
    @Test
    void assertNormalizeWithResourceUriArgument() {
        LLMMCPToolCallNormalizer.NormalizedToolCall actual = LLMMCPToolCallNormalizer.normalize(createScenario("Read the orders table."), MCPInteractionActionNames.READ_RESOURCE,
                Map.of("uri", "orders"), List.of(MCPInteractionActionNames.READ_RESOURCE), List.of());
        assertThat(actual.arguments().get("uri"), is("shardingsphere://databases/logic_db/schemas/public/tables/orders"));
    }
    
    @Test
    void assertNormalizeWithSearchMetadataScopeArgument() {
        LLMMCPToolCallNormalizer.NormalizedToolCall actual = LLMMCPToolCallNormalizer.normalize(
                createScenario("Use logical database `logic_db` and schema `public` when the MCP action needs explicit runtime scope."),
                "database_gateway_search_metadata", Map.of("query", "orders"), List.of("database_gateway_search_metadata"), List.of());
        assertThat(actual.arguments().get("database"), is("logic_db"));
        assertThat(actual.arguments().get("schema"), is("public"));
    }
    
    @Test
    void assertNormalizeWithExpectedQuerySchemaArgument() {
        LLMMCPToolCallNormalizer.NormalizedToolCall actual = LLMMCPToolCallNormalizer.normalize(createScenario(""), "database_gateway_execute_query",
                Map.of("database", "logic_db", "sql", "SELECT COUNT(*) FROM public.orders"), List.of("database_gateway_execute_query"), List.of());
        assertThat(actual.arguments().get("schema"), is("public"));
    }
    
    @Test
    void assertNormalizeWithInitialPlanningPlanIdArgument() {
        LLMMCPToolCallNormalizer.NormalizedToolCall actual = LLMMCPToolCallNormalizer.normalize(createScenario(""), "database_gateway_plan_mask_rule",
                Map.of("plan_id", "plan_id", "table", "orders"), List.of("database_gateway_plan_mask_rule"), List.of());
        assertFalse(actual.arguments().containsKey("plan_id"));
    }
    
    @Test
    void assertNormalizeWithWorkflowPlanIdArgument() {
        LLMMCPToolCallNormalizer.NormalizedToolCall actual = LLMMCPToolCallNormalizer.normalize(createScenario(""), "database_gateway_apply_workflow",
                Map.of("plan_id", "<plan_id>"), List.of("database_gateway_apply_workflow"), List.of(createPlanTrace()));
        assertThat(actual.arguments().get("plan_id"), is("plan-1"));
    }
    
    @Test
    void assertNormalizeWithCompletionArguments() {
        LLMMCPToolCallNormalizer.NormalizedToolCall actual = LLMMCPToolCallNormalizer.normalize(createScenario(""), MCPInteractionActionNames.COMPLETE,
                Map.of("argument_name", "schema"), List.of(MCPInteractionActionNames.COMPLETE), List.of(createPromptTrace()));
        assertThat(actual.arguments().get("reference"), is(Map.of("type", "ref/prompt", "name", "inspect_metadata")));
    }
    
    private LLME2EScenario createScenario(final String userPrompt) {
        return new LLME2EScenario("scenario", "", userPrompt, new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) FROM orders", 2, List.of()),
                List.of(), List.of());
    }
    
    private MCPInteractionTraceRecord createPlanTrace() {
        return new MCPInteractionTraceRecord(1, "tool_call", MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN, "database_gateway_plan_mask_rule", Map.of(), Map.of("plan_id", "plan-1"), true, 0L);
    }
    
    private MCPInteractionTraceRecord createPromptTrace() {
        return new MCPInteractionTraceRecord(1, MCPInteractionActionNames.PROMPT_GET_KIND, MCPInteractionTraceRecord.PROTOCOL_BRIDGE_ORIGIN, MCPInteractionActionNames.GET_PROMPT,
                Map.of("name", "inspect_metadata"), Map.of(), true, 0L);
    }
}
