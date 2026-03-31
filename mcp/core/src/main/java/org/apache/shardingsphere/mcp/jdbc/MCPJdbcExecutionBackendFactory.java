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

import org.apache.shardingsphere.mcp.execute.DatabaseExecutionBackend;
import org.apache.shardingsphere.mcp.execute.MCPJdbcExecutionAdapter;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import java.util.Map;

/**
 * MCP JDBC execution backend factory.
 */
public final class MCPJdbcExecutionBackendFactory {
    
    /**
     * Create one adapter-backed database runtime.
     *
     * @param runtimeDatabases runtime databases
     * @param databaseMetadataSnapshots database metadata snapshots
     * @return database runtime
     */
    public DatabaseExecutionBackend create(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases, final DatabaseMetadataSnapshots databaseMetadataSnapshots) {
        return new DatabaseExecutionBackend(new MCPJdbcExecutionAdapter(runtimeDatabases), new MCPJdbcMetadataLoader(), runtimeDatabases, databaseMetadataSnapshots);
    }
}
