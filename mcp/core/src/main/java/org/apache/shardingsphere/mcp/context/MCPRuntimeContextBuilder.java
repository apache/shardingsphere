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
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.execute.MCPSQLExecutionFacade;
import org.apache.shardingsphere.mcp.execute.MCPJdbcStatementExecutor;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionResourceManager;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionStatementExecutor;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.jdbc.MCPJdbcMetadataLoader;
import org.apache.shardingsphere.mcp.metadata.jdbc.MCPJdbcMetadataRefresher;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionExecutionCoordinator;
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
        MCPSessionExecutionCoordinator sessionExecutionCoordinator = new MCPSessionExecutionCoordinator(sessionManager);
        MCPDatabaseMetadataCatalog metadataCatalog = new MCPJdbcMetadataLoader().load(runtimeDatabases);
        MCPDatabaseCapabilityProvider databaseCapabilityProvider = new MCPDatabaseCapabilityProvider(metadataCatalog);
        MCPSQLExecutionFacade sqlExecutionFacade = new MCPSQLExecutionFacade(databaseCapabilityProvider, sessionExecutionCoordinator,
                new MCPJdbcTransactionStatementExecutor(sessionManager, transactionResourceManager),
                new MCPJdbcStatementExecutor(runtimeDatabases, transactionResourceManager),
                new MCPJdbcMetadataRefresher(runtimeDatabases, metadataCatalog));
        return new MCPRuntimeContext(sessionManager, sessionExecutionCoordinator, metadataCatalog, databaseCapabilityProvider, sqlExecutionFacade);
    }
}
