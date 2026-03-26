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

import org.apache.shardingsphere.mcp.session.MCPSessionManager.SessionContext;
import org.apache.shardingsphere.mcp.session.MCPSessionManager.TransactionState;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPSessionManagerTest {
    
    @Test
    void assertCreateSession() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        
        SessionContext actual = sessionManager.createSession("session-1");
        
        assertThat(actual.getSessionId(), is("session-1"));
        assertTrue(actual.isAutocommit());
    }
    
    @Test
    void assertFindSession() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        
        Optional<SessionContext> actual = sessionManager.findSession("session-1");
        
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSessionId(), is("session-1"));
    }
    
    @Test
    void assertBindDatabase() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        
        sessionManager.bindDatabase("session-1", "logic_db");
        
        assertThat(sessionManager.findSession("session-1").get().getBoundDatabase(), is("logic_db"));
    }
    
    @Test
    void assertBindDatabaseWithCrossDatabaseTransactionSwitch() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        sessionManager.beginTransaction("session-1", "logic_db");
        
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> sessionManager.bindDatabase("session-1", "other_db"));
        
        assertThat(actual.getMessage(), is("Cross-database transaction switching is not supported."));
    }
    
    @Test
    void assertBeginTransaction() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        
        sessionManager.beginTransaction("session-1", "logic_db");
        
        SessionContext actual = sessionManager.findSession("session-1").get();
        assertFalse(actual.isAutocommit());
        assertThat(actual.getTransactionState(), is(TransactionState.ACTIVE));
        assertThat(actual.getBoundDatabase(), is("logic_db"));
    }
    
    @Test
    void assertRememberSavepoint() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        sessionManager.beginTransaction("session-1", "logic_db");
        
        sessionManager.rememberSavepoint("session-1", "sp_1");
        
        assertTrue(sessionManager.findSession("session-1").get().getSavepoints().contains("sp_1"));
    }
    
    @Test
    void assertCommitTransaction() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        sessionManager.beginTransaction("session-1", "logic_db");
        sessionManager.rememberSavepoint("session-1", "sp_1");
        
        sessionManager.commitTransaction("session-1");
        
        SessionContext actual = sessionManager.findSession("session-1").get();
        assertTrue(actual.isAutocommit());
        assertThat(actual.getTransactionState(), is(TransactionState.IDLE));
        assertThat(actual.getSavepoints().size(), is(0));
    }
    
    @Test
    void assertRollbackTransaction() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        sessionManager.beginTransaction("session-1", "logic_db");
        sessionManager.rememberSavepoint("session-1", "sp_1");
        
        sessionManager.rollbackTransaction("session-1");
        
        SessionContext actual = sessionManager.findSession("session-1").get();
        assertTrue(actual.isAutocommit());
        assertThat(actual.getTransactionState(), is(TransactionState.IDLE));
        assertThat(actual.getSavepoints().size(), is(0));
    }
    
    @Test
    void assertRollbackToSavepoint() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        sessionManager.beginTransaction("session-1", "logic_db");
        sessionManager.rememberSavepoint("session-1", "sp_1");
        
        sessionManager.rollbackToSavepoint("session-1", "sp_1");
        
        assertTrue(sessionManager.findSession("session-1").get().getSavepoints().contains("sp_1"));
    }
    
    @Test
    void assertReleaseSavepoint() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        sessionManager.beginTransaction("session-1", "logic_db");
        sessionManager.rememberSavepoint("session-1", "sp_1");
        
        sessionManager.releaseSavepoint("session-1", "sp_1");
        
        assertFalse(sessionManager.findSession("session-1").get().getSavepoints().contains("sp_1"));
    }
    
    @Test
    void assertHasSession() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        
        assertTrue(sessionManager.hasSession("session-1"));
    }
    
    @Test
    void assertCreateSessionWithClosedSessionId() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        sessionManager.closeSession("session-1");
        
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> sessionManager.createSession("session-1"));
        
        assertThat(actual.getMessage(), is("Session recovery is not supported."));
    }
    
    @Test
    void assertCloseSession() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        sessionManager.beginTransaction("session-1", "logic_db");
        sessionManager.rememberSavepoint("session-1", "sp_1");
        SessionContext sessionContext = sessionManager.findSession("session-1").get();
        
        sessionManager.closeSession("session-1");
        
        assertFalse(sessionManager.findSession("session-1").isPresent());
        assertTrue(sessionContext.isClosed());
        assertTrue(sessionContext.isAutocommit());
        assertThat(sessionContext.getTransactionState(), is(TransactionState.IDLE));
        assertThat(sessionContext.getSavepoints().size(), is(0));
    }
}
