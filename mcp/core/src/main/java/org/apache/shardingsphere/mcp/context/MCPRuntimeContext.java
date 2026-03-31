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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.capability.MCPCapabilityBuilder;
import org.apache.shardingsphere.mcp.execute.ExecuteQueryFacade;
import org.apache.shardingsphere.mcp.execute.MCPJdbcStatementExecutor;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionResourceManager;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.session.TransactionCommandExecutor;

/**
 * MCP runtime context.
 */
@RequiredArgsConstructor
@Getter
public final class MCPRuntimeContext {
    
    private final DatabaseMetadataSnapshots databaseMetadataSnapshots;
    
    private final MCPSessionManager sessionManager;
    
    private final MCPJdbcStatementExecutor statementExecutor;
    
    private final MCPJdbcTransactionResourceManager transactionResourceManager;
    
    private final MCPCapabilityBuilder capabilityBuilder;
    
    private final TransactionCommandExecutor transactionCommandExecutor;
    
    private final ExecuteQueryFacade executeQueryFacade;
    
    /**
     * Close one MCP session and any bound JDBC transaction resources.
     *
     * @param sessionId session identifier
     */
    public void closeSession(final String sessionId) {
        try {
            transactionResourceManager.closeSession(sessionId);
        } finally {
            sessionManager.closeSession(sessionId);
        }
    }
}
