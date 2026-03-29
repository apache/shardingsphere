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

package org.apache.shardingsphere.mcp.jdbc.runtime;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.jdbc.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.util.Map;

/**
 * MCP JDBC runtime context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPJdbcRuntimeContextFactory {
    
    /**
     * Create a new instance of MCP runtime context.
     *
     * @param sessionManager session manager
     * @param runtimeDatabases runtime databases
     * @return runtime context
     */
    public static MCPRuntimeContext newInstance(final MCPSessionManager sessionManager, final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        ShardingSpherePreconditions.checkNotEmpty(runtimeDatabases, () -> new IllegalArgumentException("At least one runtime database must be configured."));
        MetadataCatalog metadataCatalog = new MCPJdbcMetadataLoader().load(runtimeDatabases);
        DatabaseRuntime databaseRuntime = new MCPDatabaseRuntimeFactory().createDatabaseRuntime(runtimeDatabases, metadataCatalog);
        return MCPRuntimeContext.create(sessionManager, metadataCatalog, databaseRuntime);
    }
}
