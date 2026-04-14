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

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.test.e2e.mcp.llm.artifact.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.chat.LLMChatClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.chat.LLMChatCompletion;
import org.apache.shardingsphere.test.e2e.mcp.llm.chat.LLMChatMessage;
import org.apache.shardingsphere.test.e2e.mcp.llm.chat.LLMToolCall;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.llm.stub.StubLLMChatClient;
import org.apache.shardingsphere.test.e2e.mcp.runtime.transport.client.MCPInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.runtime.transport.MCPInteractionResponse;
import org.apache.shardingsphere.test.e2e.mcp.runtime.transport.client.StubMCPInteractionClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LLMMCPConversationRunnerTest {
    
    private static final String QUERY = "SELECT COUNT(*) AS total_orders FROM orders";
    
    private static final LLME2EScenario SCENARIO = new LLME2EScenario("scenario-a", "system", "user",
            createExpectedAnswer(List.of("search_metadata", "mcp_read_resource", "execute_query")),
            List.of("search_metadata", "mcp_read_resource", "execute_query"),
            List.of("search_metadata", "mcp_read_resource", "execute_query"));
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertRunCases")
    void assertRun(final String name, final int maxTurns, final LLME2EScenario scenario, final LLMChatClient llmChatClient,
                   final MCPInteractionClient mcpInteractionClient, final int expectedInteractionCount,
                   final String expectedFirstActionKind, final String expectedLastTargetName) {
        LLME2EArtifactBundle result = new LLMMCPConversationRunner(maxTurns, llmChatClient, mcpInteractionClient).run(scenario);
        assertTrue(result.getAssertionReport().isSuccess());
        assertThat(result.getInteractionTrace().size(), is(expectedInteractionCount));
        assertThat(result.getInteractionTrace().get(0).getActionKind(), is(expectedFirstActionKind));
        assertThat(result.getInteractionTrace().get(result.getInteractionTrace().size() - 1).getTargetName(), is(expectedLastTargetName));
    }
    
    static Stream<Arguments> assertRunCases() {
        return Stream.of(
                Arguments.of("tool and resource read flow", 4, SCENARIO,
                        new StubLLMChatClient(
                                createToolCallCompletion(
                                        new LLMToolCall("call-1", "search_metadata",
                                                "{\"database\":\"logic_db\",\"schema\":\"public\",\"query\":\"orders\",\"object_types\":[\"TABLE\"]}"),
                                        new LLMToolCall("call-2", "mcp_read_resource",
                                                "{\"uri\":\"shardingsphere://databases/logic_db/schemas/public/tables/orders\"}"),
                                        new LLMToolCall("call-3", "execute_query",
                                                "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}")),
                                createFinalCompletion(createFinalAnswerJson("logic_db", "public", "orders", QUERY, 2,
                                        List.of("search_metadata", "mcp_read_resource", "execute_query")))),
                        new StubMCPInteractionClient(createSuccessfulToolResponses(),
                                Map.of("shardingsphere://databases/logic_db/schemas/public/tables/orders",
                                        new MCPInteractionResponse(Map.of("table", "orders", "columns", List.of(Map.of("column", "order_id"))), "{}"))),
                        3, "tool_call", "execute_query"),
                Arguments.of("resource read bridge", 3, createScenario("scenario-resource", List.of("mcp_read_resource", "execute_query")),
                        new StubLLMChatClient(
                                createToolCallCompletion(
                                        new LLMToolCall("call-1", "mcp_read_resource", "{\"uri\":\"shardingsphere://capabilities\"}"),
                                        new LLMToolCall("call-2", "execute_query",
                                                "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}")),
                                createFinalCompletion(createFinalAnswerJson("logic_db", "public", "orders", QUERY, 2,
                                        List.of("mcp_read_resource", "execute_query")))),
                        new StubMCPInteractionClient(Map.of("execute_query", createQueryResponse(2)),
                                Map.of("shardingsphere://capabilities",
                                        new MCPInteractionResponse(Map.of("supportedTools", List.of("execute_query")), "{}"))),
                        2, "resource_read", "execute_query"),
                Arguments.of("resource list bridge", 3, createScenario("scenario-resource-list", List.of("mcp_list_resources", "execute_query")),
                        new StubLLMChatClient(
                                createToolCallCompletion(
                                        new LLMToolCall("call-1", "mcp_list_resources", "{}"),
                                        new LLMToolCall("call-2", "execute_query",
                                                "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}")),
                                createFinalCompletion(createFinalAnswerJson("logic_db", "public", "orders", QUERY, 2,
                                        List.of("mcp_list_resources", "execute_query")))),
                        new StubMCPInteractionClient(Map.of("execute_query", createQueryResponse(2)), Map.of(),
                                new MCPInteractionResponse(Map.of("resources", List.of(Map.of("uri", "shardingsphere://capabilities"))), "{}")),
                        2, "resource_list", "execute_query"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertRejectEarlyFailureCases")
    void assertRejectEarlyFailure(final String name, final LLME2EScenario scenario, final LLMChatClient llmChatClient,
                                  final String expectedFailureType) {
        LLME2EArtifactBundle result = new LLMMCPConversationRunner(2, llmChatClient, new StubMCPInteractionClient(Map.of())).run(scenario);
        assertFalse(result.getAssertionReport().isSuccess());
        assertThat(result.getAssertionReport().getFailureType(), is(expectedFailureType));
    }
    
    static Stream<Arguments> assertRejectEarlyFailureCases() {
        return Stream.of(
                Arguments.of("unexpected tool", SCENARIO,
                        new StubLLMChatClient(createToolCallCompletion(new LLMToolCall("call-1", "drop_table", "{}"))),
                        "unexpected_tool_requested"),
                Arguments.of("unsafe sql", SCENARIO,
                        new StubLLMChatClient(createToolCallCompletion(
                                new LLMToolCall("call-1", "execute_query", "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"UPDATE orders SET status='DONE'\"}"))),
                        "unsafe_sql_attempted"),
                Arguments.of("invalid tool arguments", SCENARIO,
                        new StubLLMChatClient(createToolCallCompletion(new LLMToolCall("call-1", "search_metadata", "{not-json}"))),
                        "invalid_tool_arguments"),
                Arguments.of("empty resource uri", createScenario("scenario-empty-uri", List.of("mcp_read_resource", "execute_query")),
                        new StubLLMChatClient(createToolCallCompletion(new LLMToolCall("call-1", "mcp_read_resource", "{\"uri\":\"   \"}"))),
                        "invalid_tool_arguments"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertUnexpectedQueryResultCases")
    void assertUnexpectedQueryResult(final String name, final String finalAnswerJson, final String expectedMessage) {
        LLME2EArtifactBundle result = new LLMMCPConversationRunner(4,
                new StubLLMChatClient(
                        createToolCallCompletion(
                                new LLMToolCall("call-1", "search_metadata",
                                        "{\"database\":\"logic_db\",\"schema\":\"public\",\"query\":\"orders\",\"object_types\":[\"TABLE\"]}"),
                                new LLMToolCall("call-2", "mcp_read_resource",
                                        "{\"uri\":\"shardingsphere://databases/logic_db/schemas/public/tables/orders\"}"),
                                new LLMToolCall("call-3", "execute_query",
                                        "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}")),
                        createFinalCompletion(finalAnswerJson)),
                new StubMCPInteractionClient(createSuccessfulToolResponses(),
                        Map.of("shardingsphere://databases/logic_db/schemas/public/tables/orders",
                                new MCPInteractionResponse(Map.of("table", "orders", "columns", List.of(Map.of("column", "order_id"))), "{}"))))
                .run(SCENARIO);
        assertFalse(result.getAssertionReport().isSuccess());
        assertThat(result.getAssertionReport().getFailureType(), is("unexpected_query_result"));
        assertThat(result.getAssertionReport().getMessage(), is(expectedMessage));
    }
    
    static Stream<Arguments> assertUnexpectedQueryResultCases() {
        return Stream.of(
                Arguments.of("database mismatch",
                        createFinalAnswerJson("analytics_db", "public", "orders", QUERY, 2,
                                List.of("search_metadata", "mcp_read_resource", "execute_query")),
                        "Final answer database does not match expected database."),
                Arguments.of("schema mismatch",
                        createFinalAnswerJson("logic_db", "archive", "orders", QUERY, 2,
                                List.of("search_metadata", "mcp_read_resource", "execute_query")),
                        "Final answer schema does not match expected schema."),
                Arguments.of("table mismatch",
                        createFinalAnswerJson("logic_db", "public", "order_items", QUERY, 2,
                                List.of("search_metadata", "mcp_read_resource", "execute_query")),
                        "Final answer table does not match expected table."),
                Arguments.of("query mismatch",
                        createFinalAnswerJson("logic_db", "public", "orders", "SELECT COUNT(*) FROM orders", 2,
                                List.of("search_metadata", "mcp_read_resource", "execute_query")),
                        "Final answer query does not match expected query."),
                Arguments.of("total orders mismatch",
                        createFinalAnswerJson("logic_db", "public", "orders", QUERY, 3,
                                List.of("search_metadata", "mcp_read_resource", "execute_query")),
                        "Final answer totalOrders does not match the execute_query result."),
                Arguments.of("interaction sequence mismatch",
                        createFinalAnswerJson("logic_db", "public", "orders", QUERY, 2,
                                List.of("search_metadata", "execute_query")),
                        "Final answer interactionSequence does not match the observed interaction trace."));
    }
    
    @Test
    void assertInvalidFinalJson() {
        LLME2EArtifactBundle result = new LLMMCPConversationRunner(4,
                new StubLLMChatClient(
                        createToolCallCompletion(
                                new LLMToolCall("call-1", "search_metadata",
                                        "{\"database\":\"logic_db\",\"schema\":\"public\",\"query\":\"orders\",\"object_types\":[\"TABLE\"]}"),
                                new LLMToolCall("call-2", "mcp_read_resource",
                                        "{\"uri\":\"shardingsphere://databases/logic_db/schemas/public/tables/orders\"}"),
                                new LLMToolCall("call-3", "execute_query",
                                        "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}")),
                        new LLMChatCompletion("not-json", List.of(), "{}"),
                        new LLMChatCompletion("still-not-json", List.of(), "{}")),
                new StubMCPInteractionClient(createSuccessfulToolResponses(),
                        Map.of("shardingsphere://databases/logic_db/schemas/public/tables/orders",
                                new MCPInteractionResponse(Map.of("table", "orders", "columns", List.of(Map.of("column", "order_id"))), "{}"))))
                .run(SCENARIO);
        assertFalse(result.getAssertionReport().isSuccess());
        assertThat(result.getAssertionReport().getFailureType(), is("invalid_final_json"));
        assertThat(result.getAssertionReport().getMessage(), is("Model did not return a valid final JSON payload."));
    }
    
    @Test
    void assertRecoverFromInvalidFinalJsonOnRetry() {
        String expectedFinalAnswerJson = createFinalAnswerJson("logic_db", "public", "orders", QUERY, 2,
                List.of("search_metadata", "mcp_read_resource", "execute_query"));
        LLME2EArtifactBundle result = new LLMMCPConversationRunner(5,
                new StubLLMChatClient(
                        createToolCallCompletion(
                                new LLMToolCall("call-1", "search_metadata",
                                        "{\"database\":\"logic_db\",\"schema\":\"public\",\"query\":\"orders\",\"object_types\":[\"TABLE\"]}"),
                                new LLMToolCall("call-2", "mcp_read_resource",
                                        "{\"uri\":\"shardingsphere://databases/logic_db/schemas/public/tables/orders\"}"),
                                new LLMToolCall("call-3", "execute_query",
                                        "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}")),
                        new LLMChatCompletion("not-json", List.of(), "{}"),
                        createFinalCompletion(expectedFinalAnswerJson)),
                new StubMCPInteractionClient(createSuccessfulToolResponses(),
                        Map.of("shardingsphere://databases/logic_db/schemas/public/tables/orders",
                                new MCPInteractionResponse(Map.of("table", "orders", "columns", List.of(Map.of("column", "order_id"))), "{}"))))
                .run(SCENARIO);
        assertTrue(result.getAssertionReport().isSuccess());
        assertThat(result.getFinalAnswerJson(), is(expectedFinalAnswerJson));
        assertThat(result.getRawModelOutputs().size(), is(3));
    }
    
    @Test
    void assertCreateToolDefinitionsForBridgeAndRegistryTools() {
        AtomicInteger completionCount = new AtomicInteger();
        AtomicReference<List<Map<String, Object>>> actualTools = new AtomicReference<>(List.of());
        LLME2EScenario scenario = createScenario("scenario-tool-definitions",
                List.of("mcp_list_resources", "mcp_read_resource", "search_metadata", "execute_query"));
        LLME2EArtifactBundle result = new LLMMCPConversationRunner(4, createToolDefinitionChatClient(completionCount, actualTools),
                new StubMCPInteractionClient(createSuccessfulToolResponses(),
                        Map.of("shardingsphere://databases/logic_db/schemas/public/tables/orders",
                                new MCPInteractionResponse(Map.of("table", "orders"), "{}")),
                        new MCPInteractionResponse(Map.of("resources",
                                List.of(Map.of("uri", "shardingsphere://databases/logic_db/schemas/public/tables/orders"))), "{}")))
                .run(scenario);
        assertTrue(result.getAssertionReport().isSuccess());
        assertThat(actualTools.get().size(), is(4));
        Map<String, Object> listParameters = findToolParameters(actualTools.get(), "mcp_list_resources");
        assertThat(listParameters.get("type"), is("object"));
        assertThat(listParameters.get("properties"), is(Map.of()));
        assertFalse((boolean) listParameters.get("additionalProperties"));
        Map<String, Object> readParameters = findToolParameters(actualTools.get(), "mcp_read_resource");
        Map<String, Object> readProperties = toMap(readParameters.get("properties"));
        assertThat(readParameters.get("required"), is(List.of("uri")));
        assertThat(toMap(readProperties.get("uri")).get("type"), is("string"));
        assertFalse((boolean) readParameters.get("additionalProperties"));
        Map<String, Object> searchParameters = findToolParameters(actualTools.get(), "search_metadata");
        Map<String, Object> searchProperties = toMap(searchParameters.get("properties"));
        Map<String, Object> objectTypesSchema = toMap(searchProperties.get("object_types"));
        assertThat(searchParameters.get("required"), is(List.of("query")));
        assertThat(objectTypesSchema.get("type"), is("array"));
        assertThat(toMap(objectTypesSchema.get("items")).get("type"), is("string"));
        assertTrue((boolean) searchParameters.get("additionalProperties"));
        Map<String, Object> executeParameters = findToolParameters(actualTools.get(), "execute_query");
        Map<String, Object> executeProperties = toMap(executeParameters.get("properties"));
        assertThat(executeParameters.get("required"), is(List.of("database", "sql")));
        assertThat(toMap(executeProperties.get("database")).get("type"), is("string"));
        assertThat(toMap(executeProperties.get("max_rows")).get("type"), is("integer"));
        assertTrue((boolean) executeParameters.get("additionalProperties"));
    }
    
    private static LLMChatClient createToolDefinitionChatClient(final AtomicInteger completionCount,
                                                                final AtomicReference<List<Map<String, Object>>> actualTools) {
        return new LLMChatClient() {
            
            @Override
            public void waitUntilReady() {
            }
            
            @Override
            public LLMChatCompletion complete(final List<LLMChatMessage> messages, final List<Map<String, Object>> tools,
                                              final String toolChoice, final boolean jsonResponse) {
                return createToolDefinitionCompletion(completionCount, actualTools, tools);
            }
        };
    }
    
    private static LLMChatCompletion createToolDefinitionCompletion(final AtomicInteger completionCount,
                                                                    final AtomicReference<List<Map<String, Object>>> actualTools,
                                                                    final List<Map<String, Object>> tools) {
        if (0 == completionCount.getAndIncrement()) {
            actualTools.set(List.copyOf(toMapList(tools)));
            return createToolCallCompletion(
                    new LLMToolCall("call-1", "mcp_list_resources", "{}"),
                    new LLMToolCall("call-2", "mcp_read_resource",
                            "{\"uri\":\"shardingsphere://databases/logic_db/schemas/public/tables/orders\"}"),
                    new LLMToolCall("call-3", "search_metadata",
                            "{\"database\":\"logic_db\",\"schema\":\"public\",\"query\":\"orders\",\"object_types\":[\"table\"]}"),
                    new LLMToolCall("call-4", "execute_query",
                            "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}"));
        }
        return createFinalCompletion(createFinalAnswerJson("logic_db", "public", "orders", QUERY, 2,
                List.of("mcp_list_resources", "mcp_read_resource", "search_metadata", "execute_query")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertMissingRequiredToolCoverageCases")
    void assertMissingRequiredToolCoverage(final String name, final int maxTurns, final LLME2EScenario scenario,
                                           final LLMChatClient llmChatClient, final MCPInteractionClient mcpInteractionClient) {
        LLME2EArtifactBundle result = new LLMMCPConversationRunner(maxTurns, llmChatClient, mcpInteractionClient).run(scenario);
        assertFalse(result.getAssertionReport().isSuccess());
        assertThat(result.getAssertionReport().getFailureType(), is("missing_required_tool_coverage"));
    }
    
    static Stream<Arguments> assertMissingRequiredToolCoverageCases() {
        LLME2EScenario coverageScenario = new LLME2EScenario("scenario-coverage", "system", "user",
                createExpectedAnswer(List.of("search_metadata", "execute_query")),
                List.of("search_metadata", "execute_query"),
                List.of("search_metadata", "execute_query"));
        return Stream.of(
                Arguments.of("required tool returns error", 2, coverageScenario,
                        new StubLLMChatClient(
                                createToolCallCompletion(
                                        new LLMToolCall("call-1", "search_metadata",
                                                "{\"database\":\"logic_db\",\"schema\":\"public\",\"query\":\"orders\",\"object_types\":[\"TABLE\"]}"),
                                        new LLMToolCall("call-2", "execute_query",
                                                "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}")),
                                createFinalCompletion(createFinalAnswerJson("logic_db", "public", "orders", QUERY, 2,
                                        List.of("search_metadata", "execute_query")))),
                        new StubMCPInteractionClient(Map.of(
                                "search_metadata", new MCPInteractionResponse(Map.of("error_code", "not_found"), "{}"),
                                "execute_query", createQueryResponse(2)))),
                Arguments.of("turns exhausted", 1, SCENARIO,
                        new StubLLMChatClient(new LLMChatCompletion("", List.of(), "{}")),
                        new StubMCPInteractionClient(Map.of())));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertReportRuntimeFailureCases")
    void assertReportRuntimeFailure(final String name, final LLMChatClient llmChatClient, final MCPInteractionClient mcpInteractionClient,
                                    final String expectedFailureType, final String expectedMessage, final boolean exactMessage,
                                    final boolean expectedInterrupted) {
        LLME2EArtifactBundle result = new LLMMCPConversationRunner(2, llmChatClient, mcpInteractionClient).run(SCENARIO);
        assertFalse(result.getAssertionReport().isSuccess());
        assertThat(result.getAssertionReport().getFailureType(), is(expectedFailureType));
        if (exactMessage) {
            assertThat(result.getAssertionReport().getMessage(), is(expectedMessage));
        } else {
            assertThat(result.getAssertionReport().getMessage(), containsString(expectedMessage));
        }
        assertThat(Thread.interrupted(), is(expectedInterrupted));
    }
    
    static Stream<Arguments> assertReportRuntimeFailureCases() {
        return Stream.of(
                Arguments.of("model io exception",
                        new StubLLMChatClient(new IOException("service down")),
                        new StubMCPInteractionClient(Map.of()),
                        "model_service_unavailable", "service down", false, false),
                Arguments.of("mcp open failure",
                        new StubLLMChatClient(new LLMChatCompletion("", List.of(), "{}")),
                        new StubMCPInteractionClient(new IllegalStateException("MCP runtime failed to initialize."), Map.of()),
                        "mcp_runtime_unavailable", "MCP runtime failed to initialize.", true, false),
                Arguments.of("mcp tool call io exception",
                        new StubLLMChatClient(createToolCallCompletion(new LLMToolCall("call-1", "execute_query",
                                "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}"))),
                        StubMCPInteractionClient.createWithToolFailure("execute_query", new IOException("socket closed")),
                        "mcp_runtime_unavailable", "MCP action `execute_query` failed: socket closed", true, false),
                Arguments.of("mcp resource read failure",
                        new StubLLMChatClient(createToolCallCompletion(new LLMToolCall("call-1", "mcp_read_resource",
                                "{\"uri\":\"shardingsphere://capabilities\"}"))),
                        StubMCPInteractionClient.createWithResourceFailure("shardingsphere://capabilities",
                                new IllegalStateException("Failed to parse MCP resource payload.")),
                        "mcp_runtime_unavailable", "MCP action `mcp_read_resource` failed: Failed to parse MCP resource payload.", true, false),
                Arguments.of("interrupted conversation",
                        createInterruptedChatClient(),
                        new StubMCPInteractionClient(Map.of()),
                        "model_service_unavailable", "Conversation was interrupted.", true, true),
                Arguments.of("model illegal state",
                        createIllegalStateChatClient(),
                        new StubMCPInteractionClient(Map.of()),
                        "model_service_unavailable", "Model quota exceeded.", true, false));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertUnexpectedQueryResultWhenExecuteQueryTraceDoesNotContainNumericResultCases")
    void assertUnexpectedQueryResultWhenExecuteQueryTraceDoesNotContainNumericResult(final String name, final MCPInteractionResponse executeQueryResponse) {
        LLME2EArtifactBundle result = new LLMMCPConversationRunner(4,
                new StubLLMChatClient(
                        createToolCallCompletion(
                                new LLMToolCall("call-1", "search_metadata",
                                        "{\"database\":\"logic_db\",\"schema\":\"public\",\"query\":\"orders\",\"object_types\":[\"TABLE\"]}"),
                                new LLMToolCall("call-2", "mcp_read_resource",
                                        "{\"uri\":\"shardingsphere://databases/logic_db/schemas/public/tables/orders\"}"),
                                new LLMToolCall("call-3", "execute_query",
                                        "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}")),
                        createFinalCompletion(createFinalAnswerJson("logic_db", "public", "orders", QUERY, 2,
                                List.of("search_metadata", "mcp_read_resource", "execute_query")))),
                new StubMCPInteractionClient(Map.of(
                        "search_metadata", new MCPInteractionResponse(Map.of("items", List.of(Map.of("name", "orders"))), "{}"),
                        "execute_query", executeQueryResponse),
                        Map.of("shardingsphere://databases/logic_db/schemas/public/tables/orders",
                                new MCPInteractionResponse(Map.of("table", "orders", "columns", List.of(Map.of("column", "order_id"))), "{}"))))
                .run(SCENARIO);
        assertFalse(result.getAssertionReport().isSuccess());
        assertThat(result.getAssertionReport().getFailureType(), is("unexpected_query_result"));
        assertThat(result.getAssertionReport().getMessage(), is("The execute_query trace does not contain a numeric result set."));
    }
    
    static Stream<Arguments> assertUnexpectedQueryResultWhenExecuteQueryTraceDoesNotContainNumericResultCases() {
        return Stream.of(
                Arguments.of("non-numeric result value",
                        new MCPInteractionResponse(Map.of("result_kind", "result_set", "rows", List.of(List.of("not-a-number"))), "{}")),
                Arguments.of("empty result rows",
                        new MCPInteractionResponse(Map.of("result_kind", "result_set", "rows", List.of()), "{}")),
                Arguments.of("empty first row",
                        new MCPInteractionResponse(Map.of("result_kind", "result_set", "rows", List.of(List.of())), "{}")),
                Arguments.of("statement ack result",
                        new MCPInteractionResponse(Map.of("result_kind", "statement_ack"), "{}")));
    }
    
    private static LLME2EScenario createScenario(final String scenarioId, final List<String> actionNames) {
        return new LLME2EScenario(scenarioId, "system", "user", createExpectedAnswer(actionNames), actionNames, actionNames);
    }
    
    private static LLMStructuredAnswer createExpectedAnswer(final List<String> interactionSequence) {
        return new LLMStructuredAnswer("logic_db", "public", "orders", QUERY, 2, interactionSequence);
    }
    
    private static String createFinalAnswerJson(final String database, final String schema, final String table, final String query,
                                                final int totalOrders, final List<String> interactionSequence) {
        return String.format("{\"database\":\"%s\",\"schema\":\"%s\",\"table\":\"%s\",\"query\":\"%s\",\"totalOrders\":%d,\"interactionSequence\":%s}",
                database, schema, table, query, totalOrders, toJsonArray(interactionSequence));
    }
    
    private static String toJsonArray(final List<String> values) {
        StringBuilder result = new StringBuilder("[");
        for (int index = 0; index < values.size(); index++) {
            if (0 != index) {
                result.append(',');
            }
            result.append('"').append(values.get(index)).append('"');
        }
        result.append(']');
        return result.toString();
    }
    
    private static LLMChatCompletion createToolCallCompletion(final LLMToolCall... toolCalls) {
        return new LLMChatCompletion("", List.of(toolCalls), "{\"tool_calls\":" + toolCalls.length + "}");
    }
    
    private static LLMChatCompletion createFinalCompletion(final String finalAnswerJson) {
        return new LLMChatCompletion(finalAnswerJson, List.of(), "{\"final\":true}");
    }
    
    private static Map<String, MCPInteractionResponse> createSuccessfulToolResponses() {
        return Map.of(
                "search_metadata", new MCPInteractionResponse(Map.of("items", List.of(Map.of("name", "orders"))), "{}"),
                "execute_query", createQueryResponse(2));
    }
    
    private static MCPInteractionResponse createQueryResponse(final int totalOrders) {
        return new MCPInteractionResponse(Map.of("result_kind", "result_set", "rows", List.of(List.of(totalOrders))), "{}");
    }
    
    private static Map<String, Object> findToolParameters(final List<Map<String, Object>> tools, final String toolName) {
        for (Map<String, Object> each : tools) {
            Map<String, Object> function = toMap(each.get("function"));
            if (toolName.equals(function.get("name"))) {
                return toMap(function.get("parameters"));
            }
        }
        throw new IllegalArgumentException("Missing tool definition: " + toolName);
    }
    
    private static List<Map<String, Object>> toMapList(final Object value) {
        return JsonUtils.fromJsonString(JsonUtils.toJsonString(value), new TypeReference<>() {
        });
    }
    
    private static Map<String, Object> toMap(final Object value) {
        return JsonUtils.fromJsonString(JsonUtils.toJsonString(value), new TypeReference<>() {
        });
    }
    
    private static LLMChatClient createInterruptedChatClient() {
        return new LLMChatClient() {
            
            @Override
            public void waitUntilReady() {
            }
            
            @Override
            public LLMChatCompletion complete(final List<LLMChatMessage> messages, final List<Map<String, Object>> tools,
                                              final String toolChoice, final boolean jsonResponse) throws InterruptedException {
                throw new InterruptedException("interrupted");
            }
        };
    }
    
    private static LLMChatClient createIllegalStateChatClient() {
        return new LLMChatClient() {
            
            @Override
            public void waitUntilReady() {
                throw new IllegalStateException("Model quota exceeded.");
            }
            
            @Override
            public LLMChatCompletion complete(final List<LLMChatMessage> messages, final List<Map<String, Object>> tools, final String toolChoice, final boolean jsonResponse) {
                throw new UnsupportedOperationException("Should not reach complete.");
            }
        };
    }
    
}
