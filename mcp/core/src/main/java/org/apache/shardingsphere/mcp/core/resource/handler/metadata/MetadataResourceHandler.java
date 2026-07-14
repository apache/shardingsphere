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

package org.apache.shardingsphere.mcp.core.resource.handler.metadata;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseRequestContext;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.ShardingSphereMCPResourceMetadata;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Metadata resource handler backed by one metadata loader function.
 */
@RequiredArgsConstructor
public final class MetadataResourceHandler implements MCPResourceHandler<MCPDatabaseRequestContext> {
    
    private final String uriTemplate;
    
    private final BiFunction<MCPDatabaseRequestContext, MCPUriVariables, List<?>> metadataLoader;
    
    @Override
    public Class<MCPDatabaseRequestContext> getContextType() {
        return MCPDatabaseRequestContext.class;
    }
    
    @Override
    public String getResourceUriTemplate() {
        return uriTemplate;
    }
    
    @Override
    public MCPResponse handle(final MCPDatabaseRequestContext databaseContext, final MCPUriVariables uriVariables) {
        List<?> items = metadataLoader.apply(databaseContext, uriVariables);
        MCPResourceDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredResourceDescriptor(uriTemplate);
        ShardingSphereMCPResourceMetadata metadata = MCPDescriptorCatalogIndex.getRequiredShardingSphereResourceMetadata(descriptor.getUriTemplate());
        boolean detailResource = "detail".equals(metadata.getResourceKind());
        List<?> payloadItems = new MetadataResourcePayloadMapper(databaseContext.getMetadataQueryFacade(), uriVariables, detailResource).map(metadata, items);
        return new MetadataResourceResponseFactory().create(databaseContext, uriVariables, descriptor, metadata, payloadItems);
    }
    
}
