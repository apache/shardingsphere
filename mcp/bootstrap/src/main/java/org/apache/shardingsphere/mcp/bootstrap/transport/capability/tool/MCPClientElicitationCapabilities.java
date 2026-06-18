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

package org.apache.shardingsphere.mcp.bootstrap.transport.capability.tool;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * MCP client elicitation capabilities.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class MCPClientElicitationCapabilities {
    
    private final boolean formModeSupported;
    
    private final boolean urlModeSupported;
    
    /**
     * Create client elicitation capabilities from server exchange.
     *
     * @param exchange MCP sync server exchange
     * @return client elicitation capabilities
     */
    public static MCPClientElicitationCapabilities from(final McpSyncServerExchange exchange) {
        McpSchema.ClientCapabilities clientCapabilities = exchange.getClientCapabilities();
        if (null == clientCapabilities || null == clientCapabilities.elicitation()) {
            return new MCPClientElicitationCapabilities(false, false);
        }
        McpSchema.ClientCapabilities.Elicitation elicitation = clientCapabilities.elicitation();
        return new MCPClientElicitationCapabilities(null != elicitation.form() || null == elicitation.url(), null != elicitation.url());
    }
}
