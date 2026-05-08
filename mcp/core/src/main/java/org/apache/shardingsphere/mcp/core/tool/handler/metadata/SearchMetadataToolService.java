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
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.resource.MCPUriTemplateUtils;
import org.apache.shardingsphere.mcp.core.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.core.tool.response.MetadataSearchHit;
import org.apache.shardingsphere.mcp.core.tool.response.MetadataSearchResult;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.exception.InvalidPageTokenException;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPViewMetadata;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Search metadata tool service.
 */
public final class SearchMetadataToolService {

    static final int DEFAULT_PAGE_SIZE = 100;

    static final int MAX_PAGE_SIZE = 500;

    private static final Map<String, Integer> OBJECT_TYPE_ORDERS = Map.of(
            "database", 0, "schema", 1, "table", 2, "view", 3, "column", 4, "index", 5, "sequence", 6);

    private static final String DATABASES_RESOURCE_URI = "shardingsphere://databases";

    private final MCPMetadataQueryFacade metadataQueryFacade;

    public SearchMetadataToolService(final MCPMetadataQueryFacade metadataQueryFacade) {
        this.metadataQueryFacade = metadataQueryFacade;
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
        List<MetadataSearchHit> metadataItems = request.getDatabase().isEmpty()
                ? metadataQueryFacade.queryDatabases().stream().flatMap(each -> readSearchResults(each.getDatabase(), request.getSchema(), searchObjectTypes).stream()).collect(Collectors.toList())
                : readSearchResults(request.getDatabase(), request.getSchema(), searchObjectTypes);
        return paginate(metadataItems, request, searchObjectTypes, false);
    }

    private boolean isBlankAllDatabaseSearch(final MetadataSearchRequest request) {
        return request.getDatabase().isEmpty() && request.getQuery().isEmpty() && request.getObjectTypes().isEmpty();
    }

    private MetadataSearchResult executeBlankAllDatabaseSearch(final MetadataSearchRequest request) {
        Set<SupportedMCPMetadataObjectType> searchObjectTypes = Set.of(SupportedMCPMetadataObjectType.DATABASE);
        return paginate(metadataQueryFacade.queryDatabases().stream().map(this::createSearchHit).collect(Collectors.toList()), request, searchObjectTypes, true);
    }

    private List<MetadataSearchHit> readSearchResults(final String databaseName, final String schemaName, final Set<SupportedMCPMetadataObjectType> searchObjectTypes) {
        List<MetadataSearchHit> result = new LinkedList<>();
        for (SupportedMCPMetadataObjectType each : searchObjectTypes) {
            if (SupportedMCPMetadataObjectType.DATABASE == each) {
                metadataQueryFacade.queryDatabase(databaseName).ifPresent(optional -> result.add(createSearchHit(optional)));
                continue;
            }
            if (!metadataQueryFacade.isSupportedMetadataObjectType(databaseName, each)) {
                continue;
            }
            result.addAll(querySearchHits(databaseName, each, schemaName));
        }
        return result;
    }

    private List<MetadataSearchHit> querySearchHits(final String databaseName, final SupportedMCPMetadataObjectType objectType, final String schemaName) {
        List<MetadataSearchHit> result = new LinkedList<>();
        if (SupportedMCPMetadataObjectType.SCHEMA == objectType) {
            if (!schemaName.isEmpty()) {
                metadataQueryFacade.querySchema(databaseName, schemaName).ifPresent(optional -> result.add(createSearchHit(optional)));
                return result;
            }
            for (MCPSchemaMetadata each : metadataQueryFacade.querySchemas(databaseName)) {
                result.add(createSearchHit(each));
            }
            return result;
        }
        if (SupportedMCPMetadataObjectType.TABLE == objectType) {
            for (MCPTableMetadata each : queryTables(databaseName, schemaName)) {
                result.add(createSearchHit(each));
            }
            return result;
        }
        if (SupportedMCPMetadataObjectType.VIEW == objectType) {
            for (MCPViewMetadata each : queryViews(databaseName, schemaName)) {
                result.add(createSearchHit(each));
            }
            return result;
        }
        if (SupportedMCPMetadataObjectType.COLUMN == objectType) {
            result.addAll(queryColumnSearchHits(databaseName, schemaName));
            return result;
        }
        if (SupportedMCPMetadataObjectType.INDEX == objectType) {
            result.addAll(queryIndexSearchHits(databaseName, schemaName));
            return result;
        }
        result.addAll(querySequenceSearchHits(databaseName, schemaName));
        return result;
    }

