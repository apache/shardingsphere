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

import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MCP tool catalog.
 */
public final class MCPToolCatalog {
    
    private static final List<ToolDefinition> SUPPORTED_TOOLS = List.of(
            ToolDefinition.LIST_DATABASES,
            ToolDefinition.LIST_SCHEMAS,
            ToolDefinition.LIST_TABLES,
            ToolDefinition.LIST_VIEWS,
            ToolDefinition.LIST_COLUMNS,
            ToolDefinition.LIST_INDEXES,
            ToolDefinition.SEARCH_METADATA,
            ToolDefinition.DESCRIBE_TABLE,
            ToolDefinition.DESCRIBE_VIEW,
            ToolDefinition.GET_CAPABILITIES,
            ToolDefinition.EXECUTE_QUERY);
    
    private static final List<MCPToolDescriptor> SUPPORTED_TOOL_DESCRIPTORS = SUPPORTED_TOOLS.stream().map(ToolDefinition::getDescriptor).collect(Collectors.toList());
    
    private static final List<String> SUPPORTED_TOOL_NAMES = SUPPORTED_TOOL_DESCRIPTORS.stream().map(MCPToolDescriptor::getName).collect(Collectors.toList());
    
    /**
     * Get supported tool names.
     *
     * @return supported tool names
     */
    public List<String> getSupportedTools() {
        return SUPPORTED_TOOL_NAMES;
    }
    
    /**
     * Get supported tool descriptors.
     *
     * @return supported tool descriptors
     */
    public List<MCPToolDescriptor> getToolDescriptors() {
        return SUPPORTED_TOOL_DESCRIPTORS;
    }
    
    /**
     * Find one supported tool descriptor.
     *
     * @param toolName tool name
     * @return matched tool descriptor
     */
    public Optional<MCPToolDescriptor> findToolDescriptor(final String toolName) {
        return findToolDefinition(toolName).map(ToolDefinition::getDescriptor);
    }
    
    /**
     * Determine whether the named tool exists.
     *
     * @param toolName tool name
     * @return {@code true} when supported
     */
    public boolean contains(final String toolName) {
        return findToolDescriptor(toolName).isPresent();
    }
    
    /**
     * Get tool title.
     *
     * @param toolName tool name
     * @return tool title
     */
    public String getTitle(final String toolName) {
        return getRequiredToolDescriptor(toolName).getTitle();
    }
    
    /**
     * Determine whether the named tool dispatches through metadata discovery.
     *
     * @param toolName tool name
     * @return {@code true} when the tool is a metadata tool
     */
    public boolean isMetadataTool(final String toolName) {
        return findToolDescriptor(toolName).map(MCPToolDescriptor::isMetadataTool).orElse(false);
    }
    
    /**
     * Create a metadata tool request for the named tool.
     *
     * @param toolName tool name
     * @param arguments raw tool arguments
     * @return normalized metadata tool request
     */
    public ToolRequest createMetadataToolRequest(final String toolName, final Map<String, Object> arguments) {
        return getRequiredToolDefinition(toolName).createMetadataToolRequest(arguments);
    }
    
    /**
     * Resolve the database argument used by capability tools.
     *
     * @param arguments raw tool arguments
     * @return normalized database argument
     */
    public String getCapabilityDatabase(final Map<String, Object> arguments) {
        return stringArgument(arguments, "database");
    }
    
    /**
     * Create an execute-query request.
     *
     * @param sessionId session identifier
     * @param arguments raw tool arguments
     * @return normalized execute-query request
     */
    public ExecutionRequest createExecutionRequest(final String sessionId, final Map<String, Object> arguments) {
        return new ExecutionRequest(sessionId, stringArgument(arguments, "database"), stringArgument(arguments, "schema"), stringArgument(arguments, "sql"),
                integerArgument(arguments, "max_rows", 0), integerArgument(arguments, "timeout_ms", 0));
    }
    
    private MCPToolDescriptor getRequiredToolDescriptor(final String toolName) {
        return findToolDescriptor(toolName).orElseThrow(() -> createUnknownToolException(toolName));
    }
    
    private ToolDefinition getRequiredToolDefinition(final String toolName) {
        return findToolDefinition(toolName).orElseThrow(() -> createUnknownToolException(toolName));
    }
    
