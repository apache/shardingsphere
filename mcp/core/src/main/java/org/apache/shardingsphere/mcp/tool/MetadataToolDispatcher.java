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

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse.ErrorCode;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.MetadataObject;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.MetadataObjectType;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.ResourceLoadResult;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.ResourceRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Dispatch metadata discovery tools on top of the normalized metadata resource loader.
 */
public final class MetadataToolDispatcher {
    
    private final MetadataResourceLoader resourceLoader;
    
    /**
     * Construct a dispatcher with the default metadata resource loader.
     */
    public MetadataToolDispatcher() {
        this(new MetadataResourceLoader());
    }
    
    /**
     * Construct a dispatcher with a caller-provided metadata resource loader.
     *
     * @param resourceLoader metadata resource loader
     */
    public MetadataToolDispatcher(final MetadataResourceLoader resourceLoader) {
        this.resourceLoader = Objects.requireNonNull(resourceLoader, "resourceLoader cannot be null");
    }
    
    /**
     * Dispatch one metadata tool request.
     *
     * @param metadataCatalog metadata catalog
     * @param toolRequest tool request
     * @return tool dispatch result
     */
    public ToolDispatchResult dispatch(final MetadataCatalog metadataCatalog, final ToolRequest toolRequest) {
        Objects.requireNonNull(metadataCatalog, "metadataCatalog cannot be null");
        ToolRequest actualToolRequest = Objects.requireNonNull(toolRequest, "toolRequest cannot be null");
        switch (actualToolRequest.getToolName()) {
            case "list_databases":
                return paginate(resourceLoader.load(metadataCatalog, new ResourceRequest("", "", MetadataObjectType.DATABASE, "", "", "")),
                        actualToolRequest.getQuery(), actualToolRequest.getPageSize(), actualToolRequest.getPageToken());
            case "list_schemas":
                return paginate(validateAndLoad(metadataCatalog, actualToolRequest, MetadataObjectType.SCHEMA, "", true, false),
                        actualToolRequest.getQuery(), actualToolRequest.getPageSize(), actualToolRequest.getPageToken());
            case "list_tables":
                return paginate(validateAndLoad(metadataCatalog, actualToolRequest, MetadataObjectType.TABLE, "", true, true),
                        actualToolRequest.getQuery(), actualToolRequest.getPageSize(), actualToolRequest.getPageToken());
            case "list_views":
                return paginate(validateAndLoad(metadataCatalog, actualToolRequest, MetadataObjectType.VIEW, "", true, true),
                        actualToolRequest.getQuery(), actualToolRequest.getPageSize(), actualToolRequest.getPageToken());
            case "list_columns":
                if (actualToolRequest.getObjectName().isEmpty() || actualToolRequest.getParentObjectType().isEmpty()) {
                    return ToolDispatchResult.error(ErrorCode.INVALID_REQUEST, "Parent object type and object name are required.");
                }
                return paginate(validateAndLoad(metadataCatalog, actualToolRequest, MetadataObjectType.COLUMN, actualToolRequest.getParentObjectType(), true, true),
                        actualToolRequest.getQuery(), actualToolRequest.getPageSize(), actualToolRequest.getPageToken());
            case "list_indexes":
                if (actualToolRequest.getObjectName().isEmpty()) {
                    return ToolDispatchResult.error(ErrorCode.INVALID_REQUEST, "Table name is required.");
                }
                return paginate(validateAndLoad(metadataCatalog, actualToolRequest, MetadataObjectType.INDEX, "TABLE", true, true),
                        actualToolRequest.getQuery(), actualToolRequest.getPageSize(), actualToolRequest.getPageToken());
            case "search_metadata":
                return searchMetadata(metadataCatalog, actualToolRequest);
            case "describe_table":
                return describeObject(metadataCatalog, actualToolRequest, MetadataObjectType.TABLE);
            case "describe_view":
                return describeObject(metadataCatalog, actualToolRequest, MetadataObjectType.VIEW);
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
                result.addAll(filterSearchResults(metadataCatalog, each.getDatabase(), toolRequest));
            }
        } else {
            result.addAll(filterSearchResults(metadataCatalog, toolRequest.getDatabase(), toolRequest));
        }
        return paginate(result, toolRequest.getQuery(), toolRequest.getPageSize(), toolRequest.getPageToken());
    }
    
    private List<MetadataObject> filterSearchResults(final MetadataCatalog metadataCatalog, final String database, final ToolRequest toolRequest) {
        List<MetadataObject> result = new LinkedList<>();
        for (MetadataObjectType each : getSearchObjectTypes(toolRequest.getObjectTypes())) {
            ResourceLoadResult loadResult = resourceLoader.load(metadataCatalog, new ResourceRequest(database, toolRequest.getSchema(), each, "", "", ""));
            if (!loadResult.isSuccessful()) {
                continue;
            }
            for (MetadataObject metadataObject : loadResult.getMetadataObjects()) {
                if (matchesSearch(metadataObject, toolRequest.getQuery())) {
                    result.add(metadataObject);
                }
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
    
    /**
     * Tool request contract for metadata discovery.
     */
    @Getter
    public static final class ToolRequest {
        
        private final String toolName;
        
        private final String database;
        
        private final String schema;
        
        private final String objectName;
        
        private final String parentObjectType;
        
        private final String query;
        
        private final Set<MetadataObjectType> objectTypes;
        
        private final int pageSize;
        
        private final String pageToken;
        
        /**
         * Construct a metadata tool request.
         *
         * @param toolName tool identifier
         * @param database logical database name or empty string
         * @param schema schema name or empty string
         * @param objectName object name or parent object name depending on the tool
         * @param parentObjectType parent object type name or empty string
         * @param query search query or empty string
         * @param objectTypes object type filter for search or empty set
         * @param pageSize requested page size
         * @param pageToken requested page token or empty string
         */
        public ToolRequest(final String toolName, final String database, final String schema, final String objectName,
                           final String parentObjectType, final String query, final Set<MetadataObjectType> objectTypes,
                           final int pageSize, final String pageToken) {
            this.toolName = Objects.requireNonNull(toolName, "toolName cannot be null");
            this.database = Objects.requireNonNull(database, "database cannot be null");
            this.schema = Objects.requireNonNull(schema, "schema cannot be null");
            this.objectName = Objects.requireNonNull(objectName, "objectName cannot be null");
            this.parentObjectType = Objects.requireNonNull(parentObjectType, "parentObjectType cannot be null");
            this.query = Objects.requireNonNull(query, "query cannot be null");
            this.objectTypes = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(objectTypes, "objectTypes cannot be null")));
            this.pageSize = pageSize;
            this.pageToken = Objects.requireNonNull(pageToken, "pageToken cannot be null");
        }
    }
    
    /**
     * Dispatch result for one metadata tool request.
     */
    @Getter
    public static final class ToolDispatchResult {
        
        private final List<MetadataObject> metadataObjects;
        
        private final String nextPageToken;
        
        @Getter(AccessLevel.NONE)
        private final boolean errorCodePresent;
        
        @Getter(AccessLevel.NONE)
        private final ErrorCode errorCode;
        
        private final String message;
        
        private ToolDispatchResult(final Collection<MetadataObject> metadataObjects, final String nextPageToken,
                                   final boolean errorCodePresent, final ErrorCode errorCode, final String message) {
            this.metadataObjects = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(metadataObjects, "metadataObjects cannot be null")));
            this.nextPageToken = Objects.requireNonNull(nextPageToken, "nextPageToken cannot be null");
            this.errorCodePresent = errorCodePresent;
            this.errorCode = Objects.requireNonNull(errorCode, "errorCode cannot be null");
            this.message = Objects.requireNonNull(message, "message cannot be null");
        }
        
        /**
         * Determine whether the tool request finished successfully.
         *
         * @return {@code true} when no error is attached
         */
        public boolean isSuccessful() {
            return !errorCodePresent;
        }
        
        private static ToolDispatchResult success(final Collection<MetadataObject> metadataObjects, final String nextPageToken) {
            return new ToolDispatchResult(metadataObjects, nextPageToken, false, ErrorCode.INVALID_REQUEST, "");
        }
        
        private static ToolDispatchResult error(final ErrorCode errorCode, final String message) {
            return new ToolDispatchResult(Collections.emptyList(), "", true, Objects.requireNonNull(errorCode, "errorCode cannot be null"), message);
        }
        
        private static ToolDispatchResult fromResourceLoadResult(final ResourceLoadResult loadResult) {
            return loadResult.getErrorCode().isPresent()
                    ? error(loadResult.getErrorCode().get(), loadResult.getMessage())
                    : success(loadResult.getMetadataObjects(), "");
        }
        
        /**
         * Get the error code when one exists.
         *
         * @return optional error code
         */
        public Optional<ErrorCode> getErrorCode() {
            return errorCodePresent ? Optional.of(errorCode) : Optional.empty();
        }
    }
}
