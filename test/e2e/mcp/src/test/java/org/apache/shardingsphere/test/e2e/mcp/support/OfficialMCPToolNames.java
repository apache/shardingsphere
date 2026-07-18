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

package org.apache.shardingsphere.test.e2e.mcp.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.api.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolDefinitionRegistry;

import java.util.List;

/**
 * Official MCP tool names packaged by default.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OfficialMCPToolNames {
    
    private static final List<String> ALL = ToolDefinitionRegistry.getSupportedToolDescriptors().stream().map(MCPToolDescriptor::getName).toList();
    
    /**
     * Get official MCP tool names.
     *
     * @return official MCP tool names
     */
    public static List<String> getAll() {
        return ALL;
    }
}
