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

package org.apache.shardingsphere.test.e2e.mcp.support.transport;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPInteractionPayloadsTest {
    
    @Test
    void assertParseJsonPayload() {
        Map<String, Object> actualPayload = MCPInteractionPayloads.parseJsonPayload("{\"result\":{\"status\":\"ok\"}}");
        assertThat(MCPInteractionPayloads.getRequiredObject(actualPayload, "result").get("status"), is("ok"));
    }
    
    @Test
    void assertParseServerSentEventPayload() {
        Map<String, Object> actualPayload = MCPInteractionPayloads.parseJsonPayload("event: message\ndata: {\"result\":{\"status\":\"ok\"}}\n");
        assertThat(MCPInteractionPayloads.getRequiredObject(actualPayload, "result").get("status"), is("ok"));
    }
    
    @Test
    void assertParseJsonPayloadWithInvalidJson() {
        assertThrows(IllegalStateException.class, () -> MCPInteractionPayloads.parseJsonPayload("{invalid"));
    }
    
    @Test
    void assertHasJsonRpcError() {
        assertTrue(MCPInteractionPayloads.hasJsonRpcError(Map.of("error", Map.of("message", "failed"))));
    }
    
    @Test
    void assertGetRequiredJsonRpcResult() {
        assertThat(MCPInteractionPayloads.getRequiredJsonRpcResult(Map.of("result", Map.of("status", "ok"))).get("status"), is("ok"));
    }
    
    @Test
    void assertGetRequiredJsonRpcResultWithoutResult() {
        assertThrows(IllegalStateException.class, () -> MCPInteractionPayloads.getRequiredJsonRpcResult(Map.of()));
    }
    
    @Test
    void assertRejectJsonRpcErrorAsResult() {
        assertThrows(IllegalStateException.class,
                () -> MCPInteractionPayloads.getRequiredJsonRpcResult(Map.of("error", Map.of("message", "failed"))));
    }
    
    @Test
    void assertGetListResourcesPayload() {
        Map<String, Object> actualPayload = MCPInteractionPayloads.getListResourcesPayload(Map.of("result", Map.of("resources", List.of(Map.of("uri", "shardingsphere://capabilities")))));
        assertThat(MCPInteractionPayloads.getRequiredObjectList(actualPayload, "resources").getFirst().get("uri"), is("shardingsphere://capabilities"));
    }
    
    @Test
    void assertGetListResourcesPayloadWithError() {
        Map<String, Object> actualPayload = MCPInteractionPayloads.getListResourcesPayload(Map.of("error", Map.of("message", "Resource not found")));
        assertThat(actualPayload, is(Map.of("error_code", "json_rpc_error", "message", "Resource not found")));
    }
    
    @Test
    void assertGetToolCallPayload() {
        Map<String, Object> actualPayload = MCPInteractionPayloads.getToolCallPayload(Map.of("result", Map.of(
                "content", List.of(Map.of("type", "text", "text", "ok")), "structuredContent", Map.of("status", "ok"))));
        assertThat(actualPayload.get("status"), is("ok"));
    }
    
    @Test
    void assertGetToolCallPayloadPrefersStructuredContent() {
        Map<String, Object> payload = Map.of("result", Map.of(
                "structuredContent", Map.of("status", "ok"),
                "content", List.of(Map.of("type", "text", "text", "{\"status\":\"fallback\"}"))));
        assertThat(MCPInteractionPayloads.getToolCallPayload(payload).get("status"), is("ok"));
    }
    
    @Test
    void assertGetToolCallPayloadRejectsTextContentFallback() {
        Map<String, Object> payload = Map.of("result", Map.of("content", List.of(Map.of("text", "{\"status\":\"ok\"}"))));
        assertThrows(IllegalStateException.class, () -> MCPInteractionPayloads.getToolCallPayload(payload));
    }
    
    @Test
    void assertGetToolCallErrorPayloadFromTextContent() {
        Map<String, Object> payload = Map.of("result", Map.of(
                "isError", true,
                "content", List.of(Map.of("type", "text", "text", "{\"response_mode\":\"recovery\",\"message\":\"Invalid request.\"}"))));
        Map<String, Object> actual = MCPInteractionPayloads.getToolCallPayload(payload);
        assertThat(actual.get("response_mode"), is("recovery"));
        assertThat(actual.get("message"), is("Invalid request."));
    }
    
    @Test
    void assertGetToolCallErrorPayloadWithoutTextContent() {
        Map<String, Object> payload = Map.of("result", Map.of("isError", true, "content", List.of()));
        assertThrows(IllegalStateException.class, () -> MCPInteractionPayloads.getToolCallPayload(payload));
    }
    
    @Test
    void assertGetToolCallPayloadWithoutProtocolContent() {
        assertThrows(IllegalStateException.class,
                () -> MCPInteractionPayloads.getToolCallPayload(Map.of("result", Map.of("structuredContent", Map.of("status", "ok")))));
    }
    
    @Test
    void assertGetFirstResourcePayload() {
        Map<String, Object> payload = Map.of("result", Map.of("contents", List.of(Map.of("text", "{\"item\":{\"database\":\"logic_db\"}}"))));
        assertThat(MCPInteractionPayloads.getRequiredObject(MCPInteractionPayloads.getFirstResourcePayload(payload), "item").get("database"), is("logic_db"));
    }
    
    @Test
    void assertGetFirstResourcePayloadWithoutContent() {
        assertThrows(IllegalStateException.class, () -> MCPInteractionPayloads.getFirstResourcePayload(Map.of("result", Map.of())));
    }
    
    @Test
    void assertGetJsonRpcErrorPayload() {
        assertThat(MCPInteractionPayloads.getJsonRpcErrorPayload(Map.of("error", Map.of("message", "Tool not found"))),
                is(Map.of("error_code", "json_rpc_error", "message", "Tool not found")));
    }
    
    @Test
    void assertGetJsonRpcErrorPayloadWithScalarData() {
        assertThat(MCPInteractionPayloads.getJsonRpcErrorPayload(Map.of("error", Map.of(
                "message", "Unknown tool: invalid_tool_name", "data", "Tool not found: unsupported_tool"))),
                is(Map.of("error_code", "json_rpc_error", "message", "Unknown tool: invalid_tool_name")));
    }
    
    @Test
    void assertGetJsonRpcErrorPayloadPreservesErrorData() {
        Map<String, Object> actual = MCPInteractionPayloads.getJsonRpcErrorPayload(Map.of("error", Map.of(
                "message", "Tool not found",
                "data", Map.of(
                        "message", "Nested recovery message",
                        "response_mode", "recovery",
                        "recovery", Map.of("next_actions", List.of(Map.of("type", "tool_call", "tool_name", "database_gateway_search_metadata")))))));
        assertThat(actual.get("error_code"), is("json_rpc_error"));
        assertThat(actual.get("message"), is("Tool not found"));
        assertThat(actual.get("response_mode"), is("recovery"));
        assertThat(actual.get("recovery"), is(Map.of("next_actions", List.of(Map.of("type", "tool_call", "tool_name", "database_gateway_search_metadata")))));
    }
    
    @Test
    void assertGetJsonRpcErrorPayloadWithoutError() {
        assertTrue(MCPInteractionPayloads.getJsonRpcErrorPayload(Map.of()).isEmpty());
    }
    
    @Test
    void assertGetRequiredObject() {
        assertThat(MCPInteractionPayloads.getRequiredObjectValue(Map.of("status", "ok"), "payload").get("status"), is("ok"));
    }
    
    @Test
    void assertGetRequiredObjectList() {
        assertThat(MCPInteractionPayloads.getRequiredObjectList(List.of(Map.of("name", "orders")), "items").getFirst().get("name"), is("orders"));
    }
    
    @Test
    void assertGetAbsentOptionalObjectList() {
        assertTrue(MCPInteractionPayloads.getOptionalObjectList(Map.of(), "items").isEmpty());
    }
    
    @Test
    void assertRejectNonObjectListElement() {
        assertThrows(IllegalStateException.class, () -> MCPInteractionPayloads.getRequiredObjectList(List.of("orders"), "items"));
    }
}
