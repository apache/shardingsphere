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

package org.apache.shardingsphere.mcp.resource.dispatch;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.resource.MetadataResourceResult;
import org.apache.shardingsphere.mcp.resource.MCPResourceResponse;
import org.apache.shardingsphere.mcp.resource.ResourceTestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceDispatcherTest {
    
    private final ResourceDispatcher resourceDispatcher = new ResourceDispatcher();
    
    private final MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
    
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
        assertThat(actual.orElseThrow().getType(), is(MCPResourceResponse.ResourceResponseType.SERVICE_CAPABILITY));
    }
    
    @Test
    void assertDispatchDatabaseCapabilities() {
        Optional<MCPResourceResponse> actual = resourceDispatcher.dispatch("shardingsphere://databases/logic_db/capabilities", runtimeContext);
        
        assertTrue(actual.isPresent());
        MCPResourceResponse actualResult = actual.orElseThrow();
        assertThat(actualResult.getType(), is(MCPResourceResponse.ResourceResponseType.DATABASE_CAPABILITY));
        assertThat(actualResult.getDatabaseCapability().getDatabaseType(), is("MySQL"));
    }
    
    @Test
    void assertDispatchMetadataColumns() {
        Optional<MCPResourceResponse> actual = resourceDispatcher.dispatch("shardingsphere://databases/logic_db/schemas/public/tables/orders/columns/order_id", runtimeContext);
        
        assertTrue(actual.isPresent());
        MCPResourceResponse actualResult = actual.orElseThrow();
        assertThat(actualResult.getType(), is(MCPResourceResponse.ResourceResponseType.METADATA));
        MetadataResourceResult actualMetadataResult = actualResult.getMetadataResourceResult();
        assertTrue(actualMetadataResult.isSuccessful());
        assertThat(actualMetadataResult.getMetadataObjects().size(), is(1));
        assertThat(actualMetadataResult.getMetadataObjects().get(0).getName(), is("order_id"));
    }
    
    @Test
    void assertDispatchInvalidUri() {
        Optional<MCPResourceResponse> actual = resourceDispatcher.dispatch("shardingsphere://unknown", runtimeContext);
        
        assertFalse(actual.isPresent());
    }
}
