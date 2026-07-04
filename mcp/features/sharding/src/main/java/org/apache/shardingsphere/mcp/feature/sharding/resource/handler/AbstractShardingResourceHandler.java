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

package org.apache.shardingsphere.mcp.feature.sharding.resource.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.feature.sharding.tool.service.ShardingInspectionService;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPResourceNavigationPayloadBuilder;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPItemsResponse;

import java.util.List;
import java.util.Map;

abstract class AbstractShardingResourceHandler implements MCPResourceHandler<MCPDatabaseHandlerContext> {
    
    private final String resourceUriTemplate;
    
    private final ShardingInspectionService inspectionService;
    
    AbstractShardingResourceHandler(final String resourceUriTemplate, final ShardingInspectionService inspectionService) {
        this.resourceUriTemplate = resourceUriTemplate;
        this.inspectionService = inspectionService;
    }
    
    @Override
    public Class<MCPDatabaseHandlerContext> getContextType() {
        return MCPDatabaseHandlerContext.class;
    }
    
    @Override
    public String getResourceUriOrTemplate() {
        return resourceUriTemplate;
    }
    
    @Override
    public MCPResponse handle(final MCPDatabaseHandlerContext databaseContext, final MCPUriVariables uriVariables) {
        return new MCPItemsResponse(query(databaseContext, uriVariables),
                MCPResourceNavigationPayloadBuilder.create(MCPDescriptorCatalogIndex.getRequiredResourceDescriptor(getResourceUriOrTemplate()), uriVariables));
    }
    
    protected ShardingInspectionService getInspectionService() {
        return inspectionService;
    }
    
    protected abstract List<Map<String, Object>> query(MCPDatabaseHandlerContext databaseContext, MCPUriVariables uriVariables);
}
