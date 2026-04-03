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

import org.apache.shardingsphere.mcp.uri.UriTemplateMatch;

import java.util.Optional;

/**
 * Resource handler mapping.
 */
public final class ResourceHandlerMapping {
    
    private final ResourceHandlerRegistry handlerRegistry;
    
    /**
     * Create handler mapping.
     *
     * @param handlerRegistry handler registry
     */
    public ResourceHandlerMapping(final ResourceHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }
    
    /**
     * Find handler for one resource URI.
     *
     * @param resourceUri resource URI
     * @return matched handler execution
     */
    public Optional<ResourceHandlerExecution> findHandler(final String resourceUri) {
        for (ResourceHandlerRegistration each : handlerRegistry.getHandlerRegistrations()) {
            Optional<UriTemplateMatch> uriMatch = each.getUriTemplate().match(resourceUri);
            if (uriMatch.isPresent()) {
                return Optional.of(new ResourceHandlerExecution(each.getHandler(), uriMatch.get()));
            }
        }
        return Optional.empty();
    }
}
