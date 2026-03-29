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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;

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

/**
 * Core MCP tool catalog and contract normalization.
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
    
    private static final List<String> SUPPORTED_TOOL_NAMES = createSupportedToolNames();
    
    /**
     * Get supported tool names.
     *
     * @return supported tool names
     */
    public List<String> getSupportedTools() {
        return SUPPORTED_TOOL_NAMES;
    }
    
    /**
     * Determine whether the named tool exists.
     *
     * @param toolName tool name
     * @return {@code true} when supported
     */
    public boolean contains(final String toolName) {
        return findDefinition(toolName).isPresent();
    }
    
    /**
     * Get tool title.
     *
     * @param toolName tool name
     * @return tool title
     */
    public String getTitle(final String toolName) {
        return findDefinition(toolName).orElseThrow(() -> new IllegalStateException(String.format("Unknown MCP tool `%s`.", toolName))).getTitle();
    }
    
    /**
     * Determine whether the named tool dispatches through metadata discovery.
     *
     * @param toolName tool name
     * @return {@code true} when the tool is a metadata tool
     */
    public boolean isMetadataTool(final String toolName) {
        return findDefinition(toolName).map(ToolDefinition::isMetadataTool).orElse(false);
    }
    
    /**
     * Create a metadata tool request for the named tool.
     *
     * @param toolName tool name
     * @param arguments raw tool arguments
     * @return normalized metadata tool request
     */
    public ToolRequest createMetadataToolRequest(final String toolName, final Map<String, Object> arguments) {
        return findDefinition(toolName).orElseThrow(() -> new IllegalStateException(String.format("Unknown MCP tool `%s`.", toolName))).createMetadataToolRequest(arguments);
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
     * @param databaseRuntime database runtime
     * @return normalized execute-query request
     */
    public ExecutionRequest createExecutionRequest(final String sessionId, final Map<String, Object> arguments, final DatabaseRuntime databaseRuntime) {
        return new ExecutionRequest(sessionId, stringArgument(arguments, "database"), stringArgument(arguments, "schema"), stringArgument(arguments, "sql"),
                integerArgument(arguments, "max_rows", 0), integerArgument(arguments, "timeout_ms", 0), databaseRuntime);
    }
    
    private Optional<ToolDefinition> findDefinition(final String toolName) {
        for (ToolDefinition each : SUPPORTED_TOOLS) {
            if (each.getName().equals(toolName)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private static List<String> createSupportedToolNames() {
        List<String> result = new ArrayList<>(SUPPORTED_TOOLS.size());
        for (ToolDefinition each : SUPPORTED_TOOLS) {
            result.add(each.getName());
        }
        return List.copyOf(result);
    }
    
    private static ToolRequest createPagedMetadataToolRequest(final String toolName, final Map<String, Object> arguments) {
        return createToolRequest(toolName, arguments, stringArgument(arguments, "database"), stringArgument(arguments, "schema"),
                "", "", Collections.emptySet(), integerArgument(arguments, "page_size", 100), stringArgument(arguments, "page_token"));
    }
    
    private static ToolRequest createToolRequest(final String toolName, final Map<String, Object> arguments, final String database,
                                                 final String schema, final String objectName, final String parentObjectType,
                                                 final Set<MetadataObjectType> objectTypes, final int pageSize, final String pageToken) {
        String query = stringArgument(arguments, "query");
        if (query.isEmpty()) {
            query = stringArgument(arguments, "search");
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
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private enum ToolDefinition {
        
        LIST_DATABASES("list_databases", true) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createToolRequest("list_databases", arguments, "", "", "", "", Collections.emptySet(), 100, "");
            }
        },
        
        LIST_SCHEMAS("list_schemas", true) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createToolRequest("list_schemas", arguments, stringArgument(arguments, "database"), "", "", "", Collections.emptySet(), 100, "");
            }
        },
        
        LIST_TABLES("list_tables", true) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createPagedMetadataToolRequest("list_tables", arguments);
            }
        },
        
        LIST_VIEWS("list_views", true) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createPagedMetadataToolRequest("list_views", arguments);
            }
        },
        
        LIST_COLUMNS("list_columns", true) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createToolRequest("list_columns", arguments, stringArgument(arguments, "database"), stringArgument(arguments, "schema"),
                        stringArgument(arguments, "object_name"), stringArgument(arguments, "object_type").toUpperCase(Locale.ENGLISH),
                        Collections.emptySet(), integerArgument(arguments, "page_size", 100), stringArgument(arguments, "page_token"));
            }
        },
        
        LIST_INDEXES("list_indexes", true) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createToolRequest("list_indexes", arguments, stringArgument(arguments, "database"), stringArgument(arguments, "schema"),
                        stringArgument(arguments, "table"), "TABLE", Collections.emptySet(), integerArgument(arguments, "page_size", 100), stringArgument(arguments, "page_token"));
            }
        },
        
        SEARCH_METADATA("search_metadata", true) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createToolRequest("search_metadata", arguments, stringArgument(arguments, "database"), stringArgument(arguments, "schema"),
                        "", "", objectTypes(arguments), integerArgument(arguments, "page_size", 100), stringArgument(arguments, "page_token"));
            }
        },
        
        DESCRIBE_TABLE("describe_table", true) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createToolRequest("describe_table", arguments, stringArgument(arguments, "database"), stringArgument(arguments, "schema"),
                        stringArgument(arguments, "table"), "", Collections.emptySet(), 100, "");
            }
        },
        
        DESCRIBE_VIEW("describe_view", true) {
            
            @Override
            ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
                return createToolRequest("describe_view", arguments, stringArgument(arguments, "database"), stringArgument(arguments, "schema"),
                        stringArgument(arguments, "view"), "", Collections.emptySet(), 100, "");
            }
        },
        
        GET_CAPABILITIES("get_capabilities", false),
        
        EXECUTE_QUERY("execute_query", false);
        
        private final String name;
        
        private final boolean metadataTool;
        
        String getName() {
            return name;
        }
        
        boolean isMetadataTool() {
            return metadataTool;
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
        
        ToolRequest createMetadataToolRequest(final Map<String, Object> arguments) {
            throw new UnsupportedOperationException("Not a metadata tool.");
        }
    }
}
