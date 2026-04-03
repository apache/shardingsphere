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

import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPResourceResponseTest {
    
    @Test
    void assertServiceCapability() {
        MCPResourceResponse actual = MCPResourceResponse.serviceCapability(ResourceTestDataFactory.createRuntimeContext().getCapabilityBuilder().buildServiceCapability());
        assertThat(actual.getType(), is(MCPResourceResponse.ResourceResponseType.SERVICE_CAPABILITY));
        assertTrue(actual.getServiceCapability().getSupportedResources().contains("shardingsphere://capabilities"));
    }
    
    @Test
    void assertDatabaseCapability() {
        MCPResourceResponse actual = MCPResourceResponse.databaseCapability(
                ResourceTestDataFactory.createRuntimeContext().getCapabilityBuilder().buildDatabaseCapability("logic_db").orElseThrow());
        assertThat(actual.getType(), is(MCPResourceResponse.ResourceResponseType.DATABASE_CAPABILITY));
        assertThat(actual.getDatabaseCapability().getDatabase(), is("logic_db"));
    }
    
    @Test
    void assertMetadata() {
        MCPResourceResponse actual = MCPResourceResponse.metadata(
                MetadataResourceResult.success(List.of(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders", "", ""))));
        assertThat(actual.getType(), is(MCPResourceResponse.ResourceResponseType.METADATA));
        assertTrue(actual.getMetadataResourceResult().isSuccessful());
    }
    
    @Test
    void assertError() {
        MCPResourceResponse actual = MCPResourceResponse.error(MCPErrorCode.NOT_FOUND, "Database capability does not exist.");
        assertThat(actual.getType(), is(MCPResourceResponse.ResourceResponseType.ERROR));
        assertThat(actual.getErrorCode(), is(MCPErrorCode.NOT_FOUND));
        assertThat(actual.getMessage(), is("Database capability does not exist."));
    }
}
