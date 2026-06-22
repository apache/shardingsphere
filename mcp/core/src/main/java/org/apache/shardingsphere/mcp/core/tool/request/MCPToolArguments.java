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

package org.apache.shardingsphere.mcp.core.tool.request;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidMetadataObjectTypesException;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
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
        } catch (final IllegalArgumentException ignored) {
        }
        throw new MCPInvalidMetadataObjectTypesException(actualValue, createAllowedObjectTypes(supportedObjectTypes));
    }
    
    private List<String> createAllowedObjectTypes(final Set<SupportedMCPMetadataObjectType> supportedObjectTypes) {
        return supportedObjectTypes.stream().map(each -> each.name().toLowerCase(Locale.ENGLISH)).toList();
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
    
    /**
     * Get bounded integer argument.
     *
     * @param name argument name
     * @param defaultValue default value
     * @param minValue minimum accepted value
     * @param maxValue maximum accepted value
     * @return argument value
     * @throws MCPInvalidRequestException when value is not an integer or is outside the accepted range
     */
    public int getIntegerArgument(final String name, final int defaultValue, final int minValue, final int maxValue) {
        Object value = arguments.get(name);
        int result = parseIntegerArgument(name, value, defaultValue);
        if (result < minValue || result > maxValue) {
            throw new MCPInvalidRequestException(String.format("%s must be an integer between %d and %d.", name, minValue, maxValue));
        }
        return result;
    }
    
    private int parseIntegerArgument(final String name, final Object value, final int defaultValue) {
        if (null == value) {
            return defaultValue;
        }
        String actualValue = Objects.toString(value, "").trim();
        if (actualValue.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(actualValue);
        } catch (final NumberFormatException ex) {
            throw new MCPInvalidRequestException(String.format("%s must be an integer.", name), ex);
        }
    }
    
    /**
     * Get boolean argument.
     *
     * @param name argument name
     * @param defaultValue default value
     * @return argument value
     */
    public boolean getBooleanArgument(final String name, final boolean defaultValue) {
        Object result = arguments.get(name);
        if (null == result) {
            return defaultValue;
        }
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        String actualValue = result.toString().trim();
        if (actualValue.isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(actualValue);
    }
    
    /**
     * Get string collection argument.
     *
     * @param name argument name
     * @return string collection
     */
    public List<String> getStringCollectionArgument(final String name) {
        Object rawValue = arguments.get(name);
        if (!(rawValue instanceof Collection)) {
            return Collections.emptyList();
        }
        List<String> result = new LinkedList<>();
        for (Object each : (Collection<?>) rawValue) {
            String actualValue = Objects.toString(each, "").trim();
            if (!actualValue.isEmpty()) {
                result.add(actualValue);
            }
        }
        return result;
    }
    
}
