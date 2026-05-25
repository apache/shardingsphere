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
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LLMMCPConversationRunnerTest extends AbstractLLMMCPConversationRunnerTest {
    
    @Test
    void assertRunWithExecuteQuery() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final ArgumentCaptor<List<Map<String, Object>>> actualTools = createToolDefinitionsCaptor();
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(1));
        assertThat(actual.getInteractionTrace().get(0).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN));
        assertThat(actual.getRawModelOutputs(), CoreMatchers.is(List.of("tool-call-response", "final-answer-response")));
        verify(getLLMChatClient()).complete(anyList(), actualTools.capture(), eq("required"), eq(false));
        verify(getMCPInteractionClient()).open();
        verify(getMCPInteractionClient()).call("database_gateway_execute_query", executeQueryArguments);
        verify(getMCPInteractionClient()).close();
        assertThat(getToolName(actualTools.getValue().get(0)), CoreMatchers.is("database_gateway_execute_query"));
        assertThat(getMap(getFunction(actualTools.getValue().get(0)).get("parameters")).get("required"), CoreMatchers.is(List.of("database", "sql")));
    }
    
    @Test
    void assertRunNormalizesMissingExecuteQuerySchema() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> modelQueryArguments = Map.of("database", DATABASE_NAME, "sql", QUERY, "max_rows", 10);
        final Map<String, Object> expectedQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", modelQueryArguments, "tool-call-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", expectedQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().get(0).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN));
        assertThat(actual.getInteractionTrace().get(0).getArguments(), CoreMatchers.is(expectedQueryArguments));
        verify(getMCPInteractionClient()).call("database_gateway_execute_query", expectedQueryArguments);
        verify(getLLMChatClient(), never()).complete(anyList(), anyList(), eq("auto"), eq(false));
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
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", "database_gateway_search_metadata", JsonUtils.toJsonString(modelSearchArguments)),
                        new LLMToolCall("tool-2", "database_gateway_execute_query", JsonUtils.toJsonString(executeQueryArguments))),
                "tool-call-response"));
        when(getMCPInteractionClient().call("database_gateway_search_metadata", expectedSearchArguments)).thenReturn(
                Map.of("items", List.of(Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "table", TABLE_NAME))));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().get(0).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN));
        assertThat(actual.getInteractionTrace().get(0).getArguments(), CoreMatchers.is(expectedSearchArguments));
        verify(getMCPInteractionClient()).call("database_gateway_search_metadata", expectedSearchArguments);
    }
    
    @Test
    void assertRunNormalizesSchemaQualifiedExecuteQuerySchema() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final String actualQuery = "SELECT COUNT(*) AS total_orders FROM public.orders";
        final Map<String, Object> modelQueryArguments = Map.of("database", DATABASE_NAME, "sql", actualQuery);
        final Map<String, Object> expectedQueryArguments = Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "sql", actualQuery);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", modelQueryArguments, "tool-call-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", expectedQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().get(0).getArguments(), CoreMatchers.is(expectedQueryArguments));
        verify(getLLMChatClient(), never()).complete(anyList(), anyList(), eq("auto"), eq(false));
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
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", "database_gateway_execute_query", JsonUtils.toJsonString(executeQueryArguments)),
                        new LLMToolCall("tool-2", "database_gateway_execute_update", JsonUtils.toJsonString(executeUpdateArguments))),
                "tool-call-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getMCPInteractionClient().call("database_gateway_execute_update", executeUpdateArguments)).thenReturn(Map.of("result_kind", "preview"));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        verify(getLLMChatClient()).complete(actualFinalMessages.capture(), eq(List.of()), eq("none"), eq(true));
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
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", MCPInteractionActionNames.LIST_RESOURCES, "{}"),
                        new LLMToolCall("tool-2", MCPInteractionActionNames.READ_RESOURCE, JsonUtils.toJsonString(Map.of("uri", RESOURCE_URI))),
                        new LLMToolCall("tool-3", "database_gateway_execute_query", JsonUtils.toJsonString(executeQueryArguments))),
                "tool-call-response"));
        when(getMCPInteractionClient().listResources()).thenReturn(Map.of("resources", List.of(Map.of("uri", RESOURCE_URI))));
        when(getMCPInteractionClient().readResource(RESOURCE_URI)).thenReturn(Map.of("supportedTools", List.of("database_gateway_search_metadata", "database_gateway_execute_query")));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload("2"));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(actualToolNames, "2", "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(3));
        assertThat(actual.getInteractionTrace().get(0).getActionKind(), CoreMatchers.is(MCPInteractionActionNames.RESOURCE_LIST_KIND));
        assertThat(actual.getInteractionTrace().get(0).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.PROTOCOL_BRIDGE_ORIGIN));
        assertThat(actual.getInteractionTrace().get(1).getActionKind(), CoreMatchers.is(MCPInteractionActionNames.RESOURCE_READ_KIND));
        assertThat(actual.getInteractionTrace().get(2).getTargetName(), CoreMatchers.is("database_gateway_execute_query"));
        assertThat(actual.getInteractionTrace().get(2).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN));
        verify(getMCPInteractionClient()).listResources();
        verify(getMCPInteractionClient()).readResource(RESOURCE_URI);
        verify(getMCPInteractionClient()).call("database_gateway_execute_query", executeQueryArguments);
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
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", MCPInteractionActionNames.READ_RESOURCE, JsonUtils.toJsonString(Map.of("uri", "shardingsphere://features/mask/algorithms"))),
                        new LLMToolCall("tool-2", "database_gateway_plan_mask_rule", JsonUtils.toJsonString(planArguments)),
                        new LLMToolCall("tool-3", MCPInteractionActionNames.READ_RESOURCE, JsonUtils.toJsonString(Map.of("uri", "shardingsphere://runtime"))),
                        new LLMToolCall("tool-4", MCPInteractionActionNames.READ_RESOURCE, JsonUtils.toJsonString(Map.of("uri", workflowResourceUri))),
                        new LLMToolCall("tool-5", "database_gateway_apply_workflow", JsonUtils.toJsonString(applyArguments)),
                        new LLMToolCall("tool-6", "database_gateway_validate_workflow", JsonUtils.toJsonString(validateArguments)),
                        new LLMToolCall("tool-7", "database_gateway_execute_query", JsonUtils.toJsonString(executeQueryArguments))),
                "tool-call-response"));
        when(getMCPInteractionClient().readResource("shardingsphere://features/mask/algorithms")).thenReturn(Map.of("algorithms", List.of(Map.of("type", "MD5"))));
        when(getMCPInteractionClient().call("database_gateway_plan_mask_rule", planArguments))
                .thenReturn(Map.of("plan_id", "plan-1", "resources_to_read", List.of(Map.of("uri", workflowResourceUri))));
        when(getMCPInteractionClient().readResource("shardingsphere://runtime")).thenReturn(Map.of("status", "available", "capability_fingerprint", "abc"));
        when(getMCPInteractionClient().readResource(workflowResourceUri)).thenReturn(Map.of("plan_id", "plan-1", "status", "planned"));
        when(getMCPInteractionClient().call("database_gateway_apply_workflow", applyArguments)).thenReturn(Map.of("status", "awaiting-manual-execution", "response_mode", "manual_only"));
        when(getMCPInteractionClient().call("database_gateway_validate_workflow", validateArguments)).thenReturn(Map.of("status", "passed", "overall_status", "passed"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of(
                        MCPInteractionActionNames.READ_RESOURCE, "database_gateway_plan_mask_rule", MCPInteractionActionNames.READ_RESOURCE, "database_gateway_apply_workflow",
                        "database_gateway_validate_workflow", "database_gateway_execute_query"), 2,
                        "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(7));
        verify(getMCPInteractionClient()).readResource(workflowResourceUri);
        verify(getMCPInteractionClient()).call("database_gateway_apply_workflow", applyArguments);
        verify(getMCPInteractionClient()).call("database_gateway_validate_workflow", validateArguments);
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
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
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
        when(getMCPInteractionClient().listPrompts()).thenReturn(Map.of("prompts", List.of(Map.of("name", PROMPT_NAME))));
        when(getMCPInteractionClient().getPrompt(PROMPT_NAME, promptArguments)).thenReturn(Map.of("description", "Inspect metadata", "messages", List.of()));
        when(getMCPInteractionClient().complete(completionReference, "schema", "pub", completionContext)).thenReturn(Map.of("completion", Map.of("values", List.of("public"))));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(actualToolNames, 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(4));
        assertThat(actual.getInteractionTrace().get(0).getActionKind(), CoreMatchers.is(MCPInteractionActionNames.PROMPT_LIST_KIND));
        assertThat(actual.getInteractionTrace().get(1).getActionKind(), CoreMatchers.is(MCPInteractionActionNames.PROMPT_GET_KIND));
        assertThat(actual.getInteractionTrace().get(2).getActionKind(), CoreMatchers.is(MCPInteractionActionNames.COMPLETION_KIND));
        verify(getLLMChatClient()).complete(anyList(), actualTools.capture(), eq("required"), eq(false));
        verify(getMCPInteractionClient()).listPrompts();
        verify(getMCPInteractionClient()).getPrompt(PROMPT_NAME, promptArguments);
        verify(getMCPInteractionClient()).complete(completionReference, "schema", "pub", completionContext);
        assertThat(getToolName(actualTools.getValue().get(0)), CoreMatchers.is(MCPInteractionActionNames.LIST_PROMPTS));
    }
    
    @Test
    void assertRunWithSearchMetadataToolDefinition() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_search_metadata"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        final ArgumentCaptor<List<Map<String, Object>>> actualTools = createToolDefinitionsCaptor();
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(getLLMChatClient()).complete(anyList(), actualTools.capture(), eq("required"), eq(false));
        assertThat(getToolName(actualTools.getValue().get(0)), CoreMatchers.is("database_gateway_search_metadata"));
        assertThat(getRequiredFields(actualTools.getValue().get(0)), CoreMatchers.is(List.of()));
        assertThat(getPropertyType(actualTools.getValue().get(0), "database"), CoreMatchers.is("string"));
        assertThat(getPropertyType(actualTools.getValue().get(0), "schema"), CoreMatchers.is("string"));
        assertThat(getPropertyType(actualTools.getValue().get(0), "query"), CoreMatchers.is("string"));
        assertThat(getPropertyType(actualTools.getValue().get(0), "object_types"), CoreMatchers.is("array"));
        assertThat(getNestedPropertyType(actualTools.getValue().get(0), "object_types", "items"), CoreMatchers.is("string"));
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
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_search_metadata", searchArguments, "search-response"),
                createToolCallCompletion("tool-2", MCPInteractionActionNames.READ_RESOURCE, readResourceArguments, "resource-response"),
                createToolCallCompletion("tool-3", "database_gateway_execute_query", executeQueryArguments, "query-response"));
        when(getMCPInteractionClient().call("database_gateway_search_metadata", searchArguments)).thenReturn(Map.of("items", List.of(Map.of("table", TABLE_NAME))));
        when(getMCPInteractionClient().readResource(tableResourceUri)).thenReturn(Map.of("table", TABLE_NAME));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(createFinalAnswerCompletion(toolNames, 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        verify(getLLMChatClient(), times(3)).complete(anyList(), actualTools.capture(), eq("required"), eq(false));
        assertThat(getToolNames(actualTools.getAllValues().get(0)), CoreMatchers.is(List.of("database_gateway_search_metadata")));
        assertThat(getToolNames(actualTools.getAllValues().get(1)), CoreMatchers.is(List.of(MCPInteractionActionNames.READ_RESOURCE)));
        assertThat(getToolNames(actualTools.getAllValues().get(2)), CoreMatchers.is(List.of("database_gateway_execute_query")));
    }
    
    @Test
    void assertRunAcceptsObjectInteractionSequence() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final Map<String, Object> finalAnswerPayload = createFinalAnswerPayload(List.of(), 2);
        finalAnswerPayload.put("interactionSequence", List.of(Map.of("tool", "database_gateway_execute_query", "arguments", executeQueryArguments)));
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
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
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", "database_gateway_search_metadata", JsonUtils.toJsonString(searchMetadataArguments)),
                        new LLMToolCall("tool-2", "database_gateway_search_metadata", JsonUtils.toJsonString(searchMetadataArguments)),
                        new LLMToolCall("tool-3", "database_gateway_execute_query", JsonUtils.toJsonString(executeQueryArguments))),
                "tool-call-response"));
        when(getMCPInteractionClient().call("database_gateway_search_metadata", searchMetadataArguments)).thenReturn(Map.of("items", List.of()));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
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
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                new LLMChatCompletion(JsonUtils.toJsonString(finalAnswerPayload), List.of(), "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
    }
}
