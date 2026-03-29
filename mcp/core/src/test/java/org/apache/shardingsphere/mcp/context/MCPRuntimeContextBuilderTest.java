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

package org.apache.shardingsphere.mcp.context;

import org.apache.shardingsphere.mcp.capability.ServiceCapability;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPRuntimeContextBuilderTest {
    
    @Test
    void assertCreate() {
        MCPRuntimeContext actual = createRuntimeContext(new MCPSessionManager());
        
        assertNotNull(actual.getSessionLifecycleRegistry());
        assertNotNull(actual.getCapabilityAssembler());
        assertNotNull(actual.getMetadataResourceLoader());
        assertNotNull(actual.getResourceUriResolver());
        assertNotNull(actual.getMetadataToolDispatcher());
        assertNotNull(actual.getToolCatalog());
        assertNotNull(actual.getTransactionCommandExecutor());
        assertNotNull(actual.getAuditRecorder());
        assertNotNull(actual.getExecuteQueryFacade());
        assertNotNull(actual.getPayloadBuilder());
    }
    
    @Test
    void assertAssembleServiceCapability() {
        MCPRuntimeContext runtimeContext = createRuntimeContext(new MCPSessionManager());
        ServiceCapability actual = runtimeContext.getCapabilityAssembler().assembleServiceCapability();
        
        assertTrue(actual.getSupportedResources().contains("shardingsphere://capabilities"));
        assertTrue(actual.getSupportedTools().contains("execute_query"));
    }
    
    private MCPRuntimeContext createRuntimeContext(final MCPSessionManager sessionManager) {
        return MCPRuntimeContext.create(sessionManager, new MetadataCatalog(Collections.emptyMap(), Collections.emptyList()),
                new DatabaseRuntime(Collections.emptyMap(), Collections.emptyMap()));
    }
}
