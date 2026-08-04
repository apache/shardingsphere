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

package org.apache.shardingsphere.mcp.core.context;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.MCPSQLExecutionFacade;
import org.apache.shardingsphere.mcp.core.workflow.WorkflowProxyQueryService;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.context.RequestScopedMetadataContext;
import org.apache.shardingsphere.mcp.support.database.metadata.query.MetadataQueryService;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureCapabilityFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;

import java.util.Map;
import java.util.Optional;

/**
 * MCP feature runtime request context.
 */
@Getter
public final class MCPFeatureRuntimeRequestContext implements MCPFeatureRequestContext {
    
    private final MCPSessionIdentity sessionIdentity;
    
    private final MCPTransportType activeTransport;
    
    @Getter(AccessLevel.NONE)
    private final MCPDatabaseCapabilityProvider databaseCapabilityProvider;
    
    @Getter(AccessLevel.NONE)
    private final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases;
    
    private final WorkflowSessionContext workflowSessionContext;
    
    private final MCPMetadataQueryFacade metadataQueryFacade;
    
    private final MCPFeatureExecutionFacade executionFacade;
    
    private final MCPFeatureQueryFacade queryFacade;
    
    public MCPFeatureRuntimeRequestContext(final MCPRuntimeContext runtimeContext, final MCPSessionIdentity sessionIdentity) {
        this.sessionIdentity = sessionIdentity;
        activeTransport = runtimeContext.getActiveTransport();
        databaseCapabilityProvider = runtimeContext.getDatabaseCapabilityProvider();
        MCPSessionManager sessionManager = runtimeContext.getSessionManager();
        runtimeDatabases = sessionManager.getTransactionResourceManager().getRuntimeDatabases();
        workflowSessionContext = runtimeContext.getWorkflowSessionContext(sessionIdentity.getSessionId());
        metadataQueryFacade = new MetadataQueryService(databaseCapabilityProvider, new RequestScopedMetadataContext(runtimeDatabases, databaseCapabilityProvider));
        executionFacade = new MCPSQLExecutionFacade(databaseCapabilityProvider, sessionManager);
        queryFacade = new WorkflowProxyQueryService(sessionManager, databaseCapabilityProvider);
    }
    
    @Override
    public MCPFeatureCapabilityFacade getCapabilityFacade() {
        return databaseCapabilityProvider;
    }
    
    @Override
    public Optional<RuntimeDatabaseConfiguration> findRuntimeDatabaseConfiguration(final String databaseName) {
        return Optional.ofNullable(runtimeDatabases.get(databaseName));
    }
    
}
