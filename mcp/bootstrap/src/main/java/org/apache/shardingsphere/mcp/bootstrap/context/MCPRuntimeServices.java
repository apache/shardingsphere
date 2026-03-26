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
import org.apache.shardingsphere.mcp.bootstrap.server.MCPServerRegistry;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler;
import org.apache.shardingsphere.mcp.capability.ServiceCapability;
import org.apache.shardingsphere.mcp.execute.ExecuteQueryFacade;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.execute.StatementClassifier;
import org.apache.shardingsphere.mcp.metadata.MetadataRefreshCoordinator;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.session.TransactionCommandExecutor;
import org.apache.shardingsphere.mcp.tool.MetadataToolDispatcher;

import java.util.Objects;

/**
 * Assemble and expose MCP runtime services, resources, and tools.
 */
@Getter
public final class MCPRuntimeServices {
    
    private final DatabaseCapabilityAssembler capabilityAssembler;
    
    private final MetadataResourceLoader metadataResourceLoader;
    
    private final MetadataToolDispatcher metadataToolDispatcher;
    
    private final TransactionCommandExecutor transactionCommandExecutor;
    
    private final AuditRecorder auditRecorder;
    
    private final MetadataRefreshCoordinator metadataRefreshCoordinator;
    
    private final ExecuteQueryFacade executeQueryFacade;
    
    /**
     * Construct runtime services using one shared session manager and runtime inputs.
     *
     * @param sessionManager session manager
     * @param metadataCatalog metadata catalog
     * @param databaseRuntime database runtime
     */
    public MCPRuntimeServices(final MCPSessionManager sessionManager, final MetadataCatalog metadataCatalog, final DatabaseRuntime databaseRuntime) {
        capabilityAssembler = new DatabaseCapabilityAssembler(metadataCatalog);
        metadataResourceLoader = new MetadataResourceLoader(capabilityAssembler);
        metadataToolDispatcher = new MetadataToolDispatcher(metadataResourceLoader);
        transactionCommandExecutor = new TransactionCommandExecutor(capabilityAssembler, sessionManager, databaseRuntime);
        auditRecorder = new AuditRecorder();
        metadataRefreshCoordinator = new MetadataRefreshCoordinator();
        executeQueryFacade = new ExecuteQueryFacade(new StatementClassifier(), capabilityAssembler, transactionCommandExecutor, auditRecorder, metadataRefreshCoordinator);
    }
    
    /**
     * Register the full public MCP surface with the server registry.
     *
     * @param serverRegistry server registry
     */
    public void registerDefaults(final MCPServerRegistry serverRegistry) {
        MCPServerRegistry actualServerRegistry = Objects.requireNonNull(serverRegistry, "serverRegistry cannot be null");
        ServiceCapability serviceCapability = capabilityAssembler.assembleServiceCapability();
        for (String each : serviceCapability.getSupportedResources()) {
            actualServerRegistry.registerResource(each);
        }
        for (String each : serviceCapability.getSupportedTools()) {
            actualServerRegistry.registerTool(each);
        }
    }
}
