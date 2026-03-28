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

package org.apache.shardingsphere.mcp.bootstrap.transport.resource;

import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportPayloadBuilder;
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.apache.shardingsphere.mcp.resource.ResourceLoadResult;
import org.apache.shardingsphere.mcp.resource.ResourceRequest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class MCPResourcePayloadResolver {
    
    private static final String RESOURCE_SCHEME_PREFIX = "shardingsphere://";
    
    private static final String RESOURCE_CAPABILITIES = "shardingsphere://capabilities";
    
    private static final String RESOURCE_DATABASES = "shardingsphere://databases";
    
    private final MCPRuntimeContext runtimeContext;
    
    private final MCPTransportPayloadBuilder payloadBuilder;
    
    MCPResourcePayloadResolver(final MCPRuntimeContext runtimeContext, final MCPTransportPayloadBuilder payloadBuilder) {
        this.runtimeContext = runtimeContext;
        this.payloadBuilder = payloadBuilder;
    }
    
    Object resolve(final String resourceUri) {
        if (RESOURCE_CAPABILITIES.equals(resourceUri)) {
            return runtimeContext.getCapabilityAssembler().assembleServiceCapability();
        }
        if (RESOURCE_DATABASES.equals(resourceUri)) {
            return toResourcePayload(runtimeContext.getMetadataResourceLoader().load(runtimeContext.getMetadataCatalog(), new ResourceRequest("", "", MetadataObjectType.DATABASE, "", "", "")));
        }
        List<String> segments = splitResourceUri(resourceUri);
        if (segments.size() >= 3 && "databases".equals(segments.get(0)) && "capabilities".equals(segments.get(2))) {
            return resolveDatabaseCapabilityPayload(segments.get(1));
        }
        Optional<ResourceRequest> resourceRequest = createMetadataResourceRequest(segments);
        return resourceRequest.map(optional -> toResourcePayload(runtimeContext.getMetadataResourceLoader().load(runtimeContext.getMetadataCatalog(), optional)))
                .orElseGet(() -> payloadBuilder.createErrorPayload("invalid_request", "Unsupported resource URI."));
    }
    
    private Object resolveDatabaseCapabilityPayload(final String database) {
        Optional<DatabaseCapability> capability = runtimeContext.getCapabilityAssembler().assembleDatabaseCapability(database);
        return capability.map(payloadBuilder::createDatabaseCapabilityPayload).orElseGet(() -> payloadBuilder.createErrorPayload("not_found", "Database capability does not exist."));
    }
    
    private Object toResourcePayload(final ResourceLoadResult loadResult) {
        if (!loadResult.isSuccessful()) {
            return payloadBuilder.createErrorPayload(payloadBuilder.toDomainErrorCode(loadResult.getErrorCode().orElse(ErrorCode.INVALID_REQUEST)), loadResult.getMessage());
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("items", loadResult.getMetadataObjects());
        return payload;
    }
    
    private Optional<ResourceRequest> createMetadataResourceRequest(final List<String> segments) {
        if (segments.isEmpty() || !"databases".equals(segments.get(0))) {
            return Optional.empty();
        }
        if (1 == segments.size()) {
            return Optional.of(new ResourceRequest("", "", MetadataObjectType.DATABASE, "", "", ""));
        }
        String database = segments.get(1);
        if (2 == segments.size()) {
            return Optional.of(new ResourceRequest("", "", MetadataObjectType.DATABASE, database, "", ""));
        }
        if (!"schemas".equals(segments.get(2))) {
            return Optional.empty();
        }
        return createSchemaResourceRequest(database, segments);
    }
    
    private Optional<ResourceRequest> createSchemaResourceRequest(final String database, final List<String> segments) {
        if (3 == segments.size()) {
            return Optional.of(new ResourceRequest(database, "", MetadataObjectType.SCHEMA, "", "", ""));
        }
        String schema = segments.get(3);
        if (4 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.SCHEMA, schema, "", ""));
        }
        if ("tables".equals(segments.get(4))) {
            return createTableResourceRequest(database, schema, segments);
        }
        if ("views".equals(segments.get(4))) {
            return createViewResourceRequest(database, schema, segments);
        }
        return Optional.empty();
    }
    
    private Optional<ResourceRequest> createTableResourceRequest(final String database, final String schema, final List<String> segments) {
        if (5 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.TABLE, "", "", ""));
        }
        String table = segments.get(5);
        if (6 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.TABLE, table, "", ""));
        }
        if ("columns".equals(segments.get(6))) {
            return createColumnResourceRequest(database, schema, table, "TABLE", segments);
        }
        if ("indexes".equals(segments.get(6))) {
            return createIndexResourceRequest(database, schema, table, segments);
        }
        return Optional.empty();
    }
    
    private Optional<ResourceRequest> createViewResourceRequest(final String database, final String schema, final List<String> segments) {
        if (5 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.VIEW, "", "", ""));
        }
        String view = segments.get(5);
        if (6 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.VIEW, view, "", ""));
        }
        if (!"columns".equals(segments.get(6))) {
            return Optional.empty();
        }
        return createColumnResourceRequest(database, schema, view, "VIEW", segments);
    }
    
    private Optional<ResourceRequest> createColumnResourceRequest(final String database, final String schema, final String parentObjectName,
                                                                  final String parentObjectType, final List<String> segments) {
        if (7 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.COLUMN, "", parentObjectType, parentObjectName));
        }
        if (8 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.COLUMN, segments.get(7), parentObjectType, parentObjectName));
        }
        return Optional.empty();
    }
    
    private Optional<ResourceRequest> createIndexResourceRequest(final String database, final String schema, final String table, final List<String> segments) {
        if (7 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.INDEX, "", "TABLE", table));
        }
        if (8 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.INDEX, segments.get(7), "TABLE", table));
        }
        return Optional.empty();
    }
    
    private List<String> splitResourceUri(final String resourceUri) {
        if (!resourceUri.startsWith(RESOURCE_SCHEME_PREFIX)) {
            return Collections.emptyList();
        }
        String actualUri = resourceUri.substring(RESOURCE_SCHEME_PREFIX.length());
        return actualUri.isEmpty() ? Collections.emptyList() : List.of(actualUri.split("/"));
    }
}
