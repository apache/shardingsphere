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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;

/**
 * Protocol version header constraint.
 */
public final class ProtocolVersionHeaderConstraint {
    
    /**
     * Validate the MCP protocol version header value.
     *
     * @param value protocol version header value
     * @throws ServerTransportSecurityException when the protocol version is missing or unsupported
     */
    public void validate(final String value) throws ServerTransportSecurityException {
        ShardingSpherePreconditions.checkNotEmpty(value, () -> new ServerTransportSecurityException(400, "MCP-Protocol-Version header is required."));
        ShardingSpherePreconditions.checkState(MCPTransportConstants.SUPPORTED_PROTOCOL_VERSIONS.contains(value), () -> createUnsupportedProtocolVersionException(value));
    }
    
    private ServerTransportSecurityException createUnsupportedProtocolVersionException(final String protocolVersion) {
        return new ServerTransportSecurityException(400, String.format("Unsupported MCP protocol version `%s`. Supported versions are %s.", protocolVersion,
                MCPTransportConstants.SUPPORTED_PROTOCOL_VERSIONS));
    }
}
