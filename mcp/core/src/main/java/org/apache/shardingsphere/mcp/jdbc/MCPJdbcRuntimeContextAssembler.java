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

package org.apache.shardingsphere.mcp.jdbc;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.util.Map;

/**
 * MCP JDBC runtime context assembler.
 */
public final class MCPJdbcRuntimeContextAssembler {
    
    private final MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
    
    private final MCPJdbcDatabaseRuntimeFactory databaseRuntimeFactory = new MCPJdbcDatabaseRuntimeFactory();
    
    /**
     * Assemble MCP runtime context.
     *
     * @param runtimeDatabases runtime databases
     * @return runtime context
     */
    public MCPRuntimeContext assemble(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        ShardingSpherePreconditions.checkNotEmpty(runtimeDatabases, () -> new IllegalArgumentException("At least one runtime database must be configured."));
        MCPSessionManager sessionManager = new MCPSessionManager();
        MetadataCatalog metadataCatalog = metadataLoader.load(runtimeDatabases);
        DatabaseRuntime databaseRuntime = databaseRuntimeFactory.create(runtimeDatabases, metadataCatalog);
        return MCPRuntimeContext.create(sessionManager, metadataCatalog, databaseRuntime);
    }
}
