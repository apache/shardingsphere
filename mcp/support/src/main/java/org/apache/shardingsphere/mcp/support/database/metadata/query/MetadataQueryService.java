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

package org.apache.shardingsphere.mcp.support.database.metadata.query;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSequence;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.metadata.context.RequestScopedMetadataContext;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Metadata query service.
 */
@RequiredArgsConstructor
public final class MetadataQueryService implements MCPMetadataQueryFacade {
    
    private final MCPDatabaseCapabilityProvider databaseCapabilityProvider;
    
    private final RequestScopedMetadataContext metadataContext;
    
    @Override
    public List<RuntimeDatabaseProfile> queryDatabases() {
        return databaseCapabilityProvider.getDatabaseProfiles().stream().sorted(Comparator.comparing(RuntimeDatabaseProfile::getDatabase)).toList();
    }
    
    @Override
    public Optional<RuntimeDatabaseProfile> queryDatabase(final String databaseName) {
        return databaseCapabilityProvider.findDatabaseProfile(databaseName);
    }
    
    @Override
    public List<ShardingSphereSchema> querySchemas(final String databaseName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.SCHEMA)) {
            return Collections.emptyList();
        }
        return metadataContext.loadSchemas(databaseName)
                .map(optional -> optional.stream().sorted(Comparator.comparing(ShardingSphereSchema::getName)).toList()).orElse(Collections.emptyList());
    }
    
    @Override
    public Optional<ShardingSphereSchema> querySchema(final String databaseName, final String schemaName) {
        return isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.SCHEMA) ? findSchema(databaseName, schemaName) : Optional.empty();
    }
    
    @Override
    public List<ShardingSphereTable> queryTables(final String databaseName, final String schemaName) {
        return queryTables(databaseName, schemaName, TableType.TABLE, SupportedMCPMetadataObjectType.TABLE);
    }
    
    private List<ShardingSphereTable> queryTables(final String databaseName, final String schemaName, final TableType type, final SupportedMCPMetadataObjectType objectType) {
        if (!isSupportedMetadataObjectType(databaseName, objectType)) {
            return Collections.emptyList();
        }
        return findSchema(databaseName, schemaName)
                .map(optional -> optional.getAllTables().stream().filter(each -> type == each.getType()).sorted(Comparator.comparing(ShardingSphereTable::getName)).toList())
                .orElse(Collections.emptyList());
    }
    
    @Override
    public Optional<ShardingSphereTable> queryTable(final String databaseName, final String schemaName, final String tableName) {
        return findTable(queryTables(databaseName, schemaName), tableName);
    }
    
    @Override
    public List<ShardingSphereTable> queryViews(final String databaseName, final String schemaName) {
        return queryTables(databaseName, schemaName, TableType.VIEW, SupportedMCPMetadataObjectType.VIEW);
    }
    
    @Override
    public Optional<ShardingSphereTable> queryView(final String databaseName, final String schemaName, final String viewName) {
        return findTable(queryViews(databaseName, schemaName), viewName);
    }
    
    @Override
    public List<MCPColumnMetadata> queryTableColumns(final String databaseName, final String schemaName, final String tableName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.COLUMN)) {
            return Collections.emptyList();
        }
        return queryTable(databaseName, schemaName, tableName).isEmpty()
                ? Collections.emptyList()
                : metadataContext.loadColumns(databaseName, schemaName, tableName).map(this::sortColumns).orElse(Collections.emptyList());
    }
    
    @Override
    public Optional<MCPColumnMetadata> queryTableColumn(final String databaseName, final String schemaName, final String tableName, final String columnName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.COLUMN)) {
            return Optional.empty();
        }
        return findColumn(queryTableColumns(databaseName, schemaName, tableName), columnName);
    }
    
    @Override
    public List<MCPColumnMetadata> queryViewColumns(final String databaseName, final String schemaName, final String viewName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.COLUMN)) {
            return Collections.emptyList();
        }
        return queryView(databaseName, schemaName, viewName).isEmpty()
                ? Collections.emptyList()
                : metadataContext.loadColumns(databaseName, schemaName, viewName).map(this::sortColumns).orElse(Collections.emptyList());
    }
    
    @Override
    public Optional<MCPColumnMetadata> queryViewColumn(final String databaseName, final String schemaName, final String viewName, final String columnName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.COLUMN)) {
            return Optional.empty();
        }
        return findColumn(queryViewColumns(databaseName, schemaName, viewName), columnName);
    }
    
    @Override
    public List<MCPColumnMetadata> querySchemaColumns(final String databaseName, final String schemaName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.COLUMN) || querySchema(databaseName, schemaName).isEmpty()) {
            return Collections.emptyList();
        }
        return metadataContext.loadSchemaColumns(databaseName, schemaName).map(this::sortColumns).orElse(Collections.emptyList());
    }
    
    private List<MCPColumnMetadata> sortColumns(final Collection<MCPColumnMetadata> columns) {
        return columns.stream().sorted(Comparator.comparing(MCPColumnMetadata::getRelationName)
                .thenComparing(MCPColumnMetadata::getName)).toList();
    }
    
    @Override
    public List<ShardingSphereIndex> queryIndexes(final String databaseName, final String schemaName, final String tableName) {
        ShardingSpherePreconditions.checkState(isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.INDEX),
                () -> new MCPUnsupportedException("Index resources are not supported for the current database."));
        return queryTable(databaseName, schemaName, tableName).isEmpty()
                ? Collections.emptyList()
                : metadataContext.loadIndexes(databaseName, schemaName, tableName).map(this::sortIndexes).orElse(Collections.emptyList());
    }
    
    @Override
    public Optional<ShardingSphereIndex> queryIndex(final String databaseName, final String schemaName, final String tableName, final String indexName) {
        ShardingSpherePreconditions.checkState(isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.INDEX),
                () -> new MCPUnsupportedException("Index resources are not supported for the current database."));
        return findIndex(queryIndexes(databaseName, schemaName, tableName), indexName);
    }
    
    private List<ShardingSphereIndex> sortIndexes(final Collection<ShardingSphereIndex> indexes) {
        return indexes.stream().sorted(Comparator.comparing(ShardingSphereIndex::getName)).toList();
    }
    
    @Override
    public List<ShardingSphereSequence> querySequences(final String databaseName, final String schemaName) {
        ShardingSpherePreconditions.checkState(isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.SEQUENCE),
                () -> new MCPUnsupportedException("Sequence resources are not supported for the current database."));
        return findSchema(databaseName, schemaName).map(optional -> sortSequences(optional.getAllSequences())).orElse(Collections.emptyList());
    }
    
    @Override
    public Optional<ShardingSphereSequence> querySequence(final String databaseName, final String schemaName, final String sequenceName) {
        ShardingSpherePreconditions.checkState(isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.SEQUENCE),
                () -> new MCPUnsupportedException("Sequence resources are not supported for the current database."));
        return findSequence(querySequences(databaseName, schemaName), sequenceName);
    }
    
    private List<ShardingSphereSequence> sortSequences(final Collection<ShardingSphereSequence> sequences) {
        return sequences.stream().sorted(Comparator.comparing(ShardingSphereSequence::getName)).toList();
    }
    
    private Optional<ShardingSphereSchema> findSchema(final String databaseName, final String schemaName) {
        return metadataContext.loadSchemas(databaseName).flatMap(optional -> optional.stream().filter(each -> schemaName.equals(each.getName())).findFirst());
    }
    
    private Optional<ShardingSphereTable> findTable(final Collection<ShardingSphereTable> tables, final String tableName) {
        return tables.stream().filter(each -> tableName.equals(each.getName())).findFirst();
    }
    
    private Optional<MCPColumnMetadata> findColumn(final Collection<MCPColumnMetadata> columns, final String columnName) {
        return columns.stream().filter(each -> columnName.equals(each.getName())).findFirst();
    }
    
    private Optional<ShardingSphereIndex> findIndex(final Collection<ShardingSphereIndex> indexes, final String indexName) {
        return indexes.stream().filter(each -> indexName.equals(each.getName())).findFirst();
    }
    
    private Optional<ShardingSphereSequence> findSequence(final Collection<ShardingSphereSequence> sequences, final String sequenceName) {
        return sequences.stream().filter(each -> sequenceName.equals(each.getName())).findFirst();
    }
    
    @Override
    public boolean isSupportedMetadataObjectType(final String databaseName, final SupportedMCPMetadataObjectType objectType) {
        Optional<MCPDatabaseCapability> databaseCapability = databaseCapabilityProvider.provide(databaseName);
        return databaseCapability.isPresent() && databaseCapability.get().getSupportedMetadataObjectTypes().contains(objectType);
    }
}
