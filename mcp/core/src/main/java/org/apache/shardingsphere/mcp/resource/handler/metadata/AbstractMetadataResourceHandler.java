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
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.metadata.query.MetadataQueryService;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandler;
import org.apache.shardingsphere.mcp.resource.response.MCPResourceResponse;
import org.apache.shardingsphere.mcp.resource.response.MCPResourceResponseFactory;

import java.util.function.Predicate;

abstract class AbstractMetadataResourceHandler implements ResourceHandler {
    
    private final MetadataQueryService metadataQueryService = new MetadataQueryService();
    
    protected final MCPResourceResponse queryDatabases(final MCPRuntimeContext runtimeContext, final Predicate<String> predicate) {
        return MCPResourceResponseFactory.fromMetadataQueryResult(
                metadataQueryService.queryDatabases(runtimeContext.getDatabaseMetadataSnapshots(), predicate));
    }
    
    protected final MCPResourceResponse queryMetadataObjects(final MCPRuntimeContext runtimeContext, final String databaseName,
                                                             final MetadataObjectType objectType, final Predicate<MetadataObject> predicate) {
        return MCPResourceResponseFactory.fromMetadataQueryResult(
                metadataQueryService.queryMetadataObjects(runtimeContext.getDatabaseMetadataSnapshots(), databaseName, objectType, predicate));
    }
}
