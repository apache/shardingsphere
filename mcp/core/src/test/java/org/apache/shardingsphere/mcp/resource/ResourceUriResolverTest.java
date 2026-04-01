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

import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.resource.ResourceUriResolution.ResourceUriResolutionType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceUriResolverTest {
    
    private final ResourceUriResolver resourceUriResolver = new ResourceUriResolver();
    
    @Test
    void assertGetSupportedResources() {
        assertThat(resourceUriResolver.getSupportedResources().size(), is(16));
        assertThat(resourceUriResolver.getSupportedResources().get(0), is("shardingsphere://capabilities"));
        assertThat(resourceUriResolver.getSupportedResources().get(15), is("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}"));
    }
    
    @Test
    void assertResolveServiceCapabilities() {
        Optional<ResourceUriResolution> actual = resourceUriResolver.resolve("shardingsphere://capabilities");
        
        assertTrue(actual.isPresent());
        assertThat(actual.get().getType(), is(ResourceUriResolutionType.SERVICE_CAPABILITIES));
    }
    
    @Test
    void assertResolveDatabaseCapabilities() {
        Optional<ResourceUriResolution> actual = resourceUriResolver.resolve("shardingsphere://databases/logic_db/capabilities");
        
        assertTrue(actual.isPresent());
        assertThat(actual.get().getType(), is(ResourceUriResolutionType.DATABASE_CAPABILITIES));
        assertThat(actual.get().getDatabase().orElse(""), is("logic_db"));
    }
    
    @Test
    void assertResolveMetadataColumns() {
        Optional<ResourceUriResolution> actual = resourceUriResolver.resolve("shardingsphere://databases/logic_db/schemas/public/tables/orders/columns/order_id");
        
        assertTrue(actual.isPresent());
        assertThat(actual.get().getType(), is(ResourceUriResolutionType.METADATA));
        assertThat(actual.get().getResourceRequest().orElseThrow().getDatabase(), is("logic_db"));
        assertThat(actual.get().getResourceRequest().orElseThrow().getSchema(), is("public"));
        assertThat(actual.get().getResourceRequest().orElseThrow().getObjectType(), is(MetadataObjectType.COLUMN));
        assertThat(actual.get().getResourceRequest().orElseThrow().getObjectName(), is("order_id"));
        assertThat(actual.get().getResourceRequest().orElseThrow().getParentObjectType(), is("TABLE"));
        assertThat(actual.get().getResourceRequest().orElseThrow().getParentObjectName(), is("orders"));
    }
    
    @Test
    void assertResolveInvalidUri() {
        Optional<ResourceUriResolution> actual = resourceUriResolver.resolve("shardingsphere://unknown");
        
        assertFalse(actual.isPresent());
    }
}
