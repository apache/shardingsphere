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

import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionResourceManager;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MCPSessionManagerTest {
    
    @Test
    void assertCreateSession() {
        assertDoesNotThrow(() -> new MCPSessionManager(mock()).createSession("session-1"));
    }
    
    @Test
    void assertCreateSessionWithDuplicateSessionId() {
        MCPSessionManager sessionManager = new MCPSessionManager(mock());
        sessionManager.createSession("session-1");
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> sessionManager.createSession("session-1"));
        assertThat(actual.getMessage(), is("Session already exists."));
    }
    
    @Test
    void assertHasSession() {
        MCPSessionManager sessionManager = new MCPSessionManager(mock());
        sessionManager.createSession("session-1");
        assertTrue(sessionManager.hasSession("session-1"));
    }
    
    @Test
    void assertCloseSession() {
        MCPSessionManager sessionManager = new MCPSessionManager(mock());
        sessionManager.createSession("session-1");
        assertTrue(sessionManager.hasSession("session-1"));
        sessionManager.closeSession("session-1");
        assertFalse(sessionManager.hasSession("session-1"));
    }
    
    @Test
    void assertCloseSessionWithTransactionResourceManager() {
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        MCPSessionManager sessionManager = new MCPSessionManager(transactionResourceManager);
        sessionManager.createSession("session-1");
        sessionManager.closeSession("session-1");
        verify(transactionResourceManager).closeSession("session-1");
        assertFalse(sessionManager.hasSession("session-1"));
    }
    
    @Test
    void assertCloseSessionWithTransactionResourceManagerFailure() {
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        doThrow(new IllegalStateException("cleanup failed")).when(transactionResourceManager).closeSession("session-1");
        MCPSessionManager sessionManager = new MCPSessionManager(transactionResourceManager);
        sessionManager.createSession("session-1");
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> sessionManager.closeSession("session-1"));
        assertThat(actual.getMessage(), is("cleanup failed"));
        verify(transactionResourceManager).closeSession("session-1");
        assertFalse(sessionManager.hasSession("session-1"));
    }
    
    @Test
    void assertCloseAllSessions() {
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        MCPSessionManager sessionManager = new MCPSessionManager(transactionResourceManager);
        sessionManager.createSession("session-1");
        sessionManager.createSession("session-2");
        sessionManager.closeAllSessions();
        verify(transactionResourceManager).closeSession("session-1");
        verify(transactionResourceManager).closeSession("session-2");
        assertFalse(sessionManager.hasSession("session-1"));
        assertFalse(sessionManager.hasSession("session-2"));
    }
}
