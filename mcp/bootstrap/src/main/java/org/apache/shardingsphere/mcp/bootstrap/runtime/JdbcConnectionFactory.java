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

package org.apache.shardingsphere.mcp.bootstrap.runtime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Open JDBC connections for one runtime database configuration.
 */
final class JdbcConnectionFactory {
    
    Connection openConnection(final DatabaseConnectionConfiguration connectionConfiguration) throws SQLException {
        loadDriver(connectionConfiguration);
        Properties props = new Properties();
        if (!connectionConfiguration.getUsername().isEmpty()) {
            props.setProperty("user", connectionConfiguration.getUsername());
        }
        if (!connectionConfiguration.getPassword().isEmpty()) {
            props.setProperty("password", connectionConfiguration.getPassword());
        }
        return props.isEmpty() ? DriverManager.getConnection(connectionConfiguration.getJdbcUrl())
                : DriverManager.getConnection(connectionConfiguration.getJdbcUrl(), props);
    }
    
    private void loadDriver(final DatabaseConnectionConfiguration connectionConfiguration) {
        if (connectionConfiguration.getDriverClassName().isEmpty()) {
            return;
        }
        try {
            Class.forName(connectionConfiguration.getDriverClassName());
        } catch (final ClassNotFoundException ex) {
            throw new IllegalStateException(String.format("JDBC driver `%s` is not available for database `%s`.",
                    connectionConfiguration.getDriverClassName(), connectionConfiguration.getDatabase()), ex);
        }
    }
}
