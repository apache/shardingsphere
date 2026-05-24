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

import io.modelcontextprotocol.server.transport.ServerTransportSecurityValidator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.bootstrap.transport.HttpTransportHostUtils;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.constraint.OriginHeaderConstraint;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.constraint.ProtocolVersionHeaderConstraint;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.constraint.TransportHeaderConstraint;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;

import java.util.LinkedList;
import java.util.List;

/**
 * Server transport security validator factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerTransportSecurityValidatorFactory {
    
    /**
     * Create the MCP-facing transport security validator.
     *
     * @param sessionManager session manager
     * @param bindHost bind host
     * @return transport security validator
     */
    public static ServerTransportSecurityValidator create(final MCPSessionManager sessionManager, final String bindHost) {
        return new ShardingSphereServerTransportSecurityValidator(sessionManager, createConstraints(bindHost));
    }
    
    private static List<TransportHeaderConstraint> createConstraints(final String bindHost) {
        List<TransportHeaderConstraint> result = new LinkedList<>();
        result.add(new OriginHeaderConstraint(HttpTransportHostUtils.isLoopbackHost(bindHost)));
        result.add(new ProtocolVersionHeaderConstraint());
        return result;
    }
}
