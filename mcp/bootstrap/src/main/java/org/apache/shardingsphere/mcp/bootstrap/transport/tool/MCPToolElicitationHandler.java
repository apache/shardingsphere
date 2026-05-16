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

package org.apache.shardingsphere.mcp.bootstrap.transport.tool;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.tool.MCPToolController;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;

import java.util.Map;

/**
 * MCP tool elicitation handler.
 */
@RequiredArgsConstructor
final class MCPToolElicitationHandler {
    
    private final MCPToolController toolController;
    
    private final MCPToolClarificationPolicy clarificationPolicy = new MCPToolClarificationPolicy();
    
    boolean shouldElicit(final McpSyncServerExchange exchange, final MCPToolDescriptor toolDescriptor, final Map<String, Object> payload) {
        return clarificationPolicy.isPlanningTool(toolDescriptor) && supportsFormElicitation(exchange) && clarificationPolicy.hasFormSafeClarificationQuestions(payload);
    }
    
    private boolean supportsFormElicitation(final McpSyncServerExchange exchange) {
        McpSchema.ClientCapabilities clientCapabilities = exchange.getClientCapabilities();
        if (null == clientCapabilities || null == clientCapabilities.elicitation()) {
            return false;
        }
        McpSchema.ClientCapabilities.Elicitation elicitation = clientCapabilities.elicitation();
        return null != elicitation.form() || null == elicitation.url();
    }
    
    MCPResponse handle(final McpSyncServerExchange exchange, final MCPToolDescriptor toolDescriptor, final Map<String, Object> arguments,
                       final MCPResponse fallbackResponse, final Map<String, Object> payload) {
        McpSchema.ElicitResult elicitedResult = exchange.createElicitation(createElicitRequest(toolDescriptor.getName(), payload));
        return McpSchema.ElicitResult.Action.ACCEPT == elicitedResult.action() && null != elicitedResult.content()
                ? toolController.handle(exchange.sessionId(), toolDescriptor.getName(), clarificationPolicy.mergeArguments(arguments, payload, elicitedResult.content(), toolDescriptor))
                : fallbackResponse;
    }
    
    private McpSchema.ElicitRequest createElicitRequest(final String toolName, final Map<String, Object> payload) {
        return McpSchema.ElicitRequest.builder()
                .message(String.format("Provide missing ShardingSphere workflow inputs for `%s`.", toolName))
                .requestedSchema(clarificationPolicy.createRequestedSchema(payload))
                .meta(createElicitMeta(toolName, payload))
                .build();
    }
    
    private Map<String, Object> createElicitMeta(final String toolName, final Map<String, Object> payload) {
        return Map.of(MCPShardingSphereMetadataKeys.TOOL, toolName, MCPShardingSphereMetadataKeys.PLAN_ID, clarificationPolicy.getPlanId(payload));
    }
}
