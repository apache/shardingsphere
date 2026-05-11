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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool descriptor.
 */
@Getter
public final class MCPToolDescriptor {
    
    private final String name;
    
    private final String title;
    
    private final String description;
    
    private final List<MCPToolFieldDefinition> fields;
    
    private final Map<String, Object> outputSchema;
    
    private final MCPToolAnnotations annotations;
    
    private final Map<String, Object> meta;
    
    public MCPToolDescriptor(final String name, final String title, final String description, final List<MCPToolFieldDefinition> fields) {
        this(name, title, description, fields, Collections.emptyMap(), MCPToolAnnotations.EMPTY, Collections.emptyMap());
    }
    
    public MCPToolDescriptor(final String name, final String title, final String description, final List<MCPToolFieldDefinition> fields,
                             final Map<String, Object> outputSchema, final MCPToolAnnotations annotations, final Map<String, Object> meta) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.fields = null == fields ? Collections.emptyList() : fields;
        this.outputSchema = null == outputSchema ? Collections.emptyMap() : outputSchema;
        this.annotations = null == annotations ? MCPToolAnnotations.EMPTY : annotations;
        this.meta = null == meta ? Collections.emptyMap() : meta;
    }
    
    /**
     * To input schema.
     *
     * @return input schema
     */
    public Map<String, Object> toInputSchema() {
        Map<String, Object> properties = new LinkedHashMap<>(fields.size(), 1F);
        List<String> required = new ArrayList<>(fields.size());
        for (MCPToolFieldDefinition each : fields) {
            properties.put(each.getName(), each.getValueDefinition().toSchemaFragment());
            if (each.isRequired()) {
                required.add(each.getName());
            }
        }
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("type", "object");
        result.put("properties", properties);
        result.put("required", required);
        result.put("additionalProperties", false);
        return result;
    }
}
