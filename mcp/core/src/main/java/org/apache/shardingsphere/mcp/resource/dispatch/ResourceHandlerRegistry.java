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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.uri.UriTemplate;

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
    
    private final List<ResourceHandlerRegistration> handlerRegistrations;
    
    private final List<String> supportedResources;
    
    public ResourceHandlerRegistry(final Collection<ResourceHandler> handlers) {
        List<ResourceHandlerRegistration> registeredHandlers = createHandlerRegistrations(handlers);
        validateHandlerRegistrations(registeredHandlers);
        handlerRegistrations = registeredHandlers;
        supportedResources = registeredHandlers.stream().map(each -> each.getUriTemplate().getTemplate()).collect(Collectors.toList());
    }
    
    private List<ResourceHandlerRegistration> createHandlerRegistrations(final Collection<ResourceHandler> handlers) {
        ShardingSpherePreconditions.checkNotEmpty(handlers, () -> new IllegalStateException("No resource handlers are registered."));
        List<ResourceHandlerRegistration> result = new ArrayList<>(handlers.size());
        for (ResourceHandler each : handlers) {
            ShardingSpherePreconditions.checkState(null != each.getUriTemplate() && !each.getUriTemplate().isEmpty(),
                    () -> new IllegalArgumentException(String.format("Resource URI template is required for `%s`.", each.getClass().getName())));
            result.add(new ResourceHandlerRegistration(each, new UriTemplate(each.getUriTemplate())));
        }
        return result;
    }
    
    private void validateHandlerRegistrations(final List<ResourceHandlerRegistration> handlerRegistrations) {
        Map<String, Class<?>> registeredTemplates = new HashMap<>(handlerRegistrations.size(), 1F);
        for (ResourceHandlerRegistration each : handlerRegistrations) {
            String template = each.getUriTemplate().getTemplate();
            Class<?> previousTemplateClass = registeredTemplates.putIfAbsent(template, each.getHandler().getClass());
            ShardingSpherePreconditions.checkState(null == previousTemplateClass,
                    () -> new IllegalArgumentException(String.format("Duplicate resource URI template `%s` with `%s` and `%s`.",
                            template, previousTemplateClass.getName(), each.getHandler().getClass().getName())));
        }
        for (int i = 0; i < handlerRegistrations.size(); i++) {
            ResourceHandlerRegistration current = handlerRegistrations.get(i);
            for (int j = i + 1; j < handlerRegistrations.size(); j++) {
                ResourceHandlerRegistration other = handlerRegistrations.get(j);
                ShardingSpherePreconditions.checkState(!current.getUriTemplate().overlaps(other.getUriTemplate()), () -> new IllegalArgumentException(
                        String.format("Overlapping resource URI templates `%s` with `%s` and `%s`.",
                                current.getUriTemplate().getTemplate(), current.getHandler().getClass().getName(), other.getHandler().getClass().getName())));
            }
        }
    }
}
