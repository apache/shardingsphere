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

import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionResourceManager;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.util.Collections;
import java.util.Map;

/**
 * MCP runtime context factory for tests.
 */
public final class MCPRuntimeContextTestFactory {
    
    /**
     * Create MCP runtime context for tests.
     *
     * @param metadataCatalog database metadata catalog
     * @return MCP runtime context
     */
    public MCPRuntimeContext create(final MCPDatabaseMetadataCatalog metadataCatalog) {
        return create(metadataCatalog, Collections.emptyMap());
    }
    
    /**
     * Create MCP runtime context for tests with runtime databases.
     *
     * @param metadataCatalog database metadata catalog
     * @param runtimeDatabases runtime database configurations
     * @return MCP runtime context
     */
    public MCPRuntimeContext create(final MCPDatabaseMetadataCatalog metadataCatalog, final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        MCPJdbcTransactionResourceManager transactionResourceManager = new MCPJdbcTransactionResourceManager(runtimeDatabases);
        MCPSessionManager sessionManager = new MCPSessionManager(transactionResourceManager);
        MCPSessionExecutionCoordinator sessionExecutionCoordinator = new MCPSessionExecutionCoordinator(sessionManager);
        MCPDatabaseCapabilityProvider databaseCapabilityProvider = new MCPDatabaseCapabilityProvider(metadataCatalog);
        return new MCPRuntimeContext(sessionManager, sessionExecutionCoordinator, metadataCatalog, databaseCapabilityProvider);
    }
}
