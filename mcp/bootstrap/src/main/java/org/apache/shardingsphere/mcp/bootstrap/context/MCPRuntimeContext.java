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

import org.apache.shardingsphere.mcp.audit.AuditRecorder;
import org.apache.shardingsphere.mcp.bootstrap.server.MCPServerContext;
import org.apache.shardingsphere.mcp.bootstrap.server.MCPServerRegistry;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler;
import org.apache.shardingsphere.mcp.execute.ExecuteQueryFacade;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.metadata.MetadataRefreshCoordinator;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.session.TransactionCommandExecutor;
import org.apache.shardingsphere.mcp.tool.MetadataToolDispatcher;

import java.util.Objects;

/**
 * Compatibility wrapper for {@link MCPRuntimeServices}.
 *
 * @deprecated Prefer {@link MCPRuntimeServices}.
 */
@Deprecated
public final class MCPRuntimeContext {
    
    private final MCPRuntimeServices runtimeServices;
    
    /**
     * Construct compatibility runtime services using one shared session manager.
     *
     * @param sessionManager session manager
     */
    public MCPRuntimeContext(final MCPSessionManager sessionManager) {
        runtimeServices = new MCPRuntimeServices(sessionManager);
    }
    
    /**
     * Construct compatibility runtime services using one shared session manager and runtime inputs.
     *
     * @param sessionManager session manager
     * @param metadataCatalog metadata catalog
     * @param databaseRuntime database runtime
     */
    public MCPRuntimeContext(final MCPSessionManager sessionManager, final MetadataCatalog metadataCatalog, final DatabaseRuntime databaseRuntime) {
        runtimeServices = new MCPRuntimeServices(sessionManager, metadataCatalog, databaseRuntime);
    }
    
    private MCPRuntimeContext(final MCPRuntimeServices runtimeServices) {
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices cannot be null");
    }
    
    /**
     * Create one compatibility wrapper from runtime services.
     *
     * @param runtimeServices runtime services
     * @return compatibility wrapper
     */
    public static MCPRuntimeContext fromServices(final MCPRuntimeServices runtimeServices) {
        return new MCPRuntimeContext(runtimeServices);
    }
    
    /**
     * Get the underlying runtime services.
     *
     * @return runtime services
     */
    public MCPRuntimeServices getRuntimeServices() {
        return runtimeServices;
    }
    
    /**
     * Get the capability assembler.
     *
     * @return capability assembler
     */
    public DatabaseCapabilityAssembler getCapabilityAssembler() {
        return runtimeServices.getCapabilityAssembler();
    }
    
    /**
     * Get the metadata resource loader.
     *
     * @return metadata resource loader
     */
    public MetadataResourceLoader getMetadataResourceLoader() {
        return runtimeServices.getMetadataResourceLoader();
    }
    
    /**
     * Get the metadata tool dispatcher.
     *
     * @return metadata tool dispatcher
     */
    public MetadataToolDispatcher getMetadataToolDispatcher() {
        return runtimeServices.getMetadataToolDispatcher();
    }
    
    /**
     * Get the transaction command executor.
     *
     * @return transaction command executor
     */
    public TransactionCommandExecutor getTransactionCommandExecutor() {
        return runtimeServices.getTransactionCommandExecutor();
    }
    
    /**
     * Get the audit recorder.
     *
     * @return audit recorder
     */
    public AuditRecorder getAuditRecorder() {
        return runtimeServices.getAuditRecorder();
    }
    
    /**
     * Get the metadata refresh coordinator.
     *
     * @return metadata refresh coordinator
     */
    public MetadataRefreshCoordinator getMetadataRefreshCoordinator() {
        return runtimeServices.getMetadataRefreshCoordinator();
    }
    
    /**
     * Get the execute-query facade.
     *
     * @return execute-query facade
     */
    public ExecuteQueryFacade getExecuteQueryFacade() {
        return runtimeServices.getExecuteQueryFacade();
    }
    
    /**
     * Register the full public MCP surface with the compatibility server context.
     *
     * @param serverContext server context
     */
    public void registerDefaults(final MCPServerContext serverContext) {
        runtimeServices.registerDefaults(serverContext);
    }
    
    /**
     * Register the full public MCP surface with the server registry.
     *
     * @param serverRegistry server registry
     */
    public void registerDefaults(final MCPServerRegistry serverRegistry) {
        runtimeServices.registerDefaults(serverRegistry);
    }
}
