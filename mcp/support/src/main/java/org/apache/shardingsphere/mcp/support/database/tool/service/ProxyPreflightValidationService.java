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

package org.apache.shardingsphere.mcp.support.database.tool.service;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.MCPJdbcDatabaseProfileLoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.MCPJdbcMetadataLoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.support.database.tool.request.ProxyPreflightValidationRequest;
import org.apache.shardingsphere.mcp.support.database.tool.response.ProxyPreflightCheckResult;
import org.apache.shardingsphere.mcp.support.database.tool.response.ProxyPreflightValidationResult;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Proxy preflight validation service.
 */
@RequiredArgsConstructor
public final class ProxyPreflightValidationService {
    
    private static final String PRE_FLIGHT_BINDING_DATABASE = "__preflight_validation__";
    
    private final MCPJdbcDatabaseProfileLoader profileLoader;
    
    private final MCPJdbcMetadataLoader metadataLoader;
    
    public ProxyPreflightValidationService() {
        this(new MCPJdbcDatabaseProfileLoader(), new MCPJdbcMetadataLoader());
    }
    
    /**
     * Validate proxy preflight request.
     *
     * @param request validation request
     * @param runtimeDatabaseResolver runtime database resolver
     * @param recoveryFactory runtime recovery factory
     * @return validation result
     */
    public ProxyPreflightValidationResult validate(final ProxyPreflightValidationRequest request,
                                                   final Function<String, Optional<RuntimeDatabaseConfiguration>> runtimeDatabaseResolver,
                                                   final Function<RuntimeDatabaseConnectionException, Map<String, Object>> recoveryFactory) {
        List<ProxyPreflightCheckResult> checks = new LinkedList<>();
        String database = normalize(request.getDatabase());
        Optional<RuntimeDatabaseConfiguration> runtimeDatabaseConfig = findRuntimeDatabaseConfiguration(database, runtimeDatabaseResolver);
        if (runtimeDatabaseConfig.isEmpty()) {
            RuntimeDatabaseConnectionException ex = createMissingRuntimeDatabaseException(database);
            checks.add(ProxyPreflightCheckResult.failed("configuration", ex.getCategory(), "The requested database is not configured for this MCP runtime."));
            appendSkippedChecks(checks, "jdbc_driver", "configuration validation did not finish");
            appendSkippedChecks(checks, "jdbc_connectivity", "configuration validation did not finish");
            appendSkippedChecks(checks, "metadata_read", "configuration validation did not finish");
            appendSkippedChecks(checks, "database_visibility", "configuration validation did not finish");
            return createFailureResult(database, checks, ex, recoveryFactory);
        }
        checks.add(ProxyPreflightCheckResult.passed("configuration", "Resolved the configured runtime database."));
        RuntimeDatabaseProfile databaseProfile;
        try {
            databaseProfile = profileLoader.load(database, runtimeDatabaseConfig.get());
            checks.add(ProxyPreflightCheckResult.passed("jdbc_driver", "Loaded the configured JDBC driver."));
            checks.add(ProxyPreflightCheckResult.passed("jdbc_connectivity", "Opened a JDBC connection and validated the configured database type."));
        } catch (final RuntimeDatabaseConnectionException ex) {
            appendProfileFailureChecks(checks, ex);
            appendSkippedChecks(checks, "metadata_read", "driver or connectivity validation failed");
            appendSkippedChecks(checks, "database_visibility", "driver or connectivity validation failed");
            return createFailureResult(database, checks, ex, recoveryFactory);
        }
        MCPDatabaseMetadata databaseMetadata;
        try {
            databaseMetadata = metadataLoader.load(database, runtimeDatabaseConfig.get(), databaseProfile);
            checks.add(ProxyPreflightCheckResult.passed("metadata_read", "Read metadata through the configured JDBC connection."));
        } catch (final RuntimeDatabaseConnectionException ex) {
            checks.add(ProxyPreflightCheckResult.failed("metadata_read", ex.getCategory(), "Failed to read metadata through the configured JDBC connection."));
            appendSkippedChecks(checks, "database_visibility", "metadata validation failed");
            return createFailureResult(database, checks, ex, recoveryFactory);
        }
        try {
            validateDatabaseVisibility(database, runtimeDatabaseConfig.get(), databaseMetadata);
            checks.add(ProxyPreflightCheckResult.passed("database_visibility", "Validated the requested database name against visible JDBC metadata and connection context."));
        } catch (final RuntimeDatabaseConnectionException ex) {
            checks.add(ProxyPreflightCheckResult.failed("database_visibility", ex.getCategory(), "The requested database name is not visible to the configured JDBC connection."));
            return createFailureResult(database, checks, ex, recoveryFactory);
        }
        return ProxyPreflightValidationResult.ready(database, checks);
    }
    
