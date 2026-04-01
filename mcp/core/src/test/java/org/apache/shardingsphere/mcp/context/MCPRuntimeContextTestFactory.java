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

import org.apache.shardingsphere.mcp.capability.MCPCapabilityBuilder;
import org.apache.shardingsphere.mcp.execute.MCPSQLExecutionFacade;
import org.apache.shardingsphere.mcp.execute.MCPJdbcStatementExecutor;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionResourceManager;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionStatementExecutor;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import static org.mockito.Mockito.mock;

/**
 * MCP runtime context factory for tests.
 */
public final class MCPRuntimeContextTestFactory {
    
    /**
     * Create MCP runtime context for tests.
     *
     * @param databaseMetadataSnapshots database metadata snapshots
     * @param statementExecutor JDBC statement executor
     * @return MCP runtime context
     */
    public MCPRuntimeContext create(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final MCPJdbcStatementExecutor statementExecutor) {
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        MCPSessionManager sessionManager = new MCPSessionManager(transactionResourceManager);
        MCPSessionExecutionCoordinator sessionExecutionCoordinator = new MCPSessionExecutionCoordinator(sessionManager);
        MCPCapabilityBuilder capabilityBuilder = new MCPCapabilityBuilder(databaseMetadataSnapshots);
        MCPJdbcTransactionStatementExecutor transactionStatementExecutor = new MCPJdbcTransactionStatementExecutor(sessionManager, transactionResourceManager);
        MCPSQLExecutionFacade sqlExecutionFacade = new MCPSQLExecutionFacade(capabilityBuilder, sessionExecutionCoordinator, transactionStatementExecutor, statementExecutor, mock());
        return new MCPRuntimeContext(sessionManager, sessionExecutionCoordinator, databaseMetadataSnapshots, capabilityBuilder, sqlExecutionFacade);
    }
}
