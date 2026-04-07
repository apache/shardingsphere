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
import org.apache.shardingsphere.mcp.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;

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
     * @param supportedObjectTypes supported object types
     * @return object types
     * @throws MCPInvalidRequestException object types is malformed or unsupported
     */
    public Set<SupportedMCPMetadataObjectType> getObjectTypes(final Set<SupportedMCPMetadataObjectType> supportedObjectTypes) {
        Object rawValue = arguments.get("object_types");
        if (null == rawValue) {
            return Collections.emptySet();
        }
        if (!(rawValue instanceof Collection)) {
            throw new MCPInvalidRequestException("object_types must be an array.");
        }
        Collection<?> objectTypes = (Collection<?>) rawValue;
        if (objectTypes.isEmpty()) {
            return Collections.emptySet();
        }
        Set<SupportedMCPMetadataObjectType> result = new LinkedHashSet<>(objectTypes.size(), 1F);
        for (Object each : objectTypes) {
            result.add(resolveObjectType(each, supportedObjectTypes));
        }
        return result;
    }
    
    private SupportedMCPMetadataObjectType resolveObjectType(final Object objectType, final Set<SupportedMCPMetadataObjectType> supportedObjectTypes) {
        String actualValue = Objects.toString(objectType, "").trim();
        if (actualValue.isEmpty()) {
            throw new MCPInvalidRequestException("object_types cannot contain blank values.");
        }
        try {
            SupportedMCPMetadataObjectType result = SupportedMCPMetadataObjectType.valueOf(actualValue.toUpperCase(Locale.ENGLISH));
            if (supportedObjectTypes.contains(result)) {
                return result;
            }
        } catch (final IllegalArgumentException ex) {
            throw new MCPInvalidRequestException(String.format("Unsupported object_types value `%s`.", actualValue), ex);
        }
        throw new MCPInvalidRequestException(String.format("Unsupported object_types value `%s`.", actualValue));
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
