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

import org.apache.shardingsphere.mcp.resource.ResourceQueryPlan;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceHandlerRegistryTest {
    
    @Test
    void assertGetHandlers() {
        ResourceHandlerRegistry actual = new ResourceHandlerRegistry();
        
        assertThat(actual.getHandlers().size(), is(16));
    }
    
    @Test
    void assertGetSupportedResources() {
        ResourceHandlerRegistry actual = new ResourceHandlerRegistry();
        
        assertThat(actual.getSupportedResources().size(), is(16));
        assertThat(actual.getSupportedResources().get(0), is("shardingsphere://capabilities"));
        assertThat(actual.getSupportedResources().get(15), is("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}"));
    }
    
    @Test
    void assertCreateRegistryWithDuplicateTemplate() {
        assertThrows(IllegalArgumentException.class, () -> new ResourceHandlerRegistry(List.of(
                new TestResourceHandler("shardingsphere://capabilities"),
                new TestResourceHandler("shardingsphere://capabilities"))));
    }
    
    @Test
    void assertCreateRegistryWithDuplicateRouteSignature() {
        assertThrows(IllegalArgumentException.class, () -> new ResourceHandlerRegistry(List.of(
                new TestResourceHandler("shardingsphere://databases/{database}"),
                new TestResourceHandler("shardingsphere://databases/{logic_db}"))));
    }
    
    @Test
    void assertCreateRegistryWithNoHandlers() {
        assertThrows(IllegalStateException.class, () -> new ResourceHandlerRegistry(List.of()));
    }
    
    private static final class TestResourceHandler implements ResourceHandler {
        
        private final String uriTemplate;
        
        private TestResourceHandler(final String uriTemplate) {
            this.uriTemplate = uriTemplate;
        }
        
        @Override
        public String getUriTemplate() {
            return uriTemplate;
        }
        
        @Override
        public ResourceQueryPlan handle(final ResourceUriMatch uriMatch) {
            return ResourceQueryPlan.serviceCapabilities();
        }
    }
}
