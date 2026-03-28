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

package org.apache.shardingsphere.mcp.bootstrap.transport.session;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;

/**
 * MCP session closer.
 */
@RequiredArgsConstructor
public final class MCPSessionCloser {
    
    private final MCPRuntimeContext runtimeContext;
    
    /**
     * Close one session.
     *
     * @param sessionId session ID
     */
    public void closeSession(final String sessionId) {
        if (null == sessionId || sessionId.isEmpty()) {
            return;
        }
        runtimeContext.getMetadataRefreshCoordinator().clearSession(sessionId);
        runtimeContext.getDatabaseRuntime().closeSession(sessionId);
        runtimeContext.getSessionManager().closeSession(sessionId);
    }
}
