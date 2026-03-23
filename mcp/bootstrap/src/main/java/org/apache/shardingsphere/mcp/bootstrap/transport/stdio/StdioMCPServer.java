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

package org.apache.shardingsphere.mcp.bootstrap.transport.stdio;

import lombok.Getter;
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.execute.ExecuteQueryFacade.ExecutionRequest;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.tool.MetadataToolDispatcher.ToolDispatchResult;
import org.apache.shardingsphere.mcp.tool.MetadataToolDispatcher.ToolRequest;

import java.util.Objects;
import java.util.UUID;

/**
 * In-memory STDIO MCP runtime for local integration and smoke tests.
 */
public final class StdioMCPServer {
    
    private final MCPSessionManager sessionManager;
    
    private final MCPRuntimeContext runtimeContext;
    
    @Getter
    private boolean running;
    
    /**
     * Construct one STDIO MCP server.
     *
     * @param sessionManager session manager
     * @param runtimeContext runtime context
     */
    public StdioMCPServer(final MCPSessionManager sessionManager, final MCPRuntimeContext runtimeContext) {
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager cannot be null");
        this.runtimeContext = Objects.requireNonNull(runtimeContext, "runtimeContext cannot be null");
    }
    
    /**
     * Start the STDIO runtime.
     */
    public void start() {
        running = true;
    }
    
    /**
     * Stop the STDIO runtime.
     */
    public void stop() {
        running = false;
    }
    
    /**
     * Initialize one local STDIO session.
     *
     * @return session identifier
     */
    public String initializeSession() {
        String result = UUID.randomUUID().toString();
        sessionManager.createSession(result);
        return result;
    }
    
    /**
     * Invoke one metadata tool over STDIO.
     *
     * @param sessionId session identifier
     * @param metadataCatalog metadata catalog
     * @param toolRequest tool request
     * @return dispatch result
     */
    public ToolDispatchResult invokeMetadataTool(final String sessionId, final MetadataCatalog metadataCatalog, final ToolRequest toolRequest) {
        requireSession(sessionId);
        return runtimeContext.getMetadataToolDispatcher().dispatch(metadataCatalog, toolRequest);
    }
    
    /**
     * Invoke one execute-query request over STDIO.
     *
     * @param sessionId session identifier
     * @param executionRequest execution request
     * @return execute-query response
     */
    public ExecuteQueryResponse executeQuery(final String sessionId, final ExecutionRequest executionRequest) {
        requireSession(sessionId);
        return runtimeContext.getExecuteQueryFacade().execute(executionRequest);
    }
    
    /**
     * Close one STDIO session.
     *
     * @param sessionId session identifier
     */
    public void closeSession(final String sessionId) {
        runtimeContext.getMetadataRefreshCoordinator().clearSession(sessionId);
        runtimeContext.getTransactionCommandExecutor().getDatabaseRuntime().closeSession(sessionId);
        sessionManager.closeSession(sessionId);
    }
    
    private void requireSession(final String sessionId) {
        sessionManager.findSession(sessionId).orElseThrow(() -> new IllegalStateException("Session does not exist."));
    }
}
