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

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.metadata.jdbc.MCPJdbcDatabaseProfileLoader;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseProfile;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MCP database capability provider.
 */
public final class MCPDatabaseCapabilityProvider {

    private final Map<String, RuntimeDatabaseProfile> databaseProfiles;

    private final Map<String, MCPDatabaseCapability> databaseCapabilities;

    public MCPDatabaseCapabilityProvider(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        databaseProfiles = new LinkedHashMap<>(new MCPJdbcDatabaseProfileLoader().load(runtimeDatabases));
        databaseCapabilities = createDatabaseCapabilities(databaseProfiles);
    }

    /**
     * Provide the database-level capability.
     *
     * @param databaseName database name
     * @return database-level capability
     */
    public Optional<MCPDatabaseCapability> provide(final String databaseName) {
        return Optional.ofNullable(databaseCapabilities.get(databaseName));
    }

    /**
     * Find runtime database profile.
     *
     * @param databaseName database name
     * @return runtime database profile
     */
    public Optional<RuntimeDatabaseProfile> findDatabaseProfile(final String databaseName) {
        return Optional.ofNullable(databaseProfiles.get(databaseName));
    }

    /**
     * Get runtime database profiles.
     *
     * @return runtime database profiles
     */
    public List<RuntimeDatabaseProfile> getDatabaseProfiles() {
        return new LinkedList<>(databaseProfiles.values());
    }

    private Map<String, MCPDatabaseCapability> createDatabaseCapabilities(final Map<String, RuntimeDatabaseProfile> databaseProfiles) {
        Map<String, MCPDatabaseCapability> result = new LinkedHashMap<>(databaseProfiles.size(), 1F);
        for (RuntimeDatabaseProfile each : databaseProfiles.values()) {
            TypedSPILoader.findService(MCPDatabaseCapabilityOption.class, each.getDatabaseType())
                    .ifPresent(optional -> result.put(each.getDatabase(), new MCPDatabaseCapability(each.getDatabase(), each.getDatabaseVersion(), optional)));
        }
        return result;
    }
}
