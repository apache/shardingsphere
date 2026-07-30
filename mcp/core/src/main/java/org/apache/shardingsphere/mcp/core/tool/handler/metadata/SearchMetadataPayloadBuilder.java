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

package org.apache.shardingsphere.mcp.core.tool.handler.metadata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.core.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.core.tool.payload.MetadataSearchHit;
import org.apache.shardingsphere.mcp.core.tool.payload.MetadataSearchResult;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.diagnostic.MCPDiagnosticCategory;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Builder for search metadata model-facing payload metadata.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SearchMetadataPayloadBuilder {
    
    /**
     * Build search metadata payload metadata.
     *
     * @param requestContext database handler context
     * @param request metadata search request
     * @param searchResult metadata search result
     * @param toolName search metadata tool name
     * @return search metadata payload metadata
     */
    public static Map<String, Object> build(final MCPFeatureRequestContext requestContext, final MetadataSearchRequest request,
                                            final MetadataSearchResult searchResult, final String toolName) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put(MCPPayloadFieldNames.SUMMARY, createSummary(searchResult));
        result.put("search_context", searchResult.getSearchContext());
        result.put("total_match_count", searchResult.getTotalMatchCount());
        result.put("truncated", searchResult.isTruncated());
        result.put("has_more", searchResult.isTruncated());
        result.put("continuation_mode", searchResult.isTruncated() ? "pagination" : "none");
        if (searchResult.isTruncated()) {
            result.put("next_offset", request.getOffset() + searchResult.getReturnedCount());
        }
        if (searchResult.isTruncated() && searchResult.getTotalMatchCount() > searchResult.getLargeResultThreshold()) {
            result.put("large_result_guidance", createLargeResultGuidance(searchResult));
        }
        if (0 == searchResult.getTotalMatchCount()) {
            result.put("empty_state", createEmptyState(requestContext, request));
            result.put(MCPPayloadFieldNames.NEXT_ACTIONS, List.of(createEmptySearchNextAction(request, toolName)));
            return result;
        }
        List<String> duplicatedNames = findDuplicatedNames(searchResult.getItems(), request.getQuery());
        List<Map<String, Object>> nextActions = createResultNextActions(request, searchResult, duplicatedNames, toolName);
        if (!nextActions.isEmpty()) {
            result.put(MCPPayloadFieldNames.NEXT_ACTIONS, nextActions);
        }
        if (!duplicatedNames.isEmpty()) {
            result.put("ambiguity_state", createAmbiguityState(searchResult.getItems(), duplicatedNames));
        }
        return result;
    }
    
    private static String createSummary(final MetadataSearchResult searchResult) {
        return String.format("Metadata search returned %d of %d matches.", searchResult.getReturnedCount(), searchResult.getTotalMatchCount());
    }
    
    private static Map<String, Object> createLargeResultGuidance(final MetadataSearchResult searchResult) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("state", "metadata_search_result_truncated");
        result.put("threshold", searchResult.getLargeResultThreshold());
        result.put("narrowing_arguments", List.of("database", "schema", "query", "object_types"));
        result.put(MCPPayloadFieldNames.REASON, "Search matched more metadata objects than the maximum page size; continue pagination or narrow the search before reading specific resources.");
        return result;
    }
    
    private static List<Map<String, Object>> createResultNextActions(final MetadataSearchRequest request, final MetadataSearchResult searchResult,
                                                                     final List<String> duplicatedNames, final String toolName) {
        List<Map<String, Object>> result = new LinkedList<>();
        if (isBroadSearchGuarded(searchResult)) {
            result.add(MCPNextActionUtils.askUser("Blank cross-database metadata search listed databases only. Choose a database, query, or object type before searching deeper metadata.",
                    List.of("database", "query", "object_types")));
        }
        if (searchResult.isTruncated()) {
            result.add(MCPNextActionUtils.callTool(toolName, "Read the next deterministically ordered metadata search page.", createNextPageArguments(request, searchResult)));
        }
        if (!duplicatedNames.isEmpty()) {
            result.add(MCPNextActionUtils.askUser("Multiple metadata hits share the same name. Ask the user to choose database, schema, or object type before using a specific resource.",
                    List.of("database", "schema", "object_types")));
        }
        return MCPNextActionUtils.ordered(result);
    }
    
    private static Map<String, Object> createNextPageArguments(final MetadataSearchRequest request, final MetadataSearchResult searchResult) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        putIfNotEmpty(result, "database", request.getDatabase());
        putIfNotEmpty(result, "schema", request.getSchema());
        putIfNotEmpty(result, "query", request.getQuery());
        if (!request.getObjectTypes().isEmpty()) {
            result.put("object_types", request.getObjectTypes().stream().map(each -> each.name().toLowerCase(Locale.ENGLISH)).sorted().toList());
        }
        result.put("limit", request.getLimit());
        result.put("offset", request.getOffset() + searchResult.getReturnedCount());
        return result;
    }
    
    private static void putIfNotEmpty(final Map<String, Object> target, final String key, final String value) {
        if (!value.isEmpty()) {
            target.put(key, value);
        }
    }
    
    private static boolean isBroadSearchGuarded(final MetadataSearchResult searchResult) {
        return Boolean.TRUE.equals(searchResult.getSearchContext().get("broad_search_guarded"));
    }
    
    private static List<String> findDuplicatedNames(final List<MetadataSearchHit> items, final String query) {
        Set<String> observedNames = new LinkedHashSet<>();
        Set<String> duplicatedNames = new LinkedHashSet<>();
        for (MetadataSearchHit each : items) {
            if (!isAmbiguityCandidate(each, query)) {
                continue;
            }
            if (!observedNames.add(each.getName())) {
                duplicatedNames.add(each.getName());
            }
        }
        return new LinkedList<>(duplicatedNames);
    }
    
    private static boolean isAmbiguityCandidate(final MetadataSearchHit searchHit, final String query) {
        return query.isEmpty() || searchHit.getMatchedFields().contains("name");
    }
    
    private static Map<String, Object> createAmbiguityState(final List<MetadataSearchHit> items, final List<String> duplicatedNames) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("state", "duplicate_names");
        result.put("ambiguous", true);
        result.put("ambiguous_by", createAmbiguousDimensions(items, duplicatedNames));
        result.put("candidate_count", countDuplicatedCandidates(items, duplicatedNames));
        result.put("duplicated_names", duplicatedNames);
        result.put("narrowing_arguments", List.of("database", "schema", "object_types"));
        result.put(MCPPayloadFieldNames.REASON, "Multiple metadata hits share the same name; choose an explicit database, schema, or object type before reading a specific resource.");
        return result;
    }
    
    private static List<String> createAmbiguousDimensions(final List<MetadataSearchHit> items, final List<String> duplicatedNames) {
        Set<String> databases = new LinkedHashSet<>();
        Set<String> schemas = new LinkedHashSet<>();
        Set<String> objectTypes = new LinkedHashSet<>();
        for (MetadataSearchHit each : items) {
            if (duplicatedNames.contains(each.getName())) {
                addIfNotEmpty(databases, each.getDatabase());
                addIfNotEmpty(schemas, each.getSchema());
                addIfNotEmpty(objectTypes, each.getObjectType());
            }
        }
        List<String> result = new LinkedList<>();
        result.add("name");
        addDimensionIfAmbiguous(result, "database", databases);
        addDimensionIfAmbiguous(result, "schema", schemas);
        addDimensionIfAmbiguous(result, "object_type", objectTypes);
        return result;
    }
    
    private static void addIfNotEmpty(final Set<String> values, final String value) {
        if (null != value && !value.isEmpty()) {
            values.add(value);
        }
    }
    
    private static void addDimensionIfAmbiguous(final List<String> dimensions, final String dimension, final Set<String> values) {
        if (1 < values.size()) {
            dimensions.add(dimension);
        }
    }
    
    private static int countDuplicatedCandidates(final List<MetadataSearchHit> items, final List<String> duplicatedNames) {
        int result = 0;
        for (MetadataSearchHit each : items) {
            if (duplicatedNames.contains(each.getName())) {
                result++;
            }
        }
        return result;
    }
    
    private static Map<String, Object> createEmptyState(final MCPFeatureRequestContext requestContext, final MetadataSearchRequest request) {
        String category = resolveEmptyStateCategory(requestContext, request);
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("state", request.getQuery().isEmpty() ? "no_items" : "no_match");
        result.put("category", category);
        result.put(MCPPayloadFieldNames.REASON, createEmptyStateReason(category, request));
        return result;
    }
    
    private static String resolveEmptyStateCategory(final MCPFeatureRequestContext requestContext, final MetadataSearchRequest request) {
        if (!hasRuntimeDatabase(requestContext)) {
            return MCPDiagnosticCategory.NO_RUNTIME_DATABASE;
        }
        if (!request.getDatabase().isEmpty() && !isKnownDatabase(requestContext, request.getDatabase())) {
            return MCPDiagnosticCategory.UNKNOWN_DATABASE;
        }
        if (!request.getSchema().isEmpty() && !isKnownSchema(requestContext, request.getDatabase(), request.getSchema())) {
            return MCPDiagnosticCategory.SCHEMA_NOT_VISIBLE;
        }
        return request.getQuery().isEmpty() ? MCPDiagnosticCategory.EMPTY_SCOPE : MCPDiagnosticCategory.OBJECT_NOT_VISIBLE;
    }
    
    private static boolean isKnownDatabase(final MCPFeatureRequestContext requestContext, final String databaseName) {
        return Optional.ofNullable(requestContext.getCapabilityFacade()).flatMap(capabilityFacade -> capabilityFacade.findDatabaseProfile(databaseName)).isPresent();
    }
    
    private static boolean isKnownSchema(final MCPFeatureRequestContext requestContext, final String databaseName, final String schemaName) {
        return requestContext.getMetadataQueryFacade().querySchema(databaseName, schemaName).isPresent();
    }
    
    private static boolean hasRuntimeDatabase(final MCPFeatureRequestContext requestContext) {
        return !requestContext.getMetadataQueryFacade().queryDatabases().isEmpty();
    }
    
    private static String createEmptyStateReason(final String category, final MetadataSearchRequest request) {
        switch (category) {
            case MCPDiagnosticCategory.NO_RUNTIME_DATABASE:
                return "No runtime database metadata is visible to MCP.";
            case MCPDiagnosticCategory.UNKNOWN_DATABASE:
                return "The requested logical database is not visible to MCP.";
            case MCPDiagnosticCategory.SCHEMA_NOT_VISIBLE:
                return "The requested schema is not visible in the current metadata scope.";
            case MCPDiagnosticCategory.OBJECT_NOT_VISIBLE:
                return "No visible metadata object matched the query in the requested scope.";
            default:
                return request.getQuery().isEmpty() ? "No metadata is available in the requested scope." : "No metadata matched the query in the requested scope.";
        }
    }
    
    private static Map<String, Object> createEmptySearchNextAction(final MetadataSearchRequest request, final String toolName) {
        if (!request.getQuery().isEmpty() || !request.getSchema().isEmpty()) {
            return MCPNextActionUtils.callTool(toolName, "Retry database_gateway_search_metadata with a broader scope.", createBroadenedSearchArguments(request));
        }
        return MCPNextActionUtils.readResource("shardingsphere://databases", "Read configured databases before choosing a narrower metadata search.");
    }
    
    private static Map<String, Object> createBroadenedSearchArguments(final MetadataSearchRequest request) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        if (!request.getQuery().isEmpty()) {
            result.put("query", request.getQuery());
        } else if (!request.getDatabase().isEmpty()) {
            result.put("database", request.getDatabase());
        }
        return result;
    }
}
