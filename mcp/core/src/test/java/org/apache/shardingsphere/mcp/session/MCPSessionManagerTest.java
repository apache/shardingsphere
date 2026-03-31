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

import org.apache.shardingsphere.mcp.session.MCPSessionManager.MCPSessionContext;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPSessionManagerTest {
    
    @Test
    void assertCreateSession() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPSessionContext actual = sessionManager.createSession("session-1");
        assertThat(actual.getSessionId(), is("session-1"));
        assertFalse(actual.isClosed());
    }
    
    @Test
    void assertHasSession() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        assertTrue(sessionManager.hasSession("session-1"));
    }
    
    @Test
    void assertCloseSession() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        MCPSessionContext sessionContext = sessionManager.getSession("session-1");
        sessionManager.closeSession("session-1");
        assertTrue(sessionContext.isClosed());
    }
}
