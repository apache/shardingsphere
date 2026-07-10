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

package org.apache.shardingsphere.mcp.support.descriptor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.resource.MCPUriTemplate;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validator for MCP completion targets.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPCompletionTargetDescriptorValidator {
    
    /**
     * Validate completion targets against declared prompt and resource descriptors.
     *
     * @param descriptors completion target descriptors
     * @param prompts prompt descriptors
     * @param resources resource descriptors
     */
    public static void validate(final Collection<MCPCompletionTargetDescriptor> descriptors, final Collection<MCPPromptDescriptor> prompts,
                                final Collection<MCPResourceDescriptor> resources) {
        Map<String, Set<String>> promptArguments = prompts.stream().collect(Collectors.toMap(MCPPromptDescriptor::getName,
                each -> each.getArguments().stream().map(MCPPromptArgumentDescriptor::getName).collect(Collectors.toSet())));
        Set<String> promptNames = promptArguments.keySet();
        Map<String, MCPResourceDescriptor> resourceDescriptors = resources.stream().collect(Collectors.toMap(MCPResourceDescriptor::getUriTemplate, each -> each));
        Map<String, MCPCompletionTargetDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        for (MCPCompletionTargetDescriptor each : descriptors) {
            validateCompletionReference(each, promptNames, resourceDescriptors.keySet());
            validatePromptCompletionArguments(each, promptArguments);
            validateResourceCompletionArguments(each, resourceDescriptors);
            validateCompletionRequiredContextArguments(each);
            ShardingSpherePreconditions.checkState(null == registered.putIfAbsent(each.getReferenceType() + ":" + each.getReference(), each),
                    () -> new IllegalStateException(String.format("Duplicate MCP completion target `%s:%s`.", each.getReferenceType(), each.getReference())));
        }
    }
    
    private static void validateCompletionRequiredContextArguments(final MCPCompletionTargetDescriptor descriptor) {
        Object value = descriptor.getMeta().get(MCPShardingSphereMetadataKeys.REQUIRED_CONTEXT_ARGUMENTS);
        if (null == value) {
            return;
        }
        ShardingSpherePreconditions.checkState(value instanceof Map,
                () -> new IllegalStateException(String.format("Completion target `%s:%s` metadata `%s` must be an object.",
                        descriptor.getReferenceType(), descriptor.getReference(), MCPShardingSphereMetadataKeys.REQUIRED_CONTEXT_ARGUMENTS)));
        Set<String> argumentNames = new HashSet<>(descriptor.getArguments());
        for (Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            String argumentName = String.valueOf(entry.getKey());
            ShardingSpherePreconditions.checkState(argumentNames.contains(argumentName),
                    () -> new IllegalStateException(String.format("Completion target `%s:%s` required context argument `%s` is not declared by the target.",
                            descriptor.getReferenceType(), descriptor.getReference(), argumentName)));
            ShardingSpherePreconditions.checkState(entry.getValue() instanceof Collection,
                    () -> new IllegalStateException(String.format("Completion target `%s:%s` required context for `%s` must be a list.",
                            descriptor.getReferenceType(), descriptor.getReference(), argumentName)));
            for (Object each : (Collection<?>) entry.getValue()) {
                ShardingSpherePreconditions.checkState(argumentNames.contains(String.valueOf(each)),
                        () -> new IllegalStateException(String.format("Completion target `%s:%s` context argument `%s` for `%s` is not declared by the target.",
                                descriptor.getReferenceType(), descriptor.getReference(), each, argumentName)));
            }
        }
    }
    
    private static void validatePromptCompletionArguments(final MCPCompletionTargetDescriptor descriptor, final Map<String, Set<String>> promptArguments) {
        if (!"prompt".equals(descriptor.getReferenceType())) {
            return;
        }
        Set<String> argumentNames = promptArguments.getOrDefault(descriptor.getReference(), Set.of());
        for (String each : descriptor.getArguments()) {
            ShardingSpherePreconditions.checkState(argumentNames.contains(each), () -> new IllegalStateException(
                    String.format("Completion target `prompt:%s` argument `%s` is not declared by prompt `%s`.", descriptor.getReference(), each, descriptor.getReference())));
        }
    }
    
    private static void validateResourceCompletionArguments(final MCPCompletionTargetDescriptor descriptor, final Map<String, MCPResourceDescriptor> resources) {
        if (!"resource".equals(descriptor.getReferenceType())) {
            return;
        }
        MCPResourceDescriptor resource = resources.get(descriptor.getReference());
        ShardingSpherePreconditions.checkState(resource.isTemplated(),
                () -> new IllegalStateException(String.format("Completion target `resource:%s` must reference a resource template.", descriptor.getReference())));
        Collection<String> templateVariables = new MCPUriTemplate(resource.getUriTemplate()).getVariableNames();
        for (String each : descriptor.getArguments()) {
            ShardingSpherePreconditions.checkState(templateVariables.contains(each), () -> new IllegalStateException(
                    String.format("Completion target `resource:%s` argument `%s` is not a URI template variable.", descriptor.getReference(), each)));
        }
    }
    
    private static void validateCompletionReference(final MCPCompletionTargetDescriptor descriptor, final Set<String> promptNames, final Set<String> resourceUris) {
        if ("prompt".equals(descriptor.getReferenceType())) {
            ShardingSpherePreconditions.checkState(promptNames.contains(descriptor.getReference()),
                    () -> new IllegalStateException(String.format("Completion target references unknown prompt `%s`.", descriptor.getReference())));
            return;
        }
        ShardingSpherePreconditions.checkState("resource".equals(descriptor.getReferenceType()),
                () -> new IllegalStateException(String.format("Unsupported completion reference type `%s`.", descriptor.getReferenceType())));
        ShardingSpherePreconditions.checkState(resourceUris.contains(descriptor.getReference()),
                () -> new IllegalStateException(String.format("Completion target references unknown resource `%s`.", descriptor.getReference())));
    }
}
