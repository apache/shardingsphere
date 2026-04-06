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

package org.apache.shardingsphere.mcp.tool;

import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.metadata.query.MetadataQueryService;
import org.apache.shardingsphere.mcp.protocol.exception.InvalidPageTokenException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Search executor for metadata discovery.
 */
public final class MetadataSearchExecutor {
    
    private final MetadataQueryService metadataQueryService;
    
    public MetadataSearchExecutor(final MCPDatabaseMetadataCatalog metadataCatalog) {
        metadataQueryService = new MetadataQueryService(metadataCatalog);
    }
    
    /**
     * Search metadata.
     *
     * @param request search request
     * @return search result
     */
    public MetadataSearchResult execute(final MetadataSearchRequest request) {
        validate(request);
        List<MetadataSearchHit> result = new LinkedList<>();
        if (request.getDatabase().isEmpty()) {
            for (MCPDatabaseMetadata each : metadataQueryService.queryDatabases()) {
                result.addAll(readSearchResults(each.getDatabase(), request));
            }
        } else {
            result.addAll(readSearchResults(request.getDatabase(), request));
        }
        return paginate(result, request.getQuery(), request.getPageSize(), request.getPageToken());
    }
    
    private void validate(final MetadataSearchRequest request) {
        if (request.getQuery().isEmpty()) {
            throw new MCPInvalidRequestException("Query is required.");
        }
        if (!request.getSchema().isEmpty() && request.getDatabase().isEmpty()) {
            throw new MCPInvalidRequestException("Schema cannot be provided without database.");
        }
    }
    
    private List<MetadataSearchHit> readSearchResults(final String databaseName, final MetadataSearchRequest request) {
        List<MetadataSearchHit> result = new LinkedList<>();
        for (MetadataObjectType each : getSearchObjectTypes(request.getObjectTypes())) {
            if (MetadataObjectType.DATABASE == each) {
                metadataQueryService.queryDatabase(databaseName).ifPresent(optional -> result.add(createSearchHit(optional)));
                continue;
            }
            if (!metadataQueryService.isSupportedMetadataObjectType(databaseName, each)) {
                continue;
            }
            result.addAll(querySearchHits(databaseName, each, request.getSchema()));
        }
        return result;
    }
    
    private List<MetadataSearchHit> querySearchHits(final String databaseName, final MetadataObjectType objectType, final String schemaName) {
        List<MetadataSearchHit> result = new LinkedList<>();
        switch (objectType) {
            case SCHEMA:
                if (!schemaName.isEmpty()) {
                    metadataQueryService.querySchema(databaseName, schemaName).ifPresent(optional -> result.add(createSearchHit(optional)));
                    break;
                }
                for (MCPSchemaMetadata each : metadataQueryService.querySchemas(databaseName)) {
                    result.add(createSearchHit(each));
                }
                break;
            case TABLE:
                for (MCPTableMetadata each : queryTables(databaseName, schemaName)) {
                    result.add(createSearchHit(each));
                }
                break;
            case VIEW:
                for (MCPViewMetadata each : queryViews(databaseName, schemaName)) {
                    result.add(createSearchHit(each));
                }
                break;
            case COLUMN:
                result.addAll(queryColumnSearchHits(databaseName, schemaName));
                break;
            case INDEX:
                result.addAll(queryIndexSearchHits(databaseName, schemaName));
                break;
            default:
                break;
        }
        return result;
    }
    
    private List<MCPTableMetadata> queryTables(final String databaseName, final String schemaName) {
        List<MCPTableMetadata> result = new LinkedList<>();
        if (!schemaName.isEmpty()) {
            result.addAll(metadataQueryService.queryTables(databaseName, schemaName));
            return result;
        }
        for (MCPSchemaMetadata each : metadataQueryService.querySchemas(databaseName)) {
            result.addAll(metadataQueryService.queryTables(databaseName, each.getSchema()));
        }
        return result;
    }
    
    private List<MCPViewMetadata> queryViews(final String databaseName, final String schemaName) {
        List<MCPViewMetadata> result = new LinkedList<>();
        if (!schemaName.isEmpty()) {
            result.addAll(metadataQueryService.queryViews(databaseName, schemaName));
            return result;
        }
        for (MCPSchemaMetadata each : metadataQueryService.querySchemas(databaseName)) {
            result.addAll(metadataQueryService.queryViews(databaseName, each.getSchema()));
        }
        return result;
    }
    
    private List<MetadataSearchHit> queryColumnSearchHits(final String databaseName, final String schemaName) {
        List<MetadataSearchHit> result = new LinkedList<>();
        for (MCPTableMetadata each : queryTables(databaseName, schemaName)) {
            for (MCPColumnMetadata column : metadataQueryService.queryTableColumns(databaseName, each.getSchema(), each.getTable())) {
                result.add(createSearchHit(column));
            }
        }
        for (MCPViewMetadata each : queryViews(databaseName, schemaName)) {
            for (MCPColumnMetadata column : metadataQueryService.queryViewColumns(databaseName, each.getSchema(), each.getView())) {
                result.add(createSearchHit(column));
            }
        }
        return result;
    }
    
    private List<MetadataSearchHit> queryIndexSearchHits(final String databaseName, final String schemaName) {
        List<MetadataSearchHit> result = new LinkedList<>();
        if (!metadataQueryService.isSupportedMetadataObjectType(databaseName, MetadataObjectType.INDEX)) {
            return result;
        }
        for (MCPTableMetadata each : queryTables(databaseName, schemaName)) {
            for (MCPIndexMetadata index : metadataQueryService.queryIndexes(databaseName, each.getSchema(), each.getTable())) {
                result.add(createSearchHit(index));
            }
        }
        return result;
    }
    
    private Set<MetadataObjectType> getSearchObjectTypes(final Set<MetadataObjectType> objectTypes) {
        if (!objectTypes.isEmpty()) {
            return objectTypes;
        }
        Set<MetadataObjectType> result = new LinkedHashSet<>();
        result.add(MetadataObjectType.DATABASE);
        result.add(MetadataObjectType.SCHEMA);
        result.add(MetadataObjectType.TABLE);
        result.add(MetadataObjectType.VIEW);
        result.add(MetadataObjectType.COLUMN);
        result.add(MetadataObjectType.INDEX);
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
        switch (objectType) {
            case "database":
                return 0;
            case "schema":
                return 1;
            case "table":
                return 2;
            case "view":
                return 3;
            case "column":
                return 4;
            case "index":
                return 5;
            default:
                return Integer.MAX_VALUE;
        }
    }
    
    private List<MetadataSearchHit> filterByQuery(final List<MetadataSearchHit> metadataItems, final String query) {
        List<MetadataSearchHit> result = new LinkedList<>();
        for (MetadataSearchHit each : metadataItems) {
            if (matchesQuery(each, query)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean matchesQuery(final MetadataSearchHit searchHit, final String query) {
        String actualQuery = query.toLowerCase(Locale.ENGLISH);
        return matchesValue(actualQuery, searchHit.getName(), searchHit.getTable(), searchHit.getView());
    }
    
    private boolean matchesValue(final String query, final String... values) {
        for (String each : values) {
            if (null != each && !each.isEmpty() && each.toLowerCase(Locale.ENGLISH).contains(query)) {
                return true;
            }
        }
        return false;
    }
}