    private Optional<RuntimeDatabaseConfiguration> findRuntimeDatabaseConfiguration(final String database,
                                                                                    final Function<String, Optional<RuntimeDatabaseConfiguration>> runtimeDatabaseResolver) {
        return database.isEmpty() ? Optional.empty() : runtimeDatabaseResolver.apply(database);
    }
    
    private RuntimeDatabaseConnectionException createMissingRuntimeDatabaseException(final String database) {
        return RuntimeDatabaseConnectionException.invalidConfiguration(resolveExceptionDatabaseName(database),
                new IllegalStateException("Proxy preflight validation requires one configured runtime database."));
    }
    
    private void appendProfileFailureChecks(final List<ProxyPreflightCheckResult> checks, final RuntimeDatabaseConnectionException ex) {
        if (RuntimeDatabaseConnectionException.CATEGORY_MISSING_JDBC_DRIVER.equals(ex.getCategory())) {
            checks.add(ProxyPreflightCheckResult.failed("jdbc_driver", ex.getCategory(), "Failed to load the configured JDBC driver."));
            appendSkippedChecks(checks, "jdbc_connectivity", "driver loading failed");
            return;
        }
        checks.add(ProxyPreflightCheckResult.passed("jdbc_driver", "Loaded the configured JDBC driver."));
        checks.add(ProxyPreflightCheckResult.failed("jdbc_connectivity", ex.getCategory(), "Failed to open a JDBC connection or validate the configured database type."));
    }
    
    private void appendSkippedChecks(final List<ProxyPreflightCheckResult> checks, final String name, final String reason) {
        checks.add(ProxyPreflightCheckResult.skipped(name, String.format("Skipped because %s.", reason)));
    }
    
    private ProxyPreflightValidationResult createFailureResult(final String database, final List<ProxyPreflightCheckResult> checks, final RuntimeDatabaseConnectionException cause,
                                                               final Function<RuntimeDatabaseConnectionException, Map<String, Object>> recoveryFactory) {
        return ProxyPreflightValidationResult.failed(database, checks, cause.getCategory(), recoveryFactory.apply(cause));
    }
    
    private void validateDatabaseVisibility(final String database, final RuntimeDatabaseConfiguration runtimeDatabaseConfig, final MCPDatabaseMetadata databaseMetadata) {
        if (containsVisibleSchema(databaseMetadata, database)) {
            return;
        }
        try (Connection connection = runtimeDatabaseConfig.openConnection(resolveExceptionDatabaseName(database))) {
            if (isVisibleDatabase(connection, database)) {
                return;
            }
        } catch (final SQLException ex) {
            throw RuntimeDatabaseConnectionException.connectionFailed(resolveExceptionDatabaseName(database), ex);
        }
        throw RuntimeDatabaseConnectionException.databaseNotVisible(resolveExceptionDatabaseName(database),
                new IllegalStateException(String.format("Requested database `%s` is not visible to the configured JDBC connection.", database)));
    }
    
    private boolean containsVisibleSchema(final MCPDatabaseMetadata databaseMetadata, final String database) {
        for (MCPSchemaMetadata each : databaseMetadata.getSchemas()) {
            if (database.equalsIgnoreCase(each.getSchema())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isVisibleDatabase(final Connection connection, final String database) throws SQLException {
        return matches(connection.getCatalog(), database)
                || matches(connection.getSchema(), database)
                || containsCatalog(connection.getMetaData(), database)
                || containsSchema(connection.getMetaData(), database);
    }
    
    private boolean containsCatalog(final DatabaseMetaData databaseMetaData, final String database) throws SQLException {
        try (ResultSet resultSet = databaseMetaData.getCatalogs()) {
            while (resultSet.next()) {
                if (matches(resultSet.getString(1), database)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean containsSchema(final DatabaseMetaData databaseMetaData, final String database) throws SQLException {
        try (ResultSet resultSet = databaseMetaData.getSchemas()) {
            while (resultSet.next()) {
                if (matches(resultSet.getString("TABLE_SCHEM"), database)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean matches(final String actualValue, final String expectedValue) {
        return !Objects.toString(actualValue, "").trim().isEmpty() && actualValue.trim().equalsIgnoreCase(expectedValue);
    }
    
    private String resolveExceptionDatabaseName(final String database) {
        return database.isEmpty() ? PRE_FLIGHT_BINDING_DATABASE : database;
    }
    
    private String normalize(final String value) {
        return Objects.toString(value, "").trim();
    }
}
