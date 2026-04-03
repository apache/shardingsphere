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
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceHandlerContextTest {
    
    private final ResourceHandlerContext resourceHandlerContext = new ResourceHandlerContext(ResourceTestDataFactory.createRuntimeContext());
    
    @Test
    void assertGetServiceCapability() {
        assertTrue(resourceHandlerContext.getServiceCapability().getSupportedResources().contains("shardingsphere://capabilities"));
    }
    
    @Test
    void assertFindDatabaseCapability() {
        assertThat(resourceHandlerContext.findDatabaseCapability("logic_db").orElseThrow().getDatabaseType(), is("MySQL"));
    }
    
    @Test
    void assertReadMetadata() {
        MetadataResourceResult actual = resourceHandlerContext.readMetadata(
                new MetadataResourceQuery("logic_db", "public", MetadataObjectType.TABLE, "", "", ""));
        assertTrue(actual.isSuccessful());
        assertThat(actual.getMetadataObjects().size(), is(2));
        assertThat(actual.getMetadataObjects().get(0).getName(), is("order_items"));
    }
    
    @Test
    void assertFindDatabaseCapabilityWithUnknownDatabase() {
        assertFalse(resourceHandlerContext.findDatabaseCapability("missing_db").isPresent());
    }
}
