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

import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import io.modelcontextprotocol.spec.McpSchema.ResourceLink;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.json.schema.JsonSchemaValidator.ValidationResponse;
import io.modelcontextprotocol.json.schema.jackson2.DefaultJsonSchemaValidator;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPTransportPayloadUtilsTest {
    
    @Test
    void assertCreateCallToolResult() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("items", List.of(Map.of("name", "logic_db")));
        payload.put("count", 1);
        CallToolResult actual = MCPTransportPayloadUtils.createCallToolResult(payload);
        ValidationResponse actualValidation = new DefaultJsonSchemaValidator().validate(createOutputSchema(), actual.structuredContent());
        assertFalse(actual.isError());
        assertThat(actual.structuredContent(), is(payload));
        assertTrue(actualValidation.valid(), actualValidation::errorMessage);
        assertThat(actual.content().get(0), isA(TextContent.class));
        assertThat(((TextContent) actual.content().get(0)).text(), is(JsonUtils.toJsonString(payload)));
    }
    
    @Test
    void assertCreateCallToolResultWithErrorCodePayload() {
        assertFalse(MCPTransportPayloadUtils.createCallToolResult(Map.of("error_code", "invalid_request")).isError());
    }
    
    @Test
    void assertCreateCallToolResultWithResourceLinks() {
        Map<String, Object> payload = Map.of("resources_to_read", List.of(
                MCPResourceHintUtils.create("shardingsphere://databases/logic_db", "logical-database", "read_first", "Read logical database.", "resources_to_read")));
        CallToolResult actual = MCPTransportPayloadUtils.createCallToolResult(payload);
        assertThat(actual.structuredContent(), is(payload));
        assertThat(actual.content().get(1), isA(ResourceLink.class));
        ResourceLink actualLink = (ResourceLink) actual.content().get(1);
        assertThat(actualLink.uri(), is("shardingsphere://databases/logic_db"));
        assertThat(actualLink.title(), is("logical-database"));
        assertThat(actualLink.mimeType(), is(MCPTransportPayloadUtils.JSON_CONTENT_TYPE));
    }
    
    @Test
    void assertCreateCallToolResultWithItemResourceLinks() {
        Map<String, Object> payload = Map.of("items", List.of(Map.of(
                "resource", MCPResourceHintUtils.create("shardingsphere://databases/logic_db/tables/t_order", "table", "inspect_detail", "Read table.", "resource"),
                "parent_resource", MCPResourceHintUtils.create("shardingsphere://databases/logic_db", "logical-database", "inspect_parent", "Read database.", "parent_resource"),
                "next_resources", List.of(MCPResourceHintUtils.create(
                        "shardingsphere://databases/logic_db/tables/t_order/columns", "column-list", "inspect_children", "Read columns.", "next_resources")))));
        CallToolResult actual = MCPTransportPayloadUtils.createCallToolResult(payload);
        assertThat(actual.content().size(), is(4));
        assertThat(((ResourceLink) actual.content().get(1)).uri(), is("shardingsphere://databases/logic_db/tables/t_order"));
        assertThat(((ResourceLink) actual.content().get(2)).uri(), is("shardingsphere://databases/logic_db"));
        assertThat(((ResourceLink) actual.content().get(3)).uri(), is("shardingsphere://databases/logic_db/tables/t_order/columns"));
        assertThat(((Map<?, ?>) actual.content().get(1).meta()).get(MCPShardingSphereMetadataKeys.SOURCE_FIELD), is("resource"));
    }
    
    @Test
    void assertCreateCallToolResultWithBoundedPrioritizedResourceLinks() {
        Map<String, Object> payload = Map.of(
                "next_resources", createResourceHints("shardingsphere://databases/next_", "next_resources", 30),
                "parent_resource", MCPResourceHintUtils.create("shardingsphere://databases", "logical-database", "inspect_parent", "Read parent.", "parent_resource"),
                "resource", MCPResourceHintUtils.create("shardingsphere://databases/logic_db", "logical-database", "inspect_detail", "Read detail.", "resource"),
                "resources_to_read", List.of(MCPResourceHintUtils.create("shardingsphere://capabilities", "capability", "read_first", "Read capabilities.", "resources_to_read")));
        CallToolResult actual = MCPTransportPayloadUtils.createCallToolResult(payload);
        assertThat(actual.structuredContent(), is(payload));
        assertThat(actual.content().size(), is(25));
        assertThat(actual.meta().get(MCPShardingSphereMetadataKeys.RESOURCE_LINKS_EMITTED), is(24));
        assertThat(actual.meta().get(MCPShardingSphereMetadataKeys.RESOURCE_LINKS_OMITTED), is(9));
        assertThat(((ResourceLink) actual.content().get(1)).uri(), is("shardingsphere://capabilities"));
        assertThat(((ResourceLink) actual.content().get(2)).uri(), is("shardingsphere://databases/logic_db"));
        assertThat(((ResourceLink) actual.content().get(3)).uri(), is("shardingsphere://databases"));
        assertThat(((Map<?, ?>) actual.content().get(1).meta()).get(MCPShardingSphereMetadataKeys.SOURCE_FIELD), is("resources_to_read"));
    }
    
    @Test
    void assertCreateCallToolResultWithoutRawUriLink() {
        CallToolResult actual = MCPTransportPayloadUtils.createCallToolResult(Map.of("resource_uri", "shardingsphere://databases/logic_db"));
        assertThat(actual.content().size(), is(1));
    }
    
    @Test
    void assertCreateCallToolResultWithoutArbitraryNestedResourceHint() {
        Map<String, Object> payload = Map.of("debug", Map.of("resource", MCPResourceHintUtils.create(
                "shardingsphere://databases/logic_db", "logical-database", "inspect_detail", "Read logical database.", "resource")));
        CallToolResult actual = MCPTransportPayloadUtils.createCallToolResult(payload);
        assertThat(actual.content().size(), is(1));
    }
    
    @Test
    void assertCreateCallToolResultWithErrorResponse() {
        assertTrue(MCPTransportPayloadUtils.createCallToolResult(new MCPErrorResponse("invalid_request", "")).isError());
    }
    
    @Test
    void assertCreateReadResourceResult() {
        Map<String, Object> payload = Map.of("message", "ok");
        io.modelcontextprotocol.spec.McpSchema.ReadResourceResult actual = MCPTransportPayloadUtils.createReadResourceResult("shardingsphere://capabilities", payload);
        assertThat(actual.contents().get(0), isA(TextResourceContents.class));
        assertThat(((TextResourceContents) actual.contents().get(0)).text(), is(JsonUtils.toJsonString(payload)));
    }
    
    private Map<String, Object> createOutputSchema() {
        return Map.of(
                "type", "object",
                "required", List.of("items", "count"),
                "properties", Map.of(
                        "items", Map.of("type", "array"),
                        "count", Map.of("type", "integer")));
    }
    
    private List<Map<String, Object>> createResourceHints(final String uriPrefix, final String sourceField, final int count) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            result.add(MCPResourceHintUtils.create(uriPrefix + i, "logical-database", "inspect_detail", "Read resource.", sourceField));
        }
        return result;
    }
}