    private List<MCPTableMetadata> queryTables(final String databaseName, final String schemaName) {
        if (!schemaName.isEmpty()) {
            return metadataQueryFacade.queryTables(databaseName, schemaName);
        }
        return metadataQueryFacade.querySchemas(databaseName).stream().flatMap(each -> metadataQueryFacade.queryTables(databaseName, each.getSchema()).stream()).collect(Collectors.toList());
    }

    private List<MCPViewMetadata> queryViews(final String databaseName, final String schemaName) {
        List<MCPViewMetadata> result = new LinkedList<>();
        if (!schemaName.isEmpty()) {
            result.addAll(metadataQueryFacade.queryViews(databaseName, schemaName));
            return result;
        }
        for (MCPSchemaMetadata each : metadataQueryFacade.querySchemas(databaseName)) {
            result.addAll(metadataQueryFacade.queryViews(databaseName, each.getSchema()));
        }
        return result;
    }

    private List<MetadataSearchHit> queryColumnSearchHits(final String databaseName, final String schemaName) {
        List<MetadataSearchHit> result = new LinkedList<>();
        for (MCPTableMetadata each : queryTables(databaseName, schemaName)) {
            for (MCPColumnMetadata column : metadataQueryFacade.queryTableColumns(databaseName, each.getSchema(), each.getTable())) {
                result.add(createSearchHit(column));
            }
        }
        for (MCPViewMetadata each : queryViews(databaseName, schemaName)) {
            for (MCPColumnMetadata column : metadataQueryFacade.queryViewColumns(databaseName, each.getSchema(), each.getView())) {
                result.add(createSearchHit(column));
            }
        }
        return result;
    }

    private List<MetadataSearchHit> queryIndexSearchHits(final String databaseName, final String schemaName) {
        return queryTables(databaseName, schemaName).stream()
                .flatMap(each -> metadataQueryFacade.queryIndexes(databaseName, each.getSchema(), each.getTable()).stream()).map(this::createSearchHit).collect(Collectors.toList());
    }

