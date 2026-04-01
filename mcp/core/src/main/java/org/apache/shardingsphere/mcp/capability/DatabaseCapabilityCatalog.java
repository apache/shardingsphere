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

package org.apache.shardingsphere.mcp.capability;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Built-in MCP database capability catalog.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseCapabilityCatalog {
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?.*");
    
    /**
     * Find one capability definition by database type and version.
     *
     * @param databaseName logical database name
     * @param databaseType database type
     * @param databaseVersion database version
     * @return capability definition when present
     */
    public static Optional<DatabaseCapability> find(final String databaseName, final String databaseType, final String databaseVersion) {
        String normalizedDatabaseType = normalizeDatabaseType(databaseType);
        switch (normalizedDatabaseType) {
            case "MYSQL":
                return Optional.of(createDefaultCapability(databaseName, getCanonicalDatabaseType(normalizedDatabaseType), TransactionCapability.LOCAL_WITH_SAVEPOINT, true,
                        SchemaSemantics.DATABASE_AS_SCHEMA, false, databaseVersion));
            case "POSTGRESQL":
                return Optional.of(createDefaultCapability(databaseName, getCanonicalDatabaseType(normalizedDatabaseType), TransactionCapability.LOCAL_WITH_SAVEPOINT, true,
                        SchemaSemantics.NATIVE_SCHEMA, true, databaseVersion));
            case "OPENGAUSS":
                return Optional.of(createDefaultCapability(databaseName, getCanonicalDatabaseType(normalizedDatabaseType), TransactionCapability.LOCAL_WITH_SAVEPOINT, true,
                        SchemaSemantics.NATIVE_SCHEMA, true, databaseVersion));
            case "SQLSERVER":
                return Optional.of(createDefaultCapability(databaseName, getCanonicalDatabaseType(normalizedDatabaseType), TransactionCapability.LOCAL_WITH_SAVEPOINT, true,
                        SchemaSemantics.NATIVE_SCHEMA, true, databaseVersion));
            case "MARIADB":
                return Optional.of(createDefaultCapability(databaseName, getCanonicalDatabaseType(normalizedDatabaseType), TransactionCapability.LOCAL_WITH_SAVEPOINT, true,
                        SchemaSemantics.DATABASE_AS_SCHEMA, false, databaseVersion));
            case "ORACLE":
                return Optional.of(createDefaultCapability(databaseName, getCanonicalDatabaseType(normalizedDatabaseType), TransactionCapability.LOCAL_WITH_SAVEPOINT, true,
                        SchemaSemantics.NATIVE_SCHEMA, true, databaseVersion));
            case "CLICKHOUSE":
                return Optional.of(createDefaultCapability(databaseName, getCanonicalDatabaseType(normalizedDatabaseType), TransactionCapability.NONE, false,
                        SchemaSemantics.DATABASE_AS_SCHEMA, false, databaseVersion));
            case "DORIS":
                return Optional.of(createDefaultCapability(databaseName, getCanonicalDatabaseType(normalizedDatabaseType), TransactionCapability.LOCAL, true,
                        SchemaSemantics.DATABASE_AS_SCHEMA, false, databaseVersion));
            case "HIVE":
                return Optional.of(createDefaultCapability(databaseName, getCanonicalDatabaseType(normalizedDatabaseType), TransactionCapability.NONE, false,
                        SchemaSemantics.DATABASE_AS_SCHEMA, false, databaseVersion));
            case "PRESTO":
                return Optional.of(createDefaultCapability(databaseName, getCanonicalDatabaseType(normalizedDatabaseType), TransactionCapability.LOCAL, false,
                        SchemaSemantics.NATIVE_SCHEMA, true, databaseVersion));
            case "FIREBIRD":
                return Optional.of(createDefaultCapability(databaseName, getCanonicalDatabaseType(normalizedDatabaseType), TransactionCapability.LOCAL_WITH_SAVEPOINT, true,
                        SchemaSemantics.NATIVE_SCHEMA, true, databaseVersion));
            case "H2":
                return Optional.of(createDefaultCapability(databaseName, getCanonicalDatabaseType(normalizedDatabaseType), TransactionCapability.LOCAL_WITH_SAVEPOINT, true,
                        SchemaSemantics.NATIVE_SCHEMA, true, databaseVersion));
            default:
                return Optional.empty();
        }
    }
    
    static String normalizeDatabaseType(final String databaseType) {
        return databaseType.trim().toUpperCase(Locale.ENGLISH);
    }
    
    private static String getCanonicalDatabaseType(final String normalizedDatabaseType) {
        switch (normalizedDatabaseType) {
            case "MYSQL":
                return "MySQL";
            case "POSTGRESQL":
                return "PostgreSQL";
            case "OPENGAUSS":
                return "openGauss";
            case "SQLSERVER":
                return "SQLServer";
            case "MARIADB":
                return "MariaDB";
            case "ORACLE":
                return "Oracle";
            case "CLICKHOUSE":
                return "ClickHouse";
            case "DORIS":
                return "Doris";
            case "HIVE":
                return "Hive";
            case "PRESTO":
                return "Presto";
            case "FIREBIRD":
                return "Firebird";
            case "H2":
                return "H2";
            default:
                return normalizedDatabaseType;
        }
    }
    
    static Set<String> createSupportedTransactionStatements(final TransactionCapability transactionCapability) {
        Set<String> result = new LinkedHashSet<>(16, 1F);
        if (TransactionCapability.NONE != transactionCapability) {
            result.add("BEGIN");
            result.add("START TRANSACTION");
            result.add("COMMIT");
            result.add("ROLLBACK");
        }
        if (TransactionCapability.LOCAL_WITH_SAVEPOINT == transactionCapability) {
            result.add("SAVEPOINT");
            result.add("ROLLBACK TO SAVEPOINT");
            result.add("RELEASE SAVEPOINT");
        }
        return result;
    }
    
    private static DatabaseCapability createDefaultCapability(final String databaseName, final String databaseType, final TransactionCapability transactionCapability,
                                                              final boolean indexSupported, final SchemaSemantics defaultSchemaSemantics,
                                                              final boolean crossSchemaQuerySupported, final String databaseVersion) {
        boolean supportsExplainAnalyze = isExplainAnalyzeSupported(databaseType, databaseVersion);
        return new DatabaseCapability(databaseName, databaseType, "BASELINE", createSupportedMetadataObjectTypes(indexSupported),
                createSupportedStatementClasses(transactionCapability, supportsExplainAnalyze), TransactionCapability.NONE != transactionCapability,
                TransactionCapability.LOCAL_WITH_SAVEPOINT == transactionCapability, createSupportedTransactionStatements(transactionCapability),
                true, 1000, 30000, defaultSchemaSemantics, crossSchemaQuerySupported, supportsExplainAnalyze,
                TransactionBoundaryBehavior.NATIVE, TransactionBoundaryBehavior.NATIVE,
                supportsExplainAnalyze ? ResultBehavior.RESULT_SET : ResultBehavior.UNSUPPORTED,
                supportsExplainAnalyze ? TransactionBoundaryBehavior.NATIVE : TransactionBoundaryBehavior.UNSUPPORTED);
    }
    
    private static Set<MetadataObjectType> createSupportedMetadataObjectTypes(final boolean indexSupported) {
        Set<MetadataObjectType> result = new LinkedHashSet<>(16, 1F);
        result.add(MetadataObjectType.SCHEMA);
        result.add(MetadataObjectType.TABLE);
        result.add(MetadataObjectType.VIEW);
        result.add(MetadataObjectType.COLUMN);
        if (indexSupported) {
            result.add(MetadataObjectType.INDEX);
        }
        return result;
    }
    
    private static Set<StatementClass> createSupportedStatementClasses(final TransactionCapability transactionCapability, final boolean supportsExplainAnalyze) {
        Set<StatementClass> result = new LinkedHashSet<>(16, 1F);
        result.add(StatementClass.QUERY);
        result.add(StatementClass.DML);
        result.add(StatementClass.DDL);
        result.add(StatementClass.DCL);
        if (TransactionCapability.NONE != transactionCapability) {
            result.add(StatementClass.TRANSACTION_CONTROL);
        }
        if (TransactionCapability.LOCAL_WITH_SAVEPOINT == transactionCapability) {
            result.add(StatementClass.SAVEPOINT);
        }
        if (supportsExplainAnalyze) {
            result.add(StatementClass.EXPLAIN_ANALYZE);
        }
        return result;
    }
    
    private static boolean isExplainAnalyzeSupported(final String databaseType, final String databaseVersion) {
        switch (normalizeDatabaseType(databaseType)) {
            case "POSTGRESQL":
            case "OPENGAUSS":
            case "DORIS":
            case "PRESTO":
            case "H2":
                return true;
            case "MYSQL":
                return isVersionAtLeast(databaseVersion, 8, 0, 18);
            default:
                return false;
        }
    }
    
    private static boolean isVersionAtLeast(final String databaseVersion, final int major, final int minor, final int patch) {
        Matcher matcher = VERSION_PATTERN.matcher(Objects.toString(databaseVersion, "").trim());
        if (!matcher.matches()) {
            return false;
        }
        int actualMajor = Integer.parseInt(matcher.group(1));
        int actualMinor = null == matcher.group(2) ? 0 : Integer.parseInt(matcher.group(2));
        int actualPatch = null == matcher.group(3) ? 0 : Integer.parseInt(matcher.group(3));
        if (actualMajor != major) {
            return actualMajor > major;
        }
        if (actualMinor != minor) {
            return actualMinor > minor;
        }
        return actualPatch >= patch;
    }
}
