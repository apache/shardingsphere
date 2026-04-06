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

package org.apache.shardingsphere.test.e2e.mcp;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LLMMCPConversationRunnerTest {
    
    private static final LLME2EScenario SCENARIO = new LLME2EScenario("scenario-a", "system", "user",
            new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) AS total_orders FROM orders", 2,
                    List.of("search_metadata", "mcp_read_resource", "execute_query")),
            List.of("search_metadata", "mcp_read_resource", "execute_query"),
            List.of("search_metadata", "mcp_read_resource", "execute_query"));
    
    @Test
    void assertRun() {
        LLMMCPConversationRunner actual = new LLMMCPConversationRunner(4,
                new StubLLMChatClient(
                        new LLMChatCompletion("", List.of(
                                new LLMToolCall("call-1", "search_metadata", "{\"database\":\"logic_db\",\"schema\":\"public\",\"query\":\"orders\",\"object_types\":[\"TABLE\"]}"),
                                new LLMToolCall("call-2", "mcp_read_resource", "{\"uri\":\"shardingsphere://databases/logic_db/schemas/public/tables/orders\"}"),
                                new LLMToolCall("call-3", "execute_query", "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}")),
                                "{\"tool_calls\":3}"),
                        new LLMChatCompletion("{\"database\":\"logic_db\",\"schema\":\"public\",\"table\":\"orders\","
                                + "\"query\":\"SELECT COUNT(*) AS total_orders FROM orders\",\"totalOrders\":2,"
                                + "\"interactionSequence\":[\"search_metadata\",\"mcp_read_resource\",\"execute_query\"]}", List.of(), "{\"final\":true}")),
                new StubMCPInteractionClient(Map.of(
                        "search_metadata", new MCPInteractionResponse(Map.of("items", List.of(Map.of("name", "orders"))), "{}"),
                        "execute_query", new MCPInteractionResponse(Map.of("result_kind", "result_set", "rows", List.of(List.of(2))), "{}")),
                        Map.of("shardingsphere://databases/logic_db/schemas/public/tables/orders",
                                new MCPInteractionResponse(Map.of("table", "orders", "columns", List.of(Map.of("column", "order_id"))), "{}"))));
        
        LLME2EArtifactBundle result = actual.run(SCENARIO);
        
        assertTrue(result.assertionReport().success());
        assertThat(result.interactionTrace().size(), is(3));
        assertThat(result.interactionTrace().get(2).targetName(), is("execute_query"));
    }
    
    @Test
    void assertUnexpectedToolRequested() {
        LLMMCPConversationRunner actual = new LLMMCPConversationRunner(2,
                new StubLLMChatClient(new LLMChatCompletion("", List.of(new LLMToolCall("call-1", "drop_table", "{}")), "{}")),
                new StubMCPInteractionClient(Map.of()));
        
        LLME2EArtifactBundle result = actual.run(SCENARIO);
        
        assertFalse(result.assertionReport().success());
        assertThat(result.assertionReport().failureType(), is("unexpected_tool_requested"));
    }
    
    @Test
    void assertUnsafeSqlAttempted() {
        LLMMCPConversationRunner actual = new LLMMCPConversationRunner(2,
                new StubLLMChatClient(new LLMChatCompletion("", List.of(
                        new LLMToolCall("call-1", "execute_query", "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"UPDATE orders SET status='DONE'\"}")), "{}")),
                new StubMCPInteractionClient(Map.of()));
        
        LLME2EArtifactBundle result = actual.run(SCENARIO);
        
        assertFalse(result.assertionReport().success());
        assertThat(result.assertionReport().failureType(), is("unsafe_sql_attempted"));
    }
    
    @Test
    void assertInvalidToolArguments() {
        LLMMCPConversationRunner actual = new LLMMCPConversationRunner(2,
                new StubLLMChatClient(new LLMChatCompletion("", List.of(
                        new LLMToolCall("call-1", "search_metadata", "{not-json}")), "{}")),
                new StubMCPInteractionClient(Map.of()));
        
        LLME2EArtifactBundle result = actual.run(SCENARIO);
        
        assertFalse(result.assertionReport().success());
        assertThat(result.assertionReport().failureType(), is("invalid_tool_arguments"));
    }
    
    @Test
    void assertInvalidFinalJson() {
        LLMMCPConversationRunner actual = new LLMMCPConversationRunner(4,
                new StubLLMChatClient(
                        new LLMChatCompletion("", List.of(
                                new LLMToolCall("call-1", "search_metadata", "{\"database\":\"logic_db\",\"schema\":\"public\",\"query\":\"orders\",\"object_types\":[\"TABLE\"]}"),
                                new LLMToolCall("call-2", "mcp_read_resource", "{\"uri\":\"shardingsphere://databases/logic_db/schemas/public/tables/orders\"}"),
                                new LLMToolCall("call-3", "execute_query", "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}")),
                                "{}"),
                        new LLMChatCompletion("not-json", List.of(), "{}"),
                        new LLMChatCompletion("still-not-json", List.of(), "{}")),
                new StubMCPInteractionClient(Map.of(
                        "search_metadata", new MCPInteractionResponse(Map.of("items", List.of(Map.of("name", "orders"))), "{}"),
                        "execute_query", new MCPInteractionResponse(Map.of("result_kind", "result_set", "rows", List.of(List.of(2))), "{}")),
                        Map.of("shardingsphere://databases/logic_db/schemas/public/tables/orders",
                                new MCPInteractionResponse(Map.of("table", "orders", "columns", List.of(Map.of("column", "order_id"))), "{}"))));
        
        LLME2EArtifactBundle result = actual.run(SCENARIO);
        
        assertFalse(result.assertionReport().success());
        assertThat(result.assertionReport().failureType() + ":" + result.assertionReport().message(),
                is("invalid_final_json:Model did not return a valid final JSON payload."));
    }
    
    @Test
    void assertMissingRequiredToolCoverageWhenRequiredToolReturnsError() {
        LLME2EScenario coverageScenario = new LLME2EScenario("scenario-coverage", "system", "user",
                new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) AS total_orders FROM orders", 2,
                        List.of("search_metadata", "execute_query")),
                List.of("search_metadata", "execute_query"),
                List.of("search_metadata", "execute_query"));
        LLMMCPConversationRunner actual = new LLMMCPConversationRunner(2,
                new StubLLMChatClient(
                        new LLMChatCompletion("", List.of(
                                new LLMToolCall("call-1", "search_metadata", "{\"database\":\"logic_db\",\"schema\":\"public\",\"query\":\"orders\",\"object_types\":[\"TABLE\"]}"),
                                new LLMToolCall("call-2", "execute_query", "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}")),
                                "{}"),
                        new LLMChatCompletion("{\"database\":\"logic_db\",\"schema\":\"public\",\"table\":\"orders\","
                                + "\"query\":\"SELECT COUNT(*) AS total_orders FROM orders\",\"totalOrders\":2,"
                                + "\"interactionSequence\":[\"search_metadata\",\"execute_query\"]}", List.of(), "{}")),
                new StubMCPInteractionClient(Map.of(
                        "search_metadata", new MCPInteractionResponse(Map.of("error_code", "not_found"), "{}"),
                        "execute_query", new MCPInteractionResponse(Map.of("result_kind", "result_set", "rows", List.of(List.of(2))), "{}"))));
        
        LLME2EArtifactBundle result = actual.run(coverageScenario);
        
        assertFalse(result.assertionReport().success());
        assertThat(result.assertionReport().failureType(), is("missing_required_tool_coverage"));
    }
    
    @Test
    void assertRunWithResourceBridge() {
        LLME2EScenario resourceScenario = new LLME2EScenario("scenario-resource", "system", "user",
                new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) AS total_orders FROM orders", 2,
                        List.of("mcp_read_resource", "execute_query")),
                List.of("mcp_read_resource", "execute_query"),
                List.of("mcp_read_resource", "execute_query"));
        LLMMCPConversationRunner actual = new LLMMCPConversationRunner(3,
                new StubLLMChatClient(
                        new LLMChatCompletion("", List.of(
                                new LLMToolCall("call-1", "mcp_read_resource", "{\"uri\":\"shardingsphere://capabilities\"}"),
                                new LLMToolCall("call-2", "execute_query", "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}")),
                                "{\"tool_calls\":2}"),
                        new LLMChatCompletion("{\"database\":\"logic_db\",\"schema\":\"public\",\"table\":\"orders\","
                                + "\"query\":\"SELECT COUNT(*) AS total_orders FROM orders\",\"totalOrders\":2,"
                                + "\"interactionSequence\":[\"mcp_read_resource\",\"execute_query\"]}", List.of(), "{\"final\":true}")),
                new StubMCPInteractionClient(Map.of(
                        "execute_query", new MCPInteractionResponse(Map.of("result_kind", "result_set", "rows", List.of(List.of(2))), "{}")),
                        Map.of("shardingsphere://capabilities", new MCPInteractionResponse(Map.of("supportedTools", List.of("execute_query")), "{}"))));
        
        LLME2EArtifactBundle result = actual.run(resourceScenario);
        
        assertTrue(result.assertionReport().success());
        assertThat(result.interactionTrace().size(), is(2));
        assertThat(result.interactionTrace().get(0).actionKind(), is("resource_read"));
        assertThat(result.interactionTrace().get(0).arguments().get("uri"), is("shardingsphere://capabilities"));
    }
    
    @Test
    void assertRunWithResourceListBridge() {
        LLME2EScenario resourceScenario = new LLME2EScenario("scenario-resource-list", "system", "user",
                new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) AS total_orders FROM orders", 2,
                        List.of("mcp_list_resources", "execute_query")),
                List.of("mcp_list_resources", "execute_query"),
                List.of("mcp_list_resources", "execute_query"));
        LLMMCPConversationRunner actual = new LLMMCPConversationRunner(3,
                new StubLLMChatClient(
                        new LLMChatCompletion("", List.of(
                                new LLMToolCall("call-1", "mcp_list_resources", "{}"),
                                new LLMToolCall("call-2", "execute_query", "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}")),
                                "{\"tool_calls\":2}"),
                        new LLMChatCompletion("{\"database\":\"logic_db\",\"schema\":\"public\",\"table\":\"orders\","
                                + "\"query\":\"SELECT COUNT(*) AS total_orders FROM orders\",\"totalOrders\":2,"
                                + "\"interactionSequence\":[\"mcp_list_resources\",\"execute_query\"]}", List.of(), "{\"final\":true}")),
                new StubMCPInteractionClient(Map.of(
                        "execute_query", new MCPInteractionResponse(Map.of("result_kind", "result_set", "rows", List.of(List.of(2))), "{}")),
                        Map.of(),
                        new MCPInteractionResponse(Map.of("resources", List.of(Map.of("uri", "shardingsphere://capabilities"))), "{}")));
        
        LLME2EArtifactBundle result = actual.run(resourceScenario);
        
        assertTrue(result.assertionReport().success());
        assertThat(result.interactionTrace().size(), is(2));
        assertThat(result.interactionTrace().get(0).actionKind(), is("resource_list"));
        assertThat(result.interactionTrace().get(0).targetName(), is("mcp_list_resources"));
    }
    
    private static final class StubLLMChatClient implements LLMChatClient {
        
        private final Queue<LLMChatCompletion> completions;
        
        private StubLLMChatClient(final LLMChatCompletion... completions) {
            this.completions = new ArrayDeque<>(List.of(completions));
        }
        
        @Override
        public void waitUntilReady() {
        }
        
        @Override
        public LLMChatCompletion complete(final List<LLMChatMessage> messages, final List<Map<String, Object>> tools,
                                          final String toolChoice, final boolean jsonResponse) {
            return completions.remove();
        }
    }
    
    private static final class StubMCPInteractionClient implements MCPInteractionClient {
        
        private final Map<String, MCPInteractionResponse> responses;
        
        private final Map<String, MCPInteractionResponse> resourceResponses;
        
        private final MCPInteractionResponse resourceListResponse;
        
        private StubMCPInteractionClient(final Map<String, MCPInteractionResponse> responses) {
            this(responses, Map.of(), new MCPInteractionResponse(Map.of("resources", List.of()), "{}"));
        }
        
        private StubMCPInteractionClient(final Map<String, MCPInteractionResponse> responses, final Map<String, MCPInteractionResponse> resourceResponses) {
            this(responses, resourceResponses, new MCPInteractionResponse(Map.of("resources", List.of()), "{}"));
        }
        
        private StubMCPInteractionClient(final Map<String, MCPInteractionResponse> responses, final Map<String, MCPInteractionResponse> resourceResponses,
                                         final MCPInteractionResponse resourceListResponse) {
            this.responses = new LinkedHashMap<>(responses);
            this.resourceResponses = new LinkedHashMap<>(resourceResponses);
            this.resourceListResponse = resourceListResponse;
        }
        
        @Override
        public void open() {
        }
        
        @Override
        public MCPInteractionResponse call(final String toolName, final Map<String, Object> arguments) throws IOException {
            if (!responses.containsKey(toolName)) {
                throw new IOException("Unsupported tool in stub: " + toolName);
            }
            return responses.get(toolName);
        }
        
        @Override
        public MCPInteractionResponse readResource(final String resourceUri) throws IOException {
            if (!resourceResponses.containsKey(resourceUri)) {
                throw new IOException("Unsupported resource in stub: " + resourceUri);
            }
            return resourceResponses.get(resourceUri);
        }
        
        @Override
        public MCPInteractionResponse listResources() {
            return resourceListResponse;
        }
        
        @Override
        public void close() {
        }
    }
}
