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

package org.apache.shardingsphere.mcp.resource;

import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Resource URI resolver.
 */
public final class ResourceUriResolver {
    
    private static final String RESOURCE_PREFIX = "shardingsphere://";
    
    private static final List<String> SUPPORTED_RESOURCES = List.of(
            "shardingsphere://capabilities",
            "shardingsphere://databases",
            "shardingsphere://databases/{database}",
            "shardingsphere://databases/{database}/capabilities",
            "shardingsphere://databases/{database}/schemas",
            "shardingsphere://databases/{database}/schemas/{schema}",
            "shardingsphere://databases/{database}/schemas/{schema}/tables",
            "shardingsphere://databases/{database}/schemas/{schema}/views",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}",
            "shardingsphere://databases/{database}/schemas/{schema}/views/{view}",
            "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns",
            "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}");
    
    /**
     * Get supported resource URI surfaces.
     *
     * @return supported resource URI surfaces
     */
    public List<String> getSupportedResources() {
        return SUPPORTED_RESOURCES;
    }
    
    /**
     * Resolve resource URI.
     *
     * @param resourceUri resource URI
     * @return resolved resource contract when supported
     */
    public Optional<ResourceUriResolution> resolve(final String resourceUri) {
        if ("shardingsphere://capabilities".equals(resourceUri)) {
            return Optional.of(ResourceUriResolution.serviceCapabilities());
        }
        List<String> segments = splitResourceUri(resourceUri);
        if (segments.isEmpty() || !"databases".equals(segments.get(0))) {
            return Optional.empty();
        }
        if (1 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest("", "", MetadataObjectType.DATABASE, "", "", "")));
        }
        if (segments.size() >= 3 && "capabilities".equals(segments.get(2))) {
            return 3 == segments.size() ? Optional.of(ResourceUriResolution.databaseCapabilities(segments.get(1))) : Optional.empty();
        }
        return createMetadataResourceResolution(segments);
    }
    
    private List<String> splitResourceUri(final String resourceUri) {
        if (null == resourceUri || !resourceUri.startsWith(RESOURCE_PREFIX)) {
            return Collections.emptyList();
        }
        String actualUri = resourceUri.substring(RESOURCE_PREFIX.length());
        return actualUri.isEmpty() ? Collections.emptyList() : List.of(actualUri.split("/"));
    }
    
    private Optional<ResourceUriResolution> createMetadataResourceResolution(final List<String> segments) {
        String database = segments.get(1);
        if (2 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest("", "", MetadataObjectType.DATABASE, database, "", "")));
        }
        if (!"schemas".equals(segments.get(2))) {
            return Optional.empty();
        }
        return createSchemaResourceResolution(database, segments);
    }
    
    private Optional<ResourceUriResolution> createSchemaResourceResolution(final String databaseName, final List<String> segments) {
        if (3 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(databaseName, "", MetadataObjectType.SCHEMA, "", "", "")));
        }
        String schemaName = segments.get(3);
        if (4 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(databaseName, schemaName, MetadataObjectType.SCHEMA, schemaName, "", "")));
        }
        if ("tables".equals(segments.get(4))) {
            return createTableResourceResolution(databaseName, schemaName, segments);
        }
        if ("views".equals(segments.get(4))) {
            return createViewResourceResolution(databaseName, schemaName, segments);
        }
        return Optional.empty();
    }
    
    private Optional<ResourceUriResolution> createTableResourceResolution(final String databaseName, final String schemaName, final List<String> segments) {
        if (5 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(databaseName, schemaName, MetadataObjectType.TABLE, "", "", "")));
        }
        String tableName = segments.get(5);
        if (6 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(databaseName, schemaName, MetadataObjectType.TABLE, tableName, "", "")));
        }
        if ("columns".equals(segments.get(6))) {
            return createColumnResourceResolution(databaseName, schemaName, tableName, "TABLE", segments);
        }
        if ("indexes".equals(segments.get(6))) {
            return createIndexResourceResolution(databaseName, schemaName, tableName, segments);
        }
        return Optional.empty();
    }
    
    private Optional<ResourceUriResolution> createViewResourceResolution(final String databaseName, final String schemaName, final List<String> segments) {
        if (5 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(databaseName, schemaName, MetadataObjectType.VIEW, "", "", "")));
        }
        String view = segments.get(5);
        if (6 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(databaseName, schemaName, MetadataObjectType.VIEW, view, "", "")));
        }
        if (!"columns".equals(segments.get(6))) {
            return Optional.empty();
        }
        return createColumnResourceResolution(databaseName, schemaName, view, "VIEW", segments);
    }
    
    private Optional<ResourceUriResolution> createColumnResourceResolution(final String databaseName, final String schemaName, final String parentObjectName,
                                                                           final String parentObjectType, final List<String> segments) {
        if (7 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(databaseName, schemaName, MetadataObjectType.COLUMN, "", parentObjectType, parentObjectName)));
        }
        if (8 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(databaseName, schemaName, MetadataObjectType.COLUMN, segments.get(7), parentObjectType, parentObjectName)));
        }
        return Optional.empty();
    }
    
    private Optional<ResourceUriResolution> createIndexResourceResolution(final String databaseName, final String schemaName, final String tableName, final List<String> segments) {
        if (7 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(databaseName, schemaName, MetadataObjectType.INDEX, "", "TABLE", tableName)));
        }
        if (8 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(databaseName, schemaName, MetadataObjectType.INDEX, segments.get(7), "TABLE", tableName)));
        }
        return Optional.empty();
    }
}
