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
import org.apache.shardingsphere.mcp.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.context.MCPRequestContext;
import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;
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
public final class MetadataQueryService implements MCPMetadataQueryFacade {
    
    private final MCPRequestContext requestContext;
    
    /**
     * Query databases.
     *
     * @return database metadata
     */
    @Override
    public List<MCPDatabaseMetadata> queryDatabases() {
        return requestContext.getDatabaseCapabilityProvider().getDatabaseProfiles().stream()
                .map(this::createDatabaseSummary).sorted(Comparator.comparing(MCPDatabaseMetadata::getDatabase)).collect(Collectors.toList());
    }
    
    /**
     * Query database.
     *
     * @param databaseName database name
     * @return database metadata
     */
    @Override
    public Optional<MCPDatabaseMetadata> queryDatabase(final String databaseName) {
        return requestContext.getMetadataContext().loadDatabaseMetadata(databaseName).map(MCPDatabaseMetadata::createDetail);
    }
    
    /**
     * Query schemas.
     *
     * @param databaseName database name
     * @return schema metadata
     */
    @Override
    public List<MCPSchemaMetadata> querySchemas(final String databaseName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.SCHEMA)) {
            return Collections.emptyList();
        }
        return requestContext.getMetadataContext().loadDatabaseMetadata(databaseName).map(optional -> optional.getSchemas().stream()
                .map(MCPSchemaMetadata::createSummary).sorted(Comparator.comparing(MCPSchemaMetadata::getSchema)).collect(Collectors.toList())).orElse(Collections.emptyList());
    }
    
    /**
     * Query schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return schema metadata
     */
    @Override
    public Optional<MCPSchemaMetadata> querySchema(final String databaseName, final String schemaName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.SCHEMA)) {
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
    @Override
    public List<MCPTableMetadata> queryTables(final String databaseName, final String schemaName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.TABLE)) {
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
    @Override
    public Optional<MCPTableMetadata> queryTable(final String databaseName, final String schemaName, final String tableName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.TABLE)) {
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
    @Override
    public List<MCPViewMetadata> queryViews(final String databaseName, final String schemaName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.VIEW)) {
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
    @Override
    public Optional<MCPViewMetadata> queryView(final String databaseName, final String schemaName, final String viewName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.VIEW)) {
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
    @Override
    public List<MCPColumnMetadata> queryTableColumns(final String databaseName, final String schemaName, final String tableName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.COLUMN)) {
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
    @Override
    public Optional<MCPColumnMetadata> queryTableColumn(final String databaseName, final String schemaName, final String tableName, final String columnName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.COLUMN)) {
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
    @Override
    public List<MCPColumnMetadata> queryViewColumns(final String databaseName, final String schemaName, final String viewName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.COLUMN)) {
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
    @Override
    public Optional<MCPColumnMetadata> queryViewColumn(final String databaseName, final String schemaName, final String viewName, final String columnName) {
        if (!isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.COLUMN)) {
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
    @Override
    public List<MCPIndexMetadata> queryIndexes(final String databaseName, final String schemaName, final String tableName) {
        ShardingSpherePreconditions.checkState(isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.INDEX),
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
    @Override
    public Optional<MCPIndexMetadata> queryIndex(final String databaseName, final String schemaName, final String tableName, final String indexName) {
        ShardingSpherePreconditions.checkState(isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.INDEX),
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
    @Override
    public List<MCPSequenceMetadata> querySequences(final String databaseName, final String schemaName) {
        ShardingSpherePreconditions.checkState(isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.SEQUENCE),
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
    @Override
    public Optional<MCPSequenceMetadata> querySequence(final String databaseName, final String schemaName, final String sequenceName) {
        ShardingSpherePreconditions.checkState(isSupportedMetadataObjectType(databaseName, SupportedMCPMetadataObjectType.SEQUENCE),
                () -> new MCPUnsupportedException("Sequence resources are not supported for the current database."));
        return findSequence(querySequences(databaseName, schemaName), sequenceName).map(MCPSequenceMetadata::createDetail);
    }
    
    private Optional<MCPSchemaMetadata> findSchema(final String databaseName, final String schemaName) {
        return requestContext.getMetadataContext().loadDatabaseMetadata(databaseName)
                .flatMap(optional -> optional.getSchemas().stream().filter(each -> schemaName.equals(each.getSchema())).findFirst());
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
    @Override
    public boolean isSupportedMetadataObjectType(final String databaseName, final SupportedMCPMetadataObjectType objectType) {
        Optional<MCPDatabaseCapability> databaseCapability = requestContext.getDatabaseCapabilityProvider().provide(databaseName);
        return databaseCapability.isPresent() && databaseCapability.get().getSupportedMetadataObjectTypes().contains(objectType);
    }
    
    private MCPDatabaseMetadata createDatabaseSummary(final RuntimeDatabaseProfile databaseProfile) {
        return new MCPDatabaseMetadata(databaseProfile.getDatabase(), databaseProfile.getDatabaseType(), databaseProfile.getDatabaseVersion(), Collections.emptyList());
    }
}
