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
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureCapabilityFacade;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actualPayload.get("response_mode"), is("list"));
        assertThat(actualPayload.get("summary"), is("Returned 1 of 1 logical-database metadata entries."));
        assertThat(actualPayload.get("items"), is(List.of(Map.of("database", "logic_db"))));
        assertThat(actualPayload.get("count"), is(1));
        assertFalse((Boolean) actualPayload.get("has_more"));
        assertThat(actualPayload.get("continuation_mode"), is("none"));
        assertThat(actualPayload.get("self_uri"), is("shardingsphere://databases"));
        assertThat(((Map<?, ?>) actualPayload.get("self_resource")).get("uri"), is("shardingsphere://databases"));
        assertThat(((Map<?, ?>) actualPayload.get("self_resource")).get("resource_kind"), is("logical-database"));
        assertThat(((Map<?, ?>) actualPayload.get("self_resource")).get("source_field"), is("self_resource"));
        assertThat(actualPayload.get("total_count"), is(1));
        assertThat(actualPayload.get("returned_count"), is(1));
        assertFalse((Boolean) actualPayload.get("truncated"));
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
        assertThat(actualPayload.get("summary"), is("Returned 100 of 101 logical-database metadata entries."));
        assertTrue((Boolean) actualPayload.get("truncated"));
        assertTrue((Boolean) actualPayload.get("has_more"));
        assertThat(actualPayload.get("continuation_mode"), is("metadata_search"));
        assertThat(((Map<?, ?>) actualPayload.get("large_result_guidance")).get("state"), is("broad_metadata_list"));
        assertThat(((Map<?, ?>) actualPayload.get("large_result_guidance")).get("threshold"), is(100));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualPayload.get("next_actions")).getFirst();
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_search_metadata"));
        assertFalse(((Map<?, ?>) actualNextAction.get("arguments")).containsKey("page_size"));
        assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("object_types"), is(List.of("database")));
    }
    
    @Test
    void assertHandleRootListResourceWithoutRuntimeDatabase() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases", (requestContext, uriVariables) -> List.of());
        MCPResponse actual = handler.handle(mock(MCPDatabaseHandlerContext.class), new MCPUriVariables(Map.of()));
        Map<?, ?> actualEmptyState = (Map<?, ?>) actual.toPayload().get("empty_state");
        assertThat(actualEmptyState.get("category"), is("no_runtime_database"));
        assertThat(actualEmptyState.get("reason"), is("No ShardingSphere-Proxy logical database is available to MCP. Configure runtimeDatabases before reading metadata."));
        assertThat(((Map<?, ?>) actual.toPayload().get("recovery")).get("recovery_category"), is("no_runtime_database"));
    }
    
    @Test
    void assertHandleListResourceWithUnknownDatabase() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases/{database}/schemas", (requestContext, uriVariables) -> List.of());
        MCPResponse actual = handler.handle(createDatabaseContext(Optional.empty()), new MCPUriVariables(Map.of("database", "missing_db")));
        Map<?, ?> actualEmptyState = (Map<?, ?>) actual.toPayload().get("empty_state");
        assertThat(actualEmptyState.get("category"), is("unknown_database"));
        assertThat(actualEmptyState.get("reason"), is("The requested logical database is not visible to MCP. Check runtimeDatabases and ShardingSphere-Proxy connectivity."));
        assertThat(((Map<?, ?>) actual.toPayload().get("recovery")).get("recovery_category"), is("unknown_database"));
    }
    
    @Test
    void assertHandleListResourceWithEmptyScope() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases/{database}/schemas", (requestContext, uriVariables) -> List.of());
        MCPResponse actual = handler.handle(createDatabaseContext(Optional.of(new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0"))),
                new MCPUriVariables(Map.of("database", "logic_db")));
        Map<?, ?> actualEmptyState = (Map<?, ?>) actual.toPayload().get("empty_state");
        assertThat(actualEmptyState.get("category"), is("empty_scope"));
        assertThat(actualEmptyState.get("reason"), is("No metadata items are visible in this scope. Check metadata permissions if objects are expected."));
        assertThat(((Map<?, ?>) actual.toPayload().get("recovery")).get("recovery_category"), is("empty_scope"));
    }
    
    @Test
    void assertHandleSchemaDetailResourceNotVisible() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}", (requestContext, uriVariables) -> List.of());
        MCPResponse actual = handler.handle(createDatabaseContext(Optional.of(new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0"))),
                new MCPUriVariables(Map.of("database", "logic_db", "schema", "missing_schema")));
        Map<?, ?> actualEmptyState = (Map<?, ?>) actual.toPayload().get("empty_state");
        assertThat(actualEmptyState.get("category"), is("schema_not_visible"));
        assertThat(actualEmptyState.get("reason"), is("The requested schema is not visible in the current metadata scope."));
        assertThat(((Map<?, ?>) actual.toPayload().get("recovery")).get("requested_token"), is("missing_schema"));
        assertThat(((Map<?, ?>) ((List<?>) actual.toPayload().get("next_actions")).getFirst()).get("type"), is("resource_read"));
    }
    
    @Test
    void assertHandleObjectDetailResourceNotVisible() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}", (requestContext, uriVariables) -> List.of());
        MCPResponse actual = handler.handle(createDatabaseContext(Optional.of(new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0"))),
                new MCPUriVariables(Map.of("database", "logic_db", "schema", "public", "table", "missing_table")));
        Map<?, ?> actualEmptyState = (Map<?, ?>) actual.toPayload().get("empty_state");
        assertThat(actualEmptyState.get("category"), is("object_not_visible"));
        assertThat(actualEmptyState.get("reason"), is("The requested metadata object is not visible in the current metadata scope."));
        assertThat(((Map<?, ?>) actual.toPayload().get("recovery")).get("requested_token"), is("missing_table"));
    }
    
    @Test
    void assertHandleDetailResource() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases/{database}",
                (requestContext, uriVariables) -> List.of(Map.of("database", uriVariables.getValue("database"))));
        MCPUriVariables uriVariables = new MCPUriVariables(Map.of("database", "逻辑 库/2026?"));
        MCPResponse actual = handler.handle(mock(MCPDatabaseHandlerContext.class), uriVariables);
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actualPayload.get("response_mode"), is("detail"));
        assertThat(actualPayload.get("summary"), is("Returned logical-database detail for this resource URI."));
        assertThat(actualPayload.get("resource_kind"), is("detail"));
        assertThat(actualPayload.get("object_scope"), is("logical-database"));
        assertTrue((Boolean) actualPayload.get("found"));
        assertThat(actualPayload.get("items"), is(List.of(Map.of("database", "逻辑 库/2026?"))));
        assertThat(actualPayload.get("count"), is(1));
        assertThat(actualPayload.get("item"), is(Map.of("database", "逻辑 库/2026?")));
        String expectedSelfUri = "shardingsphere://databases/%E9%80%BB%E8%BE%91%20%E5%BA%93%2F2026%3F";
        assertThat(actualPayload.get("self_uri"), is(expectedSelfUri));
        assertThat(((Map<?, ?>) actualPayload.get("self_resource")).get("uri"), is(expectedSelfUri));
        Map<?, ?> actualParentResource = (Map<?, ?>) actualPayload.get("parent_resource");
        assertThat(actualParentResource.get("uri"), is("shardingsphere://databases"));
        assertThat(actualParentResource.get("source_field"), is("parent_resource"));
        List<?> actualNextResources = (List<?>) actualPayload.get("next_resources");
        assertThat(actualNextResources.size(), is(4));
        List<String> actualNextResourceUris = actualNextResources.stream().map(each -> (String) ((Map<?, ?>) each).get("uri")).toList();
        assertThat(actualNextResourceUris, is(List.of(
                expectedSelfUri + "/schemas",
                expectedSelfUri + "/storage-units",
                expectedSelfUri + "/single-tables",
                expectedSelfUri + "/single-table/default-storage-unit")));
    }
    
    @Test
    void assertHandleMissingDetailResource() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases/{database}", (requestContext, uriVariables) -> List.of());
        MCPResponse actual = handler.handle(mock(MCPDatabaseHandlerContext.class), mock(MCPUriVariables.class));
        assertThat(actual.toPayload().get("summary"), is("No logical-database detail item matched this resource URI."));
        assertFalse((Boolean) actual.toPayload().get("found"));
        assertThat(((Map<?, ?>) actual.toPayload().get("empty_state")).get("reason"), is("logical-database detail resource was not found for this URI."));
        assertThat(((Map<?, ?>) actual.toPayload().get("recovery")).get("recovery_category"), is("not_found"));
        assertThat(((Map<?, ?>) actual.toPayload().get("recovery")).get("category"), is("not_found"));
        assertThat(((Map<?, ?>) ((List<?>) actual.toPayload().get("next_actions")).getFirst()).get("type"), is("terminal"));
    }
    
    private MCPDatabaseHandlerContext createDatabaseContext(final Optional<RuntimeDatabaseProfile> databaseProfile) {
        MCPFeatureCapabilityFacade capabilityFacade = mock(MCPFeatureCapabilityFacade.class);
        when(capabilityFacade.findDatabaseProfile("logic_db")).thenReturn(databaseProfile);
        when(capabilityFacade.findDatabaseProfile("missing_db")).thenReturn(Optional.empty());
        MCPDatabaseHandlerContext result = mock(MCPDatabaseHandlerContext.class);
        when(result.getCapabilityFacade()).thenReturn(capabilityFacade);
        return result;
    }
    
    private List<Map<String, String>> createDatabases(final int count) {
        List<Map<String, String>> result = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            result.add(Map.of("database", String.format("logic_db_%03d", i)));
        }
        return result;
    }
}
