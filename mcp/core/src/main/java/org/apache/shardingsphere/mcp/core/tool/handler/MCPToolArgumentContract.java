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

package org.apache.shardingsphere.mcp.core.tool.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPExecutionModeRequiredException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPMissingToolArgumentException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPToolArgumentContract {
    
    private static final String EXECUTION_MODE = "execution_mode";
    
    private final String toolName;
    
    private final Map<String, Object> inputSchema;
    
    static MCPToolArgumentContract create(final MCPToolDescriptor toolDescriptor) {
        return new MCPToolArgumentContract(toolDescriptor.getName(), toolDescriptor.getInputSchema());
    }
    
    void validate(final Map<String, Object> arguments) {
        for (String each : getRequiredArgumentNames()) {
            ShardingSpherePreconditions.checkContainsKey(arguments, each, () -> createMissingArgumentException(arguments, each));
            if (isStringArgument(each)) {
                checkRequiredTextArgument(arguments, each);
            }
        }
    }
    
    private Collection<String> getRequiredArgumentNames() {
        Object required = inputSchema.get("required");
        return required instanceof Collection<?> ? ((Collection<?>) required).stream().filter(String.class::isInstance).map(String.class::cast).toList() : List.of();
    }
    
    private boolean isStringArgument(final String argumentName) {
        Map<?, ?> property = findProperty(argumentName);
        return "string".equals(property.get("type"));
    }
    
    private void checkRequiredTextArgument(final Map<String, Object> arguments, final String argumentName) {
        String actualValue = Objects.toString(arguments.get(argumentName), "").trim();
        ShardingSpherePreconditions.checkState(!actualValue.isEmpty(), () -> createMissingArgumentException(arguments, argumentName));
    }
    
    private RuntimeException createMissingArgumentException(final Map<String, Object> arguments, final String argumentName) {
        return EXECUTION_MODE.equals(argumentName)
                ? new MCPExecutionModeRequiredException(toolName, getEnumValues(argumentName), createExecutionModeSuggestedArguments(arguments))
                : new MCPMissingToolArgumentException(argumentName);
    }
    
    private List<String> getEnumValues(final String argumentName) {
        Object enumValues = findProperty(argumentName).get("enum");
        return enumValues instanceof Collection<?> ? ((Collection<?>) enumValues).stream().filter(String.class::isInstance).map(String.class::cast).toList() : List.of();
    }
    
    private Map<String, Object> createExecutionModeSuggestedArguments(final Map<String, Object> arguments) {
        Map<String, Object> result = new LinkedHashMap<>(arguments);
        result.remove(EXECUTION_MODE);
        result.put(EXECUTION_MODE, "preview");
        return result;
    }
    
    private Map<?, ?> findProperty(final String argumentName) {
        Object properties = inputSchema.get("properties");
        if (!(properties instanceof Map<?, ?>) || !(((Map<?, ?>) properties).get(argumentName) instanceof Map<?, ?>)) {
            return Map.of();
        }
        return (Map<?, ?>) ((Map<?, ?>) properties).get(argumentName);
    }
}
