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

package org.apache.shardingsphere.mcp.tool.handler.metadata;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;
import org.apache.shardingsphere.mcp.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.protocol.exception.InvalidPageTokenException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.tool.response.MetadataSearchHit;
import org.apache.shardingsphere.mcp.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.tool.response.MetadataSearchResult;

import java.util.Arrays;
import java.util.Collections;
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
    
    private static final Map<String, Integer> OBJECT_TYPE_ORDERS = Map.of(
            "database", 0, "schema", 1, "table", 2, "view", 3, "column", 4, "index", 5, "sequence", 6);
    
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
        List<MetadataSearchHit> metadataItems = request.getDatabase().isEmpty()
                ? metadataQueryFacade.queryDatabases().stream().flatMap(each -> readSearchResults(each.getDatabase(), request).stream()).collect(Collectors.toList())
                : readSearchResults(request.getDatabase(), request);
        return paginate(metadataItems, request.getQuery(), request.getPageSize(), request.getPageToken());
    }
    
    private List<MetadataSearchHit> readSearchResults(final String databaseName, final MetadataSearchRequest request) {
        List<MetadataSearchHit> result = new LinkedList<>();
        for (SupportedMCPMetadataObjectType each : getSearchObjectTypes(request.getObjectTypes())) {
            if (SupportedMCPMetadataObjectType.DATABASE == each) {
                metadataQueryFacade.queryDatabase(databaseName).ifPresent(optional -> result.add(createSearchHit(optional)));
                continue;
            }
            if (!metadataQueryFacade.isSupportedMetadataObjectType(databaseName, each)) {
                continue;
            }
            result.addAll(querySearchHits(databaseName, each, request.getSchema()));
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
        return new MetadataSearchHit(databaseMetadata.getDatabase(), "", "database", "", "", databaseMetadata.getDatabase());
    }
    
    private MetadataSearchHit createSearchHit(final MCPSchemaMetadata schemaMetadata) {
        return new MetadataSearchHit(schemaMetadata.getDatabase(), schemaMetadata.getSchema(), "schema", "", "", schemaMetadata.getSchema());
    }
    
    private MetadataSearchHit createSearchHit(final MCPTableMetadata tableMetadata) {
        return new MetadataSearchHit(tableMetadata.getDatabase(), tableMetadata.getSchema(), "table", tableMetadata.getTable(), "", tableMetadata.getTable());
    }
    
    private MetadataSearchHit createSearchHit(final MCPViewMetadata viewMetadata) {
        return new MetadataSearchHit(viewMetadata.getDatabase(), viewMetadata.getSchema(), "view", "", viewMetadata.getView(), viewMetadata.getView());
    }
    
    private MetadataSearchHit createSearchHit(final MCPColumnMetadata columnMetadata) {
        return new MetadataSearchHit(columnMetadata.getDatabase(), columnMetadata.getSchema(), "column", columnMetadata.getTable(), columnMetadata.getView(), columnMetadata.getColumn());
    }
    
    private MetadataSearchHit createSearchHit(final MCPIndexMetadata indexMetadata) {
        return new MetadataSearchHit(indexMetadata.getDatabase(), indexMetadata.getSchema(), "index", indexMetadata.getTable(), "", indexMetadata.getIndex());
    }
    
    private MetadataSearchHit createSearchHit(final MCPSequenceMetadata sequenceMetadata) {
        return new MetadataSearchHit(sequenceMetadata.getDatabase(), sequenceMetadata.getSchema(), "sequence", "", "", sequenceMetadata.getSequence());
    }
    
    private MetadataSearchResult paginate(final List<MetadataSearchHit> metadataItems, final String query, final int pageSize, final String pageToken) {
        int actualOffset;
        try {
            actualOffset = pageToken.isEmpty() ? 0 : Integer.parseInt(pageToken);
        } catch (final NumberFormatException ignored) {
            throw new InvalidPageTokenException();
        }
        int actualPageSize = 0 < pageSize ? pageSize : 100;
        List<MetadataSearchHit> filteredItems = filterByQuery(metadataItems, query);
        filteredItems.sort(this::compareSearchHits);
        if (actualOffset > filteredItems.size()) {
            return new MetadataSearchResult(Collections.emptyList(), "");
        }
        int actualEndIndex = Math.min(actualOffset + actualPageSize, filteredItems.size());
        String nextPageToken = actualEndIndex < filteredItems.size() ? String.valueOf(actualEndIndex) : "";
        return new MetadataSearchResult(new LinkedList<>(filteredItems.subList(actualOffset, actualEndIndex)), nextPageToken);
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
        return metadataItems.stream().filter(each -> matchesQuery(each, query)).collect(Collectors.toList());
    }
    
    private boolean matchesQuery(final MetadataSearchHit searchHit, final String query) {
        return matchesValue(query.toLowerCase(Locale.ENGLISH), searchHit.getName(), searchHit.getTable(), searchHit.getView());
    }
    
    private boolean matchesValue(final String query, final String... values) {
        return Arrays.stream(values).anyMatch(each -> null != each && !each.isEmpty() && each.toLowerCase(Locale.ENGLISH).contains(query));
    }
}
