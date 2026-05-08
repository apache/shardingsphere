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
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.MCPUriTemplateUtils;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorRegistry;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPItemsResponse;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Metadata resource handler backed by one metadata loader function.
 */
@RequiredArgsConstructor
public final class MetadataResourceHandler implements MCPResourceHandler<MCPDatabaseHandlerContext> {

    private static final int LARGE_RESULT_THRESHOLD = 100;

    private final String uriTemplate;

    private final BiFunction<MCPDatabaseHandlerContext, MCPUriVariables, List<?>> metadataLoader;

    @Override
    public Class<MCPDatabaseHandlerContext> getContextType() {
        return MCPDatabaseHandlerContext.class;
    }

    @Override
    public MCPResourceDescriptor getResourceDescriptor() {
        return MCPDescriptorRegistry.getRequiredResourceDescriptor(uriTemplate);
    }

    @Override
    public MCPResponse handle(final MCPDatabaseHandlerContext databaseContext, final MCPUriVariables uriVariables) {
        List<?> items = metadataLoader.apply(databaseContext, uriVariables);
        MCPResourceDescriptor descriptor = getResourceDescriptor();
        Map<String, Object> navigationPayload = createNavigationPayload(descriptor, uriVariables);
        if (isDetailResource(descriptor)) {
            if (items.isEmpty()) {
                appendEmptyStateGuidance(navigationPayload, descriptor);
            }
            return new MCPMapResponse(createDetailPayload(descriptor, items, navigationPayload));
        }
        List<?> returnedItems = capListItems(items);
        appendListSizeMetadata(navigationPayload, items.size(), returnedItems.size());
        if (items.isEmpty()) {
            appendEmptyStateGuidance(navigationPayload, descriptor);
        } else if (isTruncated(items, returnedItems)) {
            appendLargeResultGuidance(navigationPayload, descriptor, uriVariables, items.size());
        }
        return new MCPItemsResponse(returnedItems, navigationPayload);
    }

    private boolean isDetailResource(final MCPResourceDescriptor descriptor) {
        return "detail".equals(descriptor.getResourceKind());
    }

    private List<?> capListItems(final List<?> items) {
        return items.size() <= LARGE_RESULT_THRESHOLD ? items : items.subList(0, LARGE_RESULT_THRESHOLD);
    }

    private void appendListSizeMetadata(final Map<String, Object> payload, final int totalCount, final int returnedCount) {
        payload.put("total_count", totalCount);
        payload.put("returned_count", returnedCount);
        payload.put("truncated", returnedCount < totalCount);
    }

    private boolean isTruncated(final List<?> items, final List<?> returnedItems) {
        return returnedItems.size() < items.size();
    }

