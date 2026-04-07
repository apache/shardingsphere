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

package org.apache.shardingsphere.mcp.metadata.query;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Metadata query service.
 */
@RequiredArgsConstructor
public final class MetadataQueryService {
    
    private final MCPDatabaseMetadataCatalog metadataCatalog;
    
    /**
     * Query databases.
     *
     * @return database metadata
     */
    public List<MCPDatabaseMetadata> queryDatabases() {
        return metadataCatalog.getDatabaseMetadataMap().values().stream()
                .map(MCPDatabaseMetadata::createSummary).sorted(Comparator.comparing(MCPDatabaseMetadata::getDatabase)).collect(Collectors.toList());
    }
    
    /**
     * Query database.
     *
     * @param databaseName database name
     * @return database metadata
     */
    public Optional<MCPDatabaseMetadata> queryDatabase(final String databaseName) {
        return metadataCatalog.findMetadata(databaseName).map(MCPDatabaseMetadata::createDetail);
    }
    
    /**
     * Query schemas.
     *
     * @param databaseName database name
     * @return schema metadata
     */
    public List<MCPSchemaMetadata> querySchemas(final String databaseName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.SCHEMA)) {
            return Collections.emptyList();
        }
        return metadataCatalog.findMetadata(databaseName).map(optional -> optional.getSchemas().stream()
                .map(MCPSchemaMetadata::createSummary).sorted(Comparator.comparing(MCPSchemaMetadata::getSchema)).collect(Collectors.toList())).orElse(Collections.emptyList());
    }
    
    /**
     * Query schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return schema metadata
     */
    public Optional<MCPSchemaMetadata> querySchema(final String databaseName, final String schemaName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.SCHEMA)) {
            return Optional.empty();
        }
        return findSchema(databaseName, schemaName).map(MCPSchemaMetadata::createDetail);
    }
    
    /**
     * Query tables.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return table metadata
     */
    public List<MCPTableMetadata> queryTables(final String databaseName, final String schemaName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.TABLE)) {
            return Collections.emptyList();
        }
        return findSchema(databaseName, schemaName).map(optional -> optional.getTables().stream()
                .map(MCPTableMetadata::createSummary)
                .sorted(Comparator.comparing(MCPTableMetadata::getTable))
                .collect(Collectors.toList())).orElse(Collections.emptyList());
    }
    
    /**
     * Query table.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return table metadata
     */
    public Optional<MCPTableMetadata> queryTable(final String databaseName, final String schemaName, final String tableName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.TABLE)) {
            return Optional.empty();
        }
        return findTable(databaseName, schemaName, tableName).map(MCPTableMetadata::createDetail);
    }
    
    /**
     * Query views.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return view metadata
     */
    public List<MCPViewMetadata> queryViews(final String databaseName, final String schemaName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.VIEW)) {
            return Collections.emptyList();
        }
        return findSchema(databaseName, schemaName).map(optional -> optional.getViews().stream()
                .map(MCPViewMetadata::createSummary)
                .sorted(Comparator.comparing(MCPViewMetadata::getView))
                .collect(Collectors.toList())).orElse(Collections.emptyList());
    }
    
    /**
     * Query view.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return view metadata
     */
    public Optional<MCPViewMetadata> queryView(final String databaseName, final String schemaName, final String viewName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.VIEW)) {
            return Optional.empty();
        }
        return findView(databaseName, schemaName, viewName).map(MCPViewMetadata::createDetail);
    }
    
    /**
     * Query table columns.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return column metadata
     */
    public List<MCPColumnMetadata> queryTableColumns(final String databaseName, final String schemaName, final String tableName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.COLUMN)) {
            return Collections.emptyList();
        }
        return findTable(databaseName, schemaName, tableName).map(optional -> sortColumns(optional.getColumns())).orElse(Collections.emptyList());
    }
    
    private List<MCPColumnMetadata> sortColumns(final Collection<MCPColumnMetadata> columns) {
        List<MCPColumnMetadata> result = new LinkedList<>(columns);
        result.sort(Comparator.comparing(MCPColumnMetadata::getColumn));
        return result;
    }
    
    /**
     * Query table column.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param columnName column name
     * @return column metadata
     */
    public Optional<MCPColumnMetadata> queryTableColumn(final String databaseName, final String schemaName, final String tableName, final String columnName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.COLUMN)) {
            return Optional.empty();
        }
        return findColumn(queryTableColumns(databaseName, schemaName, tableName), columnName);
    }
    
    /**
     * Query view columns.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return column metadata
     */
    public List<MCPColumnMetadata> queryViewColumns(final String databaseName, final String schemaName, final String viewName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.COLUMN)) {
            return Collections.emptyList();
        }
        return findView(databaseName, schemaName, viewName).map(optional -> sortColumns(optional.getColumns())).orElse(Collections.emptyList());
    }
    
    /**
     * Query view column.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @param columnName column name
     * @return column metadata
     */
    public Optional<MCPColumnMetadata> queryViewColumn(final String databaseName, final String schemaName, final String viewName, final String columnName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.COLUMN)) {
            return Optional.empty();
        }
        return findColumn(queryViewColumns(databaseName, schemaName, viewName), columnName);
    }
    
    /**
     * Query indexes.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return index metadata
     */
    public List<MCPIndexMetadata> queryIndexes(final String databaseName, final String schemaName, final String tableName) {
        ShardingSpherePreconditions.checkState(isSupportedMetadataObjectType(databaseName, MetadataObjectType.INDEX),
                () -> new MCPUnsupportedException("Index resources are not supported for the current database."));
        return findTable(databaseName, schemaName, tableName).map(optional -> sortIndexes(optional.getIndexes())).orElse(Collections.emptyList());
    }
    
    private List<MCPIndexMetadata> sortIndexes(final Collection<MCPIndexMetadata> indexes) {
        List<MCPIndexMetadata> result = new LinkedList<>(indexes);
        result.sort(Comparator.comparing(MCPIndexMetadata::getIndex));
        return result;
    }
    
    /**
     * Query index.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param indexName index name
     * @return index metadata
     */
    public Optional<MCPIndexMetadata> queryIndex(final String databaseName, final String schemaName, final String tableName, final String indexName) {
        ShardingSpherePreconditions.checkState(isSupportedMetadataObjectType(databaseName, MetadataObjectType.INDEX),
                () -> new MCPUnsupportedException("Index resources are not supported for the current database."));
        return findIndex(queryIndexes(databaseName, schemaName, tableName), indexName);
    }
    
    /**
     * Query sequences.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return sequence metadata
     */
    public List<MCPSequenceMetadata> querySequences(final String databaseName, final String schemaName) {
        ShardingSpherePreconditions.checkState(isSupportedMetadataObjectType(databaseName, MetadataObjectType.SEQUENCE),
                () -> new MCPUnsupportedException("Sequence resources are not supported for the current database."));
        return findSchema(databaseName, schemaName).map(optional -> sortSequences(optional.getSequences())).orElse(Collections.emptyList());
    }
    
    private List<MCPSequenceMetadata> sortSequences(final Collection<MCPSequenceMetadata> sequences) {
        List<MCPSequenceMetadata> result = new LinkedList<>(sequences);
        result.sort(Comparator.comparing(MCPSequenceMetadata::getSequence));
        return result;
    }
    
    /**
     * Query sequence.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param sequenceName sequence name
     * @return sequence metadata
     */
    public Optional<MCPSequenceMetadata> querySequence(final String databaseName, final String schemaName, final String sequenceName) {
        ShardingSpherePreconditions.checkState(isSupportedMetadataObjectType(databaseName, MetadataObjectType.SEQUENCE),
                () -> new MCPUnsupportedException("Sequence resources are not supported for the current database."));
        return findSequence(querySequences(databaseName, schemaName), sequenceName).map(MCPSequenceMetadata::createDetail);
    }
    
    private Optional<MCPSchemaMetadata> findSchema(final String databaseName, final String schemaName) {
        return metadataCatalog.findMetadata(databaseName).flatMap(optional -> optional.getSchemas().stream().filter(each -> schemaName.equals(each.getSchema())).findFirst());
    }
    
    private Optional<MCPTableMetadata> findTable(final String databaseName, final String schemaName, final String tableName) {
        return findSchema(databaseName, schemaName).flatMap(optional -> optional.getTables().stream().filter(each -> tableName.equals(each.getTable())).findFirst());
    }
    
    private Optional<MCPViewMetadata> findView(final String databaseName, final String schemaName, final String viewName) {
        return findSchema(databaseName, schemaName).flatMap(optional -> optional.getViews().stream().filter(each -> viewName.equals(each.getView())).findFirst());
    }
    
    private Optional<MCPColumnMetadata> findColumn(final Collection<MCPColumnMetadata> columns, final String columnName) {
        return columns.stream().filter(each -> columnName.equals(each.getColumn())).findFirst();
    }
    
    private Optional<MCPIndexMetadata> findIndex(final Collection<MCPIndexMetadata> indexes, final String indexName) {
        return indexes.stream().filter(each -> indexName.equals(each.getIndex())).findFirst();
    }
    
    private Optional<MCPSequenceMetadata> findSequence(final Collection<MCPSequenceMetadata> sequences, final String sequenceName) {
        return sequences.stream().filter(each -> sequenceName.equals(each.getSequence())).findFirst();
    }
    
    /**
     * Judge whether the metadata object type is supported for the database.
     *
     * @param databaseName database name
     * @param objectType metadata object type
     * @return supported or not
     */
    public boolean isSupportedMetadataObjectType(final String databaseName, final MetadataObjectType objectType) {
        Optional<MCPDatabaseCapability> databaseCapability = new MCPDatabaseCapabilityProvider(metadataCatalog).provide(databaseName);
        return databaseCapability.isPresent() && databaseCapability.get().getSupportedMetadataObjectTypes().contains(objectType);
    }
}
