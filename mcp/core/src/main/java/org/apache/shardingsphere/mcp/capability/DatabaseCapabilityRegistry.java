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

import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for the MCP database capability matrix.
 */
public final class DatabaseCapabilityRegistry {
    
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
        return Optional.ofNullable(capabilities.get(normalizeDatabaseType(databaseType)));
    }
    
    /**
     * Get a stable snapshot of the registered capabilities.
     *
     * @return immutable capability collection
     */
    public Collection<DatabaseCapability> getRegisteredCapabilities() {
        return Collections.unmodifiableCollection(capabilities.values());
    }
    
    private static String normalizeDatabaseType(final String databaseType) {
        return Objects.requireNonNull(databaseType, "databaseType cannot be null").trim().toUpperCase(Locale.ENGLISH);
    }
    
    private static <T extends Enum<T>> Set<T> toImmutableEnumSet(final Set<T> values, final Class<T> enumType) {
        if (null == values || values.isEmpty()) {
            return Collections.unmodifiableSet(EnumSet.noneOf(enumType));
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(values));
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
    
    private static Set<String> createSupportedTransactionStatements(final TransactionCapability transactionCapability) {
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
    
    private static Set<String> toImmutableStrings(final Set<String> values) {
        Set<String> result = new LinkedHashSet<>(Objects.requireNonNull(values, "supportedTransactionStatements" + " cannot be null").size());
        for (String each : values) {
            result.add(Objects.requireNonNull(each, "supportedTransactionStatements" + " cannot contain null"));
        }
        return Collections.unmodifiableSet(result);
    }
    
    /**
     * Database-level capability definition.
     */
    @Getter
    public static final class DatabaseCapability {
        
        private final String databaseType;
        
        private final String minSupportedVersion;
        
        private final Set<SupportedObjectType> supportedObjectTypes;
        
        private final Set<StatementClass> supportedStatementClasses;
        
        private final boolean supportsTransactionControl;
        
        private final boolean supportsSavepoint;
        
        private final Set<String> supportedTransactionStatements;
        
        private final TransactionCapability transactionCapability;
        
        private final boolean defaultAutocommit;
        
        private final boolean crossSchemaQuerySupported;
        
        private final boolean supportsExplainAnalyze;
        
        private final int maxRowsDefault;
        
        private final int maxTimeoutMsDefault;
        
        private final SchemaSemantics defaultSchemaSemantics;
        
        private final TransactionBoundaryBehavior ddlTransactionBehavior;
        
        private final TransactionBoundaryBehavior dclTransactionBehavior;
        
        private final ResultBehavior explainAnalyzeResultBehavior;
        
        private final TransactionBoundaryBehavior explainAnalyzeTransactionBehavior;
        
        /**
         * Construct a database capability definition.
         *
         * @param databaseType database type
         * @param supportedObjectTypes supported object types
         * @param supportedStatementClasses supported statement classes
         * @param transactionCapability transaction capability
         * @param defaultAutocommit default autocommit flag
         * @param crossSchemaQuerySupported cross-schema query support flag
         */
        public DatabaseCapability(final String databaseType, final Set<SupportedObjectType> supportedObjectTypes, final Set<StatementClass> supportedStatementClasses,
                                  final TransactionCapability transactionCapability, final boolean defaultAutocommit, final boolean crossSchemaQuerySupported) {
            this(databaseType, "BASELINE", supportedObjectTypes, supportedStatementClasses, transactionCapability, createSupportedTransactionStatements(transactionCapability),
                    defaultAutocommit, 1000, 30000, SchemaSemantics.NATIVE_SCHEMA, crossSchemaQuerySupported,
                    null != supportedStatementClasses && supportedStatementClasses.contains(StatementClass.EXPLAIN_ANALYZE),
                    TransactionBoundaryBehavior.NATIVE, TransactionBoundaryBehavior.NATIVE, ResultBehavior.RESULT_SET, TransactionBoundaryBehavior.NATIVE);
        }
        
        /**
         * Construct a database capability definition with the full capability matrix fields.
         *
         * @param databaseType database type
         * @param minSupportedVersion minimum supported version label
         * @param supportedObjectTypes supported object types
         * @param supportedStatementClasses supported statement classes
         * @param transactionCapability transaction capability
         * @param supportedTransactionStatements supported transaction statements
         * @param defaultAutocommit default autocommit flag
         * @param maxRowsDefault default row limit
         * @param maxTimeoutMsDefault default timeout limit
         * @param defaultSchemaSemantics default schema semantics
         * @param crossSchemaQuerySupported cross-schema query support flag
         * @param supportsExplainAnalyze explain-analyze support flag
         * @param ddlTransactionBehavior DDL transaction behavior
         * @param dclTransactionBehavior DCL transaction behavior
         * @param explainAnalyzeResultBehavior explain-analyze result behavior
         * @param explainAnalyzeTransactionBehavior explain-analyze transaction behavior
         */
        public DatabaseCapability(final String databaseType, final String minSupportedVersion, final Set<SupportedObjectType> supportedObjectTypes,
                                  final Set<StatementClass> supportedStatementClasses, final TransactionCapability transactionCapability,
                                  final Set<String> supportedTransactionStatements, final boolean defaultAutocommit, final int maxRowsDefault,
                                  final int maxTimeoutMsDefault, final SchemaSemantics defaultSchemaSemantics, final boolean crossSchemaQuerySupported,
                                  final boolean supportsExplainAnalyze, final TransactionBoundaryBehavior ddlTransactionBehavior,
                                  final TransactionBoundaryBehavior dclTransactionBehavior, final ResultBehavior explainAnalyzeResultBehavior,
                                  final TransactionBoundaryBehavior explainAnalyzeTransactionBehavior) {
            this.databaseType = normalizeDatabaseType(databaseType);
            this.minSupportedVersion = Objects.requireNonNull(minSupportedVersion, "minSupportedVersion cannot be null");
            this.supportedObjectTypes = toImmutableEnumSet(supportedObjectTypes, SupportedObjectType.class);
            this.supportedStatementClasses = toImmutableEnumSet(supportedStatementClasses, StatementClass.class);
            this.transactionCapability = Objects.requireNonNull(transactionCapability, "transactionCapability cannot be null");
            this.supportsTransactionControl = TransactionCapability.NONE != transactionCapability;
            this.supportsSavepoint = TransactionCapability.LOCAL_WITH_SAVEPOINT == transactionCapability;
            this.supportedTransactionStatements = toImmutableStrings(supportedTransactionStatements);
            this.defaultAutocommit = defaultAutocommit;
            this.maxRowsDefault = maxRowsDefault;
            this.maxTimeoutMsDefault = maxTimeoutMsDefault;
            this.defaultSchemaSemantics = Objects.requireNonNull(defaultSchemaSemantics, "defaultSchemaSemantics cannot be null");
            this.crossSchemaQuerySupported = crossSchemaQuerySupported;
            this.supportsExplainAnalyze = supportsExplainAnalyze;
            this.ddlTransactionBehavior = Objects.requireNonNull(ddlTransactionBehavior, "ddlTransactionBehavior cannot be null");
            this.dclTransactionBehavior = Objects.requireNonNull(dclTransactionBehavior, "dclTransactionBehavior cannot be null");
            this.explainAnalyzeResultBehavior = Objects.requireNonNull(explainAnalyzeResultBehavior, "explainAnalyzeResultBehavior cannot be null");
            this.explainAnalyzeTransactionBehavior = Objects.requireNonNull(explainAnalyzeTransactionBehavior, "explainAnalyzeTransactionBehavior cannot be null");
        }
    }
    
    /**
     * Supported public object types.
     */
    public enum SupportedObjectType {
        
        DATABASE, SCHEMA, TABLE, VIEW, COLUMN, INDEX, CAPABILITY
    }
    
    /**
     * Supported statement classes.
     */
    public enum StatementClass {
        
        QUERY, DML, DDL, DCL, TRANSACTION_CONTROL, SAVEPOINT, EXPLAIN_ANALYZE
    }
    
    /**
     * Transaction capability levels exposed by the capability matrix.
     */
    public enum TransactionCapability {
        
        NONE, LOCAL, LOCAL_WITH_SAVEPOINT
    }
    
    /**
     * Default schema semantics exposed by database-level capability.
     */
    public enum SchemaSemantics {
        
        NATIVE_SCHEMA, DATABASE_AS_SCHEMA
    }
    
    /**
     * Transaction boundary behavior labels exposed by capability.
     */
    public enum TransactionBoundaryBehavior {
        
        UNIFORM, NATIVE, UNSUPPORTED
    }
    
    /**
     * Result behavior labels exposed by capability.
     */
    public enum ResultBehavior {
        
        RESULT_SET, STATEMENT_ACK, UNSUPPORTED
    }
}
