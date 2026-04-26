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

package org.apache.shardingsphere.mcp.metadata.jdbc;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityOption;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
    
    private static final String DORIS_VERSION_COMMENT_QUERY = "SELECT @@version_comment";
    
    private static final String MARIADB_VERSION_QUERY = "SELECT VERSION()";
    
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
     * @throws IllegalStateException when profile loading fails
     */
    public RuntimeDatabaseProfile load(final String databaseName, final RuntimeDatabaseConfiguration runtimeDatabaseConfig) {
        try (Connection connection = runtimeDatabaseConfig.openConnection(databaseName)) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String databaseType = resolveActualDatabaseType(databaseName, runtimeDatabaseConfig.getDatabaseType(), connection, databaseMetaData);
            String databaseVersion = Objects.toString(databaseMetaData.getDatabaseProductVersion(), "").trim();
            return new RuntimeDatabaseProfile(databaseName, databaseType, databaseVersion);
        } catch (final SQLException ex) {
            throw new IllegalStateException(String.format("Failed to load database profile for database `%s`.", databaseName), ex);
        }
    }
    
    private String resolveActualDatabaseType(final String databaseName, final String configuredDatabaseType,
                                             final Connection connection, final DatabaseMetaData databaseMetaData) throws SQLException {
        String actualDatabaseType = determineActualDatabaseType(databaseName, databaseMetaData);
        String configuredType = Objects.toString(configuredDatabaseType, "").trim();
        if (configuredType.isEmpty()) {
            return actualDatabaseType;
        }
        String expectedDatabaseType = normalizeDatabaseType(configuredType);
        Optional<String> compatibleBranchDatabaseType = resolveCompatibleBranchDatabaseType(connection, expectedDatabaseType, actualDatabaseType);
        if (compatibleBranchDatabaseType.isPresent()) {
            return compatibleBranchDatabaseType.get();
        }
        if (!expectedDatabaseType.equalsIgnoreCase(actualDatabaseType)) {
            throw new IllegalStateException(String.format("Configured databaseType `%s` does not match actual database type `%s` for database `%s`.",
                    configuredType, actualDatabaseType, databaseName));
        }
        return actualDatabaseType;
    }
    
    private String normalizeDatabaseType(final String databaseType) {
        return TypedSPILoader.findService(MCPDatabaseCapabilityOption.class, databaseType).map(MCPDatabaseCapabilityOption::getType).orElse(databaseType);
    }
    
    private String determineActualDatabaseType(final String databaseName, final DatabaseMetaData databaseMetaData) throws SQLException {
        String productName = Objects.toString(databaseMetaData.getDatabaseProductName(), "").trim();
        String jdbcUrl = Objects.toString(databaseMetaData.getURL(), "").trim();
        String result = resolveDatabaseTypeFromProductName(productName, jdbcUrl);
        if (null == result) {
            result = resolveDatabaseTypeFromJdbcUrl(jdbcUrl);
        }
        if (null == result) {
            throw new IllegalStateException(String.format("Actual database type cannot be determined for database `%s`.", databaseName));
        }
        return result;
    }
    
    private String resolveDatabaseTypeFromProductName(final String productName, final String jdbcUrl) {
        if (!productName.isEmpty()) {
            if (productName.toUpperCase(Locale.ENGLISH).contains("POSTGRESQL")) {
                return jdbcUrl.toLowerCase(Locale.ENGLISH).startsWith("jdbc:opengauss:") ? "openGauss" : "PostgreSQL";
            }
            if (productName.toUpperCase(Locale.ENGLISH).contains("SQL SERVER")) {
                return "SQLServer";
            }
            if (productName.toUpperCase(Locale.ENGLISH).contains("MARIADB")) {
                return "MariaDB";
            }
            if (productName.toUpperCase(Locale.ENGLISH).contains("MYSQL")) {
                return "MySQL";
            }
            if (productName.toUpperCase(Locale.ENGLISH).contains("ORACLE")) {
                return "Oracle";
            }
            if (productName.toUpperCase(Locale.ENGLISH).contains("FIREBIRD")) {
                return "Firebird";
            }
            if (productName.toUpperCase(Locale.ENGLISH).contains("H2")) {
                return "H2";
            }
        }
        return TypedSPILoader.findService(MCPDatabaseCapabilityOption.class, productName).map(MCPDatabaseCapabilityOption::getType).orElse(null);
    }
    
    private String resolveDatabaseTypeFromJdbcUrl(final String jdbcUrl) {
        String actualJdbcUrl = jdbcUrl.toLowerCase(Locale.ENGLISH);
        if (actualJdbcUrl.startsWith("jdbc:opengauss:")) {
            return "openGauss";
        }
        if (actualJdbcUrl.startsWith("jdbc:postgresql:")) {
            return "PostgreSQL";
        }
        if (actualJdbcUrl.startsWith("jdbc:sqlserver:")) {
            return "SQLServer";
        }
        if (actualJdbcUrl.startsWith("jdbc:mariadb:")) {
            return "MariaDB";
        }
        if (actualJdbcUrl.startsWith("jdbc:mysql:")) {
            return "MySQL";
        }
        if (actualJdbcUrl.startsWith("jdbc:oracle:")) {
            return "Oracle";
        }
        if (actualJdbcUrl.startsWith("jdbc:firebirdsql:")) {
            return "Firebird";
        }
        if (actualJdbcUrl.startsWith("jdbc:h2:")) {
            return "H2";
        }
        return null;
    }
    
    private Optional<String> resolveCompatibleBranchDatabaseType(final Connection connection, final String configuredDatabaseType,
                                                                 final String actualDatabaseType) throws SQLException {
        if (!"MYSQL".equalsIgnoreCase(actualDatabaseType)) {
            return Optional.empty();
        }
        if ("DORIS".equalsIgnoreCase(configuredDatabaseType) && isBranchDatabase(connection, DORIS_VERSION_COMMENT_QUERY, "DORIS")) {
            return Optional.of("Doris");
        }
        if ("MARIADB".equalsIgnoreCase(configuredDatabaseType) && isBranchDatabase(connection, MARIADB_VERSION_QUERY, "MARIADB")) {
            return Optional.of("MariaDB");
        }
        return Optional.empty();
    }
    
    private boolean isBranchDatabase(final Connection connection, final String query, final String expectedMarker) throws SQLException {
        return executeScalarQuery(connection, query).toUpperCase(Locale.ENGLISH).contains(expectedMarker);
    }
    
    private String executeScalarQuery(final Connection connection, final String query) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            return resultSet.next() ? Objects.toString(resultSet.getString(1), "").trim() : "";
        }
    }
}
