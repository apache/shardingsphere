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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Registry for the MCP database capability matrix.
 */
public final class DatabaseCapabilityRegistry {
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?.*");
    
    private final Map<String, DatabaseCapability> capabilities = new LinkedHashMap<>();
    
    /**
     * Create the default MCP capability registry.
     *
     * @return default capability registry
     */
    public static DatabaseCapabilityRegistry createDefault() {
        DatabaseCapabilityRegistry result = new DatabaseCapabilityRegistry();
        result.register(createDefaultCapability("MySQL", TransactionCapability.LOCAL_WITH_SAVEPOINT, true, SchemaSemantics.DATABASE_AS_SCHEMA, false));
        result.register(createDefaultCapability("PostgreSQL", TransactionCapability.LOCAL_WITH_SAVEPOINT, true, SchemaSemantics.NATIVE_SCHEMA, true));
        result.register(createDefaultCapability("openGauss", TransactionCapability.LOCAL_WITH_SAVEPOINT, true, SchemaSemantics.NATIVE_SCHEMA, true));
        result.register(createDefaultCapability("SQLServer", TransactionCapability.LOCAL_WITH_SAVEPOINT, true, SchemaSemantics.NATIVE_SCHEMA, true));
        result.register(createDefaultCapability("MariaDB", TransactionCapability.LOCAL_WITH_SAVEPOINT, true, SchemaSemantics.DATABASE_AS_SCHEMA, false));
        result.register(createDefaultCapability("Oracle", TransactionCapability.LOCAL_WITH_SAVEPOINT, true, SchemaSemantics.NATIVE_SCHEMA, true));
        result.register(createDefaultCapability("ClickHouse", TransactionCapability.NONE, false, SchemaSemantics.DATABASE_AS_SCHEMA, false));
        result.register(createDefaultCapability("Doris", TransactionCapability.LOCAL, true, SchemaSemantics.DATABASE_AS_SCHEMA, false));
        result.register(createDefaultCapability("Hive", TransactionCapability.NONE, false, SchemaSemantics.DATABASE_AS_SCHEMA, false));
        result.register(createDefaultCapability("Presto", TransactionCapability.LOCAL, false, SchemaSemantics.NATIVE_SCHEMA, true));
        result.register(createDefaultCapability("Firebird", TransactionCapability.LOCAL_WITH_SAVEPOINT, true, SchemaSemantics.NATIVE_SCHEMA, true));
        result.register(createDefaultCapability("H2", TransactionCapability.LOCAL_WITH_SAVEPOINT, true, SchemaSemantics.NATIVE_SCHEMA, true));
        return result;
    }
    
    /**
     * Register a database capability definition.
     *
     * @param capability capability definition
     */
    public void register(final DatabaseCapability capability) {
        DatabaseCapability actualCapability = Objects.requireNonNull(capability, "capability cannot be null");
        capabilities.put(actualCapability.getDatabaseType(), actualCapability);
    }
    
    /**
     * Find a capability definition by database type.
     *
     * @param databaseType database type
     * @return capability definition when present
     */
    public Optional<DatabaseCapability> find(final String databaseType) {
        return find(databaseType, "");
    }
    
    /**
     * Find a capability definition by database type and version.
     *
     * @param databaseType database type
     * @param databaseVersion database version
     * @return capability definition when present
     */
    public Optional<DatabaseCapability> find(final String databaseType, final String databaseVersion) {
        return Optional.ofNullable(capabilities.get(normalizeDatabaseType(databaseType))).map(each -> applyVersionAwareOverrides(each, databaseVersion));
    }
    
    /**
     * Get a stable snapshot of the registered capabilities.
     *
     * @return capability collection
     */
    public Collection<DatabaseCapability> getRegisteredCapabilities() {
        return new LinkedList<>(capabilities.values());
    }
    
    static String normalizeDatabaseType(final String databaseType) {
        return Objects.requireNonNull(databaseType, "databaseType cannot be null").trim().toUpperCase(Locale.ENGLISH);
    }
    
    static <T> T requireNonNull(final T value, final String message) {
        return Objects.requireNonNull(value, message);
    }
    
