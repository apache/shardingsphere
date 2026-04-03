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

package org.apache.shardingsphere.mcp.resource.dispatch.handler;

import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.resource.MetadataResourceQuery;
import org.apache.shardingsphere.mcp.resource.ResourceHandlerContext;
import org.apache.shardingsphere.mcp.resource.ResourceHandlerResult;
import org.apache.shardingsphere.mcp.resource.dispatch.ResourceHandler;
import org.apache.shardingsphere.mcp.uri.MCPUriVariables;

/**
 * Handler for database schema table column resource URI.
 */
public final class DatabaseSchemaTableColumnHandler implements ResourceHandler {
    
    @Override
    public String getUriPattern() {
        return "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}";
    }
    
    @Override
    public ResourceHandlerResult handle(final ResourceHandlerContext resourceHandlerContext, final MCPUriVariables uriVariables) {
        return ResourceHandlerResult.metadata(resourceHandlerContext.readMetadata(new MetadataResourceQuery(
                uriVariables.getRequired("database"), uriVariables.getRequired("schema"), MetadataObjectType.COLUMN,
                uriVariables.getRequired("column"), "TABLE", uriVariables.getRequired("table"))));
    }
}
