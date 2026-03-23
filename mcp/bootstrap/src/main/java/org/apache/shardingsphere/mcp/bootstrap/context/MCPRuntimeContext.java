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

package org.apache.shardingsphere.mcp.bootstrap.context;

import lombok.Getter;
import org.apache.shardingsphere.mcp.audit.AuditRecorder;
import org.apache.shardingsphere.mcp.bootstrap.server.MCPServerContext;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler.ServiceCapability;
import org.apache.shardingsphere.mcp.execute.ExecuteQueryFacade;
import org.apache.shardingsphere.mcp.execute.ExecuteQueryFacade.DatabaseRuntime;
import org.apache.shardingsphere.mcp.execute.StatementClassifier;
import org.apache.shardingsphere.mcp.metadata.MetadataRefreshCoordinator;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.session.TransactionCommandExecutor;
import org.apache.shardingsphere.mcp.tool.MetadataToolDispatcher;

import java.util.Objects;

/**
 * Register and expose MCP resources, tools, and runtime facades.
 */
@Getter
public final class MCPRuntimeContext {
    
    private final DatabaseCapabilityAssembler capabilityAssembler;
    
    private final MetadataResourceLoader metadataResourceLoader;
    
    private final MetadataToolDispatcher metadataToolDispatcher;
    
    private final TransactionCommandExecutor transactionCommandExecutor;
    
    private final AuditRecorder auditRecorder;
    
    private final MetadataRefreshCoordinator metadataRefreshCoordinator;
    
    private final ExecuteQueryFacade executeQueryFacade;
    
    /**
     * Construct a runtime context using one shared session manager.
     *
     * @param sessionManager session manager
     */
    public MCPRuntimeContext(final MCPSessionManager sessionManager) {
        this(sessionManager, new MetadataCatalog(java.util.Collections.emptyMap(), java.util.Collections.emptyList()),
                new DatabaseRuntime(java.util.Collections.emptyMap(), java.util.Collections.emptyMap()));
    }
    
    /**
     * Construct a runtime context using one shared session manager and runtime inputs.
     *
     * @param sessionManager session manager
     * @param metadataCatalog metadata catalog
     * @param databaseRuntime database runtime
     */
    public MCPRuntimeContext(final MCPSessionManager sessionManager, final MetadataCatalog metadataCatalog, final DatabaseRuntime databaseRuntime) {
        capabilityAssembler = new DatabaseCapabilityAssembler(Objects.requireNonNull(metadataCatalog, "metadataCatalog cannot be null"));
        metadataResourceLoader = new MetadataResourceLoader(capabilityAssembler);
        metadataToolDispatcher = new MetadataToolDispatcher(metadataResourceLoader);
        transactionCommandExecutor = new TransactionCommandExecutor(capabilityAssembler, Objects.requireNonNull(sessionManager, "sessionManager cannot be null"),
                Objects.requireNonNull(databaseRuntime, "databaseRuntime cannot be null"));
        auditRecorder = new AuditRecorder();
        metadataRefreshCoordinator = new MetadataRefreshCoordinator();
        executeQueryFacade = new ExecuteQueryFacade(new StatementClassifier(), capabilityAssembler, transactionCommandExecutor, auditRecorder, metadataRefreshCoordinator);
    }
    
    /**
     * Register the full public MCP surface with the server context.
     *
     * @param serverContext server context
     */
    public void registerDefaults(final MCPServerContext serverContext) {
        MCPServerContext actualServerContext = Objects.requireNonNull(serverContext, "serverContext cannot be null");
        ServiceCapability serviceCapability = capabilityAssembler.assembleServiceCapability();
        for (String each : serviceCapability.getSupportedResources()) {
            actualServerContext.registerResource(each);
        }
        for (String each : serviceCapability.getSupportedTools()) {
            actualServerContext.registerTool(each);
        }
    }
}
