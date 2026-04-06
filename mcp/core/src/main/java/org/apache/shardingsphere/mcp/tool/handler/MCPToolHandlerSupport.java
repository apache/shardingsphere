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
import org.apache.shardingsphere.mcp.tool.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.tool.MCPToolValueDefinition;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
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
