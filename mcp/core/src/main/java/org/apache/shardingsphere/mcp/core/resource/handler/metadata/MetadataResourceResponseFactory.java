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

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.ShardingSphereMCPResourceMetadata;
import org.apache.shardingsphere.mcp.support.diagnostic.MCPDiagnosticCategory;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPItemsPayload;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;
import org.apache.shardingsphere.mcp.support.resource.MCPUriTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Factory for metadata resource responses.
 */
public final class MetadataResourceResponseFactory {
    
    private static final int LARGE_RESULT_THRESHOLD = 100;
    
    /**
     * Create metadata resource response.
     *
     * @param requestContext database handler context
     * @param uriVariables URI variables
     * @param descriptor resource descriptor
     * @param metadata ShardingSphere resource metadata
     * @param items mapped metadata items
     * @return metadata resource response
     */
    public MCPSuccessPayload create(final MCPFeatureRequestContext requestContext, final MCPUriVariables uriVariables, final MCPResourceDescriptor descriptor,
                                    final ShardingSphereMCPResourceMetadata metadata, final List<?> items) {
        Map<String, Object> navigationPayload = createNavigationPayload(descriptor, uriVariables);
        if (isDetailResource(metadata)) {
            if (items.isEmpty()) {
                appendEmptyStateGuidance(navigationPayload, metadata, requestContext, uriVariables, descriptor.getUriTemplate());
            }
            return new MCPMapPayload(createDetailPayload(metadata, items, navigationPayload));
        }
        List<?> returnedItems = capListItems(items);
        appendListSizeMetadata(navigationPayload, items.size(), returnedItems.size());
        navigationPayload.put(MCPPayloadFieldNames.SUMMARY, createListSummary(metadata, items.size(), returnedItems.size()));
        if (items.isEmpty()) {
            appendEmptyStateGuidance(navigationPayload, metadata, requestContext, uriVariables, descriptor.getUriTemplate());
        } else if (isTruncated(items, returnedItems)) {
            appendLargeResultGuidance(navigationPayload, metadata, uriVariables, items.size());
        }
        return new MCPItemsPayload(returnedItems, navigationPayload);
    }
    
