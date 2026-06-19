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

import io.modelcontextprotocol.server.transport.ServerTransportSecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.bootstrap.transport.HttpTransportHostUtils;
import org.apache.shardingsphere.mcp.bootstrap.transport.HttpTransportOriginUtils;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.MCPTransportSecurityException;

import java.net.URI;

/**
 * Origin header constraint.
 */
@RequiredArgsConstructor
@Slf4j
public final class OriginHeaderConstraint implements TransportHeaderConstraint {
    
    private static final String FORBIDDEN_MESSAGE = "Origin is not allowed by MCP HTTP transport policy.";
    
    private final boolean loopbackBinding;
    
    @Override
    public String getConstraintKey() {
        return "Origin";
    }
    
    @Override
    public void validate(final String value) throws ServerTransportSecurityException {
        if (value.isEmpty()) {
            return;
        }
        String actualOrigin = HttpTransportOriginUtils.normalizeOrigin(value);
        ShardingSpherePreconditions.checkNotEmpty(actualOrigin, () -> createForbiddenException("invalid_origin"));
        ShardingSpherePreconditions.checkState(loopbackBinding, () -> createForbiddenException("origin_header_on_non_loopback_binding"));
        ShardingSpherePreconditions.checkState(HttpTransportHostUtils.isLoopbackHost(URI.create(actualOrigin).getHost()),
                () -> createForbiddenException("non_loopback_origin_on_loopback_binding"));
    }
    
    private ServerTransportSecurityException createForbiddenException(final String reason) {
        log.warn("Rejected MCP HTTP request origin: reason={}, loopbackBinding={}.", reason, loopbackBinding);
        return new MCPTransportSecurityException(403, FORBIDDEN_MESSAGE, MCPTransportSecurityException.CATEGORY_ORIGIN_NOT_ALLOWED);
    }
}
