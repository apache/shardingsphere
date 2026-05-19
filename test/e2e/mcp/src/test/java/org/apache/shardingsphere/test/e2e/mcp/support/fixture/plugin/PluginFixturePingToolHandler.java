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

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.core.context.MCPServiceHandlerContext;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;

import java.util.Map;

/**
 * Test-only tool handler used to prove packaged distribution plugin discovery.
 */
public final class PluginFixturePingToolHandler implements MCPToolHandler<MCPServiceHandlerContext> {
    
    private static final String TOOL_NAME = "fixture_ping";
    
    @Override
    public Class<MCPServiceHandlerContext> getContextType() {
        return MCPServiceHandlerContext.class;
    }
    
    @Override
    public String getToolName() {
        return TOOL_NAME;
    }
    
    @Override
    public MCPResponse handle(final MCPServiceHandlerContext handlerContext, final MCPToolCall toolCall) {
        return new MCPMapResponse(Map.of("status", "ready", "echo", String.valueOf(toolCall.getArguments().get("message"))));
    }
}
