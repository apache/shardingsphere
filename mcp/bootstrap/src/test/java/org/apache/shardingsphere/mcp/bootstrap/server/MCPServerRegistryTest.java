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

import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPServerRegistryTest {
    
    @Test
    void assertGetSessionManager() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPServerRegistry serverRegistry = new MCPServerRegistry(sessionManager);
        
        assertThat(serverRegistry.getSessionManager(), is(sessionManager));
    }
    
    @Test
    void assertRegisterResource() {
        MCPServerRegistry serverRegistry = new MCPServerRegistry(new MCPSessionManager());
        
        serverRegistry.registerResource(" capability ");
        
        assertTrue(serverRegistry.snapshot().getResources().contains("capability"));
    }
    
    @Test
    void assertRegisterTool() {
        MCPServerRegistry serverRegistry = new MCPServerRegistry(new MCPSessionManager());
        
        serverRegistry.registerTool(" execute_query ");
        
        assertTrue(serverRegistry.snapshot().getTools().contains("execute_query"));
    }
    
    @Test
    void assertStart() {
        MCPServerRegistry serverRegistry = new MCPServerRegistry(new MCPSessionManager());
        
        serverRegistry.start();
        
        assertTrue(serverRegistry.isRunning());
    }
    
    @Test
    void assertStop() {
        MCPServerRegistry serverRegistry = new MCPServerRegistry(new MCPSessionManager());
        serverRegistry.start();
        
        serverRegistry.stop();
        
        assertFalse(serverRegistry.isRunning());
    }
    
    @Test
    void assertSnapshot() {
        MCPServerRegistry serverRegistry = new MCPServerRegistry(new MCPSessionManager());
        serverRegistry.registerResource("capability");
        serverRegistry.registerTool("execute_query");
        serverRegistry.start();
        
        MCPServerRegistry.RegistrationSnapshot actual = serverRegistry.snapshot();
        
        assertThat(actual.getResources().size(), is(1));
        assertThat(actual.getTools().size(), is(1));
        assertTrue(actual.isRunning());
    }
    
    @Test
    void assertRegisterResourceWithEmptyName() {
        MCPServerRegistry serverRegistry = new MCPServerRegistry(new MCPSessionManager());
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> serverRegistry.registerResource("   "));
        
        assertThat(actual.getMessage(), is("resourceName cannot be empty."));
    }
    
    @Test
    void assertRegisterToolWithEmptyName() {
        MCPServerRegistry serverRegistry = new MCPServerRegistry(new MCPSessionManager());
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> serverRegistry.registerTool("   "));
        
        assertThat(actual.getMessage(), is("toolName cannot be empty."));
    }
}
