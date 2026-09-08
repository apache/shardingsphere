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

package org.apache.shardingsphere.mcp.core.resource.handler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.MCPRequestContext;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceURIVariables;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.core.context.MCPFeatureRuntimeRequestContext;
import org.apache.shardingsphere.mcp.core.handler.MCPRequestContextTypes;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.resource.MCPUriTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Resource definition registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceDefinitionRegistry {
    
    private static final List<ResourceDefinition> REGISTERED_RESOURCE_DEFINITIONS;
    
    static {
        Map<MCPUriTemplate, MCPResourceHandler<?>> resourceHandlers = createRegisteredResourceHandlers(
                ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class).stream().flatMap(each -> each.getResourceHandlers().stream()).toList());
        validateRegisteredResourceHandlers(resourceHandlers);
        REGISTERED_RESOURCE_DEFINITIONS = createRegisteredResourceDefinitions(resourceHandlers);
        validateRegisteredResourceDescriptors();
    }
    
    private static Map<MCPUriTemplate, MCPResourceHandler<?>> createRegisteredResourceHandlers(final Collection<MCPResourceHandler<?>> handlers) {
        ShardingSpherePreconditions.checkNotEmpty(handlers, () -> new IllegalStateException("No resource handlers are registered."));
        Map<MCPUriTemplate, MCPResourceHandler<?>> result = new LinkedHashMap<>(handlers.size(), 1F);
        for (MCPResourceHandler<?> each : handlers) {
            String uriTemplate = each.getResourceUriTemplate();
            ShardingSpherePreconditions.checkState(null != uriTemplate && !uriTemplate.isBlank(),
                    () -> new IllegalArgumentException(String.format("Resource URI template is required for `%s`.", each.getClass().getName())));
            MCPRequestContextTypes.validateContextType(each.getContextType(), each.getClass());
            result.put(new MCPUriTemplate(uriTemplate), each);
        }
        return result;
    }
    
    private static void validateRegisteredResourceHandlers(final Map<MCPUriTemplate, MCPResourceHandler<?>> resourceHandlers) {
        Map<String, Class<?>> registeredPatterns = new HashMap<>(resourceHandlers.size(), 1F);
        for (Entry<MCPUriTemplate, MCPResourceHandler<?>> entry : resourceHandlers.entrySet()) {
            String pattern = entry.getValue().getResourceUriTemplate();
            Class<?> previousPatternClass = registeredPatterns.putIfAbsent(pattern, entry.getValue().getClass());
            ShardingSpherePreconditions.checkState(null == previousPatternClass,
                    () -> new IllegalArgumentException(String.format("Duplicate resource URI template `%s` with `%s` and `%s`.",
                            pattern, previousPatternClass.getName(), entry.getValue().getClass().getName())));
        }
        List<Entry<MCPUriTemplate, MCPResourceHandler<?>>> entries = new ArrayList<>(resourceHandlers.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            Entry<MCPUriTemplate, MCPResourceHandler<?>> current = entries.get(i);
            for (int j = i + 1; j < entries.size(); j++) {
                Entry<MCPUriTemplate, MCPResourceHandler<?>> other = entries.get(j);
                ShardingSpherePreconditions.checkState(!current.getKey().overlaps(other.getKey()), () -> new IllegalArgumentException(
                        String.format("Overlapping resource URI templates `%s` with `%s` and `%s`.",
                                current.getValue().getResourceUriTemplate(), current.getValue().getClass().getName(), other.getValue().getClass().getName())));
            }
        }
    }
    
    private static List<ResourceDefinition> createRegisteredResourceDefinitions(final Map<MCPUriTemplate, MCPResourceHandler<?>> handlers) {
        return handlers.entrySet().stream().map(entry -> new ResourceDefinition(entry.getKey(),
                MCPDescriptorCatalogIndex.getRequiredResourceDescriptor(entry.getValue().getResourceUriTemplate()), entry.getValue())).toList();
    }
    
    private static void validateRegisteredResourceDescriptors() {
        for (MCPResourceDescriptor each : MCPDescriptorCatalogIndex.getResourceDescriptors()) {
            ShardingSpherePreconditions.checkState(isRegisteredResourceDescriptor(each),
                    () -> new IllegalStateException(String.format("MCP resource descriptor `%s` has no registered handler.", each.getUriTemplate())));
        }
    }
    
    private static boolean isRegisteredResourceDescriptor(final MCPResourceDescriptor descriptor) {
        for (ResourceDefinition each : REGISTERED_RESOURCE_DEFINITIONS) {
            if (descriptor.getUriTemplate().equals(each.handler().getResourceUriTemplate())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Dispatch resource URI to registered resource.
     *
     * @param requestContext request context
     * @param resourceUri resource URI
     * @return handled payload
     */
    public static Optional<MCPSuccessPayload> dispatch(final MCPFeatureRuntimeRequestContext requestContext, final String resourceUri) {
        for (ResourceDefinition each : REGISTERED_RESOURCE_DEFINITIONS) {
            Optional<MCPResourceURIVariables> matchedUriVariables = each.uriTemplate().parse(resourceUri);
            if (matchedUriVariables.isPresent()) {
                return Optional.of(dispatch(requestContext, each.handler(), matchedUriVariables.get()));
            }
        }
        return Optional.empty();
    }
    
    private static <T extends MCPRequestContext> MCPSuccessPayload dispatch(final MCPFeatureRuntimeRequestContext requestContext, final MCPResourceHandler<T> resourceHandler,
                                                                            final MCPResourceURIVariables uriVariables) {
        return resourceHandler.handle(resourceHandler.getContextType().cast(requestContext), uriVariables);
    }
    
    /**
     * Get supported resource descriptors.
     *
     * @return supported resource descriptors
     */
    public static Collection<MCPResourceDescriptor> getSupportedResourceDescriptors() {
        return REGISTERED_RESOURCE_DEFINITIONS.stream().map(ResourceDefinition::descriptor).toList();
    }
    
    private record ResourceDefinition(MCPUriTemplate uriTemplate, MCPResourceDescriptor descriptor, MCPResourceHandler<?> handler) {
    }
}
