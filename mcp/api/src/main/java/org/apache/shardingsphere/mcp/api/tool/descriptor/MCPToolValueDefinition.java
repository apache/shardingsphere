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

package org.apache.shardingsphere.mcp.api.tool.descriptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
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
    
    private final Collection<String> enumValues;
    
    public MCPToolValueDefinition(final Type type, final String description, final MCPToolValueDefinition itemDefinition) {
        this(type, description, itemDefinition, Collections.emptyList());
    }
    
    /**
     * To schema fragment.
     *
     * @return schema fragment
     */
    public Map<String, Object> toSchemaFragment() {
        return switch (type) {
            case STRING -> toScalarSchemaFragment("string");
            case INTEGER -> toScalarSchemaFragment("integer");
            case ARRAY -> toArraySchemaFragment();
            case BOOLEAN -> toScalarSchemaFragment("boolean");
            case OBJECT -> toObjectSchemaFragment();
        };
    }
    
    private Map<String, Object> toScalarSchemaFragment(final String type) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("type", type);
        result.put("description", description);
        if (!enumValues.isEmpty()) {
            result.put("enum", enumValues);
        }
        return result;
    }
    
    private Map<String, Object> toArraySchemaFragment() {
        return Map.of("type", "array", "description", description, "items", itemDefinition.toSchemaFragment());
    }
    
    private Map<String, Object> toObjectSchemaFragment() {
        return Map.of("type", "object", "description", description, "additionalProperties", true);
    }
    
    /**
     * Tool value type.
     */
    public enum Type {
        
        STRING,
        
        INTEGER,
        
        ARRAY,
        
        BOOLEAN,
        
        OBJECT
    }
}
