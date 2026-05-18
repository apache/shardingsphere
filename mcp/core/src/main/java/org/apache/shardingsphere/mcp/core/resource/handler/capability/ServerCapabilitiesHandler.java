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

package org.apache.shardingsphere.mcp.core.resource.handler.capability;

import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.core.context.MCPServiceHandlerContext;
import org.apache.shardingsphere.mcp.core.resource.handler.ResourceHandlerRegistry;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolHandlerRegistry;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;

import java.util.Set;

/**
 * Handler for server capabilities resource URI.
 */
public final class ServerCapabilitiesHandler implements MCPResourceHandler<MCPServiceHandlerContext> {
    
    private static final String URI_PATTERN = "shardingsphere://capabilities";
    
    @Override
    public Class<MCPServiceHandlerContext> getContextType() {
        return MCPServiceHandlerContext.class;
    }
    
    @Override
    public String getResourceUriTemplate() {
        return URI_PATTERN;
    }
    
    @Override
    public MCPResponse handle(final MCPServiceHandlerContext handlerContext, final MCPUriVariables uriVariables) {
        return new MCPMapResponse(MCPDescriptorCatalogIndex.createCapabilityPayload(
                ResourceHandlerRegistry.getSupportedResources(), ToolHandlerRegistry.getSupportedTools(), Set.of(SupportedMCPStatement.values())));
    }
}
