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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.constraint;

import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.TransportHeaderConstraintException;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ProtocolVersionHeaderConstraintTest {
    
    @Test
    void assertValidateWithoutSessionId() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        ProtocolVersionHeaderConstraint actualConstraint = new ProtocolVersionHeaderConstraint(sessionManager);
        assertDoesNotThrow(() -> actualConstraint.validate(Map.of()));
        verifyNoInteractions(sessionManager);
    }
    
    @Test
    void assertValidateWithUnknownSession() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        when(sessionManager.hasSession("session-id")).thenReturn(false);
        ProtocolVersionHeaderConstraint actualConstraint = new ProtocolVersionHeaderConstraint(sessionManager);
        assertDoesNotThrow(() -> actualConstraint.validate(Map.of("Mcp-Session-Id", List.of("session-id"))));
        verify(sessionManager).hasSession("session-id");
        verifyNoMoreInteractions(sessionManager);
    }
    
    @Test
    void assertValidateWithMissingProtocolHeader() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        when(sessionManager.hasSession("session-id")).thenReturn(true);
        ProtocolVersionHeaderConstraint actualConstraint = new ProtocolVersionHeaderConstraint(sessionManager);
        TransportHeaderConstraintException actual = assertThrows(TransportHeaderConstraintException.class,
                () -> actualConstraint.validate(Map.of("Mcp-Session-Id", List.of("session-id"))));
        assertThat(actual.getStatusCode(), is(400));
        assertThat(actual.getMessage(), is("MCP-Protocol-Version header is required."));
    }
    
    @Test
    void assertValidateWithProtocolMismatch() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        when(sessionManager.hasSession("session-id")).thenReturn(true);
        when(sessionManager.findProtocolVersion("session-id")).thenReturn(Optional.of("2025-06-18"));
        ProtocolVersionHeaderConstraint actualConstraint = new ProtocolVersionHeaderConstraint(sessionManager);
        TransportHeaderConstraintException actual = assertThrows(TransportHeaderConstraintException.class,
                () -> actualConstraint.validate(Map.of("Mcp-Session-Id", List.of("session-id"), "MCP-Protocol-Version", List.of("2025-03-26"))));
        assertThat(actual.getStatusCode(), is(400));
        assertThat(actual.getMessage(), is("Protocol version mismatch."));
    }
    
    @Test
    void assertValidateWithDefaultProtocolVersion() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        when(sessionManager.hasSession("session-id")).thenReturn(true);
        when(sessionManager.findProtocolVersion("session-id")).thenReturn(Optional.empty());
        ProtocolVersionHeaderConstraint actualConstraint = new ProtocolVersionHeaderConstraint(sessionManager);
        assertDoesNotThrow(() -> actualConstraint.validate(Map.of("Mcp-Session-Id", List.of("session-id"), "MCP-Protocol-Version", List.of(MCPTransportConstants.PROTOCOL_VERSION))));
    }
    
    @Test
    void assertValidate() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        when(sessionManager.hasSession("session-id")).thenReturn(true);
        when(sessionManager.findProtocolVersion("session-id")).thenReturn(Optional.of(MCPTransportConstants.PROTOCOL_VERSION));
        ProtocolVersionHeaderConstraint actualConstraint = new ProtocolVersionHeaderConstraint(sessionManager);
        assertDoesNotThrow(() -> actualConstraint.validate(Map.of("Mcp-Session-Id", List.of("session-id"), "MCP-Protocol-Version", List.of(MCPTransportConstants.PROTOCOL_VERSION))));
    }
}
