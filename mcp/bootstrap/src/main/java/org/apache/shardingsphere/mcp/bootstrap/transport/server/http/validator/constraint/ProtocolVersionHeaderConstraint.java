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

import io.modelcontextprotocol.spec.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.HttpTransportSecurityHeaderUtils;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.TransportHeaderConstraintException;

import java.util.List;
import java.util.Map;

/**
 * Protocol version header constraint.
 */
@RequiredArgsConstructor
public final class ProtocolVersionHeaderConstraint implements SessionRequiredTransportHeaderConstraint {
    
    private static final String PROTOCOL_HEADER_REQUIRED_MESSAGE = "MCP-Protocol-Version header is required.";
    
    private static final String PROTOCOL_VERSION_MISMATCH_MESSAGE = "Protocol version mismatch.";
    
    @Override
    public void validate(final Map<String, List<String>> headers) throws TransportHeaderConstraintException {
        String actualProtocolVersion = HttpTransportSecurityHeaderUtils.getFirstHeaderValue(headers, HttpHeaders.PROTOCOL_VERSION);
        if (actualProtocolVersion.isEmpty()) {
            throw new TransportHeaderConstraintException(400, PROTOCOL_HEADER_REQUIRED_MESSAGE);
        }
        if (!MCPTransportConstants.PROTOCOL_VERSION.equals(actualProtocolVersion)) {
            throw new TransportHeaderConstraintException(400, PROTOCOL_VERSION_MISMATCH_MESSAGE);
        }
    }
}
