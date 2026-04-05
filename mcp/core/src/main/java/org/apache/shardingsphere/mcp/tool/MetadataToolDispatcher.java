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
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.metadata.model.MetadataSearchHit;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;
import org.apache.shardingsphere.mcp.metadata.query.MetadataQueryService;
import org.apache.shardingsphere.mcp.protocol.exception.InvalidPageTokenException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Dispatch metadata discovery tools against metadata snapshots.
 */
public final class MetadataToolDispatcher {
    
    private final MetadataQueryService metadataQueryService;
    
    public MetadataToolDispatcher(final DatabaseMetadataSnapshots databaseMetadataSnapshots) {
        metadataQueryService = new MetadataQueryService(databaseMetadataSnapshots);
    }
    
    /**
     * Dispatch one metadata tool request.
     *
     * @param toolRequest tool request
     * @return tool dispatch result
     * @throws MCPInvalidRequestException when the request parameters are invalid
     * @throws InvalidPageTokenException when the page token is invalid
     * @throws MCPUnsupportedException when the requested metadata type is unsupported
     */
    public ToolDispatchResult dispatch(final ToolRequest toolRequest) {
        switch (toolRequest.getToolName()) {
            case "list_databases":
                return paginate(metadataQueryService.queryDatabases(), toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "list_schemas":
                return paginate(querySchemas(toolRequest), toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "list_tables":
                return paginate(queryTables(toolRequest), toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "list_views":
                return paginate(queryViews(toolRequest), toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "list_columns":
                return paginate(queryColumns(toolRequest), toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "list_indexes":
                return paginate(queryIndexes(toolRequest), toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "search_metadata":
                return searchMetadata(toolRequest);
            case "describe_table":
                return describeTable(toolRequest);
            case "describe_view":
                return describeView(toolRequest);
            default:
                throw new MCPInvalidRequestException("Unsupported metadata tool.");
        }
    }
    
    private List<MCPSchemaMetadata> querySchemas(final ToolRequest toolRequest) {
        if (toolRequest.getDatabase().isEmpty()) {
            throw new MCPInvalidRequestException("Database is required.");
        }
        return metadataQueryService.querySchemas(toolRequest.getDatabase());
    }
    
    private List<MCPTableMetadata> queryTables(final ToolRequest toolRequest) {
        validateDatabaseAndSchema(toolRequest);
        return metadataQueryService.queryTables(toolRequest.getDatabase(), toolRequest.getSchema());
    }
    
    private List<MCPViewMetadata> queryViews(final ToolRequest toolRequest) {
        validateDatabaseAndSchema(toolRequest);
        return metadataQueryService.queryViews(toolRequest.getDatabase(), toolRequest.getSchema());
    }
    
    private List<MCPColumnMetadata> queryColumns(final ToolRequest toolRequest) {
        validateDatabaseAndSchema(toolRequest);
        if (toolRequest.getObjectName().isEmpty() || toolRequest.getParentObjectType().isEmpty()) {
            throw new MCPInvalidRequestException("Parent object type and object name are required.");
        }
        if ("TABLE".equals(toolRequest.getParentObjectType())) {
            return metadataQueryService.queryTableColumns(toolRequest.getDatabase(), toolRequest.getSchema(), toolRequest.getObjectName());
        }
        if ("VIEW".equals(toolRequest.getParentObjectType())) {
            return metadataQueryService.queryViewColumns(toolRequest.getDatabase(), toolRequest.getSchema(), toolRequest.getObjectName());
        }
        throw new MCPInvalidRequestException("Parent object type must be table or view.");
    }
    
    private List<MCPIndexMetadata> queryIndexes(final ToolRequest toolRequest) {
        validateDatabaseAndSchema(toolRequest);
        if (toolRequest.getObjectName().isEmpty()) {
            throw new MCPInvalidRequestException("Table name is required.");
        }
        return metadataQueryService.queryIndexes(toolRequest.getDatabase(), toolRequest.getSchema(), toolRequest.getObjectName());
    }
    
    private ToolDispatchResult searchMetadata(final ToolRequest toolRequest) {
        if (!toolRequest.getSchema().isEmpty() && toolRequest.getDatabase().isEmpty()) {
            throw new MCPInvalidRequestException("Schema cannot be provided without database.");
        }
        List<MetadataSearchHit> result = new LinkedList<>();
        if (toolRequest.getDatabase().isEmpty()) {
            for (MCPDatabaseMetadata each : metadataQueryService.queryDatabases()) {
                result.addAll(readSearchResults(each.getDatabase(), toolRequest));
            }
        } else {
            result.addAll(readSearchResults(toolRequest.getDatabase(), toolRequest));
        }
        return paginate(result, toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
    }
    
    private List<MetadataSearchHit> readSearchResults(final String databaseName, final ToolRequest toolRequest) {
        List<MetadataSearchHit> result = new LinkedList<>();
        for (MetadataObjectType each : getSearchObjectTypes(toolRequest.getObjectTypes())) {
            if (MetadataObjectType.DATABASE == each) {
                metadataQueryService.queryDatabase(databaseName).ifPresent(optional -> result.add(createSearchHit(optional)));
                continue;
            }
            if (!metadataQueryService.isSupportedMetadataObjectType(databaseName, each)) {
                continue;
            }
            result.addAll(querySearchHits(databaseName, each, toolRequest.getSchema()));
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
                for (MCPTableMetadata each : queryTablesForSearch(databaseName, schemaName)) {
                    result.add(createSearchHit(each));
                }
                break;
            case VIEW:
                for (MCPViewMetadata each : queryViewsForSearch(databaseName, schemaName)) {
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
    
    private List<MCPTableMetadata> queryTablesForSearch(final String databaseName, final String schemaName) {
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
    
    private List<MCPViewMetadata> queryViewsForSearch(final String databaseName, final String schemaName) {
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
        for (MCPTableMetadata each : queryTablesForSearch(databaseName, schemaName)) {
            for (MCPColumnMetadata column : metadataQueryService.queryTableColumns(databaseName, each.getSchema(), each.getTable())) {
                result.add(createSearchHit(column));
            }
        }
        for (MCPViewMetadata each : queryViewsForSearch(databaseName, schemaName)) {
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
        for (MCPTableMetadata each : queryTablesForSearch(databaseName, schemaName)) {
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
    
    private ToolDispatchResult describeTable(final ToolRequest toolRequest) {
        validateDescribeRequest(toolRequest);
        Optional<MCPTableMetadata> tableMetadata = metadataQueryService.queryTable(toolRequest.getDatabase(), toolRequest.getSchema(), toolRequest.getObjectName());
        return new ToolDispatchResult(tableMetadata.map(List::of).orElse(Collections.emptyList()), "");
    }
    
    private ToolDispatchResult describeView(final ToolRequest toolRequest) {
        validateDescribeRequest(toolRequest);
        Optional<MCPViewMetadata> viewMetadata = metadataQueryService.queryView(toolRequest.getDatabase(), toolRequest.getSchema(), toolRequest.getObjectName());
        return new ToolDispatchResult(viewMetadata.map(List::of).orElse(Collections.emptyList()), "");
    }
    
    private void validateDatabaseAndSchema(final ToolRequest toolRequest) {
        if (toolRequest.getDatabase().isEmpty()) {
            throw new MCPInvalidRequestException("Database is required.");
        }
        if (toolRequest.getSchema().isEmpty()) {
            throw new MCPInvalidRequestException("Schema is required.");
        }
    }
    
    private void validateDescribeRequest(final ToolRequest toolRequest) {
        if (toolRequest.getDatabase().isEmpty() || toolRequest.getSchema().isEmpty() || toolRequest.getObjectName().isEmpty()) {
            throw new MCPInvalidRequestException("Database, schema, and object name are required.");
        }
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
    
    private <T> ToolDispatchResult paginate(final Collection<T> metadataItems, final String query, final int pageSize, final String pageToken) {
        int actualOffset;
        try {
            actualOffset = pageToken.isEmpty() ? 0 : Integer.parseInt(pageToken);
        } catch (final NumberFormatException ignored) {
            throw new InvalidPageTokenException();
        }
        int actualPageSize = 0 < pageSize ? pageSize : 100;
        List<T> filteredItems = filterByQuery(metadataItems, query);
        if (actualOffset > filteredItems.size()) {
            return new ToolDispatchResult(Collections.emptyList(), "");
        }
        int actualEndIndex = Math.min(actualOffset + actualPageSize, filteredItems.size());
        String nextPageToken = actualEndIndex < filteredItems.size() ? String.valueOf(actualEndIndex) : "";
        return new ToolDispatchResult(new LinkedList<>(filteredItems.subList(actualOffset, actualEndIndex)), nextPageToken);
    }
    
    private <T> List<T> filterByQuery(final Collection<T> metadataItems, final String query) {
        List<T> result = new LinkedList<>();
        for (T each : metadataItems) {
            if (matchesSearch(each, query)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean matchesSearch(final Object metadataItem, final String query) {
        if (query.isEmpty()) {
            return true;
        }
        String actualQuery = query.toLowerCase(Locale.ENGLISH);
        if (metadataItem instanceof MCPDatabaseMetadata) {
            MCPDatabaseMetadata actual = (MCPDatabaseMetadata) metadataItem;
            return matchesValue(actualQuery, actual.getDatabase());
        }
        if (metadataItem instanceof MCPSchemaMetadata) {
            MCPSchemaMetadata actual = (MCPSchemaMetadata) metadataItem;
            return matchesValue(actualQuery, actual.getSchema());
        }
        if (metadataItem instanceof MCPTableMetadata) {
            MCPTableMetadata actual = (MCPTableMetadata) metadataItem;
            return matchesValue(actualQuery, actual.getTable());
        }
        if (metadataItem instanceof MCPViewMetadata) {
            MCPViewMetadata actual = (MCPViewMetadata) metadataItem;
            return matchesValue(actualQuery, actual.getView());
        }
        if (metadataItem instanceof MCPColumnMetadata) {
            MCPColumnMetadata actual = (MCPColumnMetadata) metadataItem;
            return matchesValue(actualQuery, actual.getColumn(), actual.getTable(), actual.getView());
        }
        if (metadataItem instanceof MCPIndexMetadata) {
            MCPIndexMetadata actual = (MCPIndexMetadata) metadataItem;
            return matchesValue(actualQuery, actual.getIndex(), actual.getTable());
        }
        if (metadataItem instanceof MetadataSearchHit) {
            MetadataSearchHit actual = (MetadataSearchHit) metadataItem;
            return matchesValue(actualQuery, actual.getName(), actual.getTable(), actual.getView());
        }
        return false;
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
