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

package org.apache.shardingsphere.mcp.tool.handler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.MCPToolDispatchKind;
import org.apache.shardingsphere.mcp.tool.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.tool.MCPToolInputDefinition;
import org.apache.shardingsphere.mcp.tool.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.tool.ToolRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Tool handler support.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPToolHandlerSupport {
    
    /**
     * Create tool descriptor.
     *
     * @param name tool name
     * @param dispatchKind dispatch kind
     * @param inputDefinition input definition
     * @return tool descriptor
     */
    public static MCPToolDescriptor createDescriptor(final String name, final MCPToolDispatchKind dispatchKind, final MCPToolInputDefinition inputDefinition) {
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
    
    /**
     * Create paged metadata input definition.
     *
     * @param databaseDescription database field description
     * @param schemaDescription schema field description
     * @return tool input definition
     */
    public static MCPToolInputDefinition createPagedMetadataInputDefinition(final String databaseDescription, final String schemaDescription) {
        return MCPToolInputDefinition.create(
                requiredStringField("database", databaseDescription),
                requiredStringField("schema", schemaDescription),
                optionalStringField("search", "Optional fuzzy filter."),
                optionalIntegerField("page_size", "Requested page size."),
                optionalStringField("page_token", "Opaque pagination token."));
    }
    
    /**
     * Create required string field definition.
     *
     * @param name field name
     * @param description field description
     * @return field definition
     */
    public static MCPToolFieldDefinition requiredStringField(final String name, final String description) {
        return MCPToolFieldDefinition.required(name, MCPToolValueDefinition.string(description));
    }
    
    /**
     * Create optional string field definition.
     *
     * @param name field name
     * @param description field description
     * @return field definition
     */
    public static MCPToolFieldDefinition optionalStringField(final String name, final String description) {
        return MCPToolFieldDefinition.optional(name, MCPToolValueDefinition.string(description));
    }
    
    /**
     * Create optional integer field definition.
     *
     * @param name field name
     * @param description field description
     * @return field definition
     */
    public static MCPToolFieldDefinition optionalIntegerField(final String name, final String description) {
        return MCPToolFieldDefinition.optional(name, MCPToolValueDefinition.integer(description));
    }
    
    /**
     * Create optional string array field definition.
     *
     * @param name field name
     * @param description field description
     * @return field definition
     */
    public static MCPToolFieldDefinition optionalStringArrayField(final String name, final String description) {
        return MCPToolFieldDefinition.optional(name, MCPToolValueDefinition.array(description, MCPToolValueDefinition.string("Array element value.")));
    }
    
    /**
     * Create paged metadata tool request.
     *
     * @param toolName tool name
     * @param arguments raw tool arguments
     * @return metadata tool request
     */
    public static ToolRequest createPagedMetadataToolRequest(final String toolName, final Map<String, Object> arguments) {
        return createToolRequest(toolName, arguments, stringArgument(arguments, "database"), stringArgument(arguments, "schema"),
                "", "", Collections.emptySet(), integerArgument(arguments, "page_size", 100), stringArgument(arguments, "page_token"));
    }
    
    /**
     * Create tool request.
     *
     * @param toolName tool name
     * @param arguments raw tool arguments
     * @param databaseName database name
     * @param schemaName schema name
     * @param objectName object name
     * @param parentObjectType parent object type
     * @param objectTypes object types
     * @param pageSize page size
     * @param pageToken page token
     * @return tool request
     */
    public static ToolRequest createToolRequest(final String toolName, final Map<String, Object> arguments, final String databaseName,
                                                final String schemaName, final String objectName, final String parentObjectType,
                                                final Set<MetadataObjectType> objectTypes, final int pageSize, final String pageToken) {
        String query = stringArgument(arguments, "query");
        if (query.isEmpty()) {
            query = stringArgument(arguments, "search");
        }
        return new ToolRequest(toolName, databaseName, schemaName, objectName, parentObjectType, query, objectTypes, pageSize, pageToken);
    }
    
    /**
     * Get object types.
     *
     * @param arguments raw tool arguments
     * @return object types
     */
    public static Set<MetadataObjectType> getObjectTypes(final Map<String, Object> arguments) {
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
    
    /**
     * Resolve the database argument used by capability tools.
     *
     * @param arguments raw tool arguments
     * @return normalized database argument
     */
    public static String getCapabilityDatabase(final Map<String, Object> arguments) {
        return stringArgument(arguments, "database");
    }
    
    /**
     * Create execute-query request.
     *
     * @param sessionId session identifier
     * @param arguments raw tool arguments
     * @return normalized execute-query request
     */
    public static ExecutionRequest createExecutionRequest(final String sessionId, final Map<String, Object> arguments) {
        return new ExecutionRequest(sessionId, stringArgument(arguments, "database"), stringArgument(arguments, "schema"), stringArgument(arguments, "sql"),
                integerArgument(arguments, "max_rows", 0), integerArgument(arguments, "timeout_ms", 0));
    }
    
    /**
     * Get string argument.
     *
     * @param arguments raw tool arguments
     * @param name argument name
     * @return argument value
     */
    public static String getStringArgument(final Map<String, Object> arguments, final String name) {
        return stringArgument(arguments, name);
    }
    
    /**
     * Get integer argument.
     *
     * @param arguments raw tool arguments
     * @param name argument name
     * @param defaultValue default value
     * @return argument value
     */
    public static int getIntegerArgument(final Map<String, Object> arguments, final String name, final int defaultValue) {
        return integerArgument(arguments, name, defaultValue);
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
}
