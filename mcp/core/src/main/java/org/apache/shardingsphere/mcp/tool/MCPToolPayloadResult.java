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

/**
 * Transport-neutral payload result for one MCP tool call.
 */
@Getter
public final class MCPToolPayloadResult {
    
    private final boolean successful;
    
    private final String errorCode;
    
    private final String message;
    
    private final Object payload;
    
    private MCPToolPayloadResult(final boolean successful, final String errorCode, final String message, final Object payload) {
        this.successful = successful;
        this.errorCode = errorCode;
        this.message = message;
        this.payload = payload;
    }
    
    /**
     * Create a successful result.
     *
     * @param payload payload
     * @return result
     */
    public static MCPToolPayloadResult success(final Object payload) {
        return new MCPToolPayloadResult(true, "", "", payload);
    }
    
    /**
     * Create an error result.
     *
     * @param errorCode error code
     * @param message error message
     * @param payload error payload
     * @return result
     */
    public static MCPToolPayloadResult error(final String errorCode, final String message, final Object payload) {
        return new MCPToolPayloadResult(false, errorCode, message, payload);
    }
}
