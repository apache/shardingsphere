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

package org.apache.shardingsphere.mcp.core.tool.handler;

import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.core.tool.response.MetadataSearchHit;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.tool.handler.metadata.SearchMetadataToolHandler;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPItemsResponse;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolHandlerTest {

    @Test
    void assertGetSearchMetadataToolDescriptor() {
        MCPToolDescriptor actual = new SearchMetadataToolHandler().getToolDescriptor();
        assertThat(actual.getName(), is("search_metadata"));
        assertThat(actual.getFields().size(), is(6));
        Map<?, ?> actualProperties = (Map<?, ?>) actual.getOutputSchema().get("properties");
        Map<?, ?> actualItems = (Map<?, ?>) ((Map<?, ?>) actualProperties.get("items")).get("items");
        Map<?, ?> actualItemProperties = (Map<?, ?>) actualItems.get("properties");
        assertTrue(actualItemProperties.containsKey("resource"));
        assertTrue(actualItemProperties.containsKey("parent_resource"));
        assertTrue(actualItemProperties.containsKey("next_resources"));
        assertTrue(actualItemProperties.containsKey("derivation_status"));
        assertTrue(actualItemProperties.containsKey("match_kind"));
        assertTrue(actualItemProperties.containsKey("matched_fields"));
        assertTrue(actualItemProperties.containsKey("matched_value"));
        assertTrue(actualProperties.containsKey("search_context"));
        assertTrue(actualProperties.containsKey("total_match_count"));
        assertTrue(actualProperties.containsKey("empty_state"));
        assertTrue(actualProperties.containsKey("ambiguity_state"));
        assertTrue(actualProperties.containsKey("next_actions"));
    }

    @Test
    void assertHandleSearchMetadata() {
        try (MCPRequestScope requestContext = new MCPRequestScope(createSearchRuntimeContext())) {
            MCPResponse actual =
                    new SearchMetadataToolHandler().handle(requestContext, new MCPToolCall("session-1",
                            Map.of("query", "order", "object_types", List.of(SupportedMCPMetadataObjectType.INDEX.name()))));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(actual, isA(MCPItemsResponse.class));
            assertThat(((List<?>) actualPayload.get("items")).size(), is(1));
            assertThat(actualPayload.get("total_match_count"), is(1));
            assertThat(((MetadataSearchHit) ((List<?>) actualPayload.get("items")).get(0)).getName(), is("order_idx"));
            assertThat(((Map<?, ?>) actualPayload.get("search_context")).get("object_types"), is(List.of("index")));
        }
    }

    @Test
    void assertHandleSearchMetadataWithSequence() {
        try (MCPRequestScope requestContext = new MCPRequestScope(createSearchRuntimeContext())) {
            MCPResponse actual = new SearchMetadataToolHandler().handle(requestContext, new MCPToolCall("session-1",
                    Map.of("database", "runtime_db", "query", "order", "object_types", List.of(SupportedMCPMetadataObjectType.SEQUENCE.name()))));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(actual, isA(MCPItemsResponse.class));
            assertThat(((List<?>) actualPayload.get("items")).size(), is(1));
            assertThat(((MetadataSearchHit) ((List<?>) actualPayload.get("items")).get(0)).getName(), is("order_seq"));
        }
    }

    @Test
    void assertHandleSearchMetadataWithEmptyQuery() {
        try (MCPRequestScope requestContext = new MCPRequestScope(createSearchRuntimeContext())) {
            MCPResponse actual = new SearchMetadataToolHandler().handle(requestContext, new MCPToolCall("session-1", Map.of("database", "logic_db")));
            Map<String, Object> actualPayload = actual.toPayload();
            List<String> actualNames = new LinkedList<>();
            for (Object each : (List<?>) actualPayload.get("items")) {
                actualNames.add(((MetadataSearchHit) each).getName());
            }
            assertThat(actual, isA(MCPItemsResponse.class));
            assertThat(actualNames.size(), is(9));
            assertThat(actualPayload.get("total_match_count"), is(9));
            assertTrue(actualNames.contains("logic_db"));
            assertTrue(actualNames.contains("order_idx"));
        }
    }

    @Test
    void assertHandleSearchMetadataWithBlankAllDatabaseGuard() {
        try (MCPRequestScope requestContext = new MCPRequestScope(createSearchRuntimeContext())) {
            MCPResponse actual = new SearchMetadataToolHandler().handle(requestContext, new MCPToolCall("session-1", Map.of()));
            Map<String, Object> actualPayload = actual.toPayload();
            List<String> actualNames = new LinkedList<>();
            for (Object each : (List<?>) actualPayload.get("items")) {
                actualNames.add(((MetadataSearchHit) each).getName());
            }
            assertThat(actualNames, is(List.of("logic_db", "runtime_db", "warehouse")));
            assertThat(actualPayload.get("total_match_count"), is(3));
            Map<?, ?> actualSearchContext = (Map<?, ?>) actualPayload.get("search_context");
            assertThat(actualSearchContext.get("object_types"), is(List.of("database")));
            assertTrue((Boolean) actualSearchContext.get("broad_search_guarded"));
            Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualPayload.get("next_actions")).get(0);
            assertThat(actualNextAction.get("action_kind"), is("ask_user"));
            assertThat(actualNextAction.get("required_inputs"), is(List.of("database", "query", "object_types")));
        }
    }

    @Test
    void assertHandleSearchMetadataWithEmptyResultGuidance() {
        try (MCPRequestScope requestContext = new MCPRequestScope(createSearchRuntimeContext())) {
            MCPResponse actual = new SearchMetadataToolHandler().handle(requestContext, new MCPToolCall("session-1", Map.of("database", "logic_db", "query", "missing")));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(((Map<?, ?>) actualPayload.get("empty_state")).get("state"), is("no_match"));
            Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualPayload.get("next_actions")).get(0);
            assertThat(actualNextAction.get("action_kind"), is("call_tool"));
            assertThat(actualNextAction.get("target_tool"), is("search_metadata"));
            assertThat(((Map<?, ?>) actualNextAction.get("required_arguments")).get("database"), is("logic_db"));
        }
    }

    @Test
    void assertHandleSearchMetadataWithPaginationGuidance() {
        try (MCPRequestScope requestContext = new MCPRequestScope(createSearchRuntimeContext())) {
            MCPResponse actual = new SearchMetadataToolHandler().handle(requestContext, new MCPToolCall("session-1", Map.of("query", "order", "page_size", 1)));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(actualPayload.get("next_page_token"), is("1"));
            Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualPayload.get("next_actions")).get(0);
            assertThat(actualNextAction.get("action_kind"), is("call_tool"));
            assertThat(actualNextAction.get("target_tool"), is("search_metadata"));
            assertThat(((Map<?, ?>) actualNextAction.get("required_arguments")).get("page_token"), is("1"));
            assertThat(actualNextAction.get("order"), is(1));
        }
    }

    @Test
    void assertHandleSearchMetadataWithAmbiguityGuidance() {
        try (MCPRequestScope requestContext = new MCPRequestScope(createSearchRuntimeContext(createDuplicatedTableMetadata()))) {
            MCPResponse actual = new SearchMetadataToolHandler().handle(requestContext, new MCPToolCall("session-1", Map.of("query", "orders")));
            Map<String, Object> actualPayload = actual.toPayload();
            Map<?, ?> actualAmbiguityState = (Map<?, ?>) actualPayload.get("ambiguity_state");
            assertTrue((Boolean) actualAmbiguityState.get("ambiguous"));
            assertThat(actualAmbiguityState.get("ambiguous_by"), is(List.of("name", "database")));
            assertThat(actualAmbiguityState.get("candidate_count"), is(2));
            assertThat(actualAmbiguityState.get("duplicated_names"), is(List.of("orders")));
            assertThat(actualAmbiguityState.get("narrowing_arguments"), is(List.of("database", "schema", "object_types")));
            Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualPayload.get("next_actions")).get(0);
            assertThat(actualNextAction.get("action_kind"), is("ask_user"));
            assertThat(actualNextAction.get("required_inputs"), is(List.of("database", "schema", "object_types")));
            assertThat(actualNextAction.get("order"), is(1));
        }
    }

    @Test
    void assertHandleSearchMetadataWithPaginationAndAmbiguityOrder() {
        try (MCPRequestScope requestContext = new MCPRequestScope(createSearchRuntimeContext(createDuplicatedTableMetadata()))) {
            MCPResponse actual = new SearchMetadataToolHandler().handle(requestContext, new MCPToolCall("session-1", Map.of("query", "orders", "page_size", 2)));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(actualPayload.get("next_page_token"), is("2"));
            List<?> actualNextActions = (List<?>) actualPayload.get("next_actions");
            assertThat(((Map<?, ?>) actualNextActions.get(0)).get("action_kind"), is("call_tool"));
            assertThat(((Map<?, ?>) actualNextActions.get(0)).get("order"), is(1));
            assertThat(((Map<?, ?>) actualNextActions.get(1)).get("action_kind"), is("ask_user"));
            assertThat(((Map<?, ?>) actualNextActions.get(1)).get("order"), is(2));
        }
    }

    @Test
    void assertHandleSearchMetadataWithAmbiguityAcrossPages() {
        try (MCPRequestScope requestContext = new MCPRequestScope(createSearchRuntimeContext(createDuplicatedTableMetadata()))) {
            MCPResponse actual = new SearchMetadataToolHandler().handle(requestContext, new MCPToolCall("session-1", Map.of("query", "orders", "page_size", 1)));
            Map<String, Object> actualPayload = actual.toPayload();
            Map<?, ?> actualAmbiguityState = (Map<?, ?>) actualPayload.get("ambiguity_state");
            assertThat(((List<?>) actualPayload.get("items")).size(), is(1));
            assertThat(actualPayload.get("total_match_count"), is(3));
            assertThat(actualAmbiguityState.get("candidate_count"), is(2));
            assertThat(actualAmbiguityState.get("duplicated_names"), is(List.of("orders")));
            List<?> actualNextActions = (List<?>) actualPayload.get("next_actions");
            assertThat(((Map<?, ?>) actualNextActions.get(0)).get("action_kind"), is("call_tool"));
            assertThat(((Map<?, ?>) actualNextActions.get(1)).get("action_kind"), is("ask_user"));
        }
    }

    private MCPRuntimeContext createSearchRuntimeContext() {
        return createSearchRuntimeContext(ResourceTestDataFactory.createDatabaseMetadata());
    }

    private MCPRuntimeContext createSearchRuntimeContext(final List<MCPDatabaseMetadata> databaseMetadata) {
        MCPRuntimeContext result = ResourceTestDataFactory.createRuntimeContext(databaseMetadata);
        result.getSessionManager().createSession("session-1");
        return result;
    }

    private List<MCPDatabaseMetadata> createDuplicatedTableMetadata() {
        return List.of(createDatabaseMetadata("bar_db", "orders"), createDatabaseMetadata("baz_db", "orders"), createDatabaseMetadata("foo_db", "orders_archive"));
    }

    private MCPDatabaseMetadata createDatabaseMetadata(final String databaseName, final String tableName) {
        return new MCPDatabaseMetadata(databaseName, "MySQL", "", List.of(
                new MCPSchemaMetadata(databaseName, "public", List.of(new MCPTableMetadata(databaseName, "public", tableName, List.of(), List.of())), List.of())));
    }
}
