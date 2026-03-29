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

import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * MCP tool input root-object definition.
 */
@Getter
public final class MCPToolInputDefinition {
    
    private final List<MCPToolFieldDefinition> fields;
    
    private final boolean additionalPropertiesAllowed;
    
    private MCPToolInputDefinition(final List<MCPToolFieldDefinition> fields, final boolean additionalPropertiesAllowed) {
        this.fields = List.copyOf(fields);
        this.additionalPropertiesAllowed = additionalPropertiesAllowed;
    }
    
    /**
     * Create empty tool input definition.
     *
     * @return empty tool input definition
     */
    public static MCPToolInputDefinition empty() {
        return new MCPToolInputDefinition(Collections.emptyList(), true);
    }
    
    /**
     * Create tool input definition.
     *
     * @param fields ordered tool input fields
     * @return tool input definition
     */
    public static MCPToolInputDefinition create(final MCPToolFieldDefinition... fields) {
        return new MCPToolInputDefinition(List.of(fields), true);
    }
    
    /**
     * Create tool input definition.
     *
     * @param additionalPropertiesAllowed whether additional properties are allowed
     * @param fields ordered tool input fields
     * @return tool input definition
     */
    public static MCPToolInputDefinition create(final boolean additionalPropertiesAllowed, final List<MCPToolFieldDefinition> fields) {
        return new MCPToolInputDefinition(fields, additionalPropertiesAllowed);
    }
}
