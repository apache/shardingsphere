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

package org.apache.shardingsphere.mcp.tool.handler.capability;

import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.capability.service.MCPServiceCapabilityProvider;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.protocol.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.MCPToolDispatchKind;
import org.apache.shardingsphere.mcp.tool.MCPToolInputDefinition;
import org.apache.shardingsphere.mcp.tool.handler.MCPToolHandlerSupport;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.tool.response.MCPDatabaseCapabilityResponse;
import org.apache.shardingsphere.mcp.tool.response.MCPServiceCapabilityResponse;

import java.util.Map;
import java.util.Optional;

/**
 * Handler for get-capabilities tool.
 */
public final class GetCapabilitiesToolHandler implements ToolHandler {
    
    private static final MCPToolDescriptor TOOL_DESCRIPTOR = MCPToolHandlerSupport.createDescriptor("get_capabilities", MCPToolDispatchKind.CAPABILITY,
            MCPToolInputDefinition.create(MCPToolHandlerSupport.optionalStringField("database", "Optional logical database name.")));
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return TOOL_DESCRIPTOR;
    }
    
    @Override
    public MCPResponse handle(final String sessionId, final MCPRuntimeContext runtimeContext, final Map<String, Object> arguments) {
        String database = MCPToolHandlerSupport.getCapabilityDatabase(arguments);
        if (database.isEmpty()) {
            return new MCPServiceCapabilityResponse(MCPServiceCapabilityProvider.provide());
        }
        Optional<MCPDatabaseCapability> capability = runtimeContext.getDatabaseCapabilityProvider().provide(database);
        return capability.map(MCPDatabaseCapabilityResponse::new).orElseThrow(DatabaseCapabilityNotFoundException::new);
    }
}
