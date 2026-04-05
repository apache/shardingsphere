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
                    List.of("list_tables", "describe_table", "execute_query")),
            List.of("list_tables", "describe_table", "execute_query"),
            List.of("list_tables", "describe_table", "execute_query"));
    
    @Test
    void assertRun() {
        LLMMCPConversationRunner actual = new LLMMCPConversationRunner(4,
                new StubLLMChatClient(
                        new LLMChatCompletion("", List.of(
                                new LLMToolCall("call-1", "list_tables", "{\"database\":\"logic_db\",\"schema\":\"public\"}"),
                                new LLMToolCall("call-2", "describe_table", "{\"database\":\"logic_db\",\"schema\":\"public\",\"table\":\"orders\"}"),
                                new LLMToolCall("call-3", "execute_query", "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}")),
                                "{\"tool_calls\":3}"),
                        new LLMChatCompletion("{\"database\":\"logic_db\",\"schema\":\"public\",\"table\":\"orders\","
                                + "\"query\":\"SELECT COUNT(*) AS total_orders FROM orders\",\"totalOrders\":2,"
                                + "\"toolSequence\":[\"list_tables\",\"describe_table\",\"execute_query\"]}", List.of(), "{\"final\":true}")),
                new StubMCPToolClient(Map.of(
                        "list_tables", new MCPToolResponse(Map.of("items", List.of(Map.of("name", "orders"))), "{}"),
                        "describe_table", new MCPToolResponse(Map.of("items", List.of(Map.of("name", "order_id"))), "{}"),
                        "execute_query", new MCPToolResponse(Map.of("result_kind", "result_set", "rows", List.of(List.of(2))), "{}"))));
        
        LLME2EArtifactBundle result = actual.run(SCENARIO);
        
        assertTrue(result.assertionReport().success());
        assertThat(result.toolTrace().size(), is(3));
        assertThat(result.toolTrace().get(2).toolName(), is("execute_query"));
    }
    
    @Test
    void assertUnexpectedToolRequested() {
        LLMMCPConversationRunner actual = new LLMMCPConversationRunner(2,
                new StubLLMChatClient(new LLMChatCompletion("", List.of(new LLMToolCall("call-1", "drop_table", "{}")), "{}")),
                new StubMCPToolClient(Map.of()));
        
        LLME2EArtifactBundle result = actual.run(SCENARIO);
        
        assertFalse(result.assertionReport().success());
        assertThat(result.assertionReport().failureType(), is("unexpected_tool_requested"));
    }
    
    @Test
    void assertUnsafeSqlAttempted() {
        LLMMCPConversationRunner actual = new LLMMCPConversationRunner(2,
                new StubLLMChatClient(new LLMChatCompletion("", List.of(
                        new LLMToolCall("call-1", "execute_query", "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"UPDATE orders SET status='DONE'\"}")), "{}")),
                new StubMCPToolClient(Map.of()));
        
        LLME2EArtifactBundle result = actual.run(SCENARIO);
        
        assertFalse(result.assertionReport().success());
        assertThat(result.assertionReport().failureType(), is("unsafe_sql_attempted"));
    }
    
    @Test
    void assertInvalidToolArguments() {
        LLMMCPConversationRunner actual = new LLMMCPConversationRunner(2,
                new StubLLMChatClient(new LLMChatCompletion("", List.of(
                        new LLMToolCall("call-1", "list_tables", "{not-json}")), "{}")),
                new StubMCPToolClient(Map.of()));
        
        LLME2EArtifactBundle result = actual.run(SCENARIO);
        
        assertFalse(result.assertionReport().success());
        assertThat(result.assertionReport().failureType(), is("invalid_tool_arguments"));
    }
    
    @Test
    void assertInvalidFinalJson() {
        LLMMCPConversationRunner actual = new LLMMCPConversationRunner(4,
                new StubLLMChatClient(
                        new LLMChatCompletion("", List.of(
                                new LLMToolCall("call-1", "list_tables", "{\"database\":\"logic_db\",\"schema\":\"public\"}"),
                                new LLMToolCall("call-2", "describe_table", "{\"database\":\"logic_db\",\"schema\":\"public\",\"table\":\"orders\"}"),
                                new LLMToolCall("call-3", "execute_query", "{\"database\":\"logic_db\",\"schema\":\"public\",\"sql\":\"SELECT COUNT(*) AS total_orders FROM orders\"}")),
                                "{}"),
                        new LLMChatCompletion("not-json", List.of(), "{}"),
                        new LLMChatCompletion("still-not-json", List.of(), "{}")),
                new StubMCPToolClient(Map.of(
                        "list_tables", new MCPToolResponse(Map.of("items", List.of(Map.of("name", "orders"))), "{}"),
                        "describe_table", new MCPToolResponse(Map.of("items", List.of(Map.of("name", "order_id"))), "{}"),
                        "execute_query", new MCPToolResponse(Map.of("result_kind", "result_set", "rows", List.of(List.of(2))), "{}"))));
        
        LLME2EArtifactBundle result = actual.run(SCENARIO);
        
        assertFalse(result.assertionReport().success());
        assertThat(result.assertionReport().failureType() + ":" + result.assertionReport().message(),
                is("invalid_final_json:Model did not return a valid final JSON payload."));
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
    
    private static final class StubMCPToolClient implements MCPToolClient {
        
        private final Map<String, MCPToolResponse> responses;
        
        private StubMCPToolClient(final Map<String, MCPToolResponse> responses) {
            this.responses = new LinkedHashMap<>(responses);
        }
        
        @Override
        public void open() {
        }
        
        @Override
        public MCPToolResponse call(final String toolName, final Map<String, Object> arguments) throws IOException {
            if (!responses.containsKey(toolName)) {
                throw new IOException("Unsupported tool in stub: " + toolName);
            }
            return responses.get(toolName);
        }
        
        @Override
        public void close() {
        }
    }
}
