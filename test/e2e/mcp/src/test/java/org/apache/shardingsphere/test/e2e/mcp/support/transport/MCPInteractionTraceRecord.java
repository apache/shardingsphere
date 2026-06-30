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

package org.apache.shardingsphere.test.e2e.mcp.support.transport;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * MCP interaction trace record.
 */
@RequiredArgsConstructor
@Getter
public final class MCPInteractionTraceRecord {
    
    public static final String MODEL_TOOL_CALL_ORIGIN = "model_tool_call";
    
    public static final String PROTOCOL_BRIDGE_ORIGIN = "protocol_bridge";
    
    public static final String HARNESS_TEXT_RECOVERY_ORIGIN = "harness_text_recovery";
    
    private final int sequence;
    
    private final String actionKind;
    
    private final String actionOrigin;
    
    private final String targetName;
    
    private final Map<String, Object> arguments;
    
    private final Map<String, Object> structuredContent;
    
    private final boolean valid;
    
    private final long latencyMillis;
    
    /**
     * Create resource read.
     *
     * @param sequence sequence
     * @param resourceUri resource URI
     * @param structuredContent structured content
     * @param latencyMillis latency millis
     * @return interaction trace record
     */
    public static MCPInteractionTraceRecord createResourceRead(final int sequence, final String resourceUri, final Map<String, Object> structuredContent, final long latencyMillis) {
        return new MCPInteractionTraceRecord(sequence, MCPInteractionActionNames.RESOURCE_READ_KIND, PROTOCOL_BRIDGE_ORIGIN, MCPInteractionActionNames.READ_RESOURCE,
                Map.of("uri", resourceUri), structuredContent, true, latencyMillis);
    }
    
    /**
     * Create completion.
     *
     * @param sequence sequence
     * @param arguments completion arguments
     * @param structuredContent structured content
     * @param latencyMillis latency millis
     * @return interaction trace record
     */
    public static MCPInteractionTraceRecord createCompletion(final int sequence, final Map<String, Object> arguments,
                                                             final Map<String, Object> structuredContent, final long latencyMillis) {
        return new MCPInteractionTraceRecord(sequence, MCPInteractionActionNames.COMPLETION_KIND, PROTOCOL_BRIDGE_ORIGIN, MCPInteractionActionNames.COMPLETE,
                arguments, structuredContent, true, latencyMillis);
    }
    
    /**
     * Create invalid action.
     *
     * @param sequence sequence
     * @param actionKind action kind
     * @param targetName target name
     * @param arguments arguments
     * @param failureType failure type
     * @return interaction trace record
     */
    public static MCPInteractionTraceRecord createInvalidAction(final int sequence, final String actionKind, final String targetName,
                                                                final Map<String, Object> arguments, final String failureType) {
        return new MCPInteractionTraceRecord(sequence, actionKind, MODEL_TOOL_CALL_ORIGIN, targetName, arguments, Map.of("error_code", failureType), false, 0L);
    }
}
