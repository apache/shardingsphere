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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.MetadataRefreshCoordinator;

/**
 * Close one managed MCP session.
 */
public final class MCPSessionCloser {
    
    private final MCPRuntimeContext runtimeContext;
    
    private final MetadataRefreshCoordinator metadataRefreshCoordinator;
    
    public MCPSessionCloser(final MCPRuntimeContext runtimeContext, final MetadataRefreshCoordinator metadataRefreshCoordinator) {
        this.runtimeContext = runtimeContext;
        this.metadataRefreshCoordinator = metadataRefreshCoordinator;
    }
    
    /**
     * Close one session.
     *
     * @param sessionId session ID
     */
    public void closeSession(final String sessionId) {
        if (null == sessionId || sessionId.isEmpty()) {
            return;
        }
        metadataRefreshCoordinator.clearSession(sessionId);
        runtimeContext.getDatabaseRuntime().closeSession(sessionId);
        runtimeContext.getSessionManager().closeSession(sessionId);
    }
}
