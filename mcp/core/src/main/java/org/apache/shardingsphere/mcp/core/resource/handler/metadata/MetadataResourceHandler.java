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
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.ShardingSphereMCPResourceMetadata;
import org.apache.shardingsphere.mcp.support.diagnostic.MCPDiagnosticCategory;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPItemsResponse;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.support.resource.MCPUriTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

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
    public String getResourceUriTemplate() {
        return uriTemplate;
    }
    
    @Override
    public MCPResponse handle(final MCPDatabaseHandlerContext databaseContext, final MCPUriVariables uriVariables) {
        List<?> items = metadataLoader.apply(databaseContext, uriVariables);
        MCPResourceDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredResourceDescriptor(uriTemplate);
        ShardingSphereMCPResourceMetadata metadata = MCPDescriptorCatalogIndex.getRequiredShardingSphereResourceMetadata(descriptor.getUriTemplate());
        Map<String, Object> navigationPayload = createNavigationPayload(descriptor, uriVariables);
        if (isDetailResource(metadata)) {
            if (items.isEmpty()) {
                appendEmptyStateGuidance(navigationPayload, metadata, databaseContext, uriVariables);
            }
            return new MCPMapResponse(createDetailPayload(metadata, items, navigationPayload));
        }
        List<?> returnedItems = capListItems(items);
        appendListSizeMetadata(navigationPayload, items.size(), returnedItems.size());
        if (items.isEmpty()) {
            appendEmptyStateGuidance(navigationPayload, metadata, databaseContext, uriVariables);
        } else if (isTruncated(items, returnedItems)) {
            appendLargeResultGuidance(navigationPayload, metadata, uriVariables, items.size());
        }
        return new MCPItemsResponse(returnedItems, navigationPayload);
    }
    
    private boolean isDetailResource(final ShardingSphereMCPResourceMetadata descriptor) {
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
    
    private Map<String, Object> createDetailPayload(final ShardingSphereMCPResourceMetadata descriptor, final List<?> items, final Map<String, Object> navigationPayload) {
        Map<String, Object> result = new LinkedHashMap<>(navigationPayload.size() + 6, 1F);
        result.put("response_mode", MCPResponseMode.DETAIL);
        result.put(MCPPayloadFieldNames.RESOURCE_KIND, "detail");
        if (null != descriptor.getObjectScope()) {
            result.put("object_scope", descriptor.getObjectScope());
        }
        result.put("found", !items.isEmpty());
        result.put(MCPPayloadFieldNames.ITEMS, items);
        result.put("count", items.size());
        if (!items.isEmpty()) {
            result.put("item", items.getFirst());
        }
        result.putAll(navigationPayload);
        return result;
    }
    
    private void appendEmptyStateGuidance(final Map<String, Object> payload, final ShardingSphereMCPResourceMetadata descriptor,
                                          final MCPDatabaseHandlerContext databaseContext, final MCPUriVariables uriVariables) {
        Map<String, Object> emptyState = new LinkedHashMap<>(4, 1F);
        String resourceKind = null == descriptor.getObjectScope() ? "metadata" : descriptor.getObjectScope();
        String recoveryCategory = resolveEmptyStateCategory(descriptor, databaseContext, uriVariables);
        if (MCPDiagnosticCategory.NOT_FOUND.equals(recoveryCategory)) {
            emptyState.put("state", "not_found");
            emptyState.put("category", recoveryCategory);
        } else {
            emptyState.put("state", "no_items");
            emptyState.put("category", recoveryCategory);
        }
        String reason = createEmptyStateReason(recoveryCategory, resourceKind);
        emptyState.put(MCPPayloadFieldNames.REASON, reason);
        emptyState.put(MCPPayloadFieldNames.RESOURCE_KIND, resourceKind);
        payload.put("empty_state", emptyState);
        String parentUri = getResourceHintUri(payload.get(MCPPayloadFieldNames.PARENT_RESOURCE));
        payload.put(MCPPayloadFieldNames.RECOVERY, createRecovery(recoveryCategory, resourceKind, parentUri, uriVariables));
        payload.put(MCPPayloadFieldNames.NEXT_ACTIONS, parentUri.isEmpty()
                ? List.of(MCPNextActionUtils.stop(reason))
                : List.of(MCPNextActionUtils.readResource(parentUri, "Read the parent metadata resource before broadening or correcting the request.")));
    }
    
    private String resolveEmptyStateCategory(final ShardingSphereMCPResourceMetadata descriptor, final MCPDatabaseHandlerContext databaseContext, final MCPUriVariables uriVariables) {
        if ("shardingsphere://databases".equals(uriTemplate)) {
            return MCPDiagnosticCategory.NO_RUNTIME_DATABASE;
        }
        if (uriVariables.containsVariable("database") && !isKnownDatabase(databaseContext, uriVariables.getValue("database"))) {
            return MCPDiagnosticCategory.UNKNOWN_DATABASE;
        }
        if (isDetailResource(descriptor)) {
            return resolveDetailEmptyStateCategory(descriptor, uriVariables);
        }
        return MCPDiagnosticCategory.EMPTY_SCOPE;
    }
    
    private String resolveDetailEmptyStateCategory(final ShardingSphereMCPResourceMetadata descriptor, final MCPUriVariables uriVariables) {
        if ("schema".equals(descriptor.getObjectScope()) && uriVariables.containsVariable("schema")) {
            return MCPDiagnosticCategory.SCHEMA_NOT_VISIBLE;
        }
        if (containsObjectToken(uriVariables)) {
            return MCPDiagnosticCategory.OBJECT_NOT_VISIBLE;
        }
        if ("logical-database".equals(descriptor.getObjectScope()) && uriVariables.containsVariable("database")) {
            return MCPDiagnosticCategory.DATABASE_NOT_VISIBLE;
        }
        return MCPDiagnosticCategory.NOT_FOUND;
    }
    
    private boolean containsObjectToken(final MCPUriVariables uriVariables) {
        return Stream.of("column", "index", "sequence", "view", "storageUnit", "table").anyMatch(uriVariables::containsVariable);
    }
    
    private boolean isKnownDatabase(final MCPDatabaseHandlerContext databaseContext, final String databaseName) {
        return Optional.ofNullable(databaseContext.getCapabilityFacade()).flatMap(capabilityFacade -> capabilityFacade.findDatabaseProfile(databaseName)).isPresent();
    }
    
    private String createEmptyStateReason(final String category, final String resourceKind) {
        switch (category) {
            case MCPDiagnosticCategory.NO_RUNTIME_DATABASE:
                return "No ShardingSphere-Proxy logical database is available to MCP. Configure runtimeDatabases before reading metadata.";
            case MCPDiagnosticCategory.UNKNOWN_DATABASE:
                return "The requested logical database is not visible to MCP. Check runtimeDatabases and ShardingSphere-Proxy connectivity.";
            case MCPDiagnosticCategory.DATABASE_NOT_VISIBLE:
                return "The requested logical database is configured but not visible through the current runtime metadata scope.";
            case MCPDiagnosticCategory.SCHEMA_NOT_VISIBLE:
                return "The requested schema is not visible in the current metadata scope.";
            case MCPDiagnosticCategory.OBJECT_NOT_VISIBLE:
                return "The requested metadata object is not visible in the current metadata scope.";
            case MCPDiagnosticCategory.NOT_FOUND:
                return String.format("%s detail resource was not found for this URI.", resourceKind);
            default:
                return "No metadata items are visible in this scope. Check metadata permissions if objects are expected.";
        }
    }
    
    private Map<String, Object> createRecovery(final String category, final String resourceKind, final String parentUri, final MCPUriVariables uriVariables) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("response_mode", MCPResponseMode.RECOVERY);
        result.put("recovery_category", category);
        result.put("category", category);
        result.put(MCPPayloadFieldNames.RESOURCE_KIND, resourceKind);
        if (!parentUri.isEmpty()) {
            result.put("parent_resource_uri", parentUri);
        }
        String requestedToken = createRequestedToken(uriVariables);
        if (!requestedToken.isEmpty()) {
            result.put("requested_token", requestedToken);
            result.put("retry_arguments", Map.of("query", requestedToken));
        }
        return result;
    }
    
    private String createRequestedToken(final MCPUriVariables uriVariables) {
        return Stream.of("column", "index", "sequence", "view", "storageUnit", "table", "schema", "database")
                .filter(uriVariables::containsVariable).findFirst().map(uriVariables::getValue).orElse("");
    }
    
    private void appendLargeResultGuidance(final Map<String, Object> payload, final ShardingSphereMCPResourceMetadata descriptor, final MCPUriVariables uriVariables, final int itemCount) {
        Map<String, Object> largeResult = new LinkedHashMap<>(4, 1F);
        largeResult.put("state", "broad_metadata_list");
        largeResult.put("count", itemCount);
        largeResult.put("threshold", LARGE_RESULT_THRESHOLD);
        largeResult.put(MCPPayloadFieldNames.REASON,
                "This metadata resource returned many items. Use database_gateway_search_metadata with an explicit query or scope before reading many detail resources.");
        largeResult.put("search_arguments", createNarrowSearchArguments(descriptor, uriVariables));
        payload.put("continuation_mode", "metadata_search");
        payload.put("large_result_guidance", largeResult);
        payload.put(MCPPayloadFieldNames.NEXT_ACTIONS, List.of(MCPNextActionUtils.callTool("database_gateway_search_metadata",
                String.format("Narrow the broad %s metadata list before reading detail resources.", resolveGuidanceScope(descriptor)),
                createNarrowSearchArguments(descriptor, uriVariables))));
    }
    
    private Map<String, Object> createNarrowSearchArguments(final ShardingSphereMCPResourceMetadata descriptor, final MCPUriVariables uriVariables) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        if (uriVariables.containsVariable("database")) {
            result.put("database", uriVariables.getValue("database"));
        }
        if (uriVariables.containsVariable("schema")) {
            result.put("schema", uriVariables.getValue("schema"));
        }
        result.put("object_types", Collections.singletonList(resolveSearchObjectType(descriptor)));
        return result;
    }
    
    private String resolveSearchObjectType(final ShardingSphereMCPResourceMetadata descriptor) {
        String objectScope = descriptor.getObjectScope();
        if ("logical-database".equals(objectScope)) {
            return "database";
        }
        if ("logical-table".equals(objectScope)) {
            return "table";
        }
        return null == objectScope ? "database" : objectScope;
    }
    
    private String resolveGuidanceScope(final ShardingSphereMCPResourceMetadata descriptor) {
        return null == descriptor.getObjectScope() ? "logical" : descriptor.getObjectScope();
    }
    
    private String getResourceHintUri(final Object value) {
        if (!(value instanceof Map)) {
            return "";
        }
        Object uri = ((Map<?, ?>) value).get(MCPPayloadFieldNames.URI);
        return null == uri ? "" : uri.toString();
    }
    
    private Map<String, Object> createNavigationPayload(final MCPResourceDescriptor descriptor, final MCPUriVariables uriVariables) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        String uriOrTemplate = descriptor.getUriTemplate();
        Optional<String> selfUri = new MCPUriTemplate(uriOrTemplate).expandIfComplete(uriVariables);
        selfUri.ifPresent(optional -> result.put("self_uri", optional));
        String parentUri = createParentUri(selfUri.orElse(""));
        if (!parentUri.isEmpty()) {
            result.put(MCPPayloadFieldNames.PARENT_RESOURCE, MCPResourceHintUtils.create(parentUri, resolveResourceKind(parentUri), "inspect_parent",
                    "Read the parent metadata resource before broadening or correcting the request.", MCPPayloadFieldNames.PARENT_RESOURCE));
        }
        List<Map<String, Object>> nextResources = MCPDescriptorCatalogIndex.getResourceNavigationDescriptors(uriOrTemplate).stream()
                .filter(each -> each.getTo().startsWith("shardingsphere://"))
                .map(each -> createNextResourceHint(each.getTo(), each.getDescription(), uriVariables)).flatMap(Optional::stream).toList();
        if (!nextResources.isEmpty()) {
            result.put(MCPPayloadFieldNames.NEXT_RESOURCES, nextResources);
        }
        return result;
    }
    
    private Optional<Map<String, Object>> createNextResourceHint(final String uriTemplate, final String description, final MCPUriVariables variables) {
        return new MCPUriTemplate(uriTemplate).expandIfComplete(variables)
                .map(optional -> MCPResourceHintUtils.create(optional, resolveResourceKind(optional), "inspect_detail", description, MCPPayloadFieldNames.NEXT_RESOURCES));
    }
    
    private String resolveResourceKind(final String uri) {
        if (uri.contains("/columns")) {
            return "column";
        }
        if (uri.contains("/indexes")) {
            return "index";
        }
        if (uri.contains("/storage-units")) {
            return "storage-unit";
        }
        if (uri.contains("/single-tables") || uri.contains("/single-table/default-storage-unit")) {
            return "single-table";
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
        if (selfUri.isEmpty()) {
            return "";
        }
        String prefix = "shardingsphere://";
        if (!selfUri.startsWith(prefix)) {
            return "";
        }
        String path = selfUri.substring(prefix.length());
        int lastSeparatorIndex = path.lastIndexOf('/');
        return 0 > lastSeparatorIndex ? "" : prefix + path.substring(0, lastSeparatorIndex);
    }
}
