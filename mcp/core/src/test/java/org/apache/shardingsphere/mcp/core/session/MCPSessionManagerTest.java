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

package org.apache.shardingsphere.mcp.core.session;

import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPSessionManagerTest {
    
    @Test
    void assertCreateSession() {
        assertDoesNotThrow(() -> new MCPSessionManager(Collections.emptyMap()).createSession(new MCPSessionIdentity("session-1", "", "", Map.of())));
    }
    
    @Test
    void assertCreateSessionWithDuplicateSessionId() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of())));
        assertThat(actual.getMessage(), is("Session already exists."));
    }
    
    @Test
    void assertHasSession() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        assertTrue(sessionManager.hasSession("session-1"));
    }
    
    @Test
    void assertCloseSession() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        assertTrue(sessionManager.hasSession("session-1"));
        sessionManager.closeSession("session-1");
        assertFalse(sessionManager.hasSession("session-1"));
    }
    
    @Test
    void assertCloseSessionInvokesSessionCloseListener() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        List<String> actualClosedSessions = new LinkedList<>();
        sessionManager.addSessionCloseListener(actualClosedSessions::add);
        sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        sessionManager.closeSession("session-1");
        assertThat(actualClosedSessions, is(List.of("session-1")));
    }
    
    @Test
    void assertCloseListenerCompletesBeforeSessionCanBeRecreated() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        sessionManager.addSessionCloseListener(sessionId -> assertThrows(IllegalStateException.class, () -> sessionManager.createSession(new MCPSessionIdentity(sessionId, "", "", Map.of()))));
        sessionManager.closeSession("session-1");
        assertDoesNotThrow(() -> sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of())));
    }
    
    @Test
    void assertCloseSessionWithTransactionResourceManager() throws SQLException {
        Connection connection = mock(Connection.class);
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of("logic_db", runtimeDatabaseConfig));
        sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        sessionManager.getTransactionResourceManager().beginTransaction("session-1", "logic_db");
        sessionManager.closeSession("session-1");
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
        assertFalse(sessionManager.hasSession("session-1"));
    }
    
    @Test
    void assertCloseSessionWithTransactionResourceManagerFailure() throws SQLException {
        Connection connection = mock(Connection.class);
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of("logic_db", runtimeDatabaseConfig));
        sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        sessionManager.getTransactionResourceManager().beginTransaction("session-1", "logic_db");
        doThrow(new SQLException("cleanup failed")).when(connection).rollback();
        assertDoesNotThrow(() -> sessionManager.closeSession("session-1"));
        verify(connection).rollback();
        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
        assertFalse(sessionManager.hasSession("session-1"));
    }
    
    @Test
    void assertCloseAllSessions() throws SQLException {
        Connection firstConnection = mock(Connection.class);
        Connection secondConnection = mock(Connection.class);
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(firstConnection, secondConnection);
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of("logic_db", runtimeDatabaseConfig));
        sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        sessionManager.createSession(new MCPSessionIdentity("session-2", "", "", Map.of()));
        sessionManager.getTransactionResourceManager().beginTransaction("session-1", "logic_db");
        sessionManager.getTransactionResourceManager().beginTransaction("session-2", "logic_db");
        new MCPSessionExecutionCoordinator(sessionManager).closeAllSessions();
        verify(firstConnection).rollback();
        verify(firstConnection).setAutoCommit(true);
        verify(firstConnection).close();
        verify(secondConnection).rollback();
        verify(secondConnection).setAutoCommit(true);
        verify(secondConnection).close();
        assertFalse(sessionManager.hasSession("session-1"));
        assertFalse(sessionManager.hasSession("session-2"));
    }
    
    @Test
    void assertFindSessionIdentity() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        MCPSessionIdentity sessionIdentity = new MCPSessionIdentity("session-1", "subject", "gateway", Map.of("region", "ap-south"));
        sessionManager.createSession(sessionIdentity);
        assertThat(sessionManager.findSessionIdentity("session-1"), is(Optional.of(sessionIdentity)));
    }
    
    @Test
    void assertGetRequiredSessionIdentity() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        MCPSessionIdentity sessionIdentity = new MCPSessionIdentity("session-1", "subject", "gateway", Map.of("region", "ap-south"));
        sessionManager.createSession(sessionIdentity);
        assertThat(sessionManager.getRequiredSessionIdentity("session-1"), is(sessionIdentity));
    }
    
    @Test
    void assertGetRequiredSessionIdentityWithMissingSession() {
        assertThrows(MCPSessionNotExistedException.class, () -> new MCPSessionManager(Collections.emptyMap()).getRequiredSessionIdentity("session-1"));
    }
    
    @Test
    void assertCloseSessionRemovesSessionIdentity() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "subject", "gateway", Map.of()));
        sessionManager.closeSession("session-1");
        assertThat(sessionManager.findSessionIdentity("session-1"), is(Optional.empty()));
    }
}
