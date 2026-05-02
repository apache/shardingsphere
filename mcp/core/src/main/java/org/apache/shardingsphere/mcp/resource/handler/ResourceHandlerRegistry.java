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
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceContribution;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.handler.ServerResourceHandler;
import org.apache.shardingsphere.mcp.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.database.handler.DatabaseResourceHandler;
import org.apache.shardingsphere.mcp.contribution.MCPContributionLoader;
import org.apache.shardingsphere.mcp.resource.uri.MCPUriPattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    
    private static final Map<MCPUriPattern, MCPResourceContribution> REGISTERED_RESOURCES;
    
    private static final List<String> SUPPORTED_RESOURCES;
    
    static {
        REGISTERED_RESOURCES = createRegisteredResources();
        validateRegisteredResources();
        SUPPORTED_RESOURCES = REGISTERED_RESOURCES.keySet().stream().map(MCPUriPattern::getPattern).toList();
    }
    
    private static Map<MCPUriPattern, MCPResourceContribution> createRegisteredResources() {
        return createRegisteredResources(MCPContributionLoader.loadResourceContributions());
    }
    
    static Map<MCPUriPattern, MCPResourceContribution> createRegisteredResources(final Collection<MCPResourceContribution> contributions) {
        ShardingSpherePreconditions.checkNotEmpty(contributions, () -> new IllegalStateException("No resource contributions are registered."));
        Map<MCPUriPattern, MCPResourceContribution> result = new LinkedHashMap<>(contributions.size(), 1F);
        for (MCPResourceContribution each : contributions) {
            String uriPattern = each.getUriPattern();
            ShardingSpherePreconditions.checkState(null != uriPattern && !uriPattern.isBlank(),
                    () -> new IllegalArgumentException(String.format("Resource URI pattern is required for `%s`.", each.getClass().getName())));
            validateHandlerType(each);
            result.put(new MCPUriPattern(uriPattern), each);
        }
        return Collections.unmodifiableMap(result);
    }
    
    private static void validateHandlerType(final MCPResourceContribution contribution) {
        ShardingSpherePreconditions.checkState(contribution instanceof ServerResourceHandler || contribution instanceof DatabaseResourceHandler,
                () -> new IllegalArgumentException(String.format("Unsupported resource contribution type `%s`.", contribution.getClass().getName())));
    }
    
    private static void validateRegisteredResources() {
        Map<String, Class<?>> registeredPatterns = new HashMap<>(REGISTERED_RESOURCES.size(), 1F);
        for (Entry<MCPUriPattern, MCPResourceContribution> entry : REGISTERED_RESOURCES.entrySet()) {
            String pattern = entry.getKey().getPattern();
            Class<?> previousPatternClass = registeredPatterns.putIfAbsent(pattern, entry.getValue().getClass());
            ShardingSpherePreconditions.checkState(null == previousPatternClass,
                    () -> new IllegalArgumentException(String.format("Duplicate resource URI pattern `%s` with `%s` and `%s`.",
                            pattern, previousPatternClass.getName(), entry.getValue().getClass().getName())));
        }
        List<Entry<MCPUriPattern, MCPResourceContribution>> entries = new ArrayList<>(REGISTERED_RESOURCES.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            Entry<MCPUriPattern, MCPResourceContribution> current = entries.get(i);
            for (int j = i + 1; j < entries.size(); j++) {
                Entry<MCPUriPattern, MCPResourceContribution> other = entries.get(j);
                ShardingSpherePreconditions.checkState(!current.getKey().isOverlaps(other.getKey()), () -> new IllegalArgumentException(
                        String.format("Overlapping resource URI patterns `%s` with `%s` and `%s`.",
                                current.getKey().getPattern(), current.getValue().getClass().getName(), other.getValue().getClass().getName())));
            }
        }
    }
    
    /**
     * Get registered resources.
     *
     * @return registered resources
     */
    static Map<MCPUriPattern, MCPResourceContribution> getRegisteredResources() {
        return REGISTERED_RESOURCES;
    }
    
    /**
     * Dispatch resource URI to registered resource.
     *
     * @param requestScope request scope
     * @param resourceUri resource URI
     * @return handled response
     */
    public static Optional<MCPResponse> dispatch(final MCPRequestScope requestScope, final String resourceUri) {
        for (Entry<MCPUriPattern, MCPResourceContribution> each : REGISTERED_RESOURCES.entrySet()) {
            Optional<MCPUriVariables> matchedUriVariables = each.getKey().parse(resourceUri);
            if (matchedUriVariables.isPresent()) {
                return Optional.of(dispatch(requestScope, each.getValue(), matchedUriVariables.get()));
            }
        }
        return Optional.empty();
    }
    
    private static MCPResponse dispatch(final MCPRequestScope requestScope, final MCPResourceContribution resourceContribution, final MCPUriVariables uriVariables) {
        if (resourceContribution instanceof DatabaseResourceHandler) {
            return ((DatabaseResourceHandler) resourceContribution).handle(requestScope, uriVariables);
        }
        if (resourceContribution instanceof ServerResourceHandler) {
            return ((ServerResourceHandler) resourceContribution).handle(uriVariables);
        }
        throw new IllegalArgumentException(String.format("Unsupported resource contribution type `%s`.", resourceContribution.getClass().getName()));
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
