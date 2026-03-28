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

package org.apache.shardingsphere.mcp.bootstrap.transport.session;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class MCPSessionRegistryTest {
    
    @Test
    void assertCreate() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        MCPSessionRegistry registry = new MCPSessionRegistry(createRuntimeContext(sessionManager));
        registry.create("session-id");
        verify(sessionManager).createSession("session-id");
    }
    
    @Test
    void assertClose() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        DatabaseRuntime databaseRuntime = mock(DatabaseRuntime.class);
        MCPSessionRegistry registry = new MCPSessionRegistry(createRuntimeContext(sessionManager, databaseRuntime));
        registry.create("session-id");
        registry.close("session-id");
        verify(sessionManager).createSession("session-id");
        verify(sessionManager).closeSession("session-id");
        verify(databaseRuntime).closeSession("session-id");
    }
    
    @Test
    void assertCloseAll() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        DatabaseRuntime databaseRuntime = mock(DatabaseRuntime.class);
        MCPSessionRegistry registry = new MCPSessionRegistry(createRuntimeContext(sessionManager, databaseRuntime));
        registry.create("session-id");
        registry.closeAll();
        registry.closeAll();
        verify(sessionManager).createSession("session-id");
        verify(sessionManager).closeSession("session-id");
        verify(databaseRuntime).closeSession("session-id");
        verifyNoMoreInteractions(databaseRuntime);
    }
    
    private MCPRuntimeContext createRuntimeContext(final MCPSessionManager sessionManager) {
        return createRuntimeContext(sessionManager, mock(DatabaseRuntime.class));
    }
    
    private MCPRuntimeContext createRuntimeContext(final MCPSessionManager sessionManager, final DatabaseRuntime databaseRuntime) {
        MCPRuntimeContext result = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
        when(result.getSessionManager()).thenReturn(sessionManager);
        when(result.getDatabaseRuntime()).thenReturn(databaseRuntime);
        return result;
    }
}
