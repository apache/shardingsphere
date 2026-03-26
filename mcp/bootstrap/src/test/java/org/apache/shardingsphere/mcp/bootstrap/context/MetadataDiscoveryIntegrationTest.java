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

package org.apache.shardingsphere.mcp.bootstrap.context;

import org.apache.shardingsphere.mcp.capability.ServiceCapability;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataDiscoveryIntegrationTest {
    
    private MCPRuntimeServices createRuntimeServices() {
        return new MCPRuntimeServices(new MCPSessionManager(), new MetadataCatalog(Collections.emptyMap(), Collections.emptyList()),
                new DatabaseRuntime(Collections.emptyMap(), Collections.emptyMap()));
    }
    
    @Test
    void assertDefaultResources() {
        ServiceCapability actual = createRuntimeServices().getCapabilityAssembler().assembleServiceCapability();
        
        assertTrue(actual.getSupportedResources().contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes"));
        assertTrue(actual.getSupportedResources().contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}"));
    }
    
    @Test
    void assertDefaultServiceCapability() {
        ServiceCapability actual = createRuntimeServices().getCapabilityAssembler().assembleServiceCapability();
        
        assertTrue(actual.getSupportedTools().contains("execute_query"));
        assertTrue(actual.getSupportedResources().contains("shardingsphere://capabilities"));
    }
    
    @Test
    void assertDefaultTools() {
        ServiceCapability actual = createRuntimeServices().getCapabilityAssembler().assembleServiceCapability();
        
        assertTrue(actual.getSupportedTools().contains("search_metadata"));
        assertTrue(actual.getSupportedTools().contains("describe_table"));
    }
}
