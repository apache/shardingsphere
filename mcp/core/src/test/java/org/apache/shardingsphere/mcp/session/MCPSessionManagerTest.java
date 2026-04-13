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

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPSessionManagerTest {
    
    @Test
    void assertCreateSession() {
        assertDoesNotThrow(() -> new MCPSessionManager(Collections.emptyMap()).createSession("session-1"));
    }
    
    @Test
    void assertCreateSessionWithProtocolVersion() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession("session-1", "2025-06-18");
        assertThat(sessionManager.findProtocolVersion("session-1").orElse(""), is("2025-06-18"));
    }
    
    @Test
    void assertCreateSessionWithDuplicateSessionId() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession("session-1");
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> sessionManager.createSession("session-1"));
        assertThat(actual.getMessage(), is("Session already exists."));
    }
    
    @Test
    void assertHasSession() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession("session-1");
        assertTrue(sessionManager.hasSession("session-1"));
    }
    
    @Test
    void assertCloseSession() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession("session-1");
        assertTrue(sessionManager.hasSession("session-1"));
        sessionManager.closeSession("session-1");
        assertFalse(sessionManager.hasSession("session-1"));
    }
    
    @Test
    void assertCloseSessionWithTransactionResourceManager() throws SQLException {
        Connection connection = mock(Connection.class);
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of("logic_db", runtimeDatabaseConfig));
        sessionManager.createSession("session-1");
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
        sessionManager.createSession("session-1");
        sessionManager.getTransactionResourceManager().beginTransaction("session-1", "logic_db");
        doThrow(new SQLException("cleanup failed")).when(connection).rollback();
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> sessionManager.closeSession("session-1"));
        assertThat(actual.getMessage(), is("cleanup failed"));
        verify(connection).rollback();
        assertFalse(sessionManager.hasSession("session-1"));
    }
    
    @Test
    void assertCloseAllSessions() throws SQLException {
        Connection firstConnection = mock(Connection.class);
        Connection secondConnection = mock(Connection.class);
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(firstConnection, secondConnection);
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of("logic_db", runtimeDatabaseConfig));
        sessionManager.createSession("session-1");
        sessionManager.createSession("session-2");
        sessionManager.getTransactionResourceManager().beginTransaction("session-1", "logic_db");
        sessionManager.getTransactionResourceManager().beginTransaction("session-2", "logic_db");
        sessionManager.closeAllSessions();
        verify(firstConnection).rollback();
        verify(firstConnection).setAutoCommit(true);
        verify(firstConnection).close();
        verify(secondConnection).rollback();
        verify(secondConnection).setAutoCommit(true);
        verify(secondConnection).close();
        assertFalse(sessionManager.hasSession("session-1"));
        assertFalse(sessionManager.hasSession("session-2"));
    }
}
