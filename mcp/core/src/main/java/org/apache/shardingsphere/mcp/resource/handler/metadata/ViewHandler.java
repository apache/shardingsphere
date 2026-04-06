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

package org.apache.shardingsphere.mcp.resource.handler.metadata;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.query.MetadataQueryService;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandler;
import org.apache.shardingsphere.mcp.resource.uri.MCPUriVariables;
import org.apache.shardingsphere.mcp.protocol.response.MCPMetadataResponse;

import java.util.Collections;

/**
 * Handler for view resource URI.
 */
public final class ViewHandler implements ResourceHandler {
    
    @Override
    public String getUriPattern() {
        return "shardingsphere://databases/{database}/schemas/{schema}/views/{view}";
    }
    
    @Override
    public MCPResponse handle(final MCPRuntimeContext runtimeContext, final MCPUriVariables uriVariables) {
        MetadataQueryService metadataQueryService = new MetadataQueryService(runtimeContext.getMetadataCatalog());
        String databaseName = uriVariables.getVariable("database");
        String schemaName = uriVariables.getVariable("schema");
        String viewName = uriVariables.getVariable("view");
        return new MCPMetadataResponse(metadataQueryService.queryView(databaseName, schemaName, viewName).map(Collections::singletonList).orElse(Collections.emptyList()));
    }
}
