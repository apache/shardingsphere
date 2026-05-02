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

package org.apache.shardingsphere.mcp.resource.handler.capability;

import org.apache.shardingsphere.mcp.api.handler.MCPServiceHandlerContext;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.capability.service.MCPServiceCapability;
import org.apache.shardingsphere.mcp.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandlerRegistry;
import org.apache.shardingsphere.mcp.resource.response.MCPServiceCapabilityResponse;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandlerRegistry;

import java.util.Set;

/**
 * Handler for server capabilities resource URI.
 */
public final class ServerCapabilitiesHandler implements MCPResourceHandler<MCPServiceHandlerContext> {
    
    @Override
    public Class<MCPServiceHandlerContext> getContextType() {
        return MCPServiceHandlerContext.class;
    }
    
    @Override
    public String getUriPattern() {
        return "shardingsphere://capabilities";
    }
    
    @Override
    public MCPResponse handle(final MCPServiceHandlerContext handlerContext, final MCPUriVariables uriVariables) {
        return new MCPServiceCapabilityResponse(createServiceCapability());
    }
    
    private MCPServiceCapability createServiceCapability() {
        return new MCPServiceCapability(ResourceHandlerRegistry.getSupportedResources(), ToolHandlerRegistry.getSupportedTools(), Set.of(SupportedMCPStatement.values()));
    }
}
