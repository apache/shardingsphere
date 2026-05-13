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
import org.apache.shardingsphere.mcp.core.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.core.tool.response.MetadataSearchResult;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Search metadata tool service.
 */
public final class SearchMetadataToolService {
    
    static final int DEFAULT_PAGE_SIZE = 50;
    
    static final int MAX_PAGE_SIZE = 500;
    
    private final MetadataSearchCollector collector;
    
    private final MetadataSearchPaginator paginator = new MetadataSearchPaginator(DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
    
    public SearchMetadataToolService(final MCPMetadataQueryFacade metadataQueryFacade) {
        collector = new MetadataSearchCollector(metadataQueryFacade, new MetadataSearchResourceUriFactory());
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
        return paginator.paginate(collector.collect(request, searchObjectTypes), request, searchObjectTypes, false);
    }
    
    private boolean isBlankAllDatabaseSearch(final MetadataSearchRequest request) {
        return request.getDatabase().isEmpty() && request.getQuery().isEmpty() && request.getObjectTypes().isEmpty();
    }
    
    private MetadataSearchResult executeBlankAllDatabaseSearch(final MetadataSearchRequest request) {
        Set<SupportedMCPMetadataObjectType> searchObjectTypes = Set.of(SupportedMCPMetadataObjectType.DATABASE);
        return paginator.paginate(collector.collectDatabases(), request, searchObjectTypes, true);
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