    private List<MetadataSearchHit> querySequenceSearchHits(final String databaseName, final String schemaName) {
        if (!schemaName.isEmpty()) {
            return metadataQueryFacade.querySequences(databaseName, schemaName).stream().map(this::createSearchHit).collect(Collectors.toList());
        }
        return metadataQueryFacade.querySchemas(databaseName).stream()
                .flatMap(each -> metadataQueryFacade.querySequences(databaseName, each.getSchema()).stream()).map(this::createSearchHit).collect(Collectors.toList());
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

    private MetadataSearchHit createSearchHit(final MCPDatabaseMetadata databaseMetadata) {
        return createSearchHit(databaseMetadata.getDatabase(), "", "database", "", "", databaseMetadata.getDatabase());
    }

    private MetadataSearchHit createSearchHit(final MCPSchemaMetadata schemaMetadata) {
        return createSearchHit(schemaMetadata.getDatabase(), schemaMetadata.getSchema(), "schema", "", "", schemaMetadata.getSchema());
    }

    private MetadataSearchHit createSearchHit(final MCPTableMetadata tableMetadata) {
        return createSearchHit(tableMetadata.getDatabase(), tableMetadata.getSchema(), "table", tableMetadata.getTable(), "", tableMetadata.getTable());
    }

    private MetadataSearchHit createSearchHit(final MCPViewMetadata viewMetadata) {
        return createSearchHit(viewMetadata.getDatabase(), viewMetadata.getSchema(), "view", "", viewMetadata.getView(), viewMetadata.getView());
    }

    private MetadataSearchHit createSearchHit(final MCPColumnMetadata columnMetadata) {
        return createSearchHit(columnMetadata.getDatabase(), columnMetadata.getSchema(), "column", columnMetadata.getTable(), columnMetadata.getView(), columnMetadata.getColumn());
    }

    private MetadataSearchHit createSearchHit(final MCPIndexMetadata indexMetadata) {
        return createSearchHit(indexMetadata.getDatabase(), indexMetadata.getSchema(), "index", indexMetadata.getTable(), "", indexMetadata.getIndex());
    }

    private MetadataSearchHit createSearchHit(final MCPSequenceMetadata sequenceMetadata) {
        return createSearchHit(sequenceMetadata.getDatabase(), sequenceMetadata.getSchema(), "sequence", "", "", sequenceMetadata.getSequence());
    }

    private MetadataSearchHit createSearchHit(final String database, final String schema, final String objectType, final String table, final String view, final String name) {
        MetadataResourceUris resourceUris = createMetadataResourceUris(database, schema, objectType, table, view, name);
        return new MetadataSearchHit(database, schema, objectType, table, view, name, resourceUris.resource(), resourceUris.parentResource(), resourceUris.nextResources(),
                resourceUris.derivationStatus(), resourceUris.derivationReason(), "", List.of(), "");
    }

    private MetadataResourceUris createMetadataResourceUris(final String database, final String schema, final String objectType, final String table, final String view, final String name) {
        if ("database".equals(objectType)) {
            return createDatabaseResourceUris(database);
        }
        if ("schema".equals(objectType)) {
            return createSchemaResourceUris(database, schema);
        }
        if ("table".equals(objectType)) {
            return createTableResourceUris(database, schema, table);
        }
        if ("view".equals(objectType)) {
            return createViewResourceUris(database, schema, view);
        }
        if ("column".equals(objectType)) {
            return createColumnResourceUris(database, schema, table, view, name);
        }
        if ("index".equals(objectType)) {
            return createIndexResourceUris(database, schema, table, name);
        }
        if ("sequence".equals(objectType)) {
            return createSequenceResourceUris(database, schema, name);
        }
        return notSafe("Metadata hit object type is not backed by a descriptor resource pattern.");
    }

    private MetadataResourceUris createDatabaseResourceUris(final String database) {
        if (!canUseInUri(database)) {
            return notSafe("Metadata hit does not include a database name safe for resource URI derivation.");
        }
        String databaseUri = createResourceUri(database);
        return derived(databaseUri, DATABASES_RESOURCE_URI, List.of(createResourceUri(database, "capabilities"), createResourceUri(database, "schemas")));
    }

    private MetadataResourceUris createSchemaResourceUris(final String database, final String schema) {
        if (!canUseInUri(database, schema)) {
            return notSafe("Metadata hit does not include database and schema names safe for resource URI derivation.");
        }
        String schemaUri = createResourceUri(database, "schemas", schema);
        return derived(schemaUri, createResourceUri(database, "schemas"),
                List.of(createResourceUri(database, "schemas", schema, "tables"), createResourceUri(database, "schemas", schema, "views"),
                        createResourceUri(database, "schemas", schema, "sequences")));
    }

    private MetadataResourceUris createTableResourceUris(final String database, final String schema, final String table) {
        if (!canUseInUri(database, schema, table)) {
            return notSafe("Metadata hit does not include database, schema, and table names safe for resource URI derivation.");
        }
        String tableUri = createResourceUri(database, "schemas", schema, "tables", table);
        return derived(tableUri, createResourceUri(database, "schemas", schema, "tables"),
                List.of(createResourceUri(database, "schemas", schema, "tables", table, "columns"), createResourceUri(database, "schemas", schema, "tables", table, "indexes")));
    }

    private MetadataResourceUris createViewResourceUris(final String database, final String schema, final String view) {
        if (!canUseInUri(database, schema, view)) {
            return notSafe("Metadata hit does not include database, schema, and view names safe for resource URI derivation.");
        }
        String viewUri = createResourceUri(database, "schemas", schema, "views", view);
        return derived(viewUri, createResourceUri(database, "schemas", schema, "views"), List.of(createResourceUri(database, "schemas", schema, "views", view, "columns")));
    }

    private MetadataResourceUris createColumnResourceUris(final String database, final String schema, final String table, final String view, final String column) {
        if (canUseInUri(database, schema, table, column)) {
            return derived(createResourceUri(database, "schemas", schema, "tables", table, "columns", column),
                    createResourceUri(database, "schemas", schema, "tables", table, "columns"), List.of());
        }
        if (canUseInUri(database, schema, view, column)) {
            return derived(createResourceUri(database, "schemas", schema, "views", view, "columns", column),
                    createResourceUri(database, "schemas", schema, "views", view, "columns"), List.of());
        }
        return notSafe("Metadata hit does not include database, schema, parent table or view, and column names safe for resource URI derivation.");
    }

    private MetadataResourceUris createIndexResourceUris(final String database, final String schema, final String table, final String index) {
        if (!canUseInUri(database, schema, table, index)) {
            return notSafe("Metadata hit does not include database, schema, table, and index names safe for resource URI derivation.");
        }
        return derived(createResourceUri(database, "schemas", schema, "tables", table, "indexes", index),
                createResourceUri(database, "schemas", schema, "tables", table, "indexes"), List.of());
    }

    private MetadataResourceUris createSequenceResourceUris(final String database, final String schema, final String sequence) {
        if (!canUseInUri(database, schema, sequence)) {
            return notSafe("Metadata hit does not include database, schema, and sequence names safe for resource URI derivation.");
        }
        return derived(createResourceUri(database, "schemas", schema, "sequences", sequence), createResourceUri(database, "schemas", schema, "sequences"), List.of());
    }

    private MetadataResourceUris derived(final String resourceUri, final String parentResourceUri, final List<String> nextResourceUris) {
        return new MetadataResourceUris(MCPResourceHintUtils.create(resourceUri, resolveResourceKind(resourceUri), "inspect_detail", "Read the matched metadata detail resource.", "resource"),
                MCPResourceHintUtils.create(parentResourceUri, resolveResourceKind(parentResourceUri), "inspect_parent", "Read the parent metadata resource.", "parent_resource"),
                nextResourceUris.stream().map(each -> MCPResourceHintUtils.create(each, resolveResourceKind(each), "inspect_detail", "Read a child metadata resource.", "next_resources")).toList(),
                "derived", "");
    }

    private MetadataResourceUris notSafe(final String reason) {
        return new MetadataResourceUris(Map.of(), Map.of(), List.of(), "not_safe_to_derive", reason);
    }

    private String resolveResourceKind(final String uri) {
        if (uri.contains("/columns")) {
            return "column";
        }
        if (uri.contains("/indexes")) {
            return "index";
        }
        if (uri.contains("/tables")) {
            return "table";
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
        return "database";
    }

    private boolean canUseInUri(final String... values) {
        for (String each : values) {
            if (!canUsePathSegmentInUri(each)) {
                return false;
            }
        }
        return true;
    }

    private boolean canUsePathSegmentInUri(final String value) {
        return null != value && !value.isBlank();
    }

    private String createResourceUri(final String... pathSegments) {
        List<String> encodedSegments = new LinkedList<>();
        for (String each : pathSegments) {
            encodedSegments.add(MCPUriTemplateUtils.encodePathSegment(each));
        }
        return DATABASES_RESOURCE_URI + "/" + String.join("/", encodedSegments);
    }

    private MetadataSearchResult paginate(final List<MetadataSearchHit> metadataItems, final MetadataSearchRequest request,
                                          final Set<SupportedMCPMetadataObjectType> searchObjectTypes, final boolean broadSearchGuarded) {
        int actualOffset = resolvePageOffset(request.getPageToken());
        int actualPageSize = resolvePageSize(request.getPageSize());
        Map<String, Object> searchContext = createSearchContext(request, searchObjectTypes, actualPageSize, actualOffset, broadSearchGuarded);
        List<MetadataSearchHit> filteredItems = filterByQuery(metadataItems, request.getQuery());
        filteredItems.sort(this::compareSearchHits);
        if (actualOffset > filteredItems.size()) {
            return new MetadataSearchResult(Collections.emptyList(), "", searchContext, filteredItems.size(), filteredItems);
        }
        int actualEndIndex = Math.min(actualOffset + actualPageSize, filteredItems.size());
        String nextPageToken = actualEndIndex < filteredItems.size() ? String.valueOf(actualEndIndex) : "";
        return new MetadataSearchResult(new LinkedList<>(filteredItems.subList(actualOffset, actualEndIndex)), nextPageToken, searchContext, filteredItems.size(), filteredItems);
    }

    private int resolvePageOffset(final String pageToken) {
        try {
            int result = pageToken.isEmpty() ? 0 : Integer.parseInt(pageToken);
            if (0 <= result) {
                return result;
            }
        } catch (final NumberFormatException ignored) {
        }
        throw new InvalidPageTokenException();
    }

    private int resolvePageSize(final int pageSize) {
        int result = 0 == pageSize ? DEFAULT_PAGE_SIZE : pageSize;
        if (1 <= result && result <= MAX_PAGE_SIZE) {
            return result;
        }
        throw new MCPInvalidRequestException(String.format("page_size must be an integer between 1 and %d.", MAX_PAGE_SIZE));
    }

    private Map<String, Object> createSearchContext(final MetadataSearchRequest request, final Set<SupportedMCPMetadataObjectType> searchObjectTypes,
                                                    final int actualPageSize, final int actualOffset, final boolean broadSearchGuarded) {
        Map<String, Object> result = new LinkedHashMap<>(10, 1F);
        result.put("query", request.getQuery());
        result.put("database", request.getDatabase());
        result.put("database_scope", request.getDatabase().isEmpty() ? "all_query_databases" : "single_database");
        result.put("schema", request.getSchema());
        result.put("object_types", createObjectTypeNames(searchObjectTypes));
        result.put("page_size", actualPageSize);
        result.put("page_offset", actualOffset);
        if (broadSearchGuarded) {
            result.put("broad_search_guarded", true);
            result.put("guard_reason", "Blank cross-database metadata search lists databases only instead of expanding every object type.");
            result.put("recommended_narrowing_arguments", List.of("database", "query", "object_types"));
        }
        return result;
    }

    private List<String> createObjectTypeNames(final Set<SupportedMCPMetadataObjectType> searchObjectTypes) {
        return searchObjectTypes.stream().map(each -> each.name().toLowerCase(Locale.ENGLISH)).sorted(this::compareObjectTypeNames).collect(Collectors.toList());
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

    private List<MetadataSearchHit> filterByQuery(final List<MetadataSearchHit> metadataItems, final String query) {
        List<MetadataSearchHit> result = new LinkedList<>();
        for (MetadataSearchHit each : metadataItems) {
            SearchMatch searchMatch = createSearchMatch(each, query);
            if (!"none".equals(searchMatch.matchKind())) {
                result.add(each.withMatch(searchMatch.matchKind(), searchMatch.matchedFields(), searchMatch.matchedValue()));
            }
        }
        return result;
    }

    private SearchMatch createSearchMatch(final MetadataSearchHit searchHit, final String query) {
        String normalizedQuery = query.trim().toLowerCase(Locale.ENGLISH);
        if (normalizedQuery.isEmpty()) {
            return createMatchCandidates(searchHit).stream().anyMatch(each -> !each.value().isEmpty())
                    ? new SearchMatch("all", List.of(), "")
                    : new SearchMatch("none", List.of(), "");
        }
        for (String each : List.of("exact", "prefix", "contains")) {
            SearchMatch result = createSearchMatch(searchHit, normalizedQuery, each);
            if (!"none".equals(result.matchKind())) {
                return result;
            }
        }
        return new SearchMatch("none", List.of(), "");
    }

    private SearchMatch createSearchMatch(final MetadataSearchHit searchHit, final String normalizedQuery, final String matchKind) {
        List<String> matchedFields = new LinkedList<>();
        String matchedValue = "";
        for (MatchCandidate each : createMatchCandidates(searchHit)) {
            if (matchesValue(each.value(), normalizedQuery, matchKind)) {
                matchedFields.add(each.field());
                if (matchedValue.isEmpty()) {
                    matchedValue = each.value();
                }
            }
        }
        return matchedFields.isEmpty() ? new SearchMatch("none", List.of(), "") : new SearchMatch(matchKind, matchedFields, matchedValue);
    }

    private List<MatchCandidate> createMatchCandidates(final MetadataSearchHit searchHit) {
        List<MatchCandidate> result = new LinkedList<>();
        result.add(new MatchCandidate("name", searchHit.getName()));
        if (!searchHit.getTable().isEmpty()) {
            result.add(new MatchCandidate("table", searchHit.getTable()));
        }
        if (!searchHit.getView().isEmpty()) {
            result.add(new MatchCandidate("view", searchHit.getView()));
        }
        return result;
    }

    private boolean matchesValue(final String value, final String normalizedQuery, final String matchKind) {
        if (null == value || value.isEmpty()) {
            return false;
        }
        String normalizedValue = value.toLowerCase(Locale.ENGLISH);
        if ("exact".equals(matchKind)) {
            return normalizedValue.equals(normalizedQuery);
        }
        return "prefix".equals(matchKind) ? normalizedValue.startsWith(normalizedQuery) : normalizedValue.contains(normalizedQuery);
    }

    private record MetadataResourceUris(Map<String, Object> resource, Map<String, Object> parentResource, List<Map<String, Object>> nextResources, String derivationStatus, String derivationReason) {
    }

    private record MatchCandidate(String field, String value) {
    }

    private record SearchMatch(String matchKind, List<String> matchedFields, String matchedValue) {
    }
}
