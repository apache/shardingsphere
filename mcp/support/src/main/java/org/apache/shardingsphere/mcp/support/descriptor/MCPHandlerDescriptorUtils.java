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

package org.apache.shardingsphere.mcp.support.descriptor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;

/**
 * MCP handler descriptor utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPHandlerDescriptorUtils {
    
    /**
     * Get required tool descriptor.
     *
     * @param handler tool handler
     * @return tool descriptor
     */
    public static MCPToolDescriptor getRequiredToolDescriptor(final MCPToolHandler<?> handler) {
        return MCPDescriptorCatalogIndex.getRequiredToolDescriptor(handler.getToolName());
    }
    
    /**
     * Get required resource descriptor.
     *
     * @param handler resource handler
     * @return resource descriptor
     */
    public static MCPResourceDescriptor getRequiredResourceDescriptor(final MCPResourceHandler<?> handler) {
        return MCPDescriptorCatalogIndex.getRequiredResourceDescriptor(handler.getResourceUriTemplate());
    }
}
