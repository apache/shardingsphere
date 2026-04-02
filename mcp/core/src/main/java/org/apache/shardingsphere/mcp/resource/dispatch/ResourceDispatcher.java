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

import org.apache.shardingsphere.mcp.resource.ResourceReadPlan;

import java.util.List;
import java.util.Optional;

/**
 * Dispatcher for resource URIs.
 */
public final class ResourceDispatcher {
    
    private final ResourceHandlerRegistry handlerRegistry;
    
    private final ResourceHandlerMapping handlerMapping;
    
    /**
     * Create resource dispatcher.
     */
    public ResourceDispatcher() {
        handlerRegistry = new ResourceHandlerRegistry();
        handlerMapping = new ResourceHandlerMapping(handlerRegistry);
    }
    
    /**
     * Get supported resource URI surfaces.
     *
     * @return supported resource URI surfaces
     */
    public List<String> getSupportedResources() {
        return handlerRegistry.getSupportedResources();
    }
    
    /**
     * Dispatch resource URI.
     *
     * @param resourceUri resource URI
     * @return resource read plan when supported
     */
    public Optional<ResourceReadPlan> dispatch(final String resourceUri) {
        return handlerMapping.findHandler(resourceUri).map(optional -> optional.getHandler().handle(optional.getUriMatch()));
    }
    
}
