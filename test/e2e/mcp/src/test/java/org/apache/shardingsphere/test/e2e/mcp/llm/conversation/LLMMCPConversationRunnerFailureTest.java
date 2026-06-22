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
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMToolCall;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
class LLMMCPConversationRunnerFailureTest extends AbstractLLMMCPConversationRunnerTest {
    
    @Test
    void assertRunWithExecuteQueryErrorPayloadIgnoredForCoverage() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(2);
        Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"),
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(Map.of("error_code", "tool_failed"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), is("missing_required_tool_coverage"));
        verify(getLLMChatClient(), times(2)).complete(anyList(), anyList(), eq("required"), eq(false));
    }
    
    @Test
    void assertRunWithInvalidFinalJson() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(3);
        Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                new LLMChatCompletion("not-json", List.of(), "invalid-final-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), is("invalid_final_json"));
        assertThat(actual.getRawModelOutputs(), is(List.of("tool-call-response", "invalid-final-response")));
        verify(getMCPInteractionClient()).open();
        verify(getMCPInteractionClient()).close();
    }
    
    @Test
    void assertRunWithMcpActionFailure() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(1);
        Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenThrow(new IOException("boom"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), is("mcp_runtime_unavailable"));
    }
    
    @Test
    void assertRunWithMcpRuntimeUnavailable() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(1);
        doThrow(new IOException("boom")).when(getMCPInteractionClient()).open();
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), is("mcp_runtime_unavailable"));
        verify(getMCPInteractionClient()).open();
        verify(getMCPInteractionClient()).close();
    }
    
    @Test
    void assertRunWithModelRequestIOException() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(1);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenThrow(new IOException("http 500"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), is("model_service_unavailable"));
        verify(getMCPInteractionClient()).open();
        verify(getMCPInteractionClient()).close();
    }
    
    @Test
    void assertRunWithInterruptedConversation() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(1);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenThrow(new InterruptedException("boom"));
        try {
            LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
            
            assertThat(actual.getAssertionReport().getFailureType(), is("model_service_unavailable"));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }
    
    @Test
    void assertRunWithModelServiceUnavailable() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(1);
        doThrow(new IllegalStateException("Model service is not ready for `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.")).when(getLLMChatClient()).waitUntilReady();
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), is("model_service_unavailable"));
        verify(getMCPInteractionClient(), never()).open();
        verify(getMCPInteractionClient()).close();
    }
    
    @Test
    void assertRunIgnoresCloseIOException() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(1);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response"));
        doThrow(new IOException("close failed")).when(getMCPInteractionClient()).close();
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), is("missing_required_tool_coverage"));
        verify(getMCPInteractionClient()).close();
    }
    
    @Test
    void assertRunRestoresInterruptOnCloseFailure() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(1);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response"));
        doThrow(new InterruptedException("close interrupted")).when(getMCPInteractionClient()).close();
        try {
            LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
            
            assertThat(actual.getAssertionReport().getFailureType(), is("missing_required_tool_coverage"));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }
    
    @Test
    void assertRunWithUnexpectedTool() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(1);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("", List.of(new LLMToolCall("tool-1", "unsupported_tool", "{}")), "tool-call-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), is("unexpected_tool_requested"));
        assertThat(actual.getInteractionTrace().size(), is(1));
        verify(getMCPInteractionClient()).open();
        verify(getMCPInteractionClient(), never()).call(anyString(), anyMap());
        verify(getMCPInteractionClient()).close();
    }
    
    @Test
    void assertRunWithInvalidToolArgumentsJson() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(1);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("", List.of(new LLMToolCall("tool-1", "database_gateway_execute_query", "{invalid")), "tool-call-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), is("invalid_tool_arguments"));
        assertThat(actual.getInteractionTrace().getFirst().getTargetName(), is("database_gateway_execute_query"));
        verify(getMCPInteractionClient()).open();
        verify(getMCPInteractionClient(), never()).call(anyString(), anyMap());
        verify(getMCPInteractionClient()).close();
    }
    
    @Test
    void assertRunWithEmptyResourceUri() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of(MCPInteractionActionNames.READ_RESOURCE));
        LLMMCPConversationRunner actualRunner = createRunner(1);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", MCPInteractionActionNames.READ_RESOURCE, Map.of("uri", "   "), "tool-call-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), is("invalid_tool_arguments"));
        assertThat(actual.getInteractionTrace().getFirst().getActionKind(), is(MCPInteractionActionNames.RESOURCE_READ_KIND));
        verify(getMCPInteractionClient(), never()).readResource(anyString());
    }
    
    @Test
    void assertRunWithUnsafeSqlAttempted() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(1);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query",
                        Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "sql", "UPDATE orders SET status = 'DONE'"),
                        "tool-call-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), is("unsafe_sql_attempted"));
        assertThat(actual.getInteractionTrace().getFirst().getTargetName(), is("database_gateway_execute_query"));
        verify(getMCPInteractionClient(), never()).call(anyString(), anyMap());
    }
    
    @Test
    void assertRunWithUnsafeExecuteUpdateAttempted() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_update"));
        LLMMCPConversationRunner actualRunner = createRunner(1);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_update",
                        Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "sql", "UPDATE orders SET status = 'DONE'", "execution_mode", "execute"),
                        "tool-call-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), is("unsafe_sql_execution_attempted"));
        assertThat(actual.getInteractionTrace().getFirst().getTargetName(), is("database_gateway_execute_update"));
        verify(getMCPInteractionClient(), never()).call(anyString(), anyMap());
    }
    
    @Test
    void assertRunWithUnsafeWorkflowExecutionAttempted() throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_apply_workflow"));
        LLMMCPConversationRunner actualRunner = createRunner(1);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_apply_workflow", Map.of("plan_id", "plan-1", "execution_mode", "review-then-execute"), "tool-call-response"));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), is("unsafe_workflow_execution_attempted"));
        assertThat(actual.getInteractionTrace().getFirst().getTargetName(), is("database_gateway_apply_workflow"));
        verify(getMCPInteractionClient(), never()).call(anyString(), anyMap());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("unexpectedQueryResultCases")
    void assertRunWithUnexpectedQueryResult(final String caseName, final Map<String, Object> finalAnswerPayload) throws IOException, InterruptedException {
        LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        LLMMCPConversationRunner actualRunner = createRunner(2);
        Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                new LLMChatCompletion(JsonUtils.toJsonString(finalAnswerPayload), List.of(), caseName));
        
        LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), is("unexpected_query_result"));
        verify(getMCPInteractionClient()).call("database_gateway_execute_query", executeQueryArguments);
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
    
}
