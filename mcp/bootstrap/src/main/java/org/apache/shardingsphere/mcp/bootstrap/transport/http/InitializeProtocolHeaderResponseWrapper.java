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

package org.apache.shardingsphere.mcp.bootstrap.transport.http;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;

final class InitializeProtocolHeaderResponseWrapper extends HttpServletResponseWrapper {
    
    private static final String SESSION_HEADER = "MCP-Session-Id";
    
    private static final String PROTOCOL_HEADER = "MCP-Protocol-Version";
    
    private InitializeProtocolHeaderResponseWrapper(final HttpServletResponse response) {
        super(response);
    }
    
    static HttpServletResponse wrap(final HttpServletResponse response) {
        return new InitializeProtocolHeaderResponseWrapper(response);
    }
    
    @Override
    public void setHeader(final String name, final String value) {
        super.setHeader(name, value);
        addNegotiatedProtocolHeader(name, value);
    }
    
    @Override
    public void addHeader(final String name, final String value) {
        super.addHeader(name, value);
        addNegotiatedProtocolHeader(name, value);
    }
    
    private void addNegotiatedProtocolHeader(final String name, final String value) {
        if (SESSION_HEADER.equalsIgnoreCase(name) && null != value && !value.trim().isEmpty()) {
            super.setHeader(PROTOCOL_HEADER, MCPTransportConstants.PROTOCOL_VERSION);
        }
    }
}
