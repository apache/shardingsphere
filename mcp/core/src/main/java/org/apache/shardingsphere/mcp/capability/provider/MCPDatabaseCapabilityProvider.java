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

package org.apache.shardingsphere.mcp.capability.provider;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityCatalog;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;

import java.util.Optional;

/**
 * MCP database capability provider.
 */
@RequiredArgsConstructor
public final class MCPDatabaseCapabilityProvider {
    
    private final DatabaseMetadataSnapshots databaseMetadataSnapshots;
    
    /**
     * Provide the database-level capability.
     *
     * @param databaseName logical database name
     * @return database-level capability when the database type is supported
     */
    public Optional<DatabaseCapability> provide(final String databaseName) {
        return databaseMetadataSnapshots.findDatabaseType(databaseName).flatMap(each -> DatabaseCapabilityCatalog.find(databaseName, each, getDatabaseVersion(databaseName)));
    }
    
    private String getDatabaseVersion(final String databaseName) {
        return databaseMetadataSnapshots.findSnapshot(databaseName).map(DatabaseMetadataSnapshot::getDatabaseVersion).orElse("");
    }
}
