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

package org.apache.shardingsphere.mcp.tool;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * MCP tool descriptor.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class MCPToolDescriptor {
    
    private final String name;
    
    private final String title;
    
    private final String description;
    
    private final MCPToolDispatchKind dispatchKind;
    
    private final MCPToolInputDefinition inputDefinition;
    
    /**
     * Create MCP tool descriptor.
     *
     * @param name tool name
     * @param title tool title
     * @param description tool description
     * @param dispatchKind tool dispatch kind
     * @param inputDefinition tool input definition
     * @return MCP tool descriptor
     */
    public static MCPToolDescriptor create(final String name, final String title, final String description,
                                           final MCPToolDispatchKind dispatchKind, final MCPToolInputDefinition inputDefinition) {
        return new MCPToolDescriptor(name, title, description, dispatchKind, inputDefinition);
    }
    
    /**
     * Determine whether the tool dispatches through metadata discovery.
     *
     * @return {@code true} when the tool is a metadata tool
     */
    public boolean isMetadataTool() {
        return MCPToolDispatchKind.METADATA == dispatchKind;
    }
}
