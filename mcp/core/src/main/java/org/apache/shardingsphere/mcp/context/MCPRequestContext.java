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

package org.apache.shardingsphere.mcp.context;

import lombok.Getter;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureCapabilityFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.metadata.context.RequestScopedMetadataContext;
import org.apache.shardingsphere.mcp.metadata.query.MetadataQueryService;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.tool.handler.execute.MCPSQLExecutionFacade;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowContextStore;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowProxyQueryService;

/**
 * MCP request context.
 */
@Getter
public final class MCPRequestContext implements MCPFeatureContext {
    
    private final MCPSessionManager sessionManager;
    
    private final MCPDatabaseCapabilityProvider databaseCapabilityProvider;
    
    private final RequestScopedMetadataContext metadataContext;
    
    private final WorkflowContextStore workflowContextStore;
    
    private final MCPMetadataQueryFacade metadataQueryFacade;
    
    private final MCPFeatureExecutionFacade executionFacade;
    
    private final MCPFeatureQueryFacade queryFacade;
    
    public MCPRequestContext(final MCPRuntimeContext runtimeContext) {
        this(runtimeContext.getSessionManager(), runtimeContext.getDatabaseCapabilityProvider(),
                new RequestScopedMetadataContext(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases(), runtimeContext.getDatabaseCapabilityProvider()),
                runtimeContext.getWorkflowContextStore(),
                null, null, null);
    }
    
    public MCPRequestContext(final MCPSessionManager sessionManager, final MCPDatabaseCapabilityProvider databaseCapabilityProvider, final RequestScopedMetadataContext metadataContext,
                             final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureExecutionFacade executionFacade, final MCPFeatureQueryFacade queryFacade) {
        this(sessionManager, databaseCapabilityProvider, metadataContext, new WorkflowContextStore(), metadataQueryFacade, executionFacade, queryFacade);
    }
    
    public MCPRequestContext(final MCPSessionManager sessionManager, final MCPDatabaseCapabilityProvider databaseCapabilityProvider, final RequestScopedMetadataContext metadataContext,
                             final WorkflowContextStore workflowContextStore, final MCPMetadataQueryFacade metadataQueryFacade,
                             final MCPFeatureExecutionFacade executionFacade, final MCPFeatureQueryFacade queryFacade) {
        this.sessionManager = sessionManager;
        this.databaseCapabilityProvider = databaseCapabilityProvider;
        this.metadataContext = metadataContext;
        this.workflowContextStore = workflowContextStore;
        this.metadataQueryFacade = null == metadataQueryFacade ? new MetadataQueryService(this) : metadataQueryFacade;
        this.executionFacade = null == executionFacade ? new MCPSQLExecutionFacade(this) : executionFacade;
        this.queryFacade = null == queryFacade ? new WorkflowProxyQueryService(this) : queryFacade;
    }
    
    @Override
    public MCPFeatureCapabilityFacade getCapabilityFacade() {
        return databaseCapabilityProvider;
    }
    
    @Override
    public void close() {
        metadataContext.close();
    }
}
