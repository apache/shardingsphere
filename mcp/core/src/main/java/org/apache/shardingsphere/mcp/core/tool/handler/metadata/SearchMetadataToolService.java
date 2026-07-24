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

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.core.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.core.tool.payload.MetadataSearchHit;
import org.apache.shardingsphere.mcp.core.tool.payload.MetadataSearchResult;
import org.apache.shardingsphere.mcp.core.metadata.GovernanceMetadataQueryService;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Search metadata tool service.
 */
public final class SearchMetadataToolService {
    
    private static final int LARGE_RESULT_THRESHOLD = 100;
    
    private static final Map<String, Integer> OBJECT_TYPE_ORDERS = Map.of(
            "database", 0, "schema", 1, "storage_unit", 2, "table", 3, "view", 4, "column", 5, "index", 6, "sequence", 7);
    
    private final MetadataSearchCollector collector;
    
    private final MetadataSearchMatcher matcher = new MetadataSearchMatcher();
    
    public SearchMetadataToolService(final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade) {
        collector = new MetadataSearchCollector(metadataQueryFacade, queryFacade, new GovernanceMetadataQueryService(), new MetadataSearchResourceUriFactory());
    }
    
    /**
     * Search metadata.
     *
     * @param request search request
     * @return search result
     */
    public MetadataSearchResult execute(final MetadataSearchRequest request) {
        ShardingSpherePreconditions.checkState(request.getSchema().isEmpty() || !request.getDatabase().isEmpty(), () -> new MCPInvalidRequestException("Schema cannot be provided without database."));
        Set<SupportedMCPMetadataObjectType> searchObjectTypes = getSearchObjectTypes(request.getObjectTypes());
        if (isBlankAllDatabaseSearch(request)) {
            return executeBlankAllDatabaseSearch(request);
        }
        return createSearchResult(collector.collect(request, searchObjectTypes), request, searchObjectTypes, false);
    }
    
    private boolean isBlankAllDatabaseSearch(final MetadataSearchRequest request) {
        return request.getDatabase().isEmpty() && request.getQuery().isEmpty() && request.getObjectTypes().isEmpty();
    }
    
    private MetadataSearchResult executeBlankAllDatabaseSearch(final MetadataSearchRequest request) {
        Set<SupportedMCPMetadataObjectType> searchObjectTypes = Set.of(SupportedMCPMetadataObjectType.DATABASE);
        return createSearchResult(collector.collectDatabases(), request, searchObjectTypes, true);
    }
    
    private MetadataSearchResult createSearchResult(final List<MetadataSearchHit> metadataItems, final MetadataSearchRequest request,
                                                    final Set<SupportedMCPMetadataObjectType> searchObjectTypes, final boolean broadSearchGuarded) {
        List<MetadataSearchHit> filteredItems = matcher.filterByQuery(metadataItems, request.getQuery());
        filteredItems.sort(this::compareSearchHits);
        List<MetadataSearchHit> returnedItems = pageSearchResult(filteredItems, request);
        return new MetadataSearchResult(returnedItems, createSearchContext(request, searchObjectTypes, broadSearchGuarded), filteredItems.size(), returnedItems.size(),
                request.getOffset() + returnedItems.size() < filteredItems.size(), LARGE_RESULT_THRESHOLD);
    }
    
    private List<MetadataSearchHit> pageSearchResult(final List<MetadataSearchHit> items, final MetadataSearchRequest request) {
        int fromIndex = Math.min(request.getOffset(), items.size());
        int toIndex = (int) Math.min((long) fromIndex + request.getLimit(), items.size());
        return items.subList(fromIndex, toIndex);
    }
    
    private Map<String, Object> createSearchContext(final MetadataSearchRequest request, final Set<SupportedMCPMetadataObjectType> searchObjectTypes, final boolean broadSearchGuarded) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("query", request.getQuery());
        result.put("database", request.getDatabase());
        result.put("database_scope", request.getDatabase().isEmpty() ? "all_query_databases" : "single_database");
        result.put("schema", request.getSchema());
        result.put("object_types", createObjectTypeNames(searchObjectTypes));
        result.put("limit", request.getLimit());
        result.put("offset", request.getOffset());
        if (broadSearchGuarded) {
            result.put("broad_search_guarded", true);
            result.put("guard_reason", "Blank cross-database metadata search lists databases only instead of expanding every object type.");
            result.put("recommended_narrowing_arguments", List.of("database", "query", "object_types"));
        }
        return result;
    }
    
    private List<String> createObjectTypeNames(final Set<SupportedMCPMetadataObjectType> searchObjectTypes) {
        return searchObjectTypes.stream().map(each -> each.name().toLowerCase(Locale.ENGLISH)).sorted(this::compareObjectTypeNames).toList();
    }
    
    private int compareObjectTypeNames(final String left, final String right) {
        return Integer.compare(getObjectTypeOrder(left), getObjectTypeOrder(right));
    }
    
    private int compareSearchHits(final MetadataSearchHit left, final MetadataSearchHit right) {
        int result = left.getDatabase().compareTo(right.getDatabase());
        if (0 != result) {
            return result;
        }
        result = left.getSchema().compareTo(right.getSchema());
        if (0 != result) {
            return result;
        }
        result = Integer.compare(getObjectTypeOrder(left.getObjectType()), getObjectTypeOrder(right.getObjectType()));
        if (0 != result) {
            return result;
        }
        result = left.getTable().compareTo(right.getTable());
        if (0 != result) {
            return result;
        }
        result = left.getView().compareTo(right.getView());
        if (0 != result) {
            return result;
        }
        return left.getName().compareTo(right.getName());
    }
    
    private int getObjectTypeOrder(final String objectType) {
        return OBJECT_TYPE_ORDERS.getOrDefault(objectType, Integer.MAX_VALUE);
    }
    
    private Set<SupportedMCPMetadataObjectType> getSearchObjectTypes(final Set<SupportedMCPMetadataObjectType> objectTypes) {
        if (!objectTypes.isEmpty()) {
            return objectTypes;
        }
        Set<SupportedMCPMetadataObjectType> result = new LinkedHashSet<>();
        result.add(SupportedMCPMetadataObjectType.DATABASE);
        result.add(SupportedMCPMetadataObjectType.SCHEMA);
        result.add(SupportedMCPMetadataObjectType.TABLE);
        result.add(SupportedMCPMetadataObjectType.VIEW);
        result.add(SupportedMCPMetadataObjectType.COLUMN);
        result.add(SupportedMCPMetadataObjectType.INDEX);
        result.add(SupportedMCPMetadataObjectType.SEQUENCE);
        return result;
    }
}
