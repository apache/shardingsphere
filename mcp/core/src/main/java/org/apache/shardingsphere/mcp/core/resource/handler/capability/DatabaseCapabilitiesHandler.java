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

package org.apache.shardingsphere.mcp.core.resource.handler.capability;

import org.apache.shardingsphere.mcp.api.protocol.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseRequestContext;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.support.database.payload.MCPDatabaseCapabilityPayload;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;

/**
 * Handler for database capabilities resource URI.
 */
public final class DatabaseCapabilitiesHandler implements MCPResourceHandler<MCPDatabaseRequestContext> {
    
    private static final String URI_PATTERN = "shardingsphere://databases/{database}/capabilities";
    
    @Override
    public Class<MCPDatabaseRequestContext> getContextType() {
        return MCPDatabaseRequestContext.class;
    }
    
    @Override
    public String getResourceUriTemplate() {
        return URI_PATTERN;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPDatabaseRequestContext databaseContext, final MCPUriVariables uriVariables) {
        var databaseCapabilityProvider = databaseContext.getCapabilityFacade();
        String databaseName = uriVariables.getValue("database");
        return databaseCapabilityProvider.provide(databaseName).<MCPSuccessPayload>map(MCPDatabaseCapabilityPayload::new).orElseThrow(DatabaseCapabilityNotFoundException::new);
    }
}