    private DatabaseCapability applyVersionAwareOverrides(final DatabaseCapability capability, final String databaseVersion) {
        boolean supportsExplainAnalyze = isExplainAnalyzeSupported(capability.getDatabaseType(), databaseVersion);
        if (supportsExplainAnalyze == capability.isSupportsExplainAnalyze()) {
            return capability;
        }
        return new DatabaseCapability(capability.getDatabaseType(), capability.getMinSupportedVersion(), capability.getSupportedObjectTypes(),
                adjustSupportedStatementClasses(capability.getSupportedStatementClasses(), supportsExplainAnalyze), capability.getTransactionCapability(),
                capability.getSupportedTransactionStatements(),
                capability.isDefaultAutocommit(), capability.getMaxRowsDefault(), capability.getMaxTimeoutMsDefault(), capability.getDefaultSchemaSemantics(),
                capability.isCrossSchemaQuerySupported(), supportsExplainAnalyze, capability.getDdlTransactionBehavior(), capability.getDclTransactionBehavior(),
                supportsExplainAnalyze ? ResultBehavior.RESULT_SET : ResultBehavior.UNSUPPORTED,
                supportsExplainAnalyze ? TransactionBoundaryBehavior.NATIVE : TransactionBoundaryBehavior.UNSUPPORTED);
    }
    
    private Set<StatementClass> adjustSupportedStatementClasses(final Set<StatementClass> supportedStatementClasses, final boolean supportsExplainAnalyze) {
        Set<StatementClass> result = new LinkedHashSet<>(supportedStatementClasses);
        if (supportsExplainAnalyze) {
            result.add(StatementClass.EXPLAIN_ANALYZE);
        } else {
            result.remove(StatementClass.EXPLAIN_ANALYZE);
        }
        return result;
    }
    
    private boolean isExplainAnalyzeSupported(final String databaseType, final String databaseVersion) {
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
    
    private boolean isVersionAtLeast(final String databaseVersion, final int major, final int minor, final int patch) {
        Matcher matcher = VERSION_PATTERN.matcher(null == databaseVersion ? "" : databaseVersion.trim());
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
    
    private static DatabaseCapability createDefaultCapability(final String databaseType, final TransactionCapability transactionCapability,
                                                              final boolean indexSupported, final SchemaSemantics defaultSchemaSemantics,
                                                              final boolean crossSchemaQuerySupported) {
        Set<SupportedObjectType> supportedObjectTypes = createSupportedObjectTypes(indexSupported);
        Set<StatementClass> supportedStatementClasses = createSupportedStatementClasses(transactionCapability);
        return new DatabaseCapability(databaseType, "BASELINE", supportedObjectTypes, supportedStatementClasses, transactionCapability,
                createSupportedTransactionStatements(transactionCapability), true, 1000, 30000, defaultSchemaSemantics, crossSchemaQuerySupported,
                false, TransactionBoundaryBehavior.NATIVE, TransactionBoundaryBehavior.NATIVE, ResultBehavior.UNSUPPORTED, TransactionBoundaryBehavior.UNSUPPORTED);
    }
    
    private static Set<SupportedObjectType> createSupportedObjectTypes(final boolean indexSupported) {
        Set<SupportedObjectType> result = new LinkedHashSet<>();
        result.add(SupportedObjectType.DATABASE);
        result.add(SupportedObjectType.SCHEMA);
        result.add(SupportedObjectType.TABLE);
        result.add(SupportedObjectType.VIEW);
        result.add(SupportedObjectType.COLUMN);
        if (indexSupported) {
            result.add(SupportedObjectType.INDEX);
        }
        result.add(SupportedObjectType.CAPABILITY);
        return result;
    }
    
    private static Set<StatementClass> createSupportedStatementClasses(final TransactionCapability transactionCapability) {
        Set<StatementClass> result = new LinkedHashSet<>();
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
        return result;
    }
    
    static Set<String> createSupportedTransactionStatements(final TransactionCapability transactionCapability) {
        Set<String> result = new LinkedHashSet<>();
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
    
}
