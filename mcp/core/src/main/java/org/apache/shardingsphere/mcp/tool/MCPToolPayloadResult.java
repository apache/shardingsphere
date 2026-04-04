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

package org.apache.shardingsphere.mcp.tool;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryErrorDetail;

import java.util.Map;

/**
 * Transport-neutral payload result for one MCP tool call.
 */
@RequiredArgsConstructor
@Getter
public final class MCPToolPayloadResult {
    
    private final boolean successful;
    
    private final String errorCode;
    
    private final String message;
    
    private final Map<String, Object> payload;
    
    /**
     * Create a successful result.
     *
     * @param payload payload
     * @return result
     */
    public static MCPToolPayloadResult success(final Map<String, Object> payload) {
        return new MCPToolPayloadResult(true, "", "", payload);
    }
    
    /**
     * Create an error result.
     *
     * @param errorCode error code
     * @param payload error payload
     * @return result
     */
    public static MCPToolPayloadResult error(final ExecuteQueryErrorDetail errorCode, final Map<String, Object> payload) {
        return new MCPToolPayloadResult(false, errorCode.getErrorCode().name(), errorCode.getMessage(), payload);
    }
}
