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

package org.apache.shardingsphere.mcp.resource.handler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.uri.MCPUriPattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Resource handler registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceHandlerRegistry {
    
    private static final Map<MCPUriPattern, ResourceHandler> REGISTERED_HANDLERS;
    
    private static final List<String> SUPPORTED_RESOURCES;
    
    static {
        REGISTERED_HANDLERS = createRegisteredHandlers();
        validateRegisteredHandlers();
        SUPPORTED_RESOURCES = REGISTERED_HANDLERS.keySet().stream().map(MCPUriPattern::getPattern).toList();
    }
    
    private static Map<MCPUriPattern, ResourceHandler> createRegisteredHandlers() {
        Collection<ResourceHandler> handlers = ShardingSphereServiceLoader.getServiceInstances(ResourceHandler.class);
        ShardingSpherePreconditions.checkNotEmpty(handlers, () -> new IllegalStateException("No resource handlers are registered."));
        Map<MCPUriPattern, ResourceHandler> result = new LinkedHashMap<>(handlers.size(), 1F);
        for (ResourceHandler each : handlers) {
            ShardingSpherePreconditions.checkState(null != each.getUriPattern() && !each.getUriPattern().isEmpty(),
                    () -> new IllegalArgumentException(String.format("Resource URI pattern is required for `%s`.", each.getClass().getName())));
            result.put(new MCPUriPattern(each.getUriPattern()), each);
        }
        return Collections.unmodifiableMap(result);
    }
    
    private static void validateRegisteredHandlers() {
        Map<String, Class<?>> registeredPatterns = new HashMap<>(REGISTERED_HANDLERS.size(), 1F);
        for (Entry<MCPUriPattern, ResourceHandler> entry : REGISTERED_HANDLERS.entrySet()) {
            String pattern = entry.getKey().getPattern();
            Class<?> previousPatternClass = registeredPatterns.putIfAbsent(pattern, entry.getValue().getClass());
            ShardingSpherePreconditions.checkState(null == previousPatternClass,
                    () -> new IllegalArgumentException(String.format("Duplicate resource URI pattern `%s` with `%s` and `%s`.",
                            pattern, previousPatternClass.getName(), entry.getValue().getClass().getName())));
        }
        List<Entry<MCPUriPattern, ResourceHandler>> entries = new ArrayList<>(REGISTERED_HANDLERS.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            Entry<MCPUriPattern, ResourceHandler> current = entries.get(i);
            for (int j = i + 1; j < entries.size(); j++) {
                Entry<MCPUriPattern, ResourceHandler> other = entries.get(j);
                ShardingSpherePreconditions.checkState(!current.getKey().isOverlaps(other.getKey()), () -> new IllegalArgumentException(
                        String.format("Overlapping resource URI patterns `%s` with `%s` and `%s`.",
                                current.getKey().getPattern(), current.getValue().getClass().getName(), other.getValue().getClass().getName())));
            }
        }
    }
    
    /**
     * Get registered handlers.
     *
     * @return registered handlers
     */
    public static Map<MCPUriPattern, ResourceHandler> getRegisteredHandlers() {
        return REGISTERED_HANDLERS;
    }
    
    /**
     * Get supported resources.
     *
     * @return supported resources
     */
    public static List<String> getSupportedResources() {
        return SUPPORTED_RESOURCES;
    }
}
