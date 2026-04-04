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

package org.apache.shardingsphere.mcp.capability.provider;

import org.apache.shardingsphere.mcp.capability.ServiceCapability;
import org.apache.shardingsphere.mcp.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandlerRegistry;
import org.apache.shardingsphere.mcp.tool.MCPToolCatalog;

import java.util.Set;

/**
 * MCP service capability provider.
 */
public final class MCPServiceCapabilityProvider {
    
    private static final Set<SupportedMCPStatement> SUPPORTED_STATEMENT_TYPES = Set.of(SupportedMCPStatement.values());
    
    private static final ServiceCapability SERVICE_CAPABILITY = new ServiceCapability(
            ResourceHandlerRegistry.getSupportedResources(), new MCPToolCatalog().getSupportedTools(), SUPPORTED_STATEMENT_TYPES);
    
    /**
     * Provide the service-level capability.
     *
     * @return service-level capability
     */
    public static ServiceCapability provide() {
        return SERVICE_CAPABILITY;
    }
}
