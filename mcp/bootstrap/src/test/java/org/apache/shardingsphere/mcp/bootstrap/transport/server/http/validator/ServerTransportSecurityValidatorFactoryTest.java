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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator;

import io.modelcontextprotocol.server.transport.ServerTransportSecurityException;
import io.modelcontextprotocol.server.transport.ServerTransportSecurityValidator;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.SessionAttributionResolver;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ServerTransportSecurityValidatorFactoryTest {
    
    private static final SessionAttributionResolver DISABLED_SESSION_ATTRIBUTION_RESOLVER = new SessionAttributionResolver(null);
    
    @Test
    void assertCreateWithoutOptionalRules() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        ServerTransportSecurityValidator actual = ServerTransportSecurityValidatorFactory.create(sessionManager, "127.0.0.1", DISABLED_SESSION_ATTRIBUTION_RESOLVER);
        assertDoesNotThrow(() -> actual.validateHeaders(Map.of()));
        verifyNoInteractions(sessionManager);
    }
    
    @Test
    void assertCreateWithLoopbackOrigin() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        ServerTransportSecurityValidator actual = ServerTransportSecurityValidatorFactory.create(sessionManager, "127.0.0.1", DISABLED_SESSION_ATTRIBUTION_RESOLVER);
        assertDoesNotThrow(() -> actual.validateHeaders(Map.of("Origin", List.of("http://127.0.0.1:8080"))));
        verifyNoInteractions(sessionManager);
    }
    
    @Test
    void assertCreateWithLoopbackOriginRejectsRemoteOrigin() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        ServerTransportSecurityValidator validator = ServerTransportSecurityValidatorFactory.create(sessionManager, "127.0.0.1", DISABLED_SESSION_ATTRIBUTION_RESOLVER);
        ServerTransportSecurityException ex = assertThrows(ServerTransportSecurityException.class,
                () -> validator.validateHeaders(Map.of("Origin", List.of("http://example.com:8080"), "Mcp-Session-Id", List.of("session-id"))));
        assertThat(ex.getStatusCode(), is(403));
        verifyNoInteractions(sessionManager);
    }
    
    @Test
    void assertCreateWithNonLoopbackBindingAcceptsMissingOrigin() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        ServerTransportSecurityValidator actual = ServerTransportSecurityValidatorFactory.create(sessionManager, "0.0.0.0", DISABLED_SESSION_ATTRIBUTION_RESOLVER);
        assertDoesNotThrow(() -> actual.validateHeaders(Map.of()));
        verifyNoInteractions(sessionManager);
    }
    
    @Test
    void assertCreateWithNonLoopbackBindingRejectsPresentOrigin() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        ServerTransportSecurityValidator validator = ServerTransportSecurityValidatorFactory.create(sessionManager, "0.0.0.0", DISABLED_SESSION_ATTRIBUTION_RESOLVER);
        ServerTransportSecurityException ex = assertThrows(ServerTransportSecurityException.class,
                () -> validator.validateHeaders(Map.of("Origin", List.of("https://gateway.example.test"))));
        assertThat(ex.getStatusCode(), is(403));
        verifyNoInteractions(sessionManager);
    }
    
    @Test
    void assertCreateRejectsInvalidOrigin() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        ServerTransportSecurityValidator validator = ServerTransportSecurityValidatorFactory.create(sessionManager, "127.0.0.1", DISABLED_SESSION_ATTRIBUTION_RESOLVER);
        ServerTransportSecurityException ex = assertThrows(ServerTransportSecurityException.class, () -> validator.validateHeaders(Map.of("Origin", List.of("://bad-origin"))));
        assertThat(ex.getStatusCode(), is(403));
        verifyNoInteractions(sessionManager);
    }
    
    @Test
    void assertCreateWithProtocolVersionConstraintLast() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        when(sessionManager.hasSession("session-id")).thenReturn(true);
        ServerTransportSecurityValidator validator = ServerTransportSecurityValidatorFactory.create(sessionManager, "127.0.0.1", DISABLED_SESSION_ATTRIBUTION_RESOLVER);
        ServerTransportSecurityException ex = assertThrows(ServerTransportSecurityException.class,
                () -> validator.validateHeaders(Map.of("Mcp-Session-Id", List.of("session-id"))));
        assertThat(ex.getStatusCode(), is(400));
    }
    
    @Test
    void assertCreateWithUnknownSessionSkipsProtocolVersionConstraint() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        ServerTransportSecurityValidator actual = ServerTransportSecurityValidatorFactory.create(sessionManager, "127.0.0.1", DISABLED_SESSION_ATTRIBUTION_RESOLVER);
        assertDoesNotThrow(() -> actual.validateHeaders(Map.of("Mcp-Session-Id", List.of("unknown-session"))));
    }
}
