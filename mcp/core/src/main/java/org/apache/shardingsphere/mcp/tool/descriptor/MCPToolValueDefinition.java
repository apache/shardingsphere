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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * MCP tool value definition.
 */
@RequiredArgsConstructor
@Getter
public final class MCPToolValueDefinition {
    
    private final Type type;
    
    private final String description;
    
    private final MCPToolValueDefinition itemDefinition;
    
    /**
     * To schema fragment.
     *
     * @return schema fragment
     * @throws IllegalStateException unsupported tool value type
     */
    public Map<String, Object> toSchemaFragment() {
        switch (type) {
            case STRING:
                return toScalarSchemaFragment("string");
            case INTEGER:
                return toScalarSchemaFragment("integer");
            case ARRAY:
                return toArraySchemaFragment();
            default:
                throw new IllegalStateException(String.format("Unsupported MCP tool value type `%s`.", type));
        }
    }
    
    private Map<String, Object> toScalarSchemaFragment(final String type) {
        return Map.of("type", type, "description", description);
    }
    
    private Map<String, Object> toArraySchemaFragment() {
        return Map.of("type", "array", "description", description, "items", itemDefinition.toSchemaFragment());
    }
    
    /**
     * Tool value type.
     */
    public enum Type {
        
        STRING,
        
        INTEGER,
        
        ARRAY
    }
}
