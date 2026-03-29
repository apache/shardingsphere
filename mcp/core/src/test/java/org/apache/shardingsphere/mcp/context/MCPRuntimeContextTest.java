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

import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class MCPRuntimeContextTest {
    
    @Test
    void assertCloseSession() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-id");
        DatabaseRuntime databaseRuntime = mock(DatabaseRuntime.class);
        MCPRuntimeContext runtimeContext = createRuntimeContext(sessionManager, databaseRuntime);
        
        runtimeContext.closeSession("session-id");
        
        assertFalse(sessionManager.hasSession("session-id"));
        verify(databaseRuntime).closeSession("session-id");
    }
    
    @Test
    void assertCloseSessionWithEmptySessionId() {
        DatabaseRuntime databaseRuntime = mock(DatabaseRuntime.class);
        MCPRuntimeContext runtimeContext = createRuntimeContext(new MCPSessionManager(), databaseRuntime);
        
        runtimeContext.closeSession("");
        
        verifyNoInteractions(databaseRuntime);
    }
    
    @Test
    void assertCloseSessionWithNullSessionId() {
        DatabaseRuntime databaseRuntime = mock(DatabaseRuntime.class);
        MCPRuntimeContext runtimeContext = createRuntimeContext(new MCPSessionManager(), databaseRuntime);
        
        runtimeContext.closeSession(null);
        
        verifyNoInteractions(databaseRuntime);
    }
    
    private MCPRuntimeContext createRuntimeContext(final MCPSessionManager sessionManager, final DatabaseRuntime databaseRuntime) {
        return MCPRuntimeContext.create(sessionManager, new MetadataCatalog(Collections.emptyMap(), Collections.emptyList()), databaseRuntime);
    }
}
