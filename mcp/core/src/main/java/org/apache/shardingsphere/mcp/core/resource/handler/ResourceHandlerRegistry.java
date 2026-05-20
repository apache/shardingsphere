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
import org.apache.shardingsphere.mcp.api.MCPHandlerContext;
import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.handler.MCPHandlerContexts;
import org.apache.shardingsphere.mcp.core.resource.uri.MCPUriPattern;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPHandlerDescriptorUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Resource handler registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceHandlerRegistry {
    
    private static final Map<MCPUriPattern, MCPResourceHandler<?>> REGISTERED_RESOURCES;
    
    private static final Map<MCPUriPattern, MCPResourceDescriptor> REGISTERED_RESOURCE_DESCRIPTORS;
    
    static {
        REGISTERED_RESOURCES = createRegisteredResources(
                ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class).stream().flatMap(each -> each.getResourceHandlers().stream()).toList());
        validateRegisteredResources();
        REGISTERED_RESOURCE_DESCRIPTORS = createRegisteredResourceDescriptors();
        validateRegisteredResourceDescriptors();
    }
    
    private static Map<MCPUriPattern, MCPResourceHandler<?>> createRegisteredResources(final Collection<MCPResourceHandler<?>> handlers) {
        ShardingSpherePreconditions.checkNotEmpty(handlers, () -> new IllegalStateException("No resource handlers are registered."));
        Map<MCPUriPattern, MCPResourceHandler<?>> result = new LinkedHashMap<>(handlers.size(), 1F);
        for (MCPResourceHandler<?> each : handlers) {
            String uriOrTemplate = each.getResourceUriTemplate();
            ShardingSpherePreconditions.checkState(null != uriOrTemplate && !uriOrTemplate.isBlank(),
                    () -> new IllegalArgumentException(String.format("Resource URI or URI template is required for `%s`.", each.getClass().getName())));
            MCPHandlerContexts.validateContextType(each.getContextType(), each.getClass());
            result.put(new MCPUriPattern(uriOrTemplate), each);
        }
        return result;
    }
    
    private static Map<MCPUriPattern, MCPResourceDescriptor> createRegisteredResourceDescriptors() {
        Map<MCPUriPattern, MCPResourceDescriptor> result = new LinkedHashMap<>(REGISTERED_RESOURCES.size(), 1F);
        for (Entry<MCPUriPattern, MCPResourceHandler<?>> entry : REGISTERED_RESOURCES.entrySet()) {
            result.put(entry.getKey(), MCPHandlerDescriptorUtils.getRequiredResourceDescriptor(entry.getValue()));
        }
        return result;
    }
    
    private static void validateRegisteredResources() {
        Map<String, Class<?>> registeredPatterns = new HashMap<>(REGISTERED_RESOURCES.size(), 1F);
        for (Entry<MCPUriPattern, MCPResourceHandler<?>> entry : REGISTERED_RESOURCES.entrySet()) {
            String pattern = entry.getKey().getPattern();
            Class<?> previousPatternClass = registeredPatterns.putIfAbsent(pattern, entry.getValue().getClass());
            ShardingSpherePreconditions.checkState(null == previousPatternClass,
                    () -> new IllegalArgumentException(String.format("Duplicate resource URI template `%s` with `%s` and `%s`.",
                            pattern, previousPatternClass.getName(), entry.getValue().getClass().getName())));
        }
        List<Entry<MCPUriPattern, MCPResourceHandler<?>>> entries = new ArrayList<>(REGISTERED_RESOURCES.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            Entry<MCPUriPattern, MCPResourceHandler<?>> current = entries.get(i);
            for (int j = i + 1; j < entries.size(); j++) {
                Entry<MCPUriPattern, MCPResourceHandler<?>> other = entries.get(j);
                ShardingSpherePreconditions.checkState(!current.getKey().isOverlaps(other.getKey()), () -> new IllegalArgumentException(
                        String.format("Overlapping resource URI templates `%s` with `%s` and `%s`.",
                                current.getKey().getPattern(), current.getValue().getClass().getName(), other.getValue().getClass().getName())));
            }
        }
    }
    
    private static void validateRegisteredResourceDescriptors() {
        for (MCPResourceDescriptor each : MCPDescriptorCatalogIndex.getResourceDescriptors()) {
            ShardingSpherePreconditions.checkState(isRegisteredResourceDescriptor(each),
                    () -> new IllegalStateException(String.format("MCP resource descriptor `%s` has no registered handler.", each.getUriTemplate())));
        }
    }
    
    private static boolean isRegisteredResourceDescriptor(final MCPResourceDescriptor descriptor) {
        for (MCPUriPattern each : REGISTERED_RESOURCES.keySet()) {
            if (descriptor.getUriTemplate().equals(each.getPattern())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Dispatch resource URI to registered resource.
     *
     * @param requestScope request scope
     * @param resourceUri resource URI
     * @return handled response
     */
    public static Optional<MCPResponse> dispatch(final MCPRequestScope requestScope, final String resourceUri) {
        for (Entry<MCPUriPattern, MCPResourceHandler<?>> each : REGISTERED_RESOURCES.entrySet()) {
            Optional<MCPUriVariables> matchedUriVariables = each.getKey().parse(resourceUri);
            if (matchedUriVariables.isPresent()) {
                return Optional.of(dispatch(requestScope, each.getValue(), matchedUriVariables.get()));
            }
        }
        return Optional.empty();
    }
    
    private static <T extends MCPHandlerContext> MCPResponse dispatch(final MCPRequestScope requestScope, final MCPResourceHandler<T> resourceHandler, final MCPUriVariables uriVariables) {
        return resourceHandler.handle(resourceHandler.getContextType().cast(requestScope), uriVariables);
    }
    
    /**
     * Get supported resources.
     *
     * @return supported resources
     */
    public static Collection<String> getSupportedResources() {
        return REGISTERED_RESOURCES.keySet().stream().map(MCPUriPattern::getPattern).toList();
    }
    
    /**
     * Get supported resource descriptors.
     *
     * @return supported resource descriptors
     */
    public static Collection<MCPResourceDescriptor> getSupportedResourceDescriptors() {
        return REGISTERED_RESOURCE_DESCRIPTORS.values().stream().toList();
    }
}
