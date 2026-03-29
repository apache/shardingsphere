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

import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.execute.ShardingSphereExecutionAdapter;
import org.apache.shardingsphere.mcp.execute.ShardingSphereExecutionAdapter.ConnectionProvider;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.RuntimeDatabaseDescriptor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * MCP JDBC database runtime factory.
 */
public final class MCPJdbcDatabaseRuntimeFactory {
    
    /**
     * Create one adapter-backed database runtime.
     *
     * @param runtimeDatabases runtime databases
     * @param metadataCatalog metadata catalog
     * @return database runtime
     */
    public DatabaseRuntime create(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases, final MetadataCatalog metadataCatalog) {
        Map<String, ConnectionProvider> connectionProviders = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
        for (Entry<String, RuntimeDatabaseConfiguration> each : runtimeDatabases.entrySet()) {
            connectionProviders.put(each.getKey(), () -> each.getValue().openConnection(each.getKey()));
        }
        ShardingSphereExecutionAdapter executionAdapter = new ShardingSphereExecutionAdapter(connectionProviders);
        return new DatabaseRuntime(executionAdapter, database -> refreshMetadata(database, runtimeDatabases.get(database), metadataCatalog));
    }
    
    private void refreshMetadata(final String database, final RuntimeDatabaseConfiguration runtimeDatabaseConfig,
                                 final MetadataCatalog metadataCatalog) {
        MetadataCatalog refreshedCatalog = new MCPJdbcMetadataLoader().load(Collections.singletonMap(database, runtimeDatabaseConfig));
        RuntimeDatabaseDescriptor runtimeDatabaseDescriptor = Objects.requireNonNull(refreshedCatalog.getRuntimeDatabaseDescriptors().get(database), "runtimeDatabaseDescriptor cannot be null");
        metadataCatalog.replaceDatabaseSnapshot(database, refreshedCatalog.getDatabaseTypes().get(database), refreshedCatalog.getMetadataObjects(), runtimeDatabaseDescriptor);
    }
}
