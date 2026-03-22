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
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityRegistry.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityRegistry.ResultBehavior;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityRegistry.SchemaSemantics;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityRegistry.StatementClass;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityRegistry.SupportedObjectType;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityRegistry.TransactionBoundaryBehavior;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.RuntimeDatabaseDescriptor;

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
    
    private final DatabaseCapabilityRegistry registry;
    
    private final Optional<MetadataCatalog> metadataCatalog;
    
    /**
     * Construct an assembler with the default V1 registry.
     */
    public DatabaseCapabilityAssembler() {
        this(DatabaseCapabilityRegistry.createDefault(), Optional.empty());
    }
    
    /**
     * Construct an assembler with runtime metadata facts.
     *
     * @param metadataCatalog metadata catalog
     */
    public DatabaseCapabilityAssembler(final MetadataCatalog metadataCatalog) {
        this(DatabaseCapabilityRegistry.createDefault(), Optional.of(Objects.requireNonNull(metadataCatalog, "metadataCatalog cannot be null")));
    }
    
    /**
     * Construct an assembler with a caller-provided registry.
     *
     * @param registry capability registry
     */
    public DatabaseCapabilityAssembler(final DatabaseCapabilityRegistry registry) {
        this(registry, Optional.empty());
    }
    
    private DatabaseCapabilityAssembler(final DatabaseCapabilityRegistry registry, final Optional<MetadataCatalog> metadataCatalog) {
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
        return registry.find(databaseType).map(each -> overlayRuntimeFacts(createDatabaseCapability(actualDatabase, each)));
    }
    
    private DatabaseCapabilityView overlayRuntimeFacts(final DatabaseCapabilityView databaseCapabilityView) {
        Optional<RuntimeDatabaseDescriptor> runtimeDescriptor = metadataCatalog.flatMap(each -> each.findRuntimeDatabaseDescriptor(databaseCapabilityView.getDatabase()));
        if (runtimeDescriptor.isEmpty()) {
            return databaseCapabilityView;
        }
        Set<SupportedObjectType> supportedObjectTypes = runtimeDescriptor.get().getSupportedObjectTypes().isEmpty()
                ? databaseCapabilityView.getSupportedObjectTypes()
                : runtimeDescriptor.get().getSupportedObjectTypes();
        return new DatabaseCapabilityView(databaseCapabilityView.getDatabase(), runtimeDescriptor.get().getDatabaseType(),
                databaseCapabilityView.getMinSupportedVersion(), supportedObjectTypes, databaseCapabilityView.getSupportedStatementClasses(),
                databaseCapabilityView.isSupportsTransactionControl(), databaseCapabilityView.isSupportsSavepoint(),
                databaseCapabilityView.getSupportedTransactionStatements(), databaseCapabilityView.isDefaultAutocommit(),
                databaseCapabilityView.getMaxRowsDefault(), databaseCapabilityView.getMaxTimeoutMsDefault(),
                databaseCapabilityView.getDefaultSchemaSemantics(), runtimeDescriptor.get().isSupportsCrossSchemaSql(),
                runtimeDescriptor.get().isSupportsExplainAnalyze(), databaseCapabilityView.getDdlTransactionBehavior(),
                databaseCapabilityView.getDclTransactionBehavior(), databaseCapabilityView.getExplainAnalyzeResultBehavior(),
                databaseCapabilityView.getExplainAnalyzeTransactionBehavior());
    }
    
    private static DatabaseCapabilityView createDatabaseCapability(final String database, final DatabaseCapability capability) {
        return new DatabaseCapabilityView(database, capability.getDatabaseType(), capability.getMinSupportedVersion(), capability.getSupportedObjectTypes(),
                capability.getSupportedStatementClasses(), capability.isSupportsTransactionControl(), capability.isSupportsSavepoint(),
                capability.getSupportedTransactionStatements(), capability.isDefaultAutocommit(), capability.getMaxRowsDefault(),
                capability.getMaxTimeoutMsDefault(), capability.getDefaultSchemaSemantics(), capability.isCrossSchemaQuerySupported(),
                capability.isSupportsExplainAnalyze(), capability.getDdlTransactionBehavior(), capability.getDclTransactionBehavior(),
                capability.getExplainAnalyzeResultBehavior(), capability.getExplainAnalyzeTransactionBehavior());
    }
    
    private static <T> List<T> toImmutableList(final Collection<T> values) {
        return Collections.unmodifiableList(new LinkedList<>(Objects.requireNonNull(values, "values cannot be null")));
    }
    
    private static <T extends Enum<T>> Set<T> toImmutableEnumSet(final Set<T> values, final Class<T> enumType) {
        if (values.isEmpty()) {
            return Collections.unmodifiableSet(EnumSet.noneOf(enumType));
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(values));
    }
    
    private static Set<String> toImmutableStrings(final Collection<String> values) {
        Set<String> result = new LinkedHashSet<>(Objects.requireNonNull(values, "supportedTransactionStatements" + " cannot be null").size());
        for (String each : values) {
            result.add(Objects.requireNonNull(each, "supportedTransactionStatements" + " cannot contain null"));
        }
        return Collections.unmodifiableSet(result);
    }
    
    /**
     * Service-level capability view.
     */
    @Getter
    public static final class ServiceCapability {
        
        private final List<String> supportedResources;
        
        private final List<String> supportedTools;
        
        private final Set<StatementClass> supportedStatementClasses;
        
        ServiceCapability(final List<String> supportedResources, final List<String> supportedTools, final Set<StatementClass> supportedStatementClasses) {
            this.supportedResources = toImmutableList(supportedResources);
            this.supportedTools = toImmutableList(supportedTools);
            this.supportedStatementClasses = toImmutableEnumSet(Objects.requireNonNull(supportedStatementClasses, "supportedStatementClasses cannot be null"),
                    StatementClass.class);
        }
    }
    
    /**
     * Database-level capability view.
     */
    @Getter
    public static final class DatabaseCapabilityView {
        
        private final String database;
        
        private final String databaseType;
        
        private final String minSupportedVersion;
        
        private final Set<SupportedObjectType> supportedObjectTypes;
        
        private final Set<StatementClass> supportedStatementClasses;
        
        private final boolean supportsTransactionControl;
        
        private final boolean supportsSavepoint;
        
        private final Set<String> supportedTransactionStatements;
        
        private final boolean defaultAutocommit;
        
        private final int maxRowsDefault;
        
        private final int maxTimeoutMsDefault;
        
        private final SchemaSemantics defaultSchemaSemantics;
        
        private final boolean supportsCrossSchemaSql;
        
        private final boolean supportsExplainAnalyze;
        
        private final TransactionBoundaryBehavior ddlTransactionBehavior;
        
        private final TransactionBoundaryBehavior dclTransactionBehavior;
        
        private final ResultBehavior explainAnalyzeResultBehavior;
        
        private final TransactionBoundaryBehavior explainAnalyzeTransactionBehavior;
        
        DatabaseCapabilityView(final String database, final String databaseType, final String minSupportedVersion, final Set<SupportedObjectType> supportedObjectTypes,
                               final Set<StatementClass> supportedStatementClasses, final boolean supportsTransactionControl, final boolean supportsSavepoint,
                               final Set<String> supportedTransactionStatements, final boolean defaultAutocommit, final int maxRowsDefault,
                               final int maxTimeoutMsDefault, final SchemaSemantics defaultSchemaSemantics, final boolean supportsCrossSchemaSql,
                               final boolean supportsExplainAnalyze, final TransactionBoundaryBehavior ddlTransactionBehavior,
                               final TransactionBoundaryBehavior dclTransactionBehavior, final ResultBehavior explainAnalyzeResultBehavior,
                               final TransactionBoundaryBehavior explainAnalyzeTransactionBehavior) {
            this.database = Objects.requireNonNull(database, "database cannot be null");
            this.databaseType = Objects.requireNonNull(databaseType, "databaseType cannot be null");
            this.minSupportedVersion = Objects.requireNonNull(minSupportedVersion, "minSupportedVersion cannot be null");
            this.supportedObjectTypes = toImmutableEnumSet(Objects.requireNonNull(supportedObjectTypes, "supportedObjectTypes cannot be null"),
                    SupportedObjectType.class);
            this.supportedStatementClasses = toImmutableEnumSet(Objects.requireNonNull(supportedStatementClasses, "supportedStatementClasses cannot be null"),
                    StatementClass.class);
            this.supportsTransactionControl = supportsTransactionControl;
            this.supportsSavepoint = supportsSavepoint;
            this.supportedTransactionStatements = toImmutableStrings(supportedTransactionStatements);
            this.defaultAutocommit = defaultAutocommit;
            this.maxRowsDefault = maxRowsDefault;
            this.maxTimeoutMsDefault = maxTimeoutMsDefault;
            this.defaultSchemaSemantics = Objects.requireNonNull(defaultSchemaSemantics, "defaultSchemaSemantics cannot be null");
            this.supportsCrossSchemaSql = supportsCrossSchemaSql;
            this.supportsExplainAnalyze = supportsExplainAnalyze;
            this.ddlTransactionBehavior = Objects.requireNonNull(ddlTransactionBehavior, "ddlTransactionBehavior cannot be null");
            this.dclTransactionBehavior = Objects.requireNonNull(dclTransactionBehavior, "dclTransactionBehavior cannot be null");
            this.explainAnalyzeResultBehavior = Objects.requireNonNull(explainAnalyzeResultBehavior, "explainAnalyzeResultBehavior cannot be null");
            this.explainAnalyzeTransactionBehavior = Objects.requireNonNull(explainAnalyzeTransactionBehavior, "explainAnalyzeTransactionBehavior cannot be null");
        }
    }
}
