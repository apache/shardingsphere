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
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMToolCall;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LLMMCPConversationRunnerTest {
    
    private static final String DATABASE_NAME = "logic_db";
    
    private static final String SCHEMA_NAME = "public";
    
    private static final String TABLE_NAME = "orders";
    
    private static final String QUERY = "SELECT COUNT(*) AS total_orders FROM orders";
    
    private static final String RESOURCE_URI = "shardingsphere://capabilities";
    
    @Mock
    private LLMChatModelClient llmChatClient;
    
    @Mock
    private MCPInteractionClient mcpInteractionClient;
    
    @Test
    void assertRunWithExecuteQuery() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        final ArgumentCaptor<List<Map<String, Object>>> actualTools = createToolDefinitionsCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "execute_query", executeQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(1));
        assertThat(actual.getRawModelOutputs(), CoreMatchers.is(List.of("tool-call-response", "final-answer-response")));
        verify(llmChatClient).complete(anyList(), actualTools.capture(), eq("required"), eq(false));
        verify(mcpInteractionClient).open();
        verify(mcpInteractionClient).call("execute_query", executeQueryArguments);
        verify(mcpInteractionClient).close();
        assertThat(getToolName(actualTools.getValue().get(0)), CoreMatchers.is("execute_query"));
        assertThat(getMap(getFunction(actualTools.getValue().get(0)).get("parameters")).get("required"), CoreMatchers.is(List.of("database", "sql")));
    }
    
    @Test
    void assertRunWithResourceBridgeSequence() throws IOException, InterruptedException {
        final List<String> actualToolNames = List.of(MCPInteractionActionNames.LIST_RESOURCES, MCPInteractionActionNames.READ_RESOURCE, "execute_query");
        final LLME2EScenario actualScenario = createScenario(actualToolNames);
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(new LLMChatCompletion("",
                List.of(
                        new LLMToolCall("tool-1", MCPInteractionActionNames.LIST_RESOURCES, "{}"),
                        new LLMToolCall("tool-2", MCPInteractionActionNames.READ_RESOURCE, JsonUtils.toJsonString(Map.of("uri", RESOURCE_URI))),
                        new LLMToolCall("tool-3", "execute_query", JsonUtils.toJsonString(executeQueryArguments))),
                "tool-call-response"));
        when(mcpInteractionClient.listResources()).thenReturn(Map.of("resources", List.of(Map.of("uri", RESOURCE_URI))));
        when(mcpInteractionClient.readResource(RESOURCE_URI)).thenReturn(Map.of("supportedTools", List.of("search_metadata", "execute_query")));
        when(mcpInteractionClient.call("execute_query", executeQueryArguments)).thenReturn(createResultSetPayload("2"));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(actualToolNames, "2", "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(3));
        assertThat(actual.getInteractionTrace().get(0).getActionKind(), CoreMatchers.is(MCPInteractionActionNames.RESOURCE_LIST_KIND));
        assertThat(actual.getInteractionTrace().get(1).getActionKind(), CoreMatchers.is(MCPInteractionActionNames.RESOURCE_READ_KIND));
        assertThat(actual.getInteractionTrace().get(2).getTargetName(), CoreMatchers.is("execute_query"));
        verify(mcpInteractionClient).listResources();
        verify(mcpInteractionClient).readResource(RESOURCE_URI);
        verify(mcpInteractionClient).call("execute_query", executeQueryArguments);
    }
    
    @Test
    void assertRunWithSearchMetadataToolDefinition() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("search_metadata"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        final ArgumentCaptor<List<Map<String, Object>>> actualTools = createToolDefinitionsCaptor();
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(llmChatClient).complete(anyList(), actualTools.capture(), eq("required"), eq(false));
        assertThat(getToolName(actualTools.getValue().get(0)), CoreMatchers.is("search_metadata"));
        assertThat(getRequiredFields(actualTools.getValue().get(0)), CoreMatchers.is(List.of("query")));
        assertThat(getPropertyType(actualTools.getValue().get(0), "object_types"), CoreMatchers.is("array"));
        assertThat(getNestedPropertyType(actualTools.getValue().get(0), "object_types", "items"), CoreMatchers.is("string"));
        assertThat(getPropertyType(actualTools.getValue().get(0), "page_size"), CoreMatchers.is("integer"));
    }
    
    @Test
    void assertRunWithUnexpectedTool() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
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
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("", List.of(new LLMToolCall("tool-1", "execute_query", "{invalid")), "tool-call-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("invalid_tool_arguments"));
        assertThat(actual.getInteractionTrace().get(0).getTargetName(), CoreMatchers.is("execute_query"));
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
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "execute_query",
                        Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "sql", "UPDATE orders SET status = 'DONE'"),
                        "tool-call-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("unsafe_sql_attempted"));
        assertThat(actual.getInteractionTrace().get(0).getTargetName(), CoreMatchers.is("execute_query"));
        verify(mcpInteractionClient, never()).call(anyString(), anyMap());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("unexpectedQueryResultCases")
    void assertRunWithUnexpectedQueryResult(final String caseName, final Map<String, Object> finalAnswerPayload) throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "execute_query", executeQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                new LLMChatCompletion(JsonUtils.toJsonString(finalAnswerPayload), List.of(), caseName));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("unexpected_query_result"));
        verify(mcpInteractionClient).call("execute_query", executeQueryArguments);
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
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "execute_query", executeQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("execute_query", executeQueryArguments)).thenReturn(createResultSetPayload("NaN"));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("unexpected_query_result"));
    }
    
    @Test
    void assertRunWithNonResultSetExecuteQueryTrace() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "execute_query", executeQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("execute_query", executeQueryArguments)).thenReturn(Map.of("result_kind", "update_count", "update_count", 1));
        when(llmChatClient.complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("unexpected_query_result"));
    }
    
    @Test
    void assertRunWithMissingRequiredToolCoverage() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
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
    void assertRunWithExecuteQueryErrorPayloadIgnoredForCoverage() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "execute_query", executeQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("execute_query", executeQueryArguments)).thenReturn(Map.of("error_code", "tool_failed"));
        when(llmChatClient.complete(anyList(), anyList(), eq("auto"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(llmChatClient).complete(anyList(), anyList(), eq("auto"), eq(false));
    }
    
    @Test
    void assertRunWithInvalidFinalJson() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "execute_query", executeQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
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
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "execute_query", executeQueryArguments, "tool-call-response"));
        when(mcpInteractionClient.call("execute_query", executeQueryArguments)).thenThrow(new IOException("boom"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("mcp_runtime_unavailable"));
    }
    
    @Test
    void assertRunWithMcpRuntimeUnavailable() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        doThrow(new IOException("boom")).when(mcpInteractionClient).open();
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("mcp_runtime_unavailable"));
        verify(mcpInteractionClient).open();
        verify(mcpInteractionClient).close();
    }
    
    @Test
    void assertRunWithModelRequestIOException() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        when(llmChatClient.complete(anyList(), anyList(), eq("required"), eq(false))).thenThrow(new IOException("http 500"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("model_service_unavailable"));
        verify(mcpInteractionClient).open();
        verify(mcpInteractionClient).close();
    }
    
    @Test
    void assertRunWithInterruptedConversation() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
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
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        doThrow(new IllegalStateException("Model service is not ready for `qwen3:1.7b`.")).when(llmChatClient).waitUntilReady();
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("model_service_unavailable"));
        verify(mcpInteractionClient, never()).open();
        verify(mcpInteractionClient).close();
    }
    
    @Test
    void assertRunIgnoresCloseIOException() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
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
        final LLME2EScenario actualScenario = createScenario(List.of("execute_query"));
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
        final LLMStructuredAnswer expectedAnswer = new LLMStructuredAnswer(DATABASE_NAME, SCHEMA_NAME, TABLE_NAME, QUERY, 2, toolNames);
        return new LLME2EScenario("scenario-id", "system-prompt", "user-prompt", expectedAnswer, toolNames, toolNames);
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
        result.put("interactionSequence", List.of("execute_query"));
        result.put(fieldName, fieldValue);
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private ArgumentCaptor<List<Map<String, Object>>> createToolDefinitionsCaptor() {
        return ArgumentCaptor.forClass((Class) List.class);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getFunction(final Map<String, Object> toolDefinition) {
        return (Map<String, Object>) toolDefinition.get("function");
    }
    
    private String getToolName(final Map<String, Object> toolDefinition) {
        return String.valueOf(getFunction(toolDefinition).get("name"));
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
