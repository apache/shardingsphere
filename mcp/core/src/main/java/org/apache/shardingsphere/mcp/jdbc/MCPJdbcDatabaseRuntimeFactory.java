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

import org.apache.shardingsphere.mcp.execute.ConnectionProvider;
import org.apache.shardingsphere.mcp.execute.DatabaseExecutionBackend;
import org.apache.shardingsphere.mcp.execute.JdbcDatabaseExecutionBackend;
import org.apache.shardingsphere.mcp.execute.ShardingSphereExecutionAdapter;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * MCP JDBC database runtime factory.
 */
public final class MCPJdbcDatabaseRuntimeFactory {
    
    /**
     * Create one adapter-backed database runtime.
     *
     * @param runtimeDatabases runtime databases
     * @param databaseMetadataSnapshots database metadata snapshots
     * @return database runtime
     */
    public DatabaseExecutionBackend create(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases, final DatabaseMetadataSnapshots databaseMetadataSnapshots) {
        Map<String, ConnectionProvider> connectionProviders = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
        for (Entry<String, RuntimeDatabaseConfiguration> each : runtimeDatabases.entrySet()) {
            connectionProviders.put(each.getKey(), () -> each.getValue().openConnection(each.getKey()));
        }
        return new JdbcDatabaseExecutionBackend(new ShardingSphereExecutionAdapter(connectionProviders),
                database -> refreshMetadata(database, runtimeDatabases.get(database), databaseMetadataSnapshots));
    }
    
    private void refreshMetadata(final String databaseName, final RuntimeDatabaseConfiguration runtimeDatabaseConfig, final DatabaseMetadataSnapshots databaseMetadataSnapshots) {
        DatabaseMetadataSnapshots refreshedSnapshots = new MCPJdbcMetadataLoader().load(Collections.singletonMap(databaseName, runtimeDatabaseConfig));
        DatabaseMetadataSnapshot databaseSnapshot = refreshedSnapshots.findSnapshot(databaseName).orElseThrow(() -> new IllegalArgumentException("databaseSnapshot cannot be null"));
        databaseMetadataSnapshots.replaceSnapshot(databaseName, databaseSnapshot);
    }
}
