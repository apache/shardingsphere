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

package org.apache.shardingsphere.mcp.resource.dispatch;

import lombok.Getter;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resource handler registry.
 */
@Getter
public final class ResourceHandlerRegistry {
    
    private final List<ResourceHandler> handlers;
    
    private final List<String> supportedResources;
    
    public ResourceHandlerRegistry() {
        this(ShardingSphereServiceLoader.getServiceInstances(ResourceHandler.class));
    }
    
    ResourceHandlerRegistry(final Collection<ResourceHandler> handlers) {
        ResourceUriMatcher uriMatcher = new ResourceUriMatcher();
        List<ResourceHandler> registeredHandlers = new ArrayList<>(handlers);
        validateHandlers(registeredHandlers, uriMatcher);
        this.handlers = registeredHandlers;
        supportedResources = registeredHandlers.stream().map(ResourceHandler::getUriTemplate).collect(Collectors.toList());
    }
    
    private void validateHandlers(final Collection<ResourceHandler> handlers, final ResourceUriMatcher uriMatcher) {
        if (handlers.isEmpty()) {
            throw new IllegalStateException("No resource handlers are registered.");
        }
        Map<String, Class<?>> registeredTemplates = new HashMap<>(handlers.size(), 1F);
        Map<String, Class<?>> registeredSignatures = new HashMap<>(handlers.size(), 1F);
        for (ResourceHandler each : handlers) {
            if (null == each.getUriTemplate() || each.getUriTemplate().isEmpty()) {
                throw new IllegalArgumentException(String.format("Resource URI template is required for `%s`.", each.getClass().getName()));
            }
            Class<?> previousTemplateClass = registeredTemplates.putIfAbsent(each.getUriTemplate(), each.getClass());
            if (null != previousTemplateClass) {
                throw new IllegalArgumentException(String.format("Duplicate resource URI template `%s` with `%s` and `%s`.",
                        each.getUriTemplate(), previousTemplateClass.getName(), each.getClass().getName()));
            }
            String routeSignature = uriMatcher.createRouteSignature(each.getUriTemplate());
            Class<?> previousSignatureClass = registeredSignatures.putIfAbsent(routeSignature, each.getClass());
            if (null != previousSignatureClass) {
                throw new IllegalArgumentException(String.format("Duplicate resource URI route signature `%s` with `%s` and `%s`.",
                        routeSignature, previousSignatureClass.getName(), each.getClass().getName()));
            }
        }
    }
}
