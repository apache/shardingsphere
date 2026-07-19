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

import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.AutonomousMCPBuilderEvaluationRunner.EvaluationResult;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatCompletion;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMToolCall;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.MCPBuilderEvaluationCatalog.EvaluationCase;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutonomousMCPBuilderEvaluationRunnerTest {
    
    private static final List<String> REQUIRED_TOOL_NAMES = List.of(
            "database_gateway_search_metadata",
            "database_gateway_validate_runtime_database",
            "database_gateway_execute_query",
            "database_gateway_execute_explain_query");
    
    @Test
    void assertAutonomousMCPAnswer() throws IOException, InterruptedException {
        LLMChatModelClient modelClient = mock(LLMChatModelClient.class);
        when(modelClient.complete(anyList(), anyList(), eq("auto"), eq(false))).thenReturn(
                new LLMChatCompletion("", List.of(new LLMToolCall("call-1", "database_gateway_execute_query",
                        "{\"database\":\"logic_db\",\"sql\":\"SELECT COUNT(*) FROM orders\"}")), "raw-tool-call"),
                new LLMChatCompletion("2", List.of(), "raw-answer"));
        MCPInteractionClient interactionClient = mock(MCPInteractionClient.class);
        when(interactionClient.listTools()).thenReturn(createAdvertisedTools());
        when(interactionClient.call("database_gateway_execute_query", Map.of("database", "logic_db", "sql", "SELECT COUNT(*) FROM orders")))
                .thenReturn(Map.of("columns", List.of("COUNT(*)"), "rows", List.of(List.of(2))));
        EvaluationResult actual = new AutonomousMCPBuilderEvaluationRunner(8, modelClient, interactionClient, "provider", "model")
                .run(new EvaluationCase("q01", "aggregation", true, "Count orders and reply only with the integer.", "2"));
        assertTrue(actual.assertionReport().isSuccess());
        assertThat(actual.actualAnswer(), is("2"));
        assertThat(actual.evidence().interactionTrace().size(), is(2));
        assertThat(actual.evidence().interactionTrace().get(1).getActionOrigin(), is(MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN));
        assertFalse(actual.evidence().toolDefinitions().stream().anyMatch(each -> "database_gateway_execute_update".equals(
                LLMMCPJsonValues.castToMap(each.get("function")).get("name"))));
        verify(interactionClient).open();
        verify(interactionClient).close();
    }
    
    @Test
    void assertAnswerWithoutMCPEvidence() throws IOException, InterruptedException {
        LLMChatModelClient modelClient = mock(LLMChatModelClient.class);
        when(modelClient.complete(anyList(), anyList(), eq("auto"), eq(false))).thenReturn(new LLMChatCompletion("2", List.of(), "raw-answer"));
        MCPInteractionClient interactionClient = mock(MCPInteractionClient.class);
        when(interactionClient.listTools()).thenReturn(createAdvertisedTools());
        EvaluationResult actual = new AutonomousMCPBuilderEvaluationRunner(8, modelClient, interactionClient, "provider", "model")
                .run(new EvaluationCase("q01", "aggregation", true, "Count orders and reply only with the integer.", "2"));
        assertThat(actual.assertionReport().getFailureType(), is("missing_mcp_evidence"));
    }
    
    @Test
    void assertResourceEvidenceCountsAsAutonomousMCPAction() throws IOException, InterruptedException {
        LLMChatModelClient modelClient = mock(LLMChatModelClient.class);
        when(modelClient.complete(anyList(), anyList(), eq("auto"), eq(false))).thenReturn(
                new LLMChatCompletion("", List.of(new LLMToolCall("call-1", "mcp_read_resource",
                        "{\"uri\":\"shardingsphere://databases\"}")), "raw-tool-call"),
                new LLMChatCompletion("logic_db", List.of(), "raw-answer"));
        MCPInteractionClient interactionClient = mock(MCPInteractionClient.class);
        when(interactionClient.listTools()).thenReturn(createAdvertisedTools());
        when(interactionClient.readResource("shardingsphere://databases")).thenReturn(Map.of("items", List.of(Map.of("database", "logic_db"))));
        EvaluationResult actual = new AutonomousMCPBuilderEvaluationRunner(8, modelClient, interactionClient, "provider", "model")
                .run(new EvaluationCase("q01", "metadata", true, "Name the database.", "logic_db"));
        assertTrue(actual.assertionReport().isSuccess());
        assertThat(actual.evidence().interactionTrace().get(1).getActionOrigin(), is(MCPInteractionTraceRecord.PROTOCOL_BRIDGE_ORIGIN));
    }
    
    @Test
    void assertInvalidBridgeArguments() throws IOException, InterruptedException {
        LLMChatModelClient modelClient = mock(LLMChatModelClient.class);
        when(modelClient.complete(anyList(), anyList(), eq("auto"), eq(false))).thenReturn(
                new LLMChatCompletion("", List.of(new LLMToolCall("call-1", "mcp_read_resource", "{}")), "raw-tool-call"));
        MCPInteractionClient interactionClient = mock(MCPInteractionClient.class);
        when(interactionClient.listTools()).thenReturn(createAdvertisedTools());
        EvaluationResult actual = new AutonomousMCPBuilderEvaluationRunner(8, modelClient, interactionClient, "provider", "model")
                .run(new EvaluationCase("q01", "metadata", true, "Name the database.", "logic_db"));
        assertThat(actual.assertionReport().getFailureType(), is("invalid_tool_arguments"));
        assertThat(actual.evidence().interactionTrace().get(1).getStructuredContent().get("error_code"), is("invalid_tool_arguments"));
    }
    
    private List<Map<String, Object>> createAdvertisedTools() {
        return Stream.concat(REQUIRED_TOOL_NAMES.stream(), Stream.of("database_gateway_execute_update")).map(each -> Map.of(
                "name", each,
                "description", "Remote tool definition.",
                "inputSchema", Map.of("type", "object", "description", "remote-marker", "properties", Map.of()))).toList();
    }
}
