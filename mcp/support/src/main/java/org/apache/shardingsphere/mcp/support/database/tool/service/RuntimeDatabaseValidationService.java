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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.MCPJdbcDatabaseProfileLoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.MCPJdbcMetadataLoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.tool.request.RuntimeDatabaseValidationRequest;
import org.apache.shardingsphere.mcp.support.database.tool.response.RuntimeDatabaseValidationCheckResult;
import org.apache.shardingsphere.mcp.support.database.tool.response.RuntimeDatabaseValidationResult;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Runtime database validation service.
 */
public final class RuntimeDatabaseValidationService {
    
    private static final String VALIDATION_BINDING_DATABASE = "__preflight_validation__";
    
    private final MCPJdbcDatabaseProfileLoader profileLoader = new MCPJdbcDatabaseProfileLoader();
    
    private final MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
    
    /**
     * Validate runtime database.
     *
     * @param request validation request
     * @param runtimeDatabaseResolver runtime database resolver
     * @param recoveryFactory runtime recovery factory
     * @return validation result
     */
    public RuntimeDatabaseValidationResult validate(final RuntimeDatabaseValidationRequest request,
                                                    final Function<String, Optional<RuntimeDatabaseConfiguration>> runtimeDatabaseResolver,
                                                    final Function<RuntimeDatabaseConnectionException, Map<String, Object>> recoveryFactory) {
        List<RuntimeDatabaseValidationCheckResult> checks = new LinkedList<>();
        String database = normalize(request.getDatabase());
        Optional<RuntimeDatabaseConfiguration> runtimeDatabaseConfig = findRuntimeDatabaseConfiguration(database, runtimeDatabaseResolver);
        if (runtimeDatabaseConfig.isEmpty()) {
            RuntimeDatabaseConnectionException ex = createMissingRuntimeDatabaseException(database);
            checks.add(RuntimeDatabaseValidationCheckResult.failed("configuration", ex.getCategory(), "The requested database is not configured for this MCP runtime."));
            appendSkippedChecks(checks, "jdbc_driver", "configuration validation did not finish");
            appendSkippedChecks(checks, "jdbc_connectivity", "configuration validation did not finish");
            appendSkippedChecks(checks, "metadata_read", "configuration validation did not finish");
            appendSkippedChecks(checks, "database_visibility", "configuration validation did not finish");
            return createFailureResult(database, checks, ex, recoveryFactory);
        }
        checks.add(RuntimeDatabaseValidationCheckResult.passed("configuration", "Resolved the configured runtime database."));
        RuntimeDatabaseProfile databaseProfile;
        try {
            databaseProfile = profileLoader.load(database, runtimeDatabaseConfig.get());
            checks.add(RuntimeDatabaseValidationCheckResult.passed("jdbc_driver", "Loaded the configured JDBC driver."));
            checks.add(RuntimeDatabaseValidationCheckResult.passed("jdbc_connectivity", "Opened a JDBC connection and resolved the database type from the JDBC URL."));
        } catch (final RuntimeDatabaseConnectionException ex) {
            appendProfileFailureChecks(checks, ex);
            appendSkippedChecks(checks, "metadata_read", "driver or connectivity validation failed");
            appendSkippedChecks(checks, "database_visibility", "driver or connectivity validation failed");
            return createFailureResult(database, checks, ex, recoveryFactory);
        }
        Collection<ShardingSphereSchema> schemas;
        try {
            schemas = metadataLoader.load(database, runtimeDatabaseConfig.get(), databaseProfile);
            checks.add(RuntimeDatabaseValidationCheckResult.passed("metadata_read", "Read metadata through the configured JDBC connection."));
        } catch (final RuntimeDatabaseConnectionException ex) {
            checks.add(RuntimeDatabaseValidationCheckResult.failed("metadata_read", ex.getCategory(), "Failed to read metadata through the configured JDBC connection."));
            appendSkippedChecks(checks, "database_visibility", "metadata validation failed");
            return createFailureResult(database, checks, ex, recoveryFactory);
        }
        try {
            validateDatabaseVisibility(database, runtimeDatabaseConfig.get(), schemas, databaseProfile.getIdentifierCasePolicySet());
            checks.add(RuntimeDatabaseValidationCheckResult.passed("database_visibility", "Validated the requested database name against visible JDBC metadata and connection context."));
        } catch (final RuntimeDatabaseConnectionException ex) {
            checks.add(RuntimeDatabaseValidationCheckResult.failed("database_visibility", ex.getCategory(), "The requested database name is not visible to the configured JDBC connection."));
            return createFailureResult(database, checks, ex, recoveryFactory);
        }
        return RuntimeDatabaseValidationResult.ready(database, checks);
    }
    
    private Optional<RuntimeDatabaseConfiguration> findRuntimeDatabaseConfiguration(final String database,
                                                                                    final Function<String, Optional<RuntimeDatabaseConfiguration>> runtimeDatabaseResolver) {
        return database.isEmpty() ? Optional.empty() : runtimeDatabaseResolver.apply(database);
    }
    
