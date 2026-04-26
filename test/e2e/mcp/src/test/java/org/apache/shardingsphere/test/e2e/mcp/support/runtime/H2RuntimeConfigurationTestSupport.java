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

package org.apache.shardingsphere.test.e2e.mcp.support.runtime;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;

import java.util.Map;

/**
 * H2 runtime database configuration support for MCP E2E tests.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class H2RuntimeConfigurationTestSupport {
    
    /**
     * Create one H2 runtime database configuration map.
     *
     * @param logicalDatabase logical database
     * @param jdbcUrl JDBC URL
     * @return runtime database configuration map
     */
    public static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases(final String logicalDatabase, final String jdbcUrl) {
        return Map.of(logicalDatabase, createRuntimeDatabaseConfiguration("H2", jdbcUrl));
    }
    
    /**
     * Create one H2 runtime database configuration.
     *
     * @param databaseType database type
     * @param jdbcUrl JDBC URL
     * @return runtime database configuration
     */
    public static RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String databaseType, final String jdbcUrl) {
        return new RuntimeDatabaseConfiguration(databaseType, jdbcUrl, "", "", "org.h2.Driver");
    }
}
