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

/**
 * MCP tool input value definition.
 */
@Getter
public final class MCPToolValueDefinition {
    
    private final Type type;
    
    private final String description;
    
    private final MCPToolValueDefinition itemDefinition;
    
    private MCPToolValueDefinition(final Type type, final String description, final MCPToolValueDefinition itemDefinition) {
        this.type = type;
        this.description = description;
        this.itemDefinition = itemDefinition;
    }
    
    /**
     * Create string tool input value definition.
     *
     * @param description description
     * @return string tool input value definition
     */
    public static MCPToolValueDefinition string(final String description) {
        return new MCPToolValueDefinition(Type.STRING, description, null);
    }
    
    /**
     * Create integer tool input value definition.
     *
     * @param description description
     * @return integer tool input value definition
     */
    public static MCPToolValueDefinition integer(final String description) {
        return new MCPToolValueDefinition(Type.INTEGER, description, null);
    }
    
    /**
     * Create array tool input value definition.
     *
     * @param description description
     * @param itemDefinition item definition
     * @return array tool input value definition
     */
    public static MCPToolValueDefinition array(final String description, final MCPToolValueDefinition itemDefinition) {
        return new MCPToolValueDefinition(Type.ARRAY, description, itemDefinition);
    }
    
    /**
     * Create string-array tool input value definition.
     *
     * @param description description
     * @return string-array tool input value definition
     */
    public static MCPToolValueDefinition stringArray(final String description) {
        return array(description, string("Array element value."));
    }
    
    /**
     * Tool input value type.
     */
    public enum Type {
        
        STRING,
        
        INTEGER,
        
        ARRAY
    }
}
