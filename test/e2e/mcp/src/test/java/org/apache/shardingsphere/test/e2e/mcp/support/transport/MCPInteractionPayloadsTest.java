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
        assertThat(MCPInteractionPayloads.castToMap(actualPayload.get("result")).get("status"), is("ok"));
    }
    
    @Test
    void assertParseServerSentEventPayload() {
        Map<String, Object> actualPayload = MCPInteractionPayloads.parseJsonPayload("event: message\ndata: {\"result\":{\"status\":\"ok\"}}\n");
        assertThat(MCPInteractionPayloads.castToMap(actualPayload.get("result")).get("status"), is("ok"));
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
    void assertGetJsonRpcResult() {
        assertThat(MCPInteractionPayloads.getJsonRpcResult(Map.of("result", Map.of("status", "ok"))).get("status"), is("ok"));
    }
    
    @Test
    void assertGetJsonRpcResultWithoutResult() {
        assertTrue(MCPInteractionPayloads.getJsonRpcResult(Map.of()).isEmpty());
    }
    
    @Test
    void assertGetResultContents() {
        Map<String, Object> payload = Map.of("result", Map.of("content", List.of(Map.of("text", "{\"status\":\"ok\"}"))));
        assertThat(MCPInteractionPayloads.getResultContents(payload).get(0).get("text"), is("{\"status\":\"ok\"}"));
    }
    
    @Test
    void assertGetResultContentsWithoutContent() {
        assertTrue(MCPInteractionPayloads.getResultContents(Map.of("result", Map.of())).isEmpty());
    }
    
    @Test
    void assertGetListResourcesPayload() {
        Map<String, Object> actualPayload = MCPInteractionPayloads.getListResourcesPayload(Map.of("result", Map.of("resources", List.of(Map.of("uri", "shardingsphere://capabilities")))));
        assertThat(MCPInteractionPayloads.castToList(actualPayload.get("resources")).get(0).get("uri"), is("shardingsphere://capabilities"));
    }
    
    @Test
    void assertGetListResourcesPayloadWithError() {
        Map<String, Object> actualPayload = MCPInteractionPayloads.getListResourcesPayload(Map.of("error", Map.of("message", "Resource not found")));
        assertThat(actualPayload, is(Map.of("error_code", "json_rpc_error", "message", "Resource not found")));
    }
    
    @Test
    void assertGetStructuredContent() {
        Map<String, Object> actualPayload = MCPInteractionPayloads.getStructuredContent(Map.of("result", Map.of("structuredContent", Map.of("status", "ok"))));
        assertThat(actualPayload.get("status"), is("ok"));
    }
    
    @Test
    void assertGetStructuredContentFromTextContent() {
        Map<String, Object> payload = Map.of("result", Map.of("content", List.of(Map.of("text", "{\"status\":\"ok\"}"))));
        assertThat(MCPInteractionPayloads.getStructuredContent(payload).get("status"), is("ok"));
    }
    
    @Test
    void assertGetStructuredContentWithoutContent() {
        assertTrue(MCPInteractionPayloads.getStructuredContent(Map.of("result", Map.of())).isEmpty());
    }
    
    @Test
    void assertGetFirstResourcePayload() {
        Map<String, Object> payload = Map.of("result", Map.of("contents", List.of(Map.of("text", "{\"item\":{\"database\":\"logic_db\"}}"))));
        assertThat(MCPInteractionPayloads.castToMap(MCPInteractionPayloads.getFirstResourcePayload(payload).get("item")).get("database"), is("logic_db"));
    }
    
    @Test
    void assertGetFirstResourcePayloadWithoutContent() {
        assertTrue(MCPInteractionPayloads.getFirstResourcePayload(Map.of("result", Map.of())).isEmpty());
    }
    
    @Test
    void assertGetJsonRpcErrorPayload() {
        assertThat(MCPInteractionPayloads.getJsonRpcErrorPayload(Map.of("error", Map.of("message", "Tool not found"))),
                is(Map.of("error_code", "json_rpc_error", "message", "Tool not found")));
    }
    
    @Test
    void assertGetJsonRpcErrorPayloadWithoutError() {
        assertTrue(MCPInteractionPayloads.getJsonRpcErrorPayload(Map.of()).isEmpty());
    }
    
    @Test
    void assertCastToMap() {
        assertThat(MCPInteractionPayloads.castToMap(Map.of("status", "ok")).get("status"), is("ok"));
    }
    
    @Test
    void assertCastToList() {
        assertThat(MCPInteractionPayloads.castToList(List.of(Map.of("name", "orders"))).get(0).get("name"), is("orders"));
    }
}
