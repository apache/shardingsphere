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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.apache.shardingsphere.mcp.tool.ToolRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

enum MCPToolDefinition {
    
    LIST_DATABASES("list_databases") {
        
        @Override
        ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
            return createToolRequest("list_databases", arguments, "", "", "", "", Collections.emptySet(), 100, "");
        }
    },
    
    LIST_SCHEMAS("list_schemas") {
        
        @Override
        McpSchema.JsonSchema createInputSchema() {
            return createObjectSchema(List.of(property("database", createStringSchema("Logical database name."))), List.of("database"));
        }
        
        @Override
        ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
            return createToolRequest("list_schemas", arguments, Objects.toString(arguments.get("database"), "").trim(), "", "", "", Collections.emptySet(), 100, "");
        }
    },
    
    LIST_TABLES("list_tables") {
        
        @Override
        McpSchema.JsonSchema createInputSchema() {
            return createPagedMetadataSchema("Logical database name.", "Schema name.");
        }
        
        @Override
        ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
            return createPagedMetadataToolRequest("list_tables", arguments);
        }
    },
    
    LIST_VIEWS("list_views") {
        
        @Override
        McpSchema.JsonSchema createInputSchema() {
            return createPagedMetadataSchema("Logical database name.", "Schema name.");
        }
        
        @Override
        ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
            return createPagedMetadataToolRequest("list_views", arguments);
        }
    },
    
    LIST_COLUMNS("list_columns") {
        
        @Override
        McpSchema.JsonSchema createInputSchema() {
            return createObjectSchema(List.of(
                    property("database", createStringSchema("Logical database name.")),
                    property("schema", createStringSchema("Schema name.")),
                    property("object_type", createStringSchema("Parent object type: table or view.")),
                    property("object_name", createStringSchema("Parent object name.")),
                    property("search", createStringSchema("Optional fuzzy filter.")),
                    property("page_size", createIntegerSchema("Requested page size.")),
                    property("page_token", createStringSchema("Opaque pagination token."))),
                    List.of("database", "schema", "object_type", "object_name"));
        }
        
        @Override
        ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
            return createToolRequest("list_columns", arguments, Objects.toString(arguments.get("database"), "").trim(), Objects.toString(arguments.get("schema"), "").trim(), Objects.toString(arguments.get("object_name"), "").trim(), Objects.toString(arguments.get("object_type"), "").trim().toUpperCase(Locale.ENGLISH), Collections.emptySet(),
                    integerArgument(arguments, "page_size", 100), Objects.toString(arguments.get("page_token"), "").trim());
        }
    },
    
    LIST_INDEXES("list_indexes") {
        
        @Override
        McpSchema.JsonSchema createInputSchema() {
            return createObjectSchema(List.of(
                    property("database", createStringSchema("Logical database name.")),
                    property("schema", createStringSchema("Schema name.")),
                    property("table", createStringSchema("Table name.")),
                    property("search", createStringSchema("Optional fuzzy filter.")),
                    property("page_size", createIntegerSchema("Requested page size.")),
                    property("page_token", createStringSchema("Opaque pagination token."))),
                    List.of("database", "schema", "table"));
        }
        
        @Override
        ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
            return createToolRequest("list_indexes", arguments, Objects.toString(arguments.get("database"), "").trim(), Objects.toString(arguments.get("schema"), "").trim(), Objects.toString(arguments.get("table"), "").trim(), "TABLE", Collections.emptySet(), integerArgument(arguments, "page_size", 100), Objects.toString(arguments.get("page_token"), "").trim());
        }
    },
    
    SEARCH_METADATA("search_metadata") {
        
        @Override
        McpSchema.JsonSchema createInputSchema() {
            return createObjectSchema(List.of(
                    property("database", createStringSchema("Optional logical database name.")),
                    property("schema", createStringSchema("Optional schema name.")),
                    property("query", createStringSchema("Search query.")),
                    property("object_types", createArraySchema("Optional object-type filter.")),
                    property("page_size", createIntegerSchema("Requested page size.")),
                    property("page_token", createStringSchema("Opaque pagination token."))),
                    List.of("query"));
        }
        
        @Override
        ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
            return createToolRequest("search_metadata", arguments, Objects.toString(arguments.get("database"), "").trim(), Objects.toString(arguments.get("schema"), "").trim(), "",
                    "", objectTypes(arguments), integerArgument(arguments, "page_size", 100), Objects.toString(arguments.get("page_token"), "").trim());
        }
    },
    
    DESCRIBE_TABLE("describe_table") {
        
        @Override
        McpSchema.JsonSchema createInputSchema() {
            return createObjectSchema(List.of(
                    property("database", createStringSchema("Logical database name.")),
                    property("schema", createStringSchema("Schema name.")),
                    property("table", createStringSchema("Table name."))),
                    List.of("database", "schema", "table"));
        }
        
        @Override
        ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
            return createToolRequest("describe_table", arguments, Objects.toString(arguments.get("database"), "").trim(), Objects.toString(arguments.get("schema"), "").trim(), Objects.toString(arguments.get("table"), "").trim(), "", Collections.emptySet(), 100, "");
        }
    },
    
    DESCRIBE_VIEW("describe_view") {
        
        @Override
        McpSchema.JsonSchema createInputSchema() {
            return createObjectSchema(List.of(
                    property("database", createStringSchema("Logical database name.")),
                    property("schema", createStringSchema("Schema name.")),
                    property("view", createStringSchema("View name."))),
                    List.of("database", "schema", "view"));
        }
        
        @Override
        ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
            return createToolRequest("describe_view", arguments, Objects.toString(arguments.get("database"), "").trim(), Objects.toString(arguments.get("schema"), "").trim(), Objects.toString(arguments.get("view"), "").trim(), "", Collections.emptySet(), 100, "");
        }
    },
    
    GET_CAPABILITIES("get_capabilities"),
    
    EXECUTE_QUERY("execute_query") {
        
        @Override
        McpSchema.JsonSchema createInputSchema() {
            return createObjectSchema(List.of(
                    property("database", createStringSchema("Logical database name.")),
                    property("schema", createStringSchema("Optional schema name.")),
                    property("sql", createStringSchema("Single SQL statement.")),
                    property("max_rows", createIntegerSchema("Optional maximum row count.")),
                    property("timeout_ms", createIntegerSchema("Optional timeout in milliseconds."))),
                    List.of("database", "sql"));
        }
    };
    
    private final String name;
    
    MCPToolDefinition(final String name) {
        this.name = name;
    }
    
    static Optional<MCPToolDefinition> findByName(final String name) {
        for (MCPToolDefinition each : values()) {
            if (each.name.equals(name)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    String getName() {
        return name;
    }
    
    String getTitle() {
        String[] segments = name.split("_");
        List<String> words = new ArrayList<>(segments.length);
        for (String each : segments) {
            if (!each.isEmpty()) {
                words.add(Character.toUpperCase(each.charAt(0)) + each.substring(1));
            }
        }
        return String.join(" ", words);
    }
    
    McpSchema.JsonSchema createInputSchema() {
        return createObjectSchema(Collections.emptyList(), Collections.emptyList());
    }
    
    boolean isMetadataTool() {
        return GET_CAPABILITIES != this && EXECUTE_QUERY != this;
    }
    
    ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
        throw new UnsupportedOperationException("Not a metadata tool.");
    }
    
    private static McpSchema.JsonSchema createPagedMetadataSchema(final String databaseDescription, final String schemaDescription) {
        return createObjectSchema(List.of(
                property("database", createStringSchema(databaseDescription)),
                property("schema", createStringSchema(schemaDescription)),
                property("search", createStringSchema("Optional fuzzy filter.")),
                property("page_size", createIntegerSchema("Requested page size.")),
                property("page_token", createStringSchema("Opaque pagination token."))),
                List.of("database", "schema"));
    }
    
    private static ToolRequest createPagedMetadataToolRequest(final String toolName, final Map<String, Object> arguments) {
        return createToolRequest(toolName, arguments, Objects.toString(arguments.get("database"), "").trim(), Objects.toString(arguments.get("schema"), "").trim(),
                "", "", Collections.emptySet(), integerArgument(arguments, "page_size", 100), Objects.toString(arguments.get("page_token"), "").trim());
    }
    
    private static ToolRequest createToolRequest(final String toolName, final Map<String, Object> arguments, final String database,
                                                 final String schema, final String objectName, final String parentObjectType,
                                                 final Set<MetadataObjectType> objectTypes, final int pageSize, final String pageToken) {
        String query = Objects.toString(arguments.get("query"), "").trim();
        if (query.isEmpty()) {
            query = Objects.toString(arguments.get("search"), "").trim();
        }
        return new ToolRequest(toolName, database, schema, objectName, parentObjectType, query, objectTypes, pageSize, pageToken);
    }
    
    private static Set<MetadataObjectType> objectTypes(final Map<String, Object> arguments) {
        Object rawValue = arguments.get("object_types");
        if (!(rawValue instanceof Collection)) {
            return Collections.emptySet();
        }
        Set<MetadataObjectType> result = new LinkedHashSet<>();
        for (Object each : (Collection<?>) rawValue) {
            if (null == each) {
                continue;
            }
            try {
                result.add(MetadataObjectType.valueOf(each.toString().trim().toUpperCase(Locale.ENGLISH)));
            } catch (final IllegalArgumentException ignored) {
            }
        }
        return result;
    }
    
    private static int integerArgument(final Map<String, Object> arguments, final String name, final int defaultValue) {
        Object result = arguments.get(name);
        if (null == result) {
            return defaultValue;
        }
        if (result instanceof Number) {
            return ((Number) result).intValue();
        }
        String actualValue = result.toString().trim();
        if (actualValue.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(actualValue);
        } catch (final NumberFormatException ignored) {
            return defaultValue;
        }
    }
    
    private static McpSchema.JsonSchema createObjectSchema(final List<Entry<String, Object>> properties, final List<String> required) {
        Map<String, Object> schemaProperties = new LinkedHashMap<>(properties.size(), 1F);
        for (Entry<String, Object> entry : properties) {
            schemaProperties.put(entry.getKey(), entry.getValue());
        }
        return new McpSchema.JsonSchema("object", schemaProperties, required, Boolean.TRUE, Collections.emptyMap(), Collections.emptyMap());
    }
    
    private static Entry<String, Object> property(final String name, final Object value) {
        return Map.entry(name, value);
    }
    
    private static Map<String, Object> createStringSchema(final String description) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("type", "string");
        result.put("description", description);
        return result;
    }
    
    private static Map<String, Object> createIntegerSchema(final String description) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("type", "integer");
        result.put("description", description);
        return result;
    }
    
    private static Map<String, Object> createArraySchema(final String description) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("type", "array");
        result.put("description", description);
        result.put("items", createStringSchema("Array element value."));
        return result;
    }
}
