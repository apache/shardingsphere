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

package org.apache.shardingsphere.mcp.api.tool;

import java.util.Map;

/**
 * MCP tool call.
 */
public final class MCPToolCall {
    
    private final String sessionId;
    
    private final Map<String, Object> arguments;
    
    /**
     * Create MCP tool call.
     *
     * @param sessionId session identifier
     * @param arguments normalized tool arguments
     */
    public MCPToolCall(final String sessionId, final Map<String, Object> arguments) {
        this.sessionId = sessionId;
        this.arguments = arguments;
    }
    
    /**
     * Get session identifier.
     *
     * @return session identifier
     */
    public String sessionId() {
        return sessionId;
    }
    
    /**
     * Get normalized tool arguments.
     *
     * @return normalized tool arguments
     */
    public Map<String, Object> arguments() {
        return arguments;
    }
}
