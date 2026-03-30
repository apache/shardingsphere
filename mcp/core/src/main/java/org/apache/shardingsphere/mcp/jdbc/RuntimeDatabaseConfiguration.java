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

import lombok.Getter;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Runtime database configuration for one logical database binding.
 */
@Getter
public final class RuntimeDatabaseConfiguration {
    
    private final String databaseType;
    
    private final String jdbcUrl;
    
    private final String username;
    
    private final String password;
    
    private final String driverClassName;
    
    public RuntimeDatabaseConfiguration(final String databaseType, final String jdbcUrl, final String username, final String password, final String driverClassName) {
        this.databaseType = validateDatabaseType(databaseType);
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
    }
    
    private String validateDatabaseType(final String databaseType) {
        ShardingSpherePreconditions.checkNotNull(databaseType, () -> new IllegalArgumentException("databaseType cannot be null."));
        ShardingSpherePreconditions.checkState(!databaseType.isBlank(), () -> new IllegalArgumentException("databaseType cannot be empty."));
        return databaseType;
    }
    
    /**
     * Open connection.
     *
     * @param databaseName database name
     * @return connection
     * @throws SQLException SQL exception
     */
    public Connection openConnection(final String databaseName) throws SQLException {
        loadDriver(databaseName);
        Properties props = new Properties();
        if (!username.isEmpty()) {
            props.setProperty("user", username);
        }
        if (!password.isEmpty()) {
            props.setProperty("password", password);
        }
        return props.isEmpty() ? DriverManager.getConnection(jdbcUrl) : DriverManager.getConnection(jdbcUrl, props);
    }
    
    private void loadDriver(final String databaseName) {
        if (driverClassName.isEmpty()) {
            return;
        }
        try {
            Class.forName(driverClassName);
        } catch (final ClassNotFoundException ex) {
            throw new IllegalStateException(String.format("JDBC driver `%s` is not available for database `%s`.", driverClassName, databaseName), ex);
        }
    }
}
