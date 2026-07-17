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

package org.apache.shardingsphere.mcp.core.completion.provider;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.core.metadata.GovernanceMetadataQueryService;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSequence;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProvider;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProviderResult;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionRequest;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.resource.MCPUriPathSegmentUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * Metadata completion provider.
 */
public final class MetadataCompletionProvider implements MCPCompletionProvider<MCPFeatureRequestContext> {
    
    private static final Set<String> STORAGE_UNIT_ARGUMENTS = Set.of("storageUnit", "storage_unit", "write_storage_unit", "source_storage_unit", "shadow_storage_unit");
    
    private final GovernanceMetadataQueryService governanceMetadataQueryService = new GovernanceMetadataQueryService();
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public boolean supports(final MCPCompletionRequest request) {
        return MetadataCompletionTarget.UNKNOWN != MetadataCompletionTarget.from(request.getArgumentName());
    }
    
    @Override
    public MCPCompletionProviderResult complete(final MCPFeatureRequestContext handlerContext, final MCPCompletionRequest request) {
        MetadataCompletionTarget target = MetadataCompletionTarget.from(request.getArgumentName());
        Map<String, String> contextArguments = new LinkedHashMap<>(request.getContextArguments());
        Map<String, Object> inferredContextArguments = applyContextDefaults(handlerContext, target, contextArguments);
        Collection<String> missingContextArguments = createMissingContextArguments(target, contextArguments);
        MetadataCompletionTarget nearestTarget = missingContextArguments.isEmpty() ? target : MetadataCompletionTarget.from(missingContextArguments.iterator().next());
        String nearestResourceUri = createNearestResourceUri(nearestTarget, contextArguments);
        return new MCPCompletionProviderResult(
                completeMetadata(handlerContext, target, contextArguments), inferredContextArguments, missingContextArguments, nearestResourceUri);
    }
    
