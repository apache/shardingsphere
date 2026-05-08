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

package org.apache.shardingsphere.mcp.bootstrap.transport.completion;

import io.modelcontextprotocol.server.McpServerFeatures.SyncCompletionSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.mcp.core.completion.MCPCompletionResult;
import org.apache.shardingsphere.mcp.core.completion.MCPCompletionService;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * MCP completion specification factory.
 */
public final class MCPCompletionSpecificationFactory {

    private final MCPCompletionService completionService;

    private final List<MCPCompletionTargetDescriptor> completionTargetDescriptors;
    
    public MCPCompletionSpecificationFactory(final MCPRuntimeContext runtimeContext) {
        completionService = new MCPCompletionService(runtimeContext);
        completionTargetDescriptors = List.copyOf(MCPDescriptorRegistry.getCompletionTargetDescriptors());
    }
    
    /**
     * Create MCP completion specifications.
     *
     * @return completion specifications
     */
    public List<SyncCompletionSpecification> createCompletionSpecifications() {
        return completionTargetDescriptors.stream().map(each -> new SyncCompletionSpecification(createReference(each), (exchange, request) -> handle(exchange, request, each))).toList();
    }
    
    private McpSchema.CompleteReference createReference(final MCPCompletionTargetDescriptor descriptor) {
        return "prompt".equals(descriptor.getReferenceType()) ? new McpSchema.PromptReference(descriptor.getReference()) : new McpSchema.ResourceReference(descriptor.getReference());
    }
    
    private McpSchema.CompleteResult handle(final McpSyncServerExchange exchange, final McpSchema.CompleteRequest request,
                                            final MCPCompletionTargetDescriptor descriptor) {
        String argumentName = request.argument().name();
        String prefix = Objects.toString(request.argument().value(), "");
        Map<String, String> contextArguments = new LinkedHashMap<>(null == request.context() || null == request.context().arguments() ? Map.of() : request.context().arguments());
        MCPCompletionResult result = completionService.complete(exchange.sessionId(), descriptor, argumentName, prefix, contextArguments);
        return new McpSchema.CompleteResult(new McpSchema.CompleteResult.CompleteCompletion(result.getValues(), result.getTotal(), result.isHasMore()), result.getMeta());
    }
}
