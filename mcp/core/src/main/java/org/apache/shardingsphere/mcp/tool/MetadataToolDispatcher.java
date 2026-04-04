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

import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.metadata.query.MetadataQueryResult;
import org.apache.shardingsphere.mcp.metadata.query.MetadataQueryService;
import org.apache.shardingsphere.mcp.protocol.MCPError.MCPErrorCode;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Dispatch metadata discovery tools against normalized metadata snapshots.
 */
public final class MetadataToolDispatcher {
    
    private final MetadataQueryService metadataQueryService = new MetadataQueryService();
    
    /**
     * Dispatch one metadata tool request.
     *
     * @param databaseMetadataSnapshots database metadata snapshots
     * @param toolRequest tool request
     * @return tool dispatch result
     */
    public ToolDispatchResult dispatch(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final ToolRequest toolRequest) {
        switch (toolRequest.getToolName()) {
            case "list_databases":
                return paginate(metadataQueryService.queryDatabases(databaseMetadataSnapshots, each -> true),
                        toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "list_schemas":
                return paginate(validateAndListMetadata(databaseMetadataSnapshots, toolRequest, MetadataObjectType.SCHEMA, "", false),
                        toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "list_tables":
                return paginate(validateAndListMetadata(databaseMetadataSnapshots, toolRequest, MetadataObjectType.TABLE, "", true),
                        toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "list_views":
                return paginate(validateAndListMetadata(databaseMetadataSnapshots, toolRequest, MetadataObjectType.VIEW, "", true),
                        toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "list_columns":
                if (toolRequest.getObjectName().isEmpty() || toolRequest.getParentObjectType().isEmpty()) {
                    return ToolDispatchResult.error(MCPErrorCode.INVALID_REQUEST, "Parent object type and object name are required.");
                }
                return paginate(validateAndListMetadata(databaseMetadataSnapshots, toolRequest, MetadataObjectType.COLUMN, toolRequest.getParentObjectType(), true),
                        toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "list_indexes":
                if (toolRequest.getObjectName().isEmpty()) {
                    return ToolDispatchResult.error(MCPErrorCode.INVALID_REQUEST, "Table name is required.");
                }
                return paginate(validateAndListMetadata(databaseMetadataSnapshots, toolRequest, MetadataObjectType.INDEX, "TABLE", true),
                        toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "search_metadata":
                return searchMetadata(databaseMetadataSnapshots, toolRequest);
            case "describe_table":
                return describeObject(databaseMetadataSnapshots, toolRequest, MetadataObjectType.TABLE);
            case "describe_view":
                return describeObject(databaseMetadataSnapshots, toolRequest, MetadataObjectType.VIEW);
            default:
                return ToolDispatchResult.error(MCPErrorCode.INVALID_REQUEST, "Unsupported metadata tool.");
        }
    }
    
    private MetadataQueryResult validateAndListMetadata(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final ToolRequest toolRequest, final MetadataObjectType objectType,
                                                        final String parentObjectType, final boolean requireSchema) {
        if (toolRequest.getDatabase().isEmpty()) {
            return MetadataQueryResult.error(MCPErrorCode.INVALID_REQUEST, "Database is required.");
        }
        if (requireSchema && toolRequest.getSchema().isEmpty()) {
            return MetadataQueryResult.error(MCPErrorCode.INVALID_REQUEST, "Schema is required.");
        }
        return metadataQueryService.queryMetadataObjects(databaseMetadataSnapshots, toolRequest.getDatabase(), objectType,
                each -> (toolRequest.getSchema().isEmpty() || toolRequest.getSchema().equals(each.getSchema()))
                        && (parentObjectType.isEmpty() || parentObjectType.equals(each.getParentObjectType()))
                        && (toolRequest.getObjectName().isEmpty() || toolRequest.getObjectName().equals(each.getParentObjectName())));
    }
    
    private ToolDispatchResult searchMetadata(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final ToolRequest toolRequest) {
        if (!toolRequest.getSchema().isEmpty() && toolRequest.getDatabase().isEmpty()) {
            return ToolDispatchResult.error(MCPErrorCode.INVALID_REQUEST, "Schema cannot be provided without database.");
        }
        List<MetadataObject> result = new LinkedList<>();
        if (toolRequest.getDatabase().isEmpty()) {
            MetadataQueryResult databaseResult = metadataQueryService.queryDatabases(databaseMetadataSnapshots, each -> true);
            for (MetadataObject each : databaseResult.getMetadataObjects()) {
                result.addAll(readSearchResults(databaseMetadataSnapshots, each.getDatabase(), toolRequest));
            }
        } else {
            result.addAll(readSearchResults(databaseMetadataSnapshots, toolRequest.getDatabase(), toolRequest));
        }
        return paginate(result, toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
    }
    
    private List<MetadataObject> readSearchResults(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final String databaseName, final ToolRequest toolRequest) {
        List<MetadataObject> result = new LinkedList<>();
        for (MetadataObjectType each : getSearchObjectTypes(toolRequest.getObjectTypes())) {
            MetadataQueryResult metadataResult = MetadataObjectType.DATABASE == each
                    ? metadataQueryService.queryDatabases(databaseMetadataSnapshots, unused -> true)
                    : metadataQueryService.queryMetadataObjects(databaseMetadataSnapshots, databaseName, each,
                            object -> toolRequest.getSchema().isEmpty() || toolRequest.getSchema().equals(object.getSchema()));
            if (!metadataResult.isSuccessful()) {
                continue;
            }
            result.addAll(metadataResult.getMetadataObjects());
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
    
    private boolean matchesSearch(final MetadataObject metadataObject, final String query) {
        return query.isEmpty()
                || metadataObject.getName().toLowerCase().contains(query.toLowerCase())
                || metadataObject.getParentObjectName().toLowerCase().contains(query.toLowerCase());
    }
    
    private ToolDispatchResult describeObject(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final ToolRequest toolRequest, final MetadataObjectType objectType) {
        if (toolRequest.getDatabase().isEmpty() || toolRequest.getSchema().isEmpty() || toolRequest.getObjectName().isEmpty()) {
            return ToolDispatchResult.error(MCPErrorCode.INVALID_REQUEST, "Database, schema, and object name are required.");
        }
        MetadataQueryResult objectResult = metadataQueryService.queryMetadataObjects(databaseMetadataSnapshots, toolRequest.getDatabase(), objectType,
                each -> toolRequest.getSchema().equals(each.getSchema()) && toolRequest.getObjectName().equals(each.getName()));
        if (!objectResult.isSuccessful()) {
            return ToolDispatchResult.fromMetadataResult(objectResult);
        }
        List<MetadataObject> result = new LinkedList<>(objectResult.getMetadataObjects());
        MetadataQueryResult columnResult = metadataQueryService.queryMetadataObjects(databaseMetadataSnapshots, toolRequest.getDatabase(), MetadataObjectType.COLUMN,
                each -> toolRequest.getSchema().equals(each.getSchema())
                        && objectType.name().equals(each.getParentObjectType())
                        && toolRequest.getObjectName().equals(each.getParentObjectName()));
        if (columnResult.isSuccessful()) {
            result.addAll(columnResult.getMetadataObjects());
        }
        return ToolDispatchResult.success(result, "");
    }
    
    private ToolDispatchResult paginate(final MetadataQueryResult metadataResult, final String query, final int pageSize, final String pageToken) {
        if (!metadataResult.isSuccessful()) {
            return ToolDispatchResult.fromMetadataResult(metadataResult);
        }
        return paginate(metadataResult.getMetadataObjects(), query, pageSize, pageToken);
    }
    
    private ToolDispatchResult paginate(final Collection<MetadataObject> metadataObjects, final String query, final int pageSize, final String pageToken) {
        int actualOffset;
        try {
            actualOffset = pageToken.isEmpty() ? 0 : Integer.parseInt(pageToken);
        } catch (final NumberFormatException ignored) {
            return ToolDispatchResult.error(MCPErrorCode.INVALID_REQUEST, "Invalid page token.");
        }
        int actualPageSize = 0 < pageSize ? pageSize : 100;
        List<MetadataObject> filteredObjects = filterByQuery(metadataObjects, query);
        if (actualOffset > filteredObjects.size()) {
            return ToolDispatchResult.success(Collections.emptyList(), "");
        }
        int actualEndIndex = Math.min(actualOffset + actualPageSize, filteredObjects.size());
        String nextPageToken = actualEndIndex < filteredObjects.size() ? String.valueOf(actualEndIndex) : "";
        return ToolDispatchResult.success(filteredObjects.subList(actualOffset, actualEndIndex), nextPageToken);
    }
    
    private List<MetadataObject> filterByQuery(final Collection<MetadataObject> metadataObjects, final String query) {
        List<MetadataObject> result = new LinkedList<>();
        for (MetadataObject each : metadataObjects) {
            if (matchesSearch(each, query)) {
                result.add(each);
            }
        }
        return result;
    }
}
