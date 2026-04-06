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
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
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
import java.util.Set;

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
        List<MCPDatabaseMetadata> result = new LinkedList<>();
        for (MCPDatabaseMetadata each : metadataCatalog.getDatabaseMetadataMap().values()) {
            result.add(createDatabaseSummary(each));
        }
        result.sort(Comparator.comparing(MCPDatabaseMetadata::getDatabase));
        return result;
    }
    
    private MCPDatabaseMetadata createDatabaseSummary(final MCPDatabaseMetadata databaseMetadata) {
        return new MCPDatabaseMetadata(databaseMetadata.getDatabase(), databaseMetadata.getDatabaseType(), databaseMetadata.getDatabaseVersion(), Collections.emptyList());
    }
    
    /**
     * Query database.
     *
     * @param databaseName database name
     * @return database metadata
     */
    public Optional<MCPDatabaseMetadata> queryDatabase(final String databaseName) {
        return metadataCatalog.findMetadata(databaseName).map(this::createDatabaseDetail);
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
        return metadataCatalog.findMetadata(databaseName).map(optional -> createSchemaSummaries(optional.getSchemas())).orElse(Collections.emptyList());
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
        return findSchema(databaseName, schemaName).map(this::createSchemaDetail);
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
        return findSchema(databaseName, schemaName).map(optional -> createTableSummaries(optional.getTables())).orElse(Collections.emptyList());
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
        return findTable(databaseName, schemaName, tableName).map(this::createTableDetail);
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
        return findSchema(databaseName, schemaName).map(optional -> createViewSummaries(optional.getViews())).orElse(Collections.emptyList());
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
        return findView(databaseName, schemaName, viewName).map(this::createViewDetail);
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
        assertIndexSupported(databaseName);
        return findTable(databaseName, schemaName, tableName).map(optional -> sortIndexes(optional.getIndexes())).orElse(Collections.emptyList());
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
        assertIndexSupported(databaseName);
        return findIndex(queryIndexes(databaseName, schemaName, tableName), indexName);
    }
    
    /**
     * Judge whether the metadata object type is supported for the database.
     *
     * @param databaseName database name
     * @param objectType metadata object type
     * @return whether supported or not
     */
    public boolean isSupportedMetadataObjectType(final String databaseName, final MetadataObjectType objectType) {
        return getSupportedMetadataObjectTypes(databaseName).contains(objectType);
    }
    
    private Optional<MCPSchemaMetadata> findSchema(final String databaseName, final String schemaName) {
        Optional<MCPDatabaseMetadata> databaseMetadata = metadataCatalog.findMetadata(databaseName);
        if (databaseMetadata.isEmpty()) {
            return Optional.empty();
        }
        for (MCPSchemaMetadata each : databaseMetadata.get().getSchemas()) {
            if (schemaName.equals(each.getSchema())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private Optional<MCPTableMetadata> findTable(final String databaseName, final String schemaName, final String tableName) {
        Optional<MCPSchemaMetadata> schemaMetadata = findSchema(databaseName, schemaName);
        if (schemaMetadata.isEmpty()) {
            return Optional.empty();
        }
        for (MCPTableMetadata each : schemaMetadata.get().getTables()) {
            if (tableName.equals(each.getTable())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private Optional<MCPViewMetadata> findView(final String databaseName, final String schemaName, final String viewName) {
        Optional<MCPSchemaMetadata> schemaMetadata = findSchema(databaseName, schemaName);
        if (schemaMetadata.isEmpty()) {
            return Optional.empty();
        }
        for (MCPViewMetadata each : schemaMetadata.get().getViews()) {
            if (viewName.equals(each.getView())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private Optional<MCPColumnMetadata> findColumn(final Collection<MCPColumnMetadata> columns, final String columnName) {
        for (MCPColumnMetadata each : columns) {
            if (columnName.equals(each.getColumn())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private Optional<MCPIndexMetadata> findIndex(final Collection<MCPIndexMetadata> indexes, final String indexName) {
        for (MCPIndexMetadata each : indexes) {
            if (indexName.equals(each.getIndex())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private MCPDatabaseMetadata createDatabaseDetail(final MCPDatabaseMetadata databaseMetadata) {
        return new MCPDatabaseMetadata(databaseMetadata.getDatabase(), databaseMetadata.getDatabaseType(),
                databaseMetadata.getDatabaseVersion(), createSchemaDetails(databaseMetadata.getSchemas()));
    }
    
    private List<MCPSchemaMetadata> createSchemaSummaries(final Collection<MCPSchemaMetadata> schemas) {
        List<MCPSchemaMetadata> result = new LinkedList<>();
        for (MCPSchemaMetadata each : schemas) {
            result.add(new MCPSchemaMetadata(each.getDatabase(), each.getSchema(), Collections.emptyList(), Collections.emptyList()));
        }
        result.sort(Comparator.comparing(MCPSchemaMetadata::getSchema));
        return result;
    }
    
    private List<MCPSchemaMetadata> createSchemaDetails(final Collection<MCPSchemaMetadata> schemas) {
        List<MCPSchemaMetadata> result = new LinkedList<>();
        for (MCPSchemaMetadata each : schemas) {
            result.add(createSchemaDetail(each));
        }
        result.sort(Comparator.comparing(MCPSchemaMetadata::getSchema));
        return result;
    }
    
    private MCPSchemaMetadata createSchemaDetail(final MCPSchemaMetadata schemaMetadata) {
        return new MCPSchemaMetadata(schemaMetadata.getDatabase(), schemaMetadata.getSchema(), createTableSummaries(schemaMetadata.getTables()), createViewSummaries(schemaMetadata.getViews()));
    }
    
    private List<MCPTableMetadata> createTableSummaries(final Collection<MCPTableMetadata> tables) {
        List<MCPTableMetadata> result = new LinkedList<>();
        for (MCPTableMetadata each : tables) {
            result.add(new MCPTableMetadata(each.getDatabase(), each.getSchema(), each.getTable(), Collections.emptyList(), Collections.emptyList()));
        }
        result.sort(Comparator.comparing(MCPTableMetadata::getTable));
        return result;
    }
    
    private MCPTableMetadata createTableDetail(final MCPTableMetadata tableMetadata) {
        return new MCPTableMetadata(tableMetadata.getDatabase(), tableMetadata.getSchema(), tableMetadata.getTable(),
                sortColumns(tableMetadata.getColumns()), sortIndexes(tableMetadata.getIndexes()));
    }
    
    private List<MCPViewMetadata> createViewSummaries(final Collection<MCPViewMetadata> views) {
        List<MCPViewMetadata> result = new LinkedList<>();
        for (MCPViewMetadata each : views) {
            result.add(new MCPViewMetadata(each.getDatabase(), each.getSchema(), each.getView(), Collections.emptyList()));
        }
        result.sort(Comparator.comparing(MCPViewMetadata::getView));
        return result;
    }
    
    private MCPViewMetadata createViewDetail(final MCPViewMetadata viewMetadata) {
        return new MCPViewMetadata(viewMetadata.getDatabase(), viewMetadata.getSchema(), viewMetadata.getView(), sortColumns(viewMetadata.getColumns()));
    }
    
    private List<MCPColumnMetadata> sortColumns(final Collection<MCPColumnMetadata> columns) {
        List<MCPColumnMetadata> result = new LinkedList<>(columns);
        result.sort(Comparator.comparing(MCPColumnMetadata::getColumn));
        return result;
    }
    
    private List<MCPIndexMetadata> sortIndexes(final Collection<MCPIndexMetadata> indexes) {
        List<MCPIndexMetadata> result = new LinkedList<>(indexes);
        result.sort(Comparator.comparing(MCPIndexMetadata::getIndex));
        return result;
    }
    
    private void assertIndexSupported(final String databaseName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.INDEX)) {
            throw new MCPUnsupportedException("Index resources are not supported for the current database.");
        }
    }
    
    private Set<MetadataObjectType> getSupportedMetadataObjectTypes(final String databaseName) {
        Optional<MCPDatabaseCapability> databaseCapability = new MCPDatabaseCapabilityProvider(metadataCatalog).provide(databaseName);
        return databaseCapability.isPresent() ? databaseCapability.get().getSupportedMetadataObjectTypes() : Collections.emptySet();
    }
}
