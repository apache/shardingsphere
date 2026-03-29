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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

final class MCPToolInputSchemaFactory {
    
    McpSchema.JsonSchema createInputSchema(final String toolName) {
        switch (toolName) {
            case "list_schemas":
                return createObjectSchema(List.of(Map.entry("database", createStringSchema("Logical database name."))), List.of("database"));
            case "list_tables":
            case "list_views":
                return createPagedMetadataSchema("Logical database name.", "Schema name.");
            case "list_columns":
                return createObjectSchema(List.of(
                        Map.entry("database", createStringSchema("Logical database name.")),
                        Map.entry("schema", createStringSchema("Schema name.")),
                        Map.entry("object_type", createStringSchema("Parent object type: table or view.")),
                        Map.entry("object_name", createStringSchema("Parent object name.")),
                        Map.entry("search", createStringSchema("Optional fuzzy filter.")),
                        Map.entry("page_size", createIntegerSchema("Requested page size.")),
                        Map.entry("page_token", createStringSchema("Opaque pagination token."))),
                        List.of("database", "schema", "object_type", "object_name"));
            case "list_indexes":
                return createObjectSchema(List.of(
                        Map.entry("database", createStringSchema("Logical database name.")),
                        Map.entry("schema", createStringSchema("Schema name.")),
                        Map.entry("table", createStringSchema("Table name.")),
                        Map.entry("search", createStringSchema("Optional fuzzy filter.")),
                        Map.entry("page_size", createIntegerSchema("Requested page size.")),
                        Map.entry("page_token", createStringSchema("Opaque pagination token."))),
                        List.of("database", "schema", "table"));
            case "search_metadata":
                return createObjectSchema(List.of(
                        Map.entry("database", createStringSchema("Optional logical database name.")),
                        Map.entry("schema", createStringSchema("Optional schema name.")),
                        Map.entry("query", createStringSchema("Search query.")),
                        Map.entry("object_types", createArraySchema("Optional object-type filter.")),
                        Map.entry("page_size", createIntegerSchema("Requested page size.")),
                        Map.entry("page_token", createStringSchema("Opaque pagination token."))),
                        List.of("query"));
            case "describe_table":
                return createObjectSchema(List.of(
                        Map.entry("database", createStringSchema("Logical database name.")),
                        Map.entry("schema", createStringSchema("Schema name.")),
                        Map.entry("table", createStringSchema("Table name."))),
                        List.of("database", "schema", "table"));
            case "describe_view":
                return createObjectSchema(List.of(
                        Map.entry("database", createStringSchema("Logical database name.")),
                        Map.entry("schema", createStringSchema("Schema name.")),
                        Map.entry("view", createStringSchema("View name."))),
                        List.of("database", "schema", "view"));
            case "execute_query":
                return createObjectSchema(List.of(
                        Map.entry("database", createStringSchema("Logical database name.")),
                        Map.entry("schema", createStringSchema("Optional schema name.")),
                        Map.entry("sql", createStringSchema("Single SQL statement.")),
                        Map.entry("max_rows", createIntegerSchema("Optional maximum row count.")),
                        Map.entry("timeout_ms", createIntegerSchema("Optional timeout in milliseconds."))),
                        List.of("database", "sql"));
            default:
                return createObjectSchema(Collections.emptyList(), Collections.emptyList());
        }
    }
    
    private McpSchema.JsonSchema createPagedMetadataSchema(final String databaseDescription, final String schemaDescription) {
        return createObjectSchema(List.of(
                Map.entry("database", createStringSchema(databaseDescription)),
                Map.entry("schema", createStringSchema(schemaDescription)),
                Map.entry("search", createStringSchema("Optional fuzzy filter.")),
                Map.entry("page_size", createIntegerSchema("Requested page size.")),
                Map.entry("page_token", createStringSchema("Opaque pagination token."))),
                List.of("database", "schema"));
    }
    
    private McpSchema.JsonSchema createObjectSchema(final List<Entry<String, Object>> properties, final List<String> required) {
        Map<String, Object> schemaProperties = new LinkedHashMap<>(properties.size(), 1F);
        for (Entry<String, Object> entry : properties) {
            schemaProperties.put(entry.getKey(), entry.getValue());
        }
        return new McpSchema.JsonSchema("object", schemaProperties, required, Boolean.TRUE, Collections.emptyMap(), Collections.emptyMap());
    }
    
    private Map<String, Object> createStringSchema(final String description) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("type", "string");
        result.put("description", description);
        return result;
    }
    
    private Map<String, Object> createIntegerSchema(final String description) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("type", "integer");
        result.put("description", description);
        return result;
    }
    
    private Map<String, Object> createArraySchema(final String description) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("type", "array");
        result.put("description", description);
        result.put("items", createStringSchema("Array element value."));
        return result;
    }
}
