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
import org.apache.shardingsphere.mcp.execute.ExecuteQueryFacade;
import org.apache.shardingsphere.mcp.execute.MCPJdbcExecutionAdapter;
import org.apache.shardingsphere.mcp.execute.StatementClassifier;
import org.apache.shardingsphere.mcp.jdbc.MCPJdbcMetadataLoader;
import org.apache.shardingsphere.mcp.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.MetadataRefreshCoordinator;
import org.apache.shardingsphere.mcp.protocol.MCPPayloadBuilder;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.session.TransactionCommandExecutor;

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
     */
    public MCPRuntimeContext build(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        ShardingSpherePreconditions.checkNotEmpty(runtimeDatabases, () -> new IllegalArgumentException("At least one runtime database must be configured."));
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPJdbcExecutionAdapter jdbcExecutionAdapter = new MCPJdbcExecutionAdapter(runtimeDatabases);
        DatabaseMetadataSnapshots databaseMetadataSnapshots = new MCPJdbcMetadataLoader().load(runtimeDatabases);
        MCPCapabilityBuilder capabilityBuilder = new MCPCapabilityBuilder(databaseMetadataSnapshots);
        TransactionCommandExecutor transactionCommandExecutor = new TransactionCommandExecutor(sessionManager, jdbcExecutionAdapter);
        MetadataRefreshCoordinator metadataRefreshCoordinator = new MetadataRefreshCoordinator(runtimeDatabases, databaseMetadataSnapshots);
        ExecuteQueryFacade executeQueryFacade = new ExecuteQueryFacade(
                new StatementClassifier(), capabilityBuilder, transactionCommandExecutor, jdbcExecutionAdapter, metadataRefreshCoordinator);
        MCPPayloadBuilder payloadBuilder = new MCPPayloadBuilder();
        return new MCPRuntimeContext(sessionManager, databaseMetadataSnapshots, jdbcExecutionAdapter, capabilityBuilder, transactionCommandExecutor, executeQueryFacade, payloadBuilder);
    }
}
