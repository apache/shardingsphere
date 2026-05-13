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

package org.apache.shardingsphere.test.e2e.mcp.support.fixture.plugin;

import org.apache.shardingsphere.mcp.api.common.descriptor.MCPIcon;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.context.MCPServiceHandlerContext;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test-only tool handler used to prove packaged distribution plugin discovery.
 */
public final class PluginFixturePingToolHandler implements MCPToolHandler<MCPServiceHandlerContext> {
    
    private static final MCPToolDescriptor TOOL_DESCRIPTOR = new MCPToolDescriptor(List.of(new MCPIcon("https://example.invalid/fixture-ping.png", "image/png", List.of("64x64"), "light")),
            "fixture_ping", "Fixture Ping", "Return a fixture ping response for packaged plugin discovery.",
            Map.of("type", "object", "properties", Map.of("message", Map.of("type", "string", "description", "Fixture message.")), "required", List.of("message"), "additionalProperties", false),
            Collections.emptyMap(), MCPToolAnnotations.EMPTY, Collections.emptyMap());
    
    @Override
    public Class<MCPServiceHandlerContext> getContextType() {
        return MCPServiceHandlerContext.class;
    }
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return TOOL_DESCRIPTOR;
    }
    
    @Override
    public MCPResponse handle(final MCPServiceHandlerContext handlerContext, final MCPToolCall toolCall) {
        return new MCPMapResponse(Map.of("status", "ready", "echo", String.valueOf(toolCall.getArguments().get("message"))));
    }
}