    private Map<String, Object> applyContextDefaults(final MCPFeatureRequestContext handlerContext, final MetadataCompletionTarget target,
                                                     final Map<String, String> contextArguments) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.putAll(applySingleDatabaseDefault(handlerContext, target, contextArguments));
        mergeInferredContextArguments(contextArguments, result);
        result.putAll(applySingleSchemaDefault(handlerContext, target, contextArguments));
        mergeInferredContextArguments(contextArguments, result);
        return result;
    }
    
    private Map<String, Object> applySingleDatabaseDefault(final MCPFeatureRequestContext handlerContext, final MetadataCompletionTarget target,
                                                           final Map<String, String> contextArguments) {
        if (!target.requires("database") || !Objects.toString(contextArguments.get("database"), "").isEmpty()) {
            return Map.of();
        }
        List<RuntimeDatabaseProfile> databaseProfiles = handlerContext.getCapabilityFacade().getDatabaseProfiles();
        if (1 != databaseProfiles.size()) {
            return Map.of();
        }
        String database = Objects.toString(databaseProfiles.get(0).getDatabase(), "");
        return database.isEmpty() ? Map.of() : Map.of("database", database);
    }
    
    private Map<String, Object> applySingleSchemaDefault(final MCPFeatureRequestContext handlerContext, final MetadataCompletionTarget target,
                                                         final Map<String, String> contextArguments) {
        if (!target.requires("schema") || !Objects.toString(contextArguments.get("schema"), "").isEmpty()
                || Objects.toString(contextArguments.get("database"), "").isEmpty()) {
            return Map.of();
        }
        List<ShardingSphereSchema> schemas = handlerContext.getMetadataQueryFacade().querySchemas(contextArguments.get("database"));
        return 1 == schemas.size() ? Map.of("schema", schemas.iterator().next().getName()) : Map.of();
    }
    
    private void mergeInferredContextArguments(final Map<String, String> contextArguments, final Map<String, Object> inferredContextArguments) {
        for (Entry<String, Object> entry : inferredContextArguments.entrySet()) {
            if (Objects.toString(contextArguments.get(entry.getKey()), "").isEmpty()) {
                contextArguments.put(entry.getKey(), Objects.toString(entry.getValue(), ""));
            }
        }
    }
    
    private Collection<MCPCompletionCandidate> completeMetadata(final MCPFeatureRequestContext handlerContext, final MetadataCompletionTarget target,
                                                                final Map<String, String> contextArguments) {
        return switch (target) {
            case DATABASE -> completeDatabases(handlerContext);
            case SCHEMA -> completeSchemas(handlerContext, contextArguments);
            case TABLE -> completeTables(handlerContext, contextArguments);
            case COLUMN -> completeColumns(handlerContext, contextArguments);
            case INDEX -> completeIndexes(handlerContext, contextArguments);
            case SEQUENCE -> completeSequences(handlerContext, contextArguments);
            case STORAGE_UNIT -> completeStorageUnits(handlerContext, contextArguments);
            case UNKNOWN -> List.of();
        };
    }
    
    private List<MCPCompletionCandidate> completeDatabases(final MCPFeatureRequestContext handlerContext) {
        return handlerContext.getMetadataQueryFacade().queryDatabases().stream()
                .map(each -> new MCPCompletionCandidate(each.getDatabase(), String.format("%s %s", each.getDatabaseType(), each.getDatabaseVersion()), "metadata")).toList();
    }
    
    private List<MCPCompletionCandidate> completeSchemas(final MCPFeatureRequestContext handlerContext, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        return database.isEmpty() ? List.of()
                : handlerContext.getMetadataQueryFacade().querySchemas(database).stream().map(ShardingSphereSchema::getName)
                        .map(each -> new MCPCompletionCandidate(each, "schema", "metadata")).toList();
    }
    
    private List<MCPCompletionCandidate> completeTables(final MCPFeatureRequestContext handlerContext, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        String schema = getSchema(contextArguments);
        return database.isEmpty() || schema.isEmpty() ? List.of()
                : handlerContext.getMetadataQueryFacade().queryTables(database, schema).stream().map(ShardingSphereTable::getName)
                        .map(each -> new MCPCompletionCandidate(each, "logical table", "metadata")).toList();
    }
    
    private List<MCPCompletionCandidate> completeColumns(final MCPFeatureRequestContext handlerContext, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        String schema = getSchema(contextArguments);
        String table = contextArguments.getOrDefault("table", "");
        return database.isEmpty() || schema.isEmpty() || table.isEmpty() ? List.of()
                : handlerContext.getMetadataQueryFacade().queryTableColumns(database, schema, table).stream().map(MCPColumnMetadata::getName)
                        .map(each -> new MCPCompletionCandidate(each, "column", "metadata")).toList();
    }
    
    private List<MCPCompletionCandidate> completeIndexes(final MCPFeatureRequestContext handlerContext, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        String schema = getSchema(contextArguments);
        String table = contextArguments.getOrDefault("table", "");
        if (database.isEmpty() || schema.isEmpty() || table.isEmpty()) {
            return List.of();
        }
        try {
            return handlerContext.getMetadataQueryFacade().queryIndexes(database, schema, table).stream().map(ShardingSphereIndex::getName)
                    .map(each -> new MCPCompletionCandidate(each, "index", "metadata")).toList();
        } catch (final MCPUnsupportedException ignored) {
            return List.of();
        }
    }
    
    private List<MCPCompletionCandidate> completeSequences(final MCPFeatureRequestContext handlerContext, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        String schema = getSchema(contextArguments);
        if (database.isEmpty() || schema.isEmpty()) {
            return List.of();
        }
        try {
            return handlerContext.getMetadataQueryFacade().querySequences(database, schema).stream().map(ShardingSphereSequence::getName)
                    .map(each -> new MCPCompletionCandidate(each, "sequence", "metadata")).toList();
        } catch (final MCPUnsupportedException ignored) {
            return List.of();
        }
    }
    
    private List<MCPCompletionCandidate> completeStorageUnits(final MCPFeatureRequestContext handlerContext, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        if (database.isEmpty()) {
            return List.of();
        }
        MCPFeatureQueryFacade queryFacade = handlerContext.getQueryFacade();
        return governanceMetadataQueryService.queryStorageUnits(queryFacade, database).stream()
                .map(each -> Objects.toString(each.get("name"), ""))
                .filter(each -> !each.isEmpty())
                .map(each -> new MCPCompletionCandidate(each, "storage unit", "metadata")).toList();
    }
    
    private String getSchema(final Map<String, String> contextArguments) {
        return Objects.toString(contextArguments.get("schema"), "");
    }
    
    private List<String> createMissingContextArguments(final MetadataCompletionTarget target, final Map<String, String> contextArguments) {
        return target.requiredContextArguments.stream().filter(each -> Objects.toString(contextArguments.get(each), "").isEmpty()).toList();
    }
    
    private String createNearestResourceUri(final MetadataCompletionTarget target, final Map<String, String> contextArguments) {
        String database = Objects.toString(contextArguments.get("database"), "");
        String schema = Objects.toString(contextArguments.get("schema"), "");
        String table = Objects.toString(contextArguments.get("table"), "");
        return switch (target) {
            case DATABASE -> "shardingsphere://databases";
            case SCHEMA -> database.isEmpty() ? "" : String.format("shardingsphere://databases/%s/schemas", encode(database));
            case TABLE -> database.isEmpty() || schema.isEmpty() ? "" : String.format("shardingsphere://databases/%s/schemas/%s/tables", encode(database), encode(schema));
            case SEQUENCE -> database.isEmpty() || schema.isEmpty()
                    ? "" : String.format("shardingsphere://databases/%s/schemas/%s/sequences", encode(database), encode(schema));
            case STORAGE_UNIT -> database.isEmpty() ? "" : String.format("shardingsphere://databases/%s/storage-units", encode(database));
            case COLUMN -> database.isEmpty() || schema.isEmpty() || table.isEmpty()
                    ? "" : String.format("shardingsphere://databases/%s/schemas/%s/tables/%s/columns", encode(database), encode(schema), encode(table));
            case INDEX -> database.isEmpty() || schema.isEmpty() || table.isEmpty()
                    ? "" : String.format("shardingsphere://databases/%s/schemas/%s/tables/%s/indexes", encode(database), encode(schema), encode(table));
            case UNKNOWN -> "";
        };
    }
    
    private String encode(final String value) {
        return MCPUriPathSegmentUtils.encodePathSegment(value);
    }
    
    private enum MetadataCompletionTarget {
        
        DATABASE("database"),
        SCHEMA("schema", "database"),
        TABLE("table", "database", "schema"),
        COLUMN("column", "database", "schema", "table"),
        INDEX("index", "database", "schema", "table"),
        SEQUENCE("sequence", "database", "schema"),
        STORAGE_UNIT("storageUnit", "database"),
        UNKNOWN("");
        
        private final String argumentName;
        
        private final List<String> requiredContextArguments;
        
        MetadataCompletionTarget(final String argumentName, final String... requiredContextArguments) {
            this.argumentName = argumentName;
            this.requiredContextArguments = List.of(requiredContextArguments);
        }
        
        private static MetadataCompletionTarget from(final String argumentName) {
            String canonicalArgumentName = STORAGE_UNIT_ARGUMENTS.contains(argumentName) ? "storageUnit" : argumentName;
            for (MetadataCompletionTarget each : values()) {
                if (each.argumentName.equals(canonicalArgumentName)) {
                    return each;
                }
            }
            return UNKNOWN;
        }
        
        private boolean requires(final String argumentName) {
            return requiredContextArguments.contains(argumentName);
        }
    }
}
