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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPListResponsePayloadAdapterTest {
    
    private final McpJsonMapper jsonMapper = MCPTransportJsonMapperFactory.create();
    
    @Test
    void assertAdaptResources() throws IOException {
        McpSchema.Resource resource = McpSchema.Resource.builder()
                .uri("shardingsphere://capabilities")
                .name("capabilities")
                .title("Capabilities")
                .description("Capability catalog.")
                .mimeType("application/json")
                .meta(Map.of("org.apache.shardingsphere/resource-kind", "catalog"))
                .build();
        Map<String, Object> actual = MCPListResponsePayloadAdapter.adaptResources(jsonMapper,
                new McpSchema.ListResourcesResult(List.of(resource), null, Map.of("org.apache.shardingsphere/list", "resources")),
                Map.of("shardingsphere://capabilities", Map.of("icons", List.of(Map.of("src", "https://example.invalid/icon.png", "mimeType", "image/png")))));
        assertThat(actual.get("_meta"), is(Map.of("org.apache.shardingsphere/list", "resources")));
        assertThat(((Map<?, ?>) firstItem(actual, "resources")).get("_meta"), is(Map.of("org.apache.shardingsphere/resource-kind", "catalog")));
        assertThat(((Map<?, ?>) firstItem(actual, "resources")).get("icons"), is(List.of(Map.of("src", "https://example.invalid/icon.png", "mimeType", "image/png"))));
    }
    
    @Test
    void assertAdaptResourceTemplates() throws IOException {
        McpSchema.ResourceTemplate resourceTemplate = McpSchema.ResourceTemplate.builder()
                .uriTemplate("shardingsphere://databases/{database}")
                .name("database")
                .title("Database")
                .description("Database detail.")
                .mimeType("application/json")
                .build();
        Map<String, Object> actual = MCPListResponsePayloadAdapter.adaptResourceTemplates(jsonMapper,
                new McpSchema.ListResourceTemplatesResult(List.of(resourceTemplate), null),
                Map.of("shardingsphere://databases/{database}", Map.of("icons", List.of(Map.of("src", "https://example.invalid/database.png")))));
        assertThat(((Map<?, ?>) firstItem(actual, "resourceTemplates")).get("icons"), is(List.of(Map.of("src", "https://example.invalid/database.png"))));
    }
    
    @Test
    void assertAdaptTools() throws IOException {
        McpSchema.Tool tool = McpSchema.Tool.builder()
                .name("database_gateway_execute_query")
                .title("Execute Query")
                .description("Execute readonly SQL.")
                .inputSchema(new McpSchema.JsonSchema("object", Map.of(), List.of(), false, Map.of(), Map.of()))
                .build();
        Map<String, Object> actual = MCPListResponsePayloadAdapter.adaptTools(jsonMapper,
                new McpSchema.ListToolsResult(List.of(tool), null),
                Map.of("database_gateway_execute_query", Map.of("icons", List.of(Map.of("src", "https://example.invalid/tool.png")))));
        assertThat(((Map<?, ?>) firstItem(actual, "tools")).get("icons"), is(List.of(Map.of("src", "https://example.invalid/tool.png"))));
    }
    
    @Test
    void assertJsonMapperAdaptsListResponse() throws IOException {
        MCPListResponseJsonMapper actualMapper = new MCPListResponseJsonMapper(jsonMapper, Map.of(), Map.of(),
                Map.of("database_gateway_execute_query", Map.of("icons", List.of(Map.of("src", "https://example.invalid/tool.png")))), Map.of());
        Map<?, ?> actual = jsonMapper.readValue(actualMapper.writeValueAsString(new McpSchema.ListToolsResult(List.of(createTool()), null)), Map.class);
        assertThat(((Map<?, ?>) firstItem(actual, "tools")).get("icons"), is(List.of(Map.of("src", "https://example.invalid/tool.png"))));
    }
    
    @Test
    void assertJsonMapperAdaptsJsonRpcListResponse() throws IOException {
        MCPListResponseJsonMapper actualMapper = new MCPListResponseJsonMapper(jsonMapper, Map.of(), Map.of(),
                Map.of("database_gateway_execute_query", Map.of("icons", List.of(Map.of("src", "https://example.invalid/tool.png")))), Map.of());
        Map<?, ?> actual = jsonMapper.readValue(actualMapper.writeValueAsString(
                new McpSchema.JSONRPCResponse("2.0", "tools-list-1", new McpSchema.ListToolsResult(List.of(createTool()), null), null)), Map.class);
        Map<?, ?> actualResult = (Map<?, ?>) actual.get("result");
        assertThat(actual.get("jsonrpc"), is("2.0"));
        assertThat(actual.get("id"), is("tools-list-1"));
        assertThat(((Map<?, ?>) firstItem(actualResult, "tools")).get("icons"), is(List.of(Map.of("src", "https://example.invalid/tool.png"))));
    }
    
    @Test
    void assertJsonMapperKeepsJsonRpcNonListResponse() throws IOException {
        MCPListResponseJsonMapper actualMapper = new MCPListResponseJsonMapper(jsonMapper, Map.of(), Map.of(),
                Map.of("database_gateway_execute_query", Map.of("icons", List.of(Map.of("src", "https://example.invalid/tool.png")))), Map.of());
        Map<?, ?> actual = jsonMapper.readValue(actualMapper.writeValueAsString(new McpSchema.JSONRPCResponse("2.0", "status-1", Map.of("status", "ok"), null)), Map.class);
        assertThat(actual.get("result"), is(Map.of("status", "ok")));
    }
    
    @Test
    void assertAdaptPrompts() throws IOException {
        McpSchema.Prompt prompt = new McpSchema.Prompt("inspect_metadata", "Inspect Metadata", "Inspect metadata.", List.of());
        Map<String, Object> actual = MCPListResponsePayloadAdapter.adaptPrompts(jsonMapper,
                new McpSchema.ListPromptsResult(List.of(prompt), null),
                Map.of("inspect_metadata", Map.of("icons", List.of(Map.of("src", "https://example.invalid/prompt.png")))));
        assertThat(((Map<?, ?>) firstItem(actual, "prompts")).get("icons"), is(List.of(Map.of("src", "https://example.invalid/prompt.png"))));
    }
    
    private Object firstItem(final Map<?, ?> payload, final String itemsFieldName) {
        return ((List<?>) payload.get(itemsFieldName)).get(0);
    }
    
    private McpSchema.Tool createTool() {
        return McpSchema.Tool.builder()
                .name("database_gateway_execute_query")
                .title("Execute Query")
                .description("Execute readonly SQL.")
                .inputSchema(new McpSchema.JsonSchema("object", Map.of(), List.of(), false, Map.of(), Map.of()))
                .build();
    }
}
