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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * MCP JDBC connection factory.
 */
public final class MCPJdbcConnectionFactory {
    
    /**
     * Open connection.
     *
     * @param databaseName database name
     * @param databaseConfig runtime database configuration
     * @return connection
     * @throws SQLException SQL exception
     */
    public Connection openConnection(final String databaseName, final RuntimeDatabaseConfiguration databaseConfig) throws SQLException {
        loadDriver(databaseName, databaseConfig);
        Properties props = new Properties();
        if (!databaseConfig.getUsername().isEmpty()) {
            props.setProperty("user", databaseConfig.getUsername());
        }
        if (!databaseConfig.getPassword().isEmpty()) {
            props.setProperty("password", databaseConfig.getPassword());
        }
        return props.isEmpty() ? DriverManager.getConnection(databaseConfig.getJdbcUrl()) : DriverManager.getConnection(databaseConfig.getJdbcUrl(), props);
    }
    
    private void loadDriver(final String databaseName, final RuntimeDatabaseConfiguration runtimeDatabaseConfig) {
        if (runtimeDatabaseConfig.getDriverClassName().isEmpty()) {
            return;
        }
        try {
            Class.forName(runtimeDatabaseConfig.getDriverClassName());
        } catch (final ClassNotFoundException ex) {
            throw new IllegalStateException(String.format("JDBC driver `%s` is not available for database `%s`.", runtimeDatabaseConfig.getDriverClassName(), databaseName), ex);
        }
    }
}
