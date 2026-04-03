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
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceHandlerResultTest {
    
    @Test
    void assertServiceCapability() {
        ResourceHandlerResult actual = ResourceHandlerResult.serviceCapability(new ResourceHandlerContext(ResourceTestDataFactory.createRuntimeContext()).getServiceCapability());
        assertThat(actual.getType(), is(ResourceHandlerResult.ResourceHandlerResultType.SERVICE_CAPABILITY));
        assertTrue(actual.getServiceCapability().isPresent());
        assertFalse(actual.getDatabaseCapability().isPresent());
    }
    
    @Test
    void assertDatabaseCapability() {
        ResourceHandlerResult actual = ResourceHandlerResult.databaseCapability(
                new ResourceHandlerContext(ResourceTestDataFactory.createRuntimeContext()).findDatabaseCapability("logic_db").orElseThrow());
        assertThat(actual.getType(), is(ResourceHandlerResult.ResourceHandlerResultType.DATABASE_CAPABILITY));
        assertThat(actual.getDatabaseCapability().orElseThrow().getDatabase(), is("logic_db"));
    }
    
    @Test
    void assertMetadata() {
        ResourceHandlerResult actual = ResourceHandlerResult.metadata(new ResourceHandlerContext(ResourceTestDataFactory.createRuntimeContext()).readMetadata(
                new MetadataResourceQuery("logic_db", "public", MetadataObjectType.TABLE, "", "", "")));
        assertThat(actual.getType(), is(ResourceHandlerResult.ResourceHandlerResultType.METADATA));
        assertTrue(actual.getMetadataResourceResult().orElseThrow().isSuccessful());
    }
    
    @Test
    void assertError() {
        ResourceHandlerResult actual = ResourceHandlerResult.error(MCPErrorCode.NOT_FOUND, "Database capability does not exist.");
        assertThat(actual.getType(), is(ResourceHandlerResult.ResourceHandlerResultType.ERROR));
        assertThat(actual.getErrorCode().orElseThrow(), is(MCPErrorCode.NOT_FOUND));
        assertThat(actual.getMessage(), is("Database capability does not exist."));
    }
}
