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

import org.apache.shardingsphere.mcp.bootstrap.server.MCPServerRegistry;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPRuntimeServicesTest {
    
    @Test
    void assertConstruct() {
        MCPRuntimeServices actual = new MCPRuntimeServices(new MCPSessionManager());
        
        assertNotNull(actual.getCapabilityAssembler());
        assertNotNull(actual.getMetadataResourceLoader());
        assertNotNull(actual.getMetadataToolDispatcher());
        assertNotNull(actual.getTransactionCommandExecutor());
        assertNotNull(actual.getAuditRecorder());
        assertNotNull(actual.getMetadataRefreshCoordinator());
        assertNotNull(actual.getExecuteQueryFacade());
    }
    
    @Test
    void assertRegisterDefaults() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPRuntimeServices runtimeServices = new MCPRuntimeServices(sessionManager);
        MCPServerRegistry serverRegistry = new MCPServerRegistry(sessionManager);
        
        runtimeServices.registerDefaults(serverRegistry);
        MCPServerRegistry.RegistrationSnapshot actual = serverRegistry.snapshot();
        
        assertThat(actual.getResources().size(), is(runtimeServices.getCapabilityAssembler().assembleServiceCapability().getSupportedResources().size()));
        assertThat(actual.getTools().size(), is(runtimeServices.getCapabilityAssembler().assembleServiceCapability().getSupportedTools().size()));
        assertTrue(actual.getResources().contains("shardingsphere://capabilities"));
        assertTrue(actual.getTools().contains("execute_query"));
    }
}
