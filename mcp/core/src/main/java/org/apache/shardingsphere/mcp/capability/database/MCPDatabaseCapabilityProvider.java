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

package org.apache.shardingsphere.mcp.capability.database;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;

import java.util.Optional;

/**
 * MCP database capability provider.
 */
@RequiredArgsConstructor
public final class MCPDatabaseCapabilityProvider {
    
    private final MCPDatabaseMetadataCatalog metadataCatalog;
    
    /**
     * Provide the database-level capability.
     *
     * @param databaseName database name
     * @return database-level capability
     */
    public Optional<MCPDatabaseCapability> provide(final String databaseName) {
        return metadataCatalog.findMetadata(databaseName).map(MCPDatabaseMetadata::getDatabaseType).flatMap(optional -> find(databaseName, optional, getDatabaseVersion(databaseName)));
    }
    
    private Optional<MCPDatabaseCapability> find(final String databaseName, final String databaseType, final String databaseVersion) {
        return TypedSPILoader.findService(MCPDatabaseCapabilityOption.class, databaseType).map(optional -> new MCPDatabaseCapability(databaseName, databaseVersion, optional));
    }
    
    private String getDatabaseVersion(final String databaseName) {
        return metadataCatalog.findMetadata(databaseName).map(MCPDatabaseMetadata::getDatabaseVersion).orElse("");
    }
}
