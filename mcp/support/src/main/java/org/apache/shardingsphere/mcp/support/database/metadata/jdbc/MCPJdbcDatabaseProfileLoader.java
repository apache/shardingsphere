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

package org.apache.shardingsphere.mcp.support.database.metadata.jdbc;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.exception.external.ShardingSphereExternalException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * MCP JDBC database profile loader.
 */
public final class MCPJdbcDatabaseProfileLoader {
    
    /**
     * Load runtime database profiles.
     *
     * @param runtimeDatabases runtime database configurations
     * @return runtime database profiles
     */
    public Map<String, RuntimeDatabaseProfile> load(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        Map<String, RuntimeDatabaseProfile> result = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
        for (Entry<String, RuntimeDatabaseConfiguration> entry : runtimeDatabases.entrySet()) {
            result.put(entry.getKey(), load(entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    /**
     * Load runtime database profile.
     *
     * @param databaseName database name
     * @param runtimeDatabaseConfig runtime database configuration
     * @return runtime database profile
     * @throws RuntimeDatabaseConnectionException when runtime database connection or configuration fails
     * @throws RuntimeDatabaseConnectionException when profile metadata loading fails
     */
    public RuntimeDatabaseProfile load(final String databaseName, final RuntimeDatabaseConfiguration runtimeDatabaseConfig) {
        try (Connection connection = runtimeDatabaseConfig.openConnection(databaseName)) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            DatabaseType databaseType = loadDatabaseType(databaseName, connection);
            String databaseVersion = Objects.toString(databaseMetaData.getDatabaseProductVersion(), "").trim();
            return new RuntimeDatabaseProfile(databaseName, databaseType.getType(), databaseVersion);
        } catch (final SQLException ex) {
            throw RuntimeDatabaseConnectionException.connectionFailed(databaseName, ex);
        }
    }
    
    private DatabaseType loadDatabaseType(final String databaseName, final Connection connection) throws SQLException {
        try {
            return DatabaseTypeFactory.get(connection);
        } catch (final ShardingSphereExternalException ex) {
            throw RuntimeDatabaseConnectionException.invalidConfiguration(databaseName, ex);
        }
    }
}
