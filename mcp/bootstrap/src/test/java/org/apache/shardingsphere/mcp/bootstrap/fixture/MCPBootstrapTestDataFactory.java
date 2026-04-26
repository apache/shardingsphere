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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bootstrap test data factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPBootstrapTestDataFactory {
    
    /**
     * Create runtime context.
     *
     * @return runtime context
     */
    public static MCPRuntimeContext createRuntimeContext() {
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = createRuntimeDatabases();
        return new MCPRuntimeContext(new MCPSessionManager(runtimeDatabases), new MCPDatabaseCapabilityProvider(runtimeDatabases));
    }
    
    /**
     * Create runtime databases.
     *
     * @return runtime databases
     */
    public static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(2, 1F);
        result.put("logic_db", createMockRuntimeDatabaseConfiguration("logic-db"));
        result.put("runtime_db", createMockRuntimeDatabaseConfiguration("runtime-db"));
        return result;
    }
    
    /**
     * Create runtime databases for one prepared runtime.
     *
     * @param logicalDatabase logical database
     * @param runtimeDatabaseConfiguration runtime database configuration
     * @return runtime databases
     */
    public static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases(
                                                                                   final String logicalDatabase, final RuntimeDatabaseConfiguration runtimeDatabaseConfiguration) {
        return Map.of(logicalDatabase, runtimeDatabaseConfiguration);
    }
    
    /**
     * Create one runtime database configuration.
     *
     * @param databaseType database type
     * @param jdbcUrl JDBC URL
     * @param driverClassName driver class name
     * @return runtime database configuration
     */
    public static RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(
                                                                                  final String databaseType, final String jdbcUrl, final String driverClassName) {
        return new RuntimeDatabaseConfiguration(databaseType, jdbcUrl, "", "", driverClassName);
    }
    
    /**
     * Create one mock runtime database configuration.
     *
     * @param databaseName database name
     * @return runtime database configuration
     */
    public static RuntimeDatabaseConfiguration createMockRuntimeDatabaseConfiguration(final String databaseName) {
        return createRuntimeDatabaseConfiguration("H2",
                BootstrapMockRuntimeDriver.createJdbcUrl(databaseName), BootstrapMockRuntimeDriver.class.getName());
    }
}
