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

class MCPResourceResultTest {
    
    @Test
    void assertServiceCapability() {
        MCPResourceResult actual = MCPResourceResult.serviceCapability(ResourceTestDataFactory.createRuntimeContext().getCapabilityBuilder().buildServiceCapability());
        assertThat(actual.getType(), is(MCPResourceResult.ResourceResultType.SERVICE_CAPABILITY));
        assertTrue(actual.getServiceCapability().getSupportedResources().contains("shardingsphere://capabilities"));
    }
    
    @Test
    void assertDatabaseCapability() {
        MCPResourceResult actual = MCPResourceResult.databaseCapability(
                ResourceTestDataFactory.createRuntimeContext().getCapabilityBuilder().buildDatabaseCapability("logic_db").orElseThrow());
        assertThat(actual.getType(), is(MCPResourceResult.ResourceResultType.DATABASE_CAPABILITY));
        assertThat(actual.getDatabaseCapability().getDatabase(), is("logic_db"));
    }
    
    @Test
    void assertMetadata() {
        MCPResourceResult actual = MCPResourceResult.metadata(List.of(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders", "", "")));
        assertThat(actual.getType(), is(MCPResourceResult.ResourceResultType.METADATA));
        assertThat(actual.getMetadataObjects().size(), is(1));
        assertThat(actual.getMetadataObjects().get(0).getObjectType(), is(MetadataObjectType.TABLE));
    }
    
    @Test
    void assertError() {
        MCPResourceResult actual = MCPResourceResult.error(MCPErrorCode.NOT_FOUND, "Database capability does not exist.");
        assertThat(actual.getType(), is(MCPResourceResult.ResourceResultType.ERROR));
        assertThat(actual.getErrorCode(), is(MCPErrorCode.NOT_FOUND));
        assertThat(actual.getMessage(), is("Database capability does not exist."));
    }
}
