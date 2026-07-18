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

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.ShardingSphereMCPResourceMetadata;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MetadataResourceResponseFactoryTest {
    
    @Test
    void assertCreateBroadListResponseGuidesSearch() {
        Map<String, Object> actual = createResponse("shardingsphere://databases", new MCPUriVariables(Map.of()), createDatabases(101)).toPayload();
        assertThat(((List<?>) actual.get("items")).size(), is(100));
        assertThat(actual.get("count"), is(100));
        assertThat(actual.get("total_count"), is(101));
        assertThat(actual.get("summary"), is("Returned 100 of 101 logical-database metadata entries."));
        assertTrue((Boolean) actual.get("truncated"));
        assertThat(actual.get("continuation_mode"), is("metadata_search"));
        assertThat(((Map<?, ?>) actual.get("large_result_guidance")).get("state"), is("broad_metadata_list"));
        assertThat(((Map<?, ?>) actual.get("large_result_guidance")).get("threshold"), is(100));
        Map<?, ?> nextAction = (Map<?, ?>) ((List<?>) actual.get("next_actions")).getFirst();
        assertThat(nextAction.get("tool_name"), is("database_gateway_search_metadata"));
        assertFalse(((Map<?, ?>) nextAction.get("arguments")).containsKey("page_size"));
        assertThat(((Map<?, ?>) nextAction.get("arguments")).get("object_types"), is(List.of("database")));
    }
    
    @Test
    void assertCreateDetailResponse() {
        MCPUriVariables uriVariables = new MCPUriVariables(Map.of("database", "逻辑 库/2026?"));
        Map<String, Object> actual = createResponse("shardingsphere://databases/{database}", uriVariables,
                List.of(Map.of("database", uriVariables.getValue("database")))).toPayload();
        assertThat(actual.get("response_mode"), is("detail"));
        assertThat(actual.get("summary"), is("Returned logical-database detail for this resource URI."));
        assertThat(actual.get("resource_kind"), is("detail"));
        assertThat(actual.get("object_scope"), is("logical-database"));
        assertThat(actual.get("items"), is(List.of(Map.of("database", "逻辑 库/2026?"))));
        String expectedSelfUri = "shardingsphere://databases/%E9%80%BB%E8%BE%91%20%E5%BA%93%2F2026%3F";
        assertThat(((Map<?, ?>) actual.get("self_resource")).get("uri"), is(expectedSelfUri));
        assertThat(((Map<?, ?>) actual.get("parent_resource")).get("uri"), is("shardingsphere://databases"));
        List<String> nextResourceUris = ((List<?>) actual.get("next_resources")).stream().map(each -> (String) ((Map<?, ?>) each).get("uri")).toList();
        assertThat(nextResourceUris, is(List.of(
                expectedSelfUri + "/schemas",
                expectedSelfUri + "/storage-units",
                expectedSelfUri + "/single-tables",
                expectedSelfUri + "/single-table/default-storage-unit")));
    }
    
    @Test
    void assertCreateMissingDetailResponse() {
        Map<String, Object> actual = createResponse("shardingsphere://databases/{database}", mock(MCPUriVariables.class), List.of()).toPayload();
        assertThat(actual.get("summary"), is("No logical-database detail item matched this resource URI."));
        assertThat(actual.get("items"), is(List.of()));
        assertThat(((Map<?, ?>) actual.get("empty_state")).get("reason"), is("logical-database detail resource was not found for this URI."));
        assertThat(((Map<?, ?>) actual.get("recovery")).get("recovery_category"), is("not_found"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("next_actions")).getFirst()).get("type"), is("terminal"));
    }
    
    private MCPSuccessPayload createResponse(final String uriTemplate, final MCPUriVariables uriVariables, final List<?> items) {
        MCPResourceDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredResourceDescriptor(uriTemplate);
        ShardingSphereMCPResourceMetadata metadata = MCPDescriptorCatalogIndex.getRequiredShardingSphereResourceMetadata(uriTemplate);
        return new MetadataResourceResponseFactory().create(mock(MCPFeatureRequestContext.class), uriVariables, descriptor, metadata, items);
    }
    
    private List<Map<String, String>> createDatabases(final int count) {
        List<Map<String, String>> result = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            result.add(Map.of("database", String.format("logic_db_%03d", i)));
        }
        return result;
    }
}
