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
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMToolCall;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LLMMCPConversationRunnerTest {
    
    private static final String DATABASE_NAME = "logic_db";
    
    private static final String SCHEMA_NAME = "public";
    
    private static final String TABLE_NAME = "orders";
    
    private static final String QUERY = "SELECT COUNT(*) AS total_orders FROM orders";
    
    private static final String RESOURCE_URI = "shardingsphere://capabilities";
    
    private static final String PROMPT_NAME = "inspect_metadata";
    
    @Mock
    private LLMChatModelClient llmChatClient;
    
    @Mock
    private MCPInteractionClient mcpInteractionClient;
    
    @Test
    void assertRunWithExecuteQuery() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final ArgumentCaptor<List<Map<String, Object>>> actualTools = createToolDefinitionsCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(1));
        assertThat(actual.getInteractionTrace().get(0).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN));
        assertThat(actual.getRawModelOutputs(), CoreMatchers.is(List.of("tool-call-response", "final-answer-response")));
        verify(llmChatClient).complete(anyList(), actualTools.capture(), eq("required"), eq(false));
        verify(mcpInteractionClient).open();
        verify(mcpInteractionClient).call("database_gateway_execute_query", executeQueryArguments);
        verify(mcpInteractionClient).close();
        assertThat(getToolName(actualTools.getValue().get(0)), CoreMatchers.is("database_gateway_execute_query"));
        assertThat(getMap(getFunction(actualTools.getValue().get(0)).get("parameters")).get("required"), CoreMatchers.is(List.of("database", "sql")));
    }
    
    @Test
    void assertRunNormalizesMissingExecuteQuerySchema() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> modelQueryArguments = Map.of("database", DATABASE_NAME, "sql", QUERY, "max_rows", 10);
        final Map<String, Object> expectedQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", modelQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", expectedQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().get(0).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN));
        assertThat(actual.getInteractionTrace().get(0).getArguments(), CoreMatchers.is(expectedQueryArguments));
        verify(mcpInteractionClient).call("database_gateway_execute_query", expectedQueryArguments);
        verify(llmChatClient, never()).complete(anyList(), anyList(), eq("auto"), eq(false));
    }
    
    @Test
    void assertRunNormalizesMissingSearchMetadataScope() throws IOException, InterruptedException {
        final List<String> toolNames = List.of("database_gateway_search_metadata", "database_gateway_execute_query");
        final LLME2EScenario actualScenario = createScenario(toolNames, "Use logical database `logic_db` and schema `public` when searching.");
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> modelSearchArguments = Map.of("query", TABLE_NAME, "object_types", List.of("table"));
        final Map<String, Object> expectedSearchArguments = Map.of(
                "query", TABLE_NAME,
                "object_types", List.of("table"),
                "database", DATABASE_NAME,
                "schema", SCHEMA_NAME);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", "database_gateway_search_metadata", JsonUtils.toJsonString(modelSearchArguments)),
                        new LLMToolCall("tool-2", "database_gateway_execute_query", JsonUtils.toJsonString(executeQueryArguments))),
                "tool-call-response"));
        when(mcpInteractionClient.call("database_gateway_search_metadata", expectedSearchArguments)).thenReturn(
                Map.of("items", List.of(Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "table", TABLE_NAME))));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().get(0).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN));
        assertThat(actual.getInteractionTrace().get(0).getArguments(), CoreMatchers.is(expectedSearchArguments));
        verify(mcpInteractionClient).call("database_gateway_search_metadata", expectedSearchArguments);
    }
    
    @Test
    void assertRunNormalizesSchemaQualifiedExecuteQuerySchema() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final String actualQuery = "SELECT COUNT(*) AS total_orders FROM public.orders";
        final Map<String, Object> modelQueryArguments = Map.of("database", DATABASE_NAME, "sql", actualQuery);
        final Map<String, Object> expectedQueryArguments = Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "sql", actualQuery);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", modelQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", expectedQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().get(0).getArguments(), CoreMatchers.is(expectedQueryArguments));
        verify(llmChatClient, never()).complete(anyList(), anyList(), eq("auto"), eq(false));
    }
    
    @Test
    void assertRunPromptsExactFinalInteractionSequence() throws IOException, InterruptedException {
        final List<String> toolNames = List.of("database_gateway_execute_query", "database_gateway_execute_update");
        final LLME2EScenario actualScenario = createScenario(toolNames);
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final Map<String, Object> executeUpdateArguments = Map.of(
                "database", DATABASE_NAME,
                "schema", SCHEMA_NAME,
                "sql", "UPDATE orders SET status = status WHERE order_id = -1",
                "execution_mode", "preview");
        final ArgumentCaptor<List<LLMChatMessage>> actualFinalMessages = createChatMessagesCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", "database_gateway_execute_query", JsonUtils.toJsonString(executeQueryArguments)),
                        new LLMToolCall("tool-2", "database_gateway_execute_update", JsonUtils.toJsonString(executeUpdateArguments))),
                "tool-call-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(mcpInteractionClient.call("database_gateway_execute_update", executeUpdateArguments)).thenReturn(Map.of("result_kind", "preview"));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        verify(llmChatClient).complete(actualFinalMessages.capture(), eq(List.of()), eq("none"), eq(true));
        final List<LLMChatMessage> actualMessages = actualFinalMessages.getValue();
        final String actualInstruction = actualMessages.get(actualMessages.size() - 1).getContent();
        assertThat(actualInstruction, CoreMatchers.containsString("interactionSequence exactly to this JSON array: [\"database_gateway_execute_query\",\"database_gateway_execute_update\"]"));
        assertThat(actualInstruction, CoreMatchers.not(CoreMatchers.containsString("database_gateway_search_metadata")));
    }
    
    @Test
    void assertRunWithResourceBridgeSequence() throws IOException, InterruptedException {
        final List<String> actualToolNames = List.of(MCPInteractionActionNames.LIST_RESOURCES, MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query");
        final LLME2EScenario actualScenario = createScenario(actualToolNames);
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", MCPInteractionActionNames.LIST_RESOURCES, "{}"),
                        new LLMToolCall("tool-2", MCPInteractionActionNames.READ_RESOURCE, JsonUtils.toJsonString(Map.of("uri", RESOURCE_URI))),
                        new LLMToolCall("tool-3", "database_gateway_execute_query", JsonUtils.toJsonString(executeQueryArguments))),
                "tool-call-response"));
        when(mcpInteractionClient.listResources()).thenReturn(Map.of("resources", List.of(Map.of("uri", RESOURCE_URI))));
        when(mcpInteractionClient.readResource(RESOURCE_URI)).thenReturn(Map.of("supportedTools", List.of("database_gateway_search_metadata", "database_gateway_execute_query")));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload("2"));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(actualToolNames, "2", "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(3));
        assertThat(actual.getInteractionTrace().get(0).getActionKind(), CoreMatchers.is(MCPInteractionActionNames.RESOURCE_LIST_KIND));
        assertThat(actual.getInteractionTrace().get(0).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.PROTOCOL_BRIDGE_ORIGIN));
        assertThat(actual.getInteractionTrace().get(1).getActionKind(), CoreMatchers.is(MCPInteractionActionNames.RESOURCE_READ_KIND));
        assertThat(actual.getInteractionTrace().get(2).getTargetName(), CoreMatchers.is("database_gateway_execute_query"));
        assertThat(actual.getInteractionTrace().get(2).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN));
        verify(mcpInteractionClient).listResources();
        verify(mcpInteractionClient).readResource(RESOURCE_URI);
        verify(mcpInteractionClient).call("database_gateway_execute_query", executeQueryArguments);
    }
    
    @Test
    void assertRunWithWorkflowContextRecoveryReadback() throws IOException, InterruptedException {
        final List<String> actualToolNames = List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_plan_mask_rule", "database_gateway_apply_workflow",
                "database_gateway_validate_workflow", "database_gateway_execute_query");
        final LLME2EScenario actualScenario = createScenario(actualToolNames);
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final String workflowResourceUri = "shardingsphere://workflows/plan-1";
        final Map<String, Object> planArguments = Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "table", TABLE_NAME, "column", "status", "algorithm_type", "MD5");
        final Map<String, Object> applyArguments = Map.of("plan_id", "plan-1", "execution_mode", "manual-only");
        final Map<String, Object> validateArguments = Map.of("plan_id", "plan-1");
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", MCPInteractionActionNames.READ_RESOURCE, JsonUtils.toJsonString(Map.of("uri", "shardingsphere://features/mask/algorithms"))),
                        new LLMToolCall("tool-2", "database_gateway_plan_mask_rule", JsonUtils.toJsonString(planArguments)),
                        new LLMToolCall("tool-3", MCPInteractionActionNames.READ_RESOURCE, JsonUtils.toJsonString(Map.of("uri", "shardingsphere://runtime"))),
                        new LLMToolCall("tool-4", MCPInteractionActionNames.READ_RESOURCE, JsonUtils.toJsonString(Map.of("uri", workflowResourceUri))),
                        new LLMToolCall("tool-5", "database_gateway_apply_workflow", JsonUtils.toJsonString(applyArguments)),
                        new LLMToolCall("tool-6", "database_gateway_validate_workflow", JsonUtils.toJsonString(validateArguments)),
                        new LLMToolCall("tool-7", "database_gateway_execute_query", JsonUtils.toJsonString(executeQueryArguments))),
                "tool-call-response"));
        when(mcpInteractionClient.readResource("shardingsphere://features/mask/algorithms")).thenReturn(Map.of("algorithms", List.of(Map.of("type", "MD5"))));
        when(mcpInteractionClient.call("database_gateway_plan_mask_rule", planArguments)).thenReturn(Map.of("plan_id", "plan-1", "resources_to_read", List.of(Map.of("uri", workflowResourceUri))));
        when(mcpInteractionClient.readResource("shardingsphere://runtime")).thenReturn(Map.of("status", "available", "capability_fingerprint", "abc"));
        when(mcpInteractionClient.readResource(workflowResourceUri)).thenReturn(Map.of("plan_id", "plan-1", "status", "planned"));
        when(mcpInteractionClient.call("database_gateway_apply_workflow", applyArguments)).thenReturn(Map.of("status", "awaiting-manual-execution", "response_mode", "manual_only"));
        when(mcpInteractionClient.call("database_gateway_validate_workflow", validateArguments)).thenReturn(Map.of("status", "passed", "overall_status", "passed"));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of(
                        MCPInteractionActionNames.READ_RESOURCE, "database_gateway_plan_mask_rule", MCPInteractionActionNames.READ_RESOURCE, "database_gateway_apply_workflow",
                        "database_gateway_validate_workflow", "database_gateway_execute_query"), 2,
                        "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(7));
        verify(mcpInteractionClient).readResource(workflowResourceUri);
        verify(mcpInteractionClient).call("database_gateway_apply_workflow", applyArguments);
        verify(mcpInteractionClient).call("database_gateway_validate_workflow", validateArguments);
    }
    
    @Test
    void assertRunWithPromptAndCompletionBridgeSequence() throws IOException, InterruptedException {
        final List<String> actualToolNames = List.of(MCPInteractionActionNames.LIST_PROMPTS, MCPInteractionActionNames.GET_PROMPT, MCPInteractionActionNames.COMPLETE,
                "database_gateway_execute_query");
        final LLME2EScenario actualScenario = createScenario(actualToolNames);
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> promptArguments = Map.of("query", "orders", "database", DATABASE_NAME);
        final Map<String, Object> completionReference = Map.of("type", "ref/prompt", "name", PROMPT_NAME);
        final Map<String, Object> completionReferenceArgument = Map.of("type", "prompt", "name", PROMPT_NAME);
        final Map<String, String> completionContext = Map.of("database", DATABASE_NAME);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final ArgumentCaptor<List<Map<String, Object>>> actualTools = createToolDefinitionsCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", MCPInteractionActionNames.LIST_PROMPTS, "{}"),
                        new LLMToolCall("tool-2", MCPInteractionActionNames.GET_PROMPT, JsonUtils.toJsonString(Map.of("name", PROMPT_NAME, "arguments", promptArguments))),
                        new LLMToolCall("tool-3", MCPInteractionActionNames.COMPLETE, JsonUtils.toJsonString(Map.of(
                                "reference", completionReferenceArgument,
                                "argument_name", "schema",
                                "argument_value", "pub",
                                "context_arguments", completionContext))),
                        new LLMToolCall("tool-4", "database_gateway_execute_query", JsonUtils.toJsonString(executeQueryArguments))),
                "tool-call-response"));
        when(mcpInteractionClient.listPrompts()).thenReturn(Map.of("prompts", List.of(Map.of("name", PROMPT_NAME))));
        when(mcpInteractionClient.getPrompt(PROMPT_NAME, promptArguments)).thenReturn(Map.of("description", "Inspect metadata", "messages", List.of()));
        when(mcpInteractionClient.complete(completionReference, "schema", "pub", completionContext)).thenReturn(Map.of("completion", Map.of("values", List.of("public"))));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(actualToolNames, 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(4));
        assertThat(actual.getInteractionTrace().get(0).getActionKind(), CoreMatchers.is(MCPInteractionActionNames.PROMPT_LIST_KIND));
        assertThat(actual.getInteractionTrace().get(1).getActionKind(), CoreMatchers.is(MCPInteractionActionNames.PROMPT_GET_KIND));
        assertThat(actual.getInteractionTrace().get(2).getActionKind(), CoreMatchers.is(MCPInteractionActionNames.COMPLETION_KIND));
        verify(llmChatClient).complete(anyList(), actualTools.capture(), eq("required"), eq(false));
        verify(mcpInteractionClient).listPrompts();
        verify(mcpInteractionClient).getPrompt(PROMPT_NAME, promptArguments);
        verify(mcpInteractionClient).complete(completionReference, "schema", "pub", completionContext);
        assertThat(getToolName(actualTools.getValue().get(0)), CoreMatchers.is(MCPInteractionActionNames.LIST_PROMPTS));
    }
    
    @Test
    void assertRunWithSearchMetadataToolDefinition() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_search_metadata"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        final ArgumentCaptor<List<Map<String, Object>>> actualTools = createToolDefinitionsCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(llmChatClient).complete(anyList(), actualTools.capture(), eq("required"), eq(false));
        assertThat(getToolName(actualTools.getValue().get(0)), CoreMatchers.is("database_gateway_search_metadata"));
        assertThat(getRequiredFields(actualTools.getValue().get(0)), CoreMatchers.is(List.of()));
        assertThat(getPropertyType(actualTools.getValue().get(0), "query"), CoreMatchers.is("string"));
        assertThat(getPropertyType(actualTools.getValue().get(0), "object_types"), CoreMatchers.is("array"));
        assertThat(getNestedPropertyType(actualTools.getValue().get(0), "object_types", "items"), CoreMatchers.is("string"));
        assertThat(getPropertyType(actualTools.getValue().get(0), "page_size"), CoreMatchers.is("integer"));
    }
    
    @Test
    void assertRunOffersOnlyNextMissingRequiredTool() throws IOException, InterruptedException {
        final List<String> toolNames = List.of("database_gateway_search_metadata", MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query");
        final LLME2EScenario actualScenario = createScenario(toolNames);
        final LLMMCPConversationRunner actualRunner = createRunner(4);
        final String tableResourceUri = "shardingsphere://databases/logic_db/schemas/public/tables/orders";
        final Map<String, Object> searchArguments = Map.of("query", TABLE_NAME);
        final Map<String, Object> readResourceArguments = Map.of("uri", tableResourceUri);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final ArgumentCaptor<List<Map<String, Object>>> actualTools = createToolDefinitionsCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_search_metadata", searchArguments, "search-response"),
                createToolCallCompletion("tool-2", MCPInteractionActionNames.READ_RESOURCE, readResourceArguments, "resource-response"),
                createToolCallCompletion("tool-3", "database_gateway_execute_query", executeQueryArguments, "query-response"));
        when(mcpInteractionClient.call("database_gateway_search_metadata", searchArguments)).thenReturn(Map.of("items", List.of(Map.of("table", TABLE_NAME))));
        when(mcpInteractionClient.readResource(tableResourceUri)).thenReturn(Map.of("table", TABLE_NAME));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        verify(llmChatClient, times(3)).complete(anyList(), actualTools.capture(), eq("required"), eq(false));
        assertThat(getToolNames(actualTools.getAllValues().get(0)), CoreMatchers.is(List.of("database_gateway_search_metadata")));
        assertThat(getToolNames(actualTools.getAllValues().get(1)), CoreMatchers.is(List.of(MCPInteractionActionNames.READ_RESOURCE)));
        assertThat(getToolNames(actualTools.getAllValues().get(2)), CoreMatchers.is(List.of("database_gateway_execute_query")));
    }
    
    @Test
    void assertRunWithUnexpectedTool() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("", List.of(new LLMToolCall("tool-1", "unsupported_tool", "{}")), "tool-call-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("unexpected_tool_requested"));
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(1));
        verify(mcpInteractionClient).open();
        verify(mcpInteractionClient, never()).call(anyString(), anyMap());
        verify(mcpInteractionClient).close();
    }
    
    @Test
    void assertRunWithInvalidToolArgumentsJson() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("", List.of(new LLMToolCall("tool-1", "database_gateway_execute_query", "{invalid")), "tool-call-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("invalid_tool_arguments"));
        assertThat(actual.getInteractionTrace().get(0).getTargetName(), CoreMatchers.is("database_gateway_execute_query"));
        verify(mcpInteractionClient).open();
        verify(mcpInteractionClient, never()).call(anyString(), anyMap());
        verify(mcpInteractionClient).close();
    }
    
    @Test
    void assertRunWithEmptyResourceUri() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of(MCPInteractionActionNames.READ_RESOURCE));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", MCPInteractionActionNames.READ_RESOURCE, Map.of("uri", "   "), "tool-call-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("invalid_tool_arguments"));
        assertThat(actual.getInteractionTrace().get(0).getActionKind(), CoreMatchers.is(MCPInteractionActionNames.RESOURCE_READ_KIND));
        verify(mcpInteractionClient, never()).readResource(anyString());
    }
    
    @Test
    void assertRunWithUnsafeSqlAttempted() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query",
                        Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "sql", "UPDATE orders SET status = 'DONE'"),
                        "tool-call-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("unsafe_sql_attempted"));
        assertThat(actual.getInteractionTrace().get(0).getTargetName(), CoreMatchers.is("database_gateway_execute_query"));
        verify(mcpInteractionClient, never()).call(anyString(), anyMap());
    }
    
    @Test
    void assertRunWithUnsafeExecuteUpdateAttempted() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_update"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_update",
                        Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "sql", "UPDATE orders SET status = 'DONE'", "execution_mode", "execute"),
                        "tool-call-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("unsafe_sql_execution_attempted"));
        assertThat(actual.getInteractionTrace().get(0).getTargetName(), CoreMatchers.is("database_gateway_execute_update"));
        verify(mcpInteractionClient, never()).call(anyString(), anyMap());
    }
    
    @Test
    void assertRunWithUnsafeWorkflowExecutionAttempted() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_apply_workflow"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_apply_workflow", Map.of("plan_id", "plan-1", "execution_mode", "review-then-execute"), "tool-call-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("unsafe_workflow_execution_attempted"));
        assertThat(actual.getInteractionTrace().get(0).getTargetName(), CoreMatchers.is("database_gateway_apply_workflow"));
        verify(mcpInteractionClient, never()).call(anyString(), anyMap());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("unexpectedQueryResultCases")
    void assertRunWithUnexpectedQueryResult(final String caseName, final Map<String, Object> finalAnswerPayload) throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                new LLMChatCompletion(JsonUtils.toJsonString(finalAnswerPayload), List.of(), caseName));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("unexpected_query_result"));
        verify(mcpInteractionClient).call("database_gateway_execute_query", executeQueryArguments);
    }
    
    @Test
    void assertRunAcceptsObjectInteractionSequence() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final Map<String, Object> finalAnswerPayload = createFinalAnswerPayload(List.of(), 2);
        finalAnswerPayload.put("interactionSequence", List.of(Map.of("tool", "database_gateway_execute_query", "arguments", executeQueryArguments)));
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                new LLMChatCompletion(JsonUtils.toJsonString(finalAnswerPayload), List.of(), "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
    }
    
    @Test
    void assertRunAcceptsCollapsedRepeatedInteractionSequence() throws IOException, InterruptedException {
        final List<String> toolNames = List.of("database_gateway_search_metadata", "database_gateway_execute_query");
        final LLME2EScenario actualScenario = createScenario(toolNames);
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> searchMetadataArguments = Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "query", TABLE_NAME);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", "database_gateway_search_metadata", JsonUtils.toJsonString(searchMetadataArguments)),
                        new LLMToolCall("tool-2", "database_gateway_search_metadata", JsonUtils.toJsonString(searchMetadataArguments)),
                        new LLMToolCall("tool-3", "database_gateway_execute_query", JsonUtils.toJsonString(executeQueryArguments))),
                "tool-call-response"));
        when(mcpInteractionClient.call("database_gateway_search_metadata", searchMetadataArguments)).thenReturn(Map.of("items", List.of()));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
    }
    
    @Test
    void assertRunAcceptsSchemaQualifiedQuery() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final Map<String, Object> finalAnswerPayload = createFinalAnswerPayload(List.of("database_gateway_execute_query"), 2);
        finalAnswerPayload.put("query", "SELECT COUNT(*) AS total_orders FROM public.orders");
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                new LLMChatCompletion(JsonUtils.toJsonString(finalAnswerPayload), List.of(), "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
    }
    
    private static Stream<Arguments> unexpectedQueryResultCases() {
        return Stream.of(
                Arguments.of("database mismatch", createMutatedFinalAnswerPayload("database", "other_db")),
                Arguments.of("schema mismatch", createMutatedFinalAnswerPayload("schema", "other_schema")),
                Arguments.of("table mismatch", createMutatedFinalAnswerPayload("table", "other_table")),
                Arguments.of("query mismatch", createMutatedFinalAnswerPayload("query", "SELECT * FROM orders")),
                Arguments.of("totalOrders mismatch", createMutatedFinalAnswerPayload("totalOrders", 3)),
                Arguments.of("interactionSequence mismatch", createMutatedFinalAnswerPayload("interactionSequence", List.of(MCPInteractionActionNames.READ_RESOURCE))));
    }
    
    @Test
    void assertRunWithNonNumericExecuteQueryTrace() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload("NaN"));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("unexpected_query_result"));
    }
    
    @Test
    void assertRunWithNonResultSetExecuteQueryTrace() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(Map.of("result_kind", "update_count", "update_count", 1));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("unexpected_query_result"));
    }
    
    @Test
    void assertRunWithMissingRequiredToolCoverage() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(0));
        verify(mcpInteractionClient).open();
        verify(mcpInteractionClient).close();
    }
    
    @Test
    void assertRunNamesRemainingToolsWhenCoverageIsMissing() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final ArgumentCaptor<List<LLMChatMessage>> actualMessages = createChatMessagesCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response-1"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(llmChatClient, times(2)).complete(actualMessages.capture(), anyList(), eq("required"), eq(false));
        final List<LLMChatMessage> actualSecondTurnMessages = actualMessages.getAllValues().get(1);
        assertThat(actualSecondTurnMessages.get(actualSecondTurnMessages.size() - 1).getContent(),
                CoreMatchers.containsString("Remaining required MCP tools: database_gateway_execute_query"));
        assertThat(actualSecondTurnMessages.get(actualSecondTurnMessages.size() - 1).getContent(), CoreMatchers.containsString("actual MCP tool_call"));
        assertThat(actualSecondTurnMessages.get(actualSecondTurnMessages.size() - 1).getContent(), CoreMatchers.containsString("database `logic_db`"));
        assertThat(actualSecondTurnMessages.get(actualSecondTurnMessages.size() - 1).getContent(), CoreMatchers.containsString("schema `public`"));
        assertThat(actualSecondTurnMessages.get(actualSecondTurnMessages.size() - 1).getContent(), CoreMatchers.containsString("sql `SELECT COUNT(*) AS total_orders FROM orders`"));
    }
    
    @Test
    void assertRunNamesExactResourceUriWhenResourceCoverageIsMissing() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of(MCPInteractionActionNames.READ_RESOURCE));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final ArgumentCaptor<List<LLMChatMessage>> actualMessages = createChatMessagesCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response-1"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(llmChatClient, times(2)).complete(actualMessages.capture(), anyList(), eq("required"), eq(false));
        final List<LLMChatMessage> actualSecondTurnMessages = actualMessages.getAllValues().get(1);
        final String actualRetryInstruction = actualSecondTurnMessages.get(actualSecondTurnMessages.size() - 1).getContent();
        assertThat(actualRetryInstruction, CoreMatchers.containsString("Remaining required MCP tools: mcp_read_resource"));
        assertThat(actualRetryInstruction, CoreMatchers.containsString("exact shardingsphere:// URI"));
        assertThat(actualRetryInstruction, CoreMatchers.containsString("do not invent abbreviated URI strings"));
    }
    
    @Test
    void assertRunNamesPreviewModeWhenUpdateCoverageIsMissing() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_update"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final ArgumentCaptor<List<LLMChatMessage>> actualMessages = createChatMessagesCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response-1"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(llmChatClient, times(2)).complete(actualMessages.capture(), anyList(), eq("required"), eq(false));
        final List<LLMChatMessage> actualSecondTurnMessages = actualMessages.getAllValues().get(1);
        final String actualRetryInstruction = actualSecondTurnMessages.get(actualSecondTurnMessages.size() - 1).getContent();
        assertThat(actualRetryInstruction, CoreMatchers.containsString("Remaining required MCP tools: database_gateway_execute_update"));
        assertThat(actualRetryInstruction, CoreMatchers.containsString("database `logic_db`"));
        assertThat(actualRetryInstruction, CoreMatchers.containsString("schema `public`"));
        assertThat(actualRetryInstruction, CoreMatchers.containsString("execution_mode=preview"));
        assertThat(actualRetryInstruction, CoreMatchers.containsString("do not use execution_mode=execute"));
    }
    
    @Test
    void assertRunNamesPlanIdRuleWhenPlanningCoverageIsMissing() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_plan_mask_rule"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final ArgumentCaptor<List<LLMChatMessage>> actualMessages = createChatMessagesCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response-1"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(llmChatClient, times(2)).complete(actualMessages.capture(), anyList(), eq("required"), eq(false));
        final List<LLMChatMessage> actualSecondTurnMessages = actualMessages.getAllValues().get(1);
        final String actualRetryInstruction = actualSecondTurnMessages.get(actualSecondTurnMessages.size() - 1).getContent();
        assertThat(actualRetryInstruction, CoreMatchers.containsString("Remaining required MCP tools: database_gateway_plan_mask_rule"));
        assertThat(actualRetryInstruction, CoreMatchers.containsString("For a new database_gateway_plan_* call"));
        assertThat(actualRetryInstruction, CoreMatchers.containsString("omit plan_id"));
    }
    
    @Test
    void assertRunRemovesInitialPlanningPlanId() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_plan_mask_rule"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        final Map<String, Object> rawPlanArguments = Map.of("plan_id", "1", "database", DATABASE_NAME, "schema", SCHEMA_NAME, "table", TABLE_NAME, "column", "status");
        final Map<String, Object> expectedPlanArguments = Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "table", TABLE_NAME, "column", "status");
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_plan_mask_rule", rawPlanArguments, "plan-response"));
        when(mcpInteractionClient.call("database_gateway_plan_mask_rule", expectedPlanArguments)).thenReturn(Map.of("plan_id", "plan-1"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        assertThat(actual.getInteractionTrace().get(0).getArguments(), CoreMatchers.is(expectedPlanArguments));
        assertThat(actual.getInteractionTrace().get(0).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN));
        verify(mcpInteractionClient).call("database_gateway_plan_mask_rule", expectedPlanArguments);
    }
    
    @Test
    void assertRunKeepsCurrentPlanningPlanId() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_plan_mask_rule"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> initialPlanArguments = Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "table", TABLE_NAME, "column", "status");
        final Map<String, Object> currentPlanArguments = Map.of("plan_id", "plan-1", "database", DATABASE_NAME, "schema", SCHEMA_NAME, "table", TABLE_NAME, "column", "status");
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_plan_mask_rule", initialPlanArguments, "initial-plan-response"));
        when(llmChatClient.complete(anyList(), anyList(), eq("auto"), eq(false))).thenReturn(
                createToolCallCompletion("tool-2", "database_gateway_plan_mask_rule", currentPlanArguments, "current-plan-response"));
        when(mcpInteractionClient.call("database_gateway_plan_mask_rule", initialPlanArguments)).thenReturn(Map.of("plan_id", "plan-1", "next_actions", List.of(Map.of(
                "type", "tool_call",
                "tool_name", "database_gateway_plan_mask_rule",
                "arguments", currentPlanArguments,
                "requires_user_approval", false))));
        when(mcpInteractionClient.call("database_gateway_plan_mask_rule", currentPlanArguments)).thenReturn(Map.of("plan_id", "plan-1"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        assertThat(actual.getInteractionTrace().get(1).getArguments(), CoreMatchers.is(currentPlanArguments));
        assertThat(actual.getInteractionTrace().get(1).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN));
        verify(mcpInteractionClient).call("database_gateway_plan_mask_rule", currentPlanArguments);
    }
    
    @Test
    void assertRunRequiresToolCallWhenExpectedExecuteQueryIsReturnedAsText() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion(QUERY, List.of(), "query-text-response"),
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "query-tool-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(1));
        assertThat(actual.getInteractionTrace().get(0).getTargetName(), CoreMatchers.is("database_gateway_execute_query"));
        assertThat(actual.getInteractionTrace().get(0).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN));
        verify(mcpInteractionClient).call("database_gateway_execute_query", executeQueryArguments);
        verify(llmChatClient, times(2)).complete(anyList(), anyList(), eq("required"), eq(false));
    }
    
    @Test
    void assertRunNamesLatestPlanIdWhenWorkflowCoverageIsMissing() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_plan_mask_rule", "database_gateway_apply_workflow"));
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> planArguments = Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "table", TABLE_NAME, "column", "status");
        final ArgumentCaptor<List<LLMChatMessage>> actualMessages = createChatMessagesCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_plan_mask_rule", planArguments, "plan-response"),
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response-1"));
        when(mcpInteractionClient.call("database_gateway_plan_mask_rule", planArguments)).thenReturn(Map.of("plan_id", "plan-1"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(llmChatClient, times(3)).complete(actualMessages.capture(), anyList(), eq("required"), eq(false));
        final List<LLMChatMessage> actualThirdTurnMessages = actualMessages.getAllValues().get(2);
        final String actualRetryInstruction = actualThirdTurnMessages.get(actualThirdTurnMessages.size() - 1).getContent();
        assertThat(actualRetryInstruction, CoreMatchers.containsString("Remaining required MCP tools: database_gateway_apply_workflow"));
        assertThat(actualRetryInstruction, CoreMatchers.containsString("set plan_id `plan-1`"));
        assertThat(actualRetryInstruction, CoreMatchers.containsString("do not use placeholder text `plan_id`"));
    }
    
    @Test
    void assertRunRetriesExpectedExecuteQueryBeforeFinalAnswer() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(4);
        final Map<String, Object> wrongQueryArguments = Map.of("database", "analytics_db", "schema", SCHEMA_NAME, "sql", QUERY, "max_rows", 10);
        final Map<String, Object> expectedQueryArguments = createExecuteQueryArguments(QUERY);
        final ArgumentCaptor<List<LLMChatMessage>> actualMessages = createChatMessagesCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", wrongQueryArguments, "wrong-query-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", wrongQueryArguments)).thenReturn(createResultSetPayload(4));
        when(llmChatClient.complete(anyList(), anyList(), eq("auto"), eq(false))).thenReturn(
                createToolCallCompletion("tool-2", "database_gateway_execute_query", expectedQueryArguments, "expected-query-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", expectedQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        verify(llmChatClient).complete(actualMessages.capture(), anyList(), eq("auto"), eq(false));
        final List<LLMChatMessage> actualAutoTurnMessages = actualMessages.getValue();
        assertTrue(actualAutoTurnMessages.stream()
                .map(LLMChatMessage::getContent)
                .anyMatch(each -> each.contains("latest successful database_gateway_execute_query did not use database `logic_db`")));
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(2));
    }
    
    @Test
    void assertRunRecoversCompletionArguments() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of(MCPInteractionActionNames.COMPLETE, "database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(4);
        final Map<String, Object> completionReference = Map.of("type", "ref/prompt", "name", PROMPT_NAME);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", MCPInteractionActionNames.COMPLETE, Map.of("argument_name", "schema", "argument_value", "pub"), "bad-completion-response"),
                new LLMChatCompletion("",
                        List.of(
                                new LLMToolCall("tool-2", MCPInteractionActionNames.COMPLETE, JsonUtils.toJsonString(Map.of(
                                        "reference", completionReference,
                                        "argument_name", "schema",
                                        "argument_value", "pub"))),
                                new LLMToolCall("tool-3", "database_gateway_execute_query", JsonUtils.toJsonString(executeQueryArguments))),
                        "retry-completion-response"));
        when(mcpInteractionClient.complete(completionReference, "schema", "pub", Map.of())).thenReturn(Map.of("completion", Map.of("values", List.of(SCHEMA_NAME))));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of(MCPInteractionActionNames.COMPLETE, "database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(3));
        assertThat(actual.getInteractionTrace().get(0).getStructuredContent().get("error_code"), CoreMatchers.is("invalid_tool_arguments"));
        verify(mcpInteractionClient).complete(completionReference, "schema", "pub", Map.of());
    }
    
    @Test
    void assertRunNormalizesCompletionArgumentName() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of(MCPInteractionActionNames.COMPLETE, "database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> completionReference = Map.of("type", "ref/prompt", "name", PROMPT_NAME);
        final Map<String, String> completionContext = Map.of("database", DATABASE_NAME);
        final Map<String, Object> completionArguments = new LinkedHashMap<>(4, 1F);
        completionArguments.put("reference", completionReference);
        completionArguments.put("reference argument_name", "schema");
        completionArguments.put("argument_value", "pub");
        completionArguments.put("context_arguments", completionContext);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", MCPInteractionActionNames.COMPLETE, completionArguments, "completion-response"),
                createToolCallCompletion("tool-2", "database_gateway_execute_query", executeQueryArguments, "query-response"));
        when(mcpInteractionClient.complete(completionReference, "schema", "pub", completionContext)).thenReturn(Map.of("completion", Map.of("values", List.of(SCHEMA_NAME))));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of(MCPInteractionActionNames.COMPLETE, "database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(2));
        verify(mcpInteractionClient).complete(completionReference, "schema", "pub", completionContext);
    }
    
    @Test
    void assertRunDefaultsCompletionReferenceFromPrompt() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of(MCPInteractionActionNames.GET_PROMPT, MCPInteractionActionNames.COMPLETE, "database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(5);
        final Map<String, Object> getPromptArguments = Map.of("name", PROMPT_NAME);
        final Map<String, Object> completionReference = Map.of("type", "ref/prompt", "name", PROMPT_NAME);
        final Map<String, Object> completionArguments = Map.of("argument_name", "schema", "argument_value", "pub");
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final ArgumentCaptor<List<Map<String, Object>>> actualTools = createToolDefinitionsCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", MCPInteractionActionNames.GET_PROMPT, getPromptArguments, "prompt-response"),
                createToolCallCompletion("tool-2", MCPInteractionActionNames.COMPLETE, completionArguments, "completion-response"),
                createToolCallCompletion("tool-3", "database_gateway_execute_query", executeQueryArguments, "query-response"));
        when(mcpInteractionClient.getPrompt(PROMPT_NAME, Map.of())).thenReturn(Map.of("messages", List.of(), "next_actions", List.of(Map.of(
                "type", "completion",
                "arguments", Map.of("reference", completionReference, "argument_name", "schema", "argument_value", "pub"),
                "requires_user_approval", false))));
        when(mcpInteractionClient.complete(completionReference, "schema", "pub", Map.of())).thenReturn(Map.of("completion", Map.of("values", List.of(SCHEMA_NAME))));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of(MCPInteractionActionNames.GET_PROMPT, MCPInteractionActionNames.COMPLETE, "database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        verify(llmChatClient, times(3)).complete(anyList(), actualTools.capture(), eq("required"), eq(false));
        assertThat(getToolNames(actualTools.getAllValues().get(1)), CoreMatchers.is(List.of(MCPInteractionActionNames.COMPLETE)));
        assertThat(getToolNames(actualTools.getAllValues().get(2)), CoreMatchers.is(List.of("database_gateway_execute_query")));
        verify(mcpInteractionClient).complete(completionReference, "schema", "pub", Map.of());
    }
    
    @Test
    void assertRunPromptsImmediateResourceNextAction() throws IOException, InterruptedException {
        final List<String> toolNames = List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query");
        final LLME2EScenario actualScenario = createScenario(toolNames);
        final LLMMCPConversationRunner actualRunner = createRunner(4);
        final Map<String, Object> runtimeArguments = Map.of("uri", "shardingsphere://runtime");
        final Map<String, Object> capabilitiesArguments = Map.of("uri", RESOURCE_URI);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final ArgumentCaptor<List<LLMChatMessage>> actualMessages = createChatMessagesCaptor();
        final ArgumentCaptor<List<Map<String, Object>>> actualTools = createToolDefinitionsCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", MCPInteractionActionNames.READ_RESOURCE, runtimeArguments, "runtime-response"),
                createToolCallCompletion("tool-2", MCPInteractionActionNames.READ_RESOURCE, capabilitiesArguments, "capabilities-response"),
                createToolCallCompletion("tool-3", "database_gateway_execute_query", executeQueryArguments, "query-response"));
        when(mcpInteractionClient.readResource("shardingsphere://runtime")).thenReturn(Map.of("next_actions", List.of(Map.of(
                "type", "resource_read",
                "resource_uri", RESOURCE_URI,
                "requires_user_approval", false))));
        when(mcpInteractionClient.readResource(RESOURCE_URI)).thenReturn(Map.of("response_mode", "capabilities"));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        verify(llmChatClient, times(3)).complete(actualMessages.capture(), actualTools.capture(), eq("required"), eq(false));
        assertTrue(containsMessage(actualMessages.getAllValues().get(1), "Call mcp_read_resource with uri `" + RESOURCE_URI + "` now"));
        assertThat(getToolNames(actualTools.getAllValues().get(1)), CoreMatchers.is(List.of(MCPInteractionActionNames.READ_RESOURCE)));
    }
    
    @Test
    void assertRunPromptsImmediateToolNextAction() throws IOException, InterruptedException {
        final List<String> toolNames = List.of("database_gateway_plan_mask_rule", "database_gateway_apply_workflow", "database_gateway_execute_query");
        final LLME2EScenario actualScenario = createScenario(toolNames);
        final LLMMCPConversationRunner actualRunner = createRunner(4);
        final Map<String, Object> planArguments = Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "table", TABLE_NAME, "column", "status");
        final Map<String, Object> applyArguments = Map.of("plan_id", "plan-1", "execution_mode", "preview");
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final ArgumentCaptor<List<LLMChatMessage>> actualMessages = createChatMessagesCaptor();
        final ArgumentCaptor<List<Map<String, Object>>> actualTools = createToolDefinitionsCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_plan_mask_rule", planArguments, "plan-response"),
                createToolCallCompletion("tool-2", "database_gateway_apply_workflow", Map.of("plan_id", "1", "execution_mode", "preview"), "apply-response"),
                createToolCallCompletion("tool-3", "database_gateway_execute_query", executeQueryArguments, "query-response"));
        when(mcpInteractionClient.call("database_gateway_plan_mask_rule", planArguments)).thenReturn(Map.of("plan_id", "plan-1", "next_actions", List.of(Map.of(
                "type", "tool_call",
                "tool_name", "database_gateway_apply_workflow",
                "arguments", applyArguments,
                "requires_user_approval", false))));
        when(mcpInteractionClient.call("database_gateway_apply_workflow", applyArguments)).thenReturn(Map.of("response_mode", "preview"));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().get(1).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN));
        verify(llmChatClient, times(3)).complete(actualMessages.capture(), actualTools.capture(), eq("required"), eq(false));
        assertTrue(containsMessage(actualMessages.getAllValues().get(1), "Call `database_gateway_apply_workflow` now with exactly these arguments"));
        assertTrue(containsMessage(actualMessages.getAllValues().get(1), "\"plan_id\":\"plan-1\""));
        assertThat(getToolNames(actualTools.getAllValues().get(1)), CoreMatchers.is(List.of("database_gateway_apply_workflow")));
    }
    
    @Test
    void assertRunPromptsExactResourceAfterList() throws IOException, InterruptedException {
        final List<String> toolNames = List.of(MCPInteractionActionNames.LIST_RESOURCES, MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query");
        final String tableResourceUri = "shardingsphere://databases/logic_db/schemas/public/tables/orders";
        final LLME2EScenario actualScenario = createScenario(toolNames, "Read exact `" + tableResourceUri + "` before querying.");
        final LLMMCPConversationRunner actualRunner = createRunner(4);
        final Map<String, Object> readResourceArguments = Map.of("uri", "{description=Resource URI to read., type=string}");
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final ArgumentCaptor<List<LLMChatMessage>> actualMessages = createChatMessagesCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", MCPInteractionActionNames.LIST_RESOURCES, Map.of(), "list-response"),
                createToolCallCompletion("tool-2", MCPInteractionActionNames.READ_RESOURCE, readResourceArguments, "resource-response"),
                createToolCallCompletion("tool-3", "database_gateway_execute_query", executeQueryArguments, "query-response"));
        when(mcpInteractionClient.listResources()).thenReturn(Map.of("resources", List.of(Map.of("uri", RESOURCE_URI))));
        when(mcpInteractionClient.readResource(tableResourceUri)).thenReturn(Map.of("found", true));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        verify(llmChatClient, times(3)).complete(actualMessages.capture(), anyList(), eq("required"), eq(false));
        assertTrue(containsMessage(actualMessages.getAllValues().get(1), "Use exactly `" + tableResourceUri + "` as uri"));
        assertTrue(containsMessage(actualMessages.getAllValues().get(1), "do not copy parameter schema"));
    }
    
    @Test
    void assertRunPromptsLiveResourceAfterStaleResourceMiss() throws IOException, InterruptedException {
        final List<String> toolNames = List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query");
        final String staleResourceUri = "shardingsphere://databases/unknown/schemas/unknown/tables/orders";
        final String tableResourceUri = "shardingsphere://databases/logic_db/schemas/public/tables/orders";
        final LLME2EScenario actualScenario = createScenario(toolNames,
                "Read stale `" + staleResourceUri + "`, recover by reading exact live table resource `" + tableResourceUri + "`, then query.");
        final LLMMCPConversationRunner actualRunner = createRunner(4);
        final Map<String, Object> staleReadArguments = Map.of("uri", staleResourceUri);
        final Map<String, Object> liveReadArguments = Map.of("uri", tableResourceUri);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final ArgumentCaptor<List<LLMChatMessage>> actualMessages = createChatMessagesCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", MCPInteractionActionNames.READ_RESOURCE, staleReadArguments, "stale-response"),
                createToolCallCompletion("tool-2", MCPInteractionActionNames.READ_RESOURCE, liveReadArguments, "live-response"),
                createToolCallCompletion("tool-3", "database_gateway_execute_query", executeQueryArguments, "query-response"));
        when(mcpInteractionClient.readResource(staleResourceUri)).thenReturn(Map.of(
                "found", false,
                "next_actions", List.of(Map.of(
                        "type", "resource_read",
                        "resource_uri", "shardingsphere://databases/unknown/schemas/unknown/tables",
                        "requires_user_approval", false))));
        when(mcpInteractionClient.readResource(tableResourceUri)).thenReturn(Map.of("found", true));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        verify(llmChatClient, times(3)).complete(actualMessages.capture(), anyList(), eq("required"), eq(false));
        assertTrue(containsMessage(actualMessages.getAllValues().get(1), "Use exactly `" + tableResourceUri + "` as uri"));
        assertThat(actual.getInteractionTrace().get(1).getArguments().get("uri"), CoreMatchers.is(tableResourceUri));
    }
    
    @Test
    void assertRunFollowsPendingNextActionBeforeFinalAnswer() throws IOException, InterruptedException {
        final List<String> toolNames = List.of("database_gateway_apply_workflow", "database_gateway_execute_query");
        final LLME2EScenario actualScenario = createScenario(toolNames);
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final Map<String, Object> previewArguments = Map.of("plan_id", "plan-1", "execution_mode", "preview");
        final Map<String, Object> manualArguments = Map.of("plan_id", "plan-1", "execution_mode", "manual-only");
        final ArgumentCaptor<List<LLMChatMessage>> actualMessages = createChatMessagesCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", "database_gateway_execute_query", JsonUtils.toJsonString(executeQueryArguments)),
                        new LLMToolCall("tool-2", "database_gateway_apply_workflow", JsonUtils.toJsonString(previewArguments))),
                "initial-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(mcpInteractionClient.call("database_gateway_apply_workflow", previewArguments)).thenReturn(Map.of("response_mode", "preview", "next_actions", List.of(Map.of(
                "type", "tool_call",
                "tool_name", "database_gateway_apply_workflow",
                "arguments", manualArguments,
                "requires_user_approval", false))));
        when(llmChatClient.complete(anyList(), anyList(), eq("auto"), eq(false))).thenReturn(
                createToolCallCompletion("tool-3", "database_gateway_apply_workflow", manualArguments, "manual-response"));
        when(mcpInteractionClient.call("database_gateway_apply_workflow", manualArguments)).thenReturn(Map.of("response_mode", "manual_only"));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query", "database_gateway_apply_workflow"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        verify(llmChatClient).complete(actualMessages.capture(), anyList(), eq("auto"), eq(false));
        assertTrue(containsMessage(actualMessages.getValue(), "Call `database_gateway_apply_workflow` now with exactly these arguments"));
    }
    
    @Test
    void assertRunIgnoresApprovalRequiredNextActionBeforeReadOnlyVerification() throws IOException, InterruptedException {
        final List<String> toolNames = List.of("database_gateway_execute_update", "database_gateway_execute_query");
        final LLME2EScenario actualScenario = createScenario(toolNames);
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> previewArguments = Map.of(
                "database", DATABASE_NAME,
                "schema", SCHEMA_NAME,
                "sql", "UPDATE orders SET status = status WHERE order_id = -1",
                "execution_mode", "preview");
        final Map<String, Object> executeQueryArguments = Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "sql", QUERY);
        final Map<String, Object> misroutedQueryArguments = Map.of(
                "database", DATABASE_NAME,
                "schema", SCHEMA_NAME,
                "sql", QUERY,
                "execution_mode", "preview");
        final ArgumentCaptor<List<LLMChatMessage>> actualMessages = createChatMessagesCaptor();
        final ArgumentCaptor<List<Map<String, Object>>> actualTools = createToolDefinitionsCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_update", previewArguments, "preview-response"),
                createToolCallCompletion("tool-2", "database_gateway_execute_update", misroutedQueryArguments, "query-response"));
        when(mcpInteractionClient.call("database_gateway_execute_update", previewArguments)).thenReturn(Map.of("response_mode", "preview", "next_actions", List.of(Map.of(
                "type", "tool_call",
                "tool_name", "database_gateway_execute_update",
                "arguments", Map.of("database", DATABASE_NAME, "sql", "UPDATE orders SET status = status WHERE order_id = -1", "execution_mode", "execute", "approved_by_user", true),
                "requires_user_approval", true))));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        verify(llmChatClient, times(2)).complete(actualMessages.capture(), actualTools.capture(), eq("required"), eq(false));
        final List<LLMChatMessage> actualSecondTurnMessages = actualMessages.getAllValues().get(1);
        assertTrue(containsMessage(actualSecondTurnMessages, "do not execute them in this score lane"));
        assertFalse(actualSecondTurnMessages.stream().map(LLMChatMessage::getContent).anyMatch(each -> each.contains("approved_by_user")));
        assertThat(getToolNames(actualTools.getAllValues().get(1)), CoreMatchers.is(List.of("database_gateway_execute_query")));
        assertThat(actual.getInteractionTrace().get(1).getTargetName(), CoreMatchers.is("database_gateway_execute_query"));
        assertThat(actual.getInteractionTrace().get(1).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN));
        verify(mcpInteractionClient, never()).call("database_gateway_execute_update", misroutedQueryArguments);
    }
    
    @Test
    void assertRunRoutesReadOnlyCallAfterApprovalRequiredCallInSameCompletion() throws IOException, InterruptedException {
        final List<String> toolNames = List.of("database_gateway_execute_update", "database_gateway_execute_query");
        final LLME2EScenario actualScenario = createScenario(toolNames);
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> previewArguments = Map.of(
                "database", DATABASE_NAME,
                "schema", SCHEMA_NAME,
                "sql", "UPDATE orders SET status = status WHERE order_id = -1",
                "execution_mode", "preview");
        final Map<String, Object> executeQueryArguments = Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "sql", QUERY);
        final Map<String, Object> misroutedQueryArguments = Map.of(
                "database", DATABASE_NAME,
                "schema", SCHEMA_NAME,
                "sql", QUERY,
                "execution_mode", "preview");
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", "database_gateway_execute_update", JsonUtils.toJsonString(previewArguments)),
                        new LLMToolCall("tool-2", "database_gateway_execute_update", JsonUtils.toJsonString(misroutedQueryArguments))),
                "compound-tool-response"));
        when(mcpInteractionClient.call("database_gateway_execute_update", previewArguments)).thenReturn(Map.of("response_mode", "preview", "next_actions", List.of(Map.of(
                "type", "tool_call",
                "tool_name", "database_gateway_execute_update",
                "arguments", Map.of("database", DATABASE_NAME, "sql", "UPDATE orders SET status = status WHERE order_id = -1", "execution_mode", "execute", "approved_by_user", true),
                "requires_user_approval", true))));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().get(0).getTargetName(), CoreMatchers.is("database_gateway_execute_update"));
        assertThat(actual.getInteractionTrace().get(1).getTargetName(), CoreMatchers.is("database_gateway_execute_query"));
        assertThat(actual.getInteractionTrace().get(1).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN));
        verify(mcpInteractionClient, never()).call("database_gateway_execute_update", misroutedQueryArguments);
    }
    
    @Test
    void assertRunCompactsManualArtifactsBeforeReadOnlyVerification() throws IOException, InterruptedException {
        final List<String> toolNames = List.of("database_gateway_apply_workflow", "database_gateway_execute_query");
        final LLME2EScenario actualScenario = createScenario(toolNames);
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> manualArguments = Map.of("plan_id", "plan-1", "execution_mode", "manual-only");
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final ArgumentCaptor<List<LLMChatMessage>> actualMessages = createChatMessagesCaptor();
        final ArgumentCaptor<List<Map<String, Object>>> actualTools = createToolDefinitionsCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_apply_workflow", manualArguments, "manual-response"),
                createToolCallCompletion("tool-2", "database_gateway_execute_query", executeQueryArguments, "query-response"));
        when(mcpInteractionClient.call("database_gateway_apply_workflow", manualArguments)).thenReturn(Map.of(
                "response_mode", "manual_only",
                "manual_artifact_summary", Map.of("distsql_artifact_count", 1),
                "manual_artifacts", List.of(Map.of("distsql_artifacts", List.of(Map.of("sql", "CREATE MASK RULE orders SECRET")))),
                "next_actions", List.of(Map.of(
                        "type", "ask_user",
                        "requires_user_approval", true,
                        "reason", "Confirm manual artifacts were executed.")),
                "requires_user_approval", true));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        verify(llmChatClient, times(2)).complete(actualMessages.capture(), actualTools.capture(), eq("required"), eq(false));
        final List<LLMChatMessage> actualSecondTurnMessages = actualMessages.getAllValues().get(1);
        assertTrue(containsMessage(actualSecondTurnMessages, "\"distsql_artifact_count\":1"));
        assertFalse(actualSecondTurnMessages.stream().map(LLMChatMessage::getContent).anyMatch(each -> each.contains("CREATE MASK RULE orders SECRET")));
        assertThat(getToolNames(actualTools.getAllValues().get(1)), CoreMatchers.is(List.of("database_gateway_execute_query")));
    }
    
    @Test
    void assertRunWithExecuteQueryErrorPayloadIgnoredForCoverage() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"),
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(Map.of("error_code", "tool_failed"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(llmChatClient, times(2)).complete(anyList(), anyList(), eq("required"), eq(false));
    }
    
    @Test
    void assertRunWithInvalidFinalJson() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                new LLMChatCompletion("not-json", List.of(), "invalid-final-response-1"),
                new LLMChatCompletion("still-not-json", List.of(), "invalid-final-response-2"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("invalid_final_json"));
        assertThat(actual.getRawModelOutputs(), CoreMatchers.is(List.of("tool-call-response", "invalid-final-response-1", "invalid-final-response-2")));
        verify(mcpInteractionClient).open();
        verify(mcpInteractionClient).close();
    }
    
    @Test
    void assertRunWithMcpActionFailure() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("database_gateway_execute_query", executeQueryArguments)).thenThrow(new IOException("boom"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("mcp_runtime_unavailable"));
    }
    
    @Test
    void assertRunWithMcpRuntimeUnavailable() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        doThrow(new IOException("boom")).when(mcpInteractionClient).open();
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("mcp_runtime_unavailable"));
        verify(mcpInteractionClient).open();
        verify(mcpInteractionClient).close();
    }
    
    @Test
    void assertRunWithModelRequestIOException() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenThrow(new IOException("http 500"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("model_service_unavailable"));
        verify(mcpInteractionClient).open();
        verify(mcpInteractionClient).close();
    }
    
    @Test
    void assertRunWithInterruptedConversation() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenThrow(new InterruptedException("boom"));
        try {
            final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
            
            assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("model_service_unavailable"));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }
    
    @Test
    void assertRunWithModelServiceUnavailable() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        doThrow(new IllegalStateException("Model service is not ready for `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.")).when(llmChatClient).waitUntilReady();
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("model_service_unavailable"));
        verify(mcpInteractionClient, never()).open();
        verify(mcpInteractionClient).close();
    }
    
    @Test
    void assertRunIgnoresCloseIOException() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response"));
        doThrow(new IOException("close failed")).when(mcpInteractionClient).close();
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(mcpInteractionClient).close();
    }
    
    @Test
    void assertRunRestoresInterruptOnCloseFailure() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response"));
        doThrow(new InterruptedException("close interrupted")).when(mcpInteractionClient).close();
        try {
            final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
            
            assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }
    
    private LLMMCPConversationRunner createRunner(final int maxTurns) {
        return new LLMMCPConversationRunner(maxTurns, llmChatClient, mcpInteractionClient);
    }
    
    private LLME2EScenario createScenario(final List<String> toolNames) {
        return createScenario(toolNames, "user-prompt");
    }
    
    private LLME2EScenario createScenario(final List<String> toolNames, final String userPrompt) {
        LLMStructuredAnswer expectedAnswer = new LLMStructuredAnswer(DATABASE_NAME, SCHEMA_NAME, TABLE_NAME, QUERY, 2, toolNames);
        return new LLME2EScenario("scenario-id", "system-prompt", userPrompt, expectedAnswer, toolNames, toolNames);
    }
    
    private Map<String, Object> createExecuteQueryArguments(final String sql) {
        return Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "sql", sql, "max_rows", 10);
    }
    
    private LLMChatCompletion createToolCallCompletion(final String toolCallId, final String toolName, final Map<String, Object> arguments, final String rawResponse) {
        return new LLMChatCompletion("", List.of(new LLMToolCall(toolCallId, toolName, JsonUtils.toJsonString(arguments))), rawResponse);
    }
    
    private LLMChatCompletion createFinalAnswerCompletion(final List<String> interactionSequence, final Object totalOrders, final String rawResponse) {
        return new LLMChatCompletion(JsonUtils.toJsonString(createFinalAnswerPayload(interactionSequence, totalOrders)), List.of(), rawResponse);
    }
    
    private boolean containsMessage(final List<LLMChatMessage> messages, final String expectedContent) {
        return messages.stream().map(LLMChatMessage::getContent).anyMatch(each -> each.contains(expectedContent));
    }
    
    private Map<String, Object> createResultSetPayload(final Object totalOrders) {
        return Map.of("result_kind", "result_set", "rows", List.of(List.of(totalOrders)));
    }
    
    private Map<String, Object> createFinalAnswerPayload(final List<String> interactionSequence, final Object totalOrders) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("database", DATABASE_NAME);
        result.put("schema", SCHEMA_NAME);
        result.put("table", TABLE_NAME);
        result.put("query", QUERY);
        result.put("totalOrders", totalOrders);
        result.put("interactionSequence", interactionSequence);
        return result;
    }
    
    private static Map<String, Object> createMutatedFinalAnswerPayload(final String fieldName, final Object fieldValue) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("database", DATABASE_NAME);
        result.put("schema", SCHEMA_NAME);
        result.put("table", TABLE_NAME);
        result.put("query", QUERY);
        result.put("totalOrders", 2);
        result.put("interactionSequence", List.of("database_gateway_execute_query"));
        result.put(fieldName, fieldValue);
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private ArgumentCaptor<List<Map<String, Object>>> createToolDefinitionsCaptor() {
        return ArgumentCaptor.forClass((Class) List.class);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private ArgumentCaptor<List<LLMChatMessage>> createChatMessagesCaptor() {
        return ArgumentCaptor.forClass((Class) List.class);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getFunction(final Map<String, Object> toolDefinition) {
        return (Map<String, Object>) toolDefinition.get("function");
    }
    
    private String getToolName(final Map<String, Object> toolDefinition) {
        return String.valueOf(getFunction(toolDefinition).get("name"));
    }
    
    private List<String> getToolNames(final List<Map<String, Object>> toolDefinitions) {
        return toolDefinitions.stream().map(this::getToolName).toList();
    }
    
    private List<String> getRequiredFields(final Map<String, Object> toolDefinition) {
        return castToStringList(getMap(getFunction(toolDefinition).get("parameters")).get("required"));
    }
    
    private String getPropertyType(final Map<String, Object> propertyDefinition) {
        return String.valueOf(propertyDefinition.get("type"));
    }
    
    private String getPropertyType(final Map<String, Object> toolDefinition, final String propertyName) {
        return getPropertyType(getPropertyDefinition(toolDefinition, propertyName));
    }
    
    private String getNestedPropertyType(final Map<String, Object> toolDefinition, final String propertyName, final String nestedPropertyName) {
        return getPropertyType(getMap(getPropertyDefinition(toolDefinition, propertyName).get(nestedPropertyName)));
    }
    
    private Map<String, Object> getPropertyDefinition(final Map<String, Object> toolDefinition, final String propertyName) {
        return getMap(getProperty(toolDefinition, propertyName));
    }
    
    private Object getProperty(final Map<String, Object> toolDefinition, final String propertyName) {
        return getMap(getMap(getFunction(toolDefinition).get("parameters")).get("properties")).get(propertyName);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(final Object value) {
        return (Map<String, Object>) value;
    }
    
    @SuppressWarnings("unchecked")
    private List<String> castToStringList(final Object value) {
        return (List<String>) value;
    }
}
