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

package org.apache.shardingsphere.mcp.metadata.jdbc;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;

import java.util.Map;
import java.util.Optional;

/**
 * MCP JDBC metadata refresh coordinator.
 */
@RequiredArgsConstructor
public final class MCPJdbcMetadataRefresher {
    
    private final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases;
    
    private final MCPDatabaseMetadataCatalog metadataCatalog;
    
    /**
     * Refresh metadata.
     *
     * @param databaseName database name
     */
    public void refresh(final String databaseName) {
        MCPDatabaseMetadata metadata = new MCPJdbcMetadataLoader().load(Map.of(databaseName, getRequiredRuntimeDatabaseConfiguration(databaseName)))
                .findMetadata(databaseName).orElseThrow(() -> new IllegalStateException(String.format("Failed to refresh metadata for database `%s`.", databaseName)));
        metadataCatalog.replaceMetadata(databaseName, metadata);
    }
    
    private RuntimeDatabaseConfiguration getRequiredRuntimeDatabaseConfiguration(final String databaseName) {
        return Optional.ofNullable(runtimeDatabases.get(databaseName)).orElseThrow(() -> new IllegalArgumentException(String.format("Database `%s` is not configured.", databaseName)));
    }
}
