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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPServerContextTest {
    
    @Test
    void assertRegisterResource() {
        MCPServerContext serverContext = new MCPServerContext(new MCPSessionManager());
        
        serverContext.registerResource(" capability ");
        
        assertTrue(serverContext.snapshot().getResources().contains("capability"));
    }
    
    @Test
    void assertRegisterTool() {
        MCPServerContext serverContext = new MCPServerContext(new MCPSessionManager());
        
        serverContext.registerTool(" execute_query ");
        
        assertTrue(serverContext.snapshot().getTools().contains("execute_query"));
    }
    
    @Test
    void assertStart() {
        MCPServerContext serverContext = new MCPServerContext(new MCPSessionManager());
        
        serverContext.start();
        
        assertTrue(serverContext.isRunning());
    }
    
    @Test
    void assertStop() {
        MCPServerContext serverContext = new MCPServerContext(new MCPSessionManager());
        serverContext.start();
        
        serverContext.stop();
        
        assertFalse(serverContext.isRunning());
    }
    
    @Test
    void assertSnapshot() {
        MCPServerContext serverContext = new MCPServerContext(new MCPSessionManager());
        serverContext.registerResource("capability");
        serverContext.registerTool("execute_query");
        serverContext.start();
        
        MCPServerContext.RegistrationSnapshot actual = serverContext.snapshot();
        
        assertThat(actual.getResources().size(), is(1));
        assertThat(actual.getTools().size(), is(1));
        assertTrue(actual.isRunning());
    }
    
    @Test
    void assertRegisterResourceWithEmptyName() {
        MCPServerContext serverContext = new MCPServerContext(new MCPSessionManager());
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> serverContext.registerResource("   "));
        
        assertThat(actual.getMessage(), is("resourceName cannot be empty."));
    }
}
