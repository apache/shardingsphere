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
import org.apache.shardingsphere.mcp.core.metadata.GovernanceMetadataQueryService;
import org.apache.shardingsphere.mcp.core.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.core.tool.response.MetadataSearchHit;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPViewMetadata;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;

import java.util.LinkedList;
import java.util.List;
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
            if (SupportedMCPMetadataObjectType.DATABASE == each) {
                metadataQueryFacade.queryDatabase(databaseName).ifPresent(optional -> result.add(createSearchHit(optional)));
                continue;
            }
            if (SupportedMCPMetadataObjectType.STORAGE_UNIT == each) {
                if (null != queryFacade) {
                    result.addAll(queryStorageUnitSearchHits(databaseName));
                }
                continue;
            }
            if (metadataQueryFacade.isSupportedMetadataObjectType(databaseName, each)) {
                result.addAll(querySearchHits(databaseName, each, schemaName));
            }
        }
        return result;
    }
    
    List<MetadataSearchHit> collectDatabases() {
        return metadataQueryFacade.queryDatabases().stream().map(this::createSearchHit).toList();
    }
    
    private List<MetadataSearchHit> querySearchHits(final String databaseName, final SupportedMCPMetadataObjectType objectType, final String schemaName) {
        List<MetadataSearchHit> result = new LinkedList<>();
        if (SupportedMCPMetadataObjectType.SCHEMA == objectType) {
            addSchemaSearchHits(result, databaseName, schemaName);
        } else if (SupportedMCPMetadataObjectType.TABLE == objectType) {
            queryTables(databaseName, schemaName).forEach(each -> result.add(createSearchHit(each)));
        } else if (SupportedMCPMetadataObjectType.VIEW == objectType) {
            queryViews(databaseName, schemaName).forEach(each -> result.add(createSearchHit(each)));
        } else if (SupportedMCPMetadataObjectType.COLUMN == objectType) {
            result.addAll(queryColumnSearchHits(databaseName, schemaName));
        } else if (SupportedMCPMetadataObjectType.INDEX == objectType) {
            result.addAll(queryIndexSearchHits(databaseName, schemaName));
        } else {
            result.addAll(querySequenceSearchHits(databaseName, schemaName));
        }
        return result;
    }
    
    private void addSchemaSearchHits(final List<MetadataSearchHit> searchHits, final String databaseName, final String schemaName) {
        if (!schemaName.isEmpty()) {
            metadataQueryFacade.querySchema(databaseName, schemaName).ifPresent(optional -> searchHits.add(createSearchHit(optional)));
            return;
        }
        for (MCPSchemaMetadata each : metadataQueryFacade.querySchemas(databaseName)) {
            searchHits.add(createSearchHit(each));
        }
    }
    
    private List<MCPTableMetadata> queryTables(final String databaseName, final String schemaName) {
        if (!schemaName.isEmpty()) {
            return metadataQueryFacade.queryTables(databaseName, schemaName);
        }
        return metadataQueryFacade.querySchemas(databaseName).stream().flatMap(each -> metadataQueryFacade.queryTables(databaseName, each.getSchema()).stream()).toList();
    }
    
    private List<MCPViewMetadata> queryViews(final String databaseName, final String schemaName) {
        List<MCPViewMetadata> result = new LinkedList<>();
        if (!schemaName.isEmpty()) {
            result.addAll(metadataQueryFacade.queryViews(databaseName, schemaName));
            return result;
        }
        for (MCPSchemaMetadata each : metadataQueryFacade.querySchemas(databaseName)) {
            result.addAll(metadataQueryFacade.queryViews(databaseName, each.getSchema()));
        }
        return result;
    }
    
    private List<MetadataSearchHit> queryColumnSearchHits(final String databaseName, final String schemaName) {
        List<MetadataSearchHit> result = new LinkedList<>();
        for (MCPTableMetadata each : queryTables(databaseName, schemaName)) {
            for (MCPColumnMetadata column : metadataQueryFacade.queryTableColumns(databaseName, each.getSchema(), each.getTable())) {
                result.add(createSearchHit(column));
            }
        }
        for (MCPViewMetadata each : queryViews(databaseName, schemaName)) {
            for (MCPColumnMetadata column : metadataQueryFacade.queryViewColumns(databaseName, each.getSchema(), each.getView())) {
                result.add(createSearchHit(column));
            }
        }
        return result;
    }
    
    private List<MetadataSearchHit> queryIndexSearchHits(final String databaseName, final String schemaName) {
        return queryTables(databaseName, schemaName).stream()
                .flatMap(each -> metadataQueryFacade.queryIndexes(databaseName, each.getSchema(), each.getTable()).stream()).map(this::createSearchHit).toList();
    }
    
    private List<MetadataSearchHit> querySequenceSearchHits(final String databaseName, final String schemaName) {
        if (!schemaName.isEmpty()) {
            return metadataQueryFacade.querySequences(databaseName, schemaName).stream().map(this::createSearchHit).toList();
        }
        return metadataQueryFacade.querySchemas(databaseName).stream()
                .flatMap(each -> metadataQueryFacade.querySequences(databaseName, each.getSchema()).stream()).map(this::createSearchHit).toList();
    }
    
    private List<MetadataSearchHit> queryStorageUnitSearchHits(final String databaseName) {
        List<MetadataSearchHit> result = new LinkedList<>();
        for (Map<String, Object> each : governanceMetadataQueryService.queryStorageUnits(queryFacade, databaseName)) {
            String storageUnitName = Objects.toString(each.get("name"), "");
            if (!storageUnitName.isEmpty()) {
                result.add(createSearchHit(databaseName, "", "storage_unit", "", "", storageUnitName));
            }
        }
        return result;
    }
    
    private MetadataSearchHit createSearchHit(final MCPDatabaseMetadata databaseMetadata) {
        return createSearchHit(databaseMetadata.getDatabase(), "", "database", "", "", databaseMetadata.getDatabase());
    }
    
    private MetadataSearchHit createSearchHit(final MCPSchemaMetadata schemaMetadata) {
        return createSearchHit(schemaMetadata.getDatabase(), schemaMetadata.getSchema(), "schema", "", "", schemaMetadata.getSchema());
    }
    
    private MetadataSearchHit createSearchHit(final MCPTableMetadata tableMetadata) {
        return createSearchHit(tableMetadata.getDatabase(), tableMetadata.getSchema(), "table", tableMetadata.getTable(), "", tableMetadata.getTable());
    }
    
    private MetadataSearchHit createSearchHit(final MCPViewMetadata viewMetadata) {
        return createSearchHit(viewMetadata.getDatabase(), viewMetadata.getSchema(), "view", "", viewMetadata.getView(), viewMetadata.getView());
    }
    
    private MetadataSearchHit createSearchHit(final MCPColumnMetadata columnMetadata) {
        return createSearchHit(columnMetadata.getDatabase(), columnMetadata.getSchema(), "column", columnMetadata.getTable(), columnMetadata.getView(), columnMetadata.getColumn());
    }
    
    private MetadataSearchHit createSearchHit(final MCPIndexMetadata indexMetadata) {
        return createSearchHit(indexMetadata.getDatabase(), indexMetadata.getSchema(), "index", indexMetadata.getTable(), "", indexMetadata.getIndex());
    }
    
    private MetadataSearchHit createSearchHit(final MCPSequenceMetadata sequenceMetadata) {
        return createSearchHit(sequenceMetadata.getDatabase(), sequenceMetadata.getSchema(), "sequence", "", "", sequenceMetadata.getSequence());
    }
    
    private MetadataSearchHit createSearchHit(final String database, final String schema, final String objectType, final String table, final String view, final String name) {
        MetadataSearchResourceUriFactory.MetadataResourceUris resourceUris = resourceUriFactory.create(database, schema, objectType, table, view, name);
        return new MetadataSearchHit(database, schema, objectType, table, view, name, resourceUris.resource(), resourceUris.parentResource(), resourceUris.nextResources(),
                resourceUris.derivationStatus(), resourceUris.derivationReason(), "", List.of(), "");
    }
}
