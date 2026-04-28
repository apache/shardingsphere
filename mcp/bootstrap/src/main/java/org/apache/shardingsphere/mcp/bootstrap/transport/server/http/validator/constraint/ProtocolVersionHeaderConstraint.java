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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.TransportHeaderConstraintException;

/**
 * Protocol version header constraint.
 */
public final class ProtocolVersionHeaderConstraint implements SessionRequiredTransportHeaderConstraint {
    
    @Override
    public String getConstraintKey() {
        return HttpHeaders.PROTOCOL_VERSION;
    }
    
    @Override
    public void validate(final String value) throws TransportHeaderConstraintException {
        ShardingSpherePreconditions.checkNotEmpty(value, () -> new TransportHeaderConstraintException(400, "MCP-Protocol-Version header is required."));
        ShardingSpherePreconditions.checkState(MCPTransportConstants.PROTOCOL_VERSION.equals(value), () -> new TransportHeaderConstraintException(400, "Protocol version mismatch."));
    }
}
