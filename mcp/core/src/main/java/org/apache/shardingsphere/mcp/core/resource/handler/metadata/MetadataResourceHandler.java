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

package org.apache.shardingsphere.mcp.core.resource.handler.metadata;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPItemsResponse;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

/**
 * Metadata resource handler backed by one metadata loader function.
 */
@RequiredArgsConstructor
public final class MetadataResourceHandler implements MCPResourceHandler<MCPDatabaseHandlerContext> {
    
    private final String uriPattern;
    
    private final BiFunction<MCPDatabaseHandlerContext, MCPUriVariables, List<?>> metadataLoader;
    
    @Override
    public Class<MCPDatabaseHandlerContext> getContextType() {
        return MCPDatabaseHandlerContext.class;
    }
    
    @Override
    public MCPResourceDescriptor getResourceDescriptor() {
        return MCPDescriptorRegistry.getRequiredResourceDescriptor(uriPattern);
    }
    
    @Override
    public MCPResponse handle(final MCPDatabaseHandlerContext databaseContext, final MCPUriVariables uriVariables) {
        List<?> items = metadataLoader.apply(databaseContext, uriVariables);
        MCPResourceDescriptor descriptor = getResourceDescriptor();
        Map<String, Object> navigationPayload = createNavigationPayload(descriptor, uriVariables);
        return isDetailResource(descriptor) ? new MCPMapResponse(createDetailPayload(descriptor, items, navigationPayload)) : new MCPItemsResponse(items, navigationPayload);
    }
    
    private boolean isDetailResource(final MCPResourceDescriptor descriptor) {
        return "detail".equals(descriptor.getMeta().get("resourceKind"));
    }
    
    private Map<String, Object> createDetailPayload(final MCPResourceDescriptor descriptor, final List<?> items, final Map<String, Object> navigationPayload) {
        Map<String, Object> result = new LinkedHashMap<>(navigationPayload.size() + 6, 1F);
        result.put("resource_kind", "detail");
        if (descriptor.getMeta().containsKey("objectScope")) {
            result.put("object_scope", descriptor.getMeta().get("objectScope"));
        }
        result.put("found", !items.isEmpty());
        result.put("items", items);
        result.put("count", items.size());
        if (!items.isEmpty()) {
            result.put("item", items.get(0));
        }
        result.putAll(navigationPayload);
        return result;
    }
    
    private Map<String, Object> createNavigationPayload(final MCPResourceDescriptor descriptor, final MCPUriVariables uriVariables) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        Map<String, String> variables = null == uriVariables || null == uriVariables.getVariables() ? Map.of() : uriVariables.getVariables();
        String selfUri = createConcreteUri(descriptor.getUriPattern(), variables);
        if (!selfUri.isEmpty()) {
            result.put("self_uri", selfUri);
        }
        String parentUri = createParentUri(selfUri);
        if (!parentUri.isEmpty()) {
            result.put("parent_uri", parentUri);
        }
        List<String> nextResources = MCPDescriptorRegistry.getResourceNavigationDescriptors().stream()
                .filter(each -> descriptor.getUriPattern().equals(each.getFrom()))
                .filter(each -> each.getTo().startsWith("shardingsphere://"))
                .map(each -> createConcreteUri(each.getTo(), variables)).filter(each -> !each.isEmpty()).toList();
        if (!nextResources.isEmpty()) {
            result.put("next_resources", nextResources);
        }
        return result;
    }
    
    private String createConcreteUri(final String uriPattern, final Map<String, String> variables) {
        String result = uriPattern;
        for (Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result.contains("{") ? "" : result;
    }
    
    private String createParentUri(final String selfUri) {
        String prefix = "shardingsphere://";
        if (!selfUri.startsWith(prefix)) {
            return "";
        }
        String path = selfUri.substring(prefix.length());
        int lastSeparatorIndex = path.lastIndexOf('/');
        return 0 > lastSeparatorIndex ? "" : prefix + path.substring(0, lastSeparatorIndex);
    }
}
