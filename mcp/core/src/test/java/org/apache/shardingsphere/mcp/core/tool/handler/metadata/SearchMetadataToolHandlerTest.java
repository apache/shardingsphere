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

package org.apache.shardingsphere.mcp.core.tool.handler.metadata;

import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.context.MCPFeatureRuntimeRequestContext;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory.DatabaseMetadataFixture;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory.RequestContextFixture;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory.TableMetadataFixture;
import org.apache.shardingsphere.mcp.core.tool.payload.MetadataSearchHit;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPItemsPayload;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchMetadataToolHandlerTest {
    
    @Test
    void assertGetSearchMetadataToolDescriptor() {
        MCPToolDescriptor actual = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(new SearchMetadataToolHandler().getToolName());
        assertThat(actual.getName(), is("database_gateway_search_metadata"));
        assertThat(((Map<?, ?>) actual.getInputSchema().get("properties")).size(), is(6));
        Map<?, ?> actualInputProperties = (Map<?, ?>) actual.getInputSchema().get("properties");
        Map<?, ?> actualObjectTypeItems = (Map<?, ?>) ((Map<?, ?>) actualInputProperties.get("object_types")).get("items");
        assertTrue(((List<?>) actualObjectTypeItems.get("enum")).contains("storage_unit"));
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
        assertTrue(actualProperties.containsKey("summary"));
        assertTrue(actualProperties.containsKey("total_match_count"));
        assertTrue(actualProperties.containsKey("count"));
        assertTrue(actualProperties.containsKey("truncated"));
        assertTrue(actualProperties.containsKey("has_more"));
        assertTrue(actualProperties.containsKey("next_offset"));
        assertTrue(actualProperties.containsKey("large_result_guidance"));
        assertTrue(actualProperties.containsKey("empty_state"));
        assertTrue(actualProperties.containsKey("ambiguity_state"));
        assertTrue(actualProperties.containsKey("next_actions"));
    }
    
    @Test
    void assertHandleSearchMetadata() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext()) {
            MCPFeatureRuntimeRequestContext requestContext = requestContextFixture.getRequestContext();
            MCPSuccessPayload actual =
                    new SearchMetadataToolHandler().handle(requestContext, Map.of("query", "order", "object_types", List.of(SupportedMCPMetadataObjectType.INDEX.name())));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(actual, isA(MCPItemsPayload.class));
            assertThat(((List<?>) actualPayload.get("items")).size(), is(1));
            assertThat(actualPayload.get("summary"), is("Metadata search returned 1 of 1 matches."));
            assertThat(actualPayload.get("total_match_count"), is(1));
            assertThat(actualPayload.get("count"), is(1));
            assertFalse((Boolean) actualPayload.get("truncated"));
            assertFalse((Boolean) actualPayload.get("has_more"));
            assertFalse(actualPayload.containsKey("next_page_token"));
            assertThat(actualPayload.get("continuation_mode"), is("none"));
            assertFalse(actualPayload.containsKey("next_offset"));
            assertThat(((MetadataSearchHit) ((List<?>) actualPayload.get("items")).getFirst()).getName(), is("order_idx"));
            assertThat(((Map<?, ?>) actualPayload.get("search_context")).get("object_types"), is(List.of("index")));
        }
    }
    
    @Test
    void assertHandleSearchMetadataWithSequence() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext()) {
            MCPFeatureRuntimeRequestContext requestContext = requestContextFixture.getRequestContext();
            MCPSuccessPayload actual = new SearchMetadataToolHandler().handle(requestContext,
                    Map.of("database", "runtime_db", "query", "order", "object_types", List.of(SupportedMCPMetadataObjectType.SEQUENCE.name())));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(actual, isA(MCPItemsPayload.class));
            assertThat(((List<?>) actualPayload.get("items")).size(), is(1));
            assertThat(((MetadataSearchHit) ((List<?>) actualPayload.get("items")).getFirst()).getName(), is("order_seq"));
        }
    }
    
    @Test
    void assertHandleSearchMetadataWithStorageUnit() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "SHOW STORAGE UNITS FROM logic_db")).thenReturn(List.of(Map.of("name", "write_ds")));
        MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
        when(requestContext.getMetadataQueryFacade()).thenReturn(mock(MCPMetadataQueryFacade.class));
        when(requestContext.getQueryFacade()).thenReturn(queryFacade);
        MCPSuccessPayload actual = new SearchMetadataToolHandler().handle(requestContext, Map.of("database", "logic_db", "query", "write", "object_types", List.of("storage_unit")));
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actual, isA(MCPItemsPayload.class));
        MetadataSearchHit actualHit = (MetadataSearchHit) ((List<?>) actualPayload.get("items")).getFirst();
        assertThat(actualHit.getObjectType(), is("storage_unit"));
        assertThat(actualHit.getName(), is("write_ds"));
        assertThat(actualHit.getResource().get("uri"), is("shardingsphere://databases/logic_db/storage-units/write_ds"));
        assertThat(((Map<?, ?>) actualPayload.get("search_context")).get("object_types"), is(List.of("storage_unit")));
    }
    
    @Test
    void assertHandleSearchMetadataRejectsQueryInObjectTypes() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext()) {
            MCPFeatureRuntimeRequestContext requestContext = requestContextFixture.getRequestContext();
            MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                    () -> new SearchMetadataToolHandler().handle(requestContext,
                            Map.of("database", "logic_db", "schema", "public", "query", "orders", "object_types", List.of("table", "orders"))));
            assertThat(actual.getMessage(), is("Unsupported object_types value `orders`."));
        }
    }
    
    @Test
    void assertHandleSearchMetadataWithEmptyQuery() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext()) {
            MCPFeatureRuntimeRequestContext requestContext = requestContextFixture.getRequestContext();
            MCPSuccessPayload actual = new SearchMetadataToolHandler().handle(requestContext, Map.of("database", "logic_db"));
            Map<String, Object> actualPayload = actual.toPayload();
            List<String> actualNames = new LinkedList<>();
            for (Object each : (List<?>) actualPayload.get("items")) {
                actualNames.add(((MetadataSearchHit) each).getName());
            }
            assertThat(actual, isA(MCPItemsPayload.class));
            assertThat(actualNames.size(), is(9));
            assertThat(actualPayload.get("total_match_count"), is(9));
            assertThat(actualPayload.get("count"), is(9));
            assertFalse((Boolean) actualPayload.get("truncated"));
            assertTrue(actualNames.contains("logic_db"));
            assertTrue(actualNames.contains("order_idx"));
        }
    }
    
    @Test
    void assertHandleSearchMetadataWithLargeResultGuidance() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext(createLargeDatabaseMetadata())) {
            MCPFeatureRuntimeRequestContext requestContext = requestContextFixture.getRequestContext();
            MCPSuccessPayload actual = new SearchMetadataToolHandler().handle(requestContext, Map.of("database", "large_db", "object_types", List.of(SupportedMCPMetadataObjectType.TABLE.name())));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(((List<?>) actualPayload.get("items")).size(), is(100));
            assertThat(actualPayload.get("summary"), is("Metadata search returned 100 of 101 matches."));
            assertThat(actualPayload.get("total_match_count"), is(101));
            assertThat(actualPayload.get("count"), is(100));
            assertTrue((Boolean) actualPayload.get("truncated"));
            assertThat(actualPayload.get("continuation_mode"), is("pagination"));
            assertThat(actualPayload.get("next_offset"), is(100));
            Map<?, ?> actualLargeResultGuidance = (Map<?, ?>) actualPayload.get("large_result_guidance");
            assertThat(actualLargeResultGuidance.get("state"), is("metadata_search_result_truncated"));
            assertThat(actualLargeResultGuidance.get("threshold"), is(100));
            assertThat(actualLargeResultGuidance.get("narrowing_arguments"), is(List.of("database", "schema", "query", "object_types")));
            List<?> actualNextActions = (List<?>) actualPayload.get("next_actions");
            assertThat(actualNextActions.size(), is(1));
            Map<?, ?> actualNextAction = (Map<?, ?>) actualNextActions.getFirst();
            assertThat(actualNextAction.get("type"), is("tool_call"));
            assertThat(actualNextAction.get("tool_name"), is("database_gateway_search_metadata"));
            assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("offset"), is(100));
        }
    }
    
    @Test
    void assertHandleSearchMetadataPage() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext(createLargeDatabaseMetadata())) {
            Map<String, Object> actual = new SearchMetadataToolHandler().handle(requestContextFixture.getRequestContext(),
                    Map.of("database", "large_db", "object_types", List.of("table"), "limit", 2, "offset", 1)).toPayload();
            assertThat(((List<?>) actual.get("items")).stream().map(each -> ((MetadataSearchHit) each).getName()).toList(), is(List.of("table_1", "table_10")));
            assertThat(actual.get("count"), is(2));
            assertThat(actual.get("total_match_count"), is(101));
            assertTrue((Boolean) actual.get("has_more"));
            assertThat(actual.get("next_offset"), is(3));
            assertThat(actual.get("continuation_mode"), is("pagination"));
        }
    }
    
    @Test
    void assertHandleSearchMetadataRejectsInvalidLimit() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext()) {
            MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                    () -> new SearchMetadataToolHandler().handle(requestContextFixture.getRequestContext(), Map.of("limit", 0)));
            assertThat(actual.getMessage(), is("limit must be an integer between 1 and 100."));
        }
    }
    
    @Test
    void assertHandleSearchMetadataRejectsInvalidOffset() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext()) {
            MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                    () -> new SearchMetadataToolHandler().handle(requestContextFixture.getRequestContext(), Map.of("offset", -1)));
            assertThat(actual.getMessage(), is("offset must be an integer between 0 and 2147483647."));
        }
    }
    
    @Test
    void assertHandleSearchMetadataWithBlankAllDatabaseGuard() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext()) {
            MCPFeatureRuntimeRequestContext requestContext = requestContextFixture.getRequestContext();
            MCPSuccessPayload actual = new SearchMetadataToolHandler().handle(requestContext, Map.of());
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
            Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualPayload.get("next_actions")).getFirst();
            assertThat(actualNextAction.get("type"), is("ask_user"));
            assertThat(actualNextAction.get("required_inputs"), is(List.of("database", "query", "object_types")));
        }
    }
    
    @Test
    void assertHandleSearchMetadataWithEmptyResultGuidance() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext()) {
            MCPFeatureRuntimeRequestContext requestContext = requestContextFixture.getRequestContext();
            MCPSuccessPayload actual = new SearchMetadataToolHandler().handle(requestContext, Map.of("database", "logic_db", "query", "missing"));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(actualPayload.get("summary"), is("Metadata search returned 0 of 0 matches."));
            assertThat(((Map<?, ?>) actualPayload.get("empty_state")).get("state"), is("no_match"));
            assertThat(((Map<?, ?>) actualPayload.get("empty_state")).get("category"), is("object_not_visible"));
            Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualPayload.get("next_actions")).getFirst();
            assertThat(actualNextAction.get("type"), is("tool_call"));
            assertThat(actualNextAction.get("tool_name"), is("database_gateway_search_metadata"));
            assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("query"), is("missing"));
            assertFalse(((Map<?, ?>) actualNextAction.get("arguments")).containsKey("database"));
        }
    }
    
    @Test
    void assertHandleSearchMetadataWithObjectNotVisibleInKnownSchema() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext()) {
            MCPFeatureRuntimeRequestContext requestContext = requestContextFixture.getRequestContext();
            MCPSuccessPayload actual = new SearchMetadataToolHandler().handle(requestContext, Map.of("database", "logic_db", "schema", "public", "query", "missing", "object_types", List.of("table")));
            Map<?, ?> actualEmptyState = (Map<?, ?>) actual.toPayload().get("empty_state");
            assertThat(actualEmptyState.get("category"), is("object_not_visible"));
            assertThat(actualEmptyState.get("reason"), is("No visible metadata object matched the query in the requested scope."));
        }
    }
    
    @Test
    void assertHandleSearchMetadataWithUnknownDatabase() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext()) {
            MCPFeatureRuntimeRequestContext requestContext = requestContextFixture.getRequestContext();
            MCPSuccessPayload actual = new SearchMetadataToolHandler().handle(requestContext, Map.of("database", "missing_db", "query", "orders"));
            Map<?, ?> actualEmptyState = (Map<?, ?>) actual.toPayload().get("empty_state");
            assertThat(actualEmptyState.get("category"), is("unknown_database"));
            assertThat(actualEmptyState.get("reason"), is("The requested logical database is not visible to MCP."));
        }
    }
    
    @Test
    void assertHandleSearchMetadataWithSchemaNotVisible() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext()) {
            MCPFeatureRuntimeRequestContext requestContext = requestContextFixture.getRequestContext();
            MCPSuccessPayload actual = new SearchMetadataToolHandler().handle(requestContext, Map.of("database", "logic_db", "schema", "missing_schema", "object_types", List.of("table")));
            Map<?, ?> actualEmptyState = (Map<?, ?>) actual.toPayload().get("empty_state");
            assertThat(actualEmptyState.get("category"), is("schema_not_visible"));
            assertThat(actualEmptyState.get("reason"), is("The requested schema is not visible in the current metadata scope."));
        }
    }
    
    @Test
    void assertHandleSearchMetadataWithoutRuntimeDatabase() {
        MCPFeatureRuntimeRequestContext requestContext = new MCPFeatureRuntimeRequestContext(ResourceTestDataFactory.createRuntimeContext(List.of()),
                new MCPSessionIdentity("session-1", "", "", Map.of()));
        MCPSuccessPayload actual = new SearchMetadataToolHandler().handle(requestContext, Map.of());
        Map<?, ?> actualEmptyState = (Map<?, ?>) actual.toPayload().get("empty_state");
        assertThat(actualEmptyState.get("category"), is("no_runtime_database"));
        assertThat(((Map<?, ?>) ((List<?>) actual.toPayload().get("next_actions")).getFirst()).get("type"), is("resource_read"));
    }
    
    @Test
    void assertHandleSearchMetadataWithAmbiguityGuidance() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext(createDuplicatedTableMetadata())) {
            MCPFeatureRuntimeRequestContext requestContext = requestContextFixture.getRequestContext();
            MCPSuccessPayload actual = new SearchMetadataToolHandler().handle(requestContext, Map.of("query", "orders"));
            Map<String, Object> actualPayload = actual.toPayload();
            Map<?, ?> actualAmbiguityState = (Map<?, ?>) actualPayload.get("ambiguity_state");
            assertTrue((Boolean) actualAmbiguityState.get("ambiguous"));
            assertThat(actualAmbiguityState.get("ambiguous_by"), is(List.of("name", "database")));
            assertThat(actualAmbiguityState.get("candidate_count"), is(2));
            assertThat(actualAmbiguityState.get("duplicated_names"), is(List.of("orders")));
            assertThat(actualAmbiguityState.get("narrowing_arguments"), is(List.of("database", "schema", "object_types")));
            Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualPayload.get("next_actions")).getFirst();
            assertThat(actualNextAction.get("type"), is("ask_user"));
            assertThat(actualNextAction.get("required_inputs"), is(List.of("database", "schema", "object_types")));
            assertThat(actualNextAction.get("order"), is(1));
        }
    }
    
    @Test
    void assertHandleSearchMetadataWithPrefixNameAmbiguity() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext(createDuplicatedTableMetadata())) {
            MCPFeatureRuntimeRequestContext requestContext = requestContextFixture.getRequestContext();
            MCPSuccessPayload actual = new SearchMetadataToolHandler().handle(requestContext, Map.of("query", "order"));
            Map<?, ?> actualAmbiguityState = (Map<?, ?>) actual.toPayload().get("ambiguity_state");
            assertTrue((Boolean) actualAmbiguityState.get("ambiguous"));
            assertThat(actualAmbiguityState.get("candidate_count"), is(2));
            assertThat(actualAmbiguityState.get("duplicated_names"), is(List.of("orders")));
        }
    }
    
    @Test
    void assertHandleSearchMetadataWithoutUnrelatedChildAmbiguity() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext()) {
            MCPFeatureRuntimeRequestContext requestContext = requestContextFixture.getRequestContext();
            MCPSuccessPayload actual = new SearchMetadataToolHandler().handle(requestContext, Map.of("database", "logic_db", "schema", "public", "query", "orders"));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(((List<?>) actualPayload.get("items")).size(), is(5));
            assertFalse(actualPayload.containsKey("ambiguity_state"));
            assertFalse(actualPayload.containsKey("next_actions"));
        }
    }
    
    @Test
    void assertHandleSearchMetadataWithAmbiguityAcrossCompleteResult() {
        try (RequestContextFixture requestContextFixture = createSearchRequestContext(createDuplicatedTableMetadata())) {
            MCPFeatureRuntimeRequestContext requestContext = requestContextFixture.getRequestContext();
            MCPSuccessPayload actual = new SearchMetadataToolHandler().handle(requestContext, Map.of("query", "orders"));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(((List<?>) actualPayload.get("items")).size(), is(3));
            assertThat(actualPayload.get("total_match_count"), is(3));
            assertFalse(actualPayload.containsKey("next_page_token"));
            Map<?, ?> actualAmbiguityState = (Map<?, ?>) actualPayload.get("ambiguity_state");
            assertThat(actualAmbiguityState.get("candidate_count"), is(2));
            assertThat(actualAmbiguityState.get("duplicated_names"), is(List.of("orders")));
            List<?> actualNextActions = (List<?>) actualPayload.get("next_actions");
            assertThat(actualNextActions.size(), is(1));
            assertThat(((Map<?, ?>) actualNextActions.getFirst()).get("type"), is("ask_user"));
        }
    }
    
    private MCPRuntimeContext createSearchRuntimeContext(final List<DatabaseMetadataFixture> databaseMetadata) {
        MCPRuntimeContext result = ResourceTestDataFactory.createRuntimeContext(databaseMetadata);
        result.getSessionManager().createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        return result;
    }
    
    private RequestContextFixture createSearchRequestContext() {
        return createSearchRequestContext(ResourceTestDataFactory.createDatabaseMetadata());
    }
    
    private RequestContextFixture createSearchRequestContext(final List<DatabaseMetadataFixture> databaseMetadata) {
        return ResourceTestDataFactory.createRequestContextFixture(createSearchRuntimeContext(databaseMetadata), databaseMetadata);
    }
    
    private List<DatabaseMetadataFixture> createDuplicatedTableMetadata() {
        return List.of(createDatabaseMetadata("bar_db", "orders"), createDatabaseMetadata("baz_db", "orders"), createDatabaseMetadata("foo_db", "orders_archive"));
    }
    
    private List<DatabaseMetadataFixture> createLargeDatabaseMetadata() {
        List<TableMetadataFixture> tables = IntStream.range(0, 101).mapToObj(index -> ResourceTestDataFactory.createTable("table_" + index, List.of(), List.of())).collect(Collectors.toList());
        return List.of(ResourceTestDataFactory.createDatabase("large_db", "MySQL", "", List.of(ResourceTestDataFactory.createSchema("public", tables, List.of(), List.of()))));
    }
    
    private DatabaseMetadataFixture createDatabaseMetadata(final String databaseName, final String tableName) {
        return ResourceTestDataFactory.createDatabase(databaseName, "MySQL", "",
                List.of(ResourceTestDataFactory.createSchema("public", List.of(ResourceTestDataFactory.createTable(tableName, List.of(), List.of())), List.of(), List.of())));
    }
}
