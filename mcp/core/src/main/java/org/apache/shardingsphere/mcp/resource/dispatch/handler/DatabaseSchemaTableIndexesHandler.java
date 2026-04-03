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
import org.apache.shardingsphere.mcp.resource.ResourceReadPlan;
import org.apache.shardingsphere.mcp.resource.dispatch.ResourceHandler;
import org.apache.shardingsphere.mcp.resource.dispatch.ResourceUriMatch;

/**
 * Handler for database schema table indexes resource URI.
 */
public final class DatabaseSchemaTableIndexesHandler implements ResourceHandler {
    
    private static final String URI_TEMPLATE = "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes";
    
    @Override
    public String getUriTemplate() {
        return URI_TEMPLATE;
    }
    
    @Override
    public ResourceReadPlan handle(final ResourceUriMatch uriMatch) {
        return ResourceReadPlan.metadata(new MetadataResourceQuery(uriMatch.getVariable("database"), uriMatch.getVariable("schema"),
                MetadataObjectType.INDEX, "", "TABLE", uriMatch.getVariable("table")));
    }
}
