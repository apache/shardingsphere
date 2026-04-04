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

package org.apache.shardingsphere.mcp.resource;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;
import org.apache.shardingsphere.mcp.protocol.MCPPayloadBuilder;
import org.apache.shardingsphere.mcp.resource.response.MCPDatabaseCapabilityResponse;
import org.apache.shardingsphere.mcp.resource.response.MCPMetadataResponse;
import org.apache.shardingsphere.mcp.resource.response.MCPResourceResponse;
import org.apache.shardingsphere.mcp.resource.response.MCPServiceCapabilityResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPResourceDispatcherTest {
    
    private final MCPResourceDispatcher resourceDispatcher = new MCPResourceDispatcher();
    
    private final MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
    
    private final MCPPayloadBuilder payloadBuilder = new MCPPayloadBuilder();
    
    @Test
    void assertGetSupportedResources() {
        assertThat(resourceDispatcher.getSupportedResources().size(), is(16));
        assertThat(resourceDispatcher.getSupportedResources().get(0), is("shardingsphere://capabilities"));
        assertThat(resourceDispatcher.getSupportedResources().get(15), is("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}"));
    }
    
    @Test
    void assertDispatchServiceCapabilities() {
        Optional<MCPResourceResponse> actual = resourceDispatcher.dispatch("shardingsphere://capabilities", runtimeContext);
        
        assertTrue(actual.isPresent());
        assertThat(actual.orElseThrow(), org.hamcrest.Matchers.instanceOf(MCPServiceCapabilityResponse.class));
    }
    
    @Test
    void assertDispatchDatabaseCapabilities() {
        Optional<MCPResourceResponse> actual = resourceDispatcher.dispatch("shardingsphere://databases/logic_db/capabilities", runtimeContext);
        
        assertTrue(actual.isPresent());
        MCPResourceResponse actualResult = actual.orElseThrow();
        assertThat(actualResult, org.hamcrest.Matchers.instanceOf(MCPDatabaseCapabilityResponse.class));
        Map<String, Object> actualPayload = actualResult.toPayload(payloadBuilder);
        assertThat(actualPayload.get("databaseType"), is("MySQL"));
    }
    
    @Test
    void assertDispatchMetadataColumns() {
        Optional<MCPResourceResponse> actual = resourceDispatcher.dispatch("shardingsphere://databases/logic_db/schemas/public/tables/orders/columns/order_id", runtimeContext);
        
        assertTrue(actual.isPresent());
        MCPResourceResponse actualResult = actual.orElseThrow();
        assertThat(actualResult, org.hamcrest.Matchers.instanceOf(MCPMetadataResponse.class));
        Map<String, Object> actualPayload = actualResult.toPayload(payloadBuilder);
        List<MetadataObject> actualItems = getMetadataObjects(actualPayload);
        assertThat(actualItems.size(), is(1));
        assertThat(actualItems.get(0).getName(), is("order_id"));
    }
    
    @Test
    void assertDispatchInvalidUri() {
        Optional<MCPResourceResponse> actual = resourceDispatcher.dispatch("shardingsphere://unknown", runtimeContext);
        
        assertFalse(actual.isPresent());
    }
    
    @SuppressWarnings("unchecked")
    private List<MetadataObject> getMetadataObjects(final Map<String, Object> payload) {
        return (List<MetadataObject>) payload.get("items");
    }
}
