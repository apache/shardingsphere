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

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.core.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.core.tool.response.MetadataSearchHit;
import org.apache.shardingsphere.mcp.core.tool.response.MetadataSearchResult;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPItemsResponse;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handler for search-metadata tool.
 */
public final class SearchMetadataToolHandler implements MCPToolHandler<MCPDatabaseHandlerContext> {
    
    private static final String TOOL_NAME = "database_gateway_search_metadata";
    
    private static final Set<SupportedMCPMetadataObjectType> SUPPORTED_OBJECT_TYPES = Set.of(
            SupportedMCPMetadataObjectType.DATABASE, SupportedMCPMetadataObjectType.SCHEMA, SupportedMCPMetadataObjectType.TABLE,
            SupportedMCPMetadataObjectType.VIEW, SupportedMCPMetadataObjectType.COLUMN, SupportedMCPMetadataObjectType.INDEX, SupportedMCPMetadataObjectType.SEQUENCE);
    
    @Override
    public Class<MCPDatabaseHandlerContext> getContextType() {
        return MCPDatabaseHandlerContext.class;
    }
    
    @Override
    public String getToolName() {
        return TOOL_NAME;
    }
    
    @Override
    public MCPResponse handle(final MCPDatabaseHandlerContext databaseContext, final MCPToolCall toolCall) {
        MCPToolArguments toolArguments = new MCPToolArguments(toolCall.getArguments());
        String query = toolArguments.getStringArgument("query");
        MetadataSearchRequest request = new MetadataSearchRequest(
                toolArguments.getStringArgument("database"), toolArguments.getStringArgument("schema"), query,
                toolArguments.getObjectTypes(SUPPORTED_OBJECT_TYPES, query.isEmpty() ? Set.of() : Set.of(query)));
        MetadataSearchResult searchResult = new SearchMetadataToolService(databaseContext.getMetadataQueryFacade()).execute(request);
        return new MCPItemsResponse(searchResult.getItems(), createSearchPayloadMetadata(request, searchResult), MCPResponseMode.SEARCH);
    }
    
    private Map<String, Object> createSearchPayloadMetadata(final MetadataSearchRequest request, final MetadataSearchResult searchResult) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("search_context", searchResult.getSearchContext());
        result.put("total_match_count", searchResult.getTotalMatchCount());
        if (searchResult.getItems().isEmpty()) {
            result.put("empty_state", createEmptyState(request));
            result.put("next_actions", List.of(createEmptySearchNextAction(request)));
            return result;
        }
        List<String> duplicatedNames = findDuplicatedNames(searchResult.getItems(), request.getQuery());
        List<Map<String, Object>> nextActions = createResultNextActions(searchResult, duplicatedNames);
        if (!nextActions.isEmpty()) {
            result.put("next_actions", nextActions);
        }
        if (!duplicatedNames.isEmpty()) {
            result.put("ambiguity_state", createAmbiguityState(searchResult.getItems(), duplicatedNames));
        }
        return result;
    }
    
    private List<Map<String, Object>> createResultNextActions(final MetadataSearchResult searchResult, final List<String> duplicatedNames) {
        List<Map<String, Object>> result = new LinkedList<>();
        if (isBroadSearchGuarded(searchResult)) {
            result.add(MCPNextActionUtils.askUser("Blank cross-database metadata search listed databases only. Choose a database, query, or object type before searching deeper metadata.",
                    List.of("database", "query", "object_types")));
        }
        if (!duplicatedNames.isEmpty()) {
            result.add(MCPNextActionUtils.askUser("Multiple metadata hits share the same name. Ask the user to choose database, schema, or object type before using a specific resource.",
                    List.of("database", "schema", "object_types")));
        }
        return addOrder(result);
    }
    
    private boolean isBroadSearchGuarded(final MetadataSearchResult searchResult) {
        return Boolean.TRUE.equals(searchResult.getSearchContext().get("broad_search_guarded"));
    }
    
    private List<Map<String, Object>> addOrder(final List<Map<String, Object>> actions) {
        List<Map<String, Object>> result = new LinkedList<>();
        int order = 1;
        for (Map<String, Object> each : actions) {
            Map<String, Object> action = new LinkedHashMap<>(each);
            action.put("order", order++);
            result.add(action);
        }
        return result;
    }
    
    private List<String> findDuplicatedNames(final List<MetadataSearchHit> items, final String query) {
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
    
    private boolean isAmbiguityCandidate(final MetadataSearchHit searchHit, final String query) {
        return query.isEmpty() || searchHit.getMatchedFields().contains("name");
    }
    
    private Map<String, Object> createAmbiguityState(final List<MetadataSearchHit> items, final List<String> duplicatedNames) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("state", "duplicate_names");
        result.put("ambiguous", true);
        result.put("ambiguous_by", createAmbiguousDimensions(items, duplicatedNames));
        result.put("candidate_count", countDuplicatedCandidates(items, duplicatedNames));
        result.put("duplicated_names", duplicatedNames);
        result.put("narrowing_arguments", List.of("database", "schema", "object_types"));
        result.put("reason", "Multiple metadata hits share the same name; choose an explicit database, schema, or object type before reading a specific resource.");
        return result;
    }
    
    private List<String> createAmbiguousDimensions(final List<MetadataSearchHit> items, final List<String> duplicatedNames) {
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
    
    private void addIfNotEmpty(final Set<String> values, final String value) {
        if (null != value && !value.isEmpty()) {
            values.add(value);
        }
    }
    
    private void addDimensionIfAmbiguous(final List<String> result, final String dimension, final Set<String> values) {
        if (1 < values.size()) {
            result.add(dimension);
        }
    }
    
    private int countDuplicatedCandidates(final List<MetadataSearchHit> items, final List<String> duplicatedNames) {
        int result = 0;
        for (MetadataSearchHit each : items) {
            if (duplicatedNames.contains(each.getName())) {
                result++;
            }
        }
        return result;
    }
    
    private Map<String, Object> createEmptyState(final MetadataSearchRequest request) {
        final Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("state", request.getQuery().isEmpty() ? "no_items" : "no_match");
        result.put("category", request.getQuery().isEmpty() ? "empty_scope" : "not_found");
        result.put("reason", request.getQuery().isEmpty() ? "No metadata is available in the requested scope." : "No metadata matched the query in the requested scope.");
        return result;
    }
    
    private Map<String, Object> createEmptySearchNextAction(final MetadataSearchRequest request) {
        if (!request.getQuery().isEmpty() || !request.getSchema().isEmpty()) {
            return MCPNextActionUtils.callTool(TOOL_NAME, "Retry database_gateway_search_metadata with a broader scope.", createBroadenedSearchArguments(request));
        }
        return MCPNextActionUtils.readResource("shardingsphere://databases", "Read configured databases before choosing a narrower metadata search.");
    }
    
    private Map<String, Object> createBroadenedSearchArguments(final MetadataSearchRequest request) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        if (!request.getQuery().isEmpty()) {
            result.put("query", request.getQuery());
        } else if (!request.getDatabase().isEmpty()) {
            result.put("database", request.getDatabase());
        }
        return result;
    }
}
