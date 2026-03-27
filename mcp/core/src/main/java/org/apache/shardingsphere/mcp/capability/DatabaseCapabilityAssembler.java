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

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Assemble MCP service-level and database-level capability views.
 */
public final class DatabaseCapabilityAssembler {
    
    private static final List<String> SUPPORTED_RESOURCES = List.of(
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
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}");
    
    private static final List<String> SUPPORTED_TOOLS = List.of(
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
            "execute_query");
    
    private static final Set<StatementClass> SUPPORTED_STATEMENT_CLASSES = Set.of(StatementClass.values());
    
    private final DatabaseCapabilityRegistry registry;
    
    private final MetadataCatalog metadataCatalog;
    
    /**
     * Construct an assembler with runtime metadata facts.
     *
     * @param metadataCatalog metadata catalog
     */
    public DatabaseCapabilityAssembler(final MetadataCatalog metadataCatalog) {
        registry = DatabaseCapabilityRegistry.createDefault();
        this.metadataCatalog = metadataCatalog;
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
        Optional<RuntimeDatabaseDescriptor> runtimeDescriptor = metadataCatalog.findRuntimeDatabaseDescriptor(database);
        String actualDatabaseType = runtimeDescriptor.map(RuntimeDatabaseDescriptor::getDatabaseType).orElse(databaseType);
        String actualDatabaseVersion = runtimeDescriptor.map(RuntimeDatabaseDescriptor::getDatabaseVersion).orElse("");
        return registry.find(actualDatabaseType, actualDatabaseVersion).map(each -> overlayRuntimeFacts(createDatabaseCapability(database, each)));
    }
    
    private DatabaseCapabilityView overlayRuntimeFacts(final DatabaseCapabilityView databaseCapabilityView) {
        Optional<RuntimeDatabaseDescriptor> runtimeDescriptor = metadataCatalog.findRuntimeDatabaseDescriptor(databaseCapabilityView.getDatabase());
        if (runtimeDescriptor.isEmpty()) {
            return databaseCapabilityView;
        }
        Set<SupportedObjectType> supportedObjectTypes = runtimeDescriptor.get().getSupportedObjectTypes().isEmpty()
                ? databaseCapabilityView.getSupportedObjectTypes()
                : runtimeDescriptor.get().getSupportedObjectTypes();
        boolean supportsCrossSchemaSql = databaseCapabilityView.isSupportsCrossSchemaSql();
        boolean supportsExplainAnalyze = databaseCapabilityView.isSupportsExplainAnalyze();
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
    
}
