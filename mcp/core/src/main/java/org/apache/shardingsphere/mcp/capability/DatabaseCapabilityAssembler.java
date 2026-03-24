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

import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.RuntimeDatabaseDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Assemble MCP service-level and database-level capability views.
 */
public final class DatabaseCapabilityAssembler {
    
    private static final List<String> SUPPORTED_RESOURCES = toImmutableList(List.of(
            "shardingsphere://capabilities",
            "shardingsphere://databases",
            "shardingsphere://databases/{database}",
            "shardingsphere://databases/{database}/capabilities",
            "shardingsphere://databases/{database}/schemas",
            "shardingsphere://databases/{database}/schemas/{schema}",
            "shardingsphere://databases/{database}/schemas/{schema}/tables",
            "shardingsphere://databases/{database}/schemas/{schema}/views",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}",
            "shardingsphere://databases/{database}/schemas/{schema}/views/{view}",
            "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns",
            "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}"));
    
    private static final List<String> SUPPORTED_TOOLS = toImmutableList(List.of(
            "list_databases",
            "list_schemas",
            "list_tables",
            "list_views",
            "list_columns",
            "list_indexes",
            "search_metadata",
            "describe_table",
            "describe_view",
            "get_capabilities",
            "execute_query"));
    
    private static final Set<StatementClass> SUPPORTED_STATEMENT_CLASSES = Collections.unmodifiableSet(EnumSet.allOf(StatementClass.class));
    
    private static final MetadataCatalog EMPTY_METADATA_CATALOG = new MetadataCatalog(Collections.emptyMap(), Collections.emptyList());
    
    private final DatabaseCapabilityRegistry registry;
    
    private final MetadataCatalog metadataCatalog;
    
    /**
     * Construct an assembler with the default capability registry.
     */
    public DatabaseCapabilityAssembler() {
        this(DatabaseCapabilityRegistry.createDefault(), EMPTY_METADATA_CATALOG);
    }
    
    /**
     * Construct an assembler with runtime metadata facts.
     *
     * @param metadataCatalog metadata catalog
     */
    public DatabaseCapabilityAssembler(final MetadataCatalog metadataCatalog) {
        this(DatabaseCapabilityRegistry.createDefault(), metadataCatalog);
    }
    
    /**
     * Construct an assembler with a caller-provided registry.
     *
     * @param registry capability registry
     */
    public DatabaseCapabilityAssembler(final DatabaseCapabilityRegistry registry) {
        this(registry, EMPTY_METADATA_CATALOG);
    }
    
    private DatabaseCapabilityAssembler(final DatabaseCapabilityRegistry registry, final MetadataCatalog metadataCatalog) {
        this.registry = Objects.requireNonNull(registry, "registry cannot be null");
        this.metadataCatalog = Objects.requireNonNull(metadataCatalog, "metadataCatalog cannot be null");
    }
    
    /**
     * Assemble the service-level capability surface.
     *
     * @return service-level capability
     */
    public ServiceCapability assembleServiceCapability() {
        return new ServiceCapability(SUPPORTED_RESOURCES, SUPPORTED_TOOLS, SUPPORTED_STATEMENT_CLASSES);
    }
    
    /**
     * Assemble the database-level capability view for one logical database.
     *
     * @param database logical database name
     * @param databaseType database type
     * @return database-level capability when the database type is supported
     */
    public Optional<DatabaseCapabilityView> assembleDatabaseCapability(final String database, final String databaseType) {
        String actualDatabase = Objects.requireNonNull(database, "database cannot be null");
        Optional<RuntimeDatabaseDescriptor> runtimeDescriptor = metadataCatalog.findRuntimeDatabaseDescriptor(actualDatabase);
        String actualDatabaseType = runtimeDescriptor.map(RuntimeDatabaseDescriptor::getDatabaseType).orElse(databaseType);
        String actualDatabaseVersion = runtimeDescriptor.map(RuntimeDatabaseDescriptor::getDatabaseVersion).orElse("");
        return registry.find(actualDatabaseType, actualDatabaseVersion).map(each -> overlayRuntimeFacts(createDatabaseCapability(actualDatabase, each)));
    }
    
    private DatabaseCapabilityView overlayRuntimeFacts(final DatabaseCapabilityView databaseCapabilityView) {
        Optional<RuntimeDatabaseDescriptor> runtimeDescriptor = metadataCatalog.findRuntimeDatabaseDescriptor(databaseCapabilityView.getDatabase());
        if (runtimeDescriptor.isEmpty()) {
            return databaseCapabilityView;
        }
        Set<SupportedObjectType> supportedObjectTypes = runtimeDescriptor.get().getSupportedObjectTypes().isEmpty()
                ? databaseCapabilityView.getSupportedObjectTypes()
                : runtimeDescriptor.get().getSupportedObjectTypes();
        boolean supportsCrossSchemaSql = runtimeDescriptor.get().isLegacySupportsCrossSchemaSqlConfigured()
                ? runtimeDescriptor.get().isLegacySupportsCrossSchemaSql()
                : databaseCapabilityView.isSupportsCrossSchemaSql();
        boolean supportsExplainAnalyze = runtimeDescriptor.get().isLegacySupportsExplainAnalyzeConfigured()
                ? runtimeDescriptor.get().isLegacySupportsExplainAnalyze()
                : databaseCapabilityView.isSupportsExplainAnalyze();
        ResultBehavior explainAnalyzeResultBehavior = supportsExplainAnalyze ? ResultBehavior.RESULT_SET : ResultBehavior.UNSUPPORTED;
        TransactionBoundaryBehavior explainAnalyzeTransactionBehavior = supportsExplainAnalyze ? TransactionBoundaryBehavior.NATIVE
                : TransactionBoundaryBehavior.UNSUPPORTED;
        return new DatabaseCapabilityView(databaseCapabilityView.getDatabase(), runtimeDescriptor.get().getDatabaseType(),
                databaseCapabilityView.getMinSupportedVersion(), supportedObjectTypes, databaseCapabilityView.getSupportedStatementClasses(),
                databaseCapabilityView.isSupportsTransactionControl(), databaseCapabilityView.isSupportsSavepoint(),
                databaseCapabilityView.getSupportedTransactionStatements(), databaseCapabilityView.isDefaultAutocommit(),
                databaseCapabilityView.getMaxRowsDefault(), databaseCapabilityView.getMaxTimeoutMsDefault(),
                databaseCapabilityView.getDefaultSchemaSemantics(), supportsCrossSchemaSql, supportsExplainAnalyze, databaseCapabilityView.getDdlTransactionBehavior(),
                databaseCapabilityView.getDclTransactionBehavior(), explainAnalyzeResultBehavior, explainAnalyzeTransactionBehavior);
    }
    
    private static DatabaseCapabilityView createDatabaseCapability(final String database, final DatabaseCapability capability) {
        return new DatabaseCapabilityView(database, capability.getDatabaseType(), capability.getMinSupportedVersion(), capability.getSupportedObjectTypes(),
                capability.getSupportedStatementClasses(), capability.isSupportsTransactionControl(), capability.isSupportsSavepoint(),
                capability.getSupportedTransactionStatements(), capability.isDefaultAutocommit(), capability.getMaxRowsDefault(),
                capability.getMaxTimeoutMsDefault(), capability.getDefaultSchemaSemantics(), capability.isCrossSchemaQuerySupported(),
                capability.isSupportsExplainAnalyze(), capability.getDdlTransactionBehavior(), capability.getDclTransactionBehavior(),
                capability.getExplainAnalyzeResultBehavior(), capability.getExplainAnalyzeTransactionBehavior());
    }
    
    static <T> List<T> toImmutableList(final Collection<T> values) {
        return Collections.unmodifiableList(new LinkedList<>(Objects.requireNonNull(values, "values cannot be null")));
    }
    
    static <T extends Enum<T>> Set<T> toImmutableEnumSet(final Set<T> values, final Class<T> enumType) {
        if (values.isEmpty()) {
            return Collections.unmodifiableSet(EnumSet.noneOf(enumType));
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(values));
    }
    
    static Set<String> toImmutableStrings(final Collection<String> values) {
        Set<String> result = new LinkedHashSet<>(Objects.requireNonNull(values, "supportedTransactionStatements" + " cannot be null").size());
        for (String each : values) {
            result.add(Objects.requireNonNull(each, "supportedTransactionStatements" + " cannot contain null"));
        }
        return Collections.unmodifiableSet(result);
    }
}
