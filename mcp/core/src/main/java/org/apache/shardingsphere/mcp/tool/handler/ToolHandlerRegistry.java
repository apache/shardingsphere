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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolContribution;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition.Type;
import org.apache.shardingsphere.mcp.api.tool.handler.ServerToolHandler;
import org.apache.shardingsphere.mcp.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.database.handler.DatabaseToolHandler;
import org.apache.shardingsphere.mcp.contribution.MCPContributionLoader;
import org.apache.shardingsphere.mcp.workflow.handler.WorkflowToolHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Tool handler registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToolHandlerRegistry {
    
    private static final Map<String, MCPToolContribution> REGISTERED_TOOL_CONTRIBUTIONS;
    
    private static final List<String> SUPPORTED_TOOLS;
    
    private static final List<MCPToolDescriptor> SUPPORTED_TOOL_DESCRIPTORS;
    
    static {
        REGISTERED_TOOL_CONTRIBUTIONS = createRegisteredTools();
        SUPPORTED_TOOLS = List.copyOf(REGISTERED_TOOL_CONTRIBUTIONS.keySet());
        SUPPORTED_TOOL_DESCRIPTORS = REGISTERED_TOOL_CONTRIBUTIONS.values().stream().map(MCPToolContribution::getToolDescriptor).toList();
    }
    
    private static Map<String, MCPToolContribution> createRegisteredTools() {
        return createRegisteredTools(MCPContributionLoader.loadToolContributions());
    }
    
    static Map<String, MCPToolContribution> createRegisteredTools(final Collection<MCPToolContribution> contributions) {
        ShardingSpherePreconditions.checkNotEmpty(contributions, () -> new IllegalStateException("No tool contributions are registered."));
        Map<String, MCPToolContribution> result = new LinkedHashMap<>(contributions.size(), 1F);
        for (MCPToolContribution each : contributions) {
            MCPToolDescriptor descriptor = each.getToolDescriptor();
            ShardingSpherePreconditions.checkState(null != descriptor,
                    () -> new IllegalArgumentException(String.format("Tool descriptor is required for `%s`.", each.getClass().getName())));
            String toolName = descriptor.getName();
            ShardingSpherePreconditions.checkState(null != toolName && !toolName.isBlank(),
                    () -> new IllegalArgumentException(String.format("Tool name is required for `%s`.", each.getClass().getName())));
            validateHandlerType(each);
            MCPToolContribution previousContribution = result.putIfAbsent(toolName, each);
            ShardingSpherePreconditions.checkState(null == previousContribution, () -> new IllegalArgumentException(
                    String.format("Duplicate tool name `%s` with `%s` and `%s`.", toolName, previousContribution.getClass().getName(), each.getClass().getName())));
        }
        return Collections.unmodifiableMap(result);
    }
    
    private static void validateHandlerType(final MCPToolContribution contribution) {
        ShardingSpherePreconditions.checkState(contribution instanceof ServerToolHandler || contribution instanceof DatabaseToolHandler || contribution instanceof WorkflowToolHandler,
                () -> new IllegalArgumentException(String.format("Unsupported tool contribution type `%s`.", contribution.getClass().getName())));
    }
    
    /**
     * Get supported tools.
     *
     * @return supported tools
     */
    public static List<String> getSupportedTools() {
        return SUPPORTED_TOOLS;
    }
    
    /**
     * Get supported tool descriptors.
     *
     * @return supported tool descriptors
     */
    public static List<MCPToolDescriptor> getSupportedToolDescriptors() {
        return SUPPORTED_TOOL_DESCRIPTORS;
    }
    
    /**
     * Find registered tool.
     *
     * @param toolName tool name
     * @return registered tool
     */
    public static Optional<MCPToolContribution> findRegisteredTool(final String toolName) {
        return Optional.ofNullable(REGISTERED_TOOL_CONTRIBUTIONS.get(toolName));
    }
    
    /**
     * Dispatch tool call to registered tool.
     *
     * @param requestScope request scope
     * @param sessionId session identifier
     * @param toolName tool name
     * @param arguments tool arguments
     * @return handled response
     */
    public static Optional<MCPResponse> dispatch(final MCPRequestScope requestScope, final String sessionId,
                                                 final String toolName, final Map<String, Object> arguments) {
        Optional<MCPToolContribution> toolContribution = findRegisteredTool(toolName);
        if (toolContribution.isEmpty()) {
            return Optional.empty();
        }
        checkRequiredArguments(arguments, toolContribution.get().getToolDescriptor());
        return Optional.of(dispatch(requestScope, toolContribution.get(), new MCPToolCall(sessionId, arguments)));
    }
    
    private static MCPResponse dispatch(final MCPRequestScope requestScope, final MCPToolContribution toolContribution, final MCPToolCall toolCall) {
        if (toolContribution instanceof WorkflowToolHandler) {
            return ((WorkflowToolHandler) toolContribution).handle(requestScope, toolCall);
        }
        if (toolContribution instanceof DatabaseToolHandler) {
            return ((DatabaseToolHandler) toolContribution).handle(requestScope, toolCall);
        }
        if (toolContribution instanceof ServerToolHandler) {
            return ((ServerToolHandler) toolContribution).handle(toolCall);
        }
        throw new IllegalArgumentException(String.format("Unsupported tool contribution type `%s`.", toolContribution.getClass().getName()));
    }
    
    private static void checkRequiredArguments(final Map<String, Object> arguments, final MCPToolDescriptor toolDescriptor) {
        for (MCPToolFieldDefinition each : toolDescriptor.getFields()) {
            if (!each.isRequired()) {
                continue;
            }
            ShardingSpherePreconditions.checkContainsKey(arguments, each.getName(), () -> new MCPInvalidRequestException(String.format("%s is required.", each.getName())));
            if (Type.STRING == each.getValueDefinition().getType()) {
                checkRequiredTextArgument(arguments, each.getName());
            }
        }
    }
    
    private static void checkRequiredTextArgument(final Map<String, Object> arguments, final String argumentName) {
        String actualValue = Objects.toString(arguments.get(argumentName), "").trim();
        ShardingSpherePreconditions.checkState(!actualValue.isEmpty(), () -> new MCPInvalidRequestException(String.format("%s is required.", argumentName)));
    }
}
