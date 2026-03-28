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

package org.apache.shardingsphere.mcp.bootstrap.transport.lifecycle;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TransportSessionRegistryTest {
    
    @Test
    void assertCreate() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        when(runtimeContext.getSessionManager()).thenReturn(sessionManager);
        TransportSessionRegistry registry = new TransportSessionRegistry(runtimeContext);
        registry.create("session-id");
        verify(sessionManager).createSession("session-id");
    }
    
    @Test
    void assertClose() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        when(runtimeContext.getSessionManager()).thenReturn(sessionManager);
        TransportSessionRegistry registry = new TransportSessionRegistry(runtimeContext);
        registry.create("session-id");
        registry.close("session-id");
        verify(sessionManager).createSession("session-id");
        verify(runtimeContext).closeSession("session-id");
    }
    
    @Test
    void assertCloseAll() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        when(runtimeContext.getSessionManager()).thenReturn(sessionManager);
        TransportSessionRegistry registry = new TransportSessionRegistry(runtimeContext);
        registry.create("session-id");
        clearInvocations(runtimeContext, sessionManager);
        registry.closeAll();
        registry.closeAll();
        verify(runtimeContext).closeSession("session-id");
        verifyNoInteractions(sessionManager);
    }
}