    private Optional<ToolDefinition> findToolDefinition(final String toolName) {
        for (ToolDefinition each : SUPPORTED_TOOLS) {
            if (each.getName().equals(toolName)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private IllegalStateException createUnknownToolException(final String toolName) {
        return new IllegalStateException(String.format("Unknown MCP tool `%s`.", toolName));
    }
    
    private static ToolRequest createPagedMetadataToolRequest(final String toolName, final Map<String, Object> arguments) {
        return createToolRequest(toolName, arguments, stringArgument(arguments, "database"), stringArgument(arguments, "schema"),
                "", "", Collections.emptySet(), integerArgument(arguments, "page_size", 100), stringArgument(arguments, "page_token"));
    }
    
    private static ToolRequest createListDatabasesRequest(final Map<String, Object> arguments) {
        return createToolRequest("list_databases", arguments, "", "", "", "", Collections.emptySet(), 100, "");
    }
    
    private static ToolRequest createListSchemasRequest(final Map<String, Object> arguments) {
        return createToolRequest("list_schemas", arguments, stringArgument(arguments, "database"), "", "", "", Collections.emptySet(), 100, "");
    }
    
    private static ToolRequest createListColumnsRequest(final Map<String, Object> arguments) {
        return createToolRequest("list_columns", arguments, stringArgument(arguments, "database"), stringArgument(arguments, "schema"),
                stringArgument(arguments, "object_name"), stringArgument(arguments, "object_type").toUpperCase(Locale.ENGLISH),
                Collections.emptySet(), integerArgument(arguments, "page_size", 100), stringArgument(arguments, "page_token"));
    }
    
    private static ToolRequest createListIndexesRequest(final Map<String, Object> arguments) {
        return createToolRequest("list_indexes", arguments, stringArgument(arguments, "database"), stringArgument(arguments, "schema"),
                stringArgument(arguments, "table"), "TABLE", Collections.emptySet(), integerArgument(arguments, "page_size", 100), stringArgument(arguments, "page_token"));
    }
    
    private static ToolRequest createSearchMetadataRequest(final Map<String, Object> arguments) {
        return createToolRequest("search_metadata", arguments, stringArgument(arguments, "database"), stringArgument(arguments, "schema"),
                "", "", objectTypes(arguments), integerArgument(arguments, "page_size", 100), stringArgument(arguments, "page_token"));
    }
    
    private static ToolRequest createDescribeTableRequest(final Map<String, Object> arguments) {
        return createToolRequest("describe_table", arguments, stringArgument(arguments, "database"), stringArgument(arguments, "schema"),
                stringArgument(arguments, "table"), "", Collections.emptySet(), 100, "");
    }
    
    private static ToolRequest createDescribeViewRequest(final Map<String, Object> arguments) {
        return createToolRequest("describe_view", arguments, stringArgument(arguments, "database"), stringArgument(arguments, "schema"),
                stringArgument(arguments, "view"), "", Collections.emptySet(), 100, "");
    }
    
    private static ToolRequest createToolRequest(final String toolName, final Map<String, Object> arguments, final String databaseName,
                                                 final String schemaName, final String objectName, final String parentObjectType,
                                                 final Set<MetadataObjectType> objectTypes, final int pageSize, final String pageToken) {
        String query = stringArgument(arguments, "query");
        if (query.isEmpty()) {
            query = stringArgument(arguments, "search");
        }
        return new ToolRequest(toolName, databaseName, schemaName, objectName, parentObjectType, query, objectTypes, pageSize, pageToken);
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
    
    private static String stringArgument(final Map<String, Object> arguments, final String name) {
        return Objects.toString(arguments.get(name), "").trim();
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
    
    private enum ToolDefinition {
        
        LIST_DATABASES(createDescriptor("list_databases", MCPToolDispatchKind.METADATA, MCPToolInputDefinition.empty())) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createListDatabasesRequest(arguments);
            }
        },
        
        LIST_SCHEMAS(createDescriptor("list_schemas", MCPToolDispatchKind.METADATA, MCPToolInputDefinition.create(
                requiredStringField("database", "Logical database name.")))) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createListSchemasRequest(arguments);
            }
        },
        
        LIST_TABLES(createDescriptor("list_tables", MCPToolDispatchKind.METADATA,
                createPagedMetadataInputDefinition("Logical database name.", "Schema name."))) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createPagedMetadataToolRequest("list_tables", arguments);
            }
        },
        
