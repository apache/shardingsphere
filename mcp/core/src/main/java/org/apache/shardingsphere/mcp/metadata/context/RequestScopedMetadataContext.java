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

package org.apache.shardingsphere.mcp.metadata.context;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.metadata.jdbc.MCPJdbcMetadataLoader;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Request-scoped metadata context.
 */
@RequiredArgsConstructor
public final class RequestScopedMetadataContext implements AutoCloseable {
    
    private final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases;
    
    private final MCPDatabaseCapabilityProvider databaseCapabilityProvider;
    
    private final MCPJdbcMetadataLoader metadataLoader;
    
    private final Map<String, MCPDatabaseMetadata> loadedDatabaseMetadata = new LinkedHashMap<>(4, 1F);
    
    public RequestScopedMetadataContext(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases, final MCPDatabaseCapabilityProvider databaseCapabilityProvider) {
        this(runtimeDatabases, databaseCapabilityProvider, new MCPJdbcMetadataLoader());
    }
    
    /**
     * Load database metadata lazily within the current request.
     *
     * @param databaseName database name
     * @return database metadata
     */
    public Optional<MCPDatabaseMetadata> loadDatabaseMetadata(final String databaseName) {
        MCPDatabaseMetadata loadedMetadata = loadedDatabaseMetadata.get(databaseName);
        if (null != loadedMetadata) {
            return Optional.of(loadedMetadata);
        }
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = runtimeDatabases.get(databaseName);
        Optional<RuntimeDatabaseProfile> databaseProfile = databaseCapabilityProvider.findDatabaseProfile(databaseName);
        if (null == runtimeDatabaseConfig || databaseProfile.isEmpty()) {
            return Optional.empty();
        }
        MCPDatabaseMetadata result = metadataLoader.load(databaseName, runtimeDatabaseConfig, databaseProfile.get());
        loadedDatabaseMetadata.put(databaseName, result);
        return Optional.of(result);
    }
    
    @Override
    public void close() {
        loadedDatabaseMetadata.clear();
    }
}
