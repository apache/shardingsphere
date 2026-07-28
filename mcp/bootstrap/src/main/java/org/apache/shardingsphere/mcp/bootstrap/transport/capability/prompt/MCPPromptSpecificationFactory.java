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

package org.apache.shardingsphere.mcp.bootstrap.transport.capability.prompt;

import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.capability.prompt.MCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.api.capability.prompt.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportErrorFactory;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPPromptTemplateBinding;
import org.apache.shardingsphere.mcp.support.descriptor.MCPPromptTemplateLoader;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * MCP prompt specification factory.
 */
public final class MCPPromptSpecificationFactory {
    
    private final Collection<MCPPromptDescriptor> promptDescriptors = MCPDescriptorCatalogIndex.getPromptDescriptors();
    
    /**
     * Create MCP prompt specifications.
     *
     * @return prompt specifications
     */
    public List<SyncPromptSpecification> createPromptSpecifications() {
        return promptDescriptors.stream().map(each -> new SyncPromptSpecification(createPrompt(each), (exchange, request) -> handle(request, each))).toList();
    }
    
    private McpSchema.Prompt createPrompt(final MCPPromptDescriptor descriptor) {
        return new McpSchema.Prompt(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getArguments().stream().map(this::createPromptArgument).toList(), descriptor.getMeta());
    }
    
    private McpSchema.PromptArgument createPromptArgument(final MCPPromptArgumentDescriptor descriptor) {
        return new McpSchema.PromptArgument(descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.isRequired());
    }
    
    private McpSchema.GetPromptResult handle(final McpSchema.GetPromptRequest request, final MCPPromptDescriptor descriptor) {
        Map<String, Object> arguments = Optional.ofNullable(request.arguments()).orElse(Map.of());
        validateRequiredArguments(descriptor, arguments);
        MCPPromptTemplateBinding binding = MCPDescriptorCatalogIndex.getRequiredPromptTemplateBinding(descriptor.getName());
        String text = MCPPromptTemplateLoader.render(MCPPromptTemplateLoader.load(binding.getTemplateResource()), arguments);
        return new McpSchema.GetPromptResult(descriptor.getDescription(), List.of(new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent(text))), descriptor.getMeta());
    }
    
    private void validateRequiredArguments(final MCPPromptDescriptor descriptor, final Map<String, Object> arguments) {
        for (MCPPromptArgumentDescriptor each : descriptor.getArguments()) {
            if (each.isRequired() && Objects.toString(arguments.get(each.getName()), "").trim().isEmpty()) {
                throw MCPTransportErrorFactory.createError(new MCPInvalidRequestException(String.format("Required prompt argument `%s` is missing.", each.getName())));
            }
        }
    }
}
