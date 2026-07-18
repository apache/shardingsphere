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
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.core.workflow.InMemoryWorkflowSessionStore;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;

/**
 * MCP runtime context.
 */
@Getter
public final class MCPRuntimeContext {
    
    private final MCPSessionManager sessionManager;
    
    private final MCPDatabaseCapabilityProvider databaseCapabilityProvider;
    
    private final MCPTransportType activeTransport;
    
    @Getter(AccessLevel.NONE)
    private final InMemoryWorkflowSessionStore workflowSessionStore;
    
    public MCPRuntimeContext(final MCPSessionManager sessionManager, final MCPDatabaseCapabilityProvider databaseCapabilityProvider, final MCPTransportType activeTransport) {
        this.sessionManager = sessionManager;
        this.databaseCapabilityProvider = databaseCapabilityProvider;
        this.activeTransport = activeTransport;
        workflowSessionStore = new InMemoryWorkflowSessionStore();
        sessionManager.addSessionCloseListener(workflowSessionStore::removeSession);
    }
    
    /**
     * Get workflow session context.
     *
     * @param sessionId session identifier
     * @return session-bound workflow context
     */
    public WorkflowSessionContext getWorkflowSessionContext(final String sessionId) {
        return workflowSessionStore.getSessionContext(sessionId);
    }
}
