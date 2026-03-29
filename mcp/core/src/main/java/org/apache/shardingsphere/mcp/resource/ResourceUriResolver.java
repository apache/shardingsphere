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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Resolve MCP resource URIs into core resource contracts.
 */
public final class ResourceUriResolver {
    
    private static final String RESOURCE_SCHEME_PREFIX = "shardingsphere://";
    
    private static final String RESOURCE_CAPABILITIES = "shardingsphere://capabilities";
    
    private static final List<String> SUPPORTED_RESOURCES = List.of(
            RESOURCE_CAPABILITIES,
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
            "shardingsphere://databases/{database}/schemas/{schema}/views/{view}",
            "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns",
            "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}");
    
    /**
     * Get supported resource URI surfaces.
     *
     * @return supported resource URI surfaces
     */
    public List<String> getSupportedResources() {
        return SUPPORTED_RESOURCES;
    }
    
    /**
     * Resolve one resource URI.
     *
     * @param resourceUri resource URI
     * @return resolved resource contract when supported
     */
    public Optional<ResourceUriResolution> resolve(final String resourceUri) {
        if (RESOURCE_CAPABILITIES.equals(resourceUri)) {
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
    
    private Optional<ResourceUriResolution> createSchemaResourceResolution(final String database, final List<String> segments) {
        if (3 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(database, "", MetadataObjectType.SCHEMA, "", "", "")));
        }
        String schema = segments.get(3);
        if (4 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(database, schema, MetadataObjectType.SCHEMA, schema, "", "")));
        }
        if ("tables".equals(segments.get(4))) {
            return createTableResourceResolution(database, schema, segments);
        }
        if ("views".equals(segments.get(4))) {
            return createViewResourceResolution(database, schema, segments);
        }
        return Optional.empty();
    }
    
    private Optional<ResourceUriResolution> createTableResourceResolution(final String database, final String schema, final List<String> segments) {
        if (5 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(database, schema, MetadataObjectType.TABLE, "", "", "")));
        }
        String table = segments.get(5);
        if (6 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(database, schema, MetadataObjectType.TABLE, table, "", "")));
        }
        if ("columns".equals(segments.get(6))) {
            return createColumnResourceResolution(database, schema, table, "TABLE", segments);
        }
        if ("indexes".equals(segments.get(6))) {
            return createIndexResourceResolution(database, schema, table, segments);
        }
        return Optional.empty();
    }
    
    private Optional<ResourceUriResolution> createViewResourceResolution(final String database, final String schema, final List<String> segments) {
        if (5 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(database, schema, MetadataObjectType.VIEW, "", "", "")));
        }
        String view = segments.get(5);
        if (6 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(database, schema, MetadataObjectType.VIEW, view, "", "")));
        }
        if (!"columns".equals(segments.get(6))) {
            return Optional.empty();
        }
        return createColumnResourceResolution(database, schema, view, "VIEW", segments);
    }
    
    private Optional<ResourceUriResolution> createColumnResourceResolution(final String database, final String schema, final String parentObjectName,
                                                                           final String parentObjectType, final List<String> segments) {
        if (7 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(database, schema, MetadataObjectType.COLUMN, "", parentObjectType, parentObjectName)));
        }
        if (8 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(database, schema, MetadataObjectType.COLUMN, segments.get(7), parentObjectType, parentObjectName)));
        }
        return Optional.empty();
    }
    
    private Optional<ResourceUriResolution> createIndexResourceResolution(final String database, final String schema, final String table, final List<String> segments) {
        if (7 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(database, schema, MetadataObjectType.INDEX, "", "TABLE", table)));
        }
        if (8 == segments.size()) {
            return Optional.of(ResourceUriResolution.metadata(new ResourceRequest(database, schema, MetadataObjectType.INDEX, segments.get(7), "TABLE", table)));
        }
        return Optional.empty();
    }
    
    private List<String> splitResourceUri(final String resourceUri) {
        if (null == resourceUri || !resourceUri.startsWith(RESOURCE_SCHEME_PREFIX)) {
            return Collections.emptyList();
        }
        String actualUri = resourceUri.substring(RESOURCE_SCHEME_PREFIX.length());
        return actualUri.isEmpty() ? Collections.emptyList() : List.of(actualUri.split("/"));
    }
}
