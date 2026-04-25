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

package org.apache.shardingsphere.mcp.bootstrap.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.test.fixture.jdbc.H2RuntimeTestSupport;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.sql.SQLException;

/**
 * Bootstrap test data factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPBootstrapTestDataFactory {
    
    /**
     * Create runtime context.
     *
     * @param tempDir temp directory
     * @return runtime context
     */
    public static MCPRuntimeContext createRuntimeContext(final Path tempDir) {
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = createRuntimeDatabases(tempDir);
        return new MCPRuntimeContext(new MCPSessionManager(runtimeDatabases), new MCPDatabaseCapabilityProvider(runtimeDatabases));
    }
    
    /**
     * Create runtime databases.
     *
     * @param tempDir temp directory
     * @return runtime databases
     */
    public static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases(final Path tempDir) {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(2, 1F);
        result.put("logic_db", createInitializedRuntimeDatabaseConfiguration(tempDir, "logic-db"));
        result.put("runtime_db", createInitializedRuntimeDatabaseConfiguration(tempDir, "runtime-db"));
        return result;
    }
    
    /**
     * Create runtime databases for one prepared H2 runtime.
     *
     * @param logicalDatabase logical database
     * @param jdbcUrl JDBC URL
     * @return runtime databases
     */
    public static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases(final String logicalDatabase, final String jdbcUrl) {
        return Map.of(logicalDatabase, createRuntimeDatabaseConfiguration(jdbcUrl));
    }
    
    /**
     * Create one H2 runtime database configuration.
     *
     * @param jdbcUrl JDBC URL
     * @return runtime database configuration
     */
    public static RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String jdbcUrl) {
        return new RuntimeDatabaseConfiguration("H2", jdbcUrl, "", "", "org.h2.Driver");
    }
    
    private static RuntimeDatabaseConfiguration createInitializedRuntimeDatabaseConfiguration(final Path tempDir, final String storageDatabase) {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, storageDatabase);
        try {
            H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to initialize bootstrap test database.", ex);
        }
        return createRuntimeDatabaseConfiguration(jdbcUrl);
    }
}
