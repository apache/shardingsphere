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

package org.apache.shardingsphere.mcp.session;

import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class MCPSessionLifecycleRegistryTest {
    
    @Test
    void assertCreate() {
        DatabaseRuntime databaseRuntime = mock(DatabaseRuntime.class);
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        MCPSessionLifecycleRegistry registry = new MCPSessionLifecycleRegistry(databaseRuntime, sessionManager);
        
        registry.create("session-id");
        
        verify(sessionManager).createSession("session-id");
    }
    
    @Test
    void assertClose() {
        DatabaseRuntime databaseRuntime = mock(DatabaseRuntime.class);
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        MCPSessionLifecycleRegistry registry = new MCPSessionLifecycleRegistry(databaseRuntime, sessionManager);
        registry.create("session-id");
        clearInvocations(databaseRuntime, sessionManager);
        
        registry.close("session-id");
        
        verify(databaseRuntime).closeSession("session-id");
        verify(sessionManager).closeSession("session-id");
    }
    
    @Test
    void assertCloseAll() {
        DatabaseRuntime databaseRuntime = mock(DatabaseRuntime.class);
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        MCPSessionLifecycleRegistry registry = new MCPSessionLifecycleRegistry(databaseRuntime, sessionManager);
        registry.create("session-id");
        clearInvocations(databaseRuntime, sessionManager);
        
        registry.closeAll();
        registry.closeAll();
        
        verify(databaseRuntime).closeSession("session-id");
        verify(sessionManager).closeSession("session-id");
        verifyNoMoreInteractions(databaseRuntime, sessionManager);
    }
    
    @Test
    void assertCleanup() {
        DatabaseRuntime databaseRuntime = mock(DatabaseRuntime.class);
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        MCPSessionLifecycleRegistry registry = new MCPSessionLifecycleRegistry(databaseRuntime, sessionManager);
        
        registry.cleanup("session-id");
        
        verify(databaseRuntime).closeSession("session-id");
        verify(sessionManager).closeSession("session-id");
    }
}