    private boolean isDetailResource(final ShardingSphereMCPResourceMetadata metadata) {
        return "detail".equals(metadata.getResourceKind());
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
    
    private Map<String, Object> createDetailPayload(final ShardingSphereMCPResourceMetadata metadata, final List<?> items, final Map<String, Object> navigationPayload) {
        Map<String, Object> result = new LinkedHashMap<>(navigationPayload.size() + 7, 1F);
        result.put("response_mode", MCPResponseMode.DETAIL);
        result.put(MCPPayloadFieldNames.SUMMARY, createDetailSummary(metadata, items));
        result.put(MCPPayloadFieldNames.RESOURCE_KIND, "detail");
        if (null != metadata.getObjectScope()) {
            result.put("object_scope", metadata.getObjectScope());
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
    
    private String createListSummary(final ShardingSphereMCPResourceMetadata metadata, final int totalCount, final int returnedCount) {
        return String.format("Returned %d of %d %s metadata entries.", returnedCount, totalCount, resolveSummaryScope(metadata));
    }
    
    private String createDetailSummary(final ShardingSphereMCPResourceMetadata metadata, final List<?> items) {
        return items.isEmpty()
                ? String.format("No %s detail item matched this resource URI.", resolveSummaryScope(metadata))
                : String.format("Returned %s detail for this resource URI.", resolveSummaryScope(metadata));
    }
    
    private String resolveSummaryScope(final ShardingSphereMCPResourceMetadata metadata) {
        return null == metadata.getObjectScope() ? metadata.getResourceKind() : metadata.getObjectScope().replace('_', '-');
    }
    
    private void appendEmptyStateGuidance(final Map<String, Object> payload, final ShardingSphereMCPResourceMetadata metadata,
                                          final MCPFeatureRequestContext requestContext, final MCPUriVariables uriVariables, final String uriTemplate) {
        Map<String, Object> emptyState = new LinkedHashMap<>(4, 1F);
        String resourceKind = null == metadata.getObjectScope() ? "metadata" : metadata.getObjectScope();
        String recoveryCategory = resolveEmptyStateCategory(metadata, requestContext, uriVariables, uriTemplate);
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
    
    private String resolveEmptyStateCategory(final ShardingSphereMCPResourceMetadata metadata, final MCPFeatureRequestContext requestContext,
                                             final MCPUriVariables uriVariables, final String uriTemplate) {
        if ("shardingsphere://databases".equals(uriTemplate)) {
            return MCPDiagnosticCategory.NO_RUNTIME_DATABASE;
        }
        if (uriVariables.containsVariable("database") && !isKnownDatabase(requestContext, uriVariables.getValue("database"))) {
            return MCPDiagnosticCategory.UNKNOWN_DATABASE;
        }
        if (isDetailResource(metadata)) {
            return resolveDetailEmptyStateCategory(metadata, uriVariables);
        }
        return MCPDiagnosticCategory.EMPTY_SCOPE;
    }
    
    private String resolveDetailEmptyStateCategory(final ShardingSphereMCPResourceMetadata metadata, final MCPUriVariables uriVariables) {
        if ("schema".equals(metadata.getObjectScope()) && uriVariables.containsVariable("schema")) {
            return MCPDiagnosticCategory.SCHEMA_NOT_VISIBLE;
        }
        if (containsObjectToken(uriVariables)) {
            return MCPDiagnosticCategory.OBJECT_NOT_VISIBLE;
        }
        if ("logical-database".equals(metadata.getObjectScope()) && uriVariables.containsVariable("database")) {
            return MCPDiagnosticCategory.DATABASE_NOT_VISIBLE;
        }
        return MCPDiagnosticCategory.NOT_FOUND;
    }
    
    private boolean containsObjectToken(final MCPUriVariables uriVariables) {
        return Stream.of("column", "index", "sequence", "view", "storageUnit", "table").anyMatch(uriVariables::containsVariable);
    }
    
    private boolean isKnownDatabase(final MCPFeatureRequestContext requestContext, final String databaseName) {
        return Optional.ofNullable(requestContext.getCapabilityFacade()).flatMap(capabilityFacade -> capabilityFacade.findDatabaseProfile(databaseName)).isPresent();
    }
    
    private String createEmptyStateReason(final String category, final String resourceKind) {
        if (MCPDiagnosticCategory.NO_RUNTIME_DATABASE.equals(category)) {
            return "No ShardingSphere-Proxy logical database is available to MCP. Configure runtimeDatabases before reading metadata.";
        }
        if (MCPDiagnosticCategory.UNKNOWN_DATABASE.equals(category)) {
            return "The requested logical database is not visible to MCP. Check runtimeDatabases and ShardingSphere-Proxy connectivity.";
        }
        if (MCPDiagnosticCategory.DATABASE_NOT_VISIBLE.equals(category)) {
            return "The requested logical database is configured but not visible through the current runtime metadata scope.";
        }
        if (MCPDiagnosticCategory.SCHEMA_NOT_VISIBLE.equals(category)) {
            return "The requested schema is not visible in the current metadata scope.";
        }
        if (MCPDiagnosticCategory.OBJECT_NOT_VISIBLE.equals(category)) {
            return "The requested metadata object is not visible in the current metadata scope.";
        }
        return MCPDiagnosticCategory.NOT_FOUND.equals(category)
                ? String.format("%s detail resource was not found for this URI.", resourceKind)
                : "No metadata items are visible in this scope. Check metadata permissions if objects are expected.";
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
    
    private void appendLargeResultGuidance(final Map<String, Object> payload, final ShardingSphereMCPResourceMetadata metadata,
                                           final MCPUriVariables uriVariables, final int itemCount) {
        Map<String, Object> largeResult = new LinkedHashMap<>(4, 1F);
        largeResult.put("state", "broad_metadata_list");
        largeResult.put("count", itemCount);
        largeResult.put("threshold", LARGE_RESULT_THRESHOLD);
        largeResult.put(MCPPayloadFieldNames.REASON,
                "This metadata resource returned many items. Use database_gateway_search_metadata with an explicit query or scope before reading many detail resources.");
        largeResult.put("search_arguments", createNarrowSearchArguments(metadata, uriVariables));
        payload.put("continuation_mode", "metadata_search");
        payload.put("large_result_guidance", largeResult);
        payload.put(MCPPayloadFieldNames.NEXT_ACTIONS, List.of(MCPNextActionUtils.callTool("database_gateway_search_metadata",
                String.format("Narrow the broad %s metadata list before reading detail resources.", resolveGuidanceScope(metadata)),
                createNarrowSearchArguments(metadata, uriVariables))));
    }
    
    private Map<String, Object> createNarrowSearchArguments(final ShardingSphereMCPResourceMetadata metadata, final MCPUriVariables uriVariables) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        if (uriVariables.containsVariable("database")) {
            result.put("database", uriVariables.getValue("database"));
        }
        if (uriVariables.containsVariable("schema")) {
            result.put("schema", uriVariables.getValue("schema"));
        }
        result.put("object_types", Collections.singletonList(resolveSearchObjectType(metadata)));
        return result;
    }
    
    private String resolveSearchObjectType(final ShardingSphereMCPResourceMetadata metadata) {
        String objectScope = metadata.getObjectScope();
        if ("logical-database".equals(objectScope)) {
            return "database";
        }
        if ("logical-table".equals(objectScope)) {
            return "table";
        }
        return null == objectScope ? "database" : objectScope;
    }
    
    private String resolveGuidanceScope(final ShardingSphereMCPResourceMetadata metadata) {
        return null == metadata.getObjectScope() ? "logical" : metadata.getObjectScope();
    }
    
    private String getResourceHintUri(final Object value) {
        if (!(value instanceof Map)) {
            return "";
        }
        Object uri = ((Map<?, ?>) value).get(MCPPayloadFieldNames.URI);
        return null == uri ? "" : uri.toString();
    }
    
    private Map<String, Object> createNavigationPayload(final MCPResourceDescriptor descriptor, final MCPUriVariables uriVariables) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        String uriTemplate = descriptor.getUriTemplate();
        Optional<String> selfUri = new MCPUriTemplate(uriTemplate).expandIfComplete(uriVariables);
        selfUri.ifPresent(uri -> {
            result.put("self_uri", uri);
            result.put(MCPPayloadFieldNames.SELF_RESOURCE,
                    MCPResourceHintUtils.create(uri, resolveResourceKind(uri), "inspect_self", "Read this metadata resource.", MCPPayloadFieldNames.SELF_RESOURCE));
        });
        String parentUri = createParentUri(selfUri.orElse(""));
        if (!parentUri.isEmpty()) {
            result.put(MCPPayloadFieldNames.PARENT_RESOURCE, MCPResourceHintUtils.create(parentUri, resolveResourceKind(parentUri), "inspect_parent",
                    "Read the parent metadata resource before broadening or correcting the request.", MCPPayloadFieldNames.PARENT_RESOURCE));
        }
        List<Map<String, Object>> nextResources = MCPDescriptorCatalogIndex.getResourceNavigationDescriptors(uriTemplate).stream()
                .filter(each -> each.getTo().startsWith("shardingsphere://"))
                .map(each -> createNextResourceHint(each.getTo(), each.getDescription(), uriVariables)).flatMap(Optional::stream).toList();
        if (!nextResources.isEmpty()) {
            result.put(MCPPayloadFieldNames.NEXT_RESOURCES, nextResources);
        }
        return result;
    }
    
    private Optional<Map<String, Object>> createNextResourceHint(final String uriTemplate, final String description, final MCPUriVariables variables) {
        return new MCPUriTemplate(uriTemplate).expandIfComplete(variables)
                .map(uri -> MCPResourceHintUtils.create(uri, resolveResourceKind(uri), "inspect_detail", description, MCPPayloadFieldNames.NEXT_RESOURCES));
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
