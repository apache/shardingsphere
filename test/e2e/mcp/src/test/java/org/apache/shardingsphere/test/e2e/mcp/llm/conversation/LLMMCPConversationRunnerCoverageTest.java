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

import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatCompletion;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatMessage;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LLMMCPConversationRunnerCoverageTest extends AbstractLLMMCPConversationRunnerTest {
    
    @Test
    void assertRunWithNonNumericExecuteQueryTrace() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload("NaN"));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("unexpected_query_result"));
    }
    
    @Test
    void assertRunWithNonResultSetExecuteQueryTrace() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "tool-call-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(Map.of("result_kind", "update_count", "update_count", 1));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("unexpected_query_result"));
    }
    
    @Test
    void assertRunWithMissingRequiredToolCoverage() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(1);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(0));
        verify(getMCPInteractionClient()).open();
        verify(getMCPInteractionClient()).close();
    }
    
    @Test
    void assertRunNamesRemainingToolsWhenCoverageIsMissing() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final ArgumentCaptor<List<LLMChatMessage>> actualMessages = createChatMessagesCaptor();
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response-1"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(getLLMChatClient(), times(2)).complete(actualMessages.capture(), anyList(), eq("required"), eq(false));
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
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response-1"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(getLLMChatClient(), times(2)).complete(actualMessages.capture(), anyList(), eq("required"), eq(false));
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
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response-1"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(getLLMChatClient(), times(2)).complete(actualMessages.capture(), anyList(), eq("required"), eq(false));
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
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response-1"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(getLLMChatClient(), times(2)).complete(actualMessages.capture(), anyList(), eq("required"), eq(false));
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
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_plan_mask_rule", rawPlanArguments, "plan-response"));
        when(getMCPInteractionClient().call("database_gateway_plan_mask_rule", expectedPlanArguments)).thenReturn(Map.of("plan_id", "plan-1"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        assertThat(actual.getInteractionTrace().get(0).getArguments(), CoreMatchers.is(expectedPlanArguments));
        assertThat(actual.getInteractionTrace().get(0).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN));
        verify(getMCPInteractionClient()).call("database_gateway_plan_mask_rule", expectedPlanArguments);
    }
    
    @Test
    void assertRunKeepsCurrentPlanningPlanId() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_plan_mask_rule"));
        final LLMMCPConversationRunner actualRunner = createRunner(2);
        final Map<String, Object> initialPlanArguments = Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "table", TABLE_NAME, "column", "status");
        final Map<String, Object> currentPlanArguments = Map.of("plan_id", "plan-1", "database", DATABASE_NAME, "schema", SCHEMA_NAME, "table", TABLE_NAME, "column", "status");
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_plan_mask_rule", initialPlanArguments, "initial-plan-response"));
        when(getLLMChatClient().complete(anyList(), anyList(), eq("auto"), eq(false))).thenReturn(
                createToolCallCompletion("tool-2", "database_gateway_plan_mask_rule", currentPlanArguments, "current-plan-response"));
        when(getMCPInteractionClient().call("database_gateway_plan_mask_rule", initialPlanArguments)).thenReturn(Map.of("plan_id", "plan-1", "next_actions", List.of(Map.of(
                "type", "tool_call",
                "tool_name", "database_gateway_plan_mask_rule",
                "arguments", currentPlanArguments))));
        when(getMCPInteractionClient().call("database_gateway_plan_mask_rule", currentPlanArguments)).thenReturn(Map.of("plan_id", "plan-1"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        assertThat(actual.getInteractionTrace().get(1).getArguments(), CoreMatchers.is(currentPlanArguments));
        assertThat(actual.getInteractionTrace().get(1).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN));
        verify(getMCPInteractionClient()).call("database_gateway_plan_mask_rule", currentPlanArguments);
    }
    
    @Test
    void assertRunRequiresToolCallWhenExpectedExecuteQueryIsReturnedAsText() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_execute_query"));
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> executeQueryArguments = createExecuteQueryArguments(QUERY);
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                new LLMChatCompletion(QUERY, List.of(), "query-text-response"),
                createToolCallCompletion("tool-1", "database_gateway_execute_query", executeQueryArguments, "query-tool-response"));
        when(getMCPInteractionClient().call("database_gateway_execute_query", executeQueryArguments)).thenReturn(createResultSetPayload(2));
        when(getLLMChatClient().complete(anyList(), eq(List.of()), eq("none"), eq(true))).thenReturn(
                createFinalAnswerCompletion(List.of("database_gateway_execute_query"), 2, "final-answer-response"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertTrue(actual.getAssertionReport().isSuccess());
        assertThat(actual.getInteractionTrace().size(), CoreMatchers.is(1));
        assertThat(actual.getInteractionTrace().get(0).getTargetName(), CoreMatchers.is("database_gateway_execute_query"));
        assertThat(actual.getInteractionTrace().get(0).getActionOrigin(), CoreMatchers.is(MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN));
        verify(getMCPInteractionClient()).call("database_gateway_execute_query", executeQueryArguments);
        verify(getLLMChatClient(), times(2)).complete(anyList(), anyList(), eq("required"), eq(false));
    }
    
    @Test
    void assertRunNamesLatestPlanIdWhenWorkflowCoverageIsMissing() throws IOException, InterruptedException {
        final LLME2EScenario actualScenario = createScenario(List.of("database_gateway_plan_mask_rule", "database_gateway_apply_workflow"));
        final LLMMCPConversationRunner actualRunner = createRunner(3);
        final Map<String, Object> planArguments = Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "table", TABLE_NAME, "column", "status");
        final ArgumentCaptor<List<LLMChatMessage>> actualMessages = createChatMessagesCaptor();
        when(getLLMChatClient().complete(anyList(), anyList(), eq("required"), eq(false))).thenReturn(
                createToolCallCompletion("tool-1", "database_gateway_plan_mask_rule", planArguments, "plan-response"),
                new LLMChatCompletion("I already know the answer.", List.of(), "direct-answer-response-1"));
        when(getMCPInteractionClient().call("database_gateway_plan_mask_rule", planArguments)).thenReturn(Map.of("plan_id", "plan-1"));
        
        final LLME2EArtifactBundle actual = actualRunner.run(actualScenario);
        
        assertThat(actual.getAssertionReport().getFailureType(), CoreMatchers.is("missing_required_tool_coverage"));
        verify(getLLMChatClient(), times(3)).complete(actualMessages.capture(), anyList(), eq("required"), eq(false));
        final List<LLMChatMessage> actualThirdTurnMessages = actualMessages.getAllValues().get(2);
        final String actualRetryInstruction = actualThirdTurnMessages.get(actualThirdTurnMessages.size() - 1).getContent();
        assertThat(actualRetryInstruction, CoreMatchers.containsString("Remaining required MCP tools: database_gateway_apply_workflow"));
        assertThat(actualRetryInstruction, CoreMatchers.containsString("set plan_id `plan-1`"));
        assertThat(actualRetryInstruction, CoreMatchers.containsString("do not use placeholder text `plan_id`"));
    }
}
