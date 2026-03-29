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

package org.apache.shardingsphere.mcp.bootstrap.transport.tool;

import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.mcp.tool.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.tool.MCPToolInputDefinition;
import org.apache.shardingsphere.mcp.tool.MCPToolValueDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class MCPToolInputSchemaFactory {
    
    McpSchema.JsonSchema createInputSchema(final MCPToolInputDefinition inputDefinition) {
        Map<String, Object> properties = new LinkedHashMap<>(inputDefinition.getFields().size(), 1F);
        List<String> required = new ArrayList<>(inputDefinition.getFields().size());
        for (MCPToolFieldDefinition each : inputDefinition.getFields()) {
            properties.put(each.getName(), createValueSchema(each.getValueDefinition()));
            if (each.isRequired()) {
                required.add(each.getName());
            }
        }
        return new McpSchema.JsonSchema("object", properties, List.copyOf(required), inputDefinition.isAdditionalPropertiesAllowed(),
                Collections.emptyMap(), Collections.emptyMap());
    }
    
    private Map<String, Object> createValueSchema(final MCPToolValueDefinition valueDefinition) {
        switch (valueDefinition.getType()) {
            case STRING:
                return createScalarSchema("string", valueDefinition);
            case INTEGER:
                return createScalarSchema("integer", valueDefinition);
            case ARRAY:
                return createArraySchema(valueDefinition);
            default:
                throw new IllegalStateException(String.format("Unsupported MCP tool value type `%s`.", valueDefinition.getType()));
        }
    }
    
    private Map<String, Object> createScalarSchema(final String type, final MCPToolValueDefinition valueDefinition) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("type", type);
        result.put("description", valueDefinition.getDescription());
        return result;
    }
    
    private Map<String, Object> createArraySchema(final MCPToolValueDefinition valueDefinition) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("type", "array");
        result.put("description", valueDefinition.getDescription());
        result.put("items", createValueSchema(valueDefinition.getItemDefinition()));
        return result;
    }
}
