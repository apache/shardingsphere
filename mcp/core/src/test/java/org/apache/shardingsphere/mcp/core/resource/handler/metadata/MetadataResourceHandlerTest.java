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

package org.apache.shardingsphere.mcp.core.resource.handler.metadata;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MetadataResourceHandlerTest {
    
    @Test
    void assertGetResourceDescriptor() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases", (requestContext, uriVariables) -> List.of());
        MCPResourceDescriptor actual = MCPDescriptorCatalogIndex.getRequiredResourceDescriptor(handler.getResourceUriTemplate());
        assertThat(actual.getUriTemplate(), is("shardingsphere://databases"));
        assertThat(actual.getTitle(), is("Logical Databases"));
    }
    
    @Test
    void assertHandleListResource() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases",
                (requestContext, uriVariables) -> List.of(Map.of("database", "logic_db")));
        MCPResponse actual = handler.handle(mock(MCPDatabaseHandlerContext.class), new MCPUriVariables(Map.of()));
        assertThat(actual.toPayload(), is(Map.of("response_mode", "list", "items", List.of(Map.of("database", "logic_db")), "count", 1, "has_more", false,
                "continuation_mode", "none", "self_uri", "shardingsphere://databases", "total_count", 1, "returned_count", 1, "truncated", false)));
    }
    
    @Test
    void assertHandleBroadListResourceGuidesSearch() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases", (requestContext, uriVariables) -> createDatabases(101));
        MCPResponse actual = handler.handle(mock(MCPDatabaseHandlerContext.class), new MCPUriVariables(Map.of()));
        Map<?, ?> actualPayload = actual.toPayload();
        assertThat(((List<?>) actualPayload.get("items")).size(), is(100));
        assertThat(actualPayload.get("count"), is(100));
        assertThat(actualPayload.get("total_count"), is(101));
        assertThat(actualPayload.get("returned_count"), is(100));
        assertTrue((Boolean) actualPayload.get("truncated"));
        assertTrue((Boolean) actualPayload.get("has_more"));
        assertThat(actualPayload.get("continuation_mode"), is("metadata_search"));
        assertThat(((Map<?, ?>) actualPayload.get("large_result_guidance")).get("state"), is("broad_metadata_list"));
        assertThat(((Map<?, ?>) actualPayload.get("large_result_guidance")).get("threshold"), is(100));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualPayload.get("next_actions")).get(0);
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_search_metadata"));
        assertFalse(((Map<?, ?>) actualNextAction.get("arguments")).containsKey("page_size"));
        assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("object_types"), is(List.of("database")));
    }
    
    @Test
    void assertHandleDetailResource() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases/{database}",
                (requestContext, uriVariables) -> List.of(Map.of("database", uriVariables.getValue("database"))));
        MCPUriVariables uriVariables = new MCPUriVariables(Map.of("database", "逻辑 库/2026?"));
        MCPResponse actual = handler.handle(mock(MCPDatabaseHandlerContext.class), uriVariables);
        assertThat(actual.toPayload(), is(Map.of("response_mode", "detail", "resource_kind", "detail", "object_scope", "logical-database", "found", true,
                "items", List.of(Map.of("database", "逻辑 库/2026?")), "count", 1, "item", Map.of("database", "逻辑 库/2026?"),
                "self_uri", "shardingsphere://databases/%E9%80%BB%E8%BE%91%20%E5%BA%93%2F2026%3F",
                "parent_resource", Map.of("uri", "shardingsphere://databases", "resource_kind", "logical-database", "purpose", "inspect_parent",
                        "reason", "Read the parent metadata resource before broadening or correcting the request.", "source_field", "parent_resource"),
                "next_resources", List.of(Map.of("uri", "shardingsphere://databases/%E9%80%BB%E8%BE%91%20%E5%BA%93%2F2026%3F/schemas", "resource_kind", "schema", "purpose", "inspect_detail",
                        "reason", "List schemas after choosing a logical database.", "source_field", "next_resources")))));
    }
    
    @Test
    void assertHandleMissingDetailResource() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases/{database}", (requestContext, uriVariables) -> List.of());
        MCPResponse actual = handler.handle(mock(MCPDatabaseHandlerContext.class), mock(MCPUriVariables.class));
        assertFalse((Boolean) actual.toPayload().get("found"));
        assertThat(((Map<?, ?>) actual.toPayload().get("empty_state")).get("reason"), is("logical-database detail resource was not found for this URI."));
        assertThat(((Map<?, ?>) actual.toPayload().get("recovery")).get("recovery_category"), is("not_found"));
        assertThat(((Map<?, ?>) actual.toPayload().get("recovery")).get("category"), is("not_found"));
        assertThat(((Map<?, ?>) ((List<?>) actual.toPayload().get("next_actions")).get(0)).get("type"), is("terminal"));
    }
    
    private List<Map<String, String>> createDatabases(final int count) {
        List<Map<String, String>> result = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            result.add(Map.of("database", String.format("logic_db_%03d", i)));
        }
        return result;
    }
}
