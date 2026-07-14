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

package org.apache.shardingsphere.mcp.core.tool.handler.metadata;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSequence;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mcp.core.metadata.GovernanceMetadataQueryService;
import org.apache.shardingsphere.mcp.core.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.core.tool.response.MetadataSearchHit;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class MetadataSearchCollector {
    
    private final MCPMetadataQueryFacade metadataQueryFacade;
    
    private final MCPFeatureQueryFacade queryFacade;
    
    private final GovernanceMetadataQueryService governanceMetadataQueryService;
    
    private final MetadataSearchResourceUriFactory resourceUriFactory;
    
    List<MetadataSearchHit> collect(final MetadataSearchRequest request, final Set<SupportedMCPMetadataObjectType> searchObjectTypes) {
        return request.getDatabase().isEmpty()
                ? metadataQueryFacade.queryDatabases().stream().flatMap(each -> collect(each.getDatabase(), request.getSchema(), searchObjectTypes).stream()).toList()
                : collect(request.getDatabase(), request.getSchema(), searchObjectTypes);
    }
    
    private List<MetadataSearchHit> collect(final String databaseName, final String schemaName, final Set<SupportedMCPMetadataObjectType> searchObjectTypes) {
        List<MetadataSearchHit> result = new LinkedList<>();
        for (SupportedMCPMetadataObjectType each : searchObjectTypes) {
            result.addAll(collect(databaseName, schemaName, each));
        }
        return result;
    }
    
    private List<MetadataSearchHit> collect(final String databaseName, final String schemaName, final SupportedMCPMetadataObjectType objectType) {
        return switch (objectType) {
            case DATABASE -> metadataQueryFacade.queryDatabase(databaseName).map(each -> List.of(createSearchHit(each))).orElseGet(List::of);
            case STORAGE_UNIT -> queryStorageUnitSearchHits(databaseName);
            case SCHEMA, TABLE, VIEW, COLUMN, INDEX, SEQUENCE -> metadataQueryFacade.isSupportedMetadataObjectType(databaseName, objectType)
                    ? querySearchHits(databaseName, objectType, schemaName)
                    : List.of();
            case MATERIALIZED_VIEW, ROUTINE, TRIGGER, EVENT, SYNONYM, DATABASE_SPECIFIC -> List.of();
        };
    }
    
    List<MetadataSearchHit> collectDatabases() {
        return metadataQueryFacade.queryDatabases().stream().map(this::createSearchHit).toList();
    }
    
    private List<MetadataSearchHit> querySearchHits(final String databaseName, final SupportedMCPMetadataObjectType objectType, final String schemaName) {
        return switch (objectType) {
            case SCHEMA -> querySchemaSearchHits(databaseName, schemaName);
            case TABLE -> queryTables(databaseName, schemaName).stream().map(each -> createTableSearchHit(databaseName, each)).toList();
            case VIEW -> queryViews(databaseName, schemaName).stream().map(each -> createViewSearchHit(databaseName, each)).toList();
            case COLUMN -> queryColumnSearchHits(databaseName, schemaName);
            case INDEX -> queryIndexSearchHits(databaseName, schemaName);
            case SEQUENCE -> querySequenceSearchHits(databaseName, schemaName);
            case DATABASE, STORAGE_UNIT, MATERIALIZED_VIEW, ROUTINE, TRIGGER, EVENT, SYNONYM, DATABASE_SPECIFIC -> List.of();
        };
    }
    
    private List<MetadataSearchHit> querySchemaSearchHits(final String databaseName, final String schemaName) {
        List<MetadataSearchHit> result = new LinkedList<>();
        addSchemaSearchHits(result, databaseName, schemaName);
        return result;
    }
    
    private void addSchemaSearchHits(final List<MetadataSearchHit> searchHits, final String databaseName, final String schemaName) {
        if (!schemaName.isEmpty()) {
            metadataQueryFacade.querySchema(databaseName, schemaName).ifPresent(optional -> searchHits.add(createSearchHit(databaseName, optional)));
            return;
        }
        for (ShardingSphereSchema each : metadataQueryFacade.querySchemas(databaseName)) {
            searchHits.add(createSearchHit(databaseName, each));
        }
    }
    
    private List<TableSearchScope> queryTables(final String databaseName, final String schemaName) {
        if (!schemaName.isEmpty()) {
            return metadataQueryFacade.queryTables(databaseName, schemaName).stream().map(each -> new TableSearchScope(schemaName, each)).toList();
        }
        return metadataQueryFacade.querySchemas(databaseName).stream()
                .flatMap(each -> metadataQueryFacade.queryTables(databaseName, each.getName()).stream().map(table -> new TableSearchScope(each.getName(), table))).toList();
    }
    
    private List<TableSearchScope> queryViews(final String databaseName, final String schemaName) {
        List<TableSearchScope> result = new LinkedList<>();
        if (!schemaName.isEmpty()) {
            metadataQueryFacade.queryViews(databaseName, schemaName).forEach(each -> result.add(new TableSearchScope(schemaName, each)));
            return result;
        }
        for (ShardingSphereSchema each : metadataQueryFacade.querySchemas(databaseName)) {
            metadataQueryFacade.queryViews(databaseName, each.getName()).forEach(view -> result.add(new TableSearchScope(each.getName(), view)));
        }
        return result;
    }
    
    private List<MetadataSearchHit> queryColumnSearchHits(final String databaseName, final String schemaName) {
        List<MetadataSearchHit> result = new LinkedList<>();
        for (TableSearchScope each : queryTables(databaseName, schemaName)) {
            for (ShardingSphereColumn column : metadataQueryFacade.queryTableColumns(databaseName, each.schema, each.table.getName())) {
                result.add(createColumnSearchHit(databaseName, each.schema, each.table.getName(), "", column));
            }
        }
        for (TableSearchScope each : queryViews(databaseName, schemaName)) {
            for (ShardingSphereColumn column : metadataQueryFacade.queryViewColumns(databaseName, each.schema, each.table.getName())) {
                result.add(createColumnSearchHit(databaseName, each.schema, "", each.table.getName(), column));
            }
        }
        return result;
    }
    
    private List<MetadataSearchHit> queryIndexSearchHits(final String databaseName, final String schemaName) {
        return queryTables(databaseName, schemaName).stream()
                .flatMap(each -> metadataQueryFacade.queryIndexes(databaseName, each.schema, each.table.getName()).stream()
                        .map(index -> createIndexSearchHit(databaseName, each.schema, each.table.getName(), index)))
                .toList();
    }
    
    private List<MetadataSearchHit> querySequenceSearchHits(final String databaseName, final String schemaName) {
        if (!schemaName.isEmpty()) {
            return metadataQueryFacade.querySequences(databaseName, schemaName).stream().map(each -> createSearchHit(databaseName, schemaName, each)).toList();
        }
        return metadataQueryFacade.querySchemas(databaseName).stream()
                .flatMap(each -> metadataQueryFacade.querySequences(databaseName, each.getName()).stream().map(sequence -> createSearchHit(databaseName, each.getName(), sequence))).toList();
    }
    
    private List<MetadataSearchHit> queryStorageUnitSearchHits(final String databaseName) {
        List<MetadataSearchHit> result = new LinkedList<>();
        for (Map<String, Object> each : governanceMetadataQueryService.queryStorageUnits(queryFacade, databaseName)) {
            String storageUnitName = Objects.toString(each.get("name"), "");
            if (!storageUnitName.isEmpty()) {
                result.add(createSearchHit(databaseName, "", SupportedMCPMetadataObjectType.STORAGE_UNIT, "", "", storageUnitName));
            }
        }
        return result;
    }
    
    private MetadataSearchHit createSearchHit(final RuntimeDatabaseProfile databaseProfile) {
        return createSearchHit(databaseProfile.getDatabase(), "", SupportedMCPMetadataObjectType.DATABASE, "", "", databaseProfile.getDatabase());
    }
    
    private MetadataSearchHit createSearchHit(final String database, final ShardingSphereSchema schema) {
        return createSearchHit(database, schema.getName(), SupportedMCPMetadataObjectType.SCHEMA, "", "", schema.getName());
    }
    
    private MetadataSearchHit createSearchHit(final String database, final String schema, final ShardingSphereSequence sequence) {
        return createSearchHit(database, schema, SupportedMCPMetadataObjectType.SEQUENCE, "", "", sequence.getName());
    }
    
    private MetadataSearchHit createSearchHit(final String database, final String schema, final SupportedMCPMetadataObjectType objectType,
                                              final String table, final String view, final String name) {
        MetadataSearchResourceUriFactory.MetadataResourceUris resourceUris = resourceUriFactory.create(database, schema, objectType, table, view, name);
        return MetadataSearchHit.builder()
                .database(database)
                .schema(schema)
                .objectType(objectType.name().toLowerCase(Locale.ENGLISH))
                .table(table)
                .view(view)
                .name(name)
                .resource(resourceUris.resource())
                .parentResource(resourceUris.parentResource())
                .nextResources(resourceUris.nextResources())
                .derivationStatus(resourceUris.derivationStatus())
                .derivationReason(resourceUris.derivationReason())
                .matchKind("")
                .matchedFields(List.of())
                .matchedValue("")
                .build();
    }
    
    private MetadataSearchHit createTableSearchHit(final String database, final TableSearchScope table) {
        return createSearchHit(database, table.schema, SupportedMCPMetadataObjectType.TABLE, table.table.getName(), "", table.table.getName());
    }
    
    private MetadataSearchHit createViewSearchHit(final String database, final TableSearchScope view) {
        return createSearchHit(database, view.schema, SupportedMCPMetadataObjectType.VIEW, "", view.table.getName(), view.table.getName());
    }
    
    private MetadataSearchHit createColumnSearchHit(final String database, final String schema, final String table, final String view, final ShardingSphereColumn column) {
        return createSearchHit(database, schema, SupportedMCPMetadataObjectType.COLUMN, table, view, column.getName());
    }
    
    private MetadataSearchHit createIndexSearchHit(final String database, final String schema, final String table, final ShardingSphereIndex index) {
        return createSearchHit(database, schema, SupportedMCPMetadataObjectType.INDEX, table, "", index.getName());
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class TableSearchScope {
        
        private final String schema;
        
        private final ShardingSphereTable table;
    }
}
