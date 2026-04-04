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
import org.apache.shardingsphere.mcp.protocol.error.MCPError;
import org.apache.shardingsphere.mcp.protocol.error.MCPError.MCPErrorCode;
import org.apache.shardingsphere.mcp.resource.response.MCPDatabaseCapabilityResponse;
import org.apache.shardingsphere.mcp.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.resource.response.MCPMetadataResponse;
import org.apache.shardingsphere.mcp.resource.response.MCPServiceCapabilityResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPResponseTest {
    
    @Test
    void assertServiceCapability() {
        Map<String, Object> actual = new MCPServiceCapabilityResponse(ResourceTestDataFactory.createRuntimeContext().getCapabilityBuilder().buildServiceCapability()).toPayload();
        assertTrue(((List<?>) actual.get("supportedResources")).contains("shardingsphere://capabilities"));
    }
    
    @Test
    void assertDatabaseCapability() {
        Map<String, Object> actual = new MCPDatabaseCapabilityResponse(
                ResourceTestDataFactory.createRuntimeContext().getCapabilityBuilder().buildDatabaseCapability("logic_db").orElseThrow()).toPayload();
        assertThat(actual.get("database"), is("logic_db"));
        assertThat(actual.get("databaseType"), is("MySQL"));
    }
    
    @Test
    void assertMetadata() {
        Map<String, Object> actual = new MCPMetadataResponse(List.of(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders", "", ""))).toPayload();
        assertThat(((List<?>) actual.get("items")).size(), is(1));
    }
    
    @Test
    void assertError() {
        Map<String, Object> actual = new MCPErrorResponse(new MCPError(MCPErrorCode.NOT_FOUND, "Database capability does not exist.")).toPayload();
        assertThat(actual.get("error_code"), is("not_found"));
        assertThat(actual.get("message"), is("Database capability does not exist."));
    }
}
