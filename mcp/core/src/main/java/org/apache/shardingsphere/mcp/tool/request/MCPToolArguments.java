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

package org.apache.shardingsphere.mcp.tool.request;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * MCP tool arguments.
 */
@RequiredArgsConstructor
public final class MCPToolArguments {
    
    private final Map<String, Object> arguments;
    
    /**
     * Get object types.
     *
     * @return object types
     */
    public Set<MetadataObjectType> getObjectTypes() {
        Object rawValue = arguments.get("object_types");
        if (!(rawValue instanceof Collection)) {
            return Collections.emptySet();
        }
        Set<MetadataObjectType> result = new LinkedHashSet<>(((Collection<?>) rawValue).size(), 1F);
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
     * @return normalized execute-query request
     */
    public ExecutionRequest createExecutionRequest(final String sessionId) {
        return new ExecutionRequest(sessionId,
                getStringArgument("database"), getStringArgument("schema"), getStringArgument("sql"), getIntegerArgument("max_rows", 0), getIntegerArgument("timeout_ms", 0));
    }
    
    /**
     * Get string argument.
     *
     * @param name argument name
     * @return argument value
     */
    public String getStringArgument(final String name) {
        return Objects.toString(arguments.get(name), "").trim();
    }
    
    /**
     * Get integer argument.
     *
     * @param name argument name
     * @param defaultValue default value
     * @return argument value
     */
    public int getIntegerArgument(final String name, final int defaultValue) {
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
