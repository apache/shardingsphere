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

package org.apache.shardingsphere.mcp.tool.descriptor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * MCP tool field definition.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class MCPToolFieldDefinition {
    
    private final String name;
    
    private final MCPToolValueDefinition valueDefinition;
    
    private final boolean required;
    
    /**
     * Create required tool input field definition.
     *
     * @param name field name
     * @param valueDefinition field value definition
     * @return required tool input field definition
     */
    public static MCPToolFieldDefinition required(final String name, final MCPToolValueDefinition valueDefinition) {
        return new MCPToolFieldDefinition(name, valueDefinition, true);
    }
    
    /**
     * Create optional tool input field definition.
     *
     * @param name field name
     * @param valueDefinition field value definition
     * @return optional tool input field definition
     */
    public static MCPToolFieldDefinition optional(final String name, final MCPToolValueDefinition valueDefinition) {
        return new MCPToolFieldDefinition(name, valueDefinition, false);
    }
}