    private Map<String, Object> createDetailPayload(final MCPResourceDescriptor descriptor, final List<?> items, final Map<String, Object> navigationPayload) {
        Map<String, Object> result = new LinkedHashMap<>(navigationPayload.size() + 6, 1F);
        result.put("resource_kind", "detail");
        if (null != descriptor.getObjectScope()) {
            result.put("object_scope", descriptor.getObjectScope());
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

    private void appendEmptyStateGuidance(final Map<String, Object> payload, final MCPResourceDescriptor descriptor) {
        Map<String, Object> emptyState = new LinkedHashMap<>(3, 1F);
        String resourceKind = null == descriptor.getObjectScope() ? "metadata" : descriptor.getObjectScope();
        if (isDetailResource(descriptor)) {
            emptyState.put("state", "not_found");
            emptyState.put("reason", String.format("%s detail resource was not found for this URI.", resourceKind));
        } else {
            emptyState.put("state", "no_items");
            emptyState.put("reason", "No metadata items are available in this scope.");
        }
        emptyState.put("resource_kind", resourceKind);
        payload.put("empty_state", emptyState);
        String parentUri = getResourceHintUri(payload.get("parent_resource"));
        payload.put("next_actions", parentUri.isEmpty()
                ? List.of(MCPNextActionUtils.stop("No metadata items are available in this scope."))
                : List.of(MCPNextActionUtils.readResource(parentUri, "Read the parent metadata resource before broadening or correcting the request.")));
    }

    private void appendLargeResultGuidance(final Map<String, Object> payload, final MCPResourceDescriptor descriptor, final MCPUriVariables uriVariables, final int itemCount) {
        Map<String, Object> largeResult = new LinkedHashMap<>(4, 1F);
        largeResult.put("state", "broad_metadata_list");
        largeResult.put("count", itemCount);
        largeResult.put("threshold", LARGE_RESULT_THRESHOLD);
        largeResult.put("reason", "This metadata resource returned many items. Use search_metadata with an explicit query or scope before reading many detail resources.");
        payload.put("large_result_guidance", largeResult);
        payload.put("next_actions", List.of(MCPNextActionUtils.callTool("search_metadata",
                String.format("Narrow the broad %s metadata list before reading detail resources.", resolveGuidanceScope(descriptor)),
                createNarrowSearchArguments(descriptor, uriVariables), false)));
    }

    private Map<String, Object> createNarrowSearchArguments(final MCPResourceDescriptor descriptor, final MCPUriVariables uriVariables) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        Map<String, String> variables = null == uriVariables || null == uriVariables.getVariables() ? Map.of() : uriVariables.getVariables();
        putIfNotEmpty(result, "database", variables.get("database"));
        putIfNotEmpty(result, "schema", variables.get("schema"));
        result.put("object_types", List.of(resolveSearchObjectType(descriptor)));
        result.put("page_size", LARGE_RESULT_THRESHOLD);
        return result;
    }

    private void putIfNotEmpty(final Map<String, Object> target, final String key, final String value) {
        if (null != value && !value.isEmpty()) {
            target.put(key, value);
        }
    }

    private String resolveSearchObjectType(final MCPResourceDescriptor descriptor) {
        String objectScope = descriptor.getObjectScope();
        if ("logical-database".equals(objectScope)) {
            return "database";
        }
        if ("logical-table".equals(objectScope)) {
            return "table";
        }
        return null == objectScope ? "database" : objectScope;
    }

    private String resolveGuidanceScope(final MCPResourceDescriptor descriptor) {
        return null == descriptor.getObjectScope() ? "logical" : descriptor.getObjectScope();
    }

    private String getResourceHintUri(final Object value) {
        if (!(value instanceof Map)) {
            return "";
        }
        Object uri = ((Map<?, ?>) value).get("uri");
        return null == uri ? "" : uri.toString();
    }

    private Map<String, Object> createNavigationPayload(final MCPResourceDescriptor descriptor, final MCPUriVariables uriVariables) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        Map<String, String> variables = null == uriVariables || null == uriVariables.getVariables() ? Map.of() : uriVariables.getVariables();
        String selfUri = MCPUriTemplateUtils.expand(descriptor.getUriTemplate(), variables);
        if (!selfUri.isEmpty()) {
            result.put("self_uri", selfUri);
        }
        String parentUri = createParentUri(selfUri);
        if (!parentUri.isEmpty()) {
            result.put("parent_resource", MCPResourceHintUtils.create(parentUri, resolveResourceKind(parentUri), "inspect_parent",
                    "Read the parent metadata resource before broadening or correcting the request.", "parent_resource"));
        }
        List<Map<String, Object>> nextResources = MCPDescriptorRegistry.getResourceNavigationDescriptors().stream()
                .filter(each -> descriptor.getUriTemplate().equals(each.getFrom()))
                .filter(each -> each.getTo().startsWith("shardingsphere://"))
                .map(each -> createNextResourceHint(each.getTo(), each.getDescription(), variables)).filter(each -> !each.isEmpty()).toList();
        if (!nextResources.isEmpty()) {
            result.put("next_resources", nextResources);
        }
        return result;
    }

    private Map<String, Object> createNextResourceHint(final String uriTemplate, final String description, final Map<String, String> variables) {
        String uri = MCPUriTemplateUtils.expand(uriTemplate, variables);
        return uri.isEmpty() ? Map.of() : MCPResourceHintUtils.create(uri, resolveResourceKind(uri), "inspect_detail", description, "next_resources");
    }

    private String resolveResourceKind(final String uri) {
        if (uri.contains("/columns")) {
            return "column";
        }
        if (uri.contains("/indexes")) {
            return "index";
        }
        if (uri.contains("/tables")) {
            return "logical-table";
        }
        if (uri.contains("/views")) {
            return "view";
        }
        if (uri.contains("/sequences")) {
            return "sequence";
        }
        if (uri.contains("/schemas")) {
            return "schema";
        }
        return "logical-database";
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
