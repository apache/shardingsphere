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

package org.apache.shardingsphere.mcp.core.protocol.error;

import lombok.Getter;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Structured application error payload for MCP protocol adapters.
 */
public final class MCPErrorPayload {
    
    @Getter
    private final String message;
    
    private final Map<String, Object> recovery;
    
    private final String errorId;
    
    public MCPErrorPayload(final String message) {
        this(message, Map.of());
    }
    
    public MCPErrorPayload(final String message, final Map<String, Object> recovery) {
        this.message = message;
        this.recovery = recovery;
        errorId = UUID.randomUUID().toString();
    }
    
    /**
     * Convert to a protocol-neutral error payload.
     *
     * @return error payload
     */
    public Map<String, Object> toPayload() {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("response_mode", MCPResponseMode.RECOVERY);
        result.put(MCPPayloadFieldNames.SUMMARY, message.isEmpty() ? "Recovery guidance is available." : message);
        result.put("error_id", errorId);
        if (!recovery.isEmpty()) {
            Map<String, Object> recoveryPayload = createRecoveryPayload();
            if (!recoveryPayload.isEmpty()) {
                result.put(MCPPayloadFieldNames.RECOVERY, recoveryPayload);
            }
            if (recovery.containsKey(MCPPayloadFieldNames.NEXT_ACTIONS)) {
                result.put(MCPPayloadFieldNames.NEXT_ACTIONS, recovery.get(MCPPayloadFieldNames.NEXT_ACTIONS));
            }
        }
        return result;
    }
    
    private Map<String, Object> createRecoveryPayload() {
        Map<String, Object> result = new LinkedHashMap<>(recovery);
        result.remove("response_mode");
        result.remove(MCPPayloadFieldNames.NEXT_ACTIONS);
        return result;
    }
}