    private RuntimeDatabaseConnectionException createMissingRuntimeDatabaseException(final String database) {
        return RuntimeDatabaseConnectionException.invalidConfiguration(resolveExceptionDatabaseName(database),
                new IllegalStateException("Runtime database validation requires one configured runtime database."));
    }
    
    private void appendProfileFailureChecks(final List<RuntimeDatabaseValidationCheckResult> checks, final RuntimeDatabaseConnectionException ex) {
        if (RuntimeDatabaseConnectionException.CATEGORY_MISSING_JDBC_DRIVER.equals(ex.getCategory())) {
            checks.add(RuntimeDatabaseValidationCheckResult.failed("jdbc_driver", ex.getCategory(), "Failed to load the configured JDBC driver."));
            appendSkippedChecks(checks, "jdbc_connectivity", "driver loading failed");
            return;
        }
        checks.add(RuntimeDatabaseValidationCheckResult.passed("jdbc_driver", "Loaded the configured JDBC driver."));
        checks.add(RuntimeDatabaseValidationCheckResult.failed("jdbc_connectivity", ex.getCategory(), "Failed to open a JDBC connection or resolve the database type from the JDBC URL."));
    }
    
    private void appendSkippedChecks(final List<RuntimeDatabaseValidationCheckResult> checks, final String name, final String reason) {
        checks.add(RuntimeDatabaseValidationCheckResult.skipped(name, String.format("Skipped because %s.", reason)));
    }
    
    private RuntimeDatabaseValidationResult createFailureResult(final String database, final List<RuntimeDatabaseValidationCheckResult> checks, final RuntimeDatabaseConnectionException cause,
                                                                final Function<RuntimeDatabaseConnectionException, Map<String, Object>> recoveryFactory) {
        return RuntimeDatabaseValidationResult.failed(database, checks, cause.getCategory(), recoveryFactory.apply(cause));
    }
    
    private void validateDatabaseVisibility(final String database, final RuntimeDatabaseConfiguration runtimeDatabaseConfig, final Collection<ShardingSphereSchema> schemas,
                                            final IdentifierCasePolicySet identifierCasePolicySet) {
        if (containsVisibleSchema(schemas, database, identifierCasePolicySet.getPolicy(IdentifierScope.SCHEMA))) {
            return;
        }
        try (Connection connection = runtimeDatabaseConfig.openConnection(resolveExceptionDatabaseName(database))) {
            if (isVisibleDatabase(connection, database, identifierCasePolicySet)) {
                return;
            }
        } catch (final SQLException ex) {
            throw RuntimeDatabaseConnectionException.connectionFailed(resolveExceptionDatabaseName(database), ex);
        }
        throw RuntimeDatabaseConnectionException.databaseNotVisible(resolveExceptionDatabaseName(database),
                new IllegalStateException(String.format("Requested database `%s` is not visible to the configured JDBC connection.", database)));
    }
    
    private boolean containsVisibleSchema(final Collection<ShardingSphereSchema> schemas, final String database, final IdentifierCasePolicy identifierCasePolicy) {
        for (ShardingSphereSchema each : schemas) {
            if (matches(each.getName(), database, identifierCasePolicy)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isVisibleDatabase(final Connection connection, final String database, final IdentifierCasePolicySet identifierCasePolicySet) throws SQLException {
        IdentifierCasePolicy databasePolicy = identifierCasePolicySet.getPolicy(IdentifierScope.DATABASE);
        IdentifierCasePolicy schemaPolicy = identifierCasePolicySet.getPolicy(IdentifierScope.SCHEMA);
        return matches(connection.getCatalog(), database, databasePolicy)
                || matches(connection.getSchema(), database, schemaPolicy)
                || containsCatalog(connection.getMetaData(), database, databasePolicy)
                || containsSchema(connection.getMetaData(), database, schemaPolicy);
    }
    
    private boolean containsCatalog(final DatabaseMetaData databaseMetaData, final String database, final IdentifierCasePolicy identifierCasePolicy) throws SQLException {
        try (ResultSet resultSet = databaseMetaData.getCatalogs()) {
            while (resultSet.next()) {
                if (matches(resultSet.getString(1), database, identifierCasePolicy)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean containsSchema(final DatabaseMetaData databaseMetaData, final String database, final IdentifierCasePolicy identifierCasePolicy) throws SQLException {
        try (ResultSet resultSet = databaseMetaData.getSchemas()) {
            while (resultSet.next()) {
                if (matches(resultSet.getString("TABLE_SCHEM"), database, identifierCasePolicy)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean matches(final String storedName, final String identifier, final IdentifierCasePolicy identifierCasePolicy) {
        String actualStoredName = Objects.toString(storedName, "").trim();
        return !actualStoredName.isEmpty() && identifierCasePolicy.matches(actualStoredName, identifier, QuoteCharacter.NONE);
    }
    
    private String resolveExceptionDatabaseName(final String database) {
        return database.isEmpty() ? VALIDATION_BINDING_DATABASE : database;
    }
    
    private String normalize(final String value) {
        return Objects.toString(value, "").trim();
    }
}
