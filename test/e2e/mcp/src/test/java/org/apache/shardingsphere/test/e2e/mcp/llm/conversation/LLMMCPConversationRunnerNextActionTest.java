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

import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatCompletion;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatMessage;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMToolCall;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LLMMCPConversationRunnerNextActionTest extends AbstractLLMMCPConversationRunnerTest {
    
    @Test
    void assertRunRetriesExpectedExecuteQueryBeforeFinalAnswer() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(4);
        Map<String, Object> wrongQueryArguments = Map.of("database", "analytics_db", "schema", SCHEMA_NAME, "sql", QUERY, "max_rows", 10);
        Map<String, Object> expectedQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", wrongQueryArguments, "wrong-query-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", wrongQueryArguments)).thenReturn(createResultSetPayload(4));
        when(getLLMChatClient().complete(anyList(), anyList(), eq("auto"), eq(false))).thenReturn(
                createToolCallCompletion("tool-2", "database_gateway_execute_query", expectedQueryArguments, "expected-query-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", expectedQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query"), 2, "final-answer-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        List<List<LLMChatMessage>> actualMessages = captureAutoChatMessages();
        List<LLMChatMessage> actualAutoTurnMessages = actualMessages.getFirst();
        assertTrue(actualAutoTurnMessages.stream()
                .map(LLMChatMessage::getContent)
                .anyMatch(each -> each.contains("latest successful database_gateway_execute_query did not use database `logic_db`")));
        assertThat(actual.getInteractionTrace().size(), is(2));
    }
    
    @Test
    void assertRunCompletesWithCanonicalArguments() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of(MCPInteractionActionNames.COMPLETE, "database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(4);
        Map<String, Object> completionReference = Map.of("type", "ref/prompt", "name", PROMPT_NAME);
        Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", MCPInteractionActionNames.COMPLETE, JsonUtils.toJsonString(Map.of(
                                "ref", completionReference,
                                "argument", Map.of("name", "schema", "value", "pub")))),
                        new LLMToolCall("tool-2", "database_gateway_execute_query", JsonUtils.toJsonString(executeQueryArguments))),
                "tool-call-response"));
        when(getMCPInteractionClient().complete(completionReference, "schema", "pub", Map.of())).thenReturn(Map.of("completion", Map.of("values", List.of(SCHEMA_NAME))));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of(MCPInteractionActionNames.COMPLETE, "database_gateway_execute_query"), 2, "final-answer-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().size(), is(2));
        verify(getMCPInteractionClient()).complete(completionReference, "schema", "pub", Map.of());
    }
    
    @Test
    void assertRunRejectsLegacyCompletionArguments() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of(MCPInteractionActionNames.COMPLETE, "database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(3);
        Map<String, Object> completionReference = Map.of("type", "ref/prompt", "name", PROMPT_NAME);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", MCPInteractionActionNames.COMPLETE, Map.of("reference", completionReference, "argument_name", "schema"), "completion-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertFalse(actual.getAssertionReport().isSuccess());
        assertThat(actual.getAssertionReport().getFailureType(), is("invalid_tool_arguments"));
        assertThat(actual.getInteractionTrace().size(), is(1));
        verify(getMCPInteractionClient(), never()).complete(completionReference, "schema", "", Map.of());
    }
    
    @Test
    void assertRunPromptsImmediateCompletionNextAction() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of(MCPInteractionActionNames.GET_PROMPT, MCPInteractionActionNames.COMPLETE, "database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(5);
        Map<String, Object> getPromptArguments = Map.of("name", PROMPT_NAME);
        Map<String, Object> completionReference = Map.of("type", "ref/prompt", "name", PROMPT_NAME);
        Map<String, Object> completionArguments = Map.of("ref", completionReference, "argument", Map.of("name", "schema", "value", "pub"));
        Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", MCPInteractionActionNames.GET_PROMPT, getPromptArguments, "prompt-response"),
                createToolCallCompletion("tool-2", MCPInteractionActionNames.COMPLETE, completionArguments, "completion-response"),
                createToolCallCompletion("tool-3", "database_gateway_execute_query", executeQueryArguments, "query-response"));
        when(getMCPInteractionClient().getPrompt(PROMPT_NAME, Map.of())).thenReturn(Map.of("messages", List.of(), "next_actions", List.of(Map.of(
                "type", "completion",
                "ref", completionReference,
                "argument", Map.of("name", "schema", "value", "pub")))));
        when(getMCPInteractionClient().complete(completionReference, "schema", "pub", Map.of())).thenReturn(Map.of("completion", Map.of("values", List.of(SCHEMA_NAME))));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of(MCPInteractionActionNames.GET_PROMPT, MCPInteractionActionNames.COMPLETE, "database_gateway_execute_query"), 2, "final-answer-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        List<List<Map<String, Object>>> actualTools = captureRequiredToolDefinitions(3);
        assertThat(getToolNames(actualTools.get(1)), is(List.of(MCPInteractionActionNames.COMPLETE)));
        assertThat(getToolNames(actualTools.get(2)), is(List.of("database_gateway_execute_query")));
        verify(getMCPInteractionClient()).complete(completionReference, "schema", "pub", Map.of());
    }
    
    @Test
    void assertRunPromptsImmediateResourceNextAction() throws IOException, InterruptedException {
        List<String> toolNames = List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query");
        LLME2EScenario actualScenario = createScenario(toolNames);
        LLMMCPConversationRunner actualRunner = createRunner(4);
        Map<String, Object> runtimeArguments = Map.of("uri", "shardingsphere://runtime");
        Map<String, Object> capabilitiesArguments = Map.of("uri", RESOURCE_URI);
        Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", MCPInteractionActionNames.READ_RESOURCE, runtimeArguments, "runtime-response"),
                createToolCallCompletion("tool-2", MCPInteractionActionNames.READ_RESOURCE, capabilitiesArguments, "capabilities-response"),
                createToolCallCompletion("tool-3", "database_gateway_execute_query", executeQueryArguments, "query-response"));
        when(getMCPInteractionClient().readResource("shardingsphere://runtime")).thenReturn(Map.of("next_actions", List.of(Map.of(
                "type", "resource_read",
                "resource_uri", RESOURCE_URI))));
        when(getMCPInteractionClient().readResource(RESOURCE_URI)).thenReturn(Map.of("response_mode", "capabilities"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query"), 2, "final-answer-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        List<List<LLMChatMessage>> actualMessages = captureRequiredChatMessages(3);
        List<List<Map<String, Object>>> actualTools = captureRequiredToolDefinitions(3);
        assertTrue(containsMessage(actualMessages.get(1), "Call mcp_read_resource with uri `" + RESOURCE_URI + "` now"));
        assertThat(getToolNames(actualTools.get(1)), is(List.of(MCPInteractionActionNames.READ_RESOURCE)));
    }
    
    @Test
    void assertRunPromptsImmediateToolNextAction() throws IOException, InterruptedException {
        List<String> toolNames = List.of("database_gateway_plan_mask_rule", "database_gateway_apply_workflow", "database_gateway_execute_query");
        LLME2EScenario actualScenario = createScenario(toolNames);
        LLMMCPConversationRunner actualRunner = createRunner(4);
        Map<String, Object> planArguments = Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "table", TABLE_NAME, "column", "status");
        Map<String, Object> applyArguments = Map.of("plan_id", "plan-1", "execution_mode", "preview");
        Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_plan_mask_rule", planArguments, "plan-response"),
                createToolCallCompletion("tool-2", "database_gateway_apply_workflow", applyArguments, "apply-response"),
                createToolCallCompletion("tool-3", "database_gateway_execute_query", executeQueryArguments, "query-response"));
        when(getMCPInteractionClient().call("database_gateway_plan_mask_rule", planArguments)).thenReturn(Map.of("plan_id", "plan-1", "next_actions", List.of(Map.of(
                "type", "tool_call",
                "tool_name", "database_gateway_apply_workflow",
                "arguments", applyArguments))));
        when(getMCPInteractionClient().call("database_gateway_apply_workflow", applyArguments)).thenReturn(Map.of("response_mode", "preview"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().get(1).getActionOrigin(), is(MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN));
        List<List<LLMChatMessage>> actualMessages = captureRequiredChatMessages(3);
        List<List<Map<String, Object>>> actualTools = captureRequiredToolDefinitions(3);
        assertTrue(containsMessage(actualMessages.get(1), "Call `database_gateway_apply_workflow` now with exactly these arguments"));
        assertTrue(containsMessage(actualMessages.get(1), "\"plan_id\":\"plan-1\""));
        assertThat(getToolNames(actualTools.get(1)), is(List.of("database_gateway_apply_workflow")));
    }
    
    @Test
    void assertRunPromptsExactResourceAfterList() throws IOException, InterruptedException {
        List<String> toolNames = List.of(MCPInteractionActionNames.LIST_RESOURCES, MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query");
        String tableResourceUri = "shardingsphere://databases/logic_db/schemas/public/tables/orders";
        LLME2EScenario actualScenario = createScenario(toolNames, "Read exact `" + tableResourceUri + "` before querying.");
        LLMMCPConversationRunner actualRunner = createRunner(4);
        Map<String, Object> readResourceArguments = Map.of("uri", tableResourceUri);
        Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", MCPInteractionActionNames.LIST_RESOURCES, Map.of(), "list-response"),
                createToolCallCompletion("tool-2", MCPInteractionActionNames.READ_RESOURCE, readResourceArguments, "resource-response"),
                createToolCallCompletion("tool-3", "database_gateway_execute_query", executeQueryArguments, "query-response"));
        when(getMCPInteractionClient().listResources()).thenReturn(Map.of("resources", List.of(Map.of("uri", RESOURCE_URI))));
        when(getMCPInteractionClient().readResource(tableResourceUri)).thenReturn(Map.of("found", true));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        List<List<LLMChatMessage>> actualMessages = captureRequiredChatMessages(3);
        assertTrue(containsMessage(actualMessages.get(1), "Use exactly `" + tableResourceUri + "` as uri"));
        assertTrue(containsMessage(actualMessages.get(1), "do not copy parameter schema"));
    }
    
    @Test
    void assertRunPromptsLiveResourceAfterStaleResourceMiss() throws IOException, InterruptedException {
        List<String> toolNames = List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query");
        String staleResourceUri = "shardingsphere://databases/unknown/schemas/unknown/tables/orders";
        String tableResourceUri = "shardingsphere://databases/logic_db/schemas/public/tables/orders";
        LLME2EScenario actualScenario = createScenario(toolNames,
                "Read stale `" + staleResourceUri + "`, recover by reading exact live table resource `" + tableResourceUri + "`, then query.");
        LLMMCPConversationRunner actualRunner = createRunner(4);
        Map<String, Object> staleReadArguments = Map.of("uri", staleResourceUri);
        Map<String, Object> liveReadArguments = Map.of("uri", tableResourceUri);
        Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", MCPInteractionActionNames.READ_RESOURCE, staleReadArguments, "stale-response"),
                createToolCallCompletion("tool-2", MCPInteractionActionNames.READ_RESOURCE, liveReadArguments, "live-response"),
                createToolCallCompletion("tool-3", "database_gateway_execute_query", executeQueryArguments, "query-response"));
        when(getMCPInteractionClient().readResource(staleResourceUri)).thenReturn(Map.of(
                "found", false,
                "next_actions", List.of(Map.of(
                        "type", "resource_read",
                        "resource_uri", "shardingsphere://databases/unknown/schemas/unknown/tables"))));
        when(getMCPInteractionClient().readResource(tableResourceUri)).thenReturn(Map.of("found", true));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        List<List<LLMChatMessage>> actualMessages = captureRequiredChatMessages(3);
        assertTrue(containsMessage(actualMessages.get(1), "Use exactly `" + tableResourceUri + "` as uri"));
        assertThat(actual.getInteractionTrace().get(1).getArguments().get("uri"), is(tableResourceUri));
    }
    
    @Test
    void assertRunFollowsPendingNextActionBeforeFinalAnswer() throws IOException, InterruptedException {
        List<String> toolNames = List.of("database_gateway_apply_workflow", "database_gateway_execute_query");
        LLME2EScenario actualScenario = createScenario(toolNames);
        LLMMCPConversationRunner actualRunner = createRunner(3);
        Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        Map<String, Object> previewArguments = Map.of("plan_id", "plan-1", "execution_mode", "preview");
        Map<String, Object> manualArguments = Map.of("plan_id", "plan-1", "execution_mode", "manual-only");
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", "database_gateway_execute_query", JsonUtils.toJsonString(executeQueryArguments)),
                        new LLMToolCall("tool-2", "database_gateway_apply_workflow", JsonUtils.toJsonString(previewArguments))),
                "initial-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getMCPInteractionClient().call("database_gateway_apply_workflow", previewArguments)).thenReturn(Map.of("response_mode", "preview", "next_actions", List.of(Map.of(
                "type", "tool_call",
                "tool_name", "database_gateway_apply_workflow",
                "arguments", manualArguments))));
        when(getLLMChatClient().complete(anyList(), anyList(), eq("auto"), eq(false))).thenReturn(
                createToolCallCompletion("tool-3", "database_gateway_apply_workflow", manualArguments, "manual-response"));
        when(getMCPInteractionClient().call("database_gateway_apply_workflow", manualArguments)).thenReturn(Map.of("response_mode", "manual_only"));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query", "database_gateway_apply_workflow"), 2, "final-answer-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        List<List<LLMChatMessage>> actualMessages = captureAutoChatMessages();
        assertTrue(containsMessage(actualMessages.getFirst(), "Call `database_gateway_apply_workflow` now with exactly these arguments"));
    }
    
    @Test
    void assertRunRetriesUnavailableUpdateAfterSideEffectPreview() throws IOException, InterruptedException {
        List<String> toolNames = List.of("database_gateway_execute_update", "database_gateway_execute_query");
        LLME2EScenario actualScenario = createScenario(toolNames);
        LLMMCPConversationRunner actualRunner = createRunner(5);
        Map<String, Object> previewArguments = Map.of(
                "database", DATABASE_NAME,
                "schema", SCHEMA_NAME,
                "sql", "UPDATE orders SET status = status WHERE order_id = -1",
                "execution_mode", "preview");
        Map<String, Object> misroutedQueryArguments = Map.of(
                "database", DATABASE_NAME,
                "schema", SCHEMA_NAME,
                "sql", QUERY,
                "execution_mode", "preview");
        Map<String, Object> expectedQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_update", previewArguments, "preview-response"),
                createToolCallCompletion("tool-2", "database_gateway_execute_update", misroutedQueryArguments, "misrouted-query-response"),
                createToolCallCompletion("tool-3", "database_gateway_execute_query", expectedQueryArguments, "query-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_update", previewArguments)).thenReturn(Map.of("response_mode", "preview", "next_actions", List.of(Map.of(
                "type", "tool_call",
                "tool_name", "database_gateway_execute_update",
                "arguments", Map.of("database", DATABASE_NAME, "sql", "UPDATE orders SET status = status WHERE order_id = -1", "execution_mode", "execute")))));
        when(getMCPInteractionClient().call("database_gateway_execute_query", expectedQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        List<List<LLMChatMessage>> actualMessages = captureRequiredChatMessages(3);
        List<LLMChatMessage> actualSecondTurnMessages = actualMessages.get(1);
        assertTrue(containsMessage(actualSecondTurnMessages, "side-effect execution next_actions"));
        assertTrue(containsMessage(actualSecondTurnMessages,
                "Call database_gateway_execute_query now with database `" + DATABASE_NAME + "`, schema `" + SCHEMA_NAME + "`, and sql `" + QUERY + "`"));
        assertTrue(containsMessage(actualSecondTurnMessages, "Do not call database_gateway_execute_update for SELECT or row-count verification."));
        assertFalse(containsMessage(actualSecondTurnMessages, "\"tool_name\":\"database_gateway_execute_update\""));
        assertFalse(containsMessage(actualSecondTurnMessages, "\"title\":\"Execute\""));
        List<List<Map<String, Object>>> actualTools = captureRequiredToolDefinitions(3);
        assertThat(getToolNames(actualTools.get(1)), is(List.of("database_gateway_execute_query")));
        List<LLMChatMessage> actualThirdTurnMessages = actualMessages.get(2);
        assertTrue(containsMessage(actualThirdTurnMessages, "previous response requested `database_gateway_execute_update`"));
        assertTrue(containsMessage(actualThirdTurnMessages, "Available MCP tools for this turn: database_gateway_execute_query"));
        assertThat(getToolNames(actualTools.get(2)), is(List.of("database_gateway_execute_query")));
        assertThat(actual.getInteractionTrace().size(), is(2));
        assertThat(actual.getInteractionTrace().get(1).getTargetName(), is("database_gateway_execute_query"));
        verify(getMCPInteractionClient(), never()).call("database_gateway_execute_update", misroutedQueryArguments);
    }
    
    @Test
    void assertRunRetriesReadOnlyUpdateCallAfterSideEffectPreviewInSameCompletion() throws IOException, InterruptedException {
        List<String> toolNames = List.of("database_gateway_execute_update", "database_gateway_execute_query");
        LLME2EScenario actualScenario = createScenario(toolNames);
        LLMMCPConversationRunner actualRunner = createRunner(4);
        Map<String, Object> previewArguments = Map.of(
                "database", DATABASE_NAME,
                "schema", SCHEMA_NAME,
                "sql", "UPDATE orders SET status = status WHERE order_id = -1",
                "execution_mode", "preview");
        Map<String, Object> misroutedQueryArguments = Map.of(
                "database", DATABASE_NAME,
                "schema", SCHEMA_NAME,
                "sql", QUERY,
                "execution_mode", "preview");
        Map<String, Object> expectedQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("",
                        List.of(
                                new LLMToolCall("tool-1", "database_gateway_execute_update", JsonUtils.toJsonString(previewArguments)),
                                new LLMToolCall("tool-2", "database_gateway_execute_update", JsonUtils.toJsonString(misroutedQueryArguments))),
                        "compound-tool-response"),
                createToolCallCompletion("tool-3", "database_gateway_execute_query", expectedQueryArguments, "query-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_update", previewArguments)).thenReturn(Map.of("response_mode", "preview", "next_actions", List.of(Map.of(
                "type", "tool_call",
                "tool_name", "database_gateway_execute_update",
                "arguments", Map.of("database", DATABASE_NAME, "sql", "UPDATE orders SET status = status WHERE order_id = -1", "execution_mode", "execute")))));
        when(getMCPInteractionClient().call("database_gateway_execute_query", expectedQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        List<List<LLMChatMessage>> actualMessages = captureRequiredChatMessages(2);
        List<LLMChatMessage> actualSecondTurnMessages = actualMessages.get(1);
        assertTrue(containsMessage(actualSecondTurnMessages, "\"reason\":\"tool_not_available_in_current_turn\""));
        assertTrue(containsMessage(actualSecondTurnMessages, "previous response requested `database_gateway_execute_update`"));
        assertTrue(containsMessage(actualSecondTurnMessages, "Available MCP tools for this turn: database_gateway_execute_query"));
        List<List<Map<String, Object>>> actualTools = captureRequiredToolDefinitions(2);
        assertThat(getToolNames(actualTools.get(1)), is(List.of("database_gateway_execute_query")));
        assertThat(actual.getInteractionTrace().size(), is(2));
        assertThat(actual.getInteractionTrace().get(0).getTargetName(), is("database_gateway_execute_update"));
        assertThat(actual.getInteractionTrace().get(1).getTargetName(), is("database_gateway_execute_query"));
        verify(getMCPInteractionClient(), never()).call("database_gateway_execute_update", misroutedQueryArguments);
    }
    
    @Test
    void assertRunCompactsManualArtifactsBeforeReadOnlyVerification() throws IOException, InterruptedException {
        List<String> toolNames = List.of("database_gateway_apply_workflow", "database_gateway_execute_query");
        LLME2EScenario actualScenario = createScenario(toolNames);
        LLMMCPConversationRunner actualRunner = createRunner(3);
        Map<String, Object> manualArguments = Map.of("plan_id", "plan-1", "execution_mode", "manual-only");
        Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_apply_workflow", manualArguments, "manual-response"),
                createToolCallCompletion("tool-2", "database_gateway_execute_query", executeQueryArguments, "query-response"));
        when(getMCPInteractionClient().call("database_gateway_apply_workflow", manualArguments)).thenReturn(Map.of(
                "response_mode", "manual_only",
                "manual_artifact_summary", Map.of("distsql_artifact_count", 1),
                "manual_artifacts", List.of(Map.of("distsql_artifacts", List.of(Map.of("sql", "CREATE MASK RULE orders SECRET")))),
                "next_actions", List.of(Map.of(
                        "type", "ask_user",
                        "reason", "Confirm manual artifacts were executed."))));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        List<List<LLMChatMessage>> actualMessages = captureRequiredChatMessages(2);
        List<List<Map<String, Object>>> actualTools = captureRequiredToolDefinitions(2);
        List<LLMChatMessage> actualSecondTurnMessages = actualMessages.get(1);
        assertTrue(containsMessage(actualSecondTurnMessages, "\"distsql_artifact_count\":1"));
        assertFalse(actualSecondTurnMessages.stream().map(LLMChatMessage::getContent).anyMatch(each -> each.contains("CREATE MASK RULE orders SECRET")));
        assertThat(getToolNames(actualTools.get(1)), is(List.of("database_gateway_execute_query")));
    }
}
