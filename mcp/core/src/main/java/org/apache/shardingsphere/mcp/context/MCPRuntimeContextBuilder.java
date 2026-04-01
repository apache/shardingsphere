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

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.capability.MCPCapabilityBuilder;
import org.apache.shardingsphere.mcp.execute.MCPSQLExecutionFacade;
import org.apache.shardingsphere.mcp.execute.MCPJdbcStatementExecutor;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionResourceManager;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionStatementExecutor;
import org.apache.shardingsphere.mcp.jdbc.MCPJdbcMetadataLoader;
import org.apache.shardingsphere.mcp.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.MetadataRefreshCoordinator;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.util.Map;

/**
 * MCP runtime context builder.
 */
public final class MCPRuntimeContextBuilder {
    
    /**
     * Build MCP runtime context.
     *
     * @param runtimeDatabases runtime databases
     * @return built context
     * @throws IllegalArgumentException if runtime databases are empty
     */
    public MCPRuntimeContext build(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        ShardingSpherePreconditions.checkNotEmpty(runtimeDatabases, () -> new IllegalArgumentException("At least one runtime database must be configured."));
        MCPJdbcTransactionResourceManager transactionResourceManager = new MCPJdbcTransactionResourceManager(runtimeDatabases);
        MCPSessionManager sessionManager = new MCPSessionManager(transactionResourceManager);
        MCPJdbcTransactionStatementExecutor transactionStatementExecutor = new MCPJdbcTransactionStatementExecutor(sessionManager, transactionResourceManager);
        MCPJdbcStatementExecutor statementExecutor = new MCPJdbcStatementExecutor(runtimeDatabases, transactionResourceManager);
        DatabaseMetadataSnapshots databaseMetadataSnapshots = new MCPJdbcMetadataLoader().load(runtimeDatabases);
        MCPCapabilityBuilder capabilityBuilder = new MCPCapabilityBuilder(databaseMetadataSnapshots);
        MCPSQLExecutionFacade sqlExecutionFacade = new MCPSQLExecutionFacade(
                capabilityBuilder, sessionManager, transactionStatementExecutor, statementExecutor, new MetadataRefreshCoordinator(runtimeDatabases, databaseMetadataSnapshots));
        return new MCPRuntimeContext(sessionManager, databaseMetadataSnapshots, capabilityBuilder, sqlExecutionFacade);
    }
}
