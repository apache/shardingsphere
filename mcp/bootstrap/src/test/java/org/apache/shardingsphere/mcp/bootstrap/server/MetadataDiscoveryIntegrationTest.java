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

package org.apache.shardingsphere.mcp.bootstrap.server;

import org.apache.shardingsphere.mcp.bootstrap.wiring.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataDiscoveryIntegrationTest extends AbstractMCPIntegrationTest {
    
    private MCPRuntimeContext runtimeContext;
    
    @Override
    protected MCPServerContext createServerContext() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        runtimeContext = new MCPRuntimeContext(sessionManager);
        MCPServerContext result = new MCPServerContext(sessionManager);
        runtimeContext.registerDefaults(result);
        return result;
    }
    
    @Test
    void assertRegisterResourceDefaults() {
        MCPServerContext.RegistrationSnapshot actual = getServerContext().snapshot();
        
        assertThat(actual.getResources().size(), is(runtimeContext.getCapabilityAssembler().assembleServiceCapability().getSupportedResources().size()));
        assertTrue(actual.getResources().contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes"));
        assertTrue(getServerContext().snapshot().getResources().contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}"));
    }
    
    @Test
    void assertRegisterDefaults() {
        MCPServerContext.RegistrationSnapshot actual = getServerContext().snapshot();
        
        assertThat(actual.getResources().size(), is(runtimeContext.getCapabilityAssembler().assembleServiceCapability().getSupportedResources().size()));
        assertThat(actual.getTools().size(), is(runtimeContext.getCapabilityAssembler().assembleServiceCapability().getSupportedTools().size()));
        assertTrue(actual.getTools().contains("execute_query"));
        assertTrue(actual.getResources().contains("shardingsphere://capabilities"));
    }
    
    @Test
    void assertRegisterToolDefaults() {
        MCPServerContext.RegistrationSnapshot actual = getServerContext().snapshot();
        
        assertThat(actual.getTools().size(), is(runtimeContext.getCapabilityAssembler().assembleServiceCapability().getSupportedTools().size()));
        assertTrue(actual.getTools().contains("search_metadata"));
        assertTrue(actual.getTools().contains("describe_table"));
    }
}
