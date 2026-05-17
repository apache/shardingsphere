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

package org.apache.shardingsphere.mcp.core.protocol.response;

import lombok.Getter;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Response for resource errors.
 */
public final class MCPErrorResponse implements MCPResponse {
    
    @Getter
    private final String errorCode;
    
    private final String message;
    
    private final Map<String, Object> recovery;
    
    private final String requestId;
    
    public MCPErrorResponse(final String errorCode, final String message) {
        this(errorCode, message, Map.of());
    }
    
    public MCPErrorResponse(final String errorCode, final String message, final Map<String, Object> recovery) {
        this.errorCode = errorCode;
        this.message = message;
        this.recovery = recovery;
        requestId = UUID.randomUUID().toString();
    }
    
    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("response_mode", MCPResponseMode.RECOVERY);
        result.put("request_id", requestId);
        result.put("error_code", errorCode);
        result.put("message", message);
        if (!recovery.isEmpty()) {
            result.put("recovery", createRecoveryPayload());
        }
        return result;
    }
    
    private Map<String, Object> createRecoveryPayload() {
        Map<String, Object> result = new LinkedHashMap<>(recovery.size() + 1, 1F);
        result.putAll(recovery);
        result.putIfAbsent("request_id", requestId);
        return result;
    }
}
