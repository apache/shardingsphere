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

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityOption;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

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
            String databaseType = resolveDatabaseType(databaseName, runtimeDatabaseConfig.getDatabaseType(), databaseMetaData);
            String databaseVersion = Objects.toString(databaseMetaData.getDatabaseProductVersion(), "").trim();
            return new RuntimeDatabaseProfile(databaseName, databaseType, databaseVersion);
        } catch (final SQLException ex) {
            throw RuntimeDatabaseConnectionException.connectionFailed(databaseName, ex);
        }
    }
    
    private String resolveDatabaseType(final String databaseName, final String configuredDatabaseType, final DatabaseMetaData databaseMetaData) throws SQLException {
        String configuredType = Objects.toString(configuredDatabaseType, "").trim();
        return configuredType.isEmpty() ? determineActualDatabaseType(databaseName, databaseMetaData) : normalizeDatabaseType(configuredType);
    }
    
    private String normalizeDatabaseType(final String databaseType) {
        return TypedSPILoader.findService(MCPDatabaseCapabilityOption.class, databaseType).map(MCPDatabaseCapabilityOption::getType).orElse(databaseType);
    }
    
    private String determineActualDatabaseType(final String databaseName, final DatabaseMetaData databaseMetaData) throws SQLException {
        String productName = Objects.toString(databaseMetaData.getDatabaseProductName(), "").trim();
        String jdbcUrl = Objects.toString(databaseMetaData.getURL(), "").trim();
        Optional<String> result = resolveDatabaseTypeFromProductName(productName, jdbcUrl);
        if (result.isEmpty()) {
            result = resolveDatabaseTypeFromJdbcUrl(jdbcUrl);
        }
        if (result.isEmpty()) {
            throw RuntimeDatabaseConnectionException.invalidConfiguration(databaseName,
                    new IllegalStateException(String.format("Actual database type cannot be determined for database `%s`.", databaseName)));
        }
        return result.get();
    }
    
    private Optional<String> resolveDatabaseTypeFromProductName(final String productName, final String jdbcUrl) {
        if (!productName.isEmpty()) {
            String upperProductName = productName.toUpperCase(Locale.ENGLISH);
            if (upperProductName.contains("POSTGRESQL")) {
                return Optional.of(jdbcUrl.toLowerCase(Locale.ENGLISH).startsWith("jdbc:opengauss:") ? "openGauss" : "PostgreSQL");
            }
            if (upperProductName.contains("SQL SERVER")) {
                return Optional.of("SQLServer");
            }
            if (upperProductName.contains("MARIADB")) {
                return Optional.of("MariaDB");
            }
            if (upperProductName.contains("MYSQL")) {
                return Optional.of("MySQL");
            }
            if (upperProductName.contains("ORACLE")) {
                return Optional.of("Oracle");
            }
            if (upperProductName.contains("FIREBIRD")) {
                return Optional.of("Firebird");
            }
        }
        return TypedSPILoader.findService(MCPDatabaseCapabilityOption.class, productName).map(MCPDatabaseCapabilityOption::getType);
    }
    
    private Optional<String> resolveDatabaseTypeFromJdbcUrl(final String jdbcUrl) {
        String actualJdbcUrl = jdbcUrl.toLowerCase(Locale.ENGLISH);
        if (actualJdbcUrl.startsWith("jdbc:opengauss:")) {
            return Optional.of("openGauss");
        }
        if (actualJdbcUrl.startsWith("jdbc:postgresql:")) {
            return Optional.of("PostgreSQL");
        }
        if (actualJdbcUrl.startsWith("jdbc:sqlserver:")) {
            return Optional.of("SQLServer");
        }
        if (actualJdbcUrl.startsWith("jdbc:mariadb:")) {
            return Optional.of("MariaDB");
        }
        if (actualJdbcUrl.startsWith("jdbc:mysql:")) {
            return Optional.of("MySQL");
        }
        if (actualJdbcUrl.startsWith("jdbc:oracle:")) {
            return Optional.of("Oracle");
        }
        if (actualJdbcUrl.startsWith("jdbc:firebirdsql:")) {
            return Optional.of("Firebird");
        }
        return Optional.empty();
    }
}
