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

package org.apache.shardingsphere.mcp.core.protocol.exception;

import lombok.Getter;
import org.apache.shardingsphere.mcp.api.protocol.exception.ShardingSphereMCPException;

/**
 * Exception thrown when one MCP session exceeds the tool call quota.
 */
@Getter
public final class MCPToolCallLimitExceededException extends ShardingSphereMCPException {
    
    private static final long serialVersionUID = -5246033994322478696L;
    
    private final String sessionId;
    
    private final String toolName;
    
    private final int maxToolCallsPerSession;
    
    public MCPToolCallLimitExceededException(final String sessionId, final String toolName, final int maxToolCallsPerSession) {
        super(String.format("MCP session exceeded the maximum tool call quota of %d.", maxToolCallsPerSession));
        this.sessionId = sessionId;
        this.toolName = toolName;
        this.maxToolCallsPerSession = maxToolCallsPerSession;
    }
}
