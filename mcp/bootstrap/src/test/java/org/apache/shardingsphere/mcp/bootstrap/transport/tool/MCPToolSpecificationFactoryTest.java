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

package org.apache.shardingsphere.mcp.bootstrap.transport.tool;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.apache.shardingsphere.mcp.tool.MCPToolController;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition.Type;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPToolSpecificationFactoryTest {
    
    @Test
    void assertCreateToolSpecifications() {
        MCPToolSpecificationFactory actualFactory = new MCPToolSpecificationFactory(List.of(createToolDescriptor()), mock(MCPToolController.class));
        List<SyncToolSpecification> actual = actualFactory.createToolSpecifications();
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).tool().name(), is("search_metadata"));
        assertThat(actual.get(0).tool().title(), is("Search Metadata"));
        assertThat(actual.get(0).tool().description(), is("ShardingSphere MCP tool: search_metadata"));
        assertThat(actual.get(0).tool().inputSchema().type(), is("object"));
        assertThat(actual.get(0).tool().inputSchema().required(), is(List.of("query")));
        assertThat(actual.get(0).tool().inputSchema().properties().get("query"), is(Map.of("type", "string", "description", "Search query.")));
        assertThat(actual.get(0).tool().inputSchema().properties().get("object_types"), is(Map.of(
                "type", "array",
                "description", "Optional object-type filter.",
                "items", Map.of("type", "string", "description", "Object type."))));
        assertNotNull(actual.get(0).callHandler());
    }
    
    @Test
    void assertCreateToolSpecificationsHandleNullArguments() {
        MCPToolController toolController = mock(MCPToolController.class);
        Map<String, Object> expectedPayload = Map.of("status", "ok");
        when(toolController.handle("session-id", "search_metadata", Map.of())).thenReturn(() -> expectedPayload);
        SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(List.of(createToolDescriptor()), toolController).createToolSpecifications().get(0);
        McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
        when(exchange.sessionId()).thenReturn("session-id");
        CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("search_metadata", null));
        verify(toolController).handle("session-id", "search_metadata", Map.of());
        assertThat(actual.structuredContent(), is(expectedPayload));
        assertThat(((TextContent) actual.content().get(0)).text(), is("{\"status\":\"ok\"}"));
    }
    
    private MCPToolDescriptor createToolDescriptor() {
        return new MCPToolDescriptor("search_metadata", List.of(
                new MCPToolFieldDefinition("query", new MCPToolValueDefinition(Type.STRING, "Search query.", null), true),
                new MCPToolFieldDefinition("object_types", new MCPToolValueDefinition(Type.ARRAY, "Optional object-type filter.",
                        new MCPToolValueDefinition(Type.STRING, "Object type.", null)), false)));
    }
}
