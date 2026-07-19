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

import org.apache.shardingsphere.mcp.support.database.metadata.TransactionCapability;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.mcp.core.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.core.tool.payload.MetadataSearchHit;
import org.apache.shardingsphere.mcp.core.tool.payload.MetadataSearchResult;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureCapabilityFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchMetadataPayloadBuilderTest {
    
    private static final String TOOL_NAME = "database_gateway_search_metadata";
    
    @Test
    void assertBuildsLargeResultGuidance() {
        MetadataSearchResult searchResult = new MetadataSearchResult(List.of(createHit("logic_db", "public", "orders", List.of("name"))),
                Map.of("object_types", List.of("table")), 101, 100, true, 100);
        Map<String, Object> actual = SearchMetadataPayloadBuilder.build(mock(MCPFeatureRequestContext.class), createRequest("", "", ""), searchResult, TOOL_NAME);
        assertThat(actual.get("summary"), is("Metadata search returned 100 of 101 matches."));
        assertTrue((Boolean) actual.get("truncated"));
        Map<?, ?> actualLargeResultGuidance = (Map<?, ?>) actual.get("large_result_guidance");
        assertThat(actualLargeResultGuidance.get("state"), is("metadata_search_result_truncated"));
        assertThat(actualLargeResultGuidance.get("narrowing_arguments"), is(List.of("database", "schema", "query", "object_types")));
    }
    
    @Test
    void assertBuildsPaginationNavigation() {
        MetadataSearchResult searchResult = new MetadataSearchResult(List.of(createHit("logic_db", "public", "orders", List.of("name"))),
                Map.of("object_types", List.of("table")), 3, 1, true, 100);
        MetadataSearchRequest request = new MetadataSearchRequest("logic_db", "public", "order", Set.of(SupportedMCPMetadataObjectType.TABLE), 1, 1);
        Map<String, Object> actual = SearchMetadataPayloadBuilder.build(mock(MCPFeatureRequestContext.class), request, searchResult, TOOL_NAME);
        assertTrue((Boolean) actual.get("has_more"));
        assertThat(actual.get("next_offset"), is(2));
        assertThat(actual.get("continuation_mode"), is("pagination"));
        assertFalse(actual.containsKey("large_result_guidance"));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.get("next_actions")).getFirst();
        assertThat(actualNextAction.get("type"), is("tool_call"));
        assertThat(actualNextAction.get("tool_name"), is(TOOL_NAME));
        assertThat(actualNextAction.get("arguments"), is(Map.of(
                "database", "logic_db", "schema", "public", "query", "order", "object_types", List.of("table"), "limit", 1, "offset", 2)));
    }
    
    @Test
    void assertBuildsLastPage() {
        MetadataSearchResult searchResult = new MetadataSearchResult(List.of(createHit("logic_db", "public", "orders", List.of("name"))),
                Map.of("object_types", List.of("table")), 101, 1, false, 100);
        MetadataSearchRequest request = new MetadataSearchRequest("logic_db", "", "", Set.of(SupportedMCPMetadataObjectType.TABLE), 2, 100);
        Map<String, Object> actual = SearchMetadataPayloadBuilder.build(mock(MCPFeatureRequestContext.class), request, searchResult, TOOL_NAME);
        assertFalse((Boolean) actual.get("has_more"));
        assertFalse(actual.containsKey("next_offset"));
        assertFalse(actual.containsKey("large_result_guidance"));
        assertFalse(actual.containsKey("empty_state"));
        assertFalse(actual.containsKey("next_actions"));
    }
    
    @Test
    void assertBuildsEmptyStateWithBroadenedSearchAction() {
        MCPFeatureRequestContext requestContext = createDatabaseContext();
        MetadataSearchResult searchResult = new MetadataSearchResult(List.of(), Map.of(), 0, 0, false, 100);
        Map<String, Object> actual = SearchMetadataPayloadBuilder.build(requestContext, createRequest("logic_db", "public", "missing"), searchResult, TOOL_NAME);
        Map<?, ?> actualEmptyState = (Map<?, ?>) actual.get("empty_state");
        assertThat(actualEmptyState.get("category"), is("object_not_visible"));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.get("next_actions")).getFirst();
        assertThat(actualNextAction.get("type"), is("tool_call"));
        assertThat(actualNextAction.get("tool_name"), is(TOOL_NAME));
        assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("query"), is("missing"));
        assertFalse(((Map<?, ?>) actualNextAction.get("arguments")).containsKey("database"));
    }
    
    @Test
    void assertBuildsAmbiguityState() {
        MetadataSearchResult searchResult = new MetadataSearchResult(List.of(
                createHit("bar_db", "public", "orders", List.of("name")),
                createHit("baz_db", "public", "orders", List.of("name")),
                createHit("foo_db", "public", "orders_archive", List.of("name"))),
                Map.of(), 3, 3, false, 100);
        Map<String, Object> actual = SearchMetadataPayloadBuilder.build(mock(MCPFeatureRequestContext.class), createRequest("", "", "orders"), searchResult, TOOL_NAME);
        Map<?, ?> actualAmbiguityState = (Map<?, ?>) actual.get("ambiguity_state");
        assertTrue((Boolean) actualAmbiguityState.get("ambiguous"));
        assertThat(actualAmbiguityState.get("ambiguous_by"), is(List.of("name", "database")));
        assertThat(actualAmbiguityState.get("candidate_count"), is(2));
        assertThat(actualAmbiguityState.get("duplicated_names"), is(List.of("orders")));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.get("next_actions")).getFirst();
        assertThat(actualNextAction.get("order"), is(1));
    }
    
    private MetadataSearchRequest createRequest(final String database, final String schema, final String query) {
        return new MetadataSearchRequest(database, schema, query, Set.of(SupportedMCPMetadataObjectType.TABLE), 100, 0);
    }
    
    private MetadataSearchHit createHit(final String database, final String schema, final String name, final List<String> matchedFields) {
        return MetadataSearchHit.builder()
                .database(database).schema(schema).objectType("table").table(name).view("").name(name)
                .resource(Map.of()).parentResource(Map.of()).nextResources(List.of()).derivationStatus("complete").derivationReason("")
                .matchKind("exact").matchedFields(matchedFields).matchedValue(name).build();
    }
    
    private MCPFeatureRequestContext createDatabaseContext() {
        MCPFeatureRequestContext result = mock(MCPFeatureRequestContext.class);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureCapabilityFacade capabilityFacade = mock(MCPFeatureCapabilityFacade.class);
        when(result.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        when(result.getCapabilityFacade()).thenReturn(capabilityFacade);
        when(metadataQueryFacade.queryDatabases()).thenReturn(
                List.of(new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0", TransactionCapability.LOCAL_WITH_SAVEPOINT, IdentifierCasePolicyFactory.newInsensitivePolicySet())));
        when(metadataQueryFacade.querySchema("logic_db", "public")).thenReturn(Optional.of(mock(ShardingSphereSchema.class)));
        when(capabilityFacade.findDatabaseProfile("logic_db")).thenReturn(Optional.of(mock(RuntimeDatabaseProfile.class)));
        return result;
    }
}
