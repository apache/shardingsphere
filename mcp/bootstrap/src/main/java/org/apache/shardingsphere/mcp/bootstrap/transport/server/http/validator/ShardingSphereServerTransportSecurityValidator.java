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
import io.modelcontextprotocol.spec.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.constraint.SessionRequiredTransportHeaderConstraint;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.constraint.TransportHeaderConstraint;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ShardingSphere server transport security validator.
 */
@RequiredArgsConstructor
public final class ShardingSphereServerTransportSecurityValidator implements ServerTransportSecurityValidator {
    
    private final MCPSessionManager sessionManager;
    
    private final List<TransportHeaderConstraint> constraints;
    
    @Override
    public void validateHeaders(final Map<String, List<String>> headers) throws ServerTransportSecurityException {
        for (TransportHeaderConstraint each : constraints) {
            if (each instanceof SessionRequiredTransportHeaderConstraint) {
                String sessionId = getFirstHeaderValue(headers, HttpHeaders.MCP_SESSION_ID);
                if (sessionId.isEmpty() || !sessionManager.hasSession(sessionId)) {
                    continue;
                }
            }
            each.validate(getFirstHeaderValue(headers, each.getConstraintKey()));
        }
    }
    
    private String getFirstHeaderValue(final Map<String, List<String>> headers, final String headerName) {
        return headers.entrySet().stream()
                .filter(entry -> headerName.equalsIgnoreCase(entry.getKey()) && !entry.getValue().isEmpty()).findFirst().map(entry -> Objects.toString(entry.getValue().get(0), "").trim()).orElse("");
    }
}
