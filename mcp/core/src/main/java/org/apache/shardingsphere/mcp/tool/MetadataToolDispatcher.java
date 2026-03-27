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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.MetadataObject;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader;
import org.apache.shardingsphere.mcp.resource.ResourceLoadResult;
import org.apache.shardingsphere.mcp.resource.ResourceRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Dispatch metadata discovery tools on top of the normalized metadata resource loader.
 */
@RequiredArgsConstructor
public final class MetadataToolDispatcher {
    
    private final MetadataResourceLoader resourceLoader;
    
    /**
     * Dispatch one metadata tool request.
     *
     * @param metadataCatalog metadata catalog
     * @param toolRequest tool request
     * @return tool dispatch result
     */
    public ToolDispatchResult dispatch(final MetadataCatalog metadataCatalog, final ToolRequest toolRequest) {
        switch (toolRequest.getToolName()) {
            case "list_databases":
                return paginate(resourceLoader.load(metadataCatalog, new ResourceRequest("", "", MetadataObjectType.DATABASE, "", "", "")),
                        toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "list_schemas":
                return paginate(validateAndLoad(metadataCatalog, toolRequest, MetadataObjectType.SCHEMA, "", true, false),
                        toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "list_tables":
                return paginate(validateAndLoad(metadataCatalog, toolRequest, MetadataObjectType.TABLE, "", true, true),
                        toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "list_views":
                return paginate(validateAndLoad(metadataCatalog, toolRequest, MetadataObjectType.VIEW, "", true, true),
                        toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "list_columns":
                if (toolRequest.getObjectName().isEmpty() || toolRequest.getParentObjectType().isEmpty()) {
                    return ToolDispatchResult.error(ErrorCode.INVALID_REQUEST, "Parent object type and object name are required.");
                }
                return paginate(validateAndLoad(metadataCatalog, toolRequest, MetadataObjectType.COLUMN, toolRequest.getParentObjectType(), true, true),
                        toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "list_indexes":
                if (toolRequest.getObjectName().isEmpty()) {
                    return ToolDispatchResult.error(ErrorCode.INVALID_REQUEST, "Table name is required.");
                }
                return paginate(validateAndLoad(metadataCatalog, toolRequest, MetadataObjectType.INDEX, "TABLE", true, true),
                        toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
            case "search_metadata":
                return searchMetadata(metadataCatalog, toolRequest);
            case "describe_table":
                return describeObject(metadataCatalog, toolRequest, MetadataObjectType.TABLE);
            case "describe_view":
                return describeObject(metadataCatalog, toolRequest, MetadataObjectType.VIEW);
            default:
                return ToolDispatchResult.error(ErrorCode.INVALID_REQUEST, "Unsupported metadata tool.");
        }
    }
    
    private ResourceLoadResult validateAndLoad(final MetadataCatalog metadataCatalog, final ToolRequest toolRequest, final MetadataObjectType objectType,
                                               final String parentObjectType, final boolean requireDatabase, final boolean requireSchema) {
        if (requireDatabase && toolRequest.getDatabase().isEmpty()) {
            return ResourceLoadResult.error(ErrorCode.INVALID_REQUEST, "Database is required.");
        }
        if (requireSchema && toolRequest.getSchema().isEmpty()) {
            return ResourceLoadResult.error(ErrorCode.INVALID_REQUEST, "Schema is required.");
        }
        return resourceLoader.load(metadataCatalog, new ResourceRequest(toolRequest.getDatabase(), toolRequest.getSchema(), objectType, "",
                parentObjectType, toolRequest.getObjectName()));
    }
    
    private ToolDispatchResult searchMetadata(final MetadataCatalog metadataCatalog, final ToolRequest toolRequest) {
        if (!toolRequest.getSchema().isEmpty() && toolRequest.getDatabase().isEmpty()) {
            return ToolDispatchResult.error(ErrorCode.INVALID_REQUEST, "Schema cannot be provided without database.");
        }
        List<MetadataObject> result = new LinkedList<>();
        if (toolRequest.getDatabase().isEmpty()) {
            ResourceLoadResult databaseResult = resourceLoader.load(metadataCatalog, new ResourceRequest("", "", MetadataObjectType.DATABASE, "", "", ""));
            for (MetadataObject each : databaseResult.getMetadataObjects()) {
                result.addAll(loadSearchResults(metadataCatalog, each.getDatabase(), toolRequest));
            }
        } else {
            result.addAll(loadSearchResults(metadataCatalog, toolRequest.getDatabase(), toolRequest));
        }
        return paginate(result, toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
    }
    
    private List<MetadataObject> loadSearchResults(final MetadataCatalog metadataCatalog, final String database, final ToolRequest toolRequest) {
        List<MetadataObject> result = new LinkedList<>();
        for (MetadataObjectType each : getSearchObjectTypes(toolRequest.getObjectTypes())) {
            ResourceLoadResult loadResult = resourceLoader.load(metadataCatalog, new ResourceRequest(database, toolRequest.getSchema(), each, "", "", ""));
            if (!loadResult.isSuccessful()) {
                continue;
            }
            result.addAll(loadResult.getMetadataObjects());
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
    
    private ToolDispatchResult describeObject(final MetadataCatalog metadataCatalog, final ToolRequest toolRequest, final MetadataObjectType objectType) {
        if (toolRequest.getDatabase().isEmpty() || toolRequest.getSchema().isEmpty() || toolRequest.getObjectName().isEmpty()) {
            return ToolDispatchResult.error(ErrorCode.INVALID_REQUEST, "Database, schema, and object name are required.");
        }
        ResourceLoadResult objectResult = resourceLoader.load(metadataCatalog, new ResourceRequest(toolRequest.getDatabase(), toolRequest.getSchema(),
                objectType, toolRequest.getObjectName(), "", ""));
        if (!objectResult.isSuccessful()) {
            return ToolDispatchResult.fromResourceLoadResult(objectResult);
        }
        List<MetadataObject> result = new ArrayList<>(objectResult.getMetadataObjects());
        ResourceLoadResult columnResult = resourceLoader.load(metadataCatalog, new ResourceRequest(toolRequest.getDatabase(), toolRequest.getSchema(),
                MetadataObjectType.COLUMN, "", objectType.name(), toolRequest.getObjectName()));
        if (columnResult.isSuccessful()) {
            result.addAll(columnResult.getMetadataObjects());
        }
        return ToolDispatchResult.success(result, "");
    }
    
    private ToolDispatchResult paginate(final ResourceLoadResult loadResult, final String query, final int pageSize, final String pageToken) {
        if (!loadResult.isSuccessful()) {
            return ToolDispatchResult.fromResourceLoadResult(loadResult);
        }
        return paginate(loadResult.getMetadataObjects(), query, pageSize, pageToken);
    }
    
    private ToolDispatchResult paginate(final Collection<MetadataObject> metadataObjects, final String query, final int pageSize, final String pageToken) {
        int actualOffset;
        try {
            actualOffset = pageToken.isEmpty() ? 0 : Integer.parseInt(pageToken);
        } catch (final NumberFormatException ignored) {
            return ToolDispatchResult.error(ErrorCode.INVALID_REQUEST, "Invalid page token.");
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