        LIST_VIEWS(createDescriptor("list_views", MCPToolDispatchKind.METADATA,
                createPagedMetadataInputDefinition("Logical database name.", "Schema name."))) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createPagedMetadataToolRequest("list_views", arguments);
            }
        },
        
        LIST_COLUMNS(createDescriptor("list_columns", MCPToolDispatchKind.METADATA, MCPToolInputDefinition.create(
                requiredStringField("database", "Logical database name."),
                requiredStringField("schema", "Schema name."),
                requiredStringField("object_type", "Parent object type: table or view."),
                requiredStringField("object_name", "Parent object name."),
                optionalStringField("search", "Optional fuzzy filter."),
                optionalIntegerField("page_size", "Requested page size."),
                optionalStringField("page_token", "Opaque pagination token.")))) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createListColumnsRequest(arguments);
            }
        },
        
        LIST_INDEXES(createDescriptor("list_indexes", MCPToolDispatchKind.METADATA, MCPToolInputDefinition.create(
                requiredStringField("database", "Logical database name."),
                requiredStringField("schema", "Schema name."),
                requiredStringField("table", "Table name."),
                optionalStringField("search", "Optional fuzzy filter."),
                optionalIntegerField("page_size", "Requested page size."),
                optionalStringField("page_token", "Opaque pagination token.")))) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createListIndexesRequest(arguments);
            }
        },
        
        SEARCH_METADATA(createDescriptor("search_metadata", MCPToolDispatchKind.METADATA, MCPToolInputDefinition.create(
                optionalStringField("database", "Optional logical database name."),
                optionalStringField("schema", "Optional schema name."),
                requiredStringField("query", "Search query."),
                optionalStringArrayField("object_types", "Optional object-type filter."),
                optionalIntegerField("page_size", "Requested page size."),
                optionalStringField("page_token", "Opaque pagination token.")))) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createSearchMetadataRequest(arguments);
            }
        },
        
        DESCRIBE_TABLE(createDescriptor("describe_table", MCPToolDispatchKind.METADATA, MCPToolInputDefinition.create(
                requiredStringField("database", "Logical database name."),
                requiredStringField("schema", "Schema name."),
                requiredStringField("table", "Table name.")))) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createDescribeTableRequest(arguments);
            }
        },
        
        DESCRIBE_VIEW(createDescriptor("describe_view", MCPToolDispatchKind.METADATA, MCPToolInputDefinition.create(
                requiredStringField("database", "Logical database name."),
                requiredStringField("schema", "Schema name."),
                requiredStringField("view", "View name.")))) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createDescribeViewRequest(arguments);
            }
        },
        
        GET_CAPABILITIES(createDescriptor("get_capabilities", MCPToolDispatchKind.CAPABILITY, MCPToolInputDefinition.create(
                optionalStringField("database", "Optional logical database name.")))),
        
        EXECUTE_QUERY(createDescriptor("execute_query", MCPToolDispatchKind.EXECUTION, MCPToolInputDefinition.create(
                requiredStringField("database", "Logical database name."),
                optionalStringField("schema", "Optional schema name."),
                requiredStringField("sql", "Single SQL statement."),
                optionalIntegerField("max_rows", "Optional maximum row count."),
                optionalIntegerField("timeout_ms", "Optional timeout in milliseconds."))));
        
        private final MCPToolDescriptor descriptor;
        
        ToolDefinition(final MCPToolDescriptor descriptor) {
            this.descriptor = descriptor;
        }
        
        String getName() {
            return descriptor.getName();
        }
        
        MCPToolDescriptor getDescriptor() {
            return descriptor;
        }
        
        ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
            throw new UnsupportedOperationException("Not a metadata tool.");
        }
    }
    
    private static MCPToolDescriptor createDescriptor(final String name, final MCPToolDispatchKind dispatchKind, final MCPToolInputDefinition inputDefinition) {
        return MCPToolDescriptor.create(name, createTitle(name), "ShardingSphere MCP tool: " + name, dispatchKind, inputDefinition);
    }
    
    private static String createTitle(final String toolName) {
        String[] segments = toolName.split("_");
        List<String> words = new ArrayList<>(segments.length);
        for (String each : segments) {
            if (!each.isEmpty()) {
                words.add(Character.toUpperCase(each.charAt(0)) + each.substring(1));
            }
        }
        return String.join(" ", words);
    }
    
    private static MCPToolInputDefinition createPagedMetadataInputDefinition(final String databaseDescription, final String schemaDescription) {
        return MCPToolInputDefinition.create(
                requiredStringField("database", databaseDescription),
                requiredStringField("schema", schemaDescription),
                optionalStringField("search", "Optional fuzzy filter."),
                optionalIntegerField("page_size", "Requested page size."),
                optionalStringField("page_token", "Opaque pagination token."));
    }
    
    private static MCPToolFieldDefinition requiredStringField(final String name, final String description) {
        return MCPToolFieldDefinition.required(name, MCPToolValueDefinition.string(description));
    }
    
    private static MCPToolFieldDefinition optionalStringField(final String name, final String description) {
        return MCPToolFieldDefinition.optional(name, MCPToolValueDefinition.string(description));
    }
    
    private static MCPToolFieldDefinition optionalIntegerField(final String name, final String description) {
        return MCPToolFieldDefinition.optional(name, MCPToolValueDefinition.integer(description));
    }
    
    private static MCPToolFieldDefinition optionalStringArrayField(final String name, final String description) {
        return MCPToolFieldDefinition.optional(name, MCPToolValueDefinition.stringArray(description));
    }
}
