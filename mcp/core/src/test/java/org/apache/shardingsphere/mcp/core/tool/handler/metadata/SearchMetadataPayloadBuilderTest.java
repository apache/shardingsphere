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

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.mcp.core.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.core.tool.response.MetadataSearchHit;
import org.apache.shardingsphere.mcp.core.tool.response.MetadataSearchResult;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
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
        Map<String, Object> actual = SearchMetadataPayloadBuilder.build(mock(MCPDatabaseHandlerContext.class), createRequest("", "", ""), searchResult, TOOL_NAME);
        assertThat(actual.get("summary"), is("Metadata search returned 100 of 101 matches."));
        assertTrue((Boolean) actual.get("truncated"));
        Map<?, ?> actualLargeResultGuidance = (Map<?, ?>) actual.get("large_result_guidance");
        assertThat(actualLargeResultGuidance.get("state"), is("metadata_search_result_truncated"));
        assertThat(actualLargeResultGuidance.get("narrowing_arguments"), is(List.of("database", "schema", "query", "object_types")));
    }
    
    @Test
    void assertBuildsEmptyStateWithBroadenedSearchAction() {
        MCPDatabaseHandlerContext databaseContext = createDatabaseContext();
        MetadataSearchResult searchResult = new MetadataSearchResult(List.of(), Map.of(), 0, 0, false, 100);
        Map<String, Object> actual = SearchMetadataPayloadBuilder.build(databaseContext, createRequest("logic_db", "public", "missing"), searchResult, TOOL_NAME);
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
        Map<String, Object> actual = SearchMetadataPayloadBuilder.build(mock(MCPDatabaseHandlerContext.class), createRequest("", "", "orders"), searchResult, TOOL_NAME);
        Map<?, ?> actualAmbiguityState = (Map<?, ?>) actual.get("ambiguity_state");
        assertTrue((Boolean) actualAmbiguityState.get("ambiguous"));
        assertThat(actualAmbiguityState.get("ambiguous_by"), is(List.of("name", "database")));
        assertThat(actualAmbiguityState.get("candidate_count"), is(2));
        assertThat(actualAmbiguityState.get("duplicated_names"), is(List.of("orders")));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.get("next_actions")).getFirst();
        assertThat(actualNextAction.get("order"), is(1));
    }
    
    private MetadataSearchRequest createRequest(final String database, final String schema, final String query) {
        return new MetadataSearchRequest(database, schema, query, Set.of(SupportedMCPMetadataObjectType.TABLE));
    }
    
    private MetadataSearchHit createHit(final String database, final String schema, final String name, final List<String> matchedFields) {
        return new MetadataSearchHit(database, schema, "table", name, "", name, Map.of(), Map.of(), List.of(), "complete", "", "exact", matchedFields, name);
    }
    
    private MCPDatabaseHandlerContext createDatabaseContext() {
        MCPDatabaseHandlerContext result = mock(MCPDatabaseHandlerContext.class);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureCapabilityFacade capabilityFacade = mock(MCPFeatureCapabilityFacade.class);
        when(result.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        when(result.getCapabilityFacade()).thenReturn(capabilityFacade);
        when(metadataQueryFacade.queryDatabases()).thenReturn(List.of(new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0", true, true)));
        when(metadataQueryFacade.querySchema("logic_db", "public")).thenReturn(Optional.of(mock(ShardingSphereSchema.class)));
        when(capabilityFacade.findDatabaseProfile("logic_db")).thenReturn(Optional.of(mock(RuntimeDatabaseProfile.class)));
        return result;
    }
}
