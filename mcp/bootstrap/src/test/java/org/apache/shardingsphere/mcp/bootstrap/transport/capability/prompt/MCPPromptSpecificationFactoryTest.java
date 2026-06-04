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

package org.apache.shardingsphere.mcp.bootstrap.transport.capability.prompt;

import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MCPPromptSpecificationFactoryTest {
    
    @Test
    void assertCreatePromptSpecifications() {
        List<SyncPromptSpecification> actual = new MCPPromptSpecificationFactory().createPromptSpecifications();
        Set<String> actualNames = new LinkedHashSet<>(actual.stream().map(each -> each.prompt().name()).toList());
        assertTrue(actualNames.containsAll(Set.of("inspect_metadata", "safe_sql_execution", "recover_workflow", "plan_encrypt_rule", "plan_mask_rule")));
        SyncPromptSpecification actualPromptSpecification = findPrompt(actual, "safe_sql_execution");
        assertThat(actualPromptSpecification.prompt().title(), is("Safe SQL Execution"));
        assertThat(actualPromptSpecification.prompt().arguments().size(), is(3));
        assertThat(actualPromptSpecification.prompt().meta().get(MCPShardingSphereMetadataKeys.RELATED_TOOLS), is(List.of("database_gateway_execute_query", "database_gateway_execute_update")));
    }
    
    @Test
    void assertRenderInspectMetadataPromptTemplate() {
        McpSchema.GetPromptResult actual = renderPrompt("inspect_metadata", Map.of("database", "logic_db", "schema", "public", "query", "orders"));
        assertThat(actual.description(),
                is("Guide the model to inspect ShardingSphere logical metadata by reading capability and metadata resources before choosing database_gateway_search_metadata or detail resources."));
        assertRenderedLines(readText(actual), List.of("- database: logic_db", "- query: orders", "Stop conditions:"));
        assertThat(actual.meta().get(MCPShardingSphereMetadataKeys.STOP_CONDITIONS), is(List.of(
                "Stop after returning resolved metadata paths or after identifying the exact resource/tool to call next.",
                "Stop without SQL execution when the user only asked to inspect metadata.")));
        assertFalse(actual.meta().containsKey("templateResource"));
    }
    
    @Test
    void assertRenderSafeSQLExecutionPromptTemplate() {
        McpSchema.GetPromptResult actual = renderPrompt("safe_sql_execution", Map.of("database", "logic_db", "schema", "public", "sql_intent", "count orders"));
        String actualText = readText(actual);
        assertThat(actualText.lines().findFirst().orElse(""), is("Choose the safest MCP SQL path."));
        assertRenderedLines(actualText, List.of(
                "- database: logic_db",
                "- sql_intent: count orders",
                "2. Use database_gateway_execute_query only for one classifier-approved SELECT or EXPLAIN ANALYZE statement.",
                "5. Never split or batch multiple SQL statements into one MCP call."));
    }
    
    @Test
    void assertRenderRecoverWorkflowPromptTemplate() {
        McpSchema.GetPromptResult actual = renderPrompt("recover_workflow", Map.of("plan_id", "plan-1", "failure_summary", "metadata mismatch"));
        assertRenderedLines(readText(actual), List.of(
                "- plan_id: plan-1",
                "- failure_summary: metadata mismatch",
                "1. Treat plan_id as a current-session handle only. Do not reuse it across MCP sessions.",
                "5. Preserve user-provided corrections when re-planning with database_gateway_plan_encrypt_rule or database_gateway_plan_mask_rule."));
    }
    
    @Test
    void assertRenderPlanEncryptRulePromptTemplate() {
        McpSchema.GetPromptResult actual = renderPrompt("plan_encrypt_rule", Map.of(
                "database", "logic_db", "schema", "public", "table", "orders", "column", "phone", "algorithm_type", "AES", "plan_id", "plan-1"));
        assertRenderedLines(readText(actual), List.of(
                "- database: logic_db",
                "- column: phone",
                "- algorithm_type: AES",
                "2. Read shardingsphere://features/encrypt/algorithms before choosing algorithm_type.",
                "4. Call database_gateway_plan_encrypt_rule with gathered logical names and any reviewed algorithm choices."));
    }
    
    @Test
    void assertRenderPlanMaskRulePromptTemplate() {
        McpSchema.GetPromptResult actual = renderPrompt("plan_mask_rule", Map.of(
                "database", "logic_db", "schema", "public", "table", "orders", "column", "phone", "algorithm_type", "KEEP_FIRST_N_LAST_M", "plan_id", "plan-1"));
        assertRenderedLines(readText(actual), List.of(
                "- database: logic_db",
                "- column: phone",
                "- algorithm_type: KEEP_FIRST_N_LAST_M",
                "2. Read shardingsphere://features/mask/algorithms before choosing algorithm_type.",
                "4. Call database_gateway_plan_mask_rule with gathered logical names and any reviewed algorithm choice."));
    }
    
    @Test
    void assertRenderedPromptsContainNoGuessAndFinalAnswerRule() {
        Map<String, Map<String, Object>> promptArguments = Map.of(
                "inspect_metadata", Map.of("database", "logic_db", "schema", "public", "query", "orders"),
                "safe_sql_execution", Map.of("database", "logic_db", "schema", "public", "sql_intent", "count orders"),
                "recover_workflow", Map.of("plan_id", "plan-1", "failure_summary", "metadata mismatch"),
                "plan_encrypt_rule", Map.of("database", "logic_db", "schema", "public", "table", "orders", "column", "phone", "algorithm_type", "AES", "plan_id", "plan-1"),
                "plan_mask_rule", Map.of("database", "logic_db", "schema", "public", "table", "orders", "column", "phone", "algorithm_type", "KEEP_FIRST_N_LAST_M", "plan_id", "plan-1"));
        for (Entry<String, Map<String, Object>> entry : promptArguments.entrySet()) {
            String actual = readText(renderPrompt(entry.getKey(), entry.getValue()));
            assertTrue(actual.contains("completion/complete"), () -> "Missing completion guidance in " + entry.getKey());
            assertTrue(actual.contains("do not guess"), () -> "Missing no-guess guidance in " + entry.getKey());
            assertTrue(actual.contains("Final answer rule:"), () -> "Missing final answer rule heading in " + entry.getKey());
            assertTrue(actual.contains("Summarize confirmed facts, the selected MCP path, and any required next user action."), () -> "Missing final answer rule in " + entry.getKey());
        }
    }
    
    @Test
    void assertRejectMissingRequiredPromptArgument() {
        McpError actual = assertThrows(McpError.class, () -> renderPrompt("safe_sql_execution", Map.of("database", "logic_db")));
        assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.INVALID_PARAMS));
        assertThat(actual.getJsonRpcError().message(), is("Required prompt argument `sql_intent` is missing."));
    }
    
    @Test
    void assertRejectNullRequiredPromptArgument() {
        McpError actual = assertThrows(McpError.class, () -> renderPrompt("safe_sql_execution", Maps.of("database", null, "sql_intent", "count orders")));
        assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.INVALID_PARAMS));
        assertThat(actual.getJsonRpcError().message(), is("Required prompt argument `database` is missing."));
    }
    
    @Test
    void assertRejectBlankRequiredPromptArgument() {
        McpError actual = assertThrows(McpError.class, () -> renderPrompt("safe_sql_execution", Map.of("database", "logic_db", "sql_intent", " ")));
        assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.INVALID_PARAMS));
        assertThat(actual.getJsonRpcError().message(), is("Required prompt argument `sql_intent` is missing."));
    }
    
    private McpSchema.GetPromptResult renderPrompt(final String name, final Map<String, Object> arguments) {
        SyncPromptSpecification promptSpecification = findPrompt(new MCPPromptSpecificationFactory().createPromptSpecifications(), name);
        return promptSpecification.promptHandler().apply(mock(McpSyncServerExchange.class), new McpSchema.GetPromptRequest(name, arguments));
    }
    
    private String readText(final McpSchema.GetPromptResult promptResult) {
        return ((McpSchema.TextContent) promptResult.messages().get(0).content()).text();
    }
    
    private void assertRenderedLines(final String text, final List<String> expectedLines) {
        List<String> actualLines = text.lines().toList();
        for (String each : expectedLines) {
            assertTrue(actualLines.contains(each), () -> "Missing rendered line: " + each);
        }
    }
    
    private SyncPromptSpecification findPrompt(final List<SyncPromptSpecification> specifications, final String name) {
        return specifications.stream().filter(each -> name.equals(each.prompt().name())).findFirst().orElseThrow();
    }
}
